// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.serializers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Set of static functions to help the serializer.
 */
// Unchecked casts of Maps to Map<String, Object> are safe as long as service is returning valid twin json payloads. Since all json keys are Strings, all maps must be Map<String, Object>
@SuppressWarnings("unchecked")
public class ParserUtility
{
    private static final String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String OFFSETFORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";

    final static DateTimeFormatter UTC_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATEFORMAT);

    private static final String TIMEZONE_UTC = "UTC";
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
        if ((str == null) || str.isEmpty())
        {
            throw new IllegalArgumentException("parameter is null or empty");
        }

        if (str.getBytes(StandardCharsets.UTF_8).length != str.length())
        {
            throw new IllegalArgumentException("parameter contains non UTF-8 character");
        }
    }

    /**
     *
     * Validates if query contains select and from keywords and also if it is a valid utf-8 string
     * @param query query to be validated
     * @throws IllegalArgumentException if query does not contain "select" or "from" or is not a valid utf-8 string
     */
    public static void validateQuery(String query) throws IllegalArgumentException
    {
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
        try
        {
            validateStringUTF8(blobName);
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("The provided blob name is not valid");
        }

        if (blobName.length() > 1024)
        {
            throw new IllegalArgumentException("The provided blob name exceed maximum size of 1024 characters");
        }

        if (blobName.split("/").length > 254)
        {
            throw new IllegalArgumentException("The provided blob name exceed 254 path segments");
        }
    }

    /**
     * Helper to validate if the provided object is not null.
     *
     * @param val is the object to be validated.
     * @throws IllegalArgumentException if the object do not fit the criteria.
     */
    public static void validateObject(Object val) throws IllegalArgumentException
    {
        if (val == null)
        {
            throw new IllegalArgumentException("parameter is null");
        }
    }

    /**
     * Helper to validate if the provided map in terms of maximum
     * levels and optionally if the keys ar not metadata.
     *
     * @param map the {@code Map} to be validate. It can be {@code null}, and it will succeed in this case.
     * @throws IllegalArgumentException If the Map contains more than maxLevel levels or do not allow metadata
     *                                  but contains metadata key.
     */
    public static void validateMap(Map<String, Object> map) throws IllegalArgumentException
    {
        if (map != null)
        {
            validateMapInternal(map);
        }
    }

    private static void validateMapInternal(Map<String, Object> map) throws IllegalArgumentException
    {
        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            Object value = entry.getValue();

            if ((value != null) && ((value.getClass().isArray()) || (value.getClass().isLocalClass())))
            {
                throw new IllegalArgumentException("Map contains illegal value type " + value.getClass().getName());
            }

            if (value instanceof Map)
            {
                validateMapInternal((Map<String, Object>) value);
            }
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
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE_UTC));

        /* Codes_SRS_PARSER_UTILITY_21_022: [If the provide string is null, empty or contains an invalid data format, the getDateTimeUtc shall throw IllegalArgumentException.] */
        if ((dataTime == null) || dataTime.isEmpty())
        {
            throw new IllegalArgumentException("date is null, empty, or invalid");
        }

        try
        {
            String[] splitDateTime = dataTime.split(MILLISECONDS_REGEX);
            int milliseconds;
            if (splitDateTime.length > EXPECTED_PARTS_IN_DATE)
            {
                throw new IllegalArgumentException("invalid time:" + dataTime);
            }
            else if ((splitDateTime.length == EXPECTED_PARTS_IN_DATE) && !splitDateTime[MILLISECONDS_IN_DATE].isEmpty())
            {
                int millisecondsLength = splitDateTime[MILLISECONDS_IN_DATE].length();
                if (millisecondsLength > MAX_MILLISECONDS_LENGTH_IN_DATE)
                {
                    millisecondsLength = MAX_MILLISECONDS_LENGTH_IN_DATE;
                }

                milliseconds = Integer.parseInt(splitDateTime[MILLISECONDS_IN_DATE].substring(0, millisecondsLength)) *
                        (int)Math.pow(MILLISECONDS_NUMERIC_BASE, (MAX_MILLISECONDS_LENGTH_IN_DATE - millisecondsLength));
            }
            else
            {
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

        SimpleDateFormat dateFormat = new SimpleDateFormat(OFFSETFORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE_UTC));

        if ((dateTime == null) || dateTime.isEmpty())
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
        if (date == null)
        {
            throw new IllegalArgumentException("date cannot be null");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE_UTC));
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
    public static String getUTCDateStringFromDate(Date date) throws IllegalArgumentException
    {
        if (date == null)
        {
            throw new IllegalArgumentException("The provided date cannot be null");
        }

        OffsetDateTime offsetDateTime = date.toInstant().atOffset(ZoneOffset.UTC);

        return offsetDateTime.format(UTC_DATETIME_FORMATTER);
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
        Gson gson = new GsonBuilder().serializeNulls().create();
        JsonObject json = new JsonObject();

        if (map == null)
        {
            throw new IllegalArgumentException("null map to parse");
        }

        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            if (entry.getValue() == null)
            {
                json.addProperty(entry.getKey(), (String)null);
            }
            else if (entry.getValue() instanceof Map)
            {
                json.add(entry.getKey(), mapToJsonElement((Map<String, Object>) entry.getValue()));
            }
            else
            {
                json.add(entry.getKey(), gson.toJsonTree(entry.getValue()));
            }
        }

        return json;
    }

    public static Object resolveJsonElement(JsonElement jsonElement)
    {
        if (jsonElement == null || jsonElement.isJsonNull())
        {
            return null;
        }
        else if (jsonElement.isJsonPrimitive())
        {
            return getJsonPrimitiveValue(jsonElement.getAsJsonPrimitive());
        }
        else if (jsonElement.isJsonObject())
        {
            return getJsonObjectValue(jsonElement.getAsJsonObject());
        }
        else if (jsonElement.isJsonArray())
        {
            return getJsonArrayValue(jsonElement.getAsJsonArray());
        }
        else
        {
            // shouldn't be here
            throw new IllegalArgumentException("Invalid DeviceMethodResponse payload: unknown payload type: " + jsonElement.getClass());
        }
    }

    private static Object getJsonPrimitiveValue(JsonPrimitive jsonPrimitive)
    {
        if (jsonPrimitive.isNumber())
        {
            return jsonPrimitive.getAsNumber();
        }
        else if (jsonPrimitive.isBoolean())
        {
            return jsonPrimitive.getAsBoolean();
        }
        else
        {
            return jsonPrimitive.getAsString();
        }
    }

    public static Map<String, Object> getJsonObjectValue(JsonObject jsonObject)
    {
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet())
        {
            map.put(entry.getKey(), resolveJsonElement(entry.getValue()));
        }

        return map;
    }

    private static List<Object> getJsonArrayValue(JsonArray jsonArray)
    {
        List<Object> list = new ArrayList<>();
        for (JsonElement element : jsonArray.getAsJsonArray())
        {
            list.add(resolveJsonElement(element));
        }

        return list;
    }
}
