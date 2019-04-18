/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.amqps.AmqpsSessionManager;
import com.microsoft.azure.sdk.iot.device.transport.amqps.SasTokenRenewalHandler;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.reactor.Reactor;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class SasTokenRenewalHandlerTest
{
    @Mocked
    AmqpsSessionManager mockedAmqpsSessionManager;

    @Mocked
    DeviceClientConfig mockedConfig;

    @Mocked
    Event mockEvent;

    @Mocked
    Reactor mockReactor;

    @Mocked
    IotHubSasTokenAuthenticationProvider mockSasTokenAuthentication;

    @Test
    public void constructorSavesArguments()
    {
        //act
        SasTokenRenewalHandler sasTokenRenewalHandler = new SasTokenRenewalHandler(mockedAmqpsSessionManager, mockedConfig);

        //assert
        assertEquals(mockedConfig, Deencapsulation.getField(sasTokenRenewalHandler, "config"));
        assertEquals(mockedAmqpsSessionManager, Deencapsulation.getField(sasTokenRenewalHandler, "amqpsSessionManager"));
    }

    @Test
    public void timerTaskSchedulesNextTimerTask()
    {
        //arrange
        final SasTokenRenewalHandler sasTokenRenewalHandler = new SasTokenRenewalHandler(mockedAmqpsSessionManager, mockedConfig);

        final int renewalPeriod = 1234;

        new Expectations()
        {
            {
                mockEvent.getReactor();
                result = mockReactor;

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
        final SasTokenRenewalHandler sasTokenRenewalHandler = new SasTokenRenewalHandler(mockedAmqpsSessionManager, mockedConfig);

        final int renewalPeriod = 1234;

        new Expectations()
        {
            {
                mockEvent.getReactor();
                result = mockReactor;

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
                mockedAmqpsSessionManager.authenticate();
                times = 1;
            }
        };
    }
}
