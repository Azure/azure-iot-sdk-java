package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.IAsyncMqttClient;
import com.microsoft.azure.sdk.iot.device.MqttConnectOptions;
import com.microsoft.azure.sdk.iot.device.ReceivedMqttMessage;
import com.microsoft.azure.sdk.iot.device.transport.HttpProxySocketFactory;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.function.Consumer;

public class PahoAsyncMqttClient implements IAsyncMqttClient, MqttCallback
{
    // relatively arbitrary, but only because Paho doesn't have any particular recommendations here. Just a high enough
    // value that users who are building a gateway type solution don't find this value to be a bottleneck.
    private static final int MAX_IN_FLIGHT_COUNT = 65000;

    private MqttAsyncClient asyncPahoClient;
    private Consumer<ReceivedMqttMessage> messageCallback;
    private Consumer<Integer> connectionLossEvent;

    @Override
    public void connectAsync(MqttConnectOptions options, Consumer<Integer> onConnectionAcknowledged)
    {
        try
        {
            this.asyncPahoClient = new MqttAsyncClient(options.getServerUri(), options.getClientId(), new MemoryPersistence());
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
        pahoOptions.setUserName(options.getUsername());

        if (options.getPassword() != null && options.getPassword().length > 0)
        {
            pahoOptions.setPassword(options.getPassword());
        }

        pahoOptions.setMqttVersion(4); //TODO hardcoded for now
        pahoOptions.setKeepAliveInterval(options.getKeepAlivePeriod());
        pahoOptions.setCleanSession(true);
        pahoOptions.setMaxInflight(MAX_IN_FLIGHT_COUNT);

        if (options.getProxySettings() == null)
        {
            pahoOptions.setSocketFactory(options.getSslContext().getSocketFactory());
        }
        else
        {
            pahoOptions.setSocketFactory(new HttpProxySocketFactory(options.getSslContext().getSocketFactory(), options.getProxySettings()));
        }

        try
        {
            // Note that the connectAsync function of the Paho MQTT client does not work, so this method will
            // run synchronously.
            IMqttToken connectToken = this.asyncPahoClient.connect(pahoOptions);
            connectToken.waitForCompletion(10000);
            onConnectionAcknowledged.accept(1);
        }
        catch (MqttException e)
        {
            //TODO
            e.printStackTrace();
        }
    }

    @Override
    public void disconnectAsync(Consumer<Integer> onDisconnectionAcknowledged)
    {
        try
        {
            this.asyncPahoClient.disconnect(30000, null, new IMqttActionListener()
            {
                @Override
                public void onSuccess(IMqttToken asyncActionToken)
                {
                    onDisconnectionAcknowledged.accept(0);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception)
                {
                    //TODO error
                    onDisconnectionAcknowledged.accept(-1);
                    exception.printStackTrace();
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
    public void subscribeAsync(String topic, int qos, Consumer<Integer> onSubscriptionAcknowledged)
    {
        try
        {
            this.asyncPahoClient.subscribe(topic, qos, null, new IMqttActionListener()
            {
                @Override
                public void onSuccess(IMqttToken asyncActionToken)
                {
                    onSubscriptionAcknowledged.accept(1);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception)
                {
                    //TODO error code
                    onSubscriptionAcknowledged.accept(-1);
                    exception.printStackTrace();
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
    public void unsubscribe(String topic, Consumer<Integer> onUnsubscriptionAcknowledged)
    {
        try
        {
            this.asyncPahoClient.unsubscribe(topic, null, new IMqttActionListener()
            {
                @Override
                public void onSuccess(IMqttToken asyncActionToken)
                {
                    onUnsubscriptionAcknowledged.accept(1);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception)
                {
                    //TODO error
                    onUnsubscriptionAcknowledged.accept(-1);
                    exception.printStackTrace();
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
    public void publishAsync(String topic, byte[] payload, int qos, Consumer<Integer> onMessageAcknowledged)
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
                    onMessageAcknowledged.accept(1);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception)
                {
                    //TODO error
                    onMessageAcknowledged.accept(-1);
                    exception.printStackTrace();
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
            this.asyncPahoClient.messageArrivedComplete(messageId, qos); //TODO qos?
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
    public void setConnectionLostCallback(Consumer<Integer> connectionLossEvent)
    {
        this.connectionLossEvent = connectionLossEvent;
    }

    @Override
    public void connectionLost(Throwable throwable)
    {
        this.connectionLossEvent.accept(0); //todo details
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
