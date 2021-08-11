/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothub.twin;

import com.microsoft.azure.sdk.iot.android.helper.TestGroup17;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;

import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestClientType;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.twin.TwinTagsTests;

@TestGroup17
@RunWith(Parameterized.class)
public class TwinTagsAndroidRunner extends TwinTagsTests
{
    public TwinTagsAndroidRunner(IotHubClientProtocol protocol, AuthenticationType authenticationType, TestClientType clientType) throws IOException
    {
        super(protocol, authenticationType, clientType);
    }
}