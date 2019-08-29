// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service;

import com.azure.core.implementation.annotation.ReturnType;
import com.azure.core.implementation.annotation.ServiceClient;
import com.azure.core.implementation.annotation.ServiceMethod;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.DigitalTwins;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.implementation.DigitalTwinsImpl;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.implementation.IotHubGatewayServiceAPIs20190701PreviewImpl;
import com.microsoft.azure.sdk.iot.digitaltwin.service.models.DigitalTwin;
import retrofit2.Retrofit;
import rx.Observable;

@ServiceClient(
        builder = DigitalTwinServiceClientBuilder.class,
        serviceInterfaces = DigitalTwins.class,
        isAsync = true)
public final class DigitalTwinServiceAsyncClient {
    DigitalTwinsImpl digitalTwins;

    /**
     * Creates a {@link DigitalTwinsImpl} object that is used to invoke the Digital Twin features
     * @param retrofit The Retrofit instance built from a Retrofit builder containing the service endpoint and credentials
     * @param client The instance of service client containing the operation class
     */
    DigitalTwinServiceAsyncClient(Retrofit retrofit, IotHubGatewayServiceAPIs20190701PreviewImpl client) {
        this.digitalTwins = new DigitalTwinsImpl(retrofit, client);
    }

    /**
     * Retrieves the state of a single digital twin
     * @param digitalTwinId The ID of the digital twin. Format of digitalTwinId is DeviceId[~ModuleId]. ModuleId is optional
     * @return The observable to the state of the full digital twin, including all properties of all components registered by that digital twin
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Observable<DigitalTwin> getDigitalTwin(String digitalTwinId) {
        return this.digitalTwins.getInterfacesAsync(digitalTwinId)
                .map(DigitalTwin ::new);
    }

    /**
     * Retrieve the DigitalTwin model definition for the given id
     * @param modelId The model ID. Ex: &lt;example&gt;urn:contoso:TemperatureSensor:1&lt;/example&gt;
     * @return The observable to the DigitalTwin model definition
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Observable<String> getModel(String modelId) {
        return this.digitalTwins.getDigitalTwinModelAsync(modelId)
                .map(String :: valueOf);
    }

    /**
     * Retrieve the DigitalTwin model definition for the given id
     * @param modelId The model ID. Ex: &lt;example&gt;urn:contoso:TemperatureSensor:1&lt;/example&gt;
     * @param expand Indicates whether to expand the device capability model's interface definitions inline or not.
     *               This query parameter ONLY applies to Capability model.
     * @return The observable to the DigitalTwin model definition
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Observable<String> getModel(String modelId, Boolean expand) {
        return this.digitalTwins.getDigitalTwinModelAsync(modelId, expand)
                .map(String :: valueOf);
    }

}
