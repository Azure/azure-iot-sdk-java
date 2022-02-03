// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.
package samples.com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.service.methods.DirectMethodRequestOptions;
import com.microsoft.azure.sdk.iot.service.methods.DirectMethodsClient;
import com.microsoft.azure.sdk.iot.service.methods.MethodResult;
import com.microsoft.azure.sdk.iot.service.twin.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

// This sample uses the model - https://github.com/Azure/opendigitaltwins-dtdl/blob/master/DTDL/v2/samples/TemperatureController.json.
public class TemperatureController {
    // Get connection string and device id inputs.
    private static final String iotHubConnectionString  = System.getenv("IOTHUB_CONNECTION_STRING");
    private static final String deviceId = System.getenv("IOTHUB_DEVICE_ID");

    private static TwinClient twinClient;
    private static DirectMethodsClient methodClient;

    public static void main(String[] args) throws Exception {
        RunSample();
    }

    private static void RunSample() throws IOException, IotHubException {
        System.out.println("Initialize the service client.");
        InitializeServiceClient();

        System.out.println("Get Twin model Id and Update Twin");
        GetAndUpdateTwin();

        System.out.println("Invoke a method on component");
        InvokeMethodOnComponent();

        System.out.println("Invoke a method on root level");
        InvokeMethodOnRootLevel();
    }

    private static void InitializeServiceClient()
    {
        twinClient = new TwinClient(iotHubConnectionString);
        methodClient = new DirectMethodsClient(iotHubConnectionString);
    }

    private static void GetAndUpdateTwin() throws IOException, IotHubException {
        // Get the twin and retrieve model Id set by Device client.
        Twin twin = twinClient.getTwin(deviceId);
        System.out.println("Model Id of this Twin is: " + twin.getModelId());

        // Update the twin for thermostat1 component.
        // The update patch for a property of a component should be in the format:
        // "desired": {
        //    "componentName": {
        //      "__t": "c",
        //      "propertyName": {
        //        "value": "propertyValue"
        //      }
        //  }
        System.out.println("Updating Device twin property");
        String propertyName = "targetTemperature";
        double propertyValue = 60.2;
        String componentName = "thermostat1";
        twin.setDesiredProperties(PnpHelper.CreateComponentPropertyPatch(propertyName, propertyValue, componentName));
        twinClient.updateTwin(twin);

        // Get the updated twin properties.
        twin = twinClient.getTwin(deviceId);
        System.out.println("The updated desired properties: " + twin.getDesiredProperties().iterator().next().getValue());
    }

    private static void InvokeMethodOnRootLevel() throws IOException, IotHubException {
        // The method to invoke on the root level for a device with components should be "methodName" as defined in the DTDL.
        String methodToInvoke = "reboot";
        System.out.println("Invoking method: " + methodToInvoke);

        int responseTimeout = 200;
        int connectTimeout = 5;

        // Invoke the command.
        String commandInput = "5";
        DirectMethodRequestOptions options =
            DirectMethodRequestOptions.builder()
                .payload(commandInput)
                .methodConnectTimeout(connectTimeout)
                .methodResponseTimeout(responseTimeout)
                .build();

        MethodResult result = methodClient.invoke(deviceId, methodToInvoke, options);

        if(result == null)
        {
            throw new IOException("Method result is null");
        }

        System.out.println("Method result status is: " + result.getStatus());
    }

    private static void InvokeMethodOnComponent() throws IOException, IotHubException {
        String methodToInvoke = PnpHelper.CreateComponentCommandName("thermostat1", "getMaxMinReport");
        System.out.println("Invoking method: " + methodToInvoke);

        int responseTimeout = 200;
        int connectTimeout = 5;

        // Invoke the command.
        // The command payload should be in the following format:
        // "payload": {
        //   "commandRequest": {
        //   "value": "commandInput"
        //  }
        String commandInput = ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(5).format(DateTimeFormatter.ISO_DATE_TIME);

        DirectMethodRequestOptions options =
            DirectMethodRequestOptions.builder()
                .payload(commandInput)
                .methodConnectTimeout(connectTimeout)
                .methodResponseTimeout(responseTimeout)
                .build();

        MethodResult result = methodClient.invoke(deviceId, methodToInvoke, options);
        if(result == null)
        {
            throw new IOException("Method invoke returns null");
        }
        System.out.println("Method result status is: " + result.getStatus());
    }
}
