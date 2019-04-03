/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothubservices;

import com.microsoft.appcenter.espresso.Factory;
import com.microsoft.appcenter.espresso.ReportHelper;
import com.microsoft.azure.sdk.iot.android.BuildConfig;
import com.microsoft.azure.sdk.iot.android.helper.TestGroup6;
import com.microsoft.azure.sdk.iot.common.helpers.Rerun;
import com.microsoft.azure.sdk.iot.common.tests.iothubservices.FileUploadTests;
import com.microsoft.azure.sdk.iot.deps.util.Base64;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

@TestGroup6
public class FileUploadAndroidRunner extends FileUploadTests
{
    @Rule
    public Rerun count = new Rerun(3);

    @Rule
    public ReportHelper reportHelper = Factory.getReportHelper();

    @BeforeClass
    public static void setup() throws IOException
    {
        String privateKeyBase64Encoded = BuildConfig.IotHubPrivateKeyBase64;
        String publicKeyCertBase64Encoded = BuildConfig.IotHubPublicCertBase64;
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        String x509Thumbprint = BuildConfig.IotHubThumbprint;
        String privateKey = new String(Base64.decodeBase64Local(privateKeyBase64Encoded.getBytes()));
        String publicKeyCert = new String(Base64.decodeBase64Local(publicKeyCertBase64Encoded.getBytes()));
        FileUploadTests.setUp(publicKeyCert, privateKey, x509Thumbprint);
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
