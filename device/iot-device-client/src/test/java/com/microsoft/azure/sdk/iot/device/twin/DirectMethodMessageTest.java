// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.twin;

import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import org.junit.Test;

import static org.junit.Assert.*;

/* Unit tests for IotHubTransportMessage
* 100% methods covered
* 100% lines covered
*/
public class DirectMethodMessageTest
{
    /*
    **Tests_SRS_DEVICEMETHODMESSAGE_25_001: [**The constructor shall save the message body by calling super with the body as parameter.**]**
    **Tests_SRS_DEVICEMETHODMESSAGE_25_002: [**If the message body is null, the constructor shall throw an IllegalArgumentException thrown by base constructor.**]**
    */
    @Test
    public void constructorSucceeds()
    {
        //act
        IotHubTransportMessage testDMMessage = new IotHubTransportMessage(new byte[0], MessageType.DEVICE_METHODS);

        //assert
        assertSame(testDMMessage.getMessageType(), MessageType.DEVICE_METHODS);

    }

    /*
    **Tests_SRS_DEVICEMETHODMESSAGE_25_003: [**This method shall set the methodName.**]**
     */
    @Test
    public void setMethodNameSets()
    {
        //arrange
        IotHubTransportMessage testDMMessage = new IotHubTransportMessage(new byte[0], MessageType.DEVICE_METHODS);

        //act
        testDMMessage.setMethodName("TestName");

        //assert
        assertEquals(testDMMessage.getMethodName(), "TestName");

    }

    /*
    **Tests_SRS_DEVICEMETHODMESSAGE_25_004: [**This method shall throw IllegalArgumentException if the methodName is null.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void setMethodNameThrowsOnNull()
    {
        //arrange
        IotHubTransportMessage testDMMessage = new IotHubTransportMessage(new byte[0], MessageType.DEVICE_METHODS);

        //act
        testDMMessage.setMethodName(null);

    }

    /*
    **Tests_SRS_DEVICEMETHODMESSAGE_25_005: [**The method shall return the methodName either set by the setter or the default (null) if unset so far.**]**
     */
    @Test
    public void getMethodNameGets()
    {
        //arrange
        IotHubTransportMessage testDMMessage = new IotHubTransportMessage(new byte[0], MessageType.DEVICE_METHODS);
        testDMMessage.setMethodName("TestName");

        //act
        String actualMethodName = testDMMessage.getMethodName();

        //assert
        assertEquals(actualMethodName, "TestName");

    }

}
