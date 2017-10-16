/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import org.apache.qpid.proton.engine.HandlerException;
import org.apache.qpid.proton.reactor.Reactor;

public class IotHubReactor
{
    private final Reactor reactor;

    public IotHubReactor(Reactor reactor)
    {
        //Codes_SRS_IOTHUBREACTOR_34_001: [This constructor will save the provided reactor.]
        this.reactor = reactor;
    }
    
    public void run() throws HandlerException
    {
        //Codes_SRS_IOTHUBREACTOR_34_003: [This function shall set the timeout of the reactor to 10 milliseconds.]
        this.reactor.setTimeout(10);
        this.reactor.start();

        //Codes_SRS_IOTHUBREACTOR_34_004: [This function shall start the reactor and have it process indefinitely and stop the reactor when it finishes.]
        while(this.reactor.process()){}
        this.reactor.stop();
        this.reactor.process();
        this.reactor.free();
    }
}
