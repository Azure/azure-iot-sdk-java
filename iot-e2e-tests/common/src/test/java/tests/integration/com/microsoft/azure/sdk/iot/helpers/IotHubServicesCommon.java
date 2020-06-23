/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import org.junit.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static tests.integration.com.microsoft.azure.sdk.iot.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;

/*
 * This class contains common code for Junit and Android test cases
 */
public class IotHubServicesCommon
{
    private final static long OPEN_RETRY_TIMEOUT = 5*60*1000; //5 minutes

    //if error injection message has not taken effect after 1 minute, the test will timeout
    private final static long ERROR_INJECTION_MESSAGE_EFFECT_TIMEOUT = 1 * 60 * 1000;
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
            client.open();

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
                    while (!actualStatusUpdatesContainsStatus(statusUpdates, IotHubConnectionStatus.DISCONNECTED_RETRYING))
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

    public static void sendSecurityMessages(InternalClient client,
                                            IotHubClientProtocol protocol,
                                            final long RETRY_MILLISECONDS,
                                            final long SEND_TIMEOUT_MILLISECONDS,
                                            List<Pair<IotHubConnectionStatus, Throwable>> statusUpdates) throws IOException, InterruptedException
    {
        try
        {
            client.open();

            // Send the initial message
            MessageAndResult messageToSend = new MessageAndResult(new Message("test message"), IotHubStatusCode.OK_EMPTY);
            sendMessageAndWaitForResponse(client, messageToSend, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, protocol);

            // Send the security message
            String event_uuid = UUID.randomUUID().toString();
            MessageAndResult securityMessage = new MessageAndResult(new Message(String.format(TEST_ASC_SECURITY_MESSAGE, event_uuid)), IotHubStatusCode.OK_EMPTY);
            securityMessage.message.setAsSecurityMessage();
            sendSecurityMessageAndCheckResponse(client, securityMessage, event_uuid, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, protocol);
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
        final List<Pair<IotHubConnectionStatus, Throwable>> actualStatusUpdates = new ArrayList<>();
        client.registerConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallback()
        {
            @Override
            public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext)
            {
                actualStatusUpdates.add(new Pair<>(status, throwable));
            }
        }, new Object());

        sendMessages(client, protocol, messagesToSend, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, interMessageDelay, actualStatusUpdates);

        Assert.assertTrue(buildExceptionMessage(protocol + ", " + authType + ": Expected connection status update to occur: " + expectedStatus, client), actualStatusUpdatesContainsStatus(actualStatusUpdates, expectedStatus));
    }

    /**
     * Send some messages that wait for callbacks to signify that the SAS token in the client config has expired.
     *
     * @param client the client to send the messages from
     * @param protocol the protocol the client is using
     * @param numberOfMessages the number of messages to send
     * @param breathPeriod time to sleep between checks of message callback arrival
     * @param timeoutMilliseconds time to wait for any message to have its callback fired before the test times out
     * @param authType the authentication type used is this test
     */
    public static void sendMessagesExpectingSASTokenExpiration(DeviceClient client,
                                                               String protocol,
                                                               int numberOfMessages,
                                                               long breathPeriod,
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
                    Thread.sleep(breathPeriod);
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

            client.open();
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
        final List<Pair<IotHubConnectionStatus, Throwable>> statusUpdates = new ArrayList<>();
        client.registerConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallback()
        {
            @Override
            public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext)
            {
                statusUpdates.add(new Pair<>(status, throwable));
            }
        }, new Object());

        client.open();

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

    public static void sendSecurityMessageAndCheckResponse(InternalClient client, MessageAndResult messageAndResult, String eventId, long RETRY_MILLISECONDS, long TIMEOUT_MILLISECONDS, IotHubClientProtocol protocol)
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
                Thread.sleep(RETRY_MILLISECONDS);
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

    public static void waitForStabilizedConnection(List<Pair<IotHubConnectionStatus, Throwable>> actualStatusUpdates, long timeout, InternalClient client) throws InterruptedException
    {
        //Wait until error injection takes effect
        long startTime = System.currentTimeMillis();
        while (!actualStatusUpdatesContainsStatus(actualStatusUpdates, IotHubConnectionStatus.DISCONNECTED_RETRYING))
        {
            Thread.sleep(200);
            long timeElapsed = System.currentTimeMillis() - startTime;
            if (timeElapsed > timeout)
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

        confirmOpenStabilized(actualStatusUpdates, timeout, client);
    }

    public static void confirmOpenStabilized(List<Pair<IotHubConnectionStatus, Throwable>> actualStatusUpdates, long timeout, InternalClient client) throws InterruptedException
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
                Thread.sleep(3 * 1000);
                timeElapsed = System.currentTimeMillis() - startTime;

                if (timeElapsed > timeout)
                {
                    Assert.fail(buildExceptionMessage("Timed out waiting for a stable connection on first open", client));
                }
            }
        }
    }

    public static boolean actualStatusUpdatesContainsStatus(List<Pair<IotHubConnectionStatus, Throwable>> actualStatusUpdates, IotHubConnectionStatus status)
    {
        for (int i = 0; i < actualStatusUpdates.size(); i++)
        {
            if (actualStatusUpdates.get(i).getKey() == status)
            {
                return true;
            }
        }

        return false;
    }
}
