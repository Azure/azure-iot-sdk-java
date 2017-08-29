/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.MediumTest;
import com.microsoft.azure.sdk.iot.common.iothubservices.SendMessagesCommon;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.Message;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;


@MediumTest
@RunWith(AndroidJUnit4.class)
public class SendMessagesIT
{

    public SendMessagesIT()
    {
    }

    //How much messages each device will send to the hub for each connection.
    private static final Integer NUM_MESSAGES_PER_CONNECTION = 10;

    // How much to wait until a message makes it to the server, in milliseconds
    private static final Integer SEND_TIMEOUT_MILLISECONDS = 60000;

    //How many milliseconds between retry
    private static final Integer RETRY_MILLISECONDS = 100;

    //Device Connection String
    private String connString = "";

    @Test
    public void SendMessagesOverAmqps() throws Exception
    {
        DeviceClient client = new DeviceClient(connString, IotHubClientProtocol.AMQPS);
        SendMessagesCommon.SendMessage(client, NUM_MESSAGES_PER_CONNECTION, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS);
    }

    @Test
    @Ignore
    public void SendMessagesOverAmqps_multithreaded() throws Exception
    {
        Assert.fail("Multithreaded messages over AMQPS protocol not suported");
    }

    @Test
    public void SendMessagesOverMqtt() throws Exception
    {
        String messageString = "Java client e2e test message over Mqtt protocol";
        Message msg = new Message(messageString);
        DeviceClient client = new DeviceClient(connString, IotHubClientProtocol.MQTT);
        SendMessagesCommon.SendMessage(client, NUM_MESSAGES_PER_CONNECTION, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS);
    }

    @Test
    public void SendMessagesOverHttps() throws Exception
    {
        String messageString = "Java client e2e test message over Https protocol";
        Message msg = new Message(messageString);
        DeviceClient client = new DeviceClient(connString, IotHubClientProtocol.HTTPS);
        SendMessagesCommon.SendMessage(client, NUM_MESSAGES_PER_CONNECTION, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS);
    }

}
