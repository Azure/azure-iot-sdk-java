/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.integration.com.microsoft.azure.sdk.iot.iothub.setup;


import com.azure.core.credential.AzureSasCredential;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.twin.DirectMethodResponse;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClient;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClientOptions;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.methods.DirectMethodRequestOptions;
import com.microsoft.azure.sdk.iot.service.methods.DirectMethodsClient;
import com.microsoft.azure.sdk.iot.service.methods.DirectMethodsClientOptions;
import com.microsoft.azure.sdk.iot.service.methods.MethodResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.ClientType;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestConstants;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestIdentity;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestModuleIdentity;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Utility functions, setup and teardown for all device method integration tests. This class should not contain any tests,
 * but any children class should.
 */
@Slf4j
public class DirectMethodsCommon extends IntegrationTest
{
    public static final String METHOD_RESET = "reset";
    public static final String METHOD_LOOPBACK = "loopback";
    public static final String METHOD_DELAY_IN_MILLISECONDS = "delayInMilliseconds";
    public static final String METHOD_ECHO = "echo";
    public static final String METHOD_UNKNOWN = "unknown";

    public static final int METHOD_SUCCESS = 200;
    public static final int METHOD_THROWS = 403;
    public static final int METHOD_NOT_DEFINED = 404;

    @Parameterized.Parameters(name = "{0}_{1}_{2}")
    public static Collection inputs()
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));
        return inputsCommon();
    }

    protected static String iotHubConnectionString = "";

    protected static final int RESPONSE_TIMEOUT = 200;
    protected static final int CONNECTION_TIMEOUT = 5;
    protected static final String PAYLOAD_STRING = "This is a valid payload";

    protected DirectMethodTestInstance testInstance;
    protected Object methodDataObject;

    protected static Collection inputsCommon()
    {
        Collection<Object[]> inputs = new ArrayList<>();

        for (ClientType clientType : ClientType.values())
        {
            for (IotHubClientProtocol protocol : IotHubClientProtocol.values())
            {
                if (protocol != HTTPS)
                {
                    for (AuthenticationType authenticationType : AuthenticationType.values())
                    {
                        if (authenticationType == SAS)
                        {
                            inputs.add(makeSubArray(protocol, authenticationType, clientType));
                        }
                        else if (authenticationType == SELF_SIGNED)
                        {
                            if (protocol != AMQPS_WS && protocol != MQTT_WS)
                            {
                                inputs.add(makeSubArray(protocol, authenticationType, clientType));
                            }
                        }
                    }
                }
            }
        }

        return inputs;
    }

    private static Object[] makeSubArray(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType)
    {
        Object[] inputSubArray = new Object[3];
        inputSubArray[0] = protocol;
        inputSubArray[1] = authenticationType;
        inputSubArray[2] = clientType;
        return inputSubArray;
    }

    protected DirectMethodsCommon(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws Exception
    {
        this.testInstance = new DirectMethodTestInstance(protocol, authenticationType, clientType);
    }

    public static class DirectMethodTestInstance
    {
        public IotHubClientProtocol protocol;
        public AuthenticationType authenticationType;
        public ClientType clientType;
        public TestIdentity identity;
        public String publicKeyCert;
        public String privateKey;
        public String x509Thumbprint;
        public DirectMethodsClient methodServiceClient;
        public RegistryClient registryClient;

        protected DirectMethodTestInstance(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws Exception
        {
            this.protocol = protocol;
            this.authenticationType = authenticationType;
            this.clientType = clientType;
            this.publicKeyCert = x509CertificateGenerator.getPublicCertificatePEM();
            this.privateKey = x509CertificateGenerator.getPrivateKeyPEM();
            this.x509Thumbprint = x509CertificateGenerator.getX509Thumbprint();
            this.methodServiceClient = new DirectMethodsClient(iotHubConnectionString, DirectMethodsClientOptions.builder().httpReadTimeoutSeconds(HTTP_READ_TIMEOUT).build());
            this.registryClient = new RegistryClient(iotHubConnectionString, RegistryClientOptions.builder().httpReadTimeoutSeconds(HTTP_READ_TIMEOUT).build());
        }

        public void setup() throws Exception {

            if (clientType == ClientType.DEVICE_CLIENT)
            {
                this.identity = Tools.getTestDevice(iotHubConnectionString, this.protocol, this.authenticationType, false);
            }
            else if (clientType == ClientType.MODULE_CLIENT)
            {
                this.identity = Tools.getTestModule(iotHubConnectionString, this.protocol, this.authenticationType, false);
            }
        }

        public void dispose()
        {
            if (this.identity != null && this.identity.getClient() != null)
            {
                this.identity.getClient().close();
            }

            Tools.disposeTestIdentity(this.identity, iotHubConnectionString);
        }
    }

    private String loopback(Object methodData)
    {
        String payload = new String(methodData.toString().getBytes(StandardCharsets.UTF_8)).replace("\"", "");
        return METHOD_LOOPBACK + ":" + payload;
    }

    private String delayInMilliseconds(Object methodData) throws InterruptedException
    {
        String payload = new String(methodData.toString().getBytes(StandardCharsets.UTF_8)).replace("\"", "");
        long delay = Long.parseLong(payload);
        Thread.sleep(delay);
        return METHOD_DELAY_IN_MILLISECONDS + ":succeed";
    }

    private Object echo(Object methodData)
    {
        return methodData;
    }

    public void openDeviceClientAndSubscribeToMethods() throws Exception
    {
        testInstance.setup();
        this.testInstance.identity.getClient().open(true);

        this.testInstance.identity.getClient().subscribeToMethods(
            (methodName, methodData, context) ->
            {
                System.out.println("Device invoked " + methodName);
                DirectMethodResponse deviceMethodData;
                int status;
                String result;
                try
                {
                    switch (methodName)
                    {
                        case METHOD_RESET:
                            result = METHOD_RESET + ":succeed";
                            status = METHOD_SUCCESS;
                            this.testInstance.identity.getClient().close();
                            break;
                        case METHOD_LOOPBACK:
                            result = loopback(methodData);
                            status = METHOD_SUCCESS;
                            break;
                        case METHOD_DELAY_IN_MILLISECONDS:
                            result = delayInMilliseconds(methodData);
                            status = METHOD_SUCCESS;
                            break;
                        case METHOD_ECHO:
                            result = (String) echo(methodData);
                            status = METHOD_SUCCESS;
                            break;
                        default:
                            result = "unknown:" + methodName;
                            status = METHOD_NOT_DEFINED;
                            break;
                    }
                }
                catch (Exception e)
                {
                    result = e.toString();
                    status = METHOD_THROWS;
                }
                deviceMethodData = new DirectMethodResponse(status, new JsonPrimitive(result));

                return deviceMethodData;
            },
            null);
    }

    public void openDeviceClientAndMakeEchoCall() throws Exception
    {
        testInstance.setup();
        this.testInstance.identity.getClient().open(true);

        this.testInstance.identity.getClient().subscribeToMethods(
                (methodName, methodData, context) ->
                {
                    System.out.println("Device invoked " + methodName);
                    DirectMethodResponse deviceMethodData;
                    int status;
                    Object result;
                    try
                    {
                        switch (methodName)
                        {
                            case METHOD_ECHO:
                                result = echo(methodData);
                                status = METHOD_SUCCESS;
                                break;
                            default:
                                result = "unknown:" + methodName;
                                status = METHOD_NOT_DEFINED;
                                break;
                        }
                    }
                    catch (Exception e)
                    {
                        result = e.toString();
                        status = METHOD_THROWS;
                    }

                    methodDataObject = methodData;
                    deviceMethodData = new DirectMethodResponse(status, result);

                    return deviceMethodData;
                },
                null);
    }

    @After
    public void afterTest()
    {
        this.testInstance.dispose();
    }

    protected void invokeMethodSucceed() throws Exception
    {
        // Act
        DirectMethodRequestOptions options =
            DirectMethodRequestOptions.builder()
                .payload(new JsonPrimitive(PAYLOAD_STRING))
                .build();

        MethodResult result;
        if (testInstance.identity instanceof TestModuleIdentity)
        {
            result = testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), ((TestModuleIdentity)testInstance.identity).getModule().getId(), METHOD_LOOPBACK, options);
        }
        else
        {
            result = testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), METHOD_LOOPBACK, options);
        }

        // Assert
        assertNotNull(result);
        assertEquals((long)METHOD_SUCCESS, (long)result.getStatus());
        assertEquals(METHOD_LOOPBACK + ":" + PAYLOAD_STRING, result.getPayload().toString().replace("\"", ""));
    }

    protected void invokeMethodWithDifferentPayloadType() throws Exception
    {
        // Types which can be converted to JsonPrimitive
        invokeHelper("###test message!!!");
        invokeHelper(1.0);
        invokeHelper(true);
        invokeHelper('c');

        // Types which can be converted to JsonArray
        List<String> list = new ArrayList<>();
        list.add("element1");
        list.add("element2");
        list.add("element3");
        invokeHelper(list);

        Set<String> set = new HashSet<>();
        set.add("A");
        set.add("B");
        set.add("C");
        invokeHelper(set);

        // Types which can be converted to JsonObject
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        invokeHelper(map);

        CustomObject customObject = new CustomObject("some test message", 1, true, new NestedCustomObject("some nested test message", 2));
        invokeHelper(customObject);
    }

    private void invokeHelper(Object payload) throws Exception
    {
        String jsonString = new Gson().toJson(payload);

        DirectMethodRequestOptions options =
                DirectMethodRequestOptions.builder()
                        .payload(new JsonParser().parse(jsonString))
                        .build();

        MethodResult result;
        if (testInstance.identity instanceof TestModuleIdentity)
        {
            result = testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), ((TestModuleIdentity)testInstance.identity).getModule().getId(), METHOD_ECHO, options);
        }
        else
        {
            result = testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), METHOD_ECHO, options);
        }

        // Assert on service side
        assertNotNull(result);
        assertEquals((long)METHOD_SUCCESS, (long)result.getStatus());
        assertEquals(options.getPayload().getClass(), result.getPayload().getClass());
        assertEquals(options.getPayload(), result.getPayload());

        // Assert across service and device side
        if (payload instanceof CustomObject)
        {
            assertEquals(methodDataObject.toString(), convertCustomToMap((CustomObject) payload).toString());
        }
        else
        {
            assertEquals(methodDataObject.toString(), payload.toString());
        }
    }

    private Map<String, Object> convertCustomToMap(CustomObject customObject)
    {
        NestedCustomObject nestedCustomObject = customObject.getNestedCustomObjectAttri();
        Map<String, Object> nestedMap = new ObjectMapper().convertValue(nestedCustomObject, Map.class);

        Map<String, Object> map = new HashMap(){{
            put("stringAttri", customObject.getStringAttri());
            put("intAttri", customObject.getIntAttri());
            put("boolAttri", customObject.getBooleanAttri());
            put("nestedCustomObjectAttri", nestedMap.toString());
        }};

        return map;
    }

    protected static DirectMethodsClient buildDeviceMethodClientWithAzureSasCredential()
    {
        IotHubConnectionString iotHubConnectionStringObj = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString);
        IotHubServiceSasToken serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);
        AzureSasCredential azureSasCredential = new AzureSasCredential(serviceSasToken.toString());
        DirectMethodsClientOptions options = DirectMethodsClientOptions.builder().httpReadTimeoutSeconds(HTTP_READ_TIMEOUT).build();
        return new DirectMethodsClient(iotHubConnectionStringObj.getHostName(), azureSasCredential, options);
    }
}
