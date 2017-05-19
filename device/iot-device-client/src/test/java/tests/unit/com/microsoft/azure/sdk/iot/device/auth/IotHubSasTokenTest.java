// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasToken;
import com.microsoft.azure.sdk.iot.device.auth.Signature;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/** Unit tests for IotHubSasToken. */
public class IotHubSasTokenTest
{
    @Mocked Signature mockSig;

    // Tests_SRS_IOTHUBSASTOKEN_11_001: [The SAS token shall have the format "SharedAccessSignature sig=<signature>&se=<expiryTime>&sr=<resourceURI>". The params can be in any order.]
    @Test
    public void sasTokenHasCorrectFormat() throws URISyntaxException
    {
        final long expiryTime = 100;
        final String signature = "sample-sig";
        final IotHubConnectionString iotHubConnectionString =
                Deencapsulation.newInstance(IotHubConnectionString.class,
                        new Class[] {String.class, String.class, String.class, String.class},
                        "iothub.sample-iothub-hostname.net",
                        "sample-device-ID",
                        "sample-device-key",
                        null);
        new NonStrictExpectations()
        {
            {
                mockSig.toString();
                result = signature;
            }
        };

        IotHubSasToken token = new IotHubSasToken(new DeviceClientConfig(iotHubConnectionString), expiryTime);
        String tokenStr = token.toString();

        // assert that sig, se and sr exist in the token in any order.
        assertThat(tokenStr.indexOf("SharedAccessSignature "), is(not(-1)));
        assertThat(tokenStr.indexOf("sig="), is(not(-1)));
        assertThat(tokenStr.indexOf("se="), is(not(-1)));
        assertThat(tokenStr.indexOf("sr="), is(not(-1)));
    }

    // Tests_SRS_IOTHUBSASTOKEN_11_002: [The expiry time shall be the given expiry time, where it is a UNIX timestamp and indicates the time after which the token becomes invalid.]
    @Test
    public void expiryTimeSetCorrectly() throws URISyntaxException
    {
        final long expiryTime = 100;
        final String signature = "sample-sig";
        final IotHubConnectionString iotHubConnectionString =
                Deencapsulation.newInstance(IotHubConnectionString.class,
                        new Class[] {String.class, String.class, String.class, String.class},
                        "iothub.sample-iothub-hostname.net",
                        "sample-device-ID",
                        "sample-device-key",
                        null);
        new NonStrictExpectations()
        {
            {
                mockSig.toString();
                result = signature;
            }
        };

        IotHubSasToken token = new IotHubSasToken(new DeviceClientConfig(iotHubConnectionString), expiryTime);
        String tokenStr = token.toString();
        // extract the value assigned to se.
        int expiryTimeKeyIdx = tokenStr.indexOf("se=");
        int expiryTimeStartIdx = expiryTimeKeyIdx + 3;
        int expiryTimeEndIdx =
                tokenStr.indexOf("&", expiryTimeStartIdx);
        if (expiryTimeEndIdx == -1)
        {
            expiryTimeEndIdx = tokenStr.length();
        }
        String testExpiryTimeStr =
                tokenStr.substring(expiryTimeStartIdx,
                        expiryTimeEndIdx);

        String expectedExpiryTimeStr = Long.toString(expiryTime);
        assertThat(testExpiryTimeStr, is(expectedExpiryTimeStr));
    }

    // Tests_SRS_IOTHUBSASTOKEN_11_005: [The signature shall be correctly computed and set.]
    // Tests_SRS_IOTHUBSASTOKEN_11_006: [The function shall return the string representation of the SAS token.]
    @Test
    public void signatureSetCorrectly() throws URISyntaxException
    {
        final long expiryTime = 100;
        final String signature = "sample-sig";
        final IotHubConnectionString iotHubConnectionString =
                Deencapsulation.newInstance(IotHubConnectionString.class,
                        new Class[] {String.class, String.class, String.class, String.class},
                        "iothub.sample-iothub-hostname.net",
                        "sample-device-ID",
                        "sample-device-key",
                        null);
        new NonStrictExpectations()
        {
            {
                mockSig.toString();
                result = signature;
            }
        };

        IotHubSasToken token =
                new IotHubSasToken(new DeviceClientConfig(iotHubConnectionString), expiryTime);
        String tokenStr = token.toString();
        // extract the value assigned to sig.
        int signatureKeyIdx = tokenStr.indexOf("sig=");
        int signatureStartIdx = signatureKeyIdx + 4;
        int signatureEndIdx =
                tokenStr.indexOf("&", signatureStartIdx);
        if (signatureEndIdx == -1)
        {
            signatureEndIdx = tokenStr.length();
        }
        String testSignature = tokenStr.substring(signatureStartIdx,
                signatureEndIdx);

        final String expectedSignature = signature;
        assertThat(testSignature, is(expectedSignature));
    }

    // Tests_SRS_IOTHUBSASTOKEN_11_013: [**The token generated from DeviceClientConfig shall use correct expiry time (seconds rather than milliseconds)]
    @Test
    public void constructorSetsExpiryTimeCorrectly() throws URISyntaxException
    {
        final IotHubConnectionString iotHubConnectionString =
                Deencapsulation.newInstance(IotHubConnectionString.class,
                        new Class[] {String.class, String.class, String.class, String.class},
                        "iothub.sample-iothub-hostname.net",
                        "sample-device-ID",
                        "sample-device-key",
                        null);

        long token_valid_secs = 100;
        long expiryTimeTestErrorRange = 1;

        long expiryTimeBaseInSecs = System.currentTimeMillis() / 1000L + token_valid_secs + 1L;

        IotHubSasToken token = new IotHubSasToken(new DeviceClientConfig(iotHubConnectionString),
                                                                expiryTimeBaseInSecs);

        String tokenStr = token.toString();
        // extract the value assigned to se.
        int expiryTimeKeyIdx = tokenStr.indexOf("se=");
        int expiryTimeStartIdx = expiryTimeKeyIdx + 3;
        int expiryTimeEndIdx = tokenStr.indexOf("&", expiryTimeStartIdx);
        if (expiryTimeEndIdx == -1)
        {
            expiryTimeEndIdx = tokenStr.length();
        }
        String testExpiryTimeStr = tokenStr.substring(expiryTimeStartIdx, expiryTimeEndIdx);
        long expiryTimeInSecs = Long.valueOf(testExpiryTimeStr);

        assertTrue(expiryTimeBaseInSecs <= expiryTimeInSecs && expiryTimeInSecs <= (expiryTimeBaseInSecs + expiryTimeTestErrorRange));
    }

    // Tests_SRS_IOTHUBSASTOKEN_25_007: [**If device key is not provided in config then the SASToken from config shall be used.**]**
    @Test
    public void setValidSASTokenCorrectly() throws URISyntaxException
    {
        String sastoken = "SharedAccessSignature sr=sample-iothub-hostname.net%2fdevices%2fsample-device-ID&sig=S3%2flPidfBF48B7%2fOFAxMOYH8rpOneq68nu61D%2fBP6fo%3d&se=1469813873";
        final IotHubConnectionString iotHubConnectionString =
                Deencapsulation.newInstance(IotHubConnectionString.class,
                        new Class[] {String.class, String.class, String.class, String.class},
                        "iothub.sample-iothub-hostname.net",
                        "sample-device-ID",
                        null,
                        sastoken);

        IotHubSasToken token = new IotHubSasToken(new DeviceClientConfig(iotHubConnectionString), 0);
        String tokenStr = token.toString();
        assertTrue(tokenStr ==  sastoken);
    }

    // Tests_SRS_IOTHUBSASTOKEN_25_008: [**The required format for the SAS Token shall be verified and IllegalArgumentException is thrown if unmatched.**]**
    //Tests_SRS_IOTHUBSASTOKEN_11_001: [**The SAS token shall have the format `SharedAccessSignature sig=<signature >&se=<expiryTime>&sr=<resourceURI>`. The params can be in any order.**]**
    @Test(expected = IllegalArgumentException.class)
    public void doesNotSetInvalidSASToken() throws URISyntaxException
    {
        String sastoken = "SharedAccessSignature sr =sample-iothub-hostname.net%2fdevices%2fsample-device-ID&sig =S3%2flPidfBF48B7%2fOFAxMOYH8rpOneq68nu61D%2fBP6fo%3d&se =1469813873";
        final IotHubConnectionString iotHubConnectionString =
                Deencapsulation.newInstance(IotHubConnectionString.class,
                        new Class[] {String.class, String.class, String.class, String.class},
                        "iothub.sample-iothub-hostname.net",
                        "sample-device-ID",
                        null,
                        sastoken);

        IotHubSasToken token = new IotHubSasToken(new DeviceClientConfig(iotHubConnectionString), 0);
        String tokenStr = token.toString();
        assertTrue(tokenStr !=  sastoken);
    }

    // Tests_SRS_IOTHUBSASTOKEN_25_008: [**The required format for the SAS Token shall be verified and IllegalArgumentException is thrown if unmatched.**]**
    //Tests_SRS_IOTHUBSASTOKEN_11_001: [**The SAS token shall have the format `SharedAccessSignature sig=<signature >&se=<expiryTime>&sr=<resourceURI>`. The params can be in any order.**]**
    @Test(expected = IllegalArgumentException.class)
    public void doesNotSetSASTokenWithoutSe() throws URISyntaxException
    {
        String sastoken = "SharedAccessSignature sr=sample-iothub-hostname.net%2fdevices%2fsample-device-ID&sig=S3%2flPidfBF48B7%2fOFAxMOYH8rpOneq68nu61D%2fBP6fo%3d";
        final IotHubConnectionString iotHubConnectionString =
                Deencapsulation.newInstance(IotHubConnectionString.class,
                        new Class[] {String.class, String.class, String.class, String.class},
                        "iothub.sample-iothub-hostname.net",
                        "sample-device-ID",
                        null,
                        sastoken);

        IotHubSasToken token = new IotHubSasToken(new DeviceClientConfig(iotHubConnectionString), 0);
        String tokenStr = token.toString();
        assertTrue(tokenStr !=  sastoken);
    }

    // Tests_SRS_IOTHUBSASTOKEN_25_008: [**The required format for the SAS Token shall be verified and IllegalArgumentException is thrown if unmatched.**]**
    //Tests_SRS_IOTHUBSASTOKEN_11_001: [**The SAS token shall have the format `SharedAccessSignature sig=<signature >&se=<expiryTime>&sr=<resourceURI>`. The params can be in any order.**]**
    @Test(expected = IllegalArgumentException.class)
    public void doesNotSetSASTokenWithoutSr() throws URISyntaxException
    {
        String sastoken = "SharedAccessSignature sig=S3%2flPidfBF48B7%2fOFAxMOYH8rpOneq68nu61D%2fBP6fo%3d&se=1469813873";;
        final IotHubConnectionString iotHubConnectionString =
                Deencapsulation.newInstance(IotHubConnectionString.class,
                        new Class[] {String.class, String.class, String.class, String.class},
                        "iothub.sample-iothub-hostname.net",
                        "sample-device-ID",
                        null,
                        sastoken);

        IotHubSasToken token = new IotHubSasToken(new DeviceClientConfig(iotHubConnectionString), 0);
        String tokenStr = token.toString();
        assertTrue(tokenStr !=  sastoken);
    }

    // Tests_SRS_IOTHUBSASTOKEN_25_008: [**The required format for the SAS Token shall be verified and IllegalArgumentException is thrown if unmatched.**]**
    //Tests_SRS_IOTHUBSASTOKEN_11_001: [**The SAS token shall have the format `SharedAccessSignature sig=<signature >&se=<expiryTime>&sr=<resourceURI>`. The params can be in any order.**]**
    @Test(expected = IllegalArgumentException.class)
    public void doesNotSetSASTokenWithoutSig() throws URISyntaxException
    {
        String sastoken = "SharedAccessSignature sr=sample-iothub-hostname.net%2fdevices%2fsample-device-ID&se=1469813873";
        final IotHubConnectionString iotHubConnectionString =
                Deencapsulation.newInstance(IotHubConnectionString.class,
                        new Class[] {String.class, String.class, String.class, String.class},
                        "iothub.sample-iothub-hostname.net",
                        "sample-device-ID",
                        null,
                        sastoken);

        IotHubSasToken token = new IotHubSasToken(new DeviceClientConfig(iotHubConnectionString), 0);
        String tokenStr = token.toString();
        assertTrue(tokenStr ==  sastoken);
    }


}
