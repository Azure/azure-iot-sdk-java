// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubListener;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.exceptions.PahoExceptionTranslator;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.paho.client.mqttv3.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

abstract public class Mqtt implements MqttCallback
{
    private static final int CONNECTION_TIMEOUT = 60 * 1000;
    private static final int DISCONNECTION_TIMEOUT = 60 * 1000;

    private MqttConnection mqttConnection;
    private MqttMessageListener messageListener;
    ConcurrentLinkedQueue<Pair<String, byte[]>> allReceivedMessages;
    Object mqttLock;
    Object publishLock;

    private static Map<Integer, Message> unacknowledgedSentMessages = new ConcurrentHashMap<>();

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
    final static String OUTPUT_NAME = MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_DECODED + ".on";
    final static String CONNECTION_DEVICE_ID = MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_DECODED + ".cdid";
    final static String CONNECTION_MODULE_ID = MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_DECODED + ".cmid";
    final static String CONTENT_TYPE = MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_DECODED + ".ct";
    final static String CONTENT_ENCODING = MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_DECODED + ".ce";

    private final static String IOTHUB_ACK = "iothub-ack";

    private final static String INPUTS_PATH_STRING = "inputs";
    private final static String MODULES_PATH_STRING = "modules";

    private IotHubListener listener;
    private String connectionId;

    /**
     * Constructor to instantiate mqtt broker connection.
     * @param mqttConnection the connection to use
     * @param listener the listener to be called back upon connection established/lost and upon a message being delivered
     * @param messageListener the listener to be called back upon a message arriving
     * @param connectionId the id of the connection
     * @throws IllegalArgumentException if the provided mqttConnection is null
     */
    public Mqtt(MqttConnection mqttConnection, IotHubListener listener, MqttMessageListener messageListener, String connectionId) throws IllegalArgumentException
    {
        if (mqttConnection == null)
        {
            //Codes_SRS_Mqtt_25_002: [The constructor shall throw an IllegalArgumentException if mqttConnection is null.]
            throw new IllegalArgumentException("Mqtt connection info cannot be null");
        }

        //Codes_SRS_Mqtt_25_003: [The constructor shall retrieve lock, queue from the provided connection information and save the connection.]
        this.mqttConnection = mqttConnection;
        this.allReceivedMessages = mqttConnection.getAllReceivedMessages();
        this.mqttLock = mqttConnection.getMqttLock();
        this.publishLock = new Object();
        this.userSpecifiedSASTokenExpiredOnRetry = false;
        this.listener = listener;
        this.messageListener = messageListener;
        this.connectionId = connectionId;
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
                    connectToken.waitForCompletion(CONNECTION_TIMEOUT);
                }
            }
            catch (MqttException e)
            {
                //Codes_SRS_Mqtt_34_044: [If an MqttException is encountered while connecting, this function shall throw the associated ProtocolException.]
                throw PahoExceptionTranslator.convertToMqttException(e, "Unable to establish MQTT connection");
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
        try
        {
            if (this.mqttConnection.isConnected())
            {
                //Codes_SRS_Mqtt_34_055: [If an MQTT connection is connected, the function shall disconnect that connection.]
                IMqttToken disconnectToken = this.mqttConnection.disconnect();

                if (disconnectToken != null)
                {
                    disconnectToken.waitForCompletion(DISCONNECTION_TIMEOUT);
                }
            }

            //Codes_SRS_Mqtt_25_009: [The function shall close the MQTT client.]
            this.mqttConnection.close();
            this.mqttConnection.setMqttAsyncClient(null);
        }
        catch (MqttException e)
        {
            //Codes_SRS_Mqtt_25_011: [If an MQTT connection is unable to be closed for any reason, the function shall throw a TransportException.]
            throw PahoExceptionTranslator.convertToMqttException(e, "Unable to disconnect");
        }
    }

    /**
     * Method to publish to mqtt broker connection.
     *
     * @param publishTopic the topic to publish on mqtt broker connection.
     * @param message the message to publish.
     * @throws TransportException if sas token has expired, if connection hasn't been established yet, or if Paho throws
     * for any other reason
     */
    protected void publish(String publishTopic, Message message) throws TransportException
    {
        synchronized (this.mqttLock)
        {
            try
            {
                if (this.mqttConnection.getMqttAsyncClient() == null)
                {
                    TransportException transportException = new TransportException("Need to open first!");
                    transportException.setRetryable(true);
                    throw transportException;
                }

                if (this.userSpecifiedSASTokenExpiredOnRetry)
                {
                    //Codes_SRS_Mqtt_99_049: [If the user supplied SAS token has expired, the function shall throw a TransportException.]
                    throw new TransportException("Cannot publish when user supplied SAS token has expired");
                }

                if (!this.mqttConnection.getMqttAsyncClient().isConnected())
                {
                    //Codes_SRS_Mqtt_25_012: [If the MQTT connection is closed, the function shall throw a TransportException.]
                    TransportException transportException = new TransportException("Cannot publish when mqtt client is disconnected");
                    transportException.setRetryable(true);
                    throw transportException;
                }

                if (message == null || publishTopic == null || publishTopic.length() == 0 || message.getBytes() == null)
                {
                    //Codes_SRS_Mqtt_25_013: [If the either publishTopic is null or empty or if payload is null, the function shall throw an IllegalArgumentException.]
                    throw new IllegalArgumentException("Cannot publish on null or empty publish topic");
                }

                byte[] payload = message.getBytes();

                while (this.mqttConnection.getMqttAsyncClient().getPendingDeliveryTokens().length >= MqttConnection.MAX_IN_FLIGHT_COUNT)
                {
                    //Codes_SRS_Mqtt_25_048: [publish shall check for pending publish tokens by calling getPendingDeliveryTokens. And if there are pending tokens publish shall sleep until the number of pending tokens are less than 10 as per paho limitations]
                    Thread.sleep(10);

                    if (this.mqttConnection.getMqttAsyncClient() == null)
                    {
                        TransportException transportException = new TransportException("Connection was lost while waiting for mqtt deliveries to finish");
                        transportException.setRetryable(true);
                        throw transportException;
                    }

                    if (!this.mqttConnection.getMqttAsyncClient().isConnected())
                    {
                        //Codes_SRS_Mqtt_25_012: [If the MQTT connection is closed, the function shall throw a ProtocolException.]
                        TransportException transportException = new TransportException("Cannot publish when mqtt client is holding 10 tokens and is disconnected");
                        transportException.setRetryable(true);
                        throw transportException;
                    }
                }

                MqttMessage mqttMessage = (payload.length == 0) ? new MqttMessage() : new MqttMessage(payload);

                mqttMessage.setQos(MqttConnection.QOS);

                synchronized (this.publishLock)
                {
                    //Codes_SRS_Mqtt_25_014: [The function shall publish message payload on the publishTopic specified to the IoT Hub given in the configuration.]
                    IMqttDeliveryToken publishToken = this.mqttConnection.getMqttAsyncClient().publish(publishTopic, mqttMessage);
                    this.unacknowledgedSentMessages.put(publishToken.getMessageId(), message);
                }
            }
            catch (MqttException e)
            {
                //Codes_SRS_Mqtt_25_047: [If the Mqtt Client Async throws MqttException, the function shall throw a ProtocolException with the message.]
                throw PahoExceptionTranslator.convertToMqttException(e, "Unable to publish message on topic : " + publishTopic);
            }
            catch (InterruptedException e)
            {
                throw new TransportException("Interrupted, Unable to publish message on topic : " + publishTopic, e);
            }
        }
    }

    /**
     * Method to subscribe to mqtt broker connection.
     *
     * @param topic the topic to subscribe on mqtt broker connection.
     * @throws TransportException if failed to subscribe the mqtt topic.
     * @throws IllegalArgumentException if topic is null
     */
    protected void subscribe(String topic) throws TransportException
    {
        synchronized (this.mqttLock)
        {
            try
            {
                if (topic == null)
                {
                    //Codes_SRS_Mqtt_25_016: [If the subscribeTopic is null or empty, the function shall throw an IllegalArgumentException.]
                    throw new IllegalArgumentException("Topic cannot be null");

                }
                else if (this.userSpecifiedSASTokenExpiredOnRetry)
                {
                    //Codes_SRS_Mqtt_99_049: [If the user supplied SAS token has expired, the function shall throw a TransportException.]
                    throw new TransportException("Cannot subscribe when user supplied SAS token has expired");
                }
                else if (!this.mqttConnection.getMqttAsyncClient().isConnected())
                {

                    //Codes_SRS_Mqtt_25_015: [If the MQTT connection is closed, the function shall throw a TransportException with message.]
                    TransportException transportException = new TransportException("Cannot subscribe when mqtt client is disconnected");
                    transportException.setRetryable(true);
                    throw transportException;
                }

                //Codes_SRS_Mqtt_25_017: [The function shall subscribe to subscribeTopic specified to the IoT Hub given in the configuration.]
                IMqttToken subToken = this.mqttConnection.getMqttAsyncClient().subscribe(topic, MqttConnection.QOS);

                subToken.waitForCompletion(MqttConnection.MAX_WAIT_TIME);
            }
            catch (MqttException e)
            {
                //Codes_SRS_Mqtt_25_048: [If the Mqtt Client Async throws MqttException for any reason, the function shall throw a ProtocolException with the message.]
                throw PahoExceptionTranslator.convertToMqttException(e, "Unable to subscribe to topic :" + topic);
            }
        }
    }

    /**
     * Method to receive messages on mqtt broker connection.
     *
     * @return a received message. It can be {@code null}
     * @throws TransportException if failed to receive mqtt message.
     */
    public IotHubTransportMessage receive() throws TransportException
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
                throwable = PahoExceptionTranslator.convertToMqttException((MqttException) throwable, "Mqtt connection lost");
            }
            else
            {
                throwable = new TransportException(throwable);
            }

            //Codes_SRS_Mqtt_34_045: [If this object has a saved listener, this function shall notify the listener that connection was lost.]
            this.listener.onConnectionLost(throwable, this.connectionId);
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

        if (this.messageListener != null)
        {
            //Codes_SRS_Mqtt_34_045: [If there is a saved listener, this function shall notify that listener that a message arrived.]
            this.messageListener.onMessageArrived(mqttMessage.getId());
        }
    }

    /**
     * Event fired when the message arrived on the MQTT broker.
     * @param iMqttDeliveryToken the MqttDeliveryToken for which the message was successfully sent.
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken)
    {
        synchronized (this.publishLock)
        {
            if (this.listener != null)
            {
                if (this.unacknowledgedSentMessages.containsKey(iMqttDeliveryToken.getMessageId()))
                {
                    Message deliveredMessage = this.unacknowledgedSentMessages.get(iMqttDeliveryToken.getMessageId());

                    if (deliveredMessage instanceof IotHubTransportMessage)
                    {
                        DeviceOperations deviceOperation = ((IotHubTransportMessage) deliveredMessage).getDeviceOperationType();
                        if (deviceOperation == DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST
                                || deviceOperation == DeviceOperations.DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST
                                || deviceOperation == DeviceOperations.DEVICE_OPERATION_TWIN_UNSUBSCRIBE_DESIRED_PROPERTIES_REQUEST)
                        {
                            //Codes_SRS_Mqtt_34_056: [If the acknowledged message is of type
                            // DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST, DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST,
                            // or DEVICE_OPERATION_TWIN_UNSUBSCRIBE_DESIRED_PROPERTIES_REQUEST, this function shall not notify the saved
                            // listener that the message was sent.]
                            //no need to alert the IotHubTransport layer about these messages as they are not tracked in the inProgressQueue
                            return;
                        }
                    }

                    //Codes_SRS_Mqtt_34_042: [If this object has a saved listener, that listener shall be notified of the successfully delivered message.]
                    this.listener.onMessageSent(this.unacknowledgedSentMessages.get(iMqttDeliveryToken.getMessageId()), null);
                }
            }
        }
    }

    public Pair<String, byte[]> peekMessage()
    {
        return this.allReceivedMessages.peek();
    }

    /**
     * Attempts to send ack for the provided message. If the message does not have a saved messageId in this layer,
     * this function shall return false.
     * @param messageId The message id to send the ack for
     * @return true if the ack is sent successfully or false if the message isn't tied to this mqtt client
     * @throws TransportException if an exception occurs when sending the ack
     */
    protected boolean sendMessageAcknowledgement(int messageId) throws TransportException
    {
        //Codes_SRS_Mqtt_34_043: [This function shall invoke the saved mqttConnection object to send the message acknowledgement for the provided messageId and return that result.]
        return this.mqttConnection.sendMessageAcknowledgement(messageId);
    }

    /**
     * Converts the provided data and topic string into an instance of Message
     * @param data the payload from the topic
     * @param topic the topic string for this message
     * @return a new instance of Message containing the payload and all the properties in the topic string
     */
    private IotHubTransportMessage constructMessage(byte[] data, String topic)
    {
        //Codes_SRS_Mqtt_25_024: [This method shall construct new Message with the bytes obtained from parsePayload and return the message.]
        IotHubTransportMessage message = new IotHubTransportMessage(data, MessageType.DEVICE_TELEMETRY);

        int propertiesStringStartingIndex = topic.indexOf(MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_ENCODED);
        if (propertiesStringStartingIndex != -1)
        {
            String propertiesString = topic.substring(propertiesStringStartingIndex);

            //Codes_SRS_Mqtt_34_041: [This method shall call assignPropertiesToMessage so that all properties from the topic string can be assigned to the message]
            assignPropertiesToMessage(message, propertiesString);

            String routeString = topic.substring(0, propertiesStringStartingIndex);
            String[] routeComponents = routeString.split("/");

            if (routeComponents.length > 2 && routeComponents[2].equals(MODULES_PATH_STRING))
            {
                //Codes_SRS_Mqtt_34_051: [This function shall extract the moduleId from the topic if the topic string fits the following convention: 'devices/<deviceId>/modules/<moduleId>']
                message.setConnectionModuleId(routeComponents[3]);
            }

            if (routeComponents.length > 4 && routeComponents[4].equals(INPUTS_PATH_STRING))
            {
                //Codes_SRS_Mqtt_34_050: [This function shall extract the inputName from the topic if the topic string fits the following convention: 'devices/<deviceId>/modules/<moduleId>/inputs/<inputName>']
                message.setInputName(routeComponents[5]);
            }
        }

        return message;
    }

    /**
     * Takes propertiesString and parses it for all the properties it holds and then assigns them to the provided message
     * @param propertiesString the string to parse containing all the properties
     * @param message the message to add the parsed properties to
     * @throws IllegalArgumentException if a property's key and value are not separated by the '=' symbol
     * @throws IllegalStateException if the property for expiry time is present, but the value cannot be parsed as a Long
     * */
    private void assignPropertiesToMessage(Message message, String propertiesString) throws IllegalStateException, IllegalArgumentException
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
                    throw new IllegalStateException(e);
                }

                //Some properties are reserved system properties and must be saved in the message differently
                //Codes_SRS_Mqtt_34_057: [This function shall parse the messageId, correlationId, outputname, content encoding and content type from the provided property string]
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
                    case OUTPUT_NAME:
                        message.setOutputName(value);
                        break;
                    case CONTENT_ENCODING:
                        message.setContentEncoding(value);
                        break;
                    case CONTENT_TYPE:
                        message.setContentType(value);
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
                //Codes_SRS_Mqtt_34_051: [If a topic string's property's key and value are not separated by the '=' symbol, an IllegalArgumentException shall be thrown]
                throw new IllegalArgumentException("Unexpected property string provided. Expected '=' symbol between key and value of the property in string: " + propertyString);
            }
        }
    }
}
