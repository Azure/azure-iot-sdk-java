/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.messaging.DeliveryAcknowledgement;
import com.microsoft.azure.sdk.iot.service.messaging.FeedbackReceiver;
import com.microsoft.azure.sdk.iot.service.messaging.FileUploadNotificationReceiver;
import com.microsoft.azure.sdk.iot.service.messaging.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.service.messaging.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.messaging.Message;
import com.microsoft.azure.sdk.iot.service.messaging.ServiceClient;
import com.microsoft.azure.sdk.iot.service.registry.Device;
import com.microsoft.azure.sdk.iot.service.registry.RegistryManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    /**
     * @param args Unused
     * @throws Exception if any exception occurs
     */
    public static void main(String[] args) throws Exception
    {
        System.out.println("********* Starting ServiceClient sample...");

        serviceClient = new ServiceClient(connectionString, protocol);
        openFeedbackReceiver();
        openFileUploadNotificationReceiver();

        // Sending multiple commands
        try
        {
            sendMultipleCommands();
        }
        catch(UnsupportedEncodingException | InterruptedException e)
        {
            System.out.println("Exception:" + e.getMessage());
        }

        System.out.println("********* Sleeping main thread while waiting for file upload notifications and/or feedback batch messages...");
        Thread.sleep(5000);

        fileUploadNotificationReceiver.close();
        feedbackReceiver.close();

        System.out.println("********* Shutting down ServiceClient sample...");
    }

    protected static void openFeedbackReceiver() throws IOException
    {
        feedbackReceiver = serviceClient.getFeedbackReceiver(
            feedbackBatch ->
            {
                System.out.println(" Feedback received, feedback time: " + feedbackBatch.getEnqueuedTimeUtc());
                System.out.println(" Record size: " + feedbackBatch.getRecords().size());

                for (int i = 0; i < feedbackBatch.getRecords().size(); i++)
                {
                    System.out.println(" Message Id : " + feedbackBatch.getRecords().get(i).getOriginalMessageId());
                    System.out.println(" Device Id : " + feedbackBatch.getRecords().get(i).getDeviceId());
                    System.out.println(" Status description : " + feedbackBatch.getRecords().get(i).getDescription());
                }

                return IotHubMessageResult.COMPLETE;
            }
        );
        feedbackReceiver.open();
        System.out.println("********* Successfully opened FeedbackReceiver...");
    }

    protected static void openFileUploadNotificationReceiver() throws IOException
    {
        fileUploadNotificationReceiver = serviceClient.getFileUploadNotificationReceiver(notification ->
        {
            System.out.println("File Upload notification received");
            System.out.println("Device Id : " + notification.getDeviceId());
            System.out.println("Blob Uri: " + notification.getBlobUri());
            System.out.println("Blob Name: " + notification.getBlobName());
            System.out.println("Last Updated : " + notification.getLastUpdatedTimeDate());
            System.out.println("Blob Size (Bytes): " + notification.getBlobSizeInBytes());
            System.out.println("Enqueued Time: " + notification.getEnqueuedTimeUtcDate());
            return IotHubMessageResult.COMPLETE;
        });
        fileUploadNotificationReceiver.open();
        System.out.println("********* Successfully opened fileUploadNotificationReceiver...");
    }

    protected static void sendMultipleCommands() throws InterruptedException, IOException, IotHubException
    {
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
            serviceClient.send(deviceId, messageToSend);
        }

        System.out.println("All sends completed !");
    }
}
