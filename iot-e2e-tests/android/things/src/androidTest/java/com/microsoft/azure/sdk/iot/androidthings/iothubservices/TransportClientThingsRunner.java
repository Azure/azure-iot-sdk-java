/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.androidthings.iothubservices;

import com.microsoft.azure.sdk.iot.androidthings.BuildConfig;
import com.microsoft.azure.sdk.iot.common.helpers.Rerun;
import com.microsoft.azure.sdk.iot.common.tests.iothub.TransportClientTests;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

@RunWith(Parameterized.class)
public class TransportClientThingsRunner extends TransportClientTests
{
    @Rule
    public Rerun count = new Rerun(3);

    public TransportClientThingsRunner(IotHubClientProtocol protocol) throws InterruptedException, IOException, IotHubException, URISyntaxException {
        super(protocol);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection inputs() throws Exception
    {
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        return TransportClientTests.inputs();
    }
}
