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
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;

import static com.microsoft.azure.sdk.iot.device.IotHubMessageResult.ABANDON;
import static com.microsoft.azure.sdk.iot.device.IotHubMessageResult.COMPLETE;

@Slf4j
public class DeviceTwin
{
    private String requestId;
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
    @SuppressWarnings("rawtypes")
    private final PropertyCallBack deviceTwinGenericPropertyChangeCallback;
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
        @Override
        public IotHubMessageResult execute(Message message, Object callbackContext)
        {
            synchronized (DEVICE_TWIN_LOCK)
            {
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

                        log.trace("Executing twin callback for message {}", dtMessage);
                        deviceTwinStatusCallback.execute(iotHubStatus, deviceTwinStatusCallbackContext);
                        log.trace("Twin callback returned for message {}", dtMessage);

                        if (iotHubStatus == IotHubStatusCode.OK)
                        {
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

                        log.trace("Executing twin status callback for device operation twin update reported properties response with status " + iotHubStatus);
                        deviceTwinStatusCallback.execute(iotHubStatus, deviceTwinStatusCallbackContext);
                        break;
                    }
                    case DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE:
                    {
                        isSubscribed = true;
                        TwinState twinState = TwinState.createFromDesiredPropertyJson(new String(dtMessage.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));

                        if (twinState.getDesiredProperty() != null)
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

                for (String propertyKey : desiredPropertyMap.keySet())
                {
                    Property property = this.getProperty(desiredPropertyMap, propertyKey, false);

                    if (!reportPropertyCallback(property))
                    {
                        if (reportDeviceTwinGenericPropertyCallback(property))
                        {
                            log.info("The user subscribed desired property callback was triggered.");
                        }
                        else
                        {
                            log.debug("The user has not subscribed to desired property callback, no action was taken.");
                        }
                    }
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
                for (String propertyKey : reportedPropertyMap.keySet())
                {
                    Property property = this.getProperty(reportedPropertyMap, propertyKey, true);
                    if (deviceTwinGenericTwinPropertyChangeCallback != null)
                    {
                        deviceTwinGenericTwinPropertyChangeCallback.TwinPropertyCallBack(property, deviceTwinGenericPropertyChangeCallbackContext);
                    }
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
        if (metadata != null)
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
                if ((responseStatus != IotHubStatusCode.OK) && (responseStatus != IotHubStatusCode.OK_EMPTY))
                {
                    deviceTwinStatusCallback.execute(responseStatus, deviceTwinStatusCallbackContext);
                }
            }
        }
    }

    public <Type1, Type2> DeviceTwin(DeviceIO client, DeviceClientConfig config,
                      IotHubEventCallback deviceTwinCallback, Object deviceTwinCallbackContext,
                      PropertyCallBack<Type1, Type2> genericPropertyCallback, Object genericPropertyCallbackContext)
    {
        deviceTwinInternal(client, config, deviceTwinCallback, deviceTwinCallbackContext, genericPropertyCallbackContext);

        this.deviceTwinGenericPropertyChangeCallback = genericPropertyCallback;
        this.deviceTwinGenericTwinPropertyChangeCallback = null;
        this.deviceTwinGenericTwinPropertiesChangeCallback = null;
    }

    public DeviceTwin(DeviceIO client, DeviceClientConfig config,
                      IotHubEventCallback deviceTwinCallback, Object deviceTwinCallbackContext,
                      TwinPropertyCallBack genericPropertyCallback, Object genericPropertyCallbackContext)
    {
        deviceTwinInternal(client, config, deviceTwinCallback, deviceTwinCallbackContext, genericPropertyCallbackContext);

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
        if (client == null || config == null)
        {
            throw new IllegalArgumentException("Client or config cannot be null");
        }

        this.deviceIO = client;
        this.config = config;
        this.config.setDeviceTwinMessageCallback(new deviceTwinResponseMessageCallback(), null);
        this.requestId = UUID.randomUUID().toString();
        this.deviceTwinStatusCallback = deviceTwinCallback;
        this.deviceTwinStatusCallbackContext = deviceTwinCallbackContext;
        this.deviceTwinGenericPropertyChangeCallbackContext = genericPropertyCallbackContext;
    }

    public void getDeviceTwin()
    {
        checkSubscription();

        IotHubTransportMessage getTwinRequestMessage = new IotHubTransportMessage(new byte[0], MessageType.DEVICE_TWIN);
        getTwinRequestMessage.setRequestId(UUID.randomUUID().toString());
        getTwinRequestMessage.setCorrelationId(getTwinRequestMessage.getRequestId());
        getTwinRequestMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_GET_REQUEST);
        this.deviceIO.sendEventAsync(getTwinRequestMessage, new deviceTwinRequestMessageCallback(), null, this.config.getDeviceId());
    }

    public synchronized void updateReportedProperties(Set<Property> reportedProperties) throws IOException
    {
        this.updateReportedProperties(reportedProperties, null, null, null, new deviceTwinRequestMessageCallback(), null);
    }

    public synchronized void updateReportedProperties(Set<Property> reportedProperties, Integer version) throws IOException
    {
        this.updateReportedProperties(reportedProperties, version, null, null, new deviceTwinRequestMessageCallback(), null);
    }

    public synchronized void updateReportedProperties(Set<Property> reportedProperties, Integer version, CorrelatingMessageCallback correlatingMessageCallback, Object correlatingMessageCallbackContext, IotHubEventCallback reportedPropertiesCallback, Object callbackContext) throws IOException
    {
        if (reportedProperties == null)
        {
            throw new IllegalArgumentException("Reported properties cannot be null");
        }

        TwinCollection reportedPropertiesMap = new TwinCollection();
        for (Property p : reportedProperties)
        {
            if (reportedPropertiesMap.containsKey(p.getKey()))
            {
                throw new IOException("Duplicate keys found in reported properties: " + p.getKey());
            }

            reportedPropertiesMap.putFinal(p.getKey(), p.getValue());
        }
        String serializedReportedProperties = reportedPropertiesMap.toJsonElement().toString();

        if (serializedReportedProperties == null)
        {
            return;
        }

        IotHubTransportMessage updateReportedPropertiesRequest = new IotHubTransportMessage(serializedReportedProperties.getBytes(), MessageType.DEVICE_TWIN);
        updateReportedPropertiesRequest.setCorrelatingMessageCallback(correlatingMessageCallback);
        updateReportedPropertiesRequest.setCorrelatingMessageCallbackContext(correlatingMessageCallbackContext);
        updateReportedPropertiesRequest.setConnectionDeviceId(this.config.getDeviceId());

        // MQTT does not have the concept of correlationId for request/response handling but it does have a requestId
        // To handle this we are setting the correlationId to the requestId to better handle correlation
        // whether we use MQTT or AMQP.
        updateReportedPropertiesRequest.setRequestId(UUID.randomUUID().toString());
        updateReportedPropertiesRequest.setCorrelationId(updateReportedPropertiesRequest.getRequestId());

        if (version != null)
        {
            updateReportedPropertiesRequest.setVersion(Integer.toString(version));
        }


        updateReportedPropertiesRequest.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST);
        this.deviceIO.sendEventAsync(updateReportedPropertiesRequest, reportedPropertiesCallback, callbackContext, this.config.getDeviceId());
    }

    public void subscribeDesiredPropertiesNotification(Map<Property, Pair<PropertyCallBack<String, Object>, Object>> onDesiredPropertyChange)
    {
        if (onDesiredPropertyChangeMap == null)
        {
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
            IotHubTransportMessage desiredPropertiesNotificationRequest = new IotHubTransportMessage(new byte[0], MessageType.DEVICE_TWIN);
            desiredPropertiesNotificationRequest.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST);
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

    // Public API limits us to taking a generic PropertyCallback<Type1, Type2> when it probably should have been
    // PropertyCallback<String, Type1>. This unchecked cast should be fine though as long as we are relaying valid json
    // to the user since all json keys are strings
    @SuppressWarnings("unchecked")
    private boolean reportDeviceTwinGenericPropertyCallback(Property property)
    {
        if (deviceTwinGenericPropertyChangeCallback != null)
        {
            deviceTwinGenericPropertyChangeCallback.PropertyCall(property.getKey(), property.getValue(), deviceTwinGenericPropertyChangeCallbackContext);
            return true;
        }

        if (deviceTwinGenericTwinPropertyChangeCallback != null)
        {
            deviceTwinGenericTwinPropertyChangeCallback.TwinPropertyCallBack(property, deviceTwinGenericPropertyChangeCallbackContext);
            return true;
        }

        return false;
    }
}
