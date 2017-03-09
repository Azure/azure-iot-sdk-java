/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.service.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubDeviceMaximumQueueDepthExceededException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.ArrayList;
import java.util.List;

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
    
    private static final int MAX_COMMANDS_TO_SEND = 6; // maximum commands to send in a loop
 
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

        String commandMessage = "Cloud to device message...";

        System.out.println("********* Sending message to device...");

        Message messageToSend = new Message(commandMessage);
        messageToSend.setDeliveryAcknowledgement(DeliveryAcknowledgement.Full);

        // Setting standard properties
        messageToSend.setMessageId(java.util.UUID.randomUUID().toString());
        Date now = new Date();
        messageToSend.setExpiryTimeUtc(new Date(now.getTime() + 60 * 1000));
        messageToSend.setCorrelationId(java.util.UUID.randomUUID().toString());
        messageToSend.setUserId(java.util.UUID.randomUUID().toString());
        messageToSend.clearCustomProperties();

        // Setting user properties
        Map<String, String> propertiesToSend = new HashMap<String, String>();
        propertiesToSend.put("mycustomKey1", "mycustomValue1");
        propertiesToSend.put("mycustomKey2", "mycustomValue2");
        propertiesToSend.put("mycustomKey3", "mycustomValue3");
        propertiesToSend.put("mycustomKey4", "mycustomValue4");
        propertiesToSend.put("mycustomKey5", "mycustomValue5");
        messageToSend.setProperties(propertiesToSend);

        CompletableFuture<Void> completableFuture = serviceClient.sendAsync(deviceId, messageToSend);
        try
        {
            completableFuture.get();
        }
        catch (ExecutionException e)
        {
            System.out.println("Exception : " + e.getCause());
            return;
        }

        System.out.println("********* Waiting for feedback...");
        CompletableFuture<FeedbackBatch> future = feedbackReceiver.receiveAsync();
        FeedbackBatch feedbackBatch = future.get();

        if (feedbackBatch != null)
        {
            System.out.println("********* Feedback received, feedback time: " + feedbackBatch.getEnqueuedTimeUtc().toString());
        }

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
            feedbackReceiver = serviceClient.getFeedbackReceiver(deviceId);
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
        CompletableFuture<FeedbackBatch> future = feedbackReceiver.receiveAsync();
        FeedbackBatch feedbackBatch = future.get();

        if (feedbackBatch != null) // check if any feedback was received
        {
            System.out.println("Record count: " + feedbackBatch.getRecords().size());
            for (int i=0; i < feedbackBatch.getRecords().size(); i++)
            {
                System.out.println("Messsage id get: " + feedbackBatch.getRecords().get(i).getOriginalMessageId());
            }
        }
    }
}
