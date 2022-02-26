/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothub.twin;

import com.microsoft.azure.sdk.iot.android.helper.TestGroup11;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

import tests.integration.com.microsoft.azure.sdk.iot.helpers.ClientType;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.twin.TwinTests;

@TestGroup11
@RunWith(Parameterized.class)
public class TwinAndroidRunner extends TwinTests
{
    public TwinAndroidRunner(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws IOException, InterruptedException, IotHubException, ModuleClientException, GeneralSecurityException, URISyntaxException
    {
        super(protocol, authenticationType, clientType);
    }
}
