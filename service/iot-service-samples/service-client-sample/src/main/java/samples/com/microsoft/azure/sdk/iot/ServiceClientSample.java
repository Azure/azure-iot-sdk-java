/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.service.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubDeviceMaximumQueueDepthExceededException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
    private static FeedbackReceiver feedbackReceiver = null;
    private static FileUploadNotificationReceiver fileUploadNotificationReceiver = null;

    private static final int MAX_COMMANDS_TO_SEND = 5; // maximum commands to send in a loop
    private static final int RECEIVER_TIMEOUT = 10000; // Timeout in ms

    /**
     * @param args Unused
     * @throws Exception if any exception occurs
     */
    public static void main(String[] args) throws Exception
    {
        System.out.println("********* Starting ServiceClient sample...");

        openServiceClient();
        openFeedbackReceiver();
        openFileUploadNotificationReceiver();

        // Sending multiple commands
        try
        {
            sendMultipleCommandsAndReadFromTheFeedbackReceiver();
        }
        catch(UnsupportedEncodingException | InterruptedException e)
        {
            System.out.println("Exception:" + e.getMessage());
        }

        // Receive FileUploadNotification
        FileUploadNotification fileUploadNotification = fileUploadNotificationReceiver.receive(RECEIVER_TIMEOUT);

        if (fileUploadNotification != null)
        {
            System.out.println("File Upload notification received");
            System.out.println("Device Id : " + fileUploadNotification.getDeviceId());
            System.out.println("Blob Uri: " + fileUploadNotification.getBlobUri());
            System.out.println("Blob Name: " + fileUploadNotification.getBlobName());
            System.out.println("Last Updated : " + fileUploadNotification.getLastUpdatedTimeDate());
            System.out.println("Blob Size (Bytes): " + fileUploadNotification.getBlobSizeInBytes());
            System.out.println("Enqueued Time: " + fileUploadNotification.getEnqueuedTimeUtcDate());
        }
        else
        {
            System.out.println("No file upload notification received !");
            closeFileUploadNotificationReceiver();
            closeServiceClient();
            System.out.println("********* Shutting down ServiceClient sample...");
        }

        closeFileUploadNotificationReceiver();
        closeFeedbackReceiver();
        closeServiceClient();

        System.out.println("********* Shutting down ServiceClient sample...");
    }

    protected static void openServiceClient() throws Exception
    {
        System.out.println("Creating ServiceClient...");
        serviceClient = new ServiceClient(connectionString, protocol);

        serviceClient.open();
        System.out.println("********* Successfully created an ServiceClient.");
    }

    protected static void closeServiceClient() throws IOException
    {
        serviceClient.close();

        serviceClient.close();
        serviceClient = null;
        System.out.println("********* Successfully closed ServiceClient.");
    }

    protected static void openFeedbackReceiver() throws IOException
    {
        if (serviceClient != null)
        {
            feedbackReceiver = serviceClient.getFeedbackReceiver();
            if (feedbackReceiver != null)
            {
                feedbackReceiver.open();
                System.out.println("********* Successfully opened FeedbackReceiver...");
            }
        }
    }

    protected static void closeFeedbackReceiver() throws IOException
    {
        feedbackReceiver.close();
        feedbackReceiver = null;
        System.out.println("********* Successfully closed FeedbackReceiver.");
    }


    protected static void openFileUploadNotificationReceiver() throws IOException
    {
        if (serviceClient != null)
        {
            fileUploadNotificationReceiver = serviceClient.getFileUploadNotificationReceiver();
            if (fileUploadNotificationReceiver != null)
            {
                fileUploadNotificationReceiver.open();
                System.out.println("********* Successfully opened fileUploadNotificationReceiver...");
            }
        }
    }

    protected static void closeFileUploadNotificationReceiver() throws IOException
    {
        fileUploadNotificationReceiver.close();
        fileUploadNotificationReceiver = null;
        System.out.println("********* Successfully closed fileUploadNotificationReceiver.");
    }

    protected static void sendMultipleCommandsAndReadFromTheFeedbackReceiver() throws InterruptedException, IOException, IotHubException
    {
        Map<String, String> propertiesToSend = new HashMap<>();
        String commandMessage = "Cloud to device message: ";

        System.out.println("sendMultipleCommandsAndReadFromTheFeedbackReceiver: Send count is : " + MAX_COMMANDS_TO_SEND);

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
            serviceClient.send(deviceId, messageToSend);
        }

        System.out.println("All sends completed !");

        System.out.println("Waiting for the feedback...");
        FeedbackBatch feedbackBatch = feedbackReceiver.receive(); // Default timeout is 60 seconds. [DEFAULT_TIMEOUT_MS = 60000]

        if (feedbackBatch != null) // check if any feedback was received
        {
            System.out.println(" Feedback received, feedback time: " + feedbackBatch.getEnqueuedTimeUtc());
            System.out.println(" Record size: " + feedbackBatch.getRecords().size());

            for (int i=0; i < feedbackBatch.getRecords().size(); i++)
            {
                System.out.println(" Message Id : " + feedbackBatch.getRecords().get(i).getOriginalMessageId());
                System.out.println(" Device Id : " + feedbackBatch.getRecords().get(i).getDeviceId());
                System.out.println(" Status description : " + feedbackBatch.getRecords().get(i).getDescription());
            }
        }
        else
        {
            System.out.println("No feedback received");
        }
    }
}
