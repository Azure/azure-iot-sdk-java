// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.twin;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
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
    private InternalClient client = null;
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
    private final PropertyCallback deviceTwinGenericPropertyChangeCallback;
    private final TwinPropertyCallback deviceTwinGenericTwinPropertyChangeCallback;
    private Object deviceTwinGenericPropertyChangeCallbackContext;

    // Callback for providing user all of a given desired property update message's contents, rather than providing
    // one callback per updated property.
    private final TwinPropertiesCallback deviceTwinGenericTwinPropertiesChangeCallback;

    /*
        Map of callbacks to call when a particular desired property changed
     */
    private ConcurrentSkipListMap<String, Pair<PropertyCallback<String, Object>, Object>> onDesiredPropertyChangeMap;
    private ConcurrentSkipListMap<String, Pair<TwinPropertyCallback, Object>> onDesiredTwinPropertyChangeMap;

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
                // properties callback, then onStatusChanged the callback so that they receive the full twin payload.
                if (!desiredPropertyMap.isEmpty() && this.deviceTwinGenericTwinPropertiesChangeCallback != null)
                {
                    deviceTwinGenericTwinPropertiesChangeCallback.onPropertiesChanged(desiredPropertyMap, deviceTwinGenericPropertyChangeCallbackContext);
                }

                for (String propertyKey : desiredPropertyMap.keySet())
                {
                    Property property = this.getDesiredProperty(desiredPropertyMap, propertyKey);

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
                    Property property = this.getReportedProperty(reportedPropertyMap, propertyKey);
                    if (deviceTwinGenericTwinPropertyChangeCallback != null)
                    {
                        deviceTwinGenericTwinPropertyChangeCallback.onPropertyChanged(property, deviceTwinGenericPropertyChangeCallbackContext);
                    }
                }
            }
        }
    }

    private Property getDesiredProperty(TwinCollection twinCollection, String key)
    {
        Object value = twinCollection.get(key);
        Integer propertyVersion = twinCollection.getVersion();
        TwinMetadata metadata = twinCollection.getTwinMetadata(key);
        Date lastUpdated = null;
        Integer lastUpdatedVersion = null;
        String lastUpdatedBy = null;
        String lastUpdatedByDigest = null;
        if (metadata != null)
        {
            lastUpdated = metadata.getLastUpdated();
            lastUpdatedVersion = metadata.getLastUpdatedVersion();
            lastUpdatedBy = metadata.getLastUpdatedBy();
            lastUpdatedByDigest = metadata.getLastUpdatedByDigest();
        }
        return new Property(
                key, value,
                propertyVersion,
                false,
                lastUpdated,
                lastUpdatedVersion,
                lastUpdatedBy,
                lastUpdatedByDigest);
    }

    private Property getReportedProperty(TwinCollection twinCollection, String key)
    {
        Object value = twinCollection.get(key);
        Integer propertyVersion = twinCollection.getVersion();
        TwinMetadata metadata = twinCollection.getTwinMetadata(key);
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
                true,
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

    public <Type1, Type2> DeviceTwin(InternalClient client,
                      IotHubEventCallback deviceTwinCallback, Object deviceTwinCallbackContext,
                      PropertyCallback<Type1, Type2> genericPropertyCallback, Object genericPropertyCallbackContext)
    {
        deviceTwinInternal(client, deviceTwinCallback, deviceTwinCallbackContext, genericPropertyCallbackContext);

        this.deviceTwinGenericPropertyChangeCallback = genericPropertyCallback;
        this.deviceTwinGenericTwinPropertyChangeCallback = null;
        this.deviceTwinGenericTwinPropertiesChangeCallback = null;
    }

    public DeviceTwin(InternalClient client,
                      IotHubEventCallback deviceTwinCallback, Object deviceTwinCallbackContext,
                      TwinPropertyCallback genericPropertyCallback, Object genericPropertyCallbackContext)
    {
        deviceTwinInternal(client, deviceTwinCallback, deviceTwinCallbackContext, genericPropertyCallbackContext);

        this.deviceTwinGenericTwinPropertyChangeCallback = genericPropertyCallback;
        this.deviceTwinGenericPropertyChangeCallback = null;
        this.deviceTwinGenericTwinPropertiesChangeCallback = null;
    }

    public DeviceTwin(InternalClient client,
                      IotHubEventCallback deviceTwinCallback, Object deviceTwinCallbackContext,
                      TwinPropertiesCallback genericPropertiesCallback, Object genericPropertyCallbackContext)
    {
        deviceTwinInternal(client, deviceTwinCallback, deviceTwinCallbackContext, genericPropertyCallbackContext);

        this.deviceTwinGenericTwinPropertiesChangeCallback = genericPropertiesCallback;
        this.deviceTwinGenericTwinPropertyChangeCallback = null;
        this.deviceTwinGenericPropertyChangeCallback = null;
    }

    private void deviceTwinInternal(InternalClient client,
                      IotHubEventCallback deviceTwinCallback, Object deviceTwinCallbackContext,
                      Object genericPropertyCallbackContext)
    {
        if (client == null)
        {
            throw new IllegalArgumentException("Client or config cannot be null");
        }

        this.client = client;
        this.config = client.getConfig();
        this.config.setDeviceTwinMessageCallback(new deviceTwinResponseMessageCallback(), null);
        this.deviceTwinStatusCallback = deviceTwinCallback;
        this.deviceTwinStatusCallbackContext = deviceTwinCallbackContext;
        this.deviceTwinGenericPropertyChangeCallbackContext = genericPropertyCallbackContext;
    }

    public void getDeviceTwinAsync()
    {
        checkSubscription();

        IotHubTransportMessage getTwinRequestMessage = new IotHubTransportMessage(new byte[0], MessageType.DEVICE_TWIN);
        getTwinRequestMessage.setRequestId(UUID.randomUUID().toString());
        getTwinRequestMessage.setCorrelationId(getTwinRequestMessage.getRequestId());
        getTwinRequestMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_GET_REQUEST);
        this.client.sendEventAsync(getTwinRequestMessage, new deviceTwinRequestMessageCallback(), null);
    }

    public synchronized void updateReportedPropertiesAsync(Set<Property> reportedProperties) throws IllegalStateException
    {
        this.updateReportedPropertiesAsync(reportedProperties, null, null, null, new deviceTwinRequestMessageCallback(), null);
    }

    public synchronized void updateReportedPropertiesAsync(Set<Property> reportedProperties, Integer version) throws IllegalStateException
    {
        this.updateReportedPropertiesAsync(reportedProperties, version, null, null, new deviceTwinRequestMessageCallback(), null);
    }

    public synchronized void updateReportedPropertiesAsync(Set<Property> reportedProperties, Integer version, CorrelatingMessageCallback correlatingMessageCallback, Object correlatingMessageCallbackContext, IotHubEventCallback reportedPropertiesCallback, Object callbackContext)
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
                throw new IllegalStateException("Duplicate keys found in reported properties: " + p.getKey());
            }

            reportedPropertiesMap.put(p.getKey(), p.getValue());
        }
        String serializedReportedProperties = reportedPropertiesMap.toJsonElement().toString();

        if (serializedReportedProperties == null)
        {
            return;
        }

        IotHubTransportMessage updateReportedPropertiesRequest = new IotHubTransportMessage(serializedReportedProperties.getBytes(StandardCharsets.UTF_8), MessageType.DEVICE_TWIN);
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
        this.client.sendEventAsync(updateReportedPropertiesRequest, reportedPropertiesCallback, callbackContext);
    }

    public void subscribeDesiredPropertiesNotification(Map<Property, Pair<PropertyCallback<String, Object>, Object>> onDesiredPropertyChange)
    {
        if (onDesiredPropertyChangeMap == null)
        {
            onDesiredPropertyChangeMap = new ConcurrentSkipListMap<>();
        }

        if (onDesiredPropertyChange != null)
        {
            for (Map.Entry<Property, Pair<PropertyCallback<String, Object>, Object>> desired : onDesiredPropertyChange.entrySet())
            {
                onDesiredPropertyChangeMap.put(desired.getKey().getKey(), desired.getValue());
            }
        }

        checkSubscription();
    }

    public void subscribeDesiredPropertiesTwinPropertyNotification(Map<Property, Pair<TwinPropertyCallback, Object>> onDesiredPropertyChange)
    {
        if (onDesiredTwinPropertyChangeMap == null)
        {
            onDesiredTwinPropertyChangeMap = new ConcurrentSkipListMap<>();
        }

        if (onDesiredPropertyChange != null)
        {
            for (Map.Entry<Property, Pair<TwinPropertyCallback, Object>> desired : onDesiredPropertyChange.entrySet())
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
            this.client.sendEventAsync(desiredPropertiesNotificationRequest, new deviceTwinRequestMessageCallback(), null);
        }
    }

    private boolean reportPropertyCallback(Property property)
    {
        boolean reported = false;

        if (onDesiredPropertyChangeMap != null && onDesiredPropertyChangeMap.containsKey(property.getKey()))
        {
            Pair<PropertyCallback<String, Object>, Object> callBackObjectPair = onDesiredPropertyChangeMap.get(property.getKey());
            if (callBackObjectPair != null && callBackObjectPair.getKey() != null)
            {
                callBackObjectPair.getKey().onPropertyChanged(property.getKey(), property.getValue(), callBackObjectPair.getValue());
                reported = true;
            }
        }

        if (onDesiredTwinPropertyChangeMap != null && onDesiredTwinPropertyChangeMap.containsKey(property.getKey()))
        {
            Pair<TwinPropertyCallback, Object> callBackObjectPair = onDesiredTwinPropertyChangeMap.get(property.getKey());
            if (callBackObjectPair != null && callBackObjectPair.getKey() != null)
            {
                callBackObjectPair.getKey().onPropertyChanged(property, callBackObjectPair.getValue());
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
            deviceTwinGenericPropertyChangeCallback.onPropertyChanged(property.getKey(), property.getValue(), deviceTwinGenericPropertyChangeCallbackContext);
            return true;
        }

        if (deviceTwinGenericTwinPropertyChangeCallback != null)
        {
            deviceTwinGenericTwinPropertyChangeCallback.onPropertyChanged(property, deviceTwinGenericPropertyChangeCallbackContext);
            return true;
        }

        return false;
    }
}
