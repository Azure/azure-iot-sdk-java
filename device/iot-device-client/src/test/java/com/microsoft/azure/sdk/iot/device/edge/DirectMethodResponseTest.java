/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.edge;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonNull;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static junit.framework.TestCase.assertEquals;

public class DirectMethodResponseTest
{
    @Test
    public void constructorParsesJsonAndGettersWork()
    {
        //construct payload
        String string = "somePayload";

        List<String> list = asList("element1", "element2", "element3");

        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");

        //act
        DirectMethodResponse resultForNull = new DirectMethodResponse(0, new GsonBuilder().create().toJsonTree(null));
        DirectMethodResponse resultForString = new DirectMethodResponse(1, new GsonBuilder().create().toJsonTree(string));
        DirectMethodResponse resultForList = new DirectMethodResponse(2, new GsonBuilder().create().toJsonTree(list));
        DirectMethodResponse resultForMap = new DirectMethodResponse(3, new GsonBuilder().create().toJsonTree(map));

        //assert
        assertEquals(new JsonNull(), resultForNull.getPayloadAsJsonElement());
        assertEquals(0, resultForNull.getStatus());

        assertEquals(string, resultForString.getPayload(String.class));
        assertEquals(1, resultForString.getStatus());

        assertEquals(list, resultForList.getPayload(List.class));
        assertEquals(2, resultForList.getStatus());

        assertEquals(map, resultForMap.getPayload(Map.class));
        assertEquals(3, resultForMap.getStatus());
    }
}
