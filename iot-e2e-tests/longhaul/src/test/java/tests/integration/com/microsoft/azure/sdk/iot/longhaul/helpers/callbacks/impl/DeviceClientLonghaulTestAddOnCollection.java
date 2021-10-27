// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.callbacks.impl;

import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.ServiceClient;
import samples.com.microsoft.azure.sdk.iot.DeviceClientManager;
import tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.TestParameters;
import tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.callbacks.DeviceClientLonghaulTestAddOn;

import java.util.Arrays;
import java.util.List;

/**
 * Implementation of {@link DeviceClientLonghaulTestAddOn} that allows for the execution of multiple
 * {@link DeviceClientLonghaulTestAddOn} implementations to execute rather than just one.
 */
public class DeviceClientLonghaulTestAddOnCollection implements DeviceClientLonghaulTestAddOn
{
    final List<DeviceClientLonghaulTestAddOn> callbacks;

    public DeviceClientLonghaulTestAddOnCollection(DeviceClientLonghaulTestAddOn... callbacks)
    {
        this.callbacks = Arrays.asList(callbacks);
    }

    @Override
    public void setupClientBeforeOpen(DeviceClientManager clientUnderTest, ServiceClient serviceClient, RegistryManager registryManager, TestParameters testParameters) throws Exception
    {
        for (DeviceClientLonghaulTestAddOn callback : this.callbacks)
        {
            callback.setupClientBeforeOpen(clientUnderTest, serviceClient, registryManager, testParameters);
        }
    }

    @Override
    public void setupClientAfterOpen(DeviceClientManager clientUnderTest, ServiceClient serviceClient, RegistryManager registryManager, TestParameters testParameters) throws Exception
    {
        for (DeviceClientLonghaulTestAddOn callback : this.callbacks)
        {
            callback.setupClientAfterOpen(clientUnderTest, serviceClient, registryManager, testParameters);
        }
    }

    @Override
    public void performPeriodicStatusReport(TestParameters testParameters)
    {
        for (DeviceClientLonghaulTestAddOn callback : this.callbacks)
        {
            callback.performPeriodicStatusReport(testParameters);
        }
    }

    @Override
    public void performPeriodicTestableAction(DeviceClientManager clientUnderTest, ServiceClient serviceClient, RegistryManager registryManager, TestParameters testParameters) throws Exception
    {
        for (DeviceClientLonghaulTestAddOn callback : this.callbacks)
        {
            callback.performPeriodicTestableAction(clientUnderTest, serviceClient, registryManager, testParameters);
        }
    }

    @Override
    public boolean validateExpectations(TestParameters testParameters)
    {
        boolean result = true;
        for (DeviceClientLonghaulTestAddOn callback : this.callbacks)
        {
            result &= callback.validateExpectations(testParameters);
        }

        return result;
    }
}
