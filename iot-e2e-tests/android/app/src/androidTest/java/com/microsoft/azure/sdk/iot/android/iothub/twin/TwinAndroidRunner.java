/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothub.twin;

import com.microsoft.azure.sdk.iot.android.helper.TestGroup10;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.ClientType;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.twin.TwinTests;

@TestGroup10
@RunWith(Parameterized.class)
public class TwinAndroidRunner extends TwinTests
{
    public TwinAndroidRunner(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws Exception
    {
        super(protocol, authenticationType, clientType);
    }
}
