/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.hsm;

import com.microsoft.azure.sdk.iot.deps.util.Base64;
import com.microsoft.azure.sdk.iot.device.auth.SignatureProvider;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.hsm.parser.SignRequest;
import com.microsoft.azure.sdk.iot.device.hsm.parser.SignResponse;

import javax.crypto.Mac;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;

/**
 * Provides a means to sign data for authentication purposes
 */
public class HttpHsmSignatureProvider implements SignatureProvider
{
    private static final String ENCODING_CHARSET = "UTF-8";
    private static final String MAC = "HmacSHA256";
    private static final String DEFAULT_KEY_ID = "primary";
    private Mac defaultSignRequestAlgo = Mac.getInstance(MAC);

    private String apiVersion;
    private HttpsHsmClient httpClient;

    /**
     * Constructor for an HttpHsmSignatureProvider but using the non-default api version
     * @param providerUri the uri for the signing provider
     * @param apiVersion the api version to call
     * @throws URISyntaxException if the provided uri cannot be parsed
     * @throws NoSuchAlgorithmException if the default sign request algorithm cannot be used
     */
    public HttpHsmSignatureProvider(String providerUri, String apiVersion) throws URISyntaxException, NoSuchAlgorithmException
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

        // Codes_SRS_HTTPHSMSIGNATUREPROVIDER_34_002: [This constructor shall create a new HttpsHsmClient with the provided providerUri.]
        this.httpClient = new HttpsHsmClient(providerUri);

        // Codes_SRS_HTTPHSMSIGNATUREPROVIDER_34_003: [This constructor shall save the provided api version.]
        this.apiVersion = apiVersion;
    }

    /**
     * Sign the provided data using the provided key name
     * @param keyName the key used for signing
     * @param data the data to be signed
     * @param generationId the generation id
     * @return the signed data
     * @throws IOException If the http client cannot reach the signing party
     * @throws TransportException If the http client cannot reach the signing party
     * @throws URISyntaxException If the url for the signing party cannot be parsed
     */
    public String sign(String keyName, String data, String generationId) throws IOException, TransportException, URISyntaxException, HsmException
    {
        if (data == null || data.isEmpty())
        {
            // Codes_SRS_HTTPHSMSIGNATUREPROVIDER_34_007: [If the provided data is null or empty, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("Data cannot be null or empty");
        }

        // Codes_SRS_HTTPHSMSIGNATUREPROVIDER_34_006: [This function shall create a signRequest for the hsm http client to sign, and shall return the utf-8 encoded result of that signing.]
        SignRequest signRequest = new SignRequest();
        signRequest.setAlgo(defaultSignRequestAlgo);
        signRequest.setData(data.getBytes(ENCODING_CHARSET));
        signRequest.setKeyId(DEFAULT_KEY_ID);

        SignResponse response = this.httpClient.sign(this.apiVersion, keyName, signRequest, generationId);

        return URLEncoder.encode(response.getDigest(), ENCODING_CHARSET);
    }
}
