// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.twin.*;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Twin Sample for sending module twin updates to an IoT Hub. Default protocol is to use
 * MQTT transport.
 */
public class ModuleTwinSample
{
    private static final String SAMPLE_USAGE = "The program should be called with the following args: \n"
            + "1. [Device connection string] - String containing Hostname, Device Id, Module Id & Device Key in one of the following formats: HostName=<iothub_host_name>;deviceId=<device_id>;SharedAccessKey=<device_key>;moduleId=<module_id>\n"
            + "2. (mqtt | amqps | amqps_ws | mqtt_ws)\n";

    private static final String SAMPLE_USAGE_WITH_WRONG_ARGS = "Expected 2 or 3 arguments but received: %d.\n" + SAMPLE_USAGE;
    private static final String SAMPLE_USAGE_WITH_INVALID_PROTOCOL = "Expected argument 2 to be one of 'mqtt', 'amqps' or 'amqps_ws' but received %s\n" + SAMPLE_USAGE;

    private enum LIGHTS{ ON, OFF }

    private static Twin twin;

    private static class DesiredPropertiesUpdatedHandler implements DesiredPropertiesCallback
    {
        @Override
        public void onDesiredPropertiesUpdated(Twin desiredPropertyUpdateTwin, Object context)
        {
            if (twin == null)
            {
                // No need to care about this update because these properties will be present in the twin retrieved by getTwin.
                System.out.println("Received desired properties update before getting current twin. Ignoring this update.");
                return;
            }

            // desiredPropertyUpdateTwin.getDesiredProperties() contains all the newly updated desired properties
            // as well as the new version of the desired properties
            twin.getDesiredProperties().putAll(desiredPropertyUpdateTwin.getDesiredProperties());
            twin.getDesiredProperties().setVersion(desiredPropertyUpdateTwin.getDesiredProperties().getVersion());
            System.out.println("Received desired property update. Current twin:");
            System.out.println(twin);
        }
    }

    protected static class IotHubConnectionStatusChangeCallbackLogger implements IotHubConnectionStatusChangeCallback
    {
        @Override
        public void onStatusChanged(ConnectionStatusChangeContext connectionStatusChangeContext)
        {
            IotHubConnectionStatus status = connectionStatusChangeContext.getNewStatus();
            IotHubConnectionStatusChangeReason statusChangeReason = connectionStatusChangeContext.getNewStatusReason();
            Throwable throwable = connectionStatusChangeContext.getCause();

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
                System.out.println("The connection was lost, and is not being re-established." +
                        " Look at provided exception for how to resolve this issue." +
                        " Cannot send messages until this issue is resolved, and you manually re-open the module client");
            }
            else if (status == IotHubConnectionStatus.DISCONNECTED_RETRYING)
            {
                System.out.println("The connection was lost, but is being re-established." +
                        " Can still send messages, but they won't be sent until the connection is re-established");
            }
            else if (status == IotHubConnectionStatus.CONNECTED)
            {
                System.out.println("The connection was successfully established. Can send messages.");
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
    public static void main(String[] args) throws IOException, URISyntaxException
    {
        System.out.println("Starting...");
        System.out.println("Beginning setup.");

        if (args.length < 1)
        {
            System.out.format(SAMPLE_USAGE_WITH_WRONG_ARGS, args.length);
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
                System.out.format(SAMPLE_USAGE_WITH_INVALID_PROTOCOL, protocolStr);
                return;
            }
        }

        System.out.println("Successfully read input parameters.");
        System.out.format("Using communication protocol %s.\n",
                protocol.name());

        ModuleClient client = new ModuleClient(connString, protocol);
        System.out.println("Successfully created an IoT Hub client.");

        client.setConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallbackLogger(), new Object());

        try
        {
            System.out.println("Open connection to IoT Hub.");
            client.open(false);

            System.out.println("Subscribing to desired properties");
            client.subscribeToDesiredProperties(new DesiredPropertiesUpdatedHandler(), null);

            // It is recommended to get the initial twin state after every time you have subscribed to desired
            // properties, but is not mandatory. The benefit is that you are up to date on any twin updates
            // your client may have missed while not being subscribed, but the cost is that the get twin request
            // may not provide any new twin updates while still requiring some messaging between the client and service.
            System.out.println("Getting current twin");
            twin = client.getTwin();
            System.out.println("Received current twin:");
            System.out.println(twin);

            // After getting the current twin, you can begin sending reported property updates. You can send reported
            // property updates without getting the current twin as long as you have the correct reported properties
            // version. If you send reported properties and receive a "precondition failed" error, then your reported
            // properties version is out of date. Get the latest version by calling getTwin() again.
            TwinCollection reportedProperties = twin.getReportedProperties();
            int newTemperature = new Random().nextInt(80);
            reportedProperties.put("HomeTemp(F)", newTemperature);
            System.out.println("Updating reported property \"HomeTemp(F)\" to value " + newTemperature);
            ReportedPropertiesUpdateResponse response = client.updateReportedProperties(reportedProperties);
            System.out.println("Successfully set property \"HomeTemp(F)\" to value " + newTemperature);

            // After a successful update of the module's reported properties, the service will provide the new
            // reported properties version for the twin. You'll need to save this value in your twin object's reported
            // properties object so that subsequent updates don't fail with a "precondition failed" error.
            twin.getReportedProperties().setVersion(response.getVersion());

            System.out.println("Current twin:");
            System.out.println(twin);

            reportedProperties.put("LivingRoomLights", LIGHTS.ON);
            reportedProperties.put("BedroomRoomLights", LIGHTS.OFF);
            System.out.println("Updating reported property \"LivingRoomLights\" to value ON");
            System.out.println("Updating reported property \"BedroomRoomLights\" to value OFF");
            response = client.updateReportedProperties(reportedProperties);
            System.out.println("Successfully set property \"LivingRoomLights\" to value ON");
            System.out.println("Successfully set property \"BedroomRoomLights\" to value OFF");

            twin.getReportedProperties().setVersion(response.getVersion());

            System.out.println("Current twin:");
            System.out.println(twin);
        }
        catch (Exception e)
        {
            System.out.println("On exception, shutting down \n" + " Cause: " + e.getCause() + " \n" +  e.getMessage());
            client.close();
            System.out.println("Shutting down...");
        }

        System.out.println("Press any key to exit...");

        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name());
        scanner.nextLine();

        client.close();

        System.out.println("Shutting down...");
    }
}
