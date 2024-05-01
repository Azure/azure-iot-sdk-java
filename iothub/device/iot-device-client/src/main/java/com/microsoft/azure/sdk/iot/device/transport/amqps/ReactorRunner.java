// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.transport.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubListener;
import org.apache.qpid.proton.engine.HandlerException;
import org.apache.qpid.proton.reactor.Reactor;

import java.util.concurrent.Callable;

public class ReactorRunner implements Callable<Object>
{
    static final String THREAD_NAME = "azure-iot-sdk-ReactorRunner";
    private final Reactor reactor;
    private final IotHubListener listener;
    private final String connectionId;
    private final String threadName;
    private final ReactorRunnerStateCallback reactorRunnerStateCallback;

    ReactorRunner(
            Reactor reactor,
            IotHubListener listener,
            String connectionId,
            String threadName,
            ReactorRunnerStateCallback reactorRunnerStateCallback)
    {
        this.listener = listener;
        this.reactor = reactor;
        this.connectionId = connectionId;
        this.threadName = threadName;
        this.reactorRunnerStateCallback = reactorRunnerStateCallback;
    }

    @Override
    public Object call()
    {
        try
        {
            Thread.currentThread().setName(this.threadName);
            this.reactor.setTimeout(10);
            this.reactor.start();

            //noinspection StatementWithEmptyBody
            while (this.reactor.process())
            {
                // The empty while loop is to ensure that reactor thread runs as long as it has messages to process
            }
            this.reactor.stop();
            this.reactor.process();
        }
        catch (HandlerException e)
        {
            TransportException transportException = new TransportException(e);

            // unclassified exceptions are treated as retryable in ProtonJExceptionParser, so they should be treated
            // the same way here. Exceptions caught here tend to be transient issues.
            transportException.setRetryable(true);

            // Notify the AMQP connection layer so it can tear down the reactor's resources that would usually
            // get cleaned up in a graceful close of the reactor
            this.reactorRunnerStateCallback.onReactorClosedUnexpectedly();

            this.listener.onConnectionLost(transportException, connectionId);
        }
        finally
        {
            reactor.free();
        }

        return null;
    }
}
