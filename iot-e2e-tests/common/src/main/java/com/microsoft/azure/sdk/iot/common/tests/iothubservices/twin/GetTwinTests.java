/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.tests.iothubservices.twin;

import com.microsoft.azure.sdk.iot.common.helpers.ClientType;
import com.microsoft.azure.sdk.iot.common.helpers.ConditionalIgnoreRule;
import com.microsoft.azure.sdk.iot.common.helpers.StandardTierOnlyRule;
import com.microsoft.azure.sdk.iot.common.setup.DeviceTwinCommon;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.Test;

import java.io.IOException;

/**
 * Test class containing all non error injection tests to be run on JVM and android pertaining to getDeviceTwin/getTwin. Class needs to be extended
 * in order to run these tests as that extended class handles setting connection strings and certificate generation
 */
public class GetTwinTests extends DeviceTwinCommon
{
    public GetTwinTests(String deviceId, String moduleId, IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint)
    {
        super(deviceId, moduleId, protocol, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint);
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void testGetDeviceTwin() throws IOException, InterruptedException, IotHubException
    {
        super.testGetDeviceTwin();
    }
}
