// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.digitaltwin.authentication;

import com.microsoft.azure.sdk.iot.deps.transport.amqp.TokenCredentialType;

import java.io.IOException;

public interface SasTokenProvider {
    String getSasToken() throws IOException;
    TokenCredentialType getTokenCredentialType();
}
