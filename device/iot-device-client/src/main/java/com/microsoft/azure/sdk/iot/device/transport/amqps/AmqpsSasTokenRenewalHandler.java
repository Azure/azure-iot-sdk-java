package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.transport.TransportException;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Handler;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.Task;

import java.util.Iterator;

/**
 * This class is responsible for proactively renewing sas tokens for a single device. When multiplexing, there will
 * be one instance of this class per device. It will periodically onStatusChanged the onTimerTask logic to send a renewed sas token,
 * and then will schedule the next timer task appropriately.
 */
@Slf4j
class AmqpsSasTokenRenewalHandler extends BaseHandler implements AuthenticationMessageCallback
{
    //If the sas token renewal cannot be sent, try again in this many milliseconds
    private static final int RETRY_INTERVAL_MILLISECONDS = 5000;

    private final AmqpsCbsSessionHandler amqpsCbsSessionHandler;
    final AmqpsSessionHandler amqpsSessionHandler;
    private boolean isClosed;
    private AmqpsSasTokenRenewalHandler nextToAuthenticate;
    private Task scheduledTask;

    public AmqpsSasTokenRenewalHandler(AmqpsCbsSessionHandler amqpsCbsSessionHandler, AmqpsSessionHandler amqpsSessionHandler)
    {
        this.amqpsCbsSessionHandler = amqpsCbsSessionHandler;
        this.amqpsSessionHandler = amqpsSessionHandler;
        this.isClosed = false;
    }

    public void setNextToAuthenticate(AmqpsSasTokenRenewalHandler nextToAuthenticate)
    {
        this.nextToAuthenticate = nextToAuthenticate;
    }

    @Override
    public void onTimerTask(Event event)
    {
        if (this.amqpsSessionHandler != null)
        {
            log.trace("onTimerTask fired for sas token renewal handler for device {}", this.amqpsSessionHandler.getDeviceId());
            if (!isClosed)
            {
                try
                {
                    sendAuthenticationMessage(event.getReactor());
                }
                catch (TransportException e)
                {
                    log.error("Failed to send the CBS authentication message to authenticate device {}, trying to send again in {} milliseconds", this.amqpsSessionHandler.getDeviceId(), RETRY_INTERVAL_MILLISECONDS);
                    scheduleRenewalRetry(event.getReactor());
                }
            }
        }
    }

    public void sendAuthenticationMessage(Reactor reactor) throws TransportException
    {
        if (!isClosed)
        {
            log.debug("Sending authentication message for device {}", amqpsSessionHandler.getDeviceId());
            amqpsCbsSessionHandler.sendAuthenticationMessage(amqpsSessionHandler.getClientConfiguration(), this);

            scheduleRenewal(reactor);
        }
    }

    @Override
    public DeliveryState handleAuthenticationResponseMessage(int status, String description, Reactor reactor)
    {
        try
        {
            if (nextToAuthenticate != null)
            {
                nextToAuthenticate.sendAuthenticationMessage(reactor);
                nextToAuthenticate = null; //only need to chain the next authentication once, so remove this connection
            }
        }
        catch (TransportException e)
        {
            log.error("Failed to send authentication message for device {}", nextToAuthenticate.amqpsSessionHandler.getDeviceId(), e);
        }

        if (status == 200)
        {
            log.debug("CBS message authentication succeeded for device {}", this.amqpsSessionHandler.getDeviceId());
            amqpsSessionHandler.openLinks();
        }
        else
        {
            TransportException exception = IotHubStatusCode.getConnectionStatusException(IotHubStatusCode.getIotHubStatusCode(status), description);
            this.amqpsCbsSessionHandler.onAuthenticationFailed(this.amqpsSessionHandler.getDeviceId(), exception);
        }

        return Accepted.getInstance();
    }

    // Once closed, this handler will stop sending authentication messages for its device. This object may not be re-opened.
    public void close()
    {
        this.isClosed = true;
        clearHandlers();
    }

    // The warning is for how getSasTokenAuthentication() may return null, but this code only executes when our config
    // uses SAS_TOKEN auth, and that is sufficient at confirming that getSasTokenAuthentication() will return a non-null instance
    @SuppressWarnings("ConstantConditions")
    private void scheduleRenewal(Reactor reactor)
    {
        int sasTokenRenewalPeriod = this.amqpsSessionHandler.getClientConfiguration().getSasTokenAuthentication().getMillisecondsBeforeProactiveRenewal();

        log.trace("Scheduling proactive sas token renewal for device {} in {} milliseconds", this.amqpsSessionHandler.getDeviceId(), sasTokenRenewalPeriod);

        this.scheduledTask = reactor.schedule(sasTokenRenewalPeriod, this);
    }

    private void scheduleRenewalRetry(Reactor reactor)
    {
        this.scheduledTask = reactor.schedule(RETRY_INTERVAL_MILLISECONDS, this);
    }

    // Removes any children of this handler (such as LoggingFlowController) and disassociates this handler
    // from the proton reactor. By removing the reference of the proton reactor to this handler, this handler becomes
    // eligible for garbage collection by the JVM. This is important for multiplexed connections where SAS token renewal
    // handlers come and go but the reactor stays alive for a long time.
    private void clearHandlers()
    {
        if (this.scheduledTask != null)
        {
            this.scheduledTask.cancel();
            this.scheduledTask.attachments().clear();
        }

        // an instance of this class shouldn't have any children, but other handlers may be added as this SDK
        // grows and this protects against potential memory leaks
        Iterator<Handler> childrenIterator = this.children();
        while (childrenIterator.hasNext())
        {
            childrenIterator.next();
            childrenIterator.remove();
        }
    }
}
