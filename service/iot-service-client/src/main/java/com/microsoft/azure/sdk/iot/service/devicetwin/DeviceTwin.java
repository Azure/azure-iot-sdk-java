/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.deps.twin.*;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DeviceTwin
{
    private IotHubConnectionString iotHubConnectionString = null;
    private Integer requestId = 0;
    private final long USE_DEFAULT_TIMEOUT = 0;
    private final int DEFAULT_PAGE_SIZE = 100;

    /**
     * Static constructor to create instance from connection string
     *
     * @param connectionString The iot hub connection string
     * @return The instance of DeviceTwin
     * @throws IOException This exception is thrown if the object creation failed
     */
    public static DeviceTwin createFromConnectionString(String connectionString) throws IOException
    {
        if (connectionString == null || connectionString.length() == 0)
        {
            /*
            **Codes_SRS_DEVICETWIN_25_001: [** The constructor shall throw IllegalArgumentException if the input string is null or empty **]**
             */
            throw new IllegalArgumentException("Connection string cannot be null or empty");
        }
        /*
        **Codes_SRS_DEVICETWIN_25_003: [** The constructor shall create a new DeviceTwin instance and return it **]**
         */
        DeviceTwin deviceTwin = new DeviceTwin();
        /*
        **Codes_SRS_DEVICETWIN_25_002: [** The constructor shall create an IotHubConnectionStringBuilder object from the given connection string **]**
         */
        deviceTwin.iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        return deviceTwin;
    }

    /**
     * This method retrieves device twin for the specified device.
     *
     * @param device The device with a valid id for which device twin is to be retrieved.
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public void getTwin(DeviceTwinDevice device) throws IotHubException, IOException
    {
        if (device == null || device.getDeviceId() == null || device.getDeviceId().length() == 0)
        {
            /*
             **Codes_SRS_DEVICETWIN_25_004: [** The function shall throw IllegalArgumentException if the input device is null or if deviceId is null or empty **]**
             */
            throw new IllegalArgumentException("Instantiate a device and set device id to be used");
        }

        URL url = null;
        if ((device.getModuleId() == null) || device.getModuleId().length() ==0)
        {
            /*
             **Codes_SRS_DEVICETWIN_25_005: [** The function shall build the URL for this operation by calling getUrlTwin **]**
             */
            url = this.iotHubConnectionString.getUrlTwin(device.getDeviceId());
        }
        else
        {
            /*
             **Codes_SRS_DEVICETWIN_28_001: [** The function shall build the URL for this operation by calling getUrlModuleTwin if moduleId is not null **]**
             */
            url = this.iotHubConnectionString.getUrlModuleTwin(device.getDeviceId(), device.getModuleId());
        }

        getTwinOperation(url, device);
    }

    private void getTwinOperation(URL url, DeviceTwinDevice device) throws IotHubException, IOException
    {
        /*
        **Codes_SRS_DEVICETWIN_25_006: [** The function shall create a new SAS token **]**
        **Codes_SRS_DEVICETWIN_25_007: [** The function shall create a new HttpRequest with http method as Get **]**
        **Codes_SRS_DEVICETWIN_25_008: [** The function shall set the following HTTP headers specified in the IotHub DeviceTwin doc.
                                                1. Key as authorization with value as sastoken
                                                2. Key as request id with a new string value for every request
                                                3. Key as User-Agent with value specified by the clientIdentifier and its version
                                                4. Key as Accept with value as application/json
                                                5. Key as Content-Type and value as application/json
                                                6. Key as charset and value as utf-8
                                                7. Key as If-Match and value as '*'  **]**
         **Codes_SRS_DEVICETWIN_25_009: [** The function shall send the created request and get the response **]**
         **Codes_SRS_DEVICETWIN_25_010: [** The function shall verify the response status and throw proper Exception **]**
         */
        HttpResponse response = DeviceOperations.request(this.iotHubConnectionString, url, HttpMethod.GET, new byte[0], String.valueOf(requestId++), USE_DEFAULT_TIMEOUT);
        String twin = new String(response.getBody(), StandardCharsets.UTF_8);

        /*
        **Codes_SRS_DEVICETWIN_25_011: [** The function shall deserialize the payload by calling updateTwin Api on the twin object **]**
         */
        TwinState twinState = TwinState.createFromTwinJson(twin);

        /*
        **Codes_SRS_DEVICETWIN_25_012: [** The function shall set eTag, tags, desired property map, reported property map on the user device **]**
         */
        device.setETag(twinState.getETag());
        device.setTags(twinState.getTags());
        device.setDesiredProperties(twinState.getDesiredProperty());
        device.setReportedProperties(twinState.getReportedProperty());
        device.setCapabilities(twinState.getCapabilities());
        device.setConfigurations(twinState.getConfigurations());
    }

    /**
     * This method updates device twin for the specified device.
     *
     * @param device The device with a valid id for which device twin is to be updated.
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public synchronized void updateTwin(DeviceTwinDevice device) throws IotHubException, IOException
    {
        if (device == null || device.getDeviceId() == null || device.getDeviceId().length() == 0)
        {
            /*
            **Codes_SRS_DEVICETWIN_25_013: [** The function shall throw IllegalArgumentException if the input device is null or if deviceId is null or empty **]**
             */
            throw new IllegalArgumentException("Instantiate a device and set device id to be used");
        }

        if ((device.getDesiredMap() == null || device.getDesiredMap().isEmpty()) &&
                (device.getTagsMap() == null || device.getTagsMap().isEmpty()))
        {
            /*
            **Codes_SRS_DEVICETWIN_25_045: [** The function shall throw IllegalArgumentException if the both desired and tags maps are either empty or null **]**
             */
            throw new IllegalArgumentException("Set either desired properties or tags for the device to be updated with");
        }

        URL url;
        if ((device.getModuleId() == null) || device.getModuleId().length() ==0)
        {
            /*
             **Codes_SRS_DEVICETWIN_25_005: [** The function shall build the URL for this operation by calling getUrlTwin**]**
             */
            url = this.iotHubConnectionString.getUrlTwin(device.getDeviceId());
        }
        else
        {
            /*
             **Codes_SRS_DEVICETWIN_28_002: [** The function shall build the URL for this operation by calling getUrlModuleTwin if moduleId is not null **]**
             */
            url = this.iotHubConnectionString.getUrlModuleTwin(device.getDeviceId(), device.getModuleId());
        }

        /*
        **Codes_SRS_DEVICETWIN_25_015: [** The function shall serialize the twin map by calling updateTwin Api on the twin object for the device provided by the user**]**
         */
        TwinState twinState = new TwinState(device.getTagsMap(), device.getDesiredMap(), null);
        String twinJson = twinState.toJsonElement().toString();

        /*
        **Codes_SRS_DEVICETWIN_25_016: [** The function shall create a new SAS token **]**

        **Codes_SRS_DEVICETWIN_25_017: [** The function shall create a new HttpRequest with http method as Patch **]**

        **Codes_SRS_DEVICETWIN_25_018: [** The function shall set the following HTTP headers specified in the IotHub DeviceTwin doc.
                                                1. Key as authorization with value as sastoken
                                                2. Key as request id with a new string value for every request
                                                3. Key as User-Agent with value specified by the clientIdentifier and its version
                                                4. Key as Accept with value as application/json
                                                5. Key as Content-Type and value as application/json
                                                6. Key as charset and value as utf-8
                                                7. Key as If-Match and value as '*'  **]**

        **Codes_SRS_DEVICETWIN_25_019: [** The function shall send the created request and get the response **]**

        **Codes_SRS_DEVICETWIN_25_020: [** The function shall verify the response status and throw proper Exception **]**
         */
        HttpResponse response = DeviceOperations.request(this.iotHubConnectionString, url, HttpMethod.PATCH, twinJson.getBytes(StandardCharsets.UTF_8), String.valueOf(requestId++),0);
    }

    /**
     * This method updates desired properties for the specified device.
     *
     * @param device The device with a valid id for which desired properties is to be updated.
     * @throws UnsupportedOperationException This exception is always thrown.
     * @deprecated Use updateTwin() to update desired properties
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
     * Sql style query for twin
     * @param sqlQuery Sql query string to query IotHub for Twin
     * @param pageSize Size to limit query response by
     * @return Query Object to be used for looking up responses for this query
     * @throws IotHubException If Query request was not successful at the IotHub
     * @throws IOException If input parameters are invalid
     */
    public synchronized Query queryTwin(String sqlQuery, Integer pageSize) throws IotHubException, IOException
    {
        if (sqlQuery == null || sqlQuery.length() == 0)
        {
            //Codes_SRS_DEVICETWIN_25_047: [ The method shall throw IllegalArgumentException if the query is null or empty.]
            throw new IllegalArgumentException("Query cannot be null or empty");
        }

        if (pageSize <= 0)
        {
            //Codes_SRS_DEVICETWIN_25_048: [ The method shall throw IllegalArgumentException if the page size is zero or negative.]
            throw new IllegalArgumentException("pagesize cannot be negative or zero");
        }

        //Codes_SRS_DEVICETWIN_25_050: [ The method shall create a new Query Object of Type TWIN. ]
        Query deviceTwinQuery = new Query(sqlQuery, pageSize, QueryType.TWIN);

        //Codes_SRS_DEVICETWIN_25_049: [ The method shall build the URL for this operation by calling getUrlTwinQuery ]
        //Codes_SRS_DEVICETWIN_25_051: [ The method shall send a Query Request to IotHub as HTTP Method Post on the query Object by calling sendQueryRequest.]
        deviceTwinQuery.sendQueryRequest(iotHubConnectionString, iotHubConnectionString.getUrlTwinQuery(), HttpMethod.POST, USE_DEFAULT_TIMEOUT);
        return deviceTwinQuery;
    }

    /**
     * Sql style query for twin
     * @param sqlQuery Sql query string to query IotHub for Twin
     * @return Query Object to be used for looking up responses for this query
     * @throws IotHubException If Query request was not successful at the IotHub
     * @throws IOException If input parameters are invalid
     */
    public synchronized Query queryTwin(String sqlQuery) throws IotHubException, IOException
    {
        //Codes_SRS_DEVICETWIN_25_052: [ If the pageSize if not provided then a default pageSize of 100 is used for the query.]
        return this.queryTwin(sqlQuery, DEFAULT_PAGE_SIZE);
    }

    /**
     * Create a QueryCollection object that can be used to query whole pages of results at a time. QueryCollection objects
     * also allow you to provide a continuation token for the query to pick up from
     *
     * @param sqlQuery the sql query to run
     * @return the created QueryCollection object that can be used to query the service
     * @throws MalformedURLException If twin query url is not correct
     */
    public synchronized QueryCollection queryTwinCollection(String sqlQuery) throws MalformedURLException
    {
        //Codes_SRS_DEVICETWIN_34_069: [This function shall return the results of calling queryTwinCollection(sqlQuery, DEFAULT_PAGE_SIZE).]
        return this.queryTwinCollection(sqlQuery, DEFAULT_PAGE_SIZE);
    }

    /**
     * Create a QueryCollection object that can be used to query whole pages of results at a time. QueryCollection objects
     * also allow you to provide a continuation token for the query to pick up from
     *
     * @param sqlQuery the sql query to run
     * @param pageSize the number of results to return at a time
     * @return the created QueryCollection object that can be used to query the service
     * @throws MalformedURLException If twin query url is not correct
     */
    public synchronized QueryCollection queryTwinCollection(String sqlQuery, Integer pageSize) throws MalformedURLException
    {
        //Codes_SRS_DEVICETWIN_34_070: [This function shall return a new QueryCollection object of type TWIN with the provided sql query and page size.]
        return new QueryCollection(sqlQuery, pageSize, QueryType.TWIN, this.iotHubConnectionString, this.iotHubConnectionString.getUrlTwinQuery(), HttpMethod.POST, USE_DEFAULT_TIMEOUT);
    }

    /**
     * Returns the availability of next twin element upon query. If non was found,
     * Query is sent over again and response is updated accordingly until no response
     * for the query was found.
     * @param deviceTwinQuery Query object returned upon creation of query
     * @return True if next is available and false other wise.
     * @throws IotHubException If IotHub could not respond back to the query successfully
     * @throws IOException If input parameter is incorrect
     */
    public synchronized boolean hasNextDeviceTwin(Query deviceTwinQuery) throws IotHubException, IOException
    {
        if (deviceTwinQuery == null)
        {
            //Codes_SRS_DEVICETWIN_25_053: [ The method shall throw IllegalArgumentException if query is null ]
            throw new IllegalArgumentException("Query cannot be null");
        }

        //Codes_SRS_DEVICETWIN_25_055: [ If a queryResponse is available, this method shall return true as is to the user, and false otherwise.. ]
        return deviceTwinQuery.hasNext();
    }

    /**
     * Returns the next device twin document
     * @param deviceTwinQuery Object corresponding to the query in request
     * @return Returns the next device twin document
     * @throws IOException If input parameter is incorrect
     * @throws IotHubException If a non successful response from IotHub is received
     * @throws NoSuchElementException If no additional element was found
     */
    public synchronized DeviceTwinDevice getNextDeviceTwin(Query deviceTwinQuery) throws IOException, IotHubException, NoSuchElementException
    {
        if (deviceTwinQuery == null)
        {
            //Codes_SRS_DEVICETWIN_25_054: [ The method shall throw IllegalArgumentException if query is null ]
            throw new IllegalArgumentException("Query cannot be null");
        }

        Object nextObject = deviceTwinQuery.next();

        if (nextObject instanceof String)
        {
            //Codes_SRS_DEVICETWIN_25_059: [ The method shall parse the next element from the query response as Twin Document using TwinState and provide the response on DeviceTwinDevice.]
            String twinJson = (String) nextObject;
            return jsonToDeviceTwinDevice(twinJson);
        }
        else
        {
            //Codes_SRS_DEVICETWIN_25_060: [ If the next element from the query response is an object other than String, then this method shall throw IOException ]
            throw new IOException("Received a response that could not be parsed");
        }
    }

    /**
     * Returns if the provided deviceTwinQueryCollection has a next page to query.
     * @param deviceTwinQueryCollection the query to check
     * @return True if the provided deviceTwinQueryCollection has a next page to query, false otherwise
     * @throws IllegalArgumentException if the provided deviceTwinQueryCollection is null
     */
    public synchronized boolean hasNext(QueryCollection deviceTwinQueryCollection)
    {
        if (deviceTwinQueryCollection == null)
        {
            //Codes_SRS_DEVICETWIN_34_080: [If the provided deviceTwinQueryCollection is null, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("deviceTwinQueryCollection cannot be null");
        }

        //Codes_SRS_DEVICETWIN_34_071: [This function shall return if the provided deviceTwinQueryCollection has next.]
        return deviceTwinQueryCollection.hasNext();
    }

    /**
     * Returns the next DeviceTwinDevice collection for the given query alongside the continuation token needed for querying the next page.
     *
     * <p>This function shall update a local continuation token continuously to continue the query so you don't need to re-supply the returned
     * continuation token.</p>
     *
     * @param deviceTwinQueryCollection the query to run
     * @return The page of query results and the continuation token for the next page of results. Return value shall be {@code null} if there is no next collection
     * @throws IotHubException If an IotHubException occurs when querying the service.
     * @throws IOException If an IotHubException occurs when querying the service or if the results of that query don't match expectations.
     */
    public synchronized QueryCollectionResponse<DeviceTwinDevice> next(QueryCollection deviceTwinQueryCollection) throws IOException, IotHubException
    {
        //Codes_SRS_DEVICETWIN_34_075: [This function shall call next(deviceTwinQueryCollection, queryOptions) where queryOptions has the deviceTwinQueryCollection's current page size.]
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
     * @param deviceTwinQueryCollection the query to run
     * @param options the query options to run the query with. If the continuation token in these options is null, the internally saved continuation token shall be used.
     *                The page size set in the options will override any previously set page size.
     * @return The page of query results and the continuation token for the next page of results. Return value shall be {@code null} if there is no next collection.
     * @throws IotHubException If an IotHubException occurs when querying the service.
     * @throws IOException If an IotHubException occurs when querying the service or if the results of that query don't match expectations.
     */
    public synchronized QueryCollectionResponse<DeviceTwinDevice> next(QueryCollection deviceTwinQueryCollection, QueryOptions options) throws IOException, IotHubException
    {
        if (deviceTwinQueryCollection == null)
        {
            //Codes_SRS_DEVICETWIN_34_076: [If the provided deviceTwinQueryCollection is null, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("Query cannot be null");
        }

        if (!this.hasNext(deviceTwinQueryCollection))
        {
            //Codes_SRS_DEVICETWIN_34_077: [If the provided deviceTwinQueryCollection has no next set to give, this function shall return null.]
            return null;
        }

        QueryCollectionResponse<String> queryResults = deviceTwinQueryCollection.next(options);
        Iterator<String> jsonCollectionIterator = queryResults.getCollection().iterator();
        Collection<DeviceTwinDevice> deviceTwinDeviceList = new ArrayList<>();

        //Codes_SRS_DEVICETWIN_34_078: [If the provided deviceTwinQueryCollection has a next set to give, this function shall retrieve that set from deviceTwinQueryCollection, cast its contents to DeviceTwinDevice objects, and return it in a QueryCollectionResponse object.]
        while (jsonCollectionIterator.hasNext())
        {
            deviceTwinDeviceList.add(jsonToDeviceTwinDevice(jsonCollectionIterator.next()));
        }

        //Codes_SRS_DEVICETWIN_34_079: [The returned QueryCollectionResponse object shall contain the continuation token needed to retrieve the next set with.]
        return new QueryCollectionResponse<DeviceTwinDevice>(deviceTwinDeviceList, queryResults.getContinuationToken());
    }

    /**
     * Creates a new Job to update twin tags and desired properties on one or multiple devices
     *
     * @param queryCondition Query condition to evaluate which devices to run the job on. It can be {@code null} or empty
     * @param updateTwin Twin object to use for the update
     * @param startTimeUtc Date time in Utc to start the job
     * @param maxExecutionTimeInSeconds Max execution time in seconds, i.e., ttl duration the job can run
     * @return a Job class that represent this job on IotHub
     * @throws IOException if the function contains invalid parameters
     * @throws IotHubException if the http request failed
     */
    public Job scheduleUpdateTwin(String queryCondition,
                                  DeviceTwinDevice updateTwin,
                                  Date startTimeUtc,
                                  long maxExecutionTimeInSeconds) throws IOException, IotHubException
    {
        // Codes_SRS_DEVICETWIN_21_061: [If the updateTwin is null, the scheduleUpdateTwin shall throws IllegalArgumentException ]
        if(updateTwin == null)
        {
            throw new IllegalArgumentException("null updateTwin");
        }

        // Codes_SRS_DEVICETWIN_21_062: [If the startTimeUtc is null, the scheduleUpdateTwin shall throws IllegalArgumentException ]
        if(startTimeUtc == null)
        {
            throw new IllegalArgumentException("null startTimeUtc");
        }

        // Codes_SRS_DEVICETWIN_21_063: [If the maxExecutionTimeInSeconds is negative, the scheduleUpdateTwin shall throws IllegalArgumentException ]
        if(maxExecutionTimeInSeconds < 0)
        {
            throw new IllegalArgumentException("negative maxExecutionTimeInSeconds");
        }

        // Codes_SRS_DEVICETWIN_21_064: [The scheduleUpdateTwin shall create a new instance of the Job class ]
        // Codes_SRS_DEVICETWIN_21_065: [If the scheduleUpdateTwin failed to create a new instance of the Job class, it shall throws IOException. Threw by the Jobs constructor ]
        Job job = new Job(iotHubConnectionString.toString());

        // Codes_SRS_DEVICETWIN_21_066: [The scheduleUpdateTwin shall invoke the scheduleUpdateTwin in the Job class with the received parameters ]
        // Codes_SRS_DEVICETWIN_21_067: [If scheduleUpdateTwin failed, the scheduleUpdateTwin shall throws IotHubException. Threw by the scheduleUpdateTwin ]
        job.scheduleUpdateTwin(queryCondition, updateTwin, startTimeUtc, maxExecutionTimeInSeconds);

        // Codes_SRS_DEVICETWIN_21_068: [The scheduleUpdateTwin shall return the created instance of the Job class ]
        return job;
    }

    private DeviceTwinDevice jsonToDeviceTwinDevice(String json) throws IOException
    {
        TwinState twinState = TwinState.createFromTwinJson(json);

        DeviceTwinDevice deviceTwinDevice = new DeviceTwinDevice(twinState.getDeviceId());
        deviceTwinDevice.setVersion(twinState.getVersion());
        deviceTwinDevice.setETag(twinState.getETag());
        deviceTwinDevice.setTags(twinState.getTags());
        deviceTwinDevice.setDesiredProperties(twinState.getDesiredProperty());
        deviceTwinDevice.setReportedProperties(twinState.getReportedProperty());

        if (twinState.getModuleId() != null && !twinState.getModuleId().isEmpty())
        {
            deviceTwinDevice.setModuleId(twinState.getModuleId());
        }

        return deviceTwinDevice;
    }
}
