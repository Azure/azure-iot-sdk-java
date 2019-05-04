/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothubservices;

import com.microsoft.appcenter.espresso.Factory;
import com.microsoft.appcenter.espresso.ReportHelper;
import com.microsoft.azure.sdk.iot.android.BuildConfig;
import com.microsoft.azure.sdk.iot.android.helper.TestGroup3;
import com.microsoft.azure.sdk.iot.android.helper.TestGroup6;
import com.microsoft.azure.sdk.iot.common.helpers.Rerun;
import com.microsoft.azure.sdk.iot.common.tests.iothubservices.TokenRenewalTests;
import com.microsoft.azure.sdk.iot.deps.util.Base64;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

@TestGroup3
public class TokenRenewalAndroidRunner extends TokenRenewalTests
{
    @Rule
    public Rerun count = new Rerun(3);

    @Rule
    public ReportHelper reportHelper = Factory.getReportHelper();

    @BeforeClass
    public static void setup() throws IOException
    {
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        isBasicTierHub = Boolean.parseBoolean(BuildConfig.IsBasicTierHub);
        TokenRenewalTests.setup();
    }

    @After
    public void labelSnapshot()
    {
        reportHelper.label("Stopping App");
    }
}
