// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


/** Sends a number of event messages to an IoT Hub. */
public class SendEvent
{
    private  static final int D2C_MESSAGE_TIMEOUT = 2000; // 2 seconds

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
     * Sends a number of messages to an IoT or Edge Hub. Default protocol is to
     * use MQTT transport.
     *
     * @param args
     * args[0] = IoT Hub or Edge Hub connection string
     * args[1] = number of messages to send
     * args[2] = protocol (optional, one of 'mqtt' or 'amqps' or 'httpsnt' or 'amqps_ws')
     */
    public static void main(String[] args)
            throws IOException, URISyntaxException, InterruptedException, IotHubClientException
    {
        InputParameters params = new InputParameters(args);

        System.out.println("Starting...");
        System.out.println("Beginning setup.");

        String connString = params.getConnectionString();
        int numRequests;
        try
        {
            numRequests = Integer.parseInt(params.getNumberOfRequests());
        }
        catch (NumberFormatException e)
        {
            System.out.format(
                "Could not parse the number of requests to send. "
                    + "Expected an int but received: [" + params.getNumberOfRequests() + "]");
            return;
        }

        IotHubClientProtocol protocol = params.getProtocol();

        System.out.println("Successfully read input parameters.");
        System.out.format("Using communication protocol %s.\n", protocol.name());

        DeviceClient client = new DeviceClient(connString, protocol);

        System.out.println("Successfully created an IoT Hub client.");

        client.setConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallbackLogger(), new Object());

        client.open(true);

        System.out.println("Opened connection to IoT Hub.");
        System.out.println("Sending the following event messages:");

        for (int i = 0; i < numRequests; ++i)
        {
            double temperature = 20 + Math.random() * 10;
            double humidity = 30 + Math.random() * 20;

            String msgStr = "{\"temperature\":"+ temperature +",\"humidity\":"+ humidity +"}";

            Message msg = new Message(msgStr);
            msg.setContentType("application/json");
            msg.setProperty("temperatureAlert", temperature > 28 ? "true" : "false");
            msg.setMessageId(java.util.UUID.randomUUID().toString());
            System.out.println(msgStr);

            try
            {
                client.sendEvent(msg, D2C_MESSAGE_TIMEOUT);
                System.out.println("Successfully sent the message");
            }
            catch (IotHubClientException e)
            {
                System.out.println("Failed to send the message. Status code: " + e.getStatusCode());
            }
        }

        // close the connection
        System.out.println("Closing the client...");
        client.close();
    }
}