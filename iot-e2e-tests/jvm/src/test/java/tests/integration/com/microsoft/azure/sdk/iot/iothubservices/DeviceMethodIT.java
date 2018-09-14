package tests.integration.com.microsoft.azure.sdk.iot.iothubservices;

import com.microsoft.azure.sdk.iot.common.TestConstants;
import com.microsoft.azure.sdk.iot.common.X509Cert;
import com.microsoft.azure.sdk.iot.common.helpers.DeviceTestManager;
import com.microsoft.azure.sdk.iot.common.helpers.Tools;
import com.microsoft.azure.sdk.iot.common.iothubservices.DeviceMethodCommon;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.Module;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Collection;

@RunWith(Parameterized.class)
public class DeviceMethodIT extends DeviceMethodCommon
{
    //This function is run before even the @BeforeClass annotation, so it is used as the @BeforeClass method
    @Parameterized.Parameters(name = "{1} with {2} auth using {3}")
    public static Collection inputsCommons() throws IOException, IotHubException, GeneralSecurityException, URISyntaxException, InterruptedException, ModuleClientException
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        X509Cert cert = new X509Cert(0,false, "TestLeaf", "TestRoot");
        privateKey =  cert.getPrivateKeyLeafPem();
        publicKeyCert = cert.getPublicCertLeafPem();
        x509Thumbprint = cert.getThumbPrintLeaf();
        return DeviceMethodCommon.inputsCommon();
    }

    public DeviceMethodIT(DeviceTestManager deviceTestManager, IotHubClientProtocol protocol, AuthenticationType authenticationType, String clientType, Device device, Module module)
    {
        super(deviceTestManager, protocol, authenticationType, clientType, device, module);
    }
}
