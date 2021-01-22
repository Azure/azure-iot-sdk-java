// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.mqtt;

import com.microsoft.azure.sdk.iot.deps.util.ObjectLock;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.ProvisioningDeviceClientConfig;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.SDKUtils;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ProvisioningDeviceClientContract;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.*;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ResponseCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.DeviceRegistrationParser;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.RequestData;
import com.microsoft.azure.sdk.iot.deps.transport.mqtt.*;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.ContractState;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.ResponseData;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class ContractAPIMqtt extends ProvisioningDeviceClientContract implements MqttListener
{
    private static final String MQTT_USERNAME_FMT = "%s/registrations/%s/api-version=%s&ClientVersion=%s";
    private static final String MQTT_PROVISIONING_TOPIC_NAME = "$dps/registrations/res/#";
    private static final String MQTT_REGISTER_MESSAGE_FMT = "$dps/registrations/PUT/iotdps-register/?$rid=%d";
    private static final String MQTT_STATUS_MESSAGE_FMT = "$dps/registrations/GET/iotdps-get-operationstatus/?$rid=%d&operationId=%s";

    private static final int MAX_WAIT_TO_SEND_MSG = 60 * 1000; // 1 minute timeout

    private MqttConnection mqttConnection;
    private final String hostname;
    private final String idScope;

    private int packetId;
    private final boolean useWebSockets;

    private final ObjectLock receiveLock = new ObjectLock();
    private final Queue<MqttMessage> receivedMessages = new LinkedBlockingQueue<>();

    private Throwable lostConnection = null;

    /**
     * This constructor creates an instance of Mqtt class and initializes member variables
     * @param provisioningDeviceClientConfig Config used for provisioning Cannot be {@code null}.
     * @throws ProvisioningDeviceClientException is thrown when any of the input parameters are invalid
     */
    public ContractAPIMqtt(ProvisioningDeviceClientConfig provisioningDeviceClientConfig) throws ProvisioningDeviceClientException
    {
        // SRS_ContractAPIMqtt_07_024: [ If provisioningDeviceClientConfig is null, this method shall throw ProvisioningDeviceClientException. ]
        if (provisioningDeviceClientConfig == null)
        {
            throw new ProvisioningDeviceClientException("ProvisioningDeviceClientConfig cannot be NULL.");
        }
        String idScope = provisioningDeviceClientConfig.getIdScope();
        if ((idScope == null) || (idScope.isEmpty()))
        {
            throw new ProvisioningDeviceClientException("The idScope cannot be null or empty.");
        }
        String hostName = provisioningDeviceClientConfig.getProvisioningServiceGlobalEndpoint();
        if ((hostName == null) || (hostName.isEmpty()))
        {
            throw new ProvisioningDeviceClientException("The hostName cannot be null or empty.");
        }

        this.useWebSockets = provisioningDeviceClientConfig.getUseWebSockets();
        this.hostname = hostName;
        this.idScope = idScope;
        this.packetId = 1;
    }

    private void executeProvisioningMessage(String topic, byte[] body, ResponseCallback responseCallback, Object callbackContext) throws IOException, ProvisioningDeviceClientException
    {
        // Send the message
        this.mqttConnection.publishMessage(topic, MqttQos.DELIVER_AT_MOST_ONCE, body);

        try
        {
            // SRS_ProvisioningAmqpOperations_07_011: [This method shall wait for the response of this message for MAX_WAIT_TO_SEND_MSG and call the responseCallback with the reply.]
            synchronized (this.receiveLock)
            {
                this.receiveLock.waitLock(MAX_WAIT_TO_SEND_MSG);
            }
            if (this.receivedMessages.size() > 0)
            {

                MqttMessage message = this.receivedMessages.remove();
                responseCallback.run(new ResponseData(message.getPayload(), ContractState.DPS_REGISTRATION_RECEIVED, 0), callbackContext);
            }
            else
            {
                throw new ProvisioningDeviceClientException("Invalid message received.");
            }
        }
        catch (InterruptedException e)
        {
            // SRS_ProvisioningAmqpOperations_07_012: [This method shall throw ProvisioningDeviceClientException if any failure is encountered.]
            throw new ProvisioningDeviceClientException("Provisioning service failed to reply is allotted time.");
        }

    }

    private void processRetryAfterValue(String mqttTopic)
    {
        if (mqttTopic != null && !mqttTopic.isEmpty())
        {
            // Split the string
            for (String topicPart: mqttTopic.split("&"))
            {
                int retryPosition = topicPart.indexOf(RETRY_AFTER);
                // if retry-after is in here we need parse out the value
                if (retryPosition > -1)
                {
                    String targetRetryAfter;
                    // Make sure there's no data after the retry after
                    int topicSeparator = topicPart.indexOf(";");
                    if (topicSeparator > -1)
                    {
                        // substring the value adding 1 for the = and only go to the ;
                        targetRetryAfter = topicPart.substring(RETRY_AFTER.length()+1, topicSeparator);
                    }
                    else
                    {
                        // substring the value adding 1 for the = and only go to the ;
                        targetRetryAfter = topicPart.substring(RETRY_AFTER.length()+1);
                    }
                    setRetrieveRetryAfterValue(targetRetryAfter);
                    break;
                }
            }
        }
    }

    /**
     * Indicates need to open MQTT connection
     * @param requestData Data used for the connection initialization
     * @throws ProvisioningDeviceConnectionException is thrown when any of the input parameters are invalid
     */
    @Override
    public synchronized void open(RequestData requestData) throws ProvisioningDeviceConnectionException
    {
        if (this.mqttConnection != null && !this.mqttConnection.isMqttConnected())
        {
            throw new ProvisioningDeviceConnectionException("Open called on an already open connection");
        }
        if (requestData == null)
        {
            throw new ProvisioningDeviceConnectionException(new IllegalArgumentException("RequestData cannot be null"));
        }
        String registrationId = requestData.getRegistrationId();
        if (registrationId == null || registrationId.isEmpty())
        {
            throw new ProvisioningDeviceConnectionException(new IllegalArgumentException("registration Id cannot be null or empty"));
        }
        SSLContext sslContext = requestData.getSslContext();
        if (sslContext == null)
        {
            throw new ProvisioningDeviceConnectionException(new IllegalArgumentException("sslContext cannot be null"));
        }

        if (requestData.isX509() || (requestData.getSasToken() != null && !requestData.getSasToken().isEmpty()))
        {
            try
            {
                String username = String.format(MQTT_USERNAME_FMT, this.idScope, registrationId, SDKUtils.getServiceApiVersion(), SDKUtils.getServiceApiVersion());
                this.mqttConnection = new MqttConnection(this.hostname, registrationId, username, requestData.getSasToken(), sslContext, this, this.useWebSockets);
                this.mqttConnection.connect();

                this.mqttConnection.subscribe(MQTT_PROVISIONING_TOPIC_NAME, MqttQos.DELIVER_AT_LEAST_ONCE);
            }
            catch (IOException ex)
            {
                this.mqttConnection = null;
                throw new ProvisioningDeviceConnectionException("Exception opening connection", ex);
            }
        }
    }

    /**
     * Indicates to close the connection
     * @throws ProvisioningDeviceConnectionException thrown if a failure in disconnect
     */
    public synchronized void close() throws ProvisioningDeviceConnectionException
    {
        try
        {
            if (this.mqttConnection != null && this.mqttConnection.isMqttConnected() )
            {
                this.mqttConnection.disconnect();
            }
        }
        catch (IOException ex)
        {
            throw new ProvisioningDeviceConnectionException("Exception closing mqtt", ex);
        }
    }

    /**
     * Requests hub to authenticate this connection and start the registration process over MQTT
     * @param requestData A non {@code null} value with all the required request data
     * @param responseCallback A non {@code null} value for the callback
     * @param callbackContext An object for context. Can be {@code null}
     * @throws ProvisioningDeviceClientException If any of the parameters are invalid ({@code null} or empty)
     * @throws ProvisioningDeviceTransportException If any of the API calls to transport fail
     * @throws ProvisioningDeviceHubException If hub responds back with an invalid status
     */
    public synchronized void authenticateWithProvisioningService(RequestData requestData, ResponseCallback responseCallback, Object callbackContext) throws ProvisioningDeviceClientException
    {
        //SRS_ContractAPIAmqp_07_003: [If responseCallback is null, this method shall throw ProvisioningDeviceClientException.]
        if (responseCallback == null)
        {
            throw new ProvisioningDeviceClientException("responseCallback cannot be null");
        }

        if (!requestData.isX509())
        {
            String sasToken = requestData.getSasToken();
            if (sasToken == null || sasToken.isEmpty())
            {
                //SRS_ContractAPIAmqp_34_021: [If the requestData is not x509, but the provided requestData does not contain a sas token, this function shall
                // throw a ProvisioningDeviceConnectionException.]
                throw new ProvisioningDeviceConnectionException(new IllegalArgumentException("RequestData's sas token cannot be null or empty"));
            }

            //SRS_ContractAPIAmqp_34_020: [If the requestData is not x509, this function shall assume SymmetricKey authentication, and shall open the connection with
            // the provided request data containing a sas token.]
            open(requestData);
        }

        if (this.mqttConnection == null || !this.mqttConnection.isMqttConnected())
        {
            throw new ProvisioningDeviceConnectionException("Mqtt is not connected");
        }

        try
        {
            String topic = String.format(MQTT_REGISTER_MESSAGE_FMT, this.packetId++);

            //SRS_ContractAPIMqtt_07_026: [ This method shall build the required Json input using parser. ]
            byte[] payload = new DeviceRegistrationParser(requestData.getRegistrationId(), requestData.getPayload()).toJson().getBytes();

            // SRS_ContractAPIMqtt_07_005: [This method shall send an MQTT message with the property of iotdps-register.]
            this.executeProvisioningMessage(topic, payload, responseCallback, callbackContext);
        }
        catch (IOException ex)
        {
            throw new ProvisioningDeviceConnectionException("Exception publishing mqtt message", ex);
        }
    }

    /**
     * Gets the registration status over MQTT
     * @param requestData A non {@code null} value with all the request data
     * @param responseCallback A non {@code null} value for the callback
     * @param callbackContext An object for context. Can be {@code null}
     * @throws ProvisioningDeviceClientException If any of the parameters are invalid ({@code null} or empty)
     * @throws ProvisioningDeviceTransportException If any of the API calls to transport fail
     * @throws ProvisioningDeviceHubException If hub responds back with an invalid status
     */
    public synchronized void getRegistrationStatus(RequestData requestData, ResponseCallback responseCallback, Object callbackContext) throws ProvisioningDeviceClientException
    {
        // SRS_ContractAPIAmqp_07_009: [If requestData is null this method shall throw ProvisioningDeviceClientException.]
        if (requestData == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("requestData cannot be null"));
        }
        // SRS_ContractAPIAmqp_07_010: [If requestData.getOperationId() is null or empty, this method shall throw ProvisioningDeviceClientException.]
        String operationId = requestData.getOperationId();
        if (operationId == null || operationId.isEmpty())
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("operationId cannot be null or empty"));
        }
        // SRS_ContractAPIAmqp_07_011: [If responseCallback is null, this method shall throw ProvisioningDeviceClientException.]
        if (responseCallback == null)
        {
            throw new ProvisioningDeviceClientException("responseCallback cannot be null");
        }

        if (this.mqttConnection == null || !this.mqttConnection.isMqttConnected())
        {
            throw new ProvisioningDeviceConnectionException("Mqtt is not connected");
        }

        if (lostConnection != null)
        {
            throw new ProvisioningDeviceConnectionException("Mqtt is not connected", lostConnection);
        }

        try
        {
            String topic = String.format(MQTT_STATUS_MESSAGE_FMT, this.packetId++, operationId);

            // SRS_ContractAPIAmqp_07_005: [This method shall send an AMQP message with the property of iotdps-register.]
            this.executeProvisioningMessage(topic, null, responseCallback, callbackContext);
        }
        catch (IOException ex)
        {
            throw new ProvisioningDeviceConnectionException("Exception publishing mqtt message", ex);
        }
    }

    /**
     * Requests hub to provide a device key to begin authentication over MQTT (Only for TPM)
     * @param requestData the request data to be used while requesting nonce for TPM
     * @param responseCallback A non {@code null} value for the callback
     * @param authorizationCallbackContext An object for context. Can be {@code null}
     * @throws ProvisioningDeviceClientException If any of the parameters are invalid ({@code null} or empty)
     * @throws ProvisioningDeviceTransportException If any of the API calls to transport fail
     * @throws ProvisioningDeviceHubException If hub responds back with an invalid status
     */
    public synchronized void requestNonceForTPM(RequestData requestData, ResponseCallback responseCallback, Object authorizationCallbackContext) throws ProvisioningDeviceClientException
    {
        if (requestData == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("requestData cannot be null"));
        }
        if (responseCallback == null)
        {
            throw new ProvisioningDeviceClientException("responseCallback cannot be null");
        }
        String registrationId = requestData.getRegistrationId();
        if (registrationId == null || registrationId.isEmpty())
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("registration Id cannot be null or empty"));
        }
        byte[] endorsementKey = requestData.getEndorsementKey();
        if (endorsementKey == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("Endorsement key cannot be null"));
        }
        byte[] storageRootKey = requestData.getStorageRootKey();
        if (storageRootKey == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("Storage root key cannot be null"));
        }
        if (requestData.getSslContext() == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("sslContext cannot be null"));
        }

        throw new ProvisioningDeviceClientException(new UnsupportedOperationException());
    }

    @Override
    public void messageReceived(MqttMessage message)
    {
        processRetryAfterValue(message.getTopic());

        // SRS_ProvisioningAmqpOperations_07_013: [This method shall add the message to a message queue.]
        this.receivedMessages.add(message);
        synchronized (this.receiveLock)
        {
            // SRS_ProvisioningAmqpOperations_07_014: [This method shall then Notify the receiveLock.]
            this.receiveLock.notifyLock();
        }
    }

    @Override
    public void connectionLost(Throwable throwable)
    {
        lostConnection = throwable;
    }
}
