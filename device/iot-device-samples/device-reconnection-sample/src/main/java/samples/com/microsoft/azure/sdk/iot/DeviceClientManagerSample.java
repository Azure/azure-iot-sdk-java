// Copyright (c) Microsoft. All rights reserved.Licensed under the MIT license.
// See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;


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
                log.error("This sample is designed to show the best practices for stateful protocols such as AMQPS and MQTT. " +
                        "Since HTTP is not a stateful protocol, this sample should not be used as a reference.");
                return;
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

        log.info("Successfully created an IoT Hub client.");

        DeviceClientManager deviceClientManager = new DeviceClientManager(argDeviceConnectionString, argProtocol);

        log.info("Starting IoT Hub client...");
        deviceClientManager.run();
    }
}
