// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service.credentials;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class ServiceConnectionString {
    private static final String HOST_NAME_REGEX = "[a-zA-Z0-9_\\-\\.]+$";
    private static final String SHARED_ACCESS_KEY_NAME_REGEX = "^[a-zA-Z0-9_\\-@\\.]+$";
    private static final String SHARED_ACCESS_KEY_REGEX = "^.+$";
    private static final String SHARED_ACCESS_SIGNATURE_REGEX = "^.+$";

    private String hostName;
    private String httpsEndpoint;
    private String sharedAccessKeyName;
    private String sharedAccessKey;
    private String sharedAccessSignature;

    @Builder
    ServiceConnectionString(@NonNull String hostName, @NonNull String httpsEndpoint, @NonNull String sharedAccessKeyName, String sharedAccessKey, String sharedAccessSignature) {
        if (sharedAccessKey != null && sharedAccessSignature != null) {
            throw new IllegalArgumentException("Specify either sharedAccessKey or sharedAccessSignature");
        }

        this.hostName = hostName;
        this.httpsEndpoint = httpsEndpoint;
        this.sharedAccessKeyName = sharedAccessKeyName;
        this.sharedAccessKey = sharedAccessKey;
        this.sharedAccessSignature = sharedAccessSignature;
    }

    public SasTokenProvider createSasTokenProvider() {
        if (sharedAccessSignature != null) {
            return new StaticSasTokenProvider(sharedAccessSignature);
        } else {
            return SasTokenProviderWithSharedAccessKey.builder()
                                                      .hostName(hostName)
                                                      .sharedAccessKeyName(sharedAccessKeyName)
                                                      .sharedAccessKey(sharedAccessKey)
                                                      .build();
        }
    }
}
