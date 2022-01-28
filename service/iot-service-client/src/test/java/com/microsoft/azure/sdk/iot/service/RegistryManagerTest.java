/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.service.serializers.ConfigurationContentParser;
import com.microsoft.azure.sdk.iot.service.serializers.ConfigurationParser;
import com.microsoft.azure.sdk.iot.service.serializers.DeviceParser;
import com.microsoft.azure.sdk.iot.service.serializers.StorageAuthenticationType;
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
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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

    @Test
    public void testOptionsDefaults()
    {
        RegistryManagerOptions options = RegistryManagerOptions.builder().build();
        assertEquals((int) Deencapsulation.getField(RegistryManagerOptions.class, "DEFAULT_HTTP_READ_TIMEOUT_MS"), options.getHttpReadTimeout());
        assertEquals((int) Deencapsulation.getField(RegistryManagerOptions.class, "DEFAULT_HTTP_CONNECT_TIMEOUT_MS"), options.getHttpConnectTimeout());
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_001: [The constructor shall throw IllegalArgumentException if the input string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructor_input_null() throws Exception
    {
        String connectionString = null;

        new RegistryManager(connectionString);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_001: [The constructor shall throw IllegalArgumentException if the input string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructor_input_empty() throws Exception
    {
        String connectionString = null;

        new RegistryManager(connectionString);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_002: [The constructor shall create an IotHubConnectionString object from the given connection string]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_003: [The constructor shall create a new RegistryManager, stores the created IotHubConnectionString object and return with it]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_34_090: [The function shall start this object's executor service]
    @Test
    public void constructor_good_case() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

        assertNotNull(registryManager);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_004: [The constructor shall throw IllegalArgumentException if the input device is null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void addDevice_input_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

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

        RegistryManager registryManager = new RegistryManager(connectionString);
        Device returnDevice = registryManager.addDevice(device);

        commonVerifications(HttpMethod.PUT, deviceId, returnDevice);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_014: [The constructor shall throw IllegalArgumentException if the input string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getDevice_input_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

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

        RegistryManager registryManager = new RegistryManager(connectionString);
        Device returnDevice = registryManager.getDevice(deviceId);

        commonVerifications(HttpMethod.GET, deviceId, returnDevice);
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

        RegistryManager registryManager = new RegistryManager(connectionString);
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

        RegistryManager registryManager = new RegistryManager(connectionString);
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
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);
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

        RegistryManager registryManager = new RegistryManager(connectionString);
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

        RegistryManager registryManager = new RegistryManager(connectionString);
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

        RegistryManager registryManager = new RegistryManager(connectionString);
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

        RegistryManager registryManager = new RegistryManager(connectionString);
        Device returnDevice = registryManager.getDevice(deviceId);
        registryManager.getDeviceConnectionString(returnDevice);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_032: [The function shall throw IllegalArgumentException if the input device is null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void updateDevice_input_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        new Expectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = iotHubConnectionString;
                iotHubConnectionString.getHostName();
                result = "aaa.bbb.ccc";
            }
        };

        RegistryManager registryManager = new RegistryManager(connectionString);

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

        RegistryManager registryManager = new RegistryManager(connectionString);
        Device returnDevice = registryManager.updateDevice(device);
        commonVerifications(HttpMethod.PUT, deviceId, returnDevice);

        new VerificationsInOrder()
        {
            {
                mockHttpRequest.setHeaderField("If-Match", "*");
            }
        };
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_081: [The function shall throw IllegalArgumentException if the input device is null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeDevice_input_null_Device() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

        Device device = null;
        registryManager.removeDevice(device);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_087: [The function shall throw IllegalArgumentException if the input etag is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeDevice_input_Device_null_etag() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

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
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

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

        RegistryManager registryManager = new RegistryManager(connectionString);
        registryManager.removeDevice(device);

        new VerificationsInOrder()
        {
            {
                IotHubConnectionString.getUrlDevice(anyString, deviceId);
                times = 1;
                new HttpRequest(mockUrl, HttpMethod.DELETE, new byte[0], anyString, (Proxy) any);
                times = 1;
                mockHttpRequest.setReadTimeoutMillis(anyInt);
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
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

        String deviceId = null;
        registryManager.removeDevice(deviceId);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_046: [The function shall throw IllegalArgumentException if the input deviceId is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeDevice_input_empty() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

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

        RegistryManager registryManager = new RegistryManager(connectionString);
        registryManager.removeDevice(deviceId);

        new VerificationsInOrder()
        {
            {
                IotHubConnectionString.getUrlDevice(anyString, deviceId);
                times = 1;
                new HttpRequest(mockUrl, HttpMethod.DELETE, new byte[0], anyString, (Proxy) any);
                times = 1;
                mockHttpRequest.setReadTimeoutMillis(anyInt);
                mockHttpRequest.setHeaderField("If-Match", "*");
            }
        };
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_054: [The function shall get the URL for the device]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_055: [The function shall create a new SAS token for the device]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_056: [The function shall create a new HttpRequest for getting statistics a device from IotHub]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_057: [The function shall send the created request and get the response]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_058: [The function shall verify the response status and throw proper Exception]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_059: [The function shall create a new RegistryStatistics object from the response and return with it]
    @Test
    public void getStatistics_good_case(@Mocked Proxy mockProxy, @Mocked ProxyOptions mockProxyOptions, @Mocked RegistryManagerOptions registryManagerOptions) throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String deviceId = "somedevice";

        commonExpectations(connectionString, deviceId);

        RegistryManager registryManager = new RegistryManager(connectionString, registryManagerOptions);

        new Expectations()
        {
            {
                IotHubConnectionString.getUrlDeviceStatistics(anyString);
                registryManagerOptions.getProxyOptions();
                result = mockProxyOptions;
                mockProxyOptions.getProxy();
                result = mockProxy;
                new HttpRequest(mockUrl, HttpMethod.GET, new byte[0], anyString, mockProxy);
                mockHttpRequest.setReadTimeoutMillis(anyInt);
                mockHttpRequest.send();
                IotHubExceptionManager.httpResponseVerification((HttpResponse) any);
            }
        };

        // act
        RegistryStatistics statistics = registryManager.getStatistics();

        assertNotNull(statistics);
    }

    @Test
    public void exportDevices_jobProperties_good_case() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";

        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = iotHubConnectionString;
                iotHubConnectionString.getHostName();
                result = "aaa.bbb.ccc";
                IotHubConnectionString.getUrlCreateExportImportJob(anyString);
                result = mockUrl;
                mockHttpRequest.send();
                result = mockHttpResponse;
                IotHubExceptionManager.httpResponseVerification((HttpResponse) any);
                mockHttpResponse.getBody();
                result = jobPropertiesJson.getBytes(StandardCharsets.UTF_8);
            }
        };

        RegistryManager registryManager = new RegistryManager(connectionString);
        JobProperties exportJobProperties =
                JobProperties.createForExportJob("blob1", true, StorageAuthenticationType.IDENTITY);
        JobProperties jobProperties = registryManager.exportDevices(exportJobProperties);

        new VerificationsInOrder()
        {
            {
                IotHubConnectionString.getUrlCreateExportImportJob(anyString);
                new HttpRequest(mockUrl, HttpMethod.POST, (byte[]) any, anyString, (Proxy) any);
                mockHttpRequest.setReadTimeoutMillis(anyInt);
                mockHttpRequest.send();
                IotHubExceptionManager.httpResponseVerification((HttpResponse) any);
            }
        };
        assertNotNull(jobProperties);
    }

    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_061: [The function shall throw IllegalArgumentException if any of the input parameters is null]
    @Test (expected = IllegalArgumentException.class)
    public void exportDevices_blob_input_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

        registryManager.exportDevices(null, true);
    }

    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_061: [The function shall throw IllegalArgumentException if any of the input parameters is null]
    @Test (expected = IllegalArgumentException.class)
    public void exportDevices_exclude_keys_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

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
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = iotHubConnectionString;
                iotHubConnectionString.getHostName();
                result = "aaa.bbb.ccc";
                IotHubConnectionString.getUrlCreateExportImportJob(anyString);
                result = mockUrl;
                mockHttpRequest.send();
                result = mockHttpResponse;
                IotHubExceptionManager.httpResponseVerification((HttpResponse) any);
                mockHttpResponse.getBody();
                result = jobPropertiesJson.getBytes(StandardCharsets.UTF_8);
            }
        };

        RegistryManager registryManager = new RegistryManager(connectionString);
        JobProperties jobProperties = registryManager.exportDevices("blob1", true);

        new VerificationsInOrder()
        {
            {
                IotHubConnectionString.getUrlCreateExportImportJob(anyString);
                new HttpRequest(mockUrl, HttpMethod.POST, (byte[]) any, anyString, (Proxy) any);
                mockHttpRequest.setReadTimeoutMillis(anyInt);
                mockHttpRequest.send();
                IotHubExceptionManager.httpResponseVerification((HttpResponse) any);
            }
        };
        assertNotNull(jobProperties);
    }

    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_069: [The function shall throw IllegalArgumentException if any of the input parameters is null]
    @Test (expected = IllegalArgumentException.class)
    public void importDevices_blob_import_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

        registryManager.importDevices(null, "outputblob");
    }

    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_069: [The function shall throw IllegalArgumentException if any of the input parameters is null]
    @Test (expected = IllegalArgumentException.class)
    public void importDevices_blob_output_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

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
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = iotHubConnectionString;
                iotHubConnectionString.getHostName();
                result = "aaa.bbb.ccc";
                IotHubConnectionString.getUrlCreateExportImportJob(anyString);
                result = mockUrl;
                mockHttpRequest.send();
                result = mockHttpResponse;
                IotHubExceptionManager.httpResponseVerification((HttpResponse) any);
                mockHttpResponse.getBody();
                result = jobPropertiesJson.getBytes(StandardCharsets.UTF_8);
            }
        };

        RegistryManager registryManager = new RegistryManager(connectionString);
        JobProperties jobProperties = registryManager.importDevices("blob1", "blob2");

        new VerificationsInOrder()
        {
            {
                IotHubConnectionString.getUrlCreateExportImportJob(anyString);
                new HttpRequest(mockUrl, HttpMethod.POST, (byte[]) any, anyString, (Proxy) any);
                mockHttpRequest.setReadTimeoutMillis(anyInt);
                mockHttpRequest.send();
                IotHubExceptionManager.httpResponseVerification((HttpResponse) any);
            }
        };
        assertNotNull(jobProperties);
    }

    @Test
    public void importDevices_jobProperties_good_case() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";

        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = iotHubConnectionString;
                iotHubConnectionString.getHostName();
                result = "aaa.bbb.ccc";
                IotHubConnectionString.getUrlCreateExportImportJob(anyString);
                result = mockUrl;
                mockHttpRequest.send();
                result = mockHttpResponse;
                IotHubExceptionManager.httpResponseVerification((HttpResponse) any);
                mockHttpResponse.getBody();
                result = jobPropertiesJson.getBytes(StandardCharsets.UTF_8);
            }
        };

        RegistryManager registryManager = new RegistryManager(connectionString);
        JobProperties inputParameters =
                JobProperties.createForImportJob("blob1", "blob2", StorageAuthenticationType.IDENTITY);
        JobProperties importJobProperties = registryManager.importDevices(inputParameters);

        new VerificationsInOrder()
        {
            {
                IotHubConnectionString.getUrlCreateExportImportJob(anyString);
                new HttpRequest(mockUrl, HttpMethod.POST, (byte[]) any, anyString, (Proxy) any);
                mockHttpRequest.setReadTimeoutMillis(anyInt);
                mockHttpRequest.send();
                IotHubExceptionManager.httpResponseVerification((HttpResponse) any);
            }
        };
        assertNotNull(importJobProperties);
    }

    // TESTS_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_15_077: [The function shall throw IllegalArgumentException if the input parameter is null]
    @Test (expected = IllegalArgumentException.class)
    public void getJob_job_id_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

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
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = iotHubConnectionString;
                iotHubConnectionString.getHostName();
                result = "aaa.bbb.ccc";
                IotHubConnectionString.getUrlImportExportJob(anyString, jobId);
                result = mockUrl;
                mockHttpRequest.send();
                result = mockHttpResponse;
                IotHubExceptionManager.httpResponseVerification((HttpResponse) any);
                mockHttpResponse.getBody();
                result = jobPropertiesJson.getBytes(StandardCharsets.UTF_8);
            }
        };

        RegistryManager registryManager = new RegistryManager(connectionString);
        JobProperties jobProperties = registryManager.getJob(jobId);

        new VerificationsInOrder()
        {
            {
                IotHubConnectionString.getUrlImportExportJob(anyString, jobId);
                new HttpRequest(mockUrl, HttpMethod.GET, (byte[]) any, anyString, (Proxy) any);
                mockHttpRequest.setReadTimeoutMillis(anyInt);
                mockHttpRequest.send();
            }
        };
        assertNotNull(jobProperties);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_001: [The constructor shall throw IllegalArgumentException if the input module is null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void addModule_input_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        new Expectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = iotHubConnectionString;
                iotHubConnectionString.getHostName();
                result = "aaa.bbb.ccc";
            }
        };

        RegistryManager registryManager = new RegistryManager(connectionString);

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

        RegistryManager registryManager = new RegistryManager(connectionString);
        Module returnModule = registryManager.addModule(module);

        commonModuleVerifications(HttpMethod.PUT, deviceId, moduleId, returnModule);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_009: [The constructor shall throw IllegalArgumentException if the deviceId string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getModule_deviceId_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

        registryManager.getModule(null, "somemodule");
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_009: [The constructor shall throw IllegalArgumentException if the deviceId string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getModule_deviceId_empty() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

        registryManager.getModule("", "somemodule");
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_010: [The constructor shall throw IllegalArgumentException if the moduleId string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getModule_moduleId_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

        registryManager.getModule("somedevice", null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_010: [The constructor shall throw IllegalArgumentException if the moduleId string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getModule_moduleId_empty() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

        registryManager.getModule("somedevice","");
    }

    private void constructorExpectations(String connectionString)
    {
        new Expectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = iotHubConnectionString;
                iotHubConnectionString.getHostName();
                result = "aaa.bbb.ccc";
            }
        };
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

        RegistryManager registryManager = new RegistryManager(connectionString);
        Module returnModule = registryManager.getModule(deviceId, moduleId);

        commonModuleVerifications(HttpMethod.GET, deviceId, moduleId, returnModule);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_017: [The constructor shall throw IllegalArgumentException if the input string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getModulesOnDevice_deviceId_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

        registryManager.getModulesOnDevice(null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_017: [The constructor shall throw IllegalArgumentException if the input string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getModulesOnDevice_deviceId_empty() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

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

        RegistryManager registryManager = new RegistryManager(connectionString);
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
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

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

        RegistryManager registryManager = new RegistryManager(connectionString);
        Module returnModule = registryManager.updateModule(module);
        commonModuleVerifications(HttpMethod.PUT, deviceId, moduleId, returnModule);

        new VerificationsInOrder()
        {
            {
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
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

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

        RegistryManager registryManager = new RegistryManager(connectionString);
        registryManager.removeModule(module);

        new VerificationsInOrder()
        {
            {
                IotHubConnectionString.getUrlModule(anyString, deviceId, moduleId);
                times = 1;
                new HttpRequest(mockUrl, HttpMethod.DELETE, new byte[0], anyString, (Proxy) any);
                times = 1;
                mockHttpRequest.setReadTimeoutMillis(anyInt);
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
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

        registryManager.removeModule(null, "somemodule");
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_034: [The function shall throw IllegalArgumentException if the deviceId is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeModule_deviceId_empty() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

        registryManager.removeModule("", "somemodule");
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_035: [The function shall throw IllegalArgumentException if the moduleId is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeModule_moduleId_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

        registryManager.removeModule("somedevice", null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_035: [The function shall throw IllegalArgumentException if the moduleId is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeModule_moduleId_empty() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

        registryManager.removeModule("somedevice", "");
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_078: [The function shall throw IllegalArgumentException if the etag is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeModule_Module_etag_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

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
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

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

        RegistryManager registryManager = new RegistryManager(connectionString);
        registryManager.removeModule(deviceId, moduleId);

        new VerificationsInOrder()
        {
            {
                IotHubConnectionString.getUrlModule(anyString, deviceId, moduleId);
                times = 1;
                new HttpRequest(mockUrl, HttpMethod.DELETE, new byte[0], anyString, (Proxy) any);
                times = 1;
                mockHttpRequest.setReadTimeoutMillis(anyInt);
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
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

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

        RegistryManager registryManager = new RegistryManager(connectionString);
        Configuration returnConfig = registryManager.addConfiguration(config);

        commonConfigVerifications(HttpMethod.PUT, configId, returnConfig);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_049: [The constructor shall throw IllegalArgumentException if the configurationId string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getConfiguration_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

        registryManager.getConfiguration(null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_049: [The constructor shall throw IllegalArgumentException if the configurationId string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getConfiguration_empty() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

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

        RegistryManager registryManager = new RegistryManager(connectionString);
        Configuration returnConfiguration = registryManager.getConfiguration(configurationId);

        commonConfigVerifications(HttpMethod.GET, configurationId, returnConfiguration);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_056: [The function shall throw IllegalArgumentException if the input count number is less than 1]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getConfigurations_input_zero() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

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

        RegistryManager registryManager = new RegistryManager(connectionString);
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
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

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

        RegistryManager registryManager = new RegistryManager(connectionString);
        Configuration returnConfig = registryManager.updateConfiguration(config);
        commonConfigVerifications(HttpMethod.PUT, configId, returnConfig);

        new VerificationsInOrder()
        {
            {
                mockHttpRequest.setHeaderField("If-Match", "*");
            }
        };
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_083: [The function shall throw IllegalArgumentException if the input configuration is null]
    @Test (expected = IllegalArgumentException.class)
    public void removeConfiguration_input_Configuration_Null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

        Configuration config = null;
        registryManager.removeConfiguration(config);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_087: [The function shall throw IllegalArgumentException if the input etag is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeDevice_input_Configuration_null_etag() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

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
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

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
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

        registryManager.removeConfiguration((String)null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_073: [The function shall throw IllegalArgumentException if the input string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeConfiguration_input_empty() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryManager registryManager = new RegistryManager(connectionString);

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

        RegistryManager registryManager = new RegistryManager(connectionString);
        registryManager.removeConfiguration(config);

        new VerificationsInOrder()
        {
            {
                IotHubConnectionString.getUrlConfiguration(anyString, configId);
                times = 1;
                new HttpRequest(mockUrl, HttpMethod.DELETE, new byte[0], anyString, (Proxy) any);
                times = 1;
                mockHttpRequest.setReadTimeoutMillis(anyInt);
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

        RegistryManager registryManager = new RegistryManager(connectionString);
        registryManager.removeConfiguration(configId);

        new VerificationsInOrder()
        {
            {
                IotHubConnectionString.getUrlConfiguration(anyString, configId);
                times = 1;
                new HttpRequest(mockUrl, HttpMethod.DELETE, new byte[0], anyString, (Proxy) any);
                times = 1;
                mockHttpRequest.setReadTimeoutMillis(anyInt);
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

        RegistryManager registryManager = new RegistryManager(connectionString);

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

        RegistryManager registryManager = new RegistryManager(connectionString);

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
                IotHubConnectionString.getUrlApplyConfigurationContent(anyString, expectedDeviceId);
                times = 1;

                new HttpRequest(mockUrl, HttpMethod.POST, expectedJson.getBytes(StandardCharsets.UTF_8), anyString, (Proxy) any);
                times = 1;

                mockHttpRequest.send();
                times = 1;

                IotHubExceptionManager.httpResponseVerification(mockHttpResponse);
                times = 1;

            }
        };
    }

    @Test
    public void getDevicesWithCustomHttpTimeouts(@Mocked final IotHubConnectionString mockIotHubConnectionString,
                                                 @Mocked final DeviceParser mockDeviceParser,
                                                 @Mocked final Device mockDevice,
                                                 @Mocked final RegistryManagerOptions mockOptions)
            throws IOException, IotHubException
    {
        // arrange
        final int expectedHttpConnectTimeout = 1234;
        final int expectedHttpReadTimeout = 5678;
        final String mockDeviceId = "someDeviceToGet";

        String mockConnectionString = "someValidConnectionString";

        new Expectations()
        {
            {
                IotHubConnectionString.createIotHubConnectionString(mockConnectionString);
                result = mockIotHubConnectionString;
                IotHubConnectionString.getUrlDevice(anyString, mockDeviceId);
                result = mockUrl;
                mockIotHubConnectionString.getHostName();
                result = "someHostname";
                new HttpRequest(mockUrl, HttpMethod.GET, (byte[]) any, anyString, (Proxy) any);
                result = mockHttpRequest;
                new DeviceParser(anyString);
                result = mockDeviceParser;
                Deencapsulation.newInstance(Device.class, mockDeviceParser);
                result = mockDevice;
                mockOptions.getHttpConnectTimeout();
                result = expectedHttpConnectTimeout;
                mockOptions.getHttpReadTimeout();
                result = expectedHttpReadTimeout;
            }
        };

        RegistryManager registryManager = new RegistryManager(mockConnectionString, mockOptions);

        // act
        registryManager.getDevice(mockDeviceId);

        // assert
        new Verifications()
        {
            {
                mockHttpRequest.setConnectTimeoutMillis(expectedHttpConnectTimeout);
                mockHttpRequest.setReadTimeoutMillis(expectedHttpReadTimeout);
            }
        };
    }

    private void commonExpectations(String connectionString, String deviceId) throws Exception
    {
        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = iotHubConnectionString;
                iotHubConnectionString.getHostName();
                result = "aaa.bbb.ccc";
                IotHubConnectionString.getUrlDevice(anyString, deviceId);
                result = mockUrl;
                mockHttpRequest.send();
                result = mockHttpResponse;
                IotHubExceptionManager.httpResponseVerification((HttpResponse) any);
                mockHttpResponse.getBody();
                result = deviceJson.getBytes(StandardCharsets.UTF_8);
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
                IotHubConnectionString.getUrlDevice(anyString, requestDeviceId);
                new HttpRequest(mockUrl, httpMethod, (byte[]) any, anyString, (Proxy) any);
                mockHttpRequest.setReadTimeoutMillis(anyInt);
                mockHttpRequest.send();
            }
        };
        assertNotNull(responseDevice);
    }

    private void getDevicesExpectations(String connectionString, int numberOfDevices) throws Exception
    {
        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = iotHubConnectionString;
                iotHubConnectionString.getHostName();
                result = "aaa.bbb.ccc";
                IotHubConnectionString.getUrlDeviceList(anyString, numberOfDevices);
                result = mockUrl;
                mockHttpRequest.send();
                result = mockHttpResponse;
                IotHubExceptionManager.httpResponseVerification((HttpResponse) any);
                mockHttpResponse.getBody();
                result = devicesJson.getBytes(StandardCharsets.UTF_8);
            }
        };
    }

    private void getDevicesVerifications(int numberOfDevices, ArrayList<Device> devices) throws Exception
    {
        new VerificationsInOrder()
        {
            {
                IotHubConnectionString.getUrlDeviceList(anyString, numberOfDevices);
                times = 1;
                new HttpRequest(mockUrl, HttpMethod.GET, (byte[]) any, anyString, (Proxy) any);
                times = 1;
                mockHttpRequest.setReadTimeoutMillis(anyInt);
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
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = iotHubConnectionString;
                iotHubConnectionString.getHostName();
                result = "aaa.bbb.ccc";
                IotHubConnectionString.getUrlModule(anyString, deviceId, moduleId);
                result = mockUrl;
                mockHttpRequest.send();
                result = mockHttpResponse;
                IotHubExceptionManager.httpResponseVerification((HttpResponse) any);
                mockHttpResponse.getBody();
                result = moduleJson.getBytes(StandardCharsets.UTF_8);
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
                IotHubConnectionString.getUrlModule(anyString, requestDeviceId, requestModuleId);
                new HttpRequest(mockUrl, httpMethod, (byte[]) any, anyString, (Proxy) any);
                mockHttpRequest.setReadTimeoutMillis(anyInt);
                mockHttpRequest.setConnectTimeoutMillis(anyInt);
                mockHttpRequest.send();
            }
        };
        assertNotNull(responseModule);
    }

    private void getModulesExpectations(String connectionString, String deviceId) throws Exception
    {
        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = iotHubConnectionString;
                iotHubConnectionString.getHostName();
                result = "aaa.bbb.ccc";
                IotHubConnectionString.getUrlModulesOnDevice(anyString, deviceId);
                result = mockUrl;
                mockHttpRequest.send();
                result = mockHttpResponse;
                IotHubExceptionManager.httpResponseVerification((HttpResponse) any);
                mockHttpResponse.getBody();
                result = modulesJson.getBytes(StandardCharsets.UTF_8);
            }
        };
    }

    private void getModulesVerifications(String deviceId, List<Module> modules) throws Exception
    {
        new VerificationsInOrder()
        {
            {
                IotHubConnectionString.getUrlModulesOnDevice(anyString, deviceId);
                times = 1;
                new HttpRequest(mockUrl, HttpMethod.GET, (byte[]) any, anyString, (Proxy) any);
                times = 1;
                mockHttpRequest.setReadTimeoutMillis(anyInt);
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
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = iotHubConnectionString;
                iotHubConnectionString.getHostName();
                result = "aaa.bbb.ccc";
                IotHubConnectionString.getUrlConfiguration(anyString, configId);
                result = mockUrl;
                mockHttpRequest.send();
                result = mockHttpResponse;
                IotHubExceptionManager.httpResponseVerification((HttpResponse) any);
                mockHttpResponse.getBody();
                result = configJson.getBytes(StandardCharsets.UTF_8);
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
                IotHubConnectionString.getUrlConfiguration(anyString, requestConfigId);
                new HttpRequest(mockUrl, httpMethod, (byte[]) any, anyString, (Proxy) any);
                mockHttpRequest.setReadTimeoutMillis(anyInt);
                mockHttpRequest.send();
            }
        };
        assertNotNull(responseConfig);
    }

    private void getConfigsExpectations(String connectionString, int numOfConfigs) throws Exception
    {
        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = iotHubConnectionString;
                iotHubConnectionString.getHostName();
                result = "aaa.bbb.ccc";
                IotHubConnectionString.getUrlConfigurationsList(anyString, numOfConfigs);
                result = mockUrl;
                mockHttpRequest.send();
                result = mockHttpResponse;
                IotHubExceptionManager.httpResponseVerification((HttpResponse) any);
                mockHttpResponse.getBody();
                result = configsJson.getBytes(StandardCharsets.UTF_8);
            }
        };
    }

    private void getConfigsVerifications(int numOfConfigs, List<Configuration> configs) throws Exception
    {
        new VerificationsInOrder()
        {
            {
                IotHubConnectionString.getUrlConfigurationsList(anyString, numOfConfigs);
                times = 1;
                new HttpRequest(mockUrl, HttpMethod.GET, (byte[]) any, anyString, (Proxy) any);
                times = 1;
                mockHttpRequest.setReadTimeoutMillis(anyInt);
                mockHttpRequest.setConnectTimeoutMillis(anyInt);
                mockHttpRequest.send();
            }
        };
        assertNotNull(configs);
    }

}