// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.*;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Device Twin Sample for an IoT Hub. Default protocol is to use
 * MQTT transport.
 */
public class DeviceTwinSample
{
    private enum LIGHTS{ ON, OFF, DISABLED }
    private enum CAMERA{ DETECTED_BURGLAR, SAFELY_WORKING }
    private static final int MAX_EVENTS_TO_REPORT = 5;

    private static AtomicBoolean Succeed = new AtomicBoolean(false);

    protected static class DeviceTwinStatusCallBack implements IotHubEventCallback
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

    /*
     * If you don't care about version, you can use the PropertyCallBack.
     */
    protected static class onHomeTempChange implements TwinPropertyCallBack
    {
        @Override
        public void TwinPropertyCallBack(Property property, Object context)
        {
            System.out.println(
                    "onHomeTempChange change " + property.getKey() +
                            " to " + property.getValue() +
                            ", Properties version:" + property.getVersion());
        }
    }

    protected static class onCameraActivity implements TwinPropertyCallBack
    {
        @Override
        public void TwinPropertyCallBack(Property property, Object context)
        {
            System.out.println(
                    "onCameraActivity change " + property.getKey() +
                            " to " + property.getValue() +
                            ", Properties version:" + property.getVersion());
        }
    }

    protected static class onProperty implements TwinPropertyCallBack
    {
        @Override
        public void TwinPropertyCallBack(Property property, Object context)
        {
            System.out.println(
                    "onProperty callback for " + (property.getIsReported()?"reported": "desired") +
                            " property " + property.getKey() +
                            " to " + property.getValue() +
                            ", Properties version:" + property.getVersion());
        }
    }

    protected static class IotHubConnectionStatusChangeCallbackLogger implements IotHubConnectionStatusChangeCallback
    {
        @Override
        public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext)
        {
            System.out.println();
            System.out.println("CONNECTION STATUS UPDATE: " + status);
            System.out.println("CONNECTION STATUS REASON: " + statusChangeReason);
            System.out.println("CONNECTION STATUS THROWABLE: " + (throwable == null ? "null" : throwable.getMessage()));
            System.out.println();

            if (throwable != null)
            {
                throwable.printStackTrace();
            }

            if (status == IotHubConnectionStatus.DISCONNECTED)
            {
                //connection was lost, and is not being re-established. Look at provided exception for
                // how to resolve this issue. Cannot send messages until this issue is resolved, and you manually
                // re-open the device client
            }
            else if (status == IotHubConnectionStatus.DISCONNECTED_RETRYING)
            {
                //connection was lost, but is being re-established. Can still send messages, but they won't
                // be sent until the connection is re-established
            }
            else if (status == IotHubConnectionStatus.CONNECTED)
            {
                //Connection was successfully re-established. Can send messages.
            }
        }
    }

    /**
     * Reports properties to IotHub, receives desired property notifications from IotHub. Default protocol is to use
     * use MQTT transport.
     *
     * @param args 
     * args[0] = IoT Hub connection string
     */
    public static void main(String[] args)
            throws IOException, URISyntaxException
    {
        System.out.println("Starting...");
        System.out.println("Beginning setup.");


        if (args.length < 1)
        {
            System.out.format(
                    "Expected the following argument but received: %d.\n"
                            + "The program should be called with the following args: \n"
                            + "[Device connection string] - String containing Hostname, Device Id & Device Key in the following formats: HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key>\n"
                            + "[Protocol] - (mqtt | amqps | amqps_ws)\n",
                    args.length);
            return;
        }

        String connString = args[0];

        IotHubClientProtocol protocol;
        if (args.length == 1)
        {
            protocol = IotHubClientProtocol.MQTT;
        }
        else
        {
            String protocolStr = args[1];
            if (protocolStr.equals("amqps"))
            {
                protocol = IotHubClientProtocol.AMQPS;
            }
            else if (protocolStr.equals("mqtt"))
            {
                protocol = IotHubClientProtocol.MQTT;
            }
            else if (protocolStr.equals("amqps_ws"))
            {
                protocol = IotHubClientProtocol.AMQPS_WS;
            }
            else if (protocolStr.equals("mqtt_ws"))
            {
                protocol = IotHubClientProtocol.MQTT_WS;
            }
            else
            {
                System.out.format(
                        "Expected argument 2 to be one of 'mqtt', 'https', 'amqps' , 'mqtt_ws' or 'amqps_ws' but received %s\n"
                                + "The program should be called with the following args: \n"
                                + "1. [Device connection string] - String containing Hostname, Device Id & Device Key in one of the following formats: HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key>\n"
                                + "2. (mqtt | amqps | amqps_ws | mqtt_ws)\n",
                        protocolStr);
                return;
            }
        }

        System.out.println("Successfully read input parameters.");
        System.out.format("Using communication protocol %s.\n",
                protocol.name());

        DeviceClient client = new DeviceClient(connString, protocol);
        System.out.println("Successfully created an IoT Hub client.");

        client.registerConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallbackLogger(), new Object());

        try
        {
            System.out.println("Open connection to IoT Hub.");
            client.open();

            System.out.println("Start device Twin and get remaining properties...");
            // Properties already set in the Service will shows up in the generic onProperty callback, with value and version.
            Succeed.set(false);
            client.startDeviceTwin(new DeviceTwinStatusCallBack(), null, new onProperty(), null);
            do
            {
                Thread.sleep(1000);
            }
            while(!Succeed.get());


            System.out.println("Subscribe to Desired properties on device Twin...");
            Map<Property, Pair<TwinPropertyCallBack, Object>> desiredProperties = new HashMap<Property, Pair<TwinPropertyCallBack, Object>>()
            {
                {
                    put(new Property("HomeTemp(F)", null), new Pair<TwinPropertyCallBack, Object>(new onHomeTempChange(), null));
                    put(new Property("LivingRoomLights", null), new Pair<TwinPropertyCallBack, Object>(new onProperty(), null));
                    put(new Property("BedroomRoomLights", null), new Pair<TwinPropertyCallBack, Object>(new onProperty(), null));
                    put(new Property("HomeSecurityCamera", null), new Pair<TwinPropertyCallBack, Object>(new onCameraActivity(), null));
                }
            };
            client.subscribeToTwinDesiredProperties(desiredProperties);

            System.out.println("Get device Twin...");
            client.getDeviceTwin(); // For each desired property in the Service, the SDK will call the appropriate callback with the value and version.

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
        catch (Exception e)
        {
            System.out.println("On exception, shutting down \n" + " Cause: " + e.getCause() + " \n" +  e.getMessage());
            client.closeNow();
            System.out.println("Shutting down...");
        }

        System.out.println("Press any key to exit...");

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

        client.closeNow();

        System.out.println("Shutting down...");

    }
}
