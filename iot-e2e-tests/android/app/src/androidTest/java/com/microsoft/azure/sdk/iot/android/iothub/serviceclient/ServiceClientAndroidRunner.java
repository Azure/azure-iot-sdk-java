/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.sdk.iot.android.iothub.serviceclient;

import com.microsoft.azure.sdk.iot.android.helper.TestGroup13;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import tests.integration.com.microsoft.azure.sdk.iot.iothub.serviceclient.ServiceClientTests;

@TestGroup13
@RunWith(Parameterized.class)
public class ServiceClientAndroidRunner extends ServiceClientTests
{
    public ServiceClientAndroidRunner(IotHubServiceClientProtocol protocol)
    {
        super(protocol);
    }
}
