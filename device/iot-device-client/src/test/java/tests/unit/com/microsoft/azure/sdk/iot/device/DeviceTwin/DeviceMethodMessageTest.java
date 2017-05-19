// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.DeviceTwin;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodMessage;
import com.microsoft.azure.sdk.iot.device.MessageType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DeviceMethodMessageTest
{
    /*
    **Tests_SRS_DEVICEMETHODMESSAGE_25_001: [**The constructor shall save the message body by calling super with the body as parameter.**]**
    **Tests_SRS_DEVICEMETHODMESSAGE_25_002: [**If the message body is null, the constructor shall throw an IllegalArgumentException thrown by base constructor.**]**
    */
    @Test
    public void constructorSucceeds()
    {
        //act
        DeviceMethodMessage testDMMessage = new DeviceMethodMessage(new byte[0]);

        //assert
        assertTrue(testDMMessage.getMessageType() == MessageType.DeviceMethods);

    }

    /*
    **Tests_SRS_DEVICEMETHODMESSAGE_25_003: [**This method shall set the methodName.**]**
     */
    @Test
    public void setMethodNameSets()
    {
        //arrange
        DeviceMethodMessage testDMMessage = new DeviceMethodMessage(new byte[0]);

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
        DeviceMethodMessage testDMMessage = new DeviceMethodMessage(new byte[0]);

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
        DeviceMethodMessage testDMMessage = new DeviceMethodMessage(new byte[0]);
        testDMMessage.setMethodName("TestName");

        //act
        String actualMethodName = testDMMessage.getMethodName();

        //assert
        assertEquals(actualMethodName, "TestName");

    }

}
