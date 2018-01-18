package com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.amqp;

import com.microsoft.azure.sdk.iot.deps.transport.amqp.*;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.SDKUtils;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ResponseCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceConnectionException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceTransportException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.ContractState;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.ResponseData;
import com.microsoft.azure.sdk.iot.deps.util.ObjectLock;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class ProvisioningAmqpOperations extends AmqpDeviceOperations implements AmqpListener
{
    private static final String AMQP_ADDRESS_FMT = "/%s/registrations/%s";
    private static final String AMQP_REGISTER_DEVICE = "iotdps-register";
    private static final String AMQP_OPERATION_STATUS = "iotdps-get-operationstatus";
    private static final String AMQP_OP_TYPE_PROPERTY = "iotdps-operation-type";

    private static final String AMQP_OPERATION_ID = "iotdps-operation-id";
    private static final String API_VERSION_KEY = "com.microsoft:api-version";
    private static final String CLIENT_VERSION_IDENTIFIER_KEY = "com.microsoft:client-version";

    private static final int MAX_WAIT_TO_SEND_MSG = 1*60*1000; // 1 minute timeout
    private static final long MAX_WAIT_TO_OPEN_AMQP_CONNECTION = 1*60*1000; //1 minute timeout

    private AmqpsConnection amqpConnection;
    private final Queue<AmqpMessage> receivedMessages = new LinkedBlockingQueue<>();
    private final ObjectLock receiveLock = new ObjectLock();

    private String idScope;
    private String hostName;

    /**
     * Constructor for ProvisioningAmqpOperation that handle the AMQP transport for provisioning
     * @param idScope The Scope ID associated with this provisioning client
     * @param hostName The Provisioning Endpoint
     * @throws ProvisioningDeviceClientException Exception thrown if parameter is not provided
     */
    public ProvisioningAmqpOperations(String idScope, String hostName) throws ProvisioningDeviceClientException
    {
        // SRS_ProvisioningAmqpOperations_07_002: [The constructor shall throw ProvisioningDeviceClientException if either scopeId and hostName are null or empty.]
        if ((idScope == null) || (idScope.isEmpty()))
        {
            throw new ProvisioningDeviceClientException("The idScope cannot be null or empty.");
        }

        if ((hostName == null) || (hostName.isEmpty()))
        {
            throw new ProvisioningDeviceClientException("The hostName cannot be null or empty.");
        }

        //SRS_ProvisioningAmqpOperations_07_001: [The constructor shall save the scopeId and hostname.]
        this.idScope = idScope;
        this.hostName = hostName;
    }

    private synchronized void sendAmqpMessage(String msgType, String operationId) throws ProvisioningDeviceClientException
    {
        try
        {
            AmqpMessage outgoingMessage = new AmqpMessage();

            Map<String, Object> userProperties = new HashMap<>();
            userProperties.put(AMQP_OP_TYPE_PROPERTY, msgType);

            if (operationId != null && !operationId.isEmpty())
            {
                userProperties.put(AMQP_OPERATION_ID, operationId);
            }
            outgoingMessage.setApplicationProperty(userProperties);

            this.amqpConnection.sendAmqpMessage(outgoingMessage);
        }
        catch (Exception e)
        {
            throw new ProvisioningDeviceTransportException("Failure sending AMQP message", e);
        }
    }

    private void retrieveAmqpMessage(ResponseCallback responseCallback, Object callbackContext) throws ProvisioningDeviceClientException
    {
        if (this.receivedMessages.size() > 0)
        {
            AmqpMessage message = this.receivedMessages.remove();
            byte[] msgData = message.getAmqpBody();
            if (msgData != null)
            {
                responseCallback.run(new ResponseData(msgData, ContractState.DPS_REGISTRATION_RECEIVED, 0), callbackContext);
            }
        }
    }

    /**
     * Determines if the AMQP connect is up
     * @return boolean true is connected false otherwise
     */
    public boolean isAmqpConnected() throws ProvisioningDeviceClientException
    {
        boolean isConnected = false;
        if (this.amqpConnection != null)
        {
            try
            {
                isConnected = this.amqpConnection.isConnected();
            }
            catch (Exception e)
            {
                throw new ProvisioningDeviceConnectionException(e);
            }
        }
        return isConnected;
    }

    /**
     * Opens the Amqp connection
     * @param registrationId The specified registration id for the connection
     * @param sslContext The SSLContext that will get used for this connection
     * @param saslHandler custom handler for sasl logic. May be null if no sasl frames are expected
     * @throws ProvisioningDeviceConnectionException if connection could not succeed for any reason.
     */
    public void open(String registrationId, SSLContext sslContext, SaslHandler saslHandler, boolean useWebSockets) throws ProvisioningDeviceConnectionException
    {
        // SRS_ProvisioningAmqpOperations_07_003: [If amqpConnection is not null and is connected, open shall do nothing .]
        try
        {
            if (this.amqpConnection == null || !this.amqpConnection.isConnected())
            {
                // SRS_ProvisioningAmqpOperations_07_004: [open shall throw ProvisioningDeviceClientException if either registrationId or sslContext are null or empty.]
                if (registrationId == null || registrationId.isEmpty())
                {
                    throw new ProvisioningDeviceConnectionException(new IllegalArgumentException("registration Id cannot be null or empty"));
                }
                if (sslContext == null)
                {
                    throw new ProvisioningDeviceConnectionException(new IllegalArgumentException("sslContext cannot be null"));
                }


                addAmqpLinkProperty(API_VERSION_KEY, SDKUtils.getServiceApiVersion());
                addAmqpLinkProperty(CLIENT_VERSION_IDENTIFIER_KEY, SDKUtils.getUserAgentString());

                // SRS_ProvisioningAmqpOperations_07_005: [This method shall construct the Link Address with /<scopeId>/registrations/<registrationId>.]
                this.amqpLinkAddress = String.format(AMQP_ADDRESS_FMT, this.idScope, registrationId);

                this.amqpConnection = new AmqpsConnection(this.hostName, this, sslContext, saslHandler, useWebSockets);

                this.amqpConnection.setListener(this);

                this.amqpConnection.openAmqpAsync();
            }
        }
        catch (Exception ex)
        {
            // SRS_ProvisioningAmqpOperations_07_006: [This method shall connect to the amqp connection and throw ProvisioningDeviceConnectionException on error.]
            throw new ProvisioningDeviceConnectionException("Failure opening amqp connection", ex);
        }
    }

    /**
     * Closes the AMQP connection
     * @throws IOException if connection could not be closed.
     */
    public void close() throws IOException
    {
        // SRS_ProvisioningAmqpOperations_07_007: [If amqpConnection is null, this method shall do nothing.]
        if (this.amqpConnection != null)
        {
            // SRS_ProvisioningAmqpOperations_07_008: [This method shall call close on amqpConnection.]
            this.amqpConnection.close();
        }
    }

    /**
     * Sends the Status message to the Amqp Endpoint
     * @param operationId The operation ID of this call?
     * @param responseCallback Callback that gets initiated when the function call is complete
     * @param callbackContext Callback context for the response call.
     * @throws ProvisioningDeviceClientException If sending status is unsuccessful for any reason.
     */
    public void sendStatusMessage(String operationId, ResponseCallback responseCallback, Object callbackContext) throws ProvisioningDeviceClientException
    {
        // SRS_ProvisioningAmqpOperations_07_015: [sendStatusMessage shall throw ProvisioningDeviceClientException if either operationId or responseCallback are null or empty.]
        if (operationId == null || operationId.isEmpty())
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("operationId cannot be null or empty"));
        }
        if (responseCallback == null)
        {
            throw new ProvisioningDeviceClientException("responseCallback cannot be null");
        }

        // SRS_ProvisioningAmqpOperations_07_016: [This method shall send the Operation Status AMQP Provisioning message.]
        this.sendAmqpMessage(AMQP_OPERATION_STATUS, operationId);

        try
        {
            // SRS_ProvisioningAmqpOperations_07_017: [This method shall wait for the response of this message for MAX_WAIT_TO_SEND_MSG and call the responseCallback with the reply.]
            synchronized (this.receiveLock)
            {
                this.receiveLock.waitLock(MAX_WAIT_TO_SEND_MSG);
            }
            this.retrieveAmqpMessage(responseCallback, callbackContext);
        }
        catch (InterruptedException e)
        {
            // SRS_ProvisioningAmqpOperations_07_018: [This method shall throw ProvisioningDeviceClientException if any failure is encountered.]
            throw new ProvisioningDeviceClientException("Provisioning service failed to reply is alloted time.");
        }
    }

    /**
     * Sends the Registration message to the Amqp Endpoint
     * @param responseCallback Callback that gets initiated when the function call is complete
     * @param callbackContext Callback context for the response call.
     * @throws ProvisioningDeviceClientException If sending Register Message is unsuccessful for any reason.
     */
    public void sendRegisterMessage(ResponseCallback responseCallback, Object callbackContext) throws ProvisioningDeviceClientException
    {
        // SRS_ProvisioningAmqpOperations_07_009: [sendRegisterMessage shall throw ProvisioningDeviceClientException if either responseCallback is null.]
        if (responseCallback == null)
        {
            throw new ProvisioningDeviceClientException("responseCallback cannot be null");
        }

        //wait for AMQP connection to be opened
        long millisecondsElapsed = 0;
        long waitStartTime = System.currentTimeMillis();
        try
        {
            while (!this.amqpConnection.isConnected() && millisecondsElapsed < MAX_WAIT_TO_OPEN_AMQP_CONNECTION)
            {
                Thread.sleep(1000);
                millisecondsElapsed = System.currentTimeMillis() - waitStartTime;
            }
        }
        catch (Exception e)
        {
            throw new ProvisioningDeviceClientException("Provisioning device client encountered an exception while waiting for amqps connection to open.", e);
        }

        if (millisecondsElapsed >= MAX_WAIT_TO_OPEN_AMQP_CONNECTION)
        {
            throw new ProvisioningDeviceClientException("Provisioning device client timed out while waiting for amqps connection to open.");
        }

        // SRS_ProvisioningAmqpOperations_07_010: [This method shall send the Register AMQP Provisioning message.]
        this.sendAmqpMessage(AMQP_REGISTER_DEVICE, null);

        try
        {
            // SRS_ProvisioningAmqpOperations_07_011: [This method shall wait for the response of this message for MAX_WAIT_TO_SEND_MSG and call the responseCallback with the reply.]
            synchronized (this.receiveLock)
            {
                this.receiveLock.waitLock(MAX_WAIT_TO_SEND_MSG);
            }
            this.retrieveAmqpMessage(responseCallback, callbackContext);
        }
        catch (InterruptedException e)
        {
            // SRS_ProvisioningAmqpOperations_07_012: [This method shall throw ProvisioningDeviceClientException if any failure is encountered.]
            throw new ProvisioningDeviceClientException("Provisioning service failed to reply is allotted time.");
        }
    }

    /**
     * connectionEstablished Unused
     */
    public void connectionEstablished()
    {
    }

    /**
     * connectionLost Unused
     */
    public void connectionLost()
    {
    }

    /**
     * messageSent Unused
     */
    public void messageSent()
    {
    }

    /**
     * Function that gets called when amqp gets a message from the amqp endpoint
     * @param message Message received during transmission.
     */
    public void messageReceived(AmqpMessage message)
    {
        // SRS_ProvisioningAmqpOperations_07_013: [This method shall add the message to a message queue.]
        this.receivedMessages.add(message);
        synchronized (this.receiveLock)
        {
            // SRS_ProvisioningAmqpOperations_07_014: [This method shall then Notify the receiveLock.]
            this.receiveLock.notifyLock();
        }
    }
}
