// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import com.microsoft.azure.sdk.iot.device.InternalClient;
import com.microsoft.azure.sdk.iot.service.Device;

/**
 * An abstract representation of a device-side identity that will be used by a test. See {@link TestDeviceIdentity} and
 * {@link TestModuleIdentity} for more details.
 */
public abstract class TestIdentity
{
    // Some tests rely on the provided test identity having no pre-existing desired properties or reported properties.
    public boolean twinUpdated;

    public abstract String getDeviceId();

    public abstract Device getDevice();

    public abstract InternalClient getClient();
}
