/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.service.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubDeviceMaximumQueueDepthExceededException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
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
     * @param args
     * @throws IOException
     * @throws URISyntaxException
     */
    public static void main(String[] args) throws IOException, URISyntaxException, Exception
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
        catch(UnsupportedEncodingException e)
        {
           System.out.println("Exception:" + e.getMessage());
        } catch (InterruptedException e) 
        {
            System.out.println("Exception:" + e.getMessage());
        } catch (ExecutionException e) 
        {
            System.out.println("Exception:" + e.getMessage());
        }

        // Receive FileUploadNotification
        CompletableFuture<FileUploadNotification> fileUploadNotificationCompletableFuture = fileUploadNotificationReceiver.receiveAsync(RECEIVER_TIMEOUT);
        FileUploadNotification fileUploadNotification = fileUploadNotificationCompletableFuture.get();

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
            System.exit(0);
        }

        closeFileUploadNotificationReceiver();
        closeFeedbackReceiver();
        closeServiceClient();

        System.out.println("********* Shutting down ServiceClient sample...");
        System.exit(0);
    }

    protected static void openServiceClient() throws Exception
    {
        System.out.println("Creating ServiceClient...");
        serviceClient = ServiceClient.createFromConnectionString(connectionString, protocol);

        CompletableFuture<Void> future = serviceClient.openAsync();
        future.get();
        System.out.println("********* Successfully created an ServiceClient.");
    }

    protected static void closeServiceClient() throws ExecutionException, InterruptedException, IOException
    {
        serviceClient.close();

        CompletableFuture<Void> future = serviceClient.closeAsync();
        future.get();
        serviceClient = null;
        System.out.println("********* Successfully closed ServiceClient.");
    }

    protected static void openFeedbackReceiver() throws ExecutionException, InterruptedException
    {
        if (serviceClient != null)
        {
            feedbackReceiver = serviceClient.getFeedbackReceiver();
            if (feedbackReceiver != null)
            {
                CompletableFuture<Void> future = feedbackReceiver.openAsync();
                future.get();
                System.out.println("********* Successfully opened FeedbackReceiver...");
            }
        }
    }

    protected static void closeFeedbackReceiver() throws ExecutionException, InterruptedException
    {
        CompletableFuture<Void> future = feedbackReceiver.closeAsync();
        future.get();
        feedbackReceiver = null;
        System.out.println("********* Successfully closed FeedbackReceiver.");
    }


    protected static void openFileUploadNotificationReceiver() throws ExecutionException, InterruptedException
    {
        if (serviceClient != null)
        {
            fileUploadNotificationReceiver = serviceClient.getFileUploadNotificationReceiver();
            if (fileUploadNotificationReceiver != null)
            {
                CompletableFuture<Void> future = fileUploadNotificationReceiver.openAsync();
                future.get();
                System.out.println("********* Successfully opened fileUploadNotificationReceiver...");
            }
        }
    }

    protected static void closeFileUploadNotificationReceiver() throws ExecutionException, InterruptedException
    {
        CompletableFuture<Void> future = fileUploadNotificationReceiver.closeAsync();
        future.get();
        fileUploadNotificationReceiver = null;
        System.out.println("********* Successfully closed fileUploadNotificationReceiver.");
    }
    
     protected static void sendMultipleCommandsAndReadFromTheFeedbackReceiver() throws ExecutionException, InterruptedException, UnsupportedEncodingException
     {
        List<CompletableFuture<Void>> futureList = new ArrayList<CompletableFuture<Void>>();
        Map<String, String> propertiesToSend = new HashMap<String, String>();
        String commandMessage = "Cloud to Device Message: "; 
        
        System.out.println("sendMultipleCommandsAndReadFromTheFeedbackReceiver: Send count is : " + MAX_COMMANDS_TO_SEND);

        for (int i = 0; i < MAX_COMMANDS_TO_SEND; i++)
        {
            Message messageToSend = new Message(commandMessage + Integer.toString(i));
            messageToSend.setDeliveryAcknowledgement(DeliveryAcknowledgement.Full);

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

        System.out.println("Waiting for the feedback...");
        CompletableFuture<FeedbackBatch> future = feedbackReceiver.receiveAsync(); // Default timeout is 60 seconds. [DEFAULT_TIMEOUT_MS = 60000]
        FeedbackBatch feedbackBatch = future.get(); 

        if (feedbackBatch != null) // check if any feedback was received
        {
            System.out.println(" Feedback received, feedback time: " + feedbackBatch.getEnqueuedTimeUtc());
            System.out.println(" Record size: " + feedbackBatch.getRecords().size());
            
            for (int i=0; i < feedbackBatch.getRecords().size(); i++)
            {
                System.out.println(" Messsage id : " + feedbackBatch.getRecords().get(i).getOriginalMessageId());
                System.out.println(" Device id : " + feedbackBatch.getRecords().get(i).getDeviceId());
                System.out.println(" Status description : " + feedbackBatch.getRecords().get(i).getDescription());
            }
        }
        else
        {
            System.out.println("No feedback received");
        }
    }
}
