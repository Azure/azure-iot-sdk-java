/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.twin.Pair;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import org.junit.Assert;

import java.io.IOException;
import java.util.*;

import static tests.integration.com.microsoft.azure.sdk.iot.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;

/*
 * This class contains common code for Junit and Android test cases
 */
public class IotHubServicesCommon
{
    //if error injection message has not taken effect after 1 minute, the test will timeout
    private final static long ERROR_INJECTION_MESSAGE_EFFECT_TIMEOUT_MILLISECONDS = 60 * 1000;
    private final static String TEST_ASC_SECURITY_MESSAGE = "{ \"AgentVersion\": \"0.0.1\", "
            + "\"AgentId\" : \"{4C1B4747-E4C7-4681-B31D-4B39E390E7F8}\", "
            + "\"MessageSchemaVersion\" : \"1.0\", \"Events\" : "
            + " { \"EventType\": \"Security\", "
            + "\"Category\" : \"Periodic\", "
            + "\"Name\" : \"ListeningPorts\", "
            + "\"IsEmpty\" : true, "
            + "\"PayloadSchemaVersion\" : \"1.0\", "
            + "\"Id\" : \"%s\", "
            + "\"TimestampLocal\" : \"2012-04-23T18:25:43.511Z\", "
            + "\"TimestampUTC\" : \"2012-04-23T18:25:43.511Z\" }, "
            + "\"Payload\": { \"data\": \"test\" } } }";

    private static final int TIMEOUT_MILLISECONDS = 60 * 1000; // 1 minute
    private static final int CHECK_INTERVAL_MILLISECONDS = 300;

    /*
     * method to send message over given DeviceClient
     */
    public static void sendMessages(InternalClient client,
                                    IotHubClientProtocol protocol,
                                    List<MessageAndResult> messagesToSend,
                                    final long RETRY_MILLISECONDS,
                                    final long SEND_TIMEOUT_MILLISECONDS,
                                    long interMessageDelay,
                                    List<Pair<IotHubConnectionStatus, Throwable>> statusUpdates) throws IOException, InterruptedException
    {
        try
        {
            client.open(false);

            for (MessageAndResult messageAndResult : messagesToSend)
            {
                if ((protocol == IotHubClientProtocol.MQTT || protocol == IotHubClientProtocol.MQTT_WS) && isErrorInjectionMessage(messageAndResult))
                {
                    // error injection message will not be ack'd by service if sent over MQTT/MQTT_WS, so the SDK's
                    // retry logic will try to send it again after the connection drops. By setting expiry time,
                    // we ensure that error injection message isn't resent to service too many times. The message will still likely
                    // be sent 3 or 4 times causing 3 or 4 disconnections, but the test should recover anyways.
                    messageAndResult.message.setExpiryTime(1000);

                    // Since the message won't be ack'd, then we don't need to validate the status code when this message's callback is fired
                    messageAndResult.statusCode = null;
                }

                sendMessageAndWaitForResponse(client, messageAndResult, protocol);

                if (isErrorInjectionMessage(messageAndResult))
                {
                    //wait until error injection message takes affect before sending the next message
                    long startTime = System.currentTimeMillis();
                    while (!actualStatusUpdatesContainsStatus(statusUpdates, IotHubConnectionStatus.DISCONNECTED_RETRYING))
                    {
                        Thread.sleep(1000);

                        // send the fault injection message again in case it wasn't sent successfully before
                        sendMessageAndWaitForResponse(client, messageAndResult, protocol);

                        if (System.currentTimeMillis() - startTime > ERROR_INJECTION_MESSAGE_EFFECT_TIMEOUT_MILLISECONDS)
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
            client.close();
        }
    }

    /*
     * method to send message over given DeviceClient
     */
    public static void sendBulkMessages(InternalClient client,
                                    IotHubClientProtocol protocol,
                                    List<MessageAndResult> messagesToSend,
                                    final long RETRY_MILLISECONDS,
                                    final long SEND_TIMEOUT_MILLISECONDS,
                                    long interMessageDelay,
                                    List<Pair<IotHubConnectionStatus, Throwable>> statusUpdates) throws IOException, InterruptedException
    {
        try
        {
            client.open(false);

            if (protocol != IotHubClientProtocol.HTTPS)
            {
                sendMessages(client, protocol, messagesToSend,RETRY_MILLISECONDS ,SEND_TIMEOUT_MILLISECONDS,interMessageDelay, statusUpdates);
                return;
            }

            List<Message> bulkMessages = new ArrayList<>();
            for (MessageAndResult mar : messagesToSend) {
                bulkMessages.add(mar.message);
            }

            BulkMessagesAndResult bulkMessagesAndResult = new BulkMessagesAndResult(bulkMessages, IotHubStatusCode.OK);
            sendBulkMessagesAndWaitForResponse(client, bulkMessagesAndResult, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, protocol);
        }
        finally
        {
            client.close();
        }
    }

    public static void sendSecurityMessages(InternalClient client, IotHubClientProtocol protocol, List<Pair<IotHubConnectionStatus, Throwable>> statusUpdates) throws IOException
    {
        try
        {
            client.open(false);

            // Send the initial message
            MessageAndResult messageToSend = new MessageAndResult(new Message("test message"), IotHubStatusCode.OK);
            sendMessageAndWaitForResponse(client, messageToSend, protocol);

            // Send the security message
            String event_uuid = UUID.randomUUID().toString();
            MessageAndResult securityMessage = new MessageAndResult(new Message(String.format(TEST_ASC_SECURITY_MESSAGE, event_uuid)), IotHubStatusCode.OK);
            securityMessage.message.setAsSecurityMessage();
            sendSecurityMessageAndCheckResponse(client, securityMessage, event_uuid, protocol);
        }
        finally
        {
            client.close();
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
        final List<Pair<IotHubConnectionStatus, Throwable>> actualStatusUpdates = new ArrayList<>();
        client.setConnectionStatusChangeCallback((status, statusChangeReason, throwable, callbackContext) -> actualStatusUpdates.add(new Pair<>(status, throwable)), new Object());

        sendMessages(client, protocol, messagesToSend, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, interMessageDelay, actualStatusUpdates);

        Assert.assertTrue(buildExceptionMessage(protocol + ", " + authType + ": Expected connection status update to occur: " + expectedStatus, client), actualStatusUpdatesContainsStatus(actualStatusUpdates, expectedStatus));
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
                EventCallback callback = new EventCallback(IotHubStatusCode.OK);
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

                if (messageSent.getCallbackStatusCode() != IotHubStatusCode.OK)
                {
                    Assert.fail(buildExceptionMessage("Sending message over " + protocol + " protocol failed: expected status code OK but received: " + messageSent.getCallbackStatusCode(), client));
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

            client.open(false);
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

            client.close();

            if (messageSentExpiredCallback.getCallbackStatusCode() != IotHubStatusCode.MESSAGE_EXPIRED)
            {
                Assert.fail(buildExceptionMessage("Sending message over " + protocol + " protocol failed: expected status code MESSAGE_EXPIRED but received: " + messageSentExpiredCallback.getCallbackStatusCode(), client));
            }
        }
        catch (Exception e)
        {
            client.close();
            Assert.fail(buildExceptionMessage("Sending expired message over " + protocol + " protocol failed: Exception encountered while sending message and waiting for MESSAGE_EXPIRED callback: " + e.getMessage(), client));
        }
    }

    public static void sendMessagesExpectingUnrecoverableConnectionLossAndTimeout(InternalClient client,
                                                                                  IotHubClientProtocol protocol,
                                                                                  Message errorInjectionMessage,
                                                                                  AuthenticationType authType) throws IOException, InterruptedException
    {
        final List<Pair<IotHubConnectionStatus, Throwable>> statusUpdates = new ArrayList<>();
        client.setConnectionStatusChangeCallback((status, statusChangeReason, throwable, callbackContext) -> statusUpdates.add(new Pair<>(status, throwable)), new Object());

        client.open(false);

        client.sendEventAsync(errorInjectionMessage, new EventCallback(null), new Success());

        long startTime = System.currentTimeMillis();
        while (!(actualStatusUpdatesContainsStatus(statusUpdates, IotHubConnectionStatus.DISCONNECTED_RETRYING) && actualStatusUpdatesContainsStatus(statusUpdates, IotHubConnectionStatus.DISCONNECTED)))
        {
            Thread.sleep(500);

            if (System.currentTimeMillis() - startTime > 30 * 1000)
            {
                break;
            }
        }

        Assert.assertTrue(buildExceptionMessage(protocol + ", " + authType + ": Expected notification about disconnected but retrying.", client), actualStatusUpdatesContainsStatus(statusUpdates, IotHubConnectionStatus.DISCONNECTED_RETRYING));
        Assert.assertTrue(buildExceptionMessage(protocol + ", " + authType + ": Expected notification about disconnected.", client), actualStatusUpdatesContainsStatus(statusUpdates, IotHubConnectionStatus.DISCONNECTED));

        client.close();
    }

    public static void sendErrorInjectionMessageAndWaitForResponse(InternalClient client, MessageAndResult messageAndResult, IotHubClientProtocol protocol)
    {
        if (protocol == IotHubClientProtocol.MQTT || protocol == IotHubClientProtocol.MQTT_WS)
        {
            // error injection message will not be ack'd by service if sent over MQTT/MQTT_WS, so the SDK's
            // retry logic will try to send it again after the connection drops. By setting expiry time,
            // we ensure that error injection message isn't resent to service too many times. The message will still likely
            // be sent 3 or 4 times causing 3 or 4 disconnections, but the test should recover anyways.
            messageAndResult.message.setExpiryTime(1000);

            // Since the message won't be ack'd, then we don't need to validate the status code when this message's callback is fired
            messageAndResult.statusCode = null;
        }

        sendMessageAndWaitForResponse(client, messageAndResult, protocol);
    }

    public static void sendMessageAndWaitForResponse(InternalClient client, MessageAndResult messageAndResult, IotHubClientProtocol protocol)
    {
        try
        {
            Success messageSent = new Success();
            EventCallback callback = new EventCallback(messageAndResult.statusCode);
            client.sendEventAsync(messageAndResult.message, callback, messageSent);

            long startTime = System.currentTimeMillis();
            while (!messageSent.wasCallbackFired())
            {
                Thread.sleep(CHECK_INTERVAL_MILLISECONDS);
                if (System.currentTimeMillis() - startTime > TIMEOUT_MILLISECONDS)
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

    public static void sendBulkMessagesAndWaitForResponse(InternalClient client, BulkMessagesAndResult messagesAndResults, long RETRY_MILLISECONDS, long SEND_TIMEOUT_MILLISECONDS, IotHubClientProtocol protocol)
    {
        try
        {
            Success messageSent = new Success();
            EventCallback callback = new EventCallback(messagesAndResults.statusCode);
            client.sendEventAsync(messagesAndResults.messages, callback, messageSent);

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

            if (messagesAndResults.statusCode != null && messageSent.getCallbackStatusCode() != messagesAndResults.statusCode)
            {
                Assert.fail(buildExceptionMessage("Sending message over " + protocol + " protocol failed: expected " + messagesAndResults.statusCode + " but received " + messageSent.getCallbackStatusCode(), client));
            }
        }
        catch (Exception e)
        {
            Assert.fail(buildExceptionMessage("Sending message over " + protocol + " protocol failed: Exception encountered while sending and waiting on a message: " + e.getMessage(), client));
        }
    }

    public static void sendSecurityMessageAndCheckResponse(InternalClient client, MessageAndResult messageAndResult, String eventId, IotHubClientProtocol protocol)
    {
        try
        {
            Success messageSent = new Success();
            EventCallback callback = new EventCallback(messageAndResult.statusCode);
            client.sendEventAsync(messageAndResult.message, callback, messageSent);

            boolean messageFound = true;
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime > TIMEOUT_MILLISECONDS)
            {
                if (messageSent.wasCallbackFired())
                {
                    Assert.fail(buildExceptionMessage("Security message was received by IoTHub and should have been routed to ASC", client));
                    break;
                }
                Thread.sleep(CHECK_INTERVAL_MILLISECONDS);
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
        for (MessageProperty property : properties)
        {
            if (property.getValue().equals(ErrorInjectionHelper.FaultCloseReason_Boom) || property.getValue().equals(ErrorInjectionHelper.FaultCloseReason_Bye))
            {
                return true;
            }
        }

        return false;
    }

    public static void waitForStabilizedConnection(List<Pair<IotHubConnectionStatus, Throwable>> actualStatusUpdates, InternalClient client) throws InterruptedException
    {
        //Wait until error injection takes effect
        long startTime = System.currentTimeMillis();
        while (!actualStatusUpdatesContainsStatus(actualStatusUpdates, IotHubConnectionStatus.DISCONNECTED_RETRYING))
        {
            Thread.sleep(200);
            long timeElapsed = System.currentTimeMillis() - startTime;
            if (timeElapsed > TIMEOUT_MILLISECONDS)
            {
                Assert.fail(buildExceptionMessage("Timed out waiting for error injection message to take effect", client));
            }
        }

        // Wait for first connect
        while (actualStatusUpdates.size() == 0)
        {
            Thread.sleep(200);

            long timeElapsed = System.currentTimeMillis() - startTime;

            // 2 minutes timeout waiting for first connection to occur
            if (timeElapsed > 20000)
            {
                Assert.fail(buildExceptionMessage("Timed out waiting for a first connection success", client));
            }
        }

        confirmOpenStabilized(actualStatusUpdates, client);
    }

    public static void confirmOpenStabilized(List<Pair<IotHubConnectionStatus, Throwable>> actualStatusUpdates, InternalClient client) throws InterruptedException
    {
        long startTime = System.currentTimeMillis();
        long timeElapsed;

        int numOfUpdates = 0;
        if (actualStatusUpdates != null)
        {
            while (numOfUpdates == 0
                    || numOfUpdates != actualStatusUpdates.size()
                    || actualStatusUpdates.get(actualStatusUpdates.size() - 1).getKey() != IotHubConnectionStatus.CONNECTED)
            {
                numOfUpdates = actualStatusUpdates.size();
                Thread.sleep(1000);
                timeElapsed = System.currentTimeMillis() - startTime;

                if (timeElapsed > TIMEOUT_MILLISECONDS)
                {
                    Assert.fail(buildExceptionMessage("Timed out waiting for a stable connection on first open", client));
                }
            }
        }
    }

    public static boolean actualStatusUpdatesContainsStatus(List<Pair<IotHubConnectionStatus, Throwable>> actualStatusUpdates, IotHubConnectionStatus status)
    {
        for (Pair<IotHubConnectionStatus, Throwable> actualStatusUpdate : actualStatusUpdates)
        {
            if (actualStatusUpdate.getKey() == status)
            {
                return true;
            }
        }

        return false;
    }
}
