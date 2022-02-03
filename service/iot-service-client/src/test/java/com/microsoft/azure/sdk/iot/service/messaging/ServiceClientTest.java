/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.messaging;

import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpSendHandler;
import mockit.*;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ServiceClientTest
{
    @Mocked
    AmqpSendHandler amqpSend;
    @Mocked
    IotHubServiceSasToken iotHubServiceSasToken;
    @Mocked
    FeedbackReceiver feedbackReceiver;
    @Mocked
    FeedbackMessageReceivedCallback feedbackMessageReceivedCallback;
    @Mocked
    FileUploadNotificationReceivedCallback fileUploadNotificationReceivedCallback;

    // Tests_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_001: [The constructor shall throw IllegalArgumentException if the input string is empty or null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void createFromConnectionString_input_null() throws Exception
    {
        // Arrange
        String connectionString = null;
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        // Act
        new ServiceClient(connectionString, iotHubServiceClientProtocol);
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
        new ServiceClient(connectionString, iotHubServiceClientProtocol);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_002: [The constructor shall create IotHubConnectionString object using the IotHubConnectionStringBuilder]
    // Tests_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_003: [The constructor shall create a new instance of ServiceClient using the created IotHubConnectionString object and the given iotHubServiceClientProtocol return with it]
    @Test
    public void createFromConnectionString_check_call_flow() throws Exception
    {
        // Arrange
        String iotHubName = "b.c.d";
        String hostName = "HOSTNAME." + iotHubName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        new Expectations()
        {
            {
                IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                Deencapsulation.newInstance(ServiceClient.class, iotHubConnectionString, iotHubServiceClientProtocol);
            }
        };
        // Act
        ServiceClient iotHubServiceClient = (ServiceClient) new ServiceClient(connectionString, iotHubServiceClientProtocol);
        // Assert
        assertNotEquals(null, iotHubServiceClient);
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
        ServiceClient serviceClient = Deencapsulation.newInstance(ServiceClient.class, iotHubConnectionString, iotHubServiceClientProtocol); 
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
        ServiceClient serviceClient = new ServiceClient(connectionString, iotHubServiceClientProtocol);

        // Assert
        assertNotEquals(hostName, Deencapsulation.getField(serviceClient, "hostName"));
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
        ServiceClient serviceClient = new ServiceClient(connectionString, iotHubServiceClientProtocol);
        // Assert
        new Expectations()
        {
            {
                amqpSend.send(deviceId, null, iotMessage);
            }
        };
        // Act
        serviceClient.send(deviceId, iotMessage);
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
        ServiceClient serviceClient = new ServiceClient(connectionString, iotHubServiceClientProtocol);
        // Assert
        new Expectations()
        {
            {
                amqpSend.send(deviceId, moduleId, iotMessage);
            }
        };
        // Act
        serviceClient.send(deviceId, moduleId, iotMessage);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_017: [The function shall create a FeedbackReceiver object and returns with it]
    @Test
    public void getFeedbackReceiver_good_case() throws Exception
    {
        // Arrange
        String iotHubName = "IOTHUBNAME";
        String hostName = "HOSTNAME";
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + "." + iotHubName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        String deviceId = "XXX";
        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        ServiceClient serviceClient = new ServiceClient(connectionString, iotHubServiceClientProtocol);
        // Assert
        new Expectations()
        {
            {
                feedbackReceiver = new FeedbackReceiver(feedbackMessageReceivedCallback, anyString, anyString, iotHubServiceClientProtocol, (ProxyOptions) any, (SSLContext) any);
            }
        };
        // Act
        FeedbackReceiver feedbackReceiver = serviceClient.getFeedbackReceiver(feedbackMessageReceivedCallback);
        // Assert
        assertNotEquals(null, feedbackReceiver);
    }
    
    // Tests_SRS_SERVICE_SDK_JAVA_SERVICECLIENT_12_017: [The function shall create a FeedbackReceiver object and returns with it]
    @Test
    public void getFeedbackReceiver_good_case_without_deviceid() throws Exception
    {
        // Arrange
        String iotHubName = "IOTHUBNAME";
        String hostName = "HOSTNAME";
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + "." + iotHubName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        ServiceClient serviceClient = new ServiceClient(connectionString, iotHubServiceClientProtocol);
        // Assert
        new Expectations()
        {
            {
                feedbackReceiver = new FeedbackReceiver(feedbackMessageReceivedCallback, anyString, anyString, iotHubServiceClientProtocol, (ProxyOptions) any, (SSLContext) any);
            }
        };
        // Act
        FeedbackReceiver feedbackReceiver = serviceClient.getFeedbackReceiver(feedbackMessageReceivedCallback);
        // Assert
        assertNotEquals(null, feedbackReceiver);
    }
}
