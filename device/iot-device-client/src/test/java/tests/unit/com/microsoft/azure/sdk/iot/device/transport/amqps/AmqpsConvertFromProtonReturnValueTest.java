/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageCallback;
import com.microsoft.azure.sdk.iot.device.transport.amqps.AmqpsConvertFromProtonReturnValue;
import mockit.Deencapsulation;
import mockit.Mocked;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
* Unit tests for AmqpsConvertFromProtonReturnValue
* 100% methods covered
* 100% lines covered
*/
public class AmqpsConvertFromProtonReturnValueTest
{
    @Mocked
    Message mockMessage;

    @Mocked
    MessageCallback mockMessageCallback;

    // Tests_SRS_AMQPSCONVERTFROMPROTONRETURNVALUE_12_001: [The constructor shall initialize message, messageCallback and messageContext private member variables with the given arguments.]
    @Test
    public void constructorInitializesAllMembers()
    {
        //arrange
        String messageContext = "expectedMessageContext";

        //act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.newInstance(AmqpsConvertFromProtonReturnValue.class, mockMessage, mockMessageCallback, messageContext);
        Message actualMessage = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "message");
        MessageCallback actualMessageCallback = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageCallback");
        Object actualMessageContext = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageContext");

        //assert
        assertEquals(mockMessage, actualMessage);
        assertEquals(mockMessageCallback, actualMessageCallback);
        assertEquals(messageContext, actualMessageContext);
    }

    // Tests_SRS_AMQPSCONVERTFROMPROTONRETURNVALUE_12_002: [The function shall return the current value of message private member.]
    // Tests_SRS_AMQPSCONVERTFROMPROTONRETURNVALUE_12_003: [The function shall return the current value of messageCallback private member.]
    // Tests_SRS_AMQPSCONVERTFROMPROTONRETURNVALUE_12_004: [The function shall return the current value of messageContext private member.]
    @Test
    public void getMessageAndGetMessageCallbackAndGetMessageContextReturns()
    {
        //arrange
        String messageContext = "expectedMessageContext";

        //act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.newInstance(AmqpsConvertFromProtonReturnValue.class, mockMessage, mockMessageCallback, messageContext);
        Message actualMessage = Deencapsulation.invoke(amqpsConvertFromProtonReturnValue, "getMessage");
        MessageCallback actualMessageCallback = Deencapsulation.invoke(amqpsConvertFromProtonReturnValue, "getMessageCallback");
        Object actualMessageContext = Deencapsulation.invoke(amqpsConvertFromProtonReturnValue, "getMessageContext");

        //assert
        assertEquals(mockMessage, actualMessage);
        assertEquals(mockMessageCallback, actualMessageCallback);
        assertEquals(messageContext, actualMessageContext);
    }
}