// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test helpers.
 */
public class Helpers
{
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
        if(expected == null)
        {
            assertNull("Expected null map, received " + actual, actual);
        }
        else
        {
            assertEquals(expected.size(), actual.size());

            for (Map.Entry entry : expected.entrySet())
            {
                k key = (k)entry.getKey();
                v actualValue = actual.get(key);
                v expectedValue = expected.get(key);
                if(expectedValue == null)
                {
                    assertNull(actualValue);
                }
                else if(expectedValue instanceof Map)
                {
                    if(actualValue instanceof Map)
                    {
                        assertMap((Map<k, v>)actualValue, (Map<k, v>)expectedValue);
                    }
                    else
                    {
                        assert true;
                    }
                }
                else if(expectedValue instanceof ArrayList)
                {
                    assertTrue("Map failed: " + actualValue + " != " + expectedValue, actualValue.toString().equals(expectedValue.toString()));
                }
                else
                {
                    assertTrue("Map failed: <" + actualValue + "> != <" + expectedValue + ">", actualValue.equals(expectedValue));
                }
            }
        }
    }

    /**
     * Test helper, will throw if the actual json do not fits the expected json. Better than compare the String,
     *              because field positions can be different.
     *
     * @param actualJson
     * @param expectedJson
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
                assertMap((Map<String, Object>) actual, (Map<String, Object>)expected);
            }
            else
            {
                assertEquals(actual, expected);
            }
        }
    }

}
