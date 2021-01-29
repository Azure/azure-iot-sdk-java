/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.deps.auth.TokenCredentialType;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.Message;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.Tools;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import java.io.IOException;

/**
 * Instance of the QPID-Proton-J BaseHandler class
 * overriding the events what are needed to handle
 * high level open, close and send methods.
 * Initialize and use AmqpsSendHandler class for low level ampqs operations.
 */
@Slf4j
public class AmqpSend
{
    protected final String hostName;
    protected String userName;
    protected String sasToken;
    protected TokenCredential authenticationTokenProvider;
    private TokenCredentialType authorizationType;
    protected AmqpSendHandler amqpSendHandler;
    protected IotHubServiceClientProtocol iotHubServiceClientProtocol;
    private final ProxyOptions proxyOptions;
    private final SSLContext sslContext;

    /**
     * Constructor to set up connection parameters
     * @param hostName The address string of the service (example: AAA.BBB.CCC)
     * @param userName The username string to use SASL authentication (example: user@sas.service)
     * @param sasToken The SAS token string
     * @param iotHubServiceClientProtocol protocol to use
     */
    public AmqpSend(String hostName, String userName, String sasToken, IotHubServiceClientProtocol iotHubServiceClientProtocol)
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
    public AmqpSend(String hostName, String userName, String sasToken, IotHubServiceClientProtocol iotHubServiceClientProtocol, ProxyOptions proxyOptions)
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
     */
    public AmqpSend(String hostName, String userName, String sasToken, IotHubServiceClientProtocol iotHubServiceClientProtocol, ProxyOptions proxyOptions, SSLContext sslContext)
    {
        if (Tools.isNullOrEmpty(hostName))
        {
            throw new IllegalArgumentException("hostName can not be null or empty");
        }
        if (Tools.isNullOrEmpty(userName))
        {
            throw new IllegalArgumentException("userName can not be null or empty");
        }
        if (Tools.isNullOrEmpty(sasToken))
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

    public AmqpSend(String hostName, TokenCredential authenticationTokenProvider, TokenCredentialType authorizationType, IotHubServiceClientProtocol iotHubServiceClientProtocol, ProxyOptions proxyOptions, SSLContext sslContext)
    {
        if (Tools.isNullOrEmpty(hostName))
        {
            throw new IllegalArgumentException("hostName can not be null or empty");
        }

        if (iotHubServiceClientProtocol == null)
        {
            throw new IllegalArgumentException("iotHubServiceClientProtocol cannot be null");
        }

        this.hostName = hostName;
        this.authenticationTokenProvider = authenticationTokenProvider;
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
        this.proxyOptions = proxyOptions;
        this.sslContext = sslContext;
        this.authorizationType = authorizationType;
    }

    /**
     * Create AmqpsSendHandler and store it in a member variable
     */
    public void open()
    {
        if (this.authenticationTokenProvider != null)
        {
            amqpSendHandler = new AmqpSendHandler(this.hostName, this.authenticationTokenProvider, this.authorizationType, this.iotHubServiceClientProtocol, this.proxyOptions, this.sslContext);
        }
        else
        {
            amqpSendHandler = new AmqpSendHandler(this.hostName, this.userName, this.sasToken, this.iotHubServiceClientProtocol, this.proxyOptions, this.sslContext);
        }
    }

    /**
     * Invalidate AmqpsSendHandler member variable
     */
    public void close()
    {
        amqpSendHandler = null;
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
            if  (amqpSendHandler != null)
            {
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

                new ReactorRunner(amqpSendHandler, "AmqpSend").run();

                log.trace("Amqp send reactor stopped, checking that the connection opened, and that the message was sent");

                amqpSendHandler.verifySendSucceeded();
            }
            else
            {
                throw new IOException("send handler is not initialized. call open before send");
            }
        }
    }
}
