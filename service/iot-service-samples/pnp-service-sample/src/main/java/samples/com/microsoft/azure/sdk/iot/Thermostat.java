package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.service.devicetwin.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Thermostat {
    // Get connection string and device id inputs
    private static final String hubConnectionString  = System.getenv("IOTHUB_CONNECTION_STRING");
    private static final String deviceId = System.getenv("DEVICE_ID");

    // Default values for Thermostat sample with no component.
    private static final String propertyName = "targetTemperature";
    private static final double propertyValue = 60;
    private static final String methodToInvoke = "reboot";
    private static final String methodPayload = "{\"delay\":10}";

    private static DeviceTwin twinClient;
    private static DeviceMethod methodClient;

    public static void main(String[] args) throws Exception {
        RunSample();
    }

    private static void RunSample() throws IOException, IotHubException {
        System.out.println("Initialize the service client.");
        InitializeServiceClient();

        System.out.println("Get Twin model Id and Update Twin");
        GetAndUpdateTwin();

        System.out.println("Invoke a method");
        InvokeMethod();
    }

    private static void InitializeServiceClient() throws IOException {
        twinClient = DeviceTwin.createFromConnectionString(hubConnectionString);
        methodClient = DeviceMethod.createFromConnectionString(hubConnectionString);
    }

    private static void GetAndUpdateTwin() throws IOException, IotHubException {
        // Get a Twin and retrieves model Id set by Device client
        DeviceTwinDevice twin = new DeviceTwinDevice(deviceId);
        twinClient.getTwin(twin);
        System.out.println("Model Id of this Twin is: " + twin.getModelId());

        // Update the twin
        Set<Pair> desiredProperties = new HashSet<Pair>();
        desiredProperties.add(new Pair(propertyName, propertyValue));
        twin.setDesiredProperties(desiredProperties);
        System.out.println("Updating Device twin (targetTemperature, 75)");
        twinClient.updateTwin(twin);
    }

    private static void InvokeMethod() throws IOException, IotHubException {
        System.out.println("Invoking method: " + methodToInvoke);
        Map<String, Object> payload = new HashMap<String, Object>()
        {
            {
                put("delay", 1);
            }
        };
        Long responseTimeout = TimeUnit.SECONDS.toSeconds(200);
        Long connectTimeout = TimeUnit.SECONDS.toSeconds(5);
        MethodResult result = methodClient.invoke(deviceId, methodToInvoke, responseTimeout, connectTimeout, payload);
        if(result == null)
        {
            throw new IOException("Method invoke returns null");
        }
        System.out.println("Method result status is: " + result.getStatus());
    }
}
