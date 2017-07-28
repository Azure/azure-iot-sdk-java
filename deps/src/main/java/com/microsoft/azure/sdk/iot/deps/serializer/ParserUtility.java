// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Set of static functions to help the serializer.
 */
public class ParserUtility
{
    private static final String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String OFFSETFORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
    private static final String TIMEZONE = "UTC";
    private static final String SELECT = "select";
    private static final String FROM = "from";

    private static final int NO_MILLISECONDS_IN_DATE = 0;
    private static final int DATE_AND_TIME_IN_DATE = 0;
    private static final int MILLISECONDS_IN_DATE = 1;
    private static final int EXPECTED_PARTS_IN_DATE = 2;
    private static final int MAX_MILLISECONDS_LENGTH_IN_DATE = 3;
    private static final double MILLISECONDS_NUMERIC_BASE = 10;
    private static final String MILLISECONDS_REGEX = "[.,Z]";

    /**
     * Helper to validate if the provided string is not null, empty, and all characters are UTF-8.
     *
     * @param str is the string to be validated.
     * @throws IllegalArgumentException if the string do not fit the criteria.
     */
    protected static void validateStringUTF8(String str) throws IllegalArgumentException
    {
        /* Codes_SRS_PARSER_UTILITY_21_002: [The validateStringUTF8 shall throw IllegalArgumentException if the provided string is null or empty.] */
        if((str == null) || str.isEmpty())
        {
            throw new IllegalArgumentException("parameter is null or empty");
        }

        /* Codes_SRS_PARSER_UTILITY_21_003: [The validateStringUTF8 shall throw IllegalArgumentException if the provided string contains at least one not UTF-8 character.] */
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
     *
     * Validates if query contains select and from keywords and also if it is a valid utf-8 string
     * @param query query to be validated
     * @throws IllegalArgumentException if query does not contain "select" or "from" or is not a valid utf-8 string
     */
    public static void validateQuery(String query) throws IllegalArgumentException
    {
        /*
        Codes_SRS_PARSER_UTILITY_25_031: [The validateQuery shall do nothing if the string is valid.]
        Codes_SRS_PARSER_UTILITY_25_032: [The validateQuery shall throw IllegalArgumentException is the provided query is null or empty.]
        Codes_SRS_PARSER_UTILITY_25_033: [The validateQuery shall throw IllegalArgumentException is the provided query contains non UTF-8 character.]
        Codes_SRS_PARSER_UTILITY_25_034: [The validateQuery shall throw IllegalArgumentException is the provided query does not contain SELECT and FROM.]
         */
        try
        {
            validateStringUTF8(query);
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("The provided query is not valid");
        }

        if (!query.toLowerCase().contains(SELECT) || !query.toLowerCase().contains(FROM))
        {
            throw new IllegalArgumentException("Query must contain select and from");
        }
    }

    /**
     * Helper to validate if the provided blob name is not null, empty, and valid.
     *
     * @param blobName is the blob name to be validated.
     * @throws IllegalArgumentException if the blob name do not fit the criteria.
     */
    public static void validateBlobName(String blobName) throws IllegalArgumentException
    {
        /* Codes_SRS_PARSER_UTILITY_21_005: [The validateBlobName shall throw IllegalArgumentException if the provided blob name is null or empty.] */
        /* Codes_SRS_PARSER_UTILITY_21_006: [The validateBlobName shall throw IllegalArgumentException if the provided blob name contains at least one not UTF-8 character.] */
        try
        {
            validateStringUTF8(blobName);
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("The provided blob name is not valid");
        }

        /* Codes_SRS_PARSER_UTILITY_21_007: [The validateBlobName shall throw IllegalArgumentException if the provided blob name contains more than 1024 characters.] */
        if(blobName.length() > 1024)
        {
            throw new IllegalArgumentException("The provided blob name exceed maximum size of 1024 characters");
        }

        /* Codes_SRS_PARSER_UTILITY_21_008: [The validateBlobName shall throw IllegalArgumentException if the provided blob name contains more than 254 path segments.] */
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
        /* Codes_SRS_PARSER_UTILITY_21_010: [The validateObject shall throw IllegalArgumentException if the provided object is null.] */
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
        /* Codes_SRS_PARSER_UTILITY_21_014: [The validateKey shall throw IllegalArgumentException if the provided string is null or empty.] */
        /* Codes_SRS_PARSER_UTILITY_21_015: [The validateKey shall throw IllegalArgumentException if the provided string contains at least one not UTF-8 character.] */
        try
        {
            validateStringUTF8(key);
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("The provided key is not valid");
        }

        /* Codes_SRS_PARSER_UTILITY_21_016: [The validateKey shall throw IllegalArgumentException if the provided string contains more than 128 characters.] */
        if(key.length() > 128)
        {
            throw new IllegalArgumentException("The provided key is bigger than 128 characters");
        }

        /* Codes_SRS_PARSER_UTILITY_21_017: [The validateKey shall throw IllegalArgumentException if the provided string contains an illegal character (`$`,`.`, space).] */
        /* Codes_SRS_PARSER_UTILITY_21_018: [If `isMetadata` is `true`, the validateKey shall accept the character `$` as valid.] */
        /* Codes_SRS_PARSER_UTILITY_21_019: [If `isMetadata` is `false`, the validateKey shall not accept the character `$` as valid.] */
        if((key.contains(".") || key.contains(" ") || (key.contains("$") && ! isMetadata)))
        {
            throw new IllegalArgumentException("The provided key is not valid");
        }

        /* Codes_SRS_PARSER_UTILITY_21_013: [The validateKey shall do nothing if the string is a valid key.] */
    }

    /**
     * Validate if a provided ID is valid using the follow criteria.
     * A case-sensitive string (up to 128 char long)
     * of ASCII 7-bit alphanumeric chars
     * + {'-', ':', '.', '+', '%', '_', '#', '*', '?', '!', '(', ')', ',', '=', '@', ';', '$', '''}.
     *
     * @param id is the ID to test
     * @throws IllegalArgumentException if the ID do not fits the criteria
     */
    protected static void validateId(String id) throws IllegalArgumentException
    {
        /* Codes_SRS_PARSER_UTILITY_21_026: [The validateId shall throw IllegalArgumentException if the provided string is null or empty.] */
        /* Codes_SRS_PARSER_UTILITY_21_027: [The validateId shall throw IllegalArgumentException if the provided string contains at least one not UTF-8 character.] */
        try
        {
            validateStringUTF8(id);
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("The provided ID is not valid");
        }

        /* Codes_SRS_PARSER_UTILITY_21_028: [The validateId shall throw IllegalArgumentException if the provided string contains more than 128 characters.] */
        if(id.length() > 128)
        {
            throw new IllegalArgumentException("The provided ID is bigger than 128 characters");
        }

        /* Codes_SRS_PARSER_UTILITY_21_029: [The validateId shall throw IllegalArgumentException if the provided string contains an illegal character.] */
        byte[] chars = id.getBytes();
        for (byte c:chars)
        {
            if(!(((c>='A') && (c<='Z')) || ((c>='a') && (c<='z')) || ((c>='0') && (c<='9')) ||
                    (c=='-') || (c==':') || (c=='.') || (c=='+') || (c=='%') || (c=='_') || (c=='#') || (c=='*') || (c=='?') ||
                    (c=='!') || (c=='(') || (c==')') || (c==',') || (c=='=') || (c=='@') || (c==';') || (c=='$') || (c=='\'')))
            {
                throw new IllegalArgumentException("The provided ID is not valid");
            }
        }

        /* Codes_SRS_PARSER_UTILITY_21_030: [The validateId shall do nothing if the string is a valid ID.] */
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
        /* Codes_SRS_PARSER_UTILITY_21_021: [The getDateTimeUtc shall parse the provide string using the data format `yyyy-MM-dd'T'HH:mm:ss.SSS'Z'`.] */
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE));

        /* Codes_SRS_PARSER_UTILITY_21_022: [If the provide string is null, empty or contains an invalid data format, the getDateTimeUtc shall throw IllegalArgumentException.] */
        if((dataTime == null) || dataTime.isEmpty() || (dataTime.charAt(dataTime.length()-1) != 'Z'))
        {
            throw new IllegalArgumentException("date is null, empty, or invalid");
        }

        try
        {
            /* Codes_SRS_PARSER_UTILITY_21_040: [If the provide string contains more than 3 digits for milliseconds, the getDateTimeUtc shall reduce the milliseconds to 3 digits.] */
            String[] splitDateTime = dataTime.split(MILLISECONDS_REGEX);
            int milliseconds;
            if(splitDateTime.length > EXPECTED_PARTS_IN_DATE)
            {
                throw new IllegalArgumentException("invalid time:" + dataTime);
            }
            else if((splitDateTime.length == EXPECTED_PARTS_IN_DATE) && !splitDateTime[MILLISECONDS_IN_DATE].isEmpty())
            {
                int millisecondsLength = splitDateTime[MILLISECONDS_IN_DATE].length();
                if(millisecondsLength > MAX_MILLISECONDS_LENGTH_IN_DATE)
                {
                    millisecondsLength = MAX_MILLISECONDS_LENGTH_IN_DATE;
                }

                milliseconds = Integer.parseInt(splitDateTime[MILLISECONDS_IN_DATE].substring(0, millisecondsLength)) *
                        (int)Math.pow(MILLISECONDS_NUMERIC_BASE, (MAX_MILLISECONDS_LENGTH_IN_DATE - millisecondsLength));
            }
            else
            {
                /* Codes_SRS_PARSER_UTILITY_21_041: [The getDateTimeUtc shall accept date without milliseconds.] */
                milliseconds = NO_MILLISECONDS_IN_DATE;
            }
            dateTimeUtc =  new Date(dateFormat.parse(splitDateTime[DATE_AND_TIME_IN_DATE]).getTime() + milliseconds);
        }
        catch (ParseException e)
        {
            throw new IllegalArgumentException("invalid time:" + dataTime);
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

    /**
     * Helper to convert a provided map in to a JsonElement, including sub-maps.
     *
     * @param map is the map to serialize
     * @return a JsonElement that represents the content of the map.
     * @throws IllegalArgumentException if the provided map is null.
     */
    protected static JsonElement mapToJsonElement(Map<String, Object> map) throws IllegalArgumentException
    {
        /* Codes_SRS_PARSER_UTILITY_21_035: [The mapToJsonElement shall serialize the provided map into a JsonElement.] */
        /* Codes_SRS_PARSER_UTILITY_21_036: [The mapToJsonElement shall include keys with null values in the JsonElement.] */
        Gson gson = new GsonBuilder().serializeNulls().create();

        /* Codes_SRS_PARSER_UTILITY_21_038: [If the map is empty, the mapToJsonElement shall return a empty JsonElement.] */
        JsonObject json = new JsonObject();

        if(map == null)
        {
            /* Codes_SRS_PARSER_UTILITY_21_039: [If the map is null, the mapToJsonElement shall throw IllegalArgumentException.] */
            throw new IllegalArgumentException("null map to parse");
        }

        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            if (entry.getValue() == null)
            {
                json.addProperty(entry.getKey(), (String)null);
            }
            else if(entry.getValue() instanceof Map)
            {
                /* Codes_SRS_PARSER_UTILITY_21_037: [If the value is a map, the mapToJsonElement shall include it as a submap in the JsonElement.] */
                json.add(entry.getKey(), mapToJsonElement((Map<String, Object>) entry.getValue()));
            }
            else
            {
                json.add(entry.getKey(), gson.toJsonTree(entry.getValue()));
            }
        }

        return json;
    }

}
