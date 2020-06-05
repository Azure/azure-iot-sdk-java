/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.transport.amqps.AmqpsSendResult;
import mockit.Deencapsulation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
*  Unit tests for AmqpsSendResult
* 100% methods covered
* 100% lines covered
*/
public class AmqpsSendResultTest
{
    @Test
    public void constructorInitializesAllMembers()
    {
        //arrange
        boolean isDeliverySuccessful = false;
        byte[] expectedDeliveryTag = "-1".getBytes();

        //act
        AmqpsSendResult amqpsSendResult = Deencapsulation.newInstance(AmqpsSendResult.class, isDeliverySuccessful);
        boolean actualIsDeliverySuccessful = Deencapsulation.getField(amqpsSendResult, "deliverySuccessful");
        int actualDeliveryTag = Deencapsulation.getField(amqpsSendResult, "deliveryTag");

        //assert
        assertEquals(isDeliverySuccessful, actualIsDeliverySuccessful);
        assertEquals(new String(expectedDeliveryTag), String.valueOf(actualDeliveryTag));
    }

    @Test
    public void constructorInitializesAllMembersWithDeliveryTag() {
        //arrange
        boolean isDeliverySuccessful = false;
        int expectedDeliveryTag = 56;
        byte[] deliveryTag = String.valueOf(expectedDeliveryTag).getBytes();

        //act
        AmqpsSendResult amqpsSendResult = Deencapsulation.newInstance(AmqpsSendResult.class, isDeliverySuccessful, deliveryTag);
        boolean actualIsDeliverySuccessful = Deencapsulation.getField(amqpsSendResult, "deliverySuccessful");
        int actualDeliveryTag = Deencapsulation.getField(amqpsSendResult, "deliveryTag");

        //assert
        assertEquals(isDeliverySuccessful, actualIsDeliverySuccessful);
        assertEquals(expectedDeliveryTag, actualDeliveryTag);
    }

    @Test
    public void isDeliverySuccessfulAndGetDeliveryHashReturns()
    {
        //arrange
        boolean isDeliverySuccessful = true;
        AmqpsSendResult amqpsSendResult = Deencapsulation.newInstance(AmqpsSendResult.class, isDeliverySuccessful);

        //act
        boolean actualIsDeliverySuccessful = Deencapsulation.invoke(amqpsSendResult, "isDeliverySuccessful");

        //assert
        assertEquals(isDeliverySuccessful, actualIsDeliverySuccessful);
    }

    @Test
    public void getDeliveryTagWorks()
    {
        //arrange
        boolean isDeliverySuccessful = true;
        byte[] deliveryTag = String.valueOf(24).getBytes();
        int deliveryTagInt = Integer.parseInt(new String(deliveryTag));
        AmqpsSendResult amqpsSendResult = Deencapsulation.newInstance(AmqpsSendResult.class, isDeliverySuccessful, deliveryTag);

        //act
        int actualDeliveryTag = amqpsSendResult.getDeliveryTag();

        //assert
        assertEquals(deliveryTagInt, actualDeliveryTag);
    }
}