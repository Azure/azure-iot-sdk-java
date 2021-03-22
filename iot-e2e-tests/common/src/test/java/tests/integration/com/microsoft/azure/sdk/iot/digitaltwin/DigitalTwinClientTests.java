// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.integration.com.microsoft.azure.sdk.iot.digitaltwin;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.device.ClientOptions;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodCallback;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.TwinPropertyCallBack;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.RegistryManagerOptions;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.digitaltwin.DigitalTwinClient;
import com.microsoft.azure.sdk.iot.service.digitaltwin.DigitalTwinClientOptions;
import com.microsoft.azure.sdk.iot.service.digitaltwin.UpdateOperationUtility;
import com.microsoft.azure.sdk.iot.service.digitaltwin.customized.DigitalTwinGetHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.models.DigitalTwinCommandResponse;
import com.microsoft.azure.sdk.iot.service.digitaltwin.models.DigitalTwinInvokeCommandHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.models.DigitalTwinInvokeCommandRequestOptions;
import com.microsoft.azure.sdk.iot.service.digitaltwin.models.DigitalTwinUpdateRequestOptions;
import com.microsoft.azure.sdk.iot.service.digitaltwin.serialization.BasicDigitalTwin;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.rest.RestException;
import com.microsoft.rest.ServiceResponseWithHeaders;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import tests.integration.com.microsoft.azure.sdk.iot.digitaltwin.helpers.E2ETestConstants;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.SasTokenTools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.DigitalTwinTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT_WS;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@DigitalTwinTest
@Slf4j
@RunWith(Parameterized.class)
public class DigitalTwinClientTests extends IntegrationTest
{
    private static final String IOTHUB_CONNECTION_STRING = Tools.retrieveEnvironmentVariableValue(E2ETestConstants.IOTHUB_CONNECTION_STRING_ENV_VAR_NAME);
    private static RegistryManager registryManager;
    private String deviceId;
    private DeviceClient deviceClient;
    private DigitalTwinClient digitalTwinClient = null;
    private static final String DEVICE_ID_PREFIX = "DigitalTwinServiceClientTests_";
    protected static HttpProxyServer proxyServer;
    protected static String testProxyHostname = "127.0.0.1";
    protected static int testProxyPort = 8769;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(5 * 60); // 5 minutes max per method tested

    @Parameterized.Parameter()
    public IotHubClientProtocol protocol;

    @Parameterized.Parameters(name = "{index}: Digital Twin Test: protocol={0}")
    public static Collection<Object[]> data() {
        return (List) new ArrayList(Arrays.asList(new Object[][]{
                {MQTT},
                {MQTT_WS},
        }));
    }

    @BeforeClass
    public static void setUpBeforeClass() {
        registryManager =
            new RegistryManager(
                IOTHUB_CONNECTION_STRING,
                RegistryManagerOptions.builder()
                    .httpReadTimeout(0)
                    .build());
    }

    @Before
    public void setUp() throws URISyntaxException, IOException, IotHubException {
        this.deviceClient = createDeviceClient(protocol);
        deviceClient.open();
        digitalTwinClient =
            new DigitalTwinClient(
                IOTHUB_CONNECTION_STRING,
                DigitalTwinClientOptions.builder()
                    .httpReadTimeout(0)
                    .build());
    }

    @After
    public void cleanUp() {
        try {
            deviceClient.closeNow();
            registryManager.removeDevice(deviceId);
        } catch (Exception ex) {
            log.error("An exception occurred while closing/ deleting the device {}: {}", deviceId, ex);
        }
    }

    private DeviceClient createDeviceClient(IotHubClientProtocol protocol) throws IOException, IotHubException, URISyntaxException {
        ClientOptions options = new ClientOptions();
        options.setModelId(E2ETestConstants.THERMOSTAT_MODEL_ID);

        this.deviceId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        Device device = Device.createDevice(deviceId, AuthenticationType.SAS);
        Device registeredDevice = registryManager.addDevice(device);
        String deviceConnectionString = registryManager.getDeviceConnectionString(registeredDevice);
        return new DeviceClient(deviceConnectionString, protocol, options);
    }

    @AfterClass
    public static void cleanUpAfterClass()
    {
        registryManager.close();
    }

    @BeforeClass
    public static void startProxy()
    {
        proxyServer = DefaultHttpProxyServer.bootstrap()
            .withPort(testProxyPort)
            .start();
    }

    @AfterClass
    public static void stopProxy()
    {
        proxyServer.stop();
    }

    @Test
    @StandardTierHubOnlyTest
    public void getDigitalTwin() {
        // act
        BasicDigitalTwin response = digitalTwinClient.getDigitalTwin(deviceId, BasicDigitalTwin.class);
        ServiceResponseWithHeaders<BasicDigitalTwin, DigitalTwinGetHeaders> responseWithHeaders =
            digitalTwinClient.getDigitalTwinWithResponse(deviceId, BasicDigitalTwin.class);

        // assert
        assertEquals(response.getMetadata().getModelId(), E2ETestConstants.THERMOSTAT_MODEL_ID);
        assertEquals(responseWithHeaders.body().getMetadata().getModelId(), E2ETestConstants.THERMOSTAT_MODEL_ID);
    }

    @Test
    @StandardTierHubOnlyTest
    public void getDigitalTwinWithProxy() {
        // arrange
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(testProxyHostname, testProxyPort));
        ProxyOptions proxyOptions = new ProxyOptions(proxy);
        DigitalTwinClientOptions clientOptions =
            DigitalTwinClientOptions.builder()
                .proxyOptions(proxyOptions)
                .httpReadTimeout(0)
                .build();

        digitalTwinClient = new DigitalTwinClient(IOTHUB_CONNECTION_STRING, clientOptions);

        // act
        BasicDigitalTwin response = digitalTwinClient.getDigitalTwin(deviceId, BasicDigitalTwin.class);
        ServiceResponseWithHeaders<BasicDigitalTwin, DigitalTwinGetHeaders> responseWithHeaders =
            digitalTwinClient.getDigitalTwinWithResponse(deviceId, BasicDigitalTwin.class);

        // assert
        assertEquals(response.getMetadata().getModelId(), E2ETestConstants.THERMOSTAT_MODEL_ID);
        assertEquals(responseWithHeaders.body().getMetadata().getModelId(), E2ETestConstants.THERMOSTAT_MODEL_ID);
    }

    @Test(expected = IllegalArgumentException.class)
    @StandardTierHubOnlyTest
    public void digitalTwinConstructorThrowsForNegativeConnectTimeout() {
        // arrange
        DigitalTwinClientOptions clientOptions =
            DigitalTwinClientOptions.builder()
                .httpConnectTimeout(-1)
                .build();

        digitalTwinClient = new DigitalTwinClient(IOTHUB_CONNECTION_STRING, clientOptions);
    }

    @Test(expected = IllegalArgumentException.class)
    @StandardTierHubOnlyTest
    public void digitalTwinConstructorThrowsForNegativeReadTimeout() {
        // arrange
        DigitalTwinClientOptions clientOptions =
            DigitalTwinClientOptions.builder()
                .httpReadTimeout(-1)
                .build();

        digitalTwinClient = new DigitalTwinClient(IOTHUB_CONNECTION_STRING, clientOptions);
    }

    @Test
    @StandardTierHubOnlyTest
    public void getDigitalTwinWithAzureSasCredential() {
        if (protocol != MQTT)
        {
            // This test is for the service client, so no need to rerun it for all the different device protocols
            return;
        }

        // arrange
        digitalTwinClient = buildDigitalTwinClientWithAzureSasCredential();

        // act
        BasicDigitalTwin response = digitalTwinClient.getDigitalTwin(deviceId, BasicDigitalTwin.class);
        ServiceResponseWithHeaders<BasicDigitalTwin, DigitalTwinGetHeaders> responseWithHeaders =
            digitalTwinClient.getDigitalTwinWithResponse(deviceId, BasicDigitalTwin.class);

        // assert
        assertEquals(response.getMetadata().getModelId(), E2ETestConstants.THERMOSTAT_MODEL_ID);
        assertEquals(responseWithHeaders.body().getMetadata().getModelId(), E2ETestConstants.THERMOSTAT_MODEL_ID);
    }

    @Test
    public void digitalTwinClientTokenRenewalWithAzureSasCredential()
    {
        if (protocol != MQTT)
        {
            // This test is for the service client, so no need to rerun it for all the different device protocols
            return;
        }

        IotHubConnectionString iotHubConnectionStringObj =
            IotHubConnectionStringBuilder.createIotHubConnectionString(IOTHUB_CONNECTION_STRING);

        IotHubServiceSasToken serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);
        AzureSasCredential sasCredential = new AzureSasCredential(serviceSasToken.toString());
        digitalTwinClient = new DigitalTwinClient(iotHubConnectionStringObj.getHostName(), sasCredential);

        // get a digital twin with a valid SAS token in the AzureSasCredential instance
        // don't care about the return value, just checking that the request isn't unauthorized
        digitalTwinClient.getDigitalTwin(deviceId, BasicDigitalTwin.class);

        // deliberately expire the SAS token to provoke a 401 to ensure that the digital twin client is using the shared
        // access signature that is set here.
        sasCredential.update(SasTokenTools.makeSasTokenExpired(serviceSasToken.toString()));

        try
        {
            digitalTwinClient.getDigitalTwin(deviceId, BasicDigitalTwin.class);
            fail("Expected get digital twin call to throw unauthorized exception since an expired SAS token was used, but no exception was thrown");
        }
        catch (RestException e)
        {
            if (e.response().code() == 401)
            {
                log.debug("IotHubUnauthorizedException was thrown as expected, continuing test");
            }
            else
            {
                throw e;
            }
        }

        // Renew the expired shared access signature
        serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);
        sasCredential.update(serviceSasToken.toString());

        // get a digital twin using the renewed shared access signature
        // don't care about the return value, just checking that the request isn't unauthorized
        digitalTwinClient.getDigitalTwin(deviceId, BasicDigitalTwin.class);
    }

    @Test
    @StandardTierHubOnlyTest
    public void updateDigitalTwin() throws IOException {
        // arrange
        String newProperty = "currentTemperature";
        String newPropertyPath = "/currentTemperature";
        Integer newPropertyValue = 35;

        // Property update callback
        TwinPropertyCallBack twinPropertyCallBack = (property, context) -> {
            Set<Property> properties = new HashSet<>();
            properties.add(property);
            try {
                deviceClient.sendReportedProperties(properties);
            } catch (IOException e) {
            }
        };

        // IotHub event callback
        IotHubEventCallback iotHubEventCallback = (responseStatus, callbackContext) -> {};

        // start device twin and setup handler for property updates in device
        deviceClient.startDeviceTwin(iotHubEventCallback, null, twinPropertyCallBack, null);
        Map<Property, Pair<TwinPropertyCallBack, Object>> desiredPropertyUpdateCallback =
                Collections.singletonMap(
                        new Property(newProperty, null),
                        new Pair<>(twinPropertyCallBack, null));
        deviceClient.subscribeToTwinDesiredProperties(desiredPropertyUpdateCallback);

        DigitalTwinUpdateRequestOptions optionsWithoutEtag = new DigitalTwinUpdateRequestOptions();
        optionsWithoutEtag.setIfMatch("*");

        // get digital twin and Etag before update
        ServiceResponseWithHeaders<BasicDigitalTwin, DigitalTwinGetHeaders> responseWithHeaders =
            digitalTwinClient.getDigitalTwinWithResponse(deviceId, BasicDigitalTwin.class);
        DigitalTwinUpdateRequestOptions optionsWithEtag = new DigitalTwinUpdateRequestOptions();
        optionsWithEtag.setIfMatch(responseWithHeaders.headers().eTag());

        // act
        // Add properties at root level - conditional update with max overload
        UpdateOperationUtility updateOperationUtility = new UpdateOperationUtility().appendAddPropertyOperation(newPropertyPath, newPropertyValue);
        digitalTwinClient.updateDigitalTwinWithResponse(deviceId, updateOperationUtility.getUpdateOperations(), optionsWithEtag);
        BasicDigitalTwin digitalTwin = digitalTwinClient.getDigitalTwinWithResponse(deviceId, BasicDigitalTwin.class).body();

        // assert
        assertEquals(E2ETestConstants.THERMOSTAT_MODEL_ID, digitalTwin.getMetadata().getModelId());
        assertTrue(digitalTwin.getMetadata().getWriteableProperties().containsKey(newProperty));
        assertEquals(newPropertyValue, digitalTwin.getMetadata().getWriteableProperties().get(newProperty).getDesiredValue());
    }

    @Test
    @StandardTierHubOnlyTest
    public void invokeRootLevelCommand() throws IOException {
        // arrange
        String commandName = "getMaxMinReport";
        String commandInput = "\"" +ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(5).format(DateTimeFormatter.ISO_DATE_TIME) + "\"";
        String jsonStringInput = "{\"prop\":\"value\"}";
        DigitalTwinInvokeCommandRequestOptions options = new DigitalTwinInvokeCommandRequestOptions();
        options.setConnectTimeoutInSeconds(15);
        options.setResponseTimeoutInSeconds(15);

        // setup device callback
        Integer deviceSuccessResponseStatus = 200;
        Integer deviceFailureResponseStatus = 500;

        // Device method callback
        DeviceMethodCallback deviceMethodCallback = (methodName, methodData, context) -> {
            String jsonRequest = new String((byte[]) methodData, StandardCharsets.UTF_8);
            if(methodName.equalsIgnoreCase(commandName)) {
                return new DeviceMethodData(deviceSuccessResponseStatus, jsonRequest);
            }
            else {
                return new DeviceMethodData(deviceFailureResponseStatus, jsonRequest);
            }
        };

        // IotHub event callback
        IotHubEventCallback iotHubEventCallback = (responseStatus, callbackContext) -> {};

        deviceClient.subscribeToDeviceMethod(deviceMethodCallback, commandName, iotHubEventCallback, commandName);

        // act
        DigitalTwinCommandResponse responseWithNoPayload = this.digitalTwinClient.invokeCommand(deviceId, commandName, null);
        DigitalTwinCommandResponse responseWithJsonStringPayload = this.digitalTwinClient.invokeCommand(deviceId, commandName, jsonStringInput);
        DigitalTwinCommandResponse responseWithDatePayload = this.digitalTwinClient.invokeCommand(deviceId, commandName, commandInput);
        ServiceResponseWithHeaders<DigitalTwinCommandResponse, DigitalTwinInvokeCommandHeaders> datePayloadResponseWithHeaders = this.digitalTwinClient.invokeCommandWithResponse(deviceId, commandName, commandInput, options);

        // assert
        assertEquals(deviceSuccessResponseStatus, responseWithNoPayload.getStatus());
        assertEquals("\"\"", responseWithNoPayload.getPayload());
        assertEquals(deviceSuccessResponseStatus, responseWithJsonStringPayload.getStatus());
        assertEquals(jsonStringInput, responseWithJsonStringPayload.getPayload());
        assertEquals(deviceSuccessResponseStatus, responseWithDatePayload.getStatus());
        assertEquals(commandInput, responseWithDatePayload.getPayload());
        assertEquals(deviceSuccessResponseStatus, datePayloadResponseWithHeaders.body().getStatus());
        assertEquals(commandInput, datePayloadResponseWithHeaders.body().getPayload());
    }

    private static DigitalTwinClient buildDigitalTwinClientWithAzureSasCredential()
    {
        IotHubConnectionString iotHubConnectionStringObj = IotHubConnectionStringBuilder.createIotHubConnectionString(IOTHUB_CONNECTION_STRING);
        IotHubServiceSasToken serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);
        AzureSasCredential azureSasCredential = new AzureSasCredential(serviceSasToken.toString());
        DigitalTwinClientOptions options = DigitalTwinClientOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build();
        return new DigitalTwinClient(iotHubConnectionStringObj.getHostName(), azureSasCredential, options);
    }
}
