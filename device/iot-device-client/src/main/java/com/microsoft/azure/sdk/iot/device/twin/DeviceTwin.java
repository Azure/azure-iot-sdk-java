// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.twin;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.convention.ClientProperties;
import com.microsoft.azure.sdk.iot.device.convention.ClientPropertyCollection;
import com.microsoft.azure.sdk.iot.device.convention.GetClientPropertiesCallback;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import lombok.Getter;
import lombok.Setter;
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

    /**
     * The client properties callback.
     */
    @Getter
    @Setter
    private GetClientPropertiesCallback getClientPropertiesCallback;

    /**
     * The client properties callback context.
     */
    @Getter
    @Setter
    private Object clientPropertiesCallbackContext;

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
    public IotHubMessageResult onCloudToDeviceMessageReceived(Message message, Object callbackContext)
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
            public void onRequestAcknowledged(Message message, Object callbackContext, IotHubClientException e)
            {
                twinCallback.onRequestAcknowledged(message, callbackContext, e);
            }

            @Override
            public void onResponseReceived(Message message, Object callbackContext, IotHubClientException e)
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

        MessageSentCallback onMessageAcknowledgedCallback = (responseStatus, exception, context) ->
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
            updateReportedPropertiesRequest.setVersion(reportedProperties.getVersion());
        }

        updateReportedPropertiesRequest.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST);

        MessageSentCallback messageSentCallback = (statusCode, exception, context) ->
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
            public void onRequestAcknowledged(Message message, Object callbackContext, IotHubClientException e)
            {
                if (reportedPropertiesUpdateCorrelatingMessageCallback != null)
                {
                    reportedPropertiesUpdateCorrelatingMessageCallback.onRequestAcknowledged(message, callbackContext, e);
                }
            }

            @Override
            public void onResponseReceived(Message message, Object callbackContext, IotHubClientException e)
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
                    reportedPropertiesUpdateCorrelatingMessageCallback.onResponseReceived(message, callbackContext, iotHubStatus, new ReportedPropertiesUpdateResponse(dtMessage.getVersion()), e);
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

        this.client.sendEventAsync(updateReportedPropertiesRequest, messageSentCallback, callbackContext);
    }

    public void subscribeToDesiredPropertiesAsync(
        SubscriptionAcknowledgedCallback subscriptionAcknowledgedCallback,
        Object subscribeToDesiredPropertiesCallbackContext,
        DesiredPropertiesCallback desiredPropertiesCallback,
        Object desiredPropertiesUpdateCallbackContext)
    {
        Objects.requireNonNull(desiredPropertiesCallback, "Must set a non-null handler for desired property updates");

        this.desiredPropertiesCallback = desiredPropertiesCallback;
        this.desiredPropertiesUpdateCallbackContext = desiredPropertiesUpdateCallbackContext;

        IotHubTransportMessage desiredPropertiesNotificationRequest = new IotHubTransportMessage(new byte[0], MessageType.DEVICE_TWIN);
        desiredPropertiesNotificationRequest.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST);

        MessageSentCallback eventCallback = (sentMessage, exception, callbackContext) ->
        {
            if (subscriptionAcknowledgedCallback != null)
            {
                subscriptionAcknowledgedCallback.onSubscriptionAcknowledged(exception, callbackContext);
            }
        };

        this.client.sendEventAsync(desiredPropertiesNotificationRequest, eventCallback, subscribeToDesiredPropertiesCallbackContext);
    }

    public void getClientProperties(GetClientPropertiesCorrelatingMessageCallback getClientPropertiesCallback, Object context)
    {
        GetTwinCorrelatingMessageCallback getTwinCorrelatingMessageCallback = new GetTwinCorrelatingMessageCallback()
        {
            @Override
            public void onRequestQueued(Message message, Object callbackContext)
            {
                getClientPropertiesCallback.onRequestQueued(message, callbackContext);
            }

            @Override
            public void onRequestSent(Message message, Object callbackContext)
            {
                getClientPropertiesCallback.onRequestSent(message, callbackContext);
            }

            @Override
            public void onRequestAcknowledged(Message message, Object callbackContext, IotHubClientException e)
            {
                getClientPropertiesCallback.onRequestAcknowledged(message, callbackContext, e);
            }

            @Override
            public void onResponseReceived(Twin twin, Message message, Object callbackContext, IotHubStatusCode statusCode, IotHubClientException e)
            {
                //TODO this is equivalent to returning a twin object with no reported properties. Is that right?
                ClientPropertyCollection clientPropertyCollection = new ClientPropertyCollection(message.getBytes(), client.getConfig().getPayloadConvention(), true);
                ClientProperties clientProperties = new ClientProperties(clientPropertyCollection, null);
                getClientPropertiesCallback.onResponseReceived(clientProperties, message, callbackContext, statusCode, e);
            }

            @Override
            public void onResponseAcknowledged(Message message, Object callbackContext)
            {
                getClientPropertiesCallback.onResponseAcknowledged(message, callbackContext);
            }
        };

        getTwinAsync(getTwinCorrelatingMessageCallback, context);
    }

    public synchronized void updateClientProperties(
            ClientPropertyCollection clientPropertyCollection,
            ClientPropertiesUpdateCorrelatingMessageCallback callback,
            Object callbackContext)
    {
        if (clientPropertyCollection == null)
        {
            throw new IllegalArgumentException("Reported properties cannot be null");
        }

        String serializedReportedProperties = this.client.getConfig().getPayloadConvention().getPayloadSerializer().serializeToString(clientPropertyCollection);

        if (serializedReportedProperties == null)
        {
            //TODO throw?
            return;
        }

        IotHubTransportMessage clientPropertiesRequest = new IotHubTransportMessage(serializedReportedProperties.getBytes(), MessageType.DEVICE_TWIN);
        clientPropertiesRequest.setConnectionDeviceId(this.client.getConfig().getDeviceId());

        // MQTT does not have the concept of correlationId for request/response handling but it does have a requestId
        // To handle this we are setting the correlationId to the requestId to better handle correlation
        // whether we use MQTT or AMQP.
        clientPropertiesRequest.setRequestId(UUID.randomUUID().toString());
        clientPropertiesRequest.setCorrelationId(clientPropertiesRequest.getRequestId());

        clientPropertiesRequest.setVersion(clientPropertyCollection.getVersion());

        clientPropertiesRequest.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST);


        MessageSentCallback messageSentCallback = (statusCode, exception, context) ->
        {
            // no action needed here. The correlating message callback will handle the various message state callbacks including this one
        };

        clientPropertiesRequest.setCorrelatingMessageCallback(new CorrelatingMessageCallback()
        {
            @Override
            public void onRequestQueued(Message message, Object callbackContext)
            {
                if (callback != null)
                {
                    callback.onRequestQueued(message, callbackContext);
                }
            }

            @Override
            public void onRequestSent(Message message, Object callbackContext)
            {
                if (callback != null)
                {
                    callback.onRequestSent(message, callbackContext);
                }
            }

            @Override
            public void onRequestAcknowledged(Message message, Object callbackContext, IotHubClientException e)
            {
                if (callback != null)
                {
                    callback.onRequestAcknowledged(message, callbackContext, e);
                }
            }

            @Override
            public void onResponseReceived(Message message, Object callbackContext, IotHubClientException e)
            {
                IotHubTransportMessage dtMessage = (IotHubTransportMessage) message;
                String status = dtMessage.getStatus();
                IotHubStatusCode iotHubStatus = IotHubStatusCode.ERROR;
                if (status != null)
                {
                    iotHubStatus = IotHubStatusCode.getIotHubStatusCode(Integer.parseInt(status));
                }

                if (callback != null)
                {
                    log.trace("Executing twin status callback for device operation twin update reported properties response with status " + iotHubStatus);
                    callback.onResponseReceived(message, callbackContext, iotHubStatus, new ClientPropertiesUpdateResponse(dtMessage.getVersion()), e);
                }
            }

            @Override
            public void onResponseAcknowledged(Message message, Object callbackContext)
            {
                if (callback != null)
                {
                    callback.onResponseAcknowledged(message, callbackContext);
                }
            }
        });

        clientPropertiesRequest.setCorrelatingMessageCallbackContext(callbackContext);

        this.client.sendEventAsync(clientPropertiesRequest, messageSentCallback, callbackContext);
    }
}
