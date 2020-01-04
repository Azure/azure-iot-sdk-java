/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpSend;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Extend the ServiceClient class and provide AMPQ specific implementation.
 */
public class ServiceClient
{
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    private final String hostName;
    private final String userName;
    protected IotHubConnectionString iotHubConnectionString;
    private IotHubServiceClientProtocol iotHubServiceClientProtocol;

    //By default, generated sas tokens live for one year, but this can be configured
    private long DEFAULT_SAS_TOKEN_EXPIRY_TIME = 365*24*60*60;
    private long sasTokenExpiryTime;

    /**
     * Create ServiceClient from the specified connection string
     * @param iotHubServiceClientProtocol  protocol to use
     * @param connectionString The connection string for the IotHub
     * @return The created ServiceClient object
     * @throws IOException This exception is thrown if the object creation failed
     */
    public static ServiceClient createFromConnectionString(String connectionString, IotHubServiceClientProtocol iotHubServiceClientProtocol) throws IOException
    {
        // Codes_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_001: [The constructor shall throw IllegalArgumentException if the input string is empty or null]
        if (Tools.isNullOrEmpty(connectionString))
        {
            throw new IllegalArgumentException(connectionString);
        }

        // Codes_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_002: [The constructor shall create IotHubConnectionString object using the IotHubConnectionStringBuilder]
        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        // Codes_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_003: [The constructor shall create a new instance of ServiceClient using the created IotHubConnectionString object and return with it]
        ServiceClient iotServiceClient = new ServiceClient(iotHubConnectionString, iotHubServiceClientProtocol);
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
        // Codes_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_004: [The constructor shall throw IllegalArgumentException if the input object is null]
        if (iotHubConnectionString == null)
        {
            throw new IllegalArgumentException();
        }

        // Codes_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_006: [The constructor shall store connection string, hostname, username and sasToken]
        this.iotHubConnectionString = iotHubConnectionString;
        this.hostName = iotHubConnectionString.getHostName();
        this.userName = iotHubConnectionString.getUserString();
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
        this.sasTokenExpiryTime = DEFAULT_SAS_TOKEN_EXPIRY_TIME;
    }

    /**
     * Set how long newly generated sas tokens will live for, in seconds. By default, tokens live for 1 year.
     * A sas token is generated each time a connection is opened. This only applies for connections for sending cloud
     * to device messages. The file upload notification receiver and feedback message receiver have their own sas token
     * expiry time that can be set. See {@link FeedbackMessageListenerClient#setSasTokenExpiryTime(long)} and
     * {@link FeedbackMessageListenerClient#setSasTokenExpiryTime(long)}
     * @param sasTokenExpiryTime the number of seconds newly generated sas tokens will live for.
     */
    public void setSasTokenExpiryTime(long sasTokenExpiryTime)
    {
        this.sasTokenExpiryTime = sasTokenExpiryTime;
    }

    /**
     * Does nothing anymore
     * @throws IOException This method never throws this exception anymore
     * @deprecated This method no longer does anything. File upload notification listener and feedback message listener
     * must be opened through their respective open() APIs, and the cloud to device message sender in this client opens
     * itself before sending each message
     */
    @Deprecated
    public void open() throws IOException
    {
    }

    /**
     * Does nothing anymore
     * @throws IOException This method never throws this exception anymore
     * @deprecated This method no longer does anything. File upload notification listener and feedback message listener
     * must be closed through their respective close() APIs, and the cloud to device message sender in this client closes
     * itself after sending each message
     */
    @Deprecated
    public void close() throws IOException
    {
    }

    /**
     * Send a one-way message to the specified device
     * This method opens the connection, sends the message, and then closes the connection
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
     * Send a one-way message to the specified module of the specified device
     * This method opens the connection, sends the message, and then closes the connection
     *
     * @param deviceId The device identifier for the target device
     * @param moduleId The module identifier for the target device
     * @param message The message for the device
     * @throws IOException This exception is thrown if the AmqpSender object is not initialized
     * @throws IotHubException This exception is thrown if IotHub rejects the message for any reason
     */
    public void send(String deviceId, String moduleId, Message message) throws IOException, IotHubException
    {
        AmqpSend amqpMessageSender = new AmqpSend(hostName, userName, iotHubConnectionString, this.iotHubServiceClientProtocol, this.sasTokenExpiryTime);
        amqpMessageSender.send(deviceId, moduleId, message);
    }

    /**
     * Provide asynchronous access to open(), which does nothing anymore
     * @deprecated This method no longer does anything. See {@link #open()} documentation for more details
     * @return The future object for the requested operation
     */
    @Deprecated
    public CompletableFuture<Void> openAsync()
    {
        // Codes_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_014: [The function shall create an async wrapper around the open() function call]
        final CompletableFuture<Void> future = new CompletableFuture<>();
        executor.submit(() -> {
            try
            {
                open();
                future.complete(null);
            }
            catch (IOException e)
            {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Provide asynchronous access to close(), which does nothing anymore
     * @deprecated This method no longer does anything. See {@link #close()} documentation for more details
     * @return The future object for the requested operation
     */
    @Deprecated
    public CompletableFuture<Void> closeAsync()
    {
        // Codes_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_015: [The function shall create an async wrapper around the close() function call]
        final CompletableFuture<Void> future = new CompletableFuture<>();
        executor.submit(() -> {
            try
            {
                close();
                future.complete(null);
            }
            catch (IOException e)
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
        }
        catch (Exception e)
        {
            future.completeExceptionally(e);
        }
        });
        return future;
    }

    /**
     * Get FeedbackReceiver object.This API has been deprecated. Use new API without deviceId as an input parameter.
     * @deprecated Use {@link com.microsoft.azure.sdk.iot.service.FeedbackMessageListenerClient} instead
     * @param deviceId The device identifier for the target device
     * @return The instance of the FeedbackReceiver
     */
    @Deprecated
    public FeedbackReceiver getFeedbackReceiver(String deviceId)
    {
        return new FeedbackReceiver(hostName, userName, makeSasToken(), iotHubServiceClientProtocol, deviceId);
    }
    
     /**
     * Get FeedbackReceiver object.  
     * @deprecated Use {@link com.microsoft.azure.sdk.iot.service.FeedbackMessageListenerClient} instead
     * @return The instance of the FeedbackReceiver
     */
    @Deprecated
    public FeedbackReceiver getFeedbackReceiver()
    {
        return new FeedbackReceiver(hostName, userName, makeSasToken(), iotHubServiceClientProtocol);
    }

    /**
     * Get FileUploadNotificationReceiver object.
     * @deprecated Use {@link com.microsoft.azure.sdk.iot.service.FileUploadNotificationListenerClient} instead
     * @return The instance of the FileUploadNotificationReceiver
     */
    @Deprecated
    public FileUploadNotificationReceiver getFileUploadNotificationReceiver()
    {
        return new FileUploadNotificationReceiver(hostName, userName, makeSasToken(), iotHubServiceClientProtocol);
    }

    private String makeSasToken()
    {
        return new IotHubServiceSasToken(iotHubConnectionString, sasTokenExpiryTime).toString();
    }
}
