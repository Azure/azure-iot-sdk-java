/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.deps.serializer.TwinParser;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.NoSuchElementException;

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

        /*
        **Codes_SRS_DEVICETWIN_25_005: [** The function shall build the URL for this operation by calling getUrlTwin **]**
         */
        URL url = this.iotHubConnectionString.getUrlTwin(device.getDeviceId());

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
        device.getTwinParser().updateTwin(twin);

        /*
        **Codes_SRS_DEVICETWIN_25_012: [** The function shall set tags, desired property map, reported property map on the user device **]**
         */
        device.setTags(device.getTwinParser().getTagsMap());
        device.setDesiredProperties(device.getTwinParser().getDesiredPropertyMap());
        device.setReportedProperties(device.getTwinParser().getReportedPropertyMap());
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

        /*
        **Codes_SRS_DEVICETWIN_25_014: [** The function shall build the URL for this operation by calling getUrlTwin **]**
         */
        URL url = this.iotHubConnectionString.getUrlTwin(device.getDeviceId());

        /*
        **Codes_SRS_DEVICETWIN_25_015: [** The function shall serialize the twin map by calling updateTwin Api on the twin object for the device provided by the user**]**
         */
        String twinJson = device.getTwinParser().updateTwin(device.getDesiredMap(), null, device.getTagsMap());

        if (twinJson == null || twinJson.isEmpty())
        {
            /*
            **Codes_SRS_DEVICETWIN_25_046: [** The function shall throw IOException if updateTwin Api call returned an empty or null json**]**
             */
            throw new IOException("Serializer cannot return null json to update");
        }

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
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public void updateDesiredProperties(DeviceTwinDevice device) throws IotHubException, IOException
    {
        if (device == null || device.getDeviceId() == null || device.getDeviceId().length() == 0)
        {
            /*
            **Codes_SRS_DEVICETWIN_25_021: [** The function shall throw IllegalArgumentException if the input device is null or if deviceId is null or empty **]**
             */
            throw new IllegalArgumentException("Instantiate a device and set device id to be used");
        }

        if (device.getDesiredMap() == null)
        {
            throw new IllegalArgumentException("Set desired properties for the device to be updated with");
        }

        /*
        **Codes_SRS_DEVICETWIN_25_022: [** The function shall build the URL for this operation by calling getUrlTwinDesired **]**
         */
        URL url = this.iotHubConnectionString.getUrlTwinDesired(device.getDeviceId());

        /*
        **Codes_SRS_DEVICETWIN_25_023: [** The function shall serialize the desired properties map by calling updateDesiredProperty Api on the twin object for the device provided by the user**]**
         */
        String desiredJson = device.getTwinParser().updateDesiredProperty(device.getDesiredMap());

        if (desiredJson == null)
        {
            return;
        }

        // Currently this is not supported by service - Please use Update twin to update desired properties
        throw new NotImplementedException();
        /*
        **Codes_SRS_DEVICETWIN_25_024: [** The function shall create a new SAS token **]**

        **Codes_SRS_DEVICETWIN_25_025: [** The function shall create a new HttpRequest with http method as Patch **]**

        **Codes_SRS_DEVICETWIN_25_026: [** The function shall set the following HTTP headers specified in the IotHub DeviceTwin doc.
                                                1. Key as authorization with value as sastoken
                                                2. Key as request id with a new string value for every request
                                                3. Key as User-Agent with value specified by the clientIdentifier and its version
                                                4. Key as Accept with value as application/json
                                                5. Key as Content-Type and value as application/json
                                                6. Key as charset and value as utf-8
                                                7. Key as If-Match and value as '*'  **]**

        **Codes_SRS_DEVICETWIN_25_027: [** The function shall send the created request and get the response **]**

        **Codes_SRS_DEVICETWIN_25_028: [** The function shall verify the response status and throw proper Exception **]**
         */
       /*
        HttpResponse response = this.processHttpTwinRequest(url, HttpMethod.PATCH, desiredJson.getBytes(), String.valueOf(requestId++));
        */
    }

    /**
     * This method replaces desired properties for the specified device. desired properties can be input
     * via device's setDesiredProperties method.
     *
     * @param device The device with a valid id for which device twin is to be updated.
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public void replaceDesiredProperties(DeviceTwinDevice device) throws IotHubException, IOException
    {
        if (device == null || device.getDeviceId() == null || device.getDeviceId().length() == 0)
        {
            /*
            **Codes_SRS_DEVICETWIN_25_029: [** The function shall throw IllegalArgumentException if the input device is null or if deviceId is null or empty **]**
             */
            throw new IllegalArgumentException("Instantiate a device and set device id to be used");
        }

        if (device.getDesiredMap() == null)
        {
            throw new IllegalArgumentException("Set desired properties fort he device to be replaced with");
        }

        /*
        **Codes_SRS_DEVICETWIN_25_030: [** The function shall build the URL for this operation by calling getUrlTwinDesired **]**
         */
        URL url = this.iotHubConnectionString.getUrlTwinDesired(device.getDeviceId());

        /*
        **Codes_SRS_DEVICETWIN_25_031: [** The function shall serialize the desired properties map by calling resetDesiredProperty Api on the twin object for the device provided by the user**]**
         */
        String tags = device.getTwinParser().resetDesiredProperty(device.getDesiredMap());

        if (tags == null || tags.length() == 0)
        {
            /*
             *Codes_SRS_DEVICETWIN_25_045: [** If resetDesiredProperty call returns null or empty string then this method shall throw IOException**]**
             */
            throw new IOException("Serializer cannot return null or empty string");

        }

        throw new NotImplementedException();

        /*
        **Codes_SRS_DEVICETWIN_25_032: [** The function shall create a new SAS token **]**
        **Codes_SRS_DEVICETWIN_25_033: [** The function shall create a new HttpRequest with http method as PUT **]**

        **Codes_SRS_DEVICETWIN_25_034: [** The function shall set the following HTTP headers specified in the IotHub DeviceTwin doc.
                                                1. Key as authorization with value as sastoken
                                                2. Key as request id with a new string value for every request
                                                3. Key as User-Agent with value specified by the clientIdentifier and its version
                                                4. Key as Accept with value as application/json
                                                5. Key as Content-Type and value as application/json
                                                6. Key as charset and value as utf-8
                                                7. Key as If-Match and value as '*'  **]**

        **Codes_SRS_DEVICETWIN_25_035: [** The function shall send the created request and get the response **]**

        **Codes_SRS_DEVICETWIN_25_036: [** The function shall verify the response status and throw proper Exception **]**
         */
        // Currently not implemented on service
        // HttpResponse response = this.processHttpTwinRequest(url, HttpMethod.PUT, tags.getBytes(), String.valueOf(requestId++));
    }

    /**
     * This method replaces tags for the specified device. Tags can be input
     * via device's setTags method.
     *
     * @param device The device with a valid id for which device twin is to be updated.
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public void replaceTags(DeviceTwinDevice device) throws IotHubException, IOException
    {
        if (device == null || device.getDeviceId() == null || device.getDeviceId().length() == 0)
        {
            /*
             * Codes_SRS_DEVICETWIN_25_037: [** The function shall throw IllegalArgumentException if the input device is null or if deviceId is null or empty **]**
             */
            throw new IllegalArgumentException("Instantiate a device and set device id to be used");
        }

        if (device.getTagsMap() == null)
        {
            throw new IllegalArgumentException("Set tags to be replaced with");
        }

        /*
        **Codes_SRS_DEVICETWIN_25_038: [** The function shall build the URL for this operation by calling getUrlTwinTags **]**
         */
        URL url = this.iotHubConnectionString.getUrlTwinTags(device.getDeviceId());

        /*
        **Codes_SRS_DEVICETWIN_25_039: [** The function shall serialize the tags map by calling resetTags Api on the twin object for the device provided by the user**]**
         */
        String tags = device.getTwinParser().resetTags(device.getTagsMap());

        if (tags == null || tags.length() == 0)
        {
            /*
             **Codes_SRS_DEVICETWIN_25_046: [** If resetTags call returns null or empty string then this method shall throw IOException**]**
             */
            throw new IOException("Serializer cannot return null or empty");
        }

        /*
        **Codes_SRS_DEVICETWIN_25_040: [** The function shall create a new SAS token **]**

        **Codes_SRS_DEVICETWIN_25_041: [** The function shall create a new HttpRequest with http method as PUT **]**

        **Codes_SRS_DEVICETWIN_25_042: [** The function shall set the following HTTP headers specified in the IotHub DeviceTwin doc.
                                                1. Key as authorization with value as sastoken
                                                2. Key as request id with a new string value for every request
                                                3. Key as User-Agent with value specified by the clientIdentifier and its version
                                                4. Key as Accept with value as application/json
                                                5. Key as Content-Type and value as application/json
                                                6. Key as charset and value as utf-8
                                                7. Key as If-Match and value as '*'  **]**

        **Codes_SRS_DEVICETWIN_25_043: [** The function shall send the created request and get the response **]**

        **Codes_SRS_DEVICETWIN_25_044: [** The function shall verify the response status and throw proper Exception **]**

         */
        throw new NotImplementedException();
        // Currently not implemented on service
        // HttpResponse response = this.processHttpTwinRequest(url, HttpMethod.PUT, tags.getBytes(), String.valueOf(requestId++));
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
            //Codes_SRS_DEVICETWIN_25_059: [ The method shall parse the next element from the query response as Twin Document using TwinParser and provide the response on DeviceTwinDevice.]
            String twinJson = (String) nextObject;
            TwinParser twinParser = new TwinParser();
            twinParser.enableTags();
            twinParser.updateTwin(twinJson);

            DeviceTwinDevice deviceTwinDevice = new DeviceTwinDevice(twinParser.getDeviceId());
            deviceTwinDevice.setTags(twinParser.getTagsMap());
            deviceTwinDevice.setDesiredProperties(twinParser.getDesiredPropertyMap());
            deviceTwinDevice.setReportedProperties(twinParser.getReportedPropertyMap());

            return deviceTwinDevice;
        }
        else
        {
            //Codes_SRS_DEVICETWIN_25_060: [ If the next element from the query response is an object other than String, then this method shall throw IOException ]
            throw new IOException("Received a response that could not be parsed");
        }
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
}
