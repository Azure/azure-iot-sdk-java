/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.service.FileUploadNotification;
import com.microsoft.azure.sdk.iot.service.FileUploadNotificationReceiver;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpFileUploadNotificationReceive;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.Verifications;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class FileUploadNotificationReceiverTest
{
    @Mocked
    AmqpFileUploadNotificationReceive amqpFileUploadNotificationReceive;

    // Tests_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATIONRECEIVER_25_001: [** The constructor shall throw IllegalArgumentException if any the input string is null or empty **]**
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructorInputHostNameNull() throws Exception
    {
        // Arrange
        final String hostName = null;
        final String userName = "xxx";
        final String sasToken = "xxx";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        // Act
        FileUploadNotificationReceiver fileUploadNotificationReceiver = Deencapsulation.newInstance(FileUploadNotificationReceiver.class, hostName, userName, sasToken, iotHubServiceClientProtocol);
    }

    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructorInputUserNameNull() throws Exception
    {
        // Arrange
        final String hostName = "xxx";
        final String userName = null;
        final String sasToken = "xxx";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        // Act
        FileUploadNotificationReceiver fileUploadNotificationReceiver = Deencapsulation.newInstance(FileUploadNotificationReceiver.class, hostName, userName, sasToken, iotHubServiceClientProtocol);
    }

    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructorInputSasTokenNull() throws Exception
    {
        // Arrange
        final String hostName = "xxx";
        final String userName = "xxx";
        final String sasToken = null;
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        // Act
        FileUploadNotificationReceiver fileUploadNotificationReceiver = Deencapsulation.newInstance(FileUploadNotificationReceiver.class, hostName, userName, sasToken, iotHubServiceClientProtocol);
    }

    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructorInputProtocolNull() throws Exception
    {
        // Arrange
        final String hostName = "xxx";
        final String userName = "xxx";
        final String sasToken = "xxx";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = null;
        // Act
        FileUploadNotificationReceiver fileUploadNotificationReceiver = Deencapsulation.newInstance(FileUploadNotificationReceiver.class, hostName, userName, sasToken, iotHubServiceClientProtocol);
    }
    
  
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructorInputHostNameEmpty() throws Exception
    {
        // Arrange
        final String hostName = "";
        final String userName = "xxx";
        final String sasToken = "xxx";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        // Act
        FileUploadNotificationReceiver fileUploadNotificationReceiver = Deencapsulation.newInstance(FileUploadNotificationReceiver.class, hostName, userName, sasToken, iotHubServiceClientProtocol);
    }

    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructorInputUserNameEmpty() throws Exception
    {
        // Arrange
        final String hostName = "xxx";
        final String userName = "";
        final String sasToken = "xxx";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        // Act
        FileUploadNotificationReceiver fileUploadNotificationReceiver = Deencapsulation.newInstance(FileUploadNotificationReceiver.class, hostName, userName, sasToken, iotHubServiceClientProtocol);
    }

    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructorInputSasTokenEmpty() throws Exception
    {
        // Arrange
        final String hostName = "xxx";
        final String userName = "xxx";
        final String sasToken = "";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        // Act
        FileUploadNotificationReceiver fileUploadNotificationReceiver = Deencapsulation.newInstance(FileUploadNotificationReceiver.class, hostName, userName, sasToken, iotHubServiceClientProtocol);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATIONRECEIVER_25_002: [** The constructor shall create a new instance of AmqpFileUploadNotificationReceive object **]**
    @Test
    public void constructorSaveProperties() throws Exception
    {
        // Arrange
        final String hostName = "aaa";
        final String userName = "bbb";
        final String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;

        // Act
        FileUploadNotificationReceiver fileUploadNotificationReceiver = Deencapsulation.newInstance(FileUploadNotificationReceiver.class, hostName, userName, sasToken, iotHubServiceClientProtocol);

        new Verifications()
        {
            {
                new AmqpFileUploadNotificationReceive(anyString, anyString, anyString, iotHubServiceClientProtocol);
                times = 1;

            }
        };
    }

    // Tests_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATIONRECEIVER_25_004: [** The function shall call open() on the member AmqpFileUploadNotificationReceive object **]**
    @Test
    public void openCallReceiverOpen() throws Exception
    {
        // Arrange
        final String hostName = "xxx";
        final String userName = "xxx";
        final String sasToken = "xxx";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        FileUploadNotificationReceiver fileUploadNotificationReceiver = Deencapsulation.newInstance(FileUploadNotificationReceiver.class, hostName, userName, sasToken, iotHubServiceClientProtocol);

        // Act
        fileUploadNotificationReceiver.open();

        // Assert
        new Verifications()
        {
            {
                amqpFileUploadNotificationReceive.open();
                times = 1;
            }
        };
    }

    // Tests_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATIONRECEIVER_25_006: [ The function shall call close() on the member AmqpFileUploadNotificationReceive object ]
    @Test
    public void closeCallReceiverClose() throws Exception
    {
        // Arrange
        final String hostName = "xxx";
        final String userName = "xxx";
        final String sasToken = "xxx";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        FileUploadNotificationReceiver fileUploadNotificationReceiver = Deencapsulation.newInstance(FileUploadNotificationReceiver.class, hostName, userName, sasToken, iotHubServiceClientProtocol);

        // Act
        fileUploadNotificationReceiver.close();

        // Assert
        new Verifications()
        {
            {
                amqpFileUploadNotificationReceive.close();
                times = 1;
            }
        };
    }
    
    // Tests_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATIONRECEIVER_25_007: [ The function shall call receive(long timeoutMs) function with the default timeout ]
    @Test
    public void receiveCallReceiveTimeout() throws Exception
    {
        // Arrange
        final String hostName = "xxx";
        final String userName = "xxx";
        final String sasToken = "xxx";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        FileUploadNotificationReceiver fileUploadNotificationReceiver = Deencapsulation.newInstance(FileUploadNotificationReceiver.class, hostName, userName, sasToken, iotHubServiceClientProtocol);

        // Act
        fileUploadNotificationReceiver.receive();

        // Assert
        new Verifications()
        {
            {
                amqpFileUploadNotificationReceive.receive(Deencapsulation.getField(fileUploadNotificationReceiver, "DEFAULT_TIMEOUT_MS"));
                times = 1;
            }
        };
    }
    
    // Tests_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATIONRECEIVER_25_008: [ The function shall throw IOException if the member AmqpFileUploadNotificationReceive object has not been initialized ]
    // Assert
    @Test (expected = IOException.class)
    public void receiveWithTimeoutReceiverNull() throws Exception
    {
        // Arrange
        long timeoutMs = 1000;
        final String hostName = "xxx";
        final String userName = "xxx";
        final String sasToken = "xxx";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        FileUploadNotificationReceiver fileUploadNotificationReceiver = Deencapsulation.newInstance(FileUploadNotificationReceiver.class, hostName, userName, sasToken, iotHubServiceClientProtocol);
        Deencapsulation.setField(fileUploadNotificationReceiver, "amqpFileUploadNotificationReceive", null);
        // Act
        fileUploadNotificationReceiver.receive(timeoutMs);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATIONRECEIVER_25_009: [ The function shall call receive() on the member AmqpFileUploadNotificationReceive object and return with the result ]
    @Test
    public void receiveWithTimeoutCallReceiveTimeout() throws Exception
    {
        // Arrange
        long timeoutMs = 1000;
        final String hostName = "xxx";
        final String userName = "xxx";
        final String sasToken = "xxx";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        FileUploadNotificationReceiver fileUploadNotificationReceiver = Deencapsulation.newInstance(FileUploadNotificationReceiver.class, hostName, userName, sasToken, iotHubServiceClientProtocol);

        // Act
        fileUploadNotificationReceiver.receive(timeoutMs);

        // Assert
        new Verifications()
        {
            {
                amqpFileUploadNotificationReceive.receive(timeoutMs);
                times = 1;
            }
        };
    }
    
    // Tests_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATIONRECEIVER_25_010: [ The function shall create an async wrapper around the open() function call ]
    @Test
    public void openAsync() throws Exception
    {
        // Arrange
        final String hostName = "xxx";
        final String userName = "xxx";
        final String sasToken = "xxx";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        FileUploadNotificationReceiver fileUploadNotificationReceiver = Deencapsulation.newInstance(FileUploadNotificationReceiver.class, hostName, userName, sasToken, iotHubServiceClientProtocol);

        // Act
        CompletableFuture<Void> completableFuture = fileUploadNotificationReceiver.openAsync();
        completableFuture.get();

        // Assert
        new Verifications()
        {
            {
                amqpFileUploadNotificationReceive.open();
                times = 1;
                fileUploadNotificationReceiver.open();
                times = 1;
            }
        };
    }

    // Tests_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATIONRECEIVER_25_011: [ The function shall create an async wrapper around the close() function call ]
    @Test
    public void closeAsync() throws Exception
    {
        // Arrange
        final String hostName = "xxx";
        final String userName = "xxx";
        final String sasToken = "xxx";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        FileUploadNotificationReceiver fileUploadNotificationReceiver = Deencapsulation.newInstance(FileUploadNotificationReceiver.class, hostName, userName, sasToken, iotHubServiceClientProtocol);

        // Act
        CompletableFuture<Void> completableFuture = fileUploadNotificationReceiver.closeAsync();
        completableFuture.get();

        // Assert
        new Verifications()
        {
            {
                amqpFileUploadNotificationReceive.close();
                times = 1;
                fileUploadNotificationReceiver.close();
                times = 1;
            }
        };
    }
    
    // Tests_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATIONRECEIVER_25_012: [ The function shall create an async wrapper around the receive() function call using the default timeout ]
    @Test
    public void receiveAsync() throws Exception
    {
        // Arrange
        final String hostName = "xxx";
        final String userName = "xxx";
        final String sasToken = "xxx";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        FileUploadNotificationReceiver fileUploadNotificationReceiver = Deencapsulation.newInstance(FileUploadNotificationReceiver.class, hostName, userName, sasToken, iotHubServiceClientProtocol);

        // Act
        CompletableFuture<FileUploadNotification> completableFuture = fileUploadNotificationReceiver.receiveAsync();
        completableFuture.get();

        // Assert
        new Verifications()
        {
            {
                amqpFileUploadNotificationReceive.receive(Deencapsulation.getField(fileUploadNotificationReceiver, "DEFAULT_TIMEOUT_MS"));
                times = 1;
                fileUploadNotificationReceiver.receive();
                times = 1;
            }
        };
    }
    
    // Tests_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATIONRECEIVER_25_013: [ The function shall create an async wrapper around the receive(long timeoutMs) function call ]
    @Test
    public void receiveWithTimeoutAsync() throws Exception
    {
        // Arrange
        long timeoutMs = 1000;
        final String hostName = "xxx";
        final String userName = "xxx";
        final String sasToken = "xxx";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        FileUploadNotificationReceiver fileUploadNotificationReceiver = Deencapsulation.newInstance(FileUploadNotificationReceiver.class, hostName, userName, sasToken, iotHubServiceClientProtocol);
        // Act
        CompletableFuture<FileUploadNotification> completableFuture = fileUploadNotificationReceiver.receiveAsync(timeoutMs);
        completableFuture.get();

        // Assert
        new Verifications()
        {
            {
                amqpFileUploadNotificationReceive.receive(timeoutMs);
                times = 1;
                fileUploadNotificationReceiver.receive(anyLong);
                times = 1;
            }
        };

    }
}
