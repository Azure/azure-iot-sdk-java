package com.microsoft.azure.sdk.iot.android.iothub;

import com.microsoft.azure.sdk.iot.android.helper.TestGroup13;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.BaseDevice;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import tests.integration.com.microsoft.azure.sdk.iot.iothub.HubTierConnectionTests;

@TestGroup13
@RunWith(Parameterized.class)
public class HubTierConnectionAndroidRunner extends HubTierConnectionTests
{
    public HubTierConnectionAndroidRunner(DeviceClient client, IotHubClientProtocol protocol, BaseDevice identity, AuthenticationType authenticationType, String publicKeyCert, String privateKey, String x509Thumbprint, boolean useHttpProxy)
    {
        super(client, protocol, identity, authenticationType, publicKeyCert, privateKey, x509Thumbprint, useHttpProxy);
    }
}
