/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Event;

@Slf4j
public class AmqpSasTokenRenewalHandler extends BaseHandler
{
    AmqpsIotHubConnection amqpsIotHubConnection;
    AmqpsSessionHandler sessionHandler;
    private int retryPeriodMilliseconds = 5000;

    public AmqpSasTokenRenewalHandler(AmqpsIotHubConnection amqpsIotHubConnection, AmqpsSessionHandler sessionHandler)
    {
        this.amqpsIotHubConnection = amqpsIotHubConnection;
        this.sessionHandler = sessionHandler;
    }

    @Override
    public void onTimerTask(Event event)
    {
        try
        {
            this.log.trace("AmqpSasTokenRenewalHandler OnTimerTask called, sending authentication message");
            amqpsIotHubConnection.authenticate(sessionHandler);

            //schedule next renewal to take place at some recommended percentage before the latest token expires
            int millisecondDelayUntilNextAuthentication = this.sessionHandler.getDeviceClientConfig().getSasTokenAuthentication().getMillisecondsBeforeProactiveRenewal();
            event.getReactor().schedule(millisecondDelayUntilNextAuthentication, this);
        }
        catch (TransportException e)
        {
            if (e.isRetryable())
            {
                this.log.warn("Failed to send authentication message, trying again in {} milliseconds", retryPeriodMilliseconds, e);

                // if unable to send authentication message, try again sooner than normal
                event.getReactor().schedule(retryPeriodMilliseconds, this);
            }
            else
            {
                this.log.error("Failed to send authentication message, unable to try again", e);
            }
        }
    }
}
