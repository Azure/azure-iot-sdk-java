/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothub;

import com.microsoft.azure.sdk.iot.android.BuildConfig;
import com.microsoft.azure.sdk.iot.android.helper.TestGroup16;
import com.microsoft.azure.sdk.iot.common.helpers.Rerun;
import com.microsoft.azure.sdk.iot.common.tests.iothub.TransportClientTests;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

@TestGroup16
@RunWith(Parameterized.class)
public class TransportClientAndroidRunner extends TransportClientTests
{
    @Rule
    public Rerun count = new Rerun(3);

    public TransportClientAndroidRunner(IotHubClientProtocol protocol) throws InterruptedException, IOException, IotHubException, URISyntaxException
    {
        super(protocol);
    }

    @Parameterized.Parameters(name = "{0}_{1}")
    public static Collection inputsCommons() throws Exception
    {
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        isBasicTierHub = Boolean.parseBoolean(BuildConfig.IsBasicTierHub);
        isPullRequest = Boolean.parseBoolean(BuildConfig.IsPullRequest);
        return TransportClientTests.inputs();
    }
}
