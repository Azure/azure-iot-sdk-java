// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.ConnectionStatusChangeContext;
import com.microsoft.azure.sdk.iot.device.twin.*;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK;

/**
 * Device Twin Sample for sending module twin updates to an IoT Hub. Default protocol is to use
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
                        " Cannot send messages until this issue is resolved, and you manually re-open the device client");
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
    public static void main(String[] args) throws IOException, URISyntaxException, ModuleClientException
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

            System.out.println("Start device Twin and get remaining properties...");
            CountDownLatch twinInitializedLatch = new CountDownLatch(1);
            client.subscribeToDesiredPropertiesAsync(
                (statusCode, context) ->
                {
                    if (statusCode == OK)
                    {
                        System.out.println("Successfully subscribed to desired properties. Getting initial twin state");

                        // It is recommended to get the initial twin state after every time you have subscribed to desired
                        // properties, but is not mandatory. The benefit is that you are up to date on any twin updates
                        // your client may have missed while not being subscribed, but the cost is that the get twin request
                        // may not provide any new twin updates while still requiring some messaging between the client and service.
                        client.getTwinAsync(
                            (twin, getTwinContext) ->
                            {
                                System.out.println("Received initial twin state");
                                System.out.println(twin.toString());
                                twinInitializedLatch.countDown();
                            },
                            null);
                    }
                    else
                    {
                        System.out.println("Failed to subscribe to desired properties with status code " + statusCode);
                        System.exit(-1);
                    }
                },
                null,
                (twin, context) ->
                {
                    for (String propertyKey : twin.getDesiredProperties().keySet())
                    {
                        Object propertyValue = twin.getDesiredProperties().get(propertyKey);
                        System.out.println("Received desired property update with property key " + propertyKey + " and value " + propertyValue);
                    }
                },
                null);

            System.out.println("Update reported properties...");
            TwinCollection reportedProperties = new TwinCollection();
            reportedProperties.put("HomeTemp(F)", 70);
            reportedProperties.put("LivingRoomLights", LIGHTS.ON);
            reportedProperties.put("BedroomRoomLights", LIGHTS.OFF);
            CountDownLatch twinReportedPropertiesSentLatch = new CountDownLatch(1);
            client.updateReportedPropertiesAsync(
                reportedProperties,
                (statusCode, e, callbackContext) ->
                {
                    if (statusCode == OK)
                    {
                        System.out.println("Reported properties updated successfully");
                    }
                    else
                    {
                        System.out.println("Reported properties failed to be updated. Status code: " + statusCode);
                        e.printStackTrace();
                    }

                    twinReportedPropertiesSentLatch.countDown();
                },
                null);

            twinReportedPropertiesSentLatch.await();
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
