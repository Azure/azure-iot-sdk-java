package com.iothub.azure.microsoft.com.androidsample;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.twin.DeviceMethodData;
import com.microsoft.azure.sdk.iot.device.twin.Pair;
import com.microsoft.azure.sdk.iot.device.twin.Property;
import com.microsoft.azure.sdk.iot.device.twin.TwinPropertyCallback;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    private enum LIGHTS{ ON, OFF, DISABLED };
    private enum CAMERA{ DETECTED_BURGLAR, SAFELY_WORKING };
    private static final int MAX_EVENTS_TO_REPORT = 5;

    private final String connString = "[device connection string]";
    private final String deviceId = "MyAndroidDevice";

    private double temperature;
    private double humidity;

    private DeviceClient client;

    IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
    Context appContext;

    private static String publicKeyCertificateString = "";

    //PEM encoded representation of the private key
    private static String privateKeyString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            InitClient();
        } catch (Exception e2)
        {
            System.out.println("Exception while opening IoTHub connection: " + e2.toString());

        }
    }

    private static final int METHOD_SUCCESS = 200;
    private static final int METHOD_NOT_DEFINED = 404;

    private static int method_command(Object command)
    {
        System.out.println("invoking command on this device");
        // Insert code to invoke command here
        return METHOD_SUCCESS;
    }

    private static int method_default(Object data)
    {
        System.out.println("invoking default method for this device");
        // Insert device specific code here
        return METHOD_NOT_DEFINED;
    }

    protected static class DeviceMethodStatusCallback implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            System.out.println("IoT Hub responded to device method operation with status " + status.name());
        }
    }

    protected static class SampleDeviceMethodCallback implements com.microsoft.azure.sdk.iot.device.twin.DeviceMethodCallback
    {
        @Override
        public DeviceMethodData call(String methodName, Object methodData, Object context)
        {
            DeviceMethodData deviceMethodData ;
            switch (methodName)
            {
                case "command" :
                {
                    int status = method_command(methodData);

                    deviceMethodData = new DeviceMethodData(status, "executed " + methodName);
                    break;
                }
                default:
                {
                    int status = method_default(methodData);
                    deviceMethodData = new DeviceMethodData(status, "executed " + methodName);
                }
            }

            return deviceMethodData;
        }
    }

    private static AtomicBoolean Succeed = new AtomicBoolean(false);

    protected static class DeviceTwinStatusCallback implements IotHubEventCallback
    {
        @Override
        public void execute(IotHubStatusCode status, Object context)
        {
            if((status == IotHubStatusCode.OK) || (status == IotHubStatusCode.OK_EMPTY))
            {
                Succeed.set(true);
            }
            else
            {
                Succeed.set(false);
            }
            System.out.println("IoT Hub responded to device twin operation with status " + status.name());
        }
    }

    protected static class onProperty implements TwinPropertyCallback
    {
        @Override
        public void TwinPropertyCallback(Property property, Object context)
        {
            System.out.println(
                    "onProperty callback for " + (property.getIsReported()?"reported": "desired") +
                            " property " + property.getKey() +
                            " to " + property.getValue() +
                            ", Properties version:" + property.getVersion());
        }
    }

    private void InitClient() throws URISyntaxException, IOException
    {
        client = new DeviceClient(connString, protocol);
        //client = new DeviceClient(connString, protocol, publicKeyCertificateString, false, privateKeyString, false);

        try
        {
            client.open();
            if (protocol == IotHubClientProtocol.MQTT)
            {
                MessageCallbackMqtt callback = new MessageCallbackMqtt();
                Counter counter = new Counter(0);
                client.setMessageCallback(callback, counter);
            } else
            {
                MessageCallback callback = new MessageCallback();
                Counter counter = new Counter(0);
                client.setMessageCallback(callback, counter);
            }
            client.subscribeToDeviceMethod(new SampleDeviceMethodCallback(), null, new DeviceMethodStatusCallback(), null);
            Succeed.set(false);
            client.startDeviceTwin(new DeviceTwinStatusCallback(), null, new onProperty(), null);

            do
            {
                Thread.sleep(1000);
            }
            while(!Succeed.get());

            Map<Property, Pair<TwinPropertyCallback, Object>> desiredProperties = new HashMap<Property, Pair<TwinPropertyCallback, Object>>()
            {
                {
                    put(new Property("HomeTemp(F)", null), new Pair<TwinPropertyCallback, Object>(new onProperty(), null));
                    put(new Property("LivingRoomLights", null), new Pair<TwinPropertyCallback, Object>(new onProperty(), null));
                    put(new Property("BedroomRoomLights", null), new Pair<TwinPropertyCallback, Object>(new onProperty(), null));
                    put(new Property("HomeSecurityCamera", null), new Pair<TwinPropertyCallback, Object>(new onProperty(), null));
                }
            };

            client.subscribeToTwinDesiredProperties(desiredProperties);

            System.out.println("Subscribe to Desired properties on device Twin...");
        }
        catch (Exception e2)
        {
            System.err.println("Exception while opening IoTHub connection: " + e2.getMessage());
            client.close();
            System.out.println("Shutting down...");
        }
    }

    public void btnGetTwinOnClick(View v) throws URISyntaxException, IOException
    {
        System.out.println("Get device Twin...");
        client.getDeviceTwin(); // For each desired property in the Service, the SDK will call the appropriate callback with the value and version.
    }

    public void btnUpdateReportedOnClick(View v) throws URISyntaxException, IOException
    {
            System.out.println("Update reported properties...");
            Set<Property> reportProperties = new HashSet<Property>()
            {
                {
                    add(new Property("HomeTemp(F)", 70));
                    add(new Property("LivingRoomLights", LIGHTS.ON));
                    add(new Property("BedroomRoomLights", LIGHTS.OFF));
                }
            };
            client.sendReportedProperties(reportProperties);

        for(int i = 0; i < MAX_EVENTS_TO_REPORT; i++)
        {

            if (Math.random() % MAX_EVENTS_TO_REPORT == 3)
            {
                client.sendReportedProperties(new HashSet<Property>() {{ add(new Property("HomeSecurityCamera", CAMERA.DETECTED_BURGLAR)); }});
            }
            else
            {
                client.sendReportedProperties(new HashSet<Property>() {{ add(new Property("HomeSecurityCamera", CAMERA.SAFELY_WORKING)); }});
            }
            if(i == MAX_EVENTS_TO_REPORT-1)
            {
                client.sendReportedProperties(new HashSet<Property>() {{ add(new Property("BedroomRoomLights", null)); }});
            }
            System.out.println("Updating reported properties..");
        }

        System.out.println("Waiting for Desired properties");
    }

    public void btnSendOnClick(View v) throws URISyntaxException, IOException
    {
        temperature = 20.0 + Math.random() * 10;
        humidity = 30.0 + Math.random() * 20;

        String msgStr = "{\"deviceId\":\"" + deviceId + "\",\"temperature\":" + temperature + ",\"humidity\":" + humidity + "}";
        try
        {
            Message msg = new Message(msgStr);
            msg.setProperty("temperatureAlert", temperature > 28 ? "true" : "false");
            msg.setMessageId(java.util.UUID.randomUUID().toString());
            System.out.println(msgStr);
            EventCallback eventCallback = new EventCallback();
            client.sendEventAsync(msg, eventCallback, 1);
        }
        catch (Exception e)
        {
            System.err.println("Exception while sending event: " + e.getMessage());
        }
    }

    public void btnFileUploadOnClick(View v) throws URISyntaxException, IOException
    {
        EditText text = (EditText)findViewById(R.id.editTextFileName);
        String fullFileName = text.getText().toString();

        try
        {
            Context context = getApplicationContext();

            File directory = context.getFilesDir();
            File file = new File(directory, fullFileName);
            file.createNewFile();
            if(file.isDirectory())
            {
                throw new IllegalArgumentException(fullFileName + " is a directory, please provide a single file name, or use the FileUploadSample to upload directories.");
            }
            else
            {
                client.uploadToBlobAsync(file.getName(), new FileInputStream(file), file.length(), new FileUploadStatusCallback(), null);
            }

            System.out.println("File upload started with success");

            System.out.println("Waiting for file upload callback with the status...");
        }
        catch (Exception e)
        {
            System.err.println("Exception while sending event: " + e.getMessage());
        }
    }

    protected static class FileUploadStatusCallback implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            System.out.println("IoT Hub responded to file upload operation with status " + status.name());
        }
    }

    private void stopClient() throws URISyntaxException, IOException
    {
        String OPERATING_SYSTEM = System.getProperty("os.name");
        client.close();
        System.out.println("Shutting down..." + OPERATING_SYSTEM);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void btnStopOnClick(View v) throws URISyntaxException, IOException
    {
        stopClient();
    }

    // Our MQTT doesn't support abandon/reject, so we will only display the messaged received
    // from IoTHub and return COMPLETE
    static class MessageCallbackMqtt implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public IotHubMessageResult execute(Message msg, Object context)
        {
            Counter counter = (Counter) context;
            System.out.println(
                    "Received message " + counter.toString()
                            + " with content: " + new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));

            counter.increment();

            return IotHubMessageResult.COMPLETE;
        }
    }

    static class EventCallback implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            Integer i = (Integer) context;
            System.out.println("IoT Hub responded to message " + i.toString()
                    + " with status " + status.name());
        }
    }

    static class MessageCallback implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public IotHubMessageResult execute(Message msg, Object context)
        {
            Counter counter = (Counter) context;
            System.out.println(
                    "Received message " + counter.toString()
                            + " with content: " + new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));

            int switchVal = counter.get() % 3;
            IotHubMessageResult res;
            switch (switchVal)
            {
                case 0:
                    res = IotHubMessageResult.COMPLETE;
                    break;
                case 1:
                    res = IotHubMessageResult.ABANDON;
                    break;
                case 2:
                    res = IotHubMessageResult.REJECT;
                    break;
                default:
                    // should never happen.
                    throw new IllegalStateException("Invalid message result specified.");
            }

            System.out.println("Responding to message " + counter.toString() + " with " + res.name());

            counter.increment();

            return res;
        }
    }

    /**
     * Used as a counter in the message callback.
     */
    static class Counter
    {
        int num;

        Counter(int num) {
            this.num = num;
        }

        int get() {
            return this.num;
        }

        void increment() {
            this.num++;
        }

        @Override
        public String toString() {
            return Integer.toString(this.num);
        }
    }

}
