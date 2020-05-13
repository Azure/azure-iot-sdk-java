package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.engine.Transport;

import java.util.ArrayList;


/**
 * Manage multiple device clients and the authentication 
 * mechanism
 */
@Slf4j
public class AmqpsSessionManager
{
    private final DeviceClientConfig deviceClientConfig;
    protected Session session = null;

    private AmqpsDeviceAuthentication amqpsDeviceAuthentication;
    private ArrayList<AmqpsSessionDeviceOperation> amqpsDeviceSessionList = new ArrayList<>();
    private SubscriptionMessageRequestSentCallback subscriptionMessageRequestSentCallback;

    /**
     * Constructor that takes a device configuration.
     *
     * @param deviceClientConfig the device configuration to use for 
     *                           session management.
     * @param subscriptionMessageRequestSentCallback the callback to fire each time a subscription message is sent
     */
    public AmqpsSessionManager(DeviceClientConfig deviceClientConfig, SubscriptionMessageRequestSentCallback subscriptionMessageRequestSentCallback)
    {
        // Codes_SRS_AMQPSESSIONMANAGER_12_001: [The constructor shall throw IllegalArgumentException if the deviceClientConfig parameter is null.]
        if (deviceClientConfig == null)
        {
            throw new IllegalArgumentException("deviceClientConfig cannot be null.");
        }

        // Codes_SRS_AMQPSESSIONMANAGER_12_002: [The constructor shall save the deviceClientConfig parameter value to a member variable.]
        this.deviceClientConfig = deviceClientConfig;

        // Codes_SRS_AMQPSESSIONMANAGER_12_003: [The constructor shall create AmqpsDeviceAuthenticationSAS if the authentication type is SAS.]
        switch (this.deviceClientConfig.getAuthenticationType())
        {
            case SAS_TOKEN:
                // Codes_SRS_AMQPSESSIONMANAGER_12_005: [The constructor shall create AmqpsDeviceAuthenticationCBSTokenRenewalTask if the authentication type is CBS.]
                this.amqpsDeviceAuthentication = new AmqpsDeviceAuthenticationCBS(this.deviceClientConfig);
                break;

            case X509_CERTIFICATE:
                this.amqpsDeviceAuthentication = new AmqpsDeviceAuthenticationX509(this.deviceClientConfig);
                break;
        }

        this.subscriptionMessageRequestSentCallback = subscriptionMessageRequestSentCallback;

        // Codes_SRS_AMQPSESSIONMANAGER_12_007: [The constructor shall add the create a AmqpsSessionDeviceOperation with the given deviceClientConfig.]
        this.addDeviceOperationSession(this.deviceClientConfig);
    }

    /**
     * Register the given device to the manager.
     *
     * @param deviceClientConfig the device to register.
     */
    final void addDeviceOperationSession(DeviceClientConfig deviceClientConfig)
    {
        // Codes_SRS_AMQPSESSIONMANAGER_12_008: [The function shall throw IllegalArgumentException if the deviceClientConfig parameter is null.]
        if (deviceClientConfig == null)
        {
            throw new IllegalArgumentException("deviceClientConfig cannot be null.");
        }

        // Codes_SRS_AMQPSESSIONMANAGER_12_009: [The function shall create a new  AmqpsSessionDeviceOperation with the given deviceClientConfig and add it to the session list.]
        AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(deviceClientConfig, this.amqpsDeviceAuthentication, this.subscriptionMessageRequestSentCallback);
        this.amqpsDeviceSessionList.add(amqpsSessionDeviceOperation);
    }

    /**
     * Close the Proton objects and the schedulers.
     * After calling this function all resource freed.
     *
     */
    void closeNow()
    {
        this.log.debug("Closing AMQP session");

        // Codes_SRS_AMQPSESSIONMANAGER_12_010: [The function shall call all device session to closeNow links.]
        for (int i = 0; i < this.amqpsDeviceSessionList.size(); i++)
        {
            if (this.amqpsDeviceSessionList.get(i) != null)
            {
                this.amqpsDeviceSessionList.get(i).close();
            }
        }

        // Codes_SRS_AMQPSESSIONMANAGER_12_011: [The function shall closeNow the authentication links.]
        this.amqpsDeviceAuthentication.closeLinks();

        // Codes_SRS_AMQPSESSIONMANAGER_12_012: [The function shall closeNow the session.]
        if (this.session != null)
        {
            this.session.close();
            this.session = null;
        }
    }

    /**
     * Start the authetication process.
     *
     * @throws TransportException if authentication lock throws.
     */
    public void authenticate() throws TransportException
    {
        if (this.deviceClientConfig.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
        {
            for (int i = 0; i < this.amqpsDeviceSessionList.size(); i++)
            {
                if (this.amqpsDeviceSessionList.get(i) != null)
                {
                    // Codes_SRS_AMQPSESSIONMANAGER_12_015: [The function shall call authenticate on all session list members.]
                    this.amqpsDeviceSessionList.get(i).authenticate();
                }
            }
        }
    }

    protected void subscribeDeviceToMessageType(MessageType messageType, String deviceId)
    {
        this.log.trace("Subscribing to {}", messageType);
        // Codes_SRS_AMQPSESSIONMANAGER_12_018: [The function shall do nothing if the session is not open.]
        if (this.session != null)
        {
            for (int i = 0; i < this.amqpsDeviceSessionList.size(); i++)
            {
                if (this.amqpsDeviceSessionList.get(i).getDeviceId().equals(deviceId))
                {
                    this.amqpsDeviceSessionList.get(i).subscribeToMessageType(this.session, messageType);
                    return;
                }
            }
        }
    }

    /**
     * Event handler for connection initialization. 
     * Open the session and the links. 
     *
     * @param connection the Proton connection object to work with.
     */
    void onConnectionInit(Connection connection) throws TransportException
    {
        if (connection != null)
        {
            if (this.session == null)
            {
                // Codes_SRS_AMQPSESSIONMANAGER_12_023: [The function shall initialize the session member variable from the connection if the session is null.]
                this.session = connection.session();

                this.log.trace("Opening session...");
                // Codes_SRS_AMQPSESSIONMANAGER_12_024: [The function shall open the initialized session.]
                this.session.open();
            }
        }
    }

    void onSessionRemoteOpen(Session session)
    {
        if (this.amqpsDeviceAuthentication instanceof AmqpsDeviceAuthenticationCBS)
        {
            this.amqpsDeviceAuthentication.openLinks(session);
        }
        else
        {
            this.openWorkerLinks();
        }
    }

    /**
     * Opens all the operation links by calling the AmqpsSessionManager.
     */
    public void openWorkerLinks()
    {
        // Codes_SRS_AMQPSESSIONMANAGER_12_018: [The function shall do nothing if the session is not open.]
        if (this.session != null)
        {
            for (int i = 0; i < this.amqpsDeviceSessionList.size(); i++)
            {
                if (this.amqpsDeviceSessionList.get(i) != null)
                {
                    // Codes_SRS_AMQPSESSIONMANAGER_12_019: [The function shall call openWorkerLinks on all session list members.]
                    this.amqpsDeviceSessionList.get(i).openLinks(this.session);
                }
            }
        }
    }

    /**
     * Event handler for connection bond. 
     * Set the SSL domain and the SSL context.
     *
     * @param transport the Proton transport object to work with.
     */
    void onConnectionBound(Transport transport) throws TransportException
    {
        if (this.session != null)
        {
            // Codes_SRS_AMQPSESSIONMANAGER_12_026: [The function shall call setSslDomain on authentication if the session is not null.]
            this.amqpsDeviceAuthentication.setSslDomain(transport);
        }
    }

    /**
     * Event handler for link initialization. 
     * If the manager is in opening state initialize the 
     * authentication links. 
     * If the manager is in opened state initialize the operation 
     * links. 
     *
     * @param link the link to initialize.
     */
    void onLinkInit(Link link)
    {
        if (this.session != null)
        {
            if (this.isAuthenticationOpened())
            {
                for (int i = 0; i < this.amqpsDeviceSessionList.size(); i++)
                {
                    // Codes_SRS_AMQPSESSIONMANAGER_12_027: [The function shall call authentication initLink on all session list member if the authentication is open and the session is not null.]
                    this.amqpsDeviceSessionList.get(i).initLink(link);
                }
            }
            else
            {
                // Codes_SRS_AMQPSESSIONMANAGER_12_028: [The function shall call authentication initLink if the authentication is not open and the session is not null.]
                this.amqpsDeviceAuthentication.initLink(link);
            }
        }
    }

    /**
     * Event handler for link open. 
     * If the manager is in opening state check the authentication 
     * links state. 
     * If the manager is in opened state check the operation links 
     * state. 
     *
     * @param link Proton link object that was opened
     *
     * @return Boolean true if all links open, false otherwise.
     */
    boolean onLinkRemoteOpen(Link link)
    {
        String linkName = link.getName();
        if (this.isAuthenticationOpened())
        {
            for (int i = 0; i < this.amqpsDeviceSessionList.size(); i++)
            {
                if (this.amqpsDeviceSessionList.get(i).onLinkRemoteOpen(linkName))
                {
                    //found the worker link that was opened in the list of amqpSessionDeviceOperations and updated its state to OPEN
                    return true;
                }
            }
        }
        else
        {
            //If the link was not a worker link, then it should be a cbs link
            return this.amqpsDeviceAuthentication.onLinkRemoteOpen(linkName);
        }

        this.log.warn("onLinkRemoteOpen could not be correlated with a local link, ignoring it");

        //If the link was not a worker link, and it wasn't a cbs link, then it was not handled
        return false;
    }

    /**
     * Delegate the send call to device operation objects. 
     * Loop through the device operation list and find the sender 
     * object by message type and deviceId (connection string). 
     *
     * @param message the message to send.
     * @param messageType the message type to find the sender. 
     * @return Integer
     */
    Integer sendMessage(org.apache.qpid.proton.message.Message message, MessageType messageType, String deviceId) throws TransportException
    {
        Integer deliveryTag = -1;

        if (this.session != null)
        {
            for (int i = 0; i < this.amqpsDeviceSessionList.size(); i++)
            {
                // Codes_SRS_AMQPSESSIONMANAGER_12_032: [The function shall call sendMessage on all session list member and if there is a successful send return with the deliveryHash, otherwise return -1.]
                deliveryTag = this.amqpsDeviceSessionList.get(i).sendMessage(message, messageType, deviceId);
                if (deliveryTag != -1)
                {
                    break;
                }
            }
            if (deliveryTag == -1)
            {
                log.trace("Attempt to send message over amqp failed because no session handled it ({})", message);
            }
        }
        else
        {
            log.trace("Attempt to send message over amqp failed because the associated session is null ({})", message);
        }

        return deliveryTag;
    }

    /**
     * Delegate the onDelivery call to device operation objects.
     * Loop through the device operation list and find the receiver `
     * object by link name. 
     *
     * @param linkName the link name to identify the receiver.
     *
     * @return AmqpsMessage if the receiver found the received 
     *         message, otherwise null.
     */
    AmqpsMessage getMessageFromReceiverLink(String linkName) throws IllegalArgumentException, TransportException
    {
        AmqpsMessage amqpsMessage = null;

        // Codes_SRS_AMQPSESSIONMANAGER_12_033: [The function shall do nothing and return null if the session is not open.]
        if (this.session != null)
        {
            if (linkName.startsWith(AmqpsDeviceAuthenticationCBS.RECEIVER_LINK_TAG_PREFIX) || linkName.startsWith(AmqpsDeviceAuthenticationCBS.SENDER_LINK_TAG_PREFIX))
            {
                // Codes_SRS_AMQPSESSIONMANAGER_12_034: [The function shall call authentication getMessageFromReceiverLink if the authentication is not open.]
                amqpsMessage = this.amqpsDeviceAuthentication.getMessageFromReceiverLink(linkName);

                for (int i = 0; i < this.amqpsDeviceSessionList.size(); i++)
                {
                    if (this.amqpsDeviceSessionList.get(i).handleAuthenticationMessage(amqpsMessage))
                    {
                        break;
                    }
                }
            }
            else
            {
                for (int i = 0; i < this.amqpsDeviceSessionList.size(); i++)
                {
                    // Codes_SRS_AMQPSESSIONMANAGER_12_035: [The function shall call device sessions getMessageFromReceiverLink if the authentication is open.]
                    amqpsMessage = this.amqpsDeviceSessionList.get(i).getMessageFromReceiverLink(linkName);
                    if (amqpsMessage != null)
                    {
                        break;
                    }
                }
            }
        }

        return amqpsMessage;
    }

    /**
     * Get the status of the authentication links.
     *
     * @return Boolean true if all link open, false otherwise.
     */
    boolean isAuthenticationOpened()
    {
        // Codes_SRS_AMQPSESSIONMANAGER_12_039: [The function shall return with the return value of authentication.onLinkRemoteOpen.]
        return (this.amqpsDeviceAuthentication.operationLinksOpened());
    }

    /**
     * Find the converter to convert from IoTHub message to Proton 
     * message.
     *
     * @param message the message to convert.
     *
     * @return AmqpsConvertToProtonReturnValue the result of the 
     *         conversion containing the Proton message.
     */
    AmqpsConvertToProtonReturnValue convertToProton(com.microsoft.azure.sdk.iot.device.Message message) throws TransportException
    {
        AmqpsConvertToProtonReturnValue amqpsConvertToProtonReturnValue = null;

        for (int i = 0; i < this.amqpsDeviceSessionList.size(); i++)
        {
            // Codes_SRS_AMQPSESSIONMANAGER_12_040: [The function shall call all device session's convertToProton, and if any of them not null return with the value.]
            amqpsConvertToProtonReturnValue = this.amqpsDeviceSessionList.get(i).convertToProton(message);
            if (amqpsConvertToProtonReturnValue != null)
            {
                break;
            }
        }

        return amqpsConvertToProtonReturnValue;
    }

    /**
     * Find the converter to convert Proton message to IoTHub 
     * message. Loop through the managed devices and find the 
     * converter. 
     *
     * @param amqpsMessage the Proton message to convert.
     * @param deviceClientConfig the device client configuration for
     *                           add identification data to the
     *                           message.
     *
     * @return AmqpsConvertFromProtonReturnValue the result of the 
     *         conversion containing the IoTHub message.
     * @throws TransportException if converting the message fails
     */
    AmqpsConvertFromProtonReturnValue convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig) throws TransportException
    {
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = null;

        for (int i = 0; i < this.amqpsDeviceSessionList.size(); i++)
        {
            // Codes_SRS_AMQPSESSIONMANAGER_12_041: [The function shall call all device session's convertFromProton, and if any of them not null return with the value.]
            amqpsConvertFromProtonReturnValue = this.amqpsDeviceSessionList.get(i).convertFromProton(amqpsMessage, deviceClientConfig);
            if (amqpsConvertFromProtonReturnValue != null)
            {
                break;
            }
        }

        return amqpsConvertFromProtonReturnValue;
    }

    int getExpectedWorkerLinkCount()
    {
        int expectedWorkerLinkCount = 0;
        for (AmqpsSessionDeviceOperation deviceOperation : this.amqpsDeviceSessionList)
        {
            expectedWorkerLinkCount += deviceOperation.getExpectedWorkerLinkCount();
        }

        return expectedWorkerLinkCount;
    }

    public void onLinkRemoteClose(Link link)
    {
        String linkName = link.getName();

        for (int i = 0; i < this.amqpsDeviceSessionList.size(); i++)
        {
            if (this.amqpsDeviceSessionList.get(i).onLinkRemoteClose(linkName))
            {
                //found the worker link that was closed in the list of amqpSessionDeviceOperations and updated its state to CLOSED
                return;
            }
        }

        //If the link was not a worker link, then it should be a cbs link
        if (this.amqpsDeviceAuthentication.onLinkRemoteClose(linkName))
        {
            return;
        }

        this.log.warn("onLinkRemoteClose could not be correlated with a local link, ignoring it");
    }
}
