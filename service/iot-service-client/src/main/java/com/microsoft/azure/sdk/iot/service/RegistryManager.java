/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.google.gson.JsonSyntaxException;
import com.microsoft.azure.sdk.iot.deps.serializer.ConfigurationParser;
import com.microsoft.azure.sdk.iot.deps.serializer.DeviceParser;
import com.microsoft.azure.sdk.iot.deps.serializer.JobPropertiesParser;
import com.microsoft.azure.sdk.iot.deps.serializer.RegistryStatisticsParser;
import com.microsoft.azure.sdk.iot.deps.transport.amqp.TokenCredentialType;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringCredential;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubExceptionManager;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;

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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Use the RegistryManager class to manage the identity registry in IoT Hubs.
 */
public class RegistryManager
{
    private static final int EXECUTOR_THREAD_POOL_SIZE = 10;
    private ExecutorService executor;
    private String hostName;
    private TokenCredential authenticationTokenProvider;
    private TokenCredentialType tokenCredentialType;

    private RegistryManagerOptions options;

    /**
     * Previously was the java default constructor, should not be used.
     *
     * @deprecated As of release 1.22.0, replaced by {@link #createFromConnectionString(String)}
     */
    @Deprecated
    public RegistryManager()
    {
        // This constructor was previously a default constructor that users could use because there was no other constructor declared.
        // However, we still prefer users use the createFromConnectionString method to build their clients.
        options = RegistryManagerOptions.builder()
                .httpConnectTimeout(RegistryManagerOptions.DEFAULT_HTTP_CONNECT_TIMEOUT_MS)
                .httpReadTimeout(RegistryManagerOptions.DEFAULT_HTTP_READ_TIMEOUT_MS)
                .build();
    }

    /**
     * Static constructor to create instance from connection string
     *
     * @param connectionString The iot hub connection string
     * @return The instance of RegistryManager
     * @throws IOException This exception is never thrown.
     */
    public static RegistryManager createFromConnectionString(String connectionString) throws IOException
    {
        RegistryManagerOptions options = RegistryManagerOptions.builder()
                .httpConnectTimeout(RegistryManagerOptions.DEFAULT_HTTP_CONNECT_TIMEOUT_MS)
                .httpReadTimeout(RegistryManagerOptions.DEFAULT_HTTP_READ_TIMEOUT_MS)
                .build();

        return createFromConnectionString(connectionString, options);
    }

    /**
     * Static constructor to create instance from connection string
     *
     * @param connectionString The iot hub connection string
     * @param options The connection options to use when connecting to the service.
     * @return The instance of RegistryManager
     * @throws IOException This exception is never thrown.
     */
    public static RegistryManager createFromConnectionString(
            String connectionString,
            RegistryManagerOptions options) throws IOException
    {
        if (Tools.isNullOrEmpty(connectionString))
        {
            throw new IllegalArgumentException("The provided connection string cannot be null or empty");
        }

        if (options == null)
        {
            throw new IllegalArgumentException("RegistryManagerOptions cannot be null for this constructor");
        }

        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
        TokenCredential authenticationTokenProvider = new IotHubConnectionStringCredential(connectionString);

        return createFromTokenCredential(iotHubConnectionString.hostName, authenticationTokenProvider, TokenCredentialType.SHARED_ACCESS_SIGNATURE);
    }

    /**
     * Create a new RegistryManager instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param authenticationTokenProvider The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed.
     * @param tokenCredentialType The type of authentication tokens that the provided {@link TokenCredential}
     *                          implementation will always give.
     * @return The instance of RegistryManager
     */
    public static RegistryManager createFromTokenCredential(
            String hostName,
            TokenCredential authenticationTokenProvider,
            TokenCredentialType tokenCredentialType)
    {
        return createFromTokenCredential(hostName, authenticationTokenProvider, tokenCredentialType, RegistryManagerOptions.builder().build());
    }

    /**
     * Create a new RegistryManager instance.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param authenticationTokenProvider The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed.
     * @param tokenCredentialType The type of authentication tokens that the provided {@link TokenCredential}
     *                          implementation will always give.
     * @param options The connection options to use when connecting to the service.
     * @return The instance of RegistryManager
     */
    public static RegistryManager createFromTokenCredential(
            String hostName,
            TokenCredential authenticationTokenProvider,
            TokenCredentialType tokenCredentialType,
            RegistryManagerOptions options)
    {
        Objects.requireNonNull(authenticationTokenProvider, "authenticationTokenProvider cannot be null");
        Objects.requireNonNull(options, "options cannot be null");
        if (Tools.isNullOrEmpty(hostName))
        {
            throw new IllegalArgumentException("hostName cannot be null or empty");
        }

        RegistryManager registryManager = new RegistryManager();
        registryManager.executor = Executors.newFixedThreadPool(EXECUTOR_THREAD_POOL_SIZE);
        registryManager.options = options;
        registryManager.authenticationTokenProvider = authenticationTokenProvider;
        registryManager.tokenCredentialType = tokenCredentialType;
        registryManager.hostName = hostName;
        return registryManager;
    }

    /**
     * @deprecated as of release 1.13.0 this API is no longer supported and open is done implicitly by the respective APIs
     * Opens this registry manager's executor service after it has been closed.
     */
    @Deprecated
    public void open()
    {
    }

    /**
     * Gracefully close running threads, and then shutdown the underlying executor service
     */
    public void close()
    {
        if (executor != null && !executor.isTerminated())
        {
            this.executor.shutdownNow();
        }
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
        String authenticationToken = this.getAuthenticationToken();

        HttpRequest request = CreateRequest(url, HttpMethod.PUT, deviceJson.getBytes(), authenticationToken);

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);

        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);

        return new Device(new DeviceParser(bodyStr));
    }

    /**
     * Async wrapper for add() operation
     *
     * @param device The device object to add
     * @return The future object for the requested operation
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public CompletableFuture<Device> addDeviceAsync(Device device) throws IOException, IotHubException
    {
        if (device == null)
        {
            throw new IllegalArgumentException("device cannot be null");
        }

        final CompletableFuture<Device> future = new CompletableFuture<>();
        executor.submit(() ->
        {
            try
            {
                Device responseDevice = addDevice(device);
                future.complete(responseDevice);
            }
            catch (IOException | IotHubException e)
            {
                future.completeExceptionally(e);
            }
        });
        return future;
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
        String authenticationToken = this.getAuthenticationToken();

        HttpRequest request = CreateRequest(url, HttpMethod.GET, new byte[0], authenticationToken);

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);

        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);

        return new Device(new DeviceParser(bodyStr));
    }

    /**
     * Async wrapper for getDevice() operation
     *
     * @param deviceId The id of requested device
     * @return The future object for the requested operation
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public CompletableFuture<Device> getDeviceAsync(String deviceId) throws IOException, IotHubException
    {
        if (Tools.isNullOrEmpty(deviceId))
        {
            throw new IllegalArgumentException("deviceId cannot be null or empty");
        }

        final CompletableFuture<Device> future = new CompletableFuture<>();
        executor.submit(() ->
        {
            try
            {
                Device responseDevice = getDevice(deviceId);
                future.complete(responseDevice);
            }
            catch (IotHubException | IOException e)
            {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Get list of devices
     * @deprecated as of release 1.12.0. Please use
     * {@link com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwin#queryTwin(String sqlQuery, Integer pageSize)}
     * to query for all devices.
     *
     * @param maxCount The requested count of devices
     * @return The array of requested device objects
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    @Deprecated
    public ArrayList<Device> getDevices(Integer maxCount) throws IOException, IotHubException, JsonSyntaxException
    {
        if (maxCount < 1)
        {
            throw new IllegalArgumentException("maxCount cannot be less then 1");
        }

        URL url = IotHubConnectionString.getUrlDeviceList(this.hostName, maxCount);
        String authenticationToken = this.getAuthenticationToken();

        HttpRequest request = CreateRequest(url, HttpMethod.GET, new byte[0], authenticationToken);

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);

        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);
        try (JsonReader jsonReader = Json.createReader(new StringReader(bodyStr)))
        {
            ArrayList<Device> deviceList = new ArrayList<>();
            JsonArray deviceArray = jsonReader.readArray();
            
            for (int i = 0; i < deviceArray.size(); i++)
            {
                JsonObject jsonObject = deviceArray.getJsonObject(i);
                Device iotHubDevice = new Device(new DeviceParser(jsonObject.toString()));
                deviceList.add(iotHubDevice);
            }
            return deviceList;
        }
    }

    /**
     * Async wrapper for getDevices() operation
     *
     * @deprecated as of release 1.12.0. Please use
     * {@link com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwin#queryTwin(String sqlQuery, Integer pageSize)}
     * to query for all devices.
     *
     * @param maxCount The requested count of devices
     * @return The future object for the requested operation
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    @Deprecated
    public CompletableFuture<ArrayList<Device>> getDevicesAsync(Integer maxCount) throws IOException, IotHubException
    {
        if (maxCount < 1)
        {
            throw new IllegalArgumentException("maxCount cannot be less then 1");
        }

        final CompletableFuture<ArrayList<Device>> future = new CompletableFuture<>();
        executor.submit(() ->
        {
            try
            {
                ArrayList<Device> response = getDevices(maxCount);
                future.complete(response);
            }
            catch (IotHubException | IOException e)
            {
                future.completeExceptionally(e);
            }
        });
        return future;
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

        if(Tools.isNullOrEmpty(device.getDeviceId()) || (Tools.isNullOrEmpty(device.getPrimaryKey())) && Tools.isNullOrEmpty(device.getPrimaryThumbprint()))
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
        return updateDevice(device, false);
    }

    /**
     * Update device with forceUpdate input parameter
     *
     * @param device The device object containing updated data
     * @param forceUpdate True if the update has to be forced regardless of the device state
     * @return The updated device object
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public Device updateDevice(Device device, Boolean forceUpdate) throws IOException, IotHubException, JsonSyntaxException
    {
        if (device == null)
        {
            throw new IllegalArgumentException("device cannot be null");
        }

        device.setForceUpdate(forceUpdate);

        URL url = IotHubConnectionString.getUrlDevice(this.hostName, device.getDeviceId());
        String authenticationToken = this.getAuthenticationToken();

        HttpRequest request = CreateRequest(url, HttpMethod.PUT, device.toDeviceParser().toJson().getBytes(), authenticationToken);
        request.setHeaderField("If-Match", "*");

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);

        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);

        return new Device(new DeviceParser(bodyStr));
    }

    /**
     * Async wrapper for updateDevice() operation
     *
     * @param device The device object containing updated data
     * @return The future object for the requested operation
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public CompletableFuture<Device> updateDeviceAsync(Device device) throws IOException, IotHubException
    {
        if (device == null)
        {
            throw new IllegalArgumentException("device cannot be null");
        }
        final CompletableFuture<Device> future = new CompletableFuture<>();
        executor.submit(() ->
        {
            try
            {
                Device responseDevice = updateDevice(device);
                future.complete(responseDevice);
            }
            catch (IotHubException | IOException e)
            {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Async wrapper for forced updateDevice() operation
     *
     * @param device The device object containing updated data
     * @param forceUpdate True is the update has to be forced regardless if the device state
     * @return The future object for the requested operation
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public CompletableFuture<Device> updateDeviceAsync(Device device, Boolean forceUpdate) throws IOException, IotHubException
    {
        if (device == null)
        {
            throw new IllegalArgumentException("device cannot be null");
        }
        final CompletableFuture<Device> future = new CompletableFuture<>();
        executor.submit(() ->
        {
            try
            {
                Device responseDevice = updateDevice(device, forceUpdate);
                future.complete(responseDevice);
            }
            catch (IotHubException | IOException e)
            {
                future.completeExceptionally(e);
            }
        });
        return future;
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

        String accessToken = this.authenticationTokenProvider.getToken(new TokenRequestContext()).block().getToken();

        HttpRequest request = CreateRequest(url, HttpMethod.DELETE, new byte[0], accessToken);
        request.setHeaderField("If-Match", etag);

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);
    }

    /**
     * Async wrapper for removeDevice() operation
     *
     * @param deviceId The device object to remove
     * @return The future object for the requested operation
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public CompletableFuture<Boolean> removeDeviceAsync(String deviceId) throws IOException, IotHubException
    {

        if (Tools.isNullOrEmpty(deviceId))
        {
            throw new IllegalArgumentException("deviceId cannot be null or empty");
        }

        final CompletableFuture<Boolean> future = new CompletableFuture<>();
        executor.submit(() ->
        {
            try
            {
                removeDevice(deviceId);
                future.complete(true);
            }
            catch (IotHubException | IOException e)
            {
                future.completeExceptionally(e);
            }
        });
        return future;
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

        String authenticationToken = this.getAuthenticationToken();

        HttpRequest request = CreateRequest(url, HttpMethod.GET, new byte[0], authenticationToken);

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);

        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);
        return new RegistryStatistics(new RegistryStatisticsParser(bodyStr));
    }

    /**
     * Async wrapper for getStatistics() operation
     *
     * @return The future object for the requested operation
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public CompletableFuture<RegistryStatistics> getStatisticsAsync() throws IOException, IotHubException
    {
        final CompletableFuture<RegistryStatistics> future = new CompletableFuture<>();
        executor.submit(() ->
        {
            try
            {
                RegistryStatistics responseDevice = getStatistics();
                future.complete(responseDevice);
            }
            catch (IotHubException | IOException e)
            {
                future.completeExceptionally(e);
            }
        });
        return future;
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

        String authenticationToken = this.getAuthenticationToken();

        String jobPropertiesJson = CreateExportJobPropertiesJson(exportBlobContainerUri, excludeKeys);
        HttpRequest request = CreateRequest(url, HttpMethod.POST, jobPropertiesJson.getBytes(), authenticationToken);

        HttpResponse response = request.send();

        return ProcessJobResponse(response);
    }

    /**
     * Async wrapper for exportDevices() operation
     * @param excludeKeys if to exclude keys or not
     * @param exportBlobContainerUri the blob storage container URI to store at.
     * @return The future object for the requested operation
     *
     * @throws IllegalArgumentException This exception is thrown if the exportBlobContainerUri or excludeKeys parameters are null
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public CompletableFuture<JobProperties> exportDevicesAsync(String exportBlobContainerUri, Boolean excludeKeys)
            throws IllegalArgumentException, IOException, IotHubException, JsonSyntaxException
    {
        final CompletableFuture<JobProperties> future = new CompletableFuture<>();
        executor.submit(() ->
        {
            try
            {
                JobProperties responseJobProperties = exportDevices(exportBlobContainerUri, excludeKeys);
                future.complete(responseJobProperties);
            }
            catch (IllegalArgumentException | IotHubException | IOException e)
            {
                future.completeExceptionally(e);
            }
        });
        return future;
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

        String authenticationToken = this.getAuthenticationToken();

        exportDevicesParameters.setType(JobProperties.JobType.EXPORT);
        String jobPropertiesJson = exportDevicesParameters.toJobPropertiesParser().toJson();
        HttpRequest request = CreateRequest(url, HttpMethod.POST, jobPropertiesJson.getBytes(), authenticationToken);

        HttpResponse response = request.send();

        return ProcessJobResponse(response);
    }

    /**
     * Async wrapper for exportDevices() operation
     * @param exportDevicesParameters A JobProperties object containing input parameters for export Devices job
     *                                This API also supports identity based storage authentication, identity authentication
     *                                support is currently available in limited regions. If a user wishes to try it out,
     *                                they will need to set an Environment Variable of "EnabledStorageIdentity" and set it to "1"
     *                                otherwise default key based authentication is used for storage
     *                                <a href="https://docs.microsoft.com/en-us/azure/iot-hub/virtual-network-support"> More details here </a>
     * @return The future object for the requested operation
     *
     * @throws IllegalArgumentException This exception is thrown if the exportBlobContainerUri or excludeKeys parameters are null
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public CompletableFuture<JobProperties> exportDevicesAsync(JobProperties exportDevicesParameters)
            throws IllegalArgumentException, IOException, IotHubException, JsonSyntaxException
    {
        final CompletableFuture<JobProperties> future = new CompletableFuture<>();
        executor.submit(() ->
        {
            try
            {
                JobProperties responseJobProperties = exportDevices(exportDevicesParameters);
                future.complete(responseJobProperties);
            }
            catch (IllegalArgumentException | IotHubException | IOException e)
            {
                future.completeExceptionally(e);
            }
        });
        return future;
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

        String authenticationToken = this.getAuthenticationToken();

        String jobPropertiesJson = CreateImportJobPropertiesJson(importBlobContainerUri, outputBlobContainerUri);
        HttpRequest request = CreateRequest(url, HttpMethod.POST, jobPropertiesJson.getBytes(), authenticationToken);

        HttpResponse response = request.send();

        return ProcessJobResponse(response);
    }

    /**
     * Async wrapper for importDevices() operation
     *
     * @param importBlobContainerUri Uri for importBlobContainer
     * @param outputBlobContainerUri Uri for outputBlobContainer
     * @return The future object for the requested operation
     *
     * @throws IllegalArgumentException This exception is thrown if the exportBlobContainerUri or excludeKeys parameters are null
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public CompletableFuture<JobProperties> importDevicesAsync(String importBlobContainerUri, String outputBlobContainerUri)
            throws IllegalArgumentException, IOException, IotHubException, JsonSyntaxException
    {
        final CompletableFuture<JobProperties> future = new CompletableFuture<>();
        executor.submit(() ->
        {
            try
            {
                JobProperties responseJobProperties = importDevices(importBlobContainerUri, outputBlobContainerUri);
                future.complete(responseJobProperties);
            }
            catch (IllegalArgumentException | IotHubException | IOException e)
            {
                future.completeExceptionally(e);
            }
        });
        return future;
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

        String authenticationToken = this.getAuthenticationToken();

        importDevicesParameters.setType(JobProperties.JobType.IMPORT);
        String jobPropertiesJson = importDevicesParameters.toJobPropertiesParser().toJson();
        HttpRequest request = CreateRequest(url, HttpMethod.POST, jobPropertiesJson.getBytes(), authenticationToken);

        HttpResponse response = request.send();

        return ProcessJobResponse(response);
    }

    /**
     * Async wrapper for importDevices() operation
     *
     * @param importParameters A JobProperties object containing input parameters for import Devices job
     *                         This API also supports identity based storage authentication, identity authentication
     *                         support is currently available in limited regions. If a user wishes to try it out,
     *                         they will need to set an Environment Variable of "EnabledStorageIdentity" and set it to "1"
     *                         otherwise default key based authentication is used for storage
     *                         <a href="https://docs.microsoft.com/en-us/azure/iot-hub/virtual-network-support"> More details here </a>
     * @return The future object for the requested operation
     *
     * @throws IllegalArgumentException This exception is thrown if the exportBlobContainerUri or excludeKeys parameters are null
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public CompletableFuture<JobProperties> importDevicesAsync(JobProperties importParameters)
            throws IllegalArgumentException, IOException, IotHubException, JsonSyntaxException
    {
        final CompletableFuture<JobProperties> future = new CompletableFuture<>();
        executor.submit(() ->
        {
            try
            {
                JobProperties responseJobProperties = importDevices(importParameters);
                future.complete(responseJobProperties);
            }
            catch (IllegalArgumentException | IotHubException | IOException e)
            {
                future.completeExceptionally(e);
            }
        });
        return future;
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

        String authenticationToken = this.getAuthenticationToken();

        HttpRequest request = CreateRequest(url, HttpMethod.GET, new byte[0], authenticationToken);

        HttpResponse response = request.send();

        return ProcessJobResponse(response);
    }

    /**
     * Async wrapper for getJob() operation
     * @param jobId jobID as String
     * @return The future object for the requested operation
     *
     * @throws IllegalArgumentException This exception is thrown if the jobId parameter is null
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public CompletableFuture<JobProperties> getJobAsync(
            String jobId) throws IllegalArgumentException, IOException, IotHubException
    {
        final CompletableFuture<JobProperties> future = new CompletableFuture<>();
        executor.submit(() ->
        {
            try
            {
                JobProperties responseJobProperties = getJob(jobId);
                future.complete(responseJobProperties);
            }
            catch (IllegalArgumentException | IotHubException | IOException e)
            {
                future.completeExceptionally(e);
            }
        });
        return future;
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
        String authenticationToken = this.getAuthenticationToken();

        HttpRequest request = CreateRequest(url, HttpMethod.PUT, moduleJson.getBytes(), authenticationToken);

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
        String authenticationToken = this.getAuthenticationToken();

        HttpRequest request = CreateRequest(url, HttpMethod.GET, new byte[0], authenticationToken);

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
        String authenticationToken = this.getAuthenticationToken();

        HttpRequest request = CreateRequest(url, HttpMethod.GET, new byte[0], authenticationToken);

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
        return updateModule(module, false);
    }

    /**
     * Update module with forceUpdate input parameter
     *
     * @param module The module object containing updated data
     * @param forceUpdate True if the update has to be forced regardless of the module state
     * @return The updated module object
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public Module updateModule(Module module, Boolean forceUpdate)
            throws IOException, IotHubException, JsonSyntaxException
    {
        if (module == null)
        {
            throw new IllegalArgumentException("module cannot be null");
        }

        module.setForceUpdate(forceUpdate);

        URL url = IotHubConnectionString.getUrlModule(this.hostName, module.getDeviceId(), module.getId());
        String authenticationToken = this.getAuthenticationToken();

        HttpRequest request = CreateRequest(url, HttpMethod.PUT, module.toDeviceParser().toJson().getBytes(), authenticationToken);
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

        String accessToken = this.authenticationTokenProvider.getToken(new TokenRequestContext()).block().getToken();

        HttpRequest request = CreateRequest(url, HttpMethod.DELETE, new byte[0], accessToken);
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
        String authenticationToken = this.getAuthenticationToken();

        HttpRequest request = CreateRequest(url, HttpMethod.PUT, configurationJson.getBytes(), authenticationToken);

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
        String authenticationToken = this.getAuthenticationToken();

        HttpRequest request = CreateRequest(url, HttpMethod.GET, new byte[0], authenticationToken);

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
        String authenticationToken = this.getAuthenticationToken();

        HttpRequest request = CreateRequest(url, HttpMethod.GET, new byte[0], authenticationToken);

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
        return updateConfiguration(configuration, false);
    }

    /**
     * Update configuration with forceUpdate input parameter
     *
     * @param configuration The configuration object containing updated data
     * @param forceUpdate True if the update has to be forced regardless of the configuration state
     * @return The updated configuration object
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public Configuration updateConfiguration(Configuration configuration, Boolean forceUpdate)
            throws IOException, IotHubException, JsonSyntaxException
    {
        if (configuration == null)
        {
            throw new IllegalArgumentException("configuration cannot be null");
        }

        configuration.setForceUpdate(forceUpdate);

        URL url = IotHubConnectionString.getUrlConfiguration(this.hostName, configuration.getId());
        String authenticationToken = this.getAuthenticationToken();

        HttpRequest request = CreateRequest(url, HttpMethod.PUT, configuration.toConfigurationParser().toJson().getBytes(), authenticationToken);
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

        String accessToken = this.authenticationTokenProvider.getToken(new TokenRequestContext()).block().getToken();

        HttpRequest request = CreateRequest(url, HttpMethod.DELETE, new byte[0], accessToken);
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

        String authenticationToken = this.getAuthenticationToken();

        HttpRequest request = CreateRequest(url, HttpMethod.POST, content.toConfigurationContentParser().toJson().getBytes(), authenticationToken);
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

    private HttpRequest CreateRequest(URL url, HttpMethod method, byte[] payload, String authenticationToken)
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

        if (tokenCredentialType == TokenCredentialType.SHARED_ACCESS_SIGNATURE)
        {
            request.setHeaderField("authorization", authenticationToken);
        }
        // TODO when enabling RBAC support, need to prepend the authentication token with "Bearer "
        //else
        //{
        //    request.setHeaderField("authorization", "Bearer " + authenticationToken);
        //}

        request.setHeaderField("Request-Id", "1001");
        request.setHeaderField("Accept", "application/json");
        request.setHeaderField("Content-Type", "application/json");
        request.setHeaderField("charset", "utf-8");
        return request;
    }

    private String getAuthenticationToken()
    {
        return this.authenticationTokenProvider.getToken(new TokenRequestContext()).block().getToken();
    }
}