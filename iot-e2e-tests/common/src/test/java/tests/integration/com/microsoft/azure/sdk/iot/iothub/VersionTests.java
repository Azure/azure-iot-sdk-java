package tests.integration.com.microsoft.azure.sdk.iot.iothub;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;

@Slf4j
@IotHubTest
public class VersionTests
{
    @Test(timeout = 60000) // 1 minute
    public void checkVersion()
    {
        String version = System.getProperty("java.version");
        Assert.assertEquals(1+"", version);
    }
}
