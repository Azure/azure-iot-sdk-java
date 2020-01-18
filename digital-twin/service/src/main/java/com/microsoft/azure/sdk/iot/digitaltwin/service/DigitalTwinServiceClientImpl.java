// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service;

import com.microsoft.azure.sdk.iot.digitaltwin.service.credentials.SasTokenProvider;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.DigitalTwins;
import com.microsoft.azure.sdk.iot.digitaltwin.service.models.DigitalTwinCommandResponse;
import lombok.Builder;
import lombok.NonNull;
import lombok.Setter;

import java.io.IOException;

import static lombok.AccessLevel.PACKAGE;

public final class DigitalTwinServiceClientImpl implements DigitalTwinServiceClient {
    @Setter(PACKAGE)
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
    public String getDigitalTwin(@NonNull String digitalTwinId) {
        return digitalTwinServiceAsyncClient.getDigitalTwin(digitalTwinId).toBlocking().single();
    }

    @Override
    public String getModel(@NonNull String modelId) {
        return digitalTwinServiceAsyncClient.getModel(modelId).toBlocking().single();
    }

    @Override
    public String getModel(@NonNull String modelId, boolean expand) {
        return digitalTwinServiceAsyncClient.getModel(modelId, expand).toBlocking().single();
    }

    @Override
    public String updateDigitalTwinProperties(@NonNull String digitalTwinId, @NonNull final String componentName, @NonNull String propertyPatch) throws IOException {
        return digitalTwinServiceAsyncClient.updateDigitalTwinProperties(digitalTwinId, componentName, propertyPatch).toBlocking().single();
    }

    @Override
    public DigitalTwinCommandResponse invokeCommand(@NonNull String digitalTwinId, @NonNull String componentName, @NonNull String commandName) {
        return invokeCommand(digitalTwinId, componentName, commandName, null);
    }

    @Override
    public DigitalTwinCommandResponse invokeCommand(@NonNull String digitalTwinId, @NonNull String componentName, @NonNull String commandName, String argument) {
        return digitalTwinServiceAsyncClient.invokeCommand(digitalTwinId, componentName, commandName, argument).toBlocking().single();
    }
}
