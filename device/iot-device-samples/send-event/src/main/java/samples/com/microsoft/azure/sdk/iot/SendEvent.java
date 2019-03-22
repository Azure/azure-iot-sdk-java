// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/** Sends a number of event messages to an IoT Hub. */
public class SendEvent
{
    protected static class IotHubConnectionStatusChangeCallbackLogger implements IotHubConnectionStatusChangeCallback
    {
        @Override
        public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext)
        {
            System.out.println();
            System.out.println("CONNECTION STATUS UPDATE: " + status);
            System.out.println("CONNECTION STATUS REASON: " + statusChangeReason);
            System.out.println("CONNECTION STATUS THROWABLE: " + (throwable == null ? "null" : throwable.getMessage()));
            System.out.println();

            if (throwable != null)
            {
                throwable.printStackTrace();
            }

            if (status == IotHubConnectionStatus.DISCONNECTED)
            {
                //connection was lost, and is not being re-established. Look at provided exception for
                // how to resolve this issue. Cannot send messages until this issue is resolved, and you manually
                // re-open the device client
            }
            else if (status == IotHubConnectionStatus.DISCONNECTED_RETRYING)
            {
                //connection was lost, but is being re-established. Can still send messages, but they won't
                // be sent until the connection is re-established
            }
            else if (status == IotHubConnectionStatus.CONNECTED)
            {
                //Connection was successfully re-established. Can send messages.
            }
        }
    }

    final static int numberOfMessagesToSend = 1000000;

    //static int messageSizeInBytes = 1; // 1 byte
    static int messageSizeInBytes = 1024; //1 kilobyte
    //static int messageSizeInBytes = 1024 * 32; //32 kilobytes
    //static int messageSizeInBytes = 1024 * 64; //64 kilobytes
    //static int messageSizeInBytes = 1024 * 128; //128 kilobytes
    //static int messageSizeInBytes = 1024 * 255; //255 kilobytes (Max message size allowed for d2c telemetry)

    static long clientSendInterval = 10; //Lower number here spawns send threads more frequently, can send more quickly. By default, value is 10

    static CountDownLatch ackedMessagesCountDownLatch;
    static CountDownLatch sentButNotAckedMessagesCountDownLatch;
    static int sentMessageCount = 0;
    static int ackedMessageCount = 0;

    static DeviceClient client;

    static double[] startTimes = new double[numberOfMessagesToSend];
    static double[] stopTimes = new double[numberOfMessagesToSend];

    private static class SendEventRunnable implements java.lang.Runnable
    {
        private Message messageToSend;
        private EventCallback eventCallback;
        private int messageIndex;

        public SendEventRunnable(Message messageToSend, EventCallback eventCallback, int messageIndex)
        {
            this.messageToSend = messageToSend;
            this.eventCallback = eventCallback;
            this.messageIndex = messageIndex;
        }

        @Override
        public void run()
        {
            startTimes[messageIndex] = System.currentTimeMillis();
            client.sendEventAsync(this.messageToSend, this.eventCallback, this.messageIndex);
            sentButNotAckedMessagesCountDownLatch.countDown();
        }
    }

    protected static class EventCallback implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            //stop time
            long currentTime = System.currentTimeMillis();
            int messageIndex = (int) context;
            stopTimes[messageIndex] = currentTime;

            ackedMessageCount++;
            ackedMessagesCountDownLatch.countDown();
        }
    }

    public static void main(String[] args) throws Exception
    {
        String connString = "";
        client = new DeviceClient(connString, IotHubClientProtocol.AMQPS);

        client.registerConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallbackLogger(), new Object());

        client.setOption("SetSendInterval", clientSendInterval);
        ackedMessagesCountDownLatch = new CountDownLatch(numberOfMessagesToSend);
        sentButNotAckedMessagesCountDownLatch = new CountDownLatch(numberOfMessagesToSend);

        System.out.println("Message size in bytes per send: " + messageSizeInBytes);

        System.out.println("Sending " + numberOfMessagesToSend + " messages");

        byte[] body = new byte[messageSizeInBytes];
        for (int i = 0; i < messageSizeInBytes; i++)
        {
            body[i] = 1;
        }

        final SendEventRunnable[] sendEventRunnables = new SendEventRunnable[numberOfMessagesToSend];
        for (int messageIndex = 0; messageIndex < numberOfMessagesToSend; messageIndex++)
        {
            Message msg = new Message(new String(body.clone()));
            sendEventRunnables[messageIndex] = new SendEventRunnable(msg, new EventCallback(), messageIndex);
        }

        client.open();

        //start up all sender threads
        final long overallStartTime = System.currentTimeMillis();
        for (int sentMessageCount = 0; sentMessageCount < numberOfMessagesToSend; sentMessageCount++)
        {
            new Thread(sendEventRunnables[sentMessageCount]).start();
        }

        if (!sentButNotAckedMessagesCountDownLatch.await(10, TimeUnit.MINUTES))
        {
            throw new Exception("Timed out waiting for all messages to be queued");
        }

        long timestampWhenAllMessagesQueuedButNotNecessarilyAcked = System.currentTimeMillis();

        //wait until all sent messages have been acknowledged by the iot hub, or until 90 minutes have passed
        if (!ackedMessagesCountDownLatch.await(50, TimeUnit.MINUTES))
        {
            throw new Exception("Timed out waiting for all messages to be acknowledged");
        }


        System.out.println();
        System.out.println();
        System.out.println();

        final long overallStopTime = System.currentTimeMillis();

        client.closeNow();

        System.out.println("Seconds taken to queue all messages (disregarding acks): " + ((timestampWhenAllMessagesQueuedButNotNecessarilyAcked - overallStartTime)/1000.0));

        double secondsTaken = ((overallStopTime - overallStartTime) / 1000.0);
        System.out.println("Overall seconds taken: " + secondsTaken + " seconds");

        System.out.println("Average seconds between send and ack per message: " + calculateAverageSecondsBetweenSendAndAck());

        double messagesPerSecond = ackedMessageCount / secondsTaken;
        System.out.println("Messages per second: " + messagesPerSecond);
        System.out.println("Sent messages: " + numberOfMessagesToSend);
    }

    private static double calculateAverageSecondsBetweenSendAndAck()
    {
        double averageTimeTakenPerMessage = 0;
        for (int messageIndex = 0; messageIndex < numberOfMessagesToSend; messageIndex++)
        {
            double secondsTakenOnMessage = (stopTimes[messageIndex] - startTimes[messageIndex]) / 1000;
            averageTimeTakenPerMessage += secondsTakenOnMessage;
        }

        return averageTimeTakenPerMessage / numberOfMessagesToSend;
    }
}
