/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothub;

import com.microsoft.appcenter.espresso.Factory;
import com.microsoft.appcenter.espresso.ReportHelper;
import com.microsoft.azure.sdk.iot.android.BuildConfig;
import com.microsoft.azure.sdk.iot.android.helper.TestGroup30;
import com.microsoft.azure.sdk.iot.common.helpers.Rerun;
import com.microsoft.azure.sdk.iot.common.tests.iothub.FileUploadTests;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

@TestGroup30
public class FileUploadAndroidRunner extends FileUploadTests
{
    @Rule
    public Rerun count = new Rerun(3);

    @Rule
    public ReportHelper reportHelper = Factory.getReportHelper();

    public FileUploadAndroidRunner(IotHubClientProtocol protocol, AuthenticationType authenticationType, boolean withProxy) throws InterruptedException, IOException, IotHubException, URISyntaxException
    {
        super(protocol, authenticationType, withProxy);
    }

    @Parameterized.Parameters(name = "{0}_{1}_{2}")
    public static Collection inputsCommons() throws Exception
    {
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        isBasicTierHub = Boolean.parseBoolean(BuildConfig.IsBasicTierHub);
        return FileUploadTests.inputs();
    }

    @Ignore
    @Override
    @Test
    public void uploadToBlobAsyncSingleFileZeroLength()
    {
    }

    @After
    public void labelSnapshot()
    {
        reportHelper.label("Stopping App");
    }
}
