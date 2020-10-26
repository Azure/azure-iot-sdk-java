// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.digitaltwin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.microsoft.azure.sdk.iot.service.digitaltwin.authentication.SasTokenProvider;
import com.microsoft.azure.sdk.iot.service.digitaltwin.authentication.ServiceClientCredentialsProvider;
import com.microsoft.azure.sdk.iot.service.digitaltwin.authentication.ServiceConnectionString;
import com.microsoft.azure.sdk.iot.service.digitaltwin.authentication.ServiceConnectionStringParser;
import com.microsoft.azure.sdk.iot.service.digitaltwin.customized.DigitalTwinGetHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.customized.DigitalTwinUpdateHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.generated.implementation.DigitalTwinsImpl;
import com.microsoft.azure.sdk.iot.service.digitaltwin.generated.implementation.IotHubGatewayServiceAPIsImpl;
import com.microsoft.azure.sdk.iot.service.digitaltwin.generated.DigitalTwins;
import com.microsoft.azure.sdk.iot.service.digitaltwin.generated.models.DigitalTwinInvokeRootLevelCommandHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.serialization.DeserializationHelpers;
import com.microsoft.azure.sdk.iot.service.digitaltwin.serialization.DigitalTwinStringSerializer;
import com.microsoft.azure.sdk.iot.service.digitaltwin.models.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.rest.*;
import com.microsoft.rest.serializer.JacksonAdapter;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.util.List;

import static com.microsoft.azure.sdk.iot.service.digitaltwin.helpers.Tools.*;

/**
 * <p>
 * The Digital Twins Service Client contains asynchronous methods to retrieve and update digital twin information, and invoke commands on a digital twin device.
 * </p>
 * */
public class DigitalTwinAsyncClient {
    private DigitalTwinsImpl _protocolLayer;
    private static ObjectMapper objectMapper = new ObjectMapper();

    DigitalTwinAsyncClient(String connectionString) {
    ServiceConnectionString serviceConnectionString = ServiceConnectionStringParser.parseConnectionString(connectionString);
    SasTokenProvider sasTokenProvider = serviceConnectionString.createSasTokenProvider();
    String httpsEndpoint = serviceConnectionString.getHttpsEndpoint();
    final SimpleModule stringModule = new SimpleModule("String Serializer");
    stringModule.addSerializer(new DigitalTwinStringSerializer(String.class, objectMapper));

    JacksonAdapter adapter = new JacksonAdapter();
    adapter.serializer().registerModule(stringModule);
    RestClient simpleRestClient = new RestClient.Builder()
        .withBaseUrl(httpsEndpoint)
        .withCredentials(new ServiceClientCredentialsProvider(sasTokenProvider))
        .withResponseBuilderFactory(new ServiceResponseBuilder.Factory())
        .withSerializerAdapter(adapter)
        .build();

    IotHubGatewayServiceAPIsImpl protocolLayerClient = new IotHubGatewayServiceAPIsImpl(simpleRestClient);
    _protocolLayer = new DigitalTwinsImpl(simpleRestClient.retrofit(), protocolLayerClient);
    }

    /**
     * Creates an implementation instance of {@link DigitalTwins} that is used to invoke the Digital Twin features
     * @param connectionString The IoTHub connection string
     * @return DigitalTwinAsyncClient
     */
    public static DigitalTwinAsyncClient createFromConnectionString(String connectionString)
    {
        return new DigitalTwinAsyncClient(connectionString);
    }

    /**
     * Gets a digital twin.
     * @param digitalTwinId The Id of the digital twin.
     * @param clazz The class to deserialize the application/json into.
     * @param <T> The generic type to deserialize the application/json into.
     * @return The application/json of the digital twin.
     */
    public <T> Observable<T> getDigitalTwin(String digitalTwinId, Class<T> clazz)
    {
        return getDigitalTwinWithResponse(digitalTwinId, clazz)
                .map(response -> response.body());
    }

    /**
     * Gets a digital twin.
     * @param digitalTwinId The Id of the digital twin.
     * @param clazz The class to deserialize the application/json into.
     * @param <T> The generic type to deserialize the application/json into.
     * @return A {@link ServiceResponseWithHeaders} representing deserialized application/json of the digital twin with {@link DigitalTwinGetHeaders}.
     */
    public <T> Observable<ServiceResponseWithHeaders<T, DigitalTwinGetHeaders>> getDigitalTwinWithResponse(String digitalTwinId, Class<T> clazz)
    {
        if (clazz == null)
        {
            throw new IllegalArgumentException("Parameter clazz is required and cannot be null.");
        }

        return _protocolLayer.getDigitalTwinWithServiceResponseAsync(digitalTwinId)
                .flatMap(FUNC_TO_DIGITAL_TWIN_GET_RESPONSE)
                .flatMap(response -> {
                    try {
                        T genericResponse = DeserializationHelpers.castObject(objectMapper, response.body(), clazz);
                        return Observable.just(new ServiceResponseWithHeaders<>(genericResponse, response.headers(), response.response()));
                    } catch (JsonProcessingException e) {
                        return Observable.error(new IotHubException("Failed to parse the resonse"));
                    }

                })
                .subscribeOn(Schedulers.io());
    }

    /**
     * Updates a digital twin.
     * @param digitalTwinId The Id of the digital twin.
     * @param digitalTwinUpdateOperations The JSON patch to apply to the specified digital twin. This argument can be created using {@link UpdateOperationUtility}.
     * @return void.
     */
    public Observable<Void> updateDigitalTwin(String digitalTwinId, List<Object> digitalTwinUpdateOperations)
    {
        return updateDigitalTwinWithResponse(digitalTwinId, digitalTwinUpdateOperations, null)
                .map(response -> response.body());
    }

    /**
     * Updates a digital twin.
     * @param digitalTwinId The Id of the digital twin.
     * @param digitalTwinUpdateOperations The JSON patch to apply to the specified digital twin. This argument can be created using {@link UpdateOperationUtility}.
     * @return A {@link ServiceResponseWithHeaders} with {@link DigitalTwinUpdateHeaders}.
     */
    public Observable<ServiceResponseWithHeaders<Void, DigitalTwinUpdateHeaders>> updateDigitalTwinWithResponse(String digitalTwinId, List<Object> digitalTwinUpdateOperations)
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
    public Observable<ServiceResponseWithHeaders<Void, DigitalTwinUpdateHeaders>> updateDigitalTwinWithResponse(String digitalTwinId, List<Object> digitalTwinUpdateOperations, DigitalTwinUpdateRequestOptions options)
    {
        String ifMatch = options != null ? options.getIfMatch() : null;
        return _protocolLayer.updateDigitalTwinWithServiceResponseAsync(digitalTwinId, digitalTwinUpdateOperations, ifMatch)
                .flatMap(FUNC_TO_DIGITAL_TWIN_UPDATE_RESPONSE)
                .subscribeOn(Schedulers.io());
    }

    /**
     * Invoke a command on a digital twin.
     * @param digitalTwinId The Id of the digital twin.
     * @param commandName The command to be invoked.
     * @return A {@link DigitalTwinCommandResponse} which contains the application/json command invocation response.
     * @throws IOException can be thrown if the provided payload cannot be deserialized to an Object.
     */
    public Observable<DigitalTwinCommandResponse> invokeCommand(String digitalTwinId, String commandName) throws IOException {
        return invokeCommandWithResponse(digitalTwinId, commandName, null, null)
                .map(response -> response.body());
    }

    /**
     * Invoke a command on a digital twin.
     * @param digitalTwinId The Id of the digital twin.
     * @param commandName The command to be invoked.
     * @param payload The command payload.
     * @return A {@link DigitalTwinCommandResponse} which contains the application/json command invocation response.
     * @throws IOException can be thrown if the provided payload cannot be deserialized into a valid Json object.
     */
    public Observable<DigitalTwinCommandResponse> invokeCommand(String digitalTwinId, String commandName, String payload) throws IOException {
        // Retrofit does not work well with null in body
        return invokeCommandWithResponse(digitalTwinId, commandName, payload, null)
                .map(response -> response.body());
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
    public Observable<ServiceResponseWithHeaders<DigitalTwinCommandResponse, DigitalTwinInvokeCommandHeaders>> invokeCommandWithResponse(String digitalTwinId, String commandName, String payload, DigitalTwinInvokeCommandRequestOptions options) throws IOException {
        if (options == null)
        {
            options = new DigitalTwinInvokeCommandRequestOptions();
        }

        // Retrofit does not work well with null in body
        if (payload == null)
        {
            payload = "";
        }

        return _protocolLayer
                .invokeRootLevelCommandWithServiceResponseAsync(digitalTwinId, commandName, payload, options.getConnectTimeoutInSeconds(), options.getResponseTimeoutInSeconds())
                .flatMap(FUNC_TO_DIGITAL_TWIN_COMMAND_RESPONSE);
    }

    /**
     * Invoke a command on a digital twin component.
     * @param digitalTwinId The Id of the digital twin.
     * @param componentName The component name under which the command is defined.
     * @param commandName The command to be invoked.
     * @return A {@link DigitalTwinCommandResponse} which contains the application/json command invocation response.
     * @throws IOException can be thrown if the provided payload cannot be deserialized into a valid Json object.
     */
    public Observable<DigitalTwinCommandResponse> invokeComponentCommand(String digitalTwinId, String componentName, String commandName) throws IOException {
        return invokeComponentCommandWithResponse(digitalTwinId, componentName, commandName, null, null)
                .map(response -> response.body());
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
    public Observable<DigitalTwinCommandResponse> invokeComponentCommand(String digitalTwinId, String componentName, String commandName, String payload) throws IOException {
        return invokeComponentCommandWithResponse(digitalTwinId, componentName, commandName, payload, null)
                .map(response -> response.body());
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
    public Observable<ServiceResponseWithHeaders<DigitalTwinCommandResponse, DigitalTwinInvokeCommandHeaders>> invokeComponentCommandWithResponse(String digitalTwinId, String componentName, String commandName, String payload, DigitalTwinInvokeCommandRequestOptions options) throws IOException {
        if (options == null)
        {
            options = new DigitalTwinInvokeCommandRequestOptions();
        }

        // Retrofit does not work well with null in body
        if (payload == null)
        {
            payload = "";
        }

        return _protocolLayer.
                invokeComponentCommandWithServiceResponseAsync(digitalTwinId, componentName, commandName, payload, options.getConnectTimeoutInSeconds(), options.getResponseTimeoutInSeconds())
                .flatMap(FUNC_TO_DIGITAL_TWIN_COMPONENT_COMMAND_RESPONSE);
    }
}
