// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.callbacks.impl;

import com.microsoft.azure.sdk.iot.device.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageCallback;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.ServiceClient;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import samples.com.microsoft.azure.sdk.iot.DeviceClientManager;
import tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.TestParameters;
import tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.callbacks.DeviceClientLonghaulTestAddOn;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.fail;

@Slf4j
@NoArgsConstructor
public class DeviceClientCloudToDeviceTelemetryAddOn implements DeviceClientLonghaulTestAddOn
{
    AtomicInteger totalCloudToDeviceMessagesSent = new AtomicInteger(0);
    AtomicInteger cloudToDeviceMessageIndex = new AtomicInteger(0);
    Map<Integer, com.microsoft.azure.sdk.iot.service.Message> unreceivedCloudToDeviceMessages = new ConcurrentHashMap<>();

    @Override
    public void setupClientBeforeOpen(DeviceClientManager clientUnderTest, ServiceClient serviceClient, RegistryManager registryManager, TestParameters testParameters) throws Exception
    {
        MessageCallback cloudToDeviceMessageReceivedCallback = (message, callbackContext) ->
        {
            int messageId = Integer.parseInt(message.getMessageId());
            String deviceId = clientUnderTest.getConfig().getDeviceId();
            log.info("Received cloud to device message on device {} with message id {}", deviceId, messageId);
            unreceivedCloudToDeviceMessages.remove(messageId);
            return IotHubMessageResult.COMPLETE;
        };

        clientUnderTest.setMessageCallback(cloudToDeviceMessageReceivedCallback, null);
    }

    @Override
    public void setupClientAfterOpen(DeviceClientManager clientUnderTest, ServiceClient serviceClient, RegistryManager registryManager, TestParameters testParameters) throws Exception
    {
        // do nothing
    }

    @Override
    public void performPeriodicTestableAction(DeviceClientManager clientUnderTest, ServiceClient serviceClient, RegistryManager registryManager, TestParameters testParameters) throws Exception
    {
        cloudToDeviceMessageIndex.incrementAndGet();
        com.microsoft.azure.sdk.iot.service.Message cloudToDeviceMessage = new com.microsoft.azure.sdk.iot.service.Message();
        cloudToDeviceMessage.setMessageId(String.valueOf(cloudToDeviceMessageIndex.get()));
        unreceivedCloudToDeviceMessages.put(cloudToDeviceMessageIndex.get(), cloudToDeviceMessage);
        String deviceId = clientUnderTest.getConfig().getDeviceId();
        serviceClient.send(deviceId, cloudToDeviceMessage);

        totalCloudToDeviceMessagesSent.incrementAndGet();
    }

    @Override
    public void performPeriodicStatusReport(TestParameters testParameters)
    {
        log.info("Total c2d messages sent: {}", totalCloudToDeviceMessagesSent.get());
        log.info("Total unacknowledged c2d messages: {}", unreceivedCloudToDeviceMessages.size());

        if (unreceivedCloudToDeviceMessages.size() > 0)
        {
            log.info("List of unacknowledged c2d message correlation ids:");
            for (int key : unreceivedCloudToDeviceMessages.keySet())
            {
                log.info("Unacknowledged d2c message {} with correlation id: {}", key, unreceivedCloudToDeviceMessages.get(key).getCorrelationId());
            }
        }
    }

    @Override
    public boolean validateExpectations(TestParameters testParameters)
    {
        if (unreceivedCloudToDeviceMessages.size() > 0)
        {
            log.error("Number of unreceived cloud to device messages was greater than 0: {}", unreceivedCloudToDeviceMessages.size());
            return false;
        }

        return true;
    }
}
