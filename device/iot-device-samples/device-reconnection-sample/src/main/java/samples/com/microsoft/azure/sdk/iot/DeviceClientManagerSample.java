// Copyright (c) Microsoft. All rights reserved.Licensed under the MIT license.
// See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


@Slf4j
public class DeviceClientManagerSample
{
    /**
     * Sends a number of messages to an IoT. Default protocol is to use AMQP transport.
     */
    public static void main(String[] args)
            throws URISyntaxException, IOException, IotHubClientException
    {

        SampleParameters params = new SampleParameters(args);

        log.info("Starting...");
        log.info("Setup parameters...");

        String argDeviceConnectionString = (params.getConnectionStrings())[0];

        IotHubClientProtocol argProtocol;
        String protocol = params.getTransport().toLowerCase();

        switch (protocol)
        {
            case "https":
                argProtocol = IotHubClientProtocol.HTTPS;
                break;
            case "amqps":
                argProtocol = IotHubClientProtocol.AMQPS;
                break;
            case "amqps_ws":
                argProtocol = IotHubClientProtocol.AMQPS_WS;
                break;
            case "mqtt":
                argProtocol = IotHubClientProtocol.MQTT;
                break;
            case "mqtt_ws":
                argProtocol = IotHubClientProtocol.MQTT_WS;
                break;
            default:
                throw new IllegalArgumentException("Unsupported protocol: [" + protocol + "]");
        }
        log.debug("Setup parameter: Protocol = [{}]", protocol);

        DeviceClient client = new DeviceClient(argDeviceConnectionString, argProtocol);
        log.info("Successfully created an IoT Hub client.");

        DeviceClientManager deviceClientManager = new DeviceClientManager(client);

        log.info("Starting IoT Hub client...");
        deviceClientManager.run();
    }
}
