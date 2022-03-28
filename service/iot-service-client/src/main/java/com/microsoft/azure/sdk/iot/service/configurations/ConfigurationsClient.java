// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.configurations;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.google.gson.Gson;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.auth.TokenCredentialCache;
import com.microsoft.azure.sdk.iot.service.configurations.serializers.ConfigurationParser;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.TransportUtils;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The client for creating, updating, getting and deleting configurations.
 * @see <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-configuration-best-practices">
 *     See this document for more information on automatic device configuration</a>
 */
@Slf4j
public class ConfigurationsClient
{
    private final String hostName;
    private TokenCredentialCache credentialCache;
    private AzureSasCredential azureSasCredential;
    private IotHubConnectionString iotHubConnectionString;

    private final ConfigurationsClientOptions options;

    /**
     * Constructor to create instance from connection string
     *
     * @param connectionString The iot hub connection string
     */
    public ConfigurationsClient(String connectionString)
    {
        this(connectionString, ConfigurationsClientOptions.builder().build());
    }

    /**
     * Constructor to create instance from connection string
     *
     * @param connectionString The iot hub connection string
     * @param options The connection options to use when connecting to the service.
     */
    public ConfigurationsClient(String connectionString, ConfigurationsClientOptions options)
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
     * Create a new ConfigurationsClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed. The provided tokens must be Json Web Tokens.
     */
    public ConfigurationsClient(String hostName, TokenCredential credential)
    {
        this(hostName, credential, ConfigurationsClientOptions.builder().build());
    }

    /**
     * Create a new ConfigurationsClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed. The provided tokens must be Json Web Tokens.
     * @param options The connection options to use when connecting to the service.
     */
    public ConfigurationsClient(String hostName, TokenCredential credential, ConfigurationsClientOptions options)
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
     * Create a new ConfigurationsClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     */
    public ConfigurationsClient(String hostName, AzureSasCredential azureSasCredential)
    {
        this(hostName, azureSasCredential, ConfigurationsClientOptions.builder().build());
    }

    /**
     * Create a new ConfigurationsClient instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     * @param options The connection options to use when connecting to the service.
     */
    public ConfigurationsClient(String hostName, AzureSasCredential azureSasCredential, ConfigurationsClientOptions options)
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
        log.debug("Initialized a ConfigurationsClient instance using SDK version {}", TransportUtils.serviceVersion);
    }

    /**
     * Create a new configuration using the given Configuration object
     * Return with the response configuration object from IotHub
     *
     * @param configuration The configuration object to create
     * @return The configuration object for the requested operation
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public Configuration create(Configuration configuration) throws IOException, IotHubException
    {
        if (configuration == null)
        {
            throw new IllegalArgumentException("configuration cannot be null");
        }

        String configurationJson = configuration.toConfigurationParser().toJson();

        URL url = IotHubConnectionString.getUrlConfiguration(this.hostName, configuration.getId());

        HttpRequest request = createRequest(url, HttpMethod.PUT, configurationJson.getBytes(StandardCharsets.UTF_8));

        HttpResponse response = request.send();

        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);

        return new Configuration(new ConfigurationParser(bodyStr));
    }

    /**
     * Get configuration by configuration Id from IotHub
     *
     * @param configurationId The id of requested configuration
     * @return The configuration object of requested configuration on the specific device
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public Configuration get(String configurationId) throws IOException, IotHubException
    {
        if (configurationId == null || configurationId.isEmpty())
        {
            throw new IllegalArgumentException("configurationId cannot be null or empty");
        }

        URL url = IotHubConnectionString.getUrlConfiguration(this.hostName, configurationId);

        HttpRequest request = createRequest(url, HttpMethod.GET, new byte[0]);

        HttpResponse response = request.send();

        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);

        return new Configuration(new ConfigurationParser(bodyStr));
    }

    /**
     * Get list of Configuration
     *
     * @param maxCount The requested count of configurations
     * @return The array of requested configuration objects
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public List<Configuration> get(int maxCount) throws IOException, IotHubException
    {
        if (maxCount < 1)
        {
            throw new IllegalArgumentException("maxCount cannot be less then 1");
        }

        URL url = IotHubConnectionString.getUrlConfigurationsList(this.hostName, maxCount);

        HttpRequest request = createRequest(url, HttpMethod.GET, new byte[0]);

        HttpResponse response = request.send();

        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);
        List<Configuration> configurationList = new ArrayList<>();

        Gson gson = new Gson();
        ConfigurationParser[] configurationParsers = gson.fromJson(bodyStr, ConfigurationParser[].class);

        for (int i = 0; i < configurationParsers.length; i++)
        {
            configurationList.add(new Configuration(configurationParsers[i]));
        }

        return configurationList;
}

    /**
     * Update configuration not forced
     *
     * @param configuration The configuration object containing updated data
     * @return The updated configuration object
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public Configuration replace(Configuration configuration) throws IOException, IotHubException
    {
        if (configuration == null)
        {
            throw new IllegalArgumentException("configuration cannot be null");
        }

        URL url = IotHubConnectionString.getUrlConfiguration(this.hostName, configuration.getId());

        HttpRequest request = createRequest(url, HttpMethod.PUT, configuration.toConfigurationParser().toJson().getBytes(StandardCharsets.UTF_8));

        request.setHeaderField("If-Match", "*");

        HttpResponse response = request.send();

        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);

        return new Configuration(new ConfigurationParser(bodyStr));
    }

    /**
     * Send remove configuration request and verify response
     *
     * @param configurationId The configuration to be removed
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public void delete(String configurationId) throws IOException, IotHubException
    {
        removeConfigurationOperation(configurationId, "*");
    }

    /**
     * Send remove configuration request and verify response
     *
     * @param configuration The configuration to be removed
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public void delete(Configuration configuration) throws IOException, IotHubException
    {
        if (configuration == null)
        {
            throw new IllegalArgumentException("configuration cannot be null or empty");
        }

        removeConfigurationOperation(configuration.getId(), configuration.getEtag());
    }

    /**
     * Send remove configuration request and verify response
     *
     * @param configurationId The configuration to be removed
     * @param etag The etag of the configuration to be removed. "*" as wildcard
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    private void removeConfigurationOperation(String configurationId, String etag) throws IOException, IotHubException
    {
        if (configurationId == null || configurationId.isEmpty())
        {
            throw new IllegalArgumentException("configurationId cannot be null or empty");
        }

        if (etag == null || etag.isEmpty())
        {
            throw new IllegalArgumentException("etag cannot be null or empty");
        }

        URL url = IotHubConnectionString.getUrlConfiguration(this.hostName, configurationId);

        HttpRequest request = createRequest(url, HttpMethod.DELETE, new byte[0]);
        request.setHeaderField("If-Match", etag);

        request.send();
    }

    /**
     * Apply the provided configuration content to the provided device
     * @param deviceId The device to apply the configuration to
     * @param content The configuration content to apply to the device
     * @throws IOException If the iot hub cannot be reached
     * @throws IotHubException If the response from the hub was an error code. This exception will contain that code
     */
    public void applyConfigurationContentOnDevice(String deviceId, ConfigurationContent content) throws IOException, IotHubException
    {
        if (content == null)
        {
            throw new IllegalArgumentException("content cannot be null");
        }

        URL url = IotHubConnectionString.getUrlApplyConfigurationContent(this.hostName, deviceId);

        HttpRequest request = createRequest(url, HttpMethod.POST, content.toConfigurationContentParser().toJson().getBytes(StandardCharsets.UTF_8));

        request.send();
    }

    private HttpRequest createRequest(URL url, HttpMethod method, byte[] payload) throws IOException
    {
        Proxy proxy = null;
        if (this.options.getProxyOptions() != null)
        {
            proxy = this.options.getProxyOptions().getProxy();
        }

        HttpRequest request = new HttpRequest(url, method, payload, getAuthenticationToken(), proxy);
        request.setReadTimeoutSeconds(options.getHttpReadTimeoutSeconds());
        request.setConnectTimeoutSeconds(options.getHttpConnectTimeoutSeconds());
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
