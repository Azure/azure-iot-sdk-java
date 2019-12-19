// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service.credentials;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceConnectionStringTest {
    private static String hostName = "someHostName";
    private static String httpsEndpoint = "httos://somewhere.com";
    private static String sharedAccessKey = "someSharedAccessKey";
    private static String sharedAccessKeyName = "someSharedAccessKeyName";
    private static String sharedAccessSignature = "someSharedAccessSignature";

    private ServiceConnectionString testee;

    @Test (expected = IllegalArgumentException.class)
    public void buildServiceConnectionStringWithoutSharedAccessKeyAndSharedAccessSignature() {
        testee = ServiceConnectionString.builder()
                .hostName(hostName)
                .httpsEndpoint(httpsEndpoint)
                .sharedAccessKeyName(sharedAccessKeyName)
                .build();
    }

    @Test (expected = IllegalArgumentException.class)
    public void buildServiceConnectionStringWithBothSharedAccessKeyAndSharedAccessSignature() {
        testee = ServiceConnectionString.builder()
                .hostName(hostName)
                .httpsEndpoint(httpsEndpoint)
                .sharedAccessKey(sharedAccessKey)
                .sharedAccessSignature(sharedAccessSignature)
                .build();
    }

    @Test
    public void buildServiceConnectionStringWithSharedAccessKey() {
        testee = ServiceConnectionString.builder()
                .hostName(hostName)
                .httpsEndpoint(httpsEndpoint)
                .sharedAccessKeyName(sharedAccessKeyName)
                .sharedAccessKey(sharedAccessKey)
                .build();

        assertThat(testee).isNotNull();
    }

    @Test
    public void buildServiceConnectionStringWithSharedAccessSignature() {
        testee = ServiceConnectionString.builder()
                .hostName(hostName)
                .httpsEndpoint(httpsEndpoint)
                .sharedAccessSignature(sharedAccessSignature)
                .build();

        assertThat(testee).isNotNull();
    }
}
