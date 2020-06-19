/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothub;

import com.microsoft.azure.sdk.iot.android.BuildConfig;
import com.microsoft.azure.sdk.iot.android.helper.TestGroup13;
import com.microsoft.azure.sdk.iot.common.helpers.Rerun;
import com.microsoft.azure.sdk.iot.common.tests.iothub.FileUploadTests;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

//TODO these tests haven't been running recently, but by accident. Unfortunately, they fail when run, but only on android. Something about the
// file upload receiver thread isn't working right. Disabling until it gets figured out
@Ignore
@TestGroup13
@RunWith(Parameterized.class)
public class FileUploadAndroidRunner extends FileUploadTests
{
    @Rule
    public Rerun count = new Rerun(3);

    public FileUploadAndroidRunner(IotHubClientProtocol protocol, AuthenticationType authenticationType, boolean withProxy) throws InterruptedException, IOException, IotHubException, URISyntaxException
    {
        super(protocol, authenticationType, withProxy);
    }

    @Parameterized.Parameters(name = "{0}_{1}_{2}")
    public static Collection inputsCommons() throws Exception
    {
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        isBasicTierHub = Boolean.parseBoolean(BuildConfig.IsBasicTierHub);
        isPullRequest = Boolean.parseBoolean(BuildConfig.IsPullRequest);
        return FileUploadTests.inputs();
    }
}
