/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * Provide static constructors to create IotHubConnectionString object
 */
public class IotHubConnectionStringBuilder
{
    private static final String HOST_NAME_REGEX = "[a-zA-Z0-9_\\-\\.]+$";
    private static final String SHARED_ACCESS_KEY_NAME_REGEX = "^[a-zA-Z0-9_\\-@\\.]+$";
    private static final String SHARED_ACCESS_KEY_REGEX = "^.+$";
    private static final String SHARED_ACCESS_SIGNATURE_REGEX = "^.+$";

    /**
     * Static constructor to create IotHubConnectionString from the given string
     *
     * @param connectionString The connection string
     * @return The IotHubConnectionString object
     */
    public static IotHubConnectionString createIotHubConnectionString(String connectionString)
    {
        if (connectionString == null || connectionString.isEmpty())
        {
            throw new IllegalArgumentException("connection string cannot be null or empty");
        }

        IotHubConnectionString iotHubConnectionString = new IotHubConnectionString();
        parse(connectionString, iotHubConnectionString);
        return iotHubConnectionString;
    }

    /**
     * Static constructor to create IotHubConnectionString from host name and authentication method
     *
     * @param hostName The hostName string
     * @param authenticationMethod The AuthenticationMethod object
     * @return The IotHubConnectionString object
     */
    public static IotHubConnectionString createIotHubConnectionString(String hostName, AuthenticationMethod authenticationMethod)
    {
        if (hostName == null || hostName.isEmpty())
        {
            throw new IllegalArgumentException("hostName cannot be null or empty");
        }

        if (authenticationMethod == null)
        {
            throw new IllegalArgumentException("authenticationMethod cannot be null");
        }

        IotHubConnectionString iotHubConnectionString = new IotHubConnectionString();
        setHostName(hostName, iotHubConnectionString);
        setAuthenticationMethod(authenticationMethod, iotHubConnectionString);
        validate(iotHubConnectionString);
        return iotHubConnectionString;
    }

    /**
     * Deserialize connection string
     *
     * @param connectionString The connection string to deserialize
     * @param iotHubConnectionString The target object for deserialization
     */
    private static void parse(String connectionString, IotHubConnectionString iotHubConnectionString)
    {
        Map<String, String> keyValueMap = new HashMap<>();

        if (connectionString == null || connectionString.isEmpty())
        {
            throw new IllegalArgumentException("connectionString cannot be null or empty");
        }
        if (iotHubConnectionString == null)
        {
            throw new IllegalArgumentException("iotHubConnectionString cannot be null");
        }

        StringTokenizer stringTokenizer1 = new StringTokenizer(connectionString, IotHubConnectionString.VALUE_PAIR_DELIMITER);
        while (stringTokenizer1.hasMoreTokens())
        {
            String currentToken = stringTokenizer1.nextToken();

            String[] splitString = currentToken.split(IotHubConnectionString.VALUE_PAIR_SEPARATOR, 2);
            if (splitString.length == 2)
            {
                keyValueMap.put(splitString[0], splitString[1]);
            }
        }

        iotHubConnectionString.hostName = getValueStringByKey(keyValueMap, IotHubConnectionString.HOST_NAME_PROPERTY_NAME);
        iotHubConnectionString.sharedAccessKeyName = getValueStringByKey(keyValueMap, IotHubConnectionString.SHARED_ACCESS_KEY_NAME_PROPERTY_NAME);
        iotHubConnectionString.sharedAccessKey = getValueStringByKey(keyValueMap, IotHubConnectionString.SHARED_ACCESS_KEY_PROPERTY_NAME);
        iotHubConnectionString.sharedAccessSignature = getValueStringByKey(keyValueMap, IotHubConnectionString.SHARED_ACCESS_SIGNATURE_PROPERTY_NAME);
        iotHubConnectionString.iotHubName = parseIotHubName(iotHubConnectionString);

        if (iotHubConnectionString.sharedAccessKey == null || iotHubConnectionString.sharedAccessKey.isEmpty())
        {
            iotHubConnectionString.authenticationMethod = new ServiceAuthenticationWithSharedAccessPolicyToken(
                    iotHubConnectionString.sharedAccessKeyName,
                    iotHubConnectionString.sharedAccessSignature);
        }
        if (iotHubConnectionString.sharedAccessSignature == null || iotHubConnectionString.sharedAccessSignature.isEmpty())
        {
            iotHubConnectionString.authenticationMethod = new ServiceAuthenticationWithSharedAccessPolicyKey(
                    iotHubConnectionString.sharedAccessKeyName,
                    iotHubConnectionString.sharedAccessKey);
        }

        validate(iotHubConnectionString);
    }

    /**
     * Parse the iot hub name part from the host name
     *
     * @param iotHubConnectionString The source iotHubConnectionString containing the hostName
     * @return The substring of the host name until the first "." character
     */
    private static String parseIotHubName(IotHubConnectionString iotHubConnectionString)
    {
        int index = iotHubConnectionString.hostName.indexOf(IotHubConnectionString.HOST_NAME_SEPARATOR);
        if (index >= 0)
        {
            return iotHubConnectionString.hostName.substring(0, index);
        }

        return "";
    }

    /**
     * Validate IotHubConnectionString format
     *
     * @param iotHubConnectionString The object to validate
     * @throws IllegalArgumentException This exception is thrown if the input object is null
     */
    private static void validate(IotHubConnectionString iotHubConnectionString) throws IllegalArgumentException
    {
        if (iotHubConnectionString.sharedAccessKeyName == null || iotHubConnectionString.sharedAccessKeyName.isEmpty())
        {
            throw new IllegalArgumentException("SharedAccessKeyName cannot be null or empty");
        }
        if ((iotHubConnectionString.sharedAccessKey == null || iotHubConnectionString.sharedAccessKey.isEmpty())
            && (iotHubConnectionString.sharedAccessSignature == null || iotHubConnectionString.sharedAccessSignature.isEmpty()))
        {
            throw new IllegalArgumentException("Should specify either sharedAccessKey or sharedAccessSignature");
        }

        validateFormat(iotHubConnectionString.hostName, HOST_NAME_REGEX);
        validateFormatIfSpecified(iotHubConnectionString.sharedAccessKeyName, SHARED_ACCESS_KEY_NAME_REGEX);
        validateFormatIfSpecified(iotHubConnectionString.sharedAccessKey, SHARED_ACCESS_KEY_REGEX);
        validateFormatIfSpecified(iotHubConnectionString.sharedAccessSignature, SHARED_ACCESS_SIGNATURE_REGEX);
    }

    /**
     * Validate string property using given regex
     *
     * @param value The string value to validate
     * @param regex The regex used for validation
     */
    private static void validateFormat(String value, String regex)
    {
        final Pattern pattern = Pattern.compile(regex);
        if (!pattern.matcher(value).matches())
        {
            throw new IllegalArgumentException("The connection string has an invalid value for property.");
        }
    }

    /**
     * Validate string property using given regex if value is not null or empty
     *
     * @param value string value to validate
     * @param regex regex used for validation
     */
    private static void validateFormatIfSpecified(String value, String regex)
    {
        if (!(value == null || value.isEmpty()))
        {
            validateFormat(value, regex);
        }
    }

    /**
     * Set host name value to target IotHubConnectionString object
     *
     * @param hostName host name string
     * @param iotHubConnectionString target IotHubConnectionString object
     */
    private static void setHostName(String hostName, IotHubConnectionString iotHubConnectionString)
    {
        validateFormat(hostName, HOST_NAME_REGEX);
        iotHubConnectionString.hostName = hostName;
        iotHubConnectionString.iotHubName = parseIotHubName(iotHubConnectionString);
    }

    /**
     * Set authentication method to target IotHubConnectionString object
     *
     * @param authenticationMethod value to set
     * @param iotHubConnectionString target IotHubConnectionString object
     */
    private static void setAuthenticationMethod(AuthenticationMethod authenticationMethod, IotHubConnectionString iotHubConnectionString)
    {
        authenticationMethod.populate(iotHubConnectionString);
        iotHubConnectionString.authenticationMethod = authenticationMethod;
    }

    /**
     * Helper function to get a value from the given Map if the key name exists
     *
     * @param map The Map object to get the value from
     * @param keyName The name of the key
     * @return The value of the given key if exists otherwise empty string
     */
    private static String getValueStringByKey(Map<String, ?> map, String keyName)
    {
        String retVal;

        if ((map == null) || (keyName == null))
        {
            retVal = "";
        }
        else
        {
            Object val = map.get(keyName);
            if (val != null)
                retVal = val.toString().trim();
            else
                retVal = "";
        }

        return retVal;
    }
}
