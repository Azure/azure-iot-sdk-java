// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;

/**
 * File Upload Simple Sample for an IoT Hub. This is a sample
 * that upload a single file to blob using the IoT Hub.
 */
public class FileUploadSimpleSample
{
    /**
     * Upload a single file to blobs using IoT Hub.
     *
     * @param args
     * args[0] = IoT Hub connection string
     * args[1] = File to upload
     */
    public static void main(String[] args)
            throws IOException, URISyntaxException
    {
        String connString = null;
        String fullFileName = null;

        System.out.println("Starting...");
        System.out.println("Beginning setup.");


        if (args.length == 2)
        {
            connString = args[0];
            fullFileName = args[1];
        }
        else
        {
            System.out.format(
                    "Expected the following argument but received: %d.\n"
                            + "The program should be called with the following args: \n"
                            + "[Device connection string] - String containing Hostname, Device Id & Device Key in the following formats: HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key>\n"
                            + "[File to upload] - String containing the full path for the file to upload.\n",
                    args.length);
            return;
        }

        // File upload will always use HTTPS, DeviceClient will use this protocol only
        //   for the other services like Telemetry, Device Method and Device Twin.
        IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

        System.out.println("Successfully read input parameters.");
        System.out.format("Using communication protocol %s.\n",
                protocol.name());

        DeviceClient client = new DeviceClient(connString, protocol);

        System.out.println("Successfully created an IoT Hub client.");

        try
        {

            File file = new File(fullFileName);
            if(file.isDirectory())
            {
                throw new IllegalArgumentException(fullFileName + " is a directory, please provide a single file name, or use the FileUploadSample to upload directories.");
            }
            else
            {
                client.uploadToBlobAsync(file.getName(), new FileInputStream(file), file.length(), new FileUploadStatusCallBack(), null);
            }

            System.out.println("File upload started with success");

            System.out.println("Waiting for file upload callback with the status...");
        }
        catch (Exception e)
        {
            System.out.println("On exception, shutting down \n" + " Cause: " + e.getCause() + " \nERROR: " +  e.getMessage());
            System.out.println("Shutting down...");
            client.closeNow();
        }

        System.out.println("Press any key to exit...");

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        System.out.println("Shutting down...");
        client.closeNow();
    }

    protected static class FileUploadStatusCallBack implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            System.out.println("IoT Hub responded to file upload operation with status " + status.name());
        }
    }
}
