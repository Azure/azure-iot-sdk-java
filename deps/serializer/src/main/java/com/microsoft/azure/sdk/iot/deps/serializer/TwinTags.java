// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Twin tags representation
 */
public class TwinTags extends HashMap<String, HashMap<String, String>> {

    private static final Gson gson = new GsonBuilder().create();

    public TwinTags()
    {
        super();
    }

    public TwinTags(String tag, HashMap<String, Object> tagProperties) throws IllegalArgumentException
    {
        super();
        addTag(tag, tagProperties);
    }

    public void addTag(String tag, HashMap<String, Object> tagProperties) throws IllegalArgumentException
    {
        for (Map.Entry<String,Object> e : tagProperties.entrySet()) {
            addTag(tag, e.getKey(), e.getValue());
        }
    }

    public void addTag(String tag, String key, Object value) throws IllegalArgumentException
    {
        HashMap<String, String> property = new HashMap<>();
        property.put(key, value.toString());
        super.put(tag, property);
    }

    public String GetTagProperty(String tag, String key) throws IllegalArgumentException
    {
        HashMap<String, String> property = super.get(tag);
        return property.get(key);
    }

    public String toJson()
    {
        return gson.toJson(this);
    }

    public void fromJson(String json)
    {
        TwinTags newValues = gson.fromJson(json, TwinTags.class);
        copy(newValues);
    }

    private void copy(TwinTags newValues)
    {

    }

}
