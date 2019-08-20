// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.annotation.ReturnType;
import com.azure.core.implementation.annotation.ServiceClient;
import com.azure.core.implementation.annotation.ServiceMethod;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.DigitalTwins;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.implementation.DigitalTwinsImpl;
import com.microsoft.azure.sdk.iot.digitaltwin.service.models.DigitalTwin;

@ServiceClient(
        builder = DigitalTwinServiceClientBuilder.class,
        serviceInterfaces = DigitalTwins.class)
public final class DigitalTwinServiceClient {
    DigitalTwinsImpl digitalTwins;

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
     * @return The response containing the state of the full digital twin, including all properties
     * of all components registered by that digital twin
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DigitalTwin> getDigitalTwin(String digitalTwinId) {
        DigitalTwin digitalTwin = new DigitalTwin(this.digitalTwins.getInterfaces(digitalTwinId));
        return new SimpleResponse<DigitalTwin>(null, digitalTwin);
    }

    /**
     * Retrieves the response containing the response headers and the interface of given interfaceName
     * Example URI: "digitalTwins/{digitalTwinId}/interfaces/{interfaceName}"
     * @param digitalTwinId The ID of the digital twin. Format of digitalTwinId is DeviceId[~ModuleId]. ModuleId is optional
     * @param interfaceName The interface name
     * @return The response containing the the interface of given interfaceName
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DigitalTwin> getDigitalTwinInterface(String digitalTwinId, String interfaceName) {
        DigitalTwin digitalTwin = new DigitalTwin(this.digitalTwins.getInterface(digitalTwinId, interfaceName));
        return new SimpleResponse<DigitalTwin>(null, digitalTwin);
    }

    /**
     * Retrieve the response containing the DigitalTwin model definition for the given id
     * @param modelId The model ID. Ex: &lt;example&gt;urn:contoso:TemperatureSensor:1&lt;/example&gt;
     * @return The response containing the DigitalTwin model definition
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<String> getModel(String modelId) {
        return new SimpleResponse<String>(null, String.valueOf(this.digitalTwins.getDigitalTwinModel(modelId)));
    }

    /**
     * Retrieve the response containing the DigitalTwin model definition for the given id
     * @param modelId The model ID. Ex: &lt;example&gt;urn:contoso:TemperatureSensor:1&lt;/example&gt;
     * @param expand Indicates whether to expand the device capability model's interface definitions inline or not.
     *               This query parameter ONLY applies to Capability model.
     * @return The response containing the DigitalTwin model definition
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<String> getModel(String modelId, Boolean expand) {
        return new SimpleResponse<String>(null, String.valueOf(this.digitalTwins.getDigitalTwinModel(modelId, expand)));
    }

}
