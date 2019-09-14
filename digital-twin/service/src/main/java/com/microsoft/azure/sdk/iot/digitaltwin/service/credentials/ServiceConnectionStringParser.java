// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service.credentials;

import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public final class ServiceConnectionStringParser {
    private static final String VALUE_PAIR_DELIMITER = ";";
    private static final String VALUE_PAIR_SEPARATOR = "=";

    private static final String HOST_NAME_PROPERTY_NAME = "HostName";
    private static final String SHARED_ACCESS_KEY_NAME_PROPERTY_NAME = "SharedAccessKeyName";
    private static final String SHARED_ACCESS_KEY_PROPERTY_NAME = "SharedAccessKey";
    private static final String SHARED_ACCESS_SIGNATURE_PROPERTY_NAME = "SharedAccessSignature";

    private static final String ENDPOINT_PROTOCOL = "https";

    private ServiceConnectionStringParser() {
    }

    public static ServiceConnectionString parseConnectionString(@NonNull String connectionString) {
        Map<String, String> keyValueMap = new HashMap<>();

        StringTokenizer stringTokenizer1 = new StringTokenizer(connectionString, VALUE_PAIR_DELIMITER);
        while (stringTokenizer1.hasMoreTokens()) {
            String currentToken = stringTokenizer1.nextToken();

            String[] splitString = currentToken.split(VALUE_PAIR_SEPARATOR, 2);
            if (splitString.length == 2) {
                keyValueMap.put(splitString[0], splitString[1]);
            }
        }

        String hostName = keyValueMap.get(HOST_NAME_PROPERTY_NAME);

        return ServiceConnectionString.builder()
                                      .hostName(hostName)
                                      .httpsEndpoint(buildHttpsEndpoint(hostName))
                                      .sharedAccessKeyName(keyValueMap.get(SHARED_ACCESS_KEY_NAME_PROPERTY_NAME))
                                      .sharedAccessKey(keyValueMap.get(SHARED_ACCESS_KEY_PROPERTY_NAME))
                                      .sharedAccessSignature(keyValueMap.get(SHARED_ACCESS_SIGNATURE_PROPERTY_NAME))
                                      .build();
    }

    private static String buildHttpsEndpoint(String hostName) {
        return String.format("%s://%s", ENDPOINT_PROTOCOL, hostName);
    }
}
