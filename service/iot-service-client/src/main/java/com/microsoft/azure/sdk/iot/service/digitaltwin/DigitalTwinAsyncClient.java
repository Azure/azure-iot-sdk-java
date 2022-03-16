// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.digitaltwin;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.auth.TokenCredentialCache;
import com.microsoft.azure.sdk.iot.service.digitaltwin.authentication.BearerTokenProvider;
import com.microsoft.azure.sdk.iot.service.digitaltwin.authentication.SasTokenProvider;
import com.microsoft.azure.sdk.iot.service.digitaltwin.authentication.ServiceClientBearerTokenCredentialProvider;
import com.microsoft.azure.sdk.iot.service.digitaltwin.authentication.ServiceClientCredentialsProvider;
import com.microsoft.azure.sdk.iot.service.digitaltwin.authentication.ServiceConnectionString;
import com.microsoft.azure.sdk.iot.service.digitaltwin.authentication.ServiceConnectionStringParser;
import com.microsoft.azure.sdk.iot.service.digitaltwin.customized.DigitalTwinGetHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.customized.DigitalTwinUpdateHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.generated.DigitalTwins;
import com.microsoft.azure.sdk.iot.service.digitaltwin.generated.implementation.DigitalTwinsImpl;
import com.microsoft.azure.sdk.iot.service.digitaltwin.generated.implementation.IotHubGatewayServiceAPIsImpl;
import com.microsoft.azure.sdk.iot.service.digitaltwin.generated.models.DigitalTwinInvokeRootLevelCommandHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.models.DigitalTwinCommandResponse;
import com.microsoft.azure.sdk.iot.service.digitaltwin.models.DigitalTwinInvokeCommandHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.models.DigitalTwinInvokeCommandRequestOptions;
import com.microsoft.azure.sdk.iot.service.digitaltwin.models.DigitalTwinUpdateRequestOptions;
import com.microsoft.azure.sdk.iot.service.digitaltwin.serialization.DeserializationHelpers;
import com.microsoft.azure.sdk.iot.service.digitaltwin.serialization.DigitalTwinStringSerializer;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.TransportUtils;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.rest.ServiceResponseBuilder;
import com.microsoft.rest.ServiceResponseWithHeaders;
import com.microsoft.rest.serializer.JacksonAdapter;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.net.Proxy;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.microsoft.azure.sdk.iot.service.digitaltwin.helpers.Tools.*;

/**
 * <p>
 * The Digital Twins Service Client contains asynchronous methods to retrieve and update digital twin information, and invoke commands on a digital twin device.
 * </p>
 */
@Slf4j
public class DigitalTwinAsyncClient {
    private final DigitalTwinsImpl _protocolLayer;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String HTTPS_SCHEME= "https://";

    /**
     * Creates an implementation instance of {@link DigitalTwins} that is used to invoke the Digital Twin features
     *
     * @param connectionString The IoTHub connection string
     */
    public DigitalTwinAsyncClient(String connectionString) {
        this(connectionString, DigitalTwinClientOptions.builder().build());
    }

    /**
     * Creates an implementation instance of {@link DigitalTwins} that is used to invoke the Digital Twin features
     *
     * @param connectionString The IoTHub connection string
     * @param options The optional settings for this client. May not be null.
     */
    public DigitalTwinAsyncClient(String connectionString, DigitalTwinClientOptions options) {
        Objects.requireNonNull(options);
        ServiceConnectionString serviceConnectionString = ServiceConnectionStringParser.parseConnectionString(connectionString);
        SasTokenProvider sasTokenProvider = serviceConnectionString.createSasTokenProvider();
        String httpsEndpoint = serviceConnectionString.getHttpsEndpoint();
        final SimpleModule stringModule = new SimpleModule("String Serializer");
        stringModule.addSerializer(new DigitalTwinStringSerializer(String.class, objectMapper));

        JacksonAdapter adapter = new JacksonAdapter();
        adapter.serializer().registerModule(stringModule);

        ProxyOptions proxyOptions = options.getProxyOptions();
        Proxy proxy = null;
        if (proxyOptions != null)
        {
            proxy = proxyOptions.getProxy();
        }

        RestClient simpleRestClient = new RestClient.Builder()
            .withConnectionTimeout(options.getHttpConnectTimeoutSeconds(), TimeUnit.SECONDS)
            .withReadTimeout(options.getHttpReadTimeoutSeconds(), TimeUnit.SECONDS)
            .withProxy(proxy) // assigning a null proxy here just means no proxy will be used
            .withBaseUrl(httpsEndpoint)
            .withCredentials(new ServiceClientCredentialsProvider(sasTokenProvider))
            .withResponseBuilderFactory(new ServiceResponseBuilder.Factory())
            .withSerializerAdapter(adapter)
            .build();

        IotHubGatewayServiceAPIsImpl protocolLayerClient = new IotHubGatewayServiceAPIsImpl(simpleRestClient);
        _protocolLayer = new DigitalTwinsImpl(simpleRestClient.retrofit(), protocolLayerClient);
        commonConstructorSetup();
    }

    /**
     * Creates an implementation instance of {@link DigitalTwins} that is used to invoke the Digital Twin features
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     * this library when they are needed.
     */
    public DigitalTwinAsyncClient(String hostName, TokenCredential credential) {
        this(hostName, credential, DigitalTwinClientOptions.builder().build());
    }

    /**
     * Creates an implementation instance of {@link DigitalTwins} that is used to invoke the Digital Twin features
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     * this library when they are needed.
     * @param options The optional settings for this client. May not be null.
     */
    public DigitalTwinAsyncClient(String hostName, TokenCredential credential, DigitalTwinClientOptions options) {
        Objects.requireNonNull(options);
        final SimpleModule stringModule = new SimpleModule("String Serializer");
        stringModule.addSerializer(new DigitalTwinStringSerializer(String.class, objectMapper));
        TokenCredentialCache tokenCredentialCache = new TokenCredentialCache(credential);
        BearerTokenProvider bearerTokenProvider = () -> tokenCredentialCache.getTokenString();

        JacksonAdapter adapter = new JacksonAdapter();
        adapter.serializer().registerModule(stringModule);

        ProxyOptions proxyOptions = options.getProxyOptions();
        Proxy proxy = null;
        if (proxyOptions != null)
        {
            proxy = proxyOptions.getProxy();
        }

        RestClient simpleRestClient = new RestClient.Builder()
            .withBaseUrl(HTTPS_SCHEME + hostName) //hostname is only "my-iot-hub.azure-devices.net" so we need to add "https://"
            .withConnectionTimeout(options.getHttpConnectTimeoutSeconds(), TimeUnit.SECONDS)
            .withReadTimeout(options.getHttpReadTimeoutSeconds(), TimeUnit.SECONDS)
            .withProxy(proxy) // assigning a null proxy here just means no proxy will be used
            .withCredentials(new ServiceClientBearerTokenCredentialProvider(bearerTokenProvider))
            .withResponseBuilderFactory(new ServiceResponseBuilder.Factory())
            .withSerializerAdapter(adapter)
            .build();

        IotHubGatewayServiceAPIsImpl protocolLayerClient = new IotHubGatewayServiceAPIsImpl(simpleRestClient);
        _protocolLayer = new DigitalTwinsImpl(simpleRestClient.retrofit(), protocolLayerClient);
        commonConstructorSetup();
    }


    /**
     * Creates an implementation instance of {@link DigitalTwins} that is used to invoke the Digital Twin features
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     */
    public DigitalTwinAsyncClient(String hostName, AzureSasCredential azureSasCredential) {
        this(hostName, azureSasCredential, DigitalTwinClientOptions.builder().build());
    }

    /**
     * Creates an implementation instance of {@link DigitalTwins} that is used to invoke the Digital Twin features
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     * @param options The optional settings for this client. May not be null.
     */
    public DigitalTwinAsyncClient(String hostName, AzureSasCredential azureSasCredential, DigitalTwinClientOptions options) {
        Objects.requireNonNull(options);
        final SimpleModule stringModule = new SimpleModule("String Serializer");
        stringModule.addSerializer(new DigitalTwinStringSerializer(String.class, objectMapper));
        SasTokenProvider sasTokenProvider = azureSasCredential::getSignature;

        JacksonAdapter adapter = new JacksonAdapter();
        adapter.serializer().registerModule(stringModule);

        ProxyOptions proxyOptions = options.getProxyOptions();
        Proxy proxy = null;
        if (proxyOptions != null)
        {
            proxy = proxyOptions.getProxy();
        }

        RestClient simpleRestClient = new RestClient.Builder()
            .withBaseUrl(HTTPS_SCHEME + hostName) //hostname is only "my-iot-hub.azure-devices.net" so we need to add "https://"
            .withConnectionTimeout(options.getHttpConnectTimeoutSeconds(), TimeUnit.SECONDS)
            .withReadTimeout(options.getHttpReadTimeoutSeconds(), TimeUnit.SECONDS)
            .withProxy(proxy) // assigning a null proxy here just means no proxy will be used
            .withCredentials(new ServiceClientCredentialsProvider(sasTokenProvider))
            .withResponseBuilderFactory(new ServiceResponseBuilder.Factory())
            .withSerializerAdapter(adapter)
            .build();

        IotHubGatewayServiceAPIsImpl protocolLayerClient = new IotHubGatewayServiceAPIsImpl(simpleRestClient);
        _protocolLayer = new DigitalTwinsImpl(simpleRestClient.retrofit(), protocolLayerClient);
        commonConstructorSetup();
    }

    /**
     * Creates an implementation instance of {@link DigitalTwins} that is used to invoke the Digital Twin features
     *
     * @param connectionString The IoTHub connection string
     * @return The instantiated DigitalTwinAsyncClient.
     */
    public static DigitalTwinAsyncClient createFromConnectionString(String connectionString) {
        return new DigitalTwinAsyncClient(connectionString);
    }

    private static void commonConstructorSetup() {
        log.debug("Initialized a digital twin client instance using SDK version {}", TransportUtils.serviceVersion);
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
                .map(ServiceResponse::body);
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
                .map(ServiceResponse::body);
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
     */
    public Observable<DigitalTwinCommandResponse> invokeCommand(String digitalTwinId, String commandName)
    {
        return invokeCommandWithResponse(digitalTwinId, commandName, null, null)
                .map(ServiceResponse::body);
    }

    /**
     * Invoke a command on a digital twin.
     * @param digitalTwinId The Id of the digital twin.
     * @param commandName The command to be invoked.
     * @param payload The command payload.
     * @return A {@link DigitalTwinCommandResponse} which contains the application/json command invocation response.
     */
    public Observable<DigitalTwinCommandResponse> invokeCommand(String digitalTwinId, String commandName, String payload)
    {
        // Retrofit does not work well with null in body
        return invokeCommandWithResponse(digitalTwinId, commandName, payload, null)
                .map(ServiceResponse::body);
    }

    /**
     * Invoke a command on a digital twin.
     * @param digitalTwinId The Id of the digital twin.
     * @param commandName The command to be invoked.
     * @param payload The command payload.
     * @param options The optional settings for this request.
     * @return A {@link ServiceResponseWithHeaders} with {@link DigitalTwinInvokeRootLevelCommandHeaders} and {@link DigitalTwinCommandResponse} which contains the application/json command invocation response.
     */
    public Observable<ServiceResponseWithHeaders<DigitalTwinCommandResponse, DigitalTwinInvokeCommandHeaders>> invokeCommandWithResponse(String digitalTwinId, String commandName, String payload, DigitalTwinInvokeCommandRequestOptions options)
    {
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
     */
    public Observable<DigitalTwinCommandResponse> invokeComponentCommand(String digitalTwinId, String componentName, String commandName)
    {
        return invokeComponentCommandWithResponse(digitalTwinId, componentName, commandName, null, null)
                .map(ServiceResponse::body);
    }

    /**
     * Invoke a command on a digital twin component.
     * @param digitalTwinId The Id of the digital twin.
     * @param componentName The component name under which the command is defined.
     * @param commandName The command to be invoked.
     * @param payload The command payload.
     * @return A {@link DigitalTwinCommandResponse} which contains the application/json command invocation response.
     */
    public Observable<DigitalTwinCommandResponse> invokeComponentCommand(String digitalTwinId, String componentName, String commandName, String payload)
    {
        return invokeComponentCommandWithResponse(digitalTwinId, componentName, commandName, payload, null)
                .map(ServiceResponse::body);
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
    public Observable<ServiceResponseWithHeaders<DigitalTwinCommandResponse, DigitalTwinInvokeCommandHeaders>> invokeComponentCommandWithResponse(String digitalTwinId, String componentName, String commandName, String payload, DigitalTwinInvokeCommandRequestOptions options)
    {
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
