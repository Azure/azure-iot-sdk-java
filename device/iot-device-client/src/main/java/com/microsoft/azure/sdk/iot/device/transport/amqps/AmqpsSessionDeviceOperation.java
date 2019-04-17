package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import org.apache.qpid.proton.engine.*;

import java.nio.BufferOverflowException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.microsoft.azure.sdk.iot.device.MessageType.*;

public class AmqpsSessionDeviceOperation
{
    private final DeviceClientConfig deviceClientConfig;

    private AmqpsDeviceAuthenticationState amqpsAuthenticatorState = AmqpsDeviceAuthenticationState.UNKNOWN;
    private final AmqpsDeviceAuthentication amqpsDeviceAuthentication;

    private Map<MessageType, AmqpsDeviceOperations> amqpsDeviceOperationsMap = new HashMap<MessageType, AmqpsDeviceOperations>();

    private static long nextTag = 0;

    private Integer openLock = new Integer(1);

    private long tokenRenewalPeriodInMilliseconds = 4000; //4 seconds;

    private ScheduledExecutorService taskSchedulerTokenRenewal;
    private AmqpsDeviceAuthenticationCBSTokenRenewalTask tokenRenewalTask = null;

    private static final int MAX_WAIT_TO_AUTHENTICATE = 10*1000;
    private static final double PERCENTAGE_FACTOR = 0.75;
    private static final int MILLISECECONDS_PER_SECOND = 1000;

    private final CountDownLatch authenticationLatch = new CountDownLatch(1);

    private List<UUID> cbsCorrelationIdList = Collections.synchronizedList(new ArrayList<UUID>());

    private CustomLogger logger;

    /**
     * Create logical device entity to handle all operation.
     *
     * @param deviceClientConfig the configuration of teh device.
     * @param amqpsDeviceAuthentication the authentication object associated with the device.
     * @throws IllegalArgumentException if deviceClientConfig or amqpsDeviceAuthentication is null
     */
    public AmqpsSessionDeviceOperation(final DeviceClientConfig deviceClientConfig, AmqpsDeviceAuthentication amqpsDeviceAuthentication) throws IllegalArgumentException
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

        this.logger = new CustomLogger(this.getClass());

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
     * In the CBS case start opening the authentication links
     * and send authentication messages.
     *
     * @throws TransportException if authentication message reply takes too long.
     */
    public void authenticate() throws TransportException
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

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

            // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_005: [The function shall set the authentication state to not authenticated if the authentication type is CBS.]
            this.amqpsAuthenticatorState = AmqpsDeviceAuthenticationState.AUTHENTICATING;
            try
            {
                // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_062: [The function shall start the authentication process and start the lock wait if the authentication type is CBS.]
                this.authenticationLatch.await(MAX_WAIT_TO_AUTHENTICATE, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e)
            {
                // Codes_SRS_AMQPSESSIONDEVICEOPERATION_34_063: [If an InterruptedException is encountered while waiting for authentication to finish, this function shall throw a TransportException.]
                cbsCorrelationIdList.remove(correlationId);
                throw new TransportException("Waited too long for the authentication message reply.");
            }

        }

        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Start the token renewal process using CBS authentication.
     *
     * @throws TransportException throw if Proton operation throws.
     */
    public void renewToken() throws TransportException
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        if ((this.deviceClientConfig.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN) &&
                (this.amqpsAuthenticatorState == AmqpsDeviceAuthenticationState.AUTHENTICATED))
        {
            if (this.deviceClientConfig.getSasTokenAuthentication().isRenewalNecessary())
            {
                logger.LogDebug("Sas token cannot be renewed automatically, so amqp connection will be unauthorized soon, method: %s", logger.getMethodName());
            }
            else
            {
                // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_051: [The function start the authentication with the new token.]
                authenticate();
            }
        }

        logger.LogDebug("Exited from method %s", logger.getMethodName());
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

        for (Map.Entry<MessageType, AmqpsDeviceOperations> entry : amqpsDeviceOperationsMap.entrySet())
        {
            // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_008: [The function shall return true if all operation links are opene, otherwise return false.]
            if (!entry.getValue().operationLinksOpened())
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
     * @throws TransportException throw if Proton operation throws.
     */
    boolean openLinks(Session session, MessageType msgType) throws TransportException
    {
        boolean waitForRemoteOpenCallback = false;

        logger.LogDebug("Entered in method %s", logger.getMethodName());

        // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_042: [The function shall do nothing if the session parameter is null.]
        if (session != null)
        {
            if (this.amqpsAuthenticatorState == AmqpsDeviceAuthenticationState.AUTHENTICATED)
            {
                if (this.amqpsDeviceOperationsMap.get(msgType) == null)
                {
                    switch(msgType)
                    {
                        case DEVICE_METHODS:
                            this.amqpsDeviceOperationsMap.put(DEVICE_METHODS, new AmqpsDeviceMethods(this.deviceClientConfig));
                            break;
                        case DEVICE_TWIN:
                            this.amqpsDeviceOperationsMap.put(DEVICE_TWIN, new AmqpsDeviceTwin(this.deviceClientConfig));
                            break;
                        default:
                            break;
                    }
                }

                synchronized (this.openLock)
                {
                    // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_009: [The function shall call openLinks on all device operations if the authentication state is authenticated.]
                    waitForRemoteOpenCallback = this.amqpsDeviceOperationsMap.get(msgType).openLinks(session);
                }
            }
        }

        logger.LogDebug("Exited from method %s", logger.getMethodName());
        return waitForRemoteOpenCallback;
    }

    /**
     * Delegate the close link call to device operation objects.
     */
    void closeLinks()
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        Iterator iterator = amqpsDeviceOperationsMap.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry<MessageType, AmqpsDeviceOperations> pair = (Map.Entry<MessageType, AmqpsDeviceOperations>)iterator.next();
            pair.getValue().closeLinks();
            //iterator.remove();
        }

        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Delegate the link initialization call to device operation objects.
     *
     * @param link the link ti initialize.
     * @throws TransportException throw if Proton operation throws.
     * @throws IllegalArgumentException throw if the link parameter is null.
     */
    void initLink(Link link) throws TransportException, IllegalArgumentException
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

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

        logger.LogDebug("Exited from method %s", logger.getMethodName());
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
                            // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_053: [The function shall call authenticationMessageReceived with the correlation ID on the authentication object and if it returns true set the authentication state to authenticated.]
                            this.amqpsAuthenticatorState = AmqpsDeviceAuthenticationState.AUTHENTICATED;
                            // Codes_SRS_AMQPSESSIONDEVICEOPERATION_12_054: [The function shall call notify the lock if after receiving the message and the authentication is in authenticating state.]
                            this.authenticationLatch.countDown();

                            uuidFound = correlationId;
                            break;
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
            for (Map.Entry<MessageType, AmqpsDeviceOperations> entry : amqpsDeviceOperationsMap.entrySet())
            {
                amqpsMessage = entry.getValue().getMessageFromReceiverLink(linkName);
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
            for (Map.Entry<MessageType, AmqpsDeviceOperations> entry : amqpsDeviceOperationsMap.entrySet())
            {
                if (entry.getValue().isLinkFound(linkName))
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

    /**
     * Restart the renewal thread
     *
     * @return true is the renewal is successful
     */
    private void scheduleRenewalThread()
    {
        long renewalPeriod = calculateRenewalPeriodInMilliseconds(this.deviceClientConfig.getSasTokenAuthentication().getTokenValidSecs());
        if (renewalPeriod > 0)
        {
            this.tokenRenewalPeriodInMilliseconds = renewalPeriod;


            shutDownScheduler();
            this.taskSchedulerTokenRenewal = Executors.newScheduledThreadPool(1);
            this.taskSchedulerTokenRenewal.scheduleAtFixedRate(this.tokenRenewalTask, 0, this.tokenRenewalPeriodInMilliseconds, TimeUnit.MILLISECONDS);
        }
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
                if (!taskSchedulerTokenRenewal.awaitTermination(10, TimeUnit.SECONDS))
                {
                    taskSchedulerTokenRenewal.shutdownNow(); // Cancel currently executing tasks

                    // Wait a while for tasks to respond to being cancelled
                    if (!taskSchedulerTokenRenewal.awaitTermination(10, TimeUnit.SECONDS))
                    {
                        System.err.println("taskSchedulerTokenRenewal did not terminate correctly");
                    }
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
     * @throws IllegalArgumentException if validInSecs is less than 0
     */
    private long calculateRenewalPeriodInMilliseconds(long validInSecs) throws IllegalArgumentException
    {
        if (validInSecs < 0)
        {
            throw new IllegalArgumentException("validInSecs cannot be less than 0.");
        }

        return (long)(validInSecs * PERCENTAGE_FACTOR * MILLISECECONDS_PER_SECOND);
    }
}
