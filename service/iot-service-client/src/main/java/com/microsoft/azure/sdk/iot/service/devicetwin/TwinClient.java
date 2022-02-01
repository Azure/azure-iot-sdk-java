/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.auth.TokenCredentialCache;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import static com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest.REQUEST_ID;

/**
 * Use the TwinClient class to manage the device twins in IoT hubs.
 */
@Slf4j
public class TwinClient
{
    private int requestId = 0;
    private final int DEFAULT_PAGE_SIZE = 100;

    private final TwinClientOptions options;
    private final String hostName;
    private TokenCredentialCache credentialCache;
    private AzureSasCredential azureSasCredential;
    private IotHubConnectionString iotHubConnectionString;

    /**
     * Constructor to create instance from connection string.
     *
     * @param connectionString The iot hub connection string.
     */
    public TwinClient(String connectionString)
    {
        this(connectionString, TwinClientOptions.builder().build());
    }

    /**
     * Constructor to create instance from connection string.
     *
     * @param connectionString The iot hub connection string.
     * @param options the configurable options for each operation on this client. May not be null.
     */
    public TwinClient(String connectionString, TwinClientOptions options)
    {
        if (connectionString == null || connectionString.isEmpty())
        {
            throw new IllegalArgumentException("connectionString cannot be null or empty.");
        }

        this.options = options;
        this.iotHubConnectionString = IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
        this.hostName = this.iotHubConnectionString.getHostName();
        commonConstructorSetup();
    }

    /**
     * Create a new TwinClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed. The provided tokens must be Json Web Tokens.
     */
    public TwinClient(String hostName, TokenCredential credential)
    {
        this(hostName, credential, TwinClientOptions.builder().build());
    }

    /**
     * Create a new TwinClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed. The provided tokens must be Json Web Tokens.
     * @param options The connection options to use when connecting to the service.
     */
    public TwinClient(String hostName, TokenCredential credential, TwinClientOptions options)
    {
        Objects.requireNonNull(credential, "TokenCredential cannot be null");
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
     * Create a new TwinClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     */
    public TwinClient(String hostName, AzureSasCredential azureSasCredential)
    {
        this(hostName, azureSasCredential, TwinClientOptions.builder().build());
    }

    /**
     * Create a new TwinClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     * @param options The connection options to use when connecting to the service.
     */
    public TwinClient(String hostName, AzureSasCredential azureSasCredential, TwinClientOptions options)
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
        log.debug("Initialized a TwinClient instance using SDK version {}", TransportUtils.serviceVersion);
    }

    /**
     * This method retrieves device twin for the specified device.
     *
     * @param twin The device with a valid id for which device twin is to be retrieved.
     * @throws IOException This exception is thrown if the IO operation failed.
     * @throws IotHubException This exception is thrown if the response verification failed.
     */
    public void getTwin(Twin twin) throws IotHubException, IOException
    {
        if (twin == null || twin.getDeviceId() == null || twin.getDeviceId().length() == 0)
        {
            throw new IllegalArgumentException("Instantiate a device and set device id to be used");
        }

        URL url;
        if ((twin.getModuleId() == null) || twin.getModuleId().length() ==0)
        {
            url = IotHubConnectionString.getUrlTwin(this.hostName, twin.getDeviceId());
        }
        else
        {
            url = IotHubConnectionString.getUrlModuleTwin(this.hostName, twin.getDeviceId(), twin.getModuleId());
        }

        getTwinOperation(url, twin);
    }

    private void getTwinOperation(URL url, Twin twin) throws IotHubException, IOException
    {
        ProxyOptions proxyOptions = options.getProxyOptions();
        Proxy proxy = proxyOptions != null ? proxyOptions.getProxy() : null;
        HttpRequest httpRequest = new HttpRequest(
            url,
            HttpMethod.GET,
            new byte[0],
            this.getAuthenticationToken(),
            proxy);

        httpRequest.setReadTimeoutMillis(options.getHttpReadTimeout());
        httpRequest.setConnectTimeoutMillis(options.getHttpConnectTimeout());
        httpRequest.setHeaderField(REQUEST_ID, String.valueOf(requestId++));

        HttpResponse response = httpRequest.send();

        String twinString = new String(response.getBody(), StandardCharsets.UTF_8);

        TwinState twinState = new TwinState(twinString);

        twin.setVersion(twinState.getVersion());
        twin.setModelId(twinState.getModelId());
        twin.setETag(twinState.getETag());
        twin.setTags(twinState.getTags());
        twin.setDesiredProperties(twinState.getDesiredProperty());
        twin.setReportedProperties(twinState.getReportedProperty());
        twin.setCapabilities(twinState.getCapabilities());
        twin.setConfigurations(twinState.getConfigurations());
        twin.setConnectionState(twinState.getConnectionState());
    }

    /**
     * This method updates device twin for the specified device.
     * <p>This API uses the IoT Hub PATCH API when sending updates, but it sends the full twin with each patch update.
     * As a result, devices subscribed to twin will receive notifications that each property is changed when this API is
     * called, even if only some of the properties were changed.</p>
     * <p>See <a href="https://docs.microsoft.com/en-us/rest/api/iothub/service/devices/updatetwin">PATCH</a> for
     * more details.</p>
     *
     * @param twin The device with a valid Id for which device twin is to be updated.
     * @throws IOException This exception is thrown if the IO operation failed.
     * @throws IotHubException This exception is thrown if the response verification failed.
     */
    public void updateTwin(Twin twin) throws IotHubException, IOException
    {
        if (twin == null || twin.getDeviceId() == null || twin.getDeviceId().length() == 0)
        {
            throw new IllegalArgumentException("Instantiate a twin and set device Id to be used.");
        }

        if ((twin.getDesiredMap() == null || twin.getDesiredMap().isEmpty()) &&
                (twin.getTagsMap() == null || twin.getTagsMap().isEmpty()))
        {
            throw new IllegalArgumentException("Set either desired properties or tags for the device to be updated.");
        }

        URL url;
        if ((twin.getModuleId() == null) || twin.getModuleId().length() == 0)
        {
            url = IotHubConnectionString.getUrlTwin(this.hostName, twin.getDeviceId());
        }
        else
        {
            url = IotHubConnectionString.getUrlModuleTwin(this.hostName, twin.getDeviceId(), twin.getModuleId());
        }

        TwinState twinState = new TwinState(twin.getTagsMap(), twin.getDesiredMap(), null);
        String twinJson = twinState.toJsonElement().toString();

        ProxyOptions proxyOptions = options.getProxyOptions();
        Proxy proxy = proxyOptions != null ? proxyOptions.getProxy() : null;

        HttpRequest httpRequest = new HttpRequest(
            url,
            HttpMethod.PATCH,
            twinJson.getBytes(StandardCharsets.UTF_8),
            this.getAuthenticationToken(),
            proxy);

        httpRequest.setReadTimeoutMillis(options.getHttpReadTimeout());
        httpRequest.setConnectTimeoutMillis(options.getHttpConnectTimeout());
        httpRequest.setHeaderField(REQUEST_ID, String.valueOf(requestId++));

        // no need to return http response since method returns void
        httpRequest.send();
    }

    /**
     * Replace the full twin for a given device or module with the provided twin.
     *
     * @param twin The twin object to replace the current twin object.
     * @throws IotHubException If any an IoT hub level exception is thrown. For instance,
     * if the sendHttpRequest is unauthorized, a exception that extends IotHubException will be thrown.
     * @throws IOException If the sendHttpRequest failed to send to IoT hub.
     * @return The Twin object's current state returned from the service after the replace operation.
     */
    public Twin replaceTwin(Twin twin) throws IotHubException, IOException
    {
        if (twin == null || twin.getDeviceId() == null || twin.getDeviceId().length() == 0)
        {
            throw new IllegalArgumentException("Instantiate a device and set device Id to be used.");
        }

        URL url;
        if ((twin.getModuleId() == null) || twin.getModuleId().length() ==0)
        {
            url = this.iotHubConnectionString.getUrlTwin(twin.getDeviceId());
        }
        else
        {
            url = this.iotHubConnectionString.getUrlModuleTwin(twin.getDeviceId(), twin.getModuleId());
        }

        TwinState twinState = new TwinState(twin.getTagsMap(), twin.getDesiredMap(), null);
        String twinJson = twinState.toJsonElement().toString();

        Proxy proxy = options.getProxyOptions() == null
                ? null
                : options.getProxyOptions().getProxy();

        HttpRequest httpRequest = new HttpRequest(
            url,
            HttpMethod.PUT,
            twinJson.getBytes(StandardCharsets.UTF_8),
            this.getAuthenticationToken(),
            proxy);

        httpRequest.setReadTimeoutMillis(options.getHttpReadTimeout());
        httpRequest.setConnectTimeoutMillis(options.getHttpConnectTimeout());
        httpRequest.setHeaderField(REQUEST_ID, String.valueOf(requestId++));

        HttpResponse httpResponse = httpRequest.send();

        String responseTwinJson = new String(httpResponse.getBody(), StandardCharsets.UTF_8);

        twinState = new TwinState(responseTwinJson);

        Twin responseTwin;
        if (twinState.getModuleId() == null)
        {
            responseTwin = new Twin(twinState.getDeviceId());
        }
        else
        {
            responseTwin = new Twin(twinState.getDeviceId(), twinState.getModuleId());
        }

        responseTwin.setVersion(twinState.getVersion());
        responseTwin.setModelId(twinState.getModelId());
        responseTwin.setETag(twinState.getETag());
        responseTwin.setTags(twinState.getTags());
        responseTwin.setDesiredProperties(twinState.getDesiredProperty());
        responseTwin.setReportedProperties(twinState.getReportedProperty());
        responseTwin.setCapabilities(twinState.getCapabilities());
        responseTwin.setConfigurations(twinState.getConfigurations());
        responseTwin.setConnectionState(twinState.getConnectionState());
        return responseTwin;
    }

    /**
     * Creates a new Job to update twin tags and desired properties on one or multiple devices.
     *
     * @param queryCondition Query condition to evaluate which devices to run the job on. It can be {@code null} or
     *                       empty.
     * @param updateTwin Twin object to use for the update.
     * @param startTimeUtc Date and time in UTC to start the job.
     * @param maxExecutionTimeInSeconds Max run time in seconds, I.E., the duration the job can run.
     * @return A {@link Job} class that represent this job on IoT hub.
     * @throws IOException If the function contains invalid parameters.
     * @throws IotHubException If the HTTP sendHttpRequest failed.
     */
    public Job scheduleUpdateTwin(
            String queryCondition,
            Twin updateTwin,
            Date startTimeUtc,
            long maxExecutionTimeInSeconds) throws IOException, IotHubException
    {
        if (updateTwin == null)
        {
            throw new IllegalArgumentException("null updateTwin");
        }

        if (startTimeUtc == null)
        {
            throw new IllegalArgumentException("null startTimeUtc");
        }

        if (maxExecutionTimeInSeconds < 0)
        {
            throw new IllegalArgumentException("negative maxExecutionTimeInSeconds");
        }

        Job job;
        if (this.credentialCache != null)
        {
            job = new Job(this.hostName, this.credentialCache.getTokenCredential());
        }
        else if (this.azureSasCredential != null)
        {
            job = new Job(this.hostName, this.azureSasCredential);
        }
        else
        {
            job = new Job(this.iotHubConnectionString.toString());
        }

        job.scheduleUpdateTwin(queryCondition, updateTwin, startTimeUtc, maxExecutionTimeInSeconds);

        return job;
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
