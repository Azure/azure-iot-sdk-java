/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import com.google.gson.JsonSyntaxException;
import com.microsoft.azure.sdk.iot.deps.serializer.ConfigurationParser;
import com.microsoft.azure.sdk.iot.deps.serializer.DeviceParser;
import com.microsoft.azure.sdk.iot.deps.serializer.JobPropertiesParser;
import com.microsoft.azure.sdk.iot.deps.serializer.RegistryStatisticsParser;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
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
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Use the RegistryManager class to manage the identity registry in IoT Hubs.
 */
public class RegistryManager
{
    private final Integer DEFAULT_HTTP_TIMEOUT_MS = 24000;
    private static final int EXECUTOR_THREAD_POOL_SIZE = 10;
    private ExecutorService executor;
    private IotHubConnectionString iotHubConnectionString;

    /**
     * Static constructor to create instance from connection string
     *
     * @param connectionString The iot hub connection string
     * @return The instance of RegistryManager
     * @throws IOException This exception is thrown if the object creation failed
     */
    public static RegistryManager createFromConnectionString(String connectionString) throws IOException
    {
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_001: [The constructor shall throw IllegalArgumentException if the input string is null or empty]
        if (Tools.isNullOrEmpty(connectionString))
        {
            throw new IllegalArgumentException("The provided connection string cannot be null or empty");
        }
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_002: [The constructor shall create an IotHubConnectionString object from the given connection string]
        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_003: [The constructor shall create a new RegistryManager, stores the created IotHubConnectionString object and return with it]
        RegistryManager iotHubRegistryManager = new RegistryManager();
        iotHubRegistryManager.iotHubConnectionString = iotHubConnectionString;

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_34_090: [The function shall start this object's executor service]
        iotHubRegistryManager.executor = Executors.newFixedThreadPool(EXECUTOR_THREAD_POOL_SIZE);

        return iotHubRegistryManager;
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
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_34_087: [The function shall tell this object's executor service to shutdown]
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
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_004: [The constructor shall throw IllegalArgumentException if the input device is null]
        if (device == null)
        {
            throw new IllegalArgumentException("device cannot be null");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_005: [The function shall deserialize the given device object to Json string]
        String deviceJson = device.toDeviceParser().toJson();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_006: [The function shall get the URL for the device]
        URL url = iotHubConnectionString.getUrlDevice(device.getDeviceId());
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_007: [The function shall create a new SAS token for the device]
        String sasTokenString = new IotHubServiceSasToken(this.iotHubConnectionString).toString();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_008: [The function shall create a new HttpRequest for adding the device to IotHub]
        HttpRequest request = CreateRequest(url, HttpMethod.PUT, deviceJson.getBytes(), sasTokenString);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_009: [The function shall send the created request and get the response]
        HttpResponse response = request.send();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_010: [The function shall verify the response status and throw proper Exception]
        IotHubExceptionManager.httpResponseVerification(response);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_011: [The function shall create a new Device object from the response and return with it]
        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);

        Device iotHubDevice = new Device(new DeviceParser(bodyStr));

        return iotHubDevice;
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
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_012: [The constructor shall throw IllegalArgumentException if the input device is null]
        if (device == null)
        {
            throw new IllegalArgumentException("device cannot be null");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_ REGISTRYMANAGER_12_013: [The function shall create an async wrapper around the addDevice() function call, handle the return value or delegate exception]
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
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_014: [The constructor shall throw IllegalArgumentException if the input string is null or empty]
        if (Tools.isNullOrEmpty(deviceId))
        {
            throw new IllegalArgumentException("deviceId cannot be null or empty");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_015: [The function shall get the URL for the device]
        URL url = iotHubConnectionString.getUrlDevice(deviceId);
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_016: [The function shall create a new SAS token for the device]
        String sasTokenString = new IotHubServiceSasToken(this.iotHubConnectionString).toString();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_017: [The function shall create a new HttpRequest for getting a device from IotHub]
        HttpRequest request = CreateRequest(url, HttpMethod.GET, new byte[0], sasTokenString);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_018: [The function shall send the created request and get the response]
        HttpResponse response = request.send();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_019: [The function shall verify the response status and throw proper Exception]
        IotHubExceptionManager.httpResponseVerification(response);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_020: [The function shall create a new Device object from the response and return with it]
        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);

        Device iotHubDevice = new Device(new DeviceParser(bodyStr));
        return iotHubDevice;
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
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_021: [The constructor shall throw IllegalArgumentException if the input device is null]
        if (Tools.isNullOrEmpty(deviceId))
        {
            throw new IllegalArgumentException("deviceId cannot be null or empty");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_022: [The function shall create an async wrapper around the addDevice() function call, handle the return value or delegate exception]
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
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_023: [The constructor shall throw IllegalArgumentException if the input count number is less than 1]
        if (maxCount < 1)
        {
            throw new IllegalArgumentException("maxCount cannot be less then 1");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_024: [The function shall get the URL for the device]
        URL url = iotHubConnectionString.getUrlDeviceList(maxCount);
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_025: [The function shall create a new SAS token for the device]
        String sasTokenString = new IotHubServiceSasToken(this.iotHubConnectionString).toString();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_026: [The function shall create a new HttpRequest for getting a device list from IotHub]
        HttpRequest request = CreateRequest(url, HttpMethod.GET, new byte[0], sasTokenString);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_027: [The function shall send the created request and get the response]
        HttpResponse response = request.send();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_028: [The function shall verify the response status and throw proper Exception]
        IotHubExceptionManager.httpResponseVerification(response);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_029: [The function shall create a new ArrayList<Device> object from the response and return with it]
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
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_030: [The function shall throw IllegalArgumentException if the input count number is less than 1]
        if (maxCount < 1)
        {
            throw new IllegalArgumentException("maxCount cannot be less then 1");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_031: [The function shall create an async wrapper around the getDevices() function call, handle the return value or delegate exception]
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
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_21_086: [The function shall throw IllegalArgumentException if the input device is null, if deviceId is null, or primary key and primary thumbprint are empty or null.]
        if (device == null)
        {
            throw new IllegalArgumentException("device cannot be null");
        }

        if(Tools.isNullOrEmpty(device.getDeviceId()) || (Tools.isNullOrEmpty(device.getPrimaryKey())) && Tools.isNullOrEmpty(device.getPrimaryThumbprint()))
        {
            throw new IllegalArgumentException("device is not valid");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_21_085: [The function shall return a connectionString for the input device]
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("HostName=%s;", iotHubConnectionString.getHostName()));
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
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_032: [The function shall throw IllegalArgumentException if the input device is null]
        if (device == null)
        {
            throw new IllegalArgumentException("device cannot be null");
        }
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_033: [The function shall call updateDevice with forceUpdate = false]
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
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_034: [The function shall throw IllegalArgumentException if the input device is null]
        if (device == null)
        {
            throw new IllegalArgumentException("device cannot be null");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_035: [The function shall set forceUpdate on the device]
        device.setForceUpdate(forceUpdate);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_036: [The function shall get the URL for the device]
        URL url = iotHubConnectionString.getUrlDevice(device.getDeviceId());
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_037: [The function shall create a new SAS token for the device]
        String sasTokenString = new IotHubServiceSasToken(this.iotHubConnectionString).toString();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_038: [The function shall create a new HttpRequest for updating the device on IotHub]
        HttpRequest request = CreateRequest(url, HttpMethod.PUT, device.toDeviceParser().toJson().getBytes(), sasTokenString);
        request.setHeaderField("If-Match", "*");

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_039: [The function shall send the created request and get the response]
        HttpResponse response = request.send();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_040: [The function shall verify the response status and throw proper Exception]
        IotHubExceptionManager.httpResponseVerification(response);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_041: [The function shall create a new Device object from the response and return with it]
        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);
        Device iotHubDevice = new Device(new DeviceParser(bodyStr));

        return iotHubDevice;
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
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_042: [The function shall throw IllegalArgumentException if the input device is null]
        if (device == null)
        {
            throw new IllegalArgumentException("device cannot be null");
        }
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_043: [The function shall create an async wrapper around the updateDevice() function call, handle the return value or delegate exception]
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
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_044: [The function shall throw IllegalArgumentException if the input device is null]
        if (device == null)
        {
            throw new IllegalArgumentException("device cannot be null");
        }
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_045: [The function shall create an async wrapper around the updateDevice(Device, device, Boolean forceUpdate) function call, handle the return value or delegate exception]
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
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_087: [The function shall use * as the etag]
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
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_081: [The function shall throw IllegalArgumentException if the input device is null]
        if (device == null)
        {
            throw new IllegalArgumentException("device cannot be null or empty");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_084: [The function shall call provide device object's etag as with etag of device to be removed]
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
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_046: [The function shall throw IllegalArgumentException if the input deviceId is null or empty]
        if (Tools.isNullOrEmpty(deviceId))
        {
            throw new IllegalArgumentException("deviceId cannot be null or empty");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_087: [The function shall throw IllegalArgumentException if the input etag is null or empty]
        if (Tools.isNullOrEmpty(etag))
        {
            throw new IllegalArgumentException("etag cannot be null or empty");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_047: [The function shall get the URL for the device]
        URL url = iotHubConnectionString.getUrlDevice(deviceId);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_048: [The function shall create a new SAS token for the device]
        String sasToken = new IotHubServiceSasToken(this.iotHubConnectionString).toString();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_049: [The function shall create a new HttpRequest for removing the device from IotHub]
        HttpRequest request = new HttpRequest(url, HttpMethod.DELETE, new byte[0]);
        request.setReadTimeoutMillis(DEFAULT_HTTP_TIMEOUT_MS);
        request.setHeaderField("authorization", sasToken);
        request.setHeaderField("If-Match", etag);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_050: [The function shall send the created request and get the response]
        HttpResponse response = request.send();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_051: [The function shall verify the response status and throw proper Exception]
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

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_052: [The function shall throw IllegalArgumentException if the input string is null or empty]
        if (Tools.isNullOrEmpty(deviceId))
        {
            throw new IllegalArgumentException("deviceId cannot be null or empty");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_053: [The function shall create an async wrapper around the removeDevice() function call, handle the return value or delegate exception]
        final CompletableFuture<Boolean> future = new CompletableFuture<Boolean>();
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
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_054: [The function shall get the URL for the device]
        URL url = iotHubConnectionString.getUrlDeviceStatistics();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_055: [The function shall create a new SAS token for the device]
        String sasTokenString = new IotHubServiceSasToken(this.iotHubConnectionString).toString();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_056: [The function shall create a new HttpRequest for getting statistics a device from IotHub]
        HttpRequest request = CreateRequest(url, HttpMethod.GET, new byte[0], sasTokenString);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_057: [The function shall send the created request and get the response]
        HttpResponse response = request.send();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_058: [The function shall verify the response status and throw proper Exception]
        IotHubExceptionManager.httpResponseVerification(response);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_059: [The function shall create a new RegistryStatistics object from the response and return with it]
        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);
        RegistryStatistics registryStatistics = new RegistryStatistics(new RegistryStatisticsParser(bodyStr));
        return registryStatistics;
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
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_060: [The function shall create an async wrapper around the getStatistics() function call, handle the return value or delegate exception]
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
        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_061: [The function shall throw IllegalArgumentException
        // if any of the input parameters is null]
        if (exportBlobContainerUri == null || excludeKeys == null)
        {
            throw new IllegalArgumentException("Export blob uri cannot be null");
        }

        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_062: [The function shall get the URL for the bulk export job creation]
        URL url = iotHubConnectionString.getUrlCreateExportImportJob();

        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_063: [The function shall create a new SAS token for the bulk export job]
        String sasTokenString = new IotHubServiceSasToken(this.iotHubConnectionString).toString();

        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_064: [The function shall create a new HttpRequest for the bulk export job creation ]
        String jobPropertiesJson = CreateExportJobPropertiesJson(exportBlobContainerUri, excludeKeys);
        HttpRequest request = CreateRequest(url, HttpMethod.POST, jobPropertiesJson.getBytes(), sasTokenString);

        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_065: [The function shall send the created request and get the response]
        HttpResponse response = request.send();

        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_066: [The function shall verify the response status and throw proper Exception]
        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_067: [The function shall create a new JobProperties object from the response and return it]
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
        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_068: [The function shall create an async wrapper around the
        // exportDevices() function call, handle the return value or delegate exception]
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
        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_062: [The function shall get the URL for the bulk export job creation]
        URL url = iotHubConnectionString.getUrlCreateExportImportJob();

        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_063: [The function shall create a new SAS token for the bulk export job]
        String sasTokenString = new IotHubServiceSasToken(this.iotHubConnectionString).toString();

        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_064: [The function shall create a new HttpRequest for the bulk export job creation ]
        exportDevicesParameters.setType(JobProperties.JobType.EXPORT);
        String jobPropertiesJson = exportDevicesParameters.toJobPropertiesParser().toJson();
        HttpRequest request = CreateRequest(url, HttpMethod.POST, jobPropertiesJson.getBytes(), sasTokenString);

        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_065: [The function shall send the created request and get the response]
        HttpResponse response = request.send();

        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_066: [The function shall verify the response status and throw proper Exception]
        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_067: [The function shall create a new JobProperties object from the response and return it]
        return ProcessJobResponse(response);
    }

    /**
     * Async wrapper for exportDevices() operation
     * @param exportDevicesParameters A JobProperties object containing input parameters for export Devices job
     * @return The future object for the requested operation
     *
     * @throws IllegalArgumentException This exception is thrown if the exportBlobContainerUri or excludeKeys parameters are null
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public CompletableFuture<JobProperties> exportDevicesAsync(JobProperties exportDevicesParameters)
            throws IllegalArgumentException, IOException, IotHubException, JsonSyntaxException
    {
        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_068: [The function shall create an async wrapper around the
        // exportDevices() function call, handle the return value or delegate exception]
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
        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_069: [The function shall throw IllegalArgumentException if any of the input parameters is null]
        if (importBlobContainerUri == null || outputBlobContainerUri == null)
        {
            throw new IllegalArgumentException("Import blob uri or output blob uri cannot be null");
        }

        //CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_070: [The function shall get the URL for the bulk import job creation]
        URL url = iotHubConnectionString.getUrlCreateExportImportJob();

        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_071: [The function shall create a new SAS token for the bulk import job]
        String sasTokenString = new IotHubServiceSasToken(this.iotHubConnectionString).toString();

        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_072: [The function shall create a new HttpRequest for the bulk import job creation]
        String jobPropertiesJson = CreateImportJobPropertiesJson(importBlobContainerUri, outputBlobContainerUri);
        HttpRequest request = CreateRequest(url, HttpMethod.POST, jobPropertiesJson.getBytes(), sasTokenString);

        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_073: [The function shall send the created request and get the response]
        HttpResponse response = request.send();

        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_074: [The function shall verify the response status and throw proper Exception]
        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_075: [The function shall create a new JobProperties object from the response and return it]
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
        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_076: [The function shall create an async wrapper around
        // the importDevices() function call, handle the return value or delegate exception]
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
        //CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_070: [The function shall get the URL for the bulk import job creation]
        URL url = iotHubConnectionString.getUrlCreateExportImportJob();

        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_071: [The function shall create a new SAS token for the bulk import job]
        String sasTokenString = new IotHubServiceSasToken(this.iotHubConnectionString).toString();

        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_072: [The function shall create a new HttpRequest for the bulk import job creation]
        importDevicesParameters.setType(JobProperties.JobType.IMPORT);
        String jobPropertiesJson = importDevicesParameters.toJobPropertiesParser().toJson();
        HttpRequest request = CreateRequest(url, HttpMethod.POST, jobPropertiesJson.getBytes(), sasTokenString);

        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_073: [The function shall send the created request and get the response]
        HttpResponse response = request.send();

        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_074: [The function shall verify the response status and throw proper Exception]
        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_075: [The function shall create a new JobProperties object from the response and return it]
        return ProcessJobResponse(response);
    }

    /**
     * Async wrapper for importDevices() operation
     *
     * @param importParameters A JobProperties object containing input parameters for import Devices job
     * @return The future object for the requested operation
     *
     * @throws IllegalArgumentException This exception is thrown if the exportBlobContainerUri or excludeKeys parameters are null
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public CompletableFuture<JobProperties> importDevicesAsync(JobProperties importParameters)
            throws IllegalArgumentException, IOException, IotHubException, JsonSyntaxException
    {
        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_076: [The function shall create an async wrapper around
        // the importDevices() function call, handle the return value or delegate exception]
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
    public JobProperties getJob(String jobId) throws IllegalArgumentException, IOException, IotHubException, JsonSyntaxException
    {
        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_077: [The function shall throw IllegalArgumentException if the input parameter is null]
        if (jobId == null)
        {
            throw new IllegalArgumentException("Job id cannot be null");
        }

        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_078: [The function shall get the URL for the get request]
        URL url = iotHubConnectionString.getUrlImportExportJob(jobId);

        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_079: [The function shall create a new SAS token for the get request **]
        String sasTokenString = new IotHubServiceSasToken(this.iotHubConnectionString).toString();

        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_080: [The function shall create a new HttpRequest for getting the properties of a job]
        HttpRequest request = CreateRequest(url, HttpMethod.GET, new byte[0], sasTokenString);

        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_081: [The function shall send the created request and get the response]
        HttpResponse response = request.send();

        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_082: [The function shall verify the response status and throw proper Exception ]
        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_083: [The function shall create a new JobProperties object from the response and return it]
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
        // CODES_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_084: [The function shall create an async wrapper around
        // the getJob() function call, handle the return value or delegate exception]
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
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_001: [The constructor shall throw IllegalArgumentException if the input module is null]
        if (module == null)
        {
            throw new IllegalArgumentException("module cannot be null");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_002: [The function shall deserialize the given module object to Json string]
        String moduleJson = module.toDeviceParser().toJson();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_003: [The function shall get the URL for the module]
        URL url = iotHubConnectionString.getUrlModule(module.getDeviceId(), module.getId());
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_004: [The function shall create a new SAS token for the module]
        String sasTokenString = new IotHubServiceSasToken(this.iotHubConnectionString).toString();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_005: [The function shall create a new HttpRequest for adding the module to IotHub]
        HttpRequest request = CreateRequest(url, HttpMethod.PUT, moduleJson.getBytes(), sasTokenString);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_006: [The function shall send the created request and get the response]
        HttpResponse response = request.send();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_007: [The function shall verify the response status and throw proper Exception]
        IotHubExceptionManager.httpResponseVerification(response);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_008: [The function shall create a new Module object from the response and return with it]
        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);

        Module iotHubModule = new Module(new DeviceParser(bodyStr));

        return iotHubModule;
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
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_009: [The constructor shall throw IllegalArgumentException if the deviceId string is null or empty]
        if (Tools.isNullOrEmpty(deviceId))
        {
            throw new IllegalArgumentException("deviceId cannot be null or empty");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_010: [The constructor shall throw IllegalArgumentException if the moduleId string is null or empty]
        if (Tools.isNullOrEmpty(moduleId))
        {
            throw new IllegalArgumentException("moduleId cannot be null or empty");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_011: [The function shall get the URL for the device]
        URL url = iotHubConnectionString.getUrlModule(deviceId, moduleId);
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_012: [The function shall create a new SAS token for the device]
        String sasTokenString = new IotHubServiceSasToken(this.iotHubConnectionString).toString();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_013: [The function shall create a new HttpRequest for getting a device from IotHub]
        HttpRequest request = CreateRequest(url, HttpMethod.GET, new byte[0], sasTokenString);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_014: [The function shall send the created request and get the response]
        HttpResponse response = request.send();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_015: [The function shall verify the response status and throw proper Exception]
        IotHubExceptionManager.httpResponseVerification(response);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_016: [The function shall create a new Device object from the response and return with it]
        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);

        Module iotHubModule = new Module(new DeviceParser(bodyStr));
        return iotHubModule;
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
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_017: [The constructor shall throw IllegalArgumentException if the input string is null or empty]
        if (Tools.isNullOrEmpty(deviceId))
        {
            throw new IllegalArgumentException("deviceId cannot be null or empty");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_018: [The function shall get the URL for the device]
        URL url = iotHubConnectionString.getUrlModulesOnDevice(deviceId);
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_019: [The function shall create a new SAS token for the device]
        String sasTokenString = new IotHubServiceSasToken(this.iotHubConnectionString).toString();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_020: [The function shall create a new HttpRequest for getting a device from IotHub]
        HttpRequest request = CreateRequest(url, HttpMethod.GET, new byte[0], sasTokenString);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_021: [The function shall send the created request and get the response]
        HttpResponse response = request.send();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_022: [The function shall verify the response status and throw proper Exception]
        IotHubExceptionManager.httpResponseVerification(response);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_023: [The function shall create a new List<Modules> object from the response and return with it]
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
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_024: [The function shall throw IllegalArgumentException if the input module is null]
        if (module == null)
        {
            throw new IllegalArgumentException("module cannot be null");
        }
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_025: [The function shall call updateModule with forceUpdate = false]
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
    public Module updateModule(Module module, Boolean forceUpdate) throws IOException, IotHubException, JsonSyntaxException
    {
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_026: [The function shall throw IllegalArgumentException if the input module is null]
        if (module == null)
        {
            throw new IllegalArgumentException("module cannot be null");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_027: [The function shall set forceUpdate on the module]
        module.setForceUpdate(forceUpdate);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_028: [The function shall get the URL for the module]
        URL url = iotHubConnectionString.getUrlModule(module.getDeviceId(), module.getId());
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_029: [The function shall create a new SAS token for the module]
        String sasTokenString = new IotHubServiceSasToken(this.iotHubConnectionString).toString();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_030: [The function shall create a new HttpRequest for updating the module on IotHub]
        HttpRequest request = CreateRequest(url, HttpMethod.PUT, module.toDeviceParser().toJson().getBytes(), sasTokenString);
        request.setHeaderField("If-Match", "*");

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_031: [The function shall send the created request and get the response]
        HttpResponse response = request.send();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_032: [The function shall verify the response status and throw proper Exception]
        IotHubExceptionManager.httpResponseVerification(response);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_033: [The function shall create a new Module object from the response and return with it]
        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);
        Module iotHubModule = new Module(new DeviceParser(bodyStr));

        return iotHubModule;
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
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_086: [The function shall use * as the etag]
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
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_082: [The function shall throw IllegalArgumentException if the input module is null]
        if (module == null)
        {
            throw new IllegalArgumentException("module cannot be null or empty");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_083: [The function shall use the module's object etag as the etag module to be remove]
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
    private void removeModuleOperation(String deviceId, String moduleId, String etag) throws IOException, IotHubException
    {
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_034: [The function shall throw IllegalArgumentException if the deviceId is null or empty]
        if (Tools.isNullOrEmpty(deviceId))
        {
            throw new IllegalArgumentException("deviceId cannot be null or empty");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_035: [The function shall throw IllegalArgumentException if the moduleId is null or empty]
        if (Tools.isNullOrEmpty(moduleId))
        {
            throw new IllegalArgumentException("moduleId cannot be null or empty");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_078: [The function shall throw IllegalArgumentException if the etag is null or empty]
        if (Tools.isNullOrEmpty(etag))
        {
            throw new IllegalArgumentException("etag cannot be null or empty");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_036: [The function shall get the URL for the module]
        URL url = iotHubConnectionString.getUrlModule(deviceId, moduleId);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_037: [The function shall create a new SAS token for the module]
        String sasToken = new IotHubServiceSasToken(this.iotHubConnectionString).toString();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_038: [The function shall create a new HttpRequest for removing the module from IotHub]
        HttpRequest request = new HttpRequest(url, HttpMethod.DELETE, new byte[0]);
        request.setReadTimeoutMillis(DEFAULT_HTTP_TIMEOUT_MS);
        request.setHeaderField("authorization", sasToken);
        request.setHeaderField("If-Match", etag);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_039: [The function shall send the created request and get the response]
        HttpResponse response = request.send();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_040: [The function shall verify the response status and throw proper Exception]
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
    public Configuration addConfiguration(Configuration configuration) throws IOException, IotHubException, JsonSyntaxException
    {
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_041: [The constructor shall throw IllegalArgumentException if the input configuration is null]
        if (configuration == null)
        {
            throw new IllegalArgumentException("configuration cannot be null");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_042: [The function shall deserialize the given configuration object to Json string]
        String configurationJson = configuration.toConfigurationParser().toJson();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_043: [The function shall get the URL for the configuration]
        URL url = iotHubConnectionString.getUrlConfiguration(configuration.getId());
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_044: [The function shall create a new SAS token for the configuration]
        String sasTokenString = new IotHubServiceSasToken(this.iotHubConnectionString).toString();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_045: [The function shall create a new HttpRequest for adding the configuration to IotHub]
        HttpRequest request = CreateRequest(url, HttpMethod.PUT, configurationJson.getBytes(), sasTokenString);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_046: [The function shall send the created request and get the response]
        HttpResponse response = request.send();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_047: [The function shall verify the response status and throw proper Exception]
        IotHubExceptionManager.httpResponseVerification(response);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_048: [The function shall create a new Configuration object from the response and return with it]
        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);

        Configuration iotHubConfiguration = new Configuration(new ConfigurationParser(bodyStr));

        return iotHubConfiguration;
    }

    /**
     * Get configuration by configuration Id from IotHub
     *
     * @param configurationId The id of requested configuration
     * @return The configuration object of requested configuration on the specific device
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public Configuration getConfiguration(String configurationId) throws IOException, IotHubException, JsonSyntaxException
    {
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_049: [The constructor shall throw IllegalArgumentException if the input string is null or empty]
        if (Tools.isNullOrEmpty(configurationId))
        {
            throw new IllegalArgumentException("configurationId cannot be null or empty");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_050: [The function shall get the URL for the device]
        URL url = iotHubConnectionString.getUrlConfiguration(configurationId);
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_051: [The function shall create a new SAS token for the device]
        String sasTokenString = new IotHubServiceSasToken(this.iotHubConnectionString).toString();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_052: [The function shall create a new HttpRequest for getting a device from IotHub]
        HttpRequest request = CreateRequest(url, HttpMethod.GET, new byte[0], sasTokenString);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_053: [The function shall send the created request and get the response]
        HttpResponse response = request.send();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_054: [The function shall verify the response status and throw proper Exception]
        IotHubExceptionManager.httpResponseVerification(response);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_055: [The function shall create a new Device object from the response and return with it]
        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);

        Configuration iotHubConfiguration = new Configuration(new ConfigurationParser(bodyStr));
        return iotHubConfiguration;
    }

    /**
     * Get list of Configuration
     *
     * @param maxCount The requested count of configurations
     * @return The array of requested configuration objects
     * @throws IOException This exception is thrown if the IO operation failed
     * @throws IotHubException This exception is thrown if the response verification failed
     */
    public List<Configuration> getConfigurations(Integer maxCount) throws IOException, IotHubException, JsonSyntaxException
    {
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_056: [The function shall throw IllegalArgumentException if the input count number is less than 1]
        if (maxCount < 1)
        {
            throw new IllegalArgumentException("maxCount cannot be less then 1");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_057: [The function shall get the URL for the device]
        URL url = iotHubConnectionString.getUrlConfigurationsList(maxCount);
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_058: [The function shall create a new SAS token for the device]
        String sasTokenString = new IotHubServiceSasToken(this.iotHubConnectionString).toString();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_059: [The function shall create a new HttpRequest for getting a device from IotHub]
        HttpRequest request = CreateRequest(url, HttpMethod.GET, new byte[0], sasTokenString);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_060: [The function shall send the created request and get the response]
        HttpResponse response = request.send();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_061: [The function shall verify the response status and throw proper Exception]
        IotHubExceptionManager.httpResponseVerification(response);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_062: [The function shall create a new ArrayList<Configuration> object from the response and return with it]
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
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_063: [The function shall throw IllegalArgumentException if the configuration is null]
        if (configuration == null)
        {
            throw new IllegalArgumentException("configuration cannot be null");
        }
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_064: [The function shall call updateConfiguration with forceUpdate = false]
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
    public Configuration updateConfiguration(Configuration configuration, Boolean forceUpdate) throws IOException, IotHubException, JsonSyntaxException
    {
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_065: [The function shall throw IllegalArgumentException if the input configuration is null]
        if (configuration == null)
        {
            throw new IllegalArgumentException("configuration cannot be null");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_066: [The function shall set forceUpdate on the configuration]
        configuration.setForceUpdate(forceUpdate);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_067: [The function shall get the URL for the configuration]
        URL url = iotHubConnectionString.getUrlConfiguration(configuration.getId());
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_068: [The function shall create a new SAS token for the configuration]
        String sasTokenString = new IotHubServiceSasToken(this.iotHubConnectionString).toString();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_069: [The function shall create a new HttpRequest for updating the device on IotHub]
        HttpRequest request = CreateRequest(url, HttpMethod.PUT, configuration.toConfigurationParser().toJson().getBytes(), sasTokenString);
        request.setHeaderField("If-Match", "*");

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_070: [The function shall send the created request and get the response]
        HttpResponse response = request.send();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_071: [The function shall verify the response status and throw proper Exception]
        IotHubExceptionManager.httpResponseVerification(response);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_072: [The function shall create a new Configuration object from the response and return with it]
        String bodyStr = new String(response.getBody(), StandardCharsets.UTF_8);
        Configuration iotHubConfiguration = new Configuration(new ConfigurationParser(bodyStr));

        return iotHubConfiguration;
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
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_085: [The function shall call removeDeviceOperation with * as the etag]
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
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_082: [The function shall throw IllegalArgumentException if the input module is null]
        if (config == null)
        {
            throw new IllegalArgumentException("configuration cannot be null or empty");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_085: [The function shall use the configuration object's etag as the etag for the configuration to be removed]
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
        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_073: [The function shall throw IllegalArgumentException if the input string is null or empty]
        if (Tools.isNullOrEmpty(configurationId))
        {
            throw new IllegalArgumentException("configurationId cannot be null or empty");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_080: [The function shall throw IllegalArgumentException if the input etag is null or empty]
        if (Tools.isNullOrEmpty(etag))
        {
            throw new IllegalArgumentException("etag cannot be null or empty");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_074: [The function shall get the URL for the configuration]
        URL url = iotHubConnectionString.getUrlConfiguration(configurationId);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_075: [The function shall create a new SAS token for the configuration]
        String sasToken = new IotHubServiceSasToken(this.iotHubConnectionString).toString();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_076: [The function shall create a new HttpRequest for removing the configuration from IotHub]
        HttpRequest request = new HttpRequest(url, HttpMethod.DELETE, new byte[0]);
        request.setReadTimeoutMillis(DEFAULT_HTTP_TIMEOUT_MS);
        request.setHeaderField("authorization", sasToken);
        request.setHeaderField("If-Match", etag);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_077: [The function shall send the created request and get the response]
        HttpResponse response = request.send();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_078: [The function shall verify the response status and throw proper Exception]
        IotHubExceptionManager.httpResponseVerification(response);
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
            // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_34_088: [The function shall throw IllegalArgumentException if the provided content is null]
            throw new IllegalArgumentException("content cannot be null");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_34_089: [The function shall get the URL from the connection string using the provided deviceId]
        URL url = iotHubConnectionString.getUrlApplyConfigurationContent(deviceId);

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_34_090: [The function shall create a new SAS token for the configuration]
        String sasTokenString = new IotHubServiceSasToken(this.iotHubConnectionString).toString();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_34_091: [The function shall send a new HTTP POST request with the created url, sas token, and the provided content in json form as the body.]
        HttpRequest request = CreateRequest(url, HttpMethod.POST, content.toConfigurationContentParser().toJson().getBytes(), sasTokenString);
        HttpResponse response = request.send();

        // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_34_092: [The function shall verify the response status and throw proper Exception]
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
        JobProperties resultJobProperties = new JobProperties(new JobPropertiesParser(bodyStr));
        return resultJobProperties;
    }

    private HttpRequest CreateRequest(URL url, HttpMethod method, byte[] payload, String sasToken) throws IOException
    {
        HttpRequest request = new HttpRequest(url, method, payload);
        request.setReadTimeoutMillis(DEFAULT_HTTP_TIMEOUT_MS);
        request.setHeaderField("authorization", sasToken);
        request.setHeaderField("Request-Id", "1001");
        request.setHeaderField("Accept", "application/json");
        request.setHeaderField("Content-Type", "application/json");
        request.setHeaderField("charset", "utf-8");
        return request;
    }
}