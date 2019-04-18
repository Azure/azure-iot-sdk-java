/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.CustomLogger;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Event;

public class SasTokenRenewalHandler extends BaseHandler
{
    AmqpsSessionManager amqpsSessionManager;
    CustomLogger logger;
    DeviceClientConfig config;

    public SasTokenRenewalHandler(AmqpsSessionManager amqpsSessionManager, DeviceClientConfig config)
    {
        this.amqpsSessionManager = amqpsSessionManager;
        this.logger = new CustomLogger(this.getClass());
        this.config = config;
    }

    @Override
    public void onTimerTask(Event event)
    {
        //add message to session manager queue
        try
        {
            amqpsSessionManager.authenticate();
        }
        catch (TransportException e)
        {
            this.logger.LogError(e);
        }

        //schedule next renewal to take place at some recommended percentage before the latest token expires
        event.getReactor().schedule(this.config.getSasTokenAuthentication().getMillisecondsBeforeProactiveRenewal(), this);
    }
}
