// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.DeviceTwin;

import com.microsoft.azure.sdk.iot.deps.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.deps.twin.TwinMetadata;
import com.microsoft.azure.sdk.iot.deps.twin.TwinState;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

import static com.microsoft.azure.sdk.iot.device.IotHubMessageResult.ABANDON;
import static com.microsoft.azure.sdk.iot.device.IotHubMessageResult.COMPLETE;

@Slf4j
public class DeviceTwin
{
    private int requestId;
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
    private final PropertyCallBack<String, Object> deviceTwinGenericPropertyChangeCallback;
    private final TwinPropertyCallBack deviceTwinGenericTwinPropertyChangeCallback;
    private Object deviceTwinGenericPropertyChangeCallbackContext;

    // Callback for providing user all of a given desired property update message's contents, rather than providing
    // one callback per updated property.
    private final TwinPropertiesCallback deviceTwinGenericTwinPropertiesChangeCallback;

    /*
        Map of callbacks to call when a particular desired property changed
     */
    private ConcurrentSkipListMap<String, Pair<PropertyCallBack<String, Object>, Object>> onDesiredPropertyChangeMap;
    private ConcurrentSkipListMap<String, Pair<TwinPropertyCallBack, Object>> onDesiredTwinPropertyChangeMap;

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
                 **Codes_SRS_DEVICETWIN_25_028: [**If the message is of type DEVICE_TWIN and DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_RESPONSE and if the status is null then the user is notified on the status callback registered by the user as ERROR.**]**
                 **Codes_SRS_DEVICETWIN_25_031: [**If the message is of type DEVICE_TWIN and DEVICE_OPERATION_TWIN_GET_RESPONSE and if the status is null then the user is notified on the status callback registered by the user as ERROR.**]**
                 */
                IotHubStatusCode iotHubStatus = IotHubStatusCode.ERROR;
                if (message.getMessageType() != MessageType.DEVICE_TWIN)
                {
                    log.warn("Unexpected message type received; abandoning it");
                    deviceTwinStatusCallback.execute(iotHubStatus, deviceTwinStatusCallbackContext);
                    return ABANDON;
                }

                IotHubTransportMessage dtMessage = (IotHubTransportMessage) message;
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
                         **Codes_SRS_DEVICETWIN_25_029: [**If the message is of type DEVICE_TWIN and DEVICE_OPERATION_TWIN_GET_RESPONSE then the user call with a valid status is triggered.**]**
                         */

                        log.trace("Executing twin callback for message {}", dtMessage);
                        deviceTwinStatusCallback.execute(iotHubStatus, deviceTwinStatusCallbackContext);
                        log.trace("Twin callback returned for message {}", dtMessage);

                        if (iotHubStatus == IotHubStatusCode.OK)
                        {
                            /*
                             **Codes_SRS_DEVICETWIN_25_030: [**If the message is of type DEVICE_TWIN and DEVICE_OPERATION_TWIN_GET_RESPONSE then the payload is deserialized only if the status is ok.**]**
                             */
                            TwinState twinState = TwinState.createFromPropertiesJson(new String(dtMessage.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));
                            if (twinState.getDesiredProperty() != null)
                            {
                                OnDesiredPropertyChanged(twinState.getDesiredProperty());
                            }
                            if (twinState.getReportedProperty() != null)
                            {
                                OnReportedPropertyChanged(twinState.getReportedProperty());
                            }
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
                         **Codes_SRS_DEVICETWIN_25_027: [**If the message is of type DEVICE_TWIN and DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_RESPONSE then the user call with a valid status is triggered.**]**
                         */
                        log.trace("Executing twin status callback for device operation twin update reported properties response with status " + iotHubStatus);
                        deviceTwinStatusCallback.execute(iotHubStatus, deviceTwinStatusCallbackContext);
                        break;
                    }
                    case DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE:
                    {
                        /*
                         **Codes_SRS_DEVICETWIN_25_026: [**If the message is of type DEVICE_TWIN and DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE then the payload is deserialized.**]**
                         */
                        isSubscribed = true;
                        TwinState twinState = TwinState.createFromDesiredPropertyJson(new String(dtMessage.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));

                        if(twinState.getDesiredProperty() != null)
                        {
                            OnDesiredPropertyChanged(twinState.getDesiredProperty());
                        }

                        break;
                    }
                    default:
                        break;
                }
                return COMPLETE;
            }
        }
    }

    private void OnDesiredPropertyChanged(TwinCollection desiredPropertyMap)
    {
        synchronized (DEVICE_TWIN_LOCK)
        {
            if (desiredPropertyMap != null)
            {
                // If any desired properties are present, and the user has subscribed to receive the generic twin
                // properties callback, then execute the callback so that they receive the full twin payload.
                if (!desiredPropertyMap.isEmpty() && this.deviceTwinGenericTwinPropertiesChangeCallback != null)
                {
                    deviceTwinGenericTwinPropertiesChangeCallback.TwinPropertiesCallBack(desiredPropertyMap, deviceTwinGenericPropertyChangeCallbackContext);
                }

                for (Iterator desiredPropertyIt = desiredPropertyMap.entrySet().iterator(); desiredPropertyIt.hasNext();)
                {
                    Map.Entry<String, String> desiredProperty = (Map.Entry<String, String>) desiredPropertyIt.next();
                    Property property = this.getProperty(desiredPropertyMap, desiredProperty.getKey(), false);

                    /*
                     **Codes_SRS_DEVICETWIN_25_022: [**OnDesiredPropertyChange callback shall look for the user registered call back on the property that changed provided in desiredPropertyMap and call the user providing the desired property change key and value pair**]**
                     */
                    if (!reportPropertyCallback(property))
                    {
                        /*
                         **Codes_SRS_DEVICETWIN_25_023: [**OnDesiredPropertyChange callback shall look for the user registered call back on the property that changed and if no callback is registered or is null then OnDesiredPropertyChange shall call the user on generic callback providing with the desired property change key and value pair**]**
                         */
                        reportDeviceTwinGenericPropertyCallback(property);
                    }
                    desiredPropertyIt.remove();
                }
            }
        }
    }

    private void OnReportedPropertyChanged(TwinCollection reportedPropertyMap)
    {
        synchronized (DEVICE_TWIN_LOCK)
        {
            if (reportedPropertyMap != null)
            {
                for (Iterator reportedPropertyIt = reportedPropertyMap.entrySet().iterator(); reportedPropertyIt.hasNext();)
                {
                    Map.Entry<String, String> reportedProperty = (Map.Entry<String, String>) reportedPropertyIt.next();
                    Property property = this.getProperty(reportedPropertyMap, reportedProperty.getKey(), true);

                    /*
                     **Codes_SRS_DEVICETWIN_25_023: [**OnReportedPropertyChanged callback shall look for the user registered call back on the property that changed and if no callback is registered or is null then OnDesiredPropertyChange shall call the user on generic callback providing with the desired property change key and value pair**]**
                     */
                    if(deviceTwinGenericTwinPropertyChangeCallback != null)
                    {
                        deviceTwinGenericTwinPropertyChangeCallback.TwinPropertyCallBack(property, deviceTwinGenericPropertyChangeCallbackContext);
                    }
                    reportedPropertyIt.remove();
                }
            }
        }
    }

    private Property getProperty(TwinCollection twinCollection, String key, boolean isReported)
    {
        Object value = twinCollection.get(key);
        Integer propertyVersion = twinCollection.getVersionFinal();
        TwinMetadata metadata = twinCollection.getTwinMetadataFinal(key);
        Date lastUpdated = null;
        Integer lastUpdatedVersion = null;
        if(metadata != null)
        {
            lastUpdated = metadata.getLastUpdated();
            lastUpdatedVersion = metadata.getLastUpdatedVersion();
        }
        return new Property(
                key, value,
                propertyVersion,
                isReported,
                lastUpdated,
                lastUpdatedVersion);
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
                if((responseStatus != IotHubStatusCode.OK) && (responseStatus != IotHubStatusCode.OK_EMPTY))
                {
                    deviceTwinStatusCallback.execute(responseStatus, deviceTwinStatusCallbackContext);
                }
            }
        }
    }

    public DeviceTwin(DeviceIO client, DeviceClientConfig config,
                      IotHubEventCallback deviceTwinCallback, Object deviceTwinCallbackContext,
                      PropertyCallBack genericPropertyCallback, Object genericPropertyCallbackContext)
    {
        deviceTwinInternal(client, config, deviceTwinCallback, deviceTwinCallbackContext, genericPropertyCallbackContext);

        /*
         **Codes_SRS_DEVICETWIN_21_004: [**The constructor shall save the generic property callback.**]**
         */
        this.deviceTwinGenericPropertyChangeCallback = genericPropertyCallback;
        this.deviceTwinGenericTwinPropertyChangeCallback = null;
        this.deviceTwinGenericTwinPropertiesChangeCallback = null;

    }

    public DeviceTwin(DeviceIO client, DeviceClientConfig config,
                      IotHubEventCallback deviceTwinCallback, Object deviceTwinCallbackContext,
                      TwinPropertyCallBack genericPropertyCallback, Object genericPropertyCallbackContext)
    {
        deviceTwinInternal(client, config, deviceTwinCallback, deviceTwinCallbackContext, genericPropertyCallbackContext);

        /*
         **Codes_SRS_DEVICETWIN_21_004: [**The constructor shall save the generic property callback.**]**
         */
        this.deviceTwinGenericTwinPropertyChangeCallback = genericPropertyCallback;
        this.deviceTwinGenericPropertyChangeCallback = null;
        this.deviceTwinGenericTwinPropertiesChangeCallback = null;
    }

    public DeviceTwin(DeviceIO client, DeviceClientConfig config,
                      IotHubEventCallback deviceTwinCallback, Object deviceTwinCallbackContext,
                      TwinPropertiesCallback genericPropertiesCallback, Object genericPropertyCallbackContext)
    {
        deviceTwinInternal(client, config, deviceTwinCallback, deviceTwinCallbackContext, genericPropertyCallbackContext);

        this.deviceTwinGenericTwinPropertiesChangeCallback = genericPropertiesCallback;
        this.deviceTwinGenericTwinPropertyChangeCallback = null;
        this.deviceTwinGenericPropertyChangeCallback = null;
    }

    private void deviceTwinInternal(DeviceIO client, DeviceClientConfig config,
                      IotHubEventCallback deviceTwinCallback, Object deviceTwinCallbackContext,
                      Object genericPropertyCallbackContext)
    {
        /*
         **Codes_SRS_DEVICETWIN_25_001: [**The constructor shall throw IllegalArgumentException Exception if any of the parameters i.e client, config are null. **]**
         */
        if (client == null || config == null)
        {
            throw new IllegalArgumentException("Client or config cannot be null");
        }

        /*
         **Codes_SRS_DEVICETWIN_25_003: [**The constructor shall save all the parameters specified i.e client, config, deviceTwinCallback.**]**
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

        this.deviceTwinGenericPropertyChangeCallbackContext = genericPropertyCallbackContext;
    }

    public void getDeviceTwin()
    {
        checkSubscription();

        /*
         **Codes_SRS_DEVICETWIN_25_005: [**The method shall create a device twin message with empty payload to be sent IotHub.**]**
         */
        IotHubTransportMessage getTwinRequestMessage = new IotHubTransportMessage(new byte[0], MessageType.DEVICE_TWIN);

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
        this.deviceIO.sendEventAsync(getTwinRequestMessage, new deviceTwinRequestMessageCallback(), null, this.config.getDeviceId());
    }

    public synchronized void updateReportedProperties(Set<Property> reportedProperties) throws IOException
    {
        this.updateReportedProperties(reportedProperties, null);
    }

    public synchronized void updateReportedProperties(Set<Property> reportedProperties, Integer version) throws IOException
    {
        if (reportedProperties == null)
        {
            /*
             **Codes_SRS_DEVICETWIN_25_009: [**The method shall throw IllegalArgumentException Exception if reportedProperties is null.**]**
             */
            throw new IllegalArgumentException("Reported properties cannot be null");
        }

        /*
         **Codes_SRS_DEVICETWIN_25_011: [**The method shall serialize the properties using the TwinCollection.**]**
         */
        TwinCollection reportedPropertiesMap = new TwinCollection();
        for(Property p : reportedProperties)
        {
            if (reportedPropertiesMap.containsKey(p.getKey()))
            {
                //Codes_SRS_DEVICETWIN_34_032: [If the provided set of properties contains two keys with the same name, this function shall throw an IOException.]
                throw new IOException("Duplicate keys found in reported properties: " + p.getKey());
            }

            reportedPropertiesMap.putFinal(p.getKey(), p.getValue());
        }
        String serializedReportedProperties = reportedPropertiesMap.toJsonElement().toString();

        if (serializedReportedProperties == null)
        {
            return;
        }

        /*
         **Codes_SRS_DEVICETWIN_25_012: [**The method shall create a device twin message with the serialized payload if not null to be sent IotHub and shall include the connection device id of the sending device.**]**
         */
        IotHubTransportMessage updateReportedPropertiesRequest = new IotHubTransportMessage(serializedReportedProperties.getBytes(), MessageType.DEVICE_TWIN);
        updateReportedPropertiesRequest.setConnectionDeviceId(this.config.getDeviceId());

        /*
         **Codes_SRS_DEVICETWIN_25_014: [**This method shall set the request id for the message by calling setRequestId .**]**
         */
        updateReportedPropertiesRequest.setRequestId(String.valueOf(requestId++));

        if(version != null)
        {
            /*
             **Codes_SRS_DEVICETWIN_21_024: [**If the version is provided, this method shall set the version for the message by calling setVersion .**]**
             */
            updateReportedPropertiesRequest.setVersion(Integer.toString(version));
        }

        /*
         **Codes_SRS_DEVICETWIN_25_013: [**This method shall set the message type as DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST by calling setDeviceOperationType.**]**
         */
        updateReportedPropertiesRequest.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST);

        /*
         **Codes_SRS_DEVICETWIN_25_015: [**This method shall send the message to the lower transport layers by calling sendEventAsync.**]**
         */
        this.deviceIO.sendEventAsync(updateReportedPropertiesRequest, new deviceTwinRequestMessageCallback(), null, this.config.getDeviceId());

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

        checkSubscription();
    }

    public void subscribeDesiredPropertiesTwinPropertyNotification(Map<Property, Pair<TwinPropertyCallBack, Object>> onDesiredPropertyChange)
    {
        if (onDesiredTwinPropertyChangeMap == null)
        {
            /*
             **Codes_SRS_DEVICETWIN_25_017: [**The method shall create a treemap to store callbacks for desired property notifications specified in onDesiredPropertyChange.**]**
             */
            onDesiredTwinPropertyChangeMap = new ConcurrentSkipListMap<>();
        }

        if (onDesiredPropertyChange != null)
        {
            for (Map.Entry<Property, Pair<TwinPropertyCallBack, Object>> desired : onDesiredPropertyChange.entrySet())
            {
                onDesiredTwinPropertyChangeMap.put(desired.getKey().getKey(), desired.getValue());
            }
        }

        checkSubscription();
    }

    private void checkSubscription()
    {
        if (!isSubscribed)
        {
            /*
             **Codes_SRS_DEVICETWIN_25_018: [**If not already subscribed then this method shall create a device twin message with empty payload and set its type as DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST.**]**
             */
            IotHubTransportMessage desiredPropertiesNotificationRequest = new IotHubTransportMessage(new byte[0], MessageType.DEVICE_TWIN);

            desiredPropertiesNotificationRequest.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST);

            /*
             **Codes_SRS_DEVICETWIN_25_019: [**If not already subscribed then this method shall send the message using sendEventAsync.**]**
             */
            this.deviceIO.sendEventAsync(desiredPropertiesNotificationRequest, new deviceTwinRequestMessageCallback(), null, this.config.getDeviceId());
        }
    }

    private boolean reportPropertyCallback(Property property)
    {
        boolean reported = false;

        if (onDesiredPropertyChangeMap != null && onDesiredPropertyChangeMap.containsKey(property.getKey()))
        {
            Pair<PropertyCallBack<String, Object>, Object> callBackObjectPair = onDesiredPropertyChangeMap.get(property.getKey());
            if (callBackObjectPair != null && callBackObjectPair.getKey() != null)
            {
                callBackObjectPair.getKey().PropertyCall(property.getKey(), property.getValue(), callBackObjectPair.getValue());
                reported = true;
            }
        }

        if (onDesiredTwinPropertyChangeMap != null && onDesiredTwinPropertyChangeMap.containsKey(property.getKey()))
        {
            Pair<TwinPropertyCallBack, Object> callBackObjectPair = onDesiredTwinPropertyChangeMap.get(property.getKey());
            if (callBackObjectPair != null && callBackObjectPair.getKey() != null)
            {
                callBackObjectPair.getKey().TwinPropertyCallBack(property, callBackObjectPair.getValue());
                reported = true;
            }
        }

        return reported;
    }

    private boolean reportDeviceTwinGenericPropertyCallback(Property property)
    {
        if(deviceTwinGenericPropertyChangeCallback != null)
        {
            deviceTwinGenericPropertyChangeCallback.PropertyCall(property.getKey(), property.getValue(), deviceTwinGenericPropertyChangeCallbackContext);
            return true;
        }

        if(deviceTwinGenericTwinPropertyChangeCallback != null)
        {
            deviceTwinGenericTwinPropertyChangeCallback.TwinPropertyCallBack(property, deviceTwinGenericPropertyChangeCallbackContext);
            return true;
        }

        return false;
    }
}
