/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.sdk.iot.android.serviceclient;

import com.microsoft.appcenter.espresso.Factory;
import com.microsoft.appcenter.espresso.ReportHelper;
import com.microsoft.azure.sdk.iot.android.BuildConfig;
import com.microsoft.azure.sdk.iot.common.serviceclient.ServiceClientCommon;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.util.Collection;

@RunWith(Parameterized.class)
public class ServiceClientIT extends ServiceClientCommon
{
    @Rule
    public ReportHelper reportHelper = Factory.getReportHelper();

    //This function is run before even the @BeforeClass annotation, so it is used as the @BeforeClass method
    @Parameterized.Parameters(name = "{0}")
    public static Collection inputsCommon()
    {
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        invalidCertificateServerConnectionString = BuildConfig.IotHubInvalidCertConnectionString;
        return ServiceClientCommon.inputsCommon();
    }

    public ServiceClientIT(IotHubServiceClientProtocol protocol)
    {
        super(protocol);
    }
}
