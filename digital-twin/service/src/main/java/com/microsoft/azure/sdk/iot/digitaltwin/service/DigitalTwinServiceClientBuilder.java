// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service;

import com.azure.core.implementation.annotation.ServiceClientBuilder;
import com.microsoft.azure.sdk.iot.digitaltwin.service.credentials.IoTServiceClientCredentials;
import com.microsoft.azure.sdk.iot.digitaltwin.service.credentials.ServiceConnectionString;
import com.microsoft.azure.sdk.iot.digitaltwin.service.credentials.ServiceConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.digitaltwin.service.credentials.SharedAccessKeyCredentials;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.implementation.IotHubGatewayServiceAPIs20190701PreviewImpl;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.ServiceResponseBuilder;
import com.microsoft.rest.serializer.JacksonAdapter;

import java.io.IOException;
import java.util.Objects;

@ServiceClientBuilder(
        serviceClients = {DigitalTwinServiceClient.class, DigitalTwinServiceAsyncClient.class})
public final class DigitalTwinServiceClientBuilder
{
    private IoTServiceClientCredentials credentials;
    private DigitalTwinServiceClientOptions options;
    private String endpoint;

    /**
     * The constructor that sets the DigitalTwinServiceClientOptions to default value
     */
    public DigitalTwinServiceClientBuilder()
    {
        this.options = new DigitalTwinServiceClientOptions();
    }

    /**
     * Creates a {@link DigitalTwinServiceClient} based on the options set in the Builder. Every time {@code buildClient()} is
     * called, a new instance of {@link DigitalTwinServiceClient} is created
     * @return A DigitalTwinServiceClient with the options set from the builder
     */
    public DigitalTwinServiceClient buildClient()
    {
        return new DigitalTwinServiceClient(buildAsyncClient());
    }

    /**
     * Creates a {@link DigitalTwinServiceAsyncClient} based on the options set in the Builder. Every time {@code buildAsyncClient()} is
     * called, a new instance of {@link DigitalTwinServiceAsyncClient} is created
     * @return A DigitalTwinServiceAsyncClient with the options set from the builder
     * @throws IllegalStateException If {@link #credential(String httpsEndpoint, IoTServiceClientCredentials credentials)} has not been set
     */
    public DigitalTwinServiceAsyncClient buildAsyncClient()
    {
        if (endpoint == null || credentials == null)
        {
            throw new IllegalStateException("The 'host endpoint' and 'credentials' are required for building DigitalTwinServiceClient");
        }
        RestClient simpleRestClient = new RestClient.Builder().withBaseUrl(endpoint)
                                                              .withCredentials(credentials)
                                                              .withResponseBuilderFactory(new ServiceResponseBuilder.Factory())
                                                              .withSerializerAdapter(new JacksonAdapter())
                                                              .build();

        IotHubGatewayServiceAPIs20190701PreviewImpl protocolLayerClient = new IotHubGatewayServiceAPIs20190701PreviewImpl(simpleRestClient);
        protocolLayerClient.withApiVersion(this.options.getApiVersion());

        return new DigitalTwinServiceAsyncClient(simpleRestClient.retrofit(), protocolLayerClient);
    }

    /**
     * Sets the credential used to authorize requests sent to the service
     * @param connectionString The IoTHub connection string
     * @return The updated DigitalTwinServiceClientBuilder object
     * @throws IOException This exception is thrown if the object creation failed
     */
    public DigitalTwinServiceClientBuilder credential(String connectionString) throws IOException
    {
        ServiceConnectionString serviceConnectionString = ServiceConnectionStringBuilder.createConnectionString(connectionString);
        this.credentials = new SharedAccessKeyCredentials(serviceConnectionString);
        this.endpoint = serviceConnectionString.getHttpsEndpoint();
        return this;
    }

    /**
     * Sets the credential used to authorize requests sent to the service
     * @param httpsEndpoint The IoTHub endpoint to connect to
     * @param credentials The sas token provider to use for authorization
     * @return The updated DigitalTwinServiceClientBuilder object
     */
    public DigitalTwinServiceClientBuilder credential(String httpsEndpoint, IoTServiceClientCredentials credentials)
    {
        this.credentials = Objects.requireNonNull(credentials);
        this.endpoint = Objects.requireNonNull(httpsEndpoint);
        return this;
    }

    /**
     * Sets the DigitalTwinServiceClientOptions for the service client to use
     * @param options The DigitalTwinServiceClientOptions containing the ServiceVersion for the service client to use
     * @return The updated DigitalTwinServiceClientBuilder object
     */
    public DigitalTwinServiceClientBuilder options(DigitalTwinServiceClientOptions options)
    {
        this.options = Objects.requireNonNull(options);
        return this;
    }

}
