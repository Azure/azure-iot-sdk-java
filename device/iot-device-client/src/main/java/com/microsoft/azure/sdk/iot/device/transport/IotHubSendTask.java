// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends batched messages and invokes callbacks on completed requests. Meant to
 * be used with an executor that continuously calls run().
 */
public final class IotHubSendTask implements Runnable
{
    protected final IotHubTransport transport;

    /**
     * Private logger for class
     */
    final Logger logger = LoggerFactory.getLogger(IotHubSendTask.class);
    
    public IotHubSendTask(IotHubTransport transport)
    {
        // Codes_SRS_IOTHUBSENDTASK_11_001: [The constructor shall save the transport.]
        this.transport = transport;
        
        if (transport == null)
        	throw new IllegalArgumentException("Parameter 'transport' must not be null");
        
        logger.error("IotHubSendTask constructor called with null value for parameter transport");
    }

    public void run()
    {
    	logger.trace("Now sending all queued messages to IoT Hub");

    	try
        {
            // Codes_SRS_IOTHUBSENDTASK_11_002: [The function shall send all messages on the transport queue.]
            this.transport.sendMessages();
            
            logger.trace("Now invoking all queued callbacks");
            // Codes_SRS_IOTHUBSENDTASK_11_003: [The function shall invoke all callbacks on the transport's callback queue.]
            this.transport.invokeCallbacks();
            
            logger.trace("Successfully send all queued messages to IoT Hub");
        }
        // Codes_SRS_IOTHUBSENDTASK_11_005: [The function shall not crash because of an IOException thrown by the transport.]
        // Codes_SRS_IOTHUBSENDTASK_11_008: [The function shall not crash because of any error or exception thrown by the transport.]
        catch (Throwable e)
        {
        	logger.error(e.toString() + ": " + e.getMessage());
        	logger.debug("Exception on sending queued messages to IoT Hub", e);
        }
    }
}