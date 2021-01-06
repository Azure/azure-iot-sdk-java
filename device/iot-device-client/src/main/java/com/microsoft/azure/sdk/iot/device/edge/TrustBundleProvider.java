/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.edge;

import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.hsm.HsmException;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Interface for providing trust bundles through an external provider
 */
public interface TrustBundleProvider
{
    String getTrustBundleCerts(String providerUri, String apiVersion) throws URISyntaxException, TransportException, IOException, HsmException;
}
