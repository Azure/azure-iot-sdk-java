package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.reactor.Reactor;

/**
 * This class is responsible for proactively renewing sas tokens for a single device. When multiplexing, there will
 * be one instance of this class per device. It will periodically execute the onTimerTask logic to send a renewed sas token,
 * and then will schedule the next timer task appropriately.
 */
@Slf4j
@AllArgsConstructor
public class AmqpsSasTokenRenewalHandler extends BaseHandler implements AuthenticationMessageCallback
{
    //If the sas token renewal cannot be sent, try again in this many milliseconds
    private static final int RETRY_INTERVAL_MILLISECONDS = 5000;

    AmqpsCbsSessionHandler amqpsCbsSessionHandler;
    AmqpsSessionHandler amqpsSessionHandler;

    @Override
    public void onTimerTask(Event event)
    {
        log.trace("onTimerTask fired for sas token renewal handler for device {}", this.amqpsSessionHandler.getDeviceId());
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

    public void sendAuthenticationMessage(Reactor reactor) throws TransportException
    {
        log.debug("Sending authentication message for device {}", amqpsSessionHandler.getDeviceId());
        amqpsCbsSessionHandler.sendAuthenticationMessage(amqpsSessionHandler.getDeviceClientConfig(), this);

        scheduleRenewal(reactor);
    }

    @Override
    public DeliveryState handleAuthenticationResponseMessage(int status, String description)
    {
        if (status == 200)
        {
            log.debug("CBS message authentication succeeded for device {}", this.amqpsSessionHandler.getDeviceId());
            amqpsSessionHandler.openLinks();
            return Accepted.getInstance();
        }
        else
        {
            this.amqpsCbsSessionHandler.onAuthenticationFailed(IotHubStatusCode.getConnectionStatusException(IotHubStatusCode.getIotHubStatusCode(status), description));
            return Accepted.getInstance();
        }
    }

    private void scheduleRenewal(Reactor reactor)
    {
        int sasTokenRenewalPeriod = this.amqpsSessionHandler.getDeviceClientConfig().getSasTokenAuthentication().getMillisecondsBeforeProactiveRenewal();

        log.trace("Scheduling proactive sas token renewal for device {} in {} milliseconds", this.amqpsSessionHandler.getDeviceId(), sasTokenRenewalPeriod);

        reactor.schedule(sasTokenRenewalPeriod, this);
    }

    private void scheduleRenewalRetry(Reactor reactor)
    {
        reactor.schedule(RETRY_INTERVAL_MILLISECONDS, this);
    }
}
