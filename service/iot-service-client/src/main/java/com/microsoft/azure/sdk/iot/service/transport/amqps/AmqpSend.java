/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.Message;
import com.microsoft.azure.sdk.iot.service.Tools;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import lombok.extern.slf4j.Slf4j;

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
    protected final String userName;
    protected final String sasToken;
    protected AmqpSendHandler amqpSendHandler;
    protected IotHubServiceClientProtocol iotHubServiceClientProtocol;

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
     * Create AmqpsSendHandler and store it in a member variable
     */
    public void open()
    {
        // Codes_SRS_SERVICE_SDK_JAVA_AMQPSEND_12_004: [The function shall create an AmqpsSendHandler object to handle reactor events]
        amqpSendHandler = new AmqpSendHandler(this.hostName, this.userName, this.sasToken, this.iotHubServiceClientProtocol);
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
                    log.info("Sending cloud to device message");
                }
                else
                {
                    // Codes_SRS_SERVICE_SDK_JAVA_AMQPSEND_28_001: [The function shall create a binary message with the given content with moduleId]
                    amqpSendHandler.createProtonMessage(deviceId, moduleId, message);
                    log.info("Sending cloud to device module message");
                }

                new ReactorRunner(amqpSendHandler, "AmqpSend").run();

                log.trace("Amqp send reactor stopped, checking that the connection opened, and that the message was sent");

                // Codes_SRS_SERVICE_SDK_JAVA_AMQPSEND_28_004: [** The function shall call verifySendSucceeded to identify the status of sent message and throws exception if thrown by verifySendSucceeded **]**
                amqpSendHandler.verifySendSucceeded();
            }
            else
            {
                // Codes_SRS_SERVICE_SDK_JAVA_AMQPSEND_28_005: [The function shall throw IOException if the send handler object is not initialized]
                throw new IOException("send handler is not initialized. call open before send");
            }
        }
    }
}
