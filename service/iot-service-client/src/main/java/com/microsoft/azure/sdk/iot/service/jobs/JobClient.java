// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.jobs;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.google.gson.JsonSyntaxException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubExceptionManager;
import com.microsoft.azure.sdk.iot.service.serializers.MethodParser;
import com.microsoft.azure.sdk.iot.service.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.service.twin.TwinState;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
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
 * Use the JobClient to schedule and cancel jobs for a group of devices using IoT hub.
 */
@Slf4j
public final class JobClient
{
    private final static byte[] EMPTY_JSON = "{}".getBytes(StandardCharsets.UTF_8);

    private final String hostName;
    private TokenCredentialCache credentialCache;
    private AzureSasCredential azureSasCredential;
    private IotHubConnectionString iotHubConnectionString;
    private final JobClientOptions clientOptions;

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
     * @param clientOptions The connection clientOptions to use when connecting to the service.
     */
    public JobClient(String connectionString, JobClientOptions clientOptions)
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
     * @param clientOptions The connection clientOptions to use when connecting to the service.
     */
    public JobClient(String hostName, TokenCredential credential, JobClientOptions clientOptions)
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
     * @param clientOptions The connection clientOptions to use when connecting to the service.
     */
    public JobClient(String hostName, AzureSasCredential azureSasCredential, JobClientOptions clientOptions)
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
    }

    private static void commonConstructorSetup()
    {
        log.debug("Initialized a JobClient instance using SDK version {}", TransportUtils.serviceVersion);
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
     * @throws IOException if the function cannot create a URL for the job
     * @throws IotHubException if the http request failed
     */
    public Job scheduleUpdateTwin(
        String jobId,
        String queryCondition,
        Twin updateTwin,
        Date startTimeUtc,
        long maxExecutionTimeInSeconds)
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

        HttpRequest httpRequest = createRequest(url, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8));

        HttpResponse response = httpRequest.send();

        return new Job(new String(response.getBody()));
    }

    /**
     * Creates a new Job to invoke method on one or multiple devices
     *
     * @param jobId Unique Job Id for this job
     * @param queryCondition Query condition to evaluate which devices to run the job on. It can be {@code null} or empty
     * @param methodName Method name to be invoked
     * @param startTimeUtc Date time in Utc to start the job
     * @return a jobResult object
     * @throws IOException if the function cannot create a URL for the job, or the IO failed on request
     * @throws IotHubException if the http request failed
     */
    public Job scheduleDirectMethod(
        String jobId,
        String queryCondition,
        String methodName,
        Date startTimeUtc)
            throws IOException, IotHubException
    {
        return scheduleDirectMethod(jobId, queryCondition, methodName, startTimeUtc, DirectMethodsJobOptions.builder().build());
    }

    /**
     * Creates a new Job to invoke method on one or multiple devices
     *
     * @param jobId Unique Job Id for this job
     * @param queryCondition Query condition to evaluate which devices to run the job on. It can be {@code null} or empty
     * @param methodName Method name to be invoked
     * @param startTimeUtc Date time in Utc to start the job
     * @param options the optional parameters for this request. May not be null.
     * @return a jobResult object
     * @throws IOException if the function cannot create a URL for the job, or the IO failed on request
     * @throws IotHubException if the http request failed
     */
    public Job scheduleDirectMethod(
        String jobId,
        String queryCondition,
        String methodName,
        Date startTimeUtc,
        DirectMethodsJobOptions options)
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

        JobsParser jobsParser =
            new JobsParser(
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

        return new Job(new String(response.getBody()));
    }

    /**
     * Get the current job on the iotHub.
     *
     * @param jobId Unique Job Id for this job
     * @return the retrieved job
     * @throws IOException if the function cannot create a URL for the job, or the IO failed on request
     * @throws IotHubException if the http request failed
     */
    public Job getJob(String jobId) throws IOException, IotHubException
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

        return new Job(new String(response.getBody()));
    }

    /**
     * Cancel a current jod on the IoTHub
     *
     * @param jobId Unique Job Id for this job
     * @return the cancelled job
     * @throws IOException if the function cannot create a URL for the job, or the IO failed on request
     * @throws IotHubException if the http request failed
     */
    public Job cancelJob(String jobId) throws IOException, IotHubException
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

        return new Job(new String(response.getBody()));
    }

    /**
     * Create a bulk export job.
     *
     * @param exportBlobContainerUri URI containing SAS token to a blob container where export data will be placed
     * @param excludeKeys Whether the devices keys should be excluded from the exported data or not
     *
     * @return A JobProperties object for the newly created bulk export job
     *
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public JobProperties exportDevices(String exportBlobContainerUri, Boolean excludeKeys)
        throws IOException, IotHubException
    {
        if (exportBlobContainerUri == null || excludeKeys == null)
        {
            throw new IllegalArgumentException("Export blob uri cannot be null");
        }

        URL url = IotHubConnectionString.getUrlCreateExportImportJob(this.hostName);

        String jobPropertiesJson = CreateExportJobPropertiesJson(exportBlobContainerUri, excludeKeys);
        HttpRequest request = createRequest(url, HttpMethod.POST, jobPropertiesJson.getBytes(StandardCharsets.UTF_8));

        HttpResponse response = request.send();

        return ProcessJobResponse(response);
    }

    /**
     * Create a bulk export job.
     *
     * @param exportDevicesParameters A JobProperties object containing input parameters for export Devices job
     *                                This API also supports identity based storage authentication, identity authentication
     *                                support is currently available in limited regions. If a user wishes to try it out,
     *                                they will need to set an Environment Variable of "EnabledStorageIdentity" and set it to "1"
     *                                otherwise default key based authentication is used for storage
     *                                <a href="https://docs.microsoft.com/en-us/azure/iot-hub/virtual-network-support"> More details here </a>
     *
     * @return A JobProperties object for the newly created bulk export job
     *
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public JobProperties exportDevices(JobProperties exportDevicesParameters) throws IOException, IotHubException
    {
        URL url = IotHubConnectionString.getUrlCreateExportImportJob(this.hostName);

        exportDevicesParameters.setType(JobProperties.JobType.EXPORT);
        String jobPropertiesJson = exportDevicesParameters.toJobPropertiesParser().toJson();
        HttpRequest request = createRequest(url, HttpMethod.POST, jobPropertiesJson.getBytes(StandardCharsets.UTF_8));

        HttpResponse response = request.send();

        return ProcessJobResponse(response);
    }

    /**
     * Create a bulk import job.
     *
     * @param importBlobContainerUri URI containing SAS token to a blob container that contains registry data to sync
     * @param outputBlobContainerUri URI containing SAS token to a blob container where the result of the bulk import operation will be placed
     *
     * @return A JobProperties object for the newly created bulk import job
     *
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public JobProperties importDevices(String importBlobContainerUri, String outputBlobContainerUri)
        throws IOException, IotHubException
    {
        if (importBlobContainerUri == null || outputBlobContainerUri == null)
        {
            throw new IllegalArgumentException("Import blob uri or output blob uri cannot be null");
        }

        URL url = IotHubConnectionString.getUrlCreateExportImportJob(this.hostName);

        String jobPropertiesJson = CreateImportJobPropertiesJson(importBlobContainerUri, outputBlobContainerUri);
        HttpRequest request = createRequest(url, HttpMethod.POST, jobPropertiesJson.getBytes(StandardCharsets.UTF_8));

        HttpResponse response = request.send();

        return ProcessJobResponse(response);
    }

    /**
     * Create a bulk import job.
     *
     * @param importDevicesParameters A JobProperties object containing input parameters for import Devices job
     *                                This API also supports identity based storage authentication, identity authentication
     *                                support is currently available in limited regions. If a user wishes to try it out,
     *                                they will need to set an Environment Variable of "EnabledStorageIdentity" and set it to "1"
     *                                otherwise default key based authentication is used for storage
     *                                <a href="https://docs.microsoft.com/en-us/azure/iot-hub/virtual-network-support"> More details here </a>
     *
     * @return A JobProperties object for the newly created bulk import job
     *
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public JobProperties importDevices(JobProperties importDevicesParameters)
        throws IOException, IotHubException
    {
        URL url = IotHubConnectionString.getUrlCreateExportImportJob(this.hostName);

        importDevicesParameters.setType(JobProperties.JobType.IMPORT);
        String jobPropertiesJson = importDevicesParameters.toJobPropertiesParser().toJson();
        HttpRequest request = createRequest(url, HttpMethod.POST, jobPropertiesJson.getBytes(StandardCharsets.UTF_8));

        HttpResponse response = request.send();

        return ProcessJobResponse(response);
    }

    /**
     * Get the properties of an existing job.
     *
     * @param importExportJobId The id of the job to be retrieved.
     *
     * @return A JobProperties object for the requested job id
     *
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public JobProperties getImportExportJob(String importExportJobId) throws IOException, IotHubException
    {
        if (importExportJobId == null)
        {
            throw new IllegalArgumentException("importExportJobId cannot be null");
        }

        URL url = IotHubConnectionString.getUrlImportExportJob(this.hostName, importExportJobId);

        HttpRequest request = createRequest(url, HttpMethod.GET, new byte[0]);

        HttpResponse response = request.send();

        return ProcessJobResponse(response);
    }

    private JobProperties ProcessJobResponse(HttpResponse response) throws IotHubException
    {
        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);
        return new JobProperties(new JobPropertiesParser(bodyStr));
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

    private String CreateExportJobPropertiesJson(String exportBlobContainerUri, Boolean excludeKeysInExport)
    {
        JobProperties jobProperties = new JobProperties();
        jobProperties.setType(JobProperties.JobType.EXPORT);
        jobProperties.setOutputBlobContainerUri(exportBlobContainerUri);
        jobProperties.setExcludeKeysInExport(excludeKeysInExport);
        return jobProperties.toJobPropertiesParser().toJson();
    }

    private String CreateImportJobPropertiesJson(String importBlobContainerUri, String outputBlobContainerUri)
    {
        JobProperties jobProperties = new JobProperties();
        jobProperties.setType(JobProperties.JobType.IMPORT);
        jobProperties.setInputBlobContainerUri(importBlobContainerUri);
        jobProperties.setOutputBlobContainerUri(outputBlobContainerUri);
        return jobProperties.toJobPropertiesParser().toJson();
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
