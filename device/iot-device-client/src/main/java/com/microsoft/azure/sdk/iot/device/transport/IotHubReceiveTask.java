// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Polls an IoT Hub for messages and invokes a callback if one is found.
 * Meant to be used with an executor that continuously calls run().
 */
public final class IotHubReceiveTask implements Runnable
{
    protected final IotHubTransport transport;
    /**
     * Private logger for class
     */
    final Logger logger = LoggerFactory.getLogger(IotHubReceiveTask.class);
    		
    public IotHubReceiveTask(IotHubTransport transport)
    {
        // Codes_SRS_IOTHUBRECEIVETASK_11_001: [The constructor shall save the transport.]
        this.transport = transport;
        
        if (transport == null)
        	throw new IllegalArgumentException("Parameter 'transport' must not be null");
        
        logger.error("IotHubReceiveTask constructor called with null value for parameter transport");
    }

    public void run()
    {
    	logger.trace("Now trying to receive messages from IoT Hub");
    	
        try
        {
            // Codes_SRS_IOTHUBRECEIVETASK_11_002: [The function shall poll an IoT Hub for messages, invoke the message callback if one exists, and return one of COMPLETE, ABANDON, or REJECT to the IoT Hub.]
            this.transport.handleMessage();
            
            logger.trace("Successfully received messages from IoT Hub");
        }
        // Codes_SRS_IOTHUBRECEIVETASK_11_004: [The function shall not crash because of an IOException thrown by the transport.]
        // Codes_SRS_IOTHUBRECEIVETASK_11_005: [The function shall not crash because of any error or exception thrown by the transport.]
        catch (Throwable e)
        {
        	logger.error(e.toString() + ": " + e.getMessage());
        	logger.debug("Exception on sending queued messages to IoT Hub", e);
        }
    }
}