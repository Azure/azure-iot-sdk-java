/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadNotificationParser;
import com.microsoft.azure.sdk.iot.deps.auth.TokenCredentialType;
import com.microsoft.azure.sdk.iot.service.FileUploadNotification;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import java.io.IOException;

/**
 * Instance of the QPID-Proton-J BaseHandler class
 * overriding the events what are needed to handle
 * high level open, close methods and feedback received event.
 */
@Slf4j
public class AmqpFileUploadNotificationReceive implements AmqpFeedbackReceivedEvent
{
    private final String hostName;
    private String userName;
    private String sasToken;
    private TokenCredential authenticationTokenProvider;
    private TokenCredentialType authorizationType;
    private AmqpFileUploadNotificationReceivedHandler amqpReceiveHandler;
    private FileUploadNotification fileUploadNotification;
    private final IotHubServiceClientProtocol iotHubServiceClientProtocol;
    private final ProxyOptions proxyOptions;
    private final SSLContext sslContext;

    /**
     * Constructor to set up connection parameters
     * @param hostName The address string of the service (example: AAA.BBB.CCC)
     * @param userName The username string to use SASL authentication (example: user@sas.service)
     * @param sasToken The SAS token string
     * @param iotHubServiceClientProtocol protocol to use
     */
    public AmqpFileUploadNotificationReceive(
            String hostName,
            String userName,
            String sasToken,
            IotHubServiceClientProtocol iotHubServiceClientProtocol)
    {
        this(hostName, userName, sasToken, iotHubServiceClientProtocol, null);
    }

    /**
     * Constructor to set up connection parameters
     * @param hostName The address string of the service (example: AAA.BBB.CCC)
     * @param userName The username string to use SASL authentication (example: user@sas.service)
     * @param sasToken The SAS token string
     * @param iotHubServiceClientProtocol protocol to use
     * @param proxyOptions the proxy options to tunnel through, if a proxy should be used.
     */
    public AmqpFileUploadNotificationReceive(
            String hostName,
            String userName,
            String sasToken,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ProxyOptions proxyOptions)
    {
        this(hostName, userName, sasToken, iotHubServiceClientProtocol, proxyOptions, null);
    }

    /**
     * Constructor to set up connection parameters
     * @param hostName The address string of the service (example: AAA.BBB.CCC)
     * @param userName The username string to use SASL authentication (example: user@sas.service)
     * @param sasToken The SAS token string
     * @param iotHubServiceClientProtocol protocol to use
     * @param proxyOptions the proxy options to tunnel through, if a proxy should be used.
     * @param sslContext the SSL context to use during the TLS handshake when opening the connection. If null, a default
     *                   SSL context will be generated. This default SSLContext trusts the IoT Hub public certificates.
     */
    public AmqpFileUploadNotificationReceive(
            String hostName,
            String userName,
            String sasToken,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ProxyOptions proxyOptions,
            SSLContext sslContext)
    {
        this.hostName = hostName;
        this.userName = userName;
        this.sasToken = sasToken;
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
        this.proxyOptions = proxyOptions;
        this.sslContext = sslContext;
    }

    public AmqpFileUploadNotificationReceive(
            String hostName,
            TokenCredential authenticationTokenProvider,
            TokenCredentialType authorizationType,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ProxyOptions proxyOptions,
            SSLContext sslContext)
    {
        this.hostName = hostName;
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
        this.proxyOptions = proxyOptions;
        this.sslContext = sslContext;
        this.authenticationTokenProvider = authenticationTokenProvider;
        this.authorizationType = authorizationType;
    }

    /**
     * Create AmqpsReceiveHandler and store it in a member variable
     * @throws IOException If underlying layers throws it for any reason
     */
    public synchronized void open() throws IOException
    {
        if (amqpReceiveHandler == null)
        {
            if (this.authenticationTokenProvider != null)
            {
                amqpReceiveHandler = new AmqpFileUploadNotificationReceivedHandler(
                        this.hostName,
                        this.authenticationTokenProvider,
                        this.authorizationType,
                        this.iotHubServiceClientProtocol,
                        this,
                        this.proxyOptions,
                        this.sslContext);
            }
            else
            {
                amqpReceiveHandler = new AmqpFileUploadNotificationReceivedHandler(
                        this.hostName,
                        this.userName,
                        this.sasToken,
                        this.iotHubServiceClientProtocol,
                        this,
                        this.proxyOptions,
                        this.sslContext);
            }
        }
    }

    /**
     * Invalidate AmqpsReceiveHandler member variable
     */
    public synchronized void close()
    {
        amqpReceiveHandler = null;
        fileUploadNotification = null;
    }

    /**
     * Synchronized call to receive feedback batch
     * Hide the event based receiving mechanism from the user API
     * @param timeoutMs The timeout in milliseconds to wait for the feedback
     * @return The received feedback batch
     * @throws IOException This exception is thrown if the input AmqpReceive object is null
     * @throws InterruptedException This exception is thrown if the receive process has been interrupted
     */
    public synchronized FileUploadNotification receive(long timeoutMs) throws IOException, InterruptedException
    {
        if  (amqpReceiveHandler != null)
        {
            log.info("Receiving on file upload notification receiver for up to {} milliseconds", timeoutMs);

            new ReactorRunner(amqpReceiveHandler, "AmqpFileUploadNotificationReceiver").run(timeoutMs);

            log.trace("Amqp receive reactor stopped, checking that the connection was opened");
            this.amqpReceiveHandler.verifyConnectionWasOpened();
            log.trace("Amqp receive reactor did successfully open the connection, returning without exception");
        }
        else
        {
            throw new IOException("receive handler is not initialized. call open before receive");
        }

        return fileUploadNotification;
    }

    /**
     * Handle on feedback received Proton event
     * Parse received json and save result to a member variable
     * Release semaphore for wait function
     * @param feedbackJson Received Json string to process
     */
    public synchronized void onFeedbackReceived(String feedbackJson)
    {
        try
        {
            FileUploadNotificationParser notificationParser = new FileUploadNotificationParser(feedbackJson);

            fileUploadNotification = new FileUploadNotification(notificationParser.getDeviceId(),
                    notificationParser.getBlobUri(), notificationParser.getBlobName(), notificationParser.getLastUpdatedTime(),
                    notificationParser.getBlobSizeInBytesTag(), notificationParser.getEnqueuedTimeUtc());
        }
        catch (Exception e)
        {
            // this should never happen. However if it does, proton can't handle it. So guard against throwing it at proton.
            log.warn("Service gave feedback message with poorly formed json, message abandoned.");
        }
    }
}
