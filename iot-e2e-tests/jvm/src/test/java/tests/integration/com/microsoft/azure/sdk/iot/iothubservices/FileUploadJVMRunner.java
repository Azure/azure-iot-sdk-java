/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothubservices;

import com.microsoft.azure.sdk.iot.common.helpers.TestConstants;
import com.microsoft.azure.sdk.iot.common.helpers.Tools;
import com.microsoft.azure.sdk.iot.common.helpers.X509Cert;
import com.microsoft.azure.sdk.iot.common.tests.iothubservices.FileUploadTests;
import org.junit.BeforeClass;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class FileUploadJVMRunner extends FileUploadTests
{
    @BeforeClass
    public static void setup() throws IOException, GeneralSecurityException
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        X509Cert cert = new X509Cert(0,false, "TestLeaf", "TestRoot");
        String publicKeyCert = cert.getPublicCertLeafPem();
        String privateKey =  cert.getPrivateKeyLeafPem();
        String x509Thumbprint = cert.getThumbPrintLeaf();
        FileUploadTests.setUp(publicKeyCert, privateKey, x509Thumbprint);
    }
}
