/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothub.errorinjection.messaging;

import com.microsoft.azure.sdk.iot.android.helper.TestGroup4;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestClientType;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.errorinjection.SendMessagesErrInjTests;

@TestGroup4
@RunWith(Parameterized.class)
public class SendMessagesErrInjAndroidRunner extends SendMessagesErrInjTests
{
    public SendMessagesErrInjAndroidRunner(IotHubClientProtocol protocol, AuthenticationType authenticationType, TestClientType clientType, boolean useHttpProxy) throws Exception
    {
        super(protocol, authenticationType, clientType, useHttpProxy);
    }
}
