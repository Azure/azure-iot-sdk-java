// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.digitaltwin.authentication;

import com.microsoft.azure.sdk.iot.deps.transport.amqp.TokenCredentialType;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public class StaticSasTokenProvider implements SasTokenProvider {

    @NonNull
    private final String sharedAccessSignature;

    @Override
    public String getSasToken() {
        return this.sharedAccessSignature;
    }

    @Override
    public TokenCredentialType getTokenCredentialType()
    {
        return TokenCredentialType.SHARED_ACCESS_SIGNATURE;
    }
}
