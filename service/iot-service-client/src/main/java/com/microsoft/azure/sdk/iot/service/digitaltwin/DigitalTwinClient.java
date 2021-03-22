// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.digitaltwin;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.service.digitaltwin.customized.DigitalTwinGetHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.customized.DigitalTwinUpdateHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.generated.DigitalTwins;
import com.microsoft.azure.sdk.iot.service.digitaltwin.generated.models.DigitalTwinInvokeRootLevelCommandHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.models.*;
import com.microsoft.rest.*;

import java.io.IOException;
import java.util.List;

import static com.microsoft.azure.sdk.iot.service.digitaltwin.DigitalTwinClientOptions.DEFAULT_HTTP_CONNECT_TIMEOUT_MS;
import static com.microsoft.azure.sdk.iot.service.digitaltwin.DigitalTwinClientOptions.DEFAULT_HTTP_READ_TIMEOUT_MS;

/**
 * <p>
 * The Digital Twins Service Client contains methods to retrieve and update digital twin information, and invoke commands on a digital twin device.
 * </p>
 * */
public class DigitalTwinClient {
    private final DigitalTwinAsyncClient digitalTwinAsyncClient;

    /**
     * Creates an implementation instance of {@link DigitalTwins} that is used to invoke the Digital Twin features
     * @param connectionString The IoT Hub connection string
     * @return The instantiated DigitalTwinClient.
     */
    public DigitalTwinClient(String connectionString) {
        this(connectionString,
            DigitalTwinClientOptions.builder()
                .httpReadTimeout(DEFAULT_HTTP_READ_TIMEOUT_MS)
                .httpConnectTimeout(DEFAULT_HTTP_CONNECT_TIMEOUT_MS)
                .build());
    }

    /**
     * Creates an implementation instance of {@link DigitalTwins} that is used to invoke the Digital Twin features
     * @param connectionString The IoT Hub connection string
     * @param options The optional settings for this client. May not be null.
     * @return The instantiated DigitalTwinClient.
     */
    public DigitalTwinClient(String connectionString, DigitalTwinClientOptions options) {
        digitalTwinAsyncClient = new DigitalTwinAsyncClient(connectionString, options);
    }

    /**
     * Creates an implementation instance of {@link DigitalTwins} that is used to invoke the Digital Twin features
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     * @return The instantiated DigitalTwinClient.
     */
    public DigitalTwinClient(String hostName, AzureSasCredential azureSasCredential) {
        this(hostName,
            azureSasCredential,
            DigitalTwinClientOptions.builder()
                .httpReadTimeout(DEFAULT_HTTP_READ_TIMEOUT_MS)
                .httpConnectTimeout(DEFAULT_HTTP_CONNECT_TIMEOUT_MS)
                .build());
    }

    /**
     * Creates an implementation instance of {@link DigitalTwins} that is used to invoke the Digital Twin features
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     * @param options The optional settings for this client. May not be null.
     * @return The instantiated DigitalTwinClient.
     */
    public DigitalTwinClient(String hostName, AzureSasCredential azureSasCredential, DigitalTwinClientOptions options) {
        digitalTwinAsyncClient = new DigitalTwinAsyncClient(hostName, azureSasCredential, options);
    }

    /**
     * Creates an implementation instance of {@link DigitalTwins} that is used to invoke the Digital Twin features
     * @param connectionString The IoT Hub connection string
     * @return The instantiated DigitalTwinClient.
     */
    public static DigitalTwinClient createFromConnectionString(String connectionString)
    {
        return new DigitalTwinClient(connectionString);
    }

    /**
     * Gets a digital twin.
     * @param digitalTwinId The Id of the digital twin.
     * @param clazz The class to deserialize the application/json into.
     * @param <T> The generic type to deserialize the application/json into.
     * @return The application/json of the digital twin.
     */
    public <T> T getDigitalTwin(String digitalTwinId, Class<T> clazz)
    {
        return getDigitalTwinWithResponse(digitalTwinId, clazz).body();
    }

    /**
     * Gets a digital twin.
     * @param digitalTwinId The Id of the digital twin.
     * @param clazz The class to deserialize the application/json into.
     * @param <T> The generic type to deserialize the application/json into.
     * @return A {@link ServiceResponseWithHeaders} representing deserialized application/json of the digital twin with {@link DigitalTwinGetHeaders}.
     */
    public <T> ServiceResponseWithHeaders<T, DigitalTwinGetHeaders> getDigitalTwinWithResponse(String digitalTwinId, Class<T> clazz)
    {
        return digitalTwinAsyncClient.getDigitalTwinWithResponse(digitalTwinId, clazz)
                .toBlocking().single();
    }

    /**
     * Updates a digital twin.
     * @param digitalTwinId The Id of the digital twin.
     * @param digitalTwinUpdateOperations The JSON patch to apply to the specified digital twin. This argument can be created using {@link UpdateOperationUtility}.
     * @return void.
     */
    public Void updateDigitalTwin(String digitalTwinId, List<Object> digitalTwinUpdateOperations)
    {
        return digitalTwinAsyncClient.updateDigitalTwin(digitalTwinId, digitalTwinUpdateOperations)
            .toBlocking().single();
    }

    /**
     * Updates a digital twin.
     * @param digitalTwinId The Id of the digital twin.
     * @param digitalTwinUpdateOperations The JSON patch to apply to the specified digital twin. This argument can be created using {@link UpdateOperationUtility}.
     * @return A {@link ServiceResponseWithHeaders} with {@link DigitalTwinUpdateHeaders}.
     */
    public ServiceResponseWithHeaders<Void, DigitalTwinUpdateHeaders> updateDigitalTwinWithResponse(String digitalTwinId, List<Object> digitalTwinUpdateOperations)
    {
        return updateDigitalTwinWithResponse(digitalTwinId, digitalTwinUpdateOperations, null);
    }

    /**
     * Updates a digital twin.
     * @param digitalTwinId The Id of the digital twin.
     * @param digitalTwinUpdateOperations The JSON patch to apply to the specified digital twin. This argument can be created using {@link UpdateOperationUtility}.
     * @param options The optional settings for this request.
     * @return A {@link ServiceResponseWithHeaders} with {@link DigitalTwinUpdateHeaders}.
     */
    public ServiceResponseWithHeaders<Void, DigitalTwinUpdateHeaders> updateDigitalTwinWithResponse(String digitalTwinId, List<Object> digitalTwinUpdateOperations, DigitalTwinUpdateRequestOptions options)
    {
        return digitalTwinAsyncClient.updateDigitalTwinWithResponse(digitalTwinId, digitalTwinUpdateOperations, options)
                .toBlocking().single();
    }

    /**
     * Invoke a command on a digital twin.
     * @param digitalTwinId The Id of the digital twin.
     * @param commandName The command to be invoked.
     * @return A {@link DigitalTwinCommandResponse} which contains the application/json command invocation response.
     * @throws IOException can be thrown if the provided payload cannot be deserialized into a valid Json object.
     */
    public DigitalTwinCommandResponse invokeCommand(String digitalTwinId, String commandName) throws IOException {
        return invokeCommandWithResponse(digitalTwinId, commandName, null, null).body();
    }

    /**
     * Invoke a command on a digital twin.
     * @param digitalTwinId The Id of the digital twin.
     * @param commandName The command to be invoked.
     * @param payload The command payload.
     * @return A {@link DigitalTwinCommandResponse} which contains the application/json command invocation response.
     * @throws IOException can be thrown if the provided payload cannot be deserialized into a valid Json object.
     */
    public DigitalTwinCommandResponse invokeCommand(String digitalTwinId, String commandName, String payload) throws IOException {
        // Retrofit does not work well with null in body
        return invokeCommandWithResponse(digitalTwinId, commandName, payload, null).body();
    }

    /**
     * Invoke a command on a digital twin.
     * @param digitalTwinId The Id of the digital twin.
     * @param commandName The command to be invoked.
     * @param payload The command payload.
     * @param options The optional settings for this request.
     * @return A {@link ServiceResponseWithHeaders} with {@link DigitalTwinInvokeRootLevelCommandHeaders} and {@link DigitalTwinCommandResponse} which contains the application/json command invocation response.
     * @throws IOException can be thrown if the provided payload cannot be deserialized into a valid Json object.
     */
    public ServiceResponseWithHeaders<DigitalTwinCommandResponse, DigitalTwinInvokeCommandHeaders> invokeCommandWithResponse(String digitalTwinId, String commandName, String payload, DigitalTwinInvokeCommandRequestOptions options) throws IOException {
        return digitalTwinAsyncClient
                .invokeCommandWithResponse(digitalTwinId, commandName, payload, options)
                .toBlocking().single();
    }

    /**
     * Invoke a command on a digital twin component.
     * @param digitalTwinId The Id of the digital twin.
     * @param componentName The component name under which the command is defined.
     * @param commandName The command to be invoked.
     * @return A {@link DigitalTwinCommandResponse} which contains the application/json command invocation response.
     * @throws IOException can be thrown if the provided payload cannot be deserialized into a valid Json object.
     */
    public DigitalTwinCommandResponse invokeComponentCommand(String digitalTwinId, String componentName, String commandName) throws IOException {
        return invokeComponentCommandWithResponse(digitalTwinId, componentName, commandName, null, null).body();
    }

    /**
     * Invoke a command on a digital twin component.
     * @param digitalTwinId The Id of the digital twin.
     * @param componentName The component name under which the command is defined.
     * @param commandName The command to be invoked.
     * @param payload The command payload.
     * @return A {@link DigitalTwinCommandResponse} which contains the application/json command invocation response.
     * @throws IOException can be thrown if the provided payload cannot be deserialized into a valid Json object.
     */
    public DigitalTwinCommandResponse invokeComponentCommand(String digitalTwinId, String componentName, String commandName, String payload) throws IOException {
        // Retrofit does not work well with null in body
        return invokeComponentCommandWithResponse(digitalTwinId, componentName, commandName, payload, null).body();
    }

    /**
     * Invoke a command on a digital twin component.
     * @param digitalTwinId The Id of the digital twin.
     * @param componentName The component name under which the command is defined.
     * @param commandName The command to be invoked.
     * @param payload The command payload.
     * @param options The optional settings for this request.
     * @return A {@link ServiceResponseWithHeaders} with {@link DigitalTwinInvokeRootLevelCommandHeaders} and {@link DigitalTwinCommandResponse} which contains the application/json command invocation response.
     * @throws IOException can be thrown if the provided payload cannot be deserialized into a valid Json object.
     */
    public ServiceResponseWithHeaders<DigitalTwinCommandResponse, DigitalTwinInvokeCommandHeaders> invokeComponentCommandWithResponse(String digitalTwinId, String componentName, String commandName, String payload, DigitalTwinInvokeCommandRequestOptions options) throws IOException {
        return digitalTwinAsyncClient.invokeComponentCommandWithResponse(digitalTwinId, componentName, commandName, payload, options)
                .toBlocking().single();
    }
}
