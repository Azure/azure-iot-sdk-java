package tests.integration.com.microsoft.azure.sdk.iot.iothubservices;

import com.microsoft.azure.sdk.iot.common.TestConstants;
import com.microsoft.azure.sdk.iot.common.helpers.Tools;
import com.microsoft.azure.sdk.iot.common.iothubservices.DeviceTwinWithVersionCommon;
import org.junit.BeforeClass;

import java.io.IOException;

public class DeviceTwinWithVersionIT extends DeviceTwinWithVersionCommon
{
    @BeforeClass
    public static void setup() throws IOException
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        DeviceTwinWithVersionCommon.setUp();
    }
}
