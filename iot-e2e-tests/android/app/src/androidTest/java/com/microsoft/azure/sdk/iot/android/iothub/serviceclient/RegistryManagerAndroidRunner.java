/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothub.serviceclient;

import com.microsoft.azure.sdk.iot.android.BuildConfig;
import com.microsoft.azure.sdk.iot.android.helper.TestGroup14;
import com.microsoft.azure.sdk.iot.common.tests.iothub.serviceclient.RegistryManagerTests;

import org.junit.BeforeClass;

import java.io.IOException;

@TestGroup14
public class RegistryManagerAndroidRunner extends RegistryManagerTests
{
    @BeforeClass
    public static void setUp() throws IOException
    {
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        isBasicTierHub = Boolean.parseBoolean(BuildConfig.IsBasicTierHub);
        RegistryManagerTests.setUp();
    }
}
