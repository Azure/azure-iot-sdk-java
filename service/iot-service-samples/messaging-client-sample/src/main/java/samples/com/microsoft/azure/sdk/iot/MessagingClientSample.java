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
import com.microsoft.azure.sdk.iot.service.registry.RegistryClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.UUID;
import java.util.function.Consumer;

public class MessagingClientSample
{
    private static final String connectionString = System.getenv("IOTHUB_CONNECTION_STRING");
    private static final String deviceId = UUID.randomUUID().toString();

    /** Choose iotHubServiceClientProtocol */
    private static final IotHubServiceClientProtocol protocol = IotHubServiceClientProtocol.AMQPS;
//  private static final IotHubServiceClientProtocol protocol = IotHubServiceClientProtocol.AMQPS_WS;

    private static boolean sampleEnded = false;

    public static void main(String[] args) throws InterruptedException
    {
        // TODO delete
        String deviceId = UUID.randomUUID().toString();
        RegistryClient registryClient = new RegistryClient(connectionString);
        /*try
        {
            registryClient.addDevice(new Device(deviceId));
        }
        catch (IOException | IotHubException e)
        {
            e.printStackTrace();
        }*/
        // TODO delete

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

        // Run a thread in the background to pick up on user input so they can exit the sample at any time
        new Thread(() ->
        {
            System.out.println("Enter any key to exit");
            new Scanner(System.in, StandardCharsets.UTF_8.name()).nextLine();
            sampleEnded = true;
            synchronized (connectionEventLock)
            {
                connectionEventLock.notify();
            }
        }).start();

        int messageCount = 0;

        try
        {
            while (!sampleEnded)
            {
                if (!openMessagingClientWithRetry(messagingClient))
                {
                    // exit the sample, but close the connection in the finally block first
                    return;
                }

                while (!sampleEnded && messagingClient.isOpen())
                {
                    try
                    {
                        Message messageToSend = new Message(String.valueOf(messageCount));

                        // This is a synchronous method that is used here for simplicity. For higher throughput solutions, see
                        // the use of the async version of this send operation in the MessagingClientPerformanceSample in this repo.
                        messagingClient.send(deviceId, messageToSend);
                        messageCount++;
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
                        System.out.println("Cloud to device message was too large");
                    }
                    catch (IOException | IotHubException | InterruptedException e)
                    {
                        //TODO can be more specific for certain errors here.
                        break;
                    }

                    try
                    {
                        Thread.sleep(10000);
                    }
                    catch (InterruptedException e)
                    {
                        System.out.println("Interrupted while waiting to send next message. Exiting sample");
                        System.exit(-1);
                    }
                }
            }
        }
        finally
        {
            messagingClient.close();
        }
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
