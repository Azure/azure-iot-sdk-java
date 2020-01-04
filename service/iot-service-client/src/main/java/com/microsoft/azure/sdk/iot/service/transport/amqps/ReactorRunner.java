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
    private final static String THREAD_NAME_PREFIX = "azure-iot-sdk-ReactorRunner-";
    private final String threadName;
    private final Reactor reactor;
    public static final int REACTOR_TIMEOUT = 3141; // reactor timeout in milliseconds

    public ReactorRunner(Reactor reactor, String threadNamePostfix)
    {
        this.reactor = reactor;
        this.threadName = THREAD_NAME_PREFIX + threadNamePostfix;
    }

    @Override
    public void run()
    {
        Thread.currentThread().setName(this.threadName);

        try
        {
            log.trace("Starting reactor thread");
            this.reactor.setTimeout(REACTOR_TIMEOUT);
            this.reactor.run();
        }
        catch (HandlerException e)
        {
            log.error("Encountered an exception while running reactor on thread {}", threadName, e);
        }
        finally
        {
            log.trace("Freeing reactor now that reactor thread is done");
            this.reactor.free();
        }

        log.trace("Finished reactor thread");
    }
}

