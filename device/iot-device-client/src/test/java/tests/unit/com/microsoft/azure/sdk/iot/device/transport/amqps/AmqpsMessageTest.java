/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.transport.amqps.AmqpsMessage;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.messaging.Released;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.engine.Delivery;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
*  Unit tests for AmqpsMessage
* 100% methods covered
* 100% lines covered
*/
public class AmqpsMessageTest
{
    @Mocked
    protected Delivery mockDelivery;

    @Test
    public void acknowledgeTest()
    {
        //arrange
        AmqpsMessage message = new AmqpsMessage();
        message.setDelivery(mockDelivery);

        final DeliveryState expectedDisposition = Accepted.getInstance();

        //act
        message.acknowledge(expectedDisposition);

        //assert
        new Verifications()
        {
            {
                mockDelivery.disposition(expectedDisposition);
                mockDelivery.settle();
            }
        };

    }
}
