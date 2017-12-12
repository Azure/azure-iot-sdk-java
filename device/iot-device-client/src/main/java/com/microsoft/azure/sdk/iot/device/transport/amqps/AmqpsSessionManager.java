package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.ObjectLock;
import org.apache.qpid.proton.engine.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.*;


/**
 * Manage multiple device clients and the authentication 
 * mechanism 
 *
 */
public class AmqpsSessionManager
{
    private final DeviceClientConfig deviceClientConfig;
    protected Session session = null;

    private AmqpsDeviceAuthentication amqpsDeviceAuthentication;
    private ArrayList<AmqpsSessionDeviceOperation> amqpsDeviceSessionList = new ArrayList<>();

    private long SEND_PERIOD_MILLISECONDS = 300;
    private ScheduledExecutorService taskSchedulerCBSSend;
    private AmqpsDeviceAuthenticationCBSSendTask cbsAuthSendTask = null;

    private static final int MAX_WAIT_TO_AUTHENTICATE_MS = 10*1000;

    private final ObjectLock openLinksLock = new ObjectLock();

    /**
     * Constructor that takes a device configuration.
     *
     * @param deviceClientConfig the device configuration to use for 
     *                           session management.
     */
    public AmqpsSessionManager(DeviceClientConfig deviceClientConfig)
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

                // Codes_SRS_AMQPSESSIONMANAGER_12_006: [The constructor shall create and start a scheduler for AmqpsDeviceAuthenticationCBSTokenRenewalTask if the authentication type is CBS.]
                this.cbsAuthSendTask = new AmqpsDeviceAuthenticationCBSSendTask((AmqpsDeviceAuthenticationCBS) this.amqpsDeviceAuthentication);
                this.taskSchedulerCBSSend = Executors.newScheduledThreadPool(2);
                this.taskSchedulerCBSSend.scheduleAtFixedRate(this.cbsAuthSendTask, 0, SEND_PERIOD_MILLISECONDS, TimeUnit.MILLISECONDS);

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
    void addDeviceOperationSession(DeviceClientConfig deviceClientConfig)
    {
        // Codes_SRS_AMQPSESSIONMANAGER_12_008: [The function shall throw IllegalArgumentException if the deviceClientConfig parameter is null.]
        if (deviceClientConfig == null)
        {
            throw new IllegalArgumentException("deviceClientConfig cannot be null.");
        }

        // Codes_SRS_AMQPSESSIONMANAGER_12_009: [The function shall create a new  AmqpsSessionDeviceOperation with the given deviceClietnConfig and add it to the session list.]
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
        // Codes_SRS_AMQPSESSIONMANAGER_12_043: [THe function shall shut down the scheduler.]
        this.shutDownScheduler();

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
     * @throws IOException if authentication lock throws.
     */
    public void authenticate() throws IOException
    {
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
    }

    /**
     * Loop through the device list and open the links. 
     * Lock the execution to wait for the open finish. 
     *
     * @throws IOException if open lock throws.
     */
    public void openDeviceOperationLinks() throws IOException
    {
        // Codes_SRS_AMQPSESSIONMANAGER_12_018: [The function shall do nothing if the session is not open.]
        if (this.session != null)
        {
            for (int i = 0; i < this.amqpsDeviceSessionList.size(); i++)
            {
                if (this.amqpsDeviceSessionList.get(i) != null)
                {
                    // Codes_SRS_AMQPSESSIONMANAGER_12_019: [The function shall call openLinks on all session list members.]
                    this.amqpsDeviceSessionList.get(i).openLinks(this.session);
                    synchronized (this.openLinksLock)
                    {
                        try
                        {
                            // Codes_SRS_AMQPSESSIONMANAGER_12_020: [The function shall lock the execution with waitLock.]
                            this.openLinksLock.waitLock(MAX_WAIT_TO_AUTHENTICATE_MS);
                        }
                        catch (InterruptedException e)
                        {
                            // Codes_SRS_AMQPSESSIONMANAGER_12_021: [The function shall throw IOException if the lock throws.]
                            throw new IOException("Waited too long for the connection to onConnectionInit.");
                        }
                    }
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
    void onConnectionInit(Connection connection) throws IOException
    {
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
                for (int i = 0; i < this.amqpsDeviceSessionList.size(); i++)
                {
                    // Codes_SRS_AMQPSESSIONMANAGER_12_042: [The function shall call openLinks on all device sessions if the session is not null and the authentication is open.]
                    this.amqpsDeviceSessionList.get(i).openLinks(this.session);
                }
            }
            else
            {
                // Codes_SRS_AMQPSESSIONMANAGER_12_025: [The function shall call authentication's openLink if the session is not null and the authentication is not open.]
                this.amqpsDeviceAuthentication.openLinks(this.session);
            }
        }
    }

    /**
     * Event handler for connection bond. 
     * Set the SSL domain and the SSL context.
     *
     * @param transport the Proton transport object to work with.
     */
    void onConnectionBound(Transport transport)
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
    void onLinkInit(Link link) throws IOException, IllegalArgumentException
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
     * @param event Proton Event object to get the link name.
     *
     * @return Boolean true if all links open, false otherwise.
     */
    boolean onLinkRemoteOpen(Event event)
    {
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
                        synchronized (this.openLinksLock)
                        {
                            // Codes_SRS_AMQPSESSIONMANAGER_12_031: [The function shall call authentication isLinkFound if the authentication is not open and return true if both links are open]
                            this.openLinksLock.notifyLock();
                        }
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

        return isLinkFound;
    }

    /**
     * Delegate the send call to device operation objects. 
     * Loop through the device operation list and find the sender 
     * object by message type and deviceId (connection string). 
     *
     * @param message the message to send.
     * @param messageType the message type to find the sender. 
     * @param iotHubConnectionString the deviceconnection string to 
     *                               find the sender.
     *
     * @return Integer
     */
    Integer sendMessage(org.apache.qpid.proton.message.Message message, MessageType messageType, IotHubConnectionString iotHubConnectionString) throws IOException
    {
        Integer deliveryHash = -1;

        if (this.session != null)
        {
            for (int i = 0; i < this.amqpsDeviceSessionList.size(); i++)
            {
                // Codes_SRS_AMQPSESSIONMANAGER_12_032: [The function shall call sendMessage on all session list member and if there is a successful send return with the deliveryHash, otherwise return -1.]
                deliveryHash = this.amqpsDeviceSessionList.get(i).sendMessage(message, messageType, iotHubConnectionString);
                if (deliveryHash != -1)
                {
                    break;
                }
            }
        }

        return deliveryHash;
    }

    /**
     * Delegate the onDelivery call to device operation objects.
     * Loop through the device operation list and find the receiver 
     * object by link name. 
     *
     * @param linkName the link name to identify the receiver.
     *
     * @return AmqpsMessage if the receiver found the received 
     *         message, otherwise null.
     */
    AmqpsMessage getMessageFromReceiverLink(String linkName) throws IllegalArgumentException, IOException
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

    /**
     * Find the link by link name in the managed device operations. 
     *
     * @param linkName the name to find.
     *
     * @return Boolean true if found, false otherwise.
     */
    boolean isLinkFound(String linkName)
    {
        Boolean isLinkFound = false;

        if (this.isAuthenticationOpened())
        {
            for (int i = 0; i < this.amqpsDeviceSessionList.size(); i++)
            {
                // Codes_SRS_AMQPSESSIONMANAGER_12_038: [The function shall call all device session's isLinkFound, and if any of them true return true otherwise return false.]
                isLinkFound = this.amqpsDeviceSessionList.get(i).isLinkFound(linkName);
                if (isLinkFound == true)
                {
                    break;
                }
            }
        }
        else
        {
            // Codes_SRS_AMQPSESSIONMANAGER_12_037: [The function shall return with the authentication isLinkFound's return value if the authentication is not open.]
            isLinkFound = this.amqpsDeviceAuthentication.isLinkFound(linkName);
        }

        return isLinkFound;
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
    AmqpsConvertToProtonReturnValue convertToProton(com.microsoft.azure.sdk.iot.device.Message message) throws IOException
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
     */
    AmqpsConvertFromProtonReturnValue convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig) throws IOException
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

    /**
     * Shut down the CBS authentication sender thread
     */
    private void shutDownScheduler()
    {
        if (this.taskSchedulerCBSSend != null)
        {
            this.taskSchedulerCBSSend.shutdown(); // Disable new tasks from being submitted
            try
            {
                // Wait a while for existing tasks to terminate
                if (!this.taskSchedulerCBSSend.awaitTermination(60, TimeUnit.SECONDS))
                {
                    this.taskSchedulerCBSSend.shutdownNow(); // Cancel currently executing tasks
                    // Wait a while for tasks to respond to being cancelled
                    if (!this.taskSchedulerCBSSend.awaitTermination(60, TimeUnit.SECONDS))
                    {
                        System.err.println("taskSchedulerTokenRenewal did not terminate correctly");
                    }
                }
            }
            catch (InterruptedException ie)
            {
                // (Re-)Cancel if current thread also interrupted
                this.taskSchedulerCBSSend.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }
        }
    }
}
