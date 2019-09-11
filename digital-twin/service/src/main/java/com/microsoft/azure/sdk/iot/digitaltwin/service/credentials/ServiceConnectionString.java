// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service.credentials;

import lombok.Getter;

@Getter
public class ServiceConnectionString {
    protected static final String VALUE_PAIR_DELIMITER = ";";
    protected static final String VALUE_PAIR_SEPARATOR = "=";
    protected static final String HOST_NAME_SEPARATOR = ".";

    protected static final String HOST_NAME_PROPERTY_NAME = "HostName";
    protected static final String SHARED_ACCESS_KEY_NAME_PROPERTY_NAME = "SharedAccessKeyName";
    protected static final String SHARED_ACCESS_KEY_PROPERTY_NAME = "SharedAccessKey";
    protected static final String SHARED_ACCESS_SIGNATURE_PROPERTY_NAME = "SharedAccessSignature";

    // Included in the connection string
    protected String hostName;
    protected String iotHubName;
    protected String httpsEndpoint;
    protected String sharedAccessKeyName;
    protected String sharedAccessKey;
    protected String sharedAccessSignature;

    public SasTokenProvider createSasTokenProvider() {
        if (sharedAccessSignature != null) {
            return new StaticSasTokenProvider(sharedAccessSignature);
        } else {
            return new SasTokenProviderWithSharedAccessKey(hostName, sharedAccessKeyName, sharedAccessKey);
        }
    }

}
