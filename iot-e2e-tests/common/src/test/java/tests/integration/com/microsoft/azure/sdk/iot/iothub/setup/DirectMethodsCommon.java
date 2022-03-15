/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.integration.com.microsoft.azure.sdk.iot.iothub.setup;


import com.azure.core.credential.AzureSasCredential;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.twin.DirectMethodPayload;
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
        return inputsCommon();
    }

    protected static String iotHubConnectionString = "";

    protected static final int RESPONSE_TIMEOUT = 200;
    protected static final int CONNECTION_TIMEOUT = 5;
    protected static final String PAYLOAD_STRING = "This is a valid payload";

    protected DirectMethodTestInstance testInstance;

    private DirectMethodPayload directMethodPayload;
    private DirectMethodResponse directMethodResponse;
    private int statusCode;

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
        String payload = new String(((DirectMethodPayload)methodData).getPayloadAsJsonString().getBytes(StandardCharsets.UTF_8)).replace("\"", "");
        return METHOD_LOOPBACK + ":" + payload;
    }

    private String delayInMilliseconds(Object methodData) throws InterruptedException
    {
        String payload = new String(((DirectMethodPayload)methodData).getPayloadAsJsonString().getBytes(StandardCharsets.UTF_8)).replace("\"", "");
        long delay = Long.parseLong(payload);
        Thread.sleep(delay);
        return METHOD_DELAY_IN_MILLISECONDS + ":succeed";
    }

    private Object modifyPayload(Object methodData)
    {
        if (methodData instanceof String)
        {
            methodData = "This is a new valid payload.";
        }
        else if (methodData instanceof CustomObject)
        {
            ((CustomObject)methodData).setStringAttri("new test message");
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
                deviceMethodData = new DirectMethodResponse(status, result);

                return deviceMethodData;
            },
            null);
    }

    public void SubscribeToMethodAndReceiveAsJsonString() throws Exception
    {
        testInstance.setup();
        this.testInstance.identity.getClient().open(true);

        this.testInstance.identity.getClient().subscribeToMethods(
                (methodName, methodData, context) ->
                {
                    String methodDataAsJsonString = methodData.getPayloadAsJsonString();

                    System.out.println("Device invoked " + methodName);
                    DirectMethodResponse methodResponseAsJsonString;
                    int status;
                    Object result;

                    try
                    {
                        switch (methodName)
                        {
                            case METHOD_MODIFY:
                                result = modifyPayload(methodDataAsJsonString);
                                status = UUID.randomUUID().hashCode();
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

                    methodResponseAsJsonString = new DirectMethodResponse(status, result);

                    directMethodPayload = methodData;
                    directMethodResponse = methodResponseAsJsonString;
                    statusCode = status;

                    return methodResponseAsJsonString;
                },
                null);
    }

    public void SubscribeToMethodAndReceiveAsCustomObject() throws Exception
    {
        testInstance.setup();
        this.testInstance.identity.getClient().open(true);

        this.testInstance.identity.getClient().subscribeToMethods(
                (methodName, methodData, context) ->
                {
                    CustomObject methodDataAsCustomObject = methodData.getPayloadAsCustomType(CustomObject.class);

                    System.out.println("Device invoked " + methodName);
                    DirectMethodResponse methodResponseAsCustomObject;
                    Object result;
                    int status;

                    try
                    {
                        switch (methodName)
                        {
                            case METHOD_MODIFY:
                                result = modifyPayload(methodDataAsCustomObject);
                                status = UUID.randomUUID().hashCode();
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

                    methodResponseAsCustomObject = new DirectMethodResponse(status, result);

                    directMethodPayload = methodData;
                    directMethodResponse = methodResponseAsCustomObject;
                    statusCode = status;

                    return methodResponseAsCustomObject;
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
        assertEquals(METHOD_LOOPBACK + ":" + PAYLOAD_STRING, result.getPayloadAsJsonString());
    }

    protected void invokeHelper(Object payload) throws Exception
    {
        DirectMethodRequestOptions options =
                DirectMethodRequestOptions.builder()
                        .payload(payload)
                        .build();

        MethodResult result;
        if (testInstance.identity instanceof TestModuleIdentity)
        {
            result = testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), ((TestModuleIdentity)testInstance.identity).getModule().getId(), METHOD_MODIFY, options);
        }
        else
        {
            result = testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), METHOD_MODIFY, options);
        }

        // Assert
        if (payload instanceof String)
        {
            // e2e test for DirectMethodRequestOptions and DirectMethodPayload
            assertPayloadHelper(options.getPayload(), directMethodPayload.getPayloadAsJsonString());
            // e2e test for DirectMethodResponse and MethodResult
            assertEquals((long)statusCode, (long)result.getStatus());
            assertPayloadHelper(directMethodResponse.getResponseMessage(), result.getPayloadAsJsonString());
        }
        else if (payload instanceof CustomObject)
        {
            // e2e test for DirectMethodRequestOptions and DirectMethodPayload
            assertPayloadHelper(options.getPayload(), directMethodPayload.getPayloadAsCustomType(CustomObject.class));
            // e2e test for DirectMethodResponse and MethodResult
            assertEquals((long)statusCode, (long)result.getStatus());
            assertPayloadHelper(directMethodResponse.getResponseMessage(), result.getPayloadAsCustomType(CustomObject.class));
        }
    }

    private void assertPayloadHelper(Object senderPayload, Object receiverPayload)
    {
        assertNotNull(receiverPayload);

        if (senderPayload instanceof  String && receiverPayload instanceof String)
        {
            assertEquals(senderPayload, receiverPayload);
        }
        else if (senderPayload instanceof  CustomObject && receiverPayload instanceof CustomObject)
        {
            assertEquals(((CustomObject)senderPayload).toString(), ((CustomObject)receiverPayload).toString());
        }
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
