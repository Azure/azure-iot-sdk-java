/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.registry;

import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubExceptionManager;
import com.microsoft.azure.sdk.iot.service.registry.serializers.RegistryIdentityParser;
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

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;

/**
 * Code Coverage
 * Methods: 100%
 * Lines: 96%
 */
@RunWith(JMockit.class)
public class RegistryClientTest
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
    Module module;

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
        RegistryClientOptions options = RegistryClientOptions.builder().build();
        assertEquals((int) Deencapsulation.getField(RegistryClientOptions.class, "DEFAULT_HTTP_READ_TIMEOUT_SECONDS"), options.getHttpReadTimeoutSeconds());
        assertEquals((int) Deencapsulation.getField(RegistryClientOptions.class, "DEFAULT_HTTP_CONNECT_TIMEOUT_SECONDS"), options.getHttpConnectTimeoutSeconds());
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_001: [The constructor shall throw IllegalArgumentException if the input string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructor_input_null() throws Exception
    {
        String connectionString = null;

        new RegistryClient(connectionString);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_001: [The constructor shall throw IllegalArgumentException if the input string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructor_input_empty() throws Exception
    {
        String connectionString = null;

        new RegistryClient(connectionString);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_002: [The constructor shall create an IotHubConnectionString object from the given connection string]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_003: [The constructor shall create a new RegistryClient, stores the created IotHubConnectionString object and return with it]
    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_34_090: [The function shall start this object's executor service]
    @Test
    public void constructor_good_case() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryClient registryClient = new RegistryClient(connectionString);

        assertNotNull(registryClient);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_004: [The constructor shall throw IllegalArgumentException if the input device is null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void addDevice_input_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryClient registryClient = new RegistryClient(connectionString);

        registryClient.addDevice(null);
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

        RegistryClient registryClient = new RegistryClient(connectionString);
        Device returnDevice = registryClient.addDevice(device);

        commonVerifications(HttpMethod.PUT, deviceId, returnDevice);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_014: [The constructor shall throw IllegalArgumentException if the input string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getDevice_input_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryClient registryClient = new RegistryClient(connectionString);

        registryClient.getDevice(null);
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

        RegistryClient registryClient = new RegistryClient(connectionString);
        Device returnDevice = registryClient.getDevice(deviceId);

        commonVerifications(HttpMethod.GET, deviceId, returnDevice);
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

        RegistryClient registryClient = new RegistryClient(connectionString);

        registryClient.updateDevice(null);
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

        RegistryClient registryClient = new RegistryClient(connectionString);
        Device returnDevice = registryClient.updateDevice(device);
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
        RegistryClient registryClient = new RegistryClient(connectionString);

        Device device = null;
        registryClient.removeDevice(device);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_087: [The function shall throw IllegalArgumentException if the input etag is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeDevice_input_Device_null_etag() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryClient registryClient = new RegistryClient(connectionString);

        new NonStrictExpectations()
        {
            {
                device.getDeviceId();
                result = "somedevice";
                device.getETag();
                result = null;
            }
        };

        registryClient.removeDevice(device);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_087: [The function shall throw IllegalArgumentException if the input etag is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeDevice_input_Device_empty_etag() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryClient registryClient = new RegistryClient(connectionString);

        new NonStrictExpectations()
        {
            {
                device.getDeviceId();
                result = "somedevice";
                device.getETag();
                result = "";
            }
        };

        registryClient.removeDevice(device);
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
                device.getETag();
                result = etag;
            }
        };

        RegistryClient registryClient = new RegistryClient(connectionString);
        registryClient.removeDevice(device);

        new VerificationsInOrder()
        {
            {
                IotHubConnectionString.getUrlDevice(anyString, deviceId);
                times = 1;
                new HttpRequest(mockUrl, HttpMethod.DELETE, new byte[0], anyString, (Proxy) any);
                times = 1;
                mockHttpRequest.setReadTimeoutSeconds(anyInt);
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
        RegistryClient registryClient = new RegistryClient(connectionString);

        String deviceId = null;
        registryClient.removeDevice(deviceId);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_12_046: [The function shall throw IllegalArgumentException if the input deviceId is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeDevice_input_empty() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryClient registryClient = new RegistryClient(connectionString);

        registryClient.removeDevice("");
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

        RegistryClient registryClient = new RegistryClient(connectionString);
        registryClient.removeDevice(deviceId);

        new VerificationsInOrder()
        {
            {
                IotHubConnectionString.getUrlDevice(anyString, deviceId);
                times = 1;
                new HttpRequest(mockUrl, HttpMethod.DELETE, new byte[0], anyString, (Proxy) any);
                times = 1;
                mockHttpRequest.setReadTimeoutSeconds(anyInt);
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
    public void getStatistics_good_case(@Mocked Proxy mockProxy, @Mocked ProxyOptions mockProxyOptions, @Mocked RegistryClientOptions registryClientOptions) throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        String deviceId = "somedevice";

        commonExpectations(connectionString, deviceId);

        RegistryClient registryClient = new RegistryClient(connectionString, registryClientOptions);

        new Expectations()
        {
            {
                IotHubConnectionString.getUrlDeviceStatistics(anyString);
                registryClientOptions.getProxyOptions();
                result = mockProxyOptions;
                mockProxyOptions.getProxy();
                result = mockProxy;
                new HttpRequest(mockUrl, HttpMethod.GET, new byte[0], anyString, mockProxy);
                mockHttpRequest.setReadTimeoutSeconds(anyInt);
                mockHttpRequest.send();
                IotHubExceptionManager.httpResponseVerification((HttpResponse) any);
            }
        };

        // act
        RegistryStatistics statistics = registryClient.getStatistics();

        assertNotNull(statistics);
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

        RegistryClient registryClient = new RegistryClient(connectionString);

        registryClient.addModule(null);
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

        RegistryClient registryClient = new RegistryClient(connectionString);
        Module returnModule = registryClient.addModule(module);

        commonModuleVerifications(HttpMethod.PUT, deviceId, moduleId, returnModule);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_009: [The constructor shall throw IllegalArgumentException if the deviceId string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getModule_deviceId_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryClient registryClient = new RegistryClient(connectionString);

        registryClient.getModule(null, "somemodule");
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_009: [The constructor shall throw IllegalArgumentException if the deviceId string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getModule_deviceId_empty() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryClient registryClient = new RegistryClient(connectionString);

        registryClient.getModule("", "somemodule");
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_010: [The constructor shall throw IllegalArgumentException if the moduleId string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getModule_moduleId_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryClient registryClient = new RegistryClient(connectionString);

        registryClient.getModule("somedevice", null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_010: [The constructor shall throw IllegalArgumentException if the moduleId string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getModule_moduleId_empty() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryClient registryClient = new RegistryClient(connectionString);

        registryClient.getModule("somedevice","");
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

        RegistryClient registryClient = new RegistryClient(connectionString);
        Module returnModule = registryClient.getModule(deviceId, moduleId);

        commonModuleVerifications(HttpMethod.GET, deviceId, moduleId, returnModule);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_017: [The constructor shall throw IllegalArgumentException if the input string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getModulesOnDevice_deviceId_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryClient registryClient = new RegistryClient(connectionString);

        registryClient.getModulesOnDevice(null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_017: [The constructor shall throw IllegalArgumentException if the input string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getModulesOnDevice_deviceId_empty() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryClient registryClient = new RegistryClient(connectionString);

        registryClient.getModulesOnDevice("");
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

        RegistryClient registryClient = new RegistryClient(connectionString);
        List<Module> modules =  registryClient.getModulesOnDevice(deviceId);

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
        RegistryClient registryClient = new RegistryClient(connectionString);

        registryClient.updateModule(null);
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

        RegistryClient registryClient = new RegistryClient(connectionString);
        Module returnModule = registryClient.updateModule(module);
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
        RegistryClient registryClient = new RegistryClient(connectionString);

        Module module = null;
        registryClient.removeModule(module);
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
                module.getETag();
                result = etag;
            }
        };

        RegistryClient registryClient = new RegistryClient(connectionString);
        registryClient.removeModule(module);

        new VerificationsInOrder()
        {
            {
                IotHubConnectionString.getUrlModule(anyString, deviceId, moduleId);
                times = 1;
                new HttpRequest(mockUrl, HttpMethod.DELETE, new byte[0], anyString, (Proxy) any);
                times = 1;
                mockHttpRequest.setReadTimeoutSeconds(anyInt);
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
        RegistryClient registryClient = new RegistryClient(connectionString);

        registryClient.removeModule(null, "somemodule");
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_034: [The function shall throw IllegalArgumentException if the deviceId is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeModule_deviceId_empty() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryClient registryClient = new RegistryClient(connectionString);

        registryClient.removeModule("", "somemodule");
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_035: [The function shall throw IllegalArgumentException if the moduleId is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeModule_moduleId_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryClient registryClient = new RegistryClient(connectionString);

        registryClient.removeModule("somedevice", null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_035: [The function shall throw IllegalArgumentException if the moduleId is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeModule_moduleId_empty() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryClient registryClient = new RegistryClient(connectionString);

        registryClient.removeModule("somedevice", "");
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_078: [The function shall throw IllegalArgumentException if the etag is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeModule_Module_etag_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryClient registryClient = new RegistryClient(connectionString);

        new NonStrictExpectations()
        {
            {
                module.getDeviceId();
                result = "somedevice";
                module.getId();
                result = "somemodule";
                module.getETag();
                result = null;
            }
        };

        registryClient.removeModule(module);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_078: [The function shall throw IllegalArgumentException if the etag is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeModule_Module_etag_empty() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        RegistryClient registryClient = new RegistryClient(connectionString);

        new NonStrictExpectations()
        {
            {
                module.getDeviceId();
                result = "somedevice";
                module.getId();
                result = "somemodule";
                module.getETag();
                result = "";
            }
        };

        registryClient.removeModule(module);
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

        RegistryClient registryClient = new RegistryClient(connectionString);
        registryClient.removeModule(deviceId, moduleId);

        new VerificationsInOrder()
        {
            {
                IotHubConnectionString.getUrlModule(anyString, deviceId, moduleId);
                times = 1;
                new HttpRequest(mockUrl, HttpMethod.DELETE, new byte[0], anyString, (Proxy) any);
                times = 1;
                mockHttpRequest.setReadTimeoutSeconds(anyInt);
                mockHttpRequest.setHeaderField("If-Match", "*");
            }
        };
    }

    @Test
    public void getDevicesWithCustomHttpTimeouts(@Mocked final IotHubConnectionString mockIotHubConnectionString,
                                                 @Mocked final RegistryIdentityParser mockRegistryIdentityParser,
                                                 @Mocked final Device mockDevice,
                                                 @Mocked final RegistryClientOptions mockOptions)
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
                new RegistryIdentityParser(anyString);
                result = mockRegistryIdentityParser;
                Deencapsulation.newInstance(Device.class, mockRegistryIdentityParser);
                result = mockDevice;
                mockOptions.getHttpConnectTimeoutSeconds();
                result = expectedHttpConnectTimeout;
                mockOptions.getHttpReadTimeoutSeconds();
                result = expectedHttpReadTimeout;
            }
        };

        RegistryClient registryClient = new RegistryClient(mockConnectionString, mockOptions);

        // act
        registryClient.getDevice(mockDeviceId);

        // assert
        new Verifications()
        {
            {
                mockHttpRequest.setConnectTimeoutSeconds(expectedHttpConnectTimeout);
                mockHttpRequest.setReadTimeoutSeconds(expectedHttpReadTimeout);
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
                Deencapsulation.invoke(device, "toRegistryIdentityParser");
                result = new RegistryIdentityParser();
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
                mockHttpRequest.setReadTimeoutSeconds(anyInt);
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
                mockHttpRequest.setReadTimeoutSeconds(anyInt);
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
                Deencapsulation.invoke(module, "toRegistryIdentityParser");
                result = new RegistryIdentityParser();
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
                mockHttpRequest.setReadTimeoutSeconds(anyInt);
                mockHttpRequest.setConnectTimeoutSeconds(anyInt);
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
                mockHttpRequest.setReadTimeoutSeconds(anyInt);
                mockHttpRequest.send();
            }
        };
        assertNotNull(modules);
    }
}