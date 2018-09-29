/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.provisioning.service.auth;

import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import com.microsoft.azure.sdk.iot.deps.util.Tools;

/**
 * Provide static constructors to create ProvisioningConnectionString object
 */
public class ProvisioningConnectionStringBuilder
{
    private static final String HOST_NAME_REGEX = "[a-zA-Z0-9_\\-\\.]+$";
    private static final String SHARED_ACCESS_KEY_NAME_REGEX = "^[a-zA-Z0-9_\\-@\\.]+$";
    private static final String SHARED_ACCESS_KEY_REGEX = "^.+$";
    private static final String SHARED_ACCESS_SIGNATURE_REGEX = "^.+$";

    /**
     * Static constructor to create ProvisioningConnectionString deserialize the given string
     *
     * @param connectionString The serialized connection string
     * @return The ProvisioningConnectionString object
     * @throws IllegalArgumentException This exception is thrown if the object creation failed
     */
    public static ProvisioningConnectionString createConnectionString(String connectionString)
    {
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_001: [The function shall throw IllegalArgumentException if the input string is empty or null.] */
        if (Tools.isNullOrEmpty(connectionString))
        {
            throw new IllegalArgumentException("connection string cannot be null or empty");
        }
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_002: [The function shall create a new ProvisioningConnectionString object deserializing the given string.] */
        ProvisioningConnectionString provisioningConnectionString = new ProvisioningConnectionString();
        ProvisioningConnectionStringBuilder.parse(connectionString, provisioningConnectionString);
        return provisioningConnectionString;
    }

    /**
     * Static constructor to create ProvisioningConnectionString from host name and authentication method
     *
     * @param hostName The hostName string
     * @param authenticationMethod The AuthenticationMethod object
     * @return The ProvisioningConnectionString object
     * @throws IllegalArgumentException This exception is thrown if the object creation failed
     */
    public static ProvisioningConnectionString createConnectionString(String hostName, AuthenticationMethod authenticationMethod)
    {
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_003: [The function shall throw IllegalArgumentException if the input string is empty or null.] */
        if (Tools.isNullOrEmpty(hostName))
        {
            throw new IllegalArgumentException("hostName cannot be null or empty");
        }
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_004: [The function shall throw IllegalArgumentException if the input authenticationMethod is null.] */
        if (authenticationMethod == null)
        {
            throw new IllegalArgumentException("authenticationMethod cannot be null");
        }
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_005: [The function shall create a new ProvisioningConnectionString object using the given hostname and authenticationMethod.] */
        ProvisioningConnectionString provisioningConnectionString = new ProvisioningConnectionString();
        ProvisioningConnectionStringBuilder.setHostName(hostName, provisioningConnectionString);
        ProvisioningConnectionStringBuilder.setAuthenticationMethod(authenticationMethod, provisioningConnectionString);
        ProvisioningConnectionStringBuilder.validate(provisioningConnectionString);
        return provisioningConnectionString;
    }

    /**
     * Deserialize connection string
     *
     * @param connectionString The connection string to deserialize
     * @param provisioningConnectionString The target object for deserialization
     * @throws IllegalArgumentException This exception is thrown if the parsing failed
     */
    private static void parse(String connectionString, ProvisioningConnectionString provisioningConnectionString)
    {
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_006: [The function shall throw IllegalArgumentException if the input string is empty or null.] */
        if (Tools.isNullOrEmpty(connectionString))
        {
            throw new IllegalArgumentException("connectionString cannot be null or empty");
        }
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_007: [The function shall throw IllegalArgumentException if the input target itoHubConnectionString is null.] */
        if (provisioningConnectionString == null)
        {
            throw new IllegalArgumentException("provisioningConnectionString cannot be null");
        }

        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_008: [The function shall throw exception if tokenizing or parsing failed.] */
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_009: [The function shall tokenize and parse the given connection string and fill up the target ProvisioningConnectionString object with proper values.] */
        HashMap<String, String> keyValueMap = new HashMap<>();
        StringTokenizer stringTokenizer1 = new StringTokenizer(connectionString, ProvisioningConnectionString.VALUE_PAIR_DELIMITER);
        while (stringTokenizer1.hasMoreTokens())
        {
            String currentToken = stringTokenizer1.nextToken();

            String[] splitString = currentToken.split(ProvisioningConnectionString.VALUE_PAIR_SEPARATOR, 2);
            if (splitString.length == 2)
                keyValueMap.put(splitString[0], splitString[1]);
        }

        provisioningConnectionString.hostName = Tools.getValueStringByKey(keyValueMap, ProvisioningConnectionString.HOST_NAME_PROPERTY_NAME);
        provisioningConnectionString.sharedAccessKeyName = Tools.getValueStringByKey(keyValueMap, ProvisioningConnectionString.SHARED_ACCESS_KEY_NAME_PROPERTY_NAME);
        provisioningConnectionString.sharedAccessKey = Tools.getValueStringByKey(keyValueMap, ProvisioningConnectionString.SHARED_ACCESS_KEY_PROPERTY_NAME);
        provisioningConnectionString.sharedAccessSignature = Tools.getValueStringByKey(keyValueMap, ProvisioningConnectionString.SHARED_ACCESS_SIGNATURE_PROPERTY_NAME);
        provisioningConnectionString.deviceProvisioningServiceName = ProvisioningConnectionStringBuilder.parseDeviceProvisioningServiceName(provisioningConnectionString);

        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_010: [The function shall create a new ServiceAuthenticationWithSharedAccessPolicyToken and set the authenticationMethod if sharedAccessKey is not defined.] */
        if (Tools.isNullOrWhiteSpace(provisioningConnectionString.sharedAccessKey))
        {
            provisioningConnectionString.authenticationMethod = new ServiceAuthenticationWithSharedAccessPolicyToken(
                    provisioningConnectionString.sharedAccessKeyName,
                    provisioningConnectionString.sharedAccessSignature);
        }
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_011: [The function shall create a new ServiceAuthenticationWithSharedAccessPolicyKey and set the authenticationMethod if the sharedAccessSignature is not defined.] */
        else if (Tools.isNullOrWhiteSpace(provisioningConnectionString.sharedAccessSignature))
        {
            provisioningConnectionString.authenticationMethod = new ServiceAuthenticationWithSharedAccessPolicyKey(
                    provisioningConnectionString.sharedAccessKeyName,
                    provisioningConnectionString.sharedAccessKey);
        }

        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_012: [The function shall validate the connection string object.] */
        ProvisioningConnectionStringBuilder.validate(provisioningConnectionString);
    }

    /**
     * Parse the Device Provisioning Service name part from the host name
     *
     * @param provisioningConnectionString The source provisioningConnectionString containing the hostName
     * @return The substring of the host name until the first "." character
     */
    private static String parseDeviceProvisioningServiceName(ProvisioningConnectionString provisioningConnectionString)
    {
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_013: [The function shall return the substring of the host name until the first `.` character.] */
        Integer index = provisioningConnectionString.hostName.indexOf(ProvisioningConnectionString.HOST_NAME_SEPARATOR);
        if (index >= 0)
        {
            return provisioningConnectionString.hostName.substring(0, index);
        }
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_014: [The function shall return empty string if `.` character was not found.] */
        else
        {
            return "";
        }
    }

    /**
     * Validate ProvisioningConnectionString format
     *
     * @param provisioningConnectionString The object to validate
     * @throws IllegalArgumentException if the input ProvisioningConnectionString is null
     */
    private static void validate(ProvisioningConnectionString provisioningConnectionString)
    {
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_015: [The function shall throw IllegalArgumentException if the sharedAccessKeyName of the input itoHubConnectionString is empty.] */
        if (Tools.isNullOrWhiteSpace(provisioningConnectionString.sharedAccessKeyName))
        {
            throw new IllegalArgumentException("SharedAccessKeyName cannot be null or empty");
        }
        // CodesSRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_016: [The function shall throw IllegalArgumentException if either of the sharedAccessKey or the sharedAccessSignature of the input itoHubConnectionString is empty.] */
        if (Tools.isNullOrWhiteSpace(provisioningConnectionString.sharedAccessKey) && Tools.isNullOrWhiteSpace(provisioningConnectionString.sharedAccessSignature))
        {
            throw new IllegalArgumentException("Should specify either sharedAccessKey or sharedAccessSignature");
        }

        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_017: [The function shall call property validation functions for hostname, sharedAccessKeyName, sharedAccessKey, sharedAccessSignature.] */
        ProvisioningConnectionStringBuilder.validateFormat(provisioningConnectionString.hostName, ProvisioningConnectionString.HOST_NAME_PROPERTY_NAME, HOST_NAME_REGEX);
        ProvisioningConnectionStringBuilder.validateFormatIfSpecified(provisioningConnectionString.sharedAccessKeyName, ProvisioningConnectionString.SHARED_ACCESS_KEY_NAME_PROPERTY_NAME, SHARED_ACCESS_KEY_NAME_REGEX);
        ProvisioningConnectionStringBuilder.validateFormatIfSpecified(provisioningConnectionString.sharedAccessKey, ProvisioningConnectionString.SHARED_ACCESS_KEY_PROPERTY_NAME, SHARED_ACCESS_KEY_REGEX);
        ProvisioningConnectionStringBuilder.validateFormatIfSpecified(provisioningConnectionString.sharedAccessSignature, ProvisioningConnectionString.SHARED_ACCESS_SIGNATURE_PROPERTY_NAME, SHARED_ACCESS_SIGNATURE_REGEX);
    }

    /**
     * Validate string property using given regex
     *
     * @param value The string value to validate
     * @param propertyName The property name
     * @param regex The regex used for validation
     * @throws IllegalArgumentException if the the connection string has an invalid value for property.
     */
    private static void validateFormat(String value, String propertyName, String regex)
    {
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_018: [The function shall validate the property value against the given regex.] */
        final Pattern pattern = Pattern.compile(regex);
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_019: [The function shall throw IllegalArgumentException if the value did not match with the pattern.] */
        if (!pattern.matcher(value).matches())
        {
            throw new IllegalArgumentException("The connection string has an invalid value for property " + propertyName + ".");
        }
    }

    /**
     * Validate string property using given regex if value is not null or empty
     *
     * @param value string value to validate
     * @param propertyName property name
     * @param regex regex used for validation
     * @throws IllegalArgumentException if the the connection string has an invalid value for property.
     */
    private static void validateFormatIfSpecified(String value, String propertyName, String regex)
    {
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_020: [The function shall validate the property value against the given regex if the value is not null or empty.] */
        if (!Tools.isNullOrEmpty(value))
        {
            ProvisioningConnectionStringBuilder.validateFormat(value, propertyName, regex);
        }
    }

    /**
     * Set host name value to target ProvisioningConnectionString object
     *
     * @param hostName host name string
     * @param provisioningConnectionString target ProvisioningConnectionString object
     * @throws IllegalArgumentException if the connectionString has an invalid hostName.
     */
    private static void setHostName(String hostName, ProvisioningConnectionString provisioningConnectionString)
    {
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_021: [The function shall validate the given hostName.] */
        ProvisioningConnectionStringBuilder.validateFormat(hostName, ProvisioningConnectionString.HOST_NAME_PROPERTY_NAME, HOST_NAME_REGEX);
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_022: [The function shall parse and set the hostname to the given target provisioningConnectionString object.] */
        provisioningConnectionString.hostName = hostName;
        provisioningConnectionString.deviceProvisioningServiceName = ProvisioningConnectionStringBuilder.parseDeviceProvisioningServiceName(provisioningConnectionString);
    }

    /**
     * Set authentication method to target ProvisioningConnectionString object
     *
     * @param authenticationMethod value to set
     * @param provisioningConnectionString target ProvisioningConnectionString object
     */
    private static void setAuthenticationMethod(AuthenticationMethod authenticationMethod, ProvisioningConnectionString provisioningConnectionString)
    {
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_023: [The function shall populate and set the authenticationMethod on the given target provisioningConnectionString object.] */
        authenticationMethod.populateWithAuthenticationProperties(provisioningConnectionString);
        provisioningConnectionString.authenticationMethod = authenticationMethod;
    }
}
