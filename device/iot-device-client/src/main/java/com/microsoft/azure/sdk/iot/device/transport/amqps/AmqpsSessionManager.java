package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.CustomLogger;
import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.ObjectLock;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import org.apache.qpid.proton.engine.*;
import java.util.ArrayList;
import java.util.concurrent.*;


/**
 * Manage multiple device clients and the authentication 
 * mechanism
 */
public class AmqpsSessionManager
{
    private final DeviceClientConfig deviceClientConfig;
    protected Session session = null;

    private AmqpsDeviceAuthentication amqpsDeviceAuthentication;
    private ArrayList<AmqpsSessionDeviceOperation> amqpsDeviceSessionList = new ArrayList<>();

    private static final int MAX_WAIT_TO_AUTHENTICATE_MS = 10*1000;

    private final ObjectLock openLinksLock = new ObjectLock();

    private CustomLogger logger;

    /**
     * Constructor that takes a device configuration.
     *
     * @param deviceClientConfig the device configuration to use for 
     *                           session management.
     * @throws TransportException if a transport error occurs.
     */
    public AmqpsSessionManager(DeviceClientConfig deviceClientConfig) throws TransportException
    {
        // Codes_SRS_AMQPSESSIONMANAGER_12_001: [The constructor shall throw IllegalArgumentException if the deviceClientConfig parameter is null.]
        if (deviceClientConfig == null)
        {
            throw new IllegalArgumentException("deviceClientConfig cannot be null.");
        }

        this.logger = new CustomLogger(this.getClass());

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

        // Codes_SRS_AMQPSESSIONMANAGER_12_007: [The constructor shall add the create a AmqpsSessionDeviceOperation with the given deviceClientConfig.]
        this.addDeviceOperationSession(this.deviceClientConfig);
    }

    /**
     * Register the given device to the manager.
     *
     * @param deviceClientConfig the device to register.
     */
    final void addDeviceOperationSession(DeviceClientConfig deviceClientConfig) throws TransportException
    {
        // Codes_SRS_AMQPSESSIONMANAGER_12_008: [The function shall throw IllegalArgumentException if the deviceClientConfig parameter is null.]
        if (deviceClientConfig == null)
        {
            throw new IllegalArgumentException("deviceClientConfig cannot be null.");
        }

        // Codes_SRS_AMQPSESSIONMANAGER_12_009: [The function shall create a new  AmqpsSessionDeviceOperation with the given deviceClientConfig and add it to the session list.]
        AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(deviceClientConfig, this.amqpsDeviceAuthentication);
        this.amqpsDeviceSessionList.add(amqpsSessionDeviceOperation);
    }

    /**
     * Close the Proton objects and the schedulers.
     * After calling this function all resource freed.
     *
     */
    void closeNow()
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

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

        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Start the authetication process.
     *
     * @throws TransportException if authentication lock throws.
     */
    public void authenticate() throws TransportException
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        if (this.deviceClientConfig.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
        {
            // Codes_SRS_AMQPSESSIONMANAGER_12_014: [The function shall do nothing if the authentication is not open.]
            if (this.isAuthenticationOpened())
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

        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Loop through the device list and open the links. 
     * Lock the execution to wait for the open finish. 
     *
     * @throws TransportException if open lock throws.
     */
    public void openDeviceOperationLinks(MessageType msgType) throws TransportException
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        // Codes_SRS_AMQPSESSIONMANAGER_12_018: [The function shall do nothing if the session is not open.]
        if (this.session != null)
        {
            for (int i = 0; i < this.amqpsDeviceSessionList.size(); i++)
            {
                if (this.amqpsDeviceSessionList.get(i) != null)
                {
                    // Codes_SRS_AMQPSESSIONMANAGER_12_019: [The function shall call openLinks on all session list members.]
                    if (this.amqpsDeviceSessionList.get(i).openLinks(this.session, msgType))
                    {
                        synchronized (this.openLinksLock)
                        {
                            try
                            {
                                // Codes_SRS_AMQPSESSIONMANAGER_12_020: [The function shall lock the execution with waitLock.]
                                this.openLinksLock.waitLock(MAX_WAIT_TO_AUTHENTICATE_MS);
                            }
                            catch (InterruptedException e)
                            {
                                // Codes_SRS_AMQPSESSIONMANAGER_12_021: [The function shall throw TransportException if the lock throws.]
                                throw new TransportException("Waited too long for the connection to onConnectionInit.");
                            }
                        }
                    }
                }
            }
        }

        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Event handler for connection initialization. 
     * Open the session and the links. 
     *
     * @param connection the Proton connection object to work with.
     * @return Boolean true if connection is ready, otherwise false to indicate authentication links open in progress
     */
    Boolean onConnectionInit(Connection connection) throws TransportException
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        Boolean ret = false;
        if (connection != null)
        {
            if (this.session == null)
            {
                // Codes_SRS_AMQPSESSIONMANAGER_12_023: [The function shall initialize the session member variable from the connection if the session is null.]
                this.session = connection.session();
                // Codes_SRS_AMQPSESSIONMANAGER_12_024: [The function shall open the initialized session.]
                this.session.open();
            }
        }

        if (this.session != null)
        {
            if (this.isAuthenticationOpened())
            {
                ret = true;
            }
            else
            {
                // Codes_SRS_AMQPSESSIONMANAGER_12_025: [The function shall call authentication's openLink if the session is not null and the authentication is not open.]
                this.amqpsDeviceAuthentication.openLinks(this.session);
            }
        }

        logger.LogDebug("Exited from method %s", logger.getMethodName());
        return ret;
    }

    /**
     * Event handler for connection bond. 
     * Set the SSL domain and the SSL context.
     *
     * @param transport the Proton transport object to work with.
     */
    void onConnectionBound(Transport transport) throws TransportException
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        if (this.session != null)
        {
            // Codes_SRS_AMQPSESSIONMANAGER_12_026: [The function shall call setSslDomain on authentication if the session is not null.]
            this.amqpsDeviceAuthentication.setSslDomain(transport);
        }

        logger.LogDebug("Exited from method %s", logger.getMethodName());
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
    void onLinkInit(Link link) throws TransportException, IllegalArgumentException
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

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

        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Event handler for link open. 
     * If the manager is in opening state check the authentication 
     * links state. 
     * If the manager is in opened state check the operation links 
     * state. 
     *
     * @param event Proton Event object to get the link name.
     *
     * @return Boolean true if all links open, false otherwise.
     */
    boolean onLinkRemoteOpen(Event event)
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        Boolean isLinkFound = false;

        String linkName = event.getLink().getName();
        if (this.isAuthenticationOpened())
        {
            for (int i = 0; i < this.amqpsDeviceSessionList.size(); i++)
            {
                isLinkFound = this.amqpsDeviceSessionList.get(i).isLinkFound(linkName);
                if (isLinkFound == true)
                {
                    if (this.amqpsDeviceSessionList.get(i).operationLinksOpened())
                    {
                        logger.LogDebug("before notify openLinksLock.");
                        synchronized (this.openLinksLock)
                        {
                            // Codes_SRS_AMQPSESSIONMANAGER_12_031: [The function shall call authentication isLinkFound if the authentication is not open and return true if both links are open]
                            this.openLinksLock.notifyLock();
                        }
                        logger.LogDebug("after notify openLinksLock.");
                        break;
                    }
                }
            }
        }
        else
        {
            if (this.amqpsDeviceAuthentication.isLinkFound(linkName))
            {
                // Codes_SRS_AMQPSESSIONMANAGER_12_030: [The function shall call authentication isLinkFound if the authentication is not open and return false if only one link is open]
                if (this.isAuthenticationOpened())
                {
                    // Codes_SRS_AMQPSESSIONMANAGER_12_029: [The function shall call authentication isLinkFound if the authentication is not open and return true if both links are open]
                    isLinkFound = true;
                }
            }
        }

        logger.LogDebug("Exited from method %s", logger.getMethodName());

        return isLinkFound;
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
            if (this.isAuthenticationOpened())
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
            else
            {
                // Codes_SRS_AMQPSESSIONMANAGER_12_034: [The function shall call authentication getMessageFromReceiverLink if the authentication is not open.]
                amqpsMessage = this.amqpsDeviceAuthentication.getMessageFromReceiverLink(linkName);
            }
        }

        return amqpsMessage;
    }

    boolean areAllLinksOpen()
    {
        boolean areAllLinksOpen = true;
        if (this.isAuthenticationOpened())
        {
            for (int i = 0; i < this.amqpsDeviceSessionList.size(); i++)
            {
                // Codes_SRS_AMQPSESSIONMANAGER_34_044: [If this object's authentication is open, this function shall return if all saved sessions' links are open.]
                areAllLinksOpen &= this.amqpsDeviceSessionList.get(i).operationLinksOpened();
            }
        }
        else
        {
            // Codes_SRS_AMQPSESSIONMANAGER_34_045: [If this object's authentication is not open, this function shall return false.]
            return false;
        }

        return areAllLinksOpen;
    }

    /**
     * Get the status of the authentication links.
     *
     * @return Boolean true if all link open, false otherwise.
     */
    Boolean isAuthenticationOpened()
    {
        // Codes_SRS_AMQPSESSIONMANAGER_12_039: [The function shall return with the return value of authentication.operationLinksOpened.]
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

    public void onLinkFlow(Event event)
    {
        for (int i = 0; i < this.amqpsDeviceSessionList.size(); i++)
        {
            if (this.amqpsDeviceSessionList.get(i).onLinkFlow(event))
            {
                break;
            }
        }
    }
}
