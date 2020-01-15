/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub;

import com.microsoft.azure.sdk.iot.common.helpers.TestConstants;
import com.microsoft.azure.sdk.iot.common.helpers.Tools;
import com.microsoft.azure.sdk.iot.common.helpers.X509CertificateGenerator;
import com.microsoft.azure.sdk.iot.common.tests.iothub.FileUploadTests;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

@RunWith(Parameterized.class)
public class FileUploadJVMRunner extends FileUploadTests
{
    public FileUploadJVMRunner(IotHubClientProtocol protocol, AuthenticationType authenticationType, boolean withProxy) throws InterruptedException, IOException, IotHubException, URISyntaxException
    {
        super(protocol, authenticationType, withProxy);
    }

    @Parameterized.Parameters(name = "{0} {1} with proxy? {2}")
    public static Collection inputs() throws Exception
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        X509CertificateGenerator certificateGenerator = new X509CertificateGenerator();
        return FileUploadTests.inputs(certificateGenerator.getPublicCertificate(), certificateGenerator.getPrivateKey(), certificateGenerator.getX509Thumbprint());
    }
}
