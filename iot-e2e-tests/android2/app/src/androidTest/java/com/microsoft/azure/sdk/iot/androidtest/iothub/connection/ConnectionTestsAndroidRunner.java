package com.microsoft.azure.sdk.iot.androidtest.iothub.connection;

import com.microsoft.azure.sdk.iot.androidtest.testgroup.*;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.ClientType;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.connection.ConnectionTests;

@TestGroup11
@RunWith(Parameterized.class)
public class ConnectionTestsAndroidRunner extends ConnectionTests
{
    public ConnectionTestsAndroidRunner(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, boolean withProxy, boolean withProxyAuth) throws Exception
    {
        super(protocol, authenticationType, clientType, withProxy, withProxyAuth);
    }
}
