// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.
package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.service.digitaltwin.DigitalTwinAsyncClient;
import com.microsoft.azure.sdk.iot.service.digitaltwin.DigitalTwinClient;
import com.microsoft.azure.sdk.iot.service.digitaltwin.helpers.UpdateOperationUtility;
import com.microsoft.azure.sdk.iot.service.digitaltwin.models.DigitalTwinCommandResponse;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Thermostat {

    // Get connection string and device id inputs.
    private static final String iotHubConnectionString  = System.getenv("IOTHUB_CONNECTION_STRING");
    private static final String digitalTwinid = System.getenv("IOTHUB_DEVICE_ID");

    private static DigitalTwinClient client;

    public static void main(String[] args) {
        RunSample();
    }

    private static void RunSample() {
        System.out.println("Initialize the service client.");
        InitializeServiceClient();

        System.out.println("Get original digital twin");
        GetDigitalTwin();

        System.out.println("Update digital twin");
        UpdateDigitalTwin();

        System.out.println("Invoke a method on root level");
        InvokeMethodOnRootLevel();
    }

    private static void InitializeServiceClient() {
        DigitalTwinAsyncClient asyncClient = new DigitalTwinAsyncClient(iotHubConnectionString);
        client = new DigitalTwinClient(asyncClient);
    }

    private static void GetDigitalTwin()
    {
        // Get the digital twin.
        String getResponse = client.getDigitalTwin(digitalTwinid, String.class);
        System.out.println("Digital Twin is: " + getResponse);
    }

    private static void UpdateDigitalTwin()
    {
        // Update the digital twin.
        UpdateOperationUtility updateOperationUtility = new UpdateOperationUtility();

        // Add a new property at root level.
        updateOperationUtility.appendAddPropertyOperation("/currentTemperature", 35);
        updateOperationUtility.appendAddPropertyOperation("/targetTemperature", 35);
        client.updateDigitalTwin(digitalTwinid, updateOperationUtility.getUpdateOperations());
        System.out.println("Update Digital Twin");

        String getResponse = client.getDigitalTwin(digitalTwinid, String.class);
        System.out.println("Updated Digital Twin after adding a new property: " + getResponse);

        // Replace an existing property at root level.
        updateOperationUtility.appendReplacePropertyOperation("/targetTemperature", 50);
        client.updateDigitalTwin(digitalTwinid, updateOperationUtility.getUpdateOperations());
        System.out.println("Update Digital Twin");

        getResponse = client.getDigitalTwin(digitalTwinid, String.class);
        System.out.println("Updated Digital Twin after replacing an existing property: " + getResponse);

        // Remove a property at root level.
        updateOperationUtility.appendRemovePropertyOperation("/currentTemperature");
        client.updateDigitalTwin(digitalTwinid, updateOperationUtility.getUpdateOperations());
        System.out.println("Update Digital Twin");

        getResponse = client.getDigitalTwin(digitalTwinid, String.class);
        System.out.println("Updated Digital Twin after removing the new property: " + getResponse);
    }

    private static void InvokeMethodOnRootLevel()
    {
        String commandName = "getMaxMinReport";
        String commandInput = ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(5).format(DateTimeFormatter.ISO_DATE_TIME);

        // Invoke a method on root level.
        DigitalTwinCommandResponse response = client.invokeCommand(digitalTwinid, commandName, commandInput);
        System.out.println("Invoked Command " + commandName + " response: " + response.getPayload());
    }
}
