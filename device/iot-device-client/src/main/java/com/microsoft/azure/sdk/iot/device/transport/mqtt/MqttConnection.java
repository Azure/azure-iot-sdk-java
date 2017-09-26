/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MqttConnection
{
    private MqttAsyncClient mqttAsyncClient = null;
    private MqttConnectOptions connectionOptions = null;
    private ConcurrentLinkedQueue<Pair<String, byte[]>> allReceivedMessages;
    private Object mqttLock;
    private MqttCallback mqttCallback;

    //mqtt connection options
    private static final int KEEP_ALIVE_INTERVAL = 20;
    private static final int MQTT_VERSION = 4;
    private static final boolean SET_CLEAN_SESSION = false;
    static final int QOS = 1;
    static final int MAX_WAIT_TIME = 1000;

    // paho mqtt only supports 10 messages in flight at the same time
    static final int MAX_IN_FLIGHT_COUNT = 10;

    /**
     * Constructor to create MqttAsync Client with Paho
     * @param serverURI Uri to connect to
     * @param clientId Client Id to connect to
     * @param userName Username
     * @param password password
     * @param iotHubSSLContext SSLContext for the connection
     * @throws IOException is thrown if any of the parameters are null or empty or client cannot be instantiated
     */
    MqttConnection(String serverURI, String clientId, String userName, String password, SSLContext iotHubSSLContext) throws IOException
    {
        if (serverURI == null || clientId == null || userName == null || iotHubSSLContext == null)
        {
            //Codes_SRS_MQTTCONNECTION_25_001: [The constructor shall throw InvalidParameter Exception if any of the input parameters are null other than password.]
            throw new IllegalArgumentException();
        }

        else if (serverURI.isEmpty() || clientId.isEmpty() || userName.isEmpty())
        {
            //Codes_SRS_MQTTCONNECTION_25_002: [The constructor shall throw InvalidParameter Exception if serverUri, clientId, userName, password are empty.]
            throw new IllegalArgumentException();
        }

        try
        {
            //Codes_SRS_MQTTCONNECTION_25_004: [The constructor shall create an MqttAsync client and update the connection options using the provided serverUri, clientId, userName, password and sslContext.]
            mqttAsyncClient = new MqttAsyncClient(serverURI, clientId, new MemoryPersistence());
            connectionOptions = new MqttConnectOptions();
            this.updateConnectionOptions(userName, password, iotHubSSLContext);
        }
        catch (MqttException e)
        {
            mqttAsyncClient = null;
            connectionOptions = null;
            throw new IOException("Error initializing MQTT connection:" + e.getMessage());
        }

        //Codes_SRS_MQTTCONNECTION_25_003: [The constructor shall create lock, queue for this MqttConnection.]
        this.allReceivedMessages = new ConcurrentLinkedQueue<>();
        this.mqttLock = new Object();
    }

    /**
     * Generates the connection options for the mqtt broker connection.
     *
     * @param userName the user name for the mqtt broker connection.
     * @param userPassword the user password for the mqtt broker connection.
     */
    private void updateConnectionOptions(String userName, String userPassword, SSLContext iotHubSSLContext)
    {
        this.connectionOptions.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);
        this.connectionOptions.setCleanSession(SET_CLEAN_SESSION);
        this.connectionOptions.setMqttVersion(MQTT_VERSION);
        this.connectionOptions.setUserName(userName);
        this.connectionOptions.setSocketFactory(iotHubSSLContext.getSocketFactory());

        if (userPassword != null && !userPassword.isEmpty())
        {
            this.connectionOptions.setPassword(userPassword.toCharArray());
        }
    }

    /**
     * Callback to trigger onto if any of the Paho API's triggers callback
     * @param mqttCallback callback to be set
     * @throws IllegalArgumentException is thrown if callback is null
     */
    void setMqttCallback(MqttCallback mqttCallback) throws IllegalArgumentException
    {
        if (mqttCallback == null)
        {
            //Codes_SRS_MQTTCONNECTION_25_006: [This method shall throw IllegalArgumentException if callback is null.]
            throw new IllegalArgumentException("callback cannot be null");
        }
        //Codes_SRS_MQTTCONNECTION_25_005: [This method shall set the callback for Mqtt.]
        this.mqttCallback = mqttCallback;
        this.getMqttAsyncClient().setCallback(mqttCallback);
    }

    /**
     * Getter for Mqtt Async Client
     * @return Mqtt Async Client created by this object
     */
    MqttAsyncClient getMqttAsyncClient()
    {

        //Codes_SRS_MQTTCONNECTION_25_007: [Getter for the MqttAsyncClient.]
        return mqttAsyncClient;
    }

    /**
     * Getter for queue for the messages
     * @return Queue for the messages
     */
    ConcurrentLinkedQueue<Pair<String, byte[]>> getAllReceivedMessages()
    {
        //Codes_SRS_MQTTCONNECTION_25_008: [Getter for the Message Queue.]
        return allReceivedMessages;
    }

    /**
     * Getter for Mqtt Lock
     * @return The object to be used for the lock
     */
    Object getMqttLock()
    {
        //Codes_SRS_MQTTCONNECTION_25_009: [Getter for the Mqtt Lock on this connection.]
        return mqttLock;
    }

    /**
     * Getter for connection Options
     * @return the connection options to be used for Mqtt
     */
    MqttConnectOptions getConnectionOptions()
    {
        //Codes_SRS_MQTTCONNECTION_25_010: [Getter for the MqttConnectionOptions.]
        return connectionOptions;
    }

    /**
     * Setter for mqttAsyncClient
     * @param mqttAsyncClient set mqttAsyncClient. Can be {@code null}.
     */
    void setMqttAsyncClient(MqttAsyncClient mqttAsyncClient)
    {
        //Codes_SRS_MQTTCONNECTION_25_011: [Setter for the MqttAsyncClient which can be null.]
        this.mqttAsyncClient = mqttAsyncClient;
    }
}
