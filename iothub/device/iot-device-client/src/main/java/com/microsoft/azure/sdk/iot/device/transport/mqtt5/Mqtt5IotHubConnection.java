// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt5;

import com.google.gson.Gson;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.*;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.Proxy;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.microsoft.azure.sdk.iot.device.MessageType.DEVICE_METHODS;
import static com.microsoft.azure.sdk.iot.device.MessageType.DEVICE_TWIN;

@Slf4j
public class Mqtt5IotHubConnection implements IotHubTransportConnection, MqttCallback
{
    //string constants
    private static final String WS_SSL_PREFIX = "wss://";

    private static final String WEBSOCKET_RAW_PATH = "/$iothub/websocket";
    private static final String NO_CLIENT_CERT_QUERY_STRING = "?iothub-no-client-cert=true";

    private static final String SSL_PREFIX = "ssl://";
    private static final String SSL_PORT_SUFFIX = ":8883";

    private static final boolean SET_CLEAN_SESSION = false;

    private static final String MODEL_ID = "model-id";

    private String connectionId;
    private String webSocketQueryString;
    private final Object mqttConnectionStateLock = new Object(); // lock for preventing simultaneous open and close calls
    private final ClientConfiguration config;
    private IotHubConnectionStatus state = IotHubConnectionStatus.DISCONNECTED;
    private IotHubListener listener;

    private MqttAsyncClient mqttAsyncClient;

    private final Mqtt5 mqtt5Client;

    private final String e4kConnectionChangeTopic;

    private static final int QOS = 1;
    private static final int E4K_CONNECTION_CLOSE_MESSAGE_TIMEOUT_MILLISECONDS = 15 * 1000; // 15 seconds
    private static final int E4K_CONNECTION_OPEN_MESSAGE_TIMEOUT_MILLISECONDS = 15 * 1000; // 15 seconds
    private static final String MQTT_VERSION_STRING = "5.0.0"; //TODO should this just be 5.0? Or just 5?

    private static final Gson GSON = new Gson();

    private final Map<IotHubTransportMessage, Integer> receivedMessagesToAcknowledge = new ConcurrentHashMap<>();

    public Mqtt5IotHubConnection(ClientConfiguration config) throws TransportException
    {
        if (config == null)
        {
            throw new IllegalArgumentException("The ClientConfiguration cannot be null.");
        }
        if (config.getIotHubHostname() == null || config.getIotHubHostname().length() == 0)
        {
            throw new IllegalArgumentException("hostName cannot be null or empty.");
        }
        if (config.getDeviceId() == null || config.getDeviceId().length() == 0)
        {
            throw new IllegalArgumentException("deviceID cannot be null or empty.");
        }

        this.config = config;



        if (this.config.getAuthenticationType() == ClientConfiguration.AuthType.SAS_TOKEN)
        {
            log.trace("MQTT connection will use sas token based auth");
            this.webSocketQueryString = NO_CLIENT_CERT_QUERY_STRING;
        }
        else if (this.config.getAuthenticationType() == ClientConfiguration.AuthType.X509_CERTIFICATE)
        {
            log.trace("MQTT connection will use X509 certificate based auth");
        }

        String host = this.config.getGatewayHostname();
        if (host == null || host.isEmpty())
        {
            host = this.config.getIotHubHostname();
        }

        String serverUri;
        if (this.config.isUsingWebsocket())
        {
            if (this.webSocketQueryString == null)
            {
                serverUri = WS_SSL_PREFIX + host + WEBSOCKET_RAW_PATH;
            }
            else
            {
                serverUri = WS_SSL_PREFIX + host + WEBSOCKET_RAW_PATH + this.webSocketQueryString;
            }
        }
        else
        {
            serverUri = SSL_PREFIX + host + SSL_PORT_SUFFIX;
        }

        String clientId = this.config.getDeviceId();
        if (this.config.getModuleId() != null && !this.config.getModuleId().isEmpty())
        {
            clientId += "/" + this.config.getModuleId();
        }

        this.e4kConnectionChangeTopic = "$iothub/clients/" + clientId + "/connection";

        Map<Integer, Message> unacknowledgedSentMessages = new ConcurrentHashMap<>();
        Queue<Pair<String, MqttMessage>> receivedMessages = new ConcurrentLinkedQueue<>();

        this.mqtt5Client = new Mqtt5(
            this.config.getDeviceId(),
            this.config.getModuleId(),
            this.config.getGatewayHostname() != null && !this.config.getGatewayHostname().isEmpty(),
            this.config.isConnectingToMqttGateway(),
            unacknowledgedSentMessages,
            receivedMessages);

        try
        {
            this.mqttAsyncClient = new MqttAsyncClient(serverUri, clientId, new MemoryPersistence());
        }
        catch (MqttException e)
        {
            throw Mqtt5ExceptionTranslator.convertToMqttException(e, "Failed to create mqtt client");
        }

        this.mqttAsyncClient.setManualAcks(true);
        this.mqttAsyncClient.setCallback(this);
    }

    /**
     * Establishes a connection for the device and IoT Hub given in the client
     * configuration. If the connection is already open, the function shall do
     * nothing.
     *
     * @throws TransportException if a connection could not to be established.
     */
    public void open() throws TransportException
    {
        synchronized (this.mqttConnectionStateLock)
        {
            this.connectionId = UUID.randomUUID().toString();

            if (this.state == IotHubConnectionStatus.CONNECTED)
            {
                return;
            }

            SSLContext sslContext;
            try
            {
                sslContext = this.config.getAuthenticationProvider().getSSLContext();
            }
            catch (IOException e)
            {
                throw new TransportException("Failed to get SSLContext", e);
            }

            MqttConnectionOptions connectOptions = new MqttConnectionOptions();
            connectOptions.setKeepAliveInterval(config.getKeepAliveInterval());
            connectOptions.setCleanStart(SET_CLEAN_SESSION);

            E4KConnectionMessagePayload willMessagePayload = E4KConnectionMessagePayload.builder()
                .connectionState(E4KConnectionState.Disconnected)
                .modelId(this.config.getModelId())
                .mqttVersion(MQTT_VERSION_STRING)
                .deviceClientType(this.config.getProductInfo().getUserAgentString())
                .build();
            byte[] serializedWillPayload = GSON.toJson(willMessagePayload).getBytes(StandardCharsets.UTF_8);
            connectOptions.setWill(e4kConnectionChangeTopic, new MqttMessage(serializedWillPayload, QOS, true, new MqttProperties()));

            if (this.config.getAuthenticationType() == ClientConfiguration.AuthType.SAS_TOKEN)
            {
                String sasData = "";
                try
                {
                    sasData = new String(this.config.getSasTokenAuthentication().getSasToken());
                    connectOptions.setAuthData(sasData.getBytes(StandardCharsets.UTF_8));
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

            ProxySettings proxySettings = this.config.getProxySettings();
            if (proxySettings != null)
            {
                if (proxySettings.getProxy().type() == Proxy.Type.SOCKS)
                {
                    try
                    {
                        connectOptions.setSocketFactory(new Socks5SocketFactory(proxySettings.getHostname(), proxySettings.getPort()));
                    }
                    catch (UnknownHostException e)
                    {
                        throw new TransportException("Failed to build the Socks5SocketFactory", e);
                    }
                }
                else if (proxySettings.getProxy().type() == Proxy.Type.HTTP)
                {
                    connectOptions.setSocketFactory(new HttpProxySocketFactory(sslContext.getSocketFactory(), proxySettings));
                }
                else
                {
                    throw new IllegalArgumentException("Proxy settings must be configured to use either SOCKS or HTTP");
                }
            }
            else
            {
                connectOptions.setSocketFactory(sslContext.getSocketFactory());
            }

            log.debug("Opening MQTT connection...");
            IMqttToken token = this.mqttAsyncClient.connect(connectOptions);
            token.waitForCompletion(); // TODO timeout value?

            try
            {
                log.debug("Sending the \"Opened a connection\" message to the MQTT broker");
                E4KConnectionMessagePayload connectPayload = E4KConnectionMessagePayload.builder()
                    .connectionState(E4KConnectionState.Connected)
                    .modelId(this.config.getModelId())
                    .mqttVersion(MQTT_VERSION_STRING)
                    .deviceClientType(this.config.getProductInfo().getUserAgentString())
                    .build();
                byte[] serializedConnectPayload = GSON.toJson(connectPayload).getBytes(StandardCharsets.UTF_8);
                IMqttToken publishToken = this.mqttAsyncClient.publish(
                    e4kConnectionChangeTopic,
                    serializedConnectPayload,
                    QOS,
                    true);

                // If the message fails to be sent/acknowledged in this time span, an MqttException will be thrown here
                publishToken.waitForCompletion(E4K_CONNECTION_OPEN_MESSAGE_TIMEOUT_MILLISECONDS);
            }
            catch (MqttException e)
            {
                try
                {
                    // To avoid the case where the SDK has a connection open to the MQTT broker but E4K isn't aware,
                    // tear down the connection and make the user retry the entire "open" operation
                    this.mqttAsyncClient.close();
                }
                catch (MqttException ex)
                {
                    log.warn("Encountered an error while closing the MQTT connection", ex);
                }

                TransportException transportException = new TransportException(
                    "Successfully opened the MQTT connection but failed to send the " +
                        "\"Connection established\" message to the MQTT gateway. Closing the connection...");

                transportException.setRetryable(true);
                throw transportException;
            }

            this.mqtt5Client.setMqttAsyncClient(this.mqttAsyncClient);
            this.mqtt5Client.start();

            this.state = IotHubConnectionStatus.CONNECTED;

            log.debug("MQTT connection opened successfully");

            this.listener.onConnectionEstablished(this.connectionId);
        }
    }

    /**
     * Closes the connection. After the connection is closed, it is no longer usable.
     * If the connection is already closed, the function shall do nothing.
     */
    public void close()
    {
        synchronized (this.mqttConnectionStateLock)
        {
            if (this.state == IotHubConnectionStatus.DISCONNECTED)
            {
                return;
            }

            try
            {
                log.debug("Sending the \"Closing a connection\" message to the MQTT broker");
                E4KConnectionMessagePayload disconnectPayload = E4KConnectionMessagePayload.builder()
                    .connectionState(E4KConnectionState.Disconnected)
                    .modelId(this.config.getModelId())
                    .mqttVersion(MQTT_VERSION_STRING)
                    .deviceClientType(this.config.getProductInfo().getUserAgentString())
                    .build();
                byte[] serializedPayload = GSON.toJson(disconnectPayload).getBytes(StandardCharsets.UTF_8);
                IMqttToken token = this.mqttAsyncClient.publish(
                    e4kConnectionChangeTopic,
                    serializedPayload,
                    QOS,
                    true);

                // If the message fails to be sent/acknowledged in this time span, an MqttException will be thrown here
                token.waitForCompletion(E4K_CONNECTION_CLOSE_MESSAGE_TIMEOUT_MILLISECONDS);
            }
            catch (MqttException e)
            {
                // This is a bit of an odd case, but it seems likely that this catch block on executes if the connection
                // was ungracefully dropped while sending this message. In that case, the "will" message will be read
                // by the MQTT broker so we don't need to re-send this message or throw an exception to the user.
                log.warn("Failed to send the \"Connection will be closed gracefully\" message to the MQTT gateway. " +
                    "Still closing the connection anyways", e);
            }

            log.debug("Closing MQTT connection");
            IMqttToken token = this.mqttAsyncClient.disconnect();
            token.waitForCompletion(); // TODO timeout value?

            this.mqtt5Client.stop();

            this.state = IotHubConnectionStatus.DISCONNECTED;
            log.debug("Successfully closed MQTT connection");
        }
    }

    @Override
    public void setListener(IotHubListener listener) throws IllegalArgumentException
    {
        if (listener == null)
        {
            throw new IllegalArgumentException("listener cannot be null");
        }

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
        if (message == null || message.getBytes() == null ||
                ((message.getMessageType() != DEVICE_TWIN
                        && message.getMessageType() != DEVICE_METHODS)
                        && message.getBytes().length == 0))
        {
            return IotHubStatusCode.BAD_FORMAT;
        }

        if (this.state == IotHubConnectionStatus.DISCONNECTED)
        {
            throw new IllegalStateException("Cannot send event using a closed MQTT connection");
        }

        IotHubStatusCode result = IotHubStatusCode.OK;

        if (message.getMessageType() == DEVICE_METHODS)
        {
            log.trace("Sending MQTT device method message ({})", message);
            this.mqtt5Client.sendDirectMethodMessage((IotHubTransportMessage) message);
        }
        else if (message.getMessageType() == DEVICE_TWIN)
        {
            log.trace("Sending MQTT device twin message ({})", message);
            this.mqtt5Client.sendTwinMessage((IotHubTransportMessage) message);
        }
        else
        {
            log.trace("Sending MQTT device telemetry message ({})", message);
            this.mqtt5Client.sendTelemetryMessage(message);
        }

        return result;
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
            throw new TransportException(new IllegalArgumentException("message and result must be non-null"));
        }

        if (message.getQualityOfService() == 0)
        {
            // messages that the service sent with QoS 0 don't need to be acknowledged.
            return true;
        }

        int messageId;
        log.trace("Checking if MQTT layer can acknowledge the received message ({})", message);
        if (receivedMessagesToAcknowledge.containsKey(message))
        {
            messageId = receivedMessagesToAcknowledge.get(message);
        }
        else
        {
            TransportException e = new TransportException(new IllegalArgumentException("Provided message cannot be acknowledged because it was already acknowledged or was never received from service"));
            log.error("Mqtt layer could not acknowledge received message because it has no mapping to an outstanding mqtt message id ({})", message, e);
            throw e;
        }

        log.trace("Sending MQTT ACK for a received message ({})", message);
        this.mqtt5Client.sendMessageAcknowledgement(messageId);

        log.trace("MQTT ACK was sent for a received message so it has been removed from the messages to acknowledge list ({})", message);
        this.receivedMessagesToAcknowledge.remove(message);

        return true;
    }

    @Override
    public String getConnectionId()
    {
        return this.connectionId;
    }

    @Override
    public void disconnected(MqttDisconnectResponse mqttDisconnectResponse)
    {
        //TODO
    }

    @Override
    public void mqttErrorOccurred(MqttException e)
    {
        //TODO
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception
    {
        //TODO
    }

    @Override
    public void deliveryComplete(IMqttToken iMqttToken)
    {
        //TODO
    }

    @Override
    public void connectComplete(boolean reconnect, String serverUri)
    {
        //TODO
    }

    @Override
    public void authPacketArrived(int reasonCode, MqttProperties properties)
    {
        //TODO
    }
}
