/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.HandlerException;
import org.apache.qpid.proton.reactor.Reactor;

import java.io.IOException;

@Slf4j
public class ReactorRunner
{
    private final static String THREAD_NAME_PREFIX = "azure-iot-sdk-ReactorRunner-";
    private final String threadName;
    private final Reactor reactor;
    public static final int REACTOR_TIMEOUT = 3141; // reactor timeout in milliseconds
    public static final int CLOSE_REACTOR_GRACEFULLY_TIMEOUT = 10 * 1000;

    public ReactorRunner(BaseHandler baseHandler, String threadNamePostfix) throws IOException
    {
        this.reactor = Proton.reactor(baseHandler);
        this.threadName = THREAD_NAME_PREFIX + threadNamePostfix;
    }

    public void run(long timeoutMs)
    {
        Thread.currentThread().setName(this.threadName);

        try
        {
            log.trace("Starting reactor thread {}", this.threadName);
            this.reactor.setTimeout(REACTOR_TIMEOUT);

            long startTime = System.currentTimeMillis();
            long endTime = startTime + timeoutMs;

            boolean closedBeforeTimeout = true;
            this.reactor.start();
            while (this.reactor.process())
            {
                if (System.currentTimeMillis() > endTime)
                {
                    closedBeforeTimeout = false;
                    break;
                }
            }

            if (!closedBeforeTimeout)
            {
                // onTimerTask event will fire immediately in the basehandler being run. It is the responsibility of that handler
                // to close it's link/session/connection and stop this reactor. This runner will allow some time for the amqp connection
                // to be closed gracefully, but will forcefully free the resources if the graceful close takes too long
                log.trace("Scheduling shutdown event for reactor for thread {}", threadName);
                this.reactor.schedule(0, this.reactor.getHandler());

                startTime = System.currentTimeMillis();
                while (this.reactor.process())
                {
                    if (System.currentTimeMillis() - startTime > CLOSE_REACTOR_GRACEFULLY_TIMEOUT)
                    {
                        // The connection/session/link may not have been closed from the service's perspective, but we can free up the socket at least
                        log.trace("Amqp reactor in thread {} failed to close gracefully in expected time frame, forcefully closing it now", this.threadName);
                        break;
                    }
                }

                log.trace("Stopping reactor for thread {}", threadName);
                this.reactor.stop();
            }
            else
            {
                log.trace("Amqp reactor thread closed itself gracefully before the designated time out");
            }
        }
        catch (HandlerException e)
        {
            log.debug("Encountered an exception while running reactor on thread {}", threadName, e);
        }
        finally
        {
            log.trace("Freeing reactor now that reactor thread is done");
            this.reactor.free();
        }

        log.trace("Finished reactor thread {}", this.threadName);
    }

    public void run()
    {
        Thread.currentThread().setName(this.threadName);

        try
        {
            log.trace("Starting reactor thread {}", this.threadName);
            this.reactor.setTimeout(REACTOR_TIMEOUT);
            this.reactor.run();
        }
        catch (HandlerException e)
        {
            log.debug("Encountered an exception while running reactor on thread {}", threadName, e);
        }
        finally
        {
            log.trace("Freeing reactor now that reactor thread is done");
            this.reactor.free();
        }

        log.trace("Finished reactor thread {}", this.threadName);
    }

}

