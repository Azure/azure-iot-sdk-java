package com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.amqp;

import com.microsoft.azure.sdk.iot.deps.transport.amqp.*;
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

public class provisioningAmqpOperations extends AmqpDeviceOperations implements AmqpListener
{
    private static final String AMQP_ADDRESS_FMT = "/%s/registrations/%s";
    private static final String AMQP_REGISTER_DEVICE = "iotdps-register";
    private static final String AMQP_OPERATION_STATUS = "iotdps-get-operationstatus";
    private static final String AMQP_OP_TYPE_PROPERTY = "iotdps-operation-type";

    private static final String AMQP_OPERATION_ID = "iotdps-operation-id";

    private static final int MAX_WAIT_TO_SEND_MSG = 1*60*1000; // 1 minute timeout

    private AmqpsConnection amqpConnection;
    private final Queue<AmqpMessage> receivedMessages = new LinkedBlockingQueue<>();
    private final ObjectLock receiveLock = new ObjectLock();

    private String scopeId;
    private String hostName;

    /**
     * Constructor for ProvisioningAmqpOperation that handle the AMQP transport for provisioning
     * @param scopeId The Scope ID associated with this provisioning client
     * @param hostName The Provisioning Endpoint
     * @throws ProvisioningDeviceClientException
     */
    public provisioningAmqpOperations(String scopeId, String hostName) throws ProvisioningDeviceClientException
    {
        if ((scopeId == null) || (scopeId.isEmpty()))
        {
            throw new ProvisioningDeviceClientException("The scopeId cannot be null or empty.");
        }

        if ((hostName == null) || (hostName.isEmpty()))
        {
            throw new ProvisioningDeviceClientException("The hostName cannot be null or empty.");
        }

        this.scopeId = scopeId;
        this.hostName = hostName;
    }

    private synchronized void sendAmqpMessage(String msgType, String operationId) throws ProvisioningDeviceClientException
    {
        try
        {
            AmqpMessage outgoingMessage = new AmqpMessage();

            Map<String, String> userProperties = new HashMap<>();
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
     * @return boolean
     */
    public boolean isAmqpConnected()
    {
        boolean isConnected = false;
        if (this.amqpConnection != null)
        {
            isConnected = this.amqpConnection.isConnected();
        }
        return isConnected;
    }

    /**
     * Opens the Amqp connection
     * @param registrationId The specified registration id for the connection
     * @param sslContext The SSLContext that will get used for this connection
     * @param isX509Cert Indicates if using x509 or TPM
     * @throws ProvisioningDeviceConnectionException
     */
    public void open(String registrationId, SSLContext sslContext, boolean isX509Cert) throws ProvisioningDeviceConnectionException
    {
        if (this.amqpConnection == null || !this.amqpConnection.isConnected())
        {
            if (registrationId == null || registrationId.isEmpty())
            {
                throw new ProvisioningDeviceConnectionException(new IllegalArgumentException("registration Id cannot be null or empty"));
            }
            if (sslContext == null)
            {
                throw new ProvisioningDeviceConnectionException(new IllegalArgumentException("sslContext cannot be null"));
            }

            try
            {
                this.amqpLinkAddress = String.format(AMQP_ADDRESS_FMT, this.scopeId, registrationId);

                this.amqpConnection = new AmqpsConnection(this.hostName, this, sslContext, !isX509Cert, false);

                this.amqpConnection.setListener(this);

                this.amqpConnection.open();
            }
            catch (Exception ex)
            {
                throw new ProvisioningDeviceConnectionException("Failure opening amqp connection", ex);
            }
        }
    }

    /**
     * Closes the AMQP connection
     */
    public void close() throws IOException
    {
        if (this.amqpConnection != null)
        {
            // SRS_ContractAPIAmqp_07_023: [This method will close the amqpConnection connection.]
            this.amqpConnection.close();
        }
    }

    /**
     * Sends the Status message to the Amqp Endpoint
     * @param operationId The operation ID of this call?
     * @param responseCallback Callback that gets initiated when the function call is complete
     * @param callbackContext Callback context for the response call.
     * @throws ProvisioningDeviceClientException
     */
    public void sendStatusMessage(String operationId, ResponseCallback responseCallback, Object callbackContext) throws ProvisioningDeviceClientException
    {
        if (operationId == null || operationId.isEmpty())
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("operationId cannot be null or empty"));
        }
        if (responseCallback == null)
        {
            throw new ProvisioningDeviceClientException("responseCallback cannot be null");
        }

        this.sendAmqpMessage(AMQP_OPERATION_STATUS, operationId);

        try
        {
            synchronized (this.receiveLock)
            {
                this.receiveLock.waitLock(MAX_WAIT_TO_SEND_MSG);
            }
            this.retrieveAmqpMessage(responseCallback, callbackContext);
        }
        catch (InterruptedException e)
        {
            throw new ProvisioningDeviceClientException("Provisioning service failed to reply is alloted time.");
        }
    }

    /**
     * Sends the Registration message to the Amqp Endpoint
     * @param responseCallback Callback that gets initiated when the function call is complete
     * @param callbackContext Callback context for the response call.
     * @throws ProvisioningDeviceClientException
     */
    public void sendRegisterMessage(ResponseCallback responseCallback, Object callbackContext) throws ProvisioningDeviceClientException
    {
        if (responseCallback == null)
        {
            throw new ProvisioningDeviceClientException("responseCallback cannot be null");
        }

        this.sendAmqpMessage(AMQP_REGISTER_DEVICE, null);

        try
        {
            synchronized (this.receiveLock)
            {
                this.receiveLock.waitLock(MAX_WAIT_TO_SEND_MSG);
            }
            this.retrieveAmqpMessage(responseCallback, callbackContext);
        }
        catch (InterruptedException e)
        {
            throw new ProvisioningDeviceClientException("Provisioning service failed to reply is alloted time.");
        }
    }

    /**
     * ConnectionEstablished Unused
     */
    public void ConnectionEstablished()
    {
    }

    /**
     * ConnectionLost Unused
     */
    public void ConnectionLost()
    {
    }

    /**
     * MessageSent Unused
     */
    public void MessageSent()
    {
    }

    /**
     * Function that gets called when amqp gets a message from the amqp endpoint
     * @param message
     */
    public void MessageReceived(AmqpMessage message)
    {
        this.receivedMessages.add(message);
        synchronized (this.receiveLock)
        {
            this.receiveLock.notifyLock();
        }
    }
}
