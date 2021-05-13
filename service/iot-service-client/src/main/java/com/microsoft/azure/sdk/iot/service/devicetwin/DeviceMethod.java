// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.microsoft.azure.sdk.iot.deps.serializer.MethodParser;
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
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;

/**
 * Use the DeviceMethod client to directly invoke methods on devices and modules in IoT hub.
 */
public class DeviceMethod
{
    private Integer requestId = 0;

    private DeviceMethodClientOptions options;
    private String hostName;
    private TokenCredentialCache credentialCache;
    private AzureSasCredential azureSasCredential;
    private IotHubConnectionString iotHubConnectionString;

    /**
     * Create a DeviceMethod instance from the information in the connection string.
     *
     * @param connectionString is the IoTHub connection string.
     * @return an instance of the DeviceMethod.
     * @throws IOException This exception is never thrown.
     * @deprecated because this method declares a thrown IOException even though it never throws an IOException. Users
     * are recommended to use {@link #DeviceMethod(String)} instead
     * since it does not declare this exception even though it constructs the same DeviceMethod.
     */
    @Deprecated
    public static DeviceMethod createFromConnectionString(String connectionString) throws IOException
    {
        return createFromConnectionString(
            connectionString,
            DeviceMethodClientOptions.builder()
                .httpConnectTimeout(DeviceMethodClientOptions.DEFAULT_HTTP_CONNECT_TIMEOUT_MS)
                .httpReadTimeout(DeviceMethodClientOptions.DEFAULT_HTTP_READ_TIMEOUT_MS)
                .build());
    }

    /**
     * Create a DeviceMethod instance from the information in the connection string.
     *
     * @param connectionString is the IoTHub connection string.
     * @param options the configurable options for each operation on this client. May not be null.
     * @return an instance of the DeviceMethod.
     * @throws IOException This exception is never thrown.
     * @deprecated because this method declares a thrown IOException even though it never throws an IOException. Users
     * are recommended to use {@link #DeviceMethod(String, DeviceMethodClientOptions)} instead
     * since it does not declare this exception even though it constructs the same DeviceMethod.
     */
    @Deprecated
    public static DeviceMethod createFromConnectionString(
        String connectionString,
        DeviceMethodClientOptions options) throws IOException
    {
        if (connectionString == null || connectionString.length() == 0)
        {
            throw new IllegalArgumentException("Connection string cannot be null or empty");
        }

        if (options == null)
        {
            throw new IllegalArgumentException("options may not be null");
        }

        return new DeviceMethod(connectionString, options);
    }

    /**
     * Create a DeviceMethod instance from the information in the connection string.
     *
     * @param connectionString is the IoTHub connection string.
     */
    public DeviceMethod(String connectionString)
    {
        this(connectionString,
            DeviceMethodClientOptions.builder()
                .httpConnectTimeout(DeviceMethodClientOptions.DEFAULT_HTTP_CONNECT_TIMEOUT_MS)
                .httpReadTimeout(DeviceMethodClientOptions.DEFAULT_HTTP_READ_TIMEOUT_MS)
                .build());
    }

    /**
     * Create a DeviceMethod instance from the information in the connection string.
     *
     * @param connectionString is the IoTHub connection string.
     * @param options the configurable options for each operation on this client. May not be null.
     */
    public DeviceMethod(String connectionString, DeviceMethodClientOptions options)
    {
        Objects.requireNonNull(options);
        if (Tools.isNullOrEmpty(connectionString))
        {
            throw new IllegalArgumentException("Connection string cannot be null or empty");
        }

        this.hostName = IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString).getHostName();
        this.options = options;
        this.iotHubConnectionString = IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
    }

    /**
     * Create a new DeviceMethod instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     * this library when they are needed. The provided tokens must be Json Web Tokens.
     */
    public DeviceMethod(String hostName, TokenCredential credential)
    {
        this(hostName, credential, DeviceMethodClientOptions.builder().build());
    }

    /**
     * Create a new DeviceMethod instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     * this library when they are needed. The provided tokens must be Json Web Tokens.
     * @param options The connection options to use when connecting to the service.
     */
    public DeviceMethod(String hostName, TokenCredential credential, DeviceMethodClientOptions options)
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
     * Create a new DeviceMethod instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     */
    public DeviceMethod(String hostName, AzureSasCredential azureSasCredential)
    {
        this(hostName, azureSasCredential, DeviceMethodClientOptions.builder().build());
    }

    /**
     * Create a new DeviceMethod instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     * @param options The connection options to use when connecting to the service.
     */
    public DeviceMethod(String hostName, AzureSasCredential azureSasCredential, DeviceMethodClientOptions options)
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
     * Directly invokes a method on the device and return its result.
     *
     * @param deviceId is the device where the request is send to.
     * @param methodName is the name of the method that shall be invoked on the device.
     * @param responseTimeoutInSeconds is the maximum waiting time for a response from the device in seconds.
     * @param connectTimeoutInSeconds is the maximum waiting time for a response from the connection in seconds.
     * @param payload is the the method parameter.
     * @return the status and payload resulted from the method invoke.
     * @throws IotHubException This exception is thrown if the response verification failed.
     * @throws IOException This exception is thrown if the IO operation failed.
     */
    public synchronized MethodResult invoke(
        String deviceId,
        String methodName,
        Long responseTimeoutInSeconds,
        Long connectTimeoutInSeconds,
        Object payload) throws IotHubException, IOException
    {
        if (Tools.isNullOrEmpty(deviceId))
        {
            throw new IllegalArgumentException("deviceId is empty or null.");
        }

        if (Tools.isNullOrEmpty(methodName))
        {
            throw new IllegalArgumentException("methodName is empty or null.");
        }

        URL url = IotHubConnectionString.getUrlMethod(this.hostName, deviceId);
        return invokeMethod(url, methodName, responseTimeoutInSeconds, connectTimeoutInSeconds, payload);
    }

    /**
     * Directly invokes a method on the module and return its result.
     *
     * @param deviceId is the device where the module is related to.
     * @param moduleId is the module where the request is sent to.
     * @param methodName is the name of the method that shall be invoked on the device.
     * @param responseTimeoutInSeconds is the maximum waiting time for a response from the device in seconds.
     * @param connectTimeoutInSeconds is the maximum waiting time for a response from the connection in seconds.
     * @param payload is the the method parameter.
     * @return the status and payload resulted from the method invoke.
     * @throws IotHubException This exception is thrown if the response verification failed.
     * @throws IOException This exception is thrown if the IO operation failed.
     */
    public synchronized MethodResult invoke(
        String deviceId,
        String moduleId,
        String methodName,
        Long responseTimeoutInSeconds,
        Long connectTimeoutInSeconds,
        Object payload) throws IotHubException, IOException
    {
        if (Tools.isNullOrEmpty(deviceId))
        {
            throw new IllegalArgumentException("deviceId is empty or null.");
        }

        if (Tools.isNullOrEmpty(moduleId))
        {
            throw new IllegalArgumentException("moduleId is empty or null.");
        }

        if (Tools.isNullOrEmpty(methodName))
        {
            throw new IllegalArgumentException("methodName is empty or null.");
        }

        URL url = IotHubConnectionString.getUrlModuleMethod(this.hostName, deviceId, moduleId);

        return invokeMethod(url, methodName, responseTimeoutInSeconds, connectTimeoutInSeconds, payload);
    }

    /**
     * Directly invokes a method on the device and return its result.
     *
     * @param url is the path where the request is send to.
     * @param methodName is the name of the method that shall be invoked on the device.
     * @param responseTimeoutInSeconds is the maximum waiting time for a response from the device in seconds.
     * @param connectTimeoutInSeconds is the maximum waiting time for a response from the connection in seconds.
     * @param payload is the the method parameter.
     * @return the status and payload resulted from the method invoke.
     * @throws IotHubException This exception is thrown if the response verification failed.
     * @throws IOException This exception is thrown if the IO operation failed.
     */
    private synchronized MethodResult invokeMethod(
        URL url,
        String methodName,
        Long responseTimeoutInSeconds,
        Long connectTimeoutInSeconds,
        Object payload) throws IotHubException, IOException
    {
        MethodParser methodParser = new MethodParser(methodName, responseTimeoutInSeconds, connectTimeoutInSeconds, payload);

        String json = methodParser.toJson();
        if (json == null)
        {
            throw new IllegalArgumentException("MethodParser return null Json");
        }

        ProxyOptions proxyOptions = options.getProxyOptions();
        Proxy proxy = proxyOptions != null ? proxyOptions.getProxy() : null;
        HttpResponse response = DeviceOperations.request(
            this.getAuthenticationToken(),
            url,
            HttpMethod.POST,
            json.getBytes(StandardCharsets.UTF_8),
            String.valueOf(requestId++),
            options.getHttpConnectTimeout(),
            options.getHttpReadTimeout(),
            proxy);

        MethodParser methodParserResponse = new MethodParser();
        methodParserResponse.fromJson(new String(response.getBody(), StandardCharsets.UTF_8));

        return new MethodResult(methodParserResponse.getStatus(), methodParserResponse.getPayload());
    }

    /**
     * Creates a new Job to invoke method on one or multiple devices.
     *
     * @param queryCondition Query condition to evaluate which devices to run the job on. It can be {@code null} or empty.
     * @param methodName Method name to be invoked.
     * @param responseTimeoutInSeconds Maximum interval of time, in seconds, that the Direct Method will wait for answer. It can be {@code null}.
     * @param connectTimeoutInSeconds Maximum interval of time, in seconds, that the Direct Method will wait for the connection. It can be {@code null}.
     * @param payload Object that contains the payload defined by the user. It can be {@code null}.
     * @param startTimeUtc Date time in Utc to start the job.
     * @param maxExecutionTimeInSeconds Max execution time in seconds, i.e., ttl duration the job can run.
     * @return a Job class that represent this job on IotHub.
     * @throws IOException if the function contains invalid parameters.
     * @throws IotHubException if the http request failed.
     */
    public Job scheduleDeviceMethod(
        String queryCondition,
        String methodName,
        Long responseTimeoutInSeconds,
        Long connectTimeoutInSeconds,
        Object payload,
        Date startTimeUtc,
        long maxExecutionTimeInSeconds) throws IOException, IotHubException
    {
        if (Tools.isNullOrEmpty(methodName))
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

        job.scheduleDeviceMethod(
            queryCondition,
            methodName, responseTimeoutInSeconds, connectTimeoutInSeconds, payload,
            startTimeUtc, maxExecutionTimeInSeconds);

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
