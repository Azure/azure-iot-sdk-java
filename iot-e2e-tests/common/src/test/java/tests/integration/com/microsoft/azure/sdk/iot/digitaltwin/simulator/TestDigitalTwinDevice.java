package tests.integration.com.microsoft.azure.sdk.iot.digitaltwin.simulator;

import com.microsoft.azure.sdk.iot.device.ClientOptions;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import tests.integration.com.microsoft.azure.sdk.iot.digitaltwin.helpers.E2ETestConstants;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;

import java.io.IOException;
import java.net.URISyntaxException;

@Slf4j
@Getter
public class TestDigitalTwinDevice {
    private static final String IOT_HUB_CONNECTION_STRING = Tools.retrieveEnvironmentVariableValue(E2ETestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);

    private String deviceId;
    private DeviceClient deviceClient;

    public TestDigitalTwinDevice(@NonNull String deviceId, @NonNull IotHubClientProtocol protocol) throws IotHubException, IOException, URISyntaxException {
        if (!protocol.equals(IotHubClientProtocol.MQTT) && !protocol.equals(IotHubClientProtocol.MQTT_WS)) {
            throw new IllegalArgumentException("Supported protocols for DigitalTwin are MQTT, MQTT_WS");
        }
            this.deviceId = deviceId;
            this.deviceClient = createDeviceClient(protocol);
            this.deviceClient.registerConnectionStatusChangeCallback((iotHubConnectionStatus, iotHubConnectionStatusChangeReason, throwable, o) -> {
                TestDigitalTwinDevice testDigitalTwinDevice = (TestDigitalTwinDevice) o;
                log.debug("DeviceID={}; status={}, reason={}", testDigitalTwinDevice.getDeviceId(), iotHubConnectionStatus, iotHubConnectionStatusChangeReason);
            }, this);
            log.debug("Created device: {}", deviceId);
        }

    private DeviceClient createDeviceClient(IotHubClientProtocol protocol) throws IOException, IotHubException, URISyntaxException {
        RegistryManager registryManager = RegistryManager.createFromConnectionString(IOT_HUB_CONNECTION_STRING);
        ClientOptions options = new ClientOptions();
        options.setModelId(E2ETestConstants.MODEL_ID);
        Device device = Device.createDevice(deviceId, AuthenticationType.SAS);
        Device registeredDevice = registryManager.addDevice(device);
        String deviceConnectionString = registryManager.getDeviceConnectionString(registeredDevice);
        registryManager.close();

        return new DeviceClient(deviceConnectionString, protocol, options);
    }

    public void closeAndDeleteDevice() {
        try {
            deviceClient.closeNow();

            RegistryManager registryManager = RegistryManager.createFromConnectionString(IOT_HUB_CONNECTION_STRING);
            registryManager.removeDevice(deviceId);
            registryManager.close();
        } catch (Exception ex) {
            log.error("An exception occurred while closing/ deleting the device {}: {}", deviceId, ex);
        }
    }
}