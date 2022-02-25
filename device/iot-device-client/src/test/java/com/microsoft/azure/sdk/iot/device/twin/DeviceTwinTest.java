// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.twin;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import mockit.*;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.junit.Assert.*;

/* Unit tests for DirectMethod
 * 100% methods covered
 * 98% lines covered
 */
public class DeviceTwinTest
{
    @Mocked
    InternalClient mockedInternalClient;

    @Mocked
    DeviceClientConfig mockedConfig;

    @Mocked
    IotHubEventCallback mockedStatusCB;

    @Mocked
    PropertyCallback mockedGenericPropertyCB;

    @Mocked
    TwinPropertyCallback mockedGenericTwinPropertyCB;

    /*
     **Tests_SRS_DEVICETWIN_25_003: [**The constructor shall save all the parameters specified i.e client, config, deviceTwinCallback.**]**
     **Tests_SRS_DEVICETWIN_21_004: [**The constructor shall save the generic property callback.**]**
     */
    @Test
    public void contructorWithPropertyCallbackSetAllPrivateMembersCorrectly()
    {
        // arrange - act
        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericPropertyCB, null);

        // assert
        assertEquals(mockedGenericPropertyCB, Deencapsulation.getField(testTwin, "deviceTwinGenericPropertyChangeCallback"));
        assertEquals(mockedStatusCB, Deencapsulation.getField(testTwin, "deviceTwinStatusCallback"));
        assertEquals(mockedConfig, Deencapsulation.getField(testTwin, "config"));
    }

    @Test
    public void contructorWithTwinPropertyCallbackSetAllPrivateMembersCorrectly()
    {
        // arrange - act
        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericTwinPropertyCB, null);

        // assert
        assertEquals(mockedGenericTwinPropertyCB, Deencapsulation.getField(testTwin, "deviceTwinGenericTwinPropertyChangeCallback"));
        assertEquals(mockedStatusCB, Deencapsulation.getField(testTwin, "deviceTwinStatusCallback"));
        assertEquals(mockedConfig, Deencapsulation.getField(testTwin, "config"));
    }

    /*
     **Tests_SRS_DEVICETWIN_25_001: [**The constructor shall throw InvalidParameter Exception if any of the parameters i.e client, config, deviceTwinCallback, genericPropertyCallback are null. **]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void contructorThrowsExceptionIfClientIsNull()
    {
        DeviceTwin testTwin = new DeviceTwin(null,
                mockedStatusCB, null, mockedGenericPropertyCB, null);

    }

    /*
     **Tests_SRS_DEVICETWIN_25_002: [**The constructor shall save the device twin message callback by calling setDeviceTwinMessageCallback where any further messages for device twin shall be delivered.**]**
     */
    @Test
    public void contructorSetsDTMessageResponseCB()
    {
        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericPropertyCB, null);

        new Verifications()
        {
            {
                mockedConfig.setDeviceTwinMessageCallback((MessageCallback)any, any);
                times = 1;
            }
        };
    }

    /*
     **Tests_SRS_DEVICETWIN_25_005: [**The method shall create a device twin message with empty payload to be sent IotHub.**]**
     **Tests_SRS_DEVICETWIN_25_007: [**This method shall set the request id for the message by calling setRequestId .**]**
     **Tests_SRS_DEVICETWIN_25_008: [**This method shall send the message to the lower transport layers by calling sendEventAsync.**]**
     */
    @Test
    public void getDeviceTwinSucceeds(@Mocked final IotHubTransportMessage mockedDeviceTwinMessage)
    {
        //arrange
        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        final byte[] body = {};

        new NonStrictExpectations()
        {
            {
                new IotHubTransportMessage(body, MessageType.DEVICE_TWIN);
                result = mockedDeviceTwinMessage;
            }
        };

        //act
        testTwin.getDeviceTwinAsync();

        //assert
        new Verifications()
        {
            {
                mockedDeviceTwinMessage.setRequestId(anyString);
                times = 1;
                mockedDeviceTwinMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_GET_REQUEST);
                times = 1;
                mockedDeviceTwinMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST);
                times = 1;
                mockedInternalClient.sendEventAsync(mockedDeviceTwinMessage, (IotHubEventCallback)any , null);
                times = 2;
            }
        };
    }

    /*
     **Tests_SRS_DEVICETWIN_25_006: [**This method shall set the message type as DEVICE_OPERATION_TWIN_GET_REQUEST by calling setDeviceOperationType.**]**
     */
    @Test
    public void getDeviceTwinSetsDeviceTwinOperation(@Mocked final IotHubTransportMessage mockedDeviceTwinMessage)
    {
        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        final byte[] body = {};

        new NonStrictExpectations()
        {
            {
                new IotHubTransportMessage(body, MessageType.DEVICE_TWIN);
                result = mockedDeviceTwinMessage;
            }
        };

        testTwin.getDeviceTwinAsync();

        new Verifications()
        {
            {
                mockedDeviceTwinMessage.setRequestId(anyString);
                times = 1;
                mockedDeviceTwinMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_GET_REQUEST);
                times = 1;
                mockedDeviceTwinMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST);
                times = 1;
                mockedInternalClient.sendEventAsync(mockedDeviceTwinMessage, (IotHubEventCallback)any , null);
                times = 2;
            }
        };

    }

    /*
     **Tests_SRS_DEVICETWIN_25_005: [**The method shall create a device twin message with empty payload to be sent IotHub.**]**
     */
    @Test
    public void getDeviceTwinSetsEmptyPayload(@Mocked final IotHubTransportMessage mockedDeviceTwinMessage)
    {
        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        final byte[] body = {};

        new NonStrictExpectations()
        {
            {
                new IotHubTransportMessage(body, MessageType.DEVICE_TWIN);
                result = mockedDeviceTwinMessage;
            }
        };

        testTwin.getDeviceTwinAsync();

        new Verifications()
        {
            {
                mockedDeviceTwinMessage.setRequestId(anyString);
                times = 1;
                mockedDeviceTwinMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_GET_REQUEST);
                times = 1;
                mockedDeviceTwinMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST);
                times = 1;
                mockedInternalClient.sendEventAsync(mockedDeviceTwinMessage, (IotHubEventCallback)any , null);
                times = 2;
            }
        };

    }

    @Test
    public void getDeviceTwinRequestCompleteTriggersStatusCB()
    {
        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericPropertyCB, null);

        IotHubEventCallback deviceTwinRequestMessageCallback = Deencapsulation.newInnerInstance("deviceTwinRequestMessageCallback", testTwin);

        deviceTwinRequestMessageCallback.execute(IotHubStatusCode.ERROR, null);

        new Verifications()
        {
            {
                mockedStatusCB.execute(IotHubStatusCode.ERROR, null);
                times = 1;
            }
        };
    }

    /*
     **Tests_SRS_DEVICETWIN_25_030: [**If the message is of type DEVICE_TWIN and DEVICE_OPERATION_TWIN_GET_RESPONSE then the payload is deserialized by calling updateTwin only if the status is ok.**]**
     */
    @Test
    public void getDeviceTwinResponseCallsUpdateTwinIfStatusOk(
            @Mocked final TwinState mockedTwinState,
            @Mocked final TwinCollection mockedTwinCollection)
    {
        //arrange
        final byte[] body = {};
        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        MessageCallback deviceTwinResponseMessageCallback = Deencapsulation.newInnerInstance("deviceTwinResponseMessageCallback", testTwin);

        final IotHubTransportMessage testMessage = new IotHubTransportMessage(body, MessageType.DEVICE_TWIN);
        testMessage.setStatus(String.valueOf(200));
        testMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_GET_RESPONSE);

        new NonStrictExpectations()
        {
            {
                TwinState.createFromPropertiesJson(new String(body,  Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));
                result = mockedTwinState;
                times = 1;
                mockedTwinState.getDesiredProperty();
                result = mockedTwinCollection;
            }
        };

        //act
        deviceTwinResponseMessageCallback.execute(testMessage, null);

        //assert
        final IotHubEventCallback actualStatusCB = Deencapsulation.getField(testTwin, "deviceTwinStatusCallback");
        new Verifications()
        {
            {
                actualStatusCB.execute(IotHubStatusCode.OK, withAny(new Object()));
                times = 1;
            }
        };
    }

    /*
     **Tests_SRS_DEVICETWIN_25_029: [**If the message is of type DEVICE_TWIN and DEVICE_OPERATION_TWIN_GET_RESPONSE then the user call with a valid status is triggered.**]**
     */
    @Test
    public void getDeviceTwinResponseDoesNotCallUpdateTwinIfStatusNotOk()
    {
        //arrange
        final byte[] body = {};
        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        MessageCallback deviceTwinResponseMessageCallback = Deencapsulation.newInnerInstance("deviceTwinResponseMessageCallback", testTwin);

        final IotHubTransportMessage testMessage = new IotHubTransportMessage(body, MessageType.DEVICE_TWIN);
        testMessage.setStatus(String.valueOf(401));
        testMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_GET_RESPONSE);

        //act
        deviceTwinResponseMessageCallback.execute(testMessage, null);

        //assert
        final IotHubEventCallback actualStatusCB = Deencapsulation.getField(testTwin, "deviceTwinStatusCallback");
        new Verifications()
        {
            {
                actualStatusCB.execute(IotHubStatusCode.UNAUTHORIZED, withAny(new Object()));
                times = 1;
            }
        };
    }

    /*
     **Tests_SRS_DEVICETWIN_25_031: [**If the message is of type DEVICE_TWIN and DEVICE_OPERATION_TWIN_GET_RESPONSE and if the status is null then the user is notified on the status callback registered by the user as ERROR.**]**
     */
    @Test
    public void getDeviceTwinResponseCallStusCBWithERRORIfStatusNull()
    {
        //arrange
        final byte[] body = {};
        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        MessageCallback deviceTwinResponseMessageCallback = Deencapsulation.newInnerInstance("deviceTwinResponseMessageCallback", testTwin);

        final IotHubTransportMessage testMessage = new IotHubTransportMessage(body, MessageType.DEVICE_TWIN);
        testMessage.setStatus(null);
        testMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_GET_RESPONSE);

        //act
        deviceTwinResponseMessageCallback.execute(testMessage, null);

        //assert
        final IotHubEventCallback actualStatusCB = Deencapsulation.getField(testTwin, "deviceTwinStatusCallback");
        new Verifications()
        {
            {
                actualStatusCB.execute(IotHubStatusCode.ERROR, withAny(new Object()));
                times = 1;
            }
        };
    }

    /*
     **Tests_SRS_DEVICETWIN_25_009: [**The method shall throw InvalidParameter Exception if reportedProperties is null.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void updateReportedPropThrowsExceptionPropIsNull() throws IOException
    {
        // arrange
        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericPropertyCB, null);

        // act - assert
        testTwin.updateReportedPropertiesAsync(null);
    }

    /*
     **Tests_SRS_DEVICETWIN_25_011: [**The method shall serialize the properties using the TwinCollection.**]**
     **Tests_SRS_DEVICETWIN_25_012: [**The method shall create a device twin message with the serialized payload if not null to be sent IotHub and shall include the connection device id of the sending device.**]**
     **Tests_SRS_DEVICETWIN_25_013: [**This method shall set the message type as DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST by calling setDeviceOperationType.**]**
     **Tests_SRS_DEVICETWIN_25_014: [**This method shall set the request id for the message by calling setRequestId .**]**
     **Tests_SRS_DEVICETWIN_25_015: [**This method shall send the message to the lower transport layers by calling sendEventAsync.**]**
     */
    @Test
    public void updateReportedPropCallsTwinAPIForSerialization(
            @Mocked final IotHubTransportMessage mockedDeviceTwinMessage) throws IOException
    {
        // arrange
        final String prop1 = "prop1";
        final String prop2 = "prop2";
        final String val1 = "val1";
        final int val2 = 100;
        final String expectedDeviceId = "1234";

        final HashSet<Property> reportedProp = new HashSet<Property>()
        {
            {
                add(new Property(prop1, val1));
                add(new Property(prop2, val2));
            }
        };

        final String json = "{\"" + prop2 + "\":" + val2 + ",\"" + prop1 + "\":\"" + val1 + "\"}";

        new NonStrictExpectations()
        {
            {
                mockedConfig.getDeviceId();
                result = expectedDeviceId;

                new IotHubTransportMessage(json.getBytes(StandardCharsets.UTF_8), MessageType.DEVICE_TWIN);
                result = mockedDeviceTwinMessage;
            }
        };
        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericPropertyCB, null);

        // act
        testTwin.updateReportedPropertiesAsync(reportedProp);

        // assert
        new Verifications()
        {
            {
                mockedDeviceTwinMessage.setRequestId(anyString);
                times = 1;
                mockedDeviceTwinMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST);
                times = 1;
                mockedDeviceTwinMessage.setVersion((String)any);
                times = 0;
                mockedInternalClient.sendEventAsync(mockedDeviceTwinMessage, (IotHubEventCallback)any , null);
                times = 1;
                mockedDeviceTwinMessage.setConnectionDeviceId(expectedDeviceId);
                times = 1;
            }
        };
    }

    //Tests_SRS_DEVICETWIN_34_032: [If the provided set of properties contains two keys with the same name, this function shall throw an IOException.]                 
    @Test (expected = IOException.class)
    public void updateReportedPropThrowsForDuplicateKeys(
            @Mocked final IotHubTransportMessage mockedDeviceTwinMessage) throws IOException
    {
        // arrange
        final String prop1 = "prop1";
        final String prop2 = "prop1";
        final String val1 = "val1";
        final int val2 = 100;

        final HashSet<Property> reportedProp = new HashSet<Property>()
        {
            {
                add(new Property(prop1, val1));
                add(new Property(prop2, val2));
            }
        };

        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericPropertyCB, null);

        // act
        testTwin.updateReportedPropertiesAsync(reportedProp);
    }

    /*
     **Tests_SRS_DEVICETWIN_21_024: [**If the version is provided, this method shall set the version for the message by calling setVersion .**]**
     */
    @Test
    public void updateReportedPropWithVersionCallsTwinAPIForSerialization(
            @Mocked final IotHubTransportMessage mockedDeviceTwinMessage) throws IOException
    {
        // arrange
        final String prop1 = "prop1";
        final String prop2 = "prop2";
        final String val1 = "val1";
        final int val2 = 100;

        final HashSet<Property> reportedProp = new HashSet<Property>()
        {
            {
                add(new Property(prop1, val1));
                add(new Property(prop2, val2));
            }
        };

        final String json = "{\"" + prop2 + "\":" + val2 + ",\"" + prop1 + "\":\"" + val1 + "\"}";

        new NonStrictExpectations()
        {
            {
                new IotHubTransportMessage(json.getBytes(StandardCharsets.UTF_8), MessageType.DEVICE_TWIN);
                result = mockedDeviceTwinMessage;
            }
        };
        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericPropertyCB, null);

        // act
        testTwin.updateReportedPropertiesAsync(reportedProp, 10);

        // assert
        new Verifications()
        {
            {
                mockedDeviceTwinMessage.setRequestId(anyString);
                times = 1;
                mockedDeviceTwinMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST);
                times = 1;
                mockedDeviceTwinMessage.setVersion("10");
                times = 1;
                mockedInternalClient.sendEventAsync(mockedDeviceTwinMessage, (IotHubEventCallback)any , null);
                times = 1;
            }
        };
    }

    @Test
    public void updateReportedPropWithVersionNullCallsTwinAPIForSerialization(
            @Mocked final IotHubTransportMessage mockedDeviceTwinMessage) throws IOException
    {
        // arrange
        final String prop1 = "prop1";
        final String prop2 = "prop2";
        final String val1 = "val1";
        final int val2 = 100;

        final HashSet<Property> reportedProp = new HashSet<Property>()
        {
            {
                add(new Property(prop1, val1));
                add(new Property(prop2, val2));
            }
        };

        final String json = "{\"" + prop2 + "\":" + val2 + ",\"" + prop1 + "\":\"" + val1 + "\"}";

        new NonStrictExpectations()
        {
            {
                new IotHubTransportMessage(json.getBytes(StandardCharsets.UTF_8), MessageType.DEVICE_TWIN);
                result = mockedDeviceTwinMessage;
            }
        };
        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericPropertyCB, null);

        // act
        testTwin.updateReportedPropertiesAsync(reportedProp, null);

        // assert
        new Verifications()
        {
            {
                mockedDeviceTwinMessage.setRequestId(anyString);
                times = 1;
                mockedDeviceTwinMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST);
                times = 1;
                mockedDeviceTwinMessage.setVersion((String)any);
                times = 0;
                mockedInternalClient.sendEventAsync(mockedDeviceTwinMessage, (IotHubEventCallback)any , null);
                times = 1;
            }
        };
    }

    /*
     **Tests_SRS_DEVICETWIN_25_027: [**If the message is of type DEVICE_TWIN and DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_RESPONSE then the user call with a valid status is triggered.**]**
     */
    @Test
    public void updateReportedPropOnResponseCallsStatusCB()
    {
        //arrange
        final byte[] body = {};
        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        MessageCallback deviceTwinResponseMessageCallback = Deencapsulation.newInnerInstance("deviceTwinResponseMessageCallback", testTwin);

        final IotHubTransportMessage testMessage = new IotHubTransportMessage(body, MessageType.DEVICE_TWIN);
        testMessage.setStatus(String.valueOf(200));
        testMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_RESPONSE);

        //act
        deviceTwinResponseMessageCallback.execute(testMessage, null);

        //assert
        final IotHubEventCallback actualStatusCB = Deencapsulation.getField(testTwin, "deviceTwinStatusCallback");
        new Verifications()
        {
            {
                actualStatusCB.execute(IotHubStatusCode.OK, withAny(new Object()));
                times = 1;
            }
        };
    }

    /*
     **Tests_SRS_DEVICETWIN_25_028: [**If the message is of type DEVICE_TWIN and DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_RESPONSE and if the status is null then the user is notified on the status callback registered by the user as ERROR.**]**
     */
    @Test
    public void updateReportedPropOnResponseCallsStatusCBErrorIfNullStatus()
    {
        //arrange
        final byte[] body = {};
        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        MessageCallback deviceTwinResponseMessageCallback = Deencapsulation.newInnerInstance("deviceTwinResponseMessageCallback", testTwin);

        final IotHubTransportMessage testMessage = new IotHubTransportMessage(body, MessageType.DEVICE_TWIN);
        testMessage.setStatus(null);
        testMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_RESPONSE);

        //act
        deviceTwinResponseMessageCallback.execute(testMessage, null);

        //assert
        final IotHubEventCallback actualStatusCB = Deencapsulation.getField(testTwin, "deviceTwinStatusCallback");
        new Verifications()
        {
            {
                actualStatusCB.execute(IotHubStatusCode.ERROR, withAny(new Object()));
                times = 1;
            }
        };
    }

    @Test
    public void updateReportedPropOnResponseCallsStatusCBErrorIfMessageTypeIsNotDeviceTwin()
    {
        //arrange
        final byte[] body = {};
        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        MessageCallback deviceTwinResponseMessageCallback = Deencapsulation.newInnerInstance("deviceTwinResponseMessageCallback", testTwin);

        final IotHubTransportMessage testMessage = new IotHubTransportMessage(body, MessageType.DEVICE_METHODS);
        testMessage.setStatus(String.valueOf(200));
        testMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_RESPONSE);

        //act
        deviceTwinResponseMessageCallback.execute(testMessage, null);

        //assert
        final IotHubEventCallback actualStatusCB = Deencapsulation.getField(testTwin, "deviceTwinStatusCallback");
        new Verifications()
        {
            {
                actualStatusCB.execute(IotHubStatusCode.ERROR, withAny(new Object()));
                times = 1;
            }
        };
    }

    /*
     **Tests_SRS_DEVICETWIN_25_017: [**The method shall create a treemap to store callbacks for desired property notifications specified in onDesiredPropertyChange.**]**
     **Tests_SRS_DEVICETWIN_25_018: [**If not already subscribed then this method shall create a device twin message with empty payload and set its type as DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST.**]**
     **Tests_SRS_DEVICETWIN_25_019: [**If not already subscribed then this method shall send the message using sendEventAsync.**]**
     */
    @Test
    public void subscribeToDesiredSetsCorrectOperation(@Mocked final IotHubTransportMessage mockedDeviceTwinMessage,
                                                       @Mocked final PropertyCallback<String, Object> mockedDesiredCB)
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                new IotHubTransportMessage(withAny(new byte[0]), MessageType.DEVICE_TWIN);
                result = mockedDeviceTwinMessage;
            }
        };
        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        Map<Property, Pair<PropertyCallback<String, Object>, Object>> desiredMap = new HashMap<>();
        desiredMap.put(new Property("DesiredProp", "DesiredValue"), new Pair<>(mockedDesiredCB, null));

        // act
        testTwin.subscribeDesiredPropertiesNotification(desiredMap);

        // assert
        final ConcurrentSkipListMap<String, Pair<PropertyCallback<String, Object>, Object>> actualMap = Deencapsulation.getField(testTwin, "onDesiredPropertyChangeMap");

        assertNotNull(actualMap);
        assertFalse(actualMap.isEmpty());
        assertTrue(actualMap.containsKey("DesiredProp"));
        assertEquals(actualMap.get("DesiredProp").getKey(), mockedDesiredCB );

        new Verifications()
        {
            {
                mockedDeviceTwinMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST);
                times = 1;
                mockedInternalClient.sendEventAsync(mockedDeviceTwinMessage, (IotHubEventCallback)any , null);
                times = 1;
            }
        };
    }

    @Test
    public void subscribeToDesiredTwinPropertySetsCorrectOperation(@Mocked final IotHubTransportMessage mockedDeviceTwinMessage,
                                                                   @Mocked final TwinPropertyCallback mockedDesiredCB)
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                new IotHubTransportMessage(withAny(new byte[0]), MessageType.DEVICE_TWIN);
                result = mockedDeviceTwinMessage;
            }
        };
        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericTwinPropertyCB, null);
        Map<Property, Pair<TwinPropertyCallback, Object>> desiredMap = new HashMap<>();
        desiredMap.put(new Property("DesiredProp", "DesiredValue"), new Pair<>(mockedDesiredCB, null));

        // act
        testTwin.subscribeDesiredPropertiesTwinPropertyNotification(desiredMap);

        // assert
        final ConcurrentSkipListMap<String, Pair<TwinPropertyCallback, Object>> actualMap = Deencapsulation.getField(testTwin, "onDesiredTwinPropertyChangeMap");

        assertNotNull(actualMap);
        assertFalse(actualMap.isEmpty());
        assertTrue(actualMap.containsKey("DesiredProp"));
        assertEquals(actualMap.get("DesiredProp").getKey(), mockedDesiredCB );

        new Verifications()
        {
            {
                mockedDeviceTwinMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST);
                times = 1;
                mockedInternalClient.sendEventAsync(mockedDeviceTwinMessage, (IotHubEventCallback)any , null);
                times = 1;
            }
        };
    }

    @Test
    public void subscribeToDesiredDoesNotSubscribeIfAlreadySubscribed(
            @Mocked final IotHubTransportMessage mockedDeviceTwinMessage,
            @Mocked final PropertyCallback<String, Object> mockedDesiredCB)
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                new IotHubTransportMessage(withAny(new byte[0]), MessageType.DEVICE_TWIN);
                result = mockedDeviceTwinMessage;
            }
        };
        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        Map<Property, Pair<PropertyCallback<String, Object>, Object>> desiredMap = new HashMap<>();
        desiredMap.put(new Property("DesiredProp1", null), new Pair<>(mockedDesiredCB, null));
        testTwin.subscribeDesiredPropertiesNotification(desiredMap);

        Deencapsulation.setField(testTwin, "isSubscribed", true);

        desiredMap.put(new Property("DesiredProp2", "DesiredValue2"), new Pair<>(mockedDesiredCB, null));

        // act
        testTwin.subscribeDesiredPropertiesNotification(desiredMap);

        // assert
        final ConcurrentSkipListMap<String, Pair<PropertyCallback<String, Object>, Object>> actualMap = Deencapsulation.getField(testTwin, "onDesiredPropertyChangeMap");

        assertNotNull(actualMap);
        assertFalse(actualMap.isEmpty());
        assertTrue(actualMap.containsKey("DesiredProp1"));
        assertTrue(actualMap.containsKey("DesiredProp2"));
        assertEquals(actualMap.get("DesiredProp2").getKey(), mockedDesiredCB );

        new Verifications()
        {
            {
                mockedDeviceTwinMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST);
                times = 1;
                mockedInternalClient.sendEventAsync(mockedDeviceTwinMessage, (IotHubEventCallback)any , null);
                times = 1;
            }
        };
    }

    @Test
    public void subscribeToDesiredTwinPropertyDoesNotSubscribeIfAlreadySubscribed(
            @Mocked final IotHubTransportMessage mockedDeviceTwinMessage,
            @Mocked final TwinPropertyCallback mockedDesiredCB)
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                new IotHubTransportMessage(withAny(new byte[0]), MessageType.DEVICE_TWIN);
                result = mockedDeviceTwinMessage;
            }
        };
        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericTwinPropertyCB, null);
        Map<Property, Pair<TwinPropertyCallback, Object>> desiredMap = new HashMap<>();
        desiredMap.put(new Property("DesiredProp1", null), new Pair<>(mockedDesiredCB, null));
        testTwin.subscribeDesiredPropertiesTwinPropertyNotification(desiredMap);

        Deencapsulation.setField(testTwin, "isSubscribed", true);

        desiredMap.put(new Property("DesiredProp2", "DesiredValue2"), new Pair<>(mockedDesiredCB, null));

        // act
        testTwin.subscribeDesiredPropertiesTwinPropertyNotification(desiredMap);

        // assert
        final ConcurrentSkipListMap<String, Pair<TwinPropertyCallback, Object>> actualMap = Deencapsulation.getField(testTwin, "onDesiredTwinPropertyChangeMap");

        assertNotNull(actualMap);
        assertFalse(actualMap.isEmpty());
        assertTrue(actualMap.containsKey("DesiredProp1"));
        assertTrue(actualMap.containsKey("DesiredProp2"));
        assertEquals(actualMap.get("DesiredProp2").getKey(), mockedDesiredCB );

        new Verifications()
        {
            {
                mockedDeviceTwinMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST);
                times = 1;
                mockedInternalClient.sendEventAsync(mockedDeviceTwinMessage, (IotHubEventCallback)any , null);
                times = 1;
            }
        };

    }

    /*
     **Tests_SRS_DEVICETWIN_25_025: [**On receiving a message from IOTHub with desired property changes, the callback deviceTwinResponseMessageCallback is triggered.**]**
     **Tests_SRS_DEVICETWIN_25_026: [**If the message is of type DEVICE_TWIN and DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE then the payload is deserialize by calling updateDesiredProperty.**]**
     */
    @Test
    public void desiredPropResponseDoesNotCallsUserStatusCBOnNotification(
            @Mocked final TwinState mockedTwinState,
            @Mocked final TwinCollection mockedTwinCollection)
    {
        //arrange
        final byte[] body = {};
        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        MessageCallback deviceTwinResponseMessageCallback = Deencapsulation.newInnerInstance("deviceTwinResponseMessageCallback", testTwin);

        final IotHubTransportMessage testMessage = new IotHubTransportMessage(body, MessageType.DEVICE_TWIN);
        testMessage.setStatus(String.valueOf(200));
        testMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);

        new NonStrictExpectations()
        {
            {
                TwinState.createFromDesiredPropertyJson((String)any);
                result = mockedTwinState;
                times = 1;
                mockedTwinState.getDesiredProperty();
                result = mockedTwinCollection;
                times = 2;
            }
        };

        //act
        deviceTwinResponseMessageCallback.execute(testMessage, null);

        //assert
        final IotHubEventCallback actualStatusCB = Deencapsulation.getField(testTwin, "deviceTwinStatusCallback");
        new Verifications()
        {
            {
                actualStatusCB.execute(IotHubStatusCode.OK, withAny(new Object()));
                times = 0;
            }
        };

    }

    /*
     **Tests_SRS_DEVICETWIN_25_023: [**OnDesiredPropertyChange callback shall look for the user registered call back on the property that changed and if no callback is registered or is null then OnDesiredPropertyChange shall call the user on generic callback providing with the desired property change key and value pair**]**
     */
    @Test
    public void subscribeToDesiredCallsGenericCBOnDesiredChangeIfNoUserCBFound(
            @Mocked final PropertyCallback<String, Object> mockedDesiredCB)
    {
        // arrange
        final String prop1 = "DesiredProp1";
        final String prop2 = "DesiredProp2";
        final String val2 = "DesiredValue2";
        final String json = "{\"" + prop2 + "\":\"" + val2 + "\"}";

        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        MessageCallback deviceTwinResponseMessageCallback = Deencapsulation.newInnerInstance("deviceTwinResponseMessageCallback", testTwin);

        final IotHubTransportMessage testMessage = new IotHubTransportMessage(json.getBytes(StandardCharsets.UTF_8), MessageType.DEVICE_TWIN);
        testMessage.setStatus(String.valueOf(200));
        testMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);

        Map<Property, Pair<PropertyCallback<String, Object>, Object>> desiredMap = new HashMap<>();
        desiredMap.put(new Property(prop1, null), new Pair<>(mockedDesiredCB, null));
        testTwin.subscribeDesiredPropertiesNotification(desiredMap);

        //act
        deviceTwinResponseMessageCallback.execute(testMessage, null);

        //assert
        new Verifications()
        {
            {
                mockedGenericPropertyCB.onPropertyChanged(prop2, val2, null);
                times = 1;
            }
        };
    }

    @Test
    public void subscribeToDesiredTwinPropertyCallsGenericCBOnDesiredChangeIfNoUserCBFound(
            @Mocked final TwinPropertyCallback mockedDesiredCB)
    {
        // arrange
        final String prop1 = "DesiredProp1";
        final String prop2 = "DesiredProp2";
        final String val2 = "DesiredValue2";
        final String json = "{\"" + prop2 + "\":\"" + val2 + "\"}";

        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericTwinPropertyCB, null);
        MessageCallback deviceTwinResponseMessageCallback = Deencapsulation.newInnerInstance("deviceTwinResponseMessageCallback", testTwin);

        final IotHubTransportMessage testMessage = new IotHubTransportMessage(json.getBytes(StandardCharsets.UTF_8), MessageType.DEVICE_TWIN);
        testMessage.setStatus(String.valueOf(200));
        testMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);

        Map<Property, Pair<TwinPropertyCallback, Object>> desiredMap = new HashMap<>();
        desiredMap.put(new Property(prop1, null), new Pair<>(mockedDesiredCB, null));
        testTwin.subscribeDesiredPropertiesTwinPropertyNotification(desiredMap);

        //act
        deviceTwinResponseMessageCallback.execute(testMessage, null);

        //assert
        new Verifications()
        {
            {
                mockedGenericTwinPropertyCB.onPropertyChanged((Property)any, null);
                times = 1;
            }
        };
    }

    /*
     **Test_SRS_DEVICETWIN_25_022: [**OnDesiredPropertyChange callback shall look for the user registered call back on the property that changed provided in desiredPropertyMap and call the user providing the desired property change key and value pair**]**
     */
    @Test
    public void subscribeToDesiredCallsUserCBOnDesiredChangeIfUserCBFound(
            @Mocked final PropertyCallback<String, Object> mockedDesiredCB)
    {
        // arrange
        final String prop1 = "DesiredProp1";
        final String val2 = "DesiredValue2";
        final String json = "{\"" + prop1 + "\":\"" + val2 + "\"}";

        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        MessageCallback deviceTwinResponseMessageCallback = Deencapsulation.newInnerInstance("deviceTwinResponseMessageCallback", testTwin);

        Map<Property, Pair<PropertyCallback<String, Object>, Object>> desiredMap = new HashMap<>();
        desiredMap.put(new Property(prop1, null), new Pair<>(mockedDesiredCB, null));
        testTwin.subscribeDesiredPropertiesNotification(desiredMap);

        final IotHubTransportMessage testMessage = new IotHubTransportMessage(json.getBytes(StandardCharsets.UTF_8), MessageType.DEVICE_TWIN);
        testMessage.setStatus(String.valueOf(200));
        testMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);

        //act
        deviceTwinResponseMessageCallback.execute(testMessage, null);

        //assert
        new Verifications()
        {
            {
                mockedDesiredCB.onPropertyChanged(prop1, val2, null);
                times = 1;
            }
        };

    }

    @Test
    public void subscribeToDesiredTwinPropertyCallsUserCBOnDesiredChangeIfUserCBFound(
            @Mocked final TwinPropertyCallback mockedDesiredCB)
    {
        // arrange
        final String prop1 = "DesiredProp1";
        final String val2 = "DesiredValue2";
        final String json = "{\"" + prop1 + "\":\"" + val2 + "\"}";

        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericTwinPropertyCB, null);
        MessageCallback deviceTwinResponseMessageCallback = Deencapsulation.newInnerInstance("deviceTwinResponseMessageCallback", testTwin);

        Map<Property, Pair<TwinPropertyCallback, Object>> desiredMap = new HashMap<>();
        desiredMap.put(new Property(prop1, null), new Pair<>(mockedDesiredCB, null));
        testTwin.subscribeDesiredPropertiesTwinPropertyNotification(desiredMap);

        final IotHubTransportMessage testMessage = new IotHubTransportMessage(json.getBytes(StandardCharsets.UTF_8), MessageType.DEVICE_TWIN);
        testMessage.setStatus(String.valueOf(200));
        testMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);

        //act
        deviceTwinResponseMessageCallback.execute(testMessage, null);

        //assert
        new Verifications()
        {
            {
                mockedDesiredCB.onPropertyChanged((Property)any, null);
                times = 1;
            }
        };
    }

    @Test
    public void getDeviceTwinResponseWithDesiredPropertiesCallsTwinPropertyCallback(
            @Mocked final Property mockedProperty)
    {
        // arrange
        final String prop1 = "DesiredProp1";
        final String val2 = "DesiredValue2";
        final Integer version = 10;
        final String json = "{\"desired\":{\"" + prop1 + "\":\"" + val2 + "\", \"$version\":" + version + "}}";

        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericTwinPropertyCB, null);
        MessageCallback deviceTwinResponseMessageCallback =
                Deencapsulation.newInnerInstance("deviceTwinResponseMessageCallback", testTwin);

        final IotHubTransportMessage testMessage = new IotHubTransportMessage(json.getBytes(StandardCharsets.UTF_8), MessageType.DEVICE_TWIN);
        testMessage.setStatus(String.valueOf(200));
        testMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_GET_RESPONSE);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(
                        Property.class,
                        new Class[]{String.class, Object.class, Integer.class, boolean.class, Date.class, Integer.class, String.class, String.class},
                        prop1, val2, version, false, null, null, null, null);
                result = mockedProperty;
                times = 1;
            }
        };

        //act
        deviceTwinResponseMessageCallback.execute(testMessage, null);

        //assert
        new Verifications()
        {
            {
                mockedGenericTwinPropertyCB.onPropertyChanged(mockedProperty, null);
                times = 1;
            }
        };
    }

    @Test
    public void getDeviceTwinResponseWithReportedPropertiesCallsTwinPropertyCallback(
            @Mocked final Property mockedProperty)
    {
        // arrange
        final String prop1 = "ReportedProp1";
        final String val2 = "ReportedValue2";
        final Integer version = 10;
        final String json = "{\"reported\":{\"" + prop1 + "\":\"" + val2 + "\", \"$version\":" + version + "}}";

        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericTwinPropertyCB, null);
        MessageCallback deviceTwinResponseMessageCallback =
                Deencapsulation.newInnerInstance("deviceTwinResponseMessageCallback", testTwin);

        final IotHubTransportMessage testMessage = new IotHubTransportMessage(json.getBytes(StandardCharsets.UTF_8), MessageType.DEVICE_TWIN);
        testMessage.setStatus(String.valueOf(200));
        testMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_GET_RESPONSE);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(
                        Property.class,
                        new Class[]{String.class, Object.class, Integer.class, boolean.class, Date.class, Integer.class},
                        prop1, val2, version, true, null, null);
                result = mockedProperty;
                times = 1;
            }
        };

        //act
        deviceTwinResponseMessageCallback.execute(testMessage, null);

        //assert
        new Verifications()
        {
            {
                mockedGenericTwinPropertyCB.onPropertyChanged(mockedProperty, null);
                times = 1;
            }
        };
    }

    @Test
    public void getDeviceTwinResponseWithPropertiesCallsTwinPropertyCallback(
            @Mocked final Property mockedProperty)
    {
        // arrange
        final String reportedProp1 = "ReportedProp1";
        final String reportedVal2 = "ReportedValue2";
        final Integer reportedVersion = 15;
        final String desiredProp1 = "DesiredProp1";
        final String desiredValue2 = "DesiredValue2";
        final Integer desiredVersion = 10;
        final String json =
                "{" +
                    "\"desired\":{\"" + desiredProp1 + "\":\"" + desiredValue2 + "\", \"$version\":" + desiredVersion + "}," +
                    "\"reported\":{\"" + reportedProp1 + "\":\"" + reportedVal2 + "\", \"$version\":" + reportedVersion + "}" +
                "}";

        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericTwinPropertyCB, null);
        MessageCallback deviceTwinResponseMessageCallback =
                Deencapsulation.newInnerInstance("deviceTwinResponseMessageCallback", testTwin);

        final IotHubTransportMessage testMessage = new IotHubTransportMessage(json.getBytes(StandardCharsets.UTF_8), MessageType.DEVICE_TWIN);
        testMessage.setStatus(String.valueOf(200));
        testMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_GET_RESPONSE);

        //act
        deviceTwinResponseMessageCallback.execute(testMessage, null);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(
                        Property.class,
                        new Class[]{String.class, Object.class, Integer.class, boolean.class, Date.class, Integer.class, String.class, String.class},
                        desiredProp1, desiredValue2, desiredVersion,false, null, null, null, null);
                times = 1;
                Deencapsulation.newInstance(
                        Property.class,
                        new Class[]{String.class, Object.class, Integer.class, boolean.class, Date.class, Integer.class},
                        reportedProp1, reportedVal2, reportedVersion, true, null, null);
                times = 1;
                mockedGenericTwinPropertyCB.onPropertyChanged((Property)any, null);
                times = 2;
            }
        };
    }

    @Test
    public void getDeviceTwinResponseWithPropertiesWithMetadataCallsTwinPropertyCallback(
            @Mocked final Property mockedProperty, @Mocked final ParserUtility mockedParserUtility, @Mocked final Date mockedData)
    {
        // arrange
        final String reportedProp1 = "ReportedProp1";
        final String reportedVal2 = "ReportedValue2";
        final Integer reportedVersion = 15;

        final String desiredProp1 = "DesiredProp1";
        final String desiredValue2 = "DesiredValue2";
        final Integer desiredVersion = 10;

        final String lastUpdated = "2016-06-01T21:22:43.123Z";
        final Date lastUpdatedDate = new Date(1464816163123L);
        final Integer lastUpdatedVersion = 5;
        final String lastUpdatedBy = "testConfig";
        final String lastUpdatedByDigest = "12345";

        final String json =
                "{\n" +
                    "\"desired\": {\n" +
                        "\"" + desiredProp1 + "\":\"" + desiredValue2 + "\", \n" +
                        "\"$version\":" + desiredVersion + ",\n" +
                        "\"$metadata\":{\n" +
                            "\"" + desiredProp1 + "\":{\n" +
                                "\"$lastUpdated\":\"" + lastUpdated + "\",\n" +
                                "\"$lastUpdatedVersion\":" + lastUpdatedVersion + ",\n" +
                                "\"$lastUpdatedBy\":\"" + lastUpdatedBy + "\",\n" +
                                "\"$lastUpdatedByDigest\":\"" + lastUpdatedByDigest + "\"\n" +
                            "}\n" +
                        "}\n" +
                    "},\n" +
                    "\"reported\": {\n" +
                        "\"" + reportedProp1 + "\":\"" + reportedVal2 + "\", \n" +
                        "\"$version\":" + reportedVersion + ", \n" +
                        "\"$metadata\":{\n" +
                            "\"" + reportedProp1 + "\":{\n" +
                                "\"$lastUpdated\":\"" + lastUpdated + "\",\n" +
                                "\"$lastUpdatedVersion\":" + lastUpdatedVersion + "\n" +
                            "}\n" +
                        "}\n" +
                    "}\n" +
                "}";

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(ParserUtility.class, "getDateTimeUtc", new Class[] {String.class}, "2016-06-01T21:22:43.123Z");
                result = lastUpdatedDate;
            }
        };

        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericTwinPropertyCB, null);
        MessageCallback deviceTwinResponseMessageCallback =
                Deencapsulation.newInnerInstance("deviceTwinResponseMessageCallback", testTwin);

        final IotHubTransportMessage testMessage = new IotHubTransportMessage(json.getBytes(StandardCharsets.UTF_8), MessageType.DEVICE_TWIN);
        testMessage.setStatus(String.valueOf(200));
        testMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_GET_RESPONSE);

        //act
        deviceTwinResponseMessageCallback.execute(testMessage, null);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(
                        Property.class,
                        new Class[]{String.class, Object.class, Integer.class, boolean.class, Date.class, Integer.class, String.class, String.class},
                        desiredProp1, desiredValue2, desiredVersion, false, (Date) any, lastUpdatedVersion, lastUpdatedBy, lastUpdatedByDigest);
                times = 1;
                Deencapsulation.newInstance(
                        Property.class,
                        new Class[]{String.class, Object.class, Integer.class, boolean.class, Date.class, Integer.class},
                        reportedProp1, reportedVal2, reportedVersion, true, (Date) any, lastUpdatedVersion);
                times = 1;
                mockedGenericTwinPropertyCB.onPropertyChanged((Property)any, null);
                times = 2;
            }
        };
    }

    /*
     **Tests_SRS_DEVICETWIN_25_023: [**OnDesiredPropertyChange callback shall look for the user registered call back on the property that changed and if no callback is registered or is null then OnDesiredPropertyChange shall call the user on generic callback providing with the desired property change key and value pair**]**
     */
    @Test
    public void desiredChangeResponseCallsGenericCBCBWithDesiredChangeIfNullCB()
    {
        // arrange
        final String prop1 = "DesiredProp1";
        final String val2 = "DesiredValue2";
        final String json = "{\"" + prop1 + "\":\"" + val2 + "\"}";

        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        MessageCallback deviceTwinResponseMessageCallback = Deencapsulation.newInnerInstance("deviceTwinResponseMessageCallback", testTwin);

        Map<Property, Pair<PropertyCallback<String, Object>, Object>> desiredMap = new HashMap<>();
        desiredMap.put(new Property(prop1, null), new Pair<>((PropertyCallback<String, Object>) null, null));
        testTwin.subscribeDesiredPropertiesNotification(desiredMap);

        final IotHubTransportMessage testMessage = new IotHubTransportMessage(json.getBytes(StandardCharsets.UTF_8), MessageType.DEVICE_TWIN);
        testMessage.setStatus(String.valueOf(200));
        testMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);

        //act
        deviceTwinResponseMessageCallback.execute(testMessage, null);

        //assert
        new Verifications()
        {
            {
                mockedGenericPropertyCB.onPropertyChanged(prop1, val2, null);
                times = 1;
            }
        };
    }

    @Test
    public void desiredChangeResponseCallsGenericTwinCBCBWithDesiredChangeIfNullTwinCB()
    {
        // arrange
        final String prop1 = "DesiredProp1";
        final String val2 = "DesiredValue2";
        final String json = "{\"" + prop1 + "\":\"" + val2 + "\"}";

        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericTwinPropertyCB, null);
        MessageCallback deviceTwinResponseMessageCallback = Deencapsulation.newInnerInstance("deviceTwinResponseMessageCallback", testTwin);

        Map<Property, Pair<TwinPropertyCallback, Object>> desiredMap = new HashMap<>();
        desiredMap.put(new Property(prop1, null), new Pair<>((TwinPropertyCallback) null, null));
        testTwin.subscribeDesiredPropertiesTwinPropertyNotification(desiredMap);

        final IotHubTransportMessage testMessage = new IotHubTransportMessage(json.getBytes(StandardCharsets.UTF_8), MessageType.DEVICE_TWIN);
        testMessage.setStatus(String.valueOf(200));
        testMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);

        //act
        deviceTwinResponseMessageCallback.execute(testMessage, null);

        //assert
        new Verifications()
        {
            {
                mockedGenericTwinPropertyCB.onPropertyChanged((Property)any, null);
                times = 1;
            }
        };
    }

    @Test
    public void desiredChangeResponseCallsUserGenericCBWithDesiredChangeIfUnsubscribedYet()
    {
        // arrange
        final String prop1 = "DesiredProp1";
        final String val2 = "DesiredValue2";
        final String json = "{\"" + prop1 + "\":\"" + val2 + "\"}";

        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        MessageCallback deviceTwinResponseMessageCallback = Deencapsulation.newInnerInstance("deviceTwinResponseMessageCallback", testTwin);

        final IotHubTransportMessage testMessage = new IotHubTransportMessage(json.getBytes(StandardCharsets.UTF_8), MessageType.DEVICE_TWIN);
        testMessage.setStatus(String.valueOf(200));
        testMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);

        //act
        deviceTwinResponseMessageCallback.execute(testMessage, null);

        //assert
        new Verifications()
        {
            {
                mockedGenericPropertyCB.onPropertyChanged(prop1, val2, null);
                times = 1;
            }
        };
    }

    @Test
    public void desiredChangeResponseCallsUserGenericTwinCBWithDesiredChangeIfUnsubscribedYet()
    {
        // arrange
        final String prop1 = "DesiredProp1";
        final String val2 = "DesiredValue2";
        final String json = "{\"" + prop1 + "\":\"" + val2 + "\"}";

        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, mockedGenericTwinPropertyCB, null);
        MessageCallback deviceTwinResponseMessageCallback = Deencapsulation.newInnerInstance("deviceTwinResponseMessageCallback", testTwin);

        final IotHubTransportMessage testMessage = new IotHubTransportMessage(json.getBytes(StandardCharsets.UTF_8), MessageType.DEVICE_TWIN);
        testMessage.setStatus(String.valueOf(200));
        testMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);

        //act
        deviceTwinResponseMessageCallback.execute(testMessage, null);

        //assert
        new Verifications()
        {
            {
                mockedGenericTwinPropertyCB.onPropertyChanged((Property)any, null);
                times = 1;
            }
        };
    }

    @Test
    public void desiredChangePropertyWithNoCallback()
    {
        // arrange
        final String prop1 = "DesiredProp1";
        final String val2 = "DesiredValue2";
        final String json = "{\"" + prop1 + "\":\"" + val2 + "\"}";

        DeviceTwin testTwin = new DeviceTwin(mockedInternalClient,
                mockedStatusCB, null, (TwinPropertyCallback)null, null);
        MessageCallback deviceTwinResponseMessageCallback = Deencapsulation.newInnerInstance("deviceTwinResponseMessageCallback", testTwin);

        final IotHubTransportMessage testMessage = new IotHubTransportMessage(json.getBytes(StandardCharsets.UTF_8), MessageType.DEVICE_TWIN);
        testMessage.setStatus(String.valueOf(200));
        testMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);

        // act
        deviceTwinResponseMessageCallback.execute(testMessage, null);

        // assert
    }
}
