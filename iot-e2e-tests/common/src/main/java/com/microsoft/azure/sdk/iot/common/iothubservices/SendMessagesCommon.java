/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.iothubservices;

import com.microsoft.azure.sdk.iot.common.ConnectionStatusCallback;
import com.microsoft.azure.sdk.iot.common.EventCallback;
import com.microsoft.azure.sdk.iot.common.Success;
import com.microsoft.azure.sdk.iot.device.*;
import org.junit.Assert;

import java.io.IOException;
import java.net.URISyntaxException;

/*
 * This class contains common code for Junit and Andorid test cases
 */
public class SendMessagesCommon
{

    /*
     * method to send message over given DeviceClient
     */
    public static void sendMessages(DeviceClient client,
                                    IotHubClientProtocol protocol,
                                    final int NUM_MESSAGES_PER_CONNECTION,
                                    final Integer RETRY_MILLISECONDS,
                                    final Integer SEND_TIMEOUT_MILLISECONDS) throws IOException
    {
        String messageString = "Java client e2e test message over " + protocol + " protocol";
        Message msg = new Message(messageString);
        client.open();

        for (int i = 0; i < NUM_MESSAGES_PER_CONNECTION; ++i)
        {
            try
            {
                Success messageSent = new Success();
                EventCallback callback = new EventCallback(IotHubStatusCode.OK_EMPTY);
                client.sendEventAsync(msg, callback, messageSent);

                Integer waitDuration = 0;
                while (!messageSent.getResult())
                {
                    Thread.sleep(RETRY_MILLISECONDS);
                    if ((waitDuration += RETRY_MILLISECONDS) > SEND_TIMEOUT_MILLISECONDS)
                    {
                        break;
                    }
                }

                if (!messageSent.getResult())
                {
                    Assert.fail("Sending message over " + protocol + " protocol failed");
                }
            }
            catch (Exception e)
            {
                Assert.fail("Sending message over " + protocol + " protocol failed");
            }
        }
        client.closeNow();
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
                                                               Integer retryMilliseconds,
                                                               long timeoutMilliseconds)
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

                Integer waitDuration = 0;
                boolean timedOut = false;
                while(!messageSent.getResult() || !statusUpdated.getResult())
                {
                    Thread.sleep(retryMilliseconds);
                    if ((waitDuration += retryMilliseconds) > timeoutMilliseconds)
                    {
                        timedOut = true;
                        break;
                    }
                }

                if (timedOut)
                {
                    Assert.fail("Sending message over " + protocol + " protocol failed");
                }
            }
            catch (Exception e)
            {
                Assert.fail("Sending message over " + protocol + " protocol failed");
            }
        }
    }

    /*
     * method to send message over given DeviceClient
     */
    public static void sendMessagesMultiplex(DeviceClient client,
                                             IotHubClientProtocol protocol,
                                             final int NUM_MESSAGES_PER_CONNECTION,
                                             final Integer RETRY_MILLISECONDS,
                                             final Integer SEND_TIMEOUT_MILLISECONDS) throws IOException
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

                Integer waitDuration = 0;
                while (!messageSent.getResult())
                {
                    Thread.sleep(RETRY_MILLISECONDS);
                    if ((waitDuration += RETRY_MILLISECONDS) > SEND_TIMEOUT_MILLISECONDS)
                    {
                        break;
                    }
                }

                if (!messageSent.getResult())
                {
                    Assert.fail("Sending message over " + protocol + " protocol failed");
                }
            }
            catch (Exception e)
            {
                Assert.fail("Sending message over " + protocol + " protocol failed");
            }
        }
    }

    public static void sendExpiredMessageExpectingMessageExpiredCallback(String iotHubConnectionString,
                                                                         IotHubClientProtocol protocol,
                                                                         final Integer RETRY_MILLISECONDS,
                                                                         final Integer SEND_TIMEOUT_MILLISECONDS) throws IOException, URISyntaxException
    {
        DeviceClient client = new DeviceClient(iotHubConnectionString, protocol);
        try
        {
            Message expiredMessage = new Message("This message has expired");
            expiredMessage.setAbsoluteExpiryTime(1); //setting this to 0 causes the message to never expire
            Success messageSentExpiredCallback = new Success();

            client.open();
            client.sendEventAsync(expiredMessage, new EventCallback(IotHubStatusCode.MESSAGE_EXPIRED), messageSentExpiredCallback);

            Integer waitDuration = 0;
            while (!messageSentExpiredCallback.getResult())
            {
                Thread.sleep(RETRY_MILLISECONDS);
                if ((waitDuration += RETRY_MILLISECONDS) > SEND_TIMEOUT_MILLISECONDS)
                {
                    break;
                }
            }

            client.closeNow();

            if (!messageSentExpiredCallback.getResult())
            {
                Assert.fail("Sending expired message over " + protocol + " protocol failed");
            }
        }
        catch (Exception e)
        {
            client.closeNow();
            Assert.fail("Sending expired message over " + protocol + " protocol failed");
        }
    }
}
