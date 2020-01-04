/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.Message;
import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpSend;
import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpSendHandler;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.reactor.Reactor;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/** Unit tests for AmqpSend */
@RunWith(JMockit.class)
public class AmqpSendTest
{
    @Mocked Proton proton;
    @Mocked Reactor reactor;
    @Mocked Event event;
    @Mocked Connection connection;
    @Mocked Session session;
    @Mocked IotHubConnectionString mockIotHubConnectionString;

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSEND_12_002: [The constructor shall copy all input parameters to private member variables for event processing]
    @Test
    public void constructor_copies_params_to_members_amqps()
    {
        // Arrange
        String hostName = "aaa";
        String userName = "bbb";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        // Act
        AmqpSend amqpSend = new AmqpSend(hostName, userName, mockIotHubConnectionString, iotHubServiceClientProtocol, 100);
        String _hostName = Deencapsulation.getField(amqpSend, "hostName");
        String _userName = Deencapsulation.getField(amqpSend, "userName");
        IotHubConnectionString iotHubConnectionString = Deencapsulation.getField(amqpSend, "iotHubConnectionString");
        IotHubServiceClientProtocol _ioIotHubServiceClientProtocol = Deencapsulation.getField(amqpSend, "iotHubServiceClientProtocol");
        // Assert
        assertEquals(hostName, _hostName);
        assertEquals(userName, _userName);
        assertEquals(mockIotHubConnectionString, iotHubConnectionString);
        assertEquals(iotHubServiceClientProtocol, _ioIotHubServiceClientProtocol);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSEND_12_002: [The constructor shall copy all input parameters to private member variables for event processing]
    @Test
    public void constructor_copies_params_to_members_amqps_ws()
    {
        // Arrange
        String hostName = "aaa";
        String userName = "bbb";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS_WS;
        // Act
        AmqpSend amqpSend = new AmqpSend(hostName, userName, mockIotHubConnectionString, iotHubServiceClientProtocol, 100);
        String _hostName = Deencapsulation.getField(amqpSend, "hostName");
        String _userName = Deencapsulation.getField(amqpSend, "userName");
        IotHubConnectionString iotHubConnectionString = Deencapsulation.getField(amqpSend, "iotHubConnectionString");
        IotHubServiceClientProtocol _ioIotHubServiceClientProtocol = Deencapsulation.getField(amqpSend, "iotHubServiceClientProtocol");
        // Assert
        assertEquals(hostName, _hostName);
        assertEquals(userName, _userName);
        assertEquals(mockIotHubConnectionString, iotHubConnectionString);
        assertEquals(iotHubServiceClientProtocol, _ioIotHubServiceClientProtocol);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSEND_12_001: [The constructor shall throw IllegalArgumentException if any of the input parameter is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructor_checks_if_hostName_null()
    {
        // Arrange
        String hostName = null;
        String userName = "bbb";
        String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        // Act
        AmqpSend amqpSend = new AmqpSend(hostName, userName, mockIotHubConnectionString, iotHubServiceClientProtocol, 100);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSEND_12_001: [The constructor shall throw IllegalArgumentException if any of the input parameter is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructor_checks_if_hostName_empty()
    {
        // Arrange
        String hostName = "";
        String userName = "bbb";
        String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        // Act
        AmqpSend amqpSend = new AmqpSend(hostName, userName, mockIotHubConnectionString, iotHubServiceClientProtocol, 100);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSEND_12_001: [The constructor shall throw IllegalArgumentException if any of the input parameter is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructor_checks_if_userName_null()
    {
        // Arrange
        String hostName = "aaa";
        String userName = null;
        String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        // Act
        AmqpSend amqpSend = new AmqpSend(hostName, userName, mockIotHubConnectionString, iotHubServiceClientProtocol, 100);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSEND_12_001: [The constructor shall throw IllegalArgumentException if any of the input parameter is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructor_checks_if_userName_empty()
    {
        // Arrange
        String hostName = "aaa";
        String userName = "";
        String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        // Act
        AmqpSend amqpSend = new AmqpSend(hostName, userName, mockIotHubConnectionString, iotHubServiceClientProtocol, 100);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSEND_12_001: [The constructor shall throw IllegalArgumentException if any of the input parameter is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructor_checks_if_connectionstring_null()
    {
        // Arrange
        String hostName = "aaa";
        String userName = "bbb";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        // Act
        AmqpSend amqpSend = new AmqpSend(hostName, userName, null, iotHubServiceClientProtocol, 100);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSEND_12_001: [The constructor shall throw IllegalArgumentException if any of the input parameter is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructor_checks_if_protocol_null()
    {
        // Arrange
        String hostName = "aaa";
        String userName = "bbb";
        String sasToken = "ccc";
        
        IotHubServiceClientProtocol iotHubServiceClientProtocol = null;
        // Act
        AmqpSend amqpSend = new AmqpSend(hostName, userName, mockIotHubConnectionString, iotHubServiceClientProtocol, 100);
    }
}
