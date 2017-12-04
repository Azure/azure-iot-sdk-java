// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasToken;
import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.paho.client.mqttv3.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.concurrent.ConcurrentLinkedQueue;

abstract public class Mqtt implements MqttCallback
{
    private MqttConnection mqttConnection;
    private DeviceClientConfig deviceClientConfig = null;
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

    /**
     * Constructor to instantiate mqtt broker connection.
     * @param mqttConnection the connection to use
     */
    public Mqtt(MqttConnection mqttConnection) throws IllegalArgumentException
    {
        if (mqttConnection == null)
        {
            //Codes_SRS_Mqtt_25_002: [The constructor shall throw InvalidParameter Exception if mqttConnection is null .]
            throw new IllegalArgumentException("Mqtt connection info cannot be null");
        }

        //Codes_SRS_Mqtt_25_003: [The constructor shall retrieve lock, queue from the provided connection information and save the connection.]
        this.mqttConnection = mqttConnection;
        this.allReceivedMessages = mqttConnection.getAllReceivedMessages();
        this.mqttLock = mqttConnection.getMqttLock();
        this.userSpecifiedSASTokenExpiredOnRetry = false;
    }

    /**
     * Method to connect to mqtt broker connection.
     *
     * @throws IOException if failed to establish the mqtt connection.
     */
    protected void connect() throws IOException
    {
        synchronized (this.mqttLock)
        {
            try
            {
                if (this.mqttConnection == null)
                {
                    /*
                    ** Codes_SRS_Mqtt_25_006: [**If the inner class MqttConnectionInfo has not been instantiated then the function shall throw IOException.**]**
                     */
                    throw new IOException("Mqtt client should be initialised at least once before using it");
                }

                /*
                **Codes_SRS_Mqtt_25_008: [**If the MQTT connection is already open, the function shall do nothing.**]**
                 */
                if (!this.mqttConnection.getMqttAsyncClient().isConnected())
                {
                    /*
                    **Codes_SRS_Mqtt_25_005: [**The function shall establish an MQTT connection with an IoT Hub using the provided host name, user name, device ID, and sas token.**]**
                     */
                    IMqttToken connectToken = this.mqttConnection.getMqttAsyncClient().connect(Mqtt.this.mqttConnection.getConnectionOptions());
                    connectToken.waitForCompletion();
                }
            }
            catch (MqttException e)
            {
                /*
                ** Codes_SRS_Mqtt_25_007: [**If an MQTT connection is unable to be established for any reason, the function shall throw an IOException.**]**
                 */
                throw new IOException("Unable to connect to service" + e.getCause(), e);
            }
        }

    }

    /**
     * Method to disconnect to mqtt broker connection.
     *
     * @throws IOException if failed to ends the mqtt connection.
     */
    protected void disconnect() throws IOException
    {
        try
        {
            /*
            **Codes_SRS_Mqtt_25_010: [**If the MQTT connection is closed, the function shall do nothing.**]**
            */
            if (this.mqttConnection.getMqttAsyncClient() != null && this.mqttConnection.getMqttAsyncClient().isConnected())
            {
                /*
                ** Codes_SRS_Mqtt_25_009: [**The function shall close the MQTT connection.**]**
                */
                IMqttToken disconnectToken = this.mqttConnection.getMqttAsyncClient().disconnect();
                disconnectToken.waitForCompletion();
            }
            this.mqttConnection.setMqttAsyncClient(null);
        }
        catch (MqttException e)
        {
            /*
            ** SRS_Mqtt_25_011: [**If an MQTT connection is unable to be closed for any reason, the function shall throw an IOException.**]**
            */
            throw new IOException("Unable to disconnect" + "because " + e.getMessage(), e);
        }
    }

    /**
     * Method to publish to mqtt broker connection.
     *
     * @param publishTopic the topic to publish on mqtt broker connection.
     * @param payload   the payload to publish on publishTopic of mqtt broker connection.
     * @throws IOException if failed to publish the mqtt topic.
     */
    protected void publish(String publishTopic, byte[] payload) throws IOException
    {
        synchronized (this.mqttLock)
        {
            try
            {
                if (this.mqttConnection == null)
                {
                    throw new InvalidParameterException();
                }

                if (this.userSpecifiedSASTokenExpiredOnRetry)
                {
                    /*
                    ** Codes_SRS_Mqtt_99_049: [**If the user supplied SAS token has expired, the function shall throw an IOException.**]**
                     */
                    throw new IOException("Cannot publish when user supplied SAS token has expired");
                }

                if (!this.mqttConnection.getMqttAsyncClient().isConnected())
                {
                    /*
                    ** Codes_SRS_Mqtt_25_012: [**If the MQTT connection is closed, the function shall throw an IOException.**]**
                     */
                    throw new IOException("Cannot publish when mqtt client is disconnected");
                }

                if (publishTopic == null || publishTopic.length() == 0 || payload == null)
                {
                    /*
                    **Codes_SRS_Mqtt_25_013: [**If the either publishTopic is null or empty or if payload is null, the function shall throw an IOException.**]**
                    */
                    throw new IOException("Cannot publish on null or empty publish topic");
                }

                while (this.mqttConnection.getMqttAsyncClient().getPendingDeliveryTokens().length >= MqttConnection.MAX_IN_FLIGHT_COUNT)
                {
                    /*
                    **Codes_SRS_Mqtt_25_048: [**publish shall check for pending publish tokens by calling getPendingDeliveryTokens.
                    * And if there are pending tokens publish shall sleep until the number of pending tokens are less than 10 as per paho limitations**]**
                    */
                    Thread.sleep(10);

                    if (!this.mqttConnection.getMqttAsyncClient().isConnected())
                    {
                    /*
                    ** Codes_SRS_Mqtt_25_012: [**If the MQTT connection is closed, the function shall throw an IOException.**]**
                     */
                        throw new IOException("Cannot publish when mqtt client is holding 10 tokens and  is disconnected");
                    }
                }

                MqttMessage mqttMessage = (payload.length == 0) ? new MqttMessage() : new MqttMessage(payload);

                mqttMessage.setQos(MqttConnection.QOS);

                /*
                **Codes_SRS_Mqtt_25_014: [**The function shall publish message payload on the publishTopic specified to the IoT Hub given in the configuration.**]**
                 */

                IMqttDeliveryToken publishToken = this.mqttConnection.getMqttAsyncClient().publish(publishTopic, mqttMessage);

            }
            catch (MqttException e)
            {
                /*
                **Codes_SRS_Mqtt_25_047: [**If the Mqtt Client Async throws MqttException, the function shall throw an IOException with the message.**]**
                 */
                throw new IOException("Unable to publish message on topic : " + publishTopic + " because " + e.getCause() + e.getMessage(), e);
            }
            catch (InterruptedException e)
            {
                throw new IOException("Interrupted, Unable to publish message on topic : " + publishTopic);
            }
            catch (Exception e)
            {
                throw new IOException("Unable to publish message on topic : " + publishTopic + " " + e.getCause() + e.getMessage(), e);
            }
        }
    }

    /**
     * Method to subscribe to mqtt broker connection.
     *
     * @param topic the topic to subscribe on mqtt broker connection.
     * @throws IOException if failed to subscribe the mqtt topic.
     */
    protected void subscribe(String topic) throws IOException
    {
        synchronized (this.mqttLock)
        {
            try
            {
                if (this.mqttConnection == null)
                {
                    throw new IOException("Mqtt client should be initialised atleast once before using it");
                }
                else if (topic == null)
                {
                    /*
                    **Codes_SRS_Mqtt_25_016: [**If the subscribeTopic is null or empty, the function shall throw an InvalidParameter Exception.**]**
                     */
                    throw new InvalidParameterException("Topic cannot be null");

                }
                else if (this.userSpecifiedSASTokenExpiredOnRetry)
                {
                    /*
                    ** Codes_SRS_Mqtt_99_049: [**If the user supplied SAS token has expired, the function shall throw an IOException.**]**
                     */
                    throw new IOException("Cannot subscribe when user supplied SAS token has expired");
                }
                else if (!this.mqttConnection.getMqttAsyncClient().isConnected())
                {
                    /*
                    **Codes_SRS_Mqtt_25_015: [**If the MQTT connection is closed, the function shall throw an IOexception with message.**]**
                     */
                    throw new IOException("Cannot suscribe when mqtt client is disconnected");
                }
                /*
                **Codes_SRS_Mqtt_25_017: [**The function shall subscribe to subscribeTopic specified to the IoT Hub given in the configuration.**]**
                 */
                IMqttToken subToken = this.mqttConnection.getMqttAsyncClient().subscribe(topic, MqttConnection.QOS);

                subToken.waitForCompletion(MqttConnection.MAX_WAIT_TIME);
            }
            catch (MqttException e)
            {
                /*
                **Codes_SRS_Mqtt_25_048: [**If the Mqtt Client Async throws MqttException for any reason, the function shall throw an IOException with the message.**]**
                 */
                throw new IOException("Unable to subscribe to topic :" + topic + " because " + e.getCause() + e.getMessage(), e);
            }
        }
    }

    protected boolean isConnected()
    {
        if (this.mqttConnection == null || this.mqttConnection.getMqttAsyncClient() == null)
        {
            throw new InvalidParameterException("Mqtt client should be initialised atleast once before using it");
        }
        return this.mqttConnection.getMqttAsyncClient().isConnected();
    }

    /**
     * Method to receive messages on mqtt broker connection.
     *
     * @return a received message. It can be {@code null}
     * @throws IOException if failed to receive mqtt message.
     */
    public Message receive() throws IOException
    {
        synchronized (this.mqttLock)
        {
            if (this.mqttConnection == null)
            {
                throw new InvalidParameterException("Mqtt client should be initialised at least once before using it");
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
                        // Codes_SRS_Mqtt_34_025: [If the call to peekMessage returns null when topic is non-null then this method will throw IOException]
                        throw new IOException("Data cannot be null when topic is non-null");
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
        synchronized (this.mqttLock)
        {
            if (this.mqttConnection != null && this.mqttConnection.getMqttAsyncClient() != null)
            {
                int currentReconnectionAttempt = 0;
                while (!this.mqttConnection.getMqttAsyncClient().isConnected())
                {
                    System.out.println("Lost connection to the server. Reconnecting " + currentReconnectionAttempt + " time.");
                    try
                    {
                        currentReconnectionAttempt++;
                        if (this.deviceClientConfig.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
                        {
                            /*
                            **Codes_SRS_Mqtt_99_050: [**The function shall check if SAS token has already expired.**]**
                            */
                            if (!IotHubSasToken.isExpired(new String(this.mqttConnection.getConnectionOptions().getPassword())))
                            {
                                connect(); // Try to reconnect
                            }
                            else
                            {
                                /*
                                **Codes_SRS_Mqtt_99_050: [**The function shall check if SAS token has already expired.**]**
                                */
                                if (this.deviceClientConfig.getIotHubConnectionString().getSharedAccessKey() != null)
                                {
                                   /*
                                    **Codes_SRS_Mqtt_99_052: [**The function shall generate a new SAS token.**]**
                                    */
                                    String sasToken = this.deviceClientConfig.getSasTokenAuthentication().getRenewedSasToken();
                                    this.mqttConnection.getConnectionOptions().setPassword(sasToken.toCharArray());

                                    connect(); // Try to reconnect
                                }
                                else
                                {
                                    /*
                                    **Codes_SRS_Mqtt_99_053: [**The function shall set user supplied SAS token expiration flag to true .**]**
                                    */
                                    this.userSpecifiedSASTokenExpiredOnRetry  = true;
                                    return; // no reconnect exit now
                                }
                            }
                        }
                        else if (this.deviceClientConfig.getAuthenticationType() == DeviceClientConfig.AuthType.X509_CERTIFICATE)
                        {
                            connect(); // Try to reconnect
                        }
                    }
                    catch (IOException e)
                    {
                        try
                        {
                            /*
                            Codes_SRS_Mqtt_25_027: [**The function shall attempt to reconnect to the IoTHub in a loop with exponential backoff until it succeeds**]**
                             */
                            /*
                            **Codes_SRS_Mqtt_25_028: [**The maximum wait interval until a reconnect is attempted shall be 60 seconds.**]**
                             */
                            Thread.sleep(TransportUtils.generateSleepInterval(currentReconnectionAttempt));
                        }
                        catch (InterruptedException ie)
                        {
                            // do nothing and continue trying...
                        }
                    }
                }
            }
            else
            {
                System.out.println("Initialise before using this..");
            }
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
        /*
        **Codes_SRS_Mqtt_25_030: [**The payload of the message and the topic is added to the received messages queue .**]**
         */
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

    public Pair<String, byte[]> peekMessage() throws IOException
    {
        if (allReceivedMessages == null)
        {
            // Codes_SRS_MQTTDEVICEMETHOD_34_034: [If allReceivedMessages queue is null then this method shall throw IOException.]
            throw new IOException("Queue cannot be null");
        }

        return allReceivedMessages.peek();
    }

    /**
     * Converts the provided data and topic string into an instance of Message
     * @param data the payload from the topic
     * @param topic the topic string for this message
     * @return a new instance of Message containing the payload and all the properties in the topic string
     * @throws IllegalArgumentException if the topic string has no system properties
     */
    private Message constructMessage(byte[] data, String topic) throws IllegalArgumentException
    {
        /*
        **Codes_SRS_Mqtt_25_024: [**This method shall construct new Message with the bytes obtained from parsePayload and return the message.**]**
        */
        Message message = new Message(data);

        int propertiesStringStartingIndex = topic.indexOf(MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_ENCODED);
        if (propertiesStringStartingIndex != -1)
        {
            String propertiesString = topic.substring(propertiesStringStartingIndex);

            /*
            **Codes_SRS_Mqtt_34_041: [**This method shall call assignPropertiesToMessage so that all properties from the topic string can be assigned to the message**]**
            */
            assignPropertiesToMessage(message, propertiesString);
        }

        return message;
    }

    /**
     * Takes propertiesString and parses it for all the properties it holds and then assigns them to the provided message
     * @param propertiesString the string to parse containing all the properties
     * @param message the message to add the parsed properties to
     * @throws IllegalArgumentException if a property's key and value are not separated by the '=' symbol
     * @throws NumberFormatException if the property for expiry time is present, but the value cannot be parsed as a Long
     * */
    private void assignPropertiesToMessage(Message message, String propertiesString) throws IllegalArgumentException, NumberFormatException
    {
        /*
        **Codes_SRS_Mqtt_34_054: [**A message may have 0 to many custom properties**]**
        */
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
                    /*
                    **Codes_SRS_Mqtt_34_053: [**A property's key and value may include unusual characters such as &, %, $**]**
                    */
                    key = URLDecoder.decode(key, StandardCharsets.UTF_8.name());
                    value = URLDecoder.decode(value, StandardCharsets.UTF_8.name());
                }
                catch (UnsupportedEncodingException e)
                {
                    // should never happen, since the encoding is hard-coded.
                    throw new IllegalStateException(e);
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
                /*
                 **Codes_SRS_Mqtt_34_051: [**If a topic string's property's key and value are not separated by the '=' symbol, an IllegalArgumentException shall be thrown**]**
                 */
                throw new IllegalArgumentException("Unexpected property string provided. Expected '=' symbol between key and value of the property in string: " + propertyString);
            }
        }
    }

    /**
     * Set device client configuration used for SAS token validation.
     * @param deviceConfig is the device client configuration to be set
     * @throws IllegalArgumentException if device client configuration is null
     */
    protected void setDeviceClientConfig(DeviceClientConfig deviceConfig) throws IllegalArgumentException
    {
        if (deviceConfig == null)
        {
          /*
          ** Codes_SRS_Mqtt_99_50: [**If deviceConfig is null, the function shall throw an IllegalArgumentException**]**
          */
            throw new IllegalArgumentException("DeviceClientConfig is null");
        }

        this.deviceClientConfig = deviceConfig; // set device client config object
    }
}