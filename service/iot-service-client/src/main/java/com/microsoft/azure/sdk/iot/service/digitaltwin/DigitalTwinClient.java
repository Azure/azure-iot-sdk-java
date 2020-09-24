package com.microsoft.azure.sdk.iot.service.digitaltwin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.sdk.iot.service.digitaltwin.generated.DigitalTwins;
import com.microsoft.azure.sdk.iot.service.digitaltwin.generated.models.DigitalTwinGetHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.generated.models.DigitalTwinInvokeRootLevelCommandHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.generated.models.DigitalTwinUpdateHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.helpers.UpdateOperationUtility;
import com.microsoft.azure.sdk.iot.service.digitaltwin.models.DigitalTwinCommandResponse;
import com.microsoft.azure.sdk.iot.service.digitaltwin.models.DigitalTwinInvokeCommandHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.models.DigitalTwinInvokeCommandRequestOptions;
import com.microsoft.azure.sdk.iot.service.digitaltwin.models.DigitalTwinUpdateRequestOptions;
import com.microsoft.rest.*;
import lombok.NonNull;
import lombok.Setter;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.List;

import static lombok.AccessLevel.PACKAGE;

public class DigitalTwinClient {
    @Setter(PACKAGE)
    private final DigitalTwinAsyncClient digitalTwinAsyncClient;
    private static ObjectMapper objectMapper = new ObjectMapper();

    /***
     * Creates an implementation instance of {@link DigitalTwins} that is used to invoke the Digital Twin features
     * @param digitalTwinAsyncClient Digital Twin Async Client
     */
    public DigitalTwinClient(@NonNull DigitalTwinAsyncClient digitalTwinAsyncClient) {
        this.digitalTwinAsyncClient = digitalTwinAsyncClient;
    }

    /**
     * Gets a digital twin.
     * @param digitalTwinId The Id of the digital twin.
     * @param clazz The class to deserialize the application/json into.
     * @param <T> The generic type to deserialize the application/json into.
     * @return The application/json of the digital twin.
     */
    public <T> T getDigitalTwin (@NonNull String digitalTwinId, Class<T> clazz)
    {
        return digitalTwinAsyncClient.getDigitalTwin(digitalTwinId, clazz)
                .toBlocking().single();
    }

    /**
     * Gets a digital twin.
     * @param digitalTwinId The Id of the digital twin.
     * @param clazz The class to deserialize the application/json into.
     * @param <T> The generic type to deserialize the application/json into.
     * @return A {@link ServiceResponseWithHeaders} representing deserialized application/json of the digital twin with {@link DigitalTwinGetHeaders}.
     */
    public <T> ServiceResponseWithHeaders<T, DigitalTwinGetHeaders> getDigitalTwinWithResponse (@NonNull String digitalTwinId, Class<T> clazz)
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
    public Void updateDigitalTwin (@NonNull String digitalTwinId, @NonNull List<Object> digitalTwinUpdateOperations)
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
    public ServiceResponseWithHeaders<Void, DigitalTwinUpdateHeaders> updateDigitalTwinWithResponse (@NonNull String digitalTwinId, @NonNull List<Object> digitalTwinUpdateOperations)
    {
        return digitalTwinAsyncClient.updateDigitalTwinWithResponse(digitalTwinId, digitalTwinUpdateOperations)
                .toBlocking().single();
    }

    /**
     * Updates a digital twin.
     * @param digitalTwinId The Id of the digital twin.
     * @param digitalTwinUpdateOperations The JSON patch to apply to the specified digital twin. This argument can be created using {@link UpdateOperationUtility}.
     * @param options The optional settings for this request.
     * @return A {@link ServiceResponseWithHeaders} with {@link DigitalTwinUpdateHeaders}.
     */
    public ServiceResponseWithHeaders<Void, DigitalTwinUpdateHeaders> updateDigitalTwinWithResponse (@NonNull String digitalTwinId, @NonNull List<Object> digitalTwinUpdateOperations, @NonNull DigitalTwinUpdateRequestOptions options)
    {
        return digitalTwinAsyncClient.updateDigitalTwinWithResponse(digitalTwinId, digitalTwinUpdateOperations, options)
                .toBlocking().single();
    }

    /**
     * Invoke a command on a digital twin.
     * @param digitalTwinId The Id of the digital twin.
     * @param commandName The command to be invoked.
     * @return A {@link DigitalTwinCommandResponse} which contains the application/json command invocation response.
     */
    public DigitalTwinCommandResponse invokeCommand(@NonNull String digitalTwinId, @NonNull String commandName) {
        return digitalTwinAsyncClient.invokeCommand(digitalTwinId, commandName)
                .toBlocking().single();
    }

    /**
     * Invoke a command on a digital twin.
     * @param digitalTwinId The Id of the digital twin.
     * @param commandName The command to be invoked.
     * @param payload The command payload.
     * @return A {@link DigitalTwinCommandResponse} which contains the application/json command invocation response.
     */
    public DigitalTwinCommandResponse invokeCommand(@NonNull String digitalTwinId, @NonNull String commandName, @NonNull String payload) {
        return digitalTwinAsyncClient.invokeCommand(digitalTwinId, commandName, payload)
                .toBlocking().single();
    }

    /**
     * Invoke a command on a digital twin.
     * @param digitalTwinId The Id of the digital twin.
     * @param commandName The command to be invoked.
     * @param payload The command payload.
     * @param options The optional settings for this request.
     * @return A {@link ServiceResponseWithHeaders} with {@link DigitalTwinInvokeRootLevelCommandHeaders} and {@link DigitalTwinCommandResponse} which contains the application/json command invocation response.
     */
    public ServiceResponseWithHeaders<DigitalTwinCommandResponse, DigitalTwinInvokeCommandHeaders> invokeCommandWithResponse(@NonNull String digitalTwinId, @NonNull String commandName, @NonNull String payload, @NonNull DigitalTwinInvokeCommandRequestOptions options) {
        return digitalTwinAsyncClient.invokeCommandWithResponse(digitalTwinId, commandName, payload, options)
                .toBlocking().single();
    }

    /**
     * Invoke a command on a digital twin component.
     * @param digitalTwinId The Id of the digital twin.
     * @param componentName The component name under which the command is defined.
     * @param commandName The command to be invoked.
     * @return A {@link DigitalTwinCommandResponse} which contains the application/json command invocation response.
     */
    public DigitalTwinCommandResponse invokeComponentCommand(@NonNull String digitalTwinId, @NonNull String componentName, @NonNull String commandName) {
        return digitalTwinAsyncClient.invokeComponentCommand(digitalTwinId, componentName, commandName)
                .toBlocking().single();
    }

    /**
     * Invoke a command on a digital twin component.
     * @param digitalTwinId The Id of the digital twin.
     * @param componentName The component name under which the command is defined.
     * @param commandName The command to be invoked.
     * @param payload The command payload.
     * @return A {@link DigitalTwinCommandResponse} which contains the application/json command invocation response.
     */
    public DigitalTwinCommandResponse invokeComponentCommand(@NonNull String digitalTwinId, @NonNull String componentName, @NonNull String commandName, @NonNull String payload) {
        return digitalTwinAsyncClient.invokeComponentCommand(digitalTwinId, componentName, commandName, payload)
                .toBlocking().single();
    }

    /**
     * Invoke a command on a digital twin component.
     * @param digitalTwinId The Id of the digital twin.
     * @param componentName The component name under which the command is defined.
     * @param commandName The command to be invoked.
     * @param payload The command payload.
     * @return A {@link ServiceResponseWithHeaders} with {@link DigitalTwinInvokeRootLevelCommandHeaders} and {@link DigitalTwinCommandResponse} which contains the application/json command invocation response.
     */
    public ServiceResponseWithHeaders<DigitalTwinCommandResponse, DigitalTwinInvokeCommandHeaders> invokeComponentCommandWithResponse(@NonNull String digitalTwinId, @NonNull String componentName, @NonNull String commandName, @NonNull String payload, @NonNull DigitalTwinInvokeCommandRequestOptions options) {
        return digitalTwinAsyncClient.invokeComponentCommandWithResponse(digitalTwinId, componentName, commandName, payload, options)
                .toBlocking().single();
    }
}
