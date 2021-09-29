// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.azure.sdk.iot.service.digitaltwin.DigitalTwinAsyncClient;
import com.microsoft.azure.sdk.iot.service.digitaltwin.UpdateOperationUtility;
import com.microsoft.azure.sdk.iot.service.digitaltwin.serialization.BasicDigitalTwin;
import com.microsoft.rest.RestException;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Thermostat {

    // Get connection string and device id inputs.
    private static final String iotHubConnectionString  = System.getenv("IOTHUB_CONNECTION_STRING");
    private static final String digitalTwinid = System.getenv("IOTHUB_DEVICE_ID");
    private static final int MAX_WAIT_TIME_ASYNC_OPERATIONS_IN_SECONDS = 10;

    private static DigitalTwinAsyncClient asyncClient;

    public static void main(String[] args) throws IOException, InterruptedException {
        RunSample();
    }

    private static void RunSample() throws IOException, InterruptedException {
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
        asyncClient = DigitalTwinAsyncClient.createFromConnectionString(iotHubConnectionString);
    }

    private static void GetDigitalTwin() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        // Get the digital twin.
        asyncClient.getDigitalTwin(digitalTwinid, BasicDigitalTwin.class)
                .subscribe(
                        getResponse ->
                        {
                            System.out.println("Digital Twin Model Id: " + getResponse.getMetadata().getModelId());
                            System.out.println("Digital Twin: " + prettyBasicDigitalTwin(getResponse));
                            latch.countDown();
                        },
                        error ->
                        {
                            System.out.println("Get Digital Twin failed: " + error);
                            latch.countDown();
                        });

        latch.await(MAX_WAIT_TIME_ASYNC_OPERATIONS_IN_SECONDS, TimeUnit.SECONDS);
    }

    private static void UpdateDigitalTwin() throws InterruptedException {
        CountDownLatch latch1 = new CountDownLatch(1);

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
        asyncClient.updateDigitalTwin(digitalTwinid, updateOperationUtility.getUpdateOperations())
                .subscribe(
                        getResponse ->
                        {
                            System.out.println("Updated Digital Twin");
                            latch1.countDown();
                        },
                        error ->
                        {
                            System.out.println("Update Digital Twin failed: " + error);
                            latch1.countDown();
                        });
        latch1.await(MAX_WAIT_TIME_ASYNC_OPERATIONS_IN_SECONDS, TimeUnit.SECONDS);
        GetDigitalTwin();

        // Replace an existing property at root level.
        CountDownLatch latch2 = new CountDownLatch(1);
        System.out.println("--------------------------------------------------------------------------------------------");
        System.out.println("Replace an existing property at root level " + existingProperty);
        System.out.println("--------------------------------------------------------------------------------------------");
        updateOperationUtility.appendReplacePropertyOperation("/targetTemperature", 50);
        asyncClient.updateDigitalTwin(digitalTwinid, updateOperationUtility.getUpdateOperations())
                .subscribe(
                        getResponse ->
                        {
                            System.out.println("Updated Digital Twin");
                            latch2.countDown();
                        },
                        error ->
                        {
                            System.out.println("Update Digital Twin failed: " + error);
                            latch2.countDown();
                        });

        latch2.await(MAX_WAIT_TIME_ASYNC_OPERATIONS_IN_SECONDS, TimeUnit.SECONDS);
        GetDigitalTwin();

        // Remove the new property at root level.
        CountDownLatch latch3 = new CountDownLatch(1);
        System.out.println("--------------------------------------------------------------------------------------------");
        System.out.println("Remove the new property at root level " + newProperty);
        System.out.println("--------------------------------------------------------------------------------------------");
        updateOperationUtility.appendRemovePropertyOperation("/currentTemperature");
        asyncClient.updateDigitalTwin(digitalTwinid, updateOperationUtility.getUpdateOperations())
                .subscribe(
                        getResponse ->
                        {
                            System.out.println("Updated Digital Twin");
                            latch3.countDown();
                        },
                        error ->
                        {
                            System.out.println("Update Digital Twin failed: " + error);
                            latch3.countDown();
                        });

        latch3.await(MAX_WAIT_TIME_ASYNC_OPERATIONS_IN_SECONDS, TimeUnit.SECONDS);
        GetDigitalTwin();
    }

    private static void InvokeMethodOnRootLevel() throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        String commandName = "getMaxMinReport";
        String commandInput = ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(5).format(DateTimeFormatter.ISO_DATE_TIME);

        // Invoke a method on root level.
        asyncClient.invokeCommand(digitalTwinid, commandName, commandInput)
                .subscribe(
                        response ->
                        {
                            System.out.println("Invoked Command " + commandName + " response: " + prettyString(response.getPayload()));
                            latch.countDown();
                        },
                        error ->
                        {
                            RestException ex = (RestException)error;
                            if(ex.response().code() == 404) {
                                System.out.println("Invoked Command " + commandName + " failed: " + error);
                            }
                            else {
                                System.out.println("Ensure the device sample is running for this sample to succeed - https://github.com/Azure/azure-iot-sdk-java/tree/mamainster/device/iot-device-samples/pnp-device-sample/thermostat-device-sample.");
                            }
                            latch.countDown();
                        });

        latch.await(MAX_WAIT_TIME_ASYNC_OPERATIONS_IN_SECONDS, TimeUnit.SECONDS);
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
