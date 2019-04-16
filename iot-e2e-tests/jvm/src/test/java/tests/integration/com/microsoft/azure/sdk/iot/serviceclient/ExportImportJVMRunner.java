/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.serviceclient;

import com.microsoft.azure.sdk.iot.common.helpers.TestConstants;
import com.microsoft.azure.sdk.iot.common.helpers.Tools;
import com.microsoft.azure.sdk.iot.common.tests.serviceclient.ExportImportTests;
import com.microsoft.azure.storage.StorageException;
import org.junit.BeforeClass;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

public class ExportImportJVMRunner extends ExportImportTests
{
    @BeforeClass
    public static void setUp() throws URISyntaxException, InvalidKeyException, StorageException, IOException
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        storageAccountConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.STORAGE_ACCOUNT_CONNECTION_STRING_ENV_VAR_NAME);
        ExportImportTests.setUp();
    }
}
