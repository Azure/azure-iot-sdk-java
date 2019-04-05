/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.serviceclient;

import com.microsoft.appcenter.espresso.Factory;
import com.microsoft.appcenter.espresso.ReportHelper;
import com.microsoft.azure.sdk.iot.android.BuildConfig;
import com.microsoft.azure.sdk.iot.android.helper.TestGroup8;
import com.microsoft.azure.sdk.iot.common.tests.serviceclient.ExportImportTests;
import com.microsoft.azure.storage.StorageException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

@Ignore
@TestGroup8
public class ExportImportAndroidRunner extends ExportImportTests
{
    @Rule
    public ReportHelper reportHelper = Factory.getReportHelper();

    @BeforeClass
    public static void setUp() throws URISyntaxException, InvalidKeyException, StorageException, IOException
    {
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        storageAccountConnectionString = BuildConfig.StorageAccountConnectionString;
        ExportImportTests.setUp();
    }

    @After
    public void labelSnapshot()
    {
        reportHelper.label("Stopping App");
    }
}
