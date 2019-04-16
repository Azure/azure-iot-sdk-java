/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothubservices.twin;

import com.microsoft.appcenter.espresso.Factory;
import com.microsoft.appcenter.espresso.ReportHelper;
import com.microsoft.azure.sdk.iot.android.BuildConfig;
import com.microsoft.azure.sdk.iot.android.helper.TestGroup9;
import com.microsoft.azure.sdk.iot.common.tests.iothubservices.twin.DeviceTwinWithVersionTests;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;

import org.junit.Rule;
import org.junit.After;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Collection;

@TestGroup9
@RunWith(Parameterized.class)
public class DeviceTwinWithVersionAndroidRunner extends DeviceTwinWithVersionTests
{
    @Rule
    public ReportHelper reportHelper = Factory.getReportHelper();

    public DeviceTwinWithVersionAndroidRunner(IotHubClientProtocol protocol)
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

    @After
    public void labelSnapshot()
    {
        reportHelper.label("Stopping App");
    }
}
