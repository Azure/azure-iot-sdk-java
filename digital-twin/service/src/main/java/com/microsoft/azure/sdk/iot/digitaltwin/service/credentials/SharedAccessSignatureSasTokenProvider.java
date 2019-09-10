// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service.credentials;

public class SharedAccessSignatureSasTokenProvider implements SasTokenProvider {
    private final String sasKey;

    public SharedAccessSignatureSasTokenProvider(String sasKey) {
        this.sasKey = sasKey;
    }

    public String getSasToken() {
        return this.sasKey;
    }
}
