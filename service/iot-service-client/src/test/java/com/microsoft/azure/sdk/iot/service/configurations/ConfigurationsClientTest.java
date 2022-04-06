// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.configurations;

import com.microsoft.azure.sdk.iot.service.configurations.serializers.ConfigurationContentParser;
import com.microsoft.azure.sdk.iot.service.configurations.serializers.ConfigurationParser;
import com.microsoft.azure.sdk.iot.service.registry.Device;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import mockit.VerificationsInOrder;
import org.junit.Test;

import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

public class ConfigurationsClientTest
{
    @Mocked
    HttpResponse mockHttpResponse;

    @Mocked
    IotHubConnectionString iotHubConnectionString;

    @Mocked
    HttpRequest mockHttpRequest;

    @Mocked
    Configuration config;

    @Mocked
    ConfigurationContent mockedConfigurationContent;

    @Mocked
    ConfigurationContentParser mockedConfigurationContentParser;

    @Mocked
    URL mockUrl;

    @Mocked
    Device device;

    @Mocked
    IotHubServiceSasToken iotHubServiceSasToken;

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
                mockHttpRequest.setReadTimeoutSeconds(anyInt);
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
                mockHttpRequest.setReadTimeoutSeconds(anyInt);
                mockHttpRequest.setConnectTimeoutSeconds(anyInt);
                mockHttpRequest.send();
            }
        };
        assertNotNull(configs);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_041: [The constructor shall throw IllegalArgumentException if the input configuration is null]
    // Assert
    @Test(expected = IllegalArgumentException.class)
    public void addConfiguration_input_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        ConfigurationsClient configurationsClient = new ConfigurationsClient(connectionString);

        configurationsClient.create(null);
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

        ConfigurationsClient configurationsClient = new ConfigurationsClient(connectionString);
        Configuration returnConfig = configurationsClient.create(config);

        commonConfigVerifications(HttpMethod.PUT, configId, returnConfig);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_049: [The constructor shall throw IllegalArgumentException if the configurationId string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getConfiguration_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        ConfigurationsClient configurationsClient = new ConfigurationsClient(connectionString);

        configurationsClient.get(null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_049: [The constructor shall throw IllegalArgumentException if the configurationId string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getConfiguration_empty() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        ConfigurationsClient configurationsClient = new ConfigurationsClient(connectionString);

        configurationsClient.get("");
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

        ConfigurationsClient configurationsClient = new ConfigurationsClient(connectionString);
        Configuration returnConfiguration = configurationsClient.get(configurationId);

        commonConfigVerifications(HttpMethod.GET, configurationId, returnConfiguration);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_056: [The function shall throw IllegalArgumentException if the input count number is less than 1]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getConfigurations_input_zero() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        ConfigurationsClient configurationsClient = new ConfigurationsClient(connectionString);

        configurationsClient.get(0);
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

        ConfigurationsClient configurationsClient = new ConfigurationsClient(connectionString);
        List<Configuration> configs =  configurationsClient.get(10);

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
        ConfigurationsClient configurationsClient = new ConfigurationsClient(connectionString);

        configurationsClient.replace(null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_064: [The function shall call replace with forceUpdate = false]
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

        ConfigurationsClient configurationsClient = new ConfigurationsClient(connectionString);
        Configuration returnConfig = configurationsClient.replace(config);
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
        ConfigurationsClient configurationsClient = new ConfigurationsClient(connectionString);

        Configuration config = null;
        configurationsClient.delete(config);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_087: [The function shall throw IllegalArgumentException if the input etag is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeDevice_input_Configuration_null_etag() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        ConfigurationsClient configurationsClient = new ConfigurationsClient(connectionString);

        new NonStrictExpectations()
        {
            {
                config.getId();
                result = "someconfig";
                device.getETag();
                result = null;
            }
        };

        configurationsClient.delete(config);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_087: [The function shall throw IllegalArgumentException if the input etag is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeDevice_input_Configuration_empty_etag() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        ConfigurationsClient configurationsClient = new ConfigurationsClient(connectionString);

        new NonStrictExpectations()
        {
            {
                config.getId();
                result = "someconfig";
                config.getEtag();
                result = "";
            }
        };

        configurationsClient.delete(config);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_073: [The function shall throw IllegalArgumentException if the input string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeConfiguration_input_null() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        ConfigurationsClient configurationsClient = new ConfigurationsClient(connectionString);

        configurationsClient.delete((String)null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_REGISTRYMANAGER_28_073: [The function shall throw IllegalArgumentException if the input string is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void removeConfiguration_input_empty() throws Exception
    {
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        constructorExpectations(connectionString);
        ConfigurationsClient configurationsClient = new ConfigurationsClient(connectionString);

        configurationsClient.delete("");
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

        new NonStrictExpectations()
        {
            {
                config.getId();
                result = configId;
                config.getEtag();
                result = etag;
            }
        };

        constructorExpectations(connectionString);

        ConfigurationsClient configurationsClient = new ConfigurationsClient(connectionString);
        configurationsClient.delete(config);

        new VerificationsInOrder()
        {
            {
                IotHubConnectionString.getUrlConfiguration(anyString, configId);
                times = 1;
                new HttpRequest(mockUrl, HttpMethod.DELETE, new byte[0], anyString, (Proxy) any);
                times = 1;
                mockHttpRequest.setReadTimeoutSeconds(anyInt);
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

        constructorExpectations(connectionString);
        ConfigurationsClient configurationsClient = new ConfigurationsClient(connectionString);
        configurationsClient.delete(configId);

        new VerificationsInOrder()
        {
            {
                IotHubConnectionString.getUrlConfiguration(anyString, configId);
                times = 1;
                new HttpRequest(mockUrl, HttpMethod.DELETE, new byte[0], anyString, (Proxy) any);
                times = 1;
                mockHttpRequest.setReadTimeoutSeconds(anyInt);
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

        constructorExpectations(connectionString);
        ConfigurationsClient configurationsClient = new ConfigurationsClient(connectionString);

        //act
        configurationsClient.applyConfigurationContentOnDevice("some device", null);
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

        constructorExpectations(connectionString);
        ConfigurationsClient configurationsClient = new ConfigurationsClient(connectionString);

        new NonStrictExpectations()
        {
            {
                mockedConfigurationContent.toConfigurationContentParser();
                result = mockedConfigurationContentParser;

                mockedConfigurationContentParser.toJsonElement().toString();
                result = expectedJson;
            }
        };

        //act
        configurationsClient.applyConfigurationContentOnDevice(expectedDeviceId, mockedConfigurationContent);

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
            }
        };
    }
}
