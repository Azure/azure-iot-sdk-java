/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothub.serviceclient;

import com.microsoft.azure.sdk.iot.android.BuildConfig;
import com.microsoft.azure.sdk.iot.android.helper.TestGroup11;
import com.microsoft.azure.sdk.iot.common.tests.iothub.serviceclient.JobClientTests;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import org.junit.BeforeClass;
import org.junit.Ignore;

import java.io.IOException;
import java.net.URISyntaxException;

@Ignore
@TestGroup11
public class JobClientAndroidRunner extends JobClientTests
{
    @BeforeClass
    public static void setUp() throws IOException, IotHubException, InterruptedException, URISyntaxException
    {
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        isPullRequest = Boolean.parseBoolean(BuildConfig.IsPullRequest);
        JobClientTests.setUp();
    }
}
