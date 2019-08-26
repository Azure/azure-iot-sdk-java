package com.microsoft.azure.sdk.iot.digitaltwin.sample;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeCallback;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeReason;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinCallback;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT;
import static java.util.Collections.singletonList;

@Slf4j
public class Application {
    private static final String CONNECTION_STRING = "Your IoTHub connection string";
    private static final String DCM_ID = "urn:azureiot:samplemodel:1";
    private final static String ENVIRONMENTAL_SENSOR_INTERFACE_INSTANCE_NAME = "sensor";
    public static void main(String[] args) throws URISyntaxException, InterruptedException {
        DeviceClient deviceClient = new DeviceClient(CONNECTION_STRING, MQTT);
        deviceClient.registerConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallback() {
            @Override
            public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext) {
                log.debug("Device client status changed to: {}, reason: {}, cause: {}", status, statusChangeReason, throwable);
            }
        }, deviceClient);
        DigitalTwinDeviceClient digitalTwinDeviceClient = new DigitalTwinDeviceClient(deviceClient);
        final EnvironmentalSensor environmentalSensor = new EnvironmentalSensor(ENVIRONMENTAL_SENSOR_INTERFACE_INSTANCE_NAME);
        digitalTwinDeviceClient.registerInterfacesAsync(
                DCM_ID,
                singletonList(environmentalSensor),
                new DigitalTwinCallback() {
                    @Override
                    public void onResult(DigitalTwinClientResult digitalTwinClientResult, Object context) {
                        log.debug("Register interfaces {}.", digitalTwinClientResult);
                    }
                },
                digitalTwinDeviceClient);
        Thread.sleep(100000);
    }

}
