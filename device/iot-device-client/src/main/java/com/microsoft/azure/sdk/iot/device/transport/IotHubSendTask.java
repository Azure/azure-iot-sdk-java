// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

/**
 * Sends batched messages and invokes callbacks on completed requests. Meant to
 * be used with an executor that continuously calls run().
 */
public final class IotHubSendTask implements Runnable
{
    protected final IotHubTransport transport;
    
    /**
     * If true, full stack trace will be written to console on exception thrown by transport message sending
     */
    private boolean verboseErrorMessaging = false;

    public IotHubSendTask(IotHubTransport transport)
    {
        // Codes_SRS_IOTHUBSENDTASK_11_001: [The constructor shall save the transport.]
        this.transport = transport;
        
        if (this.transport == null)
        	throw new IllegalArgumentException("Value for parameter 'transport' must not be null");
    }
    
    public IotHubSendTask(IotHubTransport transport, boolean verboseErrMessaging)
    {
    	this(transport);
    	
    	this.verboseErrorMessaging = verboseErrMessaging;
    }

    public void run()
    {
        try
        {
            // Codes_SRS_IOTHUBSENDTASK_11_002: [The function shall send all messages on the transport queue.]
            this.transport.sendMessages();
            // Codes_SRS_IOTHUBSENDTASK_11_003: [The function shall invoke all callbacks on the transport's callback queue.]
            this.transport.invokeCallbacks();
        }
        // Codes_SRS_IOTHUBSENDTASK_11_005: [The function shall not crash because of an IOException thrown by the transport.]
        // Codes_SRS_IOTHUBSENDTASK_11_008: [The function shall not crash because of any error or exception thrown by the transport.]
        // JMayrbaeurl 2017-03-10: Bad idea to catch any Throwable here!!!
        catch (Throwable e)
        {
        	// JMayrbaeurl 2017-03-10: Should be replaced with real logging setup
            System.out.println(e.toString() + ": " + e.getMessage());
            
            // Only output full stack trace on verbose Error messaging configuration
            if (this.verboseErrorMessaging) 
            {
	            for (StackTraceElement el : e.getStackTrace())
	            {
	            	// JMayrbaeurl 2017-03-10: Should be replaced with real logging setup
	                System.out.println(el);
	            }
            }
        }
    }
}