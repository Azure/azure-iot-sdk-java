package com.microsoft.azure.sdk.iot.service.digitaltwin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.sdk.iot.service.digitaltwin.authentication.SasTokenProvider;
import com.microsoft.azure.sdk.iot.service.digitaltwin.authentication.ServiceClientCredentialsProvider;
import com.microsoft.azure.sdk.iot.service.digitaltwin.authentication.ServiceConnectionString;
import com.microsoft.azure.sdk.iot.service.digitaltwin.authentication.ServiceConnectionStringParser;
import com.microsoft.azure.sdk.iot.service.digitaltwin.generated.implementation.DigitalTwinsImpl;
import com.microsoft.azure.sdk.iot.service.digitaltwin.generated.implementation.IotHubGatewayServiceAPIsImpl;
import com.microsoft.azure.sdk.iot.service.digitaltwin.generated.DigitalTwins;
import com.microsoft.azure.sdk.iot.service.digitaltwin.generated.models.DigitalTwinGetHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.generated.models.DigitalTwinInvokeRootLevelCommandHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.generated.models.DigitalTwinUpdateHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.helpers.DeserializationHelpers;
import com.microsoft.azure.sdk.iot.service.digitaltwin.helpers.UpdateOperationUtility;
import com.microsoft.azure.sdk.iot.service.digitaltwin.models.DigitalTwinCommandResponse;
import com.microsoft.azure.sdk.iot.service.digitaltwin.models.DigitalTwinInvokeCommandHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.models.DigitalTwinInvokeCommandRequestOptions;
import com.microsoft.azure.sdk.iot.service.digitaltwin.models.DigitalTwinUpdateRequestOptions;
import com.microsoft.rest.*;
import com.microsoft.rest.serializer.JacksonAdapter;
import lombok.Builder;
import lombok.NonNull;
import lombok.Setter;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.List;
import java.util.Objects;

import static com.microsoft.azure.sdk.iot.service.digitaltwin.helpers.Tools.*;
import static lombok.AccessLevel.PACKAGE;

public class DigitalTwinAsyncClient {
    @Setter(PACKAGE)
    private DigitalTwinsImpl digitalTwin;
    private static ObjectMapper objectMapper = new ObjectMapper();

    /***
     * Creates an implementation instance of {@link DigitalTwins} that is used to invoke the Digital Twin features
     * @param connectionString The IoTHub connection string
     */
    @Builder(builderMethodName = "buildFromConnectionString", builderClassName = "FromConnectionStringBuilder")
    public DigitalTwinAsyncClient(@NonNull String connectionString) {
        ServiceConnectionString serviceConnectionString = ServiceConnectionStringParser.parseConnectionString(connectionString);
        SasTokenProvider sasTokenProvider = serviceConnectionString.createSasTokenProvider();
        String httpsEndpoint = serviceConnectionString.getHttpsEndpoint();

        init(sasTokenProvider, httpsEndpoint);
    }

    private void init(SasTokenProvider sasTokenProvider, String httpsEndpoint) {
        RestClient simpleRestClient = new RestClient.Builder()
                .withBaseUrl(httpsEndpoint)
                .withCredentials(new ServiceClientCredentialsProvider(sasTokenProvider))
                .withResponseBuilderFactory(new ServiceResponseBuilder.Factory())
                .withSerializerAdapter(new JacksonAdapter())
                .build();

        IotHubGatewayServiceAPIsImpl protocolLayerClient = new IotHubGatewayServiceAPIsImpl(simpleRestClient);
        digitalTwin = new DigitalTwinsImpl(simpleRestClient.retrofit(), protocolLayerClient);
    }

    /**
     * Gets a digital twin.
     * @param digitalTwinId The Id of the digital twin.
     * @param clazz The class to deserialize the application/json into.
     * @param <T> The generic type to deserialize the application/json into.
     * @return The application/json of the digital twin.
     */
    public <T> Observable<T> getDigitalTwin (@NonNull String digitalTwinId, Class<T> clazz)
    {
        return digitalTwin.getDigitalTwinAsync(digitalTwinId)
                .filter(Objects::nonNull)
                .map(response -> {
                    T genericResponse = null;
                    try {
                        genericResponse = DeserializationHelpers.castObject(objectMapper, response, clazz);
                    } catch (JsonProcessingException e) {
                        Observable.error(e);
                    }
                    return genericResponse;
                })
                .subscribeOn(Schedulers.io());
    }

    /**
     * Gets a digital twin.
     * @param digitalTwinId The Id of the digital twin.
     * @param clazz The class to deserialize the application/json into.
     * @param <T> The generic type to deserialize the application/json into.
     * @return A {@link ServiceResponseWithHeaders} representing deserialized application/json of the digital twin with {@link DigitalTwinGetHeaders}.
     */
    public <T> Observable<ServiceResponseWithHeaders<T, DigitalTwinGetHeaders>> getDigitalTwinWithResponse (@NonNull String digitalTwinId, Class<T> clazz)
    {
        return digitalTwin.getDigitalTwinWithServiceResponseAsync(digitalTwinId)
                .map(response -> {
                    T genericResponse = null;
                    try {
                        genericResponse = DeserializationHelpers.castObject(objectMapper, response.body(), clazz);
                    } catch (JsonProcessingException e) {
                        Observable.error(e);
                    }
                    return new ServiceResponseWithHeaders<>(genericResponse, response.headers(), response.response());
                })
                .subscribeOn(Schedulers.io());
    }

    /**
     * Updates a digital twin.
     * @param digitalTwinId The Id of the digital twin.
     * @param digitalTwinUpdateOperations The JSON patch to apply to the specified digital twin. This argument can be created using {@link UpdateOperationUtility}.
     * @return void.
     */
    public Observable<Void> updateDigitalTwin (@NonNull String digitalTwinId, @NonNull List<Object> digitalTwinUpdateOperations)
    {
        return digitalTwin.updateDigitalTwinAsync(digitalTwinId, digitalTwinUpdateOperations)
                .subscribeOn(Schedulers.io());
    }

    /**
     * Updates a digital twin.
     * @param digitalTwinId The Id of the digital twin.
     * @param digitalTwinUpdateOperations The JSON patch to apply to the specified digital twin. This argument can be created using {@link UpdateOperationUtility}.
     * @return A {@link ServiceResponseWithHeaders} with {@link DigitalTwinUpdateHeaders}.
     */
    public Observable<ServiceResponseWithHeaders<Void, DigitalTwinUpdateHeaders>> updateDigitalTwinWithResponse (@NonNull String digitalTwinId, @NonNull List<Object> digitalTwinUpdateOperations)
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
    public Observable<ServiceResponseWithHeaders<Void, DigitalTwinUpdateHeaders>> updateDigitalTwinWithResponse (@NonNull String digitalTwinId, @NonNull List<Object> digitalTwinUpdateOperations, @NonNull DigitalTwinUpdateRequestOptions options)
    {
        String ifMatch = options != null ? options.getIfMatch() : null;
        return digitalTwin.updateDigitalTwinWithServiceResponseAsync(digitalTwinId, digitalTwinUpdateOperations, ifMatch)
                .subscribeOn(Schedulers.io());
    }

    /**
     * Invoke a command on a digital twin.
     * @param digitalTwinId The Id of the digital twin.
     * @param commandName The command to be invoked.
     * @return A {@link DigitalTwinCommandResponse} which contains the application/json command invocation response.
     */
    public Observable<DigitalTwinCommandResponse> invokeCommand (@NonNull String digitalTwinId, @NonNull String commandName)
    {
        return digitalTwin.invokeRootLevelCommandWithServiceResponseAsync(digitalTwinId, commandName)
                .flatMap(FUNC_TO_DIGITAL_TWIN_COMMAND_RESPONSE);
    }

    /**
     * Invoke a command on a digital twin.
     * @param digitalTwinId The Id of the digital twin.
     * @param commandName The command to be invoked.
     * @param payload The command payload.
     * @return A {@link DigitalTwinCommandResponse} which contains the application/json command invocation response.
     */
    public Observable<DigitalTwinCommandResponse> invokeCommand (@NonNull String digitalTwinId, @NonNull String commandName, @NonNull String payload)
    {
        return digitalTwin.invokeRootLevelCommandWithServiceResponseAsync(digitalTwinId, commandName, payload, null, null)
                .flatMap(FUNC_TO_DIGITAL_TWIN_COMMAND_RESPONSE);
    }

    /**
     * Invoke a command on a digital twin.
     * @param digitalTwinId The Id of the digital twin.
     * @param commandName The command to be invoked.
     * @param payload The command payload.
     * @param options The optional settings for this request.
     * @return A {@link ServiceResponseWithHeaders} with {@link DigitalTwinInvokeRootLevelCommandHeaders} and {@link DigitalTwinCommandResponse} which contains the application/json command invocation response.
     */
    public Observable<ServiceResponseWithHeaders<DigitalTwinCommandResponse, DigitalTwinInvokeCommandHeaders>> invokeCommandWithResponse (@NonNull String digitalTwinId, @NonNull String commandName, @NonNull String payload, @NonNull DigitalTwinInvokeCommandRequestOptions options)
    {
        return digitalTwin.invokeRootLevelCommandWithServiceResponseAsync(digitalTwinId, commandName, payload, options.getConnectTimeoutInSeconds(), options.getResponseTimeoutInSeconds())
                .flatMap(FUNC_TO_DIGITAL_TWIN_COMMAND_RESPONSE_WITH_HEADERS);
    }

    /**
     * Invoke a command on a digital twin component.
     * @param digitalTwinId The Id of the digital twin.
     * @param componentName The component name under which the command is defined.
     * @param commandName The command to be invoked.
     * @return A {@link DigitalTwinCommandResponse} which contains the application/json command invocation response.
     */
    public Observable<DigitalTwinCommandResponse> invokeComponentCommand(@NonNull String digitalTwinId, @NonNull String componentName, @NonNull String commandName)
    {
        return digitalTwin.invokeComponentCommandWithServiceResponseAsync(digitalTwinId, componentName, commandName)
                .flatMap(FUNC_TO_DIGITAL_TWIN_COMPONENT_COMMAND_RESPONSE);
    }

    /**
     * Invoke a command on a digital twin component.
     * @param digitalTwinId The Id of the digital twin.
     * @param componentName The component name under which the command is defined.
     * @param commandName The command to be invoked.
     * @param payload The command payload.
     * @return A {@link DigitalTwinCommandResponse} which contains the application/json command invocation response.
     */
    public Observable<DigitalTwinCommandResponse> invokeComponentCommand(@NonNull String digitalTwinId, @NonNull String componentName, @NonNull String commandName, @NonNull String payload)
    {
        return digitalTwin.invokeComponentCommandWithServiceResponseAsync(digitalTwinId, componentName, commandName, payload, null, null)
                .flatMap(FUNC_TO_DIGITAL_TWIN_COMPONENT_COMMAND_RESPONSE);
    }

    /**
     * Invoke a command on a digital twin component.
     * @param digitalTwinId The Id of the digital twin.
     * @param componentName The component name under which the command is defined.
     * @param commandName The command to be invoked.
     * @param payload The command payload.
     * @param options The optional settings for this request.
     * @return A {@link ServiceResponseWithHeaders} with {@link DigitalTwinInvokeRootLevelCommandHeaders} and {@link DigitalTwinCommandResponse} which contains the application/json command invocation response.
     */
    public Observable<ServiceResponseWithHeaders<DigitalTwinCommandResponse, DigitalTwinInvokeCommandHeaders>> invokeComponentCommandWithResponse (@NonNull String digitalTwinId, @NonNull String componentName, @NonNull String commandName, @NonNull String payload, @NonNull DigitalTwinInvokeCommandRequestOptions options)
    {
        return digitalTwin.invokeComponentCommandWithServiceResponseAsync(digitalTwinId, componentName, commandName, payload, options.getConnectTimeoutInSeconds(), options.getResponseTimeoutInSeconds())
                .flatMap(FUNC_TO_DIGITAL_TWIN_COMPONENT_COMMAND_RESPONSE_WITH_HEADERS);
    }
}
