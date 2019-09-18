// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service;

import com.microsoft.azure.sdk.iot.digitaltwin.service.credentials.SasTokenProvider;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.DigitalTwins;
import com.microsoft.azure.sdk.iot.digitaltwin.service.models.DigitalTwin;
import lombok.Builder;
import lombok.NonNull;

import java.io.IOException;

public final class DigitalTwinServiceClientImpl implements DigitalTwinServiceClient {
    private DigitalTwinServiceAsyncClientImpl digitalTwinServiceAsyncClient;

    /***
     * Creates an implementation instance of {@link DigitalTwins} that is used to invoke the Digital Twin features
     * @param connectionString The IoTHub connection string
     */
    @Builder(builderMethodName = "buildFromConnectionString", builderClassName = "FromConnectionStringBuilder")
    DigitalTwinServiceClientImpl(@NonNull String connectionString) {
        digitalTwinServiceAsyncClient = DigitalTwinServiceAsyncClientImpl.buildFromConnectionString()
                                                                         .connectionString(connectionString)
                                                                         .build();
    }

    /***
     * Creates an implementation instance of {@link DigitalTwins} that is used to invoke the Digital Twin features
     * @param sasTokenProvider The sas token provider to use for authorization
     * @param httpsEndpoint The https endpoint to connect to
     */
    @Builder(builderMethodName = "buildFromSasProvider", builderClassName = "FromSasProviderBuilder")
    DigitalTwinServiceClientImpl(@NonNull SasTokenProvider sasTokenProvider, @NonNull String httpsEndpoint) {
        digitalTwinServiceAsyncClient = DigitalTwinServiceAsyncClientImpl.buildFromSasProvider()
                                                                         .sasTokenProvider(sasTokenProvider)
                                                                         .httpsEndpoint(httpsEndpoint)
                                                                         .build();
    }

    @Override
    public DigitalTwin getDigitalTwin(@NonNull String digitalTwinId) {
        return digitalTwinServiceAsyncClient.getDigitalTwin(digitalTwinId).toBlocking().single();
    }

    @Override
    public String getModel(@NonNull String modelId) {
        return digitalTwinServiceAsyncClient.getModel(modelId).toBlocking().single();
    }

    @Override
    public String getModel(@NonNull String modelId, @NonNull Boolean expand) {
        return digitalTwinServiceAsyncClient.getModel(modelId, expand).toBlocking().single();
    }

    @Override
    public DigitalTwin updateDigitalTwinProperties(@NonNull String digitalTwinId, @NonNull final String interfaceInstanceName, @NonNull String propertyPatch) throws IOException {
        return digitalTwinServiceAsyncClient.updateDigitalTwinProperties(digitalTwinId, interfaceInstanceName, propertyPatch).toBlocking().single();
    }

    @Override
    public String invokeCommand(@NonNull String digitalTwinId, @NonNull String interfaceInstanceName, @NonNull String commandName) {
        return digitalTwinServiceAsyncClient.invokeCommand(digitalTwinId, interfaceInstanceName, commandName).toBlocking().single();
    }

    @Override
    public String invokeCommand(@NonNull String digitalTwinId, @NonNull String interfaceInstanceName, @NonNull String commandName, String argument) {
        return digitalTwinServiceAsyncClient.invokeCommand(digitalTwinId, interfaceInstanceName, commandName, argument).toBlocking().single();
    }

    @Override
    public String invokeCommand(@NonNull String digitalTwinId, @NonNull String interfaceInstanceName, @NonNull String commandName, String argument, int connectTimeoutInSeconds, int responseTimeoutInSeconds) {
        return digitalTwinServiceAsyncClient.invokeCommand(digitalTwinId, interfaceInstanceName, commandName, argument, connectTimeoutInSeconds, responseTimeoutInSeconds).toBlocking().single();
    }
}
