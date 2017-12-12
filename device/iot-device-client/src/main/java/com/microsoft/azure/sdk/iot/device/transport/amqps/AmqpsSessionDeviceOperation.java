package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.*;
import org.apache.qpid.proton.engine.*;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AmqpsSessionDeviceOperation
{
    private final DeviceClientConfig deviceClientConfig;

    private AmqpsDeviceAuthenticationState amqpsAuthenticatorState = AmqpsDeviceAuthenticationState.UNKNOWN;
    private final AmqpsDeviceAuthentication amqpsDeviceAuthentication;

    private ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<>();;

    private long nextTag = 0;

    private Integer openLock = new Integer(1);

    private long tokenRenewalPeriodInMillisecSecs = 4000; //45*60*100;

    private ScheduledExecutorService taskSchedulerTokenRenewal;
    private AmqpsDeviceAuthenticationCBSTokenRenewalTask tokenRenewalTask = null;

    private static final int MAX_WAIT_TO_AUTHENTICATE = 10*1000;
    private final ObjectLock authenticationLock = new ObjectLock();

    private List<UUID> cbsCorrelationIdList = Collections.synchronizedList(new ArrayList<UUID>());

    /**
     * Create logical device entity to handle all operation.
     *
     * @param deviceClientConfig the configuration of teh device.
     * @param amqpsDeviceAuthentication the authentication object associated with the device.
     */
    public AmqpsSessionDeviceOperation(final DeviceClientConfig deviceClientConfig, AmqpsDeviceAuthentication amqpsDeviceAuthentication)
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

        // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_003: [The constructor shall create AmqpsDeviceTelemetry, AmqpsDeviceMethods and AmqpsDeviceTwin and add them to the device operations list. ]
        this.amqpsDeviceOperationsList.add(new AmqpsDeviceTelemetry(this.deviceClientConfig));
        this.amqpsDeviceOperationsList.add(new AmqpsDeviceMethods(this.deviceClientConfig));
        this.amqpsDeviceOperationsList.add(new AmqpsDeviceTwin(this.deviceClientConfig));

        if (this.deviceClientConfig.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
        {
            // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_004: [The constructor shall set the authentication state to not authenticated if the authentication type is CBS.]
            this.amqpsAuthenticatorState = AmqpsDeviceAuthenticationState.NOT_AUTHENTICATED;

            // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_044: [The constructor shall calculate the token renewal period as the 75% of the expiration period.]
            this.tokenRenewalTask = new AmqpsDeviceAuthenticationCBSTokenRenewalTask(this);

            // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_048: [The constructor saves the calculated renewal period if it is greater than zero.]
            // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_045: [The constructor shall create AmqpsDeviceAuthenticationCBSTokenRenewalTask if the authentication type is CBS.]
            // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_046: [The constructor shall create and start a scheduler with the calculated renewal period for AmqpsDeviceAuthenticationCBSTokenRenewalTask if the authentication type is CBS.]
            this.scheduleRenewalThread();
        }
        else
        {
            // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_047[The constructor shall set the authentication state to authenticated if the authentication type is not CBS.]
            this.amqpsAuthenticatorState = AmqpsDeviceAuthenticationState.AUTHENTICATED;
        }
    }

    /**
     * Release all resources and close all links.
     */
    public void close()
    {
        this.shutDownScheduler();
        this.closeLinks();

        if (this.deviceClientConfig.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
        {
            this.amqpsAuthenticatorState = AmqpsDeviceAuthenticationState.NOT_AUTHENTICATED;
        }
    }

    /**
     * Start the authentication process.
     * In SAS case it is nothing to do.
     * In the CBS case start openeing the authentication links
     * and send authentication messages.
     *
     * @throws IOException throw if Proton operation throws.
     */
    public void authenticate() throws IOException
    {
        // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_006: [The function shall start the authentication if the authentication type is CBS.]
        if (this.deviceClientConfig.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
        {
            // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_060: [The function shall create a new UUID and add it to the correlationIdList if the authentication type is CBS.]
            UUID correlationId = UUID.randomUUID();
            synchronized (this.cbsCorrelationIdList)
            {
                // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_061: [The function shall use the correlationID to call authenticate on the authentication object if the authentication type is CBS.]
                cbsCorrelationIdList.add(correlationId);
            }

            this.amqpsDeviceAuthentication.authenticate(this.deviceClientConfig, correlationId);
            synchronized (this.authenticationLock)
            {
                // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_005: [The function shall set the authentication state to not authenticated if the authentication type is CBS.]
                this.amqpsAuthenticatorState = AmqpsDeviceAuthenticationState.AUTHENTICATING;
                try
                {
                    // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_062: [The function shall start the authentication process and start the lock wait if the authentication type is CBS.]
                    this.authenticationLock.waitLock(MAX_WAIT_TO_AUTHENTICATE);
                } catch (InterruptedException e)
                {
                    cbsCorrelationIdList.remove(correlationId);

                    // Codes_SRS_AMQPSESSIONMANAGER_12_017: [The function shall throw IOException if the lock throws.]
                    throw new IOException("Waited too long for the authentication message reply.");
                }
            }
        }
    }

    /**
     * Start the token renewal process using CBS authentication.
     *
     * @throws IOException throw if Proton operation throws.
     */
    public void renewToken() throws IOException
    {
        if ((this.deviceClientConfig.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN) &&
                (this.amqpsAuthenticatorState == AmqpsDeviceAuthenticationState.AUTHENTICATED))
        {
            // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_050: [The function shall renew the sas token if the authentication type is CBS and the authentication state is authenticated.]
            this.deviceClientConfig.getSasTokenAuthentication().getRenewedSasToken();

            // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_052: [The function shall restart the scheduler with the calculated renewal period if the authentication type is CBS.]
            if (scheduleRenewalThread())
            {
                // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_051: [The function start the authentication with the new token.]
                authenticate();
            }
        }
    }

    /**
     * Return the current authentication state.
     *
     * @return the current state.
     */
    public AmqpsDeviceAuthenticationState getAmqpsAuthenticatorState()
    {
        // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_007: [The function shall return the current authentication state.]
        return this.amqpsAuthenticatorState;
    }

    /**
     * Verify if all operation links are open.
     *
     * @return true if all links are open, false otherwise.
     */
    public Boolean operationLinksOpened()
    {
        Boolean allLinksOpened = true;

        for (int i = 0; i < this.amqpsDeviceOperationsList.size(); i++)
        {
            // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_008: [The function shall return true if all operation links are opene, otherwise return false.]
            if (!this.amqpsDeviceOperationsList.get(i).operationLinksOpened())
            {
                allLinksOpened = false;
                break;
            }
        }

        return allLinksOpened;
    }

    /**
     *
     * @param session the Proton session to open the links on.
     * @throws IOException throw if Proton operation throws.
     * @throws IllegalArgumentException throw if session parameter is null.
     */
    void openLinks(Session session) throws IOException, IllegalArgumentException
    {
        // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_042: [The function shall do nothing if the session parameter is null.]
        if (session != null)
        {
            if (this.amqpsAuthenticatorState == AmqpsDeviceAuthenticationState.AUTHENTICATED)
            {
                for (int i = 0; i < this.amqpsDeviceOperationsList.size(); i++)
                {
                    synchronized (openLock)
                    {
                        // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_009: [The function shall call openLinks on all device operations if the authentication state is authenticated.]
                        this.amqpsDeviceOperationsList.get(i).openLinks(session);
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
        for (int i = 0; i < amqpsDeviceOperationsList.size(); i++)
        {
            // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_010: [The function shall call closeLinks on all device operations.]
            amqpsDeviceOperationsList.get(i).closeLinks();
        }
    }

    /**
     * Delegate the link initialization call to device operation objects.
     *
     * @param link the link ti initialize.
     * @throws IOException throw if Proton operation throws.
     * @throws IllegalArgumentException throw if the link parameter is null.
     */
    void initLink(Link link) throws IOException, IllegalArgumentException
    {
        // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_043: [The function shall do nothing if the link parameter is null.]
        if (link != null)
        {
            if (this.amqpsAuthenticatorState == AmqpsDeviceAuthenticationState.AUTHENTICATED)
            {
                for (int i = 0; i < this.amqpsDeviceOperationsList.size(); i++)
                {
                    // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_011: [The function shall call initLink on all device operations.**]**]
                    this.amqpsDeviceOperationsList.get(i).initLink(link);
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
     * @param iotHubConnectionString the deviceconnection string to
     *                               find the sender.
     *
     * @return Integer
     */
    Integer sendMessage(org.apache.qpid.proton.message.Message message, MessageType messageType, IotHubConnectionString iotHubConnectionString) throws IOException
    {
        // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_012: [The function shall return -1 if the state is not authenticated.]
        if (this.amqpsAuthenticatorState == AmqpsDeviceAuthenticationState.AUTHENTICATED)
        {
            // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_013: [The function shall return -1 if the deviceId int he connection string is not equeal to the deviceId in the config.]
            if (this.deviceClientConfig.getDeviceId() == iotHubConnectionString.getDeviceId())
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
                byte[] deliveryTag = String.valueOf(this.nextTag++).getBytes();

                // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_018: [The function shall call sendMessageAndGetDeliveryHash on all device operation objects.]
                // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_019: [The function shall return the delivery hash.]
                return this.sendMessageAndGetDeliveryHash(messageType, msgData, 0, length, deliveryTag);
            }
            else
            {
                return -1;
            }
        }
        else
        {
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
     *
     * @return Integer
     */
    private Integer sendMessageAndGetDeliveryHash(MessageType messageType, byte[] msgData, int offset, int length, byte[] deliveryTag) throws IllegalStateException, IllegalArgumentException, IOException
    {
        Integer deliveryHash = -1;

        for (int i = 0; i < this.amqpsDeviceOperationsList.size(); i++)
        {
            AmqpsSendReturnValue amqpsSendReturnValue = null;
            amqpsSendReturnValue = this.amqpsDeviceOperationsList.get(i).sendMessageAndGetDeliveryHash(messageType, msgData, 0, length, deliveryTag);
            if (amqpsSendReturnValue.isDeliverySuccessful())
            {
                return amqpsSendReturnValue.getDeliveryHash();
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
    synchronized AmqpsMessage getMessageFromReceiverLink(String linkName) throws IllegalArgumentException, IOException
    {
        AmqpsMessage amqpsMessage = null;

        if (this.amqpsAuthenticatorState == AmqpsDeviceAuthenticationState.AUTHENTICATING)
        {
            // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_023: [If the state is authenticating the function shall call getMessageFromReceiverLink on the authentication object.]
            amqpsMessage = this.amqpsDeviceAuthentication.getMessageFromReceiverLink(linkName);

            if (amqpsMessage != null)
            {
                synchronized (this.cbsCorrelationIdList)
                {
                    UUID uuidFound = null;
                    // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_055: [The function shall find the correlation ID in the correlationIdlist.]
                    for (UUID correlationId : this.cbsCorrelationIdList)
                    {
                        if (this.amqpsDeviceAuthentication.authenticationMessageReceived(amqpsMessage, correlationId))
                        {
                            synchronized (this.authenticationLock)
                            {
                                // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_053: [The function shall call authenticationMessageReceived with the correlation ID on the authentication object and if it returns true set the authentication state to authenticated.]
                                this.amqpsAuthenticatorState = AmqpsDeviceAuthenticationState.AUTHENTICATED;
                                // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_054: [The function shall call notify the lock if after receiving the message and the authentication is in authenticating state.]
                                this.authenticationLock.notifyLock();

                                uuidFound = correlationId;
                                break;
                            }
                        }
                    }
                    // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_056: [The function shall remove the correlationId from the list if it is found.]
                    if (uuidFound != null)
                    {
                        this.cbsCorrelationIdList.remove(uuidFound);
                    }
                }
                return amqpsMessage;
            }
        }
        else
        {
            // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_057: [If the state is other than authenticating the function shall try to read the message from the device operation objects.]
            for (int i = 0; i < this.amqpsDeviceOperationsList.size(); i++)
            {
                amqpsMessage = this.amqpsDeviceOperationsList.get(i).getMessageFromReceiverLink(linkName);
                if (amqpsMessage != null)
                {
                    break;
                }
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
    Boolean isLinkFound(String linkName)
    {
        // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_024: [The function shall return true if any of the operation's link name is a match and return false otherwise.]
        if (this.amqpsAuthenticatorState == AmqpsDeviceAuthenticationState.AUTHENTICATED)
        {
            for (int i = 0; i < this.amqpsDeviceOperationsList.size(); i++)
            {
                if (this.amqpsDeviceOperationsList.get(i).isLinkFound(linkName))
                {
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
     *
     * @return AmqpsConvertToProtonReturnValue the result of the 
     *         conversion containing the Proton message.
     */
    AmqpsConvertToProtonReturnValue convertToProton(Message message) throws IOException
    {
        AmqpsConvertToProtonReturnValue amqpsConvertToProtonReturnValue = null;

        if (this.amqpsDeviceOperationsList != null)
        {
            for (int i = 0; i < this.amqpsDeviceOperationsList.size(); i++)
            {
                // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_040: [The function shall call all device operation's convertToProton, and if any of them not null return with the value.]
                amqpsConvertToProtonReturnValue = this.amqpsDeviceOperationsList.get(i).convertToProton(message);
                if (amqpsConvertToProtonReturnValue != null)
                {
                    break;
                }
            }
        }

        return amqpsConvertToProtonReturnValue;
    }

    /**
     * Convert from IoTHub message to Proton using operation 
     * specific converter. 
     *
     * @param amqpsMessage the message to convert.
     * @param deviceClientConfig the device client configuration to 
     *                           identify the converter..
     *
     * @return AmqpsConvertToProtonReturnValue the result of the 
     *         conversion containing the Proton message.
     */
    AmqpsConvertFromProtonReturnValue convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig) throws IOException
    {
        AmqpsConvertFromProtonReturnValue amqpsHandleMessageReturnValue = null;

        if (this.amqpsDeviceOperationsList != null)
        {
            for (int i = 0; i < this.amqpsDeviceOperationsList.size(); i++)
            {
                // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_041: [The function shall call all device operation's convertFromProton, and if any of them not null return with the value.]
                amqpsHandleMessageReturnValue = this.amqpsDeviceOperationsList.get(i).convertFromProton(amqpsMessage, deviceClientConfig);
                if (amqpsHandleMessageReturnValue != null)
                {
                    break;
                }
            }
        }

        return amqpsHandleMessageReturnValue;
    }

    /**
     * Restart the renewal thread
     *
     * @return true is the renewal is successful
     */
    private Boolean scheduleRenewalThread()
    {
        long renewalPeriod = calculateRenewalTimeInMilliSecs(this.deviceClientConfig.getSasTokenAuthentication().getTokenValidSecs());
        if (renewalPeriod > 0)
        {
            shutDownScheduler();
            if (this.taskSchedulerTokenRenewal == null)
            {
                this.taskSchedulerTokenRenewal = Executors.newScheduledThreadPool(1);
            }

            this.tokenRenewalPeriodInMillisecSecs = renewalPeriod;
            this.taskSchedulerTokenRenewal.scheduleAtFixedRate(this.tokenRenewalTask, 0, tokenRenewalPeriodInMillisecSecs, TimeUnit.MILLISECONDS);

            return true;
        }
        return false;
    }

    /**
     * Shut down the renewal thread
     */
    private void shutDownScheduler()
    {
        if (this.taskSchedulerTokenRenewal  != null)
        {
            taskSchedulerTokenRenewal.shutdown(); // Disable new tasks from being submitted
            try
            {
                // Wait a while for existing tasks to terminate
                if (!taskSchedulerTokenRenewal.awaitTermination(60, TimeUnit.SECONDS))
                {
                    taskSchedulerTokenRenewal.shutdownNow(); // Cancel currently executing tasks
                    // Wait a while for tasks to respond to being cancelled
                    if (!taskSchedulerTokenRenewal.awaitTermination(60, TimeUnit.SECONDS)) System.err.println("taskSchedulerTokenRenewal did not terminate correctly");
                }
            }
            catch (InterruptedException ie)
            {
                // (Re-)Cancel if current thread also interrupted
                taskSchedulerTokenRenewal.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Calculate 75 percent of given time
     *
     * @param validInSecs the time
     * @return 75% of the given time
     */
    private long calculateRenewalTimeInMilliSecs(long validInSecs)
    {
        if (validInSecs <= 0)
        {
            throw new IllegalArgumentException("validInSecs cannot be null.");
        }

        return 3 * (validInSecs / 4) * 1000;
    }
}
