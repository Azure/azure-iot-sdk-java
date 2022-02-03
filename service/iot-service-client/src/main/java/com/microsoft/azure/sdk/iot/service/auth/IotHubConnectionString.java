/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.auth;

import com.microsoft.azure.sdk.iot.service.transport.TransportUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Expose all connections string properties and methods
 * for user, device and connection string serialization.
 */
public class IotHubConnectionString extends IotHubConnectionStringBuilder
{
    static final String VALUE_PAIR_DELIMITER = ";";
    static final String VALUE_PAIR_SEPARATOR = "=";
    static final String HOST_NAME_SEPARATOR = ".";

    static final String HOST_NAME_PROPERTY_NAME = "HostName";
    static final String SHARED_ACCESS_KEY_NAME_PROPERTY_NAME = "SharedAccessKeyName";
    static final String SHARED_ACCESS_KEY_PROPERTY_NAME = "SharedAccessKey";
    static final String SHARED_ACCESS_SIGNATURE_PROPERTY_NAME = "SharedAccessSignature";

    // Included in the device connection string
    String hostName;
    String iotHubName;
    AuthenticationMethod authenticationMethod;
    String sharedAccessKeyName;
    String sharedAccessKey;
    String sharedAccessSignature;

    // Connection
    private static final String URL_SEPARATOR_0 = "/";
    private static final String URL_SEPARATOR_1 = "?";
    private static final String URL_SEPARATOR_2 = "&";
    private static final String URL_HTTPS = "https:" + URL_SEPARATOR_0 + URL_SEPARATOR_0;
    private static final String URL_PATH_DEVICES = "devices";
    private static final String URL_PATH_MODULES = "modules";
    private static final String URL_PATH_CONFIG = "configurations";
    private static final String URL_API_VERSION = "api-version=" + TransportUtils.IOTHUB_API_VERSION;
    private static final String URL_MAX_COUNT = "top=";
    private static final String URL_PATH_DEVICESTATISTICS = "statistics";
    private static final String USER_SEPARATOR = "@";
    private static final String USER_SAS = "SAS.";
    private static final String USER_ROOT = "root.";

    // twin
    private static final String URL_PATH_TWIN = "twins";
    private static final String URL_PATH_METHODS = "methods";
    private static final String URL_PATH_TWIN_DEVICES = "devices";

    // jobs
    private static final String URL_PATH_JOBS = "jobs";
    private static final String URL_PATH_VERSION = "v2";
    private static final String URL_PATH_QUERY = "query";
    private static final String URL_PATH_JOB_TYPE = "jobType";
    private static final String URL_PATH_JOB_STATUS = "jobStatus";
    private static final String URL_PATH_CANCEL = "cancel";

    // configurations
    private static final String URL_PATH_APPLY_CONTENT_CONFIGURATION = "applyConfigurationContent";

    IotHubConnectionString()
    {

    }

    /**
     * Serialize user string
     *
     * @return The user string in the following format: "SharedAccessKeyName@SAS.root.IotHubName"
     */
    public String getUserString()
    {
        return this.sharedAccessKeyName +
                USER_SEPARATOR +
                USER_SAS +
                USER_ROOT +
                this.iotHubName;
    }

    /**
     * Create url for requesting device method
     *
     * @param deviceId The name of the device
     * @return The Url in the following format: "https:hostname/twins/deviceId/methods/"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     * @throws IllegalArgumentException This exception is thrown if device id is null or empty
     */
    public URL getUrlMethod(String deviceId) throws MalformedURLException, IllegalArgumentException
    {
        return getUrlMethod(this.hostName, deviceId);
    }

    /**
     * Create url for requesting device method
     *
     * @param hostName The hostname of the IoT Hub
     * @param deviceId The name of the device
     * @return The Url in the following format: "https:hostname/twins/deviceId/methods/"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     * @throws IllegalArgumentException This exception is thrown if device id is null or empty
     */
    public static URL getUrlMethod(String hostName, String deviceId) throws MalformedURLException, IllegalArgumentException
    {
        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("device name cannot be empty or null");
        }

        String stringBuilder = URL_HTTPS +
                hostName +
                URL_SEPARATOR_0 +
                URL_PATH_TWIN +
                URL_SEPARATOR_0 +
                deviceId +
                URL_SEPARATOR_0 +
                URL_PATH_METHODS +
                URL_SEPARATOR_1 +
                URL_API_VERSION;
        return new URL(stringBuilder);
    }

    /**
     * Create url for requesting device method for module
     *
     * @param deviceId The name of the device
     * @param moduleId The name of the module
     * @return The Url in the following format: "https:hostname/twins/deviceId/methods/"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     * @throws IllegalArgumentException This exception is thrown if device id is null or empty
     */
    public URL getUrlModuleMethod(String deviceId, String moduleId) throws MalformedURLException, IllegalArgumentException
    {
        return getUrlModuleMethod(this.hostName, deviceId, moduleId);
    }

    /**
     * Create url for requesting device method for module
     *
     * @param hostName The hostname of the IoT Hub
     * @param deviceId The name of the device
     * @param moduleId The name of the module
     * @return The Url in the following format: "https:hostname/twins/deviceId/methods/"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     * @throws IllegalArgumentException This exception is thrown if device id is null or empty
     */
    public static URL getUrlModuleMethod(String hostName, String deviceId, String moduleId) throws MalformedURLException, IllegalArgumentException
    {
        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("device name cannot be empty or null");
        }

        if (moduleId == null || moduleId.isEmpty())
        {
            throw new IllegalArgumentException("module name cannot be empty or null");
        }

        String stringBuilder = URL_HTTPS +
                hostName +
                URL_SEPARATOR_0 +
                URL_PATH_TWIN +
                URL_SEPARATOR_0 +
                deviceId +
                URL_SEPARATOR_0 +
                URL_PATH_MODULES +
                URL_SEPARATOR_0 +
                moduleId +
                URL_SEPARATOR_0 +
                URL_PATH_METHODS +
                URL_SEPARATOR_1 +
                URL_API_VERSION;
        return new URL(stringBuilder);
    }

    /**
     * Create url for requesting jobs
     *
     * @param jobId is the name of the job
     * @return the URL in the follow format: "https:[hostname]/jobs/v2/[jobId]?api-version=2016-11-14"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     * @throws IllegalArgumentException This exception is thrown if job id is null or empty
     */
    public URL getUrlJobs(String jobId) throws MalformedURLException, IllegalArgumentException
    {
        return getUrlJobs(this.hostName, jobId);
    }

    /**
     * Create url for requesting jobs
     *
     * @param hostName The hostname of the IoT Hub
     * @param jobId is the name of the job
     * @return the URL in the follow format: "https:[hostname]/jobs/v2/[jobId]?api-version=2016-11-14"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     * @throws IllegalArgumentException This exception is thrown if job id is null or empty
     */
    public static URL getUrlJobs(String hostName, String jobId) throws MalformedURLException, IllegalArgumentException
    {
        if (jobId == null || jobId.isEmpty())
        {
            throw new IllegalArgumentException("job name cannot be empty or null");
        }

        String stringBuilder = URL_HTTPS +
                hostName +
                URL_SEPARATOR_0 +
                URL_PATH_JOBS +
                URL_SEPARATOR_0 +
                URL_PATH_VERSION +
                URL_SEPARATOR_0 +
                jobId +
                URL_SEPARATOR_1 +
                URL_API_VERSION;
        return new URL(stringBuilder);
    }

    /**
     * Create url for querying twin
     *
     * @return the URL in the follow format: "https:[hostname]/devices/query?api-version=2016-11-14"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     */
    public URL getUrlTwinQuery() throws MalformedURLException
    {
        return getUrlTwinQuery(this.hostName);
    }

    /**
     * Create url for querying twin
     *
     * @param hostName The hostname of the IoT Hub
     * @return the URL in the follow format: "https:[hostname]/devices/query?api-version=2016-11-14"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     */
    public static URL getUrlTwinQuery(String hostName) throws MalformedURLException
    {
        String stringBuilder = URL_HTTPS +
                hostName +
                URL_SEPARATOR_0 +
                URL_PATH_TWIN_DEVICES +
                URL_SEPARATOR_0 +
                URL_PATH_QUERY +
                URL_SEPARATOR_1 +
                URL_API_VERSION;
        return new URL(stringBuilder);
    }

    /**
     * Create url for querying
     * @param jobStatus jobStatus as String
     * @param jobType jobType as String
     * @return the URL in the follow format: "https:[hostname]/jobs/v2/query?jobType=jobTypeValue&amp;jobStatus=jobStatusValue&amp;api-version=2016-11-14"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     */
    public URL getUrlQuery(String jobType, String jobStatus) throws MalformedURLException
    {
        return getUrlQuery(this.hostName, jobType, jobStatus);
    }

    /**
     * Create url for querying
     * @param hostName The hostname of the IoT Hub
     * @param jobStatus jobStatus as String
     * @param jobType jobType as String
     * @return the URL in the follow format: "https:[hostname]/jobs/v2/query?jobType=jobTypeValue&amp;jobStatus=jobStatusValue&amp;api-version=2016-11-14"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     */
    public static URL getUrlQuery(String hostName, String jobType, String jobStatus) throws MalformedURLException
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(URL_HTTPS);
        stringBuilder.append(hostName);
        stringBuilder.append(URL_SEPARATOR_0);
        stringBuilder.append(URL_PATH_JOBS);
        stringBuilder.append(URL_SEPARATOR_0);
        stringBuilder.append(URL_PATH_VERSION);
        stringBuilder.append(URL_SEPARATOR_0);

        stringBuilder.append(URL_PATH_QUERY);
        stringBuilder.append(URL_SEPARATOR_1);

        if (!(jobType == null || jobType.isEmpty()))
        {
            stringBuilder.append(URL_PATH_JOB_TYPE);
            stringBuilder.append(VALUE_PAIR_SEPARATOR);
            stringBuilder.append(jobType);
            stringBuilder.append(URL_SEPARATOR_2);
        }

        if (!(jobStatus == null || jobStatus.isEmpty()))
        {
            stringBuilder.append(URL_PATH_JOB_STATUS);
            stringBuilder.append(VALUE_PAIR_SEPARATOR);
            stringBuilder.append(jobStatus);
            stringBuilder.append(URL_SEPARATOR_2);
        }

        stringBuilder.append(URL_API_VERSION);
        return new URL(stringBuilder.toString());
    }

    /**
     * Create url for cancelling jobs
     *
     * @param jobId is the name of the job
     * @return the URL in the follow format: "https:[hostname]/jobs/v2/[jobId]/cancel?api-version=2016-11-14"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     * @throws IllegalArgumentException This exception is thrown if job id is null or empty
     */
    public URL getUrlJobsCancel(String jobId) throws MalformedURLException, IllegalArgumentException
    {
        return getUrlJobsCancel(this.hostName, jobId);
    }

    /**
     * Create url for cancelling jobs
     *
     * @param hostName The hostname of the IoT Hub
     * @param jobId is the name of the job
     * @return the URL in the follow format: "https:[hostname]/jobs/v2/[jobId]/cancel?api-version=2016-11-14"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     * @throws IllegalArgumentException This exception is thrown if job id is null or empty
     */
    public static URL getUrlJobsCancel(String hostName, String jobId) throws MalformedURLException, IllegalArgumentException
    {
        if (jobId == null || jobId.isEmpty())
        {
            throw new IllegalArgumentException("job name cannot be empty or null");
        }

        String stringBuilder = URL_HTTPS +
                hostName +
                URL_SEPARATOR_0 +
                URL_PATH_JOBS +
                URL_SEPARATOR_0 +
                URL_PATH_VERSION +
                URL_SEPARATOR_0 +
                jobId +
                URL_SEPARATOR_0 +
                URL_PATH_CANCEL +
                URL_SEPARATOR_1 +
                URL_API_VERSION;
        return new URL(stringBuilder);
    }

    /**
     * Create url for requesting device twin
     *
     * @param deviceId The name of the device
     * @return The Url in the following format: "https:hostname/twins/deviceId?api-version=201X-XX-XX"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     * @throws IllegalArgumentException This exception is thrown if device id is null or empty
     */
    public URL getUrlTwin(String deviceId) throws MalformedURLException, IllegalArgumentException
    {
        return getUrlTwin(this.hostName, deviceId);
    }

    /**
     * Create url for requesting device twin
     *
     * @param hostName The hostname of the IoT Hub
     * @param deviceId The name of the device
     * @return The Url in the following format: "https:hostname/twins/deviceId?api-version=201X-XX-XX"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     * @throws IllegalArgumentException This exception is thrown if device id is null or empty
     */
    public static URL getUrlTwin(String hostName, String deviceId) throws MalformedURLException, IllegalArgumentException
    {
        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("device name cannot be empty or null");
        }

        String stringBuilder = URL_HTTPS +
                hostName +
                URL_SEPARATOR_0 +
                URL_PATH_TWIN +
                URL_SEPARATOR_0 +
                deviceId +
                URL_SEPARATOR_1 +
                URL_API_VERSION;
        return new URL(stringBuilder);
    }

    /**
     * Create url for requesting module twin
     *
     * @param deviceId The name of the device
     * @param moduleId The name of the device
     * @return The Url in the following format: "https:hostname/twins/deviceId/modules/moduleId?api-version=201X-XX-XX"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     * @throws IllegalArgumentException This exception is thrown if device id is null or empty
     */
    public URL getUrlModuleTwin(String deviceId, String moduleId) throws MalformedURLException, IllegalArgumentException
    {
        return getUrlModuleTwin(this.hostName, deviceId, moduleId);
    }

    /**
     * Create url for requesting module twin
     *
     * @param hostName The hostname of the IoT Hub
     * @param deviceId The name of the device
     * @param moduleId The name of the device
     * @return The Url in the following format: "https:hostname/twins/deviceId/modules/moduleId?api-version=201X-XX-XX"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     * @throws IllegalArgumentException This exception is thrown if device id is null or empty
     */
    public static URL getUrlModuleTwin(String hostName, String deviceId, String moduleId) throws MalformedURLException, IllegalArgumentException
    {
        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("device name cannot be empty or null");
        }

        if (moduleId == null || moduleId.isEmpty())
        {
            throw new IllegalArgumentException("module id cannot be empty or null");
        }

        String stringBuilder = URL_HTTPS +
                hostName +
                URL_SEPARATOR_0 +
                URL_PATH_TWIN +
                URL_SEPARATOR_0 +
                deviceId +
                URL_SEPARATOR_0 +
                URL_PATH_MODULES +
                URL_SEPARATOR_0 +
                moduleId +
                URL_SEPARATOR_1 +
                URL_API_VERSION;
        return new URL(stringBuilder);
    }

    /**
     * Create url for requesting device data
     *
     * @param deviceId The name of the device
     * @return The device Url in the following format: "https:hostname/devices/deviceId?api-version=201X-XX-XX"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     * @throws IllegalArgumentException This exception is thrown if device id is null or empty
     */
    public URL getUrlDevice(String deviceId) throws MalformedURLException, IllegalArgumentException
    {
        return getUrlDevice(this.hostName, deviceId);
    }

    /**
     * Create url for requesting device data
     *
     * @param hostName The hostname of the IoT Hub
     * @param deviceId The name of the device
     * @return The device Url in the following format: "https:hostname/devices/deviceId?api-version=201X-XX-XX"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     * @throws IllegalArgumentException This exception is thrown if device id is null or empty
     */
    public static URL getUrlDevice(String hostName, String deviceId) throws MalformedURLException, IllegalArgumentException
    {
        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("device name cannot be empty or null");
        }

        String stringBuilder = URL_HTTPS +
                hostName +
                URL_SEPARATOR_0 +
                URL_PATH_DEVICES +
                URL_SEPARATOR_0 +
                deviceId +
                URL_SEPARATOR_1 +
                URL_API_VERSION;
        return new URL(stringBuilder);
    }

    /**
     * Create url for requesting module data
     *
     * @param deviceId The name of the device
     * @param moduleId The name of the device
     * @return The device Url in the following format: "https:hostname/devices/deviceId/modules/moduleId?api-version=201X-XX-XX"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     * @throws IllegalArgumentException This exception is thrown if device id is null or empty
     */
    public URL getUrlModule(String deviceId, String moduleId) throws MalformedURLException, IllegalArgumentException
    {
        return getUrlModule(this.hostName, deviceId, moduleId);
    }

    /**
     * Create url for requesting module data
     *
     * @param hostName The hostname of the IoT Hub
     * @param deviceId The name of the device
     * @param moduleId The name of the device
     * @return The device Url in the following format: "https:hostname/devices/deviceId/modules/moduleId?api-version=201X-XX-XX"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     * @throws IllegalArgumentException This exception is thrown if device id is null or empty
     */
    public static URL getUrlModule(String hostName, String deviceId, String moduleId) throws MalformedURLException, IllegalArgumentException
    {
        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("device name cannot be empty or null");
        }

        if (moduleId == null || moduleId.isEmpty())
        {
            throw new IllegalArgumentException("module name cannot be empty or null");
        }

        String stringBuilder = URL_HTTPS +
                hostName +
                URL_SEPARATOR_0 +
                URL_PATH_DEVICES +
                URL_SEPARATOR_0 +
                deviceId +
                URL_SEPARATOR_0 +
                URL_PATH_MODULES +
                URL_SEPARATOR_0 +
                moduleId +
                URL_SEPARATOR_1 +
                URL_API_VERSION;
        return new URL(stringBuilder);
    }

    /**
     * Create the url needed to apply some configuration content to a device
     * @param deviceId The device to apply the configuration content to
     * @return The device Url in the following format: "https:[hostname]/devices/[deviceId]/applyConfigurationContent?api-version=201X-XX-XX"
     * @throws MalformedURLException if the deviceId contains unexpected characters, and a URL cannot be constructed using it
     * @throws IllegalArgumentException if deviceId is null or empty
     */
    public URL getUrlApplyConfigurationContent(String deviceId) throws MalformedURLException, IllegalArgumentException
    {
        return getUrlApplyConfigurationContent(this.hostName, deviceId);
    }

    /**
     * Create the url needed to apply some configuration content to a device
     *
     * @param hostName The hostname of the IoT Hub
     * @param deviceId The device to apply the configuration content to
     * @return The device Url in the following format: "https:[hostname]/devices/[deviceId]/applyConfigurationContent?api-version=201X-XX-XX"
     * @throws MalformedURLException if the deviceId contains unexpected characters, and a URL cannot be constructed using it
     * @throws IllegalArgumentException if deviceId is null or empty
     */
    public static URL getUrlApplyConfigurationContent(String hostName, String deviceId) throws MalformedURLException, IllegalArgumentException
    {
        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("deviceId cannot be null");
        }

        String stringBuilder = URL_HTTPS +
                hostName +
                URL_SEPARATOR_0 +
                URL_PATH_DEVICES +
                URL_SEPARATOR_0 +
                deviceId +
                URL_SEPARATOR_0 +
                URL_PATH_APPLY_CONTENT_CONFIGURATION +
                URL_SEPARATOR_1 +
                URL_API_VERSION;
        return new URL(stringBuilder);
    }

    /**
     * Create url for requesting configuration data
     *
     * @param configurationId The name of the configuration
     * @return The device Url in the following format: "https:hostname/configurations/configurationId?api-version=201X-XX-XX"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     * @throws IllegalArgumentException This exception is thrown if device id is null or empty
     */
    public URL getUrlConfiguration(String configurationId) throws MalformedURLException, IllegalArgumentException
    {
        return getUrlConfiguration(this.hostName, configurationId);
    }

    /**
     * Create url for requesting configuration data
     *
     * @param hostName The hostname of the IoT Hub
     * @param configurationId The name of the configuration
     * @return The device Url in the following format: "https:hostname/configurations/configurationId?api-version=201X-XX-XX"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     * @throws IllegalArgumentException This exception is thrown if device id is null or empty
     */
    public static URL getUrlConfiguration(String hostName, String configurationId) throws MalformedURLException, IllegalArgumentException
    {
        if (configurationId == null || configurationId.isEmpty())
        {
            throw new IllegalArgumentException("configuration id cannot be empty or null");
        }

        String stringBuilder = URL_HTTPS +
                hostName +
                URL_SEPARATOR_0 +
                URL_PATH_CONFIG +
                URL_SEPARATOR_0 +
                configurationId +
                URL_SEPARATOR_1 +
                URL_API_VERSION;
        return new URL(stringBuilder);
    }

    /**
     * Create url for requesting configuration data
     *
     * @param maxCount The maximum number of configuration data to return
     * @return The Url in the following format: "https:hostname/configurations/?top=maxcount{@literal &}api-version=201X-XX-XX"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     * @throws IllegalArgumentException This exception is thrown if device id is null or empty
     */
    public URL getUrlConfigurationsList(Integer maxCount) throws MalformedURLException, IllegalArgumentException
    {
        return getUrlConfigurationsList(this.hostName, maxCount);
    }

    /**
     * Create url for requesting configuration data
     *
     * @param hostName The hostname of the IoT Hub
     * @param maxCount The maximum number of configuration data to return
     * @return The Url in the following format: "https:hostname/configurations/?top=maxcount{@literal &}api-version=201X-XX-XX"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     * @throws IllegalArgumentException This exception is thrown if device id is null or empty
     */
    public static URL getUrlConfigurationsList(String hostName, Integer maxCount) throws MalformedURLException, IllegalArgumentException
    {
        if ((maxCount == null) || (maxCount < 1))
        {
            throw new IllegalArgumentException("maxCount cannot be null or less then 1");
        }

        String stringBuilder = URL_HTTPS +
                hostName +
                URL_SEPARATOR_0 +
                URL_PATH_CONFIG +
                URL_SEPARATOR_1 +
                URL_MAX_COUNT +
                maxCount.toString() +
                URL_SEPARATOR_2 +
                URL_API_VERSION;
        return new URL(stringBuilder);
    }

    /**
     * Create url for requesting all modules data on a device
     *
     * @param deviceId The name of the device
     * @return The device Url in the following format: "https:hostname/devices/deviceId?api-version=201X-XX-XX"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     * @throws IllegalArgumentException This exception is thrown if device id is null or empty
     */
    public URL getUrlModulesOnDevice(String deviceId) throws MalformedURLException, IllegalArgumentException
    {
        return getUrlModulesOnDevice(this.hostName, deviceId);
    }

    /**
     * Create url for requesting all modules data on a device
     *
     * @param hostName The hostname of the IoT Hub
     * @param deviceId The name of the device
     * @return The device Url in the following format: "https:hostname/devices/deviceId?api-version=201X-XX-XX"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     * @throws IllegalArgumentException This exception is thrown if device id is null or empty
     */
    public static URL getUrlModulesOnDevice(String hostName, String deviceId) throws MalformedURLException, IllegalArgumentException
    {
        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("device name cannot be empty or null");
        }

        String stringBuilder = URL_HTTPS +
                hostName +
                URL_SEPARATOR_0 +
                URL_PATH_DEVICES +
                URL_SEPARATOR_0 +
                deviceId +
                URL_SEPARATOR_0 +
                URL_PATH_MODULES +
                URL_SEPARATOR_1 +
                URL_API_VERSION;
        return new URL(stringBuilder);
    }

    /**
     * Create url for requesting device list
     *
     * @param maxCount The number of requested devices
     * @return URL string to get the device list from IotHub
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     * @throws IllegalArgumentException This exception is thrown if maxCount is null or empty
     */
    public URL getUrlDeviceList(Integer maxCount) throws MalformedURLException, IllegalArgumentException
    {
        return getUrlDeviceList(this.hostName, maxCount);
    }

    /**
     * Create url for requesting device list
     *
     * @param hostName The hostname of the IoT Hub
     * @param maxCount The number of requested devices
     * @return URL string to get the device list from IotHub
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     * @throws IllegalArgumentException This exception is thrown if maxCount is null or empty
     */
    public static URL getUrlDeviceList(String hostName, Integer maxCount) throws MalformedURLException, IllegalArgumentException
    {
        if ((maxCount == null) || (maxCount < 1))
        {
            throw new IllegalArgumentException("maxCount cannot be null or less then 1");
        }

        String stringBuilder = URL_HTTPS +
                hostName +
                URL_SEPARATOR_0 +
                URL_PATH_DEVICES +
                URL_SEPARATOR_0 +
                URL_SEPARATOR_1 +
                URL_MAX_COUNT +
                maxCount.toString() +
                URL_SEPARATOR_2 +
                URL_API_VERSION;
        return new URL(stringBuilder);
    }

    /**
     * Create url for requesting device statistics
     *
     * @return The device statistics Url in the following format: "https:hostname/statistics/devices?api-version=201X-XX-XX"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     */
    public URL getUrlDeviceStatistics() throws MalformedURLException
    {
        return getUrlDeviceStatistics(this.hostName);
    }

    /**
     * Create url for requesting device statistics
     *
     * @param hostName The hostname of the IoT Hub
     * @return The device statistics Url in the following format: "https:hostname/statistics/devices?api-version=201X-XX-XX"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     */
    public static URL getUrlDeviceStatistics(String hostName) throws MalformedURLException
    {
        String stringBuilder = URL_HTTPS +
                hostName +
                URL_SEPARATOR_0 +
                URL_PATH_DEVICESTATISTICS +
                URL_SEPARATOR_0 +
                URL_PATH_DEVICES +
                URL_SEPARATOR_1 +
                URL_API_VERSION;
        return new URL(stringBuilder);
    }

    /**
     * Create url for processing a bulk import/export job
     *
     * @return The import/export job URL in the following format: "https:hostname/jobs/create?api-version=201X-XX-XX"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     */
    public URL getUrlCreateExportImportJob() throws MalformedURLException
    {
        return getUrlCreateExportImportJob(this.hostName);
    }

    /**
     * Create url for processing a bulk import/export job
     *
     * @param hostName The hostname of the IoT Hub
     * @return The import/export job URL in the following format: "https:hostname/jobs/create?api-version=201X-XX-XX"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     */
    public static URL getUrlCreateExportImportJob(String hostName) throws MalformedURLException
    {
        String stringBuilder = URL_HTTPS +
                hostName +
                URL_SEPARATOR_0 +
                URL_PATH_JOBS +
                URL_SEPARATOR_0 +
                "create" +
                URL_SEPARATOR_1 +
                URL_API_VERSION;
        return new URL(stringBuilder);
    }

    /**
     * @param jobId Create url for retrieving a bulk import/export job
     * @return The import/export job URL in the following format: "https:hostname/jobs/jobId?api-version=201X-XX-XX"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     */
    public URL getUrlImportExportJob(String jobId) throws MalformedURLException
    {
        return getUrlImportExportJob(this.hostName, jobId);
    }

    /**
     * @param hostName The hostname of the IoT Hub
     * @param jobId Create url for retrieving a bulk import/export job
     * @return The import/export job URL in the following format: "https:hostname/jobs/jobId?api-version=201X-XX-XX"
     * @throws MalformedURLException This exception is thrown if the URL creation failed due to malformed string
     */
    public static URL getUrlImportExportJob(String hostName, String jobId) throws MalformedURLException
    {
        String stringBuilder = URL_HTTPS +
                hostName +
                URL_SEPARATOR_0 +
                URL_PATH_JOBS +
                URL_SEPARATOR_0 +
                jobId +
                URL_SEPARATOR_1 +
                URL_API_VERSION;
        return new URL(stringBuilder);
    }

    /**
     * Serialize connection string
     *
     * @return Iot Hub connection string
     */
    @Override
    public String toString()
    {
        return HOST_NAME_PROPERTY_NAME +
                VALUE_PAIR_SEPARATOR +
                this.hostName +
                VALUE_PAIR_DELIMITER +
                SHARED_ACCESS_KEY_NAME_PROPERTY_NAME +
                VALUE_PAIR_SEPARATOR +
                this.sharedAccessKeyName +
                VALUE_PAIR_DELIMITER +
                SHARED_ACCESS_KEY_PROPERTY_NAME +
                VALUE_PAIR_SEPARATOR +
                this.sharedAccessKey +
                VALUE_PAIR_DELIMITER +
                SHARED_ACCESS_SIGNATURE_PROPERTY_NAME +
                VALUE_PAIR_SEPARATOR +
                this.sharedAccessSignature;
    }

    /**
     * Getter for iotHubName
     *
     * @return The iot hub name string
     */
    public String getIotHubName()
    {
        return this.iotHubName;
    }

    /**
     * Getter for authenticationMethod
     *
     * @return The authenticationMethod object
     */
    public AuthenticationMethod getAuthenticationMethod()
    {
        return this.authenticationMethod;
    }

    /**
     * Getter for sharedAccessKeyName
     *
     * @return The sharedAccessKeyName string
     */
    public String getSharedAccessKeyName()
    {
        return this.sharedAccessKeyName;
    }

    /**
     * Getter for sharedAccessKey
     *
     * @return The sharedAccessKey string
     */
    public String getSharedAccessKey()
    {
        return this.sharedAccessKey;
    }

    /**
     * Getter for sharedAccessSignature
     *
     * @return The sharedAccessSignature string
     */
    public String getSharedAccessSignature()
    {
        return this.sharedAccessSignature;
    }

    /**
     * Getter for hostName
     *
     * @return The hostName string
     */
    public String getHostName()
    {
        return this.hostName;
    }

    /**
     * Setter for sharedAccessKeyName
     *
     * @param sharedAccessKeyName The value of the signature to set
     */
    void setSharedAccessKeyName(String sharedAccessKeyName)
    {
        this.sharedAccessKeyName = sharedAccessKeyName;
    }

    /**
     * Setter for sharedAccessKey
     *
     * @param sharedAccessKey The value of the signature to set
     */
    void setSharedAccessKey(String sharedAccessKey)
    {
        this.sharedAccessKey = sharedAccessKey;
    }

    /**
     * Setter for sharedAccessSignature
     *
     * @param sharedAccessSignature The value of the signature to set
     */
    void setSharedAccessSignature(String sharedAccessSignature)
    {
        this.sharedAccessSignature = sharedAccessSignature;
    }
}
