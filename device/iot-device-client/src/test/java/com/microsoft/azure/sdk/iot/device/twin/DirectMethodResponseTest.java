// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.twin;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DirectMethodResponseTest
{
    /*
    **Tests_SRS_DEVICEMETHODDATA_25_001: [**The constructor shall save the status and response message provided by user.**]**
     */
    @Test
    public void constructorSucceeds()
    {
        //act
        DirectMethodResponse testData = new DirectMethodResponse(0, "testMessage");

        //assert
        int testStatus = testData.getStatus();
        Object testResponse = testData.getResponseMessage();

        assertEquals(0, testStatus);
        assertEquals(testResponse, "testMessage");
    }

    /*
    **Tests_SRS_DEVICEMETHODDATA_25_003: [**This method shall return the status previously set.**]**
     */
    @Test
    public void GetStatusGets()
    {
        //arrange
        DirectMethodResponse testData = new DirectMethodResponse(0, "testMessage");

        //act
        int testStatus = testData.getStatus();

        //assert
        assertEquals(0, testStatus);

    }

    /*
    **Tests_SRS_DEVICEMETHODDATA_25_004: [**This method shall return the response message previously set.**]**
     */
    @Test
    public void getResponseMessageGets()
    {
        //arrange
        DirectMethodResponse testData = new DirectMethodResponse(0, "testMessage");

        //act
        Object testResponse = testData.getResponseMessage();

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
        DirectMethodResponse testData = new DirectMethodResponse(0, "testMessage");

        //act
        testData.setStatus(200);

        //assert
        int testStatus = testData.getStatus();
        assertEquals(200, testStatus);

    }

    /*
    **Tests_SRS_DEVICEMETHODDATA_25_005: [**This method shall save the response message provided by the user.**]**
     */
    @Test
    public void setResponseMessageSets()
    {
        //arrange
        DirectMethodResponse testData = new DirectMethodResponse(0, "originalMessage");

        //act
        testData.setResponseMessage("testMessage");

        //assert
        Object testResponse = testData.getResponseMessage();
        assertEquals(testResponse, "testMessage");

    }

}
