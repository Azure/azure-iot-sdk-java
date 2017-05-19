// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.DeviceTwin;

import com.microsoft.azure.sdk.iot.deps.serializer.TwinChangedCallback;
import com.microsoft.azure.sdk.iot.deps.serializer.TwinParser;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.*;
import mockit.*;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.junit.Assert.*;

public class DeviceTwinTest
{
    @Mocked
    DeviceIO mockedDeviceIO;

    @Mocked
    DeviceClientConfig mockedConfig;

    @Mocked
    IotHubEventCallback mockedStatusCB;

    @Mocked
    PropertyCallBack mockedGenericPropertyCB;

    /*
    **Tests_SRS_DEVICETWIN_25_003: [**The constructor shall save all the parameters specified i.e client, config, deviceTwinCallback, genericPropertyCallback.**]**
     */
    @Test
    public void contructorSetAllPrivateMembersCorrectly() throws IOException
    {
        //Arrange

        //Act
        DeviceTwin testTwin = new DeviceTwin(mockedDeviceIO, mockedConfig,
                mockedStatusCB, null, mockedGenericPropertyCB, null);

        //Assert
        TwinParser actualTwinParserObj = Deencapsulation.getField(testTwin, "twinParser");
        DeviceIO actualClient = Deencapsulation.getField(testTwin, "deviceIO");
        DeviceClientConfig actualConfig = Deencapsulation.getField(testTwin, "config");
        IotHubEventCallback actualStatucCB = Deencapsulation.getField(testTwin, "deviceTwinStatusCallback");
        PropertyCallBack actualPropCB = Deencapsulation.getField(testTwin, "deviceTwinGenericPropertyChangeCallback");

        assertEquals(actualPropCB, mockedGenericPropertyCB);
        assertEquals(actualStatucCB, mockedStatusCB);
        assertEquals(actualClient, mockedDeviceIO);
        assertEquals(actualConfig, mockedConfig);

    }

    /*
    **Tests_SRS_DEVICETWIN_25_001: [**The constructor shall throw InvalidParameter Exception if any of the parameters i.e client, config, deviceTwinCallback, genericPropertyCallback are null. **]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void contructorThrowsExceptionIfClientIsNull() throws IOException
    {
        DeviceTwin testTwin = new DeviceTwin(null, mockedConfig,
                mockedStatusCB, null, mockedGenericPropertyCB, null);

    }

    @Test (expected = IllegalArgumentException.class)
    public void contructorThrowsExceptionIfConfigIsNull() throws IOException
    {
        DeviceTwin testTwin = new DeviceTwin(mockedDeviceIO, null,
                mockedStatusCB, null, mockedGenericPropertyCB, null);

    }

    @Test (expected = IllegalArgumentException.class)
    public void contructorThrowsExceptionIfTwinCBIsNull() throws IOException
    {
        DeviceTwin testTwin = new DeviceTwin(mockedDeviceIO, mockedConfig,
                null, null, mockedGenericPropertyCB, null);

    }

    @Test (expected = IllegalArgumentException.class)
    public void contructorThrowsExceptionIfgenericPropCBIsNull() throws IOException
    {
        DeviceTwin testTwin = new DeviceTwin(mockedDeviceIO, mockedConfig,
                mockedStatusCB, null, null, null);

    }

    /*
    **Tests_SRS_DEVICETWIN_25_002: [**The constructor shall save the device twin message callback by calling setDeviceTwinMessageCallback where any further messages for device twin shall be delivered.**]**
     */
    @Test
    public void contructorSetsDTMessageResponseCB() throws IOException
    {
        DeviceTwin testTwin = new DeviceTwin(mockedDeviceIO, mockedConfig,
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
    **Tests_SRS_DEVICETWIN_25_004: [**The constructor shall create a new twin object which will hence forth be used as a storage for all the properties provided by user.**]**
     */
    @Test
    public void constructorCreatesNewTwinObject(@Mocked final TwinParser mockedTwinParserObject) throws IOException
    {
        new StrictExpectations()
        {
            {
                new TwinParser(withAny(new TwinChangedCallback()
                {
                    @Override
                    public void execute(Map<String, Object> map)
                    {

                    }
                }), withAny(new TwinChangedCallback()
                {
                    @Override
                    public void execute(Map<String, Object> map)
                    {

                    }
                }));
                result = mockedTwinParserObject;
            }
        };

        //act
        DeviceTwin testTwin = new DeviceTwin(mockedDeviceIO, mockedConfig,
                mockedStatusCB, null, mockedGenericPropertyCB, null);

        TwinParser actualTwinParserObj = Deencapsulation.getField(testTwin, "twinParser");

        //assert
        assertNotNull(actualTwinParserObj);
    }

    /*
    **Tests_SRS_DEVICETWIN_25_005: [**The method shall create a device twin message with empty payload to be sent IotHub.**]**
    **Tests_SRS_DEVICETWIN_25_008: [**This method shall send the message to the lower transport layers by calling sendEventAsync.**]**
     */
    @Test
    public void getDeviceTwinSucceeds(@Mocked final DeviceTwinMessage mockedDeviceTwinMessage) throws IOException
    {
        //arrange
        DeviceTwin testTwin = new DeviceTwin(mockedDeviceIO, mockedConfig,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        final byte[] body = {};

        new NonStrictExpectations()
        {
            {
                new DeviceTwinMessage(body);
                result = mockedDeviceTwinMessage;
            }
        };

        //act
        testTwin.getDeviceTwin();

        //assert
        new Verifications()
        {
            {
                mockedDeviceTwinMessage.setRequestId(anyString);
                times = 1;
                mockedDeviceTwinMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_GET_REQUEST);
                times = 1;
                mockedDeviceIO.sendEventAsync(mockedDeviceTwinMessage, (IotHubEventCallback)any , null);
                times = 1;
            }
        };

    }

    /*
    **Tests_SRS_DEVICETWIN_25_006: [**This method shall set the message type as DEVICE_OPERATION_TWIN_GET_REQUEST by calling setDeviceOperationType.**]**
    */
    @Test
    public void getDeviceTwinSetsDeviceTwinOperation(@Mocked final DeviceTwinMessage mockedDeviceTwinMessage) throws IOException
    {
        DeviceTwin testTwin = new DeviceTwin(mockedDeviceIO, mockedConfig,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        final byte[] body = {};

        new NonStrictExpectations()
        {
            {
                new DeviceTwinMessage(body);
                result = mockedDeviceTwinMessage;
            }
        };

        testTwin.getDeviceTwin();

        new Verifications()
        {
            {
                mockedDeviceTwinMessage.setRequestId(anyString);
                times = 1;
                mockedDeviceTwinMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_GET_REQUEST);
                times = 1;
                mockedDeviceIO.sendEventAsync(mockedDeviceTwinMessage, (IotHubEventCallback)any , null);
                times = 1;
            }
        };

    }

    /*
    **Tests_SRS_DEVICETWIN_25_005: [**The method shall create a device twin message with empty payload to be sent IotHub.**]**
     */
    @Test
    public void getDeviceTwinSetsEmptyPayload(@Mocked final DeviceTwinMessage mockedDeviceTwinMessage) throws IOException
    {
        DeviceTwin testTwin = new DeviceTwin(mockedDeviceIO, mockedConfig,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        final byte[] body = {};

        new NonStrictExpectations()
        {
            {
                new DeviceTwinMessage(body);
                result = mockedDeviceTwinMessage;
            }
        };

        testTwin.getDeviceTwin();

        new Verifications()
        {
            {
                mockedDeviceTwinMessage.setRequestId(anyString);
                times = 1;
                mockedDeviceTwinMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_GET_REQUEST);
                times = 1;
                mockedDeviceIO.sendEventAsync(mockedDeviceTwinMessage, (IotHubEventCallback)any , null);
                times = 1;
            }
        };

    }

    @Test
    public void getDeviceTwinRequestCompleteTriggersStatusCB() throws IOException
    {
        DeviceTwin testTwin = new DeviceTwin(mockedDeviceIO, mockedConfig,
                mockedStatusCB, null, mockedGenericPropertyCB, null);

        IotHubEventCallback deviceTwinRequestMessageCallback = Deencapsulation.newInnerInstance("deviceTwinRequestMessageCallback", testTwin);

        deviceTwinRequestMessageCallback.execute(IotHubStatusCode.OK, null);

        new Verifications()
        {
            {
                mockedStatusCB.execute(IotHubStatusCode.OK, null);
                times = 1;
            }
        };
    }

    @Test
    public void getDeviceTwinResponseTriggersStatusCB(@Mocked final TwinParser mockedTwinParserObj) throws IOException
    {
        //arrange
        final byte[] body = {};
        DeviceTwin testTwin = new DeviceTwin(mockedDeviceIO, mockedConfig,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        MessageCallback deviceTwinResponseMessageCallback = Deencapsulation.newInnerInstance("deviceTwinResponseMessageCallback", testTwin);

        final DeviceTwinMessage testMessage = new DeviceTwinMessage(body);
        testMessage.setStatus(String.valueOf(200));
        testMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_GET_RESPONSE);

        //act
        deviceTwinResponseMessageCallback.execute(testMessage, null);

        //assert
        final TwinParser actualTwinParserObj = Deencapsulation.getField(testTwin, "twinParser" );
        final IotHubEventCallback actualStatusCB = Deencapsulation.getField(testTwin, "deviceTwinStatusCallback");
        new Verifications()
        {
            {
                actualStatusCB.execute(IotHubStatusCode.OK, withAny(new Object()));
                times = 1;
                actualTwinParserObj.updateTwin(anyString);
                times = 1;

            }
        };
    }

    /*
    **Tests_SRS_DEVICETWIN_25_030: [**If the message is of type DeviceTwin and DEVICE_OPERATION_TWIN_GET_RESPONSE then the payload is deserialized by calling updateTwin only if the status is ok.**]**
     */
    @Test
    public void getDeviceTwinResponseCallsUpdateTwinIfStatusOk(@Mocked TwinParser mockedTwinParserObject) throws IOException
    {
        //arrange
        final byte[] body = {};
        DeviceTwin testTwin = new DeviceTwin(mockedDeviceIO, mockedConfig,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        MessageCallback deviceTwinResponseMessageCallback = Deencapsulation.newInnerInstance("deviceTwinResponseMessageCallback", testTwin);

        final DeviceTwinMessage testMessage = new DeviceTwinMessage(body);
        testMessage.setStatus(String.valueOf(200));
        testMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_GET_RESPONSE);

        //act
        deviceTwinResponseMessageCallback.execute(testMessage, null);

        //assert
        final TwinParser actualTwinParserObj = Deencapsulation.getField(testTwin, "twinParser" );
        final IotHubEventCallback actualStatusCB = Deencapsulation.getField(testTwin, "deviceTwinStatusCallback");
        new Verifications()
        {
            {
                actualStatusCB.execute(IotHubStatusCode.OK, withAny(new Object()));
                times = 1;
                actualTwinParserObj.updateTwin(anyString);
                times = 1;

            }
        };
    }

    /*
    **Tests_SRS_DEVICETWIN_25_029: [**If the message is of type DeviceTwin and DEVICE_OPERATION_TWIN_GET_RESPONSE then the user call with a valid status is triggered.**]**
     */
    @Test
    public void getDeviceTwinResponseDoesNotCallUpdateTwinIfStatusNotOk(@Mocked TwinParser mockedTwinParserObject) throws IOException
    {
        //arrange
        final byte[] body = {};
        DeviceTwin testTwin = new DeviceTwin(mockedDeviceIO, mockedConfig,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        MessageCallback deviceTwinResponseMessageCallback = Deencapsulation.newInnerInstance("deviceTwinResponseMessageCallback", testTwin);

        final DeviceTwinMessage testMessage = new DeviceTwinMessage(body);
        testMessage.setStatus(String.valueOf(401));
        testMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_GET_RESPONSE);

        //act
        deviceTwinResponseMessageCallback.execute(testMessage, null);

        //assert
        final TwinParser actualTwinParserObj = Deencapsulation.getField(testTwin, "twinParser" );
        final IotHubEventCallback actualStatusCB = Deencapsulation.getField(testTwin, "deviceTwinStatusCallback");
        new Verifications()
        {
            {
                actualStatusCB.execute(IotHubStatusCode.UNAUTHORIZED, withAny(new Object()));
                times = 1;
                actualTwinParserObj.updateTwin(anyString);
                times = 0;

            }
        };
    }

    /*
    **Tests_SRS_DEVICETWIN_25_031: [**If the message is of type DeviceTwin and DEVICE_OPERATION_TWIN_GET_RESPONSE and if the status is null then the user is notified on the status callback registered by the user as ERROR.**]**
     */
    @Test
    public void getDeviceTwinResponseCallStusCBWithERRORIfStatusNull(@Mocked TwinParser mockedTwinParserObject) throws IOException
    {
        //arrange
        final byte[] body = {};
        DeviceTwin testTwin = new DeviceTwin(mockedDeviceIO, mockedConfig,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        MessageCallback deviceTwinResponseMessageCallback = Deencapsulation.newInnerInstance("deviceTwinResponseMessageCallback", testTwin);

        final DeviceTwinMessage testMessage = new DeviceTwinMessage(body);
        testMessage.setStatus(null);
        testMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_GET_RESPONSE);

        //act
        deviceTwinResponseMessageCallback.execute(testMessage, null);

        //assert
        final TwinParser actualTwinParserObj = Deencapsulation.getField(testTwin, "twinParser" );
        final IotHubEventCallback actualStatusCB = Deencapsulation.getField(testTwin, "deviceTwinStatusCallback");
        new Verifications()
        {
            {
                actualStatusCB.execute(IotHubStatusCode.ERROR, withAny(new Object()));
                times = 1;
                actualTwinParserObj.updateTwin(anyString);
                times = 0;

            }
        };
    }

    /*
    **Tests_SRS_DEVICETWIN_25_009: [**The method shall throw InvalidParameter Exception if reportedProperties is null.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void updateReportedPropThrowsExceptionPropIsNull() throws IOException
    {
        DeviceTwin testTwin = new DeviceTwin(mockedDeviceIO, mockedConfig,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        testTwin.updateReportedProperties(null);
    }

    /*
    **Tests_SRS_DEVICETWIN_25_011: [**The method shall send the property set to Twin Serializer for serilization by calling updateReportedProperty.**]**
     */
    @Test
    public void updateReportedPropCallsTwinAPIForSerialization(@Mocked final TwinParser mockedTwinParserObject,
                                                               @Mocked final DeviceTwinMessage mockedDeviceTwinMessage) throws IOException
    {
        final String mockedSerilizedProp = "SerializedReportedProperties";
        new NonStrictExpectations()
        {
            {
                new TwinParser(withAny(new TwinChangedCallback()
                {
                    @Override
                    public void execute(Map<String, Object> map)
                    {

                    }
                }), withAny(new TwinChangedCallback()
                {
                    @Override
                    public void execute(Map<String, Object> map)
                    {

                    }
                }));
                result = mockedTwinParserObject;
                mockedTwinParserObject.updateReportedProperty(withAny(new HashMap<String, Object>()));
                result = mockedSerilizedProp;
                new DeviceTwinMessage(withAny(new byte[0]));
                result = mockedDeviceTwinMessage;
            }
        };
        DeviceTwin testTwin = new DeviceTwin(mockedDeviceIO, mockedConfig,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        HashSet<Property> reportedProp = new HashSet<>();

        testTwin.updateReportedProperties(reportedProp);

        new Verifications()
        {
            {
                mockedTwinParserObject.updateReportedProperty(withAny(new HashMap<String, Object>()));
                times = 1;
                mockedDeviceTwinMessage.setRequestId(anyString);
                times = 1;
                mockedDeviceTwinMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST);
                times = 1;
                mockedDeviceIO.sendEventAsync(mockedDeviceTwinMessage, (IotHubEventCallback)any , null);
                times = 1;
            }
        };
    }

    /*
    **Tests_SRS_DEVICETWIN_25_012: [**The method shall create a device twin message with the serialized payload if not null to be sent IotHub.**]**
    **Tests_SRS_DEVICETWIN_25_013: [**This method shall set the message type as DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST by calling setDeviceOperationType.**]**
    **Tests_SRS_DEVICETWIN_25_014: [**This method shall set the request id for the message by calling setRequestId .**]**
    **Tests_SRS_DEVICETWIN_25_015: [**This method shall send the message to the lower transport layers by calling sendEventAsync.**]**
     */

    @Test
    public void updateReportedPropSetsCorrectTwinOperation(@Mocked final TwinParser mockedTwinParserObject,
                                                           @Mocked final DeviceTwinMessage mockedDeviceTwinMessage) throws IOException
    {
        final String mockedSerilizedProp = "SerializedReportedProperties";
        new NonStrictExpectations()
        {
            {
                new TwinParser(withAny(new TwinChangedCallback()
                {
                    @Override
                    public void execute(Map<String, Object> map)
                    {

                    }
                }), withAny(new TwinChangedCallback()
                {
                    @Override
                    public void execute(Map<String, Object> map)
                    {

                    }
                }));
                result = mockedTwinParserObject;
                mockedTwinParserObject.updateReportedProperty(withAny(new HashMap<String, Object>()));
                result = mockedSerilizedProp;
                new DeviceTwinMessage(withAny(new byte[0]));
                result = mockedDeviceTwinMessage;
            }
        };
        DeviceTwin testTwin = new DeviceTwin(mockedDeviceIO, mockedConfig,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        HashSet<Property> reportedProp = new HashSet<>();

        testTwin.updateReportedProperties(reportedProp);

        new Verifications()
        {
            {
                mockedTwinParserObject.updateReportedProperty(withAny(new HashMap<String, Object>()));
                times = 1;
                mockedDeviceTwinMessage.setRequestId(anyString);
                times = 1;
                mockedDeviceTwinMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST);
                times = 1;
                mockedDeviceIO.sendEventAsync(mockedDeviceTwinMessage, (IotHubEventCallback)any , null);
                times = 1;
            }
        };
    }

    /*
    **Tests_SRS_DEVICETWIN_25_027: [**If the message is of type DeviceTwin and DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_RESPONSE then the user call with a valid status is triggered.**]**
     */
    @Test
    public void updateReportedPropOnResponseCallsStatusCB(@Mocked TwinParser mockedTwinParserObject) throws IOException
    {
        //arrange
        final byte[] body = {};
        DeviceTwin testTwin = new DeviceTwin(mockedDeviceIO, mockedConfig,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        MessageCallback deviceTwinResponseMessageCallback = Deencapsulation.newInnerInstance("deviceTwinResponseMessageCallback", testTwin);

        final DeviceTwinMessage testMessage = new DeviceTwinMessage(body);
        testMessage.setStatus(String.valueOf(200));
        testMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_RESPONSE);

        //act
        deviceTwinResponseMessageCallback.execute(testMessage, null);

        //assert
        final TwinParser actualTwinParserObj = Deencapsulation.getField(testTwin, "twinParser" );
        final IotHubEventCallback actualStatusCB = Deencapsulation.getField(testTwin, "deviceTwinStatusCallback");
        new Verifications()
        {
            {
                actualStatusCB.execute(IotHubStatusCode.OK, withAny(new Object()));
                times = 1;
                actualTwinParserObj.updateTwin(anyString);
                times = 0;
                actualTwinParserObj.updateDesiredProperty(anyString);
                times = 0;

            }
        };
    }

    /*
    **Tests_SRS_DEVICETWIN_25_028: [**If the message is of type DeviceTwin and DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_RESPONSE and if the status is null then the user is notified on the status callback registered by the user as ERROR.**]**
     */
    @Test
    public void updateReportedPropOnResponseCallsStatusCBErrorIfNullStatus(@Mocked TwinParser mockedTwinParserObject) throws IOException
    {
        //arrange
        final byte[] body = {};
        DeviceTwin testTwin = new DeviceTwin(mockedDeviceIO, mockedConfig,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        MessageCallback deviceTwinResponseMessageCallback = Deencapsulation.newInnerInstance("deviceTwinResponseMessageCallback", testTwin);

        final DeviceTwinMessage testMessage = new DeviceTwinMessage(body);
        testMessage.setStatus(null);
        testMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_RESPONSE);

        //act
        deviceTwinResponseMessageCallback.execute(testMessage, null);

        //assert
        final TwinParser actualTwinParserObj = Deencapsulation.getField(testTwin, "twinParser" );
        final IotHubEventCallback actualStatusCB = Deencapsulation.getField(testTwin, "deviceTwinStatusCallback");
        new Verifications()
        {
            {
                actualStatusCB.execute(IotHubStatusCode.ERROR, withAny(new Object()));
                times = 1;
                actualTwinParserObj.updateTwin(anyString);
                times = 0;
                actualTwinParserObj.updateDesiredProperty(anyString);
                times = 0;

            }
        };
    }

    /*
    **Tests_SRS_DEVICETWIN_25_017: [**The method shall create a treemap to store callbacks for desired property notifications specified in onDesiredPropertyChange.**]**
    **Tests_SRS_DEVICETWIN_25_018: [**If not already subscribed then this method shall create a device twin message with empty payload and set its type as DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST.**]**
    **Tests_SRS_DEVICETWIN_25_019: [**If not already subscribed then this method shall send the message using sendEventAsync.**]**
     */
    @Test
    public void subscribeToDesiredSetsCorrectOperation(@Mocked final TwinParser mockedTwinParserObject,
                                                       @Mocked final DeviceTwinMessage mockedDeviceTwinMessage,
                                                       @Mocked final PropertyCallBack<String, Object> mockedDesiredCB) throws IOException
    {
        new NonStrictExpectations()
        {
            {
                new TwinParser(withAny(new TwinChangedCallback()
                {
                    @Override
                    public void execute(Map<String, Object> map)
                    {

                    }
                }), withAny(new TwinChangedCallback()
                {
                    @Override
                    public void execute(Map<String, Object> map)
                    {

                    }
                }));
                result = mockedTwinParserObject;
                new DeviceTwinMessage(withAny(new byte[0]));
                result = mockedDeviceTwinMessage;
            }
        };
        DeviceTwin testTwin = new DeviceTwin(mockedDeviceIO, mockedConfig,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        Map<Property, Pair<PropertyCallBack<String, Object>, Object>> desiredMap = new HashMap<>();
        desiredMap.put(new Property("DesiredProp", "DesiredValue"), new Pair<>(mockedDesiredCB, null));

        testTwin.subscribeDesiredPropertiesNotification(desiredMap);

        final ConcurrentSkipListMap<String, Pair<PropertyCallBack<String, Object>, Object>> actualMap = Deencapsulation.getField(testTwin, "onDesiredPropertyChangeMap");

        assertNotNull(actualMap);
        assertFalse(actualMap.isEmpty());
        assertTrue(actualMap.containsKey("DesiredProp"));
        assertEquals(actualMap.get("DesiredProp").getKey(), mockedDesiredCB );

        new Verifications()
        {
            {
                mockedDeviceTwinMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST);
                times = 1;
                mockedDeviceIO.sendEventAsync(mockedDeviceTwinMessage, (IotHubEventCallback)any , null);
                times = 1;
            }
        };
    }

    @Test
    public void subscribeToDesiredDoesNotSubscribeIfAlreadySubscribed(@Mocked final TwinParser mockedTwinParserObject,
                                                                      @Mocked final DeviceTwinMessage mockedDeviceTwinMessage,
                                                                      @Mocked final PropertyCallBack<String, Object> mockedDesiredCB) throws IOException
    {
        new NonStrictExpectations()
        {
            {
                new TwinParser(withAny(new TwinChangedCallback()
                {
                    @Override
                    public void execute(Map<String, Object> map)
                    {

                    }
                }), withAny(new TwinChangedCallback()
                {
                    @Override
                    public void execute(Map<String, Object> map)
                    {

                    }
                }));
                result = mockedTwinParserObject;
                new DeviceTwinMessage(withAny(new byte[0]));
                result = mockedDeviceTwinMessage;
            }
        };
        DeviceTwin testTwin = new DeviceTwin(mockedDeviceIO, mockedConfig,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        Map<Property, Pair<PropertyCallBack<String, Object>, Object>> desiredMap = new HashMap<>();
        desiredMap.put(new Property("DesiredProp1", "DesiredValue1"), new Pair<>(mockedDesiredCB, null));
        testTwin.subscribeDesiredPropertiesNotification(desiredMap);

        Deencapsulation.setField(testTwin, "isSubscribed", true);

        desiredMap.put(new Property("DesiredProp2", "DesiredValue2"), new Pair<>(mockedDesiredCB, null));

        testTwin.subscribeDesiredPropertiesNotification(desiredMap);

        final ConcurrentSkipListMap<String, Pair<PropertyCallBack<String, Object>, Object>> actualMap = Deencapsulation.getField(testTwin, "onDesiredPropertyChangeMap");

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
                mockedDeviceIO.sendEventAsync(mockedDeviceTwinMessage, (IotHubEventCallback)any , null);
                times = 1;
            }
        };

    }

    @Test
    public void desiredPropResponseDoesNotCallsUserStatusCBOnNotification(@Mocked TwinParser mockedTwinParserObject) throws IOException
    {
        //arrange
        final byte[] body = {};
        DeviceTwin testTwin = new DeviceTwin(mockedDeviceIO, mockedConfig,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        MessageCallback deviceTwinResponseMessageCallback = Deencapsulation.newInnerInstance("deviceTwinResponseMessageCallback", testTwin);

        final DeviceTwinMessage testMessage = new DeviceTwinMessage(body);
        testMessage.setStatus(String.valueOf(200));
        testMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);

        //act
        deviceTwinResponseMessageCallback.execute(testMessage, null);

        //assert
        final TwinParser actualTwinParserObj = Deencapsulation.getField(testTwin, "twinParser" );
        final IotHubEventCallback actualStatusCB = Deencapsulation.getField(testTwin, "deviceTwinStatusCallback");
        new Verifications()
        {
            {
                actualStatusCB.execute(IotHubStatusCode.OK, withAny(new Object()));
                times = 0;
                actualTwinParserObj.updateTwin(anyString);
                times = 0;
                actualTwinParserObj.updateDesiredProperty(anyString);
                times = 1;
            }
        };

    }

    /*
    **Tests_SRS_DEVICETWIN_25_026: [**If the message is of type DeviceTwin and DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE then the payload is deserialize by calling updateDesiredProperty.**]**
     */
    @Test
    public void desiredPropResponseCallsTwinApiToDeserialize(@Mocked TwinParser mockedTwinParserObject) throws IOException
    {
        //arrange
        final byte[] body = {};
        DeviceTwin testTwin = new DeviceTwin(mockedDeviceIO, mockedConfig,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        MessageCallback deviceTwinResponseMessageCallback = Deencapsulation.newInnerInstance("deviceTwinResponseMessageCallback", testTwin);

        final DeviceTwinMessage testMessage = new DeviceTwinMessage(body);
        testMessage.setStatus(String.valueOf(200));
        testMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);

        //act
        deviceTwinResponseMessageCallback.execute(testMessage, null);

        //assert
        final TwinParser actualTwinParserObj = Deencapsulation.getField(testTwin, "twinParser" );
        final IotHubEventCallback actualStatusCB = Deencapsulation.getField(testTwin, "deviceTwinStatusCallback");
        new Verifications()
        {
            {
                actualTwinParserObj.updateTwin(anyString);
                times = 0;
                actualTwinParserObj.updateDesiredProperty(anyString);
                times = 1;
            }
        };
    }

    /*
    **Codes_SRS_DEVICETWIN_25_023: [**OnDesiredPropertyChange callback shall look for the user registered call back on the property that changed and if no callback is registered or is null then OnDesiredPropertyChange shall call the user on generic callback providing with the desired property change key and value pair**]**
     */
    @Test
    public void subscribeToDesiredCallsGenericCBOnDesiredChangeIfNoUserCBFound(@Mocked final PropertyCallBack<String, Object> mockedDesiredCB) throws IOException
    {

        DeviceTwin testTwin = new DeviceTwin(mockedDeviceIO, mockedConfig,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        Map<Property, Pair<PropertyCallBack<String, Object>, Object>> desiredMap = new HashMap<>();
        desiredMap.put(new Property("DesiredProp1", "DesiredValue1"), new Pair<>(mockedDesiredCB, null));
        testTwin.subscribeDesiredPropertiesNotification(desiredMap);

        TwinChangedCallback onDesiredChange = Deencapsulation.newInnerInstance("OnDesiredPropertyChanged", testTwin);
        final HashMap<String, Object> desiredPropertyMap = new HashMap<>();
        desiredPropertyMap.put("DesiredProp2", "DesiredValue2");

        //act
        onDesiredChange.execute(desiredPropertyMap);

        //assert
        assertTrue(desiredPropertyMap.isEmpty());
        new Verifications()
        {
            {
                mockedGenericPropertyCB.PropertyCall("DesiredProp2", "DesiredValue2", null);
                times = 1;
                desiredPropertyMap.remove("DesiredProp1");
                times = 1;
            }
        };
    }

    /*
    **Test_SRS_DEVICETWIN_25_022: [**OnDesiredPropertyChange callback shall look for the user registered call back on the property that changed provided in desiredPropertyMap and call the user providing the desired property change key and value pair**]**
     */
    @Test
    public void subscribeToDesiredCallsUserCBOnDesiredChangeIfUserCBFound(@Mocked final PropertyCallBack<String, Object> mockedDesiredCB) throws IOException
    {

        DeviceTwin testTwin = new DeviceTwin(mockedDeviceIO, mockedConfig,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        Map<Property, Pair<PropertyCallBack<String, Object>, Object>> desiredMap = new HashMap<>();
        desiredMap.put(new Property("DesiredProp1", "DesiredValue1"), new Pair<>(mockedDesiredCB, null));
        testTwin.subscribeDesiredPropertiesNotification(desiredMap);

        TwinChangedCallback onDesiredChange = Deencapsulation.newInnerInstance("OnDesiredPropertyChanged", testTwin);
        final HashMap<String, Object> desiredPropertyMap = new HashMap<>();
        desiredPropertyMap.put("DesiredProp1", "DesiredValue1");

        //act
        onDesiredChange.execute(desiredPropertyMap);

        //assert
        assertTrue(desiredPropertyMap.isEmpty());
        new Verifications()
        {
            {
                mockedDesiredCB.PropertyCall("DesiredProp1", "DesiredValue1", null);
                times = 1;
                desiredPropertyMap.remove("DesiredProp1");
                times = 1;
            }
        };

    }

    /*
    **Tests_SRS_DEVICETWIN_25_023: [**OnDesiredPropertyChange callback shall look for the user registered call back on the property that changed and if no callback is registered or is null then OnDesiredPropertyChange shall call the user on generic callback providing with the desired property change key and value pair**]**
    */
    @Test
    public void desiredChangeResponseCallsGenericCBCBWithDesiredChangeIfNullCB(@Mocked TwinParser mockedTwinParserObject) throws IOException
    {

        DeviceTwin testTwin = new DeviceTwin(mockedDeviceIO, mockedConfig,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        Map<Property, Pair<PropertyCallBack<String, Object>, Object>> desiredMap = new HashMap<>();
        desiredMap.put(new Property("DesiredProp1", "DesiredValue1"), new Pair<>((PropertyCallBack<String, Object>) null, null));
        testTwin.subscribeDesiredPropertiesNotification(desiredMap);

        TwinChangedCallback onDesiredChange = Deencapsulation.newInnerInstance("OnDesiredPropertyChanged", testTwin);
        final HashMap<String, Object> desiredPropertyMap = new HashMap<>();
        desiredPropertyMap.put("DesiredProp1", "DesiredValue1");

        //act
        onDesiredChange.execute(desiredPropertyMap);

        //assert
        assertTrue(desiredPropertyMap.isEmpty());
        new Verifications()
        {
            {
                mockedGenericPropertyCB.PropertyCall("DesiredProp1", "DesiredValue1", null);
                times = 1;
                desiredPropertyMap.remove("DesiredProp1");
                times = 1;
            }
        };
    }

    @Test
    public void desiredChangeResponseCallsUserGenericCBWithDesiredChangeIfUnsubscribedYet(@Mocked TwinParser mockedTwinParserObject) throws IOException
    {
        DeviceTwin testTwin = new DeviceTwin(mockedDeviceIO, mockedConfig,
                mockedStatusCB, null, mockedGenericPropertyCB, null);
        TwinChangedCallback onDesiredChange = Deencapsulation.newInnerInstance("OnDesiredPropertyChanged", testTwin);
        final HashMap<String, Object> desiredPropertyMap = new HashMap<>();
        desiredPropertyMap.put("DesiredProp1", "DesiredValue1");

        //act
        onDesiredChange.execute(desiredPropertyMap);

        //assert
        assertTrue(desiredPropertyMap.isEmpty());

        new Verifications()
        {
            {
                mockedGenericPropertyCB.PropertyCall("DesiredProp1", "DesiredValue1", null);
                times = 1;
                desiredPropertyMap.remove("DesiredProp1");
                times = 1;
            }
        };
    }

}
