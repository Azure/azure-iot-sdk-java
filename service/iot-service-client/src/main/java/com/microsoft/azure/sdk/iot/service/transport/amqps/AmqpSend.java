/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.Message;
import com.microsoft.azure.sdk.iot.service.Tools;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.reactor.Reactor;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Instance of the QPID-Proton-J BaseHandler class
 * overriding the events what are needed to handle
 * high level open, close and send methods.
 * Initialize and use AmqpsSendHandler class for low level ampqs operations.
 */
public class AmqpSend extends BaseHandler implements AmqpSendHandlerMessageSentCallback
{
    protected final String hostName;
    protected final String userName;
    protected final String sasToken;
    protected AmqpSendHandler amqpSendHandler;
    protected IotHubServiceClientProtocol iotHubServiceClientProtocol;
    private CountDownLatch sendLatch;
    private static final int SEND_MESSAGE_TIMEOUT_SECONDS = 60;

    /**
     * Constructor to set up connection parameters
     * @param hostName The address string of the service (example: AAA.BBB.CCC)
     * @param userName The username string to use SASL authentication (example: user@sas.service)
     * @param sasToken The SAS token string
     * @param iotHubServiceClientProtocol protocol to use
     */
    public AmqpSend(String hostName, String userName, String sasToken, IotHubServiceClientProtocol iotHubServiceClientProtocol)
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
        if (Tools.isNullOrEmpty(sasToken))
        {
            throw new IllegalArgumentException("sasToken can not be null or empty");
        }
        
        if (iotHubServiceClientProtocol == null)
        {
            throw new IllegalArgumentException("iotHubServiceClientProtocol cannot be null");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_AMQPSEND_12_002: [The constructor shall copy all input parameters to private member variables for event processing]
        this.hostName = hostName;
        this.userName = userName;
        this.sasToken = sasToken;
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
    }

    /**
     * Event handler for the reactor init event
     * @param event The proton event object
     */
    @Override
    public void onReactorInit(Event event) {
        // You can use the connection method to create AMQP connections.

        // This connection's handler is the AmqpSendHandler object. All the events
        // for this connection will go to the AmqpSendHandler object instead of
        // going to the reactor. If you were to omit the AmqpSendHandler object,
        // all the events would go to the reactor.

        // Codes_SRS_SERVICE_SDK_JAVA_AMQPSEND_12_003: [The event handler shall set the member AmqpsSendHandler object to handle the given connection events]
        event.getReactor().connection(amqpSendHandler);
    }

    /**
     * Create AmqpsSendHandler and store it in a member variable
     */
    public void open()
    {
        // Codes_SRS_SERVICE_SDK_JAVA_AMQPSEND_12_004: [The function shall create an AmqpsSendHandler object to handle reactor events]
        amqpSendHandler = new AmqpSendHandler(this.hostName, this.userName, this.sasToken, this.iotHubServiceClientProtocol, this);
    }

    /**
     * Invalidate AmqpsSendHandler member variable
     */
    public void close()
    {
        // Codes_SRS_SERVICE_SDK_JAVA_AMQPSEND_12_005: [The function shall invalidate the member AmqpsSendHandler object]
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
                    // Codes_SRS_SERVICE_SDK_JAVA_AMQPSEND_28_006: [The function shall create a binary message with the given content with deviceId only if moduleId is null]
                    amqpSendHandler.createProtonMessage(deviceId, message);
                }
                else
                {
                    // Codes_SRS_SERVICE_SDK_JAVA_AMQPSEND_28_001: [The function shall create a binary message with the given content with moduleId]
                    amqpSendHandler.createProtonMessage(deviceId, moduleId, message);
                }

                sendLatch = new CountDownLatch(1);

                try
                {
                    this.amqpSendHandler.open();
                }
                catch (InterruptedException e)
                {
                    throw new IOException("Amqp connection was interrupted while opening", e);
                }

                try
                {
                    boolean sendTimedOut = !sendLatch.await(SEND_MESSAGE_TIMEOUT_SECONDS, TimeUnit.SECONDS);

                    if (sendTimedOut)
                    {
                        throw new IOException("Sending the message timed out");
                    }
                }
                catch (InterruptedException e)
                {
                    throw new IOException("Amqp connection was interrupted while sending the message", e);
                }

                this.amqpSendHandler.close();
            }
            else
            {
                // Codes_SRS_SERVICE_SDK_JAVA_AMQPSEND_28_005: [The function shall throw IOException if the send handler object is not initialized]
                throw new IOException("send handler is not initialized. call open before send");
            }
        }
    }

    @Override
    public void onMessageSent(AmqpResponseVerification deliveryAcknowledgement)
    {
        sendLatch.countDown();
    }
}
