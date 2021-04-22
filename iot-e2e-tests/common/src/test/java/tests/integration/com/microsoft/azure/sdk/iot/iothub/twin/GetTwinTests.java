/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.twin;

import com.azure.core.credential.AzureSasCredential;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwin;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinClientOptions;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubUnathorizedException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.ClientType;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.SasTokenTools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.setup.DeviceTwinCommon;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

import static junit.framework.TestCase.fail;

/**
 * Test class containing all non error injection tests to be run on JVM and android pertaining to getDeviceTwin/getTwin.
 */
@Slf4j
@IotHubTest
@RunWith(Parameterized.class)
public class GetTwinTests extends DeviceTwinCommon
{
    public GetTwinTests(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws IOException
    {
        super(protocol, authenticationType, clientType);
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
    public void testGetDeviceTwinWithTokenCredential() throws IOException, InterruptedException, IotHubException, GeneralSecurityException, ModuleClientException, URISyntaxException
    {
        super.setUpNewDeviceAndModule();
        testInstance.twinServiceClient = buildDeviceTwinClientWithTokenCredential();
        super.testGetDeviceTwin();
    }

    @Test
    @StandardTierHubOnlyTest
    public void serviceClientTokenRenewalWithAzureSasCredential() throws Exception
    {
        if (testInstance.protocol != IotHubClientProtocol.AMQPS
            || testInstance.clientType != ClientType.DEVICE_CLIENT
            || testInstance.authenticationType != AuthenticationType.SAS)
        {
            // This test is for the service client, so no need to rerun it for all the different client types or device protocols
            return;
        }

        super.setUpNewDeviceAndModule();

        IotHubConnectionString iotHubConnectionStringObj = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString);
        IotHubServiceSasToken serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);
        String sasToken = serviceSasToken.toString();

        AzureSasCredential sasCredential = new AzureSasCredential(sasToken);

        this.testInstance.twinServiceClient =
            new DeviceTwin(
                iotHubConnectionStringObj.getHostName(),
                sasCredential,
                DeviceTwinClientOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());

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
            DeviceTwinClientOptions options = DeviceTwinClientOptions.builder().proxyOptions(proxyOptions).build();

            testInstance.twinServiceClient = DeviceTwin.createFromConnectionString(iotHubConnectionString, options);

            super.testGetDeviceTwin();
        }
        finally
        {
            proxyServer.stop();
        }
    }
}
