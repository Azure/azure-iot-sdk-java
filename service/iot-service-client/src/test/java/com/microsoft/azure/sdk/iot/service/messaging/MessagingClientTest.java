/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.messaging;

import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpSendHandler;
import mockit.*;
import org.junit.Test;

import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class MessagingClientTest
{
    @Mocked
    AmqpSendHandler amqpSend;
    @Mocked
    IotHubServiceSasToken iotHubServiceSasToken;

    // Tests_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_001: [The constructor shall throw IllegalArgumentException if the input string is empty or null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void createFromConnectionString_input_null() throws Exception
    {
        // Arrange
        String connectionString = null;
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        // Act
        new MessagingClient(connectionString, iotHubServiceClientProtocol);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_001: [The constructor shall throw IllegalArgumentException if the input string is empty or null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void createFromConnectionString_input_empty() throws Exception
    {
        // Arrange
        String connectionString = "";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        // Act
        new MessagingClient(connectionString, iotHubServiceClientProtocol);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_004: [The constructor shall throw IllegalArgumentException if the input object is null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructor_input_null() throws Exception
    {
        // Arrange
        IotHubConnectionString iotHubConnectionString = null;
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        // Act
        MessagingClient messagingClient = Deencapsulation.newInstance(MessagingClient.class, iotHubConnectionString, iotHubServiceClientProtocol);
    }
}
