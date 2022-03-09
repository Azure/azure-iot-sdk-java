/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.edge;

import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.hsm.HsmException;
import com.microsoft.azure.sdk.iot.device.hsm.HttpsHsmClient;
import com.microsoft.azure.sdk.iot.device.hsm.UnixDomainSocketChannel;
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
     * @param unixDomainSocketChannel the implementation of the {@link UnixDomainSocketChannel} interface that will be used if any
     * unix domain socket communication is required. May be null if no unix domain socket communication is required. If
     * this argument is null and unix domain socket communication is required, this method will through an {@link IllegalArgumentException}.
     * @return the raw string containing all of the certificates to be trusted. May be one certificate or many certificates
     * @throws URISyntaxException if the providerUri cannot be parsed as a uri
     * @throws TransportException if the hsm cannot be reacheed
     * @throws IOException if the hsm cannot be reached
     * @throws HsmException if the hsm cannot give the trust bundle
     */
    public String getTrustBundleCerts(String providerUri, String apiVersion, UnixDomainSocketChannel unixDomainSocketChannel) throws URISyntaxException, TransportException, IOException, HsmException
    {
        HttpsHsmClient httpsHsmClient = new HttpsHsmClient(providerUri, unixDomainSocketChannel);
        TrustBundleResponse response = httpsHsmClient.getTrustBundle(apiVersion);
        return response.getCertificates();
    }
}
