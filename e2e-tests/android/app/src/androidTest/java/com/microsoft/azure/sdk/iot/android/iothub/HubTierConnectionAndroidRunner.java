package com.microsoft.azure.sdk.iot.android.iothub;

import com.microsoft.azure.sdk.iot.android.helper.TestGroup10;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.registry.RegistryIdentity;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import tests.integration.com.microsoft.azure.sdk.iot.iothub.HubTierConnectionTests;

@TestGroup10
@RunWith(Parameterized.class)
public class HubTierConnectionAndroidRunner extends HubTierConnectionTests
{
    public HubTierConnectionAndroidRunner(DeviceClient client, IotHubClientProtocol protocol, RegistryIdentity identity, AuthenticationType authenticationType, boolean useHttpProxy)
    {
        super(client, protocol, identity, authenticationType, useHttpProxy);
    }
}
