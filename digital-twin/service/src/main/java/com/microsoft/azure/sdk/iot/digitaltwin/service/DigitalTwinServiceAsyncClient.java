// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service;

import com.microsoft.azure.sdk.iot.digitaltwin.service.models.DigitalTwinCommandResponse;
import lombok.NonNull;
import rx.Observable;

import java.io.IOException;

public interface DigitalTwinServiceAsyncClient {

    /**
     * Retrieves the state of a single digital twin
     * @param digitalTwinId The ID of the digital twin. Format of digitalTwinId is DeviceId[~ModuleId]. ModuleId is optional
     * @return The observable to the json string specifying the state of the full digital twin, including all properties of all interface instances registered by that digital twin
     */
    Observable<String> getDigitalTwin(@NonNull String digitalTwinId);

    /**
     * Retrieve the DigitalTwin model definition for the given id
     * @param modelId The model ID. Ex: &lt;example&gt;urn:contoso:TemperatureSensor:1&lt;/example&gt;
     * @return The observable to the DigitalTwin model definition
     */
    Observable<String> getModel(@NonNull String modelId);

    /**
     * Retrieve the DigitalTwin model definition for the given id
     * @param modelId The model ID. Ex: &lt;example&gt;urn:contoso:TemperatureSensor:1&lt;/example&gt;
     * @param expand Indicates whether to expand the device capability model's interface definitions inline or not.
     *               This query parameter ONLY applies to Capability model.
     * @return The observable to the DigitalTwin model definition
     */
    Observable<String> getModel(@NonNull String modelId, boolean expand);

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
     * @return The observable to the json string specifying the updated state  of the digital twin representation
     * @throws IOException Throws IOException if the json deserialization fails
     */
    Observable<String> updateDigitalTwinProperties(@NonNull String digitalTwinId, @NonNull final String interfaceInstanceName, @NonNull String propertyPatch) throws IOException;

    /**
     * Invoke a digital twin command on the given interface instance that is implemented by the given digital twin
     * @param digitalTwinId The digital twin to invoke the command on
     * @param interfaceInstanceName The name of the interface instance in that digital twin that the method belongs to
     * @param commandName The name of the command to be invoked
     * @return The observable to the result of the command invocation, containing the request ID, status code and optional response payload
     */
    Observable<DigitalTwinCommandResponse> invokeCommand(@NonNull String digitalTwinId, @NonNull String interfaceInstanceName, @NonNull String commandName);

    /**
     * Invoke a digital twin command on the given interface instance that is implemented by the given digital twin
     * @param digitalTwinId The digital twin to invoke the command on
     * @param interfaceInstanceName The name of the interface instance in that digital twin that the method belongs to
     * @param commandName The name of the command to be invoked
     * @param argument Additional information to be given to the device receiving the command. Must be UTF-8 encoded JSON bytes
     * @return The observable to the result of the command invocation, containing the request ID, status code and optional response payload
     */
    Observable<DigitalTwinCommandResponse> invokeCommand(@NonNull String digitalTwinId, @NonNull String interfaceInstanceName, @NonNull String commandName, String argument);
}
