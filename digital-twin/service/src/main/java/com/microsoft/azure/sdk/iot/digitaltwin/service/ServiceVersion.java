// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service;

/**
 * The configurable service version for a digital twin service client. By default, the SDK will choose to use the
 * latest service version
 */
public enum ServiceVersion {
    V2019_07_01_preview("2019-07-01-preview");

    private String apiVersion;

    ServiceVersion(final String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getApiVersion() {
        return apiVersion;
    }
}
