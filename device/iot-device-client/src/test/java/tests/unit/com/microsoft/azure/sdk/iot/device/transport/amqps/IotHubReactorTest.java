/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.transport.amqps.IotHubReactor;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.Verifications;
import org.apache.qpid.proton.reactor.Reactor;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
* Unit tests for IotHubReactor.java
* 100% methods covered
* 100% lines covered
*/
public class IotHubReactorTest
{
    @Mocked
    Reactor mockReactor;

    // Tests_SRS_IOTHUBREACTOR_34_001: [This constructor will save the provided reactor.]
    @Test
    public void constructorSuccess()
    {
        //act
        IotHubReactor iotHubReactor = Deencapsulation.newInstance(IotHubReactor.class, new Class[] {Reactor.class}, mockReactor);

        //assert
        Reactor actualReactor = Deencapsulation.getField(iotHubReactor, "reactor");
        assertEquals(mockReactor, actualReactor);
    }

    // Tests_SRS_IOTHUBREACTOR_34_003: [This function shall set the timeout of the reactor to 10 milliseconds.]
    // Tests_SRS_IOTHUBREACTOR_34_004: [This function shall start the reactor and have it process indefinitely and stop the reactor when it finishes.]
    @Test
    public void runSucceeds()
    {
        //arrange
        final long expectedTimeout = 10;
        IotHubReactor iotHubReactor = Deencapsulation.newInstance(IotHubReactor.class, new Class[] {Reactor.class}, mockReactor);

        //act
        Deencapsulation.invoke(iotHubReactor, "run");

        //assert
        new Verifications()
        {
            {
                mockReactor.setTimeout(expectedTimeout);
                mockReactor.start();
                mockReactor.process();
                mockReactor.stop();
            }
        };
    }

}
