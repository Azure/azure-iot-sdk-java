// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

/** Sends a number of event messages to an IoT Hub. */
public class SendEventX509
{
    //PEM encoded representation of the public key certificate
    private static final String publicKeyCertificateString =
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
    private static final String privateKeyString =
            "-----BEGIN PRIVATE KEY-----\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "-----END PRIVATE KEY-----\n";

    // The maximum amount of time to wait for a message to be sent. Typically, this operation finishes in under a second.
    private static final int D2C_MESSAGE_TIMEOUT_MILLISECONDS = 10000;

    /**
     * Sends a number of messages to an IoT Hub. Default protocol is to 
     * use MQTT transport.
     *
     * @param args
     * args[0] = IoT Hub connection string
     * args[1] = number of requests to send
     * args[2] = IoT Hub protocol to use, optional and defaults to MQTT
     */
    public static void main(String[] args) throws IOException, URISyntaxException, GeneralSecurityException, IotHubClientException, InterruptedException
    {
        System.out.println("Starting...");
        System.out.println("Beginning setup.");
 
        if (!(args.length == 2 || args.length == 3))
        {
            System.out.format(
                    "Expected 2 or 3 arguments but received: %d.\n"
                            + "The program should be called with the following args: \n"
                            + "1. [Device connection string] - String containing Hostname, Device Id & Device Key in one of the following formats: HostName=<host_name>;DeviceId=<device_id>;x509=true\n"
                            + "2. [number of requests to send]\n"
                            + "3. (mqtt | https | amqps | amqps_ws | mqtt_ws)\n",
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

        IotHubClientProtocol protocol;
        if (args.length == 2)
        {
            protocol = IotHubClientProtocol.MQTT;
        }
        else
        {
            String protocolStr = args[2].toLowerCase();
            if (protocolStr.equals("https"))
            {
                protocol = IotHubClientProtocol.HTTPS;
            }
            else if (protocolStr.equals("amqps"))
            {
                protocol = IotHubClientProtocol.AMQPS;
            }
            else if (protocolStr.equals("mqtt"))
            {
                protocol = IotHubClientProtocol.MQTT;
            }
            else if (protocolStr.equals("mqtt_ws"))
            {
                protocol = IotHubClientProtocol.MQTT_WS;
            }
            else
            {
                System.out.format(
                        "Expected argument 3 to be one of 'mqtt', 'mqtt_ws', 'https', or 'amqps' but received %s\n"
                                + "The program should be called with the following args: \n"
                                + "1. [Device connection string] - String containing Hostname, Device Id & Device Key in one of the following formats: HostName=<host_name>;DeviceId=<device_id>;x509=true\n"
                                + "2. [number of requests to send]\n"
                                + "3. (mqtt | https | amqps | amqps_ws | mqtt_ws)\n",
                        protocolStr);
                return;
            }
        }

        System.out.println("Successfully read input parameters.");
        System.out.format("Using communication protocol %s.\n", protocol.name());

        SSLContext sslContext = SSLContextBuilder.buildSSLContext(publicKeyCertificateString, privateKeyString);
        ClientOptions clientOptions = ClientOptions.builder().sslContext(sslContext).build();
        DeviceClient client = new DeviceClient(connectionString, protocol, clientOptions);

        System.out.println("Successfully created an IoT Hub client.");

        client.open(true);

        System.out.println("Opened connection to IoT Hub.");
        System.out.println("Sending the following event messages:");

        for (int i = 0; i < numRequests; ++i)
        {
            double temperature = 20 + Math.random() * 10;
            double humidity = 30 + Math.random() * 20;

            String msgStr = "{\"temperature\":"+ temperature +",\"humidity\":"+ humidity +"}";
            
            try
            {
                Message msg = new Message(msgStr);
                msg.setContentType("application/json");
                msg.setProperty("temperatureAlert", temperature > 28 ? "true" : "false");
                msg.setMessageId(java.util.UUID.randomUUID().toString());
                System.out.println(msgStr);

                client.sendEvent(msg, D2C_MESSAGE_TIMEOUT_MILLISECONDS);
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
