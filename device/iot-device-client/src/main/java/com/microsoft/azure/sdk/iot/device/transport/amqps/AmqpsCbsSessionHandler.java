/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.*;

import java.util.UUID;

/**
 * This class defines the special CBS session that owns a CBS sender and receiver link. This session is responsible for
 * sending authentication messages on behalf of all device sessions in this connection. Even when multiplexing, there
 * is only one CBS session.
 */
@Slf4j
public class AmqpsCbsSessionHandler extends BaseHandler implements AmqpsLinkStateCallback
{
    private Session session;
    private AmqpsCbsSenderLinkHandler cbsSenderLinkHandler;
    private AmqpsCbsReceiverLinkHandler cbsReceiverLinkHandler;
    private AmqpsSessionStateCallback connectionStateCallback;
    private boolean senderOpened;
    private boolean receiverOpened;

    AmqpsCbsSessionHandler(Session session, AmqpsSessionStateCallback connectionStateCallback)
    {
        this.session = session;
        this.connectionStateCallback = connectionStateCallback;
        this.senderOpened = false;
        this.receiverOpened = false;

        //All events in this reactor that happened to this session will be handled in this instance (onSessionRemoteOpen, for instance)
        BaseHandler.setHandler(this.session, this);

        this.session.open();
    }

    @Override
    public void onSessionLocalOpen(Event event)
    {
        log.debug("Opening Cbs session handler");
        this.session = event.getSession();

        Sender cbsSender = this.session.sender(AmqpsCbsSenderLinkHandler.getCbsTag());
        Receiver cbsReceiver = this.session.receiver(AmqpsCbsReceiverLinkHandler.getCbsTag());

        this.cbsSenderLinkHandler = new AmqpsCbsSenderLinkHandler(cbsSender, this);
        this.cbsReceiverLinkHandler = new AmqpsCbsReceiverLinkHandler(cbsReceiver, this);
    }

    @Override
    public void onSessionRemoteOpen(Event e)
    {
        log.trace("CBS session opened remotely");
    }

    @Override
    public void onSessionLocalClose(Event e)
    {
        log.trace("CBS session closed remotely");
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
            //Service initiated this session close
            log.debug("Amqp CBS session closed remotely unexpectedly");
            this.connectionStateCallback.onSessionClosedUnexpectedly(session.getRemoteCondition());

            this.session.close();
        }
        else
        {
            log.trace("Amqp CBS session closed remotely as expected");
        }
    }

    public void sendAuthenticationMessage(DeviceClientConfig deviceClientConfig, AuthenticationMessageCallback authenticationMessageCallback) throws TransportException
    {
        //Sender link attaches a correlation id to the authentication message and returns it here.
        UUID correlationId = this.cbsSenderLinkHandler.sendAuthenticationMessage(deviceClientConfig);

        //Receiver link will get a delivery with the same correlation id containing the authentication status at some point.
        this.cbsReceiverLinkHandler.addAuthenticationMessageCorrelation(correlationId, authenticationMessageCallback);
    }

    @Override
    public void onLinkOpened(BaseHandler linkHandler)
    {
        if (linkHandler instanceof AmqpsSenderLinkHandler)
        {
            senderOpened = true;
        }
        else if (linkHandler instanceof AmqpsReceiverLinkHandler)
        {
            receiverOpened = true;
        }

        // Once the cbs links are both open, notify the connection layer to send the authentication messages over them
        // for each device session
        if (senderOpened && receiverOpened)
        {
            log.trace("CBS session opened successfully, notifying connection layer to start sending authentication messages");
            this.connectionStateCallback.onAuthenticationSessionOpened();
        }
    }

    @Override
    public void onMessageAcknowledged(Message message, int deliveryTag)
    {
        // Do nothing. Users of this SDK don't care about this ack, and the SDK doesn't open any links or sessions
        // upon receiving this ack. The CBS receiver link receives a message with the actual status of the authentication.
    }

    @Override
    public void onMessageReceived(IotHubTransportMessage message)
    {
        this.connectionStateCallback.onMessageReceived(message);
    }

    @Override
    public void onLinkClosedUnexpectedly(ErrorCondition errorCondition)
    {
        log.trace("CBS link closed unexpectedly, closing the CBS session");
        this.session.close();
        this.connectionStateCallback.onSessionClosedUnexpectedly(errorCondition);
    }

    public void onAuthenticationFailed(TransportException transportException)
    {
        this.connectionStateCallback.onAuthenticationFailed(transportException);
    }

    public void close()
    {
        log.trace("Closing this CBS session");
        this.session.close();
    }
}
