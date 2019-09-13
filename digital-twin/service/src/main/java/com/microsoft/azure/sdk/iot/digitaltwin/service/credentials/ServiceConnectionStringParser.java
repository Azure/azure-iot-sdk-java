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
    private static final String HOST_NAME_SEPARATOR = ".";

    static final String HOST_NAME_PROPERTY_NAME = "HostName";
    static final String SHARED_ACCESS_KEY_NAME_PROPERTY_NAME = "SharedAccessKeyName";
    static final String SHARED_ACCESS_KEY_PROPERTY_NAME = "SharedAccessKey";
    static final String SHARED_ACCESS_SIGNATURE_PROPERTY_NAME = "SharedAccessSignature";

    private static final String ENDPOINT_PROTOCOL = "https";

    private ServiceConnectionStringParser() {
    }

    /**
     * Static constructor to create ServiceConnectionString deserialize the given string
     *
     * @param connectionString The serialized connection string
     * @return The ServiceConnectionString object
     */
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
                                      .iotHubName(parseIotHubName(hostName))
                                      .httpsEndpoint(buildHttpsEndpoint(hostName))
                                      .sharedAccessKeyName(keyValueMap.get(SHARED_ACCESS_KEY_NAME_PROPERTY_NAME))
                                      .sharedAccessKey(keyValueMap.get(SHARED_ACCESS_KEY_PROPERTY_NAME))
                                      .sharedAccessSignature(keyValueMap.get(SHARED_ACCESS_SIGNATURE_PROPERTY_NAME))
                                      .build();
    }

    /**
     * Returns the service endpoint that the client connects to
     *
     * @param hostName The IotHub hostname
     * @return The service endpoint that the client connects to
     */
    private static String buildHttpsEndpoint(String hostName) {
        return String.format("%s://%s", ENDPOINT_PROTOCOL, hostName);
    }

    /**
     * Parse the iot hub name part from the host name
     *
     * @param hostName The IotHub hostname
     * @return The substring of the host name until the first "." character
     */
    private static String parseIotHubName(String hostName) {
        // Codes_SRS_SERVICE_SDK_JAVA_ServiceConnectionStringBUILDER_12_013: [The function shall return the substring of the host name until the first "." character]
        Integer index = hostName.indexOf(HOST_NAME_SEPARATOR);
        if (index >= 0) {
            return hostName.substring(0, index);
        }
        // Codes_SRS_SERVICE_SDK_JAVA_ServiceConnectionStringBUILDER_12_014: [The function shall return empty string if "." character was not found]
        else {
            return "";
        }
    }
}
