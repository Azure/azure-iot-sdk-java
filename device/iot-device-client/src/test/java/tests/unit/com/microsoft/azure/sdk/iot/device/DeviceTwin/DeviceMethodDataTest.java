// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.DeviceTwin;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DeviceMethodDataTest
{
    /*
    **Tests_SRS_DEVICEMETHODDATA_25_001: [**The constructor shall save the status and response message provided by user.**]**
     */
    @Test
    public void constructorSucceeds()
    {
        //act
        DeviceMethodData testData = new DeviceMethodData(0, "testMessage");

        //assert
        int testStatus = testData.getStatus();
        String testResponse = testData.getResponseMessage();

        assertTrue(testStatus == 0);
        assertEquals(testResponse, "testMessage");
    }

    /*
    **Tests_SRS_DEVICEMETHODDATA_25_003: [**This method shall return the status previously set.**]**
     */
    @Test
    public void GetStatusGets()
    {
        //arrange
        DeviceMethodData testData = new DeviceMethodData(0, "testMessage");

        //act
        int testStatus = testData.getStatus();

        //assert
        assertTrue(testStatus == 0);

    }

    /*
    **Tests_SRS_DEVICEMETHODDATA_25_004: [**This method shall return the response message previously set.**]**
     */
    @Test
    public void getResponseMessageGets()
    {
        //arrange
        DeviceMethodData testData = new DeviceMethodData(0, "testMessage");

        //act
        String testResponse = testData.getResponseMessage();

        //assert
        assertEquals(testResponse, "testMessage");
    }

    /*
    **Tests_SRS_DEVICEMETHODDATA_25_007: [**The method shall set the status.**]**
     */
    @Test
    public void setStatusSets()
    {
        //arrange
        DeviceMethodData testData = new DeviceMethodData(0, "testMessage");

        //act
        testData.setStatus(200);

        //assert
        int testStatus = testData.getStatus();
        assertTrue(testStatus == 200);

    }

    /*
    **Tests_SRS_DEVICEMETHODDATA_25_005: [**This method shall save the response message provided by the user.**]**
     */
    @Test
    public void setResponseMessageSets()
    {
        //arrange
        DeviceMethodData testData = new DeviceMethodData(0, "originalMessage");

        //act
        testData.setResponseMessage("testMessage");

        //assert
        String testResponse = testData.getResponseMessage();
        assertEquals(testResponse, "testMessage");

    }

}
