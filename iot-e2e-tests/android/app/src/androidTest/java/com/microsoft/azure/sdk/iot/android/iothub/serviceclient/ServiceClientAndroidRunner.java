/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.sdk.iot.android.iothub.serviceclient;

import com.microsoft.azure.sdk.iot.android.BuildConfig;
import com.microsoft.azure.sdk.iot.android.helper.TestGroup13;
import com.microsoft.azure.sdk.iot.common.tests.iothub.serviceclient.ServiceClientTests;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Collection;

@TestGroup13
@RunWith(Parameterized.class)
public class ServiceClientAndroidRunner extends ServiceClientTests
{
    //This function is run before even the @BeforeClass annotation, so it is used as the @BeforeClass method
    @Parameterized.Parameters(name = "{0}")
    public static Collection inputsCommon() throws IOException
    {
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        isBasicTierHub = Boolean.parseBoolean(BuildConfig.IsBasicTierHub);
        invalidCertificateServerConnectionString = BuildConfig.IotHubInvalidCertConnectionString;
        return ServiceClientTests.inputsCommon();
    }

    public ServiceClientAndroidRunner(IotHubServiceClientProtocol protocol)
    {
        super(protocol);
    }
}
