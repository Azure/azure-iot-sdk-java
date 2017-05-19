// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.DeviceTwin;

import com.microsoft.azure.sdk.iot.deps.serializer.TwinChangedCallback;
import com.microsoft.azure.sdk.iot.deps.serializer.TwinParser;
import com.microsoft.azure.sdk.iot.device.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

import static com.microsoft.azure.sdk.iot.device.IotHubMessageResult.ABANDON;
import static com.microsoft.azure.sdk.iot.device.IotHubMessageResult.COMPLETE;

public class DeviceTwin
{
    private int requestId;
    private TwinParser twinParser = null;
    private DeviceIO deviceIO = null;
    private DeviceClientConfig config = null;
    private boolean isSubscribed = false;

    private final Object DEVICE_TWIN_LOCK = new Object();

    /*
        Callback to respond to user on all of its status
     */
    private IotHubEventCallback deviceTwinStatusCallback;
    private Object deviceTwinStatusCallbackContext;

    /*
        Callbacks to respond to its user on desired property changes
     */
    private PropertyCallBack<String, Object> deviceTwinGenericPropertyChangeCallback;
    private Object deviceTwinGenericPropertyChangeCallbackContext;

    /*
        Map of callbacks to call when a particular desired property changed
     */

    private ConcurrentSkipListMap<String, Pair<PropertyCallBack<String, Object>, Object>> onDesiredPropertyChangeMap;

    /*
        Callback invoked by serializer when desired property changes
    */
    private final class OnDesiredPropertyChanged implements TwinChangedCallback
    {
        /*
        Codes_SRS_DEVICETWIN_25_021: [**On deserialization of desired properties, OnDesiredPropertyChange callback is triggered by the serializer**]**
         */
        @Override
        public void execute(Map<String, Object> desiredPropertyMap)
        {
            synchronized (DEVICE_TWIN_LOCK)
            {
                if (desiredPropertyMap != null)
                {
                    for (Iterator desiredPropertyIt = desiredPropertyMap.entrySet().iterator(); desiredPropertyIt.hasNext();)
                    {
                        Map.Entry<String, String> desiredProperty = (Map.Entry<String, String>) desiredPropertyIt.next();

                        if (onDesiredPropertyChangeMap != null && onDesiredPropertyChangeMap.containsKey(desiredProperty.getKey()))

                        {
                            Pair<PropertyCallBack<String, Object>, Object> callBackObjectPair = onDesiredPropertyChangeMap.get(desiredProperty.getKey());
                            if (callBackObjectPair != null && callBackObjectPair.getKey() != null)
                            {
                                /*
                                **Codes_SRS_DEVICETWIN_25_022: [**OnDesiredPropertyChange callback shall look for the user registered call back on the property that changed provided in desiredPropertyMap and call the user providing the desired property change key and value pair**]**
                                 */
                                callBackObjectPair.getKey().PropertyCall(desiredProperty.getKey(),
                                        desiredProperty.getValue(), callBackObjectPair.getValue());
                            }
                            else
                            {
                                /*
                                **Codes_SRS_DEVICETWIN_25_023: [**OnDesiredPropertyChange callback shall look for the user registered call back on the property that changed and if no callback is registered or is null then OnDesiredPropertyChange shall call the user on generic callback providing with the desired property change key and value pair**]**
                                 */
                                deviceTwinGenericPropertyChangeCallback.PropertyCall(desiredProperty.getKey(),
                                        desiredProperty.getValue(), deviceTwinGenericPropertyChangeCallbackContext);
                            }

                        }
                        else
                        {
                            /*
                            **Codes_SRS_DEVICETWIN_25_023: [**OnDesiredPropertyChange callback shall look for the user registered call back on the property that changed and if no callback is registered or is null then OnDesiredPropertyChange shall call the user on generic callback providing with the desired property change key and value pair**]**
                             */
                            deviceTwinGenericPropertyChangeCallback.PropertyCall(desiredProperty.getKey(),
                                    desiredProperty.getValue(), deviceTwinGenericPropertyChangeCallbackContext);

                        }
                        desiredPropertyIt.remove();
                    }
                }
            }
        }
    }

    /*
        Callback invoked by serializer when reported property changes
     */
    private final class OnReportedPropertyChanged implements TwinChangedCallback
    {
        @Override
        public void execute(Map<String, Object> hashMap)
        {
            synchronized (DEVICE_TWIN_LOCK)
            {

            }

        }
    }

    /*
        Callback invoked when a response to device twin operation is issued by iothub
     */
    private final class deviceTwinResponseMessageCallback implements MessageCallback
    {
        /*
        **Codes_SRS_DEVICETWIN_25_025: [**On receiving a message from IOTHub with desired property changes, the callback deviceTwinResponseMessageCallback is triggered.**]**
         */
        @Override
        public IotHubMessageResult execute(Message message, Object callbackContext)
        {
            synchronized (DEVICE_TWIN_LOCK)
            {
                /*
                **Codes_SRS_DEVICETWIN_25_028: [**If the message is of type DeviceTwin and DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_RESPONSE and if the status is null then the user is notified on the status callback registered by the user as ERROR.**]**
                **Codes_SRS_DEVICETWIN_25_031: [**If the message is of type DeviceTwin and DEVICE_OPERATION_TWIN_GET_RESPONSE and if the status is null then the user is notified on the status callback registered by the user as ERROR.**]**
                 */
                IotHubStatusCode iotHubStatus = IotHubStatusCode.ERROR;
                if (message.getMessageType() != MessageType.DeviceTwin)
                {
                    System.out.print("Unexpected message type received");
                    deviceTwinStatusCallback.execute(iotHubStatus, deviceTwinStatusCallbackContext);
                    return ABANDON;
                }

                DeviceTwinMessage dtMessage = (DeviceTwinMessage) message;
                String status = dtMessage.getStatus();

                switch (dtMessage.getDeviceOperationType())
                {
                    case DEVICE_OPERATION_TWIN_GET_RESPONSE:
                    {
                        if (status != null)
                        {
                            iotHubStatus = IotHubStatusCode.getIotHubStatusCode(Integer.parseInt(status));
                        }
                        /*
                        **Codes_SRS_DEVICETWIN_25_029: [**If the message is of type DeviceTwin and DEVICE_OPERATION_TWIN_GET_RESPONSE then the user call with a valid status is triggered.**]**
                         */

                        deviceTwinStatusCallback.execute(iotHubStatus, deviceTwinStatusCallbackContext);

                        if (iotHubStatus == IotHubStatusCode.OK)
                        {
                            /*
                            **Codes_SRS_DEVICETWIN_25_030: [**If the message is of type DeviceTwin and DEVICE_OPERATION_TWIN_GET_RESPONSE then the payload is deserialized by calling updateTwin only if the status is ok.**]**
                             */
                            twinParser.updateTwin(new String(dtMessage.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));
                        }
                        break;
                    }
                    case DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_RESPONSE:
                    {
                        if (status != null)
                        {
                            iotHubStatus = IotHubStatusCode.getIotHubStatusCode(Integer.parseInt(status));
                        }
                        /*
                        **Codes_SRS_DEVICETWIN_25_027: [**If the message is of type DeviceTwin and DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_RESPONSE then the user call with a valid status is triggered.**]**
                         */
                        deviceTwinStatusCallback.execute(iotHubStatus, deviceTwinStatusCallbackContext);

                        break;
                    }
                    case DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE:
                    {
                        /*
                        **Codes_SRS_DEVICETWIN_25_026: [**If the message is of type DeviceTwin and DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE then the payload is deserialized by calling updateDesiredProperty.**]**
                         */
                        isSubscribed = true;
                        twinParser.updateDesiredProperty(new String(dtMessage.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));

                        break;
                    }
                    default:
                        break;
                }
                return COMPLETE;
            }
        }
    }

    /*
        Callback invoked when device twin operation request has successfully completed
    */
    private final class deviceTwinRequestMessageCallback implements IotHubEventCallback
    {
        @Override
        public void execute(IotHubStatusCode responseStatus, Object callbackContext)
        {
            synchronized (DEVICE_TWIN_LOCK)
            {
            /*
                Don't worry about this....this is just delivery complete. Actual response is
                another message received in deviceTwinResponseMessageCallback.
             */
                deviceTwinStatusCallback.execute(responseStatus, deviceTwinStatusCallbackContext);
            }
        }
    }

    public DeviceTwin(DeviceIO client, DeviceClientConfig config, IotHubEventCallback deviceTwinCallback, Object deviceTwinCallbackContext,
                      PropertyCallBack genericPropertyCallback, Object genericPropertyCallbackContext)
    {
        /*
        **Codes_SRS_DEVICETWIN_25_001: [**The constructor shall throw IllegalArgumentException Exception if any of the parameters i.e client, config, deviceTwinCallback, genericPropertyCallback are null. **]**
         */
        if (client == null || config == null)
        {
            throw new IllegalArgumentException("Client or config cannot be null");
        }

        if (deviceTwinCallback == null)
        {
            throw new IllegalArgumentException("Device twin Callback cannot be null");
        }

        if (genericPropertyCallback == null)
        {
            throw new IllegalArgumentException("Generic property Callback cannot be null");
        }

        /*
        **Codes_SRS_DEVICETWIN_25_003: [**The constructor shall save all the parameters specified i.e client, config, deviceTwinCallback, genericPropertyCallback.**]**
         */
        this.deviceIO = client;
        this.config = config;

        /*
        **Codes_SRS_DEVICETWIN_25_002: [**The constructor shall save the device twin message callback by calling setDeviceTwinMessageCallback where any further messages for device twin shall be delivered.**]**
         */
        this.config.setDeviceTwinMessageCallback(new deviceTwinResponseMessageCallback(), null);
        this.requestId = 0;

        this.deviceTwinStatusCallback = deviceTwinCallback;
        this.deviceTwinStatusCallbackContext = deviceTwinCallbackContext;

        this.deviceTwinGenericPropertyChangeCallback = genericPropertyCallback;
        this.deviceTwinGenericPropertyChangeCallbackContext = genericPropertyCallbackContext;

        /*
        **Codes_SRS_DEVICETWIN_25_004: [**The constructor shall create a new twin object which will hence forth be used as a storage for all the properties provided by user.**]**
         */
        /*
        **Codes_SRS_DEVICETWIN_25_020: [**OnDesiredPropertyChange callback is registered with the serializer to be triggered when desired property changes.**]**
         */
        this.twinParser = new TwinParser(new OnDesiredPropertyChanged(), new OnReportedPropertyChanged());
    }


    public void getDeviceTwin()
    {
        /*
        **Codes_SRS_DEVICETWIN_25_005: [**The method shall create a device twin message with empty payload to be sent IotHub.**]**
         */
        DeviceTwinMessage getTwinRequestMessage = new DeviceTwinMessage(new byte[0]);

        /*
        **Codes_SRS_DEVICETWIN_25_007: [**This method shall set the request id for the message by calling setRequestId .**]**
         */
        getTwinRequestMessage.setRequestId(String.valueOf(requestId++));

        /*
        **Codes_SRS_DEVICETWIN_25_006: [**This method shall set the message type as DEVICE_OPERATION_TWIN_GET_REQUEST by calling setDeviceOperationType.**]**
         */
        getTwinRequestMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_GET_REQUEST);

        /*
        **Codes_SRS_DEVICETWIN_25_008: [**This method shall send the message to the lower transport layers by calling sendEventAsync.**]**
         */
        this.deviceIO.sendEventAsync(getTwinRequestMessage,new deviceTwinRequestMessageCallback(), null);
    }

    public synchronized void updateReportedProperties(Set<Property> reportedProperties) throws IOException
    {
        if (reportedProperties == null)
        {
            /*
            **Codes_SRS_DEVICETWIN_25_009: [**The method shall throw IllegalArgumentException Exception if reportedProperties is null.**]**
             */
            throw new IllegalArgumentException("Reported properties cannot be null");
        }
        if (this.twinParser == null)
        {
            /*
            **Codes_SRS_DEVICETWIN_25_010: [**The method shall throw IOException if twin object has not yet been created is null.**]**
             */
            throw new IOException("Initilaize twin object before using it");
        }

        HashMap<String, Object> reportedPropertiesMap = new HashMap<>();

        for(Property p : reportedProperties)
        {
            reportedPropertiesMap.put(p.getKey(), p.getValue());
        }

        /*
        **Codes_SRS_DEVICETWIN_25_011: [**The method shall send the property set to Twin Serializer for serilization by calling updateReportedProperty.**]**
         */
        String serializedReportedProperties = this.twinParser.updateReportedProperty(reportedPropertiesMap);

        if (serializedReportedProperties == null)
        {
            return;
        }

        /*
        **Codes_SRS_DEVICETWIN_25_012: [**The method shall create a device twin message with the serialized payload if not null to be sent IotHub.**]**
         */
        DeviceTwinMessage updateReportedPropertiesRequest = new DeviceTwinMessage(serializedReportedProperties.getBytes());

        /*
        **Codes_SRS_DEVICETWIN_25_014: [**This method shall set the request id for the message by calling setRequestId .**]**
         */
        updateReportedPropertiesRequest.setRequestId(String.valueOf(requestId++));

        /*
        **Codes_SRS_DEVICETWIN_25_013: [**This method shall set the message type as DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST by calling setDeviceOperationType.**]**
         */
        updateReportedPropertiesRequest.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST);

        /*
        **Codes_SRS_DEVICETWIN_25_015: [**This method shall send the message to the lower transport layers by calling sendEventAsync.**]**
         */
        this.deviceIO.sendEventAsync(updateReportedPropertiesRequest, new deviceTwinRequestMessageCallback(), null);

    }

    public void subscribeDesiredPropertiesNotification(Map<Property, Pair<PropertyCallBack<String, Object>, Object>> onDesiredPropertyChange)
    {
        if (onDesiredPropertyChangeMap == null)
        {
            /*
            **Codes_SRS_DEVICETWIN_25_017: [**The method shall create a treemap to store callbacks for desired property notifications specified in onDesiredPropertyChange.**]**
             */
            onDesiredPropertyChangeMap = new ConcurrentSkipListMap<>();
        }

        if (onDesiredPropertyChange != null)
        {
            for (Map.Entry<Property, Pair<PropertyCallBack<String, Object>, Object>> desired : onDesiredPropertyChange.entrySet())
            {
                onDesiredPropertyChangeMap.put(desired.getKey().getKey(), desired.getValue());
            }
        }

        if (!isSubscribed)
        {
            /*
            **Codes_SRS_DEVICETWIN_25_018: [**If not already subscribed then this method shall create a device twin message with empty payload and set its type as DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST.**]**
             */
            DeviceTwinMessage desiredPropertiesNotificationRequest = new DeviceTwinMessage(new byte[0]);

            desiredPropertiesNotificationRequest.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST);

            /*
            **Codes_SRS_DEVICETWIN_25_019: [**If not already subscribed then this method shall send the message using sendEventAsync.**]**
             */
            this.deviceIO.sendEventAsync(desiredPropertiesNotificationRequest, new deviceTwinRequestMessageCallback(), null);
        }
    }
}
