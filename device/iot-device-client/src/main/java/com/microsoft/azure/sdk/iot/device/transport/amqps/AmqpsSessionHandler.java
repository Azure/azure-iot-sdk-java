package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations.*;
import static com.microsoft.azure.sdk.iot.device.MessageType.*;

@Slf4j
public class AmqpsSessionHandler extends BaseHandler implements AmqpsLinkStateCallback
{
    @Getter
    private final DeviceClientConfig deviceClientConfig;

    //Subscriptions that are initiated by the SDK, not the user of the SDK. State should not carry over between connections.
    private final Map<Integer, SubscriptionType> implicitInProgressSubscriptionMessages = new ConcurrentHashMap<>();

    //These messages, if not null, are the messages to acknowledge once the twin/method subscription request has completed.
    //If null, then no explicit subscription request is in progress.
    private IotHubTransportMessage explicitInProgressTwinSubscriptionMessage;
    private IotHubTransportMessage explicitInProgressMethodsSubscriptionMessage;

    //Carries over state between reconnections
    private boolean subscribeToMethodsOnReconnection = false;
    private boolean subscribeToTwinOnReconnection = false;
    private AmqpsSessionStateCallback amqpsSessionStateCallback;

    //Should not carry over state between reconnects
    private List<AmqpsSenderLinkHandler> senderLinkHandlers = new ArrayList<>();
    private List<AmqpsReceiverLinkHandler> receiverLinkHandlers = new ArrayList<>();
    private Session session;
    private boolean alreadyCreatedTelemetryLinks;
    private boolean alreadyCreatedTwinLinks;
    private boolean alreadyCreatedMethodLinks;
    private boolean twinSenderLinkOpened;
    private boolean twinReceiverLinkOpened;
    private boolean methodsSenderLinkOpened;
    private boolean methodsReceiverLinkOpened;

    AmqpsSessionHandler(final DeviceClientConfig deviceClientConfig, AmqpsSessionStateCallback amqpsSessionStateCallback)
    {
        this.deviceClientConfig = deviceClientConfig;
        this.amqpsSessionStateCallback = amqpsSessionStateCallback;
    }

    public void setSession(Session session)
    {
        this.session = session;

        //All events that happen to this session will be handled in this class (onSessionRemoteOpen, for instance)
        BaseHandler.setHandler(this.session, this);

        this.session.open();

        //erase all state from previous connection state other than subscribeToMethodsOnReconnection/subscribeToTwinOnReconnection
        this.alreadyCreatedTelemetryLinks = false;
        this.alreadyCreatedTwinLinks = false;
        this.alreadyCreatedMethodLinks = false;
        this.senderLinkHandlers.clear();
        this.receiverLinkHandlers.clear();
        this.explicitInProgressTwinSubscriptionMessage = null;
        this.explicitInProgressMethodsSubscriptionMessage = null;
        this.implicitInProgressSubscriptionMessages.clear();
        this.twinSenderLinkOpened = false;
        this.twinReceiverLinkOpened = false;
        this.methodsSenderLinkOpened = false;
        this.methodsReceiverLinkOpened = false;
    }

    public void closeSession()
    {
        this.session.close();
    }

    public String getDeviceId()
    {
        return this.deviceClientConfig.getDeviceId();
    }

    @Override
    public void onSessionRemoteOpen(Event e)
    {
        log.trace("Device session opened remotely for device {}", this.getDeviceId());
        if (this.deviceClientConfig.getAuthenticationType() == DeviceClientConfig.AuthType.X509_CERTIFICATE)
        {
            openLinks();
        }
        else
        {
            // do nothing but log the event. SAS token based connections can't open links until cbs links are open, and authentication messages have been sent
            log.trace("Device session for device {} opened locally", this.getDeviceId());
        }
    }

    @Override
    public void onSessionLocalOpen(Event e)
    {
        log.trace("Device session opened locally for device {}", this.getDeviceId());
    }

    @Override
    public void onSessionRemoteClose(Event e)
    {
        Session session = e.getSession();
        if (session.getLocalState() == EndpointState.ACTIVE)
        {
            //Service initiated this session close
            log.debug("Amqp device session closed remotely unexpectedly for device {}", getDeviceId());
            this.amqpsSessionStateCallback.onSessionClosedUnexpectedly(session.getRemoteCondition());

            this.session.close();
        }
        else
        {
            log.trace("Amqp device session closed remotely as expected for device {}", getDeviceId());
        }
    }

    @Override
    public void onSessionLocalClose(Event e)
    {
        log.debug("Amqp session closed locally for device {}", this.getDeviceId());
        closeLinks();
        this.senderLinkHandlers.clear();
        this.receiverLinkHandlers.clear();
    }

    @Override
    public void onLinkOpened(BaseHandler linkHandler)
    {
        boolean allLinksOpen = true;
        for (AmqpsSenderLinkHandler senderLinkHandler : senderLinkHandlers)
        {
            allLinksOpen &= senderLinkHandler.senderLink != null && senderLinkHandler.senderLink.getRemoteState() == EndpointState.ACTIVE;
        }

        for (AmqpsReceiverLinkHandler receiverLinkHandler : receiverLinkHandlers)
        {
            allLinksOpen &= receiverLinkHandler.receiverLink != null && receiverLinkHandler.receiverLink.getRemoteState() == EndpointState.ACTIVE;
        }

        if (allLinksOpen)
        {
            this.amqpsSessionStateCallback.onDeviceSessionOpened(this.getDeviceId());
        }

        //There is an implicit subscription to desired properties that must be sent after opening a twin link.
        //Historically, this was not necessary, but the service changed its behavior to require this subscription message,
        // so this covers that up.
        if (linkHandler instanceof AmqpsTwinSenderLinkHandler)
        {
            int deliveryTag = ((AmqpsTwinSenderLinkHandler) linkHandler).sendDesiredPropertiesSubscriptionMessage();

            if (deliveryTag == -1)
            {
                log.warn("Failed to send desired properties subscription message");
            }
            else
            {
                this.implicitInProgressSubscriptionMessages.put(deliveryTag, SubscriptionType.DESIRED_PROPERTIES_SUBSCRIPTION);
            }
        }

        acknowledgeExplicitSubscriptionMessages(linkHandler);
    }

    private void acknowledgeExplicitSubscriptionMessages(BaseHandler linkHandler)
    {
        //If the twin links or method links have finished opening, then this session needs to notify the connection layer
        // that the message it sent (an explicit subscription message) has been "sent"
        if (linkHandler instanceof AmqpsTwinSenderLinkHandler)
        {
            this.twinSenderLinkOpened = true;

            if (this.twinReceiverLinkOpened && this.explicitInProgressTwinSubscriptionMessage != null)
            {
                this.amqpsSessionStateCallback.onMessageAcknowledged(this.explicitInProgressTwinSubscriptionMessage);
                this.explicitInProgressTwinSubscriptionMessage = null; //By setting this to null, this session can handle another twin subscription message
            }
        }
        else if (linkHandler instanceof AmqpsTwinReceiverLinkHandler)
        {
            this.twinReceiverLinkOpened = true;

            if (this.twinSenderLinkOpened && this.explicitInProgressTwinSubscriptionMessage != null)
            {
                this.amqpsSessionStateCallback.onMessageAcknowledged(this.explicitInProgressTwinSubscriptionMessage);
                this.explicitInProgressTwinSubscriptionMessage = null; //By setting this to null, this session can handle another twin subscription message
            }
        }
        else if (linkHandler instanceof AmqpsMethodsSenderLinkHandler)
        {
            this.methodsSenderLinkOpened = true;

            if (this.methodsReceiverLinkOpened && this.explicitInProgressMethodsSubscriptionMessage != null)
            {
                this.amqpsSessionStateCallback.onMessageAcknowledged(this.explicitInProgressMethodsSubscriptionMessage);
                this.explicitInProgressMethodsSubscriptionMessage = null; //By setting this to null, this session can handle another method subscription message
            }
        }
        else if (linkHandler instanceof AmqpsMethodsReceiverLinkHandler)
        {
            this.methodsReceiverLinkOpened = true;

            if (this.methodsSenderLinkOpened && this.explicitInProgressMethodsSubscriptionMessage != null)
            {
                this.amqpsSessionStateCallback.onMessageAcknowledged(this.explicitInProgressMethodsSubscriptionMessage);
                this.explicitInProgressMethodsSubscriptionMessage = null; //By setting this to null, this session can handle another method subscription message
            }
        }
    }

    @Override
    public void onMessageAcknowledged(Message message, int deliveryTag)
    {
        if (this.implicitInProgressSubscriptionMessages.containsKey(deliveryTag))
        {
            this.implicitInProgressSubscriptionMessages.remove(deliveryTag);
            log.trace("The acknowledged message was the desired properties subscription message");
        }
        else
        {
            this.amqpsSessionStateCallback.onMessageAcknowledged(message);
        }
    }

    @Override
    public void onMessageReceived(IotHubTransportMessage message)
    {
        this.amqpsSessionStateCallback.onMessageReceived(message);
    }

    @Override
    public void onLinkClosedUnexpectedly(ErrorCondition errorCondition)
    {
        log.trace("Link closed unexpectedly for the amqp session of device {}", this.getDeviceId());
        this.session.close();
        this.amqpsSessionStateCallback.onSessionClosedUnexpectedly(errorCondition);
    }

    public boolean acknowledgeReceivedMessage(IotHubTransportMessage message, DeliveryState ackType)
    {
        for (AmqpsReceiverLinkHandler linksHandler : receiverLinkHandlers)
        {
            if (linksHandler.acknowledgeReceivedMessage(message, ackType))
            {
                return true;
            }
        }

        return false;
    }

    void openLinks()
    {
        //Note that this method should only be called from a reactor thread such as during a callback of onSessionRemoteOpen.
        // Just like sending and receiving messages, opening links is only safe on a reactor thread.
        if (!alreadyCreatedTelemetryLinks)
        {
            createTelemetryLinks();
        }

        if (subscribeToTwinOnReconnection && !alreadyCreatedTwinLinks)
        {
            createTwinLinks();
        }

        if (subscribeToMethodsOnReconnection && !alreadyCreatedMethodLinks)
        {
            createMethodLinks();
        }
    }

    boolean sendMessage(Message message)
    {
        if (this.deviceClientConfig.getDeviceId().equals(message.getConnectionDeviceId()))
        {
            if (message.getMessageType() == null)
            {
                message.setMessageType(DEVICE_TELEMETRY);
            }

            MessageType messageType = message.getMessageType();

            //Check if the message being sent is a subscription change message. If so, open the corresponding links.
            if (message instanceof IotHubTransportMessage)
            {
                IotHubTransportMessage transportMessage = (IotHubTransportMessage) message;
                DeviceOperations subscriptionType = ((IotHubTransportMessage) message).getDeviceOperationType();

                if (subscriptionType == DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST)
                {
                    if (this.methodsSenderLinkOpened && this.methodsReceiverLinkOpened)
                    {
                        // No need to do anything. Method links are already opened
                        this.amqpsSessionStateCallback.onMessageAcknowledged(message);
                        return true;
                    }

                    if (this.explicitInProgressMethodsSubscriptionMessage == null)
                    {
                        createMethodLinks();
                        this.explicitInProgressMethodsSubscriptionMessage = transportMessage;
                        return true; //connection layer doesn't care about this delivery ta
                    }
                    else
                    {
                        log.debug("Rejecting methods subscription message because that subscription is already in progress");
                        return false;
                    }
                }
                else if (subscriptionType == DEVICE_OPERATION_TWIN_UNSUBSCRIBE_DESIRED_PROPERTIES_REQUEST)
                {
                    //TODO: can add logic here to tear down twin links if the user wants to unsubscribe to desired properties
                }
                else if (subscriptionType == DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST)
                {
                    if (this.twinSenderLinkOpened && this.twinReceiverLinkOpened)
                    {
                        // No need to do anything. Twin links are already opened and desired properties subscription is automatically
                        // sent once the twin links are opened.
                        this.amqpsSessionStateCallback.onMessageAcknowledged(message);
                        return true;
                    }

                    if (this.explicitInProgressTwinSubscriptionMessage == null)
                    {
                        createTwinLinks();
                        this.explicitInProgressTwinSubscriptionMessage = transportMessage;
                        return true;
                    }
                    else
                    {
                        log.debug("Rejecting twin subscription message because that subscription is already in progress");
                        return false;
                    }
                }
            }

            for (AmqpsSenderLinkHandler senderLinkHandler : this.senderLinkHandlers)
            {
                if (senderLinkHandler instanceof AmqpsTelemetrySenderLinkHandler && messageType == DEVICE_TELEMETRY
                        || senderLinkHandler instanceof AmqpsTwinSenderLinkHandler && messageType == DEVICE_TWIN
                        || senderLinkHandler instanceof AmqpsMethodsSenderLinkHandler && messageType == DEVICE_METHODS)
                {
                    AmqpsSendResult amqpsSendResult = senderLinkHandler.sendMessageAndGetDeliveryTag(message);

                    if (amqpsSendResult.isDeliverySuccessful())
                    {
                        return true; // since this is a loop, can't just return amqpsSendResult.isDeliverySuccessful
                    }
                }
            }
        }

        return false;
    }

    private void closeLinks()
    {
        for (AmqpsSenderLinkHandler senderLinkHandler : this.senderLinkHandlers)
        {
            senderLinkHandler.close();
        }

        for (AmqpsReceiverLinkHandler receiverLinkHandler : this.receiverLinkHandlers)
        {
            receiverLinkHandler.close();
        }
    }

    private void createTelemetryLinks()
    {
        String telemetryLinkCorrelationId = UUID.randomUUID().toString();

        Sender sender = session.sender(AmqpsTelemetrySenderLinkHandler.getTag(deviceClientConfig, telemetryLinkCorrelationId));
        this.senderLinkHandlers.add(new AmqpsTelemetrySenderLinkHandler(sender, this, this.deviceClientConfig, telemetryLinkCorrelationId));

        Receiver receiver = session.receiver(AmqpsTelemetryReceiverLinkHandler.getTag(deviceClientConfig, telemetryLinkCorrelationId));
        this.receiverLinkHandlers.add(new AmqpsTelemetryReceiverLinkHandler(receiver, this, this.deviceClientConfig, telemetryLinkCorrelationId));
        this.alreadyCreatedTelemetryLinks = true;
    }

    private void createMethodLinks()
    {
        String methodsLinkCorrelationId = UUID.randomUUID().toString();

        Sender sender = session.sender(AmqpsMethodsSenderLinkHandler.getTag(deviceClientConfig, methodsLinkCorrelationId));
        this.senderLinkHandlers.add(new AmqpsMethodsSenderLinkHandler(sender, this, this.deviceClientConfig, methodsLinkCorrelationId));

        Receiver receiver = session.receiver(AmqpsMethodsReceiverLinkHandler.getTag(deviceClientConfig, methodsLinkCorrelationId));
        this.receiverLinkHandlers.add(new AmqpsMethodsReceiverLinkHandler(receiver, this, this.deviceClientConfig, methodsLinkCorrelationId));

        this.subscribeToMethodsOnReconnection = true;
        this.alreadyCreatedMethodLinks = true;
    }

    private void createTwinLinks()
    {
        String twinLinkCorrelationId = UUID.randomUUID().toString();

        //Twin sender and receiver links need to correlate operation request messages to operation response messages.
        //This map allows the sender link to know how to handle a message that is received with a correlation id
        Map<String, DeviceOperations> twinOperationCorrelationMap = new HashMap<>();

        Sender sender = session.sender(AmqpsTwinSenderLinkHandler.getTag(deviceClientConfig, twinLinkCorrelationId));
        this.senderLinkHandlers.add(new AmqpsTwinSenderLinkHandler(sender, this, this.deviceClientConfig, twinLinkCorrelationId, twinOperationCorrelationMap));

        Receiver receiver = session.receiver(AmqpsTwinReceiverLinkHandler.getTag(deviceClientConfig, twinLinkCorrelationId));
        this.receiverLinkHandlers.add(new AmqpsTwinReceiverLinkHandler(receiver, this, this.deviceClientConfig, twinLinkCorrelationId, twinOperationCorrelationMap));

        this.subscribeToTwinOnReconnection = true;
        this.alreadyCreatedTwinLinks = true;
    }
}
