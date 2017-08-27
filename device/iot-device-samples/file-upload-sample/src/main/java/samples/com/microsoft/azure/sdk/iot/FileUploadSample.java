// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * File Upload Sample for an IoT Hub. This is a completed sample
 * that upload files and directories with subdirectories. It is
 * useful to test uploads in parallel.
 */
public class FileUploadSample
{
    private static List<String> fileNameList = new ArrayList<>();

    protected static class FileUploadStatusCallBack implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            System.out.println("IoT Hub responded to file upload for " + fileNameList.get((int)context) + " operation with status " + status.name());
        }
    }

    /**
     * Upload file or directories to blobs using IoT Hub.
     *
     * @param args 
     * args[0] = IoT Hub connection string
     * args[1] = File or directory to upload
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
                            + "[File or Directory to upload] - String containing the full path for the file or directory to upload.\n",
                    args.length);
            return;
        }

        // File upload will always use HTTPS, DeviceClient will use this protocol only
        //   for the other services like Device Telemetry, Device Method and Device Twin.
        IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

        System.out.println("Successfully read input parameters.");
        System.out.format("Using communication protocol %s.\n",
                protocol.name());

        DeviceClient client = new DeviceClient(connString, protocol);

        System.out.println("Successfully created an IoT Hub client.");
        
        try
        {

            uploadFileOrDirectory(client, fullFileName);

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

    private static void uploadFileOrDirectory(DeviceClient client, String fullFileName) throws FileNotFoundException, IOException
    {
        File file = new File(fullFileName);
        if(file.isDirectory())
        {
            uploadFileOrDirectoryRecursive(client, file.getPath(), "");
        }
        else
        {
            uploadFile(client, file.getParent(), file.getName());
        }
    }

    private static void uploadFileOrDirectoryRecursive(DeviceClient client, String baseDirectory, String relativePath) throws FileNotFoundException, IOException
    {
        String[] fileNameList = null;

        File file = new File(baseDirectory, relativePath);
        if(file.isDirectory())
        {
            fileNameList = file.list();
            if(fileNameList != null)
            {
                for (String fileNameInDirectory:fileNameList)
                {
                    File newDir = new File(relativePath, fileNameInDirectory);
                    uploadFileOrDirectoryRecursive(client, baseDirectory, newDir.toString());
                }
            }
        }
        else
        {
            uploadFile(client, baseDirectory, relativePath);
        }
    }

    private static void uploadFile(DeviceClient client, String baseDirectory, String relativeFileName) throws FileNotFoundException, IOException
    {
        File file = new File(baseDirectory, relativeFileName);
        InputStream inputStream = new FileInputStream(file);
        long streamLength = file.length();

        if(relativeFileName.startsWith("\\"))
        {
            relativeFileName = relativeFileName.substring(1);
        }

        int index = fileNameList.size();
        fileNameList.add(relativeFileName);
        client.uploadToBlobAsync(relativeFileName, inputStream, streamLength, new FileUploadStatusCallBack(), index);
    }
}
