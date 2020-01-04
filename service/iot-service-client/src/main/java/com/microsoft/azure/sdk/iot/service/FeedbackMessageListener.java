/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpFeedbackListenerHandler;
import com.microsoft.azure.sdk.iot.service.transport.amqps.ReactorRunner;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.reactor.Reactor;

import java.io.IOException;

/**
 * FeedbackReceiver is a specialized receiver whose ReceiveAsync
 * method returns a FeedbackBatch instead of a Message.
 */
public class FeedbackMessageListener
{
    private final String hostName;
    private final String userName;
    private final String sasToken;
    private AmqpFeedbackListenerHandler amqpReceiveHandler;
    private IotHubServiceClientProtocol iotHubServiceClientProtocol;

    public FeedbackMessageListener(String hostName, String userName, String sasToken, IotHubServiceClientProtocol iotHubServiceClientProtocol, FeedbackBatchMessageCallback feedbackBatchMessageCallback)
    {
        Tools.throwIfNullOrEmpty(hostName, "hostName cannot be null or empty");
        Tools.throwIfNullOrEmpty(userName, "userName cannot be null or empty");
        Tools.throwIfNullOrEmpty(sasToken, "sasToken cannot be null or empty");
        Tools.throwIfNull(iotHubServiceClientProtocol, "iotHubServiceClientProtocol cannot be null");
                
        this.hostName = hostName;
        this.userName = userName;
        this.sasToken = sasToken;
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
        amqpReceiveHandler = new AmqpFeedbackListenerHandler(this.hostName, this.userName, this.sasToken, this.iotHubServiceClientProtocol, feedbackBatchMessageCallback);
    }

    public synchronized void open() throws IOException, InterruptedException
    {
        amqpReceiveHandler.open();
    }

    public synchronized void close()
    {
        amqpReceiveHandler.close();
    }
}
