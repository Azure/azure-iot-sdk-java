// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Deterministic, JMockit-free guard test asserting that the bulk-operation
 * {@code updateIfMatchETag} mode serializes to its exact wire value. This value is part
 * of the DPS service contract and must not drift under the {@code 2026-11-02-preview} pin.
 */
public class PreviewBulkOperationModeTest
{
    @Test
    public void updateIfMatchETag_serializesToExactWireValue()
    {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        String serialized = gson.toJson(BulkOperationMode.UPDATE_IF_MATCH_ETAG);
        assertEquals("\"updateIfMatchETag\"", serialized);
    }
}
