/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.engine.HandlerException;
import org.apache.qpid.proton.reactor.Reactor;

@Slf4j
public class ReactorRunner implements Runnable
{
    private final static String THREAD_NAME = "azure-iot-sdk-ReactorRunner";
    private final Reactor reactor;
    public static final int REACTOR_TIMEOUT = 3141; // reactor timeout in milliseconds

    public ReactorRunner(Reactor reactor)
    {
        this.reactor = reactor;
    }

    @Override
    public void run()
    {
        Thread.currentThread().setName(THREAD_NAME);

        try
        {
            this.reactor.setTimeout(REACTOR_TIMEOUT);
            this.reactor.start();
            reactor.run();
        }
        catch (HandlerException e)
        {
            log.error("Encountered HandlerException while running reactor {}", e.getMessage());
        }

        log.trace("Finishing reactor thread");
    }
}

