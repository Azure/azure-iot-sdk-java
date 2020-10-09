/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothub.twin;

import com.microsoft.azure.sdk.iot.android.helper.TestGroup12;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;

import tests.integration.com.microsoft.azure.sdk.iot.helpers.ClientType;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.twin.DesiredPropertiesTests;

@TestGroup12
@RunWith(Parameterized.class)
public class DesiredPropertiesAndroidRunner extends DesiredPropertiesTests
{
    public DesiredPropertiesAndroidRunner(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint) throws IOException
    {
        super(protocol, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint);
    }
}