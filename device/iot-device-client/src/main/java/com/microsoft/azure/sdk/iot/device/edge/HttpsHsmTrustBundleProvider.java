/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.edge;

import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.hsm.HsmException;
import com.microsoft.azure.sdk.iot.device.hsm.HttpsHsmClient;
import com.microsoft.azure.sdk.iot.device.hsm.parser.TrustBundleResponse;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * This class provides trust bundles to its user by communicating with an HSM to retrieve them. The trust bundle can
 * provide one to many different trust certificates
 */
public class HttpsHsmTrustBundleProvider implements TrustBundleProvider
{
    /**
     * Retrieve the list of certificates to be trusted as dictated by the HSM
     * @param providerUri the provider uri of the HSM to communicate with
     * @param apiVersion the api version to use
     * @return the raw string containing all of the certificates to be trusted. May be one certificate or many certificates
     * @throws URISyntaxException if the providerUri cannot be parsed as a uri
     * @throws TransportException if the hsm cannot be reacheed
     * @throws IOException if the hsm cannot be reached
     * @throws HsmException if the hsm cannot give the trust bundle
     */
    public String getTrustBundleCerts(String providerUri, String apiVersion) throws URISyntaxException, TransportException, IOException, HsmException
    {
        // Codes_SRS_TRUSTBUNDLEPROVIDER_34_001: [This function shall create an HttpsHsmClient using the provided provider uri.]
        HttpsHsmClient httpsHsmClient = new HttpsHsmClient(providerUri);

        // Codes_SRS_TRUSTBUNDLEPROVIDER_34_002: [This function shall invoke getTrustBundle on the HttpsHsmClient and return the resulting certificates.]
        TrustBundleResponse response = httpsHsmClient.getTrustBundle(apiVersion);
        return response.getCertificates();
    }
}
