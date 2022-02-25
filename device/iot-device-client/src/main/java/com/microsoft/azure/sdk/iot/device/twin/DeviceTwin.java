// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.twin;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static com.microsoft.azure.sdk.iot.device.IotHubMessageResult.ABANDON;
import static com.microsoft.azure.sdk.iot.device.IotHubMessageResult.COMPLETE;

@Slf4j
public class DeviceTwin implements MessageCallback
{
    private final InternalClient client;

    private Consumer<DesiredPropertiesUpdate> desiredPropertiesUpdateCallback;

    private final Map<String, Consumer<GetTwinResponse>> getTwinRequestCallbacks = new ConcurrentHashMap<>();
    private final Map<String, Consumer<SendReportedPropertiesResponse>> sendReportedPropertiesCallbacks = new ConcurrentHashMap<>();

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
        IotHubStatusCode iotHubStatus = IotHubStatusCode.ERROR;
        if (message.getMessageType() != MessageType.DEVICE_TWIN)
        {
            log.warn("Unexpected message type received. Abandoning it");
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

                if (iotHubStatus == IotHubStatusCode.OK)
                {
                    log.trace("Executing twin callback for message {}", dtMessage);
                    Twin twin = Twin.createFromPropertiesJson(new String(dtMessage.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));
                    Consumer<GetTwinResponse> getTwinCallback = this.getTwinRequestCallbacks.remove(dtMessage.getCorrelationId());
                    if (getTwinCallback != null)
                    {
                        getTwinCallback.accept(new GetTwinResponse(twin, null)); //TODO context?
                    }

                    log.trace("Twin callback returned for message {}", dtMessage);
                }
                break;
            }
            case DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_RESPONSE:
            {
                if (status != null)
                {
                    iotHubStatus = IotHubStatusCode.getIotHubStatusCode(Integer.parseInt(status));
                }

                Consumer<SendReportedPropertiesResponse> sendReportedPropertiesResponseCallback = this.sendReportedPropertiesCallbacks.remove(dtMessage.getCorrelationId());

                if (sendReportedPropertiesResponseCallback != null)
                {
                    log.trace("Executing twin status callback for device operation twin update reported properties response with status " + iotHubStatus);
                    sendReportedPropertiesResponseCallback.accept(new SendReportedPropertiesResponse(iotHubStatus, null)); //TODO context?
                }
                break;
            }
            case DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE:
            {
                Twin twin = Twin.createFromDesiredPropertyJson(new String(dtMessage.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));

                this.desiredPropertiesUpdateCallback.accept(new DesiredPropertiesUpdate(twin, null)); //TODO context?

                break;
            }
            default:
                break;
        }
        return COMPLETE;
    }

    public void getDeviceTwinAsync(Consumer<GetTwinResponse> getTwinCallback, Runnable onGetTwinMessageAcknowledgedCallback)
    {
        IotHubTransportMessage getTwinRequestMessage = new IotHubTransportMessage(new byte[0], MessageType.DEVICE_TWIN);
        getTwinRequestMessage.setRequestId(UUID.randomUUID().toString());
        getTwinRequestMessage.setCorrelationId(getTwinRequestMessage.getRequestId());
        getTwinRequestMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_GET_REQUEST);

        this.getTwinRequestCallbacks.put(getTwinRequestMessage.getRequestId(), getTwinCallback);

        IotHubEventCallback onMessageAcknowledgedCallback = (responseStatus, callbackContext) ->
        {
            if (onGetTwinMessageAcknowledgedCallback != null)
            {
                onGetTwinMessageAcknowledgedCallback.run();
            }
        };

        this.client.sendEventAsync(getTwinRequestMessage, onMessageAcknowledgedCallback,null);
    }

    public void updateReportedPropertiesAsync(
        TwinCollection reportedProperties,
        Consumer<SendReportedPropertiesResponse> sendReportedPropertiesResponseCallback,
        Runnable onReportedPropertiesMessageAcknowledgedCallback,
        Object callbackContext)
    {
        if (reportedProperties == null)
        {
            throw new IllegalArgumentException("Reported properties cannot be null");
        }

        String serializedReportedProperties = reportedProperties.toJsonElement().toString();

        IotHubTransportMessage updateReportedPropertiesRequest = new IotHubTransportMessage(serializedReportedProperties.getBytes(StandardCharsets.UTF_8), MessageType.DEVICE_TWIN);
        //updateReportedPropertiesRequest.setTwinMessageStatusCallback(twinMessageStatusCallback); //TODO maybe re-enable this thing james made
        //updateReportedPropertiesRequest.setCorrelatingMessageCallbackContext(correlatingMessageCallbackContext);
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

        this.sendReportedPropertiesCallbacks.put(updateReportedPropertiesRequest.getRequestId(), sendReportedPropertiesResponseCallback);

        IotHubEventCallback iotHubEventCallback = (statusCode, context) ->
        {
            if (onReportedPropertiesMessageAcknowledgedCallback != null)
            {
                onReportedPropertiesMessageAcknowledgedCallback.run(); //TODO this status code is never an error, right? It is the ack, not the response message with the status code
            }
        };

        this.client.sendEventAsync(updateReportedPropertiesRequest, iotHubEventCallback, callbackContext);
    }

    public void subscribeToDesiredPropertiesAsync(Consumer<DesiredPropertiesUpdate> desiredPropertiesUpdateCallback, IotHubEventCallback onDesiredPropertiesSubscribedCallback)
    {
        //TODO what to do if called multiple times? Maybe just override the callback handler but don't send subscribe message?
        this.desiredPropertiesUpdateCallback = desiredPropertiesUpdateCallback;

        IotHubTransportMessage desiredPropertiesNotificationRequest = new IotHubTransportMessage(new byte[0], MessageType.DEVICE_TWIN);
        desiredPropertiesNotificationRequest.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST);
        this.client.sendEventAsync(desiredPropertiesNotificationRequest, onDesiredPropertiesSubscribedCallback, null);
    }
}
