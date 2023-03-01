package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.IMqttClient;
import com.microsoft.azure.sdk.iot.device.MqttConnectSettings;
import com.microsoft.azure.sdk.iot.device.ReceivedMqttMessage;
import com.microsoft.azure.sdk.iot.device.transport.HttpProxySocketFactory;
import com.microsoft.azure.sdk.iot.device.transport.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.exceptions.PahoExceptionTranslator;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.function.Consumer;

public class PahoMqttClient implements IMqttClient, MqttCallback
{
    // relatively arbitrary, but only because Paho doesn't have any particular recommendations here. Just a high enough
    // value that users who are building a gateway type solution don't find this value to be a bottleneck.
    private static final int MAX_IN_FLIGHT_COUNT = 65000;

    private static final int CONNECTION_TIMEOUT = 60 * 1000;
    private static final int DISCONNECTION_TIMEOUT = 60 * 1000;
    private static final int MAX_SUBSCRIBE_ACK_WAIT_TIME = 15 * 1000;
    private static final int MAX_UNSUBSCRIBE_ACK_WAIT_TIME = 15 * 1000;

    private MqttAsyncClient asyncPahoClient;
    private Consumer<ReceivedMqttMessage> messageCallback;
    private Consumer<TransportException> connectionLossCallback;

    @Override
    public void connect(MqttConnectSettings settings) throws TransportException
    {
        try
        {
            this.asyncPahoClient = new MqttAsyncClient(settings.getServerUri(), settings.getClientId(), new MemoryPersistence());
        }
        catch (MqttException e)
        {
            //TODO
            e.printStackTrace();
        }

        this.asyncPahoClient.setCallback(this);
        this.asyncPahoClient.setManualAcks(true);

        org.eclipse.paho.client.mqttv3.MqttConnectOptions pahoOptions =
                new org.eclipse.paho.client.mqttv3.MqttConnectOptions();
        pahoOptions.setUserName(settings.getUsername());

        if (settings.getPassword() != null && settings.getPassword().length > 0)
        {
            pahoOptions.setPassword(settings.getPassword());
        }

        //This is the only version the IoT hub device SDK needs currently
        if (settings.getMqttVersion() == MqttVersion.MQTT_VERSION_3_1_1)
        {
            pahoOptions.setMqttVersion(org.eclipse.paho.client.mqttv3.MqttConnectOptions.MQTT_VERSION_3_1_1);
        }

        pahoOptions.setKeepAliveInterval(settings.getKeepAlivePeriod());
        pahoOptions.setCleanSession(settings.isCleanSession());
        pahoOptions.setMaxInflight(MAX_IN_FLIGHT_COUNT);

        if (settings.getProxySettings() == null)
        {
            pahoOptions.setSocketFactory(settings.getSslContext().getSocketFactory());
        }
        else
        {
            pahoOptions.setSocketFactory(new HttpProxySocketFactory(settings.getSslContext().getSocketFactory(), settings.getProxySettings()));
        }

        try
        {
            // Note that the connectAsync function of the Paho MQTT client does not work, so this method will
            // run synchronously.
            IMqttToken connectToken = this.asyncPahoClient.connect(pahoOptions);
            connectToken.waitForCompletion(CONNECTION_TIMEOUT);
        }
        catch (MqttException e)
        {
            throw PahoExceptionTranslator.convertToMqttException(e, "Failed to connect");
        }
    }

    @Override
    public void disconnect() throws TransportException
    {
        try
        {
            IMqttToken disconnectToken = this.asyncPahoClient.disconnect();
            disconnectToken.waitForCompletion(DISCONNECTION_TIMEOUT);
        }
        catch (MqttException e)
        {
            throw PahoExceptionTranslator.convertToMqttException(e, "Failed to disconnect");
        }
    }

    @Override
    public void subscribe(String topic, int qos) throws TransportException
    {
        try
        {
            IMqttToken subscribeToken = this.asyncPahoClient.subscribe(topic, qos);
            subscribeToken.waitForCompletion(MAX_SUBSCRIBE_ACK_WAIT_TIME);
        }
        catch (MqttException e)
        {
            throw PahoExceptionTranslator.convertToMqttException(e, "Failed to subscribe to topic " + topic);
        }
    }

    @Override
    public void unsubscribe(String topic) throws TransportException
    {
        try
        {
            IMqttToken unsubscribeToken = this.asyncPahoClient.unsubscribe(topic);
            unsubscribeToken.waitForCompletion(MAX_UNSUBSCRIBE_ACK_WAIT_TIME);
        }
        catch (MqttException e)
        {
            throw PahoExceptionTranslator.convertToMqttException(e, "Failed to unsubscribe to topic " + topic);
        }
    }

    @Override
    public void publishAsync(String topic, byte[] payload, int qos, Runnable onMessageAcknowledged, Consumer<TransportException> onFailure)
    {
        try
        {
            MqttMessage pahoMessage = new MqttMessage(payload);
            pahoMessage.setQos(qos);
            this.asyncPahoClient.publish(topic, pahoMessage, null, new IMqttActionListener()
            {
                @Override
                public void onSuccess(IMqttToken asyncActionToken)
                {
                    onMessageAcknowledged.run();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception)
                {
                    onFailure.accept(new TransportException(exception));
                }
            });
        }
        catch (MqttException e)
        {
            //TODO
            e.printStackTrace();
        }
    }

    @Override
    public void acknowledgeMessageAsync(int messageId, int qos)
    {
        try
        {
            this.asyncPahoClient.messageArrivedComplete(messageId, qos);
        }
        catch (MqttException e)
        {
            //TODO
            e.printStackTrace();
        }
    }

    @Override
    public void setMessageCallback(Consumer<ReceivedMqttMessage> messageCallback)
    {
        this.messageCallback = messageCallback;
    }

    @Override
    public void setConnectionLostCallback(Consumer<TransportException> connectionLossCallback)
    {
        this.connectionLossCallback = connectionLossCallback;
    }

    @Override
    public void connectionLost(Throwable throwable)
    {
        TransportException transportException;
        if (throwable instanceof MqttException)
        {
            transportException = PahoExceptionTranslator.convertToMqttException((MqttException) throwable, "Mqtt connection lost");
        }
        else
        {
            transportException = new TransportException(throwable);
        }

        this.connectionLossCallback.accept(transportException);
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception
    {
        ReceivedMqttMessage message = ReceivedMqttMessage.builder()
                .payload(mqttMessage.getPayload())
                .topic(topic)
                .qos(mqttMessage.getQos())
                .messageId(mqttMessage.getId())
                .build();

        this.messageCallback.accept(message);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken)
    {
        //TODO this can be ignored, right?
    }
}
