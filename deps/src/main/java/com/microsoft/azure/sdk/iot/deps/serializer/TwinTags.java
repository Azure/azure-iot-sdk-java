// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * INNER TWINPARSER CLASS
 *
 * Twin tags representation
 * @deprecated As of release 0.4.0, replaced by {@link com.microsoft.azure.sdk.iot.deps.twin.TwinCollection}
 */
@Deprecated
public class TwinTags
{
    private static final String VERSION_TAG = "$version";
    private static final String METADATA_TAG = "$metadata";
    private static final int MAX_PROPERTY_LEVEL = 5;
    private static final int MAX_METADATA_LEVEL = MAX_PROPERTY_LEVEL + 2;

    private Map<String, Object> tags;

    protected TwinTags()
    {
        this.tags = new HashMap<>();
    }

    protected synchronized JsonElement update(Map<String, Object> tagsMap) throws IllegalArgumentException
    {
        JsonElement innerDiff = updateFromMap(tagsMap, tags);
        if((innerDiff == null) || (innerDiff.toString().equals("{}")))
        {
            return null;
        }
        return innerDiff;
    }

    private synchronized JsonElement updateFromMap(Map<String, Object> newMap, Map<String, Object> oldMap) throws IllegalArgumentException
    {
        JsonObject diffJson = new JsonObject();

        /* Codes_SRS_TWINPARSER_21_103: [The updateTags shall add all provided tags to the collection.] */
        for (Map.Entry<String,Object> entry : newMap.entrySet())
        {
            String key = entry.getKey();
            Object newValue = entry.getValue();
            Object oldValue = oldMap.get(key);

            if(newValue == null)
            {
                oldMap.remove(key);
                diffJson.add(key, null);
            }
            else
            {
                if(!oldMap.containsKey(key))
                {
                    if(newValue instanceof Map)
                    {
                        oldMap.put(key, new HashMap<String, Object>());
                        diffJson.add(key, updateFromMap((Map<String, Object>)newValue, (Map<String, Object>)oldMap.get(key)));
                    }
                    else
                    {
                        oldMap.put(key, newValue);
                        addProperty(diffJson, key, newValue);
                    }
                }
                else
                {
                    if(newValue instanceof Map)
                    {
                        if(!(oldValue instanceof Map))
                        {
                            oldMap.put(key, new HashMap<String, Object>());
                        }
                        JsonElement innerDiff = updateFromMap((Map<String, Object>)newValue, (Map<String, Object>)oldMap.get(key));
                        if((innerDiff != null) && (!innerDiff.toString().equals("{}")))
                        {
                            diffJson.add(key, innerDiff);
                        }
                    }
                    else if(!newValue.equals(oldValue))
                    {
                        oldMap.put(key, newValue);
                        addProperty(diffJson, key, newValue);
                    }
                }
            }
        }

        return (JsonElement)diffJson;
    }

    private void addProperty(JsonObject diffJson, String key, Object newValue) throws IllegalArgumentException
    {
        if(newValue instanceof Number)
        {
            diffJson.addProperty(key, (Number) newValue);
        }
        else if(newValue instanceof Boolean)
        {
            diffJson.addProperty(key, (Boolean) newValue);
        }
        else if(newValue instanceof Character)
        {
            diffJson.addProperty(key, (Character) newValue);
        }
        else if(newValue.getClass().isLocalClass() || newValue.getClass().isArray())
        {
            throw new IllegalArgumentException("Type not supported");
        }
        else
        {
            diffJson.addProperty(key, newValue.toString());
        }

    }

    protected synchronized Map<String, Object> getMap()
    {
        /* Codes_SRS_TWINPARSER_21_052: [The getTagsMap shall return a map with all tags in the collection.] */
        return this.tags;
    }

    protected String toJson()
    {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(tags);
    }

    protected JsonElement toJsonElement()
    {
        Gson gson = new GsonBuilder().create();
        /* Codes_SRS_TWINPARSER_21_017: [The toJsonElement shall return a JsonElement with information in the TwinParser using json format.] */
        return gson.toJsonTree(tags);
    }

    protected void update(Map<String, Object> tagsMap, TwinChangedCallback onTagsCallback)
    {
        Map<String, Object> diffMap = updateFromJson(tagsMap, tags);

        if(diffMap != null && onTagsCallback != null)
        {
            onTagsCallback.execute(diffMap);
        }
    }

    protected void validate(Map<String, Object> tagsMap)
    {
        if(tagsMap == null)
        {
            throw new IllegalArgumentException("property cannot be null");
        }
        for (Map.Entry<String, Object> entry : tagsMap.entrySet())
        {
            if(entry.getKey().equals(METADATA_TAG))
            {
                if(entry.getValue() instanceof Map)
                {
                    ParserUtility.validateMap((Map<String, Object>)entry.getValue(), MAX_METADATA_LEVEL, true);
                }
            }
            else if(!entry.getKey().equals(VERSION_TAG))
            {
                if(entry.getValue() instanceof Map)
                {
                    ParserUtility.validateMap((Map<String, Object>)entry.getValue(), MAX_PROPERTY_LEVEL, false);
                }
            }
        }
    }

    private synchronized Map<String, Object> updateFromJson(Map<String, Object> newMap, Map<String, Object> oldMap) throws IllegalArgumentException
    {
        Map<String, Object> diffMap = new HashMap<>();

        for (Map.Entry<String,Object> entry : newMap.entrySet())
        {
            String key = entry.getKey();
            Object newValue = entry.getValue();
            Object oldValue = oldMap.get(key);

            if(!oldMap.containsKey(key))
            {
                if(newValue != null)
                {
                    oldMap.put(key, newValue);
                    diffMap.put(key, newValue);
                }
            }
            else
            {
                if(newValue == null)
                {
                    oldMap.remove(key);
                    diffMap.put(key, null);
                }
                else if(newValue instanceof Map)
                {
                    if(oldValue instanceof Map)
                    {
                        Map<String, Object> innerDiffMap = updateFromJson((Map<String, Object>) newValue, (Map<String, Object>) oldValue);
                        if (innerDiffMap != null)
                        {
                            diffMap.put(key, innerDiffMap);
                        }
                    }
                    else
                    {
                        oldMap.put(key, newValue);
                        diffMap.put(key, newValue);
                    }
                }
                else
                {
                    oldMap.put(key, newValue);
                    diffMap.put(key, newValue);
                }
            }
        }

        if(diffMap.size() == 0)
        {
            diffMap = null;
        }

        return diffMap;
    }
}
