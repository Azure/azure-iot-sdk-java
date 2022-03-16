// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.microsoft.azure.sdk.iot.device.FileUploadCompletionNotification;
import com.microsoft.azure.sdk.iot.device.FileUploadSasUriRequest;
import com.microsoft.azure.sdk.iot.device.FileUploadSasUriResponse;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
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
        String connString;
        String fullFileName;

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

        DeviceClient client = new DeviceClient(connString, protocol);

        System.out.println("Successfully created an IoT Hub client.");

        try
        {
            File file = new File(fullFileName);
            if (file.isDirectory())
            {
                throw new IllegalArgumentException(fullFileName + " is a directory, please provide a single file name, or use the FileUploadSample to upload directories.");
            }

            System.out.println("Retrieving SAS URI from IoT Hub...");
            FileUploadSasUriResponse sasUriResponse = client.getFileUploadSasUri(new FileUploadSasUriRequest(file.getName()));

            System.out.println("Successfully got SAS URI from IoT Hub");
            System.out.println("Correlation Id: " + sasUriResponse.getCorrelationId());
            System.out.println("Container name: " + sasUriResponse.getContainerName());
            System.out.println("Blob name: " + sasUriResponse.getBlobName());
            System.out.println("Blob Uri: " + sasUriResponse.getBlobUri());

            System.out.println("Using the Azure Storage SDK to upload file to Azure Storage...");

            try
            {
                BlobClient blobClient =
                    new BlobClientBuilder()
                        .endpoint(sasUriResponse.getBlobUri().toString())
                        .buildClient();

                blobClient.uploadFromFile(fullFileName);
            }
            catch (Exception e)
            {
                System.out.println("Exception encountered while uploading file to blob: " + e.getMessage());

                System.out.println("Failed to upload file to Azure Storage.");

                System.out.println("Notifying IoT Hub that the SAS URI can be freed and that the file upload failed.");

                // Note that this is done even when the file upload fails. IoT Hub has a fixed number of SAS URIs allowed active
                // at any given time. Once you are done with the file upload, you should free your SAS URI so that other
                // SAS URIs can be generated. If a SAS URI is not freed through this API, then it will free itself eventually
                // based on how long SAS URIs are configured to live on your IoT Hub.
                FileUploadCompletionNotification completionNotification = new FileUploadCompletionNotification(sasUriResponse.getCorrelationId(), false);
                client.completeFileUpload(completionNotification);

                System.out.println("Notified IoT Hub that the SAS URI can be freed and that the file upload was a failure.");

                client.close();
                return;
            }

            System.out.println("Successfully uploaded file to Azure Storage.");

            System.out.println("Notifying IoT Hub that the SAS URI can be freed and that the file upload was a success.");
            FileUploadCompletionNotification completionNotification = new FileUploadCompletionNotification(sasUriResponse.getCorrelationId(), true);
            client.completeFileUpload(completionNotification);
            System.out.println("Successfully notified IoT Hub that the SAS URI can be freed, and that the file upload was a success");
        }
        catch (Exception e)
        {
            System.out.println("On exception, shutting down \n" + " Cause: " + e.getCause() + " \nERROR: " +  e.getMessage());
            System.out.println("Shutting down...");
            client.close();
        }

        System.out.println("Press any key to exit...");

        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name());
        scanner.nextLine();
        System.out.println("Shutting down...");
        client.close();
    }
}
