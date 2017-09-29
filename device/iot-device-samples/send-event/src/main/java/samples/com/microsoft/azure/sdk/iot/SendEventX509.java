// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/** Sends a number of event messages to an IoT Hub. */
public class SendEventX509
{
    //PEM encoded representation of the public key certificate
    private static String publicKeyCertificateString =
            "-----BEGIN CERTIFICATE-----\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "-----END CERTIFICATE-----\n";

    //PEM encoded representation of the private key
    private static String privateKeyString =
            "-----BEGIN EC PRIVATE KEY-----\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "-----END EC PRIVATE KEY-----";

    private  static final int D2C_MESSAGE_TIMEOUT = 2000; // 2 seconds
    private  static List failedMessageListOnClose = new ArrayList(); // List of messages that failed on close
  
    protected static class EventCallback implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            Message msg = (Message) context;
            
            System.out.println("IoT Hub responded to message "+ msg.getMessageId()  + " with status " + status.name());
            
            if (status== IotHubStatusCode.MESSAGE_CANCELLED_ONCLOSE)
            {
                failedMessageListOnClose.add(msg.getMessageId());
            }
        }
    }

    /**
     * Sends a number of messages to an IoT Hub. Default protocol is to 
     * use MQTT transport.
     *
     * @param args
     * args[0] = IoT Hub connection string
     * args[1] = number of requests to send
     */
    public static void main(String[] args) throws IOException, URISyntaxException
    {
        System.out.println("Starting...");
        System.out.println("Beginning setup.");
 
        if (args.length != 2)
        {
            System.out.format(
                    "Expected 2 arguments but received: %d.\n"
                            + "The program should be called with the following args: \n"
                            + "1. [Device connection string] - String containing Hostname, Device Id & Device Key in one of the following formats: HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key>\n"
                            + "2. [number of requests to send]\n",
                    args.length);
            return;
        }

        String connectionString = args[0];

        int numRequests;
        try
        {
            numRequests = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e)
        {
            System.out.format(
                    "Could not parse the number of requests to send. "
                            + "Expected an int but received:\n%s.\n", args[1]);
            return;
        }

        IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

        System.out.println("Successfully read input parameters.");
        System.out.format("Using communication protocol %s.\n", protocol.name());

        DeviceClient client = new DeviceClient(connectionString, protocol, publicKeyCertificateString, false, privateKeyString, false);

        System.out.println("Successfully created an IoT Hub client.");

        client.open();

        System.out.println("Opened connection to IoT Hub.");
        System.out.println("Sending the following event messages:");

        String deviceId = "MyJavaDevice";
        double temperature = 0.0;
        double humidity = 0.0;

        for (int i = 0; i < numRequests; ++i)
        {
            temperature = 20 + Math.random() * 10;
            humidity = 30 + Math.random() * 20;

            String msgStr = "{\"deviceId\":\"" + deviceId +"\",\"messageId\":" + i + ",\"temperature\":"+ temperature +",\"humidity\":"+ humidity +"}";
            
            try
            {
                Message msg = new Message(msgStr);
                msg.setProperty("temperatureAlert", temperature > 28 ? "true" : "false");
                msg.setMessageId(java.util.UUID.randomUUID().toString());
                msg.setExpiryTime(D2C_MESSAGE_TIMEOUT);
                System.out.println(msgStr);

                EventCallback callback = new EventCallback();
                client.sendEventAsync(msg, callback, msg);
            }
            catch (Exception e)
            {
                 e.printStackTrace();
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

        // close the connection        
        System.out.println("Closing"); 
        client.closeNow();
        
        if (!failedMessageListOnClose.isEmpty())
        {
            System.out.println("List of messages that were cancelled on close:" + failedMessageListOnClose.toString()); 
        }

        System.out.println("Shutting down...");
    }
}
