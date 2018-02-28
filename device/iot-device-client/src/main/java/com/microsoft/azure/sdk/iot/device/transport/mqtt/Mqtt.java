// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.CustomLogger;
import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasToken;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubServiceException;
import com.microsoft.azure.sdk.iot.device.exceptions.ProtocolException;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.exceptions.*;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.paho.client.mqttv3.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.eclipse.paho.client.mqttv3.MqttException.*;

abstract public class Mqtt implements MqttCallback
{
    private MqttConnection mqttConnection;
    private DeviceClientConfig deviceClientConfig = null;
    private CustomLogger logger = null;
    ConcurrentLinkedQueue<Pair<String, byte[]>> allReceivedMessages;
    Object mqttLock = null;

    // SAS token expiration check on retry
    private boolean userSpecifiedSASTokenExpiredOnRetry = false;

    /* Each property is separated by & and all system properties start with an encoded $ (except for iothub-ack) */
    final static char MESSAGE_PROPERTY_SEPARATOR = '&';
    private final static String MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_ENCODED = "%24";
    private final static char MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_DECODED = '$';
    final static char MESSAGE_PROPERTY_KEY_VALUE_SEPARATOR = '=';
    private final static int PROPERTY_KEY_INDEX = 0;
    private final static int PROPERTY_VALUE_INDEX = 1;

    /* The system property keys expected in a message */
    //This may be common with amqp as well
    private final static String ABSOLUTE_EXPIRY_TIME = MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_DECODED + ".exp";
    final static String CORRELATION_ID = MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_DECODED + ".cid";
    final static String MESSAGE_ID = MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_DECODED + ".mid";
    final static String TO = MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_DECODED + ".to";
    final static String USER_ID = MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_DECODED + ".uid";
    private final static String IOTHUB_ACK = "iothub-ack";

    private MqttConnectionStateListener listener;

    /**
     * Constructor to instantiate mqtt broker connection.
     * @param mqttConnection the connection to use
     * @throws TransportException if the provided mqttConnection is null
     */
    public Mqtt(MqttConnection mqttConnection, MqttConnectionStateListener listener) throws TransportException
    {
        if (mqttConnection == null)
        {
            //Codes_SRS_Mqtt_25_002: [The constructor shall throw a TransportException if mqttConnection is null .]
            throw new TransportException(new IllegalArgumentException("Mqtt connection info cannot be null"));
        }

        //Codes_SRS_Mqtt_25_003: [The constructor shall retrieve lock, queue from the provided connection information and save the connection.]
        this.mqttConnection = mqttConnection;
        this.allReceivedMessages = mqttConnection.getAllReceivedMessages();
        this.mqttLock = mqttConnection.getMqttLock();
        this.userSpecifiedSASTokenExpiredOnRetry = false;
        this.listener = listener;
        this.logger = new CustomLogger(Mqtt.class);
    }

    /**
     * Method to connect to mqtt broker connection.
     *
     * @throws TransportException if failed to establish the mqtt connection.
     */
    protected void connect() throws TransportException
    {
        synchronized (this.mqttLock)
        {
            try
            {
                //Codes_SRS_Mqtt_25_008: [If the MQTT connection is already open, the function shall do nothing.]
                if (!this.mqttConnection.getMqttAsyncClient().isConnected())
                {
                    //Codes_SRS_Mqtt_25_005: [The function shall establish an MQTT connection with an IoT Hub using the provided host name, user name, device ID, and sas token.]
                    IMqttToken connectToken = this.mqttConnection.getMqttAsyncClient().connect(Mqtt.this.mqttConnection.getConnectionOptions());
                    connectToken.waitForCompletion();

                    //Codes_SRS_Mqtt_34_020: [If the MQTT connection is established successfully, this function shall notify its listener that connection was established.]
                    if (this.listener != null)
                    {
                        this.listener.onConnectionEstablished();
                    }
                }
            }
            catch (MqttException e)
            {
                throw PahoExceptionTranslator.translatePahoException(e, "Unable to establish MQTT connection");
            }
        }
    }

    /**
     * Method to disconnect to mqtt broker connection.
     *
     * @throws TransportException if failed to ends the mqtt connection.
     */
    protected void disconnect() throws TransportException
    {
        //Codes_SRS_Mqtt_25_010: [If the MQTT connection is closed, the function shall do nothing.]
        if (this.mqttConnection.getMqttAsyncClient() != null)
        {
            try
            {
                if (this.mqttConnection.getMqttAsyncClient().isConnected())
                {
                    //Codes_SRS_Mqtt_34_055: [If an MQTT connection is connected, the function shall disconnect that connection.]
                    IMqttToken disconnectToken = this.mqttConnection.getMqttAsyncClient().disconnect();
                    disconnectToken.waitForCompletion();
                }

                //Codes_SRS_Mqtt_25_009: [The function shall close the MQTT client.]
                this.mqttConnection.getMqttAsyncClient().close();
                this.mqttConnection.setMqttAsyncClient(null);
            }
            catch (MqttException e)
            {
                //Codes_SRS_Mqtt_25_011: [If an MQTT connection is unable to be closed for any reason, the function shall throw a TransportException.]
                throw PahoExceptionTranslator.translatePahoException(e, "Unable to disconnect");
            }
        }
    }

    /**
     * Method to publish to mqtt broker connection.
     *
     * @param publishTopic the topic to publish on mqtt broker connection.
     * @param payload   the payload to publish on publishTopic of mqtt broker connection.
     */
    protected void publish(String publishTopic, byte[] payload) throws TransportException
    {
        synchronized (this.mqttLock)
        {
            try
            {
                if (this.userSpecifiedSASTokenExpiredOnRetry)
                {
                    //Codes_SRS_Mqtt_99_049: [If the user supplied SAS token has expired, the function shall throw a TransportException.]
                    throw new TransportException("Cannot publish when user supplied SAS token has expired");
                }

                if (!this.mqttConnection.getMqttAsyncClient().isConnected())
                {
                    //Codes_SRS_Mqtt_25_012: [If the MQTT connection is closed, the function shall throw a ProtocolException.]
                    ProtocolException connectionException = new ProtocolException("Cannot publish when mqtt client is disconnected");
                    connectionException.setRetryable(true);
                    throw connectionException;
                }

                if (publishTopic == null || publishTopic.length() == 0 || payload == null)
                {
                    //Codes_SRS_Mqtt_25_013: [If the either publishTopic is null or empty or if payload is null, the function shall throw a TransportException.]
                    throw new TransportException(new IllegalArgumentException("Cannot publish on null or empty publish topic"));
                }

                while (this.mqttConnection.getMqttAsyncClient().getPendingDeliveryTokens().length >= MqttConnection.MAX_IN_FLIGHT_COUNT)
                {
                    //Codes_SRS_Mqtt_25_048: [publish shall check for pending publish tokens by calling getPendingDeliveryTokens. And if there are pending tokens publish shall sleep until the number of pending tokens are less than 10 as per paho limitations]
                    Thread.sleep(10);

                    if (!this.mqttConnection.getMqttAsyncClient().isConnected())
                    {
                        //Codes_SRS_Mqtt_25_012: [If the MQTT connection is closed, the function shall throw a ProtocolException.]
                        ProtocolException connectionException = new ProtocolException("Cannot publish when mqtt client is holding 10 tokens and  is disconnected");
                        connectionException.setRetryable(true);
                        throw connectionException;
                    }
                }

                MqttMessage mqttMessage = (payload.length == 0) ? new MqttMessage() : new MqttMessage(payload);

                mqttMessage.setQos(MqttConnection.QOS);

                //Codes_SRS_Mqtt_25_014: [The function shall publish message payload on the publishTopic specified to the IoT Hub given in the configuration.]
                IMqttDeliveryToken publishToken = this.mqttConnection.getMqttAsyncClient().publish(publishTopic, mqttMessage);
            }
            catch (MqttException e)
            {
                //Codes_SRS_Mqtt_25_047: [If the Mqtt Client Async throws MqttException, the function shall throw a ProtocolException with the message.]
                throw PahoExceptionTranslator.translatePahoException(e, "Unable to publish message on topic : " + publishTopic);
            }
            catch (InterruptedException e)
            {
                throw new TransportException("Interrupted, Unable to publish message on topic : " + publishTopic);
            }
        }
    }

    /**
     * Method to subscribe to mqtt broker connection.
     *
     * @param topic the topic to subscribe on mqtt broker connection.
     * @throws TransportException if failed to subscribe the mqtt topic.
     */
    protected void subscribe(String topic) throws TransportException
    {
        synchronized (this.mqttLock)
        {
            try
            {
                if (topic == null)
                {
                    //Codes_SRS_Mqtt_25_016: [If the subscribeTopic is null or empty, the function shall throw a TransportException.]
                    throw new TransportException(new IllegalArgumentException("Topic cannot be null"));

                }
                else if (this.userSpecifiedSASTokenExpiredOnRetry)
                {
                    //Codes_SRS_Mqtt_99_049: [If the user supplied SAS token has expired, the function shall throw a TransportException.]
                    throw new TransportException("Cannot subscribe when user supplied SAS token has expired");
                }
                else if (!this.mqttConnection.getMqttAsyncClient().isConnected())
                {

                    //Codes_SRS_Mqtt_25_015: [If the MQTT connection is closed, the function shall throw a ProtocolException with message.]
                    ProtocolException connectionException = new ProtocolException("Cannot suscribe when mqtt client is disconnected");
                    connectionException.setRetryable(true);
                    throw connectionException;
                }

                //Codes_SRS_Mqtt_25_017: [The function shall subscribe to subscribeTopic specified to the IoT Hub given in the configuration.]
                IMqttToken subToken = this.mqttConnection.getMqttAsyncClient().subscribe(topic, MqttConnection.QOS);

                subToken.waitForCompletion(MqttConnection.MAX_WAIT_TIME);
            }
            catch (MqttException e)
            {
                //Codes_SRS_Mqtt_25_048: [If the Mqtt Client Async throws MqttException for any reason, the function shall throw a ProtocolException with the message.]
                throw PahoExceptionTranslator.translatePahoException(e, "Unable to subscribe to topic :" + topic);
            }
        }
    }

    /**
     * Method to receive messages on mqtt broker connection.
     *
     * @return a received message. It can be {@code null}
     * @throws TransportException if failed to receive mqtt message.
     */
    public Message receive() throws TransportException
    {
        synchronized (this.mqttLock)
        {
            if (this.mqttConnection == null)
            {
                throw new TransportException(new IllegalArgumentException("Mqtt client should be initialised at least once before using it"));
            }

            // Codes_SRS_Mqtt_34_023: [This method shall call peekMessage to get the message payload from the received Messages queue corresponding to the messaging client's operation.]
            Pair<String, byte[]> messagePair = peekMessage();
            if (messagePair != null)
            {
                String topic = messagePair.getKey();
                if (topic != null)
                {
                    byte[] data = messagePair.getValue();
                    if (data != null)
                    {
                        //remove this message from the queue as this is the correct handler
                        allReceivedMessages.poll();

                        // Codes_SRS_Mqtt_34_024: [This method shall construct new Message with the bytes obtained from peekMessage and return the message.]
                        return constructMessage(data, topic);
                    }
                    else
                    {
                        // Codes_SRS_Mqtt_34_025: [If the call to peekMessage returns null when topic is non-null then this method will throw a TransportException]
                        throw new TransportException("Data cannot be null when topic is non-null");
                    }
                }
                else
                {
                    // Codes_SRS_Mqtt_34_022: [If the call peekMessage returns a null or empty string then this method shall do nothing and return null]
                    return null;
                }
            }

            // Codes_SRS_Mqtt_34_021: [If the call peekMessage returns null then this method shall do nothing and return null]
            return null;
        }
    }

    /**
     * Event fired when the connection with the MQTT broker is lost.
     * @param throwable Reason for losing the connection.
     */
    @Override
    public void connectionLost(Throwable throwable)
    {
        if (this.listener != null)
        {
            if (throwable instanceof MqttException)
            {
                //Codes_SRS_Mqtt_34_055: [If the provided throwable is an instance of MqttException, this function shall derive the associated ConnectionStatusException and notify the listener of that derived exception.]
                throwable = PahoExceptionTranslator.translatePahoException((MqttException) throwable, "Mqtt connection lost");
            }
            else
            {
                throwable = new TransportException(throwable);
            }

            //Codes_SRS_Mqtt_34_045: [If this object has a saved listener, this function shall notify the listener that connection was lost.]
            this.listener.onConnectionLost(throwable);
        }
    }

    /**
     * Event fired when the message arrived on the MQTT broker.
     * @param topic the topic on which message arrived.
     * @param mqttMessage  the message arrived on the Mqtt broker.
     */
    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage)
    {
        //Codes_SRS_Mqtt_25_030: [The payload of the message and the topic is added to the received messages queue .]
        this.mqttConnection.getAllReceivedMessages().add(new MutablePair<>(topic, mqttMessage.getPayload()));
    }

    /**
     * Event fired when the message arrived on the MQTT broker.
     * @param iMqttDeliveryToken the MqttDeliveryToken for which the message was successfully sent.
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken)
    {

    }

    public Pair<String, byte[]> peekMessage() throws TransportException
    {
        if (this.allReceivedMessages == null)
        {
            // Codes_SRS_MQTTDEVICEMETHOD_34_034: [If allReceivedMessages queue is null then this method shall throw a TransportException.]
            throw new TransportException(new IllegalStateException("Queue cannot be null"));
        }

        return this.allReceivedMessages.peek();
    }

    /**
     * Converts the provided data and topic string into an instance of Message
     * @param data the payload from the topic
     * @param topic the topic string for this message
     * @return a new instance of Message containing the payload and all the properties in the topic string
     * @throws TransportException if the topic string has no system properties
     */
    private Message constructMessage(byte[] data, String topic) throws TransportException
    {
        //Codes_SRS_Mqtt_25_024: [This method shall construct new Message with the bytes obtained from parsePayload and return the message.]
        Message message = new Message(data);

        int propertiesStringStartingIndex = topic.indexOf(MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_ENCODED);
        if (propertiesStringStartingIndex != -1)
        {
            String propertiesString = topic.substring(propertiesStringStartingIndex);

            //Codes_SRS_Mqtt_34_041: [This method shall call assignPropertiesToMessage so that all properties from the topic string can be assigned to the message]
            assignPropertiesToMessage(message, propertiesString);
        }

        return message;
    }

    /**
     * Takes propertiesString and parses it for all the properties it holds and then assigns them to the provided message
     * @param propertiesString the string to parse containing all the properties
     * @param message the message to add the parsed properties to
     * @throws TransportException if a property's key and value are not separated by the '=' symbol
     * @throws TransportException if the property for expiry time is present, but the value cannot be parsed as a Long
     * */
    private void assignPropertiesToMessage(Message message, String propertiesString) throws TransportException
    {
        //Codes_SRS_Mqtt_34_054: [A message may have 0 to many custom properties]
        //expected format is <key>=<value><MESSAGE_PROPERTY_SEPARATOR><key>=<value><MESSAGE_PROPERTY_SEPARATOR>...
        for (String propertyString : propertiesString.split(String.valueOf(MESSAGE_PROPERTY_SEPARATOR)))
        {
            if (propertyString.contains("="))
            {
                //Expected format is <key>=<value> where both key and value may be encoded
                String key = propertyString.split("=")[PROPERTY_KEY_INDEX];
                String value = propertyString.split("=")[PROPERTY_VALUE_INDEX];
                try
                {
                    //Codes_SRS_Mqtt_34_053: [A property's key and value may include unusual characters such as &, %, $]
                    key = URLDecoder.decode(key, StandardCharsets.UTF_8.name());
                    value = URLDecoder.decode(value, StandardCharsets.UTF_8.name());
                }
                catch (UnsupportedEncodingException e)
                {
                    // should never happen, since the encoding is hard-coded.
                    throw new TransportException(new IllegalStateException(e));
                }

                //Some properties are reserved system properties and must be saved in the message differently
                switch (key)
                {
                    case TO:
                        //do nothing
                        break;
                    case MESSAGE_ID:
                        message.setMessageId(value);
                        break;
                    case IOTHUB_ACK:
                        //do nothing
                        break;
                    case CORRELATION_ID:
                        message.setCorrelationId(value);
                        break;
                    case USER_ID:
                        //do nothing
                        break;
                    case ABSOLUTE_EXPIRY_TIME:
                        //do nothing
                        break;
                    default:
                        message.setProperty(key, value);
                }
            }
            else
            {
                //Codes_SRS_Mqtt_34_051: [If a topic string's property's key and value are not separated by the '=' symbol, a TransportException shall be thrown]
                throw new TransportException(new IllegalArgumentException("Unexpected property string provided. Expected '=' symbol between key and value of the property in string: " + propertyString));
            }
        }
    }

    /**
     * Set device client configuration used for SAS token validation.
     * @param deviceConfig is the device client configuration to be set
     * @throws TransportException if device client configuration is null
     */
    protected void setDeviceClientConfig(DeviceClientConfig deviceConfig) throws TransportException
    {
        if (deviceConfig == null)
        {
            //Codes_SRS_Mqtt_99_50: [If deviceConfig is null, the function shall throw a TransportException]
            throw new TransportException(new IllegalArgumentException("DeviceClientConfig is null"));
        }

        this.deviceClientConfig = deviceConfig; // set device client config object
    }
}