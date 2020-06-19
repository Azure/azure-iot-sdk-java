// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.*;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

import static com.microsoft.azure.sdk.iot.device.MessageType.DEVICE_METHODS;
import static com.microsoft.azure.sdk.iot.device.MessageType.DEVICE_TWIN;

@Slf4j
public class MqttIotHubConnection implements IotHubTransportConnection, MqttMessageListener
{
    /** The MQTT connection lock. */
    private final Object MQTT_CONNECTION_LOCK = new Object();

    private final DeviceClientConfig config;
    private IotHubConnectionStatus state = IotHubConnectionStatus.DISCONNECTED;

    private String iotHubUserName;
    private String iotHubUserPassword;
    private MqttConnection mqttConnection;

    //string constants
    private static final String WS_SSL_PREFIX = "wss://";

    private static final String WEBSOCKET_RAW_PATH = "/$iothub/websocket";
    private static final String NO_CLIENT_CERT_QUERY_STRING = "?iothub-no-client-cert=true";

    private static final String SSL_PREFIX = "ssl://";
    private static final String SSL_PORT_SUFFIX = ":8883";

    private static final String ModelIdParam = "digital-twin-model-id";

    private String connectionId;
    private String webSocketQueryString;

    private IotHubListener listener;

    //Messaging clients
    private MqttMessaging deviceMessaging;
    private MqttDeviceTwin deviceTwin;
    private MqttDeviceMethod deviceMethod;

    private Map<IotHubTransportMessage, Integer> receivedMessagesToAcknowledge = new ConcurrentHashMap<>();
    private Map<Integer, Message> unacknowledgedSentMessages = new ConcurrentHashMap<>();

    /**
     * Constructs an instance from the given {@link DeviceClientConfig}
     * object.
     *
     * @param config the client configuration.
     */
    public MqttIotHubConnection(DeviceClientConfig config) throws IllegalArgumentException
    {
        synchronized (MQTT_CONNECTION_LOCK)
        {
            // Codes_SRS_MQTTIOTHUBCONNECTION_15_003: [The constructor shall throw a new IllegalArgumentException
            // if any of the parameters of the configuration is null or empty.]
            if (config == null)
            {
                throw new IllegalArgumentException("The DeviceClientConfig cannot be null.");
            }
            if (config.getIotHubHostname() == null || config.getIotHubHostname().length() == 0)
            {
                throw new IllegalArgumentException("hostName cannot be null or empty.");
            }
            if (config.getDeviceId() == null || config.getDeviceId().length() == 0)
            {
                throw new IllegalArgumentException("deviceID cannot be null or empty.");
            }
            if (config.getIotHubName() == null || config.getIotHubName().length() == 0)
            {
                throw new IllegalArgumentException("hubName cannot be null or empty.");
            }

            // Codes_SRS_MQTTIOTHUBCONNECTION_15_001: [The constructor shall save the configuration.]
            this.config = config;
            this.deviceMessaging = null;
            this.deviceMethod = null;
            this.deviceTwin = null;
        }
    }

    /**
     * Establishes a connection for the device and IoT Hub given in the client
     * configuration. If the connection is already open, the function shall do
     * nothing.
     *
     * @throws TransportException if a connection could not to be established.
     */
    public void open(Queue<DeviceClientConfig> deviceClientConfigs, ScheduledExecutorService scheduledExecutorService) throws TransportException
    {
        connectionId = UUID.randomUUID().toString();
        if (deviceClientConfigs.size() > 1)
        {
            //Codes_SRS_MQTTIOTHUBCONNECTION_34_022: [If the list of device client configuration objects is larger than 1, this function shall throw an UnsupportedOperationException.]
            throw new UnsupportedOperationException("Mqtt does not support Multiplexing");
        }

        synchronized (MQTT_CONNECTION_LOCK)
        {
            //Codes_SRS_MQTTIOTHUBCONNECTION_15_006: [If the MQTT connection is already open,
            // the function shall do nothing.]
            if (this.state == IotHubConnectionStatus.CONNECTED)
            {
                return;
            }

            this.log.debug("Opening MQTT connection...");

            // Codes_SRS_MQTTIOTHUBCONNECTION_15_004: [The function shall establish an MQTT connection
            // with an IoT Hub using the provided host name, user name, device ID, and sas token.]
            try
            {
                SSLContext sslContext = this.config.getAuthenticationProvider().getSSLContext();
                if (this.config.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
                {
                    this.log.trace("MQTT connection will use sas token based auth");
                    this.iotHubUserPassword = this.config.getSasTokenAuthentication().getRenewedSasToken(false, false);
                    this.webSocketQueryString = NO_CLIENT_CERT_QUERY_STRING;
                }
                else if (this.config.getAuthenticationType() == DeviceClientConfig.AuthType.X509_CERTIFICATE)
                {
                    this.log.trace("MQTT connection will use X509 certificate based auth");
                    this.iotHubUserPassword = null;
                }

                //URLEncoder follows HTML spec for encoding urls, which includes substituting space characters with '+'
                // We want "%20" for spaces, not '+', however, so replace them manually after utf-8 encoding
                String userAgentString = this.config.getProductInfo().getUserAgentString();
                String clientUserAgentIdentifier = "DeviceClientType=" + URLEncoder.encode(userAgentString, "UTF-8").replaceAll("\\+", "%20");
                String clientId = this.config.getDeviceId();

                String moduleId = this.config.getModuleId();
                if (moduleId != null && !moduleId.isEmpty())
                {
                    //Codes_SRS_MQTTIOTHUBCONNECTION_34_065: [If the config contains a module id, this function shall create the clientId for the connection to be <deviceId>/<moduleId>.]
                    clientId += "/" + moduleId;
                }

                String serviceParams;
                String modelId = this.config.getModelId();
                if(modelId == null || modelId.isEmpty())
                {
                    serviceParams = TransportUtils.IOTHUB_API_VERSION;
                }
                else
                {
                    serviceParams = TransportUtils.IOTHUB_API_VERSION_PREVIEW + "&" + ModelIdParam + "=" + modelId;
                }

                this.iotHubUserName = this.config.getIotHubHostname() + "/" + clientId + "/?api-version=" + serviceParams + "&" + clientUserAgentIdentifier;

                String host = this.config.getGatewayHostname();
                if (host == null || host.isEmpty())
                {
                    host = this.config.getIotHubHostname();
                }
                if (this.config.isUseWebsocket())
                {
                    //Codes_SRS_MQTTIOTHUBCONNECTION_25_018: [The function shall establish an MQTT WS connection with a server uri as wss://<hostName>/$iothub/websocket?iothub-no-client-cert=true if websocket was enabled.]
                    final String wsServerUri;
                    if (this.webSocketQueryString == null)
                    {
                        wsServerUri = WS_SSL_PREFIX + host + WEBSOCKET_RAW_PATH;
                    }
                    else
                    {
                        wsServerUri = WS_SSL_PREFIX + host + WEBSOCKET_RAW_PATH + this.webSocketQueryString;
                    }

                    mqttConnection = new MqttConnection(wsServerUri,
                            clientId, this.iotHubUserName, this.iotHubUserPassword, sslContext, this.config.getProxySettings());
                }
                else
                {
                    //Codes_SRS_MQTTIOTHUBCONNECTION_25_019: [The function shall establish an MQTT connection with a server uri as ssl://<hostName>:8883 if websocket was not enabled.]
                    final String serverUri = SSL_PREFIX + host + SSL_PORT_SUFFIX;
                    mqttConnection = new MqttConnection(serverUri,
                            clientId, this.iotHubUserName, this.iotHubUserPassword, sslContext, null);
                }

                //Codes_SRS_MQTTIOTHUBCONNECTION_34_030: [This function shall instantiate this object's MqttMessaging object with this object as the listener.]
                this.deviceMessaging = new MqttMessaging(mqttConnection, this.config.getDeviceId(), this.listener, this, this.connectionId, this.config.getModuleId(), this.config.getGatewayHostname() != null && !this.config.getGatewayHostname().isEmpty(), unacknowledgedSentMessages);
                this.mqttConnection.setMqttCallback(this.deviceMessaging);
                this.deviceMethod = new MqttDeviceMethod(mqttConnection, this.connectionId, unacknowledgedSentMessages);
                this.deviceTwin = new MqttDeviceTwin(mqttConnection, this.connectionId, unacknowledgedSentMessages);

                this.deviceMessaging.start();
                this.state = IotHubConnectionStatus.CONNECTED;

                this.log.debug("MQTT connection opened successfully");

                //Codes_SRS_MQTTIOTHUBCONNECTION_34_065: [If the connection opens successfully, this function shall notify the listener that connection was established.]
                this.listener.onConnectionEstablished(this.connectionId);
            }
            catch (IOException e)
            {
                this.log.error("Exception encountered while opening MQTT connection; closing connection", e);
                this.state = IotHubConnectionStatus.DISCONNECTED;
                // Codes_SRS_MQTTIOTHUBCONNECTION_15_005: [If an MQTT connection is unable to be established
                // for any reason, the function shall throw a TransportException.]
                if (this.deviceMethod != null)
                {
                    this.deviceMethod.stop();
                }
                if (this.deviceTwin != null )
                {
                    this.deviceTwin.stop();
                }
                if (this.deviceMessaging != null)
                {
                    this.deviceMessaging.stop();
                }
                throw new TransportException(e);
            }
        }
    }

    /**
     * Closes the connection. After the connection is closed, it is no longer usable.
     * If the connection is already closed, the function shall do nothing.
     */
    public void close() throws TransportException
    {
        // Codes_SRS_MQTTIOTHUBCONNECTION_15_007: [If the MQTT session is closed, the function shall do nothing.]
        if (this.state == IotHubConnectionStatus.DISCONNECTED)
        {
            return;
        }

        this.log.debug("Closing MQTT connection");

        // Codes_SRS_MQTTIOTHUBCONNECTION_15_006: [The function shall close the MQTT connection.]
        try
        {
            if (this.deviceMethod != null)
            {
                this.deviceMethod.stop();
                this.deviceMethod = null;
            }

            if (this.deviceTwin != null)
            {
                this.deviceTwin.stop();
                this.deviceTwin = null;
            }

            if (this.deviceMessaging != null)
            {
                this.deviceMessaging.stop();
                this.deviceMessaging = null;
            }

            this.state = IotHubConnectionStatus.DISCONNECTED;
            this.log.debug("Successfully closed MQTT connection");
        }
        catch (TransportException e)
        {
            //Codes_SRS_MQTTIOTHUBCONNECTION_34_021: [If a TransportException is encountered while closing the three clients, this function shall set this object's state to closed and then rethrow the exception.]
            this.state = IotHubConnectionStatus.DISCONNECTED;
            this.log.error("Exception encountered while closing MQTT connection, connection state is unknown", e);
            throw e;
        }
    }

    /**
     * Receives a message, if one exists.
     *
     * @return the message received, or null if none exists.
     *
     * @throws TransportException if the connection state is currently closed.
     */
    private IotHubTransportMessage receiveMessage() throws TransportException
    {
        // Codes_SRS_MQTTIOTHUBCONNECTION_15_014: [The function shall attempt to consume a message
        // from various messaging clients.]
        IotHubTransportMessage message = this.deviceMethod.receive();
        if (message != null)
        {
            this.log.trace("Received MQTT device method message ({})", message);
            return message;
        }

        message = deviceTwin.receive();
        if (message != null)
        {
            this.log.trace("Received MQTT device twin message ({})", message);
            return message;
        }

        message = deviceMessaging.receive();
        if (message != null)
        {
            this.log.trace("Received MQTT device messaging message ({})", message);
            return message;
        }

        return null;
    }

    @Override
    public void setListener(IotHubListener listener) throws IllegalArgumentException
    {
        if (listener == null)
        {
            //Codes_SRS_MQTTIOTHUBCONNECTION_34_049: [If the provided listener object is null, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("listener cannot be null");
        }

        //Codes_SRS_MQTTIOTHUBCONNECTION_34_050: [This function shall save the provided listener object.]
        this.listener = listener;
    }

    /**
     * Sends an event message.
     *
     * @param message the event message.
     *
     * @return the status code from sending the event message.
     *
     * @throws TransportException if the MqttIotHubConnection is not open
     */
    @Override
    public IotHubStatusCode sendMessage(Message message) throws TransportException
    {
        synchronized (MQTT_CONNECTION_LOCK)
        {
            // Codes_SRS_MQTTIOTHUBCONNECTION_15_010: [If the message is null or empty,
            // the function shall return status code BAD_FORMAT.]
            if (message == null || message.getBytes() == null ||
                    (
                            (message.getMessageType() != DEVICE_TWIN
                                    && message.getMessageType() != DEVICE_METHODS)
                                    && message.getBytes().length == 0))
            {
                return IotHubStatusCode.BAD_FORMAT;
            }

            // Codes_SRS_MQTTIOTHUBCONNECTION_15_013: [If the MQTT connection is closed, the function shall throw an IllegalStateException.]
            if (this.state == IotHubConnectionStatus.DISCONNECTED)
            {
                throw new IllegalStateException("Cannot send event using a closed MQTT connection");
            }

            // Codes_SRS_MQTTIOTHUBCONNECTION_15_008: [The function shall send an event message
            // to the IoT Hub given in the configuration.]
            // Codes_SRS_MQTTIOTHUBCONNECTION_15_011: [If the message was successfully received by the service,
            // the function shall return status code OK_EMPTY.]
            IotHubStatusCode result = IotHubStatusCode.OK_EMPTY;

            // Codes_SRS_MQTTIOTHUBCONNECTION_15_009: [The function shall send the message payload.]
            if (message.getMessageType() == DEVICE_METHODS)
            {
                this.deviceMethod.start();
                this.log.trace("Sending MQTT device method message ({})", message);
                this.deviceMethod.send((IotHubTransportMessage) message);
            }
            else if (message.getMessageType() == DEVICE_TWIN)
            {
                this.deviceTwin.start();
                this.log.trace("Sending MQTT device twin message ({})", message);
                this.deviceTwin.send((IotHubTransportMessage) message);
            }
            else
            {
                this.log.trace("Sending MQTT device telemetry message ({})", message);
                this.deviceMessaging.send(message);
            }

            return result;
        }
    }

    /**
     * Sends an ACK to the service for the provided message
     * @param message the message to acknowledge to the service
     * @param result Ignored. The only ack that can be sent in MQTT is COMPLETE
     * @return true if the ACK was sent successfully and false otherwise
     * @throws TransportException if the ACK could not be sent successfully
     */
    @Override
    public boolean sendMessageResult(IotHubTransportMessage message, IotHubMessageResult result) throws TransportException
    {
        if (message == null || result == null)
        {
            //Codes_SRS_MQTTIOTHUBCONNECTION_34_057: [If the provided message or result is null, this function shall throw a TransportException.]
            throw new TransportException(new IllegalArgumentException("message and result must be non-null"));
        }

        int messageId;
        this.log.trace("Checking if MQTT layer can acknowledge the received message ({})", message);
        if (receivedMessagesToAcknowledge.containsKey(message))
        {
            //Codes_SRS_MQTTIOTHUBCONNECTION_34_052: [If this object has received the provided message from the service, this function shall retrieve the Mqtt messageId for that message.]
            messageId = receivedMessagesToAcknowledge.get(message);
        }
        else
        {
            TransportException e = new TransportException(new IllegalArgumentException("Provided message cannot be acknowledged because it was already acknowledged or was never received from service"));
            this.log.error("Mqtt layer could not acknowledge received message because it has no mapping to an outstanding mqtt message id ({})", message, e);
            //Codes_SRS_MQTTIOTHUBCONNECTION_34_051: [If this object has not received the provided message from the service, this function shall throw a TransportException.]
            throw e;
        }

        boolean ackSent;
        this.log.trace("Sending MQTT ACK for a received message ({})", message);
        if (message.getMessageType() == DEVICE_METHODS)
        {
            //Codes_SRS_MQTTIOTHUBCONNECTION_34_053: [If the provided message has message type DEVICE_METHODS, this function shall invoke the methods client to send the ack and return the result.]
            this.deviceMethod.start();
            ackSent = this.deviceMethod.sendMessageAcknowledgement(messageId);
        }
        else if (message.getMessageType() == DEVICE_TWIN)
        {
            //Codes_SRS_MQTTIOTHUBCONNECTION_34_054: [If the provided message has message type DEVICE_TWIN, this function shall invoke the twin client to send the ack and return the result.]
            this.deviceTwin.start();
            ackSent = this.deviceTwin.sendMessageAcknowledgement(messageId);
        }
        else
        {
            //Codes_SRS_MQTTIOTHUBCONNECTION_34_055: [If the provided message has message type other than DEVICE_METHODS and DEVICE_TWIN, this function shall invoke the telemetry client to send the ack and return the result.]
            ackSent = this.deviceMessaging.sendMessageAcknowledgement(messageId);
        }

        if (ackSent)
        {
            //Codes_SRS_MQTTIOTHUBCONNECTION_34_056: [If the ack was sent successfully, this function shall remove the provided message from the saved map of messages to acknowledge.]
            this.log.trace("MQTT ACK was sent for a received message so it has been removed from the messages to acknowledge list ({})", message);
            this.receivedMessagesToAcknowledge.remove(message);
        }

        return ackSent;
    }

    @Override
    public String getConnectionId()
    {
        //Codes_SRS_MQTTIOTHUBCONNECTION_34_064: [This function shall return the saved connectionId.]
        return this.connectionId;
    }

    @Override
    public void onMessageArrived(int messageId)
    {
        IotHubTransportMessage transportMessage = null;
        try
        {
            //Codes_SRS_MQTTIOTHUBCONNECTION_34_058: [This function shall attempt to receive a message.]
            transportMessage = this.receiveMessage();
        }
        catch (TransportException e)
        {
            this.listener.onMessageReceived(null, new TransportException("Failed to receive message from service", e));
            this.log.error("Encountered exception while receiving message over MQTT", e);
        }

        if (transportMessage == null)
        {
            //Ack is not sent to service for this message because we cannot interpret the message. Service will likely re-send
            this.listener.onMessageReceived(null, new TransportException("Message sent from service could not be parsed"));
            this.log.warn("Received message that could not be parsed. That message has been ignored.");
        }
        else
        {
            //Codes_SRS_MQTTIOTHUBCONNECTION_34_059: [If a transport message is successfully received, this function shall save it in this object's map of messages to be acknowledged along with the provided messageId.]
            this.log.trace("MQTT received message so it has been added to the messages to acknowledge list ({})", transportMessage);
            this.receivedMessagesToAcknowledge.put(transportMessage, messageId);

            switch (transportMessage.getMessageType())
            {
                case DEVICE_TWIN:
                    //Codes_SRS_MQTTIOTHUBCONNECTION_34_060: [If a transport message is successfully received, and the message has a type of DEVICE_TWIN, this function shall set the callback and callback context of this object from the saved values in config for methods.]
                    transportMessage.setMessageCallback(this.config.getDeviceTwinMessageCallback());
                    transportMessage.setMessageCallbackContext(this.config.getDeviceTwinMessageContext());
                    break;
                case DEVICE_METHODS:
                    //Codes_SRS_MQTTIOTHUBCONNECTION_34_061: [If a transport message is successfully received, and the message has a type of DEVICE_METHODS, this function shall set the callback and callback context of this object from the saved values in config for twin.]
                    transportMessage.setMessageCallback(this.config.getDeviceMethodsMessageCallback());
                    transportMessage.setMessageCallbackContext(this.config.getDeviceMethodsMessageContext());
                    break;
                case DEVICE_TELEMETRY:
                    //Codes_SRS_MQTTIOTHUBCONNECTION_34_062: [If a transport message is successfully received, and the message has a type of DEVICE_TELEMETRY, this function shall set the callback and callback context of this object from the saved values in config for telemetry.]
                    transportMessage.setMessageCallback(this.config.getDeviceTelemetryMessageCallback(transportMessage.getInputName()));
                    transportMessage.setMessageCallbackContext(this.config.getDeviceTelemetryMessageContext(transportMessage.getInputName()));
                    break;
                case UNKNOWN:
                case CBS_AUTHENTICATION:
                default:
                    //do nothing
            }

            //Codes_SRS_MQTTIOTHUBCONNECTION_34_063: [If a transport message is successfully received, this function shall notify its listener that a message was received and provide the received message.]
            this.listener.onMessageReceived(transportMessage, null);
        }
    }
}
