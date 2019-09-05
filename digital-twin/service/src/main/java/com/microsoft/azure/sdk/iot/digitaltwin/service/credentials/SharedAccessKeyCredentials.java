// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service.credentials;

public class SharedAccessKeyCredentials extends IoTServiceClientCredentials {
    private final ServiceConnectionString serviceConnectionString;

    public SharedAccessKeyCredentials(ServiceConnectionString connectionString) {
        this.serviceConnectionString = connectionString;
    }

    protected String getSasToken() {
        return new IotHubServiceSasToken(serviceConnectionString).toString();
    }
}
