/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.transport.amqps.AmqpsSendResult;
import mockit.Deencapsulation;
import org.junit.Test;

import static org.junit.Assert.*;

/**
*  Unit tests for AmqpsSendResult
* 100% methods covered
* 100% lines covered
*/
public class AmqpsSendResultTest
{
    @Test
    public void constructorInitializesAllMembersUnsuccessfulDelivery()
    {
        //arrange
        byte[] expectedDeliveryTag = "-1".getBytes();

        //act
        AmqpsSendResult amqpsSendResult = Deencapsulation.newInstance(AmqpsSendResult.class);
        boolean actualIsDeliverySuccessful = Deencapsulation.getField(amqpsSendResult, "deliverySuccessful");
        int actualDeliveryTag = Deencapsulation.getField(amqpsSendResult, "deliveryTag");

        //assert
        assertFalse(actualIsDeliverySuccessful);
        assertEquals(new String(expectedDeliveryTag), String.valueOf(actualDeliveryTag));
    }

    @Test
    public void constructorInitializesAllMembersSuccessfulDelivery() {
        //arrange
        int expectedDeliveryTag = 56;
        byte[] deliveryTag = String.valueOf(expectedDeliveryTag).getBytes();

        //act
        AmqpsSendResult amqpsSendResult = Deencapsulation.newInstance(AmqpsSendResult.class, (Object) deliveryTag);
        boolean actualIsDeliverySuccessful = Deencapsulation.getField(amqpsSendResult, "deliverySuccessful");
        int actualDeliveryTag = Deencapsulation.getField(amqpsSendResult, "deliveryTag");

        //assert
        assertTrue(actualIsDeliverySuccessful);
        assertEquals(expectedDeliveryTag, actualDeliveryTag);
    }

    @Test
    public void getDeliveryTagWorks()
    {
        //arrange
        byte[] deliveryTag = String.valueOf(24).getBytes();
        int deliveryTagInt = Integer.parseInt(new String(deliveryTag));
        AmqpsSendResult amqpsSendResult = Deencapsulation.newInstance(AmqpsSendResult.class, (Object) deliveryTag);

        //act
        int actualDeliveryTag = amqpsSendResult.getDeliveryTag();

        //assert
        assertEquals(deliveryTagInt, actualDeliveryTag);
    }
}