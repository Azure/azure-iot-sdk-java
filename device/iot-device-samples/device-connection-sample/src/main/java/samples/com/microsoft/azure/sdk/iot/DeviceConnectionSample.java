package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import java.io.IOException;

public class DeviceConnectionSample
{
    // This sample is only useful for MQTT, MQTT_WS, AMQPS, and AMQPS_WS,  since they are the only stateful connection protocols.
    public static void main(String[] args) throws InterruptedException, IOException, IotHubException
    {
        final String connectionString = "";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final DeviceClient deviceClient = new DeviceClient(connectionString, protocol);
        DeviceClientManager deviceClientManager = new DeviceClientManager(deviceClient);

        deviceClientManager.run();
    }
}
