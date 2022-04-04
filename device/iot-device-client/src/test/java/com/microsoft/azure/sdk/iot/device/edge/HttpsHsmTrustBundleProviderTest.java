/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.edge;

import com.microsoft.azure.sdk.iot.device.hsm.UnixDomainSocketChannel;
import com.microsoft.azure.sdk.iot.device.hsm.parser.TrustBundleResponse;
import com.microsoft.azure.sdk.iot.device.transport.TransportException;
import com.microsoft.azure.sdk.iot.device.hsm.HttpsHsmClient;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static junit.framework.TestCase.assertEquals;

public class HttpsHsmTrustBundleProviderTest
{
    @Mocked
    HttpsHsmClient mockedHttpsHsmClient;

    @Mocked
    UnixDomainSocketChannel mockedUnixDomainSocketChannel;

    @Mocked
    TrustBundleResponse mockedTrustBundleResponse;

    final static String expectedUri = "someUri";
    final static String expectedAPIVersion = "1.1.1";


    // Tests_SRS_TRUSTBUNDLEPROVIDER_34_001: [This function shall create an HttpsHsmClient using the provided provider uri.]
    // Tests_SRS_TRUSTBUNDLEPROVIDER_34_002: [This function shall invoke getTrustBundle on the HttpsHsmClient and return the resulting certificates.]
    @Test
    public void getTrustBundleCertsSuccess() throws TransportException, IOException, URISyntaxException
    {
        //arrange
        final String expectedCertificatesString = "some collection of certificates";
        HttpsHsmTrustBundleProvider provider = new HttpsHsmTrustBundleProvider();
        new NonStrictExpectations()
        {
            {
                mockedHttpsHsmClient.getTrustBundle(expectedAPIVersion);
                result = mockedTrustBundleResponse;

                mockedTrustBundleResponse.getCertificates();
                result = expectedCertificatesString;
            }
        };

        //act
        String actualCertificatesString = provider.getTrustBundleCerts(expectedUri, expectedAPIVersion, mockedUnixDomainSocketChannel);

        //assert
        assertEquals(expectedCertificatesString, actualCertificatesString);
    }
}
