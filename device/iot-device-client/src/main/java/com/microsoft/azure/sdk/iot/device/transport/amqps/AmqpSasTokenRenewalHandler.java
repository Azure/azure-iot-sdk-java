/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Event;

@Slf4j
public class AmqpSasTokenRenewalHandler extends BaseHandler
{
    AmqpsSessionManager amqpsSessionManager;
    DeviceClientConfig config;
    private int retryPeriodMilliseconds = 5000;

    public AmqpSasTokenRenewalHandler(AmqpsSessionManager amqpsSessionManager, DeviceClientConfig config)
    {
        this.amqpsSessionManager = amqpsSessionManager;
        this.config = config;
    }

    @Override
    public void onTimerTask(Event event)
    {
        //add message to session manager queue
        try
        {
            this.log.trace("AmqpSasTokenRenewalHandler OnTimerTask called, sending authentication message");
            amqpsSessionManager.authenticate();

            //schedule next renewal to take place at some recommended percentage before the latest token expires
            event.getReactor().schedule(this.config.getSasTokenAuthentication().getMillisecondsBeforeProactiveRenewal(), this);
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
