package samples.com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.pnphelpers.PnpHelper;
import com.microsoft.azure.sdk.iot.service.devicetwin.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

// This sample uses the model - https://github.com/Azure/opendigitaltwins-dtdl/blob/master/DTDL/v2/samples/TemperatureController.json.
@Slf4j
public class TemperatureController {
    // Get connection string and device id inputs.
    private static final String iotHubConnectionString  = System.getenv("IOTHUB_CONNECTION_STRING");
    private static final String deviceId = System.getenv("DEVICE_ID");

    private static DeviceTwin twinClient;
    private static DeviceMethod methodClient;

    public static void main(String[] args) throws Exception {
        RunSample();
    }

    private static void RunSample() throws IOException, IotHubException {
        log.debug("Initialize the service client.");
        InitializeServiceClient();

        log.debug("Get Twin model Id and Update Twin");
        GetAndUpdateTwin();

        log.debug("Invoke a method on component");
        InvokeMethodOnComponent();

        log.debug("Invoke a method on root level");
        InvokeMethodOnRootLevel();
    }

    private static void InitializeServiceClient() throws IOException {
        twinClient = DeviceTwin.createFromConnectionString(iotHubConnectionString);
        methodClient = DeviceMethod.createFromConnectionString(iotHubConnectionString);
    }

    private static void GetAndUpdateTwin() throws IOException, IotHubException {
        // Get the twin and retrieve model Id set by Device client.
        DeviceTwinDevice twin = new DeviceTwinDevice(deviceId);
        twinClient.getTwin(twin);
        log.debug("Model Id of this Twin is: " + twin.getModelId());

        // Update the twin for thermostat1 component.
        // The update patch for a property of a component should be in the format:
        // "desired": {
        //    "componentName": {
        //      "__t": "c",
        //      "propertyName": {
        //        "value": "propertyValue"
        //      }
        //  }
        log.debug("Updating Device twin property");
        String propertyName = "targetTemperature";
        double propertyValue = 60.2;
        String componentName = "thermostat1";
        twin.setDesiredProperties(PnpHelper.CreateComponentPropertyPatch(propertyName, propertyValue, componentName));
        twinClient.updateTwin(twin);

        // Get the updated twin properties.
        twinClient.getTwin(twin);
        log.debug("The updated desired properties: " + twin.getDesiredProperties().iterator().next().getValue());
    }

    private static void InvokeMethodOnRootLevel() throws IOException, IotHubException {
        // The method to invoke on the root level for a device with components should be "methodName" as defined in the DTDL.
        String methodToInvoke = "reboot";
        log.debug("Invoking method: " + methodToInvoke);

        Long responseTimeout = TimeUnit.SECONDS.toSeconds(200);
        Long connectTimeout = TimeUnit.SECONDS.toSeconds(5);

        // Invoke the command.
        String commandInput = "5";
        MethodResult result = methodClient.invoke(deviceId, methodToInvoke, responseTimeout, connectTimeout, commandInput);
        if(result == null)
        {
            throw new IOException("Method result is null");
        }

        log.debug("Method result status is: " + result.getStatus());
    }

    private static void InvokeMethodOnComponent() throws IOException, IotHubException {
        String methodToInvoke = PnpHelper.CreateComponentCommandName("thermostat1", "getMaxMinReport");
        log.debug("Invoking method: " + methodToInvoke);

        Long responseTimeout = TimeUnit.SECONDS.toSeconds(200);
        Long connectTimeout = TimeUnit.SECONDS.toSeconds(5);

        // Invoke the command.
        // The command payload should be in the following format:
        // "payload": {
        //   "commandRequest": {
        //   "value": "commandInput"
        //  }
        String commandInput = ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(5).format(DateTimeFormatter.ISO_DATE_TIME);
        MethodResult result = methodClient.invoke(deviceId, methodToInvoke, responseTimeout, connectTimeout, commandInput);
        if(result == null)
        {
            throw new IOException("Method invoke returns null");
        }
        log.debug("Method result status is: " + result.getStatus());
    }
}
