/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.sdk.iot.androidthings.serviceclient;

import com.microsoft.azure.sdk.iot.androidthings.BuildConfig;
import com.microsoft.azure.sdk.iot.common.tests.serviceclient.ServiceClientTests;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

@RunWith(Parameterized.class)
public class ServiceClientThingsRunner extends ServiceClientTests
{
    //This function is run before even the @BeforeClass annotation, so it is used as the @BeforeClass method
    @Parameterized.Parameters(name = "{0}")
    public static Collection inputsCommon()
    {
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        invalidCertificateServerConnectionString = BuildConfig.IotHubInvalidCertConnectionString;
        return ServiceClientTests.inputsCommon();
    }

    public ServiceClientThingsRunner(IotHubServiceClientProtocol protocol)
    {
        super(protocol);
    }
}
