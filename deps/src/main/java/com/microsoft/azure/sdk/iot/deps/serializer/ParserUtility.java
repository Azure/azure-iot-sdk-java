// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.azure.sdk.iot.deps.twin.TwinMetadata;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

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
    public static void validateStringUTF8(String str) throws IllegalArgumentException
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
                throw new IllegalArgumentException("parameter contains non UTF-8 character");
            }
        }
        catch(UnsupportedEncodingException e)
        {
            throw new IllegalArgumentException("parameter contains non UTF-8 character");
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
    public static void validateObject(Object val) throws IllegalArgumentException
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
    public static void validateKey(String key, boolean isMetadata) throws IllegalArgumentException
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
     * Helper to validate if the provided map in terms of maximum
     * levels and optionally if the keys ar not metadata.
     *
     * @param map the {@code Map} to be validate. It can be {@code null}, and it will succeed in this case.
     * @param maxLevel the max number of level allowed in the map.
     * @param allowMetadata the {@code boolean} that indicates if the key can contain metadata `$` or not.
     * @throws IllegalArgumentException If the Map contains more than maxLevel levels or do not allow metadata
     *                                  but contains metadata key.
     */
    public static void validateMap(Map<String, Object> map, int maxLevel, boolean allowMetadata) throws IllegalArgumentException
    {
        /* Codes_SRS_PARSER_UTILITY_21_046: [The validateMap shall throws IllegalArgumentException if the maxLevel is `0` or negative.] */
        if(maxLevel <= 0)
        {
            throw new IllegalArgumentException("maxLevel cannot be zero or negative");
        }
        /* Codes_SRS_PARSER_UTILITY_21_048: [The validateMap shall do nothing if the map is null.] */
        if(map != null)
        {
            /* Codes_SRS_PARSER_UTILITY_21_047: [The validateMap shall do nothing if the map is a valid Map.] */
            validateMapInternal(map, 1, maxLevel, allowMetadata);
        }
    }

    private static void validateMapInternal(Map<String, Object> map, int level, int maxLevel, boolean allowMetadata) throws IllegalArgumentException
    {
        level ++;

        for(Map.Entry<String, Object> entry : map.entrySet())
        {
            String key = entry.getKey();
            Object value = entry.getValue();

            /* Codes_SRS_PARSER_UTILITY_21_049: [The validateMap shall throws IllegalArgumentException if any key in the map is null, empty, contains more than 128 characters, or illegal characters (`$`,`.`, space).] */
            /* Codes_SRS_PARSER_UTILITY_21_050: [If `isMetadata` is `true`, the validateMap shall accept the character `$` in the key.] */
            ParserUtility.validateKey(key, allowMetadata);

            /* Codes_SRS_PARSER_UTILITY_21_051: [The validateMap shall throws IllegalArgumentException if any value contains illegal type (array or invalid class).] */
            if((value != null) && ((value.getClass().isArray()) || (value.getClass().isLocalClass())))
            {
                throw new IllegalArgumentException("Map contains illegal value type " + value.getClass().getName());
            }

            if((value != null) && (value instanceof Map))
            {
                /* Codes_SRS_PARSER_UTILITY_21_052: [The validateMap shall throws IllegalArgumentException if the provided map contains more than maxLevel levels and those extra levels contain more than just metadata.] */
                if(level <= maxLevel)
                {
                    validateMapInternal((Map<String, Object>) value, level, maxLevel, allowMetadata);
                }
                else
                {
                    if (!mapOnlyContainsMetaData((Map<String, Object>)value))
                    {
                        throw new IllegalArgumentException("Map exceed maximum of " + maxLevel + " levels");
                    }
                }
            }
        }
    }

    private static boolean mapOnlyContainsMetaData(Map<String, Object> map)
    {
        for (String key : map.keySet())
        {
            if (!(key.equals(TwinMetadata.LAST_UPDATE_TAG)) && !(key.equals(TwinMetadata.LAST_UPDATE_VERSION_TAG)))
            {
                return false;
            }
        }

        return true;
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
    public static void validateId(String id) throws IllegalArgumentException
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
     * Validate if a provided host name is valid using the follow criteria.
     * A case-sensitive string (up to 128 char long)
     *   of ASCII 7-bit alphanumeric chars
     *   + {'-', ':', '.', '+', '%', '_', '#', '*', '?', '!', '(', ')', ',', '=', '@', ';', '$', '''}.
     * Contains at least one separator '.'
     *
     * @param hostName is the host name to test
     * @throws IllegalArgumentException if the provided host name do not fits the criteria
     */
    public static void validateHostName(String hostName) throws IllegalArgumentException
    {
        /* Codes_SRS_PARSER_UTILITY_21_044: [The validateHostName shall throw IllegalArgumentException if the provided string is not a valid host name.] */
        /* Codes_SRS_PARSER_UTILITY_21_045: [The validateHostName shall do nothing if the string is a valid host name.] */
        validateId(hostName);
        if (hostName.split(Pattern.quote(".")).length < 2)
        {
            throw new IllegalArgumentException("hostName is incomplete");
        }
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
    public static Date getDateTimeUtc(String dataTime) throws IllegalArgumentException
    {
        Date dateTimeUtc;
        /* Codes_SRS_PARSER_UTILITY_21_020: [The getDateTimeUtc shall parse the provide string using `UTC` timezone.] */
        /* Codes_SRS_PARSER_UTILITY_21_021: [The getDateTimeUtc shall parse the provide string using the data format `yyyy-MM-dd'T'HH:mm:ss`.] */
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE));

        /* Codes_SRS_PARSER_UTILITY_21_022: [If the provide string is null, empty or contains an invalid data format, the getDateTimeUtc shall throw IllegalArgumentException.] */
        if((dataTime == null) || dataTime.isEmpty())
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
     * @param dateTime is the string with the date and time
     * @return Date parsed from the string
     * @throws IllegalArgumentException if the date and time in the string is not in the correct format.
     */
    public static Date stringToDateTimeOffset(String dateTime) throws IllegalArgumentException
    {
        Date dateTimeOffset;

        /* Codes_SRS_PARSER_UTILITY_21_023: [The stringToDateTimeOffset shall parse the provide string using `UTC` timezone.] */
        /* Codes_SRS_PARSER_UTILITY_21_024: [The stringToDateTimeOffset shall parse the provide string using the data format `2016-06-01T21:22:41+00:00`.] */
        SimpleDateFormat dateFormat = new SimpleDateFormat(OFFSETFORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE));

        /* Codes_SRS_PARSER_UTILITY_21_025: [If the provide string is null, empty or contains an invalid data format, the stringToDateTimeOffset shall throw IllegalArgumentException.] */
        if((dateTime == null) || dateTime.isEmpty())
        {
            throw new IllegalArgumentException("date is null or empty");
        }

        try
        {
            dateTimeOffset = dateFormat.parse(dateTime);
        }
        catch (ParseException e)
        {
            throw new IllegalArgumentException("invalid time:" + e.toString());
        }

        return dateTimeOffset;
    }

    /**
     * Helper to convert the provided Date UTC into String.
     * Expected result:
     *      "2016-06-01T21:22:43.799Z"
     *
     * @param date is the {@code Date} with the date and time
     * @return the {@code String} with the date and time using the UTC format.
     * @throws IllegalArgumentException if the provided date is {@code null}.
     */
    public static String dateTimeUtcToString(Date date) throws IllegalArgumentException
    {
        /* Codes_SRS_PARSER_UTILITY_21_053: [The dateTimeUtcToString shall throws IllegalArgumentException if the provided Date is null.] */
        if(date == null)
        {
            throw new IllegalArgumentException("date cannot be null");
        }

        /* Codes_SRS_PARSER_UTILITY_21_054: [The dateTimeUtcToString shall serialize the provide Date using `UTC` timezone.] */
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
        StringBuilder dateStr = new StringBuilder();
        dateStr.append(dateFormat.format(date));
        dateStr.append(".");
        int milliseconds = (int)(date.getTime() % 1000L);
        milliseconds = milliseconds < 0 ? milliseconds + 1000 : milliseconds;
        dateStr.append(milliseconds);
        dateStr.append("Z");
        return dateStr.toString();
    }

    /**
     * Convert from a date object back into a string representation
     * Expected format of returned string:
     *      "2016-01-21T11:05:21"
     *
     * @param date the date to convert into a string
     * @throws IllegalArgumentException if the provided date is null
     * @return the date represented as a string
     */
    public static String getDateStringFromDate(Date date) throws IllegalArgumentException
    {
        if (date == null)
        {
            //Codes_SRS_PARSER_UTILITY_21_042: [If the provided date is null, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("The provided date cannot be null");
        }

        //Codes_SRS_PARSER_UTILITY_34_043: [The provided date shall be converted into this format: "yyyy-MM-dd'T'HH:mm:ss".]
        return new SimpleDateFormat(DATEFORMAT).format(date);
    }

    /**
     * Helper to convert a provided map in to a JsonElement, including sub-maps.
     *
     * @param map is the map to serialize
     * @return a JsonElement that represents the content of the map.
     * @throws IllegalArgumentException if the provided map is null.
     */
    public static JsonElement mapToJsonElement(Map<String, Object> map) throws IllegalArgumentException
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
