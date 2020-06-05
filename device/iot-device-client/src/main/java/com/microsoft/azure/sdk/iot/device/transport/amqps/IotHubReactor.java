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
        this.reactor = reactor;
    }

    public void run() throws HandlerException
    {
        this.reactor.setTimeout(10);
        this.reactor.start();

        while (this.reactor.process())
        {
        }
        this.reactor.stop();
        this.reactor.process();
        this.reactor.free();
    }
}
