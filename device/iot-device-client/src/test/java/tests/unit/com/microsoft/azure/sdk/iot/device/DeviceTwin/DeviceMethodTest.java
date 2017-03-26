// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.DeviceTwin;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.*;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;

import static com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations.DEVICE_OPERATION_METHOD_RECEIVE_REQUEST;
import static com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations.DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST;
import static org.junit.Assert.*;

public class DeviceMethodTest
{
    @Mocked
    DeviceClient mockedClient;

    @Mocked
    DeviceClientConfig mockedConfig;

    @Mocked
    IotHubEventCallback mockedStatusCB;

    @Mocked
    MessageCallback mockeddeviceMethodResponseCB;

    @Mocked
    DeviceMethodCallback mockedDeviceMethodCB;

    /*
    **Tests_SRS_DEVICEMETHOD_25_002: [**The constructor shall save the device method messages callback callback, by calling setDeviceMethodMessageCallback, where any further messages for device method shall be delivered.**]**
    **Tests_SRS_DEVICEMETHOD_25_003: [**The constructor shall save all the parameters specified i.e client, config, deviceMethodStatusCallback, deviceMethodStatusCallbackContext.**]**
     */
    @Test
    public void constructorSavesConfigSucceeds() throws IllegalArgumentException
    {
        //arrange

        //act
        DeviceMethod testMethod = new DeviceMethod(mockedClient, mockedConfig, mockedStatusCB, null);

        //assert
        new Verifications()
        {
            {
                mockedConfig.setDeviceMethodMessageCallback((MessageCallback)any, any);
                times = 1;
            }
        };

        DeviceClient testClient = Deencapsulation.getField(testMethod, "client");
        DeviceClientConfig testConfig = Deencapsulation.getField(testMethod, "config");
        IotHubEventCallback testStatusCallback = Deencapsulation.getField(testMethod, "deviceMethodStatusCallback");

        assertNotNull(testClient);
        assertEquals(testClient, mockedClient);
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
        DeviceMethod testMethod = new DeviceMethod(null, mockedConfig, mockedStatusCB, null);

    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsIfConfigNull() throws IllegalArgumentException
    {
        //act
        DeviceMethod testMethod = new DeviceMethod(mockedClient, null, mockedStatusCB, null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsIfCallBackNull() throws IllegalArgumentException
    {
        //act
        DeviceMethod testMethod = new DeviceMethod(mockedClient, mockedConfig, null, null);

    }

    /*
    **Tests_SRS_DEVICEMETHOD_25_005: [**If not already subscribed then this method shall create a device method message with empty payload and set its type as DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST.**]**
    **Tests_SRS_DEVICEMETHOD_25_006: [**If not already subscribed then this method shall send the message using sendEventAsync.**]**
     */
    @Test
    public void subscribeToMethodsSucceeds(@Mocked final DeviceMethodMessage mockedMessage) throws IllegalArgumentException
    {
        //arrange
        DeviceMethod testMethod = new DeviceMethod(mockedClient, mockedConfig, mockedStatusCB, null);

        new NonStrictExpectations()
        {
            {
                new DeviceMethodMessage((byte[]) any);
                result = mockedMessage;
            }
        };

        //act
        testMethod.subscribeToDeviceMethod(mockedDeviceMethodCB, null);

        //assert
        new Verifications()
        {
            {
                mockedMessage.setDeviceOperationType(DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST);
                times = 1;
                mockedClient.sendEventAsync((Message)any, (IotHubEventCallback)any, null);
                times = 1;
            }
        };
    }

    /*
    **Tests_SRS_DEVICEMETHOD_25_004: [**If deviceMethodCallback parameter is null then this method shall throw IllegalArgumentException**]**
     */
   @Test (expected = IllegalArgumentException.class)
    public void subscribeToMethodsThrowsIfCallBackNull() throws IllegalArgumentException
    {
        //arrange
        DeviceMethod testMethod = new DeviceMethod(mockedClient, mockedConfig, mockedStatusCB, null);

        //act
        testMethod.subscribeToDeviceMethod(null, null);

    }

    @Test
    public void subscribeToMethodsDoesNotSubscribeIfAlreadySubscribed(@Mocked final DeviceMethodMessage mockedMessage) throws IllegalArgumentException
    {
        //arrange
        DeviceMethod testMethod = new DeviceMethod(mockedClient, mockedConfig, mockedStatusCB, null);

        testMethod.subscribeToDeviceMethod(mockedDeviceMethodCB, null);

        new NonStrictExpectations()
        {
            {
                new DeviceMethodMessage((byte[]) any);
                result = mockedMessage;
            }
        };

        //act
        testMethod.subscribeToDeviceMethod(mockedDeviceMethodCB, null);

        //assert
        new Verifications()
        {
            {
                mockedMessage.setDeviceOperationType(DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST);
                maxTimes = 1;
                mockedClient.sendEventAsync((Message)any, (IotHubEventCallback)any, null);
                maxTimes = 1;
            }
        };
    }

    /*
    **Tests_SRS_DEVICEMETHOD_25_007: [**On receiving a message from IOTHub with for method invoke, the callback DeviceMethodResponseMessageCallback is triggered.**]**
    **Tests_SRS_DEVICEMETHOD_25_008: [**If the message is of type DeviceMethod and DEVICE_OPERATION_METHOD_RECEIVE_REQUEST then user registered device method callback gets invoked providing the user with method name and payload along with the user context. **]**
    **Tests_SRS_DEVICEMETHOD_25_010: [**User is expected to provide response message and status upon invoking the device method callback.**]**
    **Tests_SRS_DEVICEMETHOD_25_011: [**If the user callback is successful and user has successfully provided the response message and status, then this method shall build a device method message of type DEVICE_OPERATION_METHOD_SEND_RESPONSE, serilize the user data by invoking Method from serializer and save the user data as payload in the message before sending it to IotHub via sendeventAsync before marking the result as complete**]**
    **Tests_SRS_DEVICEMETHOD_25_012: [**The device method message sent to IotHub shall have same the request id as the invoking message.**]**
    **Tests_SRS_DEVICEMETHOD_25_013: [**The device method message sent to IotHub shall have the status provided by the user as the message status.**]**
     */
    @Test
    public void deviceMethodResponseCallbackSucceeds(final @Mocked DeviceClient mockedClient) throws IllegalArgumentException
    {
        //arrange

        DeviceMethod testMethod = new DeviceMethod(mockedClient, mockedConfig, mockedStatusCB, null);
        testMethod.subscribeToDeviceMethod(mockedDeviceMethodCB, null);

        byte[] testPayload = "TestPayload".getBytes();
        DeviceMethodMessage testMessage = new DeviceMethodMessage(testPayload);
        testMessage.setDeviceOperationType(DEVICE_OPERATION_METHOD_RECEIVE_REQUEST);

        final DeviceMethodData testUserData = new DeviceMethodData(100, "Some test message");

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
        new Verifications()
        {
            {
                mockedClient.sendEventAsync((Message)any, (IotHubEventCallback)any, null);
                maxTimes = 1;
            }
        };

        assertTrue(result == IotHubMessageResult.COMPLETE);
    }

    /*
    **Tests_SRS_DEVICEMETHOD_25_009: [**If the received message is not of type DeviceMethod and DEVICE_OPERATION_METHOD_RECEIVE_REQUEST then user shall be notified on the status callback registered by the user as ERROR before marking the status of the sent message as Abandon **]**
     */
    @Test
    public void deviceMethodResponseCallbackAbandonsOnIncorrectMessage() throws IllegalArgumentException
    {
        //arrange
        DeviceMethod testMethod = new DeviceMethod(mockedClient, mockedConfig, mockedStatusCB, null);
        testMethod.subscribeToDeviceMethod(mockedDeviceMethodCB, null);

        byte[] testPayload = "TestPayload".getBytes();
        DeviceTwinMessage testMessage = new DeviceTwinMessage(testPayload);
        testMessage.setDeviceOperationType(DEVICE_OPERATION_METHOD_RECEIVE_REQUEST);

        final DeviceMethodData testUserData = new DeviceMethodData(100, "Some test message");

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
        assertFalse(result == IotHubMessageResult.COMPLETE);

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
        DeviceMethod testMethod = new DeviceMethod(mockedClient, mockedConfig, mockedStatusCB, null);
        testMethod.subscribeToDeviceMethod(mockedDeviceMethodCB, null);

        byte[] testPayload = "TestPayload".getBytes();
        DeviceMethodMessage testMessage = new DeviceMethodMessage(testPayload);
        testMessage.setDeviceOperationType(DEVICE_OPERATION_METHOD_RECEIVE_REQUEST);

        final DeviceMethodData testUserData = null;

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
        assertFalse(result == IotHubMessageResult.COMPLETE);

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
        DeviceMethod testMethod = new DeviceMethod(mockedClient, mockedConfig, mockedStatusCB, null);
        testMethod.subscribeToDeviceMethod(mockedDeviceMethodCB, null);

        byte[] testPayload = "TestPayload".getBytes();
        DeviceMethodMessage testMessage = new DeviceMethodMessage(testPayload);
        testMessage.setDeviceOperationType(DEVICE_OPERATION_METHOD_RECEIVE_REQUEST);

        final DeviceMethodData testUserData = new DeviceMethodData(100, null);

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
        assertTrue(result == IotHubMessageResult.COMPLETE);

        new Verifications()
        {
            {
                mockedStatusCB.execute(IotHubStatusCode.ERROR, any);
                times = 0;
            }
        };

    }

    @Test
    public void deviceMethodResponseCallbackDoesNotHangOnUserCallBackHang() throws IllegalArgumentException
    {
        //arrange
        DeviceMethod testMethod = new DeviceMethod(mockedClient, mockedConfig, mockedStatusCB, null);
        testMethod.subscribeToDeviceMethod(mockedDeviceMethodCB, null);

        byte[] testPayload = "TestPayload".getBytes();
        DeviceMethodMessage testMessage = new DeviceMethodMessage(testPayload);
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
        assertFalse(result == IotHubMessageResult.COMPLETE);

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
        DeviceMethod testMethod = new DeviceMethod(mockedClient, mockedConfig, mockedStatusCB, null);

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
