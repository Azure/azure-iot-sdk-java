// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.jobs.scheduled;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.service.jobs.scheduled.serializers.ScheduledJobParser;
import com.microsoft.azure.sdk.iot.service.methods.serializers.MethodParser;
import com.microsoft.azure.sdk.iot.service.query.JobQueryResponse;
import com.microsoft.azure.sdk.iot.service.query.QueryClient;
import com.microsoft.azure.sdk.iot.service.query.QueryClientOptions;
import com.microsoft.azure.sdk.iot.service.query.QueryPageOptions;
import com.microsoft.azure.sdk.iot.service.query.serializers.QueryRequestParser;
import com.microsoft.azure.sdk.iot.service.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.service.twin.TwinState;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.auth.TokenCredentialCache;
import com.microsoft.azure.sdk.iot.service.twin.Twin;
import com.microsoft.azure.sdk.iot.service.twin.Pair;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.TransportUtils;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

/**
 * Use the ScheduledJobsClient to schedule and cancel jobs for a group of devices using IoT hub.
 */
@Slf4j
public final class ScheduledJobsClient
{
    private final static byte[] EMPTY_JSON = "{}".getBytes(StandardCharsets.UTF_8);

    private final String hostName;
    private TokenCredentialCache credentialCache;
    private AzureSasCredential azureSasCredential;
    private IotHubConnectionString iotHubConnectionString;
    private final ScheduledJobsClientOptions clientOptions;

    // keep a queryClient within this client so that twins can be queried
    private final QueryClient queryClient;

    /**
     * Constructor to create instance from connection string
     *
     * @param connectionString The iot hub connection string
     */
    public ScheduledJobsClient(String connectionString)
    {
        this(connectionString, ScheduledJobsClientOptions.builder().build());
    }

    /**
     * Constructor to create instance from connection string
     *
     * @param connectionString The iot hub connection string
     * @param clientOptions The connection clientOptions to use when connecting to the service.
     */
    public ScheduledJobsClient(String connectionString, ScheduledJobsClientOptions clientOptions)
    {
        Objects.requireNonNull(clientOptions);
        if (connectionString == null || connectionString.isEmpty())
        {
            throw new IllegalArgumentException("connection string cannot be null or empty");
        }

        this.iotHubConnectionString = IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
        this.hostName = this.iotHubConnectionString.getHostName();
        this.clientOptions = clientOptions;
        commonConstructorSetup();

        QueryClientOptions queryClientOptions =
            QueryClientOptions.builder()
                .httpReadTimeout(clientOptions.getHttpReadTimeout())
                .httpConnectTimeout(clientOptions.getHttpConnectTimeout())
                .proxyOptions(clientOptions.getProxyOptions())
                .build();

        this.queryClient = new QueryClient(connectionString, queryClientOptions);
    }

    /**
     * Create a new ScheduledJobsClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     * this library when they are needed. The provided tokens must be Json Web Tokens.
     */
    public ScheduledJobsClient(String hostName, TokenCredential credential)
    {
        this(hostName, credential, ScheduledJobsClientOptions.builder().build());
    }

    /**
     * Create a new ScheduledJobsClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     * this library when they are needed. The provided tokens must be Json Web Tokens.
     * @param clientOptions The connection clientOptions to use when connecting to the service.
     */
    public ScheduledJobsClient(String hostName, TokenCredential credential, ScheduledJobsClientOptions clientOptions)
    {
        Objects.requireNonNull(credential);
        Objects.requireNonNull(clientOptions);

        if (hostName == null || hostName.isEmpty())
        {
            throw new IllegalArgumentException("hostName cannot be null or empty");
        }

        this.hostName = hostName;
        this.credentialCache = new TokenCredentialCache(credential);
        this.clientOptions = clientOptions;
        commonConstructorSetup();

        QueryClientOptions queryClientOptions =
            QueryClientOptions.builder()
                .httpReadTimeout(clientOptions.getHttpReadTimeout())
                .httpConnectTimeout(clientOptions.getHttpConnectTimeout())
                .proxyOptions(clientOptions.getProxyOptions())
                .build();

        this.queryClient = new QueryClient(hostName, credential, queryClientOptions);
    }

    /**
     * Create a new ScheduledJobsClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     */
    public ScheduledJobsClient(String hostName, AzureSasCredential azureSasCredential)
    {
        this(hostName, azureSasCredential, ScheduledJobsClientOptions.builder().build());
    }

    /**
     * Create a new ScheduledJobsClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     * @param clientOptions The connection clientOptions to use when connecting to the service.
     */
    public ScheduledJobsClient(String hostName, AzureSasCredential azureSasCredential, ScheduledJobsClientOptions clientOptions)
    {
        Objects.requireNonNull(azureSasCredential);
        Objects.requireNonNull(clientOptions);

        if (hostName == null || hostName.isEmpty())
        {
            throw new IllegalArgumentException("hostName cannot be null or empty");
        }

        this.hostName = hostName;
        this.azureSasCredential = azureSasCredential;
        this.clientOptions = clientOptions;
        commonConstructorSetup();

        QueryClientOptions queryClientOptions =
            QueryClientOptions.builder()
                .httpReadTimeout(clientOptions.getHttpReadTimeout())
                .httpConnectTimeout(clientOptions.getHttpConnectTimeout())
                .proxyOptions(clientOptions.getProxyOptions())
                .build();

        this.queryClient = new QueryClient(hostName, azureSasCredential, queryClientOptions);
    }

    private static void commonConstructorSetup()
    {
        log.debug("Initialized a ScheduledJobsClient instance using SDK version {}", TransportUtils.serviceVersion);
    }

    /**
     * Creates a new ScheduledJob to update twin tags and desired properties on one or multiple devices
     *
     * @param jobId Unique ScheduledJob Id for this job
     * @param queryCondition Query condition to evaluate which devices to run the job on. It can be {@code null} or empty
     * @param updateTwin Twin object to use for the update
     * @param startTimeUtc Date time in Utc to start the job
     * @param maxExecutionTimeInSeconds Max execution time in seconds, i.e., ttl duration the job can run
     * @return a jobResult object
     * @throws IOException if the function cannot create a URL for the job
     * @throws IotHubException if the http request failed
     */
    public ScheduledJob scheduleUpdateTwin(String jobId, String queryCondition, Twin updateTwin, Date startTimeUtc, long maxExecutionTimeInSeconds)
            throws IOException, IotHubException
    {
        URL url;

        if (jobId == null || jobId.isEmpty())
        {
            throw new IllegalArgumentException("jobId cannot be null or empty");
        }

        if (updateTwin == null)
        {
            throw new IllegalArgumentException("updateTwin cannot be null");
        }

        if (startTimeUtc == null)
        {
            throw new IllegalArgumentException("startTimeUtc cannot be null");
        }

        if (maxExecutionTimeInSeconds < 0)
        {
            throw new IllegalArgumentException("maxExecutionTimeInSeconds cannot be negative");
        }

        ScheduledJobParser jobsParser =
            new ScheduledJobParser(
                jobId,
                getParserFromDevice(updateTwin),
                queryCondition,
                startTimeUtc,
                maxExecutionTimeInSeconds);

        String json = jobsParser.toJson();

        try
        {
            url = IotHubConnectionString.getUrlJobs(this.hostName, jobId);
        }
        catch (MalformedURLException e)
        {
            throw new IllegalArgumentException("Invalid JobId to create url");
        }

        HttpRequest httpRequest = createRequest(url, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8));

        HttpResponse response = httpRequest.send();

        return new ScheduledJob(new String(response.getBody()));
    }

    /**
     * Creates a new ScheduledJob to invoke method on one or multiple devices
     *
     * @param jobId Unique ScheduledJob Id for this job
     * @param queryCondition Query condition to evaluate which devices to run the job on. It can be {@code null} or empty
     * @param methodName Method name to be invoked
     * @param startTimeUtc Date time in Utc to start the job
     * @return a jobResult object
     * @throws IOException if the function cannot create a URL for the job, or the IO failed on request
     * @throws IotHubException if the http request failed
     */
    public ScheduledJob scheduleDirectMethod(String jobId, String queryCondition, String methodName, Date startTimeUtc)
            throws IOException, IotHubException
    {
        return scheduleDirectMethod(jobId, queryCondition, methodName, startTimeUtc, DirectMethodsJobOptions.builder().build());
    }

    /**
     * Creates a new ScheduledJob to invoke method on one or multiple devices
     *
     * @param jobId Unique ScheduledJob Id for this job
     * @param queryCondition Query condition to evaluate which devices to run the job on. It can be {@code null} or empty
     * @param methodName Method name to be invoked
     * @param startTimeUtc Date time in Utc to start the job
     * @param options the optional parameters for this request. May not be null.
     * @return a jobResult object
     * @throws IOException if the function cannot create a URL for the job, or the IO failed on request
     * @throws IotHubException if the http request failed
     */
    public ScheduledJob scheduleDirectMethod(String jobId, String queryCondition, String methodName, Date startTimeUtc, DirectMethodsJobOptions options)
            throws IOException, IotHubException
    {
        URL url;

        if (jobId == null || jobId.isEmpty())
        {
            throw new IllegalArgumentException("jobId cannot be null or empty");
        }

        if (methodName == null || methodName.isEmpty())
        {
            throw new IllegalArgumentException("method name cannot be null or empty");
        }

        if (startTimeUtc == null)
        {
            throw new IllegalArgumentException("startTimeUtc cannot be null");
        }

        Objects.requireNonNull(options);

        if (options.getMaxExecutionTimeInSeconds() < 0)
        {
            throw new IllegalArgumentException("maxExecutionTimeInSeconds cannot be less than 0");
        }

        MethodParser cloudToDeviceMethod =
            new MethodParser(
                methodName,
                options.getMethodResponseTimeout(),
                options.getMethodConnectTimeout(),
                options.getPayload());

        ScheduledJobParser jobsParser =
            new ScheduledJobParser(
                jobId,
                cloudToDeviceMethod,
                queryCondition,
                startTimeUtc,
                options.getMaxExecutionTimeInSeconds());

        String json = jobsParser.toJson();

        try
        {
            url = IotHubConnectionString.getUrlJobs(this.hostName, jobId);
        }
        catch (MalformedURLException e)
        {
            throw new IllegalArgumentException("Invalid JobId to create url");
        }

        HttpRequest httpRequest = createRequest(url, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8));

        HttpResponse response = httpRequest.send();

        return new ScheduledJob(new String(response.getBody()));
    }

    /**
     * Get the current job on the iotHub.
     *
     * @param jobId Unique ScheduledJob Id for this job
     * @return the retrieved job
     * @throws IOException if the function cannot create a URL for the job, or the IO failed on request
     * @throws IotHubException if the http request failed
     */
    public ScheduledJob get(String jobId) throws IOException, IotHubException
    {
        URL url;

        if (jobId == null || jobId.isEmpty())
        {
            throw new IllegalArgumentException("jobId cannot be null or empty");
        }

        try
        {
            url = IotHubConnectionString.getUrlJobs(this.hostName, jobId);
        }
        catch (MalformedURLException e)
        {
            throw new IllegalArgumentException("Invalid JobId to create url");
        }

        HttpRequest httpRequest = createRequest(url, HttpMethod.GET, new byte[0]);

        HttpResponse response = httpRequest.send();

        return new ScheduledJob(new String(response.getBody()));
    }

    /**
     * Cancel a current jod on the IoTHub
     *
     * @param jobId Unique ScheduledJob Id for this job
     * @return the cancelled job
     * @throws IOException if the function cannot create a URL for the job, or the IO failed on request
     * @throws IotHubException if the http request failed
     */
    public ScheduledJob cancel(String jobId) throws IOException, IotHubException
    {
        URL url;
        if (jobId == null || jobId.isEmpty())
        {
            throw new IllegalArgumentException("jobId cannot be null or empty");
        }

        try
        {
            url = IotHubConnectionString.getUrlJobsCancel(this.hostName, jobId);
        }
        catch (MalformedURLException e)
        {
            throw new IllegalArgumentException("Invalid JobId to create url");
        }

        HttpRequest httpRequest = createRequest(url, HttpMethod.POST, EMPTY_JSON);

        HttpResponse response = httpRequest.send();

        return new ScheduledJob(new String(response.getBody()));
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
    public JobQueryResponse query(String query) throws IOException, IotHubException
    {
        return this.queryClient.queryJobs(query);
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
    public JobQueryResponse query(String query, QueryPageOptions options) throws IOException, IotHubException
    {
        return this.queryClient.queryJobs(query, options);
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
    public JobQueryResponse query(ScheduledJobType jobType, ScheduledJobStatus jobStatus) throws IOException, IotHubException
    {
        return this.queryClient.queryJobs(jobType, jobStatus);
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
    public JobQueryResponse query(ScheduledJobType jobType, ScheduledJobStatus jobStatus, QueryPageOptions options) throws IOException, IotHubException
    {
        return this.queryClient.queryJobs(jobType, jobStatus, options);
    }

    private TwinState getParserFromDevice(Twin device)
    {
        TwinCollection tags = null;
        TwinCollection desired = null;
        TwinCollection reported = null;

        if (device.getTags() != null)
        {
            tags = setToMap(device.getTags());
        }

        if (device.getDesiredProperties() != null)
        {
            desired = setToMap(device.getDesiredProperties());
        }

        if (device.getReportedProperties() != null)
        {
            reported = setToMap(device.getReportedProperties());
        }

        TwinState twinState = new TwinState(tags, desired, reported);

        if (device.getDeviceId() != null)
        {
            twinState.setDeviceId(device.getDeviceId());
        }

        if (device.getETag() == null)
        {
            twinState.setETag("*");
        }
        else
        {
            twinState.setETag(device.getETag());
        }

        return twinState;
    }

    private TwinCollection setToMap(Set<Pair> set)
    {
        TwinCollection map = new TwinCollection();

        if (set != null)
        {
            for (Pair p : set)
            {
                map.put(p.getKey(), p.getValue());
            }
        }
        return map;
    }

    private String getAuthenticationToken()
    {
        // Three different constructor types for this class, and each type provides either a TokenCredential implementation,
        // an AzureSasCredential instance, or just the connection string. The sas token can be retrieved from the non-null
        // one of the three clientOptions.
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

    private HttpRequest createRequest(URL url, HttpMethod method, byte[] payload)
        throws IOException
    {
        Proxy proxy = null;
        if (this.clientOptions.getProxyOptions() != null)
        {
            proxy = this.clientOptions.getProxyOptions().getProxy();
        }

        HttpRequest request = new HttpRequest(url, method, payload, getAuthenticationToken(), proxy);
        request.setReadTimeoutMillis(this.clientOptions.getHttpReadTimeout());
        request.setConnectTimeoutMillis(this.clientOptions.getHttpConnectTimeout());
        return request;
    }
}
