/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.serviceclient;

import com.microsoft.azure.sdk.iot.common.TestConstants;
import com.microsoft.azure.sdk.iot.common.helpers.Tools;
import com.microsoft.azure.sdk.iot.common.serviceclient.ExportImportCommon;
import com.microsoft.azure.storage.StorageException;
import org.junit.BeforeClass;
import org.junit.Ignore;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

@Ignore
public class ExportImportIT extends ExportImportCommon
{
    @BeforeClass
    public static void setUp() throws URISyntaxException, InvalidKeyException, StorageException, IOException
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        storageAccountConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.STORAGE_ACCOUNT_CONNECTION_STRING_ENV_VAR_NAME);
        ExportImportCommon.setUp();
    }
}
