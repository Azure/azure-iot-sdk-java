// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.auth;

/**
 * A signature that is used in the SAS token to authenticate the client.
 */
final class Signature
{
    private final String sig;

    /**
     * Constructs a {@code Signature} instance from the given resource URI,
     * expiry time and device key.
     * @param resourceUri the resource URI.
     * @param expiryTime the time, as a UNIX timestamp, after which the token
     * will become invalid.
     * @param deviceKey the device key.
     */
    public Signature(String resourceUri, long expiryTime, String deviceKey)
    {
        byte[] rawSig = SignatureHelper.buildRawSignature(resourceUri, expiryTime);
        byte[] decodedDeviceKey = SignatureHelper.decodeDeviceKeyBase64(deviceKey);
        byte[] encryptedSig = SignatureHelper.encryptSignatureHmacSha256(rawSig, decodedDeviceKey);
        byte[] encryptedSigBase64 = SignatureHelper.encodeSignatureBase64(encryptedSig);
        String utf8Sig = SignatureHelper.encodeSignatureUtf8(encryptedSigBase64);
        this.sig = SignatureHelper.encodeSignatureWebSafe(utf8Sig);
    }

    /**
     * Returns the string representation of the signature.
     *
     * @return the string representation of the signature.
     */
    @Override
    public String toString()
    {
        return this.sig;
    }
}
