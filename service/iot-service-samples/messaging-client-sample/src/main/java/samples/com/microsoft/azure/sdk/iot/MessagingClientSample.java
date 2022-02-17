/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubUnathorizedException;
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
import com.microsoft.azure.sdk.iot.service.messaging.MessagingClientOptions;
import com.microsoft.azure.sdk.iot.service.registry.Device;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClient;
import com.sun.jna.platform.unix.X11;
import org.apache.logging.log4j.util.SystemPropertiesPropertySource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

public class MessagingClientSample
{
    private static final String connectionString = System.getenv("IOTHUB_CONNECTION_STRING");
    private static final String deviceId = UUID.randomUUID().toString();

    /** Choose iotHubServiceClientProtocol */
    private static final IotHubServiceClientProtocol protocol = IotHubServiceClientProtocol.AMQPS;
//  private static final IotHubServiceClientProtocol protocol = IotHubServiceClientProtocol.AMQPS_WS;

    public static void main(String[] args)
    {
        AtomicBoolean messagingClientIsOpen = new AtomicBoolean(false);
        Consumer<ErrorContext> errorProcessor = errorContext ->
        {
            System.out.println("Encountered an error while receiving events " + errorContext.getException().getMessage());
            messagingClientIsOpen.set(false);
        };

        MessagingClientOptions messagingClientOptions =
            MessagingClientOptions.builder()
                .errorProcessor(errorProcessor)
                .build();

        MessagingClient messagingClient = new MessagingClient(connectionString, protocol, messagingClientOptions);

        int messageCount = 0;

        //TODO have user input end this loop, close the messaging client
        while (true)
        {
            openMessagingClientWithRetry(messagingClient);
            messagingClientIsOpen.set(true);

            while (messagingClientIsOpen.get())
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

                try
                {
                    Message messageToSend = new Message(String.valueOf(messageCount));
                    messagingClient.send(deviceId, messageToSend); //TODO device not found error case?
                    messageCount++;
                }
                catch (IOException | IotHubException | InterruptedException e)
                {
                    //TODO can be more specific for certain errors here.
                    messagingClientIsOpen.set(false);
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

    private static void openMessagingClientWithRetry(MessagingClient messagingClient)
    {
        while (true)
        {
            try
            {
                messagingClient.open();
                return;
            }
            catch (IotHubUnathorizedException e)
            {
                System.out.println("Failed to open messaging client due to invalid or out of date credentials: " + e.getMessage());
                System.exit(-1);
            }
            catch (IotHubException e)
            {
                //TODO
                System.out.println("Failed to open messaging client due to hub level issue: " + e.getMessage());
            }
            catch (IOException e)
            {
                System.out.println("Failed to open messaging client due to network issue: " + e.getMessage());
            }
            catch (InterruptedException e)
            {
                System.out.println("Failed to open messaging client because it was interrupted: " + e.getMessage());
            }

            System.out.println("Retrying to open messaging client");
        }
    }
}
