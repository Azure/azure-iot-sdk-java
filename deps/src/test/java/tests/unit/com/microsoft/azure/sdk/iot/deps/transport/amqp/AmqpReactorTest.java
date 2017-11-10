/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.deps.transport.amqp;

import com.microsoft.azure.sdk.iot.deps.transport.amqp.AmqpReactor;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.integration.junit4.JMockit;
import org.apache.qpid.proton.reactor.Reactor;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for AmqpConnection.
 * Coverage : 100% method, 100% line */
@RunWith(JMockit.class)
public class AmqpReactorTest
{
    @Mocked
    private Reactor mockedReactor;

    @Test
    public void amqpReactorConstructorSucceeds()
    {
        new AmqpReactor(mockedReactor);
    }

    @Test
    public void amqpReactorNullReactorConstructorSucceeds()
    {
        new AmqpReactor(null);
    }

    @Test
    public void amqpReactorRun()
    {
        AmqpReactor amqpReactor = new AmqpReactor(mockedReactor);

        new NonStrictExpectations()
        {
            {
                mockedReactor.setTimeout(anyInt);
                mockedReactor.start();
                mockedReactor.process();
                result = false;

                mockedReactor.stop();
                mockedReactor.process();
                mockedReactor.free();
            }
        };

        amqpReactor.run();
    }

}
