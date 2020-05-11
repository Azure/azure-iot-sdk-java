/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpSend;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Extend the ServiceClient class and provide AMPQ specific implementation.
 */
@Slf4j
public class ServiceClient
{
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    private AmqpSend amqpMessageSender;
    private final String hostName;
    private final String userName;
    private final String sasToken;
    protected IotHubConnectionString iotHubConnectionString;
    private IotHubServiceClientProtocol iotHubServiceClientProtocol;

    private ServiceClientOptions options;

    /**
     * Create ServiceClient from the specified connection string
     * @param iotHubServiceClientProtocol  protocol to use
     * @param connectionString The connection string for the IotHub
     * @return The created ServiceClient object
     * @throws IOException This exception is thrown if the object creation failed
     */
    public static ServiceClient createFromConnectionString(String connectionString, IotHubServiceClientProtocol iotHubServiceClientProtocol) throws IOException
    {
        return createFromConnectionString(connectionString, iotHubServiceClientProtocol, ServiceClientOptions.builder().build());
    }

    /**
     * Create ServiceClient from the specified connection string
     * @param iotHubServiceClientProtocol  protocol to use
     * @param connectionString The connection string for the IotHub
     * @param options The connection options to use when connecting to the service. May be null if no custom options will be used.
     * @return The created ServiceClient object
     * @throws IOException This exception is thrown if the object creation failed
     */
    public static ServiceClient createFromConnectionString(String connectionString, IotHubServiceClientProtocol iotHubServiceClientProtocol, ServiceClientOptions options) throws IOException
    {
        // Codes_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_001: [The constructor shall throw IllegalArgumentException if the input string is empty or null]
        if (Tools.isNullOrEmpty(connectionString))
        {
            throw new IllegalArgumentException(connectionString);
        }

        // Codes_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_002: [The constructor shall create IotHubConnectionString object using the IotHubConnectionStringBuilder]
        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        // Codes_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_003: [The constructor shall create a new instance of ServiceClient using the created IotHubConnectionString object and return with it]
        ServiceClient iotServiceClient = new ServiceClient(iotHubConnectionString, iotHubServiceClientProtocol, options);
        return iotServiceClient;
    }

    /**
     * Initialize AMQP sender using given connection string
     *
     * @param iotHubConnectionString The ConnectionString object for the IotHub
     * @param iotHubServiceClientProtocol protocol to use
     */
    protected ServiceClient(IotHubConnectionString iotHubConnectionString, IotHubServiceClientProtocol iotHubServiceClientProtocol)
    {
        this(iotHubConnectionString, iotHubServiceClientProtocol, ServiceClientOptions.builder().build());
    }

    /**
     * Initialize AMQP sender using given connection string
     *
     * @param iotHubConnectionString The ConnectionString object for the IotHub
     * @param iotHubServiceClientProtocol protocol to use
     */
    protected ServiceClient(IotHubConnectionString iotHubConnectionString, IotHubServiceClientProtocol iotHubServiceClientProtocol, ServiceClientOptions options)
    {
        // Codes_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_004: [The constructor shall throw IllegalArgumentException if the input object is null]
        if (iotHubConnectionString == null)
        {
            throw new IllegalArgumentException();
        }

        // Codes_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_005: [The constructor shall create a SAS token object using the IotHubConnectionString]
        IotHubServiceSasToken iotHubServiceSasToken = new IotHubServiceSasToken(iotHubConnectionString);

        // Codes_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_006: [The constructor shall store connection string, hostname, username and sasToken]
        this.iotHubConnectionString = iotHubConnectionString;
        this.hostName = iotHubConnectionString.getHostName();
        this.userName = iotHubConnectionString.getUserString();
        this.sasToken = iotHubServiceSasToken.toString();
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
        this.options = options;

        if (this.options.getProxyOptions() != null && this.iotHubServiceClientProtocol != IotHubServiceClientProtocol.AMQPS_WS)
        {
            throw new UnsupportedOperationException("Proxies are only supported over AMQPS_WS");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_007: [The constructor shall create a new instance of AmqpSend object]
        this.amqpMessageSender = new AmqpSend(hostName, userName, sasToken, this.iotHubServiceClientProtocol, options.getProxyOptions());
    }

    /**
     * Open AMQP sender
     * @throws IOException This exception is thrown if the AmqpSender object is not initialized
     */
    public void open() throws IOException
    {
        // Codes_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_008: [The function shall throw IOException if the member AMQP sender object has not been initialized]
        if (this.amqpMessageSender == null)
        {
            throw new IOException("AMQP sender is not initialized");
        }

        log.info("Opening service client...");

        // Codes_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_009: [The function shall call open() on the member AMQP sender object]
        this.amqpMessageSender.open();
        log.info("Service client opened successfully");
    }

    /**
     * Close AMQP sender
     * @throws IOException This exception is thrown if the AmqpSender object is not initialized
     */
    public void close() throws IOException
    {
        // Codes_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_010: [The function shall throw IOException if the member AMQP sender object has not been initialized]
        if (this.amqpMessageSender == null)
        {
            throw new IOException("AMQP sender is not initialized");
        }

        log.info("Closing service client...");
        // Codes_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_011: [The function shall call close() on the member AMQP sender object]
        this.amqpMessageSender.close();
        log.info("Service client closed successfully");
    }

    /**
     * Send a one-way message to the specified device
     *
     * @param deviceId The device identifier for the target device
     * @param message The message for the device
     * @throws IOException This exception is thrown if the AmqpSender object is not initialized
     * @throws IotHubException This exception is thrown if IotHub rejects the message for any reason
     */
    public void send(String deviceId, Message message) throws IOException, IotHubException
    {
        // Codes_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_013: [The function shall call send() with the given parameters and null moduleId]
        this.send(deviceId, null, message);
    }

    /**
     * Send a one-way message to the specified module
     *
     * @param deviceId The device identifier for the target device
     * @param moduleId The module identifier for the target device
     * @param message The message for the device
     * @throws IOException This exception is thrown if the AmqpSender object is not initialized
     * @throws IotHubException This exception is thrown if IotHub rejects the message for any reason
     */
    public void send(String deviceId, String moduleId, Message message) throws IOException, IotHubException
    {
        // Codes_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_28_001: [The function shall throw IOException if the member AMQP sender object has not been initialized]
        if (this.amqpMessageSender == null)
        {
            throw new IOException("AMQP sender is not initialized");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_28_002: [The function shall call send() on the member AMQP sender object with the given parameters]
        this.amqpMessageSender.send(deviceId, moduleId, message);
    }

    /**
     * Provide asynchronous access to open()
     *
     * @return The future object for the requested operation
     */
    public CompletableFuture<Void> openAsync()
    {
        // Codes_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_014: [The function shall create an async wrapper around the open() function call]
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
     * Provide asynchronous access to close()
     *
     * @return The future object for the requested operation
     */
    public CompletableFuture<Void> closeAsync()
    {
        // Codes_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_015: [The function shall create an async wrapper around the close() function call]
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
     * Provide asynchronous access to send()
     *
     * @param deviceId The device identifier for the target device
     * @param message The message for the device
     * @return The future object for the requested operation
     */
    public CompletableFuture<Void> sendAsync(String deviceId, Message message)
    {
        // Codes_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_016: [The function shall create an async wrapper around the send() function call]
        final CompletableFuture<Void> future = new CompletableFuture<>();
        executor.submit(() -> {
        try
        {
            send(deviceId, message);
            future.complete(null);
        } catch (Exception e)
        {
            future.completeExceptionally(e);
        }
        });
        return future;
    }

    /**
     * Get FeedbackReceiver object.This API has been deprecated. Use new API without deviceId as an input parameter.
     * @deprecated As of release 1.1.15, replaced by {@link #getFeedbackReceiver()}
     * @param deviceId The device identifier for the target device
     * @return The instance of the FeedbackReceiver
     */
    @Deprecated public FeedbackReceiver getFeedbackReceiver(String deviceId)
    {
        if (options != null)
        {
            throw new UnsupportedOperationException("This deprecated API does not support proxies. Use the non-deprecated version of this API for proxy enabled feedback receiving");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_017: [The function shall create a FeedbackReceiver object and returns with it. This API is deprecated.]
        FeedbackReceiver feedbackReceiver = new FeedbackReceiver(hostName, userName, sasToken, iotHubServiceClientProtocol, deviceId);
        return feedbackReceiver;
    }
    
     /**
     * Get FeedbackReceiver object.  
     *
     *
     * @return The instance of the FeedbackReceiver
     */
    
     public FeedbackReceiver getFeedbackReceiver()
    {
        return new FeedbackReceiver(hostName, userName, sasToken, iotHubServiceClientProtocol, options != null ? options.getProxyOptions() : null);
    }

    /**
     * Get FileUploadNotificationReceiver object.
     *
     * @return The instance of the FileUploadNotificationReceiver
     */
    public FileUploadNotificationReceiver getFileUploadNotificationReceiver()
    {
        return new FileUploadNotificationReceiver(hostName, userName, sasToken, iotHubServiceClientProtocol, options != null ? options.getProxyOptions() : null);
    }
    
}
