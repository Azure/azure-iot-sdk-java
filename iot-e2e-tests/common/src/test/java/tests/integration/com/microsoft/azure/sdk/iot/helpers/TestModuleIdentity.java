// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import com.microsoft.azure.sdk.iot.device.InternalClient;
import com.microsoft.azure.sdk.iot.device.ModuleClient;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.Module;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A collection of all the relevant identity and client objects that should be necessary to run a device side test involving a module.
 */
@AllArgsConstructor
public class TestModuleIdentity extends TestIdentity
{
    @Getter
    ModuleClient moduleClient;

    @Getter
    Device device;

    @Getter
    Module module;

    @Override
    public String getDeviceId()
    {
        return device.getDeviceId();
    }

    public String getModuleId()
    {
        return module.getId();
    }

    @Override
    public InternalClient getClient()
    {
        return moduleClient;
    }
}
