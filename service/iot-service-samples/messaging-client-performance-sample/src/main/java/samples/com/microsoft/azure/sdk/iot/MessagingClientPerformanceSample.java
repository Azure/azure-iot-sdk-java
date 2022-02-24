/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.service.exceptions.IotHubDeviceMaximumQueueDepthExceededException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubInternalServerErrorException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubMessageTooLargeException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubNotFoundException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubUnathorizedException;
import com.microsoft.azure.sdk.iot.service.messaging.ErrorContext;
import com.microsoft.azure.sdk.iot.service.messaging.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.messaging.Message;
import com.microsoft.azure.sdk.iot.service.messaging.MessagingClient;
import com.microsoft.azure.sdk.iot.service.messaging.MessagingClientOptions;
import com.microsoft.azure.sdk.iot.service.messaging.SendResult;
import com.microsoft.azure.sdk.iot.service.registry.Device;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClient;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * This sample demonstrates the way to send cloud to device messages with the highest possible throughput by utilizing
 * the asynchronous send operation. This asynchronous send operation allows for many messages to be sent at once and for
 * the service to acknowledge these messages in bulk.
 */
public class MessagingClientPerformanceSample
{
    private static final String connectionString = System.getenv("IOTHUB_CONNECTION_STRING");
    private static final String deviceId = UUID.randomUUID().toString();
    private static final int numberOfMessagesToSend = 50; // a single device can only have up to 50 cloud to device messages queued at a time
    private static int numberOfMessagesSentSuccessfully = 0;
    private static int numberOfMessagesFailedToSend = 0;

    /** Choose iotHubServiceClientProtocol */
    private static final IotHubServiceClientProtocol protocol = IotHubServiceClientProtocol.AMQPS;
//  private static final IotHubServiceClientProtocol protocol = IotHubServiceClientProtocol.AMQPS_WS;

    public static void main(String[] args) throws InterruptedException
    {
        // TODO delete
        String deviceId = UUID.randomUUID().toString();
        RegistryClient registryClient = new RegistryClient(connectionString);
        try
        {
            registryClient.addDevice(new Device(deviceId));
        }
        catch (IOException | IotHubException e)
        {
            e.printStackTrace();
        }
        // TODO delete

        final Object connectionEventLock = new Object();
        Consumer<ErrorContext> errorProcessor = errorContext ->
        {
            if (errorContext.getIotHubException() != null)
            {
                IotHubException messageException = errorContext.getIotHubException();
                System.out.println("Encountered an IoT hub level error while sending events " + messageException.getMessage());
            }
            else
            {
                System.out.println("Encountered a network error while sending events " + errorContext.getNetworkException().getMessage());
            }

            synchronized (connectionEventLock)
            {
                connectionEventLock.notify();
            }
        };

        final CountDownLatch messagesSentLatch = new CountDownLatch(numberOfMessagesToSend);
        Consumer<SendResult> sendResultCallback = sendResult ->
        {
            if (sendResult.wasSentSuccessfully())
            {
                System.out.println("Successfully sent message with correlation id " + sendResult.getCorrelationId());
                numberOfMessagesSentSuccessfully++;
            }
            else
            {
                IotHubException messageException = sendResult.getException();

                if (messageException instanceof IotHubNotFoundException)
                {
                    System.out.println("Failed to send message with correlation id " + sendResult.getCorrelationId() + " because the device it was sent to does not exist");
                }
                else if (messageException instanceof IotHubDeviceMaximumQueueDepthExceededException)
                {
                    System.out.println("Failed to send message with correlation id " + sendResult.getCorrelationId() + " because the device it was sent to has a full queue of cloud to device messages already");
                }
                else if (messageException instanceof IotHubMessageTooLargeException)
                {
                    System.out.println("Failed to send message with correlation id " + sendResult.getCorrelationId() + " because the message was too large");
                }
                else
                {
                    System.out.println("Encountered an IoT hub level error while sending events " + messageException.getMessage());
                }

                numberOfMessagesFailedToSend++;
            }

            messagesSentLatch.countDown();
        };

        MessagingClientOptions messagingClientOptions =
            MessagingClientOptions.builder()
                .errorProcessor(errorProcessor)
                .build();

        MessagingClient messagingClient = new MessagingClient(connectionString, protocol, messagingClientOptions);

        int messageCount = 0;

        while (messageCount < numberOfMessagesToSend)
        {
            if (!openMessagingClientWithRetry(messagingClient))
            {
                // exit the sample, but close the connection in the finally block first
                return;
            }

            while (messagingClient.isOpen() && messageCount < numberOfMessagesToSend)
            {
                Message messageToSend = new Message(String.valueOf(messageCount));
                messagingClient.sendAsync(deviceId, messageToSend, sendResultCallback, null);
                messageCount++;
            }
        }

        // wait for all messages to have been sent (successfully or otherwise)
        messagesSentLatch.await();

        messagingClient.close();

        System.out.println("Successfully sent " + numberOfMessagesSentSuccessfully + " messages");
        System.out.println("Failed to send " + numberOfMessagesFailedToSend + " messages");
    }

    // return true if the client was opened successfully, false if the client encountered a terminal exception and the sample should stop
    private static boolean openMessagingClientWithRetry(MessagingClient messagingClient) throws InterruptedException
    {
        while (true)
        {
            try
            {
                messagingClient.open();
                return true;
            }
            catch (IotHubUnathorizedException e)
            {
                System.out.println("Failed to open messaging client due to invalid or out of date credentials: " + e.getMessage());
                return false;
            }
            catch (IotHubInternalServerErrorException e)
            {
                System.out.println("Failed to open messaging client due to internal server error: " + e.getMessage());
            }
            catch (IotHubException e)
            {
                System.out.println("Failed to open messaging client due to hub level issue: " + e.getMessage());
            }
            catch (IOException e)
            {
                System.out.println("Failed to open messaging client due to network issue: " + e.getMessage());
            }

            System.out.println("Retrying to open messaging client");
            Thread.sleep(1000);
        }
    }
}
