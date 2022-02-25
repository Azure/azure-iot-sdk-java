/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.service.exceptions.ClientNotOpenException;
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
import com.microsoft.azure.sdk.iot.service.registry.Device;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class MessagingClientSample
{
    private static final String connectionString = System.getenv("IOTHUB_CONNECTION_STRING");
    private static final String deviceId = System.getenv("IOTHUB_DEVICE_ID");

    private static final int NUMBER_OF_MESSAGES_TO_SEND = 10;

    private static final int OPERATION_TIMEOUT_MILLISECONDS = 1000; // timeout for each open/close/send operation

    /** Choose iotHubServiceClientProtocol */
    private static final IotHubServiceClientProtocol protocol = IotHubServiceClientProtocol.AMQPS;
//  private static final IotHubServiceClientProtocol protocol = IotHubServiceClientProtocol.AMQPS_WS;

    public static void main(String[] args) throws InterruptedException, IOException, IotHubException
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
                System.out.println("Encountered an IoT hub level error while sending events " + errorContext.getIotHubException().getMessage());
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

        MessagingClientOptions messagingClientOptions =
            MessagingClientOptions.builder()
                .errorProcessor(errorProcessor)
                .build();

        MessagingClient messagingClient = new MessagingClient(connectionString, protocol, messagingClientOptions);

        int messageCount = 0;

        try
        {
            if (!openMessagingClientWithRetry(messagingClient))
            {
                // exit the sample, but close the connection in the finally block first
                return;
            }

            while (messageCount < NUMBER_OF_MESSAGES_TO_SEND)
            {
                boolean messageSent = false;
                Message messageToSend = new Message(String.valueOf(messageCount));

                while (!messageSent)
                {
                    try
                    {
                        // This is a synchronous method that is used here for simplicity. For higher throughput solutions, see
                        // the use of the async version of this send operation in the MessagingClientPerformanceSample in this repo.
                        messagingClient.send(deviceId, messageToSend, OPERATION_TIMEOUT_MILLISECONDS);
                        messageCount++;
                        messageSent = true;
                    }
                    catch (ClientNotOpenException e)
                    {
                        System.out.println("Client was closed when attempting to send a message. Re-opening the client and trying again");
                        openMessagingClientWithRetry(messagingClient);
                    }
                    catch (IotHubNotFoundException e)
                    {
                        System.out.println("Attempted to send a cloud to device message to a device that does not exist");
                        return;
                    }
                    catch (IotHubDeviceMaximumQueueDepthExceededException e)
                    {
                        System.out.println("Cloud to device message queue limit has been reached, so the new message was rejected");
                    }
                    catch (IotHubMessageTooLargeException e)
                    {
                        System.out.println("Cloud to device message was too large so it was not sent");
                        return;
                    }
                    catch (IotHubException e)
                    {
                        System.out.println("Cloud to device message failed to send due to an IoT hub issue. See error message for more details");
                    }
                    catch (TimeoutException e)
                    {
                        System.out.println("Cloud to device message failed to send because the service did not acknowledge it within the expected amount of time.");
                    }

                    try
                    {
                        //noinspection BusyWait
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e)
                    {
                        System.out.println("Interrupted while waiting to send next message. Exiting sample");
                        return;
                    }
                }
            }
        }
        finally
        {
            messagingClient.close(OPERATION_TIMEOUT_MILLISECONDS);
        }
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
            //noinspection BusyWait
            Thread.sleep(1000);
        }
    }
}
