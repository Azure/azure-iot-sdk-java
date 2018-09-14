/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.deps.serializer.ConfigurationContentParser;
import com.microsoft.azure.sdk.iot.deps.serializer.ConfigurationParser;
import com.microsoft.azure.sdk.iot.deps.serializer.DeviceParser;
import com.microsoft.azure.sdk.iot.service.*;
import com.microsoft.azure.sdk.iot.service.Module;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubExceptionManager;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;

/**
 * Code Coverage
 * Methods: 100%
 * Lines: 96%
 */
@RunWith(JMockit.class)
public class RegistryManagerTest
{
    @Mocked
    Device device;
    @Mocked
    URL mockUrl;
    @Mocked
    HttpResponse mockHttpResponse;
    @Mocked
    IotHubServiceSasToken iotHubServiceSasToken;
    @Mocked
    IotHubExceptionManager mockIotHubExceptionManager;
    @Mocked
    RegistryStatistics registryStatistics;
    @Mocked
    IotHubConnectionString iotHubConnectionString;
    @Mocked
    HttpRequest mockHttpRequest;
    @Mocked
    ExecutorService mockExecutorService;
    @Mocked
    Module module;
    @Mocked
    Configuration config;
    @Mocked
    ConfigurationContent mockedConfigurationContent;
    @Mocked
    ConfigurationContentParser mockedConfigurationContentParser;

    final String deviceJson = "{\"deviceId\":\"mockdevice\",\"generationId\":\"635864360921156105\",\"etag\":\"MA==\",\"" +
            "connectionState\":\"Disconnected\",\"connectionStateUpdatedTime\":\"0001-01-01T00:00:00\",\"status\":\"" +
            "Disabled\",\"statusReason\":null,\"statusUpdatedTime\":\"0001-01-01T00:00:00\",\"" +
            "lastActivityTime\":\"0000-00-00T00:00:00+00:00\",\"cloudToDeviceMessageCount\":0,\n" +
            "\"authentication\":\n" +
            "{\"symmetricKey\":{\"primaryKey\":\"firstKey\",\"" +
            "secondaryKey\":\"secondKey\"}}}";

    final String moduleJson = "{\"deviceId\":\"mockdevice\", \"moduleId\":\"mockmodule\", \"generationId\":\"635864360921156105\",\"etag\":\"MA==\",\"" +
            "connectionState\":\"Disconnected\",\"connectionStateUpdatedTime\":\"0001-01-01T00:00:00\",\"status\":\"" +
            "Disabled\",\"statusReason\":null,\"statusUpdatedTime\":\"0001-01-01T00:00:00\",\"" +
            "lastActivityTime\":\"0000-00-00T00:00:00+00:00\",\"cloudToDeviceMessageCount\":0,\n" +
            "\"authentication\":\n" +
            "{\"symmetricKey\":{\"primaryKey\":\"firstKey\",\"" +
            "secondaryKey\":\"secondKey\", \"managedBy\":\"xxx\"}}}";

    final String configJson = "{\"id\":\"mockconfig\",\"schemaVersion\":\"1.0\",\"etag\":\"MQ==\",\"" +
            "labels\":{\"App\":\"label2\"},\"content\":{\"modulesContent\":{}, \"deviceContent\":{\"properties.desired.settings1\": {\"c\":3,\"d\":4}}}," +
            "\"targetCondition\":\"*\", \"createdTimeUtc\":\"0001-01-01T00:00:00\", \"lastUpdatedTimeUtc\":\"0001-01-01T00:00:00\"," +
            "\"priority\":10, \"systemMetrics\":{\"results\":{\"targetedCount\":3, \"appliedCount\":3}, " +
            "\"queries\":{}}, \"metrics\":{\"results\":{\"customMetric\":3}," +
            "\"queries\":{}}}";

    final String configsJson = "[{\"id\":\"mockconfig0\",\"schemaVersion\":\"1.0\",\"etag\":\"MQ==\",\"" +
            "labels\":{\"App\":\"label2\"},\"content\":{\"modulesContent\":{}, \"deviceContent\":{\"properties.desired.settings1\": {\"c\":3,\"d\":4}}}," +
            "\"targetCondition\":\"*\", \"createdTimeUtc\":\"0001-01-01T00:00:00\", \"lastUpdatedTimeUtc\":\"0001-01-01T00:00:00\"," +
            "\"priority\":10, \"systemMetrics\":{\"results\":{\"targetedCount\":3, \"appliedCount\":3}, " +
            "\"queries\":{}}, \"metrics\":{\"results\":{\"customMetric\":3},\"queries\":{}}}," +
            "{\"id\":\"mockconfig1\",\"schemaVersion\":\"1.0\",\"etag\":\"MQ==\",\"" +
            "labels\":{\"App\":\"label2\"},\"content\":{\"modulesContent\":{}, \"deviceContent\":{\"properties.desired.settings1\": {\"c\":3,\"d\":4}}}," +
            "\"targetCondition\":\"*\", \"createdTimeUtc\":\"0001-01-01T00:00:00\", \"lastUpdatedTimeUtc\":\"0001-01-01T00:00:00\"," +
            "\"priority\":10, \"systemMetrics\":{\"results\":{\"targetedCount\":3, \"appliedCount\":3}, " +
            "\"queries\":{}}, \"metrics\":{\"results\":{\"customMetric\":3},\"queries\":{}}}" +
            "]";

    final String devicesJson = "[{\"encryptionMethod\":\"AES\",\"UTC_TIME_DEFAULT\":\"0001-01-01T00:00:00\",\"" +
            "deviceId\":\"java-crud-e2e-test-873e7831-5778-4b1e-a998-cf11fffa6415\",\"generationId\":\"\",\"" +
            "etag\":\"\",\"status\":\"Disabled\",\"statusReason\":\"\",\"statusUpdatedTime\":\"0001-01-01T00:00:00\",\"" +
            "connectionState\":\"Disconnected\",\"connectionStateUpdatedTime\":\"0001-01-01T00:00:00\",\"" +
            "lastActivityTime\":\"0000-00-00T00:00:00+00:00\",\"cloudToDeviceMessageCount\":0,\"forceUpdate\":false,\"" +
            "authentication\":{\"symmetricKey\":{\"primaryKey\":\"CZJIeLzepSADZe3Z9mQsCg\\u003d\\u003d\",\"" +
            "secondaryKey\":\"wP7t1W95u6zF8ocRGSAoYQ\\u003d\\u003d\"}}},{\"encryptionMethod\":\"AES\",\"" +
            "UTC_TIME_DEFAULT\":\"0001-01-01T00:00:00\",\"deviceId\":\"" +
            "java-crud-e2e-test-873e7831-5778-4b1e-a998-cf11fffa64152\",\"generationId\":\"\",\"etag\":\"\",\"" +
            "status\":\"Disabled\",\"statusReason\":\"\",\"statusUpdatedTime\":\"0001-01-01T00:00:00\",\"" +
            "connectionState\":\"Disconnected\",\"connectionStateUpdatedTime\":\"0001-01-01T00:00:00\",\"" +
            "lastActivityTime\":\"0000-00-00T00:00:00+00:00\",\"cloudToDeviceMessageCount\":0,\"forceUpdate\":false,\"" +
            "authentication\":{\"symmetricKey\":{\"primaryKey\":\"V0UYjIYYLTERQheXas+YUw\\u003d\\u003d\",\"" +
            "secondaryKey\":\"cDAeY0gZkA9UIxKmSqDGDg\\u003d\\u003d\"}}},{\"encryptionMethod\":\"AES\",\"" +
            "UTC_TIME_DEFAULT\":\"0001-01-01T00:00:00\",\"deviceId\":\"" +
            "java-crud-e2e-test-873e7831-5778-4b1e-a998-cf11fffa64153\",\"generationId\":\"\",\"etag\":\"\",\"" +
            "status\":\"Disabled\",\"statusReason\":\"\",\"statusUpdatedTime\":\"0001-01-01T00:00:00\",\"" +
            "connectionState\":\"Disconnected\",\"connectionStateUpdatedTime\":\"0001-01-01T00:00:00\",\"" +
            "lastActivityTime\":\"0000-00-00T00:00:00+00:00\",\"cloudToDeviceMessageCount\":0,\"forceUpdate\":false,\"" +
            "authentication\":{\"symmetricKey\":{\"primaryKey\":\"kOZFCG80Ytfd89FFZWc4XQ\\u003d\\u003d\",\"" +
            "secondaryKey\":\"RnMYfSEbt8Ql/MhFoIpopA\\u003d\\u003d\"}}}]";

    final String modulesJson = "[{\"encryptionMethod\":\"AES\",\"UTC_TIME_DEFAULT\":\"0001-01-01T00:00:00\",\"" +
            "deviceId\":\"java-crud-e2e-test-873e7831-5778-4b1e-a998-cf11fffa6415\",\"moduleId\":\"module1\",\"generationId\":\"\",\"" +
            "etag\":\"\",\"status\":\"Disabled\",\"statusReason\":\"\",\"statusUpdatedTime\":\"0001-01-01T00:00:00\",\"" +
            "connectionState\":\"Disconnected\",\"connectionStateUpdatedTime\":\"0001-01-01T00:00:00\",\"" +
            "lastActivityTime\":\"0000-00-00T00:00:00+00:00\",\"cloudToDeviceMessageCount\":0,\"forceUpdate\":false,\"" +
            "authentication\":{\"symmetricKey\":{\"primaryKey\":\"CZJIeLzepSADZe3Z9mQsCg\\u003d\\u003d\",\"" +
            "secondaryKey\":\"wP7t1W95u6zF8ocRGSAoYQ\\u003d\\u003d\"}}, \"managedBy\":\"xxx\"},{\"encryptionMethod\":\"AES\",\"" +
            "UTC_TIME_DEFAULT\":\"0001-01-01T00:00:00\",\"deviceId\":\"" +
            "java-crud-e2e-test-873e7831-5778-4b1e-a998-cf11fffa64152\",\"moduleId\":\"module2\",\"generationId\":\"\",\"etag\":\"\",\"" +
            "status\":\"Disabled\",\"statusReason\":\"\",\"statusUpdatedTime\":\"0001-01-01T00:00:00\",\"" +
            "connectionState\":\"Disconnected\",\"connectionStateUpdatedTime\":\"0001-01-01T00:00:00\",\"" +
            "lastActivityTime\":\"0000-00-00T00:00:00+00:00\",\"cloudToDeviceMessageCount\":0,\"forceUpdate\":false,\"" +
            "authentication\":{\"symmetricKey\":{\"primaryKey\":\"V0UYjIYYLTERQheXas+YUw\\u003d\\u003d\",\"" +
            "secondaryKey\":\"cDAeY0gZkA9UIxKmSqDGDg\\u003d\\u003d\"}},\"managedBy\":\"xxx\"},{\"encryptionMethod\":\"AES\",\"" +
            "UTC_TIME_DEFAULT\":\"0001-01-01T00:00:00\",\"deviceId\":\"" +
            "java-crud-e2e-test-873e7831-5778-4b1e-a998-cf11fffa64153\",\"moduleId\":\"module3\",\"generationId\":\"\",\"etag\":\"\",\"" +
            "status\":\"Disabled\",\"statusReason\":\"\",\"statusUpdatedTime\":\"0001-01-01T00:00:00\",\"" +
            "connectionState\":\"Disconnected\",\"connectionStateUpdatedTime\":\"0001-01-01T00:00:00\",\"" +
            "lastActivityTime\":\"0000-00-00T00:00:00+00:00\",\"cloudToDeviceMessageCount\":0,\"forceUpdate\":false,\"" +
            "authentication\":{\"symmetricKey\":{\"primaryKey\":\"kOZFCG80Ytfd89FFZWc4XQ\\u003d\\u003d\",\"" +
            "secondaryKey\":\"RnMYfSEbt8Ql/MhFoIpopA\\u003d\\u003d\"}},\"managedBy\":\"xxx\"}]";

    final String registryStatisticsJson = "{\"totalDeviceCount\":45,\"enabledDeviceCount\":45,\"disabledDeviceCount\":0}";

    final String jobPropertiesJson = "{\"jobId\":\"some_guid\",\"type\":\"export\",\"progress\"" +
            ":0,\"outputBlobContainerUri\":\"https://myurl.com\",\"excludeKeysInExport\":true}";

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_001: [The constructor shall throw IllegalArgumentException if the input string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructor_input_null() throws Exception
    {
        String connectionString = null;

        RegistryManager.createFromConnectionString(connectionString);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_001: [The constructor shall throw IllegalArgumentException if the input string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructor_input_empty() throws Exception
    {
        String connectionString = null;

        RegistryManager.createFromConnectionString(connectionString);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_002: [The constructor shall create an IotHubConnectionString object from the given connection string]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_003: [The constructor shall create a new RegistryManager, stores the created IotHubConnectionString object and return with it]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_34_090: [The function shall start this object's executor service]
    @Test
    public void constructor_good_case() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";

        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createConnectionString(connectionString);
                result = iotHubConnectionString;
            }
        };

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        assertNotNull(registryManager);
        assertNotNull(Deencapsulation.getField(registryManager, "iotHubConnectionString"));
        assertNotNull(Deencapsulation.getField(registryManager, "executor"));
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_004: [The constructor shall throw IllegalArgumentException if the input device is null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void addDevice_input_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.addDevice(null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_005: [The function shall deserialize the given device object to Json string]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_006: [The function shall get the URL for the device]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_007: [The function shall create a new SAS token for the device]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_008: [The function shall create a new HttpRequest for adding the device to IotHub]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_009: [The function shall send the created request and get the response]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_010: [The function shall verify the response status and throw proper Exception]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_011: [The function shall create a new Device object from the response and return with it]
    @Test
    public void addDevice_good_case() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String deviceId = "somedevice";

        new NonStrictExpectations()
        {
            {
                device.getDeviceId();
                result = deviceId;
            }
        };

        commonExpectations(connectionString, deviceId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        Device returnDevice = registryManager.addDevice(device);
        registryManager.close();

        commonVerifications(HttpMethod.PUT, deviceId, returnDevice);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_012: [The function shall throw IllegalArgumentException if the input device is null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void addDeviceAsync_input_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.addDeviceAsync(null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_ REGISTRYMANAGER_12_013: [The function shall create an async wrapper around the addDevice() function call, handle the return value or delegate exception]
    @Test
    public void addDeviceAsync_future_return_ok() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String deviceId = "somedevice";
        new NonStrictExpectations()
        {
            {
                device.getDeviceId();
                result = deviceId;
            }
        };

        commonExpectations(connectionString, deviceId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        CompletableFuture<Device> completableFuture =  registryManager.addDeviceAsync(device);
        Device returnDevice = completableFuture.get();

        commonVerifications(HttpMethod.PUT, deviceId, returnDevice);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_ REGISTRYMANAGER_12_013: [The function shall create an async wrapper around the addDevice() function call, handle the return value or delegate exception]
    // Assert
    @Test (expected = Exception.class)
    public void addDeviceAsync_future_throw() throws Exception
    {
        new MockUp<RegistryManager>()
        {
            @Mock
            public Device addDevice(Device device) throws IOException, IotHubException
            {
                throw new IOException();
            }
        };

        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        CompletableFuture<Device> completableFuture =  registryManager.addDeviceAsync(device);
        completableFuture.get();
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_014: [The constructor shall throw IllegalArgumentException if the input string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getDevice_input_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.getDevice(null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_015: [The function shall get the URL for the device]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_016: [The function shall create a new SAS token for the device]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_017: [The function shall create a new HttpRequest for getting a device from IotHub]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_018: [The function shall send the created request and get the response]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_019: [The function shall verify the response status and throw proper Exception]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_020: [The function shall create a new Device object from the response and return with it]
    @Test
    public void getDevice_good_case() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String deviceId = "somedevice";

        commonExpectations(connectionString, deviceId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        Device returnDevice = registryManager.getDevice(deviceId);

        commonVerifications(HttpMethod.GET, deviceId, returnDevice);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_021: [The constructor shall throw IllegalArgumentException if the input device is null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getDeviceAsync_input_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.getDeviceAsync(null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_022: [The function shall create an async wrapper around the addDevice() function call, handle the return value or delegate exception]
    @Test
    public void getDeviceAsync_future_return_ok() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String deviceId = "somedevice";

        commonExpectations(connectionString, deviceId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        CompletableFuture<Device> completableFuture =  registryManager.getDeviceAsync(deviceId);
        Device returnDevice = completableFuture.get();

        commonVerifications(HttpMethod.GET, deviceId, returnDevice);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_022: [The function shall create an async wrapper around the addDevice() function call, handle the return value or delegate exception]
    // Assert
    @Test (expected = Exception.class)
    public void getDeviceAsync_future_throw() throws Exception
    {
        String deviceId = "somedevice";
        new MockUp<RegistryManager>()
        {
            @Mock
            public Device getDevice(String deviceId) throws IOException, IotHubException
            {
                throw new IOException();
            }
        };
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        CompletableFuture<Device> completableFuture =  registryManager.getDeviceAsync(deviceId);
        completableFuture.get();
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_023: [The constructor shall throw IllegalArgumentException if the input count number is less than 1]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getDevices_input_zero() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.getDevices(0);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_024: [The function shall get the URL for the device]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_025: [The function shall create a new SAS token for the device]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_026: [The function shall create a new HttpRequest for getting a device list from IotHub]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_027: [The function shall send the created request and get the response]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_028: [The function shall verify the response status and throw proper Exception]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_029: [The function shall create a new ArrayList<Device> object from the response and return with it]
    @Test
    public void getDevices_good_case() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        int numberOfDevices = 10;

        getDevicesExpectations(connectionString, numberOfDevices);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        ArrayList<Device> devices =  registryManager.getDevices(10);

        getDevicesVerifications(numberOfDevices, devices);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_030: [The function shall throw IllegalArgumentException if the input count number is less than 1]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getDevicesAsync_input_zero() throws Exception
    {

        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.getDevicesAsync(0);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_031: [The function shall create an async wrapper around the getDevices() function call, handle the return value or delegate exception]
    @Test
    public void getDevicesAsync_future_return_ok() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        int numberOfDevices = 10;

        getDevicesExpectations(connectionString, numberOfDevices);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        CompletableFuture<ArrayList<Device>> completableFuture =  registryManager.getDevicesAsync(10);
        ArrayList<Device> devices = completableFuture.get();

        getDevicesVerifications(numberOfDevices, devices);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_21_085: [The function shall return a connectionString for the input device]
    @Test
    public void getDeviceConnectionString_return_ok() throws Exception
    {
        String deviceId = "somedevice";
        String hostName = "aaa.bbb.ccc";
        String validDeviceKey = "validKey==";
        String expectedDeviceConnectionString = "HostName=" + hostName + ";DeviceId=" + deviceId + ";SharedAccessKey=" + validDeviceKey;
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=XXX;SharedAccessKey=YYY";

        new NonStrictExpectations()
        {
            {
                iotHubConnectionString.getHostName();
                result=hostName;
                device.getDeviceId();
                result = deviceId;
                device.getPrimaryKey();
                result = validDeviceKey;
            }
        };

        commonExpectations(connectionString, deviceId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        Device returnDevice = registryManager.getDevice(deviceId);
        String returnDeviceConnectionString =  registryManager.getDeviceConnectionString(returnDevice);

        assertEquals(expectedDeviceConnectionString, returnDeviceConnectionString);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_21_085: [The function shall return a connectionString for the input device]
    @Test
    public void getDeviceConnectionString_return_ok_withX509() throws Exception
    {
        String deviceId = "somedevice";
        String hostName = "aaa.bbb.ccc";
        String nullDeviceKey = null;
        String validThumbprint = "thumbrpint";
        String expectedDeviceConnectionString = "HostName=" + hostName + ";DeviceId=" + deviceId + ";x509=true";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=XXX;SharedAccessKey=YYY";

        new NonStrictExpectations()
        {
            {
                iotHubConnectionString.getHostName();
                result=hostName;
                device.getDeviceId();
                result = deviceId;
                device.getPrimaryThumbprint();
                result = validThumbprint;
                device.getPrimaryKey();
                result = nullDeviceKey;
            }
        };

        commonExpectations(connectionString, deviceId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        Device returnDevice = registryManager.getDevice(deviceId);
        String returnDeviceConnectionString =  registryManager.getDeviceConnectionString(returnDevice);

        assertEquals(expectedDeviceConnectionString, returnDeviceConnectionString);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_21_086: [The function shall throw IllegalArgumentException if the input device is null, if deviceId is null, or primary key and primary thumbprint are empty or null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getDeviceConnectionString_null_device_throw() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        registryManager.getDeviceConnectionString(null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_21_086: [The function shall throw IllegalArgumentException if the input device is null, if deviceId is null, or primary key and primary thumbprint are empty or null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getDeviceConnectionString_null_deviceId_throw() throws Exception
    {
        String deviceId = "somedevice";
        String hostName = "aaa.bbb.ccc";
        String validDeviceKey = "validKey==";
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";

        new NonStrictExpectations()
        {
            {
                iotHubConnectionString.getHostName();
                result=hostName;
                device.getDeviceId();
                result = null;
                device.getPrimaryKey();
                result = validDeviceKey;
            }
        };

        commonExpectations(connectionString, deviceId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        Device returnDevice = registryManager.getDevice(deviceId);
        registryManager.getDeviceConnectionString(returnDevice);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_21_086: [The function shall throw IllegalArgumentException if the input device is null, if deviceId is null, or primary key and primary thumbprint are empty or null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getDeviceConnectionString_empty_deviceId_throw() throws Exception
    {
        String deviceId = "somedevice";
        String hostName = "aaa.bbb.ccc";
        String validDeviceKey = "validKey==";
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";

        new NonStrictExpectations()
        {
            {
                iotHubConnectionString.getHostName();
                result=hostName;
                device.getDeviceId();
                result = "";
                device.getPrimaryKey();
                result = validDeviceKey;
            }
        };

        commonExpectations(connectionString, deviceId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        Device returnDevice = registryManager.getDevice(deviceId);
        registryManager.getDeviceConnectionString(returnDevice);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_21_086: [The function shall throw IllegalArgumentException if the input device is null, if deviceId is null, or primary key and primary thumbprint are empty or null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getDeviceConnectionString_null_deviceKey_throw() throws Exception
    {
        String deviceId = "somedevice";
        String hostName = "aaa.bbb.ccc";
        String validDeviceKey = null;
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";

        new NonStrictExpectations()
        {
            {
                iotHubConnectionString.getHostName();
                result=hostName;
                device.getDeviceId();
                result = deviceId;
                device.getPrimaryKey();
                result = validDeviceKey;
            }
        };

        commonExpectations(connectionString, deviceId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        Device returnDevice = registryManager.getDevice(deviceId);
        registryManager.getDeviceConnectionString(returnDevice);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_21_086: [The function shall throw IllegalArgumentException if the input device is null, if deviceId is null, or primary key and primary thumbprint are empty or null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getDeviceConnectionString_empty_deviceKey_throw() throws Exception
    {
        String deviceId = "somedevice";
        String hostName = "aaa.bbb.ccc";
        String validDeviceKey = "";
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";

        new NonStrictExpectations()
        {
            {
                iotHubConnectionString.getHostName();
                result=hostName;
                device.getDeviceId();
                result = deviceId;
                device.getPrimaryKey();
                result = validDeviceKey;
            }
        };

        commonExpectations(connectionString, deviceId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        Device returnDevice = registryManager.getDevice(deviceId);
        registryManager.getDeviceConnectionString(returnDevice);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_031: [The function shall create an async wrapper around the getDevices() function call, handle the return value or delegate exception]
    // Assert
    @Test (expected = Exception.class)
    public void getDevicesAsync_future_throw() throws Exception
    {
        new MockUp<RegistryManager>()
        {
            @Mock
            public Device getDevices(Integer maxCount) throws IOException, IotHubException
            {
                throw new IOException();
            }
        };
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        CompletableFuture<ArrayList<Device>> completableFuture = registryManager.getDevicesAsync(10);
        completableFuture.get();
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_032: [The function shall throw IllegalArgumentException if the input device is null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void updateDevice_input_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.updateDevice(null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_033: [The function shall call updateDevice with forceUpdate = false]
    @Test
    public void updateDevice_good_case() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String deviceId = "somedevice";

        new NonStrictExpectations()
        {
            {
                device.getDeviceId();
                result = deviceId;
            }
        };

        commonExpectations(connectionString, deviceId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        Device returnDevice = registryManager.updateDevice(device);
        commonVerifications(HttpMethod.PUT, deviceId, returnDevice);

        new VerificationsInOrder()
        {
            {
                device.setForceUpdate(false);
                mockHttpRequest.setHeaderField("If-Match", "*");
            }
        };
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_034: [The function shall throw IllegalArgumentException if the input device is null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void updateDeviceForce_input_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.updateDevice(null, true);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_035: [The function shall set forceUpdate on the device]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_036: [The function shall get the URL for the device]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_037: [The function shall create a new SAS token for the device]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_038: [The function shall create a new HttpRequest for updating the device on IotHub]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_039: [The function shall send the created request and get the response]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_040: [The function shall verify the response status and throw proper Exception]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_041: [The function shall create a new Device object from the response and return with it]
    @Test
    public void updateDeviceForce_good_case() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String deviceId = "somedevice";

        new NonStrictExpectations()
        {
            {
                device.getDeviceId();
                result = deviceId;
            }
        };

        Deencapsulation.setField(device, "deviceId", deviceId);

        commonExpectations(connectionString, deviceId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        Device returnDevice = registryManager.updateDevice(device, true);

        commonVerifications(HttpMethod.PUT, deviceId, returnDevice);

        new VerificationsInOrder()
        {
            {
                device.setForceUpdate(true);
                mockHttpRequest.setHeaderField("If-Match", "*");
            }
        };
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_042: [The function shall throw IllegalArgumentException if the input device is null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void updateDeviceAsync_input_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.updateDeviceAsync(null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_043: [The function shall create an async wrapper around the updateDevice() function call, handle the return value or delegate exception]
    @Test
    public void updateDeviceAsync_future_return_ok() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String deviceId = "somedevice";

        new NonStrictExpectations()
        {
            {
                device.getDeviceId();
                result = deviceId;
            }
        };

        commonExpectations(connectionString, deviceId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        CompletableFuture<Device> completableFuture = registryManager.updateDeviceAsync(device);
        Device returnDevice = completableFuture.get();

        commonVerifications(HttpMethod.PUT, deviceId, returnDevice);

        new VerificationsInOrder()
        {
            {
                device.setForceUpdate(false);
                mockHttpRequest.setHeaderField("If-Match", "*");
            }
        };
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_043: [The function shall create an async wrapper around the updateDevice() function call, handle the return value or delegate exception]
    // Assert
    @Test (expected = Exception.class)
    public void updateDeviceAsync_future_throw() throws Exception
    {
        new MockUp<RegistryManager>()
        {
            @Mock
            public Device updateDevice(Device device) throws IOException, IotHubException
            {
                throw new IOException();
            }
        };
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        CompletableFuture<Device> completableFuture = registryManager.updateDeviceAsync(device);
        completableFuture.get();
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_044: [The function shall throw IllegalArgumentException if the input device is null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void updateDeviceAsyncForce_input_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.updateDeviceAsync(null, true);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_045: [The function shall create an async wrapper around the updateDevice(Device, device, Boolean forceUpdate) function call, handle the return value or delegate exception]
    @Test
    public void updateDeviceAsyncForce_future_return_ok() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String deviceId = "somedevice";

        new NonStrictExpectations()
        {
            {
                device.getDeviceId();
                result = deviceId;
            }
        };

        commonExpectations(connectionString, deviceId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        CompletableFuture<Device> completableFuture =  registryManager.updateDeviceAsync(device, true);
        Device returnDevice = completableFuture.get();

        commonVerifications(HttpMethod.PUT, deviceId, returnDevice);

        new VerificationsInOrder()
        {
            {
                device.setForceUpdate(true);
                mockHttpRequest.setHeaderField("If-Match", "*");
            }
        };
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_045: [The function shall create an async wrapper around the updateDevice(Device, device, Boolean forceUpdate) function call, handle the return value or delegate exception]
    // Assert
    @Test (expected = Exception.class)
    public void updateDeviceAsyncForce_future_throw() throws Exception
    {
        new MockUp<RegistryManager>()
        {
            @Mock
            public Device updateDevice(Device device, Boolean forceUpdate) throws IOException, IotHubException
            {
                throw new IOException();
            }
        };
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        CompletableFuture<Device> completableFuture =  registryManager.updateDeviceAsync(device, true);
        completableFuture.get();
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_081: [The function shall throw IllegalArgumentException if the input device is null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeDevice_input_null_Device() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        Device device = null;
        registryManager.removeDevice(device);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_087: [The function shall throw IllegalArgumentException if the input etag is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeDevice_input_Device_null_etag() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        new NonStrictExpectations()
        {
            {
                device.getDeviceId();
                result = "somedevice";
                device.geteTag();
                result = null;
            }
        };

        registryManager.removeDevice(device);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_087: [The function shall throw IllegalArgumentException if the input etag is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeDevice_input_Device_empty_etag() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        new NonStrictExpectations()
        {
            {
                device.getDeviceId();
                result = "somedevice";
                device.geteTag();
                result = "";
            }
        };

        registryManager.removeDevice(device);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_047: [The function shall get the URL for the device]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_048: [The function shall create a new SAS token for the device]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_049: [The function shall create a new HttpRequest for removing the device from IotHub]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_050: [The function shall send the created request and get the response]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_051: [The function shall verify the response status and throw proper Exception]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_084: [The function shall call provide device object's etag as with etag of device to be removed]
    @Test
    public void removeDevice_with_etag() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String deviceId = "somedevice";
        String etag = "someetag";

        commonExpectations(connectionString, deviceId);

        new NonStrictExpectations()
        {
            {
                device.getDeviceId();
                result = deviceId;
                device.geteTag();
                result = etag;
            }
        };

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        registryManager.removeDevice(device);

        new VerificationsInOrder()
        {
            {
                iotHubConnectionString.getUrlDevice(deviceId);
                times = 1;
                new HttpRequest(mockUrl, HttpMethod.DELETE, new byte[0]);
                times = 1;
                mockHttpRequest.setReadTimeoutMillis(anyInt);
                mockHttpRequest.setHeaderField("authorization", anyString);
                mockHttpRequest.setHeaderField("If-Match", etag);
            }
        };
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_046: [The function shall throw IllegalArgumentException if the input deviceId is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeDevice_input_null_String() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        String deviceId = null;
        registryManager.removeDevice(deviceId);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_046: [The function shall throw IllegalArgumentException if the input deviceId is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeDevice_input_empty() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.removeDevice("");
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_047: [The function shall get the URL for the device]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_048: [The function shall create a new SAS token for the device]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_049: [The function shall create a new HttpRequest for removing the device from IotHub]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_050: [The function shall send the created request and get the response]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_051: [The function shall verify the response status and throw proper Exception]
    // Codes_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_087: [The function shall use * as the etag]
    @Test
    public void removeDevice_good_case() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String deviceId = "somedevice";

        commonExpectations(connectionString, deviceId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        registryManager.removeDevice(deviceId);

        new VerificationsInOrder()
        {
            {
                iotHubConnectionString.getUrlDevice(deviceId);
                times = 1;
                new HttpRequest(mockUrl, HttpMethod.DELETE, new byte[0]);
                times = 1;
                mockHttpRequest.setReadTimeoutMillis(anyInt);
                mockHttpRequest.setHeaderField("authorization", anyString);
                mockHttpRequest.setHeaderField("If-Match", "*");
            }
        };
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_052: [The function shall throw IllegalArgumentException if the input string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeDeviceAsync_input_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.removeDeviceAsync(null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_052: [The function shall throw IllegalArgumentException if the input string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeDeviceAsync_input_empty() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.removeDeviceAsync("");
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_053: [The function shall create an async wrapper around the removeDevice() function call, handle the return value or delegate exception]
    @Test
    public void removeDeviceAsync_future_return_ok() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String deviceId = "somedevice";

        commonExpectations(connectionString, deviceId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        CompletableFuture completableFuture = registryManager.removeDeviceAsync(deviceId);
        completableFuture.get();

        new VerificationsInOrder()
        {
            {
                iotHubConnectionString.getUrlDevice(deviceId);
                times = 1;
                new HttpRequest(mockUrl, HttpMethod.DELETE, new byte[0]);
                times = 1;
                mockHttpRequest.setReadTimeoutMillis(anyInt);
                mockHttpRequest.setHeaderField("authorization", anyString);
                mockHttpRequest.setHeaderField("If-Match", "*");
            }
        };
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_053: [The function shall create an async wrapper around the removeDevice() function call, handle the return value or delegate exception]
    // Assert
    @Test (expected = Exception.class)
    public void removeDeviceAsync_future_throw() throws Exception
    {
        String deviceId = "somedevice";
        new MockUp<RegistryManager>()
        {
            @Mock
            public Device removeDevice(String deviceId) throws IOException, IotHubException
            {
                throw new IOException();
            }
        };
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        CompletableFuture completableFuture = registryManager.removeDeviceAsync(deviceId);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_054: [The function shall get the URL for the device]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_055: [The function shall create a new SAS token for the device]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_056: [The function shall create a new HttpRequest for getting statistics a device from IotHub]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_057: [The function shall send the created request and get the response]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_058: [The function shall verify the response status and throw proper Exception]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_059: [The function shall create a new RegistryStatistics object from the response and return with it]
    @Test
    public void getStatistics_good_case() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String deviceId = "somedevice";

        commonExpectations(connectionString, deviceId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        RegistryStatistics statistics = registryManager.getStatistics();

        new VerificationsInOrder()
        {
            {
                iotHubConnectionString.getUrlDeviceStatistics();
                new HttpRequest(mockUrl, HttpMethod.GET, new byte[0]);
                mockHttpRequest.setReadTimeoutMillis(anyInt);
                mockHttpRequest.setHeaderField("authorization", anyString);
                mockHttpRequest.setHeaderField("Request-Id", "1001");
                mockHttpRequest.setHeaderField("Accept", "application/json");
                mockHttpRequest.setHeaderField("Content-Type", "application/json");
                mockHttpRequest.setHeaderField("charset", "utf-8");
                mockHttpRequest.send();
                mockIotHubExceptionManager.httpResponseVerification((HttpResponse) any);
            }
        };
        assertNotNull(statistics);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_060: [The function shall create an async wrapper around the getStatistics() function call, handle the return value or delegate exception]
    @Test
    public void getStatisticsAsync_future_return_ok() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String deviceId = "somedevice";

        commonExpectations(connectionString, deviceId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        CompletableFuture<RegistryStatistics> completableFuture = registryManager.getStatisticsAsync();
        RegistryStatistics statistics = completableFuture.get();

        new VerificationsInOrder()
        {
            {
                iotHubConnectionString.getUrlDeviceStatistics();
                new HttpRequest(mockUrl, HttpMethod.GET, new byte[0]);
                mockHttpRequest.setReadTimeoutMillis(anyInt);
                mockHttpRequest.setHeaderField("authorization", anyString);
                mockHttpRequest.setHeaderField("Request-Id", "1001");
                mockHttpRequest.setHeaderField("Accept", "application/json");
                mockHttpRequest.setHeaderField("Content-Type", "application/json");
                mockHttpRequest.setHeaderField("charset", "utf-8");
                mockHttpRequest.send();
                mockIotHubExceptionManager.httpResponseVerification((HttpResponse) any);
            }
        };
        assertNotNull(statistics);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_060: [The function shall create an async wrapper around the getStatistics() function call, handle the return value or delegate exception]
    // Assert
    @Test (expected = Exception.class)
    public void getStatisticsAsync_future_throw() throws Exception
    {
        new MockUp<RegistryManager>()
        {
            @Mock
            public Device getStatistics() throws IOException, IotHubException
            {
                throw new IOException();
            }
        };
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.getStatisticsAsync();
    }

    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_061: [The function shall throw IllegalArgumentException if any of the input parameters is null]
    public void exportDevices_blob_input_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.exportDevices(null, true);
    }

    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_061: [The function shall throw IllegalArgumentException if any of the input parameters is null]
    @Test (expected = IllegalArgumentException.class)
    public void exportDevices_exclude_keys_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.exportDevices("www.someurl.com", null);
    }

    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_061: [The function shall throw IllegalArgumentException if any of the input parameters is null]
    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_062: [The function shall get the URL for the bulk export job creation]
    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_063: [The function shall create a new SAS token for the bulk export job]
    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_064: [The function shall create a new HttpRequest for the export job creation]
    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_065: [The function shall send the created request and get the response]
    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_066: [The function shall verify the response status and throw proper Exception]
    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_067: [The function shall create a new JobProperties object from the response and return it]
    @Test
    public void exportDevices_good_case() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";

        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createConnectionString(connectionString);
                result = iotHubConnectionString;
                iotHubConnectionString.getUrlCreateExportImportJob();
                result = mockUrl;
                mockHttpRequest.send();
                result = mockHttpResponse;
                mockIotHubExceptionManager.httpResponseVerification((HttpResponse) any);
                mockHttpResponse.getBody();
                result = jobPropertiesJson.getBytes();
            }
        };

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        JobProperties jobProperties = registryManager.exportDevices("blob1", true);

        new VerificationsInOrder()
        {
            {
                iotHubConnectionString.getUrlCreateExportImportJob();
                new HttpRequest(mockUrl, HttpMethod.POST, (byte[]) any);
                mockHttpRequest.setReadTimeoutMillis(anyInt);
                mockHttpRequest.setHeaderField("authorization", anyString);
                mockHttpRequest.setHeaderField("Request-Id", "1001");
                mockHttpRequest.setHeaderField("Accept", "application/json");
                mockHttpRequest.setHeaderField("Content-Type", "application/json");
                mockHttpRequest.setHeaderField("charset", "utf-8");
                mockHttpRequest.send();
                mockIotHubExceptionManager.httpResponseVerification((HttpResponse) any);
            }
        };
        assertNotNull(jobProperties);
    }

    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_068: [The function shall create an async wrapper around
    // the exportDevices() function call, handle the return value or delegate exception ]
    @Test (expected = Exception.class)
    public void exportDevicesAsync_future_throw() throws Exception
    {
        new MockUp<RegistryManager>()
        {
            @Mock
            public JobProperties exportDevices(String url, Boolean excludeKeys)
                    throws IllegalArgumentException, IOException, IotHubException
            {
                throw new IllegalArgumentException();
            }
        };

        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        CompletableFuture<JobProperties> completableFuture =  registryManager.exportDevicesAsync("blah", true);
        completableFuture.get();
    }

    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_069: [The function shall throw IllegalArgumentException if any of the input parameters is null]
    @Test (expected = IllegalArgumentException.class)
    public void importDevices_blob_import_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.importDevices(null, "outputblob");
    }

    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_069: [The function shall throw IllegalArgumentException if any of the input parameters is null]
    @Test (expected = IllegalArgumentException.class)
    public void importDevices_blob_output_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.importDevices("importblob", null);
    }

    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_070: [The function shall get the URL for the bulk import job creation]
    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_071: [The function shall create a new SAS token for the bulk import job]
    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_072: [The function shall create a new HttpRequest for the bulk import job creation]
    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_073: [The function shall send the created request and get the response]
    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_074: [The function shall verify the response status and throw proper Exception]
    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_075: [The function shall create a new JobProperties object from the response and return it]
    @Test
    public void importDevices_good_case() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";

        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createConnectionString(connectionString);
                result = iotHubConnectionString;
                iotHubConnectionString.getUrlCreateExportImportJob();
                result = mockUrl;
                mockHttpRequest.send();
                result = mockHttpResponse;
                mockIotHubExceptionManager.httpResponseVerification((HttpResponse) any);
                mockHttpResponse.getBody();
                result = jobPropertiesJson.getBytes();
            }
        };

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        JobProperties jobProperties = registryManager.importDevices("blob1", "blob2");

        new VerificationsInOrder()
        {
            {
                iotHubConnectionString.getUrlCreateExportImportJob();
                new HttpRequest(mockUrl, HttpMethod.POST, (byte[]) any);
                mockHttpRequest.setReadTimeoutMillis(anyInt);
                mockHttpRequest.setHeaderField("authorization", anyString);
                mockHttpRequest.setHeaderField("Request-Id", "1001");
                mockHttpRequest.setHeaderField("Accept", "application/json");
                mockHttpRequest.setHeaderField("Content-Type", "application/json");
                mockHttpRequest.setHeaderField("charset", "utf-8");
                mockHttpRequest.send();
                mockIotHubExceptionManager.httpResponseVerification((HttpResponse) any);
            }
        };
        assertNotNull(jobProperties);
    }

    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_076: [The function shall create an async wrapper around
    // the importDevices() function call, handle the return value or delegate exception]
    @Test (expected = Exception.class)
    public void importDevicesAsync_future_throw() throws Exception
    {
        new MockUp<RegistryManager>()
        {
            @Mock
            public JobProperties importDevices(String importBlobContainerUri, String outputBlobContainerUri)
                    throws IllegalArgumentException, IOException, IotHubException
            {
                throw new IllegalArgumentException();
            }
        };

        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        CompletableFuture<JobProperties> completableFuture =  registryManager.importDevicesAsync("importblob", "outputblob");
        completableFuture.get();
    }

    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_077: [The function shall throw IllegalArgumentException if the input parameter is null]
    @Test (expected = IllegalArgumentException.class)
    public void getJob_job_id_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.getJob(null);
    }

    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_078: [The function shall get the URL for the get request]
    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_079: [The function shall create a new SAS token for the get request]
    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_080: [The function shall create a new HttpRequest for getting the properties of a job]
    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_081: [The function shall send the created request and get the response]
    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_082: [The function shall verify the response status and throw proper Exception]
    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_083: [The function shall create a new JobProperties object from the response and return it]
    @Test
    public void getJob_good_case() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String jobId = "somejobid";

        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createConnectionString(connectionString);
                result = iotHubConnectionString;
                iotHubConnectionString.getUrlImportExportJob(jobId);
                result = mockUrl;
                mockHttpRequest.send();
                result = mockHttpResponse;
                mockIotHubExceptionManager.httpResponseVerification((HttpResponse) any);
                mockHttpResponse.getBody();
                result = jobPropertiesJson.getBytes();
            }
        };

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        JobProperties jobProperties = registryManager.getJob(jobId);

        new VerificationsInOrder()
        {
            {
                iotHubConnectionString.getUrlImportExportJob(jobId);
                new HttpRequest(mockUrl, HttpMethod.GET, (byte[]) any);
                mockHttpRequest.setReadTimeoutMillis(anyInt);
                mockHttpRequest.setHeaderField("authorization", anyString);
                mockHttpRequest.setHeaderField("Request-Id", "1001");
                mockHttpRequest.setHeaderField("Accept", "application/json");
                mockHttpRequest.setHeaderField("Content-Type", "application/json");
                mockHttpRequest.setHeaderField("charset", "utf-8");
                mockHttpRequest.send();
                mockIotHubExceptionManager.httpResponseVerification((HttpResponse) any);
            }
        };
        assertNotNull(jobProperties);
    }

    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_084: [The function shall create an async wrapper
    // around the getJob() function call, handle the return value or delegate exception]
    @Test (expected = Exception.class)
    public void getJobAsync_future_throw() throws Exception
    {
        new MockUp<RegistryManager>()
        {
            @Mock
            public JobProperties getJob(String jobId)
                    throws IllegalArgumentException, IOException, IotHubException
            {
                throw new IllegalArgumentException();
            }
        };

        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        CompletableFuture<JobProperties> completableFuture =  registryManager.getJobAsync("someJobId");
        completableFuture.get();
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_34_087: [The function shall tell this object's executor service to shutdown]
    @Test
    public void closeShutsDownExecutorService() throws IOException
    {
        //arrange
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createConnectionString(connectionString);
                result = iotHubConnectionString;
            }
        };

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        Deencapsulation.setField(registryManager,"executor", mockExecutorService);

        //act
        registryManager.close();

        new Verifications()
        {
            {
                mockExecutorService.shutdownNow();
            }
        };
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_001: [The constructor shall throw IllegalArgumentException if the input module is null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void addModule_input_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.addModule(null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_002: [The function shall deserialize the given module object to Json string]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_003: [The function shall get the URL for the module]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_004: [The function shall create a new SAS token for the module]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_005: [The function shall create a new HttpRequest for adding the module to IotHub]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_006: [The function shall send the created request and get the response]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_007: [The function shall verify the response status and throw proper Exception]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_008: [The function shall create a new module object from the response and return with it]
    @Test
    public void addModule_good_case() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String deviceId = "somedeviceId";
        String moduleId = "somemodule";

        new NonStrictExpectations()
        {
            {
                module.getDeviceId();
                result = deviceId;
                module.getId();
                result = moduleId;
            }
        };

        commonModuleExpectations(connectionString, deviceId, moduleId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        Module returnModule = registryManager.addModule(module);
        registryManager.close();

        commonModuleVerifications(HttpMethod.PUT, deviceId, moduleId, returnModule);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_009: [The constructor shall throw IllegalArgumentException if the deviceId string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getModule_deviceId_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.getModule(null, "somemodule");
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_009: [The constructor shall throw IllegalArgumentException if the deviceId string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getModule_deviceId_empty() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.getModule("", "somemodule");
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_010: [The constructor shall throw IllegalArgumentException if the moduleId string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getModule_moduleId_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.getModule("somedevice", null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_010: [The constructor shall throw IllegalArgumentException if the moduleId string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getModule_moduleId_empty() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.getModule("somedevice","");
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_011: [The function shall get the URL for the device]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_012: [The function shall create a new SAS token for the device]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_013: [The function shall create a new HttpRequest for getting a device from IotHub]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_014: [The function shall send the created request and get the response]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_015: [The function shall verify the response status and throw proper Exception]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_016: [The function shall create a new Device object from the response and return with it]
    @Test
    public void getModule_good_case() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String moduleId = "somemodule";
        String deviceId = "somedevice";

        commonExpectations(connectionString, moduleId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        Module returnModule = registryManager.getModule(deviceId, moduleId);

        commonModuleVerifications(HttpMethod.GET, deviceId, moduleId, returnModule);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_017: [The constructor shall throw IllegalArgumentException if the input string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getModulesOnDevice_deviceId_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.getModulesOnDevice(null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_017: [The constructor shall throw IllegalArgumentException if the input string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getModulesOnDevice_deviceId_empty() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.getModulesOnDevice("");
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_018: [The function shall get the URL for the device]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_019: [The function shall create a new SAS token for the device]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_020: [The function shall create a new HttpRequest for getting a device list from IotHub]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_021: [The function shall send the created request and get the response]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_022: [The function shall verify the response status and throw proper Exception]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_023: [The function shall create a new ArrayList<Module> object from the response and return with it]
    @Test
    public void getModulesOnDevice_good_case() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String deviceId = "somedevice";

        getModulesExpectations(connectionString, deviceId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        List<Module> modules =  registryManager.getModulesOnDevice(deviceId);

        getModulesVerifications(deviceId, modules);
        assertEquals(3, modules.size());
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_024: [The function shall throw IllegalArgumentException if the input device is null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void updateModule_input_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.updateModule(null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_025: [The function shall call updateDevice with forceUpdate = false]
    @Test
    public void updateModule_good_case() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String moduleId = "somemodule";
        String deviceId = "somedevice";

        new NonStrictExpectations()
        {
            {
                module.getDeviceId();
                result = deviceId;
                module.getId();
                result = moduleId;
            }
        };

        commonModuleExpectations(connectionString, deviceId, moduleId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        Module returnModule = registryManager.updateModule(module);
        commonModuleVerifications(HttpMethod.PUT, deviceId, moduleId, returnModule);

        new VerificationsInOrder()
        {
            {
                module.setForceUpdate(false);
                mockHttpRequest.setHeaderField("If-Match", "*");
            }
        };
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_026: [The function shall throw IllegalArgumentException if the input module is null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void updateModuleForce_input_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.updateModule(null, true);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_027: [The function shall set forceUpdate on the module]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_028: [The function shall get the URL for the module]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_029: [The function shall create a new SAS token for the module]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_030: [The function shall create a new HttpRequest for updating the module on IotHub]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_031: [The function shall send the created request and get the response]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_032: [The function shall verify the response status and throw proper Exception]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_033: [The function shall create a new Module object from the response and return with it]
    @Test
    public void updateModuleForce_good_case() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String moduleId = "somemodule";
        String deviceId = "somedevice";

        new NonStrictExpectations()
        {
            {
                module.getDeviceId();
                result = deviceId;
                module.getId();
                result = moduleId;

            }
        };

        Deencapsulation.setField(module, "deviceId", deviceId);
        Deencapsulation.setField(module, "id", moduleId);

        commonModuleExpectations(connectionString, deviceId, moduleId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        Module returnModule = registryManager.updateModule(module, true);

        commonModuleVerifications(HttpMethod.PUT, deviceId, moduleId, returnModule);

        new VerificationsInOrder()
        {
            {
                device.setForceUpdate(true);
                mockHttpRequest.setHeaderField("If-Match", "*");
            }
        };
    }


    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_082: [The function shall throw IllegalArgumentException if the input module is null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeModule_input_null_Module() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        Module module = null;
        registryManager.removeModule(module);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_036: [The function shall get the URL for the module]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_037: [The function shall create a new SAS token for the device]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_038: [The function shall create a new HttpRequest for removing the device from IotHub]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_039: [The function shall send the created request and get the response]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_040: [The function shall verify the response status and throw proper Exception]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_083: [The function shall use the module's object etag as the etag module to be remove]
    @Test
    public void removeModule_with_etag() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String deviceId = "somedevice";
        String moduleId = "somemodule";
        String etag = "someetag";

        commonModuleExpectations(connectionString, deviceId, moduleId);

        new NonStrictExpectations()
        {
            {
                module.getDeviceId();
                result = deviceId;
                module.getId();
                result = moduleId;
                module.geteTag();
                result = etag;
            }
        };

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        registryManager.removeModule(module);

        new VerificationsInOrder()
        {
            {
                iotHubConnectionString.getUrlModule(deviceId, moduleId);
                times = 1;
                new HttpRequest(mockUrl, HttpMethod.DELETE, new byte[0]);
                times = 1;
                mockHttpRequest.setReadTimeoutMillis(anyInt);
                mockHttpRequest.setHeaderField("authorization", anyString);
                mockHttpRequest.setHeaderField("If-Match", etag);
            }
        };
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_034: [The function shall throw IllegalArgumentException if the deviceId is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeModule_deviceId_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.removeModule(null, "somemodule");
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_034: [The function shall throw IllegalArgumentException if the deviceId is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeModule_deviceId_empty() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.removeModule("", "somemodule");
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_035: [The function shall throw IllegalArgumentException if the moduleId is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeModule_moduleId_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.removeModule("somedevice", null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_035: [The function shall throw IllegalArgumentException if the moduleId is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeModule_moduleId_empty() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.removeModule("somedevice", "");
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_078: [The function shall throw IllegalArgumentException if the etag is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeModule_Module_etag_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        new NonStrictExpectations()
        {
            {
                module.getDeviceId();
                result = "somedevice";
                module.getId();
                result = "somemodule";
                module.geteTag();
                result = null;
            }
        };

        registryManager.removeModule(module);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_078: [The function shall throw IllegalArgumentException if the etag is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeModule_Module_etag_empty() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        new NonStrictExpectations()
        {
            {
                module.getDeviceId();
                result = "somedevice";
                module.getId();
                result = "somemodule";
                module.geteTag();
                result = "";
            }
        };

        registryManager.removeModule(module);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_036: [The function shall get the URL for the module]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_037: [The function shall create a new SAS token for the device]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_038: [The function shall create a new HttpRequest for removing the device from IotHub]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_039: [The function shall send the created request and get the response]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_040: [The function shall verify the response status and throw proper Exception]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_086: [The function shall use * as the etag]
    @Test
    public void removeModule_good_case() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String deviceId = "somedevice";
        String moduleId = "somemodule";

        commonModuleExpectations(connectionString, deviceId, moduleId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        registryManager.removeModule(deviceId, moduleId);

        new VerificationsInOrder()
        {
            {
                iotHubConnectionString.getUrlModule(deviceId, moduleId);
                times = 1;
                new HttpRequest(mockUrl, HttpMethod.DELETE, new byte[0]);
                times = 1;
                mockHttpRequest.setReadTimeoutMillis(anyInt);
                mockHttpRequest.setHeaderField("authorization", anyString);
                mockHttpRequest.setHeaderField("If-Match", "*");
            }
        };
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_041: [The constructor shall throw IllegalArgumentException if the input configuration is null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void addConfiguration_input_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.addConfiguration(null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_042: [The function shall deserialize the given configuration object to Json string]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_043: [The function shall get the URL for the configuration]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_044: [The function shall create a new SAS token for the configuration]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_045: [The function shall create a new HttpRequest for adding the configuration to IotHub]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_046: [The function shall send the created request and get the response]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_047: [The function shall verify the response status and throw proper Exception]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_048: [The function shall create a new Configuration object from the response and return with it]
    @Test
    public void addConfiguration_good_case() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String configId = "someconfig";

        new NonStrictExpectations()
        {
            {
                config.getId();
                result = configId;
            }
        };

        commonConfigExpectations(connectionString, configId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        Configuration returnConfig = registryManager.addConfiguration(config);
        registryManager.close();

        commonConfigVerifications(HttpMethod.PUT, configId, returnConfig);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_049: [The constructor shall throw IllegalArgumentException if the configurationId string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getConfiguration_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.getConfiguration(null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_049: [The constructor shall throw IllegalArgumentException if the configurationId string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getConfiguration_empty() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.getConfiguration("");
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_050: [The function shall get the URL for the configuration]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_051: [The function shall create a new SAS token for the configuration]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_052: [The function shall create a new HttpRequest for getting a configuration from IotHub]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_053: [The function shall send the created request and get the response]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_054: [The function shall verify the response status and throw proper Exception]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_055: [The function shall create a new Configuration object from the response and return with it]
    @Test
    public void getConfiguration_good_case() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String configurationId = "someconfiguration";

        commonConfigExpectations(connectionString, configurationId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        Configuration returnConfiguration = registryManager.getConfiguration(configurationId);

        commonConfigVerifications(HttpMethod.GET, configurationId, returnConfiguration);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_056: [The function shall throw IllegalArgumentException if the input count number is less than 1]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getConfigurations_input_zero() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.getConfigurations(0);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_057: [The function shall get the URL for the device]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_058: [The function shall create a new SAS token for the device]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_059: [The function shall create a new HttpRequest for getting a device list from IotHub]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_060: [The function shall send the created request and get the response]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_061: [The function shall verify the response status and throw proper Exception]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_062: [The function shall create a new ArrayList<Device> object from the response and return with it]
    @Test
    public void getConfigurations_good_case() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        int numberOfConfigs = 10;

        getConfigsExpectations(connectionString, numberOfConfigs);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        List<Configuration> configs =  registryManager.getConfigurations(10);

        getConfigsVerifications(numberOfConfigs, configs);
        assertEquals(2, configs.size());
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_063: [The function shall throw IllegalArgumentException if the input device is null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void updateConfiguration_input_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.updateConfiguration(null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_064: [The function shall call updateConfiguration with forceUpdate = false]
    @Test
    public void updateConfiguration_good_case() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String configId = "someconfiguration";

        new NonStrictExpectations()
        {
            {
                config.getId();
                result = configId;
            }
        };

        commonConfigExpectations(connectionString, configId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        Configuration returnConfig = registryManager.updateConfiguration(config);
        commonConfigVerifications(HttpMethod.PUT, configId, returnConfig);

        new VerificationsInOrder()
        {
            {
                config.setForceUpdate(false);
                mockHttpRequest.setHeaderField("If-Match", "*");
            }
        };
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_065: [The function shall throw IllegalArgumentException if the input configuration is null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void updateConfigurationForce_input_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.updateConfiguration(null, true);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_066: [The function shall set forceUpdate on the configuration]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_067: [The function shall get the URL for the configuration]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_068: [The function shall create a new SAS token for the configuration]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_069: [The function shall create a new HttpRequest for updating the configuration on IotHub]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_070: [The function shall send the created request and get the response]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_071: [The function shall verify the response status and throw proper Exception]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_072: [The function shall create a new Configuration object from the response and return with it]
    @Test
    public void updateConfigurationForce_good_case() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String configId = "someconfiguration";

        new NonStrictExpectations()
        {
            {
                config.getId();
                result = configId;
            }
        };

        Deencapsulation.setField(config, "id", configId);

        commonConfigExpectations(connectionString, configId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        Configuration returnConfig = registryManager.updateConfiguration(config, true);

        commonConfigVerifications(HttpMethod.PUT, configId, returnConfig);

        new VerificationsInOrder()
        {
            {
                config.setForceUpdate(true);
                mockHttpRequest.setHeaderField("If-Match", "*");
            }
        };
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_083: [The function shall throw IllegalArgumentException if the input configuration is null]
    @Test (expected = IllegalArgumentException.class)
    public void removeConfiguration_input_Configuration_Null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        Configuration config = null;
        registryManager.removeConfiguration(config);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_087: [The function shall throw IllegalArgumentException if the input etag is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeDevice_input_Configuration_null_etag() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        new NonStrictExpectations()
        {
            {
                config.getId();
                result = "someconfig";
                device.geteTag();
                result = null;
            }
        };

        registryManager.removeConfiguration(config);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_087: [The function shall throw IllegalArgumentException if the input etag is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeDevice_input_Configuration_empty_etag() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        new NonStrictExpectations()
        {
            {
                config.getId();
                result = "someconfig";
                config.getEtag();
                result = "";
            }
        };

        registryManager.removeConfiguration(config);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_073: [The function shall throw IllegalArgumentException if the input string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeConfiguration_input_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.removeConfiguration((String)null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_073: [The function shall throw IllegalArgumentException if the input string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeConfiguration_input_empty() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        registryManager.removeConfiguration("");
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_074: [The function shall get the URL for the configuration]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_075: [The function shall create a new SAS token for the configuration]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_076: [The function shall create a new HttpRequest for removing the configuration from IotHub]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_077: [The function shall send the created request and get the response]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_078: [The function shall verify the response status and throw proper Exception]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_084: [The function shall call provide device object's etag as with etag of device to be removed]
    @Test
    public void removeConfiguration_with_etag() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String configId = "someconfig";
        String etag = "someetag";

        commonExpectations(connectionString, configId);

        new NonStrictExpectations()
        {
            {
                config.getId();
                result = configId;
                config.getEtag();
                result = etag;
            }
        };

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        registryManager.removeConfiguration(config);

        new VerificationsInOrder()
        {
            {
                iotHubConnectionString.getUrlConfiguration(configId);
                times = 1;
                new HttpRequest(mockUrl, HttpMethod.DELETE, new byte[0]);
                times = 1;
                mockHttpRequest.setReadTimeoutMillis(anyInt);
                mockHttpRequest.setHeaderField("authorization", anyString);
                mockHttpRequest.setHeaderField("If-Match", etag);
            }
        };
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_074: [The function shall get the URL for the configuration]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_075: [The function shall create a new SAS token for the configuration]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_076: [The function shall create a new HttpRequest for removing the configuration from IotHub]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_077: [The function shall send the created request and get the response]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_078: [The function shall verify the response status and throw proper Exception]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_085: [The function shall call removeDeviceOperation with * as the etag]
    @Test
    public void removeConfiguration_good_case() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String configId = "someconfiguration";

        commonExpectations(connectionString, configId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        registryManager.removeConfiguration(configId);

        new VerificationsInOrder()
        {
            {
                iotHubConnectionString.getUrlConfiguration(configId);
                times = 1;
                new HttpRequest(mockUrl, HttpMethod.DELETE, new byte[0]);
                times = 1;
                mockHttpRequest.setReadTimeoutMillis(anyInt);
                mockHttpRequest.setHeaderField("authorization", anyString);
                mockHttpRequest.setHeaderField("If-Match", "*");
            }
        };
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_34_088: [The function shall throw IllegalArgumentException if the provided content is null]
    @Test (expected = IllegalArgumentException.class)
    public void applyConfigurationContentOnDeviceThrowsIfConfigurationContentIsNull() throws Exception
    {
        //arrange
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String configId = "someconfiguration";

        commonExpectations(connectionString, configId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        //act
        registryManager.applyConfigurationContentOnDevice("some device", null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_34_089: [The function shall get the URL from the connection string using the provided deviceId]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_34_090: [The function shall create a new SAS token for the configuration]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_34_091: [The function shall create a new HTTP POST request with the created url, sas token, and the provided content in json form as the body.]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_34_092: [The function shall verify the response status and throw proper Exception]
    @Test
    public void applyConfigurationContentOnDeviceSuccess() throws Exception
    {
        //arrange
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String configId = "someconfiguration";
        String expectedJson = "some json";
        String expectedDeviceId = "someDevice";

        commonExpectations(connectionString, configId);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        new NonStrictExpectations()
        {
            {
                mockedConfigurationContent.toConfigurationContentParser();
                result = mockedConfigurationContentParser;

                mockedConfigurationContentParser.toJson();
                result = expectedJson;
            }
        };

        //act
        registryManager.applyConfigurationContentOnDevice(expectedDeviceId, mockedConfigurationContent);

        //assert
        new Verifications()
        {
            {
                iotHubConnectionString.getUrlApplyConfigurationContent(expectedDeviceId);
                times = 1;

                new HttpRequest(mockUrl, HttpMethod.POST, expectedJson.getBytes());
                times = 1;

                new IotHubServiceSasToken(iotHubConnectionString);
                times = 1;

                mockHttpRequest.send();
                times = 1;

                mockIotHubExceptionManager.httpResponseVerification(mockHttpResponse);
                times = 1;

            }
        };
    }

    private void commonExpectations(String connectionString, String deviceId) throws Exception
    {
        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createConnectionString(connectionString);
                result = iotHubConnectionString;
                iotHubConnectionString.getUrlDevice(deviceId);
                result = mockUrl;
                mockHttpRequest.send();
                result = mockHttpResponse;
                mockIotHubExceptionManager.httpResponseVerification((HttpResponse) any);
                mockHttpResponse.getBody();
                result = deviceJson.getBytes();
                Deencapsulation.invoke(device, "toDeviceParser");
                result = new DeviceParser();
            }
        };
    }

    private void commonVerifications(HttpMethod httpMethod, String requestDeviceId, Device responseDevice) throws Exception
    {
        new VerificationsInOrder()
        {
            {
                iotHubConnectionString.getUrlDevice(requestDeviceId);
                new HttpRequest(mockUrl, httpMethod, (byte[]) any);
                mockHttpRequest.setReadTimeoutMillis(anyInt);
                mockHttpRequest.setHeaderField("authorization", anyString);
                mockHttpRequest.setHeaderField("Request-Id", "1001");
                mockHttpRequest.setHeaderField("Accept", "application/json");
                mockHttpRequest.setHeaderField("Content-Type", "application/json");
                mockHttpRequest.setHeaderField("charset", "utf-8");
                mockHttpRequest.send();
                mockIotHubExceptionManager.httpResponseVerification((HttpResponse) any);
            }
        };
        assertNotNull(responseDevice);
    }

    private void getDevicesExpectations(String connectionString, int numberOfDevices) throws Exception
    {
        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createConnectionString(connectionString);
                result = iotHubConnectionString;
                iotHubConnectionString.getUrlDeviceList(numberOfDevices);
                result = mockUrl;
                mockHttpRequest.send();
                result = mockHttpResponse;
                mockIotHubExceptionManager.httpResponseVerification((HttpResponse) any);
                mockHttpResponse.getBody();
                result = devicesJson.getBytes();
            }
        };
    }

    private void getDevicesVerifications(int numberOfDevices, ArrayList<Device> devices) throws Exception
    {
        new VerificationsInOrder()
        {
            {
                iotHubConnectionString.getUrlDeviceList(numberOfDevices);
                times = 1;
                new HttpRequest(mockUrl, HttpMethod.GET, (byte[]) any);
                times = 1;
                mockHttpRequest.setReadTimeoutMillis(anyInt);
                mockHttpRequest.setHeaderField("authorization", anyString);
                mockHttpRequest.setHeaderField("Request-Id", "1001");
                mockHttpRequest.setHeaderField("Accept", "application/json");
                mockHttpRequest.setHeaderField("Content-Type", "application/json");
                mockHttpRequest.setHeaderField("charset", "utf-8");
                mockHttpRequest.send();
            }
        };
        assertNotNull(devices);
    }

    private void commonModuleExpectations(String connectionString, String deviceId, String moduleId) throws Exception
    {
        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createConnectionString(connectionString);
                result = iotHubConnectionString;
                iotHubConnectionString.getUrlModule(deviceId, moduleId);
                result = mockUrl;
                mockHttpRequest.send();
                result = mockHttpResponse;
                mockIotHubExceptionManager.httpResponseVerification((HttpResponse) any);
                mockHttpResponse.getBody();
                result = moduleJson.getBytes();
                Deencapsulation.invoke(module, "toDeviceParser");
                result = new DeviceParser();
            }
        };
    }

    private void commonModuleVerifications(HttpMethod httpMethod, String requestDeviceId, String requestModuleId, Module responseModule) throws Exception
    {
        new VerificationsInOrder()
        {
            {
                iotHubConnectionString.getUrlModule(requestDeviceId, requestModuleId);
                new HttpRequest(mockUrl, httpMethod, (byte[]) any);
                mockHttpRequest.setReadTimeoutMillis(anyInt);
                mockHttpRequest.setHeaderField("authorization", anyString);
                mockHttpRequest.setHeaderField("Request-Id", "1001");
                mockHttpRequest.setHeaderField("Accept", "application/json");
                mockHttpRequest.setHeaderField("Content-Type", "application/json");
                mockHttpRequest.setHeaderField("charset", "utf-8");
                mockHttpRequest.send();
                mockIotHubExceptionManager.httpResponseVerification((HttpResponse) any);
            }
        };
        assertNotNull(responseModule);
    }

    private void getModulesExpectations(String connectionString, String deviceId) throws Exception
    {
        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createConnectionString(connectionString);
                result = iotHubConnectionString;
                iotHubConnectionString.getUrlModulesOnDevice(deviceId);
                result = mockUrl;
                mockHttpRequest.send();
                result = mockHttpResponse;
                mockIotHubExceptionManager.httpResponseVerification((HttpResponse) any);
                mockHttpResponse.getBody();
                result = modulesJson.getBytes();
            }
        };
    }

    private void getModulesVerifications(String deviceId, List<Module> modules) throws Exception
    {
        new VerificationsInOrder()
        {
            {
                iotHubConnectionString.getUrlModulesOnDevice(deviceId);
                times = 1;
                new HttpRequest(mockUrl, HttpMethod.GET, (byte[]) any);
                times = 1;
                mockHttpRequest.setReadTimeoutMillis(anyInt);
                mockHttpRequest.setHeaderField("authorization", anyString);
                mockHttpRequest.setHeaderField("Request-Id", "1001");
                mockHttpRequest.setHeaderField("Accept", "application/json");
                mockHttpRequest.setHeaderField("Content-Type", "application/json");
                mockHttpRequest.setHeaderField("charset", "utf-8");
                mockHttpRequest.send();
            }
        };
        assertNotNull(modules);
    }

    private void commonConfigExpectations(String connectionString, String configId) throws Exception
    {
        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createConnectionString(connectionString);
                result = iotHubConnectionString;
                iotHubConnectionString.getUrlConfiguration(configId);
                result = mockUrl;
                mockHttpRequest.send();
                result = mockHttpResponse;
                mockIotHubExceptionManager.httpResponseVerification((HttpResponse) any);
                mockHttpResponse.getBody();
                result = configJson.getBytes();
                Deencapsulation.invoke(config, "toConfigurationParser");
                result = new ConfigurationParser();
            }
        };
    }

    private void commonConfigVerifications(HttpMethod httpMethod, String requestConfigId, Configuration responseConfig) throws Exception
    {
        new VerificationsInOrder()
        {
            {
                iotHubConnectionString.getUrlConfiguration(requestConfigId);
                new HttpRequest(mockUrl, httpMethod, (byte[]) any);
                mockHttpRequest.setReadTimeoutMillis(anyInt);
                mockHttpRequest.setHeaderField("authorization", anyString);
                mockHttpRequest.setHeaderField("Request-Id", "1001");
                mockHttpRequest.setHeaderField("Accept", "application/json");
                mockHttpRequest.setHeaderField("Content-Type", "application/json");
                mockHttpRequest.setHeaderField("charset", "utf-8");
                mockHttpRequest.send();
                mockIotHubExceptionManager.httpResponseVerification((HttpResponse) any);
            }
        };
        assertNotNull(responseConfig);
    }

    private void getConfigsExpectations(String connectionString, int numOfConfigs) throws Exception
    {
        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createConnectionString(connectionString);
                result = iotHubConnectionString;
                iotHubConnectionString.getUrlConfigurationsList(numOfConfigs);
                result = mockUrl;
                mockHttpRequest.send();
                result = mockHttpResponse;
                mockIotHubExceptionManager.httpResponseVerification((HttpResponse) any);
                mockHttpResponse.getBody();
                result = configsJson.getBytes();
            }
        };
    }

    private void getConfigsVerifications(int numOfConfigs, List<Configuration> configs) throws Exception
    {
        new VerificationsInOrder()
        {
            {
                iotHubConnectionString.getUrlConfigurationsList(numOfConfigs);
                times = 1;
                new HttpRequest(mockUrl, HttpMethod.GET, (byte[]) any);
                times = 1;
                mockHttpRequest.setReadTimeoutMillis(anyInt);
                mockHttpRequest.setHeaderField("authorization", anyString);
                mockHttpRequest.setHeaderField("Request-Id", "1001");
                mockHttpRequest.setHeaderField("Accept", "application/json");
                mockHttpRequest.setHeaderField("Content-Type", "application/json");
                mockHttpRequest.setHeaderField("charset", "utf-8");
                mockHttpRequest.send();
            }
        };
        assertNotNull(configs);
    }

}