// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service;

import com.microsoft.azure.sdk.iot.digitaltwin.service.credentials.IoTServiceClientCredentials;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.DigitalTwins;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.implementation.DigitalTwinsImpl;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.models.DigitalTwinInterfacesPatch;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.models.DigitalTwinInterfacesPatchInterfacesValue;
import com.microsoft.azure.sdk.iot.digitaltwin.service.models.DigitalTwin;
import com.microsoft.rest.serializer.JacksonAdapter;
import lombok.Builder;

import java.io.IOException;
import java.util.HashMap;

import static com.microsoft.azure.sdk.iot.digitaltwin.service.util.Tools.FUNC_MAP_TO_STRING;

public final class DigitalTwinServiceClient {
    private DigitalTwinsImpl digitalTwin;

    /***
     * Creates an implementation instance of {@link DigitalTwins} that is used to invoke the Digital Twin features
     * @param connectionString The IoTHub connection string
     * @param credential The sas token provider to use for authorization
     * @param httpsEndpoint The https endpoint to connect to
     * @param apiVersion The ServiceVersion for the service client to use
     * @throws IOException This exception is thrown if the service connection string parsing fails
     * @throws IllegalStateException This exception is thrown if expected paramters are not passed to the builder
     */
    @Builder
    DigitalTwinServiceClient(String connectionString, IoTServiceClientCredentials credential, String
            httpsEndpoint, String apiVersion) throws IOException {

        DigitalTwinServiceAsyncClient digitalTwinServiceAsyncClient = DigitalTwinServiceAsyncClient.builder()
                                                                                                   .apiVersion(apiVersion)
                                                                                                   .connectionString(connectionString)
                                                                                                   .credential(credential)
                                                                                                   .httpsEndpoint(httpsEndpoint).build();
        this.digitalTwin = digitalTwinServiceAsyncClient.digitalTwin;
    }

    /**
     * Retrieves the state of a single digital twin
     * @param digitalTwinId The ID of the digital twin. Format of digitalTwinId is DeviceId[~ModuleId]. ModuleId is optional
     * @return The state of the full digital twin, including all properties of all interface instances registered by that digital twin
     */
    public DigitalTwin getDigitalTwin(String digitalTwinId) {
        return new DigitalTwin(this.digitalTwin.getInterfaces(digitalTwinId));
    }

    /**
     * Retrieve the DigitalTwin model definition for the given id
     * @param modelId The model ID. Ex: &lt;example&gt;urn:contoso:TemperatureSensor:1&lt;/example&gt;
     * @return The DigitalTwin model definition
     */
    public String getModel(String modelId) {
        return FUNC_MAP_TO_STRING.call(this.digitalTwin.getDigitalTwinModel(modelId));
    }

    /**
     * Retrieve the DigitalTwin model definition for the given id
     * @param modelId The model ID. Ex: &lt;example&gt;urn:contoso:TemperatureSensor:1&lt;/example&gt;
     * @param expand Indicates whether to expand the device capability model's interface definitions inline or not.
     *               This query parameter ONLY applies to Capability model.
     * @return The DigitalTwin model definition
     */
    public String getModel(String modelId, Boolean expand) {
        return FUNC_MAP_TO_STRING.call(this.digitalTwin.getDigitalTwinModel(modelId, expand));
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
     * @return The updated state of the digital twin representation
     * @throws IOException Throws IOException if the json deserialization fails
     */
    public DigitalTwin updateDigitalTwinProperties(String digitalTwinId, final String interfaceInstanceName, String propertyPatch) throws IOException {
        JacksonAdapter adapter = new JacksonAdapter();
        final DigitalTwinInterfacesPatchInterfacesValue digitalTwinInterfacesPropertyPatch = adapter.deserialize(propertyPatch, DigitalTwinInterfacesPatchInterfacesValue.class);

        DigitalTwinInterfacesPatch digitalTwinInterfacesPatch = new DigitalTwinInterfacesPatch()
                .withInterfaces(
                        new HashMap<String, DigitalTwinInterfacesPatchInterfacesValue>() {{
                            put(interfaceInstanceName, digitalTwinInterfacesPropertyPatch);
                        }}
                );

        return new DigitalTwin(this.digitalTwin.updateInterfaces(digitalTwinId, digitalTwinInterfacesPatch));
    }

    /**
     * Invoke a digital twin command on the given interface instance that is implemented by the given digital twin
     * @param digitalTwinId The digital twin to invoke the command on
     * @param interfaceInstanceName The name of the interface instance in that digital twin that the method belongs to
     * @param commandName The name of the command to be invoked
     * @param argument Additional information to be given to the device receiving the command. Must be UTF-8 encoded JSON bytes
     * @return The result of the command invocation. Like the argument given, it must be UTF-8 encoded JSON bytes
     */
    public String invokeCommand(String digitalTwinId, String interfaceInstanceName, String commandName, String argument) {
        return FUNC_MAP_TO_STRING.call(this.digitalTwin.invokeInterfaceCommand(digitalTwinId, interfaceInstanceName, commandName, argument));
    }

    /**
     * Invoke a digital twin command on the given interface instance that is implemented by the given digital twin
     * @param digitalTwinId The digital twin to invoke the command on
     * @param interfaceInstanceName The name of the interface instance in that digital twin that the method belongs to
     * @param commandName The name of the command to be invoked
     * @param argument Additional information to be given to the device receiving the command. Must be UTF-8 encoded JSON bytes
     * @param connectTimeoutInSeconds The connect timeout in seconds
     * @param responseTimeoutInSeconds The response timeout in seconds
     * @return The result of the command invocation. Like the argument given, it must be UTF-8 encoded JSON bytes
     */
    public String invokeCommand(String digitalTwinId, String interfaceInstanceName, String commandName, String argument, int connectTimeoutInSeconds, int responseTimeoutInSeconds) {
        return FUNC_MAP_TO_STRING.call(this.digitalTwin.invokeInterfaceCommand(digitalTwinId, interfaceInstanceName, commandName, argument, connectTimeoutInSeconds, responseTimeoutInSeconds));
    }

}
