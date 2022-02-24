/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.methods;


import com.azure.core.credential.AzureSasCredential;
import com.microsoft.azure.sdk.iot.service.exceptions.ErrorCodeDescription;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubUnauthorizedException;
import com.microsoft.azure.sdk.iot.service.methods.DirectMethodRequestOptions;
import com.microsoft.azure.sdk.iot.service.methods.DirectMethodsClient;
import com.microsoft.azure.sdk.iot.service.methods.DirectMethodsClientOptions;
import com.microsoft.azure.sdk.iot.service.methods.MethodResult;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubGatewayTimeoutException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.ClientType;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.DeviceEmulator;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.DeviceTestManager;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.SasTokenTools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestModuleIdentity;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.setup.DirectMethodsCommon;

import java.net.InetSocketAddress;
import java.net.Proxy;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;
import static tests.integration.com.microsoft.azure.sdk.iot.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;

/**
 * Test class containing all non error injection tests to be run on JVM and android pertaining to Device methods.
 */
@Slf4j
@IotHubTest
@RunWith(Parameterized.class)
public class DirectMethodsTests extends DirectMethodsCommon
{
    public DirectMethodsTests(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws Exception
    {
        super(protocol, authenticationType, clientType);
    }

    @Test
    @StandardTierHubOnlyTest
    public void invokeMethodSucceed() throws Exception
    {
        super.openDeviceClientAndSubscribeToMethods();
        super.invokeMethodSucceed();
    }

    @Test
    @StandardTierHubOnlyTest
    public void invokeMethodSucceedWithAzureSasCredential() throws Exception
    {
        this.testInstance.methodServiceClient = buildDeviceMethodClientWithAzureSasCredential();
        super.openDeviceClientAndSubscribeToMethods();
        super.invokeMethodSucceed();
    }

    @Test
    @StandardTierHubOnlyTest
    public void serviceClientTokenRenewalWithAzureSasCredential() throws Exception
    {
        if (testInstance.protocol != IotHubClientProtocol.AMQPS
            || testInstance.clientType != ClientType.DEVICE_CLIENT
            || testInstance.authenticationType != AuthenticationType.SAS)
        {
            // This test is for the service client, so no need to rerun it for all the different client types or device protocols
            return;
        }

        IotHubConnectionString iotHubConnectionStringObj = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString);
        IotHubServiceSasToken serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);
        AzureSasCredential sasCredential = new AzureSasCredential(serviceSasToken.toString());

        this.testInstance.methodServiceClient =
            new DirectMethodsClient(
                iotHubConnectionStringObj.getHostName(),
                sasCredential,
                DirectMethodsClientOptions.builder().httpReadTimeoutSeconds(HTTP_READ_TIMEOUT).build());

        super.openDeviceClientAndSubscribeToMethods();

        // add first device just to make sure that the first credential update worked
        super.invokeMethodSucceed();

        // deliberately expire the SAS token to provoke a 401 to ensure that the method client is using the shared
        // access signature that is set here.
        sasCredential.update(SasTokenTools.makeSasTokenExpired(serviceSasToken.toString()));

        try
        {
            super.invokeMethodSucceed();
            fail("Expected invoke method call to throw unauthorized exception since an expired SAS token was used, but no exception was thrown");
        }
        catch (IotHubUnauthorizedException e)
        {
            log.debug("IotHubUnauthorizedException was thrown as expected, continuing test");
        }

        // Renew the expired shared access signature
        serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);
        sasCredential.update(serviceSasToken.toString());

        // final method invocation should succeed since the shared access signature has been renewed
        super.invokeMethodSucceed();
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void invokeMethodNullPayloadSucceed() throws Exception
    {
        // Arrange
        super.openDeviceClientAndSubscribeToMethods();
        DeviceTestManager deviceTestManger = this.testInstance.deviceTestManager;

        // Act
        MethodResult result;
        if (testInstance.identity instanceof TestModuleIdentity)
        {
            result = testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), ((TestModuleIdentity) testInstance.identity).getModuleId(), DeviceEmulator.METHOD_LOOPBACK);
        }
        else
        {
            result = testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), DeviceEmulator.METHOD_LOOPBACK);
        }
        deviceTestManger.waitIotHub(1, 10);

        // Assert
        assertNotNull(buildExceptionMessage("method result was null", testInstance.deviceTestManager.client), result);
        assertEquals(buildExceptionMessage("Expected SUCCESS but got " + result.getStatus(), testInstance.deviceTestManager.client), (long)DeviceEmulator.METHOD_SUCCESS, (long)result.getStatus());
        assertEquals(buildExceptionMessage("Expected " + DeviceEmulator.METHOD_LOOPBACK + ":null" + " but got " + result.getPayload(), deviceTestManger.client), DeviceEmulator.METHOD_LOOPBACK + ":null", result.getPayload());
        Assert.assertEquals(buildExceptionMessage("Unexpected status errors occurred", testInstance.deviceTestManager.client), 0, deviceTestManger.getStatusError());
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void invokeMethodRecoverFromTimeoutSucceed() throws Exception
    {
        // Arrange
        super.openDeviceClientAndSubscribeToMethods();
        DeviceTestManager deviceTestManger = this.testInstance.deviceTestManager;

        try
        {
            DirectMethodRequestOptions options =
                DirectMethodRequestOptions.builder()
                    .payload("7000")
                    .methodResponseTimeoutSeconds(RESPONSE_TIMEOUT)
                    .methodConnectTimeoutSeconds(CONNECTION_TIMEOUT)
                    .build();

            if (testInstance.identity instanceof TestModuleIdentity)
            {
                testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), ((TestModuleIdentity) testInstance.identity).getModuleId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, options);
            }
            else
            {
                testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, options);
            }
            assert true;
        }
        catch(IotHubGatewayTimeoutException expected)
        {
            //Don't do anything. Expected throw.
        }

        // Act
        DirectMethodRequestOptions options =
            DirectMethodRequestOptions.builder()
                .payload("100")
                .methodResponseTimeoutSeconds(RESPONSE_TIMEOUT)
                .methodConnectTimeoutSeconds(CONNECTION_TIMEOUT)
                .build();

        MethodResult result;
        if (testInstance.identity instanceof TestModuleIdentity)
        {
            result = testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), ((TestModuleIdentity) testInstance.identity).getModuleId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, options);
        }
        else
        {
            result = testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, options);
        }
        deviceTestManger.waitIotHub(1, 10);

        // Assert
        assertNotNull(buildExceptionMessage("method result was null", testInstance.deviceTestManager.client), result);
        assertEquals(buildExceptionMessage("Expected SUCCESS but got " + result.getStatus(), testInstance.deviceTestManager.client), (long)DeviceEmulator.METHOD_SUCCESS, (long)result.getStatus());
        assertEquals(buildExceptionMessage("Expected " + DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS + ":succeed" + " But got " + result.getPayload(), testInstance.deviceTestManager.client), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS + ":succeed", result.getPayload());
        Assert.assertEquals(buildExceptionMessage("Unexpected status errors occurred", testInstance.deviceTestManager.client), 0, deviceTestManger.getStatusError());
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void invokeMethodResponseTimeoutFailed() throws Exception
    {
        // Arrange
        this.openDeviceClientAndSubscribeToMethods();

        // Act
        boolean expectedExceptionCaught = false;
        try
        {
            DirectMethodRequestOptions options =
                DirectMethodRequestOptions.builder()
                    .payload("7000")
                    .methodResponseTimeoutSeconds(5)
                    .methodConnectTimeoutSeconds(CONNECTION_TIMEOUT)
                    .build();

            if (testInstance.identity instanceof TestModuleIdentity)
            {
                testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), ((TestModuleIdentity) testInstance.identity).getModuleId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, options);
            }
            else
            {
                testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, options);
            }
        }
        catch (IotHubGatewayTimeoutException e)
        {
            expectedExceptionCaught = true;
        }

        assertTrue(buildExceptionMessage("Iot Hub did not throw the expected gateway timeout exception", testInstance.deviceTestManager.client), expectedExceptionCaught);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void invokeMethodOnOfflineDevice() throws Exception
    {
        if (testInstance.protocol != IotHubClientProtocol.HTTPS || testInstance.authenticationType != AuthenticationType.SAS)
        {
            // This test doesn't care what protocol the device client would use since it will be a 404 anyways.
            // Client authentication type always has no bearing on this test, so only run it for one protocol + one authentication type
            return;
        }

        try
        {
            //force the device offline
            testInstance.deviceTestManager.client.close();

            DirectMethodRequestOptions options =
                DirectMethodRequestOptions.builder()
                    .methodResponseTimeoutSeconds(RESPONSE_TIMEOUT)
                    .methodConnectTimeoutSeconds(CONNECTION_TIMEOUT)
                    .build();

            if (testInstance.identity instanceof TestModuleIdentity)
            {
                testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), ((TestModuleIdentity) testInstance.identity).getModuleId(), DeviceEmulator.METHOD_LOOPBACK, options);
            }
            else
            {
                testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), DeviceEmulator.METHOD_LOOPBACK, options);
            }

            Assert.fail(buildExceptionMessage("Invoking method on device or module that wasn't online should have thrown an exception", testInstance.deviceTestManager.client));
        }
        catch (IotHubNotFoundException actualException)
        {
            // Don't do anything, expected throw.
            Assert.assertEquals(404103, actualException.getErrorCode());
            Assert.assertEquals(ErrorCodeDescription.DeviceNotOnline, actualException.getErrorCodeDescription());
        }
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void invokeMethodOnUnregisteredDevice() throws Exception
    {
        if (testInstance.authenticationType != AuthenticationType.SAS)
        {
            // Client authentication type always has no bearing on this test, so only run it for one authentication type for each protocol
            return;
        }

        try
        {
            if (testInstance.identity instanceof TestModuleIdentity)
            {
                testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), "ThisModuleDoesNotExist", DeviceEmulator.METHOD_LOOPBACK);
            }
            else
            {
                testInstance.methodServiceClient.invoke("ThisDeviceDoesNotExist", DeviceEmulator.METHOD_LOOPBACK);
            }

            Assert.fail(buildExceptionMessage("Invoking method on device or module that doesn't exist should have thrown an exception", testInstance.deviceTestManager.client));
        }
        catch (IotHubNotFoundException actualException)
        {
            // Don't do anything, expected throw.
            if (testInstance.identity instanceof TestModuleIdentity)
            {
                Assert.assertEquals(404010, actualException.getErrorCode());
                Assert.assertEquals(ErrorCodeDescription.ModuleNotFound, actualException.getErrorCodeDescription());
            }
            else
            {
                Assert.assertEquals(404001, actualException.getErrorCode());
                Assert.assertEquals(ErrorCodeDescription.DeviceNotFound, actualException.getErrorCodeDescription());
            }
        }
    }

    @Test
    @StandardTierHubOnlyTest
    public void invokeMethodWithServiceSideProxy() throws Exception
    {
        if (testInstance.protocol != IotHubClientProtocol.MQTT || testInstance.authenticationType != AuthenticationType.SAS || testInstance.clientType != ClientType.DEVICE_CLIENT)
        {
            // This test doesn't really care about the device side protocol or authentication, so just run it once
            // when the device is using MQTT with SAS auth
            return;
        }

        String testProxyHostname = "127.0.0.1";
        int testProxyPort = 8894;
        HttpProxyServer proxyServer = DefaultHttpProxyServer.bootstrap()
                .withPort(testProxyPort)
                .start();

        try
        {
            Proxy serviceSideProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(testProxyHostname, testProxyPort));

            ProxyOptions proxyOptions = new ProxyOptions(serviceSideProxy);
            DirectMethodsClientOptions options = DirectMethodsClientOptions.builder().proxyOptions(proxyOptions).httpReadTimeoutSeconds(HTTP_READ_TIMEOUT).build();

            this.testInstance.methodServiceClient = new DirectMethodsClient(iotHubConnectionString, options);

            super.openDeviceClientAndSubscribeToMethods();
            super.invokeMethodSucceed();
        }
        finally
        {
            proxyServer.stop();
        }
    }
}
