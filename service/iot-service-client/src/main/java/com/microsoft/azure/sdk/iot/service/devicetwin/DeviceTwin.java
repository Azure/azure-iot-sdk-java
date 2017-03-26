/**
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubExceptionManager;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.transport.TransportUtils;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class DeviceTwin
{
    private IotHubConnectionString iotHubConnectionString = null;
    private Integer requestId = 0;

    /*
        Values for Http header
     */

    private static final String AUTHORIZATION = "authorization";
    private static final String REQUEST_ID = "Request-Id";
    private static final String USER_AGENT = "User-Agent";
    private static final String ACCEPT = "Accept";
    private static final String ACCEPT_VALUE = "application/json";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CHARSET = "charset";
    private static final String ANY_VALUE = "'*'";
    private static final String IF_MATCH = "If-Match";
    private final Integer DEFAULT_HTTP_TIMOUT_MS = 24000;

    /**
     * Static constructor to create instance from connection string
     *
     * @param connectionString The iot hub connection string
     * @return The instance of DeviceTwin
     * @throws Exception This exception is thrown if the object creation failed
     */
    public static DeviceTwin createFromConnectionString(String connectionString) throws Exception
    {
        if (connectionString == null || connectionString.length() == 0)
        {
            /**
            **Codes_SRS_DEVICETWIN_25_001: [** The constructor shall throw IllegalArgumentException if the input string is null or empty **]**
             */
            throw new IllegalArgumentException("Connection string cannot be null or empty");
        }
        /**
        **Codes_SRS_DEVICETWIN_25_003: [** The constructor shall create a new DeviceTwin instance and return it **]**
         */
        DeviceTwin deviceTwin = new DeviceTwin();
        /**
        **Codes_SRS_DEVICETWIN_25_002: [** The constructor shall create an IotHubConnectionStringBuilder object from the given connection string **]**
         */
        deviceTwin.iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        return deviceTwin;
    }

    private HttpResponse processHttpTwinRequest(URL url, HttpMethod method, byte[] payload, String requestId) throws IOException, IotHubException
    {
        HttpRequest request = new HttpRequest(url, method, payload);
        String sasTokenString = new IotHubServiceSasToken(this.iotHubConnectionString).toString();
        request.setReadTimeoutMillis(DEFAULT_HTTP_TIMOUT_MS);
        request.setHeaderField(AUTHORIZATION, sasTokenString);
        request.setHeaderField(REQUEST_ID, requestId);
        request.setHeaderField(USER_AGENT, TransportUtils.getJavaServiceClientIdentifier() + "/" + TransportUtils.getServiceVersion());
        request.setHeaderField(ACCEPT, ACCEPT_VALUE);
        request.setHeaderField(CONTENT_TYPE, ACCEPT_VALUE);
        request.setHeaderField(CHARSET, ANY_VALUE);
        request.setHeaderField(IF_MATCH, ANY_VALUE);

        HttpResponse response = request.send();
        IotHubExceptionManager.httpResponseVerification(response);
        return response;
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
            /**
            **Codes_SRS_DEVICETWIN_25_004: [** The function shall throw IllegalArgumentException if the input device is null or if deviceId is null or empty **]**
             */
            throw new IllegalArgumentException("Instantiate a device and set device id to be used");
        }

        /**
        **Codes_SRS_DEVICETWIN_25_005: [** The function shall build the URL for this operation by calling getUrlTwin **]**
         */
        URL url = this.iotHubConnectionString.getUrlTwin(device.getDeviceId());

        /**
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
        HttpResponse response = this.processHttpTwinRequest(url, HttpMethod.GET, new byte[0], String.valueOf(requestId++));

        String twin = new String(response.getBody(), StandardCharsets.UTF_8);

        /**
        **Codes_SRS_DEVICETWIN_25_011: [** The function shall deserialize the payload by calling updateTwin Api on the twin object **]**
         */
        device.getTwinObject().updateTwin(twin);

        /**
        **Codes_SRS_DEVICETWIN_25_012: [** The function shall set tags, desired property map, reported property map on the user device **]**
         */
        device.setTags(device.getTwinObject().getTagsMap());
        device.setDesiredProperties(device.getTwinObject().getDesiredPropertyMap());
        device.setReportedProperties(device.getTwinObject().getReportedPropertyMap());
    }

    /**
     * This method updates device twin for the specified device.
     *
     * @param device The device with a valid id for which device twin is to be updated.
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public void updateTwin(DeviceTwinDevice device) throws IotHubException, IOException
    {
        if (device == null || device.getDeviceId() == null || device.getDeviceId().length() == 0)
        {
            /**
            **Codes_SRS_DEVICETWIN_25_013: [** The function shall throw IllegalArgumentException if the input device is null or if deviceId is null or empty **]**
             */
            throw new IllegalArgumentException("Instantiate a device and set device id to be used");
        }

        if (device.getDesiredMap() == null && device.getTagsMap() == null)
        {
            throw new IllegalArgumentException("Set either desired properties or tags for the device to be updated with");
        }

        /**
        **Codes_SRS_DEVICETWIN_25_014: [** The function shall build the URL for this operation by calling getUrlTwin **]**
         */
        URL url = this.iotHubConnectionString.getUrlTwin(device.getDeviceId());

        /**
        **Codes_SRS_DEVICETWIN_25_015: [** The function shall serialize the twin map by calling updateTwin Api on the twin object for the device provided by the user**]**
         */
        String twinJson = device.getTwinObject().updateTwin(device.getDesiredMap(), null, device.getTagsMap());

        if (twinJson == null)
        {
            return;
        }

        throw new NotImplementedException();
        /**
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
        /**
         * * To be added to the code once service supports Patch via X-HTTP-Method
        HttpResponse response = this.processHttpTwinRequest(url, HttpMethod.PATCH, twinJson.getBytes(), String.valueOf(requestId++));
        */
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
            /**
            **Codes_SRS_DEVICETWIN_25_021: [** The function shall throw IllegalArgumentException if the input device is null or if deviceId is null or empty **]**
             */
            throw new IllegalArgumentException("Instantiate a device and set device id to be used");
        }

        if (device.getDesiredMap() == null)
        {
            throw new IllegalArgumentException("Set desired properties for the device to be updated with");
        }

        /**
        **Codes_SRS_DEVICETWIN_25_022: [** The function shall build the URL for this operation by calling getUrlTwinDesired **]**
         */
        URL url = this.iotHubConnectionString.getUrlTwinDesired(device.getDeviceId());

        /**
        **Codes_SRS_DEVICETWIN_25_023: [** The function shall serialize the desired properties map by calling updateDesiredProperty Api on the twin object for the device provided by the user**]**
         */
        String desiredJson = device.getTwinObject().updateDesiredProperty(device.getDesiredMap());

        if (desiredJson == null)
        {
            return;
        }

        throw new NotImplementedException();
        /**
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
        /**
         * To be added to the code once service supports Patch via X-HTTP-Method
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
            /***
            **Codes_SRS_DEVICETWIN_25_029: [** The function shall throw IllegalArgumentException if the input device is null or if deviceId is null or empty **]**
             */
            throw new IllegalArgumentException("Instantiate a device and set device id to be used");
        }

        if (device.getDesiredMap() == null)
        {
            throw new IllegalArgumentException("Set desired properties fort he device to be replaced with");
        }

        /***
        **Codes_SRS_DEVICETWIN_25_030: [** The function shall build the URL for this operation by calling getUrlTwinDesired **]**
         */
        URL url = this.iotHubConnectionString.getUrlTwinDesired(device.getDeviceId());

        /***
        **Codes_SRS_DEVICETWIN_25_031: [** The function shall serialize the desired properties map by calling resetDesiredProperty Api on the twin object for the device provided by the user**]**
         */
        String tags = device.getTwinObject().resetDesiredProperty(device.getDesiredMap());

        if (tags == null || tags.length() == 0)
        {
            /**
             *Codes_SRS_DEVICETWIN_25_045: [** If resetDesiredProperty call returns null or empty string then this method shall throw IOException**]**
             */
            throw new IOException("Serializer cannot return null or empty string");

        }

        throw new NotImplementedException();

        /***
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
        // To Do : uncomment when implemented for service client
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
            /**
             * Codes_SRS_DEVICETWIN_25_037: [** The function shall throw IllegalArgumentException if the input device is null or if deviceId is null or empty **]**
             */
            throw new IllegalArgumentException("Instantiate a device and set device id to be used");
        }

        if (device.getTagsMap() == null)
        {
            throw new IllegalArgumentException("Set tags to be replaced with");
        }

        /**
        **Codes_SRS_DEVICETWIN_25_038: [** The function shall build the URL for this operation by calling getUrlTwinTags **]**
         */
        URL url = this.iotHubConnectionString.getUrlTwinTags(device.getDeviceId());

        /**
        **Codes_SRS_DEVICETWIN_25_039: [** The function shall serialize the tags map by calling resetTags Api on the twin object for the device provided by the user**]**
         */
        String tags = device.getTwinObject().resetTags(device.getTagsMap());

        if (tags == null || tags.length() == 0)
        {
            /**
             **Codes_SRS_DEVICETWIN_25_046: [** If resetTags call returns null or empty string then this method shall throw IOException**]**
             */
            throw new IOException("Serializer cannot return null or empty");
        }

        /**
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
        // uncomment when implemented for service client
        // HttpResponse response = this.processHttpTwinRequest(url, HttpMethod.PUT, tags.getBytes(), String.valueOf(requestId++));

    }

}
