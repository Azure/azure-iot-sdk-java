// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service.credentials;

import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public class StaticSasTokenProvider implements SasTokenProvider {

    @NonNull
    private final String sharedAccessSignature;

    @Override
    public String getSasToken() {

        System.out.println("Should return token as is");
        return this.sharedAccessSignature;
    }
}
