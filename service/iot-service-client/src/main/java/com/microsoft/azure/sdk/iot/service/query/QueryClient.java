// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.query;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.auth.TokenCredentialCache;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.jobs.scheduled.ScheduledJobStatus;
import com.microsoft.azure.sdk.iot.service.jobs.scheduled.ScheduledJobType;
import com.microsoft.azure.sdk.iot.service.query.serializers.QueryRequestParser;
import com.microsoft.azure.sdk.iot.service.transport.TransportUtils;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
public final class QueryClient
{
    private static final String CONTINUATION_TOKEN_KEY = "x-ms-continuation";
    private static final String PAGE_SIZE_KEY = "x-ms-max-item-count";

    private final String hostName;
    private TokenCredentialCache credentialCache;
    private AzureSasCredential azureSasCredential;
    private IotHubConnectionString iotHubConnectionString;

    private final QueryClientOptions options;

    /**
     * Constructor to create instance from connection string
     *
     * @param connectionString The IoT Hub connection string
     */
    public QueryClient(String connectionString)
    {
        this(connectionString, QueryClientOptions.builder().build());
    }

    /**
     * Constructor to create instance from connection string
     *
     * @param connectionString The IoT Hub connection string
     * @param options The connection options to use when connecting to the service.
     */
    public QueryClient(String connectionString, QueryClientOptions options)
    {
        if (connectionString == null || connectionString.isEmpty())
        {
            throw new IllegalArgumentException("The provided connection string cannot be null or empty");
        }

        if (options == null)
        {
            throw new IllegalArgumentException("QueryClientOptions cannot be null for this constructor");
        }

        this.iotHubConnectionString =
            IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);

        this.hostName = iotHubConnectionString.getHostName();
        this.options = options;
        commonConstructorSetup();
    }

    /**
     * Create a new QueryClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed. The provided tokens must be Json Web Tokens.
     */
    public QueryClient(String hostName, TokenCredential credential)
    {
        this(hostName, credential, QueryClientOptions.builder().build());
    }

    /**
     * Create a new QueryClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed. The provided tokens must be Json Web Tokens.
     * @param options The connection options to use when connecting to the service.
     */
    public QueryClient(String hostName, TokenCredential credential, QueryClientOptions options)
    {
        Objects.requireNonNull(credential, "credential cannot be null");
        Objects.requireNonNull(options, "options cannot be null");
        if (hostName == null || hostName.isEmpty())
        {
            throw new IllegalArgumentException("hostName cannot be null or empty");
        }

        this.options = options;
        this.credentialCache = new TokenCredentialCache(credential);
        this.hostName = hostName;
        commonConstructorSetup();
    }

    /**
     * Create a new QueryClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     */
    public QueryClient(String hostName, AzureSasCredential azureSasCredential)
    {
        this(hostName, azureSasCredential, QueryClientOptions.builder().build());
    }

    /**
     * Create a new QueryClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     * @param options The connection options to use when connecting to the service.
     */
    public QueryClient(String hostName, AzureSasCredential azureSasCredential, QueryClientOptions options)
    {
        Objects.requireNonNull(azureSasCredential, "azureSasCredential cannot be null");
        Objects.requireNonNull(options, "options cannot be null");
        if (hostName == null || hostName.isEmpty())
        {
            throw new IllegalArgumentException("hostName cannot be null or empty");
        }

        this.options = options;
        this.azureSasCredential = azureSasCredential;
        this.hostName = hostName;
        commonConstructorSetup();
    }

    private static void commonConstructorSetup()
    {
        log.debug("Initialized a QueryClient instance client using SDK version {}", TransportUtils.serviceVersion);
    }

    /**
     * Query from your IoT Hub's set of Twins.
     *
     * @param query The IoT Hub query for selecting which twins to get.
     * @return The pageable set of Twins that were queried.
     * @throws IOException If IoT Hub cannot be reached due to network level issues.
     * @throws IotHubException If the request fails for non-network level issues such as an incorrectly formatted query.
     * @see <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-query-language">IoT Hub query language</a>
     */
    public TwinQueryResponse queryTwins(String query) throws IOException, IotHubException
    {
        return queryTwins(query, QueryPageOptions.builder().build());
    }

    /**
     * Query from your IoT Hub's set of Twins.
     *
     * @param query The IoT Hub query for selecting which twins to get.
     * @param options The optional parameters used to decide how the query's results are returned.
     * @return The pageable set of Twins that were queried.
     * @throws IOException If IoT Hub cannot be reached due to network level issues.
     * @throws IotHubException If the request fails for non-network level issues such as an incorrectly formatted query.
     * @see <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-query-language">IoT Hub query language</a>
     */
    public TwinQueryResponse queryTwins(String query, QueryPageOptions options) throws IOException, IotHubException
    {
        if (query == null || query.isEmpty())
        {
            throw new IllegalArgumentException("Query cannot be null or empty");
        }

        Objects.requireNonNull(options, "Options cannot be null");

        QueryRequestParser requestParser = new QueryRequestParser(query);
        byte[] payload = requestParser.toJson().getBytes(StandardCharsets.UTF_8);
        Proxy proxy = null;
        if (this.options.getProxyOptions() != null)
        {
            proxy = this.options.getProxyOptions().getProxy();
        }

        HttpRequest httpRequest = new HttpRequest(
            IotHubConnectionString.getUrlTwinQuery(hostName),
            HttpMethod.POST,
            payload,
            getAuthenticationToken(),
            proxy);

        setCommonHttpHeaders(httpRequest, options);

        HttpResponse httpResponse = httpRequest.send();

        String responsePayload = new String(httpResponse.getBody(), StandardCharsets.UTF_8);
        TwinQueryResponse twinQueryResponse = new TwinQueryResponse(responsePayload, this, query);

        String continuationToken = httpResponse.getHeaderFields().get(CONTINUATION_TOKEN_KEY); // may be null
        twinQueryResponse.setContinuationToken(continuationToken);

        return twinQueryResponse;
    }

    /**
     * Query from your IoT Hub's set of scheduled jobs.
     *
     * @param query The IoT Hub query for selecting which jobs to get.
     * @return The pageable set of Jobs that were queried.
     * @throws IOException If IoT Hub cannot be reached due to network level issues.
     * @throws IotHubException If the request fails for non-network level issues such as an incorrectly formatted query.
     * @see <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-query-language#get-started-with-jobs-queries">IoT Hub query language</a>
     */
    public JobQueryResponse queryJobs(String query) throws IOException, IotHubException
    {
        return queryJobs(query, QueryPageOptions.builder().build());
    }

    /**
     * Query from your IoT Hub's set of scheduled jobs.
     *
     * @param query The IoT Hub query for selecting which jobs to get.
     * @param options The optional parameters used to decide how the query's results are returned. May not be null.
     * @return The pageable set of Jobs that were queried.
     * @throws IOException If IoT Hub cannot be reached due to network level issues.
     * @throws IotHubException If the request fails for non-network level issues such as an incorrectly formatted query.
     * @see <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-query-language#get-started-with-jobs-queries">IoT Hub query language</a>
     */
    public JobQueryResponse queryJobs(String query, QueryPageOptions options) throws IOException, IotHubException
    {
        Objects.requireNonNull(options, "Options cannot be null");

        Proxy proxy = null;
        if (this.options.getProxyOptions() != null)
        {
            proxy = this.options.getProxyOptions().getProxy();
        }

        QueryRequestParser requestParser = new QueryRequestParser(query);
        byte[] payload = requestParser.toJson().getBytes(StandardCharsets.UTF_8);

        HttpRequest httpRequest = new HttpRequest(
            IotHubConnectionString.getUrlTwinQuery(this.hostName),
            HttpMethod.POST,
            payload,
            getAuthenticationToken(),
            proxy);

        setCommonHttpHeaders(httpRequest, options);

        HttpResponse httpResponse = httpRequest.send();

        String responsePayload = new String(httpResponse.getBody(), StandardCharsets.UTF_8);
        JobQueryResponse jobQueryResponse = new JobQueryResponse(responsePayload, this, query);

        String continuationToken = httpResponse.getHeaderFields().get(CONTINUATION_TOKEN_KEY); // may be null
        jobQueryResponse.setContinuationToken(continuationToken);

        return jobQueryResponse;
    }

    /**
     * Query from your IoT Hub's set of scheduled jobs by job type and job status.
     *
     * @param jobType The type of the job (methods or twin).
     * @param jobStatus The status of the job ("completed", for example)
     * @return The pageable set of Jobs that were queried.
     * @throws IOException If IoT Hub cannot be reached due to network level issues.
     * @throws IotHubException If the request fails for non-network level issues such as throttling.
     */
    public JobQueryResponse queryJobs(ScheduledJobType jobType, ScheduledJobStatus jobStatus) throws IOException, IotHubException
    {
        return queryJobs(jobType, jobStatus, QueryPageOptions.builder().build());
    }

    /**
     * Query from your IoT Hub's set of scheduled jobs by job type and job status.
     *
     * @param jobType The type of the job (methods or twin).
     * @param jobStatus The status of the job ("completed", for example)
     * @param options The optional parameters used to decide how the query's results are returned. May not be null.
     * @return The pageable set of Jobs that were queried.
     * @throws IOException If IoT Hub cannot be reached due to network level issues.
     * @throws IotHubException If the request fails for non-network level issues such as throttling.
     */
    public JobQueryResponse queryJobs(ScheduledJobType jobType, ScheduledJobStatus jobStatus, QueryPageOptions options) throws IOException, IotHubException
    {
        Objects.requireNonNull(options, "Options cannot be null");

        String jobTypeString = (jobType == null) ? null : jobType.toString();
        String jobStatusString = (jobStatus == null) ? null : jobStatus.toString();

        Proxy proxy = null;
        if (this.options.getProxyOptions() != null)
        {
            proxy = this.options.getProxyOptions().getProxy();
        }

        HttpRequest httpRequest = new HttpRequest(
            IotHubConnectionString.getUrlQuery(this.hostName, jobTypeString, jobStatusString),
            HttpMethod.GET,
            new byte[0],
            getAuthenticationToken(),
            proxy);

        setCommonHttpHeaders(httpRequest, options);

        HttpResponse httpResponse = httpRequest.send();

        String responsePayload = new String(httpResponse.getBody(), StandardCharsets.UTF_8);
        JobQueryResponse jobQueryResponse = new JobQueryResponse(responsePayload, this, jobType, jobStatus);

        String continuationToken = httpResponse.getHeaderFields().get(CONTINUATION_TOKEN_KEY); // may be null
        jobQueryResponse.setContinuationToken(continuationToken);

        return jobQueryResponse;
    }

    /**
     * Query miscellaneous data from your IoT Hub.
     *
     * @param query The IoT Hub query for selecting what information should be returned.
     * @return The pageable set of results that were queried.
     * @throws IOException If IoT Hub cannot be reached due to network level issues.
     * @throws IotHubException If the request fails for non-network level issues such as an incorrectly formatted query.
     * @see <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-query-language#basics-of-an-iot-hub-query">IoT Hub query language</a>
     */
    public RawQueryResponse queryRaw(String query) throws IOException, IotHubException
    {
        return queryRaw(query, QueryPageOptions.builder().build());
    }

    /**
     * Query miscellaneous data from your IoT Hub.
     *
     * @param query The IoT Hub query for selecting what information should be returned.
     * @param options The optional parameters used to decide how the query's results are returned. May not be null.
     * @return The pageable set of results that were queried.
     * @throws IOException If IoT Hub cannot be reached due to network level issues.
     * @throws IotHubException If the request fails for non-network level issues such as an incorrectly formatted query.
     * @see <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-query-language#basics-of-an-iot-hub-query">IoT Hub query language</a>
     */
    public RawQueryResponse queryRaw(String query, QueryPageOptions options) throws IOException, IotHubException
    {
        Objects.requireNonNull(options, "Options cannot be null");

        Proxy proxy = null;
        if (this.options.getProxyOptions() != null)
        {
            proxy = this.options.getProxyOptions().getProxy();
        }

        QueryRequestParser requestParser = new QueryRequestParser(query);
        byte[] payload = requestParser.toJson().getBytes(StandardCharsets.UTF_8);

        HttpRequest httpRequest = new HttpRequest(
            IotHubConnectionString.getUrlTwinQuery(this.hostName),
            HttpMethod.POST,
            payload,
            getAuthenticationToken(),
            proxy);

        setCommonHttpHeaders(httpRequest, options);

        HttpResponse httpResponse = httpRequest.send();

        String responsePayload = new String(httpResponse.getBody(), StandardCharsets.UTF_8);
        RawQueryResponse rawQueryResponse = new RawQueryResponse(responsePayload, this, query);

        String continuationToken = httpResponse.getHeaderFields().get(CONTINUATION_TOKEN_KEY); // may be null
        rawQueryResponse.setContinuationToken(continuationToken);

        return rawQueryResponse;
    }

    private String getAuthenticationToken()
    {
        // Three different constructor types for this class, and each type provides either a TokenCredential implementation,
        // an AzureSasCredential instance, or just the connection string. The sas token can be retrieved from the non-null
        // one of the three options.
        if (this.credentialCache != null)
        {
            return this.credentialCache.getTokenString();
        }
        else if (this.azureSasCredential != null)
        {
            return this.azureSasCredential.getSignature();
        }

        return new IotHubServiceSasToken(iotHubConnectionString).toString();
    }

    private void setCommonHttpHeaders(HttpRequest httpRequest, QueryPageOptions options)
    {
        httpRequest.setConnectTimeoutMillis(this.options.getHttpConnectTimeout());
        httpRequest.setReadTimeoutMillis(this.options.getHttpReadTimeout());

        if (options.getContinuationToken() != null)
        {
            httpRequest.setHeaderField(CONTINUATION_TOKEN_KEY, options.getContinuationToken());
        }

        httpRequest.setHeaderField(PAGE_SIZE_KEY, String.valueOf(options.getPageSize()));
    }
}
