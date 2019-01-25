/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.deps.transport.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.net.ssl.SSLContext;
import java.io.IOException;

import static com.microsoft.azure.sdk.iot.deps.transport.mqtt.MqttMessage.retrieveQosValue;

public class MqttConnection implements MqttCallback
{
    private static final String WS_SSL_URL_FORMAT = "wss://%s:443";
    private static final String SSL_URL_FORMAT = "ssl://%s:8883";

    private MqttAsyncClient mqttAsyncClient;
    private MqttConnectOptions connectionOptions;

    //mqtt connection options
    private static final int KEEP_ALIVE_INTERVAL = 230;
    private static final int MQTT_VERSION = 4;
    private static final boolean SET_CLEAN_SESSION = false;
    static final int MAX_WAIT_TIME = 1000;

    private MqttListener mqttListener;

    /**
     * Constructor to create MqttAsync Client with Paho
     * @param hostname Uri to connect to
     * @param clientId Client Id to connect to
     * @param userName Username
     * @param password password
     * @param sslContext SSLContext for the connection
     * @param listener Mqtt listener
     * @param useWebSockets true to use Mqtt over web sockets
     * @throws IOException is thrown if any of the parameters are null or empty or client cannot be instantiated
     */
    public MqttConnection(String hostname, String clientId, String userName, String password, SSLContext sslContext, MqttListener listener, boolean useWebSockets) throws IOException
    {
        if (hostname == null || clientId == null || userName == null || sslContext == null)
        {
            throw new IllegalArgumentException();
        }
        if (hostname.isEmpty() || clientId.isEmpty() || userName.isEmpty())
        {
            throw new IllegalArgumentException();
        }
        if (listener == null)
        {
            throw new IllegalArgumentException("The listener cannot be null.");
        }

        try
        {
            final String serverUri;
            if (useWebSockets)
            {
                serverUri = String.format(WS_SSL_URL_FORMAT, hostname);
            }
            else
            {
                serverUri = String.format(SSL_URL_FORMAT, hostname);
            }
            this.mqttListener = listener;
            this.mqttAsyncClient = new MqttAsyncClient(serverUri, clientId, new MemoryPersistence());
            this.connectionOptions = new MqttConnectOptions();
            this.mqttAsyncClient.setCallback(this);
            this.updateConnectionOptions(userName, password, sslContext);
        }
        catch (MqttException e)
        {
            this.mqttAsyncClient = null;
            this.connectionOptions = null;
            throw new IOException("Error initializing MQTT connection:" + e.getMessage());
        }
    }

    /**
     * Return whether the MQTT broker is connected to the endpoint
     * @return true if connected using Mqtt
     */
    public boolean isMqttConnected()
    {
        boolean result;
        if (this.mqttAsyncClient == null)
        {
            result = false;
        }
        else
        {
            result = this.mqttAsyncClient.isConnected();
        }
        return result;
    }

    /**
     * Generates the connection options for the mqtt broker connection.
     *
     * @param userName the user name for the mqtt broker connection.
     * @param userPassword the user password for the mqtt broker connection.
     */
    private void updateConnectionOptions(String userName, String userPassword, SSLContext sslContext)
    {
        this.connectionOptions.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);
        this.connectionOptions.setCleanSession(SET_CLEAN_SESSION);
        this.connectionOptions.setMqttVersion(MQTT_VERSION);
        this.connectionOptions.setUserName(userName);
        this.connectionOptions.setSocketFactory(sslContext.getSocketFactory());

        if (userPassword != null && !userPassword.isEmpty())
        {
            this.connectionOptions.setPassword(userPassword.toCharArray());
        }
    }

    /**
     * Connects to the MQTT broker
     * @throws IOException if there is a Mqtt exception.
     */
    public synchronized void connect() throws IOException
    {
        try
        {
            if (!this.mqttAsyncClient.isConnected())
            {
                IMqttToken connectToken = this.mqttAsyncClient.connect(this.connectionOptions);
                connectToken.waitForCompletion();
                if (this.mqttListener != null)
                {
                    this.mqttListener.connectionEstablished();
                }
            }
        }
        catch (MqttException e)
        {
            throw new IOException("Unable to connect to mqtt service", e);
        }
    }

    /**
     * Disconnects from the MQTT broker
     * @throws IOException if there is a Mqtt exception.
     */
    public synchronized void disconnect() throws IOException
    {
        try
        {
            if (this.mqttAsyncClient.isConnected())
            {
                IMqttToken disconnectToken = this.mqttAsyncClient.disconnect();
                disconnectToken.waitForCompletion();
            }
        }
        catch (MqttException e)
        {
            throw new IOException("Unable to connect to mqtt service", e);
        }
    }

    /**
     * Sends a PUBLISH message to the MQTT broker
     * @param topic The topic of the message
     * @param qos The QOS of the message
     * @param message The message to be sent
     * @throws IOException if there is a Mqtt exception.
     */
    public synchronized void publishMessage(String topic, MqttQos qos, byte[] message) throws IOException
    {
        MqttMessage mqttMessage;
        if (message == null || message.length == 0)
        {
            mqttMessage = new MqttMessage(topic);
        }
        else
        {
            mqttMessage = new MqttMessage(topic, message);
        }
        mqttMessage.setQos(qos);

        publishMessage(mqttMessage);
    }

    /**
     * Sends a PUBLISH message to the MQTT broker
     * @param message The message to be sent
     * @throws IOException if there is a Mqtt exception.
     */
    public synchronized void publishMessage(MqttMessage message) throws IOException
    {
        if (this.mqttAsyncClient == null || !this.mqttAsyncClient.isConnected())
        {
            throw new IOException("Mqtt is not connected");
        }
        if (message == null)
        {
            throw new IOException("MqttMessage is null");
        }

        try
        {
            IMqttDeliveryToken publishToken = this.mqttAsyncClient.publish(message.getTopic(), message.getMqttMessage());
            publishToken.waitForCompletion();
        }
        catch (MqttException e)
        {
            throw new IOException("Unable to publish message on topic : " + message.getTopic(), e);
        }
    }

    /**
     * Send the SUBSCRIBE message to the MQTT broker
     * @param topic The topic of the message
     * @param qos The QOS of the message
     * @throws IOException if there is a Mqtt exception.
     */
    public synchronized void subscribe(String topic, MqttQos qos) throws IOException
    {
        if (this.mqttAsyncClient == null || !this.mqttAsyncClient.isConnected())
        {
            throw new IOException("Mqtt is not connected");
        }

        try
        {
            IMqttToken subToken = this.mqttAsyncClient.subscribe(topic, retrieveQosValue(qos));
            subToken.waitForCompletion(MAX_WAIT_TIME);
        }
        catch (MqttException e)
        {
            throw new IOException("Unable to subscribe on topic : " + topic, e);
        }
    }

    /**
     * Send the UNSUBSCRIBE message to the MQTT broker
     * @param topic Name of the Topic to unsubscribe.
     * @throws IOException if there is a Mqtt exception.
     */
    public synchronized void unsubscribe(String topic) throws IOException
    {
        try
        {
            IMqttToken subToken = this.mqttAsyncClient.unsubscribe(topic);
            subToken.waitForCompletion();
        }
        catch (MqttException e)
        {
            throw new IOException("Unable to unsubscribe message on topic : " + topic, e);
        }
    }

    /**
     * Event fired when the message arrived on the MQTT broker.
     * @param topic the topic on which message arrived.
     * @param mqttMessage  the message arrived on the Mqtt broker.
     */
    @Override
    public synchronized void messageArrived(String topic, org.eclipse.paho.client.mqttv3.MqttMessage mqttMessage)
    {
        this.mqttListener.messageReceived(new MqttMessage(topic, mqttMessage));
    }

    /**
     * Event fired when the message arrived on the MQTT broker.
     * @param iMqttDeliveryToken the MqttDeliveryToken for which the message was successfully sent.
     */
    @Override
    public synchronized void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken)
    {
    }

    /**
     * Event fired when the connection is lost on the MQTT broker
     * @param throwable the disconnection reason.
     */
    @Override
    public synchronized void connectionLost(Throwable throwable)
    {
        this.mqttListener.connectionLost(throwable);
    }
}
