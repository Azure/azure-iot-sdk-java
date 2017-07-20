// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.deps.serializer.MethodParser;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * DeviceMethod enables service client to directly invoke methods on various devices from service client.
 */
public class DeviceMethod
{
    private IotHubConnectionString iotHubConnectionString = null;
    private Integer requestId = 0;
    private static final int DEFAULT_RESPONSE_TIMEOUT = 30; // default response timeout is 30 seconds
    private static final int DEFAULT_CONNECT_TIMEOUT = 0;
    private static final int THOUSAND_MS = 1000;
    /**
     * Create a DeviceMethod instance from the information in the connection string.
     *
     * @param connectionString is the IoTHub connection string.
     * @return an instance of the DeviceMethod.
     * @throws IOException This exception is thrown if the object creation failed
     */
    public static DeviceMethod createFromConnectionString(String connectionString) throws IOException
    {
        if (connectionString == null || connectionString.length() == 0)
        {
            /* Codes_SRS_DEVICEMETHOD_21_001: [The constructor shall throw IllegalArgumentException if the input string is null or empty.] */
            throw new IllegalArgumentException("Connection string cannot be null or empty");
        }

        /* Codes_SRS_DEVICEMETHOD_21_003: [The constructor shall create a new DeviceMethod instance and return it.] */
        DeviceMethod deviceMethod = new DeviceMethod();

        /* Codes_SRS_DEVICEMETHOD_21_002: [The constructor shall create an IotHubConnectionStringBuilder object from the given connection string.] */
        deviceMethod.iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        return deviceMethod;
    }

    /**
     * Directly invokes a method on the device and return its result.
     *
     * @param deviceId is the device identification.
     * @param methodName is the name of the method that shall be invoked on the device.
     * @param responseTimeoutInSeconds is the maximum waiting time for a response from the device in seconds.
     * @param connectTimeoutInSeconds is the maximum waiting time for a response from the connection in seconds.
     * @param payload is the the method parameter
     * @return the status and payload resulted from the method invoke
     * @throws IotHubException This exception is thrown if the response verification failed
     * @throws IOException This exception is thrown if the IO operation failed
     */
    public synchronized MethodResult invoke(String deviceId, String methodName, Long responseTimeoutInSeconds, Long connectTimeoutInSeconds, Object payload) throws IotHubException, IOException
    {
        /* Codes_SRS_DEVICEMETHOD_21_004: [The invoke shall throw IllegalArgumentException if the provided deviceId is null or empty.] */
        if((deviceId == null) || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("deviceId is empty or null.");
        }

        /* Codes_SRS_DEVICEMETHOD_21_005: [The invoke shall throw IllegalArgumentException if the provided methodName is null, empty, or not valid.] */
        if((methodName == null) || methodName.isEmpty())
        {
            throw new IllegalArgumentException("methodName is empty or null.");
        }

        /* Codes_SRS_DEVICEMETHOD_21_006: [The invoke shall throw IllegalArgumentException if the provided responseTimeoutInSeconds is negative.] */
        /* Codes_SRS_DEVICEMETHOD_21_007: [The invoke shall throw IllegalArgumentException if the provided connectTimeoutInSeconds is negative.] */
        /* Codes_SRS_DEVICEMETHOD_21_014: [The invoke shall bypass the Exception if one of the functions called by invoke failed.] */
        MethodParser methodParser = new MethodParser(methodName, responseTimeoutInSeconds, connectTimeoutInSeconds, payload);

        /* Codes_SRS_DEVICEMETHOD_21_011: [The invoke shall add a HTTP body with Json created by the `serializer.MethodParser`.] */
        String json = methodParser.toJson();
        if(json == null)
        {
            /* Codes_SRS_DEVICEMETHOD_21_012: [If `MethodParser` return a null Json, the invoke shall throw IllegalArgumentException.] */
            throw new IllegalArgumentException("MethodParser return null Json");
        }

        /* Codes_SRS_DEVICEMETHOD_21_008: [The invoke shall build the Method URL `{iot hub}/twins/{device id}/methods/` by calling getUrlMethod.] */
        URL url = this.iotHubConnectionString.getUrlMethod(deviceId);
       
        long  responseTimeout, connectTimeout;
        
        if (responseTimeoutInSeconds == null)
        {
            responseTimeout  = DEFAULT_RESPONSE_TIMEOUT; // If timeout is not set, it defaults to 30 seconds
        }
        else
        {
            responseTimeout  = responseTimeoutInSeconds;
        }
        
        if (connectTimeoutInSeconds == null)
        {
            connectTimeout  = DEFAULT_CONNECT_TIMEOUT;
        }
        else
        {
            connectTimeout  = connectTimeoutInSeconds;
        }
        
        // Calculate total timeout in milliseconds
        long timeoutInMs = (responseTimeout + connectTimeout) * THOUSAND_MS; 
               
        /* Codes_SRS_DEVICEMETHOD_21_009: [The invoke shall send the created request and get the response using the HttpRequester.] */
        /* Codes_SRS_DEVICEMETHOD_21_010: [The invoke shall create a new HttpRequest with http method as `POST`.] */
        HttpResponse response = DeviceOperations.request(this.iotHubConnectionString, url, HttpMethod.POST, json.getBytes(StandardCharsets.UTF_8), String.valueOf(requestId++), timeoutInMs);

        /* Codes_SRS_DEVICEMETHOD_21_013: [The invoke shall deserialize the payload using the `serializer.MethodParser`.] */
        MethodParser methodParserResponse = new MethodParser();
        methodParserResponse.fromJson(new String(response.getBody(), StandardCharsets.UTF_8));

        /* Codes_SRS_DEVICEMETHOD_21_015: [If the HttpStatus represents success, the invoke shall return the status and payload using the `MethodResult` class.] */
        return new MethodResult(methodParserResponse.getStatus(), methodParserResponse.getPayload());
    }

    /**
     * Creates a new Job to invoke method on one or multiple devices
     *
     * @param queryCondition Query condition to evaluate which devices to run the job on. It can be {@code null} or empty
     * @param methodName Method name to be invoked
     * @param responseTimeoutInSeconds Maximum interval of time, in seconds, that the Direct Method will wait for answer. It can be {@code null}.
     * @param connectTimeoutInSeconds Maximum interval of time, in seconds, that the Direct Method will wait for the connection. It can be {@code null}.
     * @param payload Object that contains the payload defined by the user. It can be {@code null}.
     * @param startTimeUtc Date time in Utc to start the job
     * @param maxExecutionTimeInSeconds Max execution time in seconds, i.e., ttl duration the job can run
     * @return a Job class that represent this job on IotHub
     * @throws IOException if the function contains invalid parameters
     * @throws IotHubException if the http request failed
     */
    public Job scheduleDeviceMethod(String queryCondition,
                                    String methodName, Long responseTimeoutInSeconds, Long connectTimeoutInSeconds, Object payload,
                                    Date startTimeUtc, long maxExecutionTimeInSeconds)
            throws IOException, IotHubException
    {
        /* Codes_SRS_DEVICEMETHOD_21_016: [If the methodName is null or empty, the scheduleDeviceMethod shall throws IllegalArgumentException.] */
        if((methodName == null) || methodName.isEmpty())
        {
            throw new IllegalArgumentException("null updateTwin");
        }

        /* Codes_SRS_DEVICEMETHOD_21_017: [If the startTimeUtc is null, the scheduleDeviceMethod shall throws IllegalArgumentException.] */
        if(startTimeUtc == null)
        {
            throw new IllegalArgumentException("null startTimeUtc");
        }

        /* Codes_SRS_DEVICEMETHOD_21_018: [If the maxExecutionTimeInSeconds is negative, the scheduleDeviceMethod shall throws IllegalArgumentException.] */
        if(maxExecutionTimeInSeconds < 0)
        {
            throw new IllegalArgumentException("negative maxExecutionTimeInSeconds");
        }

        /* Codes_SRS_DEVICEMETHOD_21_019: [The scheduleDeviceMethod shall create a new instance of the Job class.] */
        /* Codes_SRS_DEVICEMETHOD_21_020: [If the scheduleDeviceMethod failed to create a new instance of the Job class, it shall throws IOException. Threw by the Jobs constructor.] */
        Job job = new Job(iotHubConnectionString.toString());

        /* Codes_SRS_DEVICEMETHOD_21_021: [The scheduleDeviceMethod shall invoke the scheduleDeviceMethod in the Job class with the received parameters.] */
        /* Codes_SRS_DEVICEMETHOD_21_022: [If scheduleDeviceMethod failed, the scheduleDeviceMethod shall throws IotHubException. Threw by the scheduleUpdateTwin.] */
        job.scheduleDeviceMethod(
                queryCondition,
                methodName, responseTimeoutInSeconds, connectTimeoutInSeconds, payload,
                startTimeUtc, maxExecutionTimeInSeconds);

        /* Codes_SRS_DEVICEMETHOD_21_023: [The scheduleDeviceMethod shall return the created instance of the Job class.] */
        return job;
    }
}
