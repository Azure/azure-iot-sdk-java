package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Session;

import java.nio.BufferOverflowException;
import java.util.*;

import static com.microsoft.azure.sdk.iot.device.MessageType.*;

@Slf4j
public class AmqpsSessionDeviceOperation
{
    private final DeviceClientConfig deviceClientConfig;

    private AmqpsDeviceAuthenticationState amqpsAuthenticatorState = AmqpsDeviceAuthenticationState.UNKNOWN;
    private final AmqpsDeviceAuthentication amqpsDeviceAuthentication;

    private Map<MessageType, AmqpsDeviceOperations> amqpsDeviceOperationsMap = new HashMap<MessageType, AmqpsDeviceOperations>();

    private static long nextTag = 0;

    private Integer openLock = new Integer(1);

    private List<UUID> cbsCorrelationIdList = Collections.synchronizedList(new ArrayList<UUID>());

    private SubscriptionMessageRequestSentCallback subscriptionMessageRequestSentCallback;

    /**
     * Create logical device entity to handle all operation.
     *
     * @param deviceClientConfig the configuration of teh device.
     * @param amqpsDeviceAuthentication the authentication object associated with the device.
     * @param subscriptionMessageRequestSentCallback the callback to fire each time a subscription message is sent
     * @throws IllegalArgumentException if deviceClientConfig or amqpsDeviceAuthentication is null
     */
    public AmqpsSessionDeviceOperation(final DeviceClientConfig deviceClientConfig, AmqpsDeviceAuthentication amqpsDeviceAuthentication, SubscriptionMessageRequestSentCallback subscriptionMessageRequestSentCallback) throws IllegalArgumentException
    {
        // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_001: [The constructor shall throw IllegalArgumentException if the deviceClientConfig or the amqpsDeviceAuthentication parameter is null.]
        if (deviceClientConfig == null)
        {
            throw new IllegalArgumentException("deviceClientConfig cannot be null.");
        }
        if (amqpsDeviceAuthentication == null)
        {
            throw new IllegalArgumentException("amqpsDeviceAuthentication cannot be null.");
        }

        // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_002: [The constructor shall save the deviceClientConfig and amqpsDeviceAuthentication parameter value to a member variable.]
        this.deviceClientConfig = deviceClientConfig;
        this.amqpsDeviceAuthentication = amqpsDeviceAuthentication;

        // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_003: [The constructor shall create AmqpsDeviceTelemetry and add it to the device operations map. ]
        this.amqpsDeviceOperationsMap.put(DEVICE_TELEMETRY, new AmqpsDeviceTelemetry(this.deviceClientConfig));

        if (this.deviceClientConfig.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
        {
            // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_004: [The constructor shall set the authentication state to not authenticated if the authentication type is CBS.]
            this.amqpsAuthenticatorState = AmqpsDeviceAuthenticationState.NOT_AUTHENTICATED;
        }
        else
        {
            // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_047[The constructor shall set the authentication state to authenticated if the authentication type is not CBS.]
            this.amqpsAuthenticatorState = AmqpsDeviceAuthenticationState.AUTHENTICATED;
        }

        this.subscriptionMessageRequestSentCallback = subscriptionMessageRequestSentCallback;
    }

    /**
     * Release all resources and close all links.
     */
    public void close()
    {
        this.closeLinks();

        if (this.deviceClientConfig.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
        {
            this.amqpsAuthenticatorState = AmqpsDeviceAuthenticationState.NOT_AUTHENTICATED;
            this.log.trace("Setting amqps device authentication state to NOT_AUTHENTICATED");
        }
    }

    /**
     * Start the authentication process.
     * In SAS case it is nothing to do.
     * In the CBS case start opening the authentication links
     * and send authentication messages.
     *
     * @throws TransportException if authentication message reply takes too long.
     */
    public void authenticate() throws TransportException
    {
        // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_006: [The function shall start the authentication if the authentication type is CBS.]
        if (this.deviceClientConfig.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
        {
            // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_060: [The function shall create a new UUID and add it to the correlationIdList if the authentication type is CBS.]
            UUID correlationId = UUID.randomUUID();
            synchronized (this.cbsCorrelationIdList)
            {
                this.log.trace("Adding correlation id to cbs correlation id list {}",correlationId);
                // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_061: [The function shall use the correlationID to call authenticate on the authentication object if the authentication type is CBS.]
                cbsCorrelationIdList.add(correlationId);
            }

            this.amqpsDeviceAuthentication.authenticate(this.deviceClientConfig, correlationId);

            // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_005: [The function shall set the authentication state to not authenticated if the authentication type is CBS.]
            this.log.trace("Setting amqps device authentication state to AUTHENTICATING");
            this.amqpsAuthenticatorState = AmqpsDeviceAuthenticationState.AUTHENTICATING;

            this.log.trace("Amqp session now waiting for service to acknowledge the sent authentication message");
        }
    }

    /**
     * @param session the Proton session to open the links on.
     */
    void openLinks(Session session)
    {
        // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_042: [The function shall do nothing if the session parameter is null.]
        if (session != null)
        {
            if (this.amqpsAuthenticatorState == AmqpsDeviceAuthenticationState.AUTHENTICATED)
            {
                synchronized (this.openLock)
                {
                    for (AmqpsDeviceOperations amqpDeviceOperation : this.amqpsDeviceOperationsMap.values())
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
        Iterator iterator = amqpsDeviceOperationsMap.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry<MessageType, AmqpsDeviceOperations> pair = (Map.Entry<MessageType, AmqpsDeviceOperations>)iterator.next();
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
        // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_043: [The function shall do nothing if the link parameter is null.]
        if (link != null)
        {
            if (this.amqpsAuthenticatorState == AmqpsDeviceAuthenticationState.AUTHENTICATED)
            {
                for (Map.Entry<MessageType, AmqpsDeviceOperations> entry : amqpsDeviceOperationsMap.entrySet())
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
        // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_012: [The function shall return -1 if the state is not authenticated.]
        if (this.amqpsAuthenticatorState == AmqpsDeviceAuthenticationState.AUTHENTICATED)
        {
            // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_013: [The function shall return -1 if the deviceId int he connection string is not equal to the deviceId in the config.]
            if (this.deviceClientConfig.getDeviceId().equals(deviceId))
            {
                // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_014: [The function shall encode the message and copy the contents to the byte buffer.]
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
                        // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_015: [The function shall doubles the buffer if encode throws BufferOverflowException.]
                        msgData = new byte[msgData.length * 2];
                    }
                }
                // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_017: [The function shall set the delivery tag for the sender.]
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

                // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_018: [The function shall call sendMessageAndGetDeliveryTag on all device operation objects.]
                // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_019: [The function shall return the delivery hash.]
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
        if (amqpsDeviceOperationsMap.get(messageType) != null)
        {
            AmqpsSendReturnValue amqpsSendReturnValue = amqpsDeviceOperationsMap.get(messageType).sendMessageAndGetDeliveryTag(messageType, msgData, offset, length, deliveryTag);
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
     * @throws TransportException if Proton throws
     * @return AmqpsMessage if the receiver found the received 
     *         message, otherwise null.
     */
    synchronized AmqpsMessage getMessageFromReceiverLink(String linkName) throws IllegalArgumentException, TransportException
    {
        AmqpsMessage amqpsMessage = null;

        // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_057: [If the state is other than authenticating the function shall try to read the message from the device operation objects.]
        for (Map.Entry<MessageType, AmqpsDeviceOperations> entry : amqpsDeviceOperationsMap.entrySet())
        {
            amqpsMessage = entry.getValue().getMessageFromReceiverLink(linkName);
            if (amqpsMessage != null)
            {
                break;
            }
        }

        return amqpsMessage;
    }

    boolean handleAuthenticationMessage(AmqpsMessage amqpsMessage)
    {
        boolean handledAuthenticationMessage = false;
        if (amqpsMessage != null)
        {
            synchronized (this.cbsCorrelationIdList)
            {
                UUID uuidFound = null;
                // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_055: [The function shall find the correlation ID in the correlationIdlist.]
                for (UUID correlationId : this.cbsCorrelationIdList)
                {
                    if (this.amqpsDeviceAuthentication.handleAuthenticationMessage(amqpsMessage, correlationId))
                    {
                        // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_053: [The function shall call handleAuthenticationMessage with the correlation ID on the authentication object and if it returns true set the authentication state to authenticated.]
                        this.log.trace("Setting amqps device authentication state to AUTHENTICATED");
                        this.amqpsAuthenticatorState = AmqpsDeviceAuthenticationState.AUTHENTICATED;
                        uuidFound = correlationId;
                        break;
                    }
                }
                // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_056: [The function shall remove the correlationId from the list if it is found.]
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
     * @param linkName the name to find.
     *
     * @return Boolean true if found, false otherwise.
     */
    boolean onLinkRemoteOpen(String linkName)
    {
        // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_024: [The function shall return true if any of the operation's link name is a match and return false otherwise.]
        if (this.amqpsAuthenticatorState == AmqpsDeviceAuthenticationState.AUTHENTICATED)
        {
            for (Map.Entry<MessageType, AmqpsDeviceOperations> entry : amqpsDeviceOperationsMap.entrySet())
            {
                if (entry.getValue().onLinkRemoteOpen(linkName))
                {
                    // If the link that is being opened is a sender link and the operation is a DeviceTwin operation, we will send a subscribe message on the opened link
                    if (entry.getKey() == MessageType.DEVICE_TWIN && linkName.equals(entry.getValue().getSenderLinkTag()))
                    {
                        // since we have already checked the message type, we can safely cast it
                        AmqpsDeviceTwin deviceTwinOperations = (AmqpsDeviceTwin)entry.getValue();
                        int deliveryTag = sendMessage(deviceTwinOperations.buildSubscribeToDesiredPropertiesProtonMessage(), entry.getKey(), deviceClientConfig.getDeviceId());
                        this.subscriptionMessageRequestSentCallback.onSubscriptionMessageSent(deliveryTag, SubscriptionMessageRequestSentCallback.SubscriptionType.DESIRED_PROPERTIES_SUBSCRIPTION);
                    }

                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Convert from IoTHub message to Proton using operation 
     * specific converter. 
     *
     * @param message the message to convert.
     * @throws TransportException if conversion fails.
     * @return AmqpsConvertToProtonReturnValue the result of the 
     *         conversion containing the Proton message.
     */
    AmqpsConvertToProtonReturnValue convertToProton(Message message) throws TransportException
    {
        MessageType msgType;
        if (message.getMessageType() == null)
        {
            msgType = DEVICE_TELEMETRY;
        }
        else
        {
            msgType = message.getMessageType();
        }

        if (amqpsDeviceOperationsMap.get(msgType) != null)
        {
            // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_040: [The function shall call all device operation's convertToProton, and if any of them not null return with the value.]
            return this.amqpsDeviceOperationsMap.get(msgType).convertToProton(message);
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
     * @throws TransportException if conversion fails
     * @return AmqpsConvertToProtonReturnValue the result of the 
     *         conversion containing the Proton message.
     */
    AmqpsConvertFromProtonReturnValue convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig) throws TransportException
    {
        AmqpsConvertFromProtonReturnValue ret = null;
        if (this.amqpsDeviceOperationsMap.get(amqpsMessage.getAmqpsMessageType()) != null)
        {
            ret =  this.amqpsDeviceOperationsMap.get(amqpsMessage.getAmqpsMessageType()).convertFromProton(amqpsMessage, deviceClientConfig);
        }

        return ret;
    }

    int getExpectedWorkerLinkCount()
    {
        //For each entry (Telemetry, twin, and/or methods) there is a sender and a receiver link. This applies for both device connections and module connections
        return this.amqpsDeviceOperationsMap.size() * 2;
    }

    public String getDeviceId()
    {
        return this.deviceClientConfig.getDeviceId();
    }

    public void subscribeToMessageType(Session session, MessageType messageType)
    {
        if (messageType == DEVICE_METHODS && !this.amqpsDeviceOperationsMap.keySet().contains(DEVICE_METHODS))
        {
            this.amqpsDeviceOperationsMap.put(DEVICE_METHODS, new AmqpsDeviceMethods(this.deviceClientConfig));
            this.openLinks(session);
        }
        if (messageType == DEVICE_TWIN && !this.amqpsDeviceOperationsMap.keySet().contains(DEVICE_TWIN))
        {
            this.amqpsDeviceOperationsMap.put(DEVICE_TWIN, new AmqpsDeviceTwin(this.deviceClientConfig));
            this.openLinks(session);
        }
    }

    public boolean onLinkRemoteClose(String linkName)
    {
        for (Map.Entry<MessageType, AmqpsDeviceOperations> entry : amqpsDeviceOperationsMap.entrySet())
        {
            if (entry.getValue().onLinkRemoteClose(linkName))
            {
                return true;
            }
        }

        return false;
    }
}
