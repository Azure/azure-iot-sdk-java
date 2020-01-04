/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpFeedbackListenerHandler;
import com.microsoft.azure.sdk.iot.service.transport.amqps.ReactorRunner;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.reactor.Reactor;

import java.io.IOException;

/**
 * Abstraction of the AMQP connection used to listen for feedback messages. This class extends a callback to the user
 * that is executed each time a feedback message is received over the connection
 */
@Slf4j
public class FeedbackMessageListenerClient
{
    private final String hostName;
    private final String userName;
    private final IotHubConnectionString iotHubConnectionString;
    private AmqpFeedbackListenerHandler amqpReceiveHandler;
    private IotHubServiceClientProtocol iotHubServiceClientProtocol;

    //By default, generated sas tokens live for one year, but this can be configured
    private static final long DEFAULT_SAS_TOKEN_EXPIRY_TIME = 365*24*60*60;
    private long sasTokenExpiryTime;

    /**
     * Create an instance of a feedback message listener. This instance allows you to open a connection that will receive
     * feedback messages over time. Whenever a feedback message is received, the provided callback will be executed,
     * allowing you to complete/abandon/reject each notification.
     *
     * There is no limit to the number of listener instances that can be created. If multiple listeners are open at the same time,
     * only one will receive a given feedback message at a time, though. Each instance can abandon any received feedback messages
     * if the notification should be received by a different instance.
     *
     * @param connectionString The connection string for the IotHub
     * @param iotHubServiceClientProtocol protocol to use
     * @param feedbackBatchMessageCallback the callback to be executed whenever a feedback message is received
     */
    public FeedbackMessageListenerClient(String connectionString, IotHubServiceClientProtocol iotHubServiceClientProtocol, FeedbackBatchMessageCallback feedbackBatchMessageCallback)
    {
        Tools.throwIfNullOrEmpty(connectionString, "connectionString cannot be null");
        Tools.throwIfNull(iotHubServiceClientProtocol, "iotHubServiceClientProtocol cannot be null");

        try
        {
            this.iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        }
        catch (IOException e)
        {
            //No IO operation is occurring during parsing of the connection string, so the IOException is just confusing. Wrapping
            // it in an IllegalArgumentException instead so that customers don't have to catch an IOException
            throw new IllegalArgumentException("Failed to build iot hub connection string", e);
        }

        this.sasTokenExpiryTime = DEFAULT_SAS_TOKEN_EXPIRY_TIME;
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;

        this.hostName = this.iotHubConnectionString.getHostName();
        this.userName = this.iotHubConnectionString.getUserString();

        Tools.throwIfNullOrEmpty(this.hostName, "The provided connection string is missing a hostName");
        Tools.throwIfNullOrEmpty(this.userName, "The provided connection string is missing a userName");
        amqpReceiveHandler = new AmqpFeedbackListenerHandler(this.hostName, this.userName, this.iotHubServiceClientProtocol, feedbackBatchMessageCallback);
    }

    /**
     * Open the AMQP connection, and start listening for feedback messages. Any feedback messages that arrive
     * will be given to the user through the FileUploadNotificationCallback provided in the constructor
     *
     * @throws IOException If opening the connection fails
     * @throws InterruptedException If the thread gets interrupted while waiting for the connection to open
     */
    public synchronized void open() throws IOException, InterruptedException
    {
        amqpReceiveHandler.open(new IotHubServiceSasToken(iotHubConnectionString, sasTokenExpiryTime).toString());
        log.info("Feedback message listener connection opened");
    }

    /**
     * Close the AMQP connection. No feedback messages will be received on this listener until it is opened again.
     * Closed instances may be re-opened and re-closed any number of times
     */
    public synchronized void close()
    {
        amqpReceiveHandler.close();
        log.info("Feedback message listener connection closed");
    }

    /**
     * Set how long newly generated sas tokens will live for, in seconds. By default, tokens live for 1 year.
     * A sas token is generated each time a connection is opened.
     * @param sasTokenExpiryTime the number of seconds newly generated sas tokens will live for.
     */
    public void setSasTokenExpiryTime(long sasTokenExpiryTime)
    {
        this.sasTokenExpiryTime = sasTokenExpiryTime;
    }
}
