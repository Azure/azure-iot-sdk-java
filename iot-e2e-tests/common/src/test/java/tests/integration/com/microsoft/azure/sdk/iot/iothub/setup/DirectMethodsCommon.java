/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.integration.com.microsoft.azure.sdk.iot.iothub.setup;


import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.twin.DirectMethodPayload;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.methods.DirectMethodRequestOptions;
import com.microsoft.azure.sdk.iot.service.methods.DirectMethodResponse;
import com.microsoft.azure.sdk.iot.service.methods.DirectMethodsClient;
import com.microsoft.azure.sdk.iot.service.methods.DirectMethodsClientOptions;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClient;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClientOptions;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.*;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.AMQPS;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
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
    public static final String METHOD_MODIFY = "modify";
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

        return Arrays.asList(
            new Object[][]
                {
                    {AMQPS, SAS, ClientType.DEVICE_CLIENT},
                    {MQTT, SAS, ClientType.DEVICE_CLIENT},
                    {AMQPS, SAS, ClientType.MODULE_CLIENT},
                    {MQTT, SAS, ClientType.MODULE_CLIENT},
                });
    }

    protected static String iotHubConnectionString = "";

    protected static final int RESPONSE_TIMEOUT = 200;
    protected static final int CONNECTION_TIMEOUT = 5;
    protected static final String PAYLOAD_STRING = "This is a valid payload";

    protected DirectMethodTestInstance testInstance;

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

        // Test-specific variables used for test instances running in parallel
        public DirectMethodPayload directMethodPayload;
        public com.microsoft.azure.sdk.iot.device.twin.DirectMethodResponse directMethodResponse;
        public int statusCode;

        protected DirectMethodTestInstance(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws Exception
        {
            this.protocol = protocol;
            this.authenticationType = authenticationType;
            this.clientType = clientType;
            this.publicKeyCert = x509CertificateGenerator.getPublicCertificatePEM();
            this.privateKey = x509CertificateGenerator.getPrivateKeyPEM();
            this.x509Thumbprint = x509CertificateGenerator.getX509Thumbprint();
            this.methodServiceClient = new DirectMethodsClient(
                    iotHubConnectionString, DirectMethodsClientOptions.builder().httpReadTimeoutSeconds(HTTP_READ_TIMEOUT).build());
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

    private String loopback(DirectMethodPayload methodData)
    {
        String payload = new String(methodData.getPayload(String.class)
                .getBytes(StandardCharsets.UTF_8)).replace("\"", "");

        return METHOD_LOOPBACK + ":" + payload;
    }

    private String delayInMilliseconds(DirectMethodPayload methodData) throws InterruptedException
    {
        String payload = new String(methodData.getPayload(String.class)
                .getBytes(StandardCharsets.UTF_8)).replace("\"", "");

        long delay = Long.parseLong(payload);
        Thread.sleep(delay);

        return METHOD_DELAY_IN_MILLISECONDS + ":succeed";
    }

    private Object modifyPayload(Object methodData)
    {
        if (methodData instanceof Boolean)
        {
            // set methodData from true to false
            methodData = false;
        }
        else if (methodData instanceof String)
        {
            // set methodData (as a String) from "This is a valid payload." to "This is a new valid payload."
            methodData = "This is a new valid payload.";
        }
        else if (methodData instanceof byte[])
        {
            // set the first element in methodData (as a byte array) from 1 (original value) to 2
            ((byte[]) methodData)[0] = 2;
        }
        else if (methodData instanceof List)
        {
            if (!((List<?>) methodData).isEmpty())
            {
                // set the first element in methodData (as a list) from 1.0 (original value) to 2.0
                ((List<Double>) methodData).set(0, 2.0);
            }
        }
        else if (methodData instanceof Map)
        {
            if (!((Map<?, ?>) methodData).isEmpty())
            {
                // set the value of "key" in methodData (as a map) from "value" to "new value"
                ((Map<String, Object>) methodData).put("key", "new value");
            }
        }
        else if (methodData instanceof CustomObject)
        {
            // set the value of "stringAttri" in methodData (as a CustomObject) from "test message" to "new test message"
            ((CustomObject)methodData).setStringAttri("new test message");
        }
        else
        {
            methodData = "NULL Payload.";
        }

        return methodData;
    }

    public void openDeviceClientAndSubscribeToMethods() throws Exception
    {
        testInstance.setup();
        this.testInstance.identity.getClient().open(true);

        this.testInstance.identity.getClient().subscribeToMethods(
            (methodName, methodData, context) ->
            {
                log.info("Device invoked " + methodName);
                com.microsoft.azure.sdk.iot.device.twin.DirectMethodResponse deviceMethodData;
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
                deviceMethodData = new com.microsoft.azure.sdk.iot.device.twin.DirectMethodResponse(status, result);

                return deviceMethodData;
            },
            null);
    }

    public void subscribeToMethodAndReceiveAsDifferentTypes(String type) throws Exception
    {
        testInstance.setup();
        this.testInstance.identity.getClient().open(true);

        this.testInstance.identity.getClient().subscribeToMethods(
                (methodName, methodData, context) ->
                {
                    Object methodDataAsDifferentTypeObject;

                    if (type.equals("Boolean"))
                    {
                        methodDataAsDifferentTypeObject = methodData.getPayload(Boolean.class);
                    }
                    else if (type.equals("String"))
                    {
                        methodDataAsDifferentTypeObject = methodData.getPayload(String.class);
                    }
                    else if (type.equals("Array"))
                    {
                        methodDataAsDifferentTypeObject = methodData.getPayload(byte[].class);
                    }
                    else if (type.equals("ArrayList"))
                    {
                        methodDataAsDifferentTypeObject = methodData.getPayload(List.class);
                    }
                    else if (type.equals("HashMap"))
                    {
                        methodDataAsDifferentTypeObject = methodData.getPayload(Map.class);
                    }
                    else if (type.equals("CustomObject"))
                    {
                        methodDataAsDifferentTypeObject = methodData.getPayload(CustomObject.class);
                    }
                    else
                    {
                        methodDataAsDifferentTypeObject = methodData.getPayload(String.class);
                    }

                    log.info("Device invoked " + methodName);
                    com.microsoft.azure.sdk.iot.device.twin.DirectMethodResponse methodResponseAsDifferentTypeObject;
                    Object result;
                    int status;

                    try
                    {
                        if (METHOD_MODIFY.equals(methodName))
                        {
                            result = modifyPayload(methodDataAsDifferentTypeObject);
                            status = 1 + new Random().nextInt(998); // random number bound from 1 to 999
                        }
                        else
                        {
                            result = "unknown:" + methodName;
                            status = METHOD_NOT_DEFINED;
                        }
                    }
                    catch (Exception e)
                    {
                        result = e.toString();
                        status = METHOD_THROWS;
                    }

                    methodResponseAsDifferentTypeObject = new com.microsoft.azure.sdk.iot.device.twin.DirectMethodResponse(status, result);

                    this.testInstance.directMethodPayload = methodData;
                    this.testInstance.directMethodResponse = methodResponseAsDifferentTypeObject;
                    this.testInstance.statusCode = status;

                    return methodResponseAsDifferentTypeObject;
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
                .payload(PAYLOAD_STRING)
                .build();

        DirectMethodResponse result;
        if (testInstance.identity instanceof TestModuleIdentity)
        {
            result = testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(),
                ((TestModuleIdentity)testInstance.identity).getModule().getId(), METHOD_LOOPBACK, options);
        }
        else
        {
            result = testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), METHOD_LOOPBACK, options);
        }

        // Assert
        assertNotNull(result);
        assertEquals(METHOD_SUCCESS, (long)result.getStatus());
        assertEquals(METHOD_LOOPBACK + ":" + PAYLOAD_STRING, result.getPayload(String.class));
    }

    protected void invokeHelper(Object payload) throws Exception
    {
        DirectMethodRequestOptions options =
            DirectMethodRequestOptions.builder()
                .payload(payload)
                .build();

        DirectMethodResponse result;
        if (testInstance.identity instanceof TestModuleIdentity)
        {
            result = testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(),
                ((TestModuleIdentity)testInstance.identity).getModule().getId(), METHOD_MODIFY, options);
        }
        else
        {
            result = testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), METHOD_MODIFY, options);
        }

        // Assert
        if (payload instanceof Boolean)
        {
            // e2e test for DirectMethodRequestOptions and DirectMethodPayload
            assertPayloadHelper(options.getPayload(), this.testInstance.directMethodPayload.getPayload(Boolean.class));
            // e2e test for DirectMethodResponse between device/module and service
            assertEquals(this.testInstance.statusCode, (long)result.getStatus());
            assertPayloadHelper(this.testInstance.directMethodResponse.getPayload(), result.getPayload(Boolean.class));
        }
        else if (payload instanceof byte[])
        {
            // e2e test for DirectMethodRequestOptions and DirectMethodPayload
            assertPayloadHelper(new String((byte[]) options.getPayload(), StandardCharsets.UTF_8),
                    new String(this.testInstance.directMethodPayload.getPayload(byte[].class), StandardCharsets.UTF_8));
            // e2e test for DirectMethodResponse between device/module and service
            assertEquals(this.testInstance.statusCode, (long)result.getStatus());
            assertPayloadHelper(new String((byte[]) this.testInstance.directMethodResponse.getPayload(), StandardCharsets.UTF_8),
                    new String(result.getPayload(byte[].class), StandardCharsets.UTF_8));
        }
        else if (payload instanceof List)
        {
            // e2e test for DirectMethodRequestOptions and DirectMethodPayload
            assertPayloadHelper(options.getPayload(), this.testInstance.directMethodPayload.getPayload(List.class));
            // e2e test for DirectMethodResponse between device/module and service
            assertEquals(this.testInstance.statusCode, (long)result.getStatus());
            assertPayloadHelper(this.testInstance.directMethodResponse.getPayload(), result.getPayload(List.class));
        }
        else if (payload instanceof Map)
        {
            // e2e test for DirectMethodRequestOptions and DirectMethodPayload
            assertPayloadHelper(options.getPayload(), this.testInstance.directMethodPayload.getPayload(Map.class));
            // e2e test for DirectMethodResponse between device/module and service
            assertEquals(this.testInstance.statusCode, (long)result.getStatus());
            assertPayloadHelper(this.testInstance.directMethodResponse.getPayload(), result.getPayload(Map.class));
        }
        else if (payload instanceof CustomObject)
        {
            // e2e test for DirectMethodRequestOptions and DirectMethodPayload
            assertPayloadHelper(options.getPayload(), this.testInstance.directMethodPayload.getPayload(CustomObject.class));
            // e2e test for DirectMethodResponse between device/module and service
            assertEquals(this.testInstance.statusCode, (long)result.getStatus());
            assertPayloadHelper(this.testInstance.directMethodResponse.getPayload(), result.getPayload(CustomObject.class));
        }
        else
        {
            // e2e test for DirectMethodRequestOptions and DirectMethodPayload
            assertPayloadHelper(options.getPayload(), this.testInstance.directMethodPayload.getPayload(String.class));
            // e2e test for DirectMethodResponse between device/module and service
            assertEquals(this.testInstance.statusCode, (long)result.getStatus());
            assertPayloadHelper(this.testInstance.directMethodResponse.getPayload(), result.getPayload(String.class));
        }
    }

    private void assertPayloadHelper(Object senderPayload, Object receiverPayload)
    {
        if (senderPayload != null)
        {
            assertNotNull(receiverPayload);
        }

        if (senderPayload instanceof CustomObject && receiverPayload instanceof CustomObject)
        {
            assertEquals(senderPayload.toString(), receiverPayload.toString());
        }
        else
        {
            assertEquals(senderPayload, receiverPayload);
        }
    }
}
