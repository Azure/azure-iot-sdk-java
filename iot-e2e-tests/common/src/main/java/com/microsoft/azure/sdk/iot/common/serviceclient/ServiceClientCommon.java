/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.serviceclient;

import com.microsoft.azure.sdk.iot.common.iothubservices.MethodNameLoggingIntegrationTest;
import com.microsoft.azure.sdk.iot.service.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.Test;
import org.junit.runners.Parameterized;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import static org.junit.Assert.*;

public class ServiceClientCommon extends MethodNameLoggingIntegrationTest
{
    protected static String iotHubConnectionString = "";
    protected static String invalidCertificateServerConnectionString = "";
    private static String deviceId = "java-service-client-e2e-test";
    private static String content = "abcdefghijklmnopqrstuvwxyz1234567890";

    public ServiceClientCommon(IotHubServiceClientProtocol protocol)
    {
        this.testInstance = new ServiceClientITRunner(protocol);
    }

    private class ServiceClientITRunner
    {
        private IotHubServiceClientProtocol protocol;

        public ServiceClientITRunner(IotHubServiceClientProtocol protocol)
        {
            this.protocol = protocol;
        }
    }

    private ServiceClientITRunner testInstance;

    //This function is run before even the @BeforeClass annotation, so it is used as the @BeforeClass method
    @Parameterized.Parameters(name = "{0}")
    public static Collection inputsCommon()
    {
        String uuid = UUID.randomUUID().toString();
        deviceId = deviceId.concat("-" + uuid);


        List inputs = Arrays.asList(
                new Object[][]
                        {
                                {IotHubServiceClientProtocol.AMQPS},
                                {IotHubServiceClientProtocol.AMQPS_WS}
                        }
        );

        return inputs;
    }

    @Test
    public void cloudToDeviceTelemetry() throws Exception
    {
        // Arrange

        // We remove and recreate the device for a clean start
        RegistryManager registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);

        try
        {
            // We remove and recreate the device for a clean start
            registryManager.removeDevice(deviceId);
        }
        catch (IOException|IotHubException e)
        {
        }

        Device deviceAdded = Device.createFromId(deviceId, null, null);
        registryManager.addDevice(deviceAdded);

        Device deviceGetBefore = registryManager.getDevice(deviceId);

        // Act

        // Create service client
        ServiceClient serviceClient = ServiceClient.createFromConnectionString(iotHubConnectionString, testInstance.protocol);
        CompletableFuture<Void> futureOpen = serviceClient.openAsync();
        futureOpen.get();

        Message message = new Message(content.getBytes());

        CompletableFuture<Void> completableFuture = serviceClient.sendAsync(deviceId, message);
        completableFuture.get();

        Device deviceGetAfter = registryManager.getDevice(deviceId);
        CompletableFuture<Void> futureClose = serviceClient.closeAsync();
        futureClose.get();

        registryManager.removeDevice(deviceId);

        // Assert
        assertEquals(deviceGetBefore.getDeviceId(), deviceGetAfter.getDeviceId());
        assertEquals(0, deviceGetBefore.getCloudToDeviceMessageCount());
        assertEquals(1, deviceGetAfter.getCloudToDeviceMessageCount());

        registryManager.close();
    }

    @Test
    public void serviceClientValidatesRemoteCertificateWhenSendingTelemetry() throws IOException
    {
        boolean expectedExceptionWasCaught = false;

        ServiceClient serviceClient = ServiceClient.createFromConnectionString(invalidCertificateServerConnectionString, testInstance.protocol);

        try
        {
            serviceClient.open();
            serviceClient.send(deviceId, new Message("some message"));
        }
        catch (IOException e)
        {
            expectedExceptionWasCaught = true;
        }
        catch (Exception e)
        {
            fail("Expected IOException, but received: " + e.getMessage());
        }

        assertTrue("Expected an exception due to service presenting invalid certificate", expectedExceptionWasCaught);
    }

    @Test
    public void serviceClientValidatesRemoteCertificateWhenGettingFeedbackReceiver() throws IOException
    {
        boolean expectedExceptionWasCaught = false;

        ServiceClient serviceClient = ServiceClient.createFromConnectionString(invalidCertificateServerConnectionString, testInstance.protocol);

        try
        {
            serviceClient.open();
            FeedbackReceiver receiver = serviceClient.getFeedbackReceiver();
            receiver.open();
            receiver.receive(1000);
        }
        catch (IOException e)
        {
            expectedExceptionWasCaught = true;
        }
        catch (Exception e)
        {
            fail("Expected IOException, but received: " + e.getMessage());
        }

        assertTrue("Expected an exception due to service presenting invalid certificate", expectedExceptionWasCaught);
    }

    @Test
    public void serviceClientValidatesRemoteCertificateWhenGettingFileUploadFeedbackReceiver() throws IOException
    {
        boolean expectedExceptionWasCaught = false;

        ServiceClient serviceClient = ServiceClient.createFromConnectionString(invalidCertificateServerConnectionString, testInstance.protocol);

        try
        {
            serviceClient.open();
            FileUploadNotificationReceiver receiver = serviceClient.getFileUploadNotificationReceiver();
            receiver.open();
            receiver.receive(1000);
        }
        catch (IOException e)
        {
            expectedExceptionWasCaught = true;
        }
        catch (Exception e)
        {
            fail("Expected IOException, but received: " + e.getMessage());
        }

        assertTrue("Expected an exception due to service presenting invalid certificate", expectedExceptionWasCaught);
    }
}
