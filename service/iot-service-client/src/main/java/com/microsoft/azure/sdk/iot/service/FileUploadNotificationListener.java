/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpFileUploadNotificationListenerHandler;
import com.microsoft.azure.sdk.iot.service.transport.amqps.ReactorRunner;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.reactor.Reactor;

import java.io.IOException;

public class FileUploadNotificationListener
{
    FileUploadNotificationCallback fileUploadNotificationCallback;

    private final String hostName;
    private final String userName;
    private final String sasToken;
    private AmqpFileUploadNotificationListenerHandler amqpReceiveHandler;
    private IotHubServiceClientProtocol iotHubServiceClientProtocol;

    /**
     * Constructor to set up connection parameters
     * @param hostName The address string of the service (example: AAA.BBB.CCC)
     * @param userName The username string to use SASL authentication (example: user@sas.service)
     * @param sasToken The SAS token string
     * @param iotHubServiceClientProtocol protocol to use
     */
    public FileUploadNotificationListener(String hostName, String userName, String sasToken, IotHubServiceClientProtocol iotHubServiceClientProtocol, FileUploadNotificationCallback fileUploadNotificationCallback)
    {
        Tools.throwIfNullOrEmpty(hostName, "hostName cannot be null or empty");
        Tools.throwIfNullOrEmpty(userName, "userName cannot be null or empty");
        Tools.throwIfNullOrEmpty(sasToken, "sasToken cannot be null or empty");
        Tools.throwIfNull(iotHubServiceClientProtocol, "iotHubServiceClientProtocol cannot be null");

        this.hostName = hostName;
        this.userName = userName;
        this.sasToken = sasToken;
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
        this.fileUploadNotificationCallback = fileUploadNotificationCallback;

        amqpReceiveHandler = new AmqpFileUploadNotificationListenerHandler(this.hostName, this.userName, this.sasToken, this.iotHubServiceClientProtocol, fileUploadNotificationCallback);
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
