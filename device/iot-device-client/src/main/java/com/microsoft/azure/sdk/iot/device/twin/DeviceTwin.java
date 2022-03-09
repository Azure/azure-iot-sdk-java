// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.twin;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

import static com.microsoft.azure.sdk.iot.device.IotHubMessageResult.ABANDON;
import static com.microsoft.azure.sdk.iot.device.IotHubMessageResult.COMPLETE;

@Slf4j
public class DeviceTwin implements MessageCallback
{
    private final InternalClient client;

    private DesiredPropertiesCallback desiredPropertiesCallback;
    private Object desiredPropertiesUpdateCallbackContext; // may be null

    public DeviceTwin(InternalClient client)
    {
        if (client == null)
        {
            throw new IllegalArgumentException("Client or config cannot be null");
        }

        this.client = client;
        this.client.getConfig().setDeviceTwinMessageCallback(this, null);
    }

    @Override
    public IotHubMessageResult execute(Message message, Object callbackContext)
    {
        if (message.getMessageType() != MessageType.DEVICE_TWIN)
        {
            log.warn("Unexpected message type received. Abandoning it");
            return ABANDON;
        }

        IotHubTransportMessage dtMessage = (IotHubTransportMessage) message;

        if (dtMessage.getDeviceOperationType() == DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE)
        {
            Twin twin = Twin.createFromDesiredPropertyJson(new String(dtMessage.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));
            this.desiredPropertiesCallback.onDesiredPropertiesUpdated(twin, desiredPropertiesUpdateCallbackContext);
        }

        return COMPLETE;
    }

    public void getTwinAsync(
        GetTwinCorrelatingMessageCallback twinCallback,
        Object callbackContext)
    {
        if (desiredPropertiesCallback == null)
        {
            throw new IllegalStateException("Must subscribe to desired properties before getting twin.");
        }

        Objects.requireNonNull(twinCallback, "Must provide a non-null callback for receiving the twin");

        IotHubTransportMessage getTwinRequestMessage = new IotHubTransportMessage(new byte[0], MessageType.DEVICE_TWIN);
        getTwinRequestMessage.setRequestId(UUID.randomUUID().toString());
        getTwinRequestMessage.setCorrelationId(getTwinRequestMessage.getRequestId());
        getTwinRequestMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_GET_REQUEST);
        getTwinRequestMessage.setCorrelatingMessageCallback(new CorrelatingMessageCallback()
        {
            @Override
            public void onRequestQueued(Message message, Object callbackContext)
            {
                twinCallback.onRequestQueued(message, callbackContext);
            }

            @Override
            public void onRequestSent(Message message, Object callbackContext)
            {
                twinCallback.onRequestSent(message, callbackContext);
            }

            @Override
            public void onRequestAcknowledged(Message message, Object callbackContext, TransportException e)
            {
                twinCallback.onRequestAcknowledged(message, callbackContext, e);
            }

            @Override
            public void onResponseReceived(Message message, Object callbackContext, TransportException e)
            {
                int status = Integer.parseInt(((IotHubTransportMessage) message).getStatus());
                Twin twin = Twin.createFromPropertiesJson(new String(message.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));
                twinCallback.onResponseReceived(twin, message, callbackContext, IotHubStatusCode.getIotHubStatusCode(status), e);
            }

            @Override
            public void onResponseAcknowledged(Message message, Object callbackContext)
            {
                twinCallback.onResponseAcknowledged(message, callbackContext);
            }
        });
        getTwinRequestMessage.setCorrelatingMessageCallbackContext(callbackContext);

        IotHubEventCallback onMessageAcknowledgedCallback = (responseStatus, context) ->
        {
            // no action needed here. The correlating message callback will handle the various message state callbacks including this one
        };

        this.client.sendEventAsync(getTwinRequestMessage, onMessageAcknowledgedCallback,null);
    }

    public void updateReportedPropertiesAsync(
        TwinCollection reportedProperties,
        ReportedPropertiesUpdateCorrelatingMessageCallback reportedPropertiesUpdateCorrelatingMessageCallback,
        Object callbackContext)
    {
        if (desiredPropertiesCallback == null)
        {
            throw new IllegalStateException("Must subscribe to desired properties before sending reported properties.");
        }

        Objects.requireNonNull(reportedProperties, "Reported properties cannot be null");

        String serializedReportedProperties = reportedProperties.toJsonElement().toString();

        IotHubTransportMessage updateReportedPropertiesRequest = new IotHubTransportMessage(serializedReportedProperties.getBytes(StandardCharsets.UTF_8), MessageType.DEVICE_TWIN);
        updateReportedPropertiesRequest.setConnectionDeviceId(this.client.getConfig().getDeviceId());

        // MQTT does not have the concept of correlationId for request/response handling but it does have a requestId
        // To handle this we are setting the correlationId to the requestId to better handle correlation
        // whether we use MQTT or AMQP.
        updateReportedPropertiesRequest.setRequestId(UUID.randomUUID().toString());
        updateReportedPropertiesRequest.setCorrelationId(updateReportedPropertiesRequest.getRequestId());

        if (reportedProperties.getVersion() != null)
        {
            updateReportedPropertiesRequest.setVersion(Integer.toString(reportedProperties.getVersion()));
        }

        updateReportedPropertiesRequest.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST);

        IotHubEventCallback iotHubEventCallback = (statusCode, context) ->
        {
            // no action needed here. The correlating message callback will handle the various message state callbacks including this one
        };

        updateReportedPropertiesRequest.setCorrelatingMessageCallback(new CorrelatingMessageCallback()
        {
            @Override
            public void onRequestQueued(Message message, Object callbackContext)
            {
                if (reportedPropertiesUpdateCorrelatingMessageCallback != null)
                {
                    reportedPropertiesUpdateCorrelatingMessageCallback.onRequestQueued(message, callbackContext);
                }
            }

            @Override
            public void onRequestSent(Message message, Object callbackContext)
            {
                if (reportedPropertiesUpdateCorrelatingMessageCallback != null)
                {
                    reportedPropertiesUpdateCorrelatingMessageCallback.onRequestSent(message, callbackContext);
                }
            }

            @Override
            public void onRequestAcknowledged(Message message, Object callbackContext, TransportException e)
            {
                if (reportedPropertiesUpdateCorrelatingMessageCallback != null)
                {
                    reportedPropertiesUpdateCorrelatingMessageCallback.onRequestAcknowledged(message, callbackContext, e);
                }
            }

            @Override
            public void onResponseReceived(Message message, Object callbackContext, TransportException e)
            {
                IotHubTransportMessage dtMessage = (IotHubTransportMessage) message;
                String status = dtMessage.getStatus();
                IotHubStatusCode iotHubStatus = IotHubStatusCode.ERROR;
                if (status != null)
                {
                    iotHubStatus = IotHubStatusCode.getIotHubStatusCode(Integer.parseInt(status));
                }

                if (reportedPropertiesUpdateCorrelatingMessageCallback != null)
                {
                    log.trace("Executing twin status callback for device operation twin update reported properties response with status " + iotHubStatus);
                    reportedPropertiesUpdateCorrelatingMessageCallback.onResponseReceived(message, callbackContext, iotHubStatus, e);
                }
            }

            @Override
            public void onResponseAcknowledged(Message message, Object callbackContext)
            {
                if (reportedPropertiesUpdateCorrelatingMessageCallback != null)
                {
                    reportedPropertiesUpdateCorrelatingMessageCallback.onResponseAcknowledged(message, callbackContext);
                }
            }
        });

        updateReportedPropertiesRequest.setCorrelatingMessageCallbackContext(callbackContext);

        this.client.sendEventAsync(updateReportedPropertiesRequest, iotHubEventCallback, callbackContext);
    }

    public void subscribeToDesiredPropertiesAsync(
        DesiredPropertiesSubscriptionCallback desiredPropertiesSubscriptionCallback,
        Object subscribeToDesiredPropertiesCallbackContext,
        DesiredPropertiesCallback desiredPropertiesCallback,
        Object desiredPropertiesUpdateCallbackContext)
    {
        Objects.requireNonNull(desiredPropertiesCallback, "Must set a non-null handler for desired property updates");

        this.desiredPropertiesCallback = desiredPropertiesCallback;
        this.desiredPropertiesUpdateCallbackContext = desiredPropertiesUpdateCallbackContext;

        IotHubTransportMessage desiredPropertiesNotificationRequest = new IotHubTransportMessage(new byte[0], MessageType.DEVICE_TWIN);
        desiredPropertiesNotificationRequest.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST);

        IotHubEventCallback eventCallback = (responseStatus, callbackContext) ->
        {
            if (desiredPropertiesSubscriptionCallback != null)
            {
                desiredPropertiesSubscriptionCallback.onSubscriptionAcknowledged(responseStatus, callbackContext);
            }
        };

        this.client.sendEventAsync(desiredPropertiesNotificationRequest, eventCallback, subscribeToDesiredPropertiesCallbackContext);
    }
}
