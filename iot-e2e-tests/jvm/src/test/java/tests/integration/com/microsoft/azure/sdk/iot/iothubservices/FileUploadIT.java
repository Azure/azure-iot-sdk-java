package tests.integration.com.microsoft.azure.sdk.iot.iothubservices;

import com.microsoft.azure.sdk.iot.common.TestConstants;
import com.microsoft.azure.sdk.iot.common.helpers.Tools;
import com.microsoft.azure.sdk.iot.common.iothubservices.FileUploadCommon;
import org.junit.BeforeClass;

import java.io.IOException;

public class FileUploadIT extends FileUploadCommon
{
    @BeforeClass
    public static void setup() throws IOException
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        FileUploadCommon.setUp();
    }
}
