package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.message.impl.MessageImpl;

import java.nio.BufferOverflowException;
import java.util.*;

import static com.microsoft.azure.sdk.iot.device.MessageType.*;

@Slf4j
public class AmqpsSessionHandler
{
    @Getter
    private final DeviceClientConfig deviceClientConfig;

    private AmqpsDeviceAuthenticationState amqpsAuthenticatorState = AmqpsDeviceAuthenticationState.UNKNOWN;

    private Map<MessageType, AmqpsLinksHandler> amqpsLinkMap = new HashMap<MessageType, AmqpsLinksHandler>();

    private static long nextTag = 0;

    public Session session;

    private Integer openLock = new Integer(1);

    public List<UUID> cbsCorrelationIdList = Collections.synchronizedList(new ArrayList<UUID>());

    private SubscriptionMessageRequestSentCallback subscriptionMessageRequestSentCallback;

    /**
     * Create logical device entity to handle all operation.
     *
     * @param deviceClientConfig the configuration of teh device.
     * @param subscriptionMessageRequestSentCallback the callback to fire each time a subscription message is sent
     * @throws IllegalArgumentException if deviceClientConfig or amqpsConnectionAuthentication is null
     */
    public AmqpsSessionHandler(final DeviceClientConfig deviceClientConfig, SubscriptionMessageRequestSentCallback subscriptionMessageRequestSentCallback) throws IllegalArgumentException
    {
        if (deviceClientConfig == null)
        {
            throw new IllegalArgumentException("deviceClientConfig cannot be null.");
        }

        this.deviceClientConfig = deviceClientConfig;

        this.amqpsLinkMap.put(DEVICE_TELEMETRY, new AmqpsTelemetryLinksHandler(this.deviceClientConfig));

        if (this.deviceClientConfig.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
        {
            this.amqpsAuthenticatorState = AmqpsDeviceAuthenticationState.NOT_AUTHENTICATED;
        }
        else
        {
            this.amqpsAuthenticatorState = AmqpsDeviceAuthenticationState.AUTHENTICATED;
        }

        this.subscriptionMessageRequestSentCallback = subscriptionMessageRequestSentCallback;
    }

    /**
     * Release all resources and close all links.
     */
    public void close()
    {
        this.log.debug("Closing amqp session for device {}", this.getDeviceId());

        this.closeLinks();

        if (this.deviceClientConfig.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
        {
            this.amqpsAuthenticatorState = AmqpsDeviceAuthenticationState.NOT_AUTHENTICATED;
            this.log.trace("Setting amqp session authentication state to NOT_AUTHENTICATED for device {}", this.getDeviceId());
        }
    }

    void openLinks()
    {
        if (session != null)
        {
            if (this.amqpsAuthenticatorState == AmqpsDeviceAuthenticationState.AUTHENTICATED)
            {
                synchronized (this.openLock)
                {
                    for (AmqpsLinksHandler amqpDeviceOperation : this.amqpsLinkMap.values())
                    {
                        amqpDeviceOperation.openLinks(session);
                    }
                }
            }
        }
    }

    /**
     * Delegate the close link call to device operation objects.
     */
    void closeLinks()
    {
        Iterator iterator = amqpsLinkMap.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry<MessageType, AmqpsLinksHandler> pair = (Map.Entry<MessageType, AmqpsLinksHandler>)iterator.next();
            pair.getValue().closeLinks();
        }
    }

    /**
     * Delegate the link initialization call to device operation objects.
     *
     * @param link the link that was initialized
     */
    void initLink(Link link)
    {
        if (link != null)
        {
            if (this.amqpsAuthenticatorState == AmqpsDeviceAuthenticationState.AUTHENTICATED)
            {
                for (Map.Entry<MessageType, AmqpsLinksHandler> entry : amqpsLinkMap.entrySet())
                {
                    entry.getValue().initLink(link);
                }
            }
        }
    }

    /**
     * Delegate the send call to device operation objects.
     * Loop through the device operation list and find the sender
     * object by message type and deviceId (connection string).
     *
     * @param message the message to send.
     * @param messageType the message type to find the sender.
     * @param deviceId the deviceId of the message
     * @throws IllegalStateException if sender link has not been initialized
     * @throws IllegalArgumentException if deliveryTag's length is 0
     * @return Integer
     */
    Integer sendMessage(org.apache.qpid.proton.message.Message message, MessageType messageType, String deviceId) throws IllegalStateException, IllegalArgumentException
    {
        if (this.amqpsAuthenticatorState == AmqpsDeviceAuthenticationState.AUTHENTICATED)
        {
            if (this.deviceClientConfig.getDeviceId().equals(deviceId))
            {
                byte[] msgData = new byte[1024];
                int length;

                while (true)
                {
                    try
                    {
                        length = message.encode(msgData, 0, msgData.length);
                        break;
                    }
                    catch (BufferOverflowException e)
                    {
                        msgData = new byte[msgData.length * 2];
                    }
                }

                byte[] deliveryTag = String.valueOf(this.nextTag).getBytes();

                //want to avoid negative delivery tags since -1 is the designated failure value
                if (this.nextTag == Integer.MAX_VALUE || this.nextTag < 0)
                {
                    this.nextTag = 0;
                }
                else
                {
                    this.nextTag++;
                }

                return this.sendMessageAndGetDeliveryTag(messageType, msgData, 0, length, deliveryTag);
            }
            else
            {
                return -1;
            }
        }
        else
        {
            log.trace("Attempted to send a message while state was not AUTHENTICATED, returning delivery tag of -1 ({})", message);
            return -1;
        }
    }

    /**
     * Delegate the send call to device operation objects.
     * Loop through the device operation list and find the sender 
     * object by message type. 
     *
     * @param messageType the message type to identify the sender.
     * @param msgData the binary content of the message.
     * @param offset the start index to read the binary.
     * @param length the length of the binary to read.
     * @param deliveryTag the message delivery tag.
     * @throws IllegalStateException if sender link has not been initialized
     * @throws IllegalArgumentException if deliveryTag's length is 0
     * @return Integer
     */
    private Integer sendMessageAndGetDeliveryTag(MessageType messageType, byte[] msgData, int offset, int length, byte[] deliveryTag) throws IllegalStateException, IllegalArgumentException
    {
        if (amqpsLinkMap.get(messageType) != null)
        {
            AmqpsSendReturnValue amqpsSendReturnValue = amqpsLinkMap.get(messageType).sendMessageAndGetDeliveryTag(messageType, msgData, offset, length, deliveryTag);
            if (amqpsSendReturnValue.isDeliverySuccessful())
            {
                return Integer.parseInt(new String(amqpsSendReturnValue.getDeliveryTag()));
            }
        }

        return -1;
    }

    /**
     * Delegate the onDelivery call to device operation objects.
     * Loop through the device operation list and find the receiver 
     * object by link name. 
     *
     * @param linkName the link name to identify the receiver.
     * @throws IllegalArgumentException if linkName argument is empty
     * @return AmqpsMessage if the receiver found the received
     *         message, otherwise null.
     */
    synchronized AmqpsMessage getMessageFromReceiverLink(String linkName)
    {
        AmqpsMessage amqpsMessage = null;

        for (Map.Entry<MessageType, AmqpsLinksHandler> entry : amqpsLinkMap.entrySet())
        {
            amqpsMessage = entry.getValue().getMessageFromReceiverLink(linkName);
            if (amqpsMessage != null)
            {
                break;
            }
        }

        return amqpsMessage;
    }

    boolean handleAuthenticationMessage(AmqpsMessage amqpsMessage, AmqpsAuthenticationLinkHandler amqpsAuthenticationLinkHandler)
    {
        boolean handledAuthenticationMessage = false;
        if (amqpsMessage != null)
        {
            synchronized (this.cbsCorrelationIdList)
            {
                UUID uuidFound = null;
                for (UUID correlationId : this.cbsCorrelationIdList)
                {
                    if (amqpsAuthenticationLinkHandler.handleAuthenticationMessage(amqpsMessage, correlationId))
                    {
                        this.amqpsAuthenticatorState = AmqpsDeviceAuthenticationState.AUTHENTICATED;
                        uuidFound = correlationId;
                        break;
                    }
                }

                if (uuidFound != null)
                {
                    this.cbsCorrelationIdList.remove(uuidFound);
                    handledAuthenticationMessage = true;
                }
            }
        }

        return handledAuthenticationMessage;
    }

    /**
     * Find the link by link name in the managed device operations. 
     *
     * @param link the link that remotely opened.
     *
     * @return Boolean true if found, false otherwise.
     */
    boolean onLinkRemoteOpen(Link link, AmqpsConnectionStateCallback sessionOpenedCallback)
    {
        boolean result = false;

        if (this.amqpsAuthenticatorState == AmqpsDeviceAuthenticationState.AUTHENTICATED && hasLink(link))
        {
            for (Map.Entry<MessageType, AmqpsLinksHandler> entry : amqpsLinkMap.entrySet())
            {
                if (entry.getValue().onLinkRemoteOpen(link))
                {
                    // If the link that is being opened is a sender link and the operation is a DeviceTwin operation, we will send a subscribe message on the opened link
                    if (entry.getKey() == MessageType.DEVICE_TWIN && link.equals(entry.getValue().getSenderLinkTag()))
                    {
                        // since we have already checked the message type, we can safely cast it
                        AmqpsTwinLinksHandler deviceTwinOperations = (AmqpsTwinLinksHandler)entry.getValue();
                        int deliveryTag = sendMessage(deviceTwinOperations.buildSubscribeToDesiredPropertiesProtonMessage(), entry.getKey(), deviceClientConfig.getDeviceId());
                        this.subscriptionMessageRequestSentCallback.onSubscriptionMessageSent(deliveryTag, SubscriptionMessageRequestSentCallback.SubscriptionType.DESIRED_PROPERTIES_SUBSCRIPTION);
                    }

                    result = true;
                }
            }

            for (Map.Entry<MessageType, AmqpsLinksHandler> entry : amqpsLinkMap.entrySet())
            {
                if (!entry.getValue().isOpen())
                {
                    return result;
                }
            }

            sessionOpenedCallback.onDeviceSessionOpened(this.deviceClientConfig.getDeviceId());
        }

        return result;
    }

    /**
     * Convert from IoTHub message to Proton using operation 
     * specific converter. 
     *
     * @param message the message to convert.
     * @return The result of the conversion containing the Proton message.
     */
    MessageImpl convertToProton(Message message)
    {
        if (message.getMessageType() == null)
        {
            message.setMessageType(DEVICE_TELEMETRY);
        }

        MessageType messageType = message.getMessageType();
        if (amqpsLinkMap.get(messageType) != null)
        {
            return this.amqpsLinkMap.get(messageType).iotHubMessageToProtonMessage(message);
        }
        else
        {
            return null;
        }
    }

    /**
     * Convert from IoTHub message to Proton using operation 
     * specific converter. 
     *
     * @param amqpsMessage the message to convert.
     * @param deviceClientConfig the device client configuration to 
     *                           identify the converter..
     * @return AmqpsConvertToProtonReturnValue the result of the
     *         conversion containing the Proton message.
     */
    IotHubTransportMessage convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig)
    {
        IotHubTransportMessage ret = null;
        if (this.amqpsLinkMap.get(amqpsMessage.getAmqpsMessageType()) != null)
        {
            ret =  this.amqpsLinkMap.get(amqpsMessage.getAmqpsMessageType()).protonMessageToIoTHubMessage(amqpsMessage, deviceClientConfig);
        }

        return ret;
    }

    public String getDeviceId()
    {
        return this.deviceClientConfig.getDeviceId();
    }

    public void subscribeToMessageType(MessageType messageType)
    {
        if (messageType == DEVICE_METHODS && !this.amqpsLinkMap.keySet().contains(DEVICE_METHODS))
        {
            this.amqpsLinkMap.put(DEVICE_METHODS, new AmqpsMethodsLinksHandler(this.deviceClientConfig));
            this.openLinks();
        }
        if (messageType == DEVICE_TWIN && !this.amqpsLinkMap.keySet().contains(DEVICE_TWIN))
        {
            this.amqpsLinkMap.put(DEVICE_TWIN, new AmqpsTwinLinksHandler(this.deviceClientConfig));
            this.openLinks();
        }
    }

    public boolean onLinkRemoteClose(String linkName)
    {
        for (Map.Entry<MessageType, AmqpsLinksHandler> entry : amqpsLinkMap.entrySet())
        {
            if (entry.getValue().onLinkRemoteClose(linkName))
            {
                return true;
            }
        }

        return false;
    }

    public boolean hasLink(Link link)
    {
        for (Map.Entry<MessageType, AmqpsLinksHandler> entry : amqpsLinkMap.entrySet())
        {
            if (entry.getValue().hasLink(link))
            {
                return true;
            }
        }

        return false;
    }
}
