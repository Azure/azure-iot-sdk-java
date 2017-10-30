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

        //act
        AmqpsSendReturnValue amqpsSendReturnValue = Deencapsulation.newInstance(AmqpsSendReturnValue.class, isDeliverySuccessful, deliveryHash);
        boolean actualIsDeliverySuccessful = Deencapsulation.getField(amqpsSendReturnValue, "deliverySuccessful");
        int actualDeliveryHash = Deencapsulation.getField(amqpsSendReturnValue, "deliveryHash");

        //assert
        assertEquals(isDeliverySuccessful, actualIsDeliverySuccessful);
        assertEquals(deliveryHash, actualDeliveryHash);
    }

    // Tests_SRS_AMQPSSENDRETURNVALUE_12_002: [The function shall return the current value of deliverySuccessful private member.]
    // Tests_SRS_AMQPSSENDRETURNVALUE_12_003: [The function shall return the current value of deliveryHash private member.]
    @Test
    public void isDeliverySuccessfulAndGetDeliveryHashReturns()
    {
        //arrange
        boolean isDeliverySuccessful = true;
        int deliveryHash = 42;
        AmqpsSendReturnValue amqpsSendReturnValue = Deencapsulation.newInstance(AmqpsSendReturnValue.class, isDeliverySuccessful, deliveryHash);

        //act
        boolean actualIsDeliverySuccessful = Deencapsulation.invoke(amqpsSendReturnValue, "isDeliverySuccessful");
        int actualDeliveryHash = Deencapsulation.invoke(amqpsSendReturnValue, "getDeliveryHash");

        //assert
        assertEquals(isDeliverySuccessful, actualIsDeliverySuccessful);
        assertEquals(deliveryHash, actualDeliveryHash);
    }
}