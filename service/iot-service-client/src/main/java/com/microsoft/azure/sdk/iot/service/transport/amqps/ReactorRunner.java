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
import org.apache.qpid.proton.reactor.ReactorOptions;

import java.io.IOException;

@Slf4j
class ReactorRunner
{
    private final static String THREAD_NAME = "azure-iot-sdk-ReactorRunner";
    private final String threadName;
    private final Reactor reactor;
    private static final int REACTOR_TIMEOUT = 3141; // reactor timeout in milliseconds
    private static final int CLOSE_REACTOR_GRACEFULLY_TIMEOUT = 10 * 1000;
    private static final int MAX_FRAME_SIZE = 4 * 1024;

    public ReactorRunner(BaseHandler baseHandler, String threadNamePrefix, String threadNamePostfix) throws IOException
    {
        ReactorOptions options = new ReactorOptions();

        // If this option isn't set, proton defaults to 16 * 1024 max frame size. This used to default to 4 * 1024,
        // and this change to 16 * 1024 broke the websocket implementation that we layer on top of proton-j.
        // By setting this frame size back to 4 * 1024, AMQPS_WS clients can send messages with payloads up to the
        // expected 64 * 1024 bytes. For more context, see https://github.com/Azure/azure-iot-sdk-java/issues/742
        options.setMaxFrameSize(MAX_FRAME_SIZE);

        this.reactor = Proton.reactor(options, baseHandler);
        this.threadName = threadNamePrefix + "-" + THREAD_NAME + "-" + threadNamePostfix;
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

