// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.jobs.registry;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.auth.TokenCredentialCache;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.jobs.registry.serializers.JobPropertiesParser;
import com.microsoft.azure.sdk.iot.service.transport.TransportUtils;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Use the ScheduledJobsClient to schedule and cancel jobs for a group of devices using IoT hub.
 */
@Slf4j
public final class RegistryJobsClient
{
    private final String hostName;
    private TokenCredentialCache credentialCache;
    private AzureSasCredential azureSasCredential;
    private IotHubConnectionString iotHubConnectionString;
    private final RegistryJobsClientOptions clientOptions;

    /**
     * Constructor to create instance from connection string
     *
     * @param connectionString The iot hub connection string
     */
    public RegistryJobsClient(String connectionString)
    {
        this(connectionString, RegistryJobsClientOptions.builder().build());
    }

    /**
     * Constructor to create instance from connection string
     *
     * @param connectionString The iot hub connection string
     * @param clientOptions The connection clientOptions to use when connecting to the service.
     */
    public RegistryJobsClient(String connectionString, RegistryJobsClientOptions clientOptions)
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
     * Create a new ScheduledJobsClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     * this library when they are needed. The provided tokens must be Json Web Tokens.
     */
    public RegistryJobsClient(String hostName, TokenCredential credential)
    {
        this(hostName, credential, RegistryJobsClientOptions.builder().build());
    }

    /**
     * Create a new ScheduledJobsClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     * this library when they are needed. The provided tokens must be Json Web Tokens.
     * @param clientOptions The connection clientOptions to use when connecting to the service.
     */
    public RegistryJobsClient(String hostName, TokenCredential credential, RegistryJobsClientOptions clientOptions)
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
     * Create a new ScheduledJobsClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     */
    public RegistryJobsClient(String hostName, AzureSasCredential azureSasCredential)
    {
        this(hostName, azureSasCredential, RegistryJobsClientOptions.builder().build());
    }

    /**
     * Create a new ScheduledJobsClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     * @param clientOptions The connection clientOptions to use when connecting to the service.
     */
    public RegistryJobsClient(String hostName, AzureSasCredential azureSasCredential, RegistryJobsClientOptions clientOptions)
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
        log.debug("Initialized a ScheduledJobsClient instance using SDK version {}", TransportUtils.serviceVersion);
    }

    /**
     * Create a bulk export job.
     *
     * @param exportBlobContainerUri URI containing SAS token to a blob container where export data will be placed
     * @param excludeKeys Whether the devices keys should be excluded from the exported data or not
     *
     * @return A RegistryJob object for the newly created bulk export job
     *
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public RegistryJob exportDevices(String exportBlobContainerUri, boolean excludeKeys) throws IOException, IotHubException
    {
        if (exportBlobContainerUri == null)
        {
            throw new IllegalArgumentException("Export blob uri cannot be null");
        }

        URL url = IotHubConnectionString.getUrlCreateExportImportJob(this.hostName);

        String jobPropertiesJson = createExportJobPropertiesJson(exportBlobContainerUri, excludeKeys);
        HttpRequest request = createRequest(url, HttpMethod.POST, jobPropertiesJson.getBytes(StandardCharsets.UTF_8));

        HttpResponse response = request.send();

        return processJobResponse(response);
    }

    /**
     * Create a bulk export job.
     *
     * @param exportDevicesParameters A RegistryJob object containing input parameters for export Devices job
     *                                This API also supports identity based storage authentication, identity authentication
     *                                support is currently available in limited regions. If a user wishes to try it out,
     *                                they will need to set an Environment Variable of "EnabledStorageIdentity" and set it to "1"
     *                                otherwise default key based authentication is used for storage
     *                                <a href="https://docs.microsoft.com/en-us/azure/iot-hub/virtual-network-support"> More details here </a>
     *
     * @return A RegistryJob object for the newly created bulk export job
     *
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public RegistryJob exportDevices(RegistryJob exportDevicesParameters) throws IOException, IotHubException
    {
        URL url = IotHubConnectionString.getUrlCreateExportImportJob(this.hostName);

        exportDevicesParameters.setType(RegistryJob.JobType.EXPORT);
        String jobPropertiesJson = exportDevicesParameters.toJobPropertiesParser().toJson();
        HttpRequest request = createRequest(url, HttpMethod.POST, jobPropertiesJson.getBytes(StandardCharsets.UTF_8));

        HttpResponse response = request.send();

        return processJobResponse(response);
    }

    /**
     * Create a bulk import job.
     *
     * @param importBlobContainerUri URI containing SAS token to a blob container that contains registry data to sync
     * @param outputBlobContainerUri URI containing SAS token to a blob container where the result of the bulk import operation will be placed
     *
     * @return A RegistryJob object for the newly created bulk import job
     *
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public RegistryJob importDevices(String importBlobContainerUri, String outputBlobContainerUri) throws IOException, IotHubException
    {
        if (importBlobContainerUri == null || outputBlobContainerUri == null)
        {
            throw new IllegalArgumentException("Import blob uri or output blob uri cannot be null");
        }

        URL url = IotHubConnectionString.getUrlCreateExportImportJob(this.hostName);

        String jobPropertiesJson = createImportJobPropertiesJson(importBlobContainerUri, outputBlobContainerUri);
        HttpRequest request = createRequest(url, HttpMethod.POST, jobPropertiesJson.getBytes(StandardCharsets.UTF_8));

        HttpResponse response = request.send();

        return processJobResponse(response);
    }

    /**
     * Create a bulk import job.
     *
     * @param importDevicesParameters A RegistryJob object containing input parameters for import Devices job
     *                                This API also supports identity based storage authentication, identity authentication
     *                                support is currently available in limited regions. If a user wishes to try it out,
     *                                they will need to set an Environment Variable of "EnabledStorageIdentity" and set it to "1"
     *                                otherwise default key based authentication is used for storage
     *                                <a href="https://docs.microsoft.com/en-us/azure/iot-hub/virtual-network-support"> More details here </a>
     *
     * @return A RegistryJob object for the newly created bulk import job
     *
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public RegistryJob importDevices(RegistryJob importDevicesParameters) throws IOException, IotHubException
    {
        URL url = IotHubConnectionString.getUrlCreateExportImportJob(this.hostName);

        importDevicesParameters.setType(RegistryJob.JobType.IMPORT);
        String jobPropertiesJson = importDevicesParameters.toJobPropertiesParser().toJson();
        HttpRequest request = createRequest(url, HttpMethod.POST, jobPropertiesJson.getBytes(StandardCharsets.UTF_8));

        HttpResponse response = request.send();

        return processJobResponse(response);
    }

    /**
     * Get the properties of an existing job.
     *
     * @param jobId The id of the import/export job to be retrieved.
     *
     * @return A RegistryJob object for the requested job id
     *
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public RegistryJob get(String jobId) throws IOException, IotHubException
    {
        if (jobId == null)
        {
            throw new IllegalArgumentException("importExportJobId cannot be null");
        }

        URL url = IotHubConnectionString.getUrlImportExportJob(this.hostName, jobId);

        HttpRequest request = createRequest(url, HttpMethod.GET, new byte[0]);

        HttpResponse response = request.send();

        return processJobResponse(response);
    }

    private RegistryJob processJobResponse(HttpResponse response) throws IotHubException
    {
        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);
        return new RegistryJob(new JobPropertiesParser(bodyStr));
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

    private String createExportJobPropertiesJson(String exportBlobContainerUri, boolean excludeKeysInExport)
    {
        RegistryJob jobProperties = new RegistryJob();
        jobProperties.setType(RegistryJob.JobType.EXPORT);
        jobProperties.setOutputBlobContainerUri(exportBlobContainerUri);
        jobProperties.setExcludeKeysInExport(excludeKeysInExport);
        return jobProperties.toJobPropertiesParser().toJson();
    }

    private String createImportJobPropertiesJson(String importBlobContainerUri, String outputBlobContainerUri)
    {
        RegistryJob jobProperties = new RegistryJob();
        jobProperties.setType(RegistryJob.JobType.IMPORT);
        jobProperties.setInputBlobContainerUri(importBlobContainerUri);
        jobProperties.setOutputBlobContainerUri(outputBlobContainerUri);
        return jobProperties.toJobPropertiesParser().toJson();
    }

    private HttpRequest createRequest(URL url, HttpMethod method, byte[] payload) throws IOException
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
