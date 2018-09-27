/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android;

import android.os.Bundle;
import android.support.test.InstrumentationRegistry;

import com.microsoft.azure.sdk.iot.common.iothubservices.SendMessagesCommon;
import com.microsoft.azure.sdk.iot.common.TestConstants;
import com.microsoft.azure.sdk.iot.device.InternalClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.android.helper.Tools;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Collection;

@RunWith(Parameterized.class)
public class SendMessagesIT extends SendMessagesCommon
{
    //This function is run before even the @BeforeClass annotation, so it is used as the @BeforeClass method
    @Parameterized.Parameters(name = "{1} with {3} auth using {4}")
    public static Collection inputs() throws IOException, IotHubException, GeneralSecurityException, URISyntaxException, ModuleClientException
    {
        Bundle bundle = InstrumentationRegistry.getArguments();
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME, bundle);
        privateKey = Tools.retrieveEnvironmentVariableValue("IOTHUB_E2E_X509_PRIVATE_KEY_BASE64", bundle);
        publicKeyCert = Tools.retrieveEnvironmentVariableValue("IOTHUB_E2E_X509_CERT_BASE64", bundle);
        x509Thumbprint = Tools.retrieveEnvironmentVariableValue("IOTHUB_E2E_X509_THUMBPRINT", bundle);

        return SendMessagesCommon.inputsCommon();
    }

    public SendMessagesIT(InternalClient client, IotHubClientProtocol protocol, Device device, AuthenticationType authenticationType, String clientType)
    {
        super(client, protocol, device, authenticationType, clientType);
    }
}