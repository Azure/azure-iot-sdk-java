/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothubservices.errorinjection.twin;

import com.microsoft.azure.sdk.iot.common.helpers.ClientType;
import com.microsoft.azure.sdk.iot.common.helpers.TestConstants;
import com.microsoft.azure.sdk.iot.common.helpers.Tools;
import com.microsoft.azure.sdk.iot.common.helpers.X509Cert;
import com.microsoft.azure.sdk.iot.common.tests.iothubservices.errorinjection.GetTwinErrInjTests;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.BaseDevice;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collection;

@RunWith(Parameterized.class)
public class GetTwinErrInjJVMRunner extends GetTwinErrInjTests
{
    static Collection<BaseDevice> identities;

    public GetTwinErrInjJVMRunner(String deviceId, String moduleId, IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint)
    {
        super(deviceId, moduleId, protocol, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint);
    }

    //This function is run before even the @BeforeClass annotation, so it is used as the @BeforeClass method
    @Parameterized.Parameters(name = "{2} with {3} auth using {4}")
    public static Collection inputs() throws IOException, GeneralSecurityException
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        X509Cert cert = new X509Cert(0,false, "TestLeaf", "TestRoot");
        String privateKey =  cert.getPrivateKeyLeafPem();
        String publicKeyCert = cert.getPublicCertLeafPem();
        String x509Thumbprint = cert.getThumbPrintLeaf();
        Collection inputs = inputsCommon(ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint);

        identities = getIdentities(inputs);

        return inputs;
    }

    @AfterClass
    public static void cleanUpResources()
    {
        tearDown(identities);
    }
}
