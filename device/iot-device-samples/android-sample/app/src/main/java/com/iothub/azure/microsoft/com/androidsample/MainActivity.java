package com.iothub.azure.microsoft.com.androidsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.device.twin.*;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements DesiredPropertiesCallback, MethodCallback
{
    private final String connString = "";

    private DeviceClient client;
    private Twin twin;

    IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try
        {
            InitClient();
        }
        catch (Exception e2)
        {
            System.out.println("Exception while opening IoTHub connection");
            e2.printStackTrace();
        }
    }

    private static final int METHOD_SUCCESS = 200;
    private static final int METHOD_NOT_DEFINED = 404;

    @Override
    public DirectMethodResponse onMethodInvoked(String methodName, DirectMethodPayload directMethodPayload, Object payload)
    {
        // simulating a device that knows what to do when given a command with the method name "performAction".
        if (methodName.equals("performAction"))
        {
            return new DirectMethodResponse(METHOD_SUCCESS, null);
        }

        // if the command was unrecognized, return a status code to signal that to the client that invoked the method.
        return new DirectMethodResponse(METHOD_NOT_DEFINED, null);
    }

    @Override
    public void onDesiredPropertiesUpdated(Twin desiredPropertiesUpdate, Object o)
    {
        System.out.println("Received desired property update:");
        System.out.println(desiredPropertiesUpdate);
        twin.getDesiredProperties().putAll(desiredPropertiesUpdate.getDesiredProperties());
        twin.getDesiredProperties().setVersion(desiredPropertiesUpdate.getDesiredProperties().getVersion());
    }

    private void InitClient()
    {
        client = new DeviceClient(connString, protocol);

        try
        {
            client.open(true);

            if (protocol == IotHubClientProtocol.MQTT)
            {
                MessageCallbackMqtt callback = new MessageCallbackMqtt();
                Counter counter = new Counter(0);
                client.setMessageCallback(callback, counter);
            }
            else
            {
                MessageCallback callback = new MessageCallback();
                Counter counter = new Counter(0);
                client.setMessageCallback(callback, counter);
            }

            client.subscribeToMethods(this, null);
            client.subscribeToDesiredProperties(this, null);
            twin = client.getTwin();
        }
        catch (Exception e2)
        {
            System.err.println("Exception while opening IoTHub connection: " + e2.getMessage());
            client.close();
            System.out.println("Shutting down...");
        }
    }

    public void btnGetTwinOnClick(View v) throws IotHubClientException, InterruptedException
    {
        System.out.println("Get device Twin...");
        twin = client.getTwin();
        System.out.println(twin);
    }

    public void btnUpdateReportedOnClick(View v) throws IotHubClientException, InterruptedException
    {
        int newTemperatureValue = new Random().nextInt(80);
        System.out.println("Updating reported properties to set HomeTemp(F) to new value " + newTemperatureValue);
        twin.getReportedProperties().put("HomeTemp(F)", newTemperatureValue);
        ReportedPropertiesUpdateResponse response = client.updateReportedProperties(twin.getReportedProperties());
        twin.getReportedProperties().setVersion(response.getVersion());
        System.out.println("Reported properties update sent successfully. New version is " + response.getVersion());
    }

    public void btnSendOnClick(View v)
    {
        double temperature = 20.0 + Math.random() * 10;
        double humidity = 30.0 + Math.random() * 20;

        String msgStr = "{\"temperature\":" + temperature + ",\"humidity\":" + humidity + "}";
        try
        {
            Message msg = new Message(msgStr);
            msg.setProperty("temperatureAlert", temperature > 28 ? "true" : "false");
            msg.setMessageId(java.util.UUID.randomUUID().toString());
            System.out.println(msgStr);
            client.sendEventAsync(msg, new MessageSentCallbackImpl(), null);
        }
        catch (Exception e)
        {
            System.err.println("Exception while sending event: " + e.getMessage());
        }
    }

    private void stopClient()
    {
        String OPERATING_SYSTEM = System.getProperty("os.name");
        client.close();
        System.out.println("Shutting down..." + OPERATING_SYSTEM);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void btnStopOnClick(View v)
    {
        stopClient();
    }

    // Our MQTT doesn't support abandon/reject, so we will only display the messaged received
    // from IoTHub and return COMPLETE
    static class MessageCallbackMqtt implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public IotHubMessageResult onCloudToDeviceMessageReceived(Message msg, Object context)
        {
            Counter counter = (Counter) context;
            System.out.println(
                    "Received message " + counter.toString()
                            + " with content: " + new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));

            counter.increment();

            return IotHubMessageResult.COMPLETE;
        }
    }

    static class MessageSentCallbackImpl implements MessageSentCallback
    {
        @Override
        public void onMessageSent(Message message, IotHubClientException e, Object o)
        {
            if (e == null)
            {
                System.out.println("IoT Hub responded to message " + message.getMessageId() + " with status OK");
            }
            else
            {
                System.out.println("IoT Hub responded to message " + message.getMessageId() + " with status " + e.getStatusCode().name());
            }
        }
    }

    static class MessageCallback implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public IotHubMessageResult onCloudToDeviceMessageReceived(Message msg, Object context)
        {
            Counter counter = (Counter) context;
            System.out.println(
                    "Received message " + counter.toString()
                            + " with content: " + new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));

            counter.increment();

            return IotHubMessageResult.COMPLETE;
        }
    }

    /**
     * Used as a counter in the message callback.
     */
    static class Counter
    {
        int num;

        Counter(int num)
        {
            this.num = num;
        }

        int get()
        {
            return this.num;
        }

        void increment()
        {
            this.num++;
        }

        @Override
        public String toString()
        {
            return Integer.toString(this.num);
        }
    }
}
