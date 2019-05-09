/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.transport.amqps.AmqpsSendReturnValue;
import mockit.Deencapsulation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
*  Unit tests for AmqpsSendReturnValue
* 100% methods covered
* 100% lines covered
*/
public class AmqpsSendReturnValueTest
{
    // Tests_SRS_AMQPSSENDRETURNVALUE_12_001: [The constructor shall initialize deliverySuccessful and deliveryHash private member variables with the given arguments.]
    @Test
    public void constructorInitializesAllMembers()
    {
        //arrange
        boolean isDeliverySuccessful = false;
        int deliveryHash = 42;
        byte[] expectedDeliveryTag = "-1".getBytes();

        //act
        AmqpsSendReturnValue amqpsSendReturnValue = Deencapsulation.newInstance(AmqpsSendReturnValue.class, isDeliverySuccessful, deliveryHash);
        boolean actualIsDeliverySuccessful = Deencapsulation.getField(amqpsSendReturnValue, "deliverySuccessful");
        int actualDeliveryHash = Deencapsulation.getField(amqpsSendReturnValue, "deliveryHash");
        byte[] actualDeliveryTag = Deencapsulation.getField(amqpsSendReturnValue, "deliveryTag");

        //assert
        assertEquals(isDeliverySuccessful, actualIsDeliverySuccessful);
        assertEquals(deliveryHash, actualDeliveryHash);
        assertEquals(new String(expectedDeliveryTag), new String(actualDeliveryTag));
    }

    // Tests_SRS_AMQPSSENDRETURNVALUE_34_005: [The constructor shall initialize deliverySuccessful, deliveryHash, and deliveryTag private member variables with the given arguments.]
    @Test
    public void constructorInitializesAllMembersWithDeliveryTag()
    {
        //arrange
        boolean isDeliverySuccessful = false;
        int deliveryHash = 42;
        byte[] expectedDeliveryTag = "1234".getBytes();

        //act
        AmqpsSendReturnValue amqpsSendReturnValue = Deencapsulation.newInstance(AmqpsSendReturnValue.class, isDeliverySuccessful, deliveryHash, expectedDeliveryTag);
        boolean actualIsDeliverySuccessful = Deencapsulation.getField(amqpsSendReturnValue, "deliverySuccessful");
        int actualDeliveryHash = Deencapsulation.getField(amqpsSendReturnValue, "deliveryHash");
        byte[] actualDeliveryTag = Deencapsulation.getField(amqpsSendReturnValue, "deliveryTag");

        //assert
        assertEquals(isDeliverySuccessful, actualIsDeliverySuccessful);
        assertEquals(deliveryHash, actualDeliveryHash);
        assertEquals(expectedDeliveryTag, actualDeliveryTag);
    }

    // Tests_SRS_AMQPSSENDRETURNVALUE_12_002: [The function shall return the current value of deliverySuccessful private member.]
    // Tests_SRS_AMQPSSENDRETURNVALUE_12_003: [The function shall return the current value of deliveryHash private member.]
    @Test
    public void isDeliverySuccessfulAndGetDeliveryHashReturns()
    {
        //arrange
        boolean isDeliverySuccessful = true;
        int deliveryHash = 42;
        byte[] deliveryTag = new byte[] {1,2,3,4};
        AmqpsSendReturnValue amqpsSendReturnValue = Deencapsulation.newInstance(AmqpsSendReturnValue.class, isDeliverySuccessful, deliveryHash);

        //act
        boolean actualIsDeliverySuccessful = Deencapsulation.invoke(amqpsSendReturnValue, "isDeliverySuccessful");
        int actualDeliveryHash = Deencapsulation.invoke(amqpsSendReturnValue, "getDeliveryHash");

        //assert
        assertEquals(isDeliverySuccessful, actualIsDeliverySuccessful);
        assertEquals(deliveryHash, actualDeliveryHash);
    }

    // Tests_SRS_AMQPSSENDRETURNVALUE_34_004: [The function shall return the saved value of the delivery tag.]
    @Test
    public void getDeliveryTagWorks()
    {
        //arrange
        boolean isDeliverySuccessful = true;
        int deliveryHash = 42;
        byte[] deliveryTag = new byte[] {1,2,3,4};
        AmqpsSendReturnValue amqpsSendReturnValue = Deencapsulation.newInstance(AmqpsSendReturnValue.class, isDeliverySuccessful, deliveryHash, deliveryTag);

        //act
        byte[] actualDeliveryTag = amqpsSendReturnValue.getDeliveryTag();

        //assert
        assertEquals(new String(deliveryTag), new String(actualDeliveryTag));
    }
}