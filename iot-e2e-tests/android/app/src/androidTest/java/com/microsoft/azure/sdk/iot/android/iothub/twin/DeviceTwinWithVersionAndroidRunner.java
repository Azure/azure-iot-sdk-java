/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothub.twin;

import com.microsoft.azure.sdk.iot.android.BuildConfig;
import com.microsoft.azure.sdk.iot.android.helper.TestGroup13;
import com.microsoft.azure.sdk.iot.common.tests.iothub.twin.DeviceTwinWithVersionTests;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Collection;

@TestGroup13
@RunWith(Parameterized.class)
public class DeviceTwinWithVersionAndroidRunner extends DeviceTwinWithVersionTests
{
    public DeviceTwinWithVersionAndroidRunner(IotHubClientProtocol protocol) throws IOException
    {
        super(protocol);
    }

    //This function is run before even the @BeforeClass annotation, so it is used as the @BeforeClass method
    @Parameterized.Parameters(name = "{0}")
    public static Collection inputs() throws IOException
    {
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        isBasicTierHub = Boolean.parseBoolean(BuildConfig.IsBasicTierHub);
        return inputsCommon();
    }
}
