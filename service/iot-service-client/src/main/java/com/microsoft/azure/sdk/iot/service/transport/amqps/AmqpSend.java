/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.Message;
import com.microsoft.azure.sdk.iot.service.Tools;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Allows for sending of a single cloud to device message. Automatically opens the amqp connection prior to sending the message,
 * and automatically closes the connection after sending it
 */
@Slf4j
public class AmqpSend implements AmqpSendHandlerMessageSentCallback
{
    protected final String hostName;
    protected final String userName;
    protected final IotHubConnectionString iotHubConnectionString;
    protected IotHubServiceClientProtocol iotHubServiceClientProtocol;
    private CountDownLatch sendLatch;
    private static final int SEND_MESSAGE_TIMEOUT_SECONDS = 60;
    private long sasTokenExpiryTime;
    private String correlationId;
    private static long DEFAULT_SAS_TOKEN_EXPIRY_TIME = 365*24*60*60;

    /**
     * Constructor to set up connection parameters
     * @param hostName The address string of the service (example: AAA.BBB.CCC)
     * @param userName The username string to use SASL authentication (example: user@sas.service)
     * @param iotHubConnectionString The iotHubConnectionString to build sas tokens from
     * @param iotHubServiceClientProtocol protocol to use
     */
    public AmqpSend(String hostName, String userName, IotHubConnectionString iotHubConnectionString, IotHubServiceClientProtocol iotHubServiceClientProtocol)
    {
        this(hostName, userName, iotHubConnectionString, iotHubServiceClientProtocol, DEFAULT_SAS_TOKEN_EXPIRY_TIME);
    }

    /**
     * Constructor to set up connection parameters
     * @param hostName The address string of the service (example: AAA.BBB.CCC)
     * @param userName The username string to use SASL authentication (example: user@sas.service)
     * @param iotHubConnectionString The iotHubConnectionString to build sas tokens from
     * @param iotHubServiceClientProtocol protocol to use
     * @param sasTokenExpiryTime the time for sas tokens to live for
     */
    public AmqpSend(String hostName, String userName, IotHubConnectionString iotHubConnectionString, IotHubServiceClientProtocol iotHubServiceClientProtocol, long sasTokenExpiryTime)
    {
        // Codes_SRS_SERVICE_SDK_JAVA_AMQPSEND_12_001: [The constructor shall throw IllegalArgumentException if any of the input parameter is null or empty]
        if (Tools.isNullOrEmpty(hostName))
        {
            throw new IllegalArgumentException("hostName can not be null or empty");
        }
        if (Tools.isNullOrEmpty(userName))
        {
            throw new IllegalArgumentException("userName can not be null or empty");
        }
        if (iotHubConnectionString == null)
        {
            throw new IllegalArgumentException("iotHubConnectionString can not be null");
        }
        if (iotHubServiceClientProtocol == null)
        {
            throw new IllegalArgumentException("iotHubServiceClientProtocol cannot be null");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_AMQPSEND_12_002: [The constructor shall copy all input parameters to private member variables for event processing]
        this.hostName = hostName;
        this.userName = userName;
        this.iotHubConnectionString = iotHubConnectionString;
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
        this.sasTokenExpiryTime = sasTokenExpiryTime;
    }


    /**
     * Create AmqpsSendHandler and store it in a member variable
     * @deprecated this method no longer does anything. In order to send a C2D message, simply call
     * {@link #send(String, String, Message)}} which opens the connection, sends the message, and then closes the connection
     */
    @Deprecated
    public void open()
    {
    }

    /**
     * Invalidate AmqpsSendHandler member variable
     * @deprecated this method no longer does anything. In order to send a C2D message, simply call
     * {@link #send(String, String, Message)}} which opens the connection, sends the message, and then closes the connection
     */
    @Deprecated
    public void close()
    {
    }

    /**
     * Open an AMQP connection, send the created message, and the close the AMQP connection
     *
     * @param deviceId The device name string
     * @param moduleId The module name string
     * @param message The message to be sent
     * @throws IOException This exception is thrown if the AmqpSend object is not initialized
     * @throws IotHubException This exception isn't thrown anymore
     */
    public void send(String deviceId, String moduleId, Message message) throws IOException, IotHubException
    {
        AmqpSendHandler amqpSendHandler = new AmqpSendHandler(this.hostName, this.userName, this.iotHubServiceClientProtocol, this);

        if (moduleId == null)
        {
            // Codes_SRS_SERVICE_SDK_JAVA_AMQPSEND_28_006: [The function shall create a binary message with the given content with deviceId only if moduleId is null]
            amqpSendHandler.createProtonMessage(deviceId, message);
        }
        else
        {
            // Codes_SRS_SERVICE_SDK_JAVA_AMQPSEND_28_001: [The function shall create a binary message with the given content with moduleId]
            amqpSendHandler.createProtonMessage(deviceId, moduleId, message);
        }

        this.correlationId = message.correlationId;

        sendLatch = new CountDownLatch(1);

        try
        {
            amqpSendHandler.open(new IotHubServiceSasToken(iotHubConnectionString, sasTokenExpiryTime).toString());
            log.info("Amqp connection for sending message with correlation id {} was opened", this.correlationId);
        }
        catch (InterruptedException e)
        {
            throw new IOException("Amqp connection was interrupted while opening", e);
        }

        amqpSendHandler.validateConnectionWasOpenedSuccessfully();

        try
        {
            boolean sendTimedOut = !sendLatch.await(SEND_MESSAGE_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (sendTimedOut)
            {
                throw new IOException("Sending the message with correlation id " + message.correlationId + " timed out");
            }
        }
        catch (InterruptedException e)
        {
            throw new IOException("Amqp connection was interrupted while sending the message with correlation id " + message.correlationId, e);
        }

        amqpSendHandler.validateMessageWasSent();

        // Old API usage dictates a single AMQP connection per-send, so we have to close the connection here
        amqpSendHandler.close();
        log.info("Amqp connection for sending message with correlation id {} was closed", this.correlationId);
    }

    @Override
    public void onMessageSent(AmqpResponseVerification deliveryAcknowledgement)
    {
        sendLatch.countDown();
        log.info("Message with correlation id {} was sent successfully", this.correlationId);
    }
}
