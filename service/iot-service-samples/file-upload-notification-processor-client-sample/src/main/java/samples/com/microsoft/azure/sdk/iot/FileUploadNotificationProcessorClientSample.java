/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubInternalServerErrorException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubUnathorizedException;
import com.microsoft.azure.sdk.iot.service.messaging.DeliveryAcknowledgement;
import com.microsoft.azure.sdk.iot.service.messaging.ErrorContext;
import com.microsoft.azure.sdk.iot.service.messaging.FeedbackBatch;
import com.microsoft.azure.sdk.iot.service.messaging.FileUploadNotification;
import com.microsoft.azure.sdk.iot.service.messaging.AcknowledgementType;
import com.microsoft.azure.sdk.iot.service.messaging.FileUploadNotificationProcessorClient;
import com.microsoft.azure.sdk.iot.service.messaging.FileUploadNotificationProcessorClientOptions;
import com.microsoft.azure.sdk.iot.service.messaging.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.messaging.Message;
import com.microsoft.azure.sdk.iot.service.messaging.MessageFeedbackProcessorClient;
import com.microsoft.azure.sdk.iot.service.messaging.MessageFeedbackProcessorClientOptions;
import com.microsoft.azure.sdk.iot.service.messaging.FileUploadNotificationProcessorClient;
import com.microsoft.azure.sdk.iot.service.messaging.FileUploadNotificationProcessorClientOptions;
import com.microsoft.azure.sdk.iot.service.registry.Device;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClient;
import com.sun.jna.platform.unix.X11;
import org.apache.logging.log4j.util.SystemPropertiesPropertySource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

public class FileUploadNotificationProcessorClientSample
{
    private static final String connectionString = System.getenv("IOTHUB_CONNECTION_STRING");

    /** Choose iotHubServiceClientProtocol */
    private static final IotHubServiceClientProtocol protocol = IotHubServiceClientProtocol.AMQPS;
//  private static final IotHubServiceClientProtocol protocol = IotHubServiceClientProtocol.AMQPS_WS;

    private static boolean sampleEnded = false;

    public static void main(String[] args) throws InterruptedException
    {
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

        // Run a thread in the background to pick up on user input so they can exit the sample at any time
        new Thread(() ->
        {
            System.out.println("Enter any key to exit");
            new Scanner(System.in).nextLine();
            sampleEnded = true;
            synchronized (connectionEventLock)
            {
                connectionEventLock.notify();
            }
        }).start();

        try
        {
            while (!sampleEnded)
            {
                if (!openFileUploadNotificationProcessorClientWithRetry(fileUploadNotificationProcessorClient))
                {
                    // exit the sample, but close the connection in the finally block first
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
                    System.exit(-1);
                }
            }
        }
        finally
        {
            fileUploadNotificationProcessorClient.stop();
        }
    }

    // return true if the client was opened successfully, false if the client encountered a terminal exception and the sample should stop
    private static boolean openFileUploadNotificationProcessorClientWithRetry(FileUploadNotificationProcessorClient fileUploadNotificationProcessorClient) throws InterruptedException
    {
        while (true)
        {
            try
            {
                System.out.println("Attempting to open the fileUploadNotificationProcessorClient");
                fileUploadNotificationProcessorClient.start();
                System.out.println("Successfully opened the fileUploadNotificationProcessorClient");
                return true;
            }
            catch (IotHubUnathorizedException e)
            {
                System.out.println("Failed to open file upload notification processing client due to invalid or out of date credentials: " + e.getMessage());
                return false;
            }
            catch (IotHubInternalServerErrorException e)
            {
                System.out.println("Failed to open file upload notification processing client due to internal server error: " + e.getMessage());
            }
            catch (IotHubException e)
            {
                System.out.println("Failed to open file upload notification processing client due to hub level issue: " + e.getMessage());
            }
            catch (IOException e)
            {
                System.out.println("Failed to open file upload notification processing client due to network issue: " + e.getMessage());
            }

            System.out.println("Retrying to open file upload notification processing client");
            Thread.sleep(1000);
        }
    }
}
