/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.deps.util.Base64;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;

import javax.crypto.Mac;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;

public class HttpHsmSignatureProvider implements SignatureProvider
{
    private static final String ENCODING_CHARSET = "UTF-8";
    private static final String MAC = "HmacSHA256";
    private static final String DEFAULT_API_VERSION = "2018-06-28";
    private static final String DEFAULT_KEY_ID = "primary";
    private Mac defaultSignRequestAlgo = Mac.getInstance(MAC);

    private String apiVersion;
    private HsmHttpClient httpClient;

    public HttpHsmSignatureProvider(String providerUri) throws NoSuchAlgorithmException
    {
        // Codes_SRS_HTTPHSMSIGNATUREPROVIDER_34_001: [This constructor shall call the overloaded constructor with the default api version.]
        this(providerUri, DEFAULT_API_VERSION);
    }

    public HttpHsmSignatureProvider(String providerUri, String apiVersion) throws NoSuchAlgorithmException
    {
        if (providerUri == null || providerUri.isEmpty())
        {
            // Codes_SRS_HTTPHSMSIGNATUREPROVIDER_34_004: [If the providerUri is null or empty, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("provider uri cannot be null or empty");
        }

        if (apiVersion == null || apiVersion.isEmpty())
        {
            // Codes_SRS_HTTPHSMSIGNATUREPROVIDER_34_005: [If the apiVersion is null or empty, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("apiVersion cannot be null or empty");
        }

        // Codes_SRS_HTTPHSMSIGNATUREPROVIDER_34_002: [This constructor shall create a new HsmHttpClient with the provided providerUri.]
        this.httpClient = new HsmHttpClient(providerUri);

        // Codes_SRS_HTTPHSMSIGNATUREPROVIDER_34_003: [This constructor shall save the provided api version.]
        this.apiVersion = apiVersion;
    }

    public String sign(String keyName, String data) throws UnsupportedEncodingException, MalformedURLException, TransportException
    {
        if (data == null || data.isEmpty())
        {
            // Codes_SRS_HTTPHSMSIGNATUREPROVIDER_34_007: [If the provided data is null or empty, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("Data cannot be null or empty");
        }

        // Codes_SRS_HTTPHSMSIGNATUREPROVIDER_34_006: [This function shall create a signRequest for the hsm http client to sign, and shall return the base64 encoded result of that signing.]
        HttpHsmSignRequest signRequest = new HttpHsmSignRequest();
        signRequest.setAlgo(defaultSignRequestAlgo);
        signRequest.setData(data.getBytes(ENCODING_CHARSET));
        signRequest.setKeyId(DEFAULT_KEY_ID);

        HttpHsmSignResponse response = this.httpClient.sign(this.apiVersion, keyName, signRequest);

        return Base64.encodeBase64StringLocal(response.getDigest());
    }
}
