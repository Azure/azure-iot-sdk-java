/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.device.transport.TransportException;

import java.io.UnsupportedEncodingException;

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
     * @throws TransportException If the http client cannot reach the signing party
     */
    String sign(String keyName, String data, String generationId) throws TransportException, UnsupportedEncodingException;
}
