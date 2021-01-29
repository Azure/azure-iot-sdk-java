/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.deps.transport.amqp.TokenCredentialType;
import com.microsoft.azure.sdk.iot.deps.twin.TwinState;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.Tools;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringCredential;
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

public class DeviceTwin
{
    private int requestId = 0;
    private final int DEFAULT_PAGE_SIZE = 100;

    private DeviceTwinClientOptions options;
    private TokenCredential authenticationTokenProvider;
    private TokenCredentialType tokenCredentialType;
    private String hostName;

    /**
     * Static constructor to create instance from connection string.
     *
     * @param connectionString The iot hub connection string.
     * @return The instance of DeviceTwin.
     * @throws IOException This exception is never thrown.
     */
    public static DeviceTwin createFromConnectionString(String connectionString) throws IOException
    {
        return createFromConnectionString(
                connectionString,
                DeviceTwinClientOptions.builder()
                    .httpConnectTimeout(DeviceTwinClientOptions.DEFAULT_HTTP_CONNECT_TIMEOUT_MS)
                    .httpReadTimeout(DeviceTwinClientOptions.DEFAULT_HTTP_READ_TIMEOUT_MS)
                    .build());
    }

    /**
     * Static constructor to create instance from connection string.
     *
     * @param connectionString The iot hub connection string.
     * @param options the configurable options for each operation on this client. May not be null.
     * @return The instance of DeviceTwin.
     * @throws IOException This exception is never thrown.
     */
    public static DeviceTwin createFromConnectionString(
            String connectionString,
            DeviceTwinClientOptions options) throws IOException
    {
        if (connectionString == null || connectionString.length() == 0)
        {
            throw new IllegalArgumentException("Connection string cannot be null or empty");
        }

        IotHubConnectionString iotHubConnectionString =
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);

        return createFromTokenCredential(
                iotHubConnectionString.getHostName(),
                new IotHubConnectionStringCredential(connectionString),
                TokenCredentialType.SHARED_ACCESS_SIGNATURE,
                options);
    }

    /**
     * Create a new DeviceTwin instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param authenticationTokenProvider The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed.
     * @param tokenCredentialType The type of authentication tokens that the provided {@link TokenCredential}
     *                          implementation will always give.
     * @return the new DeviceTwin instance.
     */
    public static DeviceTwin createFromTokenCredential(
            String hostName,
            TokenCredential authenticationTokenProvider,
            TokenCredentialType tokenCredentialType)
    {
        return createFromTokenCredential(
                hostName,
                authenticationTokenProvider,
                tokenCredentialType,
                DeviceTwinClientOptions.builder().build());
    }

    /**
     * Create a new DeviceTwin instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param authenticationTokenProvider The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed.
     * @param tokenCredentialType The type of authentication tokens that the provided {@link TokenCredential}
     *                          implementation will always give.
     * @param options The connection options to use when connecting to the service.
     * @return the new DeviceTwin instance.
     */
    public static DeviceTwin createFromTokenCredential(
            String hostName,
            TokenCredential authenticationTokenProvider,
            TokenCredentialType tokenCredentialType,
            DeviceTwinClientOptions options)
    {
        Objects.requireNonNull(authenticationTokenProvider, "TokenCredential cannot be null");
        Objects.requireNonNull(options, "options cannot be null");
        if (Tools.isNullOrEmpty(hostName))
        {
            throw new IllegalArgumentException("hostName cannot be null or empty");
        }

        DeviceTwin deviceTwin = new DeviceTwin();
        deviceTwin.options = options;
        deviceTwin.authenticationTokenProvider = authenticationTokenProvider;
        deviceTwin.tokenCredentialType = tokenCredentialType;
        deviceTwin.hostName = hostName;
        return deviceTwin;
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
        Proxy proxy = options.getProxyOptions() != null ? options.getProxyOptions().getProxy() : null;
        HttpResponse response = DeviceOperations.request(this.authenticationTokenProvider, this.tokenCredentialType, url, HttpMethod.GET, new byte[0], String.valueOf(requestId++), options.getHttpConnectTimeout(), options.getHttpReadTimeout(), proxy);
        String twin = new String(response.getBody(), StandardCharsets.UTF_8);

        TwinState twinState = TwinState.createFromTwinJson(twin);

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
     *
     * This API uses the IoT Hub PATCH API when sending updates, but it sends the full twin with each patch update.
     * As a result, devices subscribed to twin will receive notifications that each property is changed when this API is called, even
     * if only some of the properties were changed.
     *
     * See <a href="https://docs.microsoft.com/en-us/rest/api/iothub/service/twin/updatedevicetwin">PATCH</a> for more details
     *
     * @param device The device with a valid id for which device twin is to be updated.
     * @throws IOException This exception is thrown if the IO operation failed.
     * @throws IotHubException This exception is thrown if the response verification failed.
     */
    public synchronized void updateTwin(DeviceTwinDevice device) throws IotHubException, IOException
    {
        if (device == null || device.getDeviceId() == null || device.getDeviceId().length() == 0)
        {
            throw new IllegalArgumentException("Instantiate a device and set device id to be used");
        }

        if ((device.getDesiredMap() == null || device.getDesiredMap().isEmpty()) &&
                (device.getTagsMap() == null || device.getTagsMap().isEmpty()))
        {
            throw new IllegalArgumentException("Set either desired properties or tags for the device to be updated with");
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

        TwinState twinState = new TwinState(device.getTagsMap(), device.getDesiredMap(), null);
        String twinJson = twinState.toJsonElement().toString();

        Proxy proxy = options.getProxyOptions() != null ? options.getProxyOptions().getProxy() : null;
        DeviceOperations.request(
                this.authenticationTokenProvider,
                this.tokenCredentialType,
                url,
                HttpMethod.PATCH,
                twinJson.getBytes(StandardCharsets.UTF_8),
                String.valueOf(requestId++),
                options.getHttpConnectTimeout(),
                options.getHttpReadTimeout(),
                proxy);
    }

    /**
     * This method updates desired properties for the specified device.
     *
     * @param device The device with a valid id for which desired properties is to be updated.
     * @throws UnsupportedOperationException This exception is always thrown.
     * @deprecated Use updateTwin() to update desired properties.
     */
    @Deprecated
    public void updateDesiredProperties(DeviceTwinDevice device) throws UnsupportedOperationException
    {
        // Currently this is not supported by service - Please use Update twin to update desired properties
        throw new UnsupportedOperationException();
    }

    /**
     * This method replaces desired properties for the specified device. desired properties can be input
     * via device's setDesiredProperties method.
     *
     * @param device The device with a valid id for which device twin is to be updated.
     * @throws UnsupportedOperationException This exception is always thrown.
     */
    public void replaceDesiredProperties(DeviceTwinDevice device) throws UnsupportedOperationException
    {
        // Currently this is not supported by service
        throw new UnsupportedOperationException();
    }

    /**
     * This method replaces tags for the specified device. Tags can be input
     * via device's setTags method.
     *
     * @param device The device with a valid id for which device twin is to be updated.
     * @throws UnsupportedOperationException This exception is always thrown.
     */
    public void replaceTags(DeviceTwinDevice device) throws UnsupportedOperationException
    {
        // Currently this is not supported by service
        throw new UnsupportedOperationException();
    }

    /**
     * Sql style query for twin.
     *
     * @param sqlQuery Sql query string to query IotHub for Twin.
     * @param pageSize Size to limit query response by.
     * @return Query Object to be used for looking up responses for this query.
     * @throws IotHubException If Query request was not successful at the IotHub.
     * @throws IOException If input parameters are invalid.
     */
    public synchronized Query queryTwin(String sqlQuery, Integer pageSize) throws IotHubException, IOException
    {
        if (sqlQuery == null || sqlQuery.length() == 0)
        {
            throw new IllegalArgumentException("Query cannot be null or empty");
        }

        if (pageSize <= 0)
        {
            throw new IllegalArgumentException("pagesize cannot be negative or zero");
        }

        Query deviceTwinQuery = new Query(sqlQuery, pageSize, QueryType.TWIN);

        Proxy proxy = options.getProxyOptions() != null ? options.getProxyOptions().getProxy() : null;
        deviceTwinQuery.sendQueryRequest(
                this.authenticationTokenProvider,
                this.tokenCredentialType,
                IotHubConnectionString.getUrlTwinQuery(this.hostName),
                HttpMethod.POST,
                options.getHttpConnectTimeout(),
                options.getHttpReadTimeout(),
                proxy);

        return deviceTwinQuery;
    }

    /**
     * Sql style query for twin.
     *
     * @param sqlQuery Sql query string to query IotHub for Twin.
     * @return Query Object to be used for looking up responses for this query.
     * @throws IotHubException If Query request was not successful at the IotHub.
     * @throws IOException If input parameters are invalid.
     */
    public synchronized Query queryTwin(String sqlQuery) throws IotHubException, IOException
    {
        return this.queryTwin(sqlQuery, DEFAULT_PAGE_SIZE);
    }

    /**
     * Create a QueryCollection object that can be used to query whole pages of results at a time. QueryCollection objects
     * also allow you to provide a continuation token for the query to pick up from.
     *
     * @param sqlQuery the sql query to run.
     * @return the created QueryCollection object that can be used to query the service.
     * @throws MalformedURLException If twin query url is not correct.
     */
    public synchronized QueryCollection queryTwinCollection(String sqlQuery) throws MalformedURLException
    {
        return this.queryTwinCollection(sqlQuery, DEFAULT_PAGE_SIZE);
    }

    /**
     * Create a QueryCollection object that can be used to query whole pages of results at a time. QueryCollection objects
     * also allow you to provide a continuation token for the query to pick up from.
     *
     * @param sqlQuery the sql query to run.
     * @param pageSize the number of results to return at a time.
     * @return the created QueryCollection object that can be used to query the service.
     * @throws MalformedURLException If twin query url is not correct.
     */
    public synchronized QueryCollection queryTwinCollection(String sqlQuery, Integer pageSize) throws MalformedURLException
    {
        Proxy proxy = options.getProxyOptions() != null ? options.getProxyOptions().getProxy() : null;
        return new QueryCollection(
                sqlQuery,
                pageSize,
                QueryType.TWIN,
                this.hostName,
                this.authenticationTokenProvider,
                this.tokenCredentialType,
                IotHubConnectionString.getUrlTwinQuery(this.hostName),
                HttpMethod.POST,
                options.getHttpConnectTimeout(),
                options.getHttpReadTimeout(),
                proxy);
    }

    /**
     * Returns the availability of next twin element upon query. If non was found,
     * Query is sent over again and response is updated accordingly until no response
     * for the query was found.
     *
     * @param deviceTwinQuery Query object returned upon creation of query.
     * @return True if next is available and false other wise.
     * @throws IotHubException If IotHub could not respond back to the query successfully.
     * @throws IOException If input parameter is incorrect.
     */
    public synchronized boolean hasNextDeviceTwin(Query deviceTwinQuery) throws IotHubException, IOException
    {
        if (deviceTwinQuery == null)
        {
            throw new IllegalArgumentException("Query cannot be null");
        }

        return deviceTwinQuery.hasNext();
    }

    /**
     * Returns the next device twin document.
     *
     * @param deviceTwinQuery Object corresponding to the query in request.
     * @return Returns the next device twin document.
     * @throws IOException If input parameter is incorrect.
     * @throws IotHubException If a non successful response from IotHub is received.
     * @throws NoSuchElementException If no additional element was found.
     */
    public synchronized DeviceTwinDevice getNextDeviceTwin(Query deviceTwinQuery) throws IOException, IotHubException, NoSuchElementException
    {
        if (deviceTwinQuery == null)
        {
            throw new IllegalArgumentException("Query cannot be null");
        }

        Object nextObject = deviceTwinQuery.next();

        if (nextObject instanceof String)
        {
            String twinJson = (String) nextObject;
            return jsonToDeviceTwinDevice(twinJson);
        }
        else
        {
            throw new IOException("Received a response that could not be parsed");
        }
    }

    /**
     * Returns if the provided deviceTwinQueryCollection has a next page to query.
     * @param deviceTwinQueryCollection the query to check.
     * @return True if the provided deviceTwinQueryCollection has a next page to query, false otherwise.
     * @throws IllegalArgumentException if the provided deviceTwinQueryCollection is null.
     */
    public synchronized boolean hasNext(QueryCollection deviceTwinQueryCollection)
    {
        if (deviceTwinQueryCollection == null)
        {
            throw new IllegalArgumentException("deviceTwinQueryCollection cannot be null");
        }

        return deviceTwinQueryCollection.hasNext();
    }

    /**
     * Returns the next DeviceTwinDevice collection for the given query alongside the continuation token needed for querying the next page.
     *
     * <p>This function shall update a local continuation token continuously to continue the query so you don't need to re-supply the returned
     * continuation token.</p>
     *
     * @param deviceTwinQueryCollection the query to run.
     * @return The page of query results and the continuation token for the next page of results. Return value shall be {@code null} if there is no next collection.
     * @throws IotHubException If an IotHubException occurs when querying the service.
     * @throws IOException If an IotHubException occurs when querying the service or if the results of that query don't match expectations.
     */
    public synchronized QueryCollectionResponse<DeviceTwinDevice> next(
            QueryCollection deviceTwinQueryCollection) throws IOException, IotHubException
    {
        QueryOptions options = new QueryOptions();
        options.setPageSize(deviceTwinQueryCollection.getPageSize());
        return this.next(deviceTwinQueryCollection, options);
    }

    /**
     * Returns the next DeviceTwinDevice collection for the given query alongside the continuation token needed for querying the next page.
     *
     * <p>This function shall update a local continuation token continuously to continue the query so you don't need to re-supply the returned
     * continuation token unless you want to continue the query from a different starting point. To do that, set your new continuation token in the query options object.</p>
     *
     * <p>The provided option's page size shall override any previously saved page size.</p>
     *
     * @param deviceTwinQueryCollection the query to run.
     * @param options the query options to run the query with. If the continuation token in these options is null, the internally saved continuation token shall be used.
     *                The page size set in the options will override any previously set page size.
     * @return The page of query results and the continuation token for the next page of results. Return value shall be {@code null} if there is no next collection.
     * @throws IotHubException If an IotHubException occurs when querying the service.
     * @throws IOException If an IotHubException occurs when querying the service or if the results of that query don't match expectations.
     */
    public synchronized QueryCollectionResponse<DeviceTwinDevice> next(
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
     * @param queryCondition Query condition to evaluate which devices to run the job on. It can be {@code null} or empty.
     * @param updateTwin Twin object to use for the update.
     * @param startTimeUtc Date time in Utc to start the job.
     * @param maxExecutionTimeInSeconds Max execution time in seconds, i.e., ttl duration the job can run.
     * @return a Job class that represent this job on IotHub.
     * @throws IOException if the function contains invalid parameters.
     * @throws IotHubException if the http request failed.
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

        Job job = new Job(this.hostName, this.authenticationTokenProvider, this.tokenCredentialType);

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

        if (twinState.getModuleId() != null && !twinState.getModuleId().isEmpty())
        {
            deviceTwinDevice.setModuleId(twinState.getModuleId());
        }

        return deviceTwinDevice;
    }
}
