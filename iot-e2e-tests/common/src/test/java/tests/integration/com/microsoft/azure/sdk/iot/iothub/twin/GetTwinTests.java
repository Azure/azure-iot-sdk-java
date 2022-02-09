/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.twin;

import com.azure.core.credential.AzureSasCredential;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.twin.TwinClient;
import com.microsoft.azure.sdk.iot.service.twin.TwinClientOptions;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubUnathorizedException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.ClientType;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.SasTokenTools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.setup.TwinCommon;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

import static junit.framework.TestCase.fail;

/**
 * Test class containing all non error injection tests to be run on JVM and android pertaining to getTwinAsync/getTwinAsync.
 */
@Slf4j
@IotHubTest
@RunWith(Parameterized.class)
public class GetTwinTests extends TwinCommon
{
    public GetTwinTests(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws IOException
    {
        super(protocol, authenticationType, clientType);
    }

    @Before
    public void setup()
    {
        // this class primarily tests the twin service client, so no need to parameterize on most device side options
        Assume.assumeTrue(
            testInstance.protocol == IotHubClientProtocol.MQTT
                && testInstance.authenticationType == AuthenticationType.SAS);
    }

    @Test
    @StandardTierHubOnlyTest
    public void testGetDeviceTwinWithConnectionString() throws IOException, InterruptedException, IotHubException, GeneralSecurityException, ModuleClientException, URISyntaxException
    {
        super.setUpNewDeviceAndModule();
        super.testGetDeviceTwin();
    }

    @Test
    @StandardTierHubOnlyTest
    public void testGetDeviceTwinWithAzureSasCredential() throws IOException, InterruptedException, IotHubException, GeneralSecurityException, ModuleClientException, URISyntaxException
    {
        super.setUpNewDeviceAndModule();
        testInstance.twinServiceClient = buildDeviceTwinClientWithAzureSasCredential();
        super.testGetDeviceTwin();
    }

    @Test
    @StandardTierHubOnlyTest
    public void serviceClientTokenRenewalWithAzureSasCredential() throws Exception
    {
        super.setUpNewDeviceAndModule();

        IotHubConnectionString iotHubConnectionStringObj = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString);
        IotHubServiceSasToken serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);
        String sasToken = serviceSasToken.toString();

        AzureSasCredential sasCredential = new AzureSasCredential(sasToken);

        this.testInstance.twinServiceClient =
            new TwinClient(
                iotHubConnectionStringObj.getHostName(),
                sasCredential,
                TwinClientOptions.builder().httpReadTimeoutSeconds(HTTP_READ_TIMEOUT).build());

        // add first device just to make sure that the first credential update worked
        super.testGetDeviceTwin();

        // deliberately expire the SAS token to provoke a 401 to ensure that the twin client is using the shared
        // access signature that is set here.
        sasCredential.update(SasTokenTools.makeSasTokenExpired(sasToken));

        try
        {
            super.testGetDeviceTwin();
            fail("Expected get twin call to throw unauthorized exception since an expired SAS token was used, but no exception was thrown");
        }
        catch (IotHubUnathorizedException e)
        {
            log.debug("IotHubUnauthorizedException was thrown as expected, continuing test");
        }

        // Renew the expired shared access signature
        serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);
        sasCredential.update(serviceSasToken.toString());

        // adding third device should succeed since the shared access signature has been renewed
        super.testGetDeviceTwin();
    }

    @Test
    @StandardTierHubOnlyTest
    public void testGetDeviceTwinWithProxy() throws IOException, InterruptedException, IotHubException, GeneralSecurityException, ModuleClientException, URISyntaxException
    {
        if (testInstance.protocol != IotHubClientProtocol.MQTT || testInstance.authenticationType != AuthenticationType.SAS || testInstance.clientType != ClientType.DEVICE_CLIENT)
        {
            // This test doesn't really care about the device side protocol or authentication, so just run it once
            // when the device is using MQTT with SAS auth
            return;
        }

        super.setUpNewDeviceAndModule();

        String testProxyHostname = "127.0.0.1";
        int testProxyPort = 8892;
        HttpProxyServer proxyServer = DefaultHttpProxyServer.bootstrap()
                .withPort(testProxyPort)
                .start();

        try
        {
            Proxy serviceSideProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(testProxyHostname, testProxyPort));

            ProxyOptions proxyOptions = new ProxyOptions(serviceSideProxy);
            TwinClientOptions options = TwinClientOptions.builder().proxyOptions(proxyOptions).build();

            testInstance.twinServiceClient = new TwinClient(iotHubConnectionString, options);

            super.testGetDeviceTwin();
        }
        finally
        {
            proxyServer.stop();
        }
    }
}
