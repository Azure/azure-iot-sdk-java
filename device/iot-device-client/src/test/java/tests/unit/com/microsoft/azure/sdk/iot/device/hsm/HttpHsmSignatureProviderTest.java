/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device.hsm;

import com.microsoft.azure.sdk.iot.deps.util.Base64;
import com.microsoft.azure.sdk.iot.device.ModuleClient;
import com.microsoft.azure.sdk.iot.device.hsm.HsmException;
import com.microsoft.azure.sdk.iot.device.hsm.HttpsHsmClient;
import com.microsoft.azure.sdk.iot.device.hsm.parser.SignRequest;
import com.microsoft.azure.sdk.iot.device.hsm.parser.SignResponse;
import com.microsoft.azure.sdk.iot.device.hsm.HttpHsmSignatureProvider;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;

import javax.crypto.Mac;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import static junit.framework.TestCase.assertEquals;

public class HttpHsmSignatureProviderTest
{
    @Mocked
    Mac mockedMac;

    @Mocked
    HttpsHsmClient mockedHttpsHsmClient;

    @Mocked
    SignRequest mockedSignRequest;

    @Mocked
    SignResponse mockedSignResponse;

    @Mocked
    Base64 mockedBase64;

    private static final String expectedProviderUri = "someProviderUri";
    private static final String expectedApiVersion = "1.1.1";
    private static final String expectedGenId = "gen1";
    private static final String defaultApiVersion = Deencapsulation.getField(ModuleClient.class, "DEFAULT_API_VERSION");

    // Codes_SRS_HTTPHSMSIGNATUREPROVIDER_34_001: [This constructor shall call the overloaded constructor with the default api version.]
    // Codes_SRS_HTTPHSMSIGNATUREPROVIDER_34_002: [This constructor shall create a new HttpsHsmClient with the provided providerUri.]
    @Test
    public void constructorSuccess() throws NoSuchAlgorithmException, URISyntaxException
    {
        //act
        HttpHsmSignatureProvider httpHsmSignatureProvider = new HttpHsmSignatureProvider(expectedProviderUri, defaultApiVersion);

        //assert
        assertEquals(defaultApiVersion, Deencapsulation.getField(httpHsmSignatureProvider, "apiVersion"));
        new Verifications()
        {
            {
                new HttpsHsmClient(expectedProviderUri);
                times = 1;
            }
        };
    }

    // Codes_SRS_HTTPHSMSIGNATUREPROVIDER_34_003: [This constructor shall save the provided api version.]
    @Test
    public void constructorSuccessWithApiVersion() throws NoSuchAlgorithmException, URISyntaxException
    {
        //act
        HttpHsmSignatureProvider httpHsmSignatureProvider = new HttpHsmSignatureProvider(expectedProviderUri, expectedApiVersion);

        //assert
        assertEquals(expectedApiVersion, Deencapsulation.getField(httpHsmSignatureProvider, "apiVersion"));
        new Verifications()
        {
            {
                new HttpsHsmClient(expectedProviderUri);
                times = 1;
            }
        };
    }

    // Codes_SRS_HTTPHSMSIGNATUREPROVIDER_34_004: [If the providerUri is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsIfProviderUriNullOrEmpty() throws NoSuchAlgorithmException, URISyntaxException
    {
        //act
        HttpHsmSignatureProvider httpHsmSignatureProvider = new HttpHsmSignatureProvider(null, expectedApiVersion);
    }

    // Codes_SRS_HTTPHSMSIGNATUREPROVIDER_34_005: [If the apiVersion is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsIfApiVersionNullOrEmpty() throws NoSuchAlgorithmException, URISyntaxException
    {
        //act
        HttpHsmSignatureProvider httpHsmSignatureProvider = new HttpHsmSignatureProvider(expectedProviderUri, null);
    }

    // Codes_SRS_HTTPHSMSIGNATUREPROVIDER_34_006: [This function shall create a signRequest for the hsm http client to sign, and shall return the base64 encoded result of that signing.]
    @Test
    public void signSuccess() throws NoSuchAlgorithmException, TransportException, IOException, URISyntaxException, HsmException
    {
        //arrange
        final String keyName = "keyName";
        final String data = "some data";
        new NonStrictExpectations()
        {
            {
                new HttpsHsmClient(expectedProviderUri);
                result = mockedHttpsHsmClient;

                new SignRequest();
                result = mockedSignRequest;

                mockedHttpsHsmClient.sign(expectedApiVersion, keyName, mockedSignRequest, expectedGenId);
                result = mockedSignResponse;
            }
        };

        final HttpHsmSignatureProvider signatureProvider = new HttpHsmSignatureProvider(expectedProviderUri, expectedApiVersion);

        //act
        signatureProvider.sign(keyName, data, expectedGenId);

        //assert
        new Verifications()
        {
            {
                mockedSignRequest.setData(data.getBytes("UTF-8"));
                mockedSignRequest.setKeyId("primary");
                mockedSignRequest.setAlgo((Mac) Deencapsulation.getField(signatureProvider, "defaultSignRequestAlgo"));
                mockedHttpsHsmClient.sign(expectedApiVersion, keyName, mockedSignRequest, expectedGenId);
                Base64.encodeBase64StringLocal(mockedSignResponse.getDigest());
            }
        };
    }


    // Codes_SRS_HTTPHSMSIGNATUREPROVIDER_34_007: [If the provided data is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void signThrowsForNullData() throws NoSuchAlgorithmException, TransportException, IOException, URISyntaxException, HsmException
    {
        //arrange
        final String keyName = "keyName";
        final HttpHsmSignatureProvider signatureProvider = new HttpHsmSignatureProvider(expectedProviderUri, expectedApiVersion);

        //act
        signatureProvider.sign(keyName, null, expectedGenId);
    }

}
