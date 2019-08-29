// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service;

import com.azure.core.implementation.annotation.ReturnType;
import com.azure.core.implementation.annotation.ServiceClient;
import com.azure.core.implementation.annotation.ServiceMethod;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.DigitalTwins;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.implementation.DigitalTwinsImpl;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.implementation.IotHubGatewayServiceAPIs20190701PreviewImpl;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.models.DigitalTwinInterfacesPatch;
import com.microsoft.azure.sdk.iot.digitaltwin.service.models.DigitalTwin;
import com.microsoft.rest.serializer.JacksonAdapter;
import retrofit2.Retrofit;
import rx.Observable;

import java.io.IOException;

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
     * @return The observable to the state of the full digital twin, including all properties of all interface instances registered by that digital twin
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
     * @return The observable to the updated state of the digital twin representation
     * @throws IOException
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Observable<DigitalTwin> updateDigitalTwinProperties(String digitalTwinId, String patch) throws IOException {
        JacksonAdapter adapter = new JacksonAdapter();
        DigitalTwinInterfacesPatch digitalTwinInterfacesPatch = adapter.deserialize(patch, DigitalTwinInterfacesPatch.class);

        return this.digitalTwins.updateInterfacesAsync(digitalTwinId, digitalTwinInterfacesPatch)
                                .map(DigitalTwin ::new);
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
     * @return The observable to the updated state of the digital twin representation
     * @throws IOException
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Observable<DigitalTwin> updateDigitalTwinProperties(String digitalTwinId, String patch, String etag) throws IOException {
        JacksonAdapter adapter = new JacksonAdapter();
        DigitalTwinInterfacesPatch digitalTwinInterfacesPatch = adapter.deserialize(patch, DigitalTwinInterfacesPatch.class);

        return this.digitalTwins.updateInterfacesAsync(digitalTwinId, digitalTwinInterfacesPatch, etag)
                                .map(DigitalTwin ::new);
    }

}
