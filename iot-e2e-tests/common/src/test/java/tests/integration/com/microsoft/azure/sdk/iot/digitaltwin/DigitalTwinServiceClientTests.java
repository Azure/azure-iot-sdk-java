package tests.integration.com.microsoft.azure.sdk.iot.digitaltwin;

import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import org.junit.Test;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.DigitalTwinTest;

import java.io.IOException;

@DigitalTwinTest
public class DigitalTwinServiceClientTests {

    @Test
    public void getDigitalTwin() {
        String digitalTwinId = setup();
    }

    @Test
    public void getDigitalTwinWithResponse() {
        String digitalTwinId = setup();
    }

    @Test
    public void updateDigitalTwin() {
        String digitalTwinId = setup();
    }

    @Test
    public void updateDigitalTwinWithResponse() {
        String digitalTwinId = setup();
    }

    private String setup()
    {
        // Create a device
        // Call Open using AMQP and pass the modelId to make the device plug and play.
        return "digitalTwinId";
    }
}
