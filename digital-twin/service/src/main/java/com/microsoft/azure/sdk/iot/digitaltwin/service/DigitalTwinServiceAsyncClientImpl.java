// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.sdk.iot.digitaltwin.service.credentials.SasTokenProvider;
import com.microsoft.azure.sdk.iot.digitaltwin.service.credentials.ServiceClientCredentialsProvider;
import com.microsoft.azure.sdk.iot.digitaltwin.service.credentials.ServiceConnectionString;
import com.microsoft.azure.sdk.iot.digitaltwin.service.credentials.ServiceConnectionStringParser;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.DigitalTwins;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.implementation.DigitalTwinsImpl;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.implementation.IotHubGatewayServiceAPIs20190701PreviewImpl;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.models.DigitalTwinInterfacesPatch;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.models.DigitalTwinInterfacesPatchInterfacesValue;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.models.DigitalTwinInvokeInterfaceCommandHeaders;
import com.microsoft.azure.sdk.iot.digitaltwin.service.models.DigitalTwinCommandResponse;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.ServiceResponseBuilder;
import com.microsoft.rest.serializer.JacksonAdapter;
import lombok.Builder;
import lombok.NonNull;
import lombok.Setter;
import rx.Observable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import static com.microsoft.azure.sdk.iot.digitaltwin.service.util.Tools.FUNC_MAP_TO_JSON_STRING;
import static com.microsoft.azure.sdk.iot.digitaltwin.service.util.Tools.nullToEmpty;
import static lombok.AccessLevel.PACKAGE;

public final class DigitalTwinServiceAsyncClientImpl implements DigitalTwinServiceAsyncClient {
    @Setter(PACKAGE)
    private DigitalTwins digitalTwin;
    private static ObjectMapper objectMapper = new ObjectMapper();

    /***
     * Creates an implementation instance of {@link DigitalTwins} that is used to invoke the Digital Twin features
     * @param connectionString The IoTHub connection string
     */
    @Builder(builderMethodName = "buildFromConnectionString", builderClassName = "FromConnectionStringBuilder")
    DigitalTwinServiceAsyncClientImpl(@NonNull String connectionString) {
        ServiceConnectionString serviceConnectionString = ServiceConnectionStringParser.parseConnectionString(connectionString);
        SasTokenProvider sasTokenProvider = serviceConnectionString.createSasTokenProvider();
        String httpsEndpoint = serviceConnectionString.getHttpsEndpoint();

        init(sasTokenProvider, httpsEndpoint);
    }

    /***
     * Creates an implementation instance of {@link DigitalTwins} that is used to invoke the Digital Twin features
     * @param sasTokenProvider The sas token provider to use for authorization
     * @param httpsEndpoint The https endpoint to connect to
     */
    @Builder(builderMethodName = "buildFromSasProvider", builderClassName = "FromSasProviderBuilder")
    DigitalTwinServiceAsyncClientImpl(@NonNull SasTokenProvider sasTokenProvider, @NonNull String httpsEndpoint) {
        init(sasTokenProvider, httpsEndpoint);
    }

    private void init(SasTokenProvider sasTokenProvider, String httpsEndpoint) {
        RestClient simpleRestClient = new RestClient.Builder().withBaseUrl(httpsEndpoint)
                                                              .withCredentials(new ServiceClientCredentialsProvider(sasTokenProvider))
                                                              .withResponseBuilderFactory(new ServiceResponseBuilder.Factory())
                                                              .withSerializerAdapter(new JacksonAdapter())
                                                              .build();

        IotHubGatewayServiceAPIs20190701PreviewImpl protocolLayerClient = new IotHubGatewayServiceAPIs20190701PreviewImpl(simpleRestClient);
        digitalTwin = new DigitalTwinsImpl(simpleRestClient.retrofit(), protocolLayerClient);
    }

    public Observable<String> getDigitalTwin(@NonNull String digitalTwinId) {
        return digitalTwin.getInterfacesAsync(digitalTwinId)
                          .filter(Objects :: nonNull)
                          .flatMap(FUNC_MAP_TO_JSON_STRING);
    }

    @Override
    public Observable<String> getModel(@NonNull String modelId) {
        return digitalTwin.getDigitalTwinModelAsync(modelId)
                          .filter(Objects :: nonNull)
                          .flatMap(FUNC_MAP_TO_JSON_STRING);
    }

    @Override
    public Observable<String> getModel(@NonNull String modelId, boolean expand) {
        return digitalTwin.getDigitalTwinModelAsync(modelId, expand)
                          .filter(Objects :: nonNull)
                          .flatMap(FUNC_MAP_TO_JSON_STRING);
    }

    @Override
    public Observable<String> updateDigitalTwinProperties(@NonNull String digitalTwinId, @NonNull final String interfaceInstanceName, @NonNull String propertyPatch) throws IOException {
        final DigitalTwinInterfacesPatchInterfacesValue digitalTwinInterfacesPatchInterfacesValue = objectMapper.readValue(propertyPatch, DigitalTwinInterfacesPatchInterfacesValue.class);

        DigitalTwinInterfacesPatch digitalTwinInterfacesPatch = new DigitalTwinInterfacesPatch()
                .withInterfaces(
                        new HashMap<String, DigitalTwinInterfacesPatchInterfacesValue>() {{
                            put(interfaceInstanceName, digitalTwinInterfacesPatchInterfacesValue);
                        }}
                );
        return digitalTwin.updateInterfacesAsync(digitalTwinId, digitalTwinInterfacesPatch)
                          .filter(Objects :: nonNull)
                          .flatMap(FUNC_MAP_TO_JSON_STRING);
    }

    @Override
    public Observable<DigitalTwinCommandResponse> invokeCommand(@NonNull String digitalTwinId, @NonNull String interfaceInstanceName, @NonNull String commandName) {
        return invokeCommand(digitalTwinId, interfaceInstanceName, commandName, null);
    }

    @Override
    public Observable<DigitalTwinCommandResponse> invokeCommand(@NonNull String digitalTwinId, @NonNull String interfaceInstanceName, @NonNull String commandName, String argument) {
        return digitalTwin.invokeInterfaceCommandWithServiceResponseAsync(digitalTwinId, interfaceInstanceName, commandName, nullToEmpty(argument), null, null)
                .map(responseWithHeaders -> {
                    DigitalTwinInvokeInterfaceCommandHeaders invokeInterfaceCommandHeaders = responseWithHeaders.headers();
                    return new DigitalTwinCommandResponse(invokeInterfaceCommandHeaders.xMsCommandStatuscode(), invokeInterfaceCommandHeaders.xMsRequestId(), responseWithHeaders.body().toString());
                });
    }
}
