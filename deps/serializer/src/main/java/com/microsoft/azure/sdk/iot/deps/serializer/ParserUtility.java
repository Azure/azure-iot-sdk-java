// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Set of static functions to help the serializer.
 */
public class ParserUtility
{
    private static final String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSS'Z'";
    private static final String OFFSETFORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
    private static final String TIMEZONE = "UTC";

    /**
     * Helper to validate if the provided string is not null, empty, and all characters are UTF-8.
     *
     * @param str is the string to be validated.
     * @throws IllegalArgumentException if the string do not fit the criteria.
     */
    protected static void validateStringUTF8(String str) throws IllegalArgumentException
    {
        /* Codes_SRS_PARSER_UTILITY_21_002: [The validateStringUTF8 shall throw IllegalArgumentException is the provided string is null or empty.] */
        if((str == null) || str.isEmpty())
        {
            throw new IllegalArgumentException("parameter is null or empty");
        }

        /* Codes_SRS_PARSER_UTILITY_21_003: [The validateStringUTF8 shall throw IllegalArgumentException is the provided string contains at least one not UTF-8 character.] */
        try
        {
            if(str.getBytes("UTF-8").length != str.length())
            {
                throw new IllegalArgumentException("invalid parameter");
            }
        }
        catch(UnsupportedEncodingException e)
        {
            throw new IllegalArgumentException("invalid parameter");
        }

        /* Codes_SRS_PARSER_UTILITY_21_001: [The validateStringUTF8 shall do nothing if the string is valid.] */
    }

    /**
     * Helper to validate if the provided blob name is not null, empty, and valid.
     *
     * @param blobName is the blob name to be validated.
     * @throws IllegalArgumentException if the blob name do not fit the criteria.
     */
    public static void validateBlobName(String blobName) throws IllegalArgumentException
    {
        /* Codes_SRS_PARSER_UTILITY_21_005: [The validateBlobName shall throw IllegalArgumentException is the provided blob name is null or empty.] */
        /* Codes_SRS_PARSER_UTILITY_21_006: [The validateBlobName shall throw IllegalArgumentException is the provided blob name contains at least one not UTF-8 character.] */
        try
        {
            validateStringUTF8(blobName);
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("The provided blob name is not valid");
        }

        /* Codes_SRS_PARSER_UTILITY_21_007: [The validateBlobName shall throw IllegalArgumentException is the provided blob name contains more than 1024 characters.] */
        if(blobName.length() > 1024)
        {
            throw new IllegalArgumentException("The provided blob name exceed maximum size of 1024 characters");
        }

        /* Codes_SRS_PARSER_UTILITY_21_008: [The validateBlobName shall throw IllegalArgumentException is the provided blob name contains more than 254 path segments.] */
        if (blobName.split("/").length > 254)
        {
            throw new IllegalArgumentException("The provided blob name exceed 254 path segments");
        }

        /* Codes_SRS_PARSER_UTILITY_21_004: [The validateBlobName shall do nothing if the string is valid.] */
    }

    /**
     * Helper to validate if the provided object is not null.
     *
     * @param val is the object to be validated.
     * @throws IllegalArgumentException if the object do not fit the criteria.
     */
    protected static void validateObject(Object val) throws IllegalArgumentException
    {
        /* Codes_SRS_PARSER_UTILITY_21_009: [The validateObject shall do nothing if the object is valid.] */
        /* Codes_SRS_PARSER_UTILITY_21_010: [The validateObject shall throw IllegalArgumentException is the provided object is null.] */
        if(val == null)
        {
            throw new IllegalArgumentException("parameter is null");
        }
    }

    /**
     * Helper to validate if the provided string is a valid json key.
     *
     * @param key is the string to be validated.
     * @param isMetadata defines if the key belongs to a metadata, which allows character `$`.
     * @throws IllegalArgumentException if the string do not fit the criteria.
     */
    protected static void validateKey(String key, boolean isMetadata) throws IllegalArgumentException
    {
        /* Codes_SRS_PARSER_UTILITY_21_014: [The validateKey shall throw IllegalArgumentException is the provided string is null or empty.] */
        /* Codes_SRS_PARSER_UTILITY_21_015: [The validateKey shall throw IllegalArgumentException is the provided string contains at least one not UTF-8 character.] */
        try
        {
            validateStringUTF8(key);
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("The provided key is not valid");
        }

        /* Codes_SRS_PARSER_UTILITY_21_016: [The validateKey shall throw IllegalArgumentException is the provided string contains more than 128 characters.] */
        if(key.length() > 128)
        {
            throw new IllegalArgumentException("The provided key is bigger than 128 characters");
        }

        /* Codes_SRS_PARSER_UTILITY_21_017: [The validateKey shall throw IllegalArgumentException is the provided string contains an illegal character (`$`,`.`, space).] */
        /* Codes_SRS_PARSER_UTILITY_21_018: [If `isMetadata` is `true`, the validateKey shall accept the character `$` as valid.] */
        /* Codes_SRS_PARSER_UTILITY_21_019: [If `isMetadata` is `false`, the validateKey shall not accept the character `$` as valid.] */
        if((key.contains(".") || key.contains(" ") || (key.contains("$") && ! isMetadata)))
        {
            throw new IllegalArgumentException("The provided key is not valid");
        }

        /* Codes_SRS_PARSER_UTILITY_21_013: [The validateKey shall do nothing if the string is a valid key.] */
    }

    /**
     * Helper to convert the provided string in a UTC Date.
     * Expected format:
     *      "2016-06-01T21:22:43.7996883Z"
     *
     * @param dataTime is the string with the date and time
     * @return Date parsed from the string
     * @throws IllegalArgumentException if the date and time in the string is not in the correct format.
     */
    protected static Date getDateTimeUtc(String dataTime) throws IllegalArgumentException
    {
        Date dateTimeUtc;
        /* Codes_SRS_PARSER_UTILITY_21_020: [The getDateTimeUtc shall parse the provide string using `UTC` timezone.] */
        /* Codes_SRS_PARSER_UTILITY_21_021: [The getDateTimeUtc shall parse the provide string using the data format `yyyy-MM-dd'T'HH:mm:ss.SSSS'Z'`.] */
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE));

        /* Codes_SRS_PARSER_UTILITY_21_022: [If the provide string is null, empty or contains an invalid data format, the getDateTimeUtc shall throw IllegalArgumentException.] */
        if((dataTime == null) || dataTime.isEmpty())
        {
            throw new IllegalArgumentException("date is null or empty");
        }

        try
        {
            dateTimeUtc = dateFormat.parse(dataTime);
        }
        catch (ParseException e)
        {
            throw new IllegalArgumentException("invalid time:" + e.toString());
        }
        
        return dateTimeUtc;
    }

    /**
     * Helper to convert the provided string in a offset Date.
     * Expected format:
     *      "2016-06-01T21:22:41+00:00"
     *
     * @param dataTime is the string with the date and time
     * @return Date parsed from the string
     * @throws IllegalArgumentException if the date and time in the string is not in the correct format.
     */
    protected static Date getDateTimeOffset(String dataTime) throws IllegalArgumentException
    {
        Date dateTimeOffset;

        /* Codes_SRS_PARSER_UTILITY_21_023: [The getDateTimeOffset shall parse the provide string using `UTC` timezone.] */
        /* Codes_SRS_PARSER_UTILITY_21_024: [The getDateTimeOffset shall parse the provide string using the data format `2016-06-01T21:22:41+00:00`.] */
        SimpleDateFormat dateFormat = new SimpleDateFormat(OFFSETFORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE));

        /* Codes_SRS_PARSER_UTILITY_21_025: [If the provide string is null, empty or contains an invalid data format, the getDateTimeOffset shall throw IllegalArgumentException.] */
        if((dataTime == null) || dataTime.isEmpty())
        {
            throw new IllegalArgumentException("date is null or empty");
        }

        try
        {
            dateTimeOffset = dateFormat.parse(dataTime);
        }
        catch (ParseException e)
        {
            throw new IllegalArgumentException("invalid time:" + e.toString());
        }

        return dateTimeOffset;
    }
}
