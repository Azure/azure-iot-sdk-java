/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

public class FileUploadNotificationProcessorClientSample
{
    private static final String connectionString = System.getenv("IOTHUB_CONNECTION_STRING");

    /** Choose iotHubServiceClientProtocol */
    private static final IotHubServiceClientProtocol protocol = IotHubServiceClientProtocol.AMQPS;
//  private static final IotHubServiceClientProtocol protocol = IotHubServiceClientProtocol.AMQPS_WS;

    public static void main(String[] args)
    {
        Consumer<ErrorContext> errorProcessor = errorContext ->
        {
            System.out.println("Encountered an error while receiving events " + errorContext.getException().getMessage());
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
            return AcknowledgementType.COMPLETE;
        };

        FileUploadNotificationProcessorClientOptions clientOptions =
            FileUploadNotificationProcessorClientOptions.builder()
                .errorProcessor(errorProcessor)
                .build();

        FileUploadNotificationProcessorClient fileUploadNotificationProcessorClient =
            new FileUploadNotificationProcessorClient(connectionString, protocol, fileUploadNotificationProcessor, clientOptions);

        //TODO have user input end this loop, close the messaging client
        while (true)
        {
            //TODO re-opening after a disconnection event succeeds even with no network? Needs more debugging
            openFileUploadNotificationProcessorClientWithRetry(fileUploadNotificationProcessorClient);

            while (fileUploadNotificationProcessorClient.isRunning())
            {
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                    System.out.println("Interrupted, exiting sample");
                    System.exit(-1);
                }
            }
        }
    }

    private static void openFileUploadNotificationProcessorClientWithRetry(FileUploadNotificationProcessorClient fileUploadNotificationProcessorClient)
    {
        while (true)
        {
            try
            {
                System.out.println("Attempting to open the fileUploadNotificationProcessorClient");
                fileUploadNotificationProcessorClient.start();

                System.out.println("Successfully opened the fileUploadNotificationProcessorClient");
                return;
            }
            catch (IotHubUnathorizedException e)
            {
                System.out.println("Failed to open messaging client due to invalid or out of date credentials: " + e.getMessage());
                System.exit(-1);
            }
            catch (IotHubException e)
            {
                //TODO
                System.out.println("Failed to open messaging client due to hub level issue: " + e.getMessage());
            }
            catch (IOException e)
            {
                System.out.println("Failed to open messaging client due to network issue: " + e.getMessage());
            }
            catch (InterruptedException e)
            {
                System.out.println("Failed to open messaging client because it was interrupted: " + e.getMessage());
            }

            System.out.println("Retrying to open messaging client");
        }
    }
}
