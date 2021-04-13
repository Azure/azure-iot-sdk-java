/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothub.errorinjection.twin;

import com.microsoft.azure.sdk.iot.android.helper.TestGroup6;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;

import tests.integration.com.microsoft.azure.sdk.iot.helpers.ClientType;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.errorinjection.DesiredPropertiesErrInjTests;

@TestGroup6
@RunWith(Parameterized.class)
public class DesiredPropertiesErrInjAndroidRunner extends DesiredPropertiesErrInjTests
{
    public DesiredPropertiesErrInjAndroidRunner(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws IOException
    {
        super(protocol, authenticationType, clientType);
    }
}