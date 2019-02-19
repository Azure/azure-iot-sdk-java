/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.androidthings.iothubservices;

import com.microsoft.azure.sdk.iot.androidthings.BuildConfig;
import com.microsoft.azure.sdk.iot.common.helpers.Rerun;
import com.microsoft.azure.sdk.iot.common.tests.iothubservices.TransportClientTests;

import org.junit.BeforeClass;
import org.junit.Rule;

public class TransportClientThingsRunner extends TransportClientTests
{
    @Rule
    public Rerun count = new Rerun(3);

    @BeforeClass
    public static void setup() throws Exception
    {
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        setUpCommon();
    }
}
