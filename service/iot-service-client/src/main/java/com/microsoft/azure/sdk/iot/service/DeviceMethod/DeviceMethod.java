// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.DeviceMethod;

import com.microsoft.azure.sdk.iot.deps.serializer.Method;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubExceptionManager;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * DeviceMethod enables service client to directly invoke methods from various devices.
 */
public class DeviceMethod
{
    private IotHubConnectionString iotHubConnectionString = null;
    private Integer requestId = 0;

    /**
     *  Values for Http header
     */

    private static final String AUTHORIZATION = "authorization";
    private static final String REQUEST_ID = "Request-Id";
    private final Integer DEFAULT_HTTP_TIMEOUT_MS = 24000;

    /**
     * Create a DeviceMethod instance from the information in the connection string.
     *
     * @param connectionString is a Azure IoTHub connection string.
     * @return an instance of the DeviceMethod.
     * @throws Exception This exception is thrown if the object creation failed
     */
    public static DeviceMethod createFromConnectionString(String connectionString) throws Exception
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
     * @param timeout is the maximum waiting time for a response from the device.
     * @param payload is the the method parameter
     * @return the returned values from the method in the device.
     * @throws IotHubException This exception is thrown if the response verification failed
     * @throws IOException This exception is thrown if the IO operation failed
     */
    public Object invoke(String deviceId, String methodName, Long timeout, Object payload) throws IotHubException, IOException
    {
        /* Codes_SRS_DEVICEMETHOD_21_004: [The invoke shall throw IllegalArgumentException if the provided deviceId is null or empty.] */
        if((deviceId == null) || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("deviceId is empty or null.");
        }

        /* Codes_SRS_DEVICEMETHOD_21_018: [The invoke shall bypass the Exception if one of the functions called by invoke failed.] */
        /* Codes_SRS_DEVICEMETHOD_21_005: [The invoke shall throw IllegalArgumentException if the provided methodName is null, empty, or not valid.] */
        /* Codes_SRS_DEVICEMETHOD_21_006: [The invoke shall throw IllegalArgumentException if the provided timeout is negative.] */
        Method method = new Method(methodName, timeout, payload);

        /* Codes_SRS_DEVICEMETHOD_21_013: [The invoke shall add a HTTP body with Json created by the `serializer.Method`.] */
        String json = method.toJson();

        /* Codes_SRS_DEVICEMETHOD_21_007: [The invoke shall build the Method URL `{iot hub}/twins/{device id}/methods/`.] */
        URL url = this.iotHubConnectionString.getUrlMethod(deviceId);

        /* Codes_SRS_DEVICEMETHOD_21_009: [The invoke shall create a new HttpRequest with http method as `POST`.] */
        HttpRequest request = new HttpRequest(url, HttpMethod.POST, json.getBytes(StandardCharsets.UTF_8));

        /* Codes_SRS_DEVICEMETHOD_21_008: [The invoke shall create a new SASToken with the ServiceConnect rights.] */
        String sasTokenString = new IotHubServiceSasToken(this.iotHubConnectionString).toString();

        /* Codes_SRS_DEVICEMETHOD_21_010: [The invoke shall add to the HTTP header an default timeout in milliseconds.] */
        request.setReadTimeoutMillis(DEFAULT_HTTP_TIMEOUT_MS);

        /* Codes_SRS_DEVICEMETHOD_21_011: [The invoke shall add to the HTTP header an `authorization` key with the SASToken.] */
        request.setHeaderField(AUTHORIZATION, sasTokenString);

        /* Codes_SRS_DEVICEMETHOD_21_012: [The invoke shall add to the HTTP header a `request-id` key with a new unique string value for every request.] */
        request.setHeaderField(REQUEST_ID, String.valueOf(requestId++));

        /* Codes_SRS_DEVICEMETHOD_21_014: [The invoke shall send the created request and get the response.] */
        HttpResponse response = request.send();

        /* Codes_SRS_DEVICEMETHOD_21_015: [The invoke shall deserialize the payload using the `serializer.Method`.] */
        /* Codes_SRS_DEVICEMETHOD_21_016: [If the resulted status represents fail, the invoke shall throw proper Exception.] */
        IotHubExceptionManager.httpResponseVerification(response);
        Method methodResponse = new Method(response.getBody());

        /* Codes_SRS_DEVICEMETHOD_21_017: [If the resulted status represents success, the invoke shall return the result payload.] */
        return methodResponse.getPayload();
    }

}
