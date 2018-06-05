/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.deps.util.Base64;
import com.microsoft.azure.sdk.iot.device.auth.HsmHttpClient;
import com.microsoft.azure.sdk.iot.device.auth.HttpHsmSignRequest;
import com.microsoft.azure.sdk.iot.device.auth.HttpHsmSignResponse;
import com.microsoft.azure.sdk.iot.device.auth.HttpHsmSignatureProvider;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;

import javax.crypto.Mac;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;

import static junit.framework.TestCase.assertEquals;

public class HttpHsmSignatureProviderTest
{
    @Mocked
    Mac mockedMac;

    @Mocked
    HsmHttpClient mockedHsmHttpClient;

    @Mocked
    HttpHsmSignRequest mockedHttpHsmSignRequest;

    @Mocked
    HttpHsmSignResponse mockedHttpHsmSignResponse;

    @Mocked
    Base64 mockedBase64;

    private static final String expectedProviderUri = "someProviderUri";
    private static final String expectedApiVersion = "1.1.1";
    private static final String defaultApiVersion = Deencapsulation.getField(HttpHsmSignatureProvider.class, "DEFAULT_API_VERSION");

    // Codes_SRS_HTTPHSMSIGNATUREPROVIDER_34_001: [This constructor shall call the overloaded constructor with the default api version.]
    // Codes_SRS_HTTPHSMSIGNATUREPROVIDER_34_002: [This constructor shall create a new HsmHttpClient with the provided providerUri.]
    @Test
    public void constructorSuccess() throws NoSuchAlgorithmException
    {
        //act
        HttpHsmSignatureProvider httpHsmSignatureProvider = new HttpHsmSignatureProvider(expectedProviderUri);

        //assert
        assertEquals(defaultApiVersion, Deencapsulation.getField(httpHsmSignatureProvider, "apiVersion"));
        new Verifications()
        {
            {
                new HsmHttpClient(expectedProviderUri);
                times = 1;
            }
        };
    }

    // Codes_SRS_HTTPHSMSIGNATUREPROVIDER_34_003: [This constructor shall save the provided api version.]
    @Test
    public void constructorSuccessWithApiVersion() throws NoSuchAlgorithmException
    {
        //act
        HttpHsmSignatureProvider httpHsmSignatureProvider = new HttpHsmSignatureProvider(expectedProviderUri, expectedApiVersion);

        //assert
        assertEquals(expectedApiVersion, Deencapsulation.getField(httpHsmSignatureProvider, "apiVersion"));
        new Verifications()
        {
            {
                new HsmHttpClient(expectedProviderUri);
                times = 1;
            }
        };
    }

    // Codes_SRS_HTTPHSMSIGNATUREPROVIDER_34_004: [If the providerUri is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsIfProviderUriNullOrEmpty() throws NoSuchAlgorithmException
    {
        //act
        HttpHsmSignatureProvider httpHsmSignatureProvider = new HttpHsmSignatureProvider(null, expectedApiVersion);
    }

    // Codes_SRS_HTTPHSMSIGNATUREPROVIDER_34_005: [If the apiVersion is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsIfApiVersionNullOrEmpty() throws NoSuchAlgorithmException
    {
        //act
        HttpHsmSignatureProvider httpHsmSignatureProvider = new HttpHsmSignatureProvider(expectedProviderUri, null);
    }

    // Codes_SRS_HTTPHSMSIGNATUREPROVIDER_34_006: [This function shall create a signRequest for the hsm http client to sign, and shall return the base64 encoded result of that signing.]
    @Test
    public void signSuccess() throws NoSuchAlgorithmException, TransportException, UnsupportedEncodingException, MalformedURLException
    {
        //arrange
        final String keyName = "keyName";
        final String data = "some data";
        new NonStrictExpectations()
        {
            {
                new HsmHttpClient(expectedProviderUri);
                result = mockedHsmHttpClient;

                new HttpHsmSignRequest();
                result = mockedHttpHsmSignRequest;

                mockedHsmHttpClient.sign(expectedApiVersion, keyName, mockedHttpHsmSignRequest);
                result = mockedHttpHsmSignResponse;
            }
        };

        final HttpHsmSignatureProvider signatureProvider = new HttpHsmSignatureProvider(expectedProviderUri, expectedApiVersion);

        //act
        signatureProvider.sign(keyName, data);

        //assert
        new Verifications()
        {
            {
                mockedHttpHsmSignRequest.setData(data.getBytes("UTF-8"));
                mockedHttpHsmSignRequest.setKeyId("primary");
                mockedHttpHsmSignRequest.setAlgo((Mac) Deencapsulation.getField(signatureProvider, "defaultSignRequestAlgo"));
                mockedHsmHttpClient.sign(expectedApiVersion, keyName, mockedHttpHsmSignRequest);
                Base64.encodeBase64StringLocal(mockedHttpHsmSignResponse.getDigest());
            }
        };
    }


    // Codes_SRS_HTTPHSMSIGNATUREPROVIDER_34_007: [If the provided data is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void signThrowsForNullData() throws NoSuchAlgorithmException, TransportException, UnsupportedEncodingException, MalformedURLException
    {
        //arrange
        final String keyName = "keyName";
        final HttpHsmSignatureProvider signatureProvider = new HttpHsmSignatureProvider(expectedProviderUri, expectedApiVersion);

        //act
        signatureProvider.sign(keyName, null);
    }

}
