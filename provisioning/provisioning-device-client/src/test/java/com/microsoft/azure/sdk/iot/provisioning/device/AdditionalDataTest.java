/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device;

import com.microsoft.azure.sdk.iot.provisioning.device.AdditionalData;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertSame;

@RunWith(JMockit.class)
public class AdditionalDataTest
{
    private static final String TEST_CUSTOM_PAYLOAD = "{ \"a\" : 1 }";

    @Test
    public void setProvisioningPayloadSucceeds() throws Exception
    {
        //act
        AdditionalData testResult = new AdditionalData();

        testResult.setProvisioningPayload(TEST_CUSTOM_PAYLOAD);
    }

    @Test
    public void getProvisioningPayloadSucceeds() throws Exception
    {
        //act
        AdditionalData testResult = new AdditionalData();
        testResult.setProvisioningPayload(TEST_CUSTOM_PAYLOAD);

        assertSame(testResult.getProvisioningPayload(), TEST_CUSTOM_PAYLOAD);
    }
}
