// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.digitaltwin.authentication;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class ServiceConnectionString {
    private static final String HOST_NAME_REGEX = "[a-zA-Z0-9_\\-\\.]+$";
    private static final String SHARED_ACCESS_KEY_NAME_REGEX = "^[a-zA-Z0-9_\\-@\\.]+$";
    private static final String SHARED_ACCESS_KEY_REGEX = "^.+$";
    private static final String SHARED_ACCESS_SIGNATURE_REGEX = "^.+$";

    private final String hostName;
    private final String httpsEndpoint;
    private final String sharedAccessKeyName;
    private final String sharedAccessKey;
    private final String sharedAccessSignature;

    @Builder
    ServiceConnectionString(@NonNull String hostName, @NonNull String httpsEndpoint, String sharedAccessKeyName, String sharedAccessKey, String sharedAccessSignature) {
        if (!validInput(sharedAccessKeyName, sharedAccessKey, sharedAccessSignature)) {
            throw new IllegalArgumentException("Specify either both the sharedAccessKey and sharedAccessKeyName, or sharedAccessSignature");
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

    private boolean validInput(String sharedAccessKeyName, String sharedAccessKey, String sharedAccessSignature) {
        if (sharedAccessSignature == null) {
            return sharedAccessKey != null && sharedAccessKeyName != null;
        } else {
            return sharedAccessKey == null && sharedAccessKeyName == null;
        }
    }
}
