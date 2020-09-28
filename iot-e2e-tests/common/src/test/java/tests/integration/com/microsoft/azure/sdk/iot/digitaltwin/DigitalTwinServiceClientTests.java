package tests.integration.com.microsoft.azure.sdk.iot.digitaltwin;

import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.digitaltwin.DigitalTwinAsyncClient;
import com.microsoft.azure.sdk.iot.service.digitaltwin.DigitalTwinClient;
import com.microsoft.azure.sdk.iot.service.digitaltwin.models.BasicDigitalTwin;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.digitaltwin.helpers.E2ETestConstants;
import tests.integration.com.microsoft.azure.sdk.iot.digitaltwin.simulator.TestDigitalTwinDevice;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.DigitalTwinTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.UUID;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT_WS;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@DigitalTwinTest
@Slf4j
@RunWith(Parameterized.class)
public class DigitalTwinServiceClientTests extends IntegrationTest
{

    protected static String iotHubConnectionString = "";
    private TestDigitalTwinDevice testDevice;
    private DigitalTwinClient digitalTwinClient = null;
    private static final String DEVICE_ID_PREFIX = "DigitalTwinE2ETests_";

    @Rule
    public Timeout globalTimeout = Timeout.seconds(5 * 60); // 5 minutes max per method tested

    @Parameterized.Parameter(0)
    public IotHubClientProtocol protocol;

    @Parameterized.Parameters(name = "{index}: Digital Twin Test: protocol={0}")
    public static Collection<Object[]> data() {
        return asList(new Object[][] {
                {MQTT},
                {MQTT_WS},
        });
    }

    @Before
    public void setUp() throws URISyntaxException, IOException, IotHubException {
        testDevice = new TestDigitalTwinDevice(DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString()), protocol);
        testDevice.getDeviceClient().open();
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(E2ETestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        DigitalTwinAsyncClient asyncClient = new DigitalTwinAsyncClient(iotHubConnectionString);
        digitalTwinClient = new DigitalTwinClient(asyncClient);
    }

    @After
    public void cleanUp(){
        testDevice.closeAndDeleteDevice();
    }

    @Test
    @DigitalTwinTest
    public void getDigitalTwin() {
        BasicDigitalTwin getResponse = this.digitalTwinClient.getDigitalTwin(testDevice.getDeviceId(), BasicDigitalTwin.class);
        assertEquals(getResponse.getMetadata().getModelId(), E2ETestConstants.MODEL_ID);
    }

    @Test
    public void getDigitalTwinWithResponse() {
        String digitalTwinId = "";
    }

    @Test
    public void updateDigitalTwin() {
        String digitalTwinId = "";
    }

    @Test
    public void updateDigitalTwinWithResponse() {
        String digitalTwinId = "";
    }

}
