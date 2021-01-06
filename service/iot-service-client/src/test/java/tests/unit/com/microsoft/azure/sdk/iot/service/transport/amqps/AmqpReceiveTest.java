/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.service.FeedbackBatchMessage;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpReceive;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.reactor.Reactor;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/** Unit tests for AmqpReceive */
@RunWith(JMockit.class)
public class AmqpReceiveTest
{
    @Mocked Proton proton;
    @Mocked Reactor reactor;
    @Mocked Event event;
    @Mocked Connection connection;
    @Mocked Message message;
    
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPRECEIVE_12_001: [The constructor shall copy all input parameters to private member variables for event processing]
    @Test
    public void amqpReceive_init_ok()
    {
        // Arrange
        String hostName = "aaa";
        String userName = "bbb";
        String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        // Act
        AmqpReceive amqpReceive = new AmqpReceive(hostName, userName, sasToken, iotHubServiceClientProtocol);
        String _hostName = Deencapsulation.getField(amqpReceive, "hostName");
        String _userName = Deencapsulation.getField(amqpReceive, "userName");
        String _sasToken = Deencapsulation.getField(amqpReceive, "sasToken");
        // Assert
        assertEquals(hostName, _hostName);
        assertEquals(userName, _userName);
        assertEquals(sasToken, _sasToken);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPRECEIVE_12_008: [The function shall throw IOException if the send handler object is not initialized]
    // Assert
    @Test (expected = IOException.class)
    public void receiveException_throw() throws IOException, InterruptedException
    {
        // Arrange
        String hostName = "aaa";
        String userName = "bbb";
        String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        int timeoutMs = 1;
        AmqpReceive amqpReceive = new AmqpReceive(hostName, userName, sasToken, iotHubServiceClientProtocol);
        // Act
        amqpReceive.receive(timeoutMs);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPRECEIVE_12_010: [The function shall parse the received Json string to FeedbackBath object]
    @Test
    public void onFeedbackReceived_call_flow_ok()
    {
        // Arrange
        String hostName = "aaa";
        String userName = "bbb";
        String sasToken = "ccc";
        String jsonData = "[]";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        AmqpReceive amqpReceive = new AmqpReceive(hostName, userName, sasToken, iotHubServiceClientProtocol);
        // Assert
        new Expectations()
        {
            {
                FeedbackBatchMessage.parse(jsonData);
                
            }
        };
        // Act
        amqpReceive.onFeedbackReceived(jsonData);
    }
}
