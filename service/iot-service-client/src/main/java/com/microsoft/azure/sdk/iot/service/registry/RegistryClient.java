/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.registry;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.auth.TokenCredentialCache;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubExceptionManager;
import com.microsoft.azure.sdk.iot.service.registry.serializers.RegistryIdentityParser;
import com.microsoft.azure.sdk.iot.service.registry.serializers.RegistryStatisticsParser;
import com.microsoft.azure.sdk.iot.service.transport.TransportUtils;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;
import com.microsoft.azure.sdk.iot.service.twin.TwinClient;
import lombok.extern.slf4j.Slf4j;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Use the RegistryClient to manage the identity registry in IoT hubs.
 * To access twins, use the {@link TwinClient}.
 */
@Slf4j
public final class RegistryClient
{
    private final String hostName;
    private TokenCredentialCache credentialCache;
    private AzureSasCredential azureSasCredential;
    private IotHubConnectionString iotHubConnectionString;

    private final RegistryClientOptions options;

    /**
     * Constructor to create instance from connection string
     *
     * @param connectionString The iot hub connection string
     */
    public RegistryClient(String connectionString)
    {
        this(connectionString, RegistryClientOptions.builder().build());
    }

    /**
     * Constructor to create instance from connection string
     *
     * @param connectionString The iot hub connection string
     * @param options The connection options to use when connecting to the service.
     */
    public RegistryClient(String connectionString, RegistryClientOptions options)
    {
        if (connectionString == null || connectionString.isEmpty())
        {
            throw new IllegalArgumentException("The provided connection string cannot be null or empty");
        }

        if (options == null)
        {
            throw new IllegalArgumentException("RegistryClientOptions cannot be null for this constructor");
        }

        this.iotHubConnectionString =
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);

        this.hostName = iotHubConnectionString.getHostName();
        this.options = options;
        commonConstructorSetup();
    }

    /**
     * Create a new RegistryClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed. The provided tokens must be Json Web Tokens.
     */
    public RegistryClient(String hostName, TokenCredential credential)
    {
        this(hostName, credential, RegistryClientOptions.builder().build());
    }

    /**
     * Create a new RegistryClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed. The provided tokens must be Json Web Tokens.
     * @param options The connection options to use when connecting to the service.
     */
    public RegistryClient(String hostName, TokenCredential credential, RegistryClientOptions options)
    {
        Objects.requireNonNull(credential, "credential cannot be null");
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
     * Create a new RegistryClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     */
    public RegistryClient(String hostName, AzureSasCredential azureSasCredential)
    {
        this(hostName, azureSasCredential, RegistryClientOptions.builder().build());
    }

    /**
     * Create a new RegistryClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     * @param options The connection options to use when connecting to the service.
     */
    public RegistryClient(String hostName, AzureSasCredential azureSasCredential, RegistryClientOptions options)
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
        log.debug("Initialized a RegistryClient instance client using SDK version {}", TransportUtils.serviceVersion);
    }

    /**
     * Add device using the given Device object
     * Return with the response device object from IotHub
     *
     * @param device The device object to add
     * @return The future object for the requested operation
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public Device addDevice(Device device) throws IOException, IotHubException
    {
        if (device == null)
        {
            throw new IllegalArgumentException("device cannot be null");
        }

        String deviceJson = device.toRegistryIdentityParser().toJson();

        URL url = IotHubConnectionString.getUrlDevice(this.hostName, device.getDeviceId());
        HttpRequest request = createRequest(url, HttpMethod.PUT, deviceJson.getBytes(StandardCharsets.UTF_8));

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);

        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);

        return new Device(new RegistryIdentityParser(bodyStr));
    }

    /**
     * Get device data by device Id from IotHub
     *
     * @param deviceId The id of requested device
     * @return The device object of requested device
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public Device getDevice(String deviceId) throws IOException, IotHubException
    {
        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("deviceId cannot be null or empty");
        }

        URL url = IotHubConnectionString.getUrlDevice(this.hostName, deviceId);

        HttpRequest request = createRequest(url, HttpMethod.GET, new byte[0]);

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);

        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);

        return new Device(new RegistryIdentityParser(bodyStr));
    }

    /**
     * Update device not forced
     *
     * @param device The device object containing updated data
     * @return The updated device object
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public Device updateDevice(Device device) throws IOException, IotHubException
    {
        if (device == null)
        {
            throw new IllegalArgumentException("device cannot be null");
        }

        URL url = IotHubConnectionString.getUrlDevice(this.hostName, device.getDeviceId());
        HttpRequest request = createRequest(url, HttpMethod.PUT, device.toRegistryIdentityParser().toJson().getBytes(StandardCharsets.UTF_8));

        request.setHeaderField("If-Match", "*");

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);

        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);

        return new Device(new RegistryIdentityParser(bodyStr));
    }

    /**
     * Remove device
     *
     * @param deviceId The device name to remove
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public void removeDevice(String deviceId) throws IOException, IotHubException
    {
        removeDeviceOperation(deviceId, "*");
    }

    /**
     * Remove device
     *
     * @param device The device name to remove
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public void removeDevice(Device device) throws IOException, IotHubException
    {
        if (device == null)
        {
            throw new IllegalArgumentException("device cannot be null or empty");
        }

        removeDeviceOperation(device.getDeviceId(), device.getETag());
    }

    /**
     * send remove device request and verify response
     *
     * @param deviceId The device name to remove
     * @param etag The etag associated with the device to remove
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    private void removeDeviceOperation(String deviceId, String etag) throws IOException, IotHubException
    {
        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("deviceId cannot be null or empty");
        }

        if (etag == null || etag.isEmpty())
        {
            throw new IllegalArgumentException("etag cannot be null or empty");
        }

        URL url = IotHubConnectionString.getUrlDevice(this.hostName, deviceId);

        HttpRequest request = createRequest(url, HttpMethod.DELETE, new byte[0]);
        request.setHeaderField("If-Match", etag);

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);
    }

    /**
     * Get device statistics
     *
     * @return RegistryStatistics object containing the requested data
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public RegistryStatistics getStatistics() throws IOException, IotHubException
    {
        URL url = IotHubConnectionString.getUrlDeviceStatistics(this.hostName);

        HttpRequest request = createRequest(url, HttpMethod.GET, new byte[0]);

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);

        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);
        return new RegistryStatistics(new RegistryStatisticsParser(bodyStr));
    }

    /**
     * Add module using the given Module object
     * Return with the response module object from IotHub
     *
     * @param module The module object to add
     * @return The module object for the requested operation
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public Module addModule(Module module) throws IOException, IotHubException
    {
        if (module == null)
        {
            throw new IllegalArgumentException("module cannot be null");
        }

        String moduleJson = module.toRegistryIdentityParser().toJson();

        URL url = IotHubConnectionString.getUrlModule(this.hostName, module.getDeviceId(), module.getId());

        HttpRequest request = createRequest(url, HttpMethod.PUT, moduleJson.getBytes(StandardCharsets.UTF_8));

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);

        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);

        return new Module(new RegistryIdentityParser(bodyStr));
    }

    /**
     * Get module data by device Id and module Id from IotHub
     *
     * @param deviceId The id of requested device
     * @param moduleId The id of requested module
     * @return The module object of requested module on the specific device
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public Module getModule(String deviceId, String moduleId) throws IOException, IotHubException
    {
        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("deviceId cannot be null or empty");
        }

        if (moduleId == null || moduleId.isEmpty())
        {
            throw new IllegalArgumentException("moduleId cannot be null or empty");
        }

        URL url = IotHubConnectionString.getUrlModule(this.hostName, deviceId, moduleId);

        HttpRequest request = createRequest(url, HttpMethod.GET, new byte[0]);

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);

        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);

        return new Module(new RegistryIdentityParser(bodyStr));
    }

    /**
     * Get modules data by device Id from IotHub
     *
     * @param deviceId The id of requested device
     * @return The module objects on the specific device
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public List<Module> getModulesOnDevice(String deviceId) throws IOException, IotHubException
    {
        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("deviceId cannot be null or empty");
        }

        URL url = IotHubConnectionString.getUrlModulesOnDevice(this.hostName, deviceId);

        HttpRequest request = createRequest(url, HttpMethod.GET, new byte[0]);

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);

        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);
        try (JsonReader jsonReader = Json.createReader(new StringReader(bodyStr)))
        {
            List<Module> moduleList = new ArrayList<>();
            JsonArray deviceArray = jsonReader.readArray();

            for (int i = 0; i < deviceArray.size(); i++)
            {
                JsonObject jsonObject = deviceArray.getJsonObject(i);
                Module iotHubModule = new Module(new RegistryIdentityParser(jsonObject.toString()));
                moduleList.add(iotHubModule);
            }
            return moduleList;
        }
    }

    /**
     * Update module not forced
     *
     * @param module The module object containing updated data
     * @return The updated module object
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public Module updateModule(Module module) throws IOException, IotHubException
    {
        if (module == null)
        {
            throw new IllegalArgumentException("module cannot be null");
        }

        URL url = IotHubConnectionString.getUrlModule(this.hostName, module.getDeviceId(), module.getId());

        HttpRequest request = createRequest(url, HttpMethod.PUT, module.toRegistryIdentityParser().toJson().getBytes(StandardCharsets.UTF_8));
        request.setHeaderField("If-Match", "*");

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);

        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);

        return new Module(new RegistryIdentityParser(bodyStr));
    }

    /**
     * Remove module
     *
     * @param deviceId The device name associated with the module to be removed
     * @param moduleId The module name to be removed
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public void removeModule(String deviceId, String moduleId) throws IOException, IotHubException
    {
        removeModuleOperation(deviceId, moduleId, "*");
    }

    /**
     * Remove module
     *
     * @param module The module to be removed
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public void removeModule(Module module) throws IOException, IotHubException
    {
        if (module == null)
        {
            throw new IllegalArgumentException("module cannot be null or empty");
        }

        removeModuleOperation(module.getDeviceId(), module.getId(), module.getETag());
    }

    /**
     * Send remove module request and verify response
     *
     * @param deviceId The device name associated with the module to be removed
     * @param moduleId The module name to be removed
     * @param etag The etag of the module to be removed, "*" as wildcard.
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    private void removeModuleOperation(String deviceId, String moduleId, String etag)
            throws IOException, IotHubException
    {
        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("deviceId cannot be null or empty");
        }

        if (moduleId == null || moduleId.isEmpty())
        {
            throw new IllegalArgumentException("moduleId cannot be null or empty");
        }

        if (etag == null || etag.isEmpty())
        {
            throw new IllegalArgumentException("etag cannot be null or empty");
        }

        URL url = IotHubConnectionString.getUrlModule(this.hostName, deviceId, moduleId);

        HttpRequest request = createRequest(url, HttpMethod.DELETE, new byte[0]);
        request.setHeaderField("If-Match", etag);

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);
    }

    private HttpRequest createRequest(URL url, HttpMethod method, byte[] payload) throws IOException
    {
        Proxy proxy = null;
        if (this.options.getProxyOptions() != null)
        {
            proxy = this.options.getProxyOptions().getProxy();
        }

        HttpRequest request = new HttpRequest(url, method, payload, getAuthenticationToken(), proxy);
        request.setReadTimeoutMillis(options.getHttpReadTimeout());
        request.setConnectTimeoutMillis(options.getHttpConnectTimeout());
        return request;
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