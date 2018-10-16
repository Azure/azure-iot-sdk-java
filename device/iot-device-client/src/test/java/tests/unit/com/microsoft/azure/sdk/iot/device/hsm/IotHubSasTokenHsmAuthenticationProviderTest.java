/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device.hsm;

import com.microsoft.azure.sdk.iot.device.auth.IotHubSasToken;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.hsm.HsmException;
import com.microsoft.azure.sdk.iot.device.hsm.IotHubSasTokenHsmAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.auth.SignatureProvider;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import mockit.*;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class IotHubSasTokenHsmAuthenticationProviderTest
{
    @Mocked
    SignatureProvider mockedSignatureProvider;

    @Mocked
    IotHubSasToken mockedIotHubSasToken;

    @Mocked
    IotHubSasTokenAuthenticationProvider mockedIotHubSasTokenAuthenticationProvider;

    private static final String expectedHostname = "hostname";
    private static final String expectedGatewayHostname = "gatewayHostname";
    private static final String expectedDeviceId = "device";
    private static final String expectedModuleId = "module";
    private static final int expectedTimeToLive = 56;
    private static final int expectedBufferPercent = 25;
    private static final String expectedSignature = "someSignature";
    private static final String expectedSharedAccessToken = "someSharedAccessToken";


    // Tests_SRS_MODULEAUTHENTICATIONWITHHSM_34_001: [This function shall construct a sas token from the provided arguments and then return a IotHubSasTokenHsmAuthenticationProvider instance that uses that sas token.]
    // Tests_SRS_MODULEAUTHENTICATIONWITHHSM_34_003: [If the gatewayHostname is not null or empty, this function shall construct the sas token using the gateway hostname instead of the hostname.]
    @Test
    public void staticConstructorSuccess() throws IOException, TransportException, URISyntaxException, HsmException
    {
        //arrange
        new MockUp<System>()
        {
            @Mock long currentTimeMillis()
            {
                return 0;
            }
        };
        new Expectations()
        {
            {
                mockedSignatureProvider.sign("module", anyString, anyString);
                result = expectedSignature;

                IotHubSasToken.buildSharedAccessToken(anyString, expectedSignature, anyLong);
                result = expectedSharedAccessToken;

                Deencapsulation.newInstance(IotHubSasToken.class,
                        new Class[] {String.class, String.class, String.class, String.class, String.class, long.class},
                        expectedGatewayHostname, expectedDeviceId, null, expectedSharedAccessToken, expectedModuleId, expectedTimeToLive);
                result = mockedIotHubSasToken;
            }
        };

        //act
        IotHubSasTokenHsmAuthenticationProvider.create(mockedSignatureProvider, expectedDeviceId, expectedModuleId, expectedHostname, expectedGatewayHostname, "gen1", expectedTimeToLive, expectedBufferPercent);
    }

    // Codes_SRS_MODULEAUTHENTICATIONWITHHSM_34_006: [If the gatewayHostname is present, this function shall construct the sas token using the gateway hostname instead of the hostname.]
    @Test
    public void staticConstructorSuccessWithGatewayHostname() throws IOException, TransportException, URISyntaxException, HsmException
    {
        //arrange
        new MockUp<System>()
        {
            @Mock long currentTimeMillis()
            {
                return 0;
            }
        };
        new Expectations()
        {
            {
                mockedSignatureProvider.sign("module", anyString, anyString);
                result = expectedSignature;

                IotHubSasToken.buildSharedAccessToken(anyString, expectedSignature, anyLong);
                result = expectedSharedAccessToken;

                Deencapsulation.newInstance(IotHubSasToken.class,
                        new Class[] {String.class, String.class, String.class, String.class, String.class, long.class},
                        expectedGatewayHostname, expectedDeviceId, null, expectedSharedAccessToken, expectedModuleId, expectedTimeToLive);
                result = mockedIotHubSasToken;
            }
        };

        //act
        IotHubSasTokenHsmAuthenticationProvider.create(mockedSignatureProvider, expectedDeviceId, expectedModuleId, expectedHostname, expectedGatewayHostname, "gen1", expectedTimeToLive, expectedBufferPercent);
    }

    // Tests_SRS_MODULEAUTHENTICATIONWITHHSM_34_004: [If the gatewayHostname is null or empty, this function shall construct the sas token using the hostname instead of the gateway hostname.]
    @Test
    public void staticConstructorSuccessWithoutGatewayHostname() throws IOException, TransportException, URISyntaxException, HsmException
    {
        //arrange
        new MockUp<System>()
        {
            @Mock long currentTimeMillis()
            {
                return 0;
            }
        };
        new Expectations()
        {
            {
                mockedSignatureProvider.sign("module", anyString, anyString);
                result = expectedSignature;

                IotHubSasToken.buildSharedAccessToken(anyString, expectedSignature, anyLong);
                result = expectedSharedAccessToken;

                Deencapsulation.newInstance(IotHubSasToken.class,
                        new Class[] {String.class, String.class, String.class, String.class, String.class, long.class},
                        expectedHostname, expectedDeviceId, null, expectedSharedAccessToken, expectedModuleId, expectedTimeToLive);
                result = mockedIotHubSasToken;
                times = 1;
            }
        };

        //act
        IotHubSasTokenHsmAuthenticationProvider.create(mockedSignatureProvider, expectedDeviceId, expectedModuleId, expectedHostname, null, "gen1", expectedTimeToLive, expectedBufferPercent);
    }

    // Tests_SRS_MODULEAUTHENTICATIONWITHHSM_34_005: [This function shall create a new sas token and save it locally.]
    @Test
    public void refreshSasTokenCreatesNewSasToken() throws TransportException, IOException, URISyntaxException, HsmException
    {
        //arrange
        new MockUp<System>()
        {
            @Mock long currentTimeMillis()
            {
                return 0;
            }
        };
        new Expectations()
        {
            {
                mockedSignatureProvider.sign("module", anyString, anyString);
                result = expectedSignature;

                IotHubSasToken.buildSharedAccessToken(anyString, expectedSignature, anyLong);
                result = expectedSharedAccessToken;

                Deencapsulation.newInstance(IotHubSasToken.class,
                        new Class[] {String.class, String.class, String.class, String.class, String.class, long.class},
                        expectedHostname, expectedDeviceId, null, expectedSharedAccessToken, expectedModuleId, expectedTimeToLive);
                result = mockedIotHubSasToken;
                times = 2;
            }
        };

        IotHubSasTokenHsmAuthenticationProvider auth = IotHubSasTokenHsmAuthenticationProvider.create(mockedSignatureProvider, expectedDeviceId, expectedModuleId, expectedHostname, "", "gen1", expectedTimeToLive, expectedBufferPercent);
        Deencapsulation.setField(auth, "hostname", expectedHostname);
        Deencapsulation.setField(auth, "gatewayHostname", "");
        Deencapsulation.setField(auth, "deviceId", expectedDeviceId);
        Deencapsulation.setField(auth, "moduleId", expectedModuleId);
        Deencapsulation.setField(auth, "signatureProvider", mockedSignatureProvider);
        Deencapsulation.setField(auth, "tokenValidSecs", expectedTimeToLive);

        //act
        auth.refreshSasToken();
    }

    // Tests_SRS_MODULEAUTHENTICATIONWITHHSM_34_002: [If the provided signature provider is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void staticConstructorThrowsForNullSignatureProvider() throws IOException, TransportException
    {
        //act
        IotHubSasTokenHsmAuthenticationProvider.create(null, expectedDeviceId, expectedModuleId, expectedHostname, expectedGatewayHostname, "gen1", expectedTimeToLive, expectedBufferPercent);
    }
}
