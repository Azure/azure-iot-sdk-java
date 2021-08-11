/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothub.methods;

import com.microsoft.azure.sdk.iot.android.helper.TestGroup10;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestClientType;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.methods.DeviceMethodTests;

@TestGroup10
@RunWith(Parameterized.class)
public class DeviceMethodAndroidRunner extends DeviceMethodTests
{
    public DeviceMethodAndroidRunner(IotHubClientProtocol protocol, AuthenticationType authenticationType, TestClientType clientType) throws Exception
    {
        super(protocol, authenticationType, clientType);
    }
}
