// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service.credentials;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
public class ServiceConnectionString {
    private static final String HOST_NAME_REGEX = "[a-zA-Z0-9_\\-\\.]+$";
    private static final String SHARED_ACCESS_KEY_NAME_REGEX = "^[a-zA-Z0-9_\\-@\\.]+$";
    private static final String SHARED_ACCESS_KEY_REGEX = "^.+$";
    private static final String SHARED_ACCESS_SIGNATURE_REGEX = "^.+$";

    private String hostName;
    private String iotHubName;
    private String httpsEndpoint;
    private String sharedAccessKeyName;
    private String sharedAccessKey;
    private String sharedAccessSignature;

    @Builder
    ServiceConnectionString(@NonNull String hostName, @NonNull String iotHubName, @NonNull String httpsEndpoint, @NonNull String sharedAccessKeyName, String sharedAccessKey, String sharedAccessSignature) {
        if (sharedAccessKey != null && sharedAccessSignature != null) {
            throw new IllegalArgumentException("Specify either sharedAccessKey or sharedAccessSignature");
        }

        validateFormat(hostName, ServiceConnectionStringParser.HOST_NAME_PROPERTY_NAME, HOST_NAME_REGEX);
        validateFormatIfSpecified(sharedAccessKeyName, ServiceConnectionStringParser.SHARED_ACCESS_KEY_NAME_PROPERTY_NAME, SHARED_ACCESS_KEY_NAME_REGEX);
        validateFormatIfSpecified(sharedAccessKey, ServiceConnectionStringParser.SHARED_ACCESS_KEY_PROPERTY_NAME, SHARED_ACCESS_KEY_REGEX);
        validateFormatIfSpecified(sharedAccessSignature, ServiceConnectionStringParser.SHARED_ACCESS_SIGNATURE_PROPERTY_NAME, SHARED_ACCESS_SIGNATURE_REGEX);

        this.hostName = hostName;
        this.iotHubName = iotHubName;
        this.httpsEndpoint = httpsEndpoint;
        this.sharedAccessKeyName = sharedAccessKeyName;
        this.sharedAccessKey = sharedAccessKey;
        this.sharedAccessSignature = sharedAccessSignature;
    }

    public SasTokenProvider createSasTokenProvider() {
        if (sharedAccessSignature != null) {
            return new StaticSasTokenProvider(sharedAccessSignature);
        } else {
            return SasTokenProviderWithSharedAccessKey.builder()
                                                      .hostName(hostName)
                                                      .sharedAccessKeyName(sharedAccessKeyName)
                                                      .sharedAccessKey(sharedAccessKey)
                                                      .build();
        }
    }

    /**
     * Validate string property using given regex
     *
     * @param value        The string value to validate
     * @param propertyName The property name
     * @param regex        The regex used for validation
     */
    private static void validateFormat(String value, String propertyName, String regex) {
        // Codes_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRINGBUILDER_12_018: [The function shall validate the property value against the given regex]
        final Pattern pattern = Pattern.compile(regex);
        // Codes_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRINGBUILDER_12_019: [The function shall throw IllegalArgumentException if the value did not match with the pattern]
        if (! pattern.matcher(value).matches()) {
            throw new IllegalArgumentException(String.format("The connection string has an invalid value for property: %s ", propertyName));
        }
    }

    /**
     * Validate string property using given regex if value is not null or empty
     *
     * @param value        string value to validate
     * @param propertyName property name
     * @param regex        regex used for validation
     */
    private static void validateFormatIfSpecified(String value, String propertyName, String regex) {
        // Codes_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRINGBUILDER_12_020: [The function shall validate the property value against the given regex if the value is not null or empty]
        if (! isBlank(value)) {
            validateFormat(value, propertyName, regex);
        }
    }

}
