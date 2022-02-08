// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.twin.MethodCallback;
import com.microsoft.azure.sdk.iot.device.twin.DirectMethodResponse;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Device Method Sample for sending a message from a module to IoT Hub. Default protocol is to use
 * MQTT transport.
 */
public class ModuleMethodSample
{
    private static final String SAMPLE_USAGE = "The program should be called with the following args: \n"
            + "1. [Device connection string] - String containing Hostname, Device Id, Module Id & Device Key in one of the following formats: HostName=<iothub_host_name>;deviceId=<device_id>;SharedAccessKey=<device_key>;moduleId=<module_id>\n"
            + "2. (mqtt | amqps | amqps_ws | mqtt_ws)\n";

    private static final String SAMPLE_USAGE_WITH_WRONG_ARGS = "Expected 2 or 3 arguments but received: %d.\n" + SAMPLE_USAGE;
    private static final String SAMPLE_USAGE_WITH_INVALID_PROTOCOL = "Expected argument 2 to be one of 'mqtt', 'amqps' or 'amqps_ws' but received %s\n" + SAMPLE_USAGE;
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

    protected static class DirectMethodStatusCallback implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            System.out.println("IoT Hub responded to device method operation with status " + status.name());
        }
    }

    protected static class SampleMethodCallback implements MethodCallback
    {
        @Override
        public DirectMethodResponse call(String methodName, Object methodData, Object context)
        {
            DirectMethodResponse deviceDirectMethodResponse;
            int status = method_default(methodData);
            if ("command".equals(methodName))
            {
                status = method_command(methodData);
            }

            deviceDirectMethodResponse = new DirectMethodResponse(status, "executed " + methodName);

            return deviceDirectMethodResponse;
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
     * Receives method calls from IotHub. Default protocol is to use
     * use MQTT transport.
     *
     * @param args The connection string and the selected protocol
     * @throws Exception if any exception is encountered
     */
    public static void main(String[] args) throws Exception
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
            client.open(false);

            System.out.println("Opened connection to IoT Hub.");

            client.subscribeToMethodsAsync(new SampleMethodCallback(), null, new DirectMethodStatusCallback(), null);

            System.out.println("Subscribed to device method");

            System.out.println("Waiting for method trigger");
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
