// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.jobs;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.deps.serializer.JobsParser;
import com.microsoft.azure.sdk.iot.deps.serializer.MethodParser;
import com.microsoft.azure.sdk.iot.deps.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.deps.twin.TwinState;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.Tools;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.auth.TokenCredentialCache;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceOperations;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinDevice;
import com.microsoft.azure.sdk.iot.service.devicetwin.Pair;
import com.microsoft.azure.sdk.iot.service.devicetwin.Query;
import com.microsoft.azure.sdk.iot.service.devicetwin.QueryType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 * Use the JobClient to schedule and cancel jobs for a group of devices using IoT hub.
 */
public class JobClient
{
    private final static Integer DEFAULT_PAGE_SIZE = 100;

    private final static byte[] EMPTY_JSON = "{}".getBytes();

    private String hostName;
    private TokenCredentialCache credentialCache;
    private AzureSasCredential azureSasCredential;
    private IotHubConnectionString iotHubConnectionString;
    private JobClientOptions options;

    /**
     * Static constructor to create instance from connection string
     *
     * @param connectionString The iot hub connection string
     * @return The instance of JobClient
     * @throws IOException This exception is never thrown.
     * @throws IllegalArgumentException if the provided connectionString is {@code null} or empty
     * @deprecated because this method declares a thrown IOException even though it never throws an IOException. Users
     * are recommended to use {@link #JobClient(String)} instead
     * since it does not declare this exception even though it constructs the same JobClient.
     */
    @Deprecated
    public static JobClient createFromConnectionString(String connectionString)
        throws IOException, IllegalArgumentException
    {
        return new JobClient(connectionString);
    }

    /**
     * Constructor to create instance from connection string
     *
     * @param connectionString The iot hub connection string
     */
    public JobClient(String connectionString)
    {
        this(connectionString, JobClientOptions.builder().build());
    }

    /**
     * Constructor to create instance from connection string
     *
     * @param connectionString The iot hub connection string
     * @param options The connection options to use when connecting to the service.
     */
    public JobClient(String connectionString, JobClientOptions options)
    {
        Objects.requireNonNull(options);
        if (Tools.isNullOrEmpty(connectionString))
        {
            throw new IllegalArgumentException("connection string cannot be null or empty");
        }

        this.iotHubConnectionString = IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
        this.hostName = this.iotHubConnectionString.getHostName();
        this.options = options;
    }

    /**
     * Create a new JobClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     * this library when they are needed. The provided tokens must be Json Web Tokens.
     */
    public JobClient(String hostName, TokenCredential credential)
    {
        this(hostName, credential, JobClientOptions.builder().build());
    }

    /**
     * Create a new JobClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     * this library when they are needed. The provided tokens must be Json Web Tokens.
     * @param options The connection options to use when connecting to the service.
     */
    public JobClient(String hostName, TokenCredential credential, JobClientOptions options)
    {
        Objects.requireNonNull(credential);
        Objects.requireNonNull(options);

        if (Tools.isNullOrEmpty(hostName))
        {
            throw new IllegalArgumentException("hostName cannot be null or empty");
        }

        this.hostName = hostName;
        this.credentialCache = new TokenCredentialCache(credential, options.getTokenCredentialAuthenticationScopes());
        this.options = options;
    }

    /**
     * Create a new JobClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     */
    public JobClient(String hostName, AzureSasCredential azureSasCredential)
    {
        this(hostName, azureSasCredential, JobClientOptions.builder().build());
    }

    /**
     * Create a new JobClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     * @param options The connection options to use when connecting to the service.
     */
    public JobClient(String hostName, AzureSasCredential azureSasCredential, JobClientOptions options)
    {
        Objects.requireNonNull(azureSasCredential);
        Objects.requireNonNull(options);

        if (Tools.isNullOrEmpty(hostName))
        {
            throw new IllegalArgumentException("hostName cannot be null or empty");
        }

        this.hostName = hostName;
        this.azureSasCredential = azureSasCredential;
        this.options = options;
    }

    /**
     * Creates a new Job to update twin tags and desired properties on one or multiple devices
     *
     * @param jobId Unique Job Id for this job
     * @param queryCondition Query condition to evaluate which devices to run the job on. It can be {@code null} or empty
     * @param updateTwin Twin object to use for the update
     * @param startTimeUtc Date time in Utc to start the job
     * @param maxExecutionTimeInSeconds Max execution time in seconds, i.e., ttl duration the job can run
     * @return a jobResult object
     * @throws IllegalArgumentException if one of the provided parameters is invalid
     * @throws IOException if the function cannot create a URL for the job
     * @throws IotHubException if the http request failed
     */
    @SuppressWarnings("deprecation")
    public synchronized JobResult scheduleUpdateTwin(
        String jobId,
        String queryCondition,
        DeviceTwinDevice updateTwin,
        Date startTimeUtc,
        long maxExecutionTimeInSeconds)
        throws IllegalArgumentException, IOException, IotHubException
    {
        URL url;

        if (Tools.isNullOrEmpty(jobId))
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

        JobsParser jobsParser =
            new JobsParser(
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

        ProxyOptions proxyOptions = options.getProxyOptions();
        Proxy proxy = proxyOptions != null ? proxyOptions.getProxy() : null;
        HttpResponse response = DeviceOperations.request(
            this.getAuthenticationToken(),
            url,
            HttpMethod.PUT,
            json.getBytes(StandardCharsets.UTF_8),
            null,
            options.getHttpConnectTimeout(),
            options.getHttpReadTimeout(),
            proxy);

        return new JobResult(response.getBody());
    }

    /**
     * Creates a new Job to invoke method on one or multiple devices
     *
     * @param jobId Unique Job Id for this job
     * @param queryCondition Query condition to evaluate which devices to run the job on. It can be {@code null} or empty
     * @param methodName Method name to be invoked
     * @param responseTimeoutInSeconds Maximum interval of time, in seconds, that the Direct Method will wait for answer. It can be {@code null}.
     * @param connectTimeoutInSeconds Maximum interval of time, in seconds, that the Direct Method will wait for the connection. It can be {@code null}.
     * @param payload Object that contains the payload defined by the user. It can be {@code null}.
     * @param startTimeUtc Date time in Utc to start the job
     * @param maxExecutionTimeInSeconds Max execution time in seconds, i.e., ttl duration the job can run
     * @return a jobResult object
     * @throws IllegalArgumentException if one of the provided parameters is invalid
     * @throws IOException if the function cannot create a URL for the job, or the IO failed on request
     * @throws IotHubException if the http request failed
     */
    @SuppressWarnings("deprecation")
    public synchronized JobResult scheduleDeviceMethod(
        String jobId,
        String queryCondition,
        String methodName,
        Long responseTimeoutInSeconds,
        Long connectTimeoutInSeconds,
        Object payload,
        Date startTimeUtc,
        long maxExecutionTimeInSeconds)
        throws IllegalArgumentException, IOException, IotHubException
    {
        URL url;

        if (Tools.isNullOrEmpty(jobId))
        {
            throw new IllegalArgumentException("jobId cannot be null or empty");
        }

        if (Tools.isNullOrEmpty(methodName))
        {
            throw new IllegalArgumentException("method name cannot be null or empty");
        }

        if (startTimeUtc == null)
        {
            throw new IllegalArgumentException("startTimeUtc cannot be null");
        }

        if (maxExecutionTimeInSeconds < 0)
        {
            throw new IllegalArgumentException("maxExecutionTimeInSeconds cannot be less than 0");
        }

        MethodParser cloudToDeviceMethod =
            new MethodParser(
                methodName,
                responseTimeoutInSeconds,
                connectTimeoutInSeconds,
                payload);

        JobsParser jobsParser =
            new JobsParser(
                jobId,
                cloudToDeviceMethod,
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

        ProxyOptions proxyOptions = options.getProxyOptions();
        Proxy proxy = proxyOptions != null ? proxyOptions.getProxy() : null;
        HttpResponse response = DeviceOperations.request(
            this.getAuthenticationToken(),
            url,
            HttpMethod.PUT,
            json.getBytes(StandardCharsets.UTF_8),
            null,
            options.getHttpConnectTimeout(),
            options.getHttpReadTimeout(),
            proxy);

        return new JobResult(response.getBody());
    }

    /**
     * Get the current job on the iotHub.
     *
     * @param jobId Unique Job Id for this job
     * @return a jobResult object
     * @throws IllegalArgumentException if the jobId is invalid
     * @throws IOException if the function cannot create a URL for the job, or the IO failed on request
     * @throws IotHubException if the http request failed
     */
    @SuppressWarnings("deprecation")
    public synchronized JobResult getJob(String jobId)
        throws IllegalArgumentException, IOException, IotHubException
    {
        URL url;

        if (Tools.isNullOrEmpty(jobId))
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

        ProxyOptions proxyOptions = options.getProxyOptions();
        Proxy proxy = proxyOptions != null ? proxyOptions.getProxy() : null;
        HttpResponse response = DeviceOperations.request(
            this.getAuthenticationToken(),
            url,
            HttpMethod.GET,
            new byte[]{},
            null,
            options.getHttpConnectTimeout(),
            options.getHttpReadTimeout(),
            proxy);

        return new JobResult(response.getBody());
    }

    /**
     * Cancel a current jod on the IoTHub
     *
     * @param jobId Unique Job Id for this job
     * @return a jobResult object
     * @throws IllegalArgumentException if the jobId is invalid
     * @throws IOException if the function cannot create a URL for the job, or the IO failed on request
     * @throws IotHubException if the http request failed
     */
    @SuppressWarnings("deprecation")
    public synchronized JobResult cancelJob(String jobId)
        throws IllegalArgumentException, IOException, IotHubException
    {
        URL url;
        if (Tools.isNullOrEmpty(jobId))
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

        ProxyOptions proxyOptions = options.getProxyOptions();
        Proxy proxy = proxyOptions != null ? proxyOptions.getProxy() : null;
        HttpResponse response = DeviceOperations.request(
            this.getAuthenticationToken(),
            url,
            HttpMethod.POST,
            EMPTY_JSON,
            null,
            options.getHttpConnectTimeout(),
            options.getHttpReadTimeout(),
            proxy);

        return new JobResult(response.getBody());
    }

    private TwinState getParserFromDevice(DeviceTwinDevice device)
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
                map.putFinal(p.getKey(), p.getValue());
            }
        }
        return map;
    }

    /**
     * Query for device Job
     *
     * @param sqlQuery sql style query over device.jobs
     * @param pageSize the value per which to limit the size of query response by.
     * @return Query object for this query
     * @throws IotHubException When IotHub fails to respond
     * @throws IOException When any of the parameters are incorrect
     */
    @SuppressWarnings("deprecation")
    public synchronized Query queryDeviceJob(String sqlQuery, Integer pageSize) throws IotHubException, IOException
    {
        if (sqlQuery == null || sqlQuery.length() == 0)
        {
            throw new IllegalArgumentException("Query cannot be null or empty");
        }

        if (pageSize <= 0)
        {
            throw new IllegalArgumentException("pagesize cannot be negative or zero");
        }

        Query deviceJobQuery = new Query(sqlQuery, pageSize, QueryType.DEVICE_JOB);

        ProxyOptions proxyOptions = options.getProxyOptions();
        Proxy proxy = proxyOptions != null ? proxyOptions.getProxy() : null;

        deviceJobQuery.sendQueryRequest(
            this.credentialCache,
            this.azureSasCredential,
            this.iotHubConnectionString,
            IotHubConnectionString.getUrlTwinQuery(this.hostName),
            HttpMethod.POST,
            options.getHttpConnectTimeout(),
            options.getHttpReadTimeout(),
            proxy);

        return deviceJobQuery;
    }

    /**
     * Query for device Job limited by default page size of 100 for response
     *
     * @param sqlQuery sql style query over device.jobs
     * @return Query object for this query
     * @throws IotHubException When IotHub fails to respond
     * @throws IOException When any of the parameters are incorrect
     */
    public synchronized Query queryDeviceJob(String sqlQuery) throws IotHubException, IOException
    {
        return queryDeviceJob(sqlQuery, DEFAULT_PAGE_SIZE);
    }

    /**
     * returns the availability of next job result in response. Query's further if page size has been met
     *
     * @param query Query for which to look for next job response by
     * @return true if next job result is available , false otherwise
     * @throws IotHubException When IotHub fails to respond
     * @throws IOException if any of the input parameters are incorrect
     */
    public synchronized boolean hasNextJob(Query query) throws IotHubException, IOException
    {
        if (query == null)
        {
            throw new IllegalArgumentException("Query cannot be null");
        }

        return query.hasNext();
    }

    /**
     * returns the next job result in response. Query's further if page size has been met and has next is not called
     *
     * @param query Query for which to look for next job response by
     * @return next job result if available
     * @throws IotHubException When IotHub fails to respond
     * @throws IOException if any of the input parameters are incorrect
     * @throws NoSuchElementException if called when no further responses are left
     */
    public synchronized JobResult getNextJob(Query query) throws IOException, IotHubException, NoSuchElementException
    {
        if (query == null)
        {
            throw new IllegalArgumentException("Query cannot be null");
        }
        Object nextObject = query.next();

        if (nextObject instanceof String)
        {
            String deviceJobJson = (String) nextObject;
            return new JobResult(deviceJobJson.getBytes());
        }
        else
        {
            throw new IOException("Received a response that could not be parsed");
        }
    }

    /**
     * Query the iot hub for a jobs response. Query response are limited by page size per attempt
     *
     * @param jobType The type of job to query for
     * @param jobStatus The status of the job to query for
     * @param pageSize The value to which to limit the job response size by
     * @return A query object on which to look for responses by
     * @throws IOException If any of the input parameters are incorrect
     * @throws IotHubException If IotHub failed to respond
     */
    @SuppressWarnings("deprecation")
    public synchronized Query queryJobResponse(JobType jobType, JobStatus jobStatus, Integer pageSize)
        throws IOException, IotHubException
    {
        if (pageSize <= 0)
        {
            throw new IllegalArgumentException("pagesize cannot be negative or zero");
        }

        Query jobResponseQuery = new Query(pageSize, QueryType.JOB_RESPONSE);

        String jobTypeString = (jobType == null) ? null : jobType.toString();
        String jobStatusString = (jobStatus == null) ? null : jobStatus.toString();
        ProxyOptions proxyOptions = options.getProxyOptions();
        Proxy proxy = proxyOptions != null ? proxyOptions.getProxy() : null;

        jobResponseQuery.sendQueryRequest(
            this.credentialCache,
            this.azureSasCredential,
            this.iotHubConnectionString,
            IotHubConnectionString.getUrlQuery(this.hostName, jobTypeString, jobStatusString),
            HttpMethod.GET,
            options.getHttpConnectTimeout(),
            options.getHttpReadTimeout(),
            proxy);

        return jobResponseQuery;
    }

    /**
     * Query the iot hub for a jobs response. Query response are limited by default page size per attempt
     *
     * @param jobType The type of job to query for
     * @param jobStatus The status of the job to query for
     * @return A query object on which to look for responses by
     * @throws IOException If any of the input parameters are incorrect
     * @throws IotHubException If IotHub failed to respond
     */
    public synchronized Query queryJobResponse(JobType jobType, JobStatus jobStatus)
        throws IotHubException, IOException
    {
        return queryJobResponse(jobType, jobStatus, DEFAULT_PAGE_SIZE);
    }

    @SuppressWarnings("unused")
    protected JobClient()
    {
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
}
