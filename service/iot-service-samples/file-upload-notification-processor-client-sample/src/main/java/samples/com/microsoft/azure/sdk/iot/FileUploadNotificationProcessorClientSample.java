/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubInternalServerErrorException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubUnauthorizedException;
import com.microsoft.azure.sdk.iot.service.messaging.AcknowledgementType;
import com.microsoft.azure.sdk.iot.service.messaging.ErrorContext;
import com.microsoft.azure.sdk.iot.service.messaging.FileUploadNotification;
import com.microsoft.azure.sdk.iot.service.messaging.FileUploadNotificationProcessorClient;
import com.microsoft.azure.sdk.iot.service.messaging.FileUploadNotificationProcessorClientOptions;
import com.microsoft.azure.sdk.iot.service.messaging.IotHubServiceClientProtocol;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Sample code that demonstrates how to start processing file upload notifications from your IoT Hub. For more details
 * on file upload notifications, see <a href="https://docs.microsoft.com/azure/iot-hub/iot-hub-devguide-file-upload#service-file-upload-notifications">this document</a>.
 * This sample also demonstrates best practices for reacting to network instability issues when using this client.
 */
public class FileUploadNotificationProcessorClientSample
{
    private static final String connectionString = System.getenv("IOTHUB_CONNECTION_STRING");

    private static final int OPERATION_TIMEOUT_MILLISECONDS = 1000; // timeout for each start/stop operation

    /** Choose iotHubServiceClientProtocol */
    private static final IotHubServiceClientProtocol protocol = IotHubServiceClientProtocol.AMQPS;
//  private static final IotHubServiceClientProtocol protocol = IotHubServiceClientProtocol.AMQPS_WS;

    public static void main(String[] args) throws InterruptedException
    {
        if (connectionString == null || connectionString.isEmpty())
        {
            throw new IllegalArgumentException("Must provide your IoT Hub's connection string");
        }

        final Object connectionEventLock = new Object();
        Consumer<ErrorContext> errorProcessor = errorContext ->
        {
            if (errorContext.getIotHubException() != null)
            {
                System.out.println("Encountered an IoT hub level error while receiving events " + errorContext.getIotHubException().getMessage());
            }
            else
            {
                System.out.println("Encountered a network error while receiving events " + errorContext.getNetworkException().getMessage());
            }

            synchronized (connectionEventLock)
            {
                // wake up the thread that owns the FileUploadNotificationProcessorClient so that it can restart the processing
                connectionEventLock.notify();
            }
        };

        Function<FileUploadNotification, AcknowledgementType> fileUploadNotificationProcessor = notification ->
        {
            System.out.println("File Upload notification received");
            System.out.println("Device Id : " + notification.getDeviceId());
            System.out.println("Blob Uri: " + notification.getBlobUri());
            System.out.println("Blob Name: " + notification.getBlobName());
            System.out.println("Last Updated : " + notification.getLastUpdatedTimeDate());
            System.out.println("Blob Size (Bytes): " + notification.getBlobSizeInBytes());
            System.out.println("Enqueued Time: " + notification.getEnqueuedTimeUtcDate());

            // The delivered file upload notification will no longer be sent to this or any other file upload
            // notification processor
            return AcknowledgementType.COMPLETE;

            // The delivered file upload notification is made available for re-delivery and can be sent again to this
            // or any other file upload notification processor
            // return AcknowledgementType.ABANDON;
        };

        FileUploadNotificationProcessorClientOptions clientOptions =
            FileUploadNotificationProcessorClientOptions.builder()
                .errorProcessor(errorProcessor)
                .build();

        FileUploadNotificationProcessorClient fileUploadNotificationProcessorClient =
            new FileUploadNotificationProcessorClient(connectionString, protocol, fileUploadNotificationProcessor, clientOptions);

        try
        {
            while (true)
            {
                if (!startFileUploadNotificationProcessorClientWithRetry(fileUploadNotificationProcessorClient))
                {
                    // Fatal error encountered. Exit the sample, but stop the client in the finally block first
                    return;
                }

                try
                {
                    synchronized (connectionEventLock)
                    {
                        connectionEventLock.wait(); // do nothing on this thread until some error occurs within the file upload notification processor client
                    }
                }
                catch (InterruptedException e)
                {
                    System.out.println("Interrupted, exiting sample");
                    return;
                }
            }
        }
        finally
        {
            fileUploadNotificationProcessorClient.stop(OPERATION_TIMEOUT_MILLISECONDS);
        }
    }

    // return true if the client was started successfully, false if the client encountered a terminal exception and the sample should stop
    private static boolean startFileUploadNotificationProcessorClientWithRetry(FileUploadNotificationProcessorClient fileUploadNotificationProcessorClient) throws InterruptedException
    {
        while (true)
        {
            try
            {
                System.out.println("Attempting to start the file upload notification processing client");
                fileUploadNotificationProcessorClient.start(OPERATION_TIMEOUT_MILLISECONDS);
                System.out.println("Successfully started the file upload notification processing client");
                return true;
            }
            catch (IotHubUnauthorizedException e)
            {
                System.out.println("Failed to start file upload notification processing client due to invalid or out of date credentials: " + e.getMessage());
                return false;
            }
            catch (IotHubInternalServerErrorException e)
            {
                System.out.println("Failed to start file upload notification processing client due to internal server error: " + e.getMessage());
            }
            catch (IotHubException e)
            {
                System.out.println("Failed to start file upload notification processing client due to hub level issue: " + e.getMessage());
            }
            catch (IOException e)
            {
                System.out.println("Failed to start file upload notification processing client due to network issue: " + e.getMessage());
            }
            catch (TimeoutException e)
            {
                System.out.println("Failed to start file upload notification processing client due to service taking too long to respond.");
            }

            System.out.println("Retrying to start file upload notification processing client");
            Thread.sleep(1000);
        }
    }
}
