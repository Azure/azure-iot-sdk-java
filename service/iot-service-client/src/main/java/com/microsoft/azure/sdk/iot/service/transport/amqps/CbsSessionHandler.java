// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubExceptionManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
class CbsSessionHandler extends ErrorLoggingBaseHandlerWithCleanup implements AuthenticationMessageCallback, LinkStateCallback
{
    // Token's should be proactively renewed at 85% of the lifespan of the previous token
    private static final double TOKEN_RENEWAL_PERCENT = .85;
    private Session session;
    private CbsSenderLinkHandler cbsSenderLinkHandler;
    private CbsReceiverLinkHandler cbsReceiverLinkHandler;
    private final CbsSessionStateCallback cbsSessionStateCallback;
    private TokenCredential credential;
    private String connectionString;
    private AzureSasCredential sasTokenProvider;
    private boolean senderLinkOpened = false;
    private boolean receiverLinkOpened = false;

    CbsSessionHandler(Session session, CbsSessionStateCallback cbsSessionStateCallback, TokenCredential credential)
    {
        this(session, cbsSessionStateCallback);
        this.credential = credential;
    }

    CbsSessionHandler(Session session, CbsSessionStateCallback cbsSessionStateCallback, AzureSasCredential sasTokenProvider)
    {
        this(session, cbsSessionStateCallback);
        this.sasTokenProvider = sasTokenProvider;
    }

    CbsSessionHandler(Session session, CbsSessionStateCallback cbsSessionStateCallback, String connectionString)
    {
        this(session, cbsSessionStateCallback);
        this.connectionString = connectionString;
    }

    private CbsSessionHandler(Session session, CbsSessionStateCallback cbsSessionStateCallback)
    {
        //All events in this reactor that happened to this session will be handled in this instance (onSessionRemoteOpen, for instance)
        BaseHandler.setHandler(session, this);

        this.session = session;
        this.cbsSessionStateCallback = cbsSessionStateCallback;
        this.session.open();
    }

    @Override
    public void onSessionLocalOpen(Event event)
    {
        this.session = event.getSession();

        Sender cbsSender = this.session.sender(CbsSenderLinkHandler.getCbsTag());

        if (this.credential != null)
        {
            this.cbsSenderLinkHandler = new CbsSenderLinkHandler(cbsSender, this, this.credential);
        }
        else if (this.sasTokenProvider != null)
        {
            this.cbsSenderLinkHandler = new CbsSenderLinkHandler(cbsSender, this, this.sasTokenProvider);
        }
        else
        {
            this.cbsSenderLinkHandler = new CbsSenderLinkHandler(cbsSender, this, this.connectionString);
        }

        Receiver cbsReceiver = this.session.receiver(CbsReceiverLinkHandler.getCbsTag());
        this.cbsReceiverLinkHandler = new CbsReceiverLinkHandler(cbsReceiver, this, this);
    }

    @Override
    public void onSessionRemoteOpen(Event e)
    {
        log.trace("CBS session opened remotely");
    }

    @Override
    public void onSessionLocalClose(Event e)
    {
        log.trace("CBS session closed locally");
        this.session.getConnection().close();

        this.cbsSenderLinkHandler.close();
        this.cbsReceiverLinkHandler.close();
    }

    @Override
    public void onSessionRemoteClose(Event e)
    {
        Session session = e.getSession();
        if (session.getLocalState() == EndpointState.ACTIVE)
        {
            this.close();
        }
    }

    void close()
    {
        log.trace("Closing this CBS session");
        this.session.close();
        this.cbsSenderLinkHandler.close();
        this.cbsReceiverLinkHandler.close();
    }

    @Override
    public DeliveryState handleAuthenticationResponseMessage(int status, String description)
    {
        if (status == 200)
        {
            log.debug("CBS session successfully authenticated");
            this.cbsSessionStateCallback.onAuthenticationSucceeded();
        }
        else
        {
            IotHubException e = IotHubExceptionManager.mapException(status, description);
            log.error("CBS session failed to authenticate", e);
            this.cbsSessionStateCallback.onAuthenticationFailed(e);
            this.session.close(); // should chain to close the connection from logic in ErrorLoggingBaseHandlerWithCleanup
        }

        // always acknowledge the received status message
        return Accepted.getInstance();
    }

    @Override
    public void onSenderLinkRemoteOpen()
    {
        this.senderLinkOpened = true;

        if (this.receiverLinkOpened)
        {
            authenticate();
        }
    }

    @Override
    public void onReceiverLinkRemoteOpen()
    {
        this.receiverLinkOpened = true;

        if (this.senderLinkOpened)
        {
            authenticate();
        }
    }

    @Override
    public void onTimerTask(Event event)
    {
        // Timer task is executed periodically to send a proactively renewed authentication token in order to keep the connection alive
        log.debug("Proactively renewing AMQPS connection by sending a new authentication message");
        authenticate();
    }

    boolean isOpen()
    {
        return this.session != null
            && this.session.getLocalState() == EndpointState.ACTIVE
            && this.session.getRemoteState() == EndpointState.ACTIVE
            && this.cbsSenderLinkHandler != null
            && this.cbsSenderLinkHandler.isOpen();
    }

    private void authenticate()
    {
        UUID authenticationMessageCorrelationId = UUID.randomUUID();

        this.cbsReceiverLinkHandler.setAuthenticationMessageCorrelationId(authenticationMessageCorrelationId);
        int authenticationMessageDeliveryTag = this.cbsSenderLinkHandler.sendAuthenticationMessage(authenticationMessageCorrelationId);
        AccessToken currentAccessToken = this.cbsSenderLinkHandler.getCurrentAccessToken();

        if (authenticationMessageDeliveryTag == -1)
        {
            log.error("Failed to send authentication message");
        }
        else
        {
            log.debug("Successfully sent authentication message");
        }

        // delivery tags are used to map acknowledgements from service to messages sent by client.
        // in this CBS session's case, only one message should ever be sent, so as long
        // as the delivery tag is not -1 (proton's failure case) then it is safe to ignore

        // Connection only proactively renews when a token provider is present
        if (this.credential != null || this.sasTokenProvider != null)
        {
            // Each execution of onTimerTask is responsible for scheduling the next occurrence based on how long the previous token is valid for
            OffsetDateTime currentOffsetDateTime = OffsetDateTime.now();
            OffsetDateTime tokenExpiryOffsetDateTime = currentAccessToken.getExpiresAt();
            Duration diff = Duration.between(tokenExpiryOffsetDateTime, currentOffsetDateTime).abs();
            long millisecondsToTokenExpiry = diff.toMillis();

            // Cast of double to int here is safe because this value does not need to be precisely 85% of the token renewal time
            // so it is okay to truncate this double to its int value
            double proactiveTokenRenewalMillis = (millisecondsToTokenExpiry * TOKEN_RENEWAL_PERCENT);

            if (proactiveTokenRenewalMillis >= Integer.MAX_VALUE)
            {
                // To avoid overflow issues, don't try to schedule any further in the future than Integer.MAX_VALUE
                scheduleProactiveRenewal(Integer.MAX_VALUE);
            }
            else
            {
                // Safe cast since we don't need to preserve the precision of the double, we just need to be at roughly 85% of
                // the token's lifespan
                scheduleProactiveRenewal((int) (proactiveTokenRenewalMillis));
            }
        }
    }

    private void scheduleProactiveRenewal(int millisecondsBeforeRenewal)
    {
        log.debug("Scheduling proactive token renewal for {} milliseconds in the future", millisecondsBeforeRenewal);
        this.session.getConnection().getReactor().schedule(millisecondsBeforeRenewal, this);
    }
}
