/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.ClientConfiguration;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.transport.TransportException;
import mockit.Delegate;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Handler;
import org.apache.qpid.proton.engine.Record;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.Task;
import org.junit.Test;

/**
 * Unit tests for AmqpsSasTokenRenewalHandler.
 */
@SuppressWarnings("ThrowableNotThrown")
public class AmqpsSasTokenRenewalHandlerTest
{
    @Mocked
    AmqpsCbsSessionHandler mockedCbsSessionHandler;

    @Mocked
    AmqpsSessionHandler mockedSessionHandler;

    @Mocked
    ClientConfiguration mockedConfig;

    @Mocked
    IotHubSasTokenAuthenticationProvider mockedSasTokenAuthenticationProvider;

    @Mocked
    Reactor mockedReactor;

    @Mocked
    Task mockedAuthenticationTimeoutTask;

    @Mocked
    Task mockedRenewalTask;

    @Mocked
    Event mockedEvent;

    @Mocked
    Record mockedRecord;

    // Tests_SRS_AMQPSSASTOKENRENEWALHANDLER_34_001: [If no CBS authentication response is received before the operation timeout, this function shall notify the CBS session that authentication timed out.]
    @Test
    public void authenticationResponseTimeoutNotifiesCbsSession() throws TransportException
    {
        //arrange
        final String deviceId = "someDevice";
        final int authenticationTimeout = 1000;
        final int renewalPeriod = 2000;
        final Handler[] authenticationTimeoutHandler = new Handler[1];

        new Expectations()
        {
            {
                mockedSessionHandler.getDeviceId();
                result = deviceId;

                mockedSessionHandler.getClientConfiguration();
                result = mockedConfig;

                mockedConfig.getOperationTimeout();
                result = authenticationTimeout;

                mockedConfig.getSasTokenAuthentication();
                result = mockedSasTokenAuthenticationProvider;

                mockedSasTokenAuthenticationProvider.getMillisecondsBeforeProactiveRenewal();
                result = renewalPeriod;

                mockedCbsSessionHandler.sendAuthenticationMessage(mockedConfig, (AuthenticationMessageCallback) any);

                mockedReactor.schedule(anyInt, (Handler) any);
                result = new Delegate<Reactor>()
                {
                    @SuppressWarnings("unused")
                    Task schedule(int delay, Handler handler)
                    {
                        if (delay == authenticationTimeout)
                        {
                            authenticationTimeoutHandler[0] = handler;
                            return mockedAuthenticationTimeoutTask;
                        }

                        return mockedRenewalTask;
                    }
                };
            }
        };

        AmqpsSasTokenRenewalHandler sasTokenRenewalHandler = new AmqpsSasTokenRenewalHandler(mockedCbsSessionHandler, mockedSessionHandler);
        sasTokenRenewalHandler.sendAuthenticationMessage(mockedReactor);

        //act
        ((BaseHandler) authenticationTimeoutHandler[0]).onTimerTask(mockedEvent);

        //assert
        new Verifications()
        {
            {
                mockedCbsSessionHandler.onAuthenticationTimedOut(deviceId);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSSASTOKENRENEWALHANDLER_34_002: [If a CBS authentication response is received before the operation timeout, this function shall cancel the authentication response timeout.]
    @Test
    public void authenticationResponseCancelsResponseTimeoutTask() throws TransportException
    {
        //arrange
        final String deviceId = "someDevice";
        final int authenticationTimeout = 1000;
        final int renewalPeriod = 2000;

        new Expectations()
        {
            {
                mockedSessionHandler.getDeviceId();
                result = deviceId;

                mockedSessionHandler.getClientConfiguration();
                result = mockedConfig;

                mockedConfig.getOperationTimeout();
                result = authenticationTimeout;

                mockedConfig.getSasTokenAuthentication();
                result = mockedSasTokenAuthenticationProvider;

                mockedSasTokenAuthenticationProvider.getMillisecondsBeforeProactiveRenewal();
                result = renewalPeriod;

                mockedCbsSessionHandler.sendAuthenticationMessage(mockedConfig, (AuthenticationMessageCallback) any);

                mockedReactor.schedule(authenticationTimeout, (Handler) any);
                result = mockedAuthenticationTimeoutTask;

                mockedReactor.schedule(renewalPeriod, (Handler) any);
                result = mockedRenewalTask;

                mockedAuthenticationTimeoutTask.attachments();
                result = mockedRecord;

                mockedSessionHandler.openLinks();
            }
        };

        AmqpsSasTokenRenewalHandler sasTokenRenewalHandler = new AmqpsSasTokenRenewalHandler(mockedCbsSessionHandler, mockedSessionHandler);
        sasTokenRenewalHandler.sendAuthenticationMessage(mockedReactor);

        //act
        sasTokenRenewalHandler.handleAuthenticationResponseMessage(200, "", mockedReactor);

        //assert
        new Verifications()
        {
            {
                mockedAuthenticationTimeoutTask.cancel();
                times = 1;

                mockedRecord.clear();
                times = 1;

                mockedCbsSessionHandler.onAuthenticationTimedOut(anyString);
                times = 0;
            }
        };
    }
}
