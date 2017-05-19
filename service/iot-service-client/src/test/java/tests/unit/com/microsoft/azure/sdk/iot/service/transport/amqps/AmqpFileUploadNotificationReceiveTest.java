/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadNotificationParser;
import com.microsoft.azure.sdk.iot.service.FileUploadNotification;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpFileUploadNotificationReceive;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
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
import static org.junit.Assert.assertNotNull;

/** Unit tests for AmqpFileUploadNotificationReceive */
@RunWith(JMockit.class)
public class AmqpFileUploadNotificationReceiveTest
{
    @Mocked Proton proton;
    @Mocked Reactor reactor;
    @Mocked Event event;
    @Mocked Connection connection;
    @Mocked Message message;
    @Mocked FileUploadNotificationParser mockNotificationParser;
    
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVE_25_001: [The constructor shall copy all input parameters to private member variables for event processing]
    @Test
    public void amqpFileUploadNotificationReceiveInitOk()
    {
        // Arrange
        final String hostName = "aaa";
        final String userName = "bbb";
        final String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        // Act
        AmqpFileUploadNotificationReceive amqpFileUploadNotificationReceive = new AmqpFileUploadNotificationReceive(hostName, userName, sasToken, iotHubServiceClientProtocol);
        final String _hostName = Deencapsulation.getField(amqpFileUploadNotificationReceive, "hostName");
        final String _userName = Deencapsulation.getField(amqpFileUploadNotificationReceive, "userName");
        final String _sasToken = Deencapsulation.getField(amqpFileUploadNotificationReceive, "sasToken");
        // Assert
        assertEquals(hostName, _hostName);
        assertEquals(userName, _userName);
        assertEquals(sasToken, _sasToken);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVE_25_002: [The event handler shall set the member AmqpsReceiveHandler object to handle the given connection events]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVE_25_003: [The function shall create an AmqpsReceiveHandler object to handle reactor events]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVE_25_004: [The function shall invalidate the member AmqpsReceiveHandler object]
    @Test
    public void onReactorInitCallFlowAndInitOk() throws IOException
    {
        // Arrange
        final String hostName = "aaa";
        final String userName = "bbb";
        final String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        AmqpFileUploadNotificationReceive amqpFileUploadNotificationReceive = new AmqpFileUploadNotificationReceive(hostName, userName, sasToken, iotHubServiceClientProtocol);
        amqpFileUploadNotificationReceive.open();
        // Assert
        new Expectations()
        {
            {
                reactor = event.getReactor();
                connection = reactor.connection(Deencapsulation.getField(amqpFileUploadNotificationReceive, "amqpReceiveHandler"));
            }
        };
        // Act
        amqpFileUploadNotificationReceive.onReactorInit(event);

        // Assert
        assertNotNull(Deencapsulation.getField(amqpFileUploadNotificationReceive, "amqpReceiveHandler"));

        // Clean up
        amqpFileUploadNotificationReceive.close();
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVE_25_005: [The function shall initialize the Proton reactor object]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVE_25_006: [The function shall start the Proton reactor object]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVE_25_008: [The function shall stop and free the Proton reactor object ]
    @Test
    public void receiveWithTimoutZeroCallFlowOk() throws IOException, InterruptedException
    {
        // Arrange
        final String hostName = "aaa";
        final String userName = "bbb";
        final String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        int timeoutMs = 0;
        AmqpFileUploadNotificationReceive amqpFileUploadNotificationReceive = new AmqpFileUploadNotificationReceive(hostName, userName, sasToken, iotHubServiceClientProtocol);
        amqpFileUploadNotificationReceive.open();
        // Assert
        new Expectations()
        {
            {
                reactor = proton.reactor(amqpFileUploadNotificationReceive);
                reactor.start();
                reactor.stop();
                reactor.process();
                reactor.free();
        
            }
        };
        // Act
        amqpFileUploadNotificationReceive.receive(timeoutMs);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVE_25_005: [The function shall initialize the Proton reactor object]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVE_25_006: [The function shall start the Proton reactor object]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVE_25_008: [The function shall stop and free the Proton reactor object ]
    @Test
    public void receiveWithTimeoutNonZeroCallOk() throws IOException, InterruptedException
    {
        // Arrange
        final String hostName = "aaa";
        final String userName = "bbb";
        final String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        int timeoutMs = 1;
        AmqpFileUploadNotificationReceive amqpFileUploadNotificationReceive = new AmqpFileUploadNotificationReceive(hostName, userName, sasToken, iotHubServiceClientProtocol);
        amqpFileUploadNotificationReceive.open();
        // Assert
        new Expectations()
        {
            {
                reactor = proton.reactor(amqpFileUploadNotificationReceive);
                reactor.start();
                reactor.stop();
                reactor.process();
                reactor.free();
            }
        };
        // Act
        amqpFileUploadNotificationReceive.receive(timeoutMs);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVE_25_008: [The function shall throw IOException if the send handler object is not initialized]
    // Assert
    @Test (expected = IOException.class)
    public void receiveExceptionThrow() throws IOException, InterruptedException
    {
        // Arrange
        final String hostName = "aaa";
        final String userName = "bbb";
        final String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        int timeoutMs = 1;
        AmqpFileUploadNotificationReceive amqpFileUploadNotificationReceive = new AmqpFileUploadNotificationReceive(hostName, userName, sasToken, iotHubServiceClientProtocol);
        // Act
        amqpFileUploadNotificationReceive.receive(timeoutMs);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVE_25_010: [The function shall parse the received Json string to FeedbackBath object]
    @Test
    public void onFeedbackReceivedCallFlowOk(@Mocked FileUploadNotification mockedNotification)
    {
        // Arrange
        final String hostName = "aaa";
        final String userName = "bbb";
        final String sasToken = "ccc";
        final String jsonData = "[]";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        AmqpFileUploadNotificationReceive amqpFileUploadNotificationReceive = new AmqpFileUploadNotificationReceive(hostName, userName, sasToken, iotHubServiceClientProtocol);

        // Act
        amqpFileUploadNotificationReceive.onFeedbackReceived(jsonData);

        //assert
        new Verifications()
        {
            {
                mockNotificationParser.getBlobName();
                times = 1;
                mockNotificationParser.getBlobSizeInBytesTag();
                times = 1;
                mockNotificationParser.getBlobUri();
                times = 1;
                mockNotificationParser.getDeviceId();
                times = 1;
                mockNotificationParser.getEnqueuedTimeUtc();
                times = 1;
                mockNotificationParser.getLastUpdatedTime();
                times = 1;
            }
        };
    }

}
