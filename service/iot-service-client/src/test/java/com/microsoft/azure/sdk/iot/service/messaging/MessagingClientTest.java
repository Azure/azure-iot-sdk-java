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

    // Tests_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_005: [The constructor shall create a SAS token object using the IotHubConnectionString]
    // Tests_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_006: [The constructor shall store connection string, hostname, username and sasToken]
    // Tests_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_007: [The constructor shall create a new instance of AmqpSend object]
    @Test
    public void constructor_create_sas_token() throws Exception
    {
        // Arrange
        String iotHubName = "IOTHUBNAME";
        String hostName = "HOSTNAME";
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + "." + iotHubName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        // Act
        MessagingClient messagingClient = new MessagingClient(connectionString, iotHubServiceClientProtocol);

        // Assert
        assertNotEquals(hostName, Deencapsulation.getField(messagingClient, "hostName"));
    }

    // Tests_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_013: [The function shall call send() on the member AMQP sender object with the given parameters]
    @Test
    public void send_call_sender_close() throws Exception
    {
        // Arrange
        String iotHubName = "IOTHUBNAME";
        String hostName = "HOSTNAME";
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + "." + iotHubName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
        String deviceId = "XXX";
        String content = "HELLO";
        Message iotMessage = new Message(content);
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        MessagingClient messagingClient = new MessagingClient(connectionString, iotHubServiceClientProtocol);
        // Assert
        new Expectations()
        {
            {
                amqpSend.sendAsync(deviceId, null, iotMessage, (Consumer<SendResult>) any, any);
            }
        };
        // Act
        messagingClient.send(deviceId, iotMessage);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_28_002: [The function shall call send() on the member AMQP sender object with the given parameters]
    @Test
    public void send_module_call_sender_close() throws Exception
    {
        // Arrange
        String iotHubName = "IOTHUBNAME";
        String hostName = "HOSTNAME";
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + "." + iotHubName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
        String deviceId = "XXX";
        String moduleId = "XXX";
        String content = "HELLO";
        Message iotMessage = new Message(content);
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        MessagingClient messagingClient = new MessagingClient(connectionString, iotHubServiceClientProtocol);
        // Assert
        new Expectations()
        {
            {
                amqpSend.sendAsync(deviceId, moduleId, iotMessage, (Consumer<SendResult>) any, any);
            }
        };
        // Act
        messagingClient.send(deviceId, moduleId, iotMessage);
    }
}
