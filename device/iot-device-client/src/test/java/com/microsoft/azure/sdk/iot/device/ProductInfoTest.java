/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.ProductInfo;
import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;
import mockit.Deencapsulation;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Unit tests for ProductInfo
 * 100% methods
 * 100% lines
 */
public class ProductInfoTest
{
    //Tests_SRS_PRODUCTINFO_34_001: [This function shall set the extra field to an empty string.]
    @Test
    public void constructorSetsEmptyExtra()
    {
        //act
        ProductInfo actual = new ProductInfo();

        //assert
        assertEquals("", Deencapsulation.getField(actual, "extra"));
    }

    //Tests_SRS_PRODUCTINFO_34_002: [If the saved extra field is an empty string, this function shall return just the user agent string defined in TransportUtils.]
    @Test
    public void getUserAgentStringWithoutExtra()
    {
        //arrange
        ProductInfo actual = new ProductInfo();

        //act
        String actualUserAgentString = actual.getUserAgentString();

        //assert
        assertEquals(TransportUtils.USER_AGENT_STRING, actualUserAgentString);
    }

    //Tests_SRS_PRODUCTINFO_34_003: [If the saved extra field is not an empty string, this function shall return the user agent string defined in TransportUtils with the extra string appended.]
    @Test
    public void getUserAgentStringWithExtra()
    {
        //arrange
        ProductInfo actual = new ProductInfo();
        String expectedExtra = "some extra information";
        actual.setExtra(expectedExtra);

        //act
        String actualUserAgentString = actual.getUserAgentString();

        //assert
        assertEquals(TransportUtils.USER_AGENT_STRING + " " + expectedExtra, actualUserAgentString);
    }


    //Tests_SRS_PRODUCTINFO_34_004: [This function shall save the provided extra.]
    @Test
    public void setExtraSavesExtra()
    {
        //arrange
        String expectedExtra = "some user agent extra";
        ProductInfo actual = new ProductInfo();

        //act
        actual.setExtra(expectedExtra);

        //assert
        assertEquals(expectedExtra, Deencapsulation.getField(actual, "extra"));
    }

}
