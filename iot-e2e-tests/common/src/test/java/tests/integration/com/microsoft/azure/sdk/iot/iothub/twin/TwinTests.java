// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.iothub.twin;

import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.ClientType;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.setup.TwinCommon;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

@IotHubTest
@RunWith(Parameterized.class)
public class TwinTests extends TwinCommon
{
    public TwinTests(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws IOException, InterruptedException, IotHubException, URISyntaxException, GeneralSecurityException, ModuleClientException
    {
        super(protocol, authenticationType, clientType);
    }

    @Test
    public void testBasicTwinFlow() throws InterruptedException, IOException, IotHubException
    {
        super.testBasicTwinFlow();
    }
}
