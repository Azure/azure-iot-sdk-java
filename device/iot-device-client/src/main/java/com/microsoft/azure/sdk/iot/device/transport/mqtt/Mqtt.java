// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubListener;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.ReconnectionNotifier;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.exceptions.PahoExceptionTranslator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.paho.client.mqttv3.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Queue;

@Slf4j
abstract public class Mqtt implements MqttCallback
{
    private static final int CONNECTION_TIMEOUT = 60 * 1000;
    private static final int DISCONNECTION_TIMEOUT = 60 * 1000;

    //mqtt connection options
    private static final int QOS = 1;
    private static final int MAX_SUBSCRIBE_ACK_WAIT_TIME = 15 * 1000;

    // relatively arbitrary, but only because Paho doesn't have any particular recommendations here. Just a high enough
    // value that users who are building a gateway type solution don't find this value to be a bottleneck.
    static final int MAX_IN_FLIGHT_COUNT = 65000;

    private MqttAsyncClient mqttAsyncClient;
    private final MqttConnectOptions connectOptions;
    private final MqttMessageListener messageListener;
    private final Map<Integer, Message> unacknowledgedSentMessages;

    final Object receivedMessagesLock; // lock for making operations on the receivedMessagesQueue atomic
    final Queue<Pair<String, byte[]>> receivedMessages;

    /* Each property is separated by & and all system properties start with an encoded $ (except for iothub-ack) */
    final static char MESSAGE_PROPERTY_SEPARATOR = '&';
    private final static String MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_ENCODED = "%24";
    private final static char MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_DECODED = '$';
    final static char MESSAGE_PROPERTY_KEY_VALUE_SEPARATOR = '=';
    private final static int PROPERTY_KEY_INDEX = 0;
    private final static int PROPERTY_VALUE_INDEX = 1;

    /* The system property keys expected in a message */
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
    final static String CREATION_TIME_UTC = MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_DECODED + ".ctime";
    final static String MQTT_SECURITY_INTERFACE_ID = MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_DECODED + ".ifid";

    private final static String IOTHUB_ACK = "iothub-ack";

    private final static String INPUTS_PATH_STRING = "inputs";
    private final static String MODULES_PATH_STRING = "modules";

    private IotHubListener listener;
    private String connectionId;
    private final String deviceId;

    private final Object stateLock; // lock for preventing simultaneous connects and subscribe requests
    private final Object unacknowledgedSentMessagesLock; // lock for making operations on unacknowledgedSentMessages atomic

    /**
     * Constructor to instantiate mqtt broker connection.
     * @param messageListener the listener to be called back upon a message arriving
     * @param deviceId the Id of the device this connection belongs to
     */
    Mqtt(
        MqttMessageListener messageListener,
        String deviceId,
        MqttConnectOptions connectOptions,
        Map<Integer, Message> unacknowledgedSentMessages,
        Queue<Pair<String, byte[]>> receivedMessages)
    {
        this.deviceId = deviceId;
        this.receivedMessages = receivedMessages;
        this.stateLock = new Object();
        this.receivedMessagesLock = new Object();
        this.unacknowledgedSentMessagesLock = new Object();
        this.messageListener = messageListener;
        this.connectOptions = connectOptions;
        this.unacknowledgedSentMessages = unacknowledgedSentMessages;
    }

    void updatePassword(char[] newPassword)
    {
        this.connectOptions.setPassword(newPassword);
    }

    /**
     * Method to connect to mqtt broker connection.
     *
     * @throws TransportException if failed to establish the mqtt connection.
     */
    void connect() throws TransportException
    {
        synchronized (this.stateLock)
        {
            try
            {
                if (!this.mqttAsyncClient.isConnected())
                {
                    log.debug("Sending MQTT CONNECT packet...");
                    IMqttToken connectToken = this.mqttAsyncClient.connect(this.connectOptions);
                    connectToken.waitForCompletion(CONNECTION_TIMEOUT);
                    log.debug("Sent MQTT CONNECT packet was acknowledged");
                }
            }
            catch (MqttException e)
            {
                log.warn("Exception encountered while sending MQTT CONNECT packet", e);

                this.disconnect();
                throw PahoExceptionTranslator.convertToMqttException(e, "Unable to establish MQTT connection");
            }
        }
    }

    /**
     * Method to disconnect to mqtt broker connection.
     *
     * @throws TransportException if failed to ends the mqtt connection.
     */
    void disconnect() throws TransportException
    {
        try
        {
            if (this.mqttAsyncClient.isConnected())
            {
                log.debug("Sending MQTT DISCONNECT packet");
                IMqttToken disconnectToken = this.mqttAsyncClient.disconnect();

                if (disconnectToken != null)
                {
                    disconnectToken.waitForCompletion(DISCONNECTION_TIMEOUT);
                }

                log.debug("Sent MQTT DISCONNECT packet was acknowledged");
                this.mqttAsyncClient.close();
            }
        }
        catch (MqttException e)
        {
            log.warn("Exception encountered while sending MQTT DISCONNECT packet", e);
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
    void publish(String publishTopic, Message message) throws TransportException
    {
        try
        {
            if (!this.mqttAsyncClient.isConnected())
            {
                TransportException transportException = new TransportException("Cannot publish when mqtt client is disconnected");
                transportException.setRetryable(true);
                throw transportException;
            }

            if (message == null || publishTopic == null || publishTopic.length() == 0 || message.getBytes() == null)
            {
                throw new IllegalArgumentException("Cannot publish on null or empty publish topic");
            }

            byte[] payload = message.getBytes();

            while (this.mqttAsyncClient.getPendingDeliveryTokens().length >= MAX_IN_FLIGHT_COUNT)
            {
                //noinspection BusyWait
                Thread.sleep(10);

                if (!this.mqttAsyncClient.isConnected())
                {
                    TransportException transportException = new TransportException("Cannot publish when mqtt client is holding 10 tokens and is disconnected");
                    transportException.setRetryable(true);
                    throw transportException;
                }
            }

            MqttMessage mqttMessage = (payload.length == 0) ? new MqttMessage() : new MqttMessage(payload);

            mqttMessage.setQos(QOS);

            synchronized (this.unacknowledgedSentMessagesLock)
            {
                log.trace("Publishing message ({}) to MQTT topic {}", message, publishTopic);
                IMqttDeliveryToken publishToken = this.mqttAsyncClient.publish(publishTopic, mqttMessage);
                unacknowledgedSentMessages.put(publishToken.getMessageId(), message);
                log.trace("Message published to MQTT topic {}. Mqtt message id {} added to list of messages to wait for acknowledgement ({})", publishTopic, publishToken.getMessageId(), message);
            }
        }
        catch (MqttException e)
        {
            log.warn("Message could not be published to MQTT topic {} ({})", publishTopic, message, e);
            throw PahoExceptionTranslator.convertToMqttException(e, "Unable to publish message on topic : " + publishTopic);
        }
        catch (InterruptedException e)
        {
            throw new TransportException("Interrupted, Unable to publish message on topic : " + publishTopic, e);
        }
    }

    /**
     * Method to subscribe to mqtt broker connection.
     *
     * @param topic the topic to subscribe on mqtt broker connection.
     * @throws TransportException if failed to subscribe the mqtt topic.
     * @throws IllegalArgumentException if topic is null
     */
    void subscribe(String topic) throws TransportException
    {
        synchronized (this.stateLock)
        {
            try
            {
                if (topic == null)
                {
                    throw new IllegalArgumentException("Topic cannot be null");
                }
                else if (!this.mqttAsyncClient.isConnected())
                {

                    TransportException transportException = new TransportException("Cannot subscribe when mqtt client is disconnected");
                    transportException.setRetryable(true);
                    throw transportException;
                }

                log.debug("Sending MQTT SUBSCRIBE packet for topic {}", topic);

                IMqttToken subToken = this.mqttAsyncClient.subscribe(topic, QOS);

                subToken.waitForCompletion(MAX_SUBSCRIBE_ACK_WAIT_TIME);
                log.debug("Sent MQTT SUBSCRIBE packet for topic {} was acknowledged", topic);

            }
            catch (MqttException e)
            {
                log.warn("Encountered exception while sending MQTT SUBSCRIBE packet for topic {}", topic, e);

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
        synchronized (this.receivedMessagesLock)
        {
            Pair<String, byte[]> messagePair = this.receivedMessages.peek();
            if (messagePair != null)
            {
                String topic = messagePair.getKey();
                if (topic != null)
                {
                    byte[] data = messagePair.getValue();
                    if (data != null)
                    {
                        // remove this message from the queue as this is the correct handler
                        this.receivedMessages.poll();

                        return constructMessage(data, topic);
                    }
                    else
                    {
                        throw new TransportException("Data cannot be null when topic is non-null");
                    }
                }
                else
                {
                    return null;
                }
            }

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
        TransportException ex = null;

        log.warn("Mqtt connection lost", throwable);

        try
        {
            this.disconnect();
        }
        catch (TransportException e)
        {
            ex = e;
        }

        if (this.listener != null)
        {
            if (ex == null)
            {
                if (throwable instanceof MqttException)
                {
                    throwable = PahoExceptionTranslator.convertToMqttException((MqttException) throwable, "Mqtt connection lost");
                    log.trace("Mqtt connection loss interpreted into transport exception", throwable);
                }
                else
                {
                    throwable = new TransportException(throwable);
                }
            }
            else
            {
                throwable = ex;
            }

            ReconnectionNotifier.notifyDisconnectAsync(throwable, this.listener, this.connectionId);
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
        log.trace("Mqtt message arrived on topic {} with mqtt message id {}", topic, mqttMessage.getId());
        this.receivedMessages.add(new MutablePair<>(topic, mqttMessage.getPayload()));

        if (this.messageListener != null)
        {
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
        Message deliveredMessage = null;
        log.trace("Mqtt message with message id {} was acknowledge by service", iMqttDeliveryToken.getMessageId());
        synchronized (this.unacknowledgedSentMessagesLock)
        {
            if (unacknowledgedSentMessages.containsKey(iMqttDeliveryToken.getMessageId()))
            {
                log.trace("Mqtt message with message id {} that was acknowledge by service was sent by this client", iMqttDeliveryToken.getMessageId());
                deliveredMessage = unacknowledgedSentMessages.remove(iMqttDeliveryToken.getMessageId());
            }
            else
            {
                log.warn("Mqtt message with message id {} that was acknowledge by service was not sent by this client, will be ignored", iMqttDeliveryToken.getMessageId());
            }
        }

        if (deliveredMessage instanceof IotHubTransportMessage)
        {
            DeviceOperations deviceOperation = ((IotHubTransportMessage) deliveredMessage).getDeviceOperationType();
            if (deviceOperation == DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST
                    || deviceOperation == DeviceOperations.DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST
                    || deviceOperation == DeviceOperations.DEVICE_OPERATION_TWIN_UNSUBSCRIBE_DESIRED_PROPERTIES_REQUEST)
            {
                // no need to alert the IotHubTransport layer about these messages as they are not tracked in the inProgressQueue
                return;
            }
        }

        if (this.listener != null)
        {
            this.listener.onMessageSent(deliveredMessage, this.deviceId, null);
        }
        else
        {
            log.warn("Message sent, but no listener set");
        }
    }

    /**
     * Send ack for the provided message.
     * @param messageId The message id to send the ack for
     * @throws TransportException If the ack fails to be sent
     */
    void sendMessageAcknowledgement(int messageId) throws TransportException
    {
        log.trace("Sending mqtt ack for received message with mqtt message id {}", messageId);
        try
        {
            this.mqttAsyncClient.messageArrivedComplete(messageId, QOS);
        }
        catch (MqttException e)
        {
            throw PahoExceptionTranslator.convertToMqttException(e, "Error sending message ack");
        }
    }

    /**
     * Converts the provided data and topic string into an instance of Message
     * @param data the payload from the topic
     * @param topic the topic string for this message
     * @return a new instance of Message containing the payload and all the properties in the topic string
     */
    private IotHubTransportMessage constructMessage(byte[] data, String topic)
    {
        IotHubTransportMessage message = new IotHubTransportMessage(data, MessageType.DEVICE_TELEMETRY);

        int propertiesStringStartingIndex = topic.indexOf(MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_ENCODED);
        if (propertiesStringStartingIndex != -1)
        {
            String propertiesString = topic.substring(propertiesStringStartingIndex);

            assignPropertiesToMessage(message, propertiesString);

            String routeString = topic.substring(0, propertiesStringStartingIndex);
            String[] routeComponents = routeString.split("/");

            if (routeComponents.length > 2 && routeComponents[2].equals(MODULES_PATH_STRING))
            {
                message.setConnectionModuleId(routeComponents[3]);
            }

            if (routeComponents.length > 4 && routeComponents[4].equals(INPUTS_PATH_STRING))
            {
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
        for (String propertyString : propertiesString.split(String.valueOf(MESSAGE_PROPERTY_SEPARATOR)))
        {
            if (propertyString.contains("="))
            {
                //Expected format is <key>=<value> where both key and value may be encoded
                String key = propertyString.split("=")[PROPERTY_KEY_INDEX];
                String value = propertyString.split("=")[PROPERTY_VALUE_INDEX];
                try
                {
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
                    case IOTHUB_ACK:
                    case USER_ID:
                    case ABSOLUTE_EXPIRY_TIME:
                        //do nothing
                        break;
                    case MESSAGE_ID:
                        message.setMessageId(value);
                        break;
                    case CORRELATION_ID:
                        message.setCorrelationId(value);
                        break;
                    case OUTPUT_NAME:
                        message.setOutputName(value);
                        break;
                    case CONTENT_ENCODING:
                        message.setContentEncoding(value);
                        break;
                    case CONTENT_TYPE:
                        message.setContentTypeFinal(value);
                        break;
                    default:
                        message.setProperty(key, value);
                }
            }
            else
            {
                throw new IllegalArgumentException("Unexpected property string provided. Expected '=' symbol between key and value of the property in string: " + propertyString);
            }
        }
    }

    void setListener(IotHubListener listener)
    {
        this.listener = listener;
    }

    void setConnectionId(String connectionId)
    {
        this.connectionId = connectionId;
    }

    void setMqttAsyncClient(MqttAsyncClient mqttAsyncClient)
    {
        // should never be set to null
        // mqttAsyncClients are single use, so this setter is used when the MqttIotHubConnection layer needs to open a new connection
        this.mqttAsyncClient = mqttAsyncClient;
    }
}
