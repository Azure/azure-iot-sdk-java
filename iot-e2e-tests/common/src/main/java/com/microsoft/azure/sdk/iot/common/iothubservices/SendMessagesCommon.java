/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.iothubservices;

import com.microsoft.azure.sdk.iot.common.EventCallback;
import com.microsoft.azure.sdk.iot.common.Success;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
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
    public static void SendMessage(DeviceClient client, final int NUM_MESSAGES_PER_CONNECTION,
                                   final Integer RETRY_MILLISECONDS, final Integer SEND_TIMEOUT_MILLISECONDS)
        throws URISyntaxException, IOException, InterruptedException
    {
        String messageString = "Java client e2e test message over Amqps protocol";
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
                    Assert.fail("Sending message over AMQPS protocol failed");
                }
            } catch (Exception e)
            {
                Assert.fail("Sending message over AMQPS protocol failed");
            }
        }
        client.closeNow();
    }
}
