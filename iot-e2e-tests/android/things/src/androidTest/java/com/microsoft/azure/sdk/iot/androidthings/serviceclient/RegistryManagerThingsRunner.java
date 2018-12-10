/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.androidthings.serviceclient;

import com.microsoft.azure.sdk.iot.androidthings.BuildConfig;
import com.microsoft.azure.sdk.iot.common.tests.serviceclient.RegistryManagerTests;
import org.junit.BeforeClass;

import java.io.IOException;

public class RegistryManagerThingsRunner extends RegistryManagerTests
{
    @BeforeClass
    public static void setUp() throws IOException
    {
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        RegistryManagerTests.setUp();
    }
}
