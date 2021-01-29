/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.deps.auth.TokenCredentialType;
import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpFileUploadNotificationReceive;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class FileUploadNotificationReceiver extends Receiver
{
    private final long DEFAULT_TIMEOUT_MS = 60000;
    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    private final AmqpFileUploadNotificationReceive amqpFileUploadNotificationReceive;

    /**
     * Constructor to verify initialization parameters
     * Create instance of AmqpReceive
     * @param hostName The iot hub host name
     * @param userName The iot hub user name
     * @param sasToken The iot hub SAS token for the given device
     * @param iotHubServiceClientProtocol The iot hub protocol name
     * @param proxyOptions the proxy options to tunnel through, if a proxy should be used.
     * @param sslContext the SSL context to use during the TLS handshake when opening the connection. If null, a default
     *                   SSL context will be generated. This default SSLContext trusts the IoT Hub public certificates.
     */
    FileUploadNotificationReceiver(String hostName, String userName, String sasToken, IotHubServiceClientProtocol iotHubServiceClientProtocol, ProxyOptions proxyOptions, SSLContext sslContext)
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

        this.amqpFileUploadNotificationReceive = new AmqpFileUploadNotificationReceive(hostName, userName, sasToken, iotHubServiceClientProtocol, proxyOptions, sslContext);
    }

    /**
     * Construct a new FileUploadNotificationReceiver.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param authenticationTokenProvider The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed.
     * @param authorizationType The type of authentication tokens that the provided {@link TokenCredential}
     *                          implementation will always give.
     * @param iotHubServiceClientProtocol The protocol to open the connection with.
     * @param proxyOptions the proxy options to tunnel through, if a proxy should be used.
     * @param sslContext the SSL context to use during the TLS handshake when opening the connection. If null, a default
     *                   SSL context will be generated. This default SSLContext trusts the IoT Hub public certificates.
     */
    FileUploadNotificationReceiver(String hostName, TokenCredential authenticationTokenProvider, TokenCredentialType authorizationType, IotHubServiceClientProtocol iotHubServiceClientProtocol, ProxyOptions proxyOptions, SSLContext sslContext)
    {
        if (Tools.isNullOrEmpty(hostName))
        {
            throw new IllegalArgumentException("hostName cannot be null or empty");
        }
        if (iotHubServiceClientProtocol == null)
        {
            throw new IllegalArgumentException("iotHubServiceClientProtocol cannot be null");
        }

        this.amqpFileUploadNotificationReceive =
                new AmqpFileUploadNotificationReceive(
                        hostName,
                        authenticationTokenProvider,
                        authorizationType,
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
        log.info("Opening file upload notification receiver");

        this.amqpFileUploadNotificationReceive.open();

        log.info("Opened file upload notification receiver");
    }

    /**
     * Close AmqpReceive object
     *
     * @throws IOException This exception is thrown if the input AmqpReceive object is null
     */
    public void close() throws IOException
    {
        log.info("Closing file upload notification receiver");

        this.amqpFileUploadNotificationReceive.close();

        log.info("Closed file upload notification receiver");
    }

    /**
     * Receive FileUploadNotification with default timeout
     *
     * QoS for receiving file upload notifications is at least once
     *
     * This function is synchronized internally so that only one receive operation is allowed at a time.
     * In order to do more receive operations at a time, you will need to instantiate another FileUploadNotificationReceiver instance.
     *
     * @return The received FileUploadNotification object
     * @throws IOException This exception is thrown if the input AmqpReceive object is null
     * @throws InterruptedException This exception is thrown if the receive process has been interrupted
     */
    public FileUploadNotification receive() throws IOException, InterruptedException
    {
        return receive(DEFAULT_TIMEOUT_MS);
    }

    /**
     * Receive FileUploadNotification with specific timeout
     *
     * QoS for receiving file upload notifications is at least once
     *
     * This function is synchronized internally so that only one receive operation is allowed at a time.
     * In order to do more receive operations at a time, you will need to instantiate another FileUploadNotificationReceiver instance.
     *
     * @param timeoutMs The timeout in milliseconds
     * @return The received FileUploadNotification object
     * @throws IOException This exception is thrown if the input AmqpReceive object is null
     * @throws InterruptedException This exception is thrown if the receive process has been interrupted
     */
    public FileUploadNotification receive(long timeoutMs) throws IOException, InterruptedException
    {
        if (this.amqpFileUploadNotificationReceive == null)
        {
            throw new IOException("AMQP receiver is not initialized");
        }

        return this.amqpFileUploadNotificationReceive.receive(timeoutMs);
    }

    /**
     * Async wrapper for open() operation
     *
     * @return The future object for the requested operation
     */
    @Override
    public CompletableFuture<Void> openAsync()
    {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        executor.submit(() -> {
            try
            {
                open();
                future.complete(null);
            } catch (IOException e)
            {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Async wrapper for close() operation
     *
     * @return The future object for the requested operation
     */
    @Override
    public CompletableFuture<Void> closeAsync()
    {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        executor.submit(() -> {
            try
            {
                close();
                future.complete(null);
            } catch (IOException e)
            {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Async wrapper for receive() operation with default timeout
     *
     * QoS for receiving file upload notifications is at least once
     *
     * @return The future object for the requested operation
     */
    @Override
    public CompletableFuture<FileUploadNotification> receiveAsync()
    {
        return receiveAsync(DEFAULT_TIMEOUT_MS);
    }

    /**
     * Async wrapper for receive() operation with specific timeout
     *
     * QoS for receiving file upload notifications is at least once
     *
     * @return The future object for the requested operation
     */
    @Override
    public CompletableFuture<FileUploadNotification> receiveAsync(long timeoutMs)
    {
        final CompletableFuture<FileUploadNotification> future = new CompletableFuture<>();
        executor.submit(() -> {
            try
            {
                FileUploadNotification responseFileUploadNotification = receive(timeoutMs);
                future.complete(responseFileUploadNotification);
            } catch (IOException | InterruptedException e)
            {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

}
