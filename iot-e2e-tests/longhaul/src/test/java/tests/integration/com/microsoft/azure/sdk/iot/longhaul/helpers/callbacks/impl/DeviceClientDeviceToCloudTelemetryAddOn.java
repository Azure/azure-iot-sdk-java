// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.callbacks.impl;

import com.microsoft.azure.sdk.iot.device.Message;
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

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.HTTPS;
import static org.junit.Assert.fail;
import static tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.LonghaulTests.ONE_SECOND_POLLING_INTERVAL;
import static tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.LonghaulTests.SET_MINIMUM_POLLING_INTERVAL;

@Slf4j
@NoArgsConstructor
public class DeviceClientDeviceToCloudTelemetryAddOn implements DeviceClientLonghaulTestAddOn
{
    private final AtomicInteger totalDeviceToCloudMessagesSent = new AtomicInteger(0);
    private final AtomicInteger deviceToCloudMessageIndex = new AtomicInteger(0);
    private final Map<Integer, Message> unacknowledgedDeviceToCloudMessages = new ConcurrentHashMap<>();

    @Override
    public void setupClientBeforeOpen(DeviceClientManager clientUnderTest, ServiceClient serviceClient, RegistryManager registryManager, TestParameters testParameters) throws Exception
    {
        if (testParameters.getProtocol() == HTTPS)
        {
            // By default, HTTP device clients only poll for C2D messages once every 25 minutes. Change this to once every second.
            clientUnderTest.setOption(SET_MINIMUM_POLLING_INTERVAL, ONE_SECOND_POLLING_INTERVAL);
        }
    }

    @Override
    public void setupClientAfterOpen(DeviceClientManager clientUnderTest, ServiceClient serviceClient, RegistryManager registryManager, TestParameters testParameters) throws Exception
    {
        // Do nothing. No setup required.
    }

    @Override
    public void performPeriodicTestableAction(DeviceClientManager clientUnderTest, ServiceClient serviceClient, RegistryManager registryManager, TestParameters testParameters) throws Exception
    {
        deviceToCloudMessageIndex.incrementAndGet();
        Message message = new Message(deviceToCloudMessageIndex + "");
        unacknowledgedDeviceToCloudMessages.put(deviceToCloudMessageIndex.get(), message);
        clientUnderTest.sendEventAsync(
            message,
            (responseStatus, callbackContext) ->
            {
                log.info("Received {} acknowledgement for message {}", responseStatus, callbackContext);
                unacknowledgedDeviceToCloudMessages.remove(((AtomicInteger) callbackContext).get());
            },
            deviceToCloudMessageIndex);

        totalDeviceToCloudMessagesSent.incrementAndGet();
    }

    @Override
    public void performPeriodicStatusReport(TestParameters testParameters)
    {
        log.info("Total d2c messages sent: {}", totalDeviceToCloudMessagesSent.get());
        log.info("Total unacknowledged d2c messages: {}", unacknowledgedDeviceToCloudMessages.size());
        if (unacknowledgedDeviceToCloudMessages.size() > 0)
        {
            log.info("List of unacknowledged d2c message correlation ids:");
            for (int key : unacknowledgedDeviceToCloudMessages.keySet())
            {
                log.info("Unacknowledged d2c message {} with correlation id: {}", key, unacknowledgedDeviceToCloudMessages.get(key).getCorrelationId());
            }
        }
    }

    @Override
    public boolean validateExpectations(TestParameters testParameters)
    {
        if (unacknowledgedDeviceToCloudMessages.size() > 0)
        {
            log.error("Number of unacknowledged device to cloud messages was greater than 0: {}", unacknowledgedDeviceToCloudMessages.size());
            return false;
        }

        return true;
    }
}
