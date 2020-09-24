// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.
package samples.com.microsoft.azure.sdk.iot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.azure.sdk.iot.service.digitaltwin.DigitalTwinAsyncClient;
import com.microsoft.azure.sdk.iot.service.digitaltwin.DigitalTwinClient;
import com.microsoft.azure.sdk.iot.service.digitaltwin.helpers.UpdateOperationUtility;
import com.microsoft.azure.sdk.iot.service.digitaltwin.models.BasicDigitalTwin;
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

        System.out.println("--------------------------------------------------------------------------------------------");
        System.out.println("Get original digital twin");
        System.out.println("--------------------------------------------------------------------------------------------");
        GetDigitalTwin();

        System.out.println("--------------------------------------------------------------------------------------------");
        System.out.println("Update digital twin");
        System.out.println("--------------------------------------------------------------------------------------------");
        UpdateDigitalTwin();

        System.out.println("--------------------------------------------------------------------------------------------");
        System.out.println("Invoke a method on root level - getMaxMinReport");
        System.out.println("--------------------------------------------------------------------------------------------");
        InvokeMethodOnRootLevel();
    }

    private static void InitializeServiceClient() {
        DigitalTwinAsyncClient asyncClient = new DigitalTwinAsyncClient(iotHubConnectionString);
        client = new DigitalTwinClient(asyncClient);
    }

    private static void GetDigitalTwin()
    {
        // Get the digital twin.
        BasicDigitalTwin getResponse = client.getDigitalTwin(digitalTwinid, BasicDigitalTwin.class);
        System.out.println("Digital Twin: " + prettyBasicDigitalTwin(getResponse));
    }

    private static void UpdateDigitalTwin()
    {
        // Update the digital twin.
        UpdateOperationUtility updateOperationUtility = new UpdateOperationUtility();

        // Add a new property at root level.
        String newProperty = "currentTemperature";
        String existingProperty = "targetTemperature";
        System.out.println("--------------------------------------------------------------------------------------------");
        System.out.println("Add properties at root level - " + newProperty + " and " + existingProperty);
        System.out.println("--------------------------------------------------------------------------------------------");
        updateOperationUtility.appendAddPropertyOperation("/" + newProperty, 35);
        updateOperationUtility.appendAddPropertyOperation("/" + existingProperty, 35);
        client.updateDigitalTwin(digitalTwinid, updateOperationUtility.getUpdateOperations());
        System.out.println("Updated Digital Twin");

        GetDigitalTwin();

        // Replace an existing property at root level.
        System.out.println("--------------------------------------------------------------------------------------------");
        System.out.println("Replace an existing property at root level " + existingProperty);
        System.out.println("--------------------------------------------------------------------------------------------");
        updateOperationUtility.appendReplacePropertyOperation("/targetTemperature", 50);
        client.updateDigitalTwin(digitalTwinid, updateOperationUtility.getUpdateOperations());
        System.out.println("Updated Digital Twin");

        GetDigitalTwin();

        // Remove the new property at root level.
        System.out.println("--------------------------------------------------------------------------------------------");
        System.out.println("Remove the new property at root level " + newProperty);
        System.out.println("--------------------------------------------------------------------------------------------");
        updateOperationUtility.appendRemovePropertyOperation("/currentTemperature");
        client.updateDigitalTwin(digitalTwinid, updateOperationUtility.getUpdateOperations());
        System.out.println("Updated Digital Twin");

        GetDigitalTwin();
    }

    private static void InvokeMethodOnRootLevel()
    {
        String commandName = "getMaxMinReport";
        String commandInput = ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(5).format(DateTimeFormatter.ISO_DATE_TIME);

        // Invoke a method on root level.
        DigitalTwinCommandResponse response = client.invokeCommand(digitalTwinid, commandName, commandInput);
        System.out.println("Invoked Command " + commandName + " response: " + prettyString(response.getPayload()));
    }

    private static String prettyBasicDigitalTwin(BasicDigitalTwin basicDigitalTwin)
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(basicDigitalTwin);
    }

    private static String prettyString(String str)
    {
        Gson gson = new Gson();
        Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        return gsonBuilder.toJson(gson.fromJson(str, Object.class));
    }
}
