// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.callbacks.impl;

import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.ServiceClient;
import samples.com.microsoft.azure.sdk.iot.DeviceClientManager;
import samples.com.microsoft.azure.sdk.iot.MultiplexingClientManager;
import tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.TestParameters;
import tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.callbacks.DeviceClientLonghaulTestAddOn;
import tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.callbacks.MultiplexingClientLonghaulTestAddOn;

import java.util.Arrays;
import java.util.List;

/**
 * Implementation of {@link MultiplexingClientLonghaulTestAddOn} that allows for the execution of multiple
 * {@link MultiplexingClientLonghaulTestAddOn} implementations to execute rather than just one.
 */
public class MultiplexingClientLonghaulTestAddOnCollection implements MultiplexingClientLonghaulTestAddOn
{
    final List<MultiplexingClientLonghaulTestAddOn> callbacks;

    public MultiplexingClientLonghaulTestAddOnCollection(MultiplexingClientLonghaulTestAddOn... callbacks)
    {
        this.callbacks = Arrays.asList(callbacks);
    }

    @Override
    public void setupClientBeforeOpen(MultiplexingClientManager multiplexingClientUnderTest, DeviceClientManager[] multiplexedClientsUnderTest, ServiceClient serviceClient, RegistryManager registryManager, TestParameters testParameters) throws Exception
    {
        for (MultiplexingClientLonghaulTestAddOn callback : this.callbacks)
        {
            callback.setupClientBeforeOpen(multiplexingClientUnderTest, multiplexedClientsUnderTest, serviceClient, registryManager, testParameters);
        }
    }

    @Override
    public void setupClientAfterOpen(MultiplexingClientManager multiplexingClientUnderTest, DeviceClientManager[] multiplexedClientsUnderTest, ServiceClient serviceClient, RegistryManager registryManager, TestParameters testParameters) throws Exception
    {
        for (MultiplexingClientLonghaulTestAddOn callback : this.callbacks)
        {
            callback.setupClientAfterOpen(multiplexingClientUnderTest, multiplexedClientsUnderTest, serviceClient, registryManager, testParameters);
        }
    }

    @Override
    public void performPeriodicStatusReport(TestParameters testParameters)
    {
        for (MultiplexingClientLonghaulTestAddOn callback : this.callbacks)
        {
            callback.performPeriodicStatusReport(testParameters);
        }
    }

    @Override
    public void performPeriodicTestableAction(MultiplexingClientManager multiplexingClientUnderTest, DeviceClientManager[] multiplexedClientsUnderTest, ServiceClient serviceClient, RegistryManager registryManager, TestParameters testParameters) throws Exception
    {
        for (MultiplexingClientLonghaulTestAddOn callback : this.callbacks)
        {
            callback.performPeriodicTestableAction(multiplexingClientUnderTest, multiplexedClientsUnderTest, serviceClient, registryManager, testParameters);
        }
    }

    @Override
    public boolean validateExpectations(TestParameters testParameters)
    {
        boolean result = true;
        for (MultiplexingClientLonghaulTestAddOn callback : this.callbacks)
        {
            result &= callback.validateExpectations(testParameters);
        }

        return result;
    }
}
