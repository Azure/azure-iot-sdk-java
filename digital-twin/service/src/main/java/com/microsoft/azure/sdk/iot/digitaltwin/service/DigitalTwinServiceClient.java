// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service;

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
     * @return The state of the full digital twin, including all properties of all components registered by that digital twin
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

}
