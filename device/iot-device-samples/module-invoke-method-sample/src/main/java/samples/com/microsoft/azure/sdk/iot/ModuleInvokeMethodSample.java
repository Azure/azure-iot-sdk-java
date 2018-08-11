// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Device;
import com.microsoft.azure.sdk.iot.device.edge.MethodRequest;
import com.microsoft.azure.sdk.iot.device.edge.MethodResult;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;

public class ModuleInvokeMethodSample
{
    /**
     * Receives requests from an IoT Hub. Default protocol is to use
     * MQTT transport.
     *
     * @param args 
     * args[0] = IoT Hub connection string
     * args[1] = protocol (one of 'mqtt' or 'amqps' or 'mqtt_ws' or 'amqps_ws')
     * args[2] = the method name
     * args[3] = the string payload to send alongside the method invocation
     * args[4] = the device id of the device to invoke the method on
     * args[5] = (optional) the module id to invoke the method on
     */
    public static void main(String[] args) throws IOException, URISyntaxException, ModuleClientException
    {
        System.out.println("Starting...");
        System.out.println("Beginning setup.");

        if (args.length != 4 && args.length != 5)
        {
            System.out.format(
                    "Expected 4 or 5 arguments but received: %d.\n"
                            + "The program should be called with the following args: \n"
                            + "1. (mqtt | https | amqps | amqps_ws | mqtt_ws)\n"
                            + "2. The name of the method to invoke\n"
                            + "3. The string payload to send alongside the method invocation\n"
                            + "4. The id of the device to invoke the method on\n"
                            + "5. (optional) The id of the module to invoke the method on\n",
                    args.length);
            return;
        }

        IotHubClientProtocol protocol;
        String protocolStr = args[0];
        if (protocolStr.equalsIgnoreCase("https"))
        {
            throw new UnsupportedOperationException("Module Client does not support HTTPS communication");
        }
        else if (protocolStr.equalsIgnoreCase("amqps"))
        {
            protocol = IotHubClientProtocol.AMQPS;
        }
        else if (protocolStr.equalsIgnoreCase("mqtt"))
        {
            protocol = IotHubClientProtocol.MQTT;
        }
        else if (protocolStr.equalsIgnoreCase("amqps_ws"))
        {
            protocol = IotHubClientProtocol.AMQPS_WS;
        }
        else if (protocolStr.equalsIgnoreCase("mqtt_ws"))
        {
            protocol = IotHubClientProtocol.MQTT_WS;
        }
        else
        {
            System.out.format(
                    "Expected argument 2 to be one of 'mqtt', 'mqtt_ws', 'amqps' or 'amqps_ws' but received %s\n",
                    protocolStr);
            return;
        }

        String methodName = args[1];
        String methodPayload = args[2];
        String deviceIdToInvokeOn = args[3];
        String moduleIdToInvokeOn = args.length > 4 ? args[4] : null;

        ModuleClient client = ModuleClient.createFromEnvironment(protocol);
        client.open();

        MethodRequest methodRequest = new MethodRequest(methodName, methodPayload);
        MethodResult result;
        try
        {
            if (moduleIdToInvokeOn == null || moduleIdToInvokeOn.isEmpty())
            {
                result = invokeMethodOnDevice(methodName, deviceIdToInvokeOn, client, methodRequest);
            }
            else
            {
                result = invokeMethodOnModule(methodName, deviceIdToInvokeOn, moduleIdToInvokeOn, client, methodRequest);
            }

            System.out.println("Received response status: " + result.getStatus());
            System.out.println("Received response payload: " + result.getPayloadObject());
        }
        catch (ModuleClientException e)
        {
            System.out.println("Encountered an exception while invoking method");
            e.printStackTrace();
        }
        finally
        {
            client.closeNow();
        }
    }

    private static MethodResult invokeMethodOnDevice(String methodName, String deviceIdToInvokeOn, ModuleClient client, MethodRequest methodRequest) throws ModuleClientException, URISyntaxException, IOException
    {
        System.out.println("Invoking method \"" + methodName + "\" on device \"" + deviceIdToInvokeOn + "\"");
        MethodResult result = client.invokeMethod(deviceIdToInvokeOn, methodRequest);
        System.out.println("Finished Invoking method \"" + methodName + "\" on device \"" + deviceIdToInvokeOn + "\"");
        return result;
    }

    private static MethodResult invokeMethodOnModule(String methodName, String deviceIdToInvokeOn, String moduleIdToInvokeOn, ModuleClient client, MethodRequest methodRequest) throws ModuleClientException, URISyntaxException, IOException
    {
        System.out.println("Invoking method \"" + methodName + "\" on module \"" + moduleIdToInvokeOn + "\"");
        MethodResult result = client.invokeMethod(deviceIdToInvokeOn, moduleIdToInvokeOn, methodRequest);
        System.out.println("Finished Invoking method \"" + methodName + "\" on module \"" + moduleIdToInvokeOn + "\"");
        return result;
    }
}
