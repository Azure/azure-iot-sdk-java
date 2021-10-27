// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.callbacks.impl;

import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.ServiceClient;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import samples.com.microsoft.azure.sdk.iot.DeviceClientManager;
import samples.com.microsoft.azure.sdk.iot.MultiplexingClientManager;
import tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.TestParameters;
import tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.callbacks.MultiplexingClientLonghaulTestAddOn;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class MultiplexingClientDeviceToCloudTelemetryAddOn implements MultiplexingClientLonghaulTestAddOn
{
    Map<Integer, AtomicInteger> totalMessagesSent = new ConcurrentHashMap<>();
    Map<Integer, AtomicInteger> deviceToCloudMessageIndex = new ConcurrentHashMap<>();
    Map<Integer, Map<Integer, Message>> unacknowledgedMessages = new ConcurrentHashMap<>();

    public MultiplexingClientDeviceToCloudTelemetryAddOn(int multiplexedDeviceCount)
    {
        for (int multiplexedClientIndex = 0; multiplexedClientIndex < multiplexedDeviceCount; multiplexedClientIndex++)
        {
            unacknowledgedMessages.put(multiplexedClientIndex, new ConcurrentHashMap<>());
            deviceToCloudMessageIndex.put(multiplexedClientIndex, new AtomicInteger(0));
            totalMessagesSent.put(multiplexedClientIndex, new AtomicInteger(0));
        }
    }

    @Override
    public void setupClientBeforeOpen(MultiplexingClientManager multiplexingClientUnderTest, DeviceClientManager[] multiplexedClientsUnderTest, ServiceClient serviceClient, RegistryManager registryManager, TestParameters testParameters) throws Exception
    {
        // Do nothing. No setup required.
    }

    @Override
    public void setupClientAfterOpen(MultiplexingClientManager multiplexingClientUnderTest, DeviceClientManager[] multiplexedClientsUnderTest, ServiceClient serviceClient, RegistryManager registryManager, TestParameters testParameters) throws Exception
    {
        // Do nothing. No setup required.
    }

    @Override
    public void performPeriodicStatusReport(TestParameters testParameters)
    {
        for (int multiplexedClientIndex = 0; multiplexedClientIndex < unacknowledgedMessages.size(); multiplexedClientIndex++)
        {
            log.info("Total d2c messages sent: {}", totalMessagesSent.get(multiplexedClientIndex));
            log.info("Total unacknowledged d2c messages: {}", unacknowledgedMessages.size());
            if (unacknowledgedMessages.get(multiplexedClientIndex).size() > 0)
            {
                log.info("List of unacknowledged d2c message correlation ids:");
                for (int key : unacknowledgedMessages.get(multiplexedClientIndex).keySet())
                {
                    log.info("Unacknowledged d2c message {} with correlation id: {}", key, unacknowledgedMessages.get(multiplexedClientIndex).get(key).getCorrelationId());
                }
            }
        }
    }

    @Override
    public void performPeriodicTestableAction(MultiplexingClientManager multiplexingClientUnderTest, DeviceClientManager[] multiplexedClientsUnderTest, ServiceClient serviceClient, RegistryManager registryManager, TestParameters testParameters) throws Exception
    {
        for (int multiplexedClientIndex = 0; multiplexedClientIndex < multiplexedClientsUnderTest.length; multiplexedClientIndex++)
        {
            deviceToCloudMessageIndex.get(multiplexedClientIndex).incrementAndGet();
            Message message = new Message(deviceToCloudMessageIndex + "");
            unacknowledgedMessages.get(multiplexedClientIndex).put(deviceToCloudMessageIndex.get(multiplexedClientIndex).get(), message);
            int finalMultiplexedClientIndex = multiplexedClientIndex;
            multiplexedClientsUnderTest[multiplexedClientIndex].sendEventAsync(
                message,
                (responseStatus, callbackContext) ->
                {
                    String deviceId = multiplexedClientsUnderTest[finalMultiplexedClientIndex].getConfig().getDeviceId();
                    log.info("Received {} acknowledgement for device {} message {}", responseStatus, deviceId, callbackContext);
                    unacknowledgedMessages.get(finalMultiplexedClientIndex).remove(((AtomicInteger) callbackContext).get());
                },
                deviceToCloudMessageIndex.get(multiplexedClientIndex));

            totalMessagesSent.get(multiplexedClientIndex).incrementAndGet();
        }
    }

    @Override
    public boolean validateExpectations(TestParameters testParameters)
    {
        //TODO
        return true;
    }
}
