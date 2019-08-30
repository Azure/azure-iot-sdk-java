// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service;

import com.azure.core.implementation.annotation.ReturnType;
import com.azure.core.implementation.annotation.ServiceClient;
import com.azure.core.implementation.annotation.ServiceMethod;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.DigitalTwins;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.implementation.DigitalTwinsImpl;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.models.DigitalTwinInterfacesPatch;
import com.microsoft.azure.sdk.iot.digitaltwin.service.models.DigitalTwin;
import com.microsoft.rest.serializer.JacksonAdapter;

import java.io.IOException;

@ServiceClient(
        builder = DigitalTwinServiceClientBuilder.class,
        serviceInterfaces = DigitalTwins.class)
public final class DigitalTwinServiceClient {
    private DigitalTwinsImpl digitalTwins;

    /**
     * Creates a {@link DigitalTwinsImpl} object that is used to invoke the Digital Twin features
     * @param client The DigitalTwinServiceAsyncCilent containing the DigitalTwinImpl object
     */
    DigitalTwinServiceClient(DigitalTwinServiceAsyncClient client) {
        this.digitalTwins = client.digitalTwins;
    }

    /**
     * Retrieves the state of a single digital twin
     * @param digitalTwinId The ID of the digital twin. Format of digitalTwinId is DeviceId[~ModuleId]. ModuleId is optional
     * @return The state of the full digital twin, including all properties of all interface instances registered by that digital twin
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DigitalTwin getDigitalTwin(String digitalTwinId) {
        return new DigitalTwin(this.digitalTwins.getInterfaces(digitalTwinId));
    }

    /**
     * Retrieve the DigitalTwin model definition for the given id
     * @param modelId The model ID. Ex: &lt;example&gt;urn:contoso:TemperatureSensor:1&lt;/example&gt;
     * @return The DigitalTwin model definition
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public String getModel(String modelId) {
        return String.valueOf(this.digitalTwins.getDigitalTwinModel(modelId));
    }

    /**
     * Retrieve the DigitalTwin model definition for the given id
     * @param modelId The model ID. Ex: &lt;example&gt;urn:contoso:TemperatureSensor:1&lt;/example&gt;
     * @param expand Indicates whether to expand the device capability model's interface definitions inline or not.
     *               This query parameter ONLY applies to Capability model.
     * @return The DigitalTwin model definition
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public String getModel(String modelId, Boolean expand) {
        return String.valueOf(this.digitalTwins.getDigitalTwinModel(modelId, expand));
    }

    /**
     * Update one to many properties on one to many interface instances on one digital twin instance
     * @param digitalTwinId The ID of the digital twin to update
     * @param patch The JSON representation of the patch. For example, to update two separate properties on the interface instance "sampleDeviceInfo", the JSON should look like:
     *              {
     *                  "interfaces": {
     *                      "sampleDeviceInfo": {
     *                          "properties": {
     *                              "somePropertyName": {
     *                                  "desired": {
     *                                      "value": "somePropertyValue"
     *                                  }
     *                              },
     *                              "somePropertyName2": {
     *                                  "desired": {
     *                                      "value": "somePropertyValue"
     *                                  }
     *                              }
     *                          }
     *                      }
     *                  }
     *              }
     *              Nested properties are allowed, but the maximum depth allowed is 7.
     * @return The updated state of the digital twin representation
     * @throws IOException
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DigitalTwin updateDigitalTwinProperties(String digitalTwinId, String patch) throws IOException {
        JacksonAdapter adapter = new JacksonAdapter();
        DigitalTwinInterfacesPatch digitalTwinInterfacesPatch = adapter.deserialize(patch, DigitalTwinInterfacesPatch.class);

        return new DigitalTwin(this.digitalTwins.updateInterfaces(digitalTwinId, digitalTwinInterfacesPatch));
    }

    /**
     * Update one to many properties on one to many interface instances on one digital twin instance
     * @param digitalTwinId The ID of the digital twin to update
     * @param patch The JSON representation of the patch. For example, to update two separate properties on the interface instance "sampleDeviceInfo", the JSON should look like:
     *              {
     *                  "interfaces": {
     *                      "sampleDeviceInfo": {
     *                          "properties": {
     *                              "somePropertyName": {
     *                                  "desired": {
     *                                      "value": "somePropertyValue"
     *                                  }
     *                              },
     *                              "somePropertyName2": {
     *                                  "desired": {
     *                                      "value": "somePropertyValue"
     *                                  }
     *                              }
     *                          }
     *                      }
     *                  }
     *              }
     *              Nested properties are allowed, but the maximum depth allowed is 7.
     * @param etag The ETag of the digital twin
     * @return The updated state of the digital twin representation
     * @throws IOException
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DigitalTwin updateDigitalTwinProperties(String digitalTwinId, String patch, String etag) throws IOException {
        JacksonAdapter adapter = new JacksonAdapter();
        DigitalTwinInterfacesPatch digitalTwinInterfacesPatch = adapter.deserialize(patch, DigitalTwinInterfacesPatch.class);

        return new DigitalTwin(this.digitalTwins.updateInterfaces(digitalTwinId, digitalTwinInterfacesPatch, etag));
    }

    /**
     * Invoke a digital twin command on the given interface instance that is implemented by the given digital twin
     * @param digitalTwinId The digital twin to invoke the command on
     * @param interfaceInstanceName The name of the interface instance in that digital twin that the method belongs to
     * @param commandName The name of the command to be invoked
     * @param argument Additional information to be given to the device receiving the command. Must be UTF-8 encoded JSON bytes
     * @return The result of the command invocation. Like the argument given, it must be UTF-8 encoded JSON bytes
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public String invokeCommand(String digitalTwinId, String interfaceInstanceName, String commandName, String argument) {
        return String.valueOf(this.digitalTwins.invokeInterfaceCommand(digitalTwinId, interfaceInstanceName, commandName, argument));
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    public String invokeCommand(String digitalTwinId, String interfaceInstanceName, String commandName, String argument, int connectTimeoutInSeconds, int responseTimeoutInSeconds) {
        return String.valueOf(this.digitalTwins.invokeInterfaceCommand(digitalTwinId, interfaceInstanceName, commandName, argument, connectTimeoutInSeconds, responseTimeoutInSeconds));
    }

}
