/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.amqps.AmqpSasTokenRenewalHandler;
import com.microsoft.azure.sdk.iot.device.transport.amqps.AmqpsIotHubConnection;
import com.microsoft.azure.sdk.iot.device.transport.amqps.AmqpsSessionHandler;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.reactor.Reactor;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class AmqpSasTokenRenewalHandlerTest
{
    @Mocked
    AmqpsIotHubConnection mockedAmqpsIotHubConnection;

    @Mocked
    DeviceClientConfig mockedConfig;

    @Mocked
    Event mockEvent;

    @Mocked
    Reactor mockReactor;

    @Mocked
    IotHubSasTokenAuthenticationProvider mockSasTokenAuthentication;

    @Mocked
    AmqpsSessionHandler mockAmqpsSessionHandler;

    @Test
    public void constructorSavesArguments()
    {
        //act
        AmqpSasTokenRenewalHandler sasTokenRenewalHandler = new AmqpSasTokenRenewalHandler(mockedAmqpsIotHubConnection, mockAmqpsSessionHandler);

        //assert
        assertEquals(mockAmqpsSessionHandler, Deencapsulation.getField(sasTokenRenewalHandler, "sessionHandler"));
        assertEquals(mockedAmqpsIotHubConnection, Deencapsulation.getField(sasTokenRenewalHandler, "amqpsIotHubConnection"));
    }

    @Test
    public void timerTaskSchedulesNextTimerTask()
    {
        //arrange
        final AmqpSasTokenRenewalHandler sasTokenRenewalHandler = new AmqpSasTokenRenewalHandler(mockedAmqpsIotHubConnection, mockAmqpsSessionHandler);

        final int renewalPeriod = 1234;

        new Expectations()
        {
            {
                mockEvent.getReactor();
                result = mockReactor;

                mockAmqpsSessionHandler.getDeviceClientConfig();
                result = mockedConfig;

                mockedConfig.getSasTokenAuthentication();
                result = mockSasTokenAuthentication;
                mockSasTokenAuthentication.getMillisecondsBeforeProactiveRenewal();
                result = renewalPeriod;
            }
        };

        //act
        sasTokenRenewalHandler.onTimerTask(mockEvent);

        //assert
        new Verifications()
        {
            {
                mockReactor.schedule(renewalPeriod, sasTokenRenewalHandler);
            }
        };
    }

    @Test
    public void timerTaskAuthenticatesUsingSessionManager() throws TransportException
    {
        //arrange
        final AmqpSasTokenRenewalHandler sasTokenRenewalHandler = new AmqpSasTokenRenewalHandler(mockedAmqpsIotHubConnection, mockAmqpsSessionHandler);

        final int renewalPeriod = 1234;

        new Expectations()
        {
            {
                mockEvent.getReactor();
                result = mockReactor;

                mockAmqpsSessionHandler.getDeviceClientConfig();
                result = mockedConfig;

                mockedConfig.getSasTokenAuthentication();
                result = mockSasTokenAuthentication;

                mockSasTokenAuthentication.getMillisecondsBeforeProactiveRenewal();
                result = renewalPeriod;
            }
        };

        //act
        sasTokenRenewalHandler.onTimerTask(mockEvent);

        //assert
        new Verifications()
        {
            {
                mockedAmqpsIotHubConnection.authenticate(mockAmqpsSessionHandler);
                times = 1;
            }
        };
    }
}
