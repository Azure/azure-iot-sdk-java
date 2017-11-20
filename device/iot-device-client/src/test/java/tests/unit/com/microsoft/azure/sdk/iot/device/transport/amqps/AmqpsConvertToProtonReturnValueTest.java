/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.transport.amqps.AmqpsConvertToProtonReturnValue;
import mockit.Deencapsulation;
import mockit.Mocked;
import org.apache.qpid.proton.message.impl.MessageImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
* Unit tests for AmqpsConvertToProtonReturnValue
* 100% methods covered
* 100% lines covered
*/
public class AmqpsConvertToProtonReturnValueTest
{
    @Mocked
    MessageImpl mockMessageImpl;

    // Tests_SRS_AMQPSCONVERTTOPROTONRETURNVALUE_12_001: [The constructor shall initialize messageImpl and messageType private member variables with the given arguments.]
    @Test
    public void constructorInitializesAllMembers()
    {
        //arrange
        MessageType messageType = MessageType.DEVICE_TWIN;

        //act
        AmqpsConvertToProtonReturnValue amqpsConvertToProtonReturnValue = Deencapsulation.newInstance(AmqpsConvertToProtonReturnValue.class, mockMessageImpl, messageType);
        MessageImpl actualMessageImpl = Deencapsulation.getField(amqpsConvertToProtonReturnValue, "messageImpl");
        MessageType actualMessageType = Deencapsulation.getField(amqpsConvertToProtonReturnValue, "messageType");

        //assert
        assertEquals(mockMessageImpl, actualMessageImpl);
        assertEquals(messageType, actualMessageType);
    }


    // Tests_SRS_AMQPSCONVERTTOPROTONRETURNVALUE_12_002: [The function shall return the current value of messageImpl private member.]
    // Tests_SRS_AMQPSCONVERTTOPROTONRETURNVALUE_12_003: [The function shall return the current value of messageType private member.]
    @Test
    public void getMessageImplAndGetMessageTypeReturns()
    {
        //arrange
        MessageType messageType = MessageType.DEVICE_METHODS;

        //act
        AmqpsConvertToProtonReturnValue amqpsConvertToProtonReturnValue = Deencapsulation.newInstance(AmqpsConvertToProtonReturnValue.class, mockMessageImpl, messageType);
        MessageImpl actualMessageImpl = Deencapsulation.invoke(amqpsConvertToProtonReturnValue, "getMessageImpl");
        MessageType actualMessageType = Deencapsulation.invoke(amqpsConvertToProtonReturnValue, "getMessageType");

        //assert
        assertEquals(mockMessageImpl, actualMessageImpl);
        assertEquals(messageType, actualMessageType);
    }
}