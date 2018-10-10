/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.serviceclient;

import com.microsoft.appcenter.espresso.Factory;
import com.microsoft.appcenter.espresso.ReportHelper;
import com.microsoft.azure.sdk.iot.android.BuildConfig;
import com.microsoft.azure.sdk.iot.common.serviceclient.ExportImportCommon;
import com.microsoft.azure.storage.StorageException;
import org.junit.BeforeClass;
import org.junit.Rule;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

public class ExportImportIT extends ExportImportCommon
{
    @Rule
    public ReportHelper reportHelper = Factory.getReportHelper();

    @BeforeClass
    public static void setUp() throws URISyntaxException, InvalidKeyException, StorageException, IOException
    {
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        storageAccountConnectionString = BuildConfig.IotHubInvalidCertConnectionString;
        ExportImportCommon.setUp();
    }
}
