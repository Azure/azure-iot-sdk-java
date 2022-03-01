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
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubUnauthorizedException;
import com.microsoft.azure.sdk.iot.service.messaging.ErrorContext;
import com.microsoft.azure.sdk.iot.service.messaging.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.messaging.Message;
import com.microsoft.azure.sdk.iot.service.messaging.MessagingClient;
import com.microsoft.azure.sdk.iot.service.messaging.MessagingClientOptions;
import com.microsoft.azure.sdk.iot.service.messaging.SendResult;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * This sample demonstrates the way to send cloud to device messages with the highest possible throughput by utilizing
 * the asynchronous send operation. This asynchronous send operation allows for many messages to be sent at once and for
 * the service to acknowledge these messages in bulk.
 */
public class MessagingClientPerformanceSample
{
    private static final String connectionString = System.getenv("IOTHUB_CONNECTION_STRING");
    private static final String deviceId = System.getenv("IOTHUB_DEVICE_ID");

    private static final int numberOfMessagesToSend = 50; // a single device can only have up to 50 cloud to device messages queued at a time
    private static int numberOfMessagesSentSuccessfully = 0;
    private static int numberOfMessagesFailedToSend = 0;
    private static final int OPERATION_TIMEOUT_MILLISECONDS = 1000; // timeout for each open/close/send operation

    /** Choose iotHubServiceClientProtocol */
    private static final IotHubServiceClientProtocol protocol = IotHubServiceClientProtocol.AMQPS;
//  private static final IotHubServiceClientProtocol protocol = IotHubServiceClientProtocol.AMQPS_WS;

    public static void main(String[] args) throws InterruptedException
    {
        if (connectionString == null || connectionString.isEmpty())
        {
            throw new IllegalArgumentException("Must provide your IoT Hub's connection string");
        }

        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("Must provide a deviceId");
        }

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

        try
        {
            int messageCount = 0;

            if (!openMessagingClientWithRetry(messagingClient))
            {
                // exit the sample, but close the connection in the finally block first
                return;
            }

            while (messageCount < numberOfMessagesToSend)
            {
                Message messageToSend = new Message(String.valueOf(messageCount));

                boolean messageSent = false;
                while (!messageSent)
                {
                    try
                    {
                        messagingClient.sendAsync(deviceId, messageToSend, sendResultCallback, null);
                        messageCount++;
                        messageSent = true;
                    }
                    catch (IllegalStateException e)
                    {
                        System.out.println("Client was closed when attempting to send a message. Re-opening the client and trying again");
                        if (!openMessagingClientWithRetry(messagingClient))
                        {
                            // exit the sample, but close the connection in the finally block first
                            return;
                        }
                    }
                }
            }

            // wait for all messages to have been sent (successfully or otherwise)
            messagesSentLatch.await();
        }
        finally
        {
            messagingClient.close(OPERATION_TIMEOUT_MILLISECONDS);
        }

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
                System.out.println("Attempting to open the messaging client");
                messagingClient.open(OPERATION_TIMEOUT_MILLISECONDS);
                System.out.println("Successfully opened the messaging client");
                return true;
            }
            catch (IotHubUnauthorizedException e)
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
            catch (TimeoutException e)
            {
                System.out.println("Failed to open messaging client due to service taking too long to respond.");
            }

            System.out.println("Retrying to open messaging client");
            Thread.sleep(1000);
        }
    }
}
