/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.hsm.HsmException;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Interface for a valid signature provider
 */
public interface SignatureProvider
{
    /**
     * Sign the provided data using the provided key name
     * @param keyName the key used for signing
     * @param data the data to be signed
     * @param generationId the generation id
     * @return the signed data
     * @throws IOException If the http client cannot reach the signing party
     * @throws TransportException If the http client cannot reach the signing party
     * @throws URISyntaxException If the url for the signing party cannot be parsed
     * @throws HsmException if the hsm raises an exception
     */
    String sign(String keyName, String data, String generationId) throws IOException, TransportException, URISyntaxException, HsmException;
}
