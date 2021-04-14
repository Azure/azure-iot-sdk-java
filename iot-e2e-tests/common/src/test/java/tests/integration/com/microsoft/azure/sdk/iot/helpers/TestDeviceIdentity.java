// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.InternalClient;
import com.microsoft.azure.sdk.iot.service.Device;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A collection of all the relevant identity and client objects that should be necessary to run a device side test involving a device.
 */
@AllArgsConstructor
public class TestDeviceIdentity extends TestIdentity
{
    @Getter
    DeviceClient deviceClient;

    @Getter
    Device device;

    @Override
    public String getDeviceId()
    {
        return device.getDeviceId();
    }

    @Override
    public InternalClient getClient()
    {
        return deviceClient;
    }
}