/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothub.serviceclient;

import com.microsoft.appcenter.espresso.Factory;
import com.microsoft.appcenter.espresso.ReportHelper;
import com.microsoft.azure.sdk.iot.android.BuildConfig;
import com.microsoft.azure.sdk.iot.android.helper.TestGroup37;
import com.microsoft.azure.sdk.iot.common.tests.iothub.serviceclient.RegistryManagerTests;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;

import java.io.IOException;

@TestGroup37
public class RegistryManagerAndroidRunner extends RegistryManagerTests
{
    @Rule
    public ReportHelper reportHelper = Factory.getReportHelper();

    @BeforeClass
    public static void setUp() throws IOException
    {
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        isBasicTierHub = Boolean.parseBoolean(BuildConfig.IsBasicTierHub);
        RegistryManagerTests.setUp();
    }

    @After
    public void labelSnapshot()
    {
        reportHelper.label("Stopping App");
    }
}
