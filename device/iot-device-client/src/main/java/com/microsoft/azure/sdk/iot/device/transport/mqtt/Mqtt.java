// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.security.*;
import java.util.concurrent.ConcurrentSkipListMap;

abstract public class Mqtt implements MqttCallback
{

    abstract String parseTopic() throws IOException;
    abstract byte[] parsePayload(String topic) throws IOException;

     /*
     Variables which apply to all the concrete classes as well as to Mqtt and are to be instantiated only once
     in lifetime.
     */
    private static MqttConnectionInfo info ;
    protected static ConcurrentSkipListMap<String, byte[]> allReceivedMessages;
    private static Object MQTT_LOCK;

    /*
      Inner class which holds the basic information related to Mqtt Client Async.
     */
    protected class MqttConnectionInfo
    {
        protected MqttAsyncClient mqttAsyncClient = null;
        private MqttConnectOptions connectionOptions = null;

        //mqtt connection options
        private static final int keepAliveInterval = 20;
        private static final int mqttVersion = 4;
        private static final boolean setCleanSession = false;
        private static final int qos = 1;
        private static final int MAX_WAIT_TIME = 1000;

        // paho mqtt only supports 10 messages in flight at the same time
        private static final int maxInFlightCount = 10;


        MqttConnectionInfo(String serverURI, String clientId, String userName, String password, IotHubSSLContext iotHubSSLContext) throws IOException
        {
            try
            {

                mqttAsyncClient = new MqttAsyncClient(serverURI, clientId, new MemoryPersistence());
                mqttAsyncClient.setCallback(Mqtt.this);
                connectionOptions = new MqttConnectOptions();
                this.updateConnectionOptions(userName, password, iotHubSSLContext);
            }
            catch (MqttException e)
            {
                mqttAsyncClient = null;
                connectionOptions = null;
                throw new IOException("Error initializing MQTT connection:" + e.getMessage());
            }
            catch (Exception e)
            {
                mqttAsyncClient = null;
                connectionOptions = null;
                throw new IOException("Error initializing MQTT connection:" + e.getMessage());
            }

        }

        /**
         * Generates the connection options for the mqtt broker connection.
         *
         * @param userName the user name for the mqtt broker connection.
         * @param userPassword the user password for the mqtt broker connection.
         */

        private void updateConnectionOptions(String userName, String userPassword, IotHubSSLContext iotHubSSLContext)
        {
            this.connectionOptions.setKeepAliveInterval(keepAliveInterval);
            this.connectionOptions.setCleanSession(setCleanSession);
            this.connectionOptions.setMqttVersion(mqttVersion);
            this.connectionOptions.setUserName(userName);
            this.connectionOptions.setPassword(userPassword.toCharArray());
            this.connectionOptions.setSocketFactory(iotHubSSLContext.getIotHubSSlContext().getSocketFactory());
        }

    }

    private void setMqttInfo(String serverURI, String clientId, String userName, String password, IotHubSSLContext iotHubSSLContext) throws IOException
    {
        /*
        **Codes_SRS_Mqtt_25_003: [**The constructor shall use the configuration to instantiate an instance of the inner class MqttConnectionInfo if not already created.**]**
         */
        /*
        ** Codes_SRS_Mqtt_25_004: [**If an instance of the inner class MqttConnectionInfo is already created than it shall return doing nothing.**]**
         */
        if (Mqtt.info == null)
        {
            Mqtt.info = new MqttConnectionInfo(serverURI, clientId, userName, password, iotHubSSLContext);
            Mqtt.allReceivedMessages = new ConcurrentSkipListMap<String, byte[]>();
            Mqtt.MQTT_LOCK = new Object();
        }

    }


    /**
     * Constructor to instantiate mqtt broker connection.
     *
     */

    public Mqtt() throws IOException
    {
        /*
        ** Codes_SRS_Mqtt_25_001: [**The constructor shall instantiate MQTT lock for using base class.**]**
        */
        if (Mqtt.MQTT_LOCK == null)
        {
            Mqtt.MQTT_LOCK = new Object();
        }

    }

    /**
     * Constructor to instantiate mqtt broker connection.
     *
     * @param serverURI the server uri associated with this mqtt broker connection
     * @param clientId the client Id associated with this mqtt broker connection.
     * @param userName the user name for the mqtt broker connection.
     * @param userPassword the user password for the mqtt broker connection.
     */

    public Mqtt(String serverURI, String clientId, String userName, String userPassword, IotHubSSLContext iotHubSSLContext) throws IOException
    {
        /*
         ** Codes_SRS_Mqtt_25_002: [**The constructor shall throw InvalidParameter Exception if any of the parameters are null or empty .**]**
         */
        if (serverURI == null || clientId == null || userName == null || userPassword == null || iotHubSSLContext == null)
        {
            throw new InvalidParameterException();
        }

        else if (serverURI.length() == 0 || clientId.length() == 0 || userName.length() == 0 || userPassword.length() == 0)
        {
            throw new InvalidParameterException();
        }

        try
        {
            /*
            **Codes_SRS_Mqtt_25_003: [**The constructor shall use the configuration to instantiate an instance of the inner class MqttConnectionInfo if not already created.**]**
             */
            setMqttInfo(serverURI, clientId, userName, userPassword, iotHubSSLContext);
        }
        catch (IOException e)
        {
            /*
            **Codes_SRS_Mqtt_25_045: [**The constructor throws IOException if MqttException is thrown and doesn't instantiate this instance.**]**
             */
            Mqtt.info = null;
            Mqtt.allReceivedMessages = null;
            Mqtt.MQTT_LOCK = null;
            throw new IOException(e.getMessage());
        }

    }

    /**
     * Method to restart mqtt broker connection.
     *
     */

    public void restartBaseMqtt()
    {
        /*
            As this is abstract class, if we ever want to restart application
            in the current scope to create a new instance of this base class,
            we have to unset all its static variables.
        */

        /*
        ** Codes_SRS_Mqtt_25_046: [**restartBaseMqtt shall unset all the static variables.**]**
         */
        Mqtt.MQTT_LOCK = null;
        Mqtt.allReceivedMessages = null;
        Mqtt.info = null;

    }

    /**
     * Method to connect to mqtt broker connection.
     *
     */

    protected void connect() throws IOException
    {
        synchronized (Mqtt.MQTT_LOCK)
        {
            try
            {
                if (Mqtt.info == null)
                {
                    /*
                    ** Codes_SRS_Mqtt_25_006: [**If the inner class MqttConnectionInfo has not been instantiated then the function shall throw IOException.**]**
                     */
                    throw new IOException("Mqtt client should be initialised atleast once before using it");
                }

                /*
                **Codes_SRS_Mqtt_25_008: [**If the MQTT connection is already open, the function shall do nothing.**]**
                 */
                if (!Mqtt.info.mqttAsyncClient.isConnected())
                {
                    /*
                    **Codes_SRS_Mqtt_25_005: [**The function shall establish an MQTT connection with an IoT Hub using the provided host name, user name, device ID, and sas token.**]**
                     */
                    IMqttToken connectToken = Mqtt.info.mqttAsyncClient.connect(Mqtt.info.connectionOptions);
                    connectToken.waitForCompletion();
                }
            }
            catch (MqttException e)
            {
                /*
                ** Codes_SRS_Mqtt_25_007: [**If an MQTT connection is unable to be established for any reason, the function shall throw an IOException.**]**
                 */
                throw new IOException("Unable to connect to service" + e.getMessage());
            }
        }

    }

    /**
     * Method to disconnect to mqtt broker connection.
     *
     */

    protected void disconnect() throws IOException
    {
        try
        {
            /*
            **Codes_SRS_Mqtt_25_010: [**If the MQTT connection is closed, the function shall do nothing.**]**
            */
            if (Mqtt.info.mqttAsyncClient.isConnected())
            {
                /*
                ** Codes_SRS_Mqtt_25_009: [**The function shall close the MQTT connection.**]**
                */
                IMqttToken disconnectToken = Mqtt.info.mqttAsyncClient.disconnect();
                disconnectToken.waitForCompletion();
            }
                Mqtt.info.mqttAsyncClient = null;
        }
        
        catch (MqttException e)
        {
            /*
            ** SRS_Mqtt_25_011: [**If an MQTT connection is unable to be closed for any reason, the function shall throw an IOException.**]**
            */
            throw new IOException("Unable to disconnect" + "because " + e.getMessage() );
        }
    }

    /**
     * Method to publish to mqtt broker connection.
     *
     * @param publishTopic the topic to publish on mqtt broker connection.
     * @param payload   the payload to publish on publishTopic of mqtt broker connection.
     */
    protected void publish(String publishTopic, byte[] payload) throws IOException
    {
        synchronized (Mqtt.MQTT_LOCK)
        {
            try
            {
                if (Mqtt.info == null)
                {
                    System.out.println("Mqtt client should be initialised atleast once before using it");
                    throw new InvalidParameterException();
                }

                if (!Mqtt.info.mqttAsyncClient.isConnected())
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

                while (Mqtt.info.mqttAsyncClient.getPendingDeliveryTokens().length >= Mqtt.info.maxInFlightCount)
                {
                    /*
                    **Codes_SRS_Mqtt_25_048: [**publish shall check for pending publish tokens by calling getPendingDeliveryTokens.
                    * And if there are pending tokens publish shall sleep until the number of pending tokens are less than 10 as per paho limitations**]**
                    */
                    Thread.sleep(10);
                }

                MqttMessage mqttMessage = (payload.length == 0) ? new MqttMessage() : new MqttMessage(payload);

                mqttMessage.setQos(Mqtt.info.qos);

                /*
                **Codes_SRS_Mqtt_25_014: [**The function shall publish message payload on the publishTopic specified to the IoT Hub given in the configuration.**]**
                 */

                IMqttDeliveryToken publishToken = Mqtt.info.mqttAsyncClient.publish(publishTopic, mqttMessage);

            }
            catch (MqttException e)
            {
                /*
                **Codes_SRS_Mqtt_25_047: [**If the Mqtt Client Async throws MqttException, the function shall throw an IOException with the message.**]**
                 */
                throw new IOException("Unable to publish message on topic : " + publishTopic + " because " + e.getCause() + e.getMessage());
            }
            catch (InterruptedException e)
            {
                throw new IOException("Interrupted, Unable to publish message on topic : " + publishTopic);
            }
            catch (Exception e)
            {
                throw new IOException("Unable to publish message on topic : " + publishTopic + " " + e.getCause() + e.getMessage());
            }

        }

    }

    /**
     * Method to subscribe to mqtt broker connection.
     *
     * @param topic the topic to subscribe on mqtt broker connection.
     */
    protected void subscribe(String topic) throws IOException
    {
        synchronized (Mqtt.MQTT_LOCK)
        {
            try
            {
                if (Mqtt.info == null)
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
                else if (!Mqtt.info.mqttAsyncClient.isConnected())
                {
                    /*
                    **Codes_SRS_Mqtt_25_015: [**If the MQTT connection is closed, the function shall throw an IOexception with message.**]**
                     */
                    throw new IOException("Cannot suscribe when mqtt client is disconnected");
                }
                /*
                **Codes_SRS_Mqtt_25_017: [**The function shall subscribe to subscribeTopic specified to the IoT Hub given in the configuration.**]**
                 */
                IMqttToken subToken = Mqtt.info.mqttAsyncClient.subscribe(topic, Mqtt.info.qos);
                subToken.waitForCompletion(Mqtt.info.MAX_WAIT_TIME);
            }
            catch (MqttException e)
            {
                /*
                **Codes_SRS_Mqtt_25_048: [**If the Mqtt Client Async throws MqttException for any reason, the function shall throw an IOException with the message.**]**
                 */
                throw new IOException("Unable to subscribe to topic :" + topic + " because " + e.getCause() + e.getMessage());
            }

        }

    }

    /**
     * Method to unsubscribe to mqtt broker connection.
     *
     * @param topic the topic to unsubscribe on mqtt broker connection.
     */

    protected void unsubscribe(String topic) throws IOException
    {
        synchronized (Mqtt.MQTT_LOCK)
        {
            try
            {
                if (!Mqtt.info.mqttAsyncClient.isConnected())
                {
                    /*
                    **Codes_SRS_Mqtt_25_018: [**If the MQTT connection is closed, the function shall throw an IOException with message.**]**
                     */
                    throw new IOException("Cannot unsubscribe when mqtt client is disconnected");
                }

                /*
                **Codes_SRS_Mqtt_25_020: [**The function shall unsubscribe from subscribeTopic specified to the IoT Hub given in the configuration.**]**
                 */
                IMqttToken subToken = Mqtt.info.mqttAsyncClient.unsubscribe(topic);
                subToken.waitForCompletion();

            }
            catch (MqttException e)
            {
                /*
                **Codes_SRS_Mqtt_25_019: [**If the unsubscribeTopic is null or empty, the function shall throw an IOException.**]**
                 */
                throw new IOException("Unable to unsubscribe to topic :" + topic + "because " + e.getCause() + e.getMessage());
            }

        }
    }

    protected boolean isConnected() throws IOException
    {
        if (Mqtt.info == null || Mqtt.info.mqttAsyncClient == null)
        {
            throw new InvalidParameterException("Mqtt client should be initialised atleast once before using it");
        }
        return Mqtt.info.mqttAsyncClient.isConnected();

    }

    /**
     * Method to receive messages on mqtt broker connection.
     */
    public Message receive() throws IOException
    {
        synchronized (Mqtt.MQTT_LOCK)
        {
            if (Mqtt.info == null)
            {
                throw new InvalidParameterException("Mqtt client should be initialised atleast once before using it");
            }
            /*
            **Codes_SRS_Mqtt_25_021: [**This method shall call parseTopic to parse the topic from the recevived Messages queue corresponding to the messaging client's operation.**]**
             */
            String topic = parseTopic();

            if (topic != null)
            {
                /*
                 **Codes_SRS_Mqtt_25_023: [**This method shall call parsePayload to get the message payload from the recevived Messages queue corresponding to the messaging client's operation.**]**
                 */
                byte[] data = parsePayload(topic);
                if (data != null)
                {
                    /*
                    **Codes_SRS_Mqtt_25_024: [**This method shall construct new Message with the bytes obtained from parsePayload and return the message.**]**
                     */
                    return new Message(data);
                }
                else
                {
                    /*
                    **Codes_SRS_Mqtt_25_025: [**If the call to parsePayload returns null when topic is non-null then this method will throw IOException**]**
                     */
                    throw new IOException("Data cannot be null when topic is non-null");
                }

            }

            else
            {
                /*
                **Codes_SRS_Mqtt_25_022: [**If the call parseTopic returns null or empty string then this method shall do nothing and return null**]**
                 */
                return null;
            }

        }
    }

    /**
     * Event fired when the connection with the MQTT broker is lost.
     * @param throwable Reason for losing the connection.
     */
    @Override
    public void connectionLost(Throwable throwable)
    {
        synchronized (Mqtt.MQTT_LOCK)
        {

            if (Mqtt.info != null && Mqtt.info.mqttAsyncClient != null)
            {
                int currentReconnectionAttempt = 0;
                while (!Mqtt.info.mqttAsyncClient.isConnected())
                {
                    System.out.println("Lost connection to the server. Reconnecting " + currentReconnectionAttempt + " time.");
                    try
                    {
                        currentReconnectionAttempt++;
                        connect();

                    }
                    catch (Exception e)
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
        Mqtt.allReceivedMessages.put(topic, mqttMessage.getPayload());
    }

    /**
     * Event fired when the message arrived on the MQTT broker.
     * @param iMqttDeliveryToken the MqttDeliveryToken for which the message was successfully sent.
     */

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken)
    {

    }

}
