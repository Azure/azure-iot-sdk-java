/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpReceive;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * FeedbackReceiver is a specialized receiver whose ReceiveAsync
 * method returns a FeedbackBatch instead of a Message.
 */
@Slf4j
public class FeedbackReceiver
{
    private final long DEFAULT_TIMEOUT_MS = 60000;

    private final AmqpReceive amqpReceive;

    /**
     * Constructor to verify initialization parameters
     * Create instance of AmqpReceive
     *
     * @param hostName The iot hub host name
     * @param userName The iot hub user name
     * @param sasToken The iot hub SAS token for the given device
     * @param iotHubServiceClientProtocol protocol to be used
     *
     */
    FeedbackReceiver(String hostName, String userName, String sasToken, IotHubServiceClientProtocol iotHubServiceClientProtocol)
    {
        this(hostName, userName, sasToken, iotHubServiceClientProtocol, null);
    }

    /**
     * Constructor to verify initialization parameters
     * Create instance of AmqpReceive
     *
     * @param hostName The iot hub host name
     * @param userName The iot hub user name
     * @param sasToken The iot hub SAS token for the given device
     * @param iotHubServiceClientProtocol protocol to be used
     * @param proxyOptions the proxy options to tunnel through, if a proxy should be used.
     */
    private FeedbackReceiver(String hostName, String userName, String sasToken, IotHubServiceClientProtocol iotHubServiceClientProtocol, ProxyOptions proxyOptions)
    {
        this(hostName, userName, sasToken, iotHubServiceClientProtocol, proxyOptions, null);
    }

    /**
     * Constructor to verify initialization parameters
     * Create instance of AmqpReceive
     *
     * @param hostName The iot hub host name
     * @param userName The iot hub user name
     * @param sasToken The iot hub SAS token for the given device
     * @param iotHubServiceClientProtocol protocol to be used
     * @param proxyOptions the proxy options to tunnel through, if a proxy should be used.
     * @param sslContext the SSL context to use during the TLS handshake when opening the connection. If null, a default
     *                   SSL context will be generated. This default SSLContext trusts the IoT Hub public certificates.
     */
    FeedbackReceiver(
            String hostName,
            String userName,
            String sasToken,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ProxyOptions proxyOptions,
            SSLContext sslContext)
    {
        if (Tools.isNullOrEmpty(hostName))
        {
            throw new IllegalArgumentException("hostName cannot be null or empty");
        }

        if (Tools.isNullOrEmpty(userName))
        {
            throw new IllegalArgumentException("userName cannot be null or empty");
        }

        if (Tools.isNullOrEmpty(sasToken))
        {
            throw new IllegalArgumentException("sasToken cannot be null or empty");
        }

        if (iotHubServiceClientProtocol == null)
        {
            throw new IllegalArgumentException("iotHubServiceClientProtocol cannot be null");
        }

        this.amqpReceive = new AmqpReceive(hostName, userName, sasToken, iotHubServiceClientProtocol, proxyOptions, sslContext);
    }

    FeedbackReceiver(
            String hostName,
            TokenCredential credential,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ProxyOptions proxyOptions,
            SSLContext sslContext)
    {
        if (Tools.isNullOrEmpty(hostName))
        {
            throw new IllegalArgumentException("hostName cannot be null or empty");
        }

        Objects.requireNonNull(credential);
        Objects.requireNonNull(iotHubServiceClientProtocol);

        this.amqpReceive =
                new AmqpReceive(
                        hostName,
                        credential,
                        iotHubServiceClientProtocol,
                        proxyOptions,
                        sslContext);
    }

    FeedbackReceiver(
            String hostName,
            AzureSasCredential sasTokenProvider,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ProxyOptions proxyOptions,
            SSLContext sslContext)
    {
        if (Tools.isNullOrEmpty(hostName))
        {
            throw new IllegalArgumentException("hostName cannot be null or empty");
        }

        Objects.requireNonNull(sasTokenProvider);
        Objects.requireNonNull(iotHubServiceClientProtocol);

        this.amqpReceive =
                new AmqpReceive(
                        hostName,
                        sasTokenProvider,
                        iotHubServiceClientProtocol,
                        proxyOptions,
                        sslContext);
    }
        
    /**
     * Open AmqpReceive object
     *
     * @throws IOException This exception is thrown if the input AmqpReceive object is null
     */
    public void open() throws IOException
    {
        if (this.amqpReceive == null)
        {
            throw new IOException("AMQP receiver is not initialized");
        }

        log.info("Opening feedback receiver client");

        this.amqpReceive.open();

        log.info("Opened feedback receiver client");
    }

    /**
     * Close AmqpReceive object
     *
     * @throws IOException This exception is thrown if the input AmqpReceive object is null
     */
    public void close() throws IOException
    {
        if (this.amqpReceive == null)
        {
            throw new IOException("AMQP receiver is not initialized");
        }

        log.info("Closing feedback receiver client");

        this.amqpReceive.close();

        log.info("Closed feedback receiver client");
    }

    /**
     * Receive FeedbackBatch with default timeout
     *
     * This function is synchronized internally so that only one receive operation is allowed at a time.
     * In order to do more receive operations at a time, you will need to instantiate another FeedbackReceiver instance.
     *
     * @return The received FeedbackBatch object
     * @throws IOException This exception is thrown if the input AmqpReceive object is null
     */
    public FeedbackBatch receive() throws IOException
    {
        return receive(DEFAULT_TIMEOUT_MS);
    }

    /**
     * Receive FeedbackBatch with specific timeout
     *
     * This function is synchronized internally so that only one receive operation is allowed at a time.
     * In order to do more receive operations at a time, you will need to instantiate another FeedbackReceiver instance.
     *
     * @param timeoutMs The timeout in milliseconds
     * @return The received FeedbackBatch object
     * @throws IOException This exception is thrown if the input AmqpReceive object is null
     */
    public FeedbackBatch receive(long timeoutMs) throws IOException
    {
        if (this.amqpReceive == null)
        {
            throw new IOException("AMQP receiver is not initialized");
        }

        return this.amqpReceive.receive(timeoutMs);
    }
}
