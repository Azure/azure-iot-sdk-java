// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service.credentials;

import com.microsoft.azure.sdk.iot.digitaltwin.service.util.Tools;
import okhttp3.HttpUrl;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class ServiceConnectionStringParser {
    private static final String HOST_NAME_REGEX = "[a-zA-Z0-9_\\-\\.]+$";
    private static final String SHARED_ACCESS_KEY_NAME_REGEX = "^[a-zA-Z0-9_\\-@\\.]+$";
    private static final String SHARED_ACCESS_KEY_REGEX = "^.+$";
    private static final String SHARED_ACCESS_SIGNATURE_REGEX = "^.+$";
    private static final String ENDPOINT_PROTOCOL = "https";

    /**
     * Static constructor to create ServiceConnectionString deserialize the given string
     *
     * @param connectionString The serialized connection string
     * @return The ServiceConnectionString object
     * @throws IOException This exception is thrown if the object creation failed
     */
    public static ServiceConnectionString parseConnectionString(String connectionString) throws IOException {
        // Codes_SRS_SERVICE_SDK_JAVA_ServiceConnectionStringBUILDER_12_001: [The function shall throw IllegalArgumentException if the input string is empty or null]
        if (isBlank(connectionString)) {
            throw new IllegalArgumentException("connection string cannot be null or empty");
        }
        // Codes_SRS_SERVICE_SDK_JAVA_ServiceConnectionStringBUILDER_12_002: [The function shall create a new ServiceConnectionString object deserializing the given string]
        ServiceConnectionString ServiceConnectionString = new ServiceConnectionString();
        parse(connectionString, ServiceConnectionString);
        return ServiceConnectionString;
    }

    /**
     * Deserialize connection string
     *
     * @param connectionString        The connection string to deserialize
     * @param ServiceConnectionString The target object for deserialization
     * @throws IOException This exception is thrown if the parsing failed
     */
    protected static void parse(String connectionString, ServiceConnectionString ServiceConnectionString) throws IOException {
        Map<String, String> keyValueMap = new HashMap<String, String>();

        // Codes_SRS_SERVICE_SDK_JAVA_ServiceConnectionStringBUILDER_12_006: [The function shall throw IllegalArgumentException if the input string is empty or null]
        if (isBlank(connectionString)) {
            throw new IllegalArgumentException("connectionString cannot be null or empty");
        }
        // Codes_SRS_SERVICE_SDK_JAVA_ServiceConnectionStringBUILDER_12_007: [The function shall throw IllegalArgumentException if the input target itoHubConnectionString is null]
        if (ServiceConnectionString == null) {
            throw new IllegalArgumentException("ServiceConnectionString cannot be null");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_ServiceConnectionStringBUILDER_12_008: [The function shall throw exception if tokenizing or parsing failed]
        // Codes_SRS_SERVICE_SDK_JAVA_ServiceConnectionStringBUILDER_12_009: [The function shall tokenize and parse the given connection string and fill up the target ServiceConnectionString object with proper values]
        StringTokenizer stringTokenizer1 = new StringTokenizer(connectionString, ServiceConnectionString.VALUE_PAIR_DELIMITER);
        while (stringTokenizer1.hasMoreTokens()) {
            String currentToken = stringTokenizer1.nextToken();

            String[] splitString = currentToken.split(ServiceConnectionString.VALUE_PAIR_SEPARATOR, 2);
            if (splitString.length == 2) {
                keyValueMap.put(splitString[0], splitString[1]);
            }
        }

        ServiceConnectionString.hostName = keyValueMap.get(ServiceConnectionString.HOST_NAME_PROPERTY_NAME);
        ServiceConnectionString.sharedAccessKeyName = keyValueMap.get(ServiceConnectionString.SHARED_ACCESS_KEY_NAME_PROPERTY_NAME);
        ServiceConnectionString.sharedAccessKey = keyValueMap.get(ServiceConnectionString.SHARED_ACCESS_KEY_PROPERTY_NAME);
        ServiceConnectionString.sharedAccessSignature = keyValueMap.get(ServiceConnectionString.SHARED_ACCESS_SIGNATURE_PROPERTY_NAME);
        ServiceConnectionString.iotHubName = parseIotHubName(ServiceConnectionString);
        ServiceConnectionString.httpsEndpoint = buildHttpsEndpoint(ServiceConnectionString);

        // Codes_SRS_SERVICE_SDK_JAVA_ServiceConnectionStringBUILDER_12_012: [The function shall validate the connection string object]
        validate(ServiceConnectionString);
    }

    /**
     * Returns the service endpoint that the client connects to
     *
     * @param ServiceConnectionString The ServiceConnectionString containing the endpoint and authentication details
     * @return The service endpoint that the client connects to
     */
    protected static String buildHttpsEndpoint(ServiceConnectionString ServiceConnectionString) {
        URL url = new HttpUrl.Builder()
                .scheme(ENDPOINT_PROTOCOL)
                .host(ServiceConnectionString.hostName)
                .build().url();

        return url.toString();
    }

    /**
     * Parse the iot hub name part from the host name
     *
     * @param ServiceConnectionString The source ServiceConnectionString containing the hostName
     * @return The substring of the host name until the first "." character
     */
    protected static String parseIotHubName(ServiceConnectionString ServiceConnectionString) {
        // Codes_SRS_SERVICE_SDK_JAVA_ServiceConnectionStringBUILDER_12_013: [The function shall return the substring of the host name until the first "." character]
        Integer index = ServiceConnectionString.hostName.indexOf(ServiceConnectionString.HOST_NAME_SEPARATOR);
        if (index >= 0) {
            return ServiceConnectionString.hostName.substring(0, index);
        }
        // Codes_SRS_SERVICE_SDK_JAVA_ServiceConnectionStringBUILDER_12_014: [The function shall return empty string if "." character was not found]
        else {
            return "";
        }
    }

    /**
     * Validate ServiceConnectionString format
     *
     * @param ServiceConnectionString The object to validate
     * @throws IllegalArgumentException This exception is thrown if the input object is null
     */
    protected static void validate(ServiceConnectionString ServiceConnectionString) throws IllegalArgumentException {
        // Codes_SRS_SERVICE_SDK_JAVA_ServiceConnectionStringBUILDER_12_015: [The function shall throw IllegalArgumentException if the sharedAccessKeyName of the input itoHubConnectionString is empty]
        if (isBlank(ServiceConnectionString.sharedAccessKeyName)) {
            throw new IllegalArgumentException("SharedAccessKeyName cannot be null or empty");
        }
        // CodesSRS_SERVICE_SDK_JAVA_ServiceConnectionStringBUILDER_12_016: [The function shall throw IllegalArgumentException if either of the sharedAccessKey or the sharedAccessSignature of the input itoHubConnectionString is empty]
        if (isBlank(ServiceConnectionString.sharedAccessKey) && isBlank(ServiceConnectionString.sharedAccessSignature)) {
            throw new IllegalArgumentException("Should specify either sharedAccessKey or sharedAccessSignature");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_ServiceConnectionStringBUILDER_12_017: [The function shall call property validation functions for hostname, sharedAccessKeyName, sharedAccessKey, sharedAccessSignature]
        validateFormat(ServiceConnectionString.hostName, ServiceConnectionString.HOST_NAME_PROPERTY_NAME, HOST_NAME_REGEX);
        validateFormatIfSpecified(ServiceConnectionString.sharedAccessKeyName, ServiceConnectionString.SHARED_ACCESS_KEY_NAME_PROPERTY_NAME, SHARED_ACCESS_KEY_NAME_REGEX);
        validateFormatIfSpecified(ServiceConnectionString.sharedAccessKey, ServiceConnectionString.SHARED_ACCESS_KEY_PROPERTY_NAME, SHARED_ACCESS_KEY_REGEX);
        validateFormatIfSpecified(ServiceConnectionString.sharedAccessSignature, ServiceConnectionString.SHARED_ACCESS_SIGNATURE_PROPERTY_NAME, SHARED_ACCESS_SIGNATURE_REGEX);
    }

    /**
     * Validate string property using given regex
     *
     * @param value        The string value to validate
     * @param propertyName The property name
     * @param regex        The regex used for validation
     */
    protected static void validateFormat(String value, String propertyName, String regex) {
        // Codes_SRS_SERVICE_SDK_JAVA_ServiceConnectionStringBUILDER_12_018: [The function shall validate the property value against the given regex]
        final Pattern pattern = Pattern.compile(regex);
        // Codes_SRS_SERVICE_SDK_JAVA_ServiceConnectionStringBUILDER_12_019: [The function shall throw IllegalArgumentException if the value did not match with the pattern]
        if (! pattern.matcher(value).matches()) {
            throw new IllegalArgumentException("The connection string has an invalid value for property.");
        }
    }

    /**
     * Validate string property using given regex if value is not null or empty
     *
     * @param value        string value to validate
     * @param propertyName property name
     * @param regex        regex used for validation
     */
    protected static void validateFormatIfSpecified(String value, String propertyName, String regex) {
        // Codes_SRS_SERVICE_SDK_JAVA_ServiceConnectionStringBUILDER_12_020: [The function shall validate the property value against the given regex if the value is not null or empty]
        if (! isBlank(value)) {
            validateFormat(value, propertyName, regex);
        }
    }

    /**
     * Set host name value to target ServiceConnectionString object
     *
     * @param hostName                host name string
     * @param ServiceConnectionString target ServiceConnectionString object
     */
    protected static void setHostName(String hostName, ServiceConnectionString ServiceConnectionString) {
        // Codes_SRS_SERVICE_SDK_JAVA_ServiceConnectionStringBUILDER_12_021: [The function shall validate the given hostName]
        validateFormat(hostName, ServiceConnectionString.HOST_NAME_PROPERTY_NAME, HOST_NAME_REGEX);
        // Codes_SRS_SERVICE_SDK_JAVA_ServiceConnectionStringBUILDER_12_022: [The function shall parse and set the hostname to the given target ServiceConnectionString object]
        ServiceConnectionString.hostName = hostName;
        ServiceConnectionString.iotHubName = parseIotHubName(ServiceConnectionString);
    }
}
