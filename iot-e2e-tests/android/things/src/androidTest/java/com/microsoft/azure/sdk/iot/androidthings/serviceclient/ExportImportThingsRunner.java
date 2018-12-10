/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.androidthings.serviceclient;

import com.microsoft.azure.sdk.iot.androidthings.BuildConfig;
import com.microsoft.azure.sdk.iot.common.tests.serviceclient.ExportImportTests;
import com.microsoft.azure.storage.StorageException;
import org.junit.BeforeClass;
import org.junit.Ignore;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

@Ignore
public class ExportImportThingsRunner extends ExportImportTests
{
    @BeforeClass
    public static void setUp() throws URISyntaxException, InvalidKeyException, StorageException, IOException
    {
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        storageAccountConnectionString = BuildConfig.StorageAccountConnectionString;
        ExportImportTests.setUp();
    }
}
