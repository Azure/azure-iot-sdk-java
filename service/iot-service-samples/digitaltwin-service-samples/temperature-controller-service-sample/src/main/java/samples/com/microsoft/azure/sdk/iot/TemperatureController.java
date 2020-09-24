// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.
package samples.com.microsoft.azure.sdk.iot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.azure.sdk.iot.service.digitaltwin.DigitalTwinAsyncClient;
import com.microsoft.azure.sdk.iot.service.digitaltwin.DigitalTwinClient;
import com.microsoft.azure.sdk.iot.service.digitaltwin.generated.models.DigitalTwinGetHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.generated.models.DigitalTwinUpdateHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.helpers.UpdateOperationUtility;
import com.microsoft.azure.sdk.iot.service.digitaltwin.models.DigitalTwinCommandResponse;
import com.microsoft.azure.sdk.iot.service.digitaltwin.models.DigitalTwinInvokeCommandHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.models.DigitalTwinInvokeCommandRequestOptions;
import com.microsoft.azure.sdk.iot.service.digitaltwin.models.DigitalTwinUpdateRequestOptions;
import com.microsoft.rest.ServiceResponseWithHeaders;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemperatureController {
    // Get connection string and device id inputs.
    private static final String iotHubConnectionString  = System.getenv("IOTHUB_CONNECTION_STRING");
    private static final String digitalTwinid = System.getenv("IOTHUB_DEVICE_ID");
    private static final String componentName = "thermostat1";
    private static final String propertyName = "targetTemperature";

    private static DigitalTwinClient client;

    public static void main(String[] args) {
        RunSample();
    }

    private static void RunSample() {
        System.out.println("Initialize the service client.");
        InitializeServiceClient();

        System.out.println("Get digital twin");
        GetDigitalTwin();

        System.out.println("Update digital twin");
        UpdateDigitalTwinComponentProperty();

        System.out.println("Invoke a method on component");
        InvokeMethodOnComponent();

        System.out.println("Invoke a method on root level");
        InvokeMethodOnRootLevel();
    }

    private static void InitializeServiceClient() {
        DigitalTwinAsyncClient asyncClient = new DigitalTwinAsyncClient(iotHubConnectionString);
        client = new DigitalTwinClient(asyncClient);
    }

    private static void GetDigitalTwin()
    {
        ServiceResponseWithHeaders<String, DigitalTwinGetHeaders> getResponse = client.getDigitalTwinWithResponse(digitalTwinid, String.class);
        System.out.println("Digital Twin: " + prettyString(getResponse.body()));
        System.out.println("Digital Twin eTag: " + getResponse.headers().eTag());
        System.out.println("Digital Twin get response message: " + getResponse.response().message());
    }

    private static void UpdateDigitalTwinComponentProperty() {
        // Get digital twin.
        ServiceResponseWithHeaders<String, DigitalTwinGetHeaders> getResponse = client.getDigitalTwinWithResponse(digitalTwinid, String.class);

        // Construct the options for conditional update.
        DigitalTwinUpdateRequestOptions options = new DigitalTwinUpdateRequestOptions();
        options.setIfMatch(getResponse.headers().eTag());

        UpdateOperationUtility updateOperationUtility = new UpdateOperationUtility();

        // The json patch format can be found here - https://docs.microsoft.com/en-us/azure/iot-pnp/howto-manage-digital-twin#update-a-digital-twin.
        // Add a new component.
        options.setIfMatch(getResponse.headers().eTag());
        String path = "/newThermostat";
        Map<String, Object> properties = new HashMap<>();
        properties.put("targetTemperature", 50);
        updateOperationUtility.appendAddComponentOperation(path, properties);
        List<Object> digitalTwinUpdateOperations = updateOperationUtility.getUpdateOperations();
        ServiceResponseWithHeaders<Void, DigitalTwinUpdateHeaders> updateResponse = client.updateDigitalTwinWithResponse(digitalTwinid, digitalTwinUpdateOperations, options);
        System.out.println("Update Digital Twin response status: " + updateResponse.response().message());

        getResponse = client.getDigitalTwinWithResponse(digitalTwinid, String.class);
        System.out.println("Updated Digital Twin after adding a new component: " + getResponse.body());

        // Replace an existing component.
        options.setIfMatch(getResponse.headers().eTag());
        path = "/thermostat1";
        Map<String, Object> t1properties = new HashMap<>();
        t1properties.put("targetTemperature", 50);
        updateOperationUtility.appendReplaceComponentOperation(path, t1properties);
        path = "/thermostat2";
        Map<String, Object> t2properties = new HashMap<>();
        t2properties.put("targetTemperature", 40);
        updateOperationUtility.appendReplaceComponentOperation(path, t2properties);
        digitalTwinUpdateOperations = updateOperationUtility.getUpdateOperations();
        updateResponse = client.updateDigitalTwinWithResponse(digitalTwinid, digitalTwinUpdateOperations, options);
        System.out.println("Update Digital Twin response status: " + updateResponse.response().message());

        getResponse = client.getDigitalTwinWithResponse(digitalTwinid, String.class);
        System.out.println("Updated Digital Twin after replacing an existing component: " + getResponse.body());

        // Remove an existing component.
        options.setIfMatch(getResponse.headers().eTag());
        path = "/newThermostat";
        updateOperationUtility.appendRemoveComponentOperation(path);
        digitalTwinUpdateOperations = updateOperationUtility.getUpdateOperations();
        updateResponse = client.updateDigitalTwinWithResponse(digitalTwinid, digitalTwinUpdateOperations, options);
        System.out.println("Update Digital Twin response status: " + updateResponse.response().message());

        getResponse = client.getDigitalTwinWithResponse(digitalTwinid, String.class);
        System.out.println("Updated Digital Twin after removing the new component: " + getResponse.body());
    }

    private static void InvokeMethodOnComponent()
    {
        String commandName = "getMaxMinReport";
        String commandInput = ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(5).format(DateTimeFormatter.ISO_DATE_TIME);

        DigitalTwinInvokeCommandRequestOptions options = new DigitalTwinInvokeCommandRequestOptions();
        ServiceResponseWithHeaders<DigitalTwinCommandResponse, DigitalTwinInvokeCommandHeaders> commandResponse = client.invokeComponentCommandWithResponse(digitalTwinid, componentName, commandName, commandInput, options);
        System.out.println("Command " + commandName + ", payload: " + commandResponse.body().getPayload());
        System.out.println("Command " + commandName + ", status: " + commandResponse.body().getStatus());
        System.out.println("Command " + commandName + ", requestId: " + commandResponse.headers().getRequestId());
    }

    private static void InvokeMethodOnRootLevel()
    {
        String commandName = "reboot";
        String commandInput = "5";

        DigitalTwinInvokeCommandRequestOptions options = new DigitalTwinInvokeCommandRequestOptions();
        options.setConnectTimeoutInSeconds(5);
        options.setResponseTimeoutInSeconds(10);

        ServiceResponseWithHeaders<DigitalTwinCommandResponse, DigitalTwinInvokeCommandHeaders> commandResponse = client.invokeCommandWithResponse(digitalTwinid, commandName, commandInput, options);
        System.out.println("Command " + commandName + ", payload: " + commandResponse.body().getPayload());
        System.out.println("Command " + commandName + ", status: " + commandResponse.body().getStatus());
        System.out.println("Command " + commandName + ", requestId: " + commandResponse.headers().getRequestId());
    }

    private static String prettyString(String str)
    {
        Gson gson = new Gson();
        Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        return gsonBuilder.toJson(gson.fromJson(str, Object.class));
    }
}
