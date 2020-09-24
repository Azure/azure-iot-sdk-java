// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.
package samples.com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.service.devicetwin.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

// This sample uses the model - https://github.com/Azure/opendigitaltwins-dtdl/blob/master/DTDL/v2/samples/Thermostat.json.
public class Thermostat {
    // Get connection string and device id inputs.
    private static final String iotHubConnectionString  = System.getenv("IOTHUB_CONNECTION_STRING");
    private static final String deviceId = System.getenv("IOTHUB_DEVICE_ID");

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
        twinClient = DeviceTwin.createFromConnectionString(iotHubConnectionString);
        methodClient = DeviceMethod.createFromConnectionString(iotHubConnectionString);
    }

    private static void GetAndUpdateTwin() throws IOException, IotHubException {
        // Get the twin and retrieve model Id set by Device client.
        DeviceTwinDevice twin = new DeviceTwinDevice(deviceId);
        twinClient.getTwin(twin);
        System.out.println("Model Id of this Twin is: " + twin.getModelId());

        // Update the twin.
        // The update patch for a property of a component should be in the format:
        // "desired": {
        //      "propertyName": {
        //        "value": "propertyValue"
        //      }
        //  }
        System.out.println("Updating Device twin property");
        String propertyName = "targetTemperature";
        double propertyValue = 60.2;
        twin.setDesiredProperties(Collections.singleton(new Pair(propertyName, propertyValue)));
        twinClient.updateTwin(twin);

        // Get the updated twin properties.
        twinClient.getTwin(twin);
        System.out.println("The updated desired properties: " + twin.getDesiredProperties().iterator().next().getValue());
    }

    private static void InvokeMethod() throws IOException, IotHubException {
        // The method to invoke for a device without components should be "methodName" as defined in the DTDL.
        String methodToInvoke = "getMaxMinReport";
        System.out.println("Invoking method: " + methodToInvoke);

        Long responseTimeout = TimeUnit.SECONDS.toSeconds(200);
        Long connectTimeout = TimeUnit.SECONDS.toSeconds(5);

        // Invoke the command.
        String commandInput = ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(5).format(DateTimeFormatter.ISO_DATE_TIME);
        MethodResult result = methodClient.invoke(deviceId, methodToInvoke, responseTimeout, connectTimeout, commandInput);
        if(result == null)
        {
            throw new IOException("Method result is null");
        }

        System.out.println("Method result status is: " + result.getStatus());
    }
}
