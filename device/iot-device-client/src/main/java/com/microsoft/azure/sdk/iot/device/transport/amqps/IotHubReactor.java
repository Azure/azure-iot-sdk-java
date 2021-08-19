/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import org.apache.qpid.proton.engine.HandlerException;
import org.apache.qpid.proton.reactor.Reactor;

class IotHubReactor
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

        //noinspection StatementWithEmptyBody
        while (this.reactor.process())
        {
            // The empty while loop is to ensure that reactor thread runs as long as it has messages to process
        }
        this.reactor.stop();
        this.reactor.process();
        this.reactor.free();
    }

    public void free()
    {
        this.reactor.free();
    }
}
