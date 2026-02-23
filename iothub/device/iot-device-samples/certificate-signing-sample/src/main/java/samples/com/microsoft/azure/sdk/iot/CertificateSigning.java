// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClient;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol;

import java.io.IOException;
import java.net.URISyntaxException;


/** Sends a number of event messages to an IoT Hub. */
public class CertificateSigning
{
    private static String idScope = "<Your DPS instance's id scope>";

    public static void main(String[] args)
            throws IOException, URISyntaxException, InterruptedException, IotHubClientException
    {
        // Certificate signing feature is currently only supported over MQTT/MQTT_WS
        IotHubClientProtocol iotHubProtocol = IotHubClientProtocol.MQTT;
        //IotHubClientProtocol iotHubProtocol = IotHubClientProtocol.MQTT_WS;

        ProvisioningDeviceClientTransportProtocol dpsProtocol = ProvisioningDeviceClientTransportProtocol.MQTT;
        //ProvisioningDeviceClientTransportProtocol dpsProtocol = ProvisioningDeviceClientTransportProtocol.MQTT_WS;


        ProvisioningDeviceClient provisioningDeviceClient = new ProvisioningDeviceClient(
                "global.azure-devices-provisioning.net",
                idScope,
                dpsProtocol,
                securityProvider);
    }
}