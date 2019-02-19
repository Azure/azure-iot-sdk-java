/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.androidthings.iothubservices.twin;

import com.microsoft.azure.sdk.iot.androidthings.BuildConfig;
import com.microsoft.azure.sdk.iot.common.helpers.Rerun;
import com.microsoft.azure.sdk.iot.common.tests.iothubservices.twin.DeviceTwinWithVersionTests;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;

import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Collection;

@RunWith(Parameterized.class)
public class DeviceTwinWithVersionThingsRunner extends DeviceTwinWithVersionTests
{
    @Rule
    public Rerun count = new Rerun(3);

    public DeviceTwinWithVersionThingsRunner(IotHubClientProtocol protocol)
    {
        super(protocol);
    }

    //This function is run before even the @BeforeClass annotation, so it is used as the @BeforeClass method
    @Parameterized.Parameters(name = "{0}")
    public static Collection inputs() throws IOException
    {
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        return inputsCommon();
    }
}
