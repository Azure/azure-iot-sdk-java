/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.Message;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.Objects;

/**
 * Instance of the QPID-Proton-J BaseHandler class
 * overriding the events what are needed to handle
 * high level open, close and send methods.
 * Initialize and use AmqpsSendHandler class for low level ampqs operations.
 */
@Slf4j
public class AmqpSend
{
    private final String hostName;
    private String userName;
    private String sasToken;
    private TokenCredential credential;
    private AzureSasCredential sasTokenProvider;
    private final IotHubServiceClientProtocol iotHubServiceClientProtocol;
    private final ProxyOptions proxyOptions;
    private final SSLContext sslContext;

    /**
     * Constructor to set up connection parameters
     * @param hostName The address string of the service (example: AAA.BBB.CCC)
     * @param userName The username string to use SASL authentication (example: user@sas.service)
     * @param sasToken The SAS token string
     * @param iotHubServiceClientProtocol protocol to use
     * @param proxyOptions the proxy options to tunnel through, if a proxy should be used.
     */
    public AmqpSend(
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
     * @param sslContext the custom SSL context to open the connection with.
     */
    public AmqpSend(
            String hostName,
            String userName,
            String sasToken,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ProxyOptions proxyOptions,
            SSLContext sslContext)
    {
        if (hostName == null || hostName.isEmpty())
        {
            throw new IllegalArgumentException("hostName can not be null or empty");
        }
        if (sasToken == null || sasToken.isEmpty())
        {
            throw new IllegalArgumentException("sasToken can not be null or empty");
        }

        if (iotHubServiceClientProtocol == null)
        {
            throw new IllegalArgumentException("iotHubServiceClientProtocol cannot be null");
        }

        this.hostName = hostName;
        this.userName = userName;
        this.sasToken = sasToken;
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
        this.proxyOptions = proxyOptions;
        this.sslContext = sslContext;
    }

    public AmqpSend(
            String hostName,
            TokenCredential credential,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ProxyOptions proxyOptions,
            SSLContext sslContext)
    {
        if (hostName == null || hostName.isEmpty())
        {
            throw new IllegalArgumentException("hostName can not be null or empty");
        }

        Objects.requireNonNull(iotHubServiceClientProtocol);
        Objects.requireNonNull(credential);

        this.hostName = hostName;
        this.credential = credential;
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
        this.proxyOptions = proxyOptions;
        this.sslContext = sslContext;
    }

    public AmqpSend(
            String hostName,
            AzureSasCredential sasTokenProvider,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ProxyOptions proxyOptions,
            SSLContext sslContext)
    {
        if (hostName == null || hostName.isEmpty())
        {
            throw new IllegalArgumentException("hostName can not be null or empty");
        }

        Objects.requireNonNull(iotHubServiceClientProtocol);
        Objects.requireNonNull(sasTokenProvider);

        this.hostName = hostName;
        this.sasTokenProvider = sasTokenProvider;
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
        this.proxyOptions = proxyOptions;
        this.sslContext = sslContext;
    }

    /**
     * Create AmqpsSendHandler and store it in a member variable
     */
    @SuppressWarnings("EmptyMethod")
    public void open()
    {
    }

    /**
     * Invalidate AmqpsSendHandler member variable
     */
    @SuppressWarnings("EmptyMethod")
    public void close()
    {
    }

    /**
     * Create binary message
     * Initialize and start Proton reactor
     * Send the created message
     * @param deviceId The device name string
     * @param moduleId The module name string
     * @param message The message to be sent
     * @throws IOException This exception is thrown if the AmqpSend object is not initialized
     * @throws IotHubException If IotHub rejects the message for any reason
     */
    public void send(String deviceId, String moduleId, Message message) throws IOException, IotHubException
    {
        synchronized(this)
        {
            AmqpSendHandler amqpSendHandler;
            if (this.credential != null)
            {
                amqpSendHandler =
                        new AmqpSendHandler(
                                this.hostName,
                                this.credential,
                                this.iotHubServiceClientProtocol,
                                this.proxyOptions,
                                this.sslContext);
            }
            else if (this.sasTokenProvider != null)
            {
                amqpSendHandler =
                        new AmqpSendHandler(
                                this.hostName,
                                this.sasTokenProvider,
                                this.iotHubServiceClientProtocol,
                                this.proxyOptions,
                                this.sslContext);
            }
            else
            {
                amqpSendHandler =
                        new AmqpSendHandler(
                                this.hostName,
                                this.userName,
                                this.sasToken,
                                this.iotHubServiceClientProtocol,
                                this.proxyOptions,
                                this.sslContext);
            }

            if (moduleId == null)
            {
                amqpSendHandler.createProtonMessage(deviceId, message);
                log.info("Sending cloud to device message");
            }
            else
            {
                amqpSendHandler.createProtonMessage(deviceId, moduleId, message);
                log.info("Sending cloud to device module message");
            }

            String reactorRunnerPrefix = this.hostName + "-" + "Cxn" + amqpSendHandler.getConnectionId();
            new ReactorRunner(amqpSendHandler, reactorRunnerPrefix,"AmqpSend").run();

            log.trace("Amqp send reactor stopped, checking that the connection opened, and that the message was sent");

            amqpSendHandler.verifySendSucceeded();
        }
    }
}
