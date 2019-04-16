/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.tests.iothubservices.methods;

import com.microsoft.azure.sdk.iot.common.helpers.*;
import com.microsoft.azure.sdk.iot.common.setup.DeviceMethodCommon;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.BaseDevice;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.Module;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.devicetwin.MethodResult;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubGatewayTimeoutException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubNotFoundException;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.microsoft.azure.sdk.iot.common.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test class containing all non error injection tests to be run on JVM and android pertaining to Device methods. Class needs to be extended
 * in order to run these tests as that extended class handles setting connection strings and certificate generation
 */
public class DeviceMethodTests extends DeviceMethodCommon
{
    public DeviceMethodTests(DeviceTestManager deviceTestManager, IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, BaseDevice identity, String publicKeyCert, String privateKey, String x509Thumbprint)
    {
        super(deviceTestManager, protocol, authenticationType, clientType, identity, publicKeyCert, privateKey, x509Thumbprint);

        System.out.println(clientType + " DeviceMethodTests UUID: " + (identity instanceof Module ? ((Module) identity).getId() : identity.getDeviceId()));
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void invokeMethodSucceed() throws Exception
    {
        super.invokeMethodSucceed();
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void invokeMethodInvokeParallelSucceed() throws Exception
    {
        // Arrange
        CountDownLatch cdl = new CountDownLatch(NUMBER_INVOKES_PARALLEL);
        List<RunnableInvoke> runs = new LinkedList<>();

        for (int i = 0; i < NUMBER_INVOKES_PARALLEL; i++)
        {
            RunnableInvoke runnableInvoke;
            if (testInstance.identity instanceof Module)
            {
                runnableInvoke = new RunnableInvoke(methodServiceClient, testInstance.identity.getDeviceId(), ((Module) testInstance.identity).getId(),"Thread" + i, cdl);
            }
            else
            {
                runnableInvoke = new RunnableInvoke(methodServiceClient, testInstance.identity.getDeviceId(), null,"Thread" + i, cdl);
            }
            new Thread(runnableInvoke).start();
            runs.add(runnableInvoke);
        }

        cdl.await();

        for (RunnableInvoke run:runs)
        {
            MethodResult result = run.getResult();
            assertNotNull(buildExceptionMessage(run.getException() == null ? "Runnable returns null without exception information" : run.getException().getMessage(), testInstance.deviceTestManager.client), result);
            assertEquals(buildExceptionMessage("Expected SUCCESS but was " + result.getStatus(), testInstance.deviceTestManager.client), (long)DeviceEmulator.METHOD_SUCCESS,(long)result.getStatus());
            assertEquals(buildExceptionMessage("Expected payload " + run.getExpectedPayload() + " but got " + result.getPayload().toString(), testInstance.deviceTestManager.client), run.getExpectedPayload(), result.getPayload().toString());
        }
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void invokeMethodStandardTimeoutSucceed() throws Exception
    {
        // Arrange
        DeviceTestManager deviceTestManger = this.testInstance.deviceTestManager;

        // Act
        MethodResult result;
        if (testInstance.identity instanceof Module)
        {
            result = methodServiceClient.invoke(testInstance.identity.getDeviceId(), ((Module) testInstance.identity).getId(), DeviceEmulator.METHOD_LOOPBACK, null, null, PAYLOAD_STRING);
        }
        else
        {
            result = methodServiceClient.invoke(testInstance.identity.getDeviceId(), DeviceEmulator.METHOD_LOOPBACK, null, null, PAYLOAD_STRING);
        }

        deviceTestManger.waitIotHub(1, 10);

        // Assert
        assertNotNull(buildExceptionMessage("method result was null", testInstance.deviceTestManager.client), result);
        assertEquals(buildExceptionMessage("Expected SUCCESS but got " + result.getStatus(), testInstance.deviceTestManager.client), (long)DeviceEmulator.METHOD_SUCCESS, (long)result.getStatus());
        assertEquals(buildExceptionMessage("Expected " + DeviceEmulator.METHOD_LOOPBACK + ":" + PAYLOAD_STRING + " but got " + result.getPayload(), testInstance.deviceTestManager.client), DeviceEmulator.METHOD_LOOPBACK + ":" + PAYLOAD_STRING, result.getPayload());
        Assert.assertEquals(buildExceptionMessage("Unexpected status errors occurred", testInstance.deviceTestManager.client), 0, deviceTestManger.getStatusError());
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void invokeMethodNullPayloadSucceed() throws Exception
    {
        // Arrange
        DeviceTestManager deviceTestManger = this.testInstance.deviceTestManager;

        // Act
        MethodResult result;
        if (testInstance.identity instanceof Module)
        {
            result = methodServiceClient.invoke(testInstance.identity.getDeviceId(), ((Module) testInstance.identity).getId(), DeviceEmulator.METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, null);
        }
        else
        {
            result = methodServiceClient.invoke(testInstance.identity.getDeviceId(), DeviceEmulator.METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, null);
        }
        deviceTestManger.waitIotHub(1, 10);

        // Assert
        assertNotNull(buildExceptionMessage("method result was null", testInstance.deviceTestManager.client), result);
        assertEquals(buildExceptionMessage("Expected SUCCESS but got " + result.getStatus(), testInstance.deviceTestManager.client), (long)DeviceEmulator.METHOD_SUCCESS, (long)result.getStatus());
        assertEquals(buildExceptionMessage("Expected " + DeviceEmulator.METHOD_LOOPBACK + ":null" + " but got " + result.getPayload(), deviceTestManger.client), DeviceEmulator.METHOD_LOOPBACK + ":null", result.getPayload());
        Assert.assertEquals(buildExceptionMessage("Unexpected status errors occurred", testInstance.deviceTestManager.client), 0, deviceTestManger.getStatusError());
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void invokeMethodNumberSucceed() throws Exception
    {
        // Arrange
        DeviceTestManager deviceTestManger = this.testInstance.deviceTestManager;

        // Act
        MethodResult result;
        if (testInstance.identity instanceof Module)
        {
            result = methodServiceClient.invoke(testInstance.identity.getDeviceId(), ((Module) testInstance.identity).getId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, "100");
        }
        else
        {
            result = methodServiceClient.invoke(testInstance.identity.getDeviceId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, "100");
        }
        deviceTestManger.waitIotHub(1, 10);

        // Assert
        assertNotNull(buildExceptionMessage("method result was null", testInstance.deviceTestManager.client), result);
        assertEquals(buildExceptionMessage("Expected SUCCESS but got " + result.getStatus(), testInstance.deviceTestManager.client), (long)DeviceEmulator.METHOD_SUCCESS, (long)result.getStatus());
        assertEquals(buildExceptionMessage("Expected " + DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS + ":succeed" + " But got " + result.getPayload(), testInstance.deviceTestManager.client), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS + ":succeed", result.getPayload());
        Assert.assertEquals(buildExceptionMessage("Unexpected status errors occurred", testInstance.deviceTestManager.client), 0, deviceTestManger.getStatusError());
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void invokeMethodThrowsNumberFormatExceptionFailed() throws Exception
    {
        // Arrange
        DeviceTestManager deviceTestManger = this.testInstance.deviceTestManager;

        // Act
        MethodResult result;
        if (testInstance.identity instanceof Module)
        {
            result = methodServiceClient.invoke(testInstance.identity.getDeviceId(), ((Module) testInstance.identity).getId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING);
        }
        else
        {
            result = methodServiceClient.invoke(testInstance.identity.getDeviceId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING);
        }
        deviceTestManger.waitIotHub(1, 10);

        // Assert
        assertNotNull(buildExceptionMessage("method result was null", testInstance.deviceTestManager.client), result);
        assertEquals(buildExceptionMessage("Expected " + DeviceEmulator.METHOD_THROWS + " but got " + result.getStatus(), testInstance.deviceTestManager.client), (long)DeviceEmulator.METHOD_THROWS, (long)result.getStatus());
        assertEquals(buildExceptionMessage("Expected NumberFormatException, but got " + result.getPayload(), testInstance.deviceTestManager.client), "java.lang.NumberFormatException: For input string: \"" + PAYLOAD_STRING + "\"", result.getPayload());
        Assert.assertEquals(buildExceptionMessage("Unexpected status errors occurred", testInstance.deviceTestManager.client), 0, deviceTestManger.getStatusError());
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void invokeMethodUnknownFailed() throws Exception
    {
        // Arrange
        DeviceTestManager deviceTestManger = this.testInstance.deviceTestManager;

        // Act
        MethodResult result;
        if (testInstance.identity instanceof Module)
        {
            result = methodServiceClient.invoke(testInstance.identity.getDeviceId(), ((Module) testInstance.identity).getId(), DeviceEmulator.METHOD_UNKNOWN, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING);
        }
        else
        {
            result = methodServiceClient.invoke(testInstance.identity.getDeviceId(), DeviceEmulator.METHOD_UNKNOWN, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING);
        }
        deviceTestManger.waitIotHub(1, 10);

        // Assert
        assertNotNull(buildExceptionMessage("method result was null", testInstance.deviceTestManager.client), result);
        assertEquals(buildExceptionMessage("Expected METHOD_NOT_DEFINED but got " + result.getStatus(), testInstance.deviceTestManager.client), (long)DeviceEmulator.METHOD_NOT_DEFINED, (long)result.getStatus());
        Assert.assertEquals(buildExceptionMessage("Expected " + "unknown:" + DeviceEmulator.METHOD_UNKNOWN + " but got " + result.getPayload(), testInstance.deviceTestManager.client), "unknown:" + DeviceEmulator.METHOD_UNKNOWN, result.getPayload());
        Assert.assertEquals(buildExceptionMessage("Unexpected status errors occurred", testInstance.deviceTestManager.client), 0, deviceTestManger.getStatusError());
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void invokeMethodRecoverFromTimeoutSucceed() throws Exception
    {
        // Arrange
        DeviceTestManager deviceTestManger = this.testInstance.deviceTestManager;

        try
        {
            if (testInstance.identity instanceof Module)
            {
                methodServiceClient.invoke(testInstance.identity.getDeviceId(), ((Module) testInstance.identity).getId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, (long)5, CONNECTION_TIMEOUT, "7000");
            }
            else
            {
                methodServiceClient.invoke(testInstance.identity.getDeviceId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, (long)5, CONNECTION_TIMEOUT, "7000");
            }
            assert true;
        }
        catch(IotHubGatewayTimeoutException expected)
        {
            //Don't do anything. Expected throw.
        }

        // Act
        MethodResult result;
        if (testInstance.identity instanceof Module)
        {
            result = methodServiceClient.invoke(testInstance.identity.getDeviceId(), ((Module) testInstance.identity).getId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, "100");
        }
        else
        {
            result = methodServiceClient.invoke(testInstance.identity.getDeviceId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, "100");
        }
        deviceTestManger.waitIotHub(1, 10);

        // Assert
        assertNotNull(buildExceptionMessage("method result was null", testInstance.deviceTestManager.client), result);
        assertEquals(buildExceptionMessage("Expected SUCCESS but got " + result.getStatus(), testInstance.deviceTestManager.client), (long)DeviceEmulator.METHOD_SUCCESS, (long)result.getStatus());
        assertEquals(buildExceptionMessage("Expected " + DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS + ":succeed" + " But got " + result.getPayload(), testInstance.deviceTestManager.client), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS + ":succeed", result.getPayload());
        Assert.assertEquals(buildExceptionMessage("Unexpected status errors occurred", testInstance.deviceTestManager.client), 0, deviceTestManger.getStatusError());
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void invokeMethodDefaultResponseTimeoutSucceed() throws Exception
    {
        // Arrange
        DeviceTestManager deviceTestManger = this.testInstance.deviceTestManager;

        // Act
        MethodResult result;
        if (testInstance.identity instanceof Module)
        {
            result = methodServiceClient.invoke(testInstance.identity.getDeviceId(), ((Module) testInstance.identity).getId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, null, CONNECTION_TIMEOUT, "100");
        }
        else
        {
            result = methodServiceClient.invoke(testInstance.identity.getDeviceId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, null, CONNECTION_TIMEOUT, "100");
        }
        deviceTestManger.waitIotHub(1, 10);

        // Assert
        assertNotNull(buildExceptionMessage("method result was null", testInstance.deviceTestManager.client), result);
        assertEquals(buildExceptionMessage("Expected SUCCESS but got " + result.getStatus(), testInstance.deviceTestManager.client), (long)DeviceEmulator.METHOD_SUCCESS, (long)result.getStatus());
        assertEquals(buildExceptionMessage("Expected " + DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS + ":succeed" + " But got " + result.getPayload(), testInstance.deviceTestManager.client), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS + ":succeed", result.getPayload());
        Assert.assertEquals(buildExceptionMessage("Unexpected status errors occurred", testInstance.deviceTestManager.client), 0, deviceTestManger.getStatusError());
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void invokeMethodDefaultConnectionTimeoutSucceed() throws Exception
    {
        // Arrange
        DeviceTestManager deviceTestManger = this.testInstance.deviceTestManager;

        // Act
        MethodResult result;
        if (testInstance.identity instanceof Module)
        {
            result = methodServiceClient.invoke(testInstance.identity.getDeviceId(), ((Module) testInstance.identity).getId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, RESPONSE_TIMEOUT, null, "100");
        }
        else
        {
            result = methodServiceClient.invoke(testInstance.identity.getDeviceId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, RESPONSE_TIMEOUT, null, "100");
        }
        deviceTestManger.waitIotHub(1, 10);

        // Assert
        assertNotNull(buildExceptionMessage("method result was null", testInstance.deviceTestManager.client), result);
        assertEquals(buildExceptionMessage("Expected SUCCESS but got " + result.getStatus(), testInstance.deviceTestManager.client), (long)DeviceEmulator.METHOD_SUCCESS, (long)result.getStatus());
        assertEquals(buildExceptionMessage("Expected " + DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS + ":succeed" + " But got " + result.getPayload(), testInstance.deviceTestManager.client), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS + ":succeed", result.getPayload());
        Assert.assertEquals(buildExceptionMessage("Unexpected status errors occurred", testInstance.deviceTestManager.client), 0, deviceTestManger.getStatusError());
    }

    @Test (expected = IotHubGatewayTimeoutException.class)
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void invokeMethodResponseTimeoutFailed() throws Exception
    {
        // Arrange
        // Act
        if (testInstance.identity instanceof Module)
        {
            methodServiceClient.invoke(testInstance.identity.getDeviceId(), ((Module) testInstance.identity).getId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, (long)5, CONNECTION_TIMEOUT, "7000");
        }
        else
        {
            methodServiceClient.invoke(testInstance.identity.getDeviceId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, (long)5, CONNECTION_TIMEOUT, "7000");
        }
    }

    @Test(expected = IotHubNotFoundException.class)
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void invokeMethodUnknownDeviceFailed() throws Exception
    {
        if (testInstance.identity instanceof Module)
        {
            methodServiceClient.invoke(testInstance.identity.getDeviceId(), "someModuleThatDoesNotExistOnADeviceThatDoesExist", DeviceEmulator.METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING);
        }
        else
        {
            methodServiceClient.invoke("someDeviceThatDoesNotExist", DeviceEmulator.METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING);
        }
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void invokeMethodResetDeviceFailed() throws Exception
    {
        // Arrange
        DeviceTestManager deviceTestManger = this.testInstance.deviceTestManager;

        // Act
        try
        {
            if (testInstance.identity instanceof Module)
            {
                methodServiceClient.invoke(testInstance.identity.getDeviceId(), ((Module) testInstance.identity).getId(), DeviceEmulator.METHOD_RESET, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, null);
                deviceTestManger.restartDevice(getModuleConnectionString((Module) testInstance.identity), testInstance.protocol, testInstance.publicKeyCert, testInstance.privateKey);
            }
            else
            {
                methodServiceClient.invoke(testInstance.identity.getDeviceId(), DeviceEmulator.METHOD_RESET, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, null);
                deviceTestManger.restartDevice(registryManager.getDeviceConnectionString((Device) testInstance.identity), testInstance.protocol, testInstance.publicKeyCert, testInstance.privateKey);
            }

            Assert.fail(buildExceptionMessage("Reset identity do not affect the method invoke on the service", testInstance.deviceTestManager.client));
        }
        catch (IotHubNotFoundException expected)
        {
            // Don't do anything, expected throw.
        }

        if (testInstance.identity instanceof Module)
        {
            deviceTestManger.restartDevice(getModuleConnectionString((Module) testInstance.identity), testInstance.protocol, testInstance.publicKeyCert, testInstance.privateKey);
        }
        else
        {
            deviceTestManger.restartDevice(registryManager.getDeviceConnectionString((Device) testInstance.identity), testInstance.protocol, testInstance.publicKeyCert, testInstance.privateKey);
        }
    }
}
