// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Device;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.PropertyCallBack;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Multiplex devices an IoT Hub using AMQPS / AMQPS_WS
 * @deprecated use {@link MultiplexingClient} instead of {@link TransportClient}.
 */
@Deprecated
public class TransportClientSample
{
    private static final int D2C_MESSAGE_TIMEOUT = 2000; // 2 seconds
    private static final List<String> failedMessageListOnClose = new ArrayList<>(); // List of messages that failed on closeNow

    private static final int METHOD_SUCCESS = 200;
    private static final int METHOD_HUNG = 300;
    private static final int METHOD_NOT_FOUND = 404;
    private static final int METHOD_NOT_DEFINED = 404;
    private enum LIGHTS{ ON, OFF, DISABLED }
    private enum CAMERA{ DETECTED_BURGLAR, SAFELY_WORKING }
    private static final int MAX_EVENTS_TO_REPORT = 3;

    /** Used as a counter in the message callback. */
    protected static class Counter
    {
        protected int num;

        public Counter(int num)
        {
            this.num = num;
        }

        public int get()
        {
            return this.num;
        }

        public void increment()
        {
            this.num++;
        }

        @Override
        public String toString()
        {
            return Integer.toString(this.num);
        }
    }

    protected static class MessageCallback
            implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public IotHubMessageResult execute(Message msg,
                                           Object context)
        {
            Counter counter = (Counter) context;
            System.out.println(
                    " received message " + counter.toString()
                            + " with content: " + new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));
            for (MessageProperty messageProperty : msg.getProperties())
            {
                System.out.println(messageProperty.getName() + " : " + messageProperty.getValue());
            }

            IotHubMessageResult res = IotHubMessageResult.COMPLETE;

            System.out.println(
                    "Responding to message " + counter.toString()
                            + " with " + res.name());

            counter.increment();

            return res;
        }
    }

    protected static class EventCallback1 implements IotHubEventCallback{
        public void execute(IotHubStatusCode status, Object context){
            Message msg = (Message) context;
            System.out.println("Device Client 1: IoT Hub responded to message "+ msg.getMessageId()  + " with status " + status.name());
            if (status==IotHubStatusCode.MESSAGE_CANCELLED_ONCLOSE)
            {
                failedMessageListOnClose.add(msg.getMessageId());
            }
        }
    }

    protected static class EventCallback2 implements IotHubEventCallback{
        public void execute(IotHubStatusCode status, Object context){
            Message msg = (Message) context;
            System.out.println("Device Client 2: IoT Hub responded to message "+ msg.getMessageId()  + " with status " + status.name());
            if (status==IotHubStatusCode.MESSAGE_CANCELLED_ONCLOSE)
            {
                failedMessageListOnClose.add(msg.getMessageId());
            }
        }
    }

    protected static class EventCallback3 implements IotHubEventCallback{
        public void execute(IotHubStatusCode status, Object context){
            Message msg = (Message) context;
            System.out.println("Device Client 3: IoT Hub responded to message "+ msg.getMessageId()  + " with status " + status.name());
            if (status==IotHubStatusCode.MESSAGE_CANCELLED_ONCLOSE)
            {
                failedMessageListOnClose.add(msg.getMessageId());
            }
        }
    }

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

    protected static class DeviceMethodStatusCallBack1 implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            System.out.println("Device Client 1: IoT Hub responded to device method operation with status " + status.name());
        }
    }

    protected static class DeviceMethodStatusCallBack2 implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            System.out.println("Device Client 2: IoT Hub responded to device method operation with status " + status.name());
        }
    }

    protected static class DeviceMethodStatusCallBack3 implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            System.out.println("Device Client 3: IoT Hub responded to device method operation with status " + status.name());
        }
    }

    protected static class SampleDeviceMethodCallback implements com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodCallback
    {
        @Override
        public DeviceMethodData call(String methodName, Object methodData, Object context)
        {
            DeviceMethodData deviceMethodData ;
            if ("command".equals(methodName))
            {
                int status = method_command(methodData);
                deviceMethodData = new DeviceMethodData(status, "executed " + methodName);
            }
            else
            {
                int status = method_default(methodData);
                deviceMethodData = new DeviceMethodData(status, "executed " + methodName);
            }

            return deviceMethodData;
        }
    }

    protected static class DeviceTwinStatusCallBack implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            System.out.println("IoT Hub responded to device twin operation with status " + status.name());
        }
    }

    protected static class onHomeTempChange implements PropertyCallBack<String, Object>
    {
        @Override
        public void PropertyCall(String propertyKey, Object propertyValue, Object context)
        {
            if (propertyValue.equals(80))
            {
                System.out.println("Cooling down home, temp changed to " + propertyValue);
            }
        }

    }

    protected static class onCameraActivity implements PropertyCallBack<String, Object>
    {
        @Override
        public void PropertyCall(String propertyKey, Object propertyValue, Object context)
        {
            System.out.println(propertyKey + " changed to " + propertyValue);
            if (propertyValue.equals(CAMERA.DETECTED_BURGLAR))
            {
                System.out.println("Triggering alarm, burglar detected");
            }
        }

    }
    /**
     * Multiplex devices an IoT Hub using AMQPS / AMQPS_WS
     *
     * @param args
     * args[0] = IoT Hub connection string - Device Client 1
     * args[1] = IoT Hub connection string - Device Client 2
     * args[2] = IoT Hub connection string - Device Client 3
     */

    public static void main(String[] args)
            throws IOException, URISyntaxException
    {
        System.out.println("Starting...");
        System.out.println("Beginning setup.");

        if (args.length != 4)
        {
            System.out.format(
                    "Expected 3 arguments but received: %d.\n"
                            + "The program should be called with the following args: \n"
                            + "1. [Device 1 connection string] - String containing Hostname, Device Id & Device Key in one of the following formats: HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key>\n"
                            + "2. [Device 2 connection string] - String containing Hostname, Device Id & Device Key in one of the following formats: HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key>\n"
                            + "3. [Device 3 connection string] - String containing Hostname, Device Id & Device Key in one of the following formats: HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key>\n"
                            + "4. [Protocol]                   - amqps | amqps_ws\n",
                    args.length);
            return;
        }

        String connString1 = args[0];
        String connString2 = args[1];
        String connString3 = args[2];
        IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        if (args[3].equalsIgnoreCase("amqps_ws"))
        {
            protocol = IotHubClientProtocol.AMQPS_WS;
        }

        int numRequests = 3;

        System.out.println("Successfully read input parameters.");

        TransportClient transportClient = new TransportClient(protocol);

        DeviceClient client1 = new DeviceClient(connString1, transportClient);
        System.out.println("Successfully created an IoT Hub Client 1.");

        long time1 = 2400;
        client1.setOption("SetSASTokenExpiryTime", time1);

        MessageCallback callback1 = new MessageCallback();
        Counter counter1 = new Counter(0);
        client1.setMessageCallback(callback1, counter1);

        System.out.println("Successfully set message callback for Client 1.");


        DeviceClient client2 = new DeviceClient(connString2, transportClient);
        System.out.println("Successfully created an IoT Hub Client 2.");

        long time2 = 2400;
        client2.setOption("SetSASTokenExpiryTime", time2);

        MessageCallback callback2 = new MessageCallback();
        Counter counter2 = new Counter(0);
        client2.setMessageCallback(callback2, counter2);

        System.out.println("Successfully set message callback for Client 2.");

        DeviceClient client3 = new DeviceClient(connString3, transportClient);
        System.out.println("Successfully created an IoT Hub client3.");

        long time3 = 2400;
        client3.setOption("SetSASTokenExpiryTime", time3);

        MessageCallback callback3 = new MessageCallback();
        Counter counter3 = new Counter(0);
        client3.setMessageCallback(callback3, counter3);

        System.out.println("Successfully set message callback for Client 3.");

        // Open connection with multiplexing
        transportClient.open();
        System.out.format("Connection opened with multiplexing");

        transportClient.setSendInterval(42);

        // Start device operations
        client1.subscribeToDeviceMethod(new SampleDeviceMethodCallback(), null, new DeviceMethodStatusCallBack1(), null);
        client2.subscribeToDeviceMethod(new SampleDeviceMethodCallback(), null, new DeviceMethodStatusCallBack2(), null);
        client3.subscribeToDeviceMethod(new SampleDeviceMethodCallback(), null, new DeviceMethodStatusCallBack3(), null);

        Device homeKit = new Device()
        {
            @Override
            public void PropertyCall(String propertyKey, Object propertyValue, Object context)
            {
                System.out.println(propertyKey + " changed to " + propertyValue);
            }
        };

        client1.startDeviceTwin(new DeviceTwinStatusCallBack(), null, homeKit, null);
        client1.subscribeToDesiredProperties(homeKit.getDesiredProp());

        client2.startDeviceTwin(new DeviceTwinStatusCallBack(), null, homeKit, null);
        client2.subscribeToDesiredProperties(homeKit.getDesiredProp());

        client3.startDeviceTwin(new DeviceTwinStatusCallBack(), null, homeKit, null);
        client3.subscribeToDesiredProperties(homeKit.getDesiredProp());

        try
        {
            homeKit.setDesiredPropertyCallback(new Property("HomeTemp(F)", null), new onHomeTempChange(), null);
            homeKit.setDesiredPropertyCallback(new Property("LivingRoomLights", null), homeKit, null);
            homeKit.setDesiredPropertyCallback(new Property("BedroomRoomLights", null), homeKit, null);
            homeKit.setDesiredPropertyCallback(new Property("HomeSecurityCamera", null), new onCameraActivity(), null);

            homeKit.setReportedProp(new Property("HomeTemp(F)", 70));
            homeKit.setReportedProp(new Property("LivingRoomLights", LIGHTS.ON));
            homeKit.setReportedProp(new Property("BedroomRoomLights", LIGHTS.OFF));

            for (int i = 0; i < MAX_EVENTS_TO_REPORT; i++)
            {

                if (Math.random() % MAX_EVENTS_TO_REPORT == 3)
                {
                    homeKit.setReportedProp(new Property("HomeSecurityCamera", CAMERA.DETECTED_BURGLAR));
                }
                else
                {
                    homeKit.setReportedProp(new Property("HomeSecurityCamera", CAMERA.SAFELY_WORKING));
                }

                if (i == MAX_EVENTS_TO_REPORT - 1)
                {
                    homeKit.setReportedProp(new Property("BedroomRoomLights", null));
                }

                client1.sendReportedProperties(homeKit.getReportedProp());

                client2.sendReportedProperties(homeKit.getReportedProp());

                client3.sendReportedProperties(homeKit.getReportedProp());

                System.out.println("Updating reported properties..");
            }

            System.out.println("Waiting for Desired properties");
        }
        catch (Exception e)
        {
            System.out.println("On exception, shutting down \n" + " Cause: " + e.getCause() + " \n" +  e.getMessage());
            homeKit.clean();
            transportClient.closeNow();
            System.out.println("Shutting down...");
        }

        String deviceId = "MyJavaDevice";
        double temperature;
        double humidity;

        for (int i = 0; i < numRequests; ++i)
        {
            temperature = 20 + Math.random() * 10;
            humidity = 30 + Math.random() * 20;

            String msgStr = "{\"deviceId\":\"" + deviceId + i +"\",\"messageId\":" + i + ",\"temperature\":"+ temperature +",\"humidity\":"+ humidity +"}";

            try
            {
                Message msg = new Message(msgStr);
                msg.setProperty("temperatureAlert", temperature > 28 ? "true" : "false");
                msg.setMessageId(java.util.UUID.randomUUID().toString());
                msg.setExpiryTime(D2C_MESSAGE_TIMEOUT);
                msg.setMessageType(MessageType.DEVICE_TELEMETRY);

                if (i%3 == 0)
                {
                    EventCallback1 eventCallback1 = new EventCallback1();
                    client1.sendEventAsync(msg, eventCallback1, msg);
                }
                else if (i%3 == 1)
                {
                    EventCallback2 eventCallback2 = new EventCallback2();
                    client2.sendEventAsync(msg, eventCallback2, msg);
                }
                else
                {
                    EventCallback3 eventCallback3 = new EventCallback3();
                    client3.sendEventAsync(msg, eventCallback3, msg);
                }
            }

            catch (Exception e)
            {
                e.printStackTrace(); // Trace the exception
            }

        }

        System.out.println("Wait for " + D2C_MESSAGE_TIMEOUT / 1000 + " second(s) for response from the IoT Hub...");

        // Wait for IoT Hub to respond.
        try
        {
            Thread.sleep(D2C_MESSAGE_TIMEOUT);
        }

        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        System.out.println("In receive mode. Waiting for receiving C2D messages. Press ENTER to closeNow");

        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name());
        scanner.nextLine();

        // closeNow the connection
        System.out.println("Closing");
        homeKit.clean();
        transportClient.closeNow();

        if (!failedMessageListOnClose.isEmpty())
        {
            System.out.println("List of messages that were cancelled on closeNow:" + failedMessageListOnClose.toString());
        }

        System.out.println("Shutting down...");
    }
}
