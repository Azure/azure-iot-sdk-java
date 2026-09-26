// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.contract;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests that pin the provisioning service client to the single supported
 * DPS service API version {@code 2026-11-02-preview}.
 *
 * <p>This preview package supports exactly one service API version. The api-version
 * query parameter on every service call is produced solely from
 * {@link SDKUtils#getServiceApiVersion()}, so pinning this constant pins every call.</p>
 */
public class SDKUtilsApiVersionTest
{
    private static final String EXPECTED_SERVICE_API_VERSION = "2026-11-02-preview";

    /* The sole service API version must be exactly 2026-11-02-preview. */
    @Test
    public void getServiceApiVersion_returnsPinnedPreviewVersion()
    {
        // act
        String actual = SDKUtils.getServiceApiVersion();

        // assert
        assertEquals(EXPECTED_SERVICE_API_VERSION, actual);
    }
}
