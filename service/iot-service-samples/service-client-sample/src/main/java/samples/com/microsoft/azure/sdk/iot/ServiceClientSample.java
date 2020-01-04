/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.service.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubDeviceMaximumQueueDepthExceededException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

    private static ServiceClient serviceClient = null;
    private static FeedbackMessageListenerClient feedbackMessageListenerClient = null;
    private static FileUploadNotificationListenerClient fileUploadNotificationListenerClient = null;

    private static final int MAX_MESSAGES_TO_SEND = 5; // maximum messages to send in a loop
    private static final int SECONDS_TO_WAIT_FOR_FEEDBACK = 10; //wait 10 seconds for feedback messages
    private static final int SECONDS_TO_WAIT_FOR_FILE_UPLOAD_NOTIFICATIONS = 10; //wait 10 seconds for file upload notifications

    /**
     * @param args Unused
     * @throws Exception if any exception occurs
     */
    public static void main(String[] args) throws Exception
    {
        System.out.println("********* Starting ServiceClient sample...");
        serviceClient = ServiceClient.createFromConnectionString(connectionString, protocol);
        feedbackMessageListenerClient = new FeedbackMessageListenerClient(connectionString, protocol, new FeedbackBatchMessageCallbackImpl());
        fileUploadNotificationListenerClient = new FileUploadNotificationListenerClient(connectionString, protocol, new FileUploadNotificationCallbackImpl());

        sendMultipleMessagesAndReadFromTheFeedbackReceiver();

        listenForFileUploadNotifications();

        System.out.println("********* Shutting down ServiceClient sample...");
    }

    protected static void sendMultipleMessagesAndReadFromTheFeedbackReceiver() throws InterruptedException, IOException
    {
        List<CompletableFuture<Void>> futureList = new ArrayList<CompletableFuture<Void>>();
        Map<String, String> propertiesToSend = new HashMap<String, String>();
        String cloudToDeviceMessage = "Cloud to Device Message: ";

        System.out.println("sendMultipleMessagesAndReadFromTheFeedbackReceiver: Send count is : " + MAX_MESSAGES_TO_SEND);

        for (int i = 0; i < MAX_MESSAGES_TO_SEND; i++)
        {
            Message messageToSend = new Message(cloudToDeviceMessage + Integer.toString(i));
            messageToSend.setDeliveryAcknowledgementFinal(DeliveryAcknowledgement.Full);

            // Setting standard properties
            messageToSend.setMessageId(java.util.UUID.randomUUID().toString());
            System.out.println("Message id set: " + messageToSend.getMessageId());

            // Setting user properties
            propertiesToSend.clear();
            propertiesToSend.put("key_" + Integer.toString(i), "value_" + Integer.toString(i));
            messageToSend.setProperties(propertiesToSend);

            // send the message
            CompletableFuture<Void> future = serviceClient.sendAsync(deviceId, messageToSend);
            futureList.add(future);

        }

        System.out.println("Waiting for all sends to be completed...");
        for (CompletableFuture<Void> future : futureList)
        {
            try
            {
                future.get();
            }
            catch (ExecutionException e)
            {
                if (e.getCause() instanceof  IotHubDeviceMaximumQueueDepthExceededException)
                {
                    System.out.println("Maximum queue depth reached");
                }
                else
                {
                    System.out.println("Exception : " + e.getMessage());
                }
            }
        }
        System.out.println("All sends completed !");

        System.out.println("Opening feedback message listener...");
        feedbackMessageListenerClient.open();
        System.out.println("Waiting for the feedback for " + SECONDS_TO_WAIT_FOR_FEEDBACK + " seconds...");

        Thread.sleep(SECONDS_TO_WAIT_FOR_FEEDBACK * 1000);

        System.out.println("Closing feedback message listener");
        feedbackMessageListenerClient.close();
    }

    protected static void listenForFileUploadNotifications() throws IOException, InterruptedException
    {
        System.out.println("Opening file upload notification listener...");
        fileUploadNotificationListenerClient.open();

        System.out.println("Waiting for file upload notifications for " + SECONDS_TO_WAIT_FOR_FILE_UPLOAD_NOTIFICATIONS + " seconds...");
        Thread.sleep(SECONDS_TO_WAIT_FOR_FILE_UPLOAD_NOTIFICATIONS * 1000);

        System.out.println("Closing file upload notification listener...");
        fileUploadNotificationListenerClient.close();
    }

    static class FileUploadNotificationCallbackImpl implements FileUploadNotificationCallback
    {
        @Override
        public DeliveryOutcome onFileUploadNotificationReceived(FileUploadNotification fileUploadNotification) {
            System.out.println("File Upload notification received");
            System.out.println("Device Id : " + fileUploadNotification.getDeviceId());
            System.out.println("Blob Uri: " + fileUploadNotification.getBlobUri());
            System.out.println("Blob Name: " + fileUploadNotification.getBlobName());
            System.out.println("Last Updated : " + fileUploadNotification.getLastUpdatedTimeDate());
            System.out.println("Blob Size (Bytes): " + fileUploadNotification.getBlobSizeInBytes());
            System.out.println("Enqueued Time: " + fileUploadNotification.getEnqueuedTimeUtcDate());

            //Acknowledge the file upload notification as a successful delivery, so that it won't be sent again
            return DeliveryOutcome.Complete;
        }
    }

    static class FeedbackBatchMessageCallbackImpl implements FeedbackBatchMessageCallback
    {
        @Override
        public DeliveryOutcome onFeedbackMessageReceived(FeedbackBatch feedbackBatch) {
            System.out.println(" Feedback received, feedback time: " + feedbackBatch.getEnqueuedTimeUtc());
            System.out.println(" Record size: " + feedbackBatch.getRecords().size());

            for (int i = 0; i < feedbackBatch.getRecords().size(); i++)
            {
                System.out.println(" Messsage id : " + feedbackBatch.getRecords().get(i).getOriginalMessageId());
                System.out.println(" Device id : " + feedbackBatch.getRecords().get(i).getDeviceId());
                System.out.println(" Status description : " + feedbackBatch.getRecords().get(i).getDescription());
            }

            //Acknowledge the message as a successful delivery, so that it won't be sent again
            return DeliveryOutcome.Complete;
        }
    }
}