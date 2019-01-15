/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.helpers;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import org.junit.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.microsoft.azure.sdk.iot.common.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;

/*
 * This class contains common code for Junit and Android test cases
 */
public class IotHubServicesCommon
{
    private final static long OPEN_RETRY_TIMEOUT = 5*60*1000; //5 minutes

    //if error injection message has not taken effect after 1 minute, the test will timeout
    private final static long ERROR_INJECTION_MESSAGE_EFFECT_TIMEOUT = 1 * 60 * 1000;

    /*
     * method to send message over given DeviceClient
     */
    public static void sendMessages(InternalClient client,
                                    IotHubClientProtocol protocol,
                                    List<MessageAndResult> messagesToSend,
                                    final long RETRY_MILLISECONDS,
                                    final long SEND_TIMEOUT_MILLISECONDS,
                                    long interMessageDelay,
                                    List<IotHubConnectionStatus> statusUpdates) throws IOException, InterruptedException
    {
        try
        {
            openClientWithRetry(client);
            if (statusUpdates != null)
            {
                confirmOpenStabilized(statusUpdates, 120000, client);
            }

            for (int i = 0; i < messagesToSend.size(); ++i)
            {
                if (isErrorInjectionMessage(messagesToSend.get(i)))
                {
                    //error injection message is not guaranteed to be ack'd by service so it may be re-sent. By setting expiry time,
                    // we ensure that error injection message isn't resent to service too many times. The message will still likely
                    // be sent 3 or 4 times causing 3 or 4 disconnections, but the test should recover anyways.
                    messagesToSend.get(i).message.setExpiryTime(200);
                }

                sendMessageAndWaitForResponse(client, messagesToSend.get(i), RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, protocol);

                if (isErrorInjectionMessage(messagesToSend.get(i)))
                {
                    //wait until error injection message takes affect before sending the next message
                    long startTime = System.currentTimeMillis();
                    while (!statusUpdates.contains(IotHubConnectionStatus.DISCONNECTED_RETRYING))
                    {
                        Thread.sleep(300);

                        if (System.currentTimeMillis() - startTime > ERROR_INJECTION_MESSAGE_EFFECT_TIMEOUT)
                        {
                            Assert.fail(buildExceptionMessage("Sending message over " + protocol + " protocol failed: Error injection message never caused connection to be lost", client));
                        }
                    }
                }
                else
                {
                    Thread.sleep(interMessageDelay);
                }
            }
        }
        finally
        {
            client.closeNow();
        }
    }

    public static void sendMessagesExpectingConnectionStatusChangeUpdate(InternalClient client,
                                                                         IotHubClientProtocol protocol,
                                                                         List<MessageAndResult> messagesToSend,
                                                                         final long RETRY_MILLISECONDS,
                                                                         final long SEND_TIMEOUT_MILLISECONDS,
                                                                         final IotHubConnectionStatus expectedStatus,
                                                                         int interMessageDelay,
                                                                         AuthenticationType authType) throws IOException, InterruptedException
    {
        final List<IotHubConnectionStatus> actualStatusUpdates = new ArrayList<>();
        client.registerConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallback()
        {
            @Override
            public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext)
            {
                actualStatusUpdates.add(status);
            }
        }, new Object());

        sendMessages(client, protocol, messagesToSend, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, interMessageDelay, actualStatusUpdates);

        Assert.assertTrue(buildExceptionMessage(protocol + ", " + authType + ": Expected connection status update to occur: " + expectedStatus, client), actualStatusUpdates.contains(expectedStatus));
    }

    /**
     * Send some messages that wait for callbacks to signify that the SAS token in the client config has expired.
     *
     * @param client the client to send the messages from
     * @param protocol the protocol the client is using
     */
    public static void sendMessagesExpectingSASTokenExpiration(DeviceClient client,
                                                               String protocol,
                                                               int numberOfMessages,
                                                               long retryMilliseconds,
                                                               long timeoutMilliseconds,
                                                               AuthenticationType authType)
    {
        for (int i = 0; i < numberOfMessages; ++i)
        {
            try
            {
                Message messageToSend = new Message("Test message expecting SAS Token Expired callback for protocol: " + protocol);
                Success messageSent = new Success();
                Success statusUpdated = new Success();

                ConnectionStatusCallback stateCallback = new ConnectionStatusCallback(IotHubConnectionState.SAS_TOKEN_EXPIRED);
                EventCallback messageCallback = new EventCallback(IotHubStatusCode.UNAUTHORIZED);

                client.registerConnectionStateCallback(stateCallback, statusUpdated);
                client.sendEventAsync(messageToSend, messageCallback, messageSent);

                long startTime = System.currentTimeMillis();
                while(!messageSent.wasCallbackFired() || !statusUpdated.getResult())
                {
                    Thread.sleep(retryMilliseconds);
                    if (System.currentTimeMillis() - startTime > timeoutMilliseconds)
                    {
                        Assert.fail(buildExceptionMessage(protocol + ", " + authType + ": Sending message over " + protocol + " protocol failed: " +
                                "never received connection status update for SAS_TOKEN_EXPIRED " +
                                "or never received UNAUTHORIZED message callback", client));
                    }
                }

                if (messageSent.getCallbackStatusCode() != IotHubStatusCode.UNAUTHORIZED)
                {
                    Assert.fail(buildExceptionMessage(protocol + ", " + authType + ": Send messages expecting sas token expiration failed: expected UNAUTHORIZED message callback, but got " + messageSent.getCallbackStatusCode(), client));
                }
            }
            catch (Exception e)
            {
                Assert.fail(buildExceptionMessage(protocol + ", " + authType + ": Sending message over " + protocol + " protocol failed", client));
            }
        }
    }

    /*
     * method to send message over given DeviceClient
     */
    public static void sendMessagesMultiplex(InternalClient client,
                                             IotHubClientProtocol protocol,
                                             final int NUM_MESSAGES_PER_CONNECTION,
                                             final long RETRY_MILLISECONDS,
                                             final long SEND_TIMEOUT_MILLISECONDS)
    {
        String messageString = "Java client e2e test message over " + protocol + " protocol";
        Message msg = new Message(messageString);

        for (int i = 0; i < NUM_MESSAGES_PER_CONNECTION; ++i)
        {
            try
            {
                Success messageSent = new Success();
                EventCallback callback = new EventCallback(IotHubStatusCode.OK_EMPTY);
                client.sendEventAsync(msg, callback, messageSent);

                long startTime = System.currentTimeMillis();
                while (!messageSent.wasCallbackFired())
                {
                    Thread.sleep(RETRY_MILLISECONDS);
                    if (System.currentTimeMillis() - startTime > SEND_TIMEOUT_MILLISECONDS)
                    {
                        Assert.fail(buildExceptionMessage("Timed out waiting for message callback", client));
                    }
                }

                if (messageSent.getCallbackStatusCode() != IotHubStatusCode.OK_EMPTY)
                {
                    Assert.fail(buildExceptionMessage("Sending message over " + protocol + " protocol failed: expected status code OK_EMPTY but received: " + messageSent.getCallbackStatusCode(), client));
                }
            }
            catch (Exception e)
            {
                Assert.fail(buildExceptionMessage("Sending message over " + protocol + " protocol failed: Exception encountered while sending messages: " + e.getMessage(), client));
            }
        }
    }

    public static void sendExpiredMessageExpectingMessageExpiredCallback(InternalClient client,
                                                                         IotHubClientProtocol protocol,
                                                                         final long RETRY_MILLISECONDS,
                                                                         final long SEND_TIMEOUT_MILLISECONDS,
                                                                         AuthenticationType authType) throws IOException
    {
        try
        {
            Message expiredMessage = new Message("This message has expired");
            expiredMessage.setAbsoluteExpiryTime(1); //setting this to 0 causes the message to never expire
            Success messageSentExpiredCallback = new Success();

            openClientWithRetry(client);
            client.sendEventAsync(expiredMessage, new EventCallback(IotHubStatusCode.MESSAGE_EXPIRED), messageSentExpiredCallback);

            long startTime = System.currentTimeMillis();
            while (!messageSentExpiredCallback.wasCallbackFired())
            {
                Thread.sleep(RETRY_MILLISECONDS);
                if (System.currentTimeMillis() - startTime > SEND_TIMEOUT_MILLISECONDS)
                {
                    Assert.fail(buildExceptionMessage(protocol + ", " + authType + ": Timed out waiting for a message callback", client));
                }
            }

            client.closeNow();

            if (messageSentExpiredCallback.getCallbackStatusCode() != IotHubStatusCode.MESSAGE_EXPIRED)
            {
                Assert.fail(buildExceptionMessage("Sending message over " + protocol + " protocol failed: expected status code MESSAGE_EXPIRED but received: " + messageSentExpiredCallback.getCallbackStatusCode(), client));
            }
        }
        catch (Exception e)
        {
            client.closeNow();
            Assert.fail(buildExceptionMessage("Sending expired message over " + protocol + " protocol failed: Exception encountered while sending message and waiting for MESSAGE_EXPIRED callback: " + e.getMessage(), client));
        }
    }

    public static void sendMessagesExpectingUnrecoverableConnectionLossAndTimeout(InternalClient client,
                                                                                  IotHubClientProtocol protocol,
                                                                                  Message errorInjectionMessage,
                                                                                  AuthenticationType authType) throws IOException, InterruptedException
    {
        final List<IotHubConnectionStatus> statusUpdates = new ArrayList<>();
        client.registerConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallback()
        {
            @Override
            public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext) {
                statusUpdates.add(status);
            }
        }, new Object());

        openClientWithRetry(client);

        client.sendEventAsync(errorInjectionMessage, new EventCallback(null), new Success());

        long startTime = System.currentTimeMillis();
        while (!(statusUpdates.contains(IotHubConnectionStatus.DISCONNECTED_RETRYING) && statusUpdates.contains(IotHubConnectionStatus.DISCONNECTED)))
        {
            Thread.sleep(500);

            if (System.currentTimeMillis() - startTime > 30 * 1000)
            {
                break;
            }
        }

        Assert.assertTrue(buildExceptionMessage(protocol + ", " + authType + ": Expected notification about disconnected but retrying.", client), statusUpdates.contains(IotHubConnectionStatus.DISCONNECTED_RETRYING));
        Assert.assertTrue(buildExceptionMessage(protocol + ", " + authType + ": Expected notification about disconnected.", client), statusUpdates.contains(IotHubConnectionStatus.DISCONNECTED));

        client.closeNow();
    }

    public static void sendMessageAndWaitForResponse(InternalClient client, MessageAndResult messageAndResult, long RETRY_MILLISECONDS, long SEND_TIMEOUT_MILLISECONDS, IotHubClientProtocol protocol)
    {
        try
        {
            Success messageSent = new Success();
            EventCallback callback = new EventCallback(messageAndResult.statusCode);
            client.sendEventAsync(messageAndResult.message, callback, messageSent);

            long startTime = System.currentTimeMillis();
            while (!messageSent.wasCallbackFired())
            {
                Thread.sleep(RETRY_MILLISECONDS);
                if (System.currentTimeMillis() - startTime > SEND_TIMEOUT_MILLISECONDS)
                {
                    Assert.fail(buildExceptionMessage("Timed out waiting for a message callback", client));
                    break;
                }
            }

            if (messageAndResult.statusCode != null && messageSent.getCallbackStatusCode() != messageAndResult.statusCode)
            {
                Assert.fail(buildExceptionMessage("Sending message over " + protocol + " protocol failed: expected " + messageAndResult.statusCode + " but received " + messageSent.getCallbackStatusCode(), client));
            }
        }
        catch (Exception e)
        {
            Assert.fail(buildExceptionMessage("Sending message over " + protocol + " protocol failed: Exception encountered while sending and waiting on a message: " + e.getMessage(), client));
        }
    }

    private static boolean isErrorInjectionMessage(MessageAndResult messageAndResult)
    {
        MessageProperty[] properties = messageAndResult.message.getProperties();
        for (int i = 0; i < properties.length; i++)
        {
            if (properties[i].getValue().equals(ErrorInjectionHelper.FaultCloseReason_Boom.toString()) || properties[i].getValue().equals(ErrorInjectionHelper.FaultCloseReason_Bye.toString()))
            {
                return true;
            }
        }

        return false;
    }

    public static void openClientWithRetry(InternalClient client) throws InterruptedException
    {
        //Check again
        int count =0;
        boolean clientOpenSucceeded = false;
        long startTime = System.currentTimeMillis();
        while (!clientOpenSucceeded)
        {
            if (System.currentTimeMillis() - startTime > OPEN_RETRY_TIMEOUT)
            {
                Assert.fail(buildExceptionMessage("Timed out trying to open the client " + count, client));
            }

            try
            {
                count++;
                client.open();
                clientOpenSucceeded = true;
            }
            catch (IOException e)
            {
                //ignore and try again
                System.out.println("Encountered exception while opening device client, retrying...");
                e.printStackTrace();
                Thread.sleep(400);
            }
        }

        System.out.println("Successfully opened connection!");
    }

    public static void openTransportClientWithRetry(TransportClient client, Collection<InternalClient> clients) throws InterruptedException
    {
        boolean clientOpenSucceeded = false;
        long startTime = System.currentTimeMillis();
        while (!clientOpenSucceeded)
        {
            if (System.currentTimeMillis() - startTime > OPEN_RETRY_TIMEOUT)
            {
                Assert.fail(CorrelationDetailsLoggingAssert.buildExceptionMessage("Timed out trying to open the transport client", clients));
            }

            try
            {
                client.open();
                clientOpenSucceeded = true;
            }
            catch (IOException e)
            {
                //ignore and try again
                System.out.println("Encountered exception while opening transport client, retrying...");
                e.printStackTrace();

                try
                {
                    client.closeNow();
                }
                catch (IOException ioException)
                {
                    System.out.println("Failed to close client");
                    ioException.printStackTrace();
                }
                Thread.sleep(400);
            }
        }

        System.out.println("Successfully opened connection!");
    }

    public static void waitForStabilizedConnection(List actualStatusUpdates, long timeout, InternalClient client) throws InterruptedException
    {
        System.out.println("Waiting for stabilized connection...");

        //wait to send the message because we want to ensure that the tcp connection drop happens before the message is received
        long startTime = System.currentTimeMillis();
        long timeElapsed = 0;

        while (!actualStatusUpdates.contains(IotHubConnectionStatus.DISCONNECTED_RETRYING))
        {
            Thread.sleep(200);
            timeElapsed = System.currentTimeMillis() - startTime;

            // 2 minutes timeout waiting for error injection to occur
            if (timeElapsed > timeout)
            {
                Assert.fail(buildExceptionMessage("Timed out waiting for error injection message to take effect", client));
            }
        }

        while (actualStatusUpdates.size() == 0)
        {
            System.out.println("Waiting for a first connection success");
            Thread.sleep(200);

            timeElapsed = System.currentTimeMillis() - startTime;

            // 2 minutes timeout waiting for first connection to occur
            if (timeElapsed > 20000)
            {
                Assert.fail(buildExceptionMessage("Timed out waiting for a first connection success", client));
            }
        }

        int numOfUpdates = 0;
        while (numOfUpdates != actualStatusUpdates.size() || actualStatusUpdates.get(actualStatusUpdates.size()-1) != IotHubConnectionStatus.CONNECTED)
        {
            numOfUpdates = actualStatusUpdates.size();
            Thread.sleep(6 * 1000);
            timeElapsed = System.currentTimeMillis() - startTime;

            // 2 minutes timeout waiting for connection to stabilized
            if (timeElapsed > timeout)
            {
                Assert.fail(buildExceptionMessage("Timed out waiting for a stable connection after error injection", client));
            }
        }

        for (int i=0; i<actualStatusUpdates.size(); i++)
        {
            System.out.println("actualStatusUpdate"+ i +" =" + actualStatusUpdates.get(i));
        }
    }

    public static void confirmOpenStabilized(List actualStatusUpdates, long timeout, InternalClient client) throws InterruptedException
    {
        long startTime = System.currentTimeMillis();
        long timeElapsed;

        int numOfUpdates = 0;
        if (actualStatusUpdates != null)
        {
            while (numOfUpdates == 0 || numOfUpdates != actualStatusUpdates.size() || actualStatusUpdates.get(actualStatusUpdates.size() - 1) != IotHubConnectionStatus.CONNECTED)
            {
                numOfUpdates = actualStatusUpdates.size();
                Thread.sleep(6 * 1000);
                timeElapsed = System.currentTimeMillis() - startTime;

                if (timeElapsed > timeout)
                {
                    Assert.fail(buildExceptionMessage("Timed out waiting for a stable connection on first open", client));
                }
            }
        }
    }
}
