/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.sdk.iot.android.serviceclient;

import com.microsoft.appcenter.espresso.Factory;
import com.microsoft.appcenter.espresso.ReportHelper;
import com.microsoft.azure.sdk.iot.android.BuildConfig;
import com.microsoft.azure.sdk.iot.android.helper.TestGroup1;
import com.microsoft.azure.sdk.iot.common.tests.serviceclient.ServiceClientTests;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;

import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.After;

import java.io.IOException;
import java.util.Collection;

@TestGroup1
@RunWith(Parameterized.class)
public class ServiceClientAndroidRunner extends ServiceClientTests
{
    @Rule
    public ReportHelper reportHelper = Factory.getReportHelper();

    //This function is run before even the @BeforeClass annotation, so it is used as the @BeforeClass method
    @Parameterized.Parameters(name = "{0}")
    public static Collection inputsCommon() throws IOException
    {
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        invalidCertificateServerConnectionString = BuildConfig.IotHubInvalidCertConnectionString;
        return ServiceClientTests.inputsCommon();
    }

    public ServiceClientAndroidRunner(IotHubServiceClientProtocol protocol)
    {
        super(protocol);
    }

    @After
    public void labelSnapshot()
    {
        reportHelper.label("Stopping App");
    }
}
