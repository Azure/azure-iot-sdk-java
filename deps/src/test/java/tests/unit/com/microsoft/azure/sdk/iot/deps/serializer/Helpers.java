// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.*;

/**
 * Test helpers.
 */
public class Helpers
{
    private static final String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String DATEFORMAT_NO_MS = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String TIMEZONE = "UTC";
    private static final long MAX_TIME_ERROR_IN_MILLISECONDS = 100L;

    private static final int NO_MILLISECONDS_IN_DATE = 0;
    private static final int DATE_AND_TIME_IN_DATE = 0;
    private static final int MILLISECONDS_IN_DATE = 1;
    private static final int EXPECTED_PARTS_IN_DATE = 2;
    private static final int MAX_MILLISECONDS_LENGTH_IN_DATE = 3;
    private static final double MILLISECONDS_NUMERIC_BASE = 10;
    private static final String MILLISECONDS_REGEX = "[.,Z]";

    /**
     * Test helper, will throw if the actual map do not fits the expected one. This helper will
     *              test maps and sub-maps.
     *
     * @param actual is the resulted map.
     * @param expected is the expected result map.
     * @param <k> it the key type, normally a String.
     * @param <v> is the value type, normally an Object.
     */
    protected static <k,v> void assertMap(Map<k, v> actual, Map<k, v> expected)
    {
        assertMap(actual, expected, null);
    }

    /**
     * Test helper, will throw if the actual map do not fits the expected one. This helper will
     *              test maps and sub-maps.
     *
     * @param actual is the resulted map.
     * @param expected is the expected result map.
     * @param <k> it the key type, normally a String.
     * @param <v> is the value type, normally an Object.
     * @param message is the string with the error message.
     */
    protected static <k,v> void assertMap(Map<k, v> actual, Map<k, v> expected, String message)
    {
        if(expected == null)
        {
            assertNull((message==null?"Expected null map, received " + actual : message), actual);
        }
        else
        {
            if(message == null)
            {
                assertEquals(expected.size(), actual.size());
            }
            else
            {
                assertEquals(message, expected.size(), actual.size());
            }

            for (Map.Entry entry : expected.entrySet())
            {
                k key = (k)entry.getKey();
                v actualValue = actual.get(key);
                v expectedValue = expected.get(key);
                if(expectedValue == null)
                {
                    if(message == null)
                    {
                        assertNull(actualValue);
                    }
                    else
                    {
                        assertNull(message, actualValue);
                    }
                }
                else if(actualValue == null)
                {
                    if(message == null)
                    {
                        assertTrue("Expected key:" + key + " does not exist in Actual Map", false);
                    }
                    else
                    {
                        assertTrue(message, false);
                    }
                }
                else if(expectedValue instanceof Map)
                {
                    if(actualValue instanceof Map)
                    {
                        assertMap((Map<k, v>)actualValue, (Map<k, v>)expectedValue, message);
                    }
                    else
                    {
                        if(message == null)
                        {
                            assertTrue("Map contains invalid Object", false);
                        }
                        else
                        {
                            assertTrue(message, false);
                        }
                    }
                }
                else if(expectedValue instanceof ArrayList)
                {
                    if(message == null)
                    {
                        assertTrue("Map failed on " + key + ": " + actualValue + " != " + expectedValue, actualValue.toString().equals(expectedValue.toString()));
                    }
                    else
                    {
                        assertTrue(message, actualValue.toString().equals(expectedValue.toString()));
                    }
                }
                else
                {
                    if(message == null)
                    {
                        assertEquals("Map failed on " + key + ": <" + actualValue + "> != <" + expectedValue + ">", actualValue.toString(), expectedValue.toString());
                    }
                    else
                    {
                        assertTrue(message, actualValue.equals(expectedValue));
                    }
                }
            }
        }
    }

    /**
     * Test helper, will throw if the actual json do not fits the expected json. Better than compare the String,
     *              because field positions can be different.
     *
     * @param actualJson is a String with a json to compared
     * @param expectedJson is a String with a valid json
     */
    protected static void assertJson(String actualJson, String expectedJson)
    {
        Gson gson = new GsonBuilder().create();

        if(expectedJson == null)
        {
            assertNull(actualJson);
        }
        else
        {
            Object actual = gson.fromJson(actualJson, Object.class);
            Object expected = gson.fromJson(expectedJson, Object.class);

            if(actual instanceof Map)
            {
                assertMap((Map<String, Object>) actual, (Map<String, Object>)expected, "\r\nExpected :" + expectedJson + "\r\nActual   :" + actualJson);
            }
            else
            {
                assertEquals(actual, expected);
            }
        }
    }

    /**
     * Test helper, will throw if the string contains invalid data and time, or the
     *              difference between data and time for both strings is bigger than 100 milliseconds.
     * @param dt1Str is the first string with data and time
     * @param dt2Str is the second string with data and time.
     */
    protected static void assertDateWithError(String dt1Str, String dt2Str)
    {
        Date dt1 = null;
        Date dt2 = null;

        try
        {
            dt1 = getDateTimeUtc(dt1Str);
            dt2 = getDateTimeUtc(dt2Str);
        }
        catch (ParseException e)
        {
            assert(true);
        }

        long error = Math.abs(dt1.getTime()-dt2.getTime());

        assertThat(error, lessThanOrEqualTo(MAX_TIME_ERROR_IN_MILLISECONDS));
    }

    /**
     * Test helper, will throw if the string contains invalid data and time, or the
     *              difference between data and time and the data and time in the string
     *              is bigger than 100 milliseconds.
     * @param dt1 is the data and time
     * @param dt2Str is the string with data and time.
     */
    protected static void assertDateWithError(Date dt1, String dt2Str)
    {
        Date dt2 = null;

        try
        {
            dt2 = getDateTimeUtc(dt2Str);
        }
        catch (ParseException e)
        {
            assert(true);
        }

        long error = Math.abs(dt1.getTime()-dt2.getTime());

        assertThat(error, lessThanOrEqualTo(MAX_TIME_ERROR_IN_MILLISECONDS));
    }

    /**
     * Test helper, will throw if the string contains invalid data and time, or the
     *              difference between the data and time in the string and the actual
     *              data and time is bigger than 100 milliseconds.
     * @param dt1Str is the string with data and time
     */
    protected static void assertNowWithError(String dt1Str)
    {
        Date dt1 = null;
        Date dt2 = new Date();

        try
        {
            dt1 = getDateTimeUtc(dt1Str);
        }
        catch (ParseException e)
        {
            assert(true);
        }

        long error = Math.abs(dt1.getTime()-dt2.getTime());

        assertThat(error, lessThanOrEqualTo(MAX_TIME_ERROR_IN_MILLISECONDS));
    }

    /**
     * Return a string with the provided date and time in the UTC format.
     *
     * @param date is the date and time to be format in the string.
     * @return String with the date and time in UTC format.
     */
    protected static String formatUTC(Date date)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
        return dateFormat.format(date);

    }

    /**
     * Asserts when list contents are not equal
     * @param expected expected list to verify
     * @param test  list to test
     */
    protected static void assertListEquals(List expected, List test)
    {
        assertNotNull(expected);
        assertNotNull(test);
        assertTrue(expected.size() == test.size());
        for(Object o : expected)
        {
            assertTrue(test.contains(o));
        }
    }

    private static Date getDateTimeUtc(String dataTime) throws ParseException
    {
        Date dateTimeUtc;
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT_NO_MS);
        dateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE));

        if((dataTime == null) || dataTime.isEmpty() || (dataTime.charAt(dataTime.length()-1) != 'Z'))
        {
            throw new ParseException("date is null, empty, or invalid", 0);
        }

        try
        {
            String[] splitDateTime = dataTime.split(MILLISECONDS_REGEX);
            int milliseconds;
            if(splitDateTime.length > EXPECTED_PARTS_IN_DATE)
            {
                throw new ParseException("invalid time", 0);
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
                milliseconds = NO_MILLISECONDS_IN_DATE;
            }
            dateTimeUtc =  new Date(dateFormat.parse(splitDateTime[DATE_AND_TIME_IN_DATE]).getTime() + milliseconds);
        }
        catch (ParseException e)
        {
            throw new ParseException("invalid time", 0);
        }

        return dateTimeUtc;
    }

}
