// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.digitaltwin.authentication;

/**
 * Interface for providing a Bearer authentication token for use in RBAC based authentication.
 */
public interface BearerTokenProvider {

    /**
     * Get a bearer token.
     * @return a bearer token.
     */
    String getBearerToken();
}
