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

        System.out.println("--------------------------------------------------------------------------------------------");
        System.out.println("Get digital twin");
        System.out.println("--------------------------------------------------------------------------------------------");
        GetDigitalTwin();

        System.out.println("--------------------------------------------------------------------------------------------");
        System.out.println("Update digital twin");
        System.out.println("--------------------------------------------------------------------------------------------");
        UpdateDigitalTwinComponentProperty();

        System.out.println("--------------------------------------------------------------------------------------------");
        System.out.println("Invoke a method on component - getMaxMinReport");
        System.out.println("--------------------------------------------------------------------------------------------");
        InvokeMethodOnComponent();

        System.out.println("--------------------------------------------------------------------------------------------");
        System.out.println("Invoke a method on root level - reboot");
        System.out.println("--------------------------------------------------------------------------------------------");
        InvokeMethodOnRootLevel();
    }

    private static void InitializeServiceClient() {
        DigitalTwinAsyncClient asyncClient = new DigitalTwinAsyncClient(iotHubConnectionString);
        client = new DigitalTwinClient(asyncClient);
    }

    private static ServiceResponseWithHeaders<String, DigitalTwinGetHeaders> GetDigitalTwin()
    {
        ServiceResponseWithHeaders<String, DigitalTwinGetHeaders> getResponse = client.getDigitalTwinWithResponse(digitalTwinid, String.class);
        System.out.println("Digital Twin: " + prettyString(getResponse.body()));
        System.out.println("Digital Twin eTag: " + getResponse.headers().eTag());
        System.out.println("Digital Twin get response message: " + getResponse.response().message());
        return getResponse;
    }

    private static void UpdateDigitalTwinComponentProperty() {
        // Get digital twin.
        ServiceResponseWithHeaders<String, DigitalTwinGetHeaders> getResponse = GetDigitalTwin();

        // Construct the options for conditional update.
        DigitalTwinUpdateRequestOptions options = new DigitalTwinUpdateRequestOptions();
        options.setIfMatch(getResponse.headers().eTag());

        UpdateOperationUtility updateOperationUtility = new UpdateOperationUtility();

        // The json patch format can be found here - https://docs.microsoft.com/en-us/azure/iot-pnp/howto-manage-digital-twin#update-a-digital-twin.
        // Add a new component.
        String newComponentName = "newThermostat";
        System.out.println("--------------------------------------------------------------------------------------------");
        System.out.println("Add a new component " + newComponentName);
        System.out.println("--------------------------------------------------------------------------------------------");
        options.setIfMatch(getResponse.headers().eTag());
        String path = "/" + newComponentName;
        Map<String, Object> properties = new HashMap<>();
        properties.put(propertyName, 50);
        updateOperationUtility.appendAddComponentOperation(path, properties);
        List<Object> digitalTwinUpdateOperations = updateOperationUtility.getUpdateOperations();
        ServiceResponseWithHeaders<Void, DigitalTwinUpdateHeaders> updateResponse = client.updateDigitalTwinWithResponse(digitalTwinid, digitalTwinUpdateOperations, options);
        System.out.println("Update Digital Twin response status: " + updateResponse.response().message());

        getResponse = GetDigitalTwin();

        // Replace an existing component.
        String component1 = "thermostat1";
        String component2 = "thermostat2";
        System.out.println("--------------------------------------------------------------------------------------------");
        System.out.println("Replace existing components " + component1 + " and " + component2);
        System.out.println("--------------------------------------------------------------------------------------------");
        options.setIfMatch(getResponse.headers().eTag());
        path = "/" + component1;
        Map<String, Object> t1properties = new HashMap<>();
        t1properties.put(propertyName, 50);
        updateOperationUtility.appendReplaceComponentOperation(path, t1properties);
        path = "/" + component2;
        Map<String, Object> t2properties = new HashMap<>();
        t2properties.put(propertyName, 40);
        updateOperationUtility.appendReplaceComponentOperation(path, t2properties);
        digitalTwinUpdateOperations = updateOperationUtility.getUpdateOperations();
        updateResponse = client.updateDigitalTwinWithResponse(digitalTwinid, digitalTwinUpdateOperations, options);
        System.out.println("Update Digital Twin response status: " + updateResponse.response().message());

        getResponse = GetDigitalTwin();

        // Remove the new component.
        System.out.println("--------------------------------------------------------------------------------------------");
        System.out.println("Remove the new component " + newComponentName);
        System.out.println("--------------------------------------------------------------------------------------------");
        options.setIfMatch(getResponse.headers().eTag());
        path = "/newThermostat";
        updateOperationUtility.appendRemoveComponentOperation(path);
        digitalTwinUpdateOperations = updateOperationUtility.getUpdateOperations();
        updateResponse = client.updateDigitalTwinWithResponse(digitalTwinid, digitalTwinUpdateOperations, options);
        System.out.println("Update Digital Twin response status: " + updateResponse.response().message());

        GetDigitalTwin();
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
