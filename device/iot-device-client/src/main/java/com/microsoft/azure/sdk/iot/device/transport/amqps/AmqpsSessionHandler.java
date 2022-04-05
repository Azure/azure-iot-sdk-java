package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.ClientConfiguration;
import com.microsoft.azure.sdk.iot.device.twin.DeviceOperations;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.microsoft.azure.sdk.iot.device.twin.DeviceOperations.*;
import static com.microsoft.azure.sdk.iot.device.MessageType.*;

@Slf4j
class AmqpsSessionHandler extends BaseHandler implements AmqpsLinkStateCallback
{
    @Getter
    private final ClientConfiguration clientConfiguration;

    // "Explicit" vs "Implicit" here refers to actions that the user initiated ("Explicit"ly calling startTwin)
    // vs actions the SDK initiated ("Implicit"ly opening twin links after reconnecting because they were open previously)

    //Subscriptions that are initiated by the SDK, not the user of the SDK. State should not carry over between connections.
    private final Map<Integer, SubscriptionType> implicitInProgressSubscriptionMessages = new ConcurrentHashMap<>();

    //These messages, if not null, are the messages to acknowledge once the twin/method subscription request has completed.
    //If null, then no explicit subscription request is in progress.
    private IotHubTransportMessage explicitInProgressTwinSubscriptionMessage;
    private IotHubTransportMessage explicitInProgressMethodsSubscriptionMessage;

    //Carries over state between reconnections
    private boolean subscribeToMethodsOnReconnection = false;
    private boolean subscribeToTwinOnReconnection = false;
    private final AmqpsSessionStateCallback amqpsSessionStateCallback;

    //Should not carry over state between reconnects
    //Maps from the type of message (twin/method/telemetry) to the appropriate sender/receiver link handler
    private final Map<MessageType, AmqpsSenderLinkHandler> senderLinkHandlers = new ConcurrentHashMap<>();
    private final Map<MessageType, AmqpsReceiverLinkHandler> receiverLinkHandlers = new ConcurrentHashMap<>();

    private Session session;
    private boolean alreadyCreatedTelemetryLinks;
    private boolean alreadyCreatedTwinLinks;
    private boolean alreadyCreatedMethodLinks;
    private boolean twinSenderLinkOpened;
    private boolean twinReceiverLinkOpened;
    private boolean methodsSenderLinkOpened;
    private boolean methodsReceiverLinkOpened;
    private boolean sessionOpenedRemotely;
    private boolean sessionHandlerClosedBeforeRemoteSessionOpened;
    private boolean isClosing;

    AmqpsSessionHandler(final ClientConfiguration clientConfiguration, AmqpsSessionStateCallback amqpsSessionStateCallback)
    {
        this.clientConfiguration = clientConfiguration;
        this.amqpsSessionStateCallback = amqpsSessionStateCallback;
    }

    public void setSession(Session session)
    {
        this.session = session;

        //All events that happen to this session will be handled in this class (onSessionRemoteOpen, for instance)
        BaseHandler.setHandler(this.session, this);

        log.trace("Opening device session for device {}", getDeviceId());
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
        this.sessionOpenedRemotely = false;
        this.sessionHandlerClosedBeforeRemoteSessionOpened = false;
        this.isClosing = false;
    }

    public void closeSession()
    {
        if (this.session != null)
        {
            if (this.sessionOpenedRemotely)
            {
                // Closing a session locally before the session was opened remotely causes a NPE to throw from proton with
                // error message "uncorrelated channel: X"

                // The reason for this is that closing a session locally removes it from proton's list of sessions.
                // So when the service sends a "begin session" frame for this closed session, proton doesn't know how to handle
                // it, and just throws a NPE.

                // To avoid this, we will purposefully delay closing this session locally until it has been opened remotely

                // This is a difficult scenario to reproduce, but it typically happens during retry logic if the client gives
                // up on a retry attempt prior to the service opening the session remotely.
                this.isClosing = true;
                this.session.close();

                if (session.getLocalState() == EndpointState.CLOSED)
                {
                    // Since session was never opened, there will be no callback for onSessionRemoteClose, so now is
                    // the appropriate time to notify the connection layer that this session has finished closing.
                    this.amqpsSessionStateCallback.onSessionClosedAsExpected(this.getDeviceId());
                }
            }
            else
            {
                // This flag signals to this session handler to close the session once the service has opened it remotely
                // see above for more details on why.
                log.trace("Session handler was closed but the service has not opened the session remotely yet, so the session will be closed once that happens.");
                this.sessionHandlerClosedBeforeRemoteSessionOpened = true;
            }
        }
    }

    public String getDeviceId()
    {
        return this.clientConfiguration.getDeviceId();
    }

    @Override
    public void onSessionFinal(Event e)
    {
        this.session.free();
    }

    @Override
    public void onSessionRemoteOpen(Event e)
    {
        log.trace("Device session opened remotely for device {}", this.getDeviceId());
        this.sessionOpenedRemotely = true;
        if (this.sessionHandlerClosedBeforeRemoteSessionOpened)
        {
            // If the session handler was closed earlier, before this session opened remotely, then now is the soonest
            // that the session itself can safely be closed.
            log.trace("Closing an out of date session now that the service has opened the session remotely.");
            this.session.close();
        }
        else if (this.clientConfiguration.getAuthenticationType() == ClientConfiguration.AuthType.X509_CERTIFICATE)
        {
            log.trace("Opening worker links for device {}", this.getDeviceId());
            openLinks();
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
        if (session.getLocalState() == EndpointState.ACTIVE || !isClosing)
        {
            //Service initiated this session close
            this.session.close();

            log.debug("Amqp device session closed remotely unexpectedly for device {}", getDeviceId());
            clearHandlers();
            this.amqpsSessionStateCallback.onSessionClosedUnexpectedly(session.getRemoteCondition(), this.getDeviceId());
        }
        else
        {
            log.trace("Amqp device session closed remotely as expected for device {}", getDeviceId());
            clearHandlers();
            this.amqpsSessionStateCallback.onSessionClosedAsExpected(this.getDeviceId());
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
        for (AmqpsSenderLinkHandler senderLinkHandler : senderLinkHandlers.values())
        {
            // Ignored because Sender is passed in from elsewhere and we won't know the condition of the Link
            //noinspection ConstantConditions
            allLinksOpen &= senderLinkHandler.senderLink != null && senderLinkHandler.senderLink.getRemoteState() == EndpointState.ACTIVE;
        }

        for (AmqpsReceiverLinkHandler receiverLinkHandler : receiverLinkHandlers.values())
        {
            // Ignored because Sender is passed in from elsewhere and we won't know the condition of the Link
            //noinspection ConstantConditions
            allLinksOpen &= receiverLinkHandler.receiverLink != null && receiverLinkHandler.receiverLink.getRemoteState() == EndpointState.ACTIVE;
        }

        if (allLinksOpen)
        {
            log.trace("Device session for device {} has finished opening its worker links. Notifying the connection layer.", this.getDeviceId());
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

            // not acknowledging the explicit twin subsription message yet since the implicit twin desired properties
            // subscription message has not been acknowledged yet. See onMessageAcknowledged for the code path that
            // acknowledges the twin subscription message.
        }
        else if (linkHandler instanceof AmqpsTwinReceiverLinkHandler)
        {
            this.twinReceiverLinkOpened = true;

            // not acknowledging the explicit twin subsription message yet since the implicit twin desired properties
            // subscription message has not been acknowledged yet. See onMessageAcknowledged for the code path that
            // acknowledges the twin subscription message.
        }
        else if (linkHandler instanceof AmqpsMethodsSenderLinkHandler)
        {
            this.methodsSenderLinkOpened = true;

            if (this.methodsReceiverLinkOpened && this.explicitInProgressMethodsSubscriptionMessage != null)
            {
                this.amqpsSessionStateCallback.onMessageAcknowledged(this.explicitInProgressMethodsSubscriptionMessage, Accepted.getInstance(), this.getDeviceId());
                this.explicitInProgressMethodsSubscriptionMessage = null; //By setting this to null, this session can handle another method subscription message
            }
        }
        else if (linkHandler instanceof AmqpsMethodsReceiverLinkHandler)
        {
            this.methodsReceiverLinkOpened = true;

            if (this.methodsSenderLinkOpened && this.explicitInProgressMethodsSubscriptionMessage != null)
            {
                this.amqpsSessionStateCallback.onMessageAcknowledged(this.explicitInProgressMethodsSubscriptionMessage, Accepted.getInstance(), this.getDeviceId());
                this.explicitInProgressMethodsSubscriptionMessage = null; //By setting this to null, this session can handle another method subscription message
            }
        }
    }

    @Override
    public void onMessageAcknowledged(Message message, int deliveryTag, DeliveryState deliveryState)
    {
        if (this.implicitInProgressSubscriptionMessages.containsKey(deliveryTag))
        {
            this.implicitInProgressSubscriptionMessages.remove(deliveryTag);
            log.trace("The acknowledged message was the desired properties subscription message");

            // Now that both the twin links have opened, and the desired property subscription message has been ack'd, twin
            // has successfully been subscribed to.
            this.amqpsSessionStateCallback.onMessageAcknowledged(this.explicitInProgressTwinSubscriptionMessage, Accepted.getInstance(), this.getDeviceId());
            this.explicitInProgressTwinSubscriptionMessage = null; //By setting this to null, this session can handle another twin subscription message
        }
        else
        {
            this.amqpsSessionStateCallback.onMessageAcknowledged(message, deliveryState, this.getDeviceId());
        }
    }

    @Override
    public void onMessageReceived(IotHubTransportMessage message)
    {
        message.setConnectionDeviceId(this.getDeviceId());
        this.amqpsSessionStateCallback.onMessageReceived(message);
    }

    @Override
    public void onLinkClosedUnexpectedly(ErrorCondition errorCondition)
    {
        log.trace("Link closed unexpectedly for the amqp session of device {}", this.getDeviceId());
        this.session.close();
        this.amqpsSessionStateCallback.onSessionClosedUnexpectedly(errorCondition, this.getDeviceId());
    }

    public boolean acknowledgeReceivedMessage(IotHubTransportMessage message, DeliveryState ackType)
    {
        AmqpsReceiverLinkHandler receiverLinkHandler = receiverLinkHandlers.get(message.getMessageType());

        if (receiverLinkHandler != null)
        {
            return receiverLinkHandler.acknowledgeReceivedMessage(message, ackType);
        }

        log.warn("Failed to acknowledge the received message because its receiver link is no longer active");
        return false;
    }

    void openLinks()
    {
        // Note that this method should only be called from a reactor thread such as during a callback of onSessionRemoteOpen.
        // Just like sending and receiving messages, opening links is only safe on a reactor thread.
        if (!alreadyCreatedTelemetryLinks)
        {
            createTelemetryLinksAsync();
        }

        if (subscribeToTwinOnReconnection && !alreadyCreatedTwinLinks)
        {
            createTwinLinksAsync();
        }

        if (subscribeToMethodsOnReconnection && !alreadyCreatedMethodLinks)
        {
            createMethodLinksAsync();
        }
    }

    SendResult sendMessage(Message message)
    {
        if (!this.clientConfiguration.getDeviceId().equals(message.getConnectionDeviceId()))
        {
            // This should never happen since this session handler was chosen from a map of device Id -> session handler
            // so it should have the same device Id as in the map it was grabbed from.
            log.warn("Failed to send the message because this session belongs to a different device");
            return SendResult.WRONG_DEVICE;
        }

        MessageType messageType = message.getMessageType();
        if (messageType == null)
        {
            // Twin and method messages have a message type assigned to them when they are constructed by this SDK
            // (users can't construct twin/method messages directly), but telemetry messages don't necessarily have this
            // type assigned since users may create telemetry messages. By default, assume any messages with an
            // unassigned type are telemetry messages.
            messageType = DEVICE_TELEMETRY;
        }

        // Check if the message being sent is a subscription change message. If so, open the corresponding links.
        if (message instanceof IotHubTransportMessage)
        {
            IotHubTransportMessage transportMessage = (IotHubTransportMessage) message;
            DeviceOperations subscriptionType = transportMessage.getDeviceOperationType();

            if (subscriptionType == DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST)
            {
                return handleMethodSubscriptionRequest(transportMessage);
            }

            if (subscriptionType == DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST)
            {
                return handleTwinSubscriptionRequest(transportMessage);
            }
        }

        AmqpsSenderLinkHandler senderLinkHandler = this.senderLinkHandlers.get(messageType);

        if (senderLinkHandler == null)
        {
            // no sender link handler saved for this message type, so it can't be sent
            // Should never happen since telemetry links are always opened, and twin/method messages can't be sent
            // before their respective subscription messages have already opened their links.
            return SendResult.LINKS_NOT_OPEN;
        }

        if (messageType == DEVICE_TWIN)
        {
            if (explicitInProgressTwinSubscriptionMessage != null)
            {
                // When this variable is not null, it means there is a subscription on twin in progress. These are initiated
                // by the user when they call startTwin.
                //
                // Don't send any twin messages while a twin subscription is in progress. Wait until the subscription
                // has been acknowledged by the service before sending it.
                return SendResult.SUBSCRIPTION_IN_PROGRESS;
            }

            for (SubscriptionType subscriptionType : this.implicitInProgressSubscriptionMessages.values())
            {
                if (subscriptionType == SubscriptionType.DESIRED_PROPERTIES_SUBSCRIPTION)
                {
                    // When there is at least one desired properties subscription in the implicitInProgressSubscriptionMessages
                    // value set, then that means there is a desired properties subscription message that has been sent
                    // to the service, but has not been acknowledged yet. These implicit subscriptions happen when a
                    // session loses connectivity temporarily, and the session handler sends out subscription messages
                    // to the service to re-establish all subscritptions that were active prior to the disconnection.
                    //
                    // Don't send any twin messages while a twin subscription is in progress. Reject this message until
                    // the subscription has been acknowledged by the service. The connection layer will requeue this message
                    // and it will have another chance to send when the timer task that checks for outgoing queued messages
                    // executes again.
                    return SendResult.SUBSCRIPTION_IN_PROGRESS;
                }
            }
        }

        AmqpsSendResult amqpsSendResult = senderLinkHandler.sendMessageAndGetDeliveryTag(message);

        if (amqpsSendResult.isDeliverySuccessful())
        {
            return SendResult.SUCCESS;
        }

        return SendResult.UNKNOWN_FAILURE;
    }

    private SendResult handleTwinSubscriptionRequest(IotHubTransportMessage transportMessage)
    {
        if (this.twinSenderLinkOpened && this.twinReceiverLinkOpened)
        {
            // No need to do anything besides ack the message. Twin links are already opened and desired properties
            // subscription is automatically sent once the twin links are opened, so there is no need to send
            // this message over the wire.
            log.trace("Automatically acknowledging the twin subscription request because the twin links are already open");
            this.amqpsSessionStateCallback.onMessageAcknowledged(transportMessage, Accepted.getInstance(), this.getDeviceId());
            return SendResult.SUCCESS;
        }

        // If this session hasn't already started subscribing to twins
        if (this.explicitInProgressTwinSubscriptionMessage == null)
        {
            // Don't ack the subscription message here. Once the twin links have finished opening both locally and remotely,
            // it will be ack'd.
            log.trace("Creating the twin links to handle the twin subscription message");
            createTwinLinksAsync();
            this.explicitInProgressTwinSubscriptionMessage = transportMessage;
            return SendResult.SUCCESS;
        }

        log.debug("Rejecting twin subscription message because that subscription is already in progress");
        return SendResult.DUPLICATE_SUBSCRIPTION_MESSAGE;
    }

    private SendResult handleMethodSubscriptionRequest(IotHubTransportMessage transportMessage)
    {
        if (this.methodsSenderLinkOpened && this.methodsReceiverLinkOpened)
        {
            // No need to do anything besides ack the message. Method links are already opened so there is no need to send
            // this message over the wire.
            log.trace("Automatically acknowledging the direct method subscription request because the direct method links are already open");
            this.amqpsSessionStateCallback.onMessageAcknowledged(transportMessage, Accepted.getInstance(), this.getDeviceId());
            return SendResult.SUCCESS;
        }

        // If this session hasn't already started subscribing to methods
        if (this.explicitInProgressMethodsSubscriptionMessage == null)
        {
            // Don't ack the subscription message here. Once the method links have finished opening both locally and remotely,
            // it will be ack'd.
            log.trace("Creating the direct method links to handle the direct method subscription message");
            createMethodLinksAsync();
            this.explicitInProgressMethodsSubscriptionMessage = transportMessage;
            return SendResult.SUCCESS;
        }

        log.debug("Rejecting methods subscription message because that subscription is already in progress");
        return SendResult.DUPLICATE_SUBSCRIPTION_MESSAGE;
    }

    private void closeLinks()
    {
        for (AmqpsSenderLinkHandler senderLinkHandler : this.senderLinkHandlers.values())
        {
            senderLinkHandler.close();
        }

        for (AmqpsReceiverLinkHandler receiverLinkHandler : this.receiverLinkHandlers.values())
        {
            receiverLinkHandler.close();
        }
    }

    // This opens the telemetry links locally, but the service still needs to open them remotely as well. The
    // "onLinkOpened()" event will onStatusChanged when that happens.
    private void createTelemetryLinksAsync()
    {
        String telemetryLinkCorrelationId = UUID.randomUUID().toString();

        Sender sender = session.sender(AmqpsTelemetrySenderLinkHandler.getTag(clientConfiguration, telemetryLinkCorrelationId));
        this.senderLinkHandlers.put(DEVICE_TELEMETRY, new AmqpsTelemetrySenderLinkHandler(sender, this, this.clientConfiguration, telemetryLinkCorrelationId));

        Receiver receiver = session.receiver(AmqpsTelemetryReceiverLinkHandler.getTag(clientConfiguration, telemetryLinkCorrelationId));
        this.receiverLinkHandlers.put(DEVICE_TELEMETRY, new AmqpsTelemetryReceiverLinkHandler(receiver, this, this.clientConfiguration, telemetryLinkCorrelationId));
        this.alreadyCreatedTelemetryLinks = true;
    }

    // This opens the methods links locally, but the service still needs to open them remotely as well. The
    // "onLinkOpened()" event will onStatusChanged when that happens.
    private void createMethodLinksAsync()
    {
        log.debug("Creating direct method links");
        String methodsLinkCorrelationId = UUID.randomUUID().toString();

        Sender sender = session.sender(AmqpsMethodsSenderLinkHandler.getTag(clientConfiguration, methodsLinkCorrelationId));
        this.senderLinkHandlers.put(DEVICE_METHODS, new AmqpsMethodsSenderLinkHandler(sender, this, this.clientConfiguration, methodsLinkCorrelationId));

        Receiver receiver = session.receiver(AmqpsMethodsReceiverLinkHandler.getTag(clientConfiguration, methodsLinkCorrelationId));
        this.receiverLinkHandlers.put(DEVICE_METHODS, new AmqpsMethodsReceiverLinkHandler(receiver, this, this.clientConfiguration, methodsLinkCorrelationId));

        this.subscribeToMethodsOnReconnection = true;
        this.alreadyCreatedMethodLinks = true;
    }

    // This opens the twin links locally, but the service still needs to open them remotely as well. The
    // "onLinkOpened()" event will onStatusChanged when that happens.
    private void createTwinLinksAsync()
    {
        log.debug("Creating twin links");
        String twinLinkCorrelationId = UUID.randomUUID().toString();

        //Twin sender and receiver links need to correlate operation request messages to operation response messages.
        //This map allows the sender link to know how to handle a message that is received with a correlation id
        Map<String, DeviceOperations> twinOperationCorrelationMap = new HashMap<>();

        Sender sender = session.sender(AmqpsTwinSenderLinkHandler.getTag(clientConfiguration, twinLinkCorrelationId));
        this.senderLinkHandlers.put(DEVICE_TWIN, new AmqpsTwinSenderLinkHandler(sender, this, this.clientConfiguration, twinLinkCorrelationId, twinOperationCorrelationMap));

        Receiver receiver = session.receiver(AmqpsTwinReceiverLinkHandler.getTag(clientConfiguration, twinLinkCorrelationId));
        this.receiverLinkHandlers.put(DEVICE_TWIN, new AmqpsTwinReceiverLinkHandler(receiver, this, this.clientConfiguration, twinLinkCorrelationId, twinOperationCorrelationMap));

        this.subscribeToTwinOnReconnection = true;
        this.alreadyCreatedTwinLinks = true;
    }

    // Removes any children of this handler (such as LoggingFlowController) and disassociates this handler
    // from the proton reactor. By removing the reference of the proton reactor to this handler, this handler becomes
    // eligible for garbage collection by the JVM. This is important for multiplexed connections where links come and go
    // but the reactor stays alive for a long time.
    private void clearHandlers()
    {
        this.session.attachments().clear();

        // This session handler shouldn't have any children, but other handlers may be added as this SDK grows and
        // this protects against potential memory leaks.
        Iterator<Handler> childrenIterator = this.children();
        while (childrenIterator.hasNext())
        {
            childrenIterator.next();
            childrenIterator.remove();
        }

        this.session.free();
    }
}
