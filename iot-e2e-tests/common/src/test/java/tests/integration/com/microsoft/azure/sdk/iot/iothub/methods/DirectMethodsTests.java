/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.methods;

import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.ErrorCodeDescription;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubGatewayTimeoutException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubNotFoundException;
import com.microsoft.azure.sdk.iot.service.methods.DirectMethodRequestOptions;
import com.microsoft.azure.sdk.iot.service.methods.DirectMethodResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.ClientType;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.CustomObject;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.NestedCustomObject;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestModuleIdentity;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.setup.DirectMethodsCommon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

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
    @ContinuousIntegrationTest
    public void invokeMethodRecoverFromTimeoutSucceed() throws Exception
    {
        // Arrange
        super.openDeviceClientAndSubscribeToMethods();

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
                testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), ((TestModuleIdentity) testInstance.identity).getModuleId(), METHOD_DELAY_IN_MILLISECONDS, options);
            }
            else
            {
                testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), METHOD_DELAY_IN_MILLISECONDS, options);
            }
            assert true;
        }
        catch (IotHubGatewayTimeoutException expected)
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

        DirectMethodResponse result;
        if (testInstance.identity instanceof TestModuleIdentity)
        {
            result = testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), ((TestModuleIdentity) testInstance.identity).getModuleId(), METHOD_DELAY_IN_MILLISECONDS, options);
        }
        else
        {
            result = testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), METHOD_DELAY_IN_MILLISECONDS, options);
        }

        // Assert
        assertNotNull(buildExceptionMessage("method result was null", testInstance.identity.getClient()), result);
        assertEquals(buildExceptionMessage("Expected SUCCESS but got " + result.getStatus(), testInstance.identity.getClient()), METHOD_SUCCESS, (long)result.getStatus());
        assertEquals(buildExceptionMessage("Expected " + METHOD_DELAY_IN_MILLISECONDS + ":succeed" + " But got " + result.getPayload(String.class),
                testInstance.identity.getClient()), METHOD_DELAY_IN_MILLISECONDS + ":succeed", result.getPayload(String.class));
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
                testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), ((TestModuleIdentity) testInstance.identity).getModuleId(), METHOD_DELAY_IN_MILLISECONDS, options);
            }
            else
            {
                testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), METHOD_DELAY_IN_MILLISECONDS, options);
            }
        }
        catch (IotHubGatewayTimeoutException e)
        {
            expectedExceptionCaught = true;
        }

        assertTrue(buildExceptionMessage("Iot Hub did not throw the expected gateway timeout exception", testInstance.identity.getClient()), expectedExceptionCaught);
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
            testInstance.identity.getClient().close();

            DirectMethodRequestOptions options =
                DirectMethodRequestOptions.builder()
                    .methodResponseTimeoutSeconds(RESPONSE_TIMEOUT)
                    .methodConnectTimeoutSeconds(CONNECTION_TIMEOUT)
                    .build();

            if (testInstance.identity instanceof TestModuleIdentity)
            {
                testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), ((TestModuleIdentity) testInstance.identity).getModuleId(), METHOD_LOOPBACK, options);
            }
            else
            {
                testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), METHOD_LOOPBACK, options);
            }

            Assert.fail(buildExceptionMessage("Invoking method on device or module that wasn't online should have thrown an exception", testInstance.identity.getClient()));
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
                testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), "ThisModuleDoesNotExist", METHOD_LOOPBACK);
            }
            else
            {
                testInstance.methodServiceClient.invoke("ThisDeviceDoesNotExist", METHOD_LOOPBACK);
            }

            Assert.fail(buildExceptionMessage("Invoking method on device or module that doesn't exist should have thrown an exception", testInstance.identity.getClient()));
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
    public void invokeMethodWithPayloadAsNull() throws Exception
    {
        // Direct method with null payload
        super.subscribeToMethodAndReceiveAsDifferentTypes("Null");
        super.invokeHelper(null);
    }

    @Test
    @StandardTierHubOnlyTest
    public void invokeMethodWithPayloadAsPrimitiveType() throws Exception
    {
        // Direct method payload in boolean (one of Primitive types)
        boolean bool = true;
        super.subscribeToMethodAndReceiveAsDifferentTypes("Boolean");
        super.invokeHelper(bool);
    }

    @Test
    @StandardTierHubOnlyTest
    public void invokeMethodWithPayloadAsString() throws Exception
    {
        // Direct method payload in String type
        String string = "This is a valid payload.";
        super.subscribeToMethodAndReceiveAsDifferentTypes(string.getClass().getSimpleName());
        super.invokeHelper(string);
    }

    @Test
    @StandardTierHubOnlyTest
    public void invokeMethodWithPayloadAsArray() throws Exception
    {
        // Direct method payload in Array
        byte[] bytes = new byte[]{1, 1, 1};
        super.subscribeToMethodAndReceiveAsDifferentTypes("Array");
        super.invokeHelper(bytes);
    }

    @Test
    @StandardTierHubOnlyTest
    public void invokeMethodWithPayloadAsList() throws Exception
    {
        // Direct method payload in List type
        List<Double> list = new ArrayList<>();
        list.add(1.0);
        list.add(1.0);
        list.add(1.0);
        super.subscribeToMethodAndReceiveAsDifferentTypes(list.getClass().getSimpleName());
        super.invokeHelper(list);
    }

    @Test
    @StandardTierHubOnlyTest
    public void invokeMethodWithPayloadAsMap() throws Exception
    {
        // Direct method payload in Map type
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");

        super.subscribeToMethodAndReceiveAsDifferentTypes(map.getClass().getSimpleName());
        super.invokeHelper(map);
    }

    @Test
    @StandardTierHubOnlyTest
    public void invokeMethodWithPayloadAsCustomObject() throws Exception
    {
        // Direct method payload in Custom type
        CustomObject customObject = new CustomObject("some test message", 1, true, new NestedCustomObject("some nested test message", 2));
        super.subscribeToMethodAndReceiveAsDifferentTypes(customObject.getClass().getSimpleName());
        super.invokeHelper(customObject);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void subscribeToDirectMethodsOverwritesPreviousCallbacks() throws Exception
    {
        this.testInstance.setup();
        AtomicBoolean oldCallbackCalled = new AtomicBoolean(false);
        AtomicBoolean newCallbackCalled = new AtomicBoolean(false);

        this.testInstance.identity.getClient().open(true);

        // set the initial callback
        this.testInstance.identity.getClient().subscribeToMethods(
            (methodName, methodPayload, context) ->
            {
                oldCallbackCalled.set(true);
                return new com.microsoft.azure.sdk.iot.device.twin.DirectMethodResponse(200, null);
            },
            null);

        // set the new callback that should overwrite the previous callback
        this.testInstance.identity.getClient().subscribeToMethods(
            (methodName, methodPayload, context) ->
            {
                newCallbackCalled.set(true);
                return new com.microsoft.azure.sdk.iot.device.twin.DirectMethodResponse(200, null);
            },
            null);

        // invoke a method so that the device/module method callback will execute
        if (testInstance.identity instanceof TestModuleIdentity)
        {
            testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(),
                ((TestModuleIdentity)testInstance.identity).getModule().getId(), METHOD_MODIFY);
        }
        else
        {
            testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), METHOD_MODIFY);
        }

        assertFalse("Old callback should not have been called since it was overwritten.", oldCallbackCalled.get());
        assertTrue("New callback should have been called.", newCallbackCalled.get());
    }
}
