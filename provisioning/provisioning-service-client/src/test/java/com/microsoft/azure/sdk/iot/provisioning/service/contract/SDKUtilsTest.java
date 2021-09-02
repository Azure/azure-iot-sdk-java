// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.contract;

import mockit.Deencapsulation;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for Device Provisioning Service Contract APIs for HTTP.
 * 100% methods, 100% lines covered
 */
public class SDKUtilsTest
{
    /* SRS_SDK_UTILS_21_001: [The getServiceApiVersion shall return a string with the rest API version.] */
    @Test
    public void getServiceApiVersionSucceed()
    {
        // arrange
        // act
        String serviceApiVersion = Deencapsulation.invoke("com.microsoft.azure.sdk.iot.provisioning.service.contract.SDKUtils", "getServiceApiVersion");

        // assert
        assertNotNull(serviceApiVersion);
        assertFalse(serviceApiVersion.isEmpty());
    }

    /* SRS_SDK_UTILS_21_002: [The getUserAgentString shall return a string with the SDK name and version separated by `/`.] */
    @Test
    public void getUserAgentStringSucceed()
    {
        // arrange
        // act
        String userAgentString = Deencapsulation.invoke("com.microsoft.azure.sdk.iot.provisioning.service.contract.SDKUtils", "getUserAgentString");

        // assert
        assertNotNull(userAgentString);
        assertFalse(userAgentString.isEmpty());
        String[] fields = userAgentString.split("/");
        assertTrue(fields.length > 1);
    }

}
