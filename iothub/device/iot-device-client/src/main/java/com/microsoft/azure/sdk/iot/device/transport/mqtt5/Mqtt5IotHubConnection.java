// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt5;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.*;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.*;
import lombok.extern.slf4j.Slf4j;
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

        try
        {
            this.mqttAsyncClient = new MqttAsyncClient(serverUri, clientId, new MemoryPersistence());
        }
        catch (MqttException e)
        {
            throw Mqtt5ExceptionTranslator.convertToMqttException(e, "Failed to create mqtt client");
        }

        mqttAsyncClient.setManualAcks(true);
        mqttAsyncClient.setCallback(this);
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

            //TODO send CONNECT message to MQTT broker so E4K knows this client has connected

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

            //TODO send DISCONNECT message to MQTT broker so E4K knows this client is disconnecting

            log.debug("Closing MQTT connection");
            IMqttToken token = this.mqttAsyncClient.disconnect();
            token.waitForCompletion(); // TODO timeout value?

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
        IotHubStatusCode result = IotHubStatusCode.OK;

        if (message.getMessageType() == DEVICE_METHODS)
        {
            // TODO
        }
        else if (message.getMessageType() == DEVICE_TWIN)
        {
            // TODO
        }
        else
        {
            // TODO
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
        if (message.getMessageType() == DEVICE_METHODS)
        {
            // TODO
        }
        else if (message.getMessageType() == DEVICE_TWIN)
        {
            // TODO
        }
        else
        {
            // TODO
        }

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
