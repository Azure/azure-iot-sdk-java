/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubInternalServerErrorException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubUnauthorizedException;
import com.microsoft.azure.sdk.iot.service.messaging.AcknowledgementType;
import com.microsoft.azure.sdk.iot.service.messaging.ErrorContext;
import com.microsoft.azure.sdk.iot.service.messaging.FeedbackBatch;
import com.microsoft.azure.sdk.iot.service.messaging.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.messaging.MessageFeedbackProcessorClient;
import com.microsoft.azure.sdk.iot.service.messaging.MessageFeedbackProcessorClientOptions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Sample code that demonstrates how to start processing cloud to device message feedback. For more details on cloud to
 * device message feedback, see <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-messages-c2d#message-feedback">this document</a>.
 * This sample also demonstrates best practices for reacting to network instability issues when using this client.
 */
public class MessageFeedbackProcessorClientSample
{
    private static final String connectionString = System.getenv("IOTHUB_CONNECTION_STRING");

    private static final int OPERATION_TIMEOUT_MILLISECONDS = 1000; // timeout for each start/stop operation

    /** Choose iotHubServiceClientProtocol */
    private static final IotHubServiceClientProtocol protocol = IotHubServiceClientProtocol.AMQPS;
//  private static final IotHubServiceClientProtocol protocol = IotHubServiceClientProtocol.AMQPS_WS;

    public static void main(String[] args) throws InterruptedException
    {
        if (connectionString == null || connectionString.isEmpty())
        {
            throw new IllegalArgumentException("Must provide your IoT Hub's connection string");
        }

        final Object connectionEventLock = new Object();
        Consumer<ErrorContext> errorProcessor = errorContext ->
        {
            if (errorContext.getIotHubException() != null)
            {
                System.out.println("Encountered an IoT hub level error while receiving message feedback " + errorContext.getIotHubException().getMessage());
            }
            else
            {
                System.out.println("Encountered a network error while receiving message feedback " + errorContext.getNetworkException().getMessage());
            }

            synchronized (connectionEventLock)
            {
                // wake up the thread that owns the MessageFeedbackProcessorClient so that it can restart the processing
                connectionEventLock.notify();
            }
        };

        Function<FeedbackBatch, AcknowledgementType> messageFeedbackProcessor = feedbackBatch ->
        {
            System.out.println(" Feedback received, feedback time: " + feedbackBatch.getEnqueuedTimeUtc());
            System.out.println(" Record size: " + feedbackBatch.getRecords().size());

            for (int i = 0; i < feedbackBatch.getRecords().size(); i++)
            {
                System.out.println(" Message Id : " + feedbackBatch.getRecords().get(i).getOriginalMessageId());
                System.out.println(" Device Id : " + feedbackBatch.getRecords().get(i).getDeviceId());
                System.out.println(" Status description : " + feedbackBatch.getRecords().get(i).getDescription());
            }

            // The delivered feedback will no longer be sent to this or any other message feedback notification processor
            return AcknowledgementType.COMPLETE;

            // The delivered message feedback is made available for re-delivery and can be sent again to this
            // or any other message feedback processor
            // return AcknowledgementType.ABANDON;
        };

        MessageFeedbackProcessorClientOptions clientOptions =
            MessageFeedbackProcessorClientOptions.builder()
                .errorProcessor(errorProcessor)
                .build();

        MessageFeedbackProcessorClient messageFeedbackProcessorClient =
            new MessageFeedbackProcessorClient(connectionString, protocol, messageFeedbackProcessor, clientOptions);

        try
        {
            while (true)
            {
                if (!startMessageFeedbackProcessorClientWithRetry(messageFeedbackProcessorClient))
                {
                    // Fatal error encountered. Exit the sample, but stop the client in the finally block first
                    return;
                }

                try
                {
                    synchronized (connectionEventLock)
                    {
                        connectionEventLock.wait(); // do nothing on this thread until some error occurs within the message feedback processor client
                    }
                }
                catch (InterruptedException e)
                {
                    System.out.println("Interrupted, exiting sample");
                    return;
                }
            }
        }
        finally
        {
            messageFeedbackProcessorClient.stop(OPERATION_TIMEOUT_MILLISECONDS);
        }
    }

    // return true if the client was started successfully, false if the client encountered a terminal exception and the sample should stop
    private static boolean startMessageFeedbackProcessorClientWithRetry(MessageFeedbackProcessorClient messageFeedbackProcessorClient) throws InterruptedException
    {
        while (true)
        {
            try
            {
                System.out.println("Attempting to start the message feedback processing client");
                messageFeedbackProcessorClient.start(OPERATION_TIMEOUT_MILLISECONDS);
                System.out.println("Successfully started the message feedback processing client");
                return true;
            }
            catch (IotHubUnauthorizedException e)
            {
                System.out.println("Failed to start message feedback processing client due to invalid or out of date credentials: " + e.getMessage());
                return false;
            }
            catch (IotHubInternalServerErrorException e)
            {
                System.out.println("Failed to start message feedback processing client due to internal server error: " + e.getMessage());
            }
            catch (IotHubException e)
            {
                System.out.println("Failed to start message feedback processing client due to hub level issue: " + e.getMessage());
            }
            catch (IOException e)
            {
                System.out.println("Failed to start message feedback processing client due to network issue: " + e.getMessage());
            }
            catch (TimeoutException e)
            {
                System.out.println("Failed to start message feedback processing client due to service taking too long to respond.");
            }

            System.out.println("Retrying to start message feedback processing client");
            Thread.sleep(1000);
        }
    }
}
