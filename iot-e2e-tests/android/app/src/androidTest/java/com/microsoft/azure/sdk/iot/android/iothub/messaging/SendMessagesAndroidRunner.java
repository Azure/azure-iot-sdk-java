/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothub.messaging;

import com.microsoft.azure.sdk.iot.android.helper.TestGroup9;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestClientType;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.telemetry.SendMessagesTests;

@TestGroup9
@RunWith(Parameterized.class)
public class SendMessagesAndroidRunner extends SendMessagesTests
{
    public SendMessagesAndroidRunner(IotHubClientProtocol protocol, AuthenticationType authenticationType, TestClientType clientType, boolean useHttpProxy) throws Exception
    {
        super(protocol, authenticationType, clientType, useHttpProxy);
    }
}