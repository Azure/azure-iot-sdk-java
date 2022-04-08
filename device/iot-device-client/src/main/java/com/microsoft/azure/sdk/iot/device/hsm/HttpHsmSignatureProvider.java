/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.hsm;

import com.microsoft.azure.sdk.iot.device.auth.SignatureProvider;
import com.microsoft.azure.sdk.iot.device.transport.TransportException;
import com.microsoft.azure.sdk.iot.device.hsm.parser.SignRequest;
import com.microsoft.azure.sdk.iot.device.hsm.parser.SignResponse;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;

/**
 * Provides a means to sign data for authentication purposes
 */
@Slf4j
public class HttpHsmSignatureProvider implements SignatureProvider
{
    private static final String ENCODING_CHARSET = "UTF-8";
    private static final String MAC = "HmacSHA256";
    private static final String DEFAULT_KEY_ID = "primary";
    private final Mac defaultSignRequestAlgo = Mac.getInstance(MAC);

    private final String apiVersion;
    private final HttpsHsmClient httpClient;

    /**
     * Constructor for an HttpHsmSignatureProvider but using the non-default api version
     * @param providerUri the uri for the signing provider
     * @param apiVersion the api version to call
     * @param unixDomainSocketChannel the implementation of the {@link UnixDomainSocketChannel} interface that will be used if any
     * unix domain socket communication is required. May be null if no unix domain socket communication is required.
     * @throws URISyntaxException if the provided uri cannot be parsed
     * @throws NoSuchAlgorithmException if the default sign request algorithm cannot be used
     */
    public HttpHsmSignatureProvider(String providerUri, String apiVersion, UnixDomainSocketChannel unixDomainSocketChannel) throws URISyntaxException, NoSuchAlgorithmException
    {
        if (providerUri == null || providerUri.isEmpty())
        {
            throw new IllegalArgumentException("provider uri cannot be null or empty");
        }

        if (apiVersion == null || apiVersion.isEmpty())
        {
            throw new IllegalArgumentException("apiVersion cannot be null or empty");
        }

        log.trace("Creating HttpHsmSignatureProvider with providerUri {}", providerUri);

        this.httpClient = new HttpsHsmClient(providerUri, unixDomainSocketChannel);
        this.apiVersion = apiVersion;
    }

    /**
     * Sign the provided data using the provided key name
     * @param keyName the key used for signing
     * @param data the data to be signed
     * @param generationId the generation id
     * @return the signed data
     */
    public String sign(String keyName, String data, String generationId) throws TransportException, UnsupportedEncodingException
    {
        if (data == null || data.isEmpty())
        {
            throw new IllegalArgumentException("Data cannot be null or empty");
        }

        SignRequest signRequest = new SignRequest();
        signRequest.setAlgo(defaultSignRequestAlgo);
        signRequest.setData(data.getBytes(ENCODING_CHARSET));
        signRequest.setKeyId(DEFAULT_KEY_ID);

        SignResponse response = this.httpClient.sign(this.apiVersion, keyName, signRequest, generationId);

        return URLEncoder.encode(response.getDigest(), ENCODING_CHARSET);
    }
}
