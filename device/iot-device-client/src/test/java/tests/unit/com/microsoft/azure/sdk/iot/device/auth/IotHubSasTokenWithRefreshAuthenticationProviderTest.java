/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.device.auth.IotHubSasToken;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenWithRefreshAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import mockit.Deencapsulation;
import mockit.Mocked;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;

public class IotHubSasTokenWithRefreshAuthenticationProviderTest
{
    private class IotHubImplSasTokenWithRefreshAuthenticationProvider extends IotHubSasTokenWithRefreshAuthenticationProvider
    {
        protected IotHubImplSasTokenWithRefreshAuthenticationProvider(String hostname, String gatewayHostName, String deviceId, String moduleId, String sharedAccessToken, int suggestedTimeToLiveSeconds, int timeBufferPercentage)
        {
            super(hostname, gatewayHostName, deviceId, moduleId, sharedAccessToken, suggestedTimeToLiveSeconds, timeBufferPercentage);
        }

        public void refreshSasToken() throws IOException, TransportException
        {
            this.sasToken = nextToken;
        }

        @Override
        public boolean canRefreshToken()
        {
            return true;
        }

        @Override
        public boolean shouldRefreshToken(boolean proactivelyRenew)
        {
            return this.shouldRefresh;
        }

        //test impl only methods. Used to create scenarios
        private IotHubSasToken nextToken;
        private boolean shouldRefresh;
    }

    private final String expectedHostname = "somehostname";
    private final String expectedDeviceId = "somedeviceid";
    private final String expectedModuleId = "somemoduleid";
    private final String expectedGatewayHostname = "somegatewayhostname";
    private final String expectedSharedAccessToken = "1234";
    private final int expectedTimeToLive = 10;
    private final int expectedTimeBufferPercentage = 10;

    @Mocked
    IotHubSasToken mockedIotHubSasToken;

    // Tests_SRS_MODULEAUTHENTICATIONWITHTOKENREFRESH_34_001: [If any of the provided arguments are null or empty, this
    // function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void buildAudienceRequiresHostname()
    {
        //act
        Deencapsulation.invoke(IotHubSasTokenWithRefreshAuthenticationProvider.class, "buildAudience", new Class[] {String.class, String.class, String.class}, null, "1", "2");
    }

    // Tests_SRS_MODULEAUTHENTICATIONWITHTOKENREFRESH_34_001: [If any of the provided arguments are null or empty, this
    // function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void buildAudienceRequiresDeviceId()
    {
        //act
        Deencapsulation.invoke(IotHubSasTokenWithRefreshAuthenticationProvider.class, "buildAudience", new Class[] {String.class, String.class, String.class}, "1", null, "2");
    }

    // Tests_SRS_MODULEAUTHENTICATIONWITHTOKENREFRESH_34_001: [If any of the provided arguments are null or empty, this
    // function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void buildAudienceRequiresModuleId()
    {
        //act
        Deencapsulation.invoke(IotHubSasTokenWithRefreshAuthenticationProvider.class, "buildAudience", new Class[] {String.class, String.class, String.class}, "1", "2", null);
    }

    // Tests_SRS_MODULEAUTHENTICATIONWITHTOKENREFRESH_34_002: [This function shall return the path
    // "<hostname>/devices/<device id>/modules/<module id> url encoded with utf-8.]
    @Test
    public void buildAudienceSuccess()
    {
        //arrange
        String expected = expectedHostname + "%2Fdevices%2F" + expectedDeviceId + "%2Fmodules%2F" + expectedModuleId;

        //act
        String actual = Deencapsulation.invoke(IotHubSasTokenWithRefreshAuthenticationProvider.class, "buildAudience", new Class[] {String.class, String.class, String.class}, expectedHostname, expectedDeviceId, expectedModuleId);

        //assert
        assertEquals(expected, actual);
    }

    // Tests_SRS_MODULEAUTHENTICATIONWITHTOKENREFRESH_34_003: [This function shall always return false.]
    @Test
    public void isRenewalNecessaryReturnsFalse()
    {
        //arrange
        IotHubSasTokenWithRefreshAuthenticationProvider moduleAuthenticationWithTokenRefresh = new IotHubImplSasTokenWithRefreshAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, expectedSharedAccessToken, expectedTimeToLive, expectedTimeBufferPercentage);

        //act, assert
        assertFalse(moduleAuthenticationWithTokenRefresh.isRenewalNecessary());
    }

    // Tests_SRS_MODULEAUTHENTICATIONWITHTOKENREFRESH_34_004: [This function shall invoke shouldRefreshSasToken, and if it should refresh, this function shall refresh the sas token.]
    // Tests_SRS_MODULEAUTHENTICATIONWITHTOKENREFRESH_34_005: [This function shall return the saved sas token's string representation.]
    @Test
    public void getRenewedSasTokenRefreshesIfNeeded() throws IOException, TransportException
    {
        //arrange
        final IotHubSasToken oldSasToken = Deencapsulation.newInstance(IotHubSasToken.class, new Class[] {String.class, String.class, String.class, String.class, String.class, long.class}, "", "", "", "", "", 1);
        final IotHubSasToken newSasToken = mockedIotHubSasToken;
        IotHubImplSasTokenWithRefreshAuthenticationProvider moduleAuthenticationWithTokenRefresh = new IotHubImplSasTokenWithRefreshAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, expectedSharedAccessToken, expectedTimeToLive, expectedTimeBufferPercentage);
        Deencapsulation.setField(moduleAuthenticationWithTokenRefresh, "sasToken", oldSasToken);
        moduleAuthenticationWithTokenRefresh.shouldRefresh = true;
        moduleAuthenticationWithTokenRefresh.nextToken = newSasToken;

        //act
        String actual = moduleAuthenticationWithTokenRefresh.getRenewedSasToken(true, false);

        //assert
        assertEquals(newSasToken.toString(), actual);
    }

    @Test
    public void getRenewedSasTokenForcesRefresh() throws IOException, TransportException
    {
        //arrange
        final IotHubSasToken oldSasToken = Deencapsulation.newInstance(IotHubSasToken.class, new Class[] {String.class, String.class, String.class, String.class, String.class, long.class}, "", "", "", "", "", 1);
        final IotHubSasToken newSasToken = mockedIotHubSasToken;
        IotHubImplSasTokenWithRefreshAuthenticationProvider moduleAuthenticationWithTokenRefresh = new IotHubImplSasTokenWithRefreshAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, expectedSharedAccessToken, expectedTimeToLive, expectedTimeBufferPercentage);
        Deencapsulation.setField(moduleAuthenticationWithTokenRefresh, "sasToken", oldSasToken);
        moduleAuthenticationWithTokenRefresh.shouldRefresh = false;
        moduleAuthenticationWithTokenRefresh.nextToken = newSasToken;

        //act
        String actual = moduleAuthenticationWithTokenRefresh.getRenewedSasToken(true, true);

        //assert
        assertEquals(newSasToken.toString(), actual);
    }

    // Tests_SRS_MODULEAUTHENTICATIONWITHTOKENREFRESH_34_004: [This function shall invoke shouldRefreshSasToken, and if it should refresh, this function shall refresh the sas token.]
    // Tests_SRS_MODULEAUTHENTICATIONWITHTOKENREFRESH_34_005: [This function shall return the saved sas token's string representation.]
    @Test
    public void getRenewedSasTokenDoesNotRefreshIfNotNeeded() throws IOException, TransportException
    {
        //arrange
        final IotHubSasToken oldSasToken = Deencapsulation.newInstance(IotHubSasToken.class, new Class[] {String.class, String.class, String.class, String.class, String.class, long.class}, "", "", "", "", "", 1);
        final IotHubSasToken newSasToken = mockedIotHubSasToken;
        IotHubImplSasTokenWithRefreshAuthenticationProvider moduleAuthenticationWithTokenRefresh = new IotHubImplSasTokenWithRefreshAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, expectedSharedAccessToken, expectedTimeToLive, expectedTimeBufferPercentage);
        Deencapsulation.setField(moduleAuthenticationWithTokenRefresh, "sasToken", oldSasToken);
        moduleAuthenticationWithTokenRefresh.shouldRefresh = false;
        moduleAuthenticationWithTokenRefresh.nextToken = newSasToken;

        //act
        String actual = moduleAuthenticationWithTokenRefresh.getRenewedSasToken(false, false);

        //assert
        assertEquals(oldSasToken.toString(), actual);
    }
}
