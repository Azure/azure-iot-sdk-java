// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.twin;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;

import static com.microsoft.azure.sdk.iot.device.twin.DeviceOperations.DEVICE_OPERATION_METHOD_RECEIVE_REQUEST;
import static com.microsoft.azure.sdk.iot.device.twin.DeviceOperations.DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST;
import java.nio.charset.StandardCharsets;
import static com.microsoft.azure.sdk.iot.device.MessageType.DEVICE_METHODS;
import static org.junit.Assert.*;

/* Unit tests for DirectMethod
* 100% methods covered
* 93% lines covered
*/
public class DirectMethodTest
{
    @Mocked
    InternalClient mockedInternalClient;

    @Mocked
    DeviceClientConfig mockedConfig;

    @Mocked
    IotHubEventCallback mockedStatusCB;

    @Mocked
    MessageCallback mockeddeviceMethodResponseCB;

    @Mocked
    MethodCallback mockedDeviceMethodCB;

    /*
    **Tests_SRS_DEVICEMETHOD_25_002: [**The constructor shall save the device method messages callback callback, by calling setDirectMethodsMessageCallback, where any further messages for device method shall be delivered.**]**
    **Tests_SRS_DEVICEMETHOD_25_003: [**The constructor shall save all the parameters specified i.e client, config, deviceMethodStatusCallback, deviceMethodStatusCallbackContext.**]**
     */
    @Test
    public void constructorSavesConfigSucceeds() throws IllegalArgumentException
    {
        //arrange

        //act
        DirectMethod testMethod = new DirectMethod(mockedInternalClient, mockedStatusCB, null);

        //assert
        new Verifications()
        {
            {
                mockedConfig.setDirectMethodsMessageCallback((MessageCallback)any, any);
                times = 1;
            }
        };

        InternalClient testClient = Deencapsulation.getField(testMethod, "client");
        DeviceClientConfig testConfig = Deencapsulation.getField(testMethod, "config");
        IotHubEventCallback testStatusCallback = Deencapsulation.getField(testMethod, "deviceMethodStatusCallback");

        assertNotNull(testClient);
        assertEquals(testClient, mockedInternalClient);
        assertNotNull(testConfig);
        assertEquals(testConfig, mockedConfig);
        assertNotNull(testStatusCallback);
        assertEquals(testStatusCallback, mockedStatusCB);

    }

    /*
    **Tests_SRS_DEVICEMETHOD_25_001: [**The constructor shall throw IllegalArgument Exception if any of the parameters i.e client, config, deviceMethodStatusCallback are null. **]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsIfClientNull() throws IllegalArgumentException
    {
        //act
        DirectMethod testMethod = new DirectMethod(null, mockedStatusCB, null);

    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsIfCallbackNull() throws IllegalArgumentException
    {
        //act
        DirectMethod testMethod = new DirectMethod(mockedInternalClient, null, null);
    }

    /*
     **Tests_SRS_DEVICEMETHOD_25_005: [**If not already subscribed then this method shall create a device method message with empty payload and set its type as DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST, and set it's connection id to the sending device's id.**]**
     **Tests_SRS_DEVICEMETHOD_25_006: [**If not already subscribed then this method shall send the message using sendEventAsync.**]**
     */
    @Test
    public void subscribeToMethodsSucceeds(@Mocked final IotHubTransportMessage mockedMessage) throws IllegalArgumentException
    {
        //arrange
        DirectMethod testMethod = new DirectMethod(mockedInternalClient, mockedStatusCB, null);
        final String expectedDeviceId = "1234";
        new NonStrictExpectations()
        {
            {
                new IotHubTransportMessage((byte[]) any, DEVICE_METHODS);
                result = mockedMessage;

                mockedConfig.getDeviceId();
                result = expectedDeviceId;
            }
        };

        //act
        testMethod.subscribeToDirectMethods(mockedDeviceMethodCB, null);

        //assert
        new Verifications()
        {
            {
                mockedMessage.setConnectionDeviceId(expectedDeviceId);
                mockedMessage.setDeviceOperationType(DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST);
                times = 1;
                mockedInternalClient.sendEventAsync((Message)any, (IotHubEventCallback)any, null);
                times = 1;
            }
        };
    }

    /*
    **Tests_SRS_DEVICEMETHOD_25_004: [**If deviceMethodCallback parameter is null then this method shall throw IllegalArgumentException**]**
     */
   @Test (expected = IllegalArgumentException.class)
    public void subscribeToMethodsThrowsIfCallbackNull() throws IllegalArgumentException
    {
        //arrange
        DirectMethod testMethod = new DirectMethod(mockedInternalClient, mockedStatusCB, null);

        //act
        testMethod.subscribeToDirectMethods(null, null);

    }

    @Test
    public void subscribeToMethodsDoesNotSubscribeIfAlreadySubscribed(@Mocked final IotHubTransportMessage mockedMessage) throws IllegalArgumentException
    {
        //arrange
        DirectMethod testMethod = new DirectMethod(mockedInternalClient, mockedStatusCB, null);

        testMethod.subscribeToDirectMethods(mockedDeviceMethodCB, null);

        new NonStrictExpectations()
        {
            {
                new IotHubTransportMessage((byte[]) any, DEVICE_METHODS);
                result = mockedMessage;
            }
        };

        //act
        testMethod.subscribeToDirectMethods(mockedDeviceMethodCB, null);

        //assert
        new Verifications()
        {
            {
                mockedMessage.setDeviceOperationType(DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST);
                maxTimes = 1;
                mockedInternalClient.sendEventAsync((Message)any, (IotHubEventCallback)any, null);
                maxTimes = 1;
            }
        };
    }

    /*
    **Tests_SRS_DEVICEMETHOD_25_007: [**On receiving a message from IOTHub with for method invoke, the callback DeviceMethodResponseMessageCallback is triggered.**]**
    **Tests_SRS_DEVICEMETHOD_25_008: [**If the message is of type DirectMethod and DEVICE_OPERATION_METHOD_RECEIVE_REQUEST then user registered device method callback gets invoked providing the user with method name and payload along with the user context. **]**
    **Tests_SRS_DEVICEMETHOD_25_010: [**User is expected to provide response message and status upon invoking the device method callback.**]**
    **Tests_SRS_DEVICEMETHOD_25_011: [**If the user callback is successful and user has successfully provided the response message and status, then this method shall build a device method message of type DEVICE_OPERATION_METHOD_SEND_RESPONSE, serilize the user data by invoking Method from serializer and save the user data as payload in the message before sending it to IotHub via sendeventAsync before marking the result as complete**]**
    **Tests_SRS_DEVICEMETHOD_25_012: [**The device method message sent to IotHub shall have same the request id as the invoking message.**]**
    **Tests_SRS_DEVICEMETHOD_34_016: [The device method message sent to IotHub shall have the sending device's id set as the connection device id.]
    **Tests_SRS_DEVICEMETHOD_25_013: [**The device method message sent to IotHub shall have the status provided by the user as the message status.**]**
     */
    @Test
    public void deviceMethodResponseCallbackSucceeds(final @Mocked InternalClient mockedInternalClient, final @Mocked IotHubTransportMessage mockedTransportMessage) throws IllegalArgumentException
    {
        //arrange
        final String expectedDeviceId = "2345";
        DirectMethod testMethod = new DirectMethod(mockedInternalClient, mockedStatusCB, null);
        testMethod.subscribeToDirectMethods(mockedDeviceMethodCB, null);

        final DirectMethodResponse testUserData = new DirectMethodResponse(100, "Some test message");

        MessageCallback testDeviceMethodResponseMessageCallback = Deencapsulation.newInnerInstance("deviceMethodResponseCallback", testMethod);
        new NonStrictExpectations()
        {
            {
                mockedDeviceMethodCB.call(anyString, any, any);
                result = testUserData;

                new IotHubTransportMessage((byte[]) any, DEVICE_METHODS);
                result = mockedTransportMessage;

                mockedTransportMessage.getDeviceOperationType();
                result = DEVICE_OPERATION_METHOD_RECEIVE_REQUEST;

                mockedConfig.getDeviceId();
                result = expectedDeviceId;

                mockedTransportMessage.getMessageType();
                result = DEVICE_METHODS;
            }
        };

        //act
        IotHubMessageResult result =  testDeviceMethodResponseMessageCallback.execute(mockedTransportMessage, null);

        //assert
        new Verifications()
        {
            {
                mockedTransportMessage.setConnectionDeviceId(expectedDeviceId);
                times = 1;

                mockedInternalClient.sendEventAsync((Message)any, (IotHubEventCallback)any, null);
                maxTimes = 1;
            }
        };

        assertSame(result, IotHubMessageResult.COMPLETE);
    }

    /*
    **Tests_SRS_DEVICEMETHOD_25_009: [**If the received message is not of type DirectMethod and DEVICE_OPERATION_METHOD_RECEIVE_REQUEST then user shall be notified on the status callback registered by the user as ERROR before marking the status of the sent message as Abandon **]**
     */
    @Test
    public void deviceMethodResponseCallbackAbandonsOnIncorrectMessage() throws IllegalArgumentException
    {
        //arrange
        DirectMethod testMethod = new DirectMethod(mockedInternalClient, mockedStatusCB, null);
        testMethod.subscribeToDirectMethods(mockedDeviceMethodCB, null);

        byte[] testPayload = "TestPayload".getBytes(StandardCharsets.UTF_8);
        IotHubTransportMessage testMessage = new IotHubTransportMessage(testPayload, MessageType.DEVICE_TWIN);
        testMessage.setDeviceOperationType(DEVICE_OPERATION_METHOD_RECEIVE_REQUEST);

        final DirectMethodResponse testUserData = new DirectMethodResponse(100, "Some test message");

        MessageCallback testDeviceMethodResponseMessageCallback = Deencapsulation.newInnerInstance("deviceMethodResponseCallback", testMethod);
        new NonStrictExpectations()
        {
            {
                mockedDeviceMethodCB.call(anyString, any, any);
                result = testUserData;
            }
        };

        //act
        IotHubMessageResult result =  testDeviceMethodResponseMessageCallback.execute(testMessage, null);

        //assert
        assertNotSame(result, IotHubMessageResult.COMPLETE);

        new Verifications()
        {
            {
                mockedStatusCB.execute(IotHubStatusCode.ERROR, any);
            }
        };
    }

    /*
    **Tests_SRS_DEVICEMETHOD_25_014: [**If the user invoked callback failed for any reason then the user shall be notified on the status callback registered by the user as ERROR before marking the status of the sent message as Rejected.**]**
     */
    @Test
    public void deviceMethodResponseCallbackSendsResponseOnlyIfNonNull() throws IllegalArgumentException
    {
        //arrange
        DirectMethod testMethod = new DirectMethod(mockedInternalClient, mockedStatusCB, null);
        testMethod.subscribeToDirectMethods(mockedDeviceMethodCB, null);

        byte[] testPayload = "TestPayload".getBytes(StandardCharsets.UTF_8);
        IotHubTransportMessage testMessage = new IotHubTransportMessage(testPayload, DEVICE_METHODS);
        testMessage.setDeviceOperationType(DEVICE_OPERATION_METHOD_RECEIVE_REQUEST);

        final DirectMethodResponse testUserData = null;

        MessageCallback testDeviceMethodResponseMessageCallback = Deencapsulation.newInnerInstance("deviceMethodResponseCallback", testMethod);
        new NonStrictExpectations()
        {
            {
                mockedDeviceMethodCB.call(anyString, any, any);
                result = testUserData;
            }
        };

        //act
        IotHubMessageResult result =  testDeviceMethodResponseMessageCallback.execute(testMessage, null);

        //assert
        assertNotSame(result, IotHubMessageResult.COMPLETE);

        new Verifications()
        {
            {
                mockedStatusCB.execute(IotHubStatusCode.ERROR, any);
                times = 1;
            }
        };

    }

    /*
    **Tests_SRS_DEVICEMETHOD_25_015: [**User can provide null response message upon invoking the device method callback which will be serialized as is, before sending it to IotHub.**]**
     */
    @Test
    public void deviceMethodResponseCallbackSendsResponseMessageEvenIfNonNull() throws IllegalArgumentException
    {
        //arrange
        DirectMethod testMethod = new DirectMethod(mockedInternalClient, mockedStatusCB, null);
        testMethod.subscribeToDirectMethods(mockedDeviceMethodCB, null);

        byte[] testPayload = "TestPayload".getBytes(StandardCharsets.UTF_8);
        IotHubTransportMessage testMessage = new IotHubTransportMessage(testPayload, DEVICE_METHODS);
        testMessage.setDeviceOperationType(DEVICE_OPERATION_METHOD_RECEIVE_REQUEST);

        final DirectMethodResponse testUserData = new DirectMethodResponse(100, null);

        MessageCallback testDeviceMethodResponseMessageCallback = Deencapsulation.newInnerInstance("deviceMethodResponseCallback", testMethod);
        new NonStrictExpectations()
        {
            {
                mockedDeviceMethodCB.call(anyString, any, any);
                result = testUserData;
            }
        };

        //act
        IotHubMessageResult result =  testDeviceMethodResponseMessageCallback.execute(testMessage, null);

        //assert
        assertSame(result, IotHubMessageResult.COMPLETE);

        new Verifications()
        {
            {
                mockedStatusCB.execute(IotHubStatusCode.ERROR, any);
                times = 0;
            }
        };

    }

    @Test
    public void deviceMethodResponseCallbackDoesNotHangOnUserCallbackHang() throws IllegalArgumentException
    {
        //arrange
        DirectMethod testMethod = new DirectMethod(mockedInternalClient, mockedStatusCB, null);
        testMethod.subscribeToDirectMethods(mockedDeviceMethodCB, null);

        byte[] testPayload = "TestPayload".getBytes(StandardCharsets.UTF_8);
        IotHubTransportMessage testMessage = new IotHubTransportMessage(testPayload, DEVICE_METHODS);
        testMessage.setDeviceOperationType(DEVICE_OPERATION_METHOD_RECEIVE_REQUEST);

        MessageCallback testDeviceMethodResponseMessageCallback = Deencapsulation.newInnerInstance("deviceMethodResponseCallback", testMethod);
        new NonStrictExpectations()
        {
            {
                mockedDeviceMethodCB.call(anyString, any, any);
                result = new Exception("Test Exception");
            }
        };

        //act
        IotHubMessageResult result =  testDeviceMethodResponseMessageCallback.execute(testMessage, null);

        //assert
        assertNotSame(result, IotHubMessageResult.COMPLETE);

        new Verifications()
        {
            {
                mockedStatusCB.execute(IotHubStatusCode.ERROR, any);
                times = 1;
            }
        };

    }

    @Test
    public void deviceMethodRequestMessageCallbackExecutes() throws IllegalArgumentException
    {
        //arrange
        DirectMethod testMethod = new DirectMethod(mockedInternalClient, mockedStatusCB, null);

        IotHubEventCallback testDeviceMethodRequestMessageCallback = Deencapsulation.newInnerInstance("deviceMethodRequestMessageCallback", testMethod);

        //act
        testDeviceMethodRequestMessageCallback.execute(IotHubStatusCode.ERROR, null);

        //assert

        new Verifications()
        {
            {
                mockedStatusCB.execute(IotHubStatusCode.ERROR, any);
                times = 1;
            }
        };


    }

}
