/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothub.errorinjection;

import com.microsoft.azure.sdk.iot.android.helper.TestGroup3;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import tests.integration.com.microsoft.azure.sdk.iot.helpers.ClientType;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.errorinjection.ReceiveMessagesErrInjTests;

@TestGroup3
@RunWith(Parameterized.class)
public class ReceiveMessagesErrInjAndroidRunner extends ReceiveMessagesErrInjTests
{
    public ReceiveMessagesErrInjAndroidRunner(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws Exception
    {
        super(protocol, authenticationType, clientType);
    }
}
