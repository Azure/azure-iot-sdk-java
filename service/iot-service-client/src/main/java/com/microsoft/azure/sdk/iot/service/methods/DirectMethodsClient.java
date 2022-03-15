// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.methods;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.google.gson.GsonBuilder;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.auth.TokenCredentialCache;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.methods.serializers.MethodParser;
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

import static com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest.REQUEST_ID;

/**
 * The client to directly invoke direct methods on devices and modules in IoT hub.
 */
@Slf4j
public final class DirectMethodsClient
{
    private Integer requestId = 0;

    private final DirectMethodsClientOptions options;
    private final String hostName;
    private TokenCredentialCache credentialCache;
    private AzureSasCredential azureSasCredential;
    private IotHubConnectionString iotHubConnectionString;

    /**
     * Create a DirectMethodsClient instance from the information in the connection string.
     *
     * @param connectionString is the IoTHub connection string.
     */
    public DirectMethodsClient(String connectionString)
    {
        this(connectionString, DirectMethodsClientOptions.builder().build());
    }

    /**
     * Create a DirectMethodsClient instance from the information in the connection string.
     *
     * @param connectionString is the IoTHub connection string.
     * @param options the configurable options for each operation on this client. May not be null.
     */
    public DirectMethodsClient(String connectionString, DirectMethodsClientOptions options)
    {
        Objects.requireNonNull(options);
        if (connectionString == null || connectionString.isEmpty())
        {
            throw new IllegalArgumentException("Connection string cannot be null or empty");
        }

        this.hostName = IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString).getHostName();
        this.options = options;
        this.iotHubConnectionString = IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
        commonConstructorSetup();
    }

    /**
     * Create a new DirectMethodsClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     * this library when they are needed. The provided tokens must be Json Web Tokens.
     */
    public DirectMethodsClient(String hostName, TokenCredential credential)
    {
        this(hostName, credential, DirectMethodsClientOptions.builder().build());
    }

    /**
     * Create a new DirectMethodsClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     * this library when they are needed. The provided tokens must be Json Web Tokens.
     * @param options The connection options to use when connecting to the service.
     */
    public DirectMethodsClient(String hostName, TokenCredential credential, DirectMethodsClientOptions options)
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
     * Create a new DirectMethodsClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     */
    public DirectMethodsClient(String hostName, AzureSasCredential azureSasCredential)
    {
        this(hostName, azureSasCredential, DirectMethodsClientOptions.builder().build());
    }

    /**
     * Create a new DirectMethodsClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     * @param options The connection options to use when connecting to the service.
     */
    public DirectMethodsClient(String hostName, AzureSasCredential azureSasCredential, DirectMethodsClientOptions options)
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
        log.debug("Initialized a DirectMethodsClient instance using SDK version {}", TransportUtils.serviceVersion);
    }

    /**
     * Directly invokes a method on the device and return its result.
     *
     * @param deviceId is the device where the sendHttpRequest is send to.
     * @param methodName is the name of the method that shall be invoked on the device.
     * @return the status and payload resulted from the method invoke.
     * @throws IotHubException This exception is thrown if the response verification failed.
     * @throws IOException This exception is thrown if the IO operation failed.
     */
    public MethodResult invoke(String deviceId, String methodName) throws IotHubException, IOException
    {
        return invoke(deviceId, methodName, DirectMethodRequestOptions.builder().build());
    }

    /**
     * Directly invokes a method on the device and return its result.
     *
     * @param deviceId is the device where the sendHttpRequest is send to.
     * @param methodName is the name of the method that shall be invoked on the device.
     * @param options the optional parameters for this request, including the method's payload. May not be null.
     * @return the status and payload resulted from the method invoke.
     * @throws IotHubException This exception is thrown if the response verification failed.
     * @throws IOException This exception is thrown if the IO operation failed.
     */
    public MethodResult invoke(String deviceId, String methodName, DirectMethodRequestOptions options) throws IotHubException, IOException
    {
        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("deviceId is empty or null.");
        }

        if (methodName == null || methodName.isEmpty())
        {
            throw new IllegalArgumentException("methodName is empty or null.");
        }

        Objects.requireNonNull(options);

        URL url = IotHubConnectionString.getUrlMethod(this.hostName, deviceId);
        return invokeMethod(url, methodName, options);
    }

    /**
     * Directly invokes a method on the module and return its result.
     *
     * @param deviceId is the device where the module is related to.
     * @param moduleId is the module where the sendHttpRequest is sent to.
     * @param methodName is the name of the method that shall be invoked on the device.
     * @return the status and payload resulted from the method invoke.
     * @throws IotHubException This exception is thrown if the response verification failed.
     * @throws IOException This exception is thrown if the IO operation failed.
     */
    public MethodResult invoke(String deviceId, String moduleId, String methodName) throws IotHubException, IOException
    {
        return invoke(deviceId, moduleId, methodName, DirectMethodRequestOptions.builder().build());
    }

    /**
     * Directly invokes a method on the module and return its result.
     *
     * @param deviceId is the device where the module is related to.
     * @param moduleId is the module where the sendHttpRequest is sent to.
     * @param methodName is the name of the method that shall be invoked on the device.
     * @param options the optional parameters for this request, including the method's payload. May not be null.
     * @return the status and payload resulted from the method invoke.
     * @throws IotHubException This exception is thrown if the response verification failed.
     * @throws IOException This exception is thrown if the IO operation failed.
     */
    public MethodResult invoke(String deviceId, String moduleId, String methodName, DirectMethodRequestOptions options)
        throws IotHubException, IOException
    {
        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("deviceId is empty or null.");
        }

        if (moduleId == null || moduleId.isEmpty())
        {
            throw new IllegalArgumentException("moduleId is empty or null.");
        }

        if (methodName == null || methodName.isEmpty())
        {
            throw new IllegalArgumentException("methodName is empty or null.");
        }

        Objects.requireNonNull(options);

        URL url = IotHubConnectionString.getUrlModuleMethod(this.hostName, deviceId, moduleId);

        return invokeMethod(url, methodName, options);
    }

    private MethodResult invokeMethod(URL url, String methodName, DirectMethodRequestOptions options)
            throws IotHubException, IOException
    {
        MethodParser methodParser =
            new MethodParser(
                methodName,
                options.getMethodResponseTimeoutSeconds(),
                options.getMethodConnectTimeoutSeconds(),
                options.getPayload());

        String json = methodParser.toJson();
        if (json == null)
        {
            throw new IllegalArgumentException("MethodParser return null Json");
        }

        ProxyOptions proxyOptions = this.options.getProxyOptions();
        Proxy proxy = proxyOptions != null ? proxyOptions.getProxy() : null;
        HttpRequest httpRequest = new HttpRequest(
            url,
            HttpMethod.POST,
            json.getBytes(StandardCharsets.UTF_8),
            this.getAuthenticationToken(),
            proxy);

        httpRequest.setReadTimeoutSeconds(this.options.getHttpReadTimeoutSeconds());
        httpRequest.setConnectTimeoutSeconds(this.options.getHttpConnectTimeoutSeconds());
        httpRequest.setHeaderField(REQUEST_ID, String.valueOf(requestId++));

        HttpResponse response = httpRequest.send();

        MethodParser methodParserResponse = new MethodParser();
        methodParserResponse.fromJson(new String(response.getBody(), StandardCharsets.UTF_8));

        return new MethodResult(methodParserResponse.getStatus(), new GsonBuilder().create().toJsonTree(methodParserResponse.getPayload()));
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
