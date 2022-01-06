/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.google.gson.JsonSyntaxException;
import com.microsoft.azure.sdk.iot.deps.serializer.ConfigurationParser;
import com.microsoft.azure.sdk.iot.deps.serializer.DeviceParser;
import com.microsoft.azure.sdk.iot.deps.serializer.JobPropertiesParser;
import com.microsoft.azure.sdk.iot.deps.serializer.RegistryStatisticsParser;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.auth.TokenCredentialCache;
import com.microsoft.azure.sdk.iot.service.devicetwin.TwinClient;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubExceptionManager;
import com.microsoft.azure.sdk.iot.service.transport.TransportUtils;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;
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
 * Use the RegistryManager client to manage the identity registry in IoT hubs.
 * To access twins, use the {@link TwinClient}.
 */
@Slf4j
public class RegistryManager
{
    private final String hostName;
    private TokenCredentialCache credentialCache;
    private AzureSasCredential azureSasCredential;
    private IotHubConnectionString iotHubConnectionString;

    private final RegistryManagerOptions options;

    /**
     * Constructor to create instance from connection string
     *
     * @param connectionString The iot hub connection string
     */
    public RegistryManager(String connectionString)
    {
        this(connectionString, RegistryManagerOptions.builder().build());
    }

    /**
     * Constructor to create instance from connection string
     *
     * @param connectionString The iot hub connection string
     * @param options The connection options to use when connecting to the service.
     */
    public RegistryManager(String connectionString, RegistryManagerOptions options)
    {
        if (Tools.isNullOrEmpty(connectionString))
        {
            throw new IllegalArgumentException("The provided connection string cannot be null or empty");
        }

        if (options == null)
        {
            throw new IllegalArgumentException("RegistryManagerOptions cannot be null for this constructor");
        }

        this.iotHubConnectionString =
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);

        this.hostName = iotHubConnectionString.getHostName();
        this.options = options;
        commonConstructorSetup();
    }

    /**
     * Create a new RegistryManager instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed. The provided tokens must be Json Web Tokens.
     */
    public RegistryManager(String hostName, TokenCredential credential)
    {
        this(hostName, credential, RegistryManagerOptions.builder().build());
    }

    /**
     * Create a new RegistryManager instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed. The provided tokens must be Json Web Tokens.
     * @param options The connection options to use when connecting to the service.
     */
    public RegistryManager(String hostName, TokenCredential credential, RegistryManagerOptions options)
    {
        Objects.requireNonNull(credential, "credential cannot be null");
        Objects.requireNonNull(options, "options cannot be null");
        if (Tools.isNullOrEmpty(hostName))
        {
            throw new IllegalArgumentException("hostName cannot be null or empty");
        }

        this.options = options;
        this.credentialCache = new TokenCredentialCache(credential);
        this.hostName = hostName;
        commonConstructorSetup();
    }

    /**
     * Create a new RegistryManager instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     */
    public RegistryManager(String hostName, AzureSasCredential azureSasCredential)
    {
        this(hostName, azureSasCredential, RegistryManagerOptions.builder().build());
    }

    /**
     * Create a new RegistryManager instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     * @param options The connection options to use when connecting to the service.
     */
    public RegistryManager(String hostName, AzureSasCredential azureSasCredential, RegistryManagerOptions options)
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
        commonConstructorSetup();
    }

    private static void commonConstructorSetup()
    {
        log.debug("Initialized a RegistryManager instance client using SDK version {}", TransportUtils.serviceVersion);
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
    public Device addDevice(Device device) throws IOException, IotHubException, JsonSyntaxException
    {
        if (device == null)
        {
            throw new IllegalArgumentException("device cannot be null");
        }

        String deviceJson = device.toDeviceParser().toJson();

        URL url = IotHubConnectionString.getUrlDevice(this.hostName, device.getDeviceId());
        HttpRequest request = CreateRequest(url, HttpMethod.PUT, deviceJson.getBytes(StandardCharsets.UTF_8));

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);

        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);

        return new Device(new DeviceParser(bodyStr));
    }

    /**
     * Get device data by device Id from IotHub
     *
     * @param deviceId The id of requested device
     * @return The device object of requested device
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public Device getDevice(String deviceId) throws IOException, IotHubException, JsonSyntaxException
    {
        if (Tools.isNullOrEmpty(deviceId))
        {
            throw new IllegalArgumentException("deviceId cannot be null or empty");
        }

        URL url = IotHubConnectionString.getUrlDevice(this.hostName, deviceId);

        HttpRequest request = CreateRequest(url, HttpMethod.GET, new byte[0]);

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);

        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);

        return new Device(new DeviceParser(bodyStr));
    }

    /**
     * Return the iothub device connection string for a provided device.
     * @param device The device object to get the connectionString
     * @return The iothub device connection string
     */
    public String getDeviceConnectionString(Device device)
    {
        if (device == null)
        {
            throw new IllegalArgumentException("device cannot be null");
        }

        if (Tools.isNullOrEmpty(device.getDeviceId()) || (Tools.isNullOrEmpty(device.getPrimaryKey())) && Tools.isNullOrEmpty(device.getPrimaryThumbprint()))
        {
            throw new IllegalArgumentException("device is not valid");
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("HostName=%s;", this.hostName));
        stringBuilder.append(String.format("DeviceId=%s;", device.getDeviceId()));
        if (device.getPrimaryKey() == null)
        {
            //self signed or CA signed
            stringBuilder.append("x509=true");
        }
        else
        {
            stringBuilder.append(String.format("SharedAccessKey=%s", device.getPrimaryKey()));
        }
        return stringBuilder.toString();
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
        HttpRequest request = CreateRequest(url, HttpMethod.PUT, device.toDeviceParser().toJson().getBytes(StandardCharsets.UTF_8));

        request.setHeaderField("If-Match", "*");

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);

        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);

        return new Device(new DeviceParser(bodyStr));
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
     * @throws IllegalArgumentException This exception is thrown if the device is null
     */
    public void removeDevice(Device device) throws IOException, IotHubException, IllegalArgumentException
    {
        if (device == null)
        {
            throw new IllegalArgumentException("device cannot be null or empty");
        }

        removeDeviceOperation(device.getDeviceId(), device.geteTag());
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
        if (Tools.isNullOrEmpty(deviceId))
        {
            throw new IllegalArgumentException("deviceId cannot be null or empty");
        }

        if (Tools.isNullOrEmpty(etag))
        {
            throw new IllegalArgumentException("etag cannot be null or empty");
        }

        URL url = IotHubConnectionString.getUrlDevice(this.hostName, deviceId);

        HttpRequest request = CreateRequest(url, HttpMethod.DELETE, new byte[0]);
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
    public RegistryStatistics getStatistics() throws IOException, IotHubException, JsonSyntaxException
    {
        URL url = IotHubConnectionString.getUrlDeviceStatistics(this.hostName);

        HttpRequest request = CreateRequest(url, HttpMethod.GET, new byte[0]);

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);

        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);
        return new RegistryStatistics(new RegistryStatisticsParser(bodyStr));
    }

    /**
     * Create a bulk export job.
     *
     * @param exportBlobContainerUri URI containing SAS token to a blob container where export data will be placed
     * @param excludeKeys Whether the devices keys should be excluded from the exported data or not
     *
     * @return A JobProperties object for the newly created bulk export job
     *
     * @throws IllegalArgumentException This exception is thrown if the exportBlobContainerUri or excludeKeys parameters are null
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public JobProperties exportDevices(String exportBlobContainerUri, Boolean excludeKeys)
            throws IllegalArgumentException, IOException, IotHubException, JsonSyntaxException
    {
        if (exportBlobContainerUri == null || excludeKeys == null)
        {
            throw new IllegalArgumentException("Export blob uri cannot be null");
        }

        URL url = IotHubConnectionString.getUrlCreateExportImportJob(this.hostName);

        String jobPropertiesJson = CreateExportJobPropertiesJson(exportBlobContainerUri, excludeKeys);
        HttpRequest request = CreateRequest(url, HttpMethod.POST, jobPropertiesJson.getBytes(StandardCharsets.UTF_8));

        HttpResponse response = request.send();

        return ProcessJobResponse(response);
    }

    /**
     * Create a bulk export job.
     *
     * @param exportDevicesParameters A JobProperties object containing input parameters for export Devices job
     *                                This API also supports identity based storage authentication, identity authentication
     *                                support is currently available in limited regions. If a user wishes to try it out,
     *                                they will need to set an Environment Variable of "EnabledStorageIdentity" and set it to "1"
     *                                otherwise default key based authentication is used for storage
     *                                <a href="https://docs.microsoft.com/en-us/azure/iot-hub/virtual-network-support"> More details here </a>
     *
     * @return A JobProperties object for the newly created bulk export job
     *
     * @throws IllegalArgumentException This exception is thrown if the exportBlobContainerUri or excludeKeys parameters are null
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public JobProperties exportDevices(JobProperties exportDevicesParameters)
            throws IllegalArgumentException, IOException, IotHubException, JsonSyntaxException
    {
        URL url = IotHubConnectionString.getUrlCreateExportImportJob(this.hostName);

        exportDevicesParameters.setType(JobProperties.JobType.EXPORT);
        String jobPropertiesJson = exportDevicesParameters.toJobPropertiesParser().toJson();
        HttpRequest request = CreateRequest(url, HttpMethod.POST, jobPropertiesJson.getBytes(StandardCharsets.UTF_8));

        HttpResponse response = request.send();

        return ProcessJobResponse(response);
    }

    /**
     * Create a bulk import job.
     *
     * @param importBlobContainerUri URI containing SAS token to a blob container that contains registry data to sync
     * @param outputBlobContainerUri URI containing SAS token to a blob container where the result of the bulk import operation will be placed
     *
     * @return A JobProperties object for the newly created bulk import job
     *
     * @throws IllegalArgumentException This exception is thrown if the importBlobContainerUri or outputBlobContainerUri parameters are null
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public JobProperties importDevices(String importBlobContainerUri, String outputBlobContainerUri)
            throws IllegalArgumentException, IOException, IotHubException, JsonSyntaxException
    {
        if (importBlobContainerUri == null || outputBlobContainerUri == null)
        {
            throw new IllegalArgumentException("Import blob uri or output blob uri cannot be null");
        }

        URL url = IotHubConnectionString.getUrlCreateExportImportJob(this.hostName);

        String jobPropertiesJson = CreateImportJobPropertiesJson(importBlobContainerUri, outputBlobContainerUri);
        HttpRequest request = CreateRequest(url, HttpMethod.POST, jobPropertiesJson.getBytes(StandardCharsets.UTF_8));

        HttpResponse response = request.send();

        return ProcessJobResponse(response);
    }

    /**
     * Create a bulk import job.
     *
     * @param importDevicesParameters A JobProperties object containing input parameters for import Devices job
     *                                This API also supports identity based storage authentication, identity authentication
     *                                support is currently available in limited regions. If a user wishes to try it out,
     *                                they will need to set an Environment Variable of "EnabledStorageIdentity" and set it to "1"
     *                                otherwise default key based authentication is used for storage
     *                                <a href="https://docs.microsoft.com/en-us/azure/iot-hub/virtual-network-support"> More details here </a>
     *
     * @return A JobProperties object for the newly created bulk import job
     *
     * @throws IllegalArgumentException This exception is thrown if the importBlobContainerUri or outputBlobContainerUri parameters are null
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public JobProperties importDevices(JobProperties importDevicesParameters)
            throws IllegalArgumentException, IOException, IotHubException, JsonSyntaxException
    {
        URL url = IotHubConnectionString.getUrlCreateExportImportJob(this.hostName);

        importDevicesParameters.setType(JobProperties.JobType.IMPORT);
        String jobPropertiesJson = importDevicesParameters.toJobPropertiesParser().toJson();
        HttpRequest request = CreateRequest(url, HttpMethod.POST, jobPropertiesJson.getBytes(StandardCharsets.UTF_8));

        HttpResponse response = request.send();

        return ProcessJobResponse(response);
    }

    /**
     * Get the properties of an existing job.
     *
     * @param jobId The id of the job to be retrieved.
     *
     * @return A JobProperties object for the requested job id
     *
     * @throws IllegalArgumentException This exception is thrown if the jobId parameter is null
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public JobProperties getJob(String jobId)
            throws IllegalArgumentException, IOException, IotHubException, JsonSyntaxException
    {
        if (jobId == null)
        {
            throw new IllegalArgumentException("Job id cannot be null");
        }

        URL url = IotHubConnectionString.getUrlImportExportJob(this.hostName, jobId);

        HttpRequest request = CreateRequest(url, HttpMethod.GET, new byte[0]);

        HttpResponse response = request.send();

        return ProcessJobResponse(response);
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
    public Module addModule(Module module) throws IOException, IotHubException, JsonSyntaxException
    {
        if (module == null)
        {
            throw new IllegalArgumentException("module cannot be null");
        }

        String moduleJson = module.toDeviceParser().toJson();

        URL url = IotHubConnectionString.getUrlModule(this.hostName, module.getDeviceId(), module.getId());

        HttpRequest request = CreateRequest(url, HttpMethod.PUT, moduleJson.getBytes(StandardCharsets.UTF_8));

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);

        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);

        return new Module(new DeviceParser(bodyStr));
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
    public Module getModule(String deviceId, String moduleId) throws IOException, IotHubException, JsonSyntaxException
    {
        if (Tools.isNullOrEmpty(deviceId))
        {
            throw new IllegalArgumentException("deviceId cannot be null or empty");
        }

        if (Tools.isNullOrEmpty(moduleId))
        {
            throw new IllegalArgumentException("moduleId cannot be null or empty");
        }

        URL url = IotHubConnectionString.getUrlModule(this.hostName, deviceId, moduleId);

        HttpRequest request = CreateRequest(url, HttpMethod.GET, new byte[0]);

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);

        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);

        return new Module(new DeviceParser(bodyStr));
    }

    /**
     * Get modules data by device Id from IotHub
     *
     * @param deviceId The id of requested device
     * @return The module objects on the specific device
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public List<Module> getModulesOnDevice(String deviceId) throws IOException, IotHubException, JsonSyntaxException
    {
        if (Tools.isNullOrEmpty(deviceId))
        {
            throw new IllegalArgumentException("deviceId cannot be null or empty");
        }

        URL url = IotHubConnectionString.getUrlModulesOnDevice(this.hostName, deviceId);

        HttpRequest request = CreateRequest(url, HttpMethod.GET, new byte[0]);

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
                Module iotHubModule = new Module(new DeviceParser(jsonObject.toString()));
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

        HttpRequest request = CreateRequest(url, HttpMethod.PUT, module.toDeviceParser().toJson().getBytes(StandardCharsets.UTF_8));
        request.setHeaderField("If-Match", "*");

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);

        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);

        return new Module(new DeviceParser(bodyStr));
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
     * @throws IllegalArgumentException This exception is thrown if the input module is null
     */
    public void removeModule(Module module) throws IOException, IotHubException, IllegalArgumentException
    {
        if (module == null)
        {
            throw new IllegalArgumentException("module cannot be null or empty");
        }

        removeModuleOperation(module.getDeviceId(), module.getId(), module.geteTag());
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
        if (Tools.isNullOrEmpty(deviceId))
        {
            throw new IllegalArgumentException("deviceId cannot be null or empty");
        }

        if (Tools.isNullOrEmpty(moduleId))
        {
            throw new IllegalArgumentException("moduleId cannot be null or empty");
        }

        if (Tools.isNullOrEmpty(etag))
        {
            throw new IllegalArgumentException("etag cannot be null or empty");
        }

        URL url = IotHubConnectionString.getUrlModule(this.hostName, deviceId, moduleId);

        HttpRequest request = CreateRequest(url, HttpMethod.DELETE, new byte[0]);
        request.setHeaderField("If-Match", etag);

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);
    }

    /**
     * Add configuration using the given Configuration object
     * Return with the response configuration object from IotHub
     *
     * @param configuration The configuration object to add
     * @return The configuration object for the requested operation
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public Configuration addConfiguration(Configuration configuration)
            throws IOException, IotHubException, JsonSyntaxException
    {
        if (configuration == null)
        {
            throw new IllegalArgumentException("configuration cannot be null");
        }

        String configurationJson = configuration.toConfigurationParser().toJson();

        URL url = IotHubConnectionString.getUrlConfiguration(this.hostName, configuration.getId());

        HttpRequest request = CreateRequest(url, HttpMethod.PUT, configurationJson.getBytes(StandardCharsets.UTF_8));

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);

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
    public Configuration getConfiguration(String configurationId)
            throws IOException, IotHubException, JsonSyntaxException
    {
        if (Tools.isNullOrEmpty(configurationId))
        {
            throw new IllegalArgumentException("configurationId cannot be null or empty");
        }

        URL url = IotHubConnectionString.getUrlConfiguration(this.hostName, configurationId);

        HttpRequest request = CreateRequest(url, HttpMethod.GET, new byte[0]);

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);

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
    public List<Configuration> getConfigurations(Integer maxCount)
            throws IOException, IotHubException, JsonSyntaxException
    {
        if (maxCount < 1)
        {
            throw new IllegalArgumentException("maxCount cannot be less then 1");
        }

        URL url = IotHubConnectionString.getUrlConfigurationsList(this.hostName, maxCount);

        HttpRequest request = CreateRequest(url, HttpMethod.GET, new byte[0]);

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);

        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);
        try (JsonReader jsonReader = Json.createReader(new StringReader(bodyStr)))
        {
            List<Configuration> configurationList = new ArrayList<>();
            JsonArray deviceArray = jsonReader.readArray();

            for (int i = 0; i < deviceArray.size(); i++)
            {
                JsonObject jsonObject = deviceArray.getJsonObject(i);
                Configuration iotHubConfiguration = new Configuration(new ConfigurationParser(jsonObject.toString()));
                configurationList.add(iotHubConfiguration);
            }
            return configurationList;
        }
    }

    /**
     * Update configuration not forced
     *
     * @param configuration The configuration object containing updated data
     * @return The updated configuration object
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public Configuration updateConfiguration(Configuration configuration) throws IOException, IotHubException
    {
        if (configuration == null)
        {
            throw new IllegalArgumentException("configuration cannot be null");
        }

        URL url = IotHubConnectionString.getUrlConfiguration(this.hostName, configuration.getId());

        HttpRequest request = CreateRequest(url, HttpMethod.PUT, configuration.toConfigurationParser().toJson().getBytes(StandardCharsets.UTF_8));

        request.setHeaderField("If-Match", "*");

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);

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
    public void removeConfiguration(String configurationId) throws IOException, IotHubException
    {
        removeConfigurationOperation(configurationId, "*");
    }

    /**
     * Send remove configuration request and verify response
     *
     * @param config The configuration to be removed
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     * @throws IllegalArgumentException This exception is thrown if the input configuration is null
     */
    public void removeConfiguration(Configuration config) throws IOException, IotHubException, IllegalArgumentException
    {
        if (config == null)
        {
            throw new IllegalArgumentException("configuration cannot be null or empty");
        }

        removeConfigurationOperation(config.getId(), config.getEtag());
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
        if (Tools.isNullOrEmpty(configurationId))
        {
            throw new IllegalArgumentException("configurationId cannot be null or empty");
        }

        if (Tools.isNullOrEmpty(etag))
        {
            throw new IllegalArgumentException("etag cannot be null or empty");
        }

        URL url = IotHubConnectionString.getUrlConfiguration(this.hostName, configurationId);

        HttpRequest request = CreateRequest(url, HttpMethod.DELETE, new byte[0]);
        request.setHeaderField("If-Match", etag);

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);
    }

    /**
     * Apply the provided configuration content to the provided device
     * @param deviceId The device to apply the configuration to
     * @param content The configuration content to apply to the device
     * @throws IOException If the iot hub cannot be reached
     * @throws IotHubException If the response from the hub was an error code. This exception will contain that code
     */
    public void applyConfigurationContentOnDevice(String deviceId, ConfigurationContent content)
            throws IOException, IotHubException
    {
        if (content == null)
        {
            throw new IllegalArgumentException("content cannot be null");
        }

        URL url = IotHubConnectionString.getUrlApplyConfigurationContent(this.hostName, deviceId);

        HttpRequest request = CreateRequest(url, HttpMethod.POST, content.toConfigurationContentParser().toJson().getBytes(StandardCharsets.UTF_8));

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);
    }

    private String CreateExportJobPropertiesJson(String exportBlobContainerUri, Boolean excludeKeysInExport)
    {
        JobProperties jobProperties = new JobProperties();
        jobProperties.setType(JobProperties.JobType.EXPORT);
        jobProperties.setOutputBlobContainerUri(exportBlobContainerUri);
        jobProperties.setExcludeKeysInExport(excludeKeysInExport);
        return jobProperties.toJobPropertiesParser().toJson();
    }

    private String CreateImportJobPropertiesJson(String importBlobContainerUri, String outputBlobContainerUri)
    {
        JobProperties jobProperties = new JobProperties();
        jobProperties.setType(JobProperties.JobType.IMPORT);
        jobProperties.setInputBlobContainerUri(importBlobContainerUri);
        jobProperties.setOutputBlobContainerUri(outputBlobContainerUri);
        return jobProperties.toJobPropertiesParser().toJson();
    }

    private JobProperties ProcessJobResponse(HttpResponse response) throws IotHubException, JsonSyntaxException {
        IotHubExceptionManager.httpResponseVerification(response);
        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);
        return new JobProperties(new JobPropertiesParser(bodyStr));
    }

    private HttpRequest CreateRequest(URL url, HttpMethod method, byte[] payload)
            throws IOException
    {
        Proxy proxy = null;
        if (this.options.getProxyOptions() != null)
        {
            proxy = this.options.getProxyOptions().getProxy();
        }

        HttpRequest request = new HttpRequest(url, method, payload, proxy);
        request.setReadTimeoutMillis(options.getHttpReadTimeout());
        request.setConnectTimeoutMillis(options.getHttpConnectTimeout());

        request.setHeaderField("authorization", getAuthenticationToken());
        request.setHeaderField("Request-Id", "1001");
        request.setHeaderField("Accept", "application/json");
        request.setHeaderField("Content-Type", "application/json");
        request.setHeaderField("charset", "utf-8");
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