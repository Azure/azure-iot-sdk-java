/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
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
import com.microsoft.azure.sdk.iot.service.messaging.MessagingClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Service client example for:
 *  - sending message to the device
 *  - waiting and receive feedback from the device
 */
public class ServiceClientSample
{
    private static final String connectionString = "[Connection string goes here]";
    private static final String deviceId = "[Device name goes here]";

    /** Choose iotHubServiceClientProtocol */
    private static final IotHubServiceClientProtocol protocol = IotHubServiceClientProtocol.AMQPS;
//  private static final IotHubServiceClientProtocol protocol = IotHubServiceClientProtocol.AMQPS_WS;

    private static final int MAX_COMMANDS_TO_SEND = 5; // maximum commands to send in a loop

    /**
     * @param args Unused
     * @throws Exception if any exception occurs
     */
    public static void main(String[] args) throws Exception
    {
        System.out.println("********* Starting MessagingClient sample...");


        Function<FeedbackBatch, AcknowledgementType> feedbackMessageProcessor = feedbackBatch ->
        {
            System.out.println(" Feedback received, feedback time: " + feedbackBatch.getEnqueuedTimeUtc());
            System.out.println(" Record size: " + feedbackBatch.getRecords().size());

            for (int i = 0; i < feedbackBatch.getRecords().size(); i++)
            {
                System.out.println(" Message Id : " + feedbackBatch.getRecords().get(i).getOriginalMessageId());
                System.out.println(" Device Id : " + feedbackBatch.getRecords().get(i).getDeviceId());
                System.out.println(" Status description : " + feedbackBatch.getRecords().get(i).getDescription());
            }

            return AcknowledgementType.COMPLETE;
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

        Consumer<ErrorContext> errorProcessor = errorContext ->
        {
            System.out.println("Encountered an error while receiving events " + errorContext.getException().getMessage());
        };

        FileUploadNotificationProcessorClientOptions fileUploadNotificationProcessorClientOptions =
            FileUploadNotificationProcessorClientOptions.builder()
                .errorProcessor(errorProcessor)
                .build();

        FileUploadNotificationProcessorClient fileUploadNotificationProcessorClient =
            new FileUploadNotificationProcessorClient(connectionString, protocol, fileUploadNotificationProcessor, fileUploadNotificationProcessorClientOptions);

        MessageFeedbackProcessorClientOptions messageFeedbackProcessorClientOptions =
            MessageFeedbackProcessorClientOptions.builder()
                .errorProcessor(errorProcessor)
                .build();

        MessageFeedbackProcessorClient messageFeedbackProcessorClient =
            new MessageFeedbackProcessorClient(connectionString, protocol, feedbackMessageProcessor, messageFeedbackProcessorClientOptions);

        fileUploadNotificationProcessorClient.start();
        messageFeedbackProcessorClient.start();

        Thread.sleep(2000);

        // Sending multiple commands
        /*try
        {
            sendMultipleCommands();
        }
        catch(UnsupportedEncodingException | InterruptedException e)
        {
            System.out.println("Exception:" + e.getMessage());
        }
        */

        System.out.println("********* Sleeping main thread while waiting for file upload notifications and/or feedback batch messages...");
        Thread.sleep(1000);

        fileUploadNotificationProcessorClient.stop();
        messageFeedbackProcessorClient.stop();

        System.out.println("********* Shutting down MessagingClient sample...");
    }

    protected static void sendMultipleCommands() throws InterruptedException, IOException, IotHubException
    {
        MessagingClient messagingClient = new MessagingClient(connectionString, protocol);
        messagingClient.open();

        Map<String, String> propertiesToSend = new HashMap<>();
        String commandMessage = "Cloud to device message: ";

        System.out.println("sendMultipleCommands: Send count is : " + MAX_COMMANDS_TO_SEND);

        for (int i = 0; i < MAX_COMMANDS_TO_SEND; i++)
        {
            Message messageToSend = new Message(commandMessage + i);
            messageToSend.setDeliveryAcknowledgement(DeliveryAcknowledgement.Full);

            // Setting standard properties
            messageToSend.setMessageId(java.util.UUID.randomUUID().toString());
            System.out.println("Message id set: " + messageToSend.getMessageId());

            // Setting user properties
            propertiesToSend.clear();
            propertiesToSend.put("key_" + i, "value_" + i);
            messageToSend.setProperties(propertiesToSend);

            // send the message
            messagingClient.send(deviceId, messageToSend);
        }

        messagingClient.close();

        System.out.println("All cloud to device messages sent");
    }
}
