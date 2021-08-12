/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.deps.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.deps.twin.TwinState;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.Tools;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.auth.TokenCredentialCache;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;

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

/**
 * Use the DeviceTwin class to manage the device twins in IoT hubs.
 */
public class DeviceTwin
{
    private int requestId = 0;
    private final int DEFAULT_PAGE_SIZE = 100;

    private final DeviceTwinClientOptions options;
    private final String hostName;
    private TokenCredentialCache credentialCache;
    private AzureSasCredential azureSasCredential;
    private IotHubConnectionString iotHubConnectionString;

    /**
     * Constructor to create instance from connection string.
     *
     * @param connectionString The iot hub connection string.
     */
    public DeviceTwin(String connectionString)
    {
        this(connectionString, DeviceTwinClientOptions.builder().build());
    }

    /**
     * Constructor to create instance from connection string.
     *
     * @param connectionString The iot hub connection string.
     * @param options the configurable options for each operation on this client. May not be null.
     */
    public DeviceTwin(String connectionString, DeviceTwinClientOptions options)
    {
        if (Tools.isNullOrEmpty(connectionString))
        {
            throw new IllegalArgumentException("connectionString cannot be null or empty.");
        }

        this.options = options;
        this.iotHubConnectionString = IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
        this.hostName = this.iotHubConnectionString.getHostName();
    }

    /**
     * Create a new DeviceTwin instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed. The provided tokens must be Json Web Tokens.
     */
    public DeviceTwin(String hostName, TokenCredential credential)
    {
        this(hostName, credential, DeviceTwinClientOptions.builder().build());
    }

    /**
     * Create a new DeviceTwin instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed. The provided tokens must be Json Web Tokens.
     * @param options The connection options to use when connecting to the service.
     */
    public DeviceTwin(String hostName, TokenCredential credential, DeviceTwinClientOptions options)
    {
        Objects.requireNonNull(credential, "TokenCredential cannot be null");
        Objects.requireNonNull(options, "options cannot be null");
        if (Tools.isNullOrEmpty(hostName))
        {
            throw new IllegalArgumentException("hostName cannot be null or empty");
        }

        this.options = options;
        this.credentialCache = new TokenCredentialCache(credential);
        this.hostName = hostName;
    }

    /**
     * Create a new DeviceTwin instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     */
    public DeviceTwin(String hostName, AzureSasCredential azureSasCredential)
    {
        this(hostName, azureSasCredential, DeviceTwinClientOptions.builder().build());
    }

    /**
     * Create a new DeviceTwin instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     * @param options The connection options to use when connecting to the service.
     */
    public DeviceTwin(String hostName, AzureSasCredential azureSasCredential, DeviceTwinClientOptions options)
    {
        Objects.requireNonNull(azureSasCredential, "azureSasCredential cannot be null");
        Objects.requireNonNull(options, "options cannot be null");
        if (Tools.isNullOrEmpty(hostName))
        {
            throw new IllegalArgumentException("hostName cannot be null or empty");
        }

        this.options = options;
        this.azureSasCredential = azureSasCredential;
        this.hostName = hostName;
    }

    /**
     * This method retrieves device twin for the specified device.
     *
     * @param device The device with a valid id for which device twin is to be retrieved.
     * @throws IOException This exception is thrown if the IO operation failed.
     * @throws IotHubException This exception is thrown if the response verification failed.
     */
    public void getTwin(DeviceTwinDevice device) throws IotHubException, IOException
    {
        if (device == null || device.getDeviceId() == null || device.getDeviceId().length() == 0)
        {
            throw new IllegalArgumentException("Instantiate a device and set device id to be used");
        }

        URL url;
        if ((device.getModuleId() == null) || device.getModuleId().length() ==0)
        {
            url = IotHubConnectionString.getUrlTwin(this.hostName, device.getDeviceId());
        }
        else
        {
            url = IotHubConnectionString.getUrlModuleTwin(this.hostName, device.getDeviceId(), device.getModuleId());
        }

        getTwinOperation(url, device);
    }

    private void getTwinOperation(URL url, DeviceTwinDevice device) throws IotHubException, IOException
    {
        ProxyOptions proxyOptions = options.getProxyOptions();
        Proxy proxy = proxyOptions != null ? proxyOptions.getProxy() : null;
        HttpResponse response = DeviceOperations.request(
                this.getAuthenticationToken(),
                url,
                HttpMethod.GET,
                new byte[0],
                String.valueOf(requestId++),
                options.getHttpConnectTimeout(),
                options.getHttpReadTimeout(),
                proxy);

        String twin = new String(response.getBody(), StandardCharsets.UTF_8);

        TwinState twinState = TwinState.createFromTwinJson(twin);

        device.setVersion(twinState.getVersion());
        device.setModelId(twinState.getModelId());
        device.setETag(twinState.getETag());
        device.setTags(twinState.getTags());
        device.setDesiredProperties(twinState.getDesiredProperty());
        device.setReportedProperties(twinState.getReportedProperty());
        device.setCapabilities(twinState.getCapabilities());
        device.setConfigurations(twinState.getConfigurations());
        device.setConnectionState(twinState.getConnectionState());
    }

    /**
     * This method updates device twin for the specified device.
     * <p>This API uses the IoT Hub PATCH API when sending updates, but it sends the full twin with each patch update.
     * As a result, devices subscribed to twin will receive notifications that each property is changed when this API is
     * called, even if only some of the properties were changed.</p>
     * <p>See <a href="https://docs.microsoft.com/en-us/rest/api/iothub/service/devices/updatetwin">PATCH</a> for
     * more details.</p>
     *
     * @param device The device with a valid Id for which device twin is to be updated.
     * @throws IOException This exception is thrown if the IO operation failed.
     * @throws IotHubException This exception is thrown if the response verification failed.
     */
    public void updateTwin(DeviceTwinDevice device) throws IotHubException, IOException
    {
        if (device == null || device.getDeviceId() == null || device.getDeviceId().length() == 0)
        {
            throw new IllegalArgumentException("Instantiate a device and set device Id to be used.");
        }

        if ((device.getDesiredMap() == null || device.getDesiredMap().isEmpty()) &&
                (device.getTagsMap() == null || device.getTagsMap().isEmpty()))
        {
            throw new IllegalArgumentException("Set either desired properties or tags for the device to be updated.");
        }

        URL url;
        if ((device.getModuleId() == null) || device.getModuleId().length() == 0)
        {
            url = IotHubConnectionString.getUrlTwin(this.hostName, device.getDeviceId());
        }
        else
        {
            url = IotHubConnectionString.getUrlModuleTwin(this.hostName, device.getDeviceId(), device.getModuleId());
        }

        TwinState twinState = new TwinState(device.getTagsMap(), device.getDesiredMap(), null);
        String twinJson = twinState.toJsonElement().toString();

        ProxyOptions proxyOptions = options.getProxyOptions();
        Proxy proxy = proxyOptions != null ? proxyOptions.getProxy() : null;
        DeviceOperations.request(
                this.getAuthenticationToken(),
                url,
                HttpMethod.PATCH,
                twinJson.getBytes(StandardCharsets.UTF_8),
                String.valueOf(requestId++),
                options.getHttpConnectTimeout(),
                options.getHttpReadTimeout(),
                proxy);
    }

    /**
     * Replace the full device twin for a given device with the provided device twin.
     *
     * @param device The device twin object to replace the current device twin object.
     * @throws IotHubException If any an IoT hub level exception is thrown. For instance,
     * if the request is unauthorized, a exception that extends IotHubException will be thrown.
     * @throws IOException If the request failed to send to IoT hub.
     * @return The Twin object's current state returned from the service after the replace operation.
     */
    public DeviceTwinDevice replaceTwin(DeviceTwinDevice device) throws IotHubException, IOException
    {
        if (device == null || device.getDeviceId() == null || device.getDeviceId().length() == 0)
        {
            throw new IllegalArgumentException("Instantiate a device and set device Id to be used.");
        }

        URL url;
        if ((device.getModuleId() == null) || device.getModuleId().length() ==0)
        {
            url = this.iotHubConnectionString.getUrlTwin(device.getDeviceId());
        }
        else
        {
            url = this.iotHubConnectionString.getUrlModuleTwin(device.getDeviceId(), device.getModuleId());
        }

        TwinState twinState = new TwinState(device.getTagsMap(), device.getDesiredMap(), null);
        String twinJson = twinState.toJsonElement().toString();

        Proxy proxy = options.getProxyOptions() == null
                ? null
                : options.getProxyOptions().getProxy();

        HttpResponse httpResponse = DeviceOperations.request(
            this.iotHubConnectionString,
            url,
            HttpMethod.PUT,
            twinJson.getBytes(StandardCharsets.UTF_8),
            String.valueOf(requestId++),
            options.getHttpConnectTimeout(),
            options.getHttpReadTimeout(),
            proxy);

        String responseTwinJson = new String(httpResponse.getBody(), StandardCharsets.UTF_8);

        twinState = TwinState.createFromTwinJson(responseTwinJson);

        DeviceTwinDevice responseTwin;
        if (twinState.getModuleId() == null)
        {
            responseTwin = new DeviceTwinDevice(twinState.getDeviceId());
        }
        else
        {
            responseTwin = new DeviceTwinDevice(twinState.getDeviceId(), twinState.getModuleId());
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
     * SQL-style query for twin.
     *
     * @param sqlQuery SQL-style query string to query Iot hub for Twin.
     * @param pageSize Size to limit query response by.
     * @return {@link Query} Object to be used for looking up responses for this query.
     * @throws IotHubException If query request was not successful at the Iot hub.
     * @throws IOException If input parameters are invalid.
     */
    public Query queryTwin(String sqlQuery, Integer pageSize) throws IotHubException, IOException
    {
        if (sqlQuery == null || sqlQuery.length() == 0)
        {
            throw new IllegalArgumentException("Query cannot be null or empty.");
        }

        if (pageSize <= 0)
        {
            throw new IllegalArgumentException("pageSize cannot be negative or zero.");
        }

        Query deviceTwinQuery = new Query(sqlQuery, pageSize, QueryType.TWIN);

        ProxyOptions proxyOptions = options.getProxyOptions();
        Proxy proxy = proxyOptions != null ? proxyOptions.getProxy() : null;

        deviceTwinQuery.sendQueryRequest(
                this.credentialCache,
                this.azureSasCredential,
                this.iotHubConnectionString,
                IotHubConnectionString.getUrlTwinQuery(this.hostName),
                HttpMethod.POST,
                options.getHttpConnectTimeout(),
                options.getHttpReadTimeout(),
                proxy);

        return deviceTwinQuery;
    }

    /**
     * SQL-style query for twin.
     *
     * @param sqlQuery SQL-style query string to query Iot hub for twin.
     * @return {@link Query} Object to be used for looking up responses for this query.
     * @throws IotHubException If query request was not successful at the Iot hub.
     * @throws IOException If input parameters are invalid.
     */
    public Query queryTwin(String sqlQuery) throws IotHubException, IOException
    {
        return this.queryTwin(sqlQuery, DEFAULT_PAGE_SIZE);
    }

    /**
     * Create a {@link QueryCollection} object that can be used to query whole pages of results at a time.
     * QueryCollection objects also allow you to provide a continuation token for the query to pick up from.
     *
     * @param sqlQuery The SQL-style query to run.
     * @return The created {@link QueryCollection} object that can be used to query the service.
     * @throws MalformedURLException If twin query URL is not correct.
     */
    public QueryCollection queryTwinCollection(String sqlQuery) throws MalformedURLException
    {
        return this.queryTwinCollection(sqlQuery, DEFAULT_PAGE_SIZE);
    }

    /**
     * Create a {@link QueryCollection} object that can be used to query whole pages of results at a time.
     * QueryCollection objects also allow you to provide a continuation token for the query to pick up from.
     *
     * @param sqlQuery The SQL-style query to run.
     * @param pageSize the number of results to return at a time.
     * @return The created QueryCollection object that can be used to query the service.
     * @throws MalformedURLException If twin query URL is not correct.
     */
    public QueryCollection queryTwinCollection(String sqlQuery, Integer pageSize) throws MalformedURLException
    {
        ProxyOptions proxyOptions = options.getProxyOptions();
        Proxy proxy = proxyOptions != null ? proxyOptions.getProxy() : null;

        if (this.credentialCache != null)
        {
            return new QueryCollection(
                    sqlQuery,
                    pageSize,
                    QueryType.TWIN,
                    this.credentialCache,
                    IotHubConnectionString.getUrlTwinQuery(this.hostName),
                    HttpMethod.POST,
                    options.getHttpConnectTimeout(),
                    options.getHttpReadTimeout(),
                    proxy);
        }
        else if (this.azureSasCredential != null)
        {
            return new QueryCollection(
                    sqlQuery,
                    pageSize,
                    QueryType.TWIN,
                    this.azureSasCredential,
                    IotHubConnectionString.getUrlTwinQuery(this.hostName),
                    HttpMethod.POST,
                    options.getHttpConnectTimeout(),
                    options.getHttpReadTimeout(),
                    proxy);
        }
        else
        {
            return new QueryCollection(
                    sqlQuery,
                    pageSize,
                    QueryType.TWIN,
                    this.iotHubConnectionString,
                    IotHubConnectionString.getUrlTwinQuery(this.hostName),
                    HttpMethod.POST,
                    options.getHttpConnectTimeout(),
                    options.getHttpReadTimeout(),
                    proxy);
        }
    }

    /**
     * Returns the availability of next twin element upon query. If non was found,
     * Query is sent over again and response is updated accordingly until no response
     * for the query was found.
     *
     * @param deviceTwinQuery The Query object returned upon creation of query.
     * @return {@code True} if next is available and {@code false} otherwise.
     * @throws IotHubException If Iot hub could not respond back to the query successfully.
     * @throws IOException If input parameter is incorrect.
     */
    public boolean hasNextDeviceTwin(Query deviceTwinQuery) throws IotHubException, IOException
    {
        if (deviceTwinQuery == null)
        {
            throw new IllegalArgumentException("Query cannot be null.");
        }

        return deviceTwinQuery.hasNext();
    }

    /**
     * Returns the next device twin document.
     *
     * @param deviceTwinQuery The object corresponding to the query in request.
     * @return Returns the next device twin document.
     * @throws IOException If the input parameter is incorrect.
     * @throws IotHubException If an unsuccessful response from IoT Hub is received.
     * @throws NoSuchElementException If no additional element was found.
     */
    public DeviceTwinDevice getNextDeviceTwin(Query deviceTwinQuery)
            throws IOException, IotHubException, NoSuchElementException
    {
        if (deviceTwinQuery == null)
        {
            throw new IllegalArgumentException("Query cannot be null.");
        }

        Object nextObject = deviceTwinQuery.next();

        if (nextObject instanceof String)
        {
            String twinJson = (String) nextObject;
            return jsonToDeviceTwinDevice(twinJson);
        }
        else
        {
            throw new IOException("Received a response that could not be parsed.");
        }
    }

    /**
     * Returns {@code True} if the provided {@code deviceTwinQueryCollection} has a next page to query.
     * @param deviceTwinQueryCollection The query to check.
     * @return {@code True} if the provided deviceTwinQueryCollection has a next page to query, {@code false} otherwise.
     * @throws IllegalArgumentException if the provided deviceTwinQueryCollection is null.
     */
    public boolean hasNext(QueryCollection deviceTwinQueryCollection)
    {
        if (deviceTwinQueryCollection == null)
        {
            throw new IllegalArgumentException("deviceTwinQueryCollection cannot be null.");
        }

        return deviceTwinQueryCollection.hasNext();
    }

    /**
     * Returns the next {@link DeviceTwinDevice} collection for the given query alongside the continuation token needed
     * for querying the next page.
     * <p>This function shall update a local continuation token continuously to continue the query so you don't need to
     * re-supply the returned continuation token.</p>
     *
     * @param deviceTwinQueryCollection The query to run.
     * @return The page of query results and the continuation token for the next page of results. Return value shall be
     * {@code null} if there is no next collection.
     * @throws IotHubException If an {@link IotHubException} occurs when querying the service.
     * @throws IOException If an {@link IotHubException} occurs when querying the service or if the results of that
     * query doesn't match expectations.
     */
    public QueryCollectionResponse<DeviceTwinDevice> next(
            QueryCollection deviceTwinQueryCollection) throws IOException, IotHubException
    {
        QueryOptions options = new QueryOptions();
        options.setPageSize(deviceTwinQueryCollection.getPageSize());
        return this.next(deviceTwinQueryCollection, options);
    }

    /**
     * Returns the next {@link DeviceTwinDevice} collection for the given query alongside the continuation token needed
     * for querying the next page.
     * <p>This function shall update a local continuation token continuously to continue the query so you don't need to
     * re-supply the returned continuation token unless you want to continue the query from a different starting point.
     * To do that, set your new continuation token in the query options object.</p>
     * <p>The provided option's page size shall override any previously saved page size.</p>
     *
     * @param deviceTwinQueryCollection The query to run.
     * @param options The query options to run the query with. If the continuation token in these options is
     * {@code null}, the internally saved continuation token shall be used. The page size set in the options will
     *                override any previously set page size.
     * @return The page of query results and the continuation token for the next page of results. Return value shall be
     * {@code null} if there is no next collection.
     * @throws IotHubException If an {@link IotHubException} occurs when querying the service.
     * @throws IOException If an {@link IotHubException} occurs when querying the service or if the results of that
     * query doesn't match expectations.
     */
    public QueryCollectionResponse<DeviceTwinDevice> next(
            QueryCollection deviceTwinQueryCollection,
            QueryOptions options) throws IOException, IotHubException
    {
        if (deviceTwinQueryCollection == null)
        {
            throw new IllegalArgumentException("Query cannot be null");
        }

        if (!this.hasNext(deviceTwinQueryCollection))
        {
            return null;
        }

        QueryCollectionResponse<String> queryResults = deviceTwinQueryCollection.next(options);
        Iterator<String> jsonCollectionIterator = queryResults.getCollection().iterator();
        Collection<DeviceTwinDevice> deviceTwinDeviceList = new ArrayList<>();

        while (jsonCollectionIterator.hasNext())
        {
            deviceTwinDeviceList.add(jsonToDeviceTwinDevice(jsonCollectionIterator.next()));
        }

        return new QueryCollectionResponse<>(deviceTwinDeviceList, queryResults.getContinuationToken());
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
     * @throws IotHubException If the HTTP request failed.
     */
    public Job scheduleUpdateTwin(
            String queryCondition,
            DeviceTwinDevice updateTwin,
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

    private DeviceTwinDevice jsonToDeviceTwinDevice(String json)
    {
        TwinState twinState = TwinState.createFromTwinJson(json);

        DeviceTwinDevice deviceTwinDevice = new DeviceTwinDevice(twinState.getDeviceId());
        deviceTwinDevice.setVersion(twinState.getVersion());
        deviceTwinDevice.setETag(twinState.getETag());
        deviceTwinDevice.setTags(twinState.getTags());
        deviceTwinDevice.setDesiredProperties(twinState.getDesiredProperty());
        deviceTwinDevice.setReportedProperties(twinState.getReportedProperty());
        deviceTwinDevice.setCapabilities(twinState.getCapabilities());
        deviceTwinDevice.setConnectionState(twinState.getConnectionState());
        deviceTwinDevice.setConfigurations(twinState.getConfigurations());
        deviceTwinDevice.setModelId(twinState.getModelId());
        deviceTwinDevice.setDeviceScope(twinState.getDeviceScope());
        deviceTwinDevice.setParentScopes(twinState.getParentScopes());

        if (twinState.getModuleId() != null && !twinState.getModuleId().isEmpty())
        {
            deviceTwinDevice.setModuleId(twinState.getModuleId());
        }

        return deviceTwinDevice;
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
