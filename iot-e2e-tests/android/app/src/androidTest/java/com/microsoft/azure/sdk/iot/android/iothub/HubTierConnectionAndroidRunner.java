package com.microsoft.azure.sdk.iot.android.iothub;

import com.microsoft.azure.sdk.iot.android.BuildConfig;
import com.microsoft.azure.sdk.iot.android.helper.TestGroup13;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Rerun;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.HubTierConnectionTests;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.service.BaseDevice;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Collection;

@TestGroup13
@RunWith(Parameterized.class)
public class HubTierConnectionAndroidRunner extends HubTierConnectionTests
{
    @Rule
    public Rerun count = new Rerun(3);

    public HubTierConnectionAndroidRunner(DeviceClient client, IotHubClientProtocol protocol, BaseDevice identity, AuthenticationType authenticationType, String publicKeyCert, String privateKey, String x509Thumbprint, boolean useHttpProxy)
    {
        super(client, protocol, identity, authenticationType, publicKeyCert, privateKey, x509Thumbprint, useHttpProxy);
    }
}
