// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service.credentials;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(PowerMockRunner.class)
public class SasTokenProviderWithSharedAccessKeyTest {

    private String hostName = "someHostName";
    private String sharedAccessKeyName = "someSharedAccessKeyName";
    private String sharedAccessKey = "someSharedAccessKey";
    private int timeToLive = 100;

    private SasTokenProviderWithSharedAccessKey testee;

    @Test
    public void buildSasTokenProviderWithAllArgs() {
        testee = SasTokenProviderWithSharedAccessKey.builder()
                .hostName(hostName)
                .sharedAccessKeyName(sharedAccessKeyName)
                .sharedAccessKey(sharedAccessKey)
                .timeToLiveInSecs(timeToLive)
                .build();

        assertThat(testee).as("Verify SasTokenProviderWithSharedAccessKey").isNotNull();
    }

    @Test
    public void buildSasTokenProviderWithRequiredArgs() {
        testee = SasTokenProviderWithSharedAccessKey.builder()
                .hostName(hostName)
                .sharedAccessKeyName(sharedAccessKeyName)
                .sharedAccessKey(sharedAccessKey)
                .build();

        assertThat(testee).as("Verify SasTokenProviderWithSharedAccessKey").isNotNull();
    }

    @Test(expected = NullPointerException.class)
    public void buildSasTokenProviderWithRequiredArgMissing() {
        testee = SasTokenProviderWithSharedAccessKey.builder()
                .hostName(hostName)
                .sharedAccessKeyName(sharedAccessKeyName)
                .timeToLiveInSecs(timeToLive)
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void buildSasTokenProviderWithNoArgsThrowsException() {
        testee = SasTokenProviderWithSharedAccessKey.builder().build();
    }
}
