// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service.credentials;

public class SharedAccessKeySasTokenProvider implements SasTokenProvider {
    private final ServiceConnectionString serviceConnectionString;

    public SharedAccessKeySasTokenProvider(ServiceConnectionString connectionString) {
        this.serviceConnectionString = connectionString;
    }

    public String getSasToken() {
        System.out.println("Should verify if token is valid or should be refreshed");
        return new IotHubServiceSasToken(serviceConnectionString).toString();
    }
}
