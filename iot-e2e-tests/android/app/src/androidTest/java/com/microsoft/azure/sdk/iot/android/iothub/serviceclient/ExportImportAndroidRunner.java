/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothub.serviceclient;

import com.microsoft.azure.sdk.iot.android.BuildConfig;
import com.microsoft.azure.sdk.iot.android.helper.TestGroup11;
import com.microsoft.azure.sdk.iot.common.tests.iothub.serviceclient.ExportImportTests;
import com.microsoft.azure.storage.StorageException;

import org.junit.BeforeClass;
import org.junit.Ignore;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

@Ignore
@TestGroup11
public class ExportImportAndroidRunner extends ExportImportTests
{
    @BeforeClass
    public static void setUp() throws URISyntaxException, InvalidKeyException, StorageException, IOException
    {
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        storageAccountConnectionString = BuildConfig.StorageAccountConnectionString;
        ExportImportTests.setUp();
    }
}
