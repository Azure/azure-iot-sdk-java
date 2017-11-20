/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.deps.transport.amqp;

import org.apache.qpid.proton.engine.HandlerException;
import org.apache.qpid.proton.reactor.Reactor;

public class AmqpReactor
{
    private static final int REACTOR_TIMEOUT = 10;

    private final Reactor reactor;

    /**
     * Amqp Reactor Constructor
     * @param reactor Proton Reactor object
     */
    public AmqpReactor(Reactor reactor)
    {
        this.reactor = reactor;
    }

    /**
     * Main run function to pump Proton messages
     * @throws HandlerException
     */
    public void run() throws HandlerException
    {
        this.reactor.setTimeout(REACTOR_TIMEOUT);
        this.reactor.start();

        while(this.reactor.process()){}
        this.reactor.stop();
        this.reactor.process();
        this.reactor.free();
    }
}
