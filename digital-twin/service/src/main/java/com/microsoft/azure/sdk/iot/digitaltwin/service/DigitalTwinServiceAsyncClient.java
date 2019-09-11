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
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.models.DigitalTwinInterfaces;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.models.DigitalTwinInterfacesPatch;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.models.DigitalTwinInterfacesPatchInterfacesValue;
import com.microsoft.azure.sdk.iot.digitaltwin.service.models.DigitalTwin;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.ServiceResponseBuilder;
import lombok.Builder;
import rx.Observable;
import rx.functions.Func1;

import java.io.IOException;
import java.util.HashMap;

import static com.microsoft.azure.sdk.iot.digitaltwin.service.util.Tools.FUNC_MAP_TO_STRING;

public final class DigitalTwinServiceAsyncClient {
    DigitalTwinsImpl digitalTwin;

    /***
     * Creates an implementation instance of {@link DigitalTwins} that is used to invoke the Digital Twin features
     * @param connectionString The IoTHub connection string
     * @param sasTokenProvider The sas token provider to use for authorization
     * @param httpsEndpoint The https endpoint to connect to
     * @param apiVersion The ServiceVersion for the service client to use
     * @throws IOException This exception is thrown if the service connection string parsing fails
     * @throws IllegalStateException This exception is thrown if expected paramters are not passed to the builder
     */
    @Builder
    DigitalTwinServiceAsyncClient(String connectionString, SasTokenProvider sasTokenProvider, String
            httpsEndpoint, String apiVersion) throws IOException {

        if (apiVersion == null) {
            apiVersion = ServiceVersion.V2019_07_01_preview.getApiVersion();
        }
        if (sasTokenProvider != null && httpsEndpoint == null) {
            throw new IllegalStateException("Please provide the Sas token provider and host https endpoint to connect to).");
        }
        if (connectionString != null) {
            if (sasTokenProvider !=null || httpsEndpoint != null)
                throw new IllegalStateException("Please provide either 'Service Connection String' or ('ServiceClientCredentails' and host https endpoint to connect to).");

            ServiceConnectionString serviceConnectionString = ServiceConnectionStringParser.parseConnectionString(connectionString);
            sasTokenProvider = serviceConnectionString.createSasTokenProvider();
            httpsEndpoint = serviceConnectionString.getHttpsEndpoint();
        }

        RestClient simpleRestClient = new RestClient.Builder().withBaseUrl(httpsEndpoint)
                                                              .withCredentials(new ServiceClientCredentialsProvider(sasTokenProvider))
                                                              .withResponseBuilderFactory(new ServiceResponseBuilder.Factory())
                                                              .withSerializerAdapter(new CustomJsonAdapter())
                                                              .build();

        IotHubGatewayServiceAPIs20190701PreviewImpl protocolLayerClient = new IotHubGatewayServiceAPIs20190701PreviewImpl(simpleRestClient);
        protocolLayerClient.withApiVersion(apiVersion);

        this.digitalTwin = new DigitalTwinsImpl(simpleRestClient.retrofit(), protocolLayerClient);
    }

    /**
     * Retrieves the state of a single digital twin
     * @param digitalTwinId The ID of the digital twin. Format of digitalTwinId is DeviceId[~ModuleId]. ModuleId is optional
     * @return The observable to the state of the full digital twin, including all properties of all interface instances registered by that digital twin
     */
    public Observable<DigitalTwin> getDigitalTwin(String digitalTwinId) {
        return this.digitalTwin.getInterfacesAsync(digitalTwinId)
                .map(new Func1<DigitalTwinInterfaces, DigitalTwin>() {

                    @Override
                    public DigitalTwin call(DigitalTwinInterfaces digitalTwin) {
                        return new DigitalTwin(digitalTwin);
                    }
                });
    }

    /**
     * Retrieve the DigitalTwin model definition for the given id
     * @param modelId The model ID. Ex: &lt;example&gt;urn:contoso:TemperatureSensor:1&lt;/example&gt;
     * @return The observable to the DigitalTwin model definition
     */
    public Observable<String> getModel(String modelId) {
        return this.digitalTwin.getDigitalTwinModelAsync(modelId)
                .map(FUNC_MAP_TO_STRING);
    }

    /**
     * Retrieve the DigitalTwin model definition for the given id
     * @param modelId The model ID. Ex: &lt;example&gt;urn:contoso:TemperatureSensor:1&lt;/example&gt;
     * @param expand Indicates whether to expand the device capability model's interface definitions inline or not.
     *               This query parameter ONLY applies to Capability model.
     * @return The observable to the DigitalTwin model definition
     */
    public Observable<String> getModel(String modelId, Boolean expand) {
        return this.digitalTwin.getDigitalTwinModelAsync(modelId, expand)
                .map(FUNC_MAP_TO_STRING);
    }

    /**
     * Update one to many properties on one interface instance on one digital twin instance
     * @param digitalTwinId The ID of the digital twin to update
     * @param propertyPatch The JSON representation of the patch. For example, to update two separate properties on the interface instance "sampleDeviceInfo", the JSON should look like:
     *				{
     *                  "properties": {
     *                      "somePropertyName": {
     *                          "desired": {
     *                              "value": "somePropertyValue"
     *                          }
     *                      },
     *                      "somePropertyName2": {
     *                          "desired": {
     *                              "value": "somePropertyValue"
     *                          }
     *                      }
     *                  }
     *              }
     *              Nested properties are allowed, but the maximum depth allowed is 7.
     * @return The observable to the updated state  of the digital twin representation
     * @throws IOException Throws IOException if the json deserialization fails
     */
    public Observable<DigitalTwin> updateDigitalTwinProperties(String digitalTwinId, final String interfaceInstanceName, String propertyPatch) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        final DigitalTwinInterfacesPatchInterfacesValue digitalTwinInterfacesPropertyPatch = objectMapper.readValue(propertyPatch, DigitalTwinInterfacesPatchInterfacesValue.class);

        DigitalTwinInterfacesPatch digitalTwinInterfacesPatch = new DigitalTwinInterfacesPatch()
                .withInterfaces(
                        new HashMap<String, DigitalTwinInterfacesPatchInterfacesValue>() {{
                            put(interfaceInstanceName, digitalTwinInterfacesPropertyPatch);
                        }}
                );
        String digitalTwinInterfacesPatchString = objectMapper.writeValueAsString(digitalTwinInterfacesPatch);

        return this.digitalTwin.updateInterfacesAsync(digitalTwinId, digitalTwinInterfacesPatchString)
                               .map(new Func1<DigitalTwinInterfaces, DigitalTwin>() {

                                   @Override
                                   public DigitalTwin call(DigitalTwinInterfaces digitalTwinInterfaces) {
                                       return new DigitalTwin(digitalTwinInterfaces);
                                   }
                               });
    }

    /**
     * Invoke a digital twin command on the given interface instance that is implemented by the given digital twin
     * @param digitalTwinId The digital twin to invoke the command on
     * @param interfaceInstanceName The name of the interface instance in that digital twin that the method belongs to
     * @param commandName The name of the command to be invoked
     * @param argument Additional information to be given to the device receiving the command. Must be UTF-8 encoded JSON bytes
     * @return The observable to the result of the command invocation. Like the argument given, the result must be
     * UTF-8 encoded JSON bytes
     */
    public Observable<String> invokeCommand(String digitalTwinId, String interfaceInstanceName, String commandName, String argument) {
        return this.digitalTwin.invokeInterfaceCommandAsync(digitalTwinId, interfaceInstanceName, commandName, argument)
                .map(FUNC_MAP_TO_STRING);
    }

    /**
     * Invoke a digital twin command on the given interface instance that is implemented by the given digital twin
     * @param digitalTwinId The digital twin to invoke the command on
     * @param interfaceInstanceName The name of the interface instance in that digital twin that the method belongs to
     * @param commandName The name of the command to be invoked
     * @param argument Additional information to be given to the device receiving the command. Must be UTF-8 encoded JSON bytes
     * @param connectTimeoutInSeconds The connect timeout in seconds
     * @param responseTimeoutInSeconds The response timeout in seconds
     * @return The observable to the result of the command invocation. Like the argument given, the result must be
     * UTF-8 encoded JSON bytes
     */
    public Observable<String> invokeCommand(String digitalTwinId, String interfaceInstanceName, final String commandName, String argument, int connectTimeoutInSeconds, int responseTimeoutInSeconds) {
        return this.digitalTwin.invokeInterfaceCommandAsync(digitalTwinId, interfaceInstanceName, commandName, argument, connectTimeoutInSeconds, responseTimeoutInSeconds)
                .map(FUNC_MAP_TO_STRING);
    }
}
