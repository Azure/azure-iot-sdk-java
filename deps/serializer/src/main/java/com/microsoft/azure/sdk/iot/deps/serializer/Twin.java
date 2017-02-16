// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Twin Representation including the twin database and Json serializer and deserializer.
 */
public class Twin
{

    private TwinPropertiesChangeCallback onDesiredCallback = null;
    private TwinPropertiesChangeCallback onReportedCallback = null;

    private static final String PROPERTIES_TAG = "properties";
    private static final String DESIRED_TAG = "desired";
    private static final String REPORTED_TAG = "reported";

    protected TwinProperties properties = new TwinProperties();

    /**
     * CONSTRUCTOR
     * Create a Twin instance with default values.
     *      set OnDesiredCallback as null
     *      set OnReportedCallback as null
     *
     */
    public Twin()
    {
        /* Codes_SRS_TWIN_21_001: [The constructor shall create an instance of the properties.] */
        /* Codes_SRS_TWIN_21_002: [The constructor shall set OnDesiredCallback as null.] */
        /* Codes_SRS_TWIN_21_003: [The constructor shall set OnReportedCallback as null.] */
    }

    /**
     * CONSTRUCTOR
     * Create a Twin instance with default values.
     *      set OnReportedCallback as null
     *
     * @param onDesiredCallback - Callback function to report changes on the `Desired` database.
     */
    public Twin(TwinPropertiesChangeCallback onDesiredCallback)
    {
        /* Codes_SRS_TWIN_21_005: [The constructor shall call the standard constructor.] */
        /* Codes_SRS_TWIN_21_007: [The constructor shall set OnReportedCallback as null.] */
        this();

        /* Codes_SRS_TWIN_21_006: [The constructor shall set OnDesiredCallback with the provided Callback function.] */
        setDesiredCallback(onDesiredCallback);
    }

    /**
     * CONSTRUCTOR
     * Create a Twin instance with default values.
     *
     * @param onDesiredCallback - Callback function to report changes on the `Desired` database.
     * @param onReportedCallback - Callback function to report changes on the `Reported` database.
     */
    public Twin(TwinPropertiesChangeCallback onDesiredCallback, TwinPropertiesChangeCallback onReportedCallback)
    {
        /* Codes_SRS_TWIN_21_009: [The constructor shall call the standard constructor.] */
        this();

        /* Codes_SRS_TWIN_21_010: [The constructor shall set OnDesiredCallback with the provided Callback function.] */
        /* Codes_SRS_TWIN_21_011: [The constructor shall set OnReportedCallback with the provided Callback function.] */
        setDesiredCallback(onDesiredCallback);
        setReportedCallback(onReportedCallback);
    }

    /**
     * Set the callback function to report changes on the `Desired` database when `Twin`
     * receives a new json.
     *
     * @param onDesiredCallback - Callback function to report changes on the `Desired` database.
     */
    public void setDesiredCallback(TwinPropertiesChangeCallback onDesiredCallback)
    {
        /* Codes_SRS_TWIN_21_013: [The setDesiredCallback shall set OnDesiredCallback with the provided Callback function.] */
        /* Codes_SRS_TWIN_21_053: [The setDesiredCallback shall keep only one instance of the callback.] */
        /* Codes_SRS_TWIN_21_054: [If the OnDesiredCallback is already set, the setDesiredCallback shall replace the first one.] */
        /* Codes_SRS_TWIN_21_055: [If callback is null, the setDesiredCallback will set the OnDesiredCallback as null.] */
        this.onDesiredCallback = onDesiredCallback;
    }

    /**
     * Set the callback function to report changes on the `Reported` database when `Twin`
     * receives a new json.
     *
     * @param onReportedCallback - Callback function to report changes on the `Reported` database.
     */
    public void setReportedCallback(TwinPropertiesChangeCallback onReportedCallback)
    {
        /* Codes_SRS_TWIN_21_014: [The setReportedCallback shall set OnReportedCallback with the provided Callback function.] */
        /* Codes_SRS_TWIN_21_056: [The setReportedCallback shall keep only one instance of the callback.] */
        /* Codes_SRS_TWIN_21_057: [If the OnReportedCallback is already set, the setReportedCallback shall replace the first one.] */
        /* Codes_SRS_TWIN_21_058: [If callback is null, the setReportedCallback will set the OnReportedCallback as null.] */
        this.onReportedCallback = onReportedCallback;
    }

    /**
     * Create a String with a json content that represents all the information in the Twin class and innerClasses.
     *
     * @return String with the json content.
     */
    public String toJson()
    {
        /* Codes_SRS_TWIN_21_015: [The toJson shall create a String with information in the Twin using json format.] */
        /* Codes_SRS_TWIN_21_016: [The toJson shall not include null fields.] */
        return toJsonElement().toString();
    }

    /**
     * Create a JsonElement that represents all the information in the Twin class and innerClasses.
     *
     * @return JsonElement with the Twin information.
     */
    public JsonElement toJsonElement()
    {
        /* Codes_SRS_TWIN_21_017: [The toJsonElement shall return a JsonElement with information in the Twin using json format.] */
        JsonObject twinJson = new JsonObject();

        /* Codes_SRS_TWIN_21_018: [The toJsonElement shall not include null fields.] */
        /* Codes_SRS_TWIN_21_086: [**The toJsonElement shall include the `properties` in the json even if it has no content.] */
        /* Codes_SRS_TWIN_21_087: [**The toJsonElement shall include the `desired` property in the json even if it has no content.] */
        /* Codes_SRS_TWIN_21_088: [**The toJsonElement shall include the `reported` property in the json even if it has no content.] */
        twinJson.add(PROPERTIES_TAG, properties.toJsonElement());

        return (JsonElement) twinJson;
    }

    /**
     * Enable metadata report in the Json.
     *
     */
    public void enableMetadata()
    {
        /* Codes_SRS_TWIN_21_020: [The enableMetadata shall enable report metadata in Json for the Desired and for the Reported Properties.] */
        properties.enableDesiredMetadata();
        properties.enableReportedMetadata();
    }

    /**
     * Update the `desired` properties information in the database, and return a string with a json that contains a
     * collection of added properties, or properties with new value.
     *
     * @param propertyMap - Map of `desired` property to change the database.
     * @return Json with added or changed properties
     * @throws IllegalArgumentException This exception is thrown if the properties in the map do not fits the requirements.
     */
    public String updateDesiredProperty(Map<String, Object> propertyMap) throws IllegalArgumentException
    {
        String json;
        if(propertyMap != null)
        {
            /* Codes_SRS_TWIN_21_021: [The updateDesiredProperty shall add all provided properties to the Desired property.] */
            /* Codes_SRS_TWIN_21_059: [The updateDesiredProperty shall only change properties in the map, keep the others as is.] */
            /* Codes_SRS_TWIN_21_061: [All `key` and `value` in property shall be case sensitive.] */
            /* Codes_SRS_TWIN_21_073: [If any `key` is null, the updateDesiredProperty shall throw IllegalArgumentException.] */
            /* Codes_SRS_TWIN_21_074: [If any `key` is empty, the updateDesiredProperty shall throw IllegalArgumentException.] */
            /* Codes_SRS_TWIN_21_075: [If any `key` is more than 128 characters long, the updateDesiredProperty shall throw IllegalArgumentException.] */
            /* Codes_SRS_TWIN_21_076: [If any `key` has an illegal character, the updateDesiredProperty shall throw IllegalArgumentException.] */
            /* Codes_SRS_TWIN_21_077: [If any `key` already exists, the updateDesiredProperty shall replace the existed value by the new one.] */
            /* Codes_SRS_TWIN_21_078: [If any `value` is null, the updateDesiredProperty shall store it but do not report on Json.] */
            JsonElement updatedElements = properties.updateDesired(propertyMap);

            if (updatedElements == null)
            {
                /* Codes_SRS_TWIN_21_023: [If the provided `property` map is null, the updateDesiredProperty shall not change the database and return null.] */
                /* Codes_SRS_TWIN_21_024: [If no Desired property changed its value, the updateDesiredProperty shall return null.] */
                /* Codes_SRS_TWIN_21_063: [If the provided `property` map is empty, the updateDesiredProperty shall not change the database and return null.] */
                json = null;
            }
            else
            {
                /* Codes_SRS_TWIN_21_022: [The updateDesiredProperty shall return a string with json representing the desired properties with changes.] */
                json = updatedElements.toString();
            }
        }
        else
        {
            /* Codes_SRS_TWIN_21_023: [If the provided `property` map is null, the updateDesiredProperty shall not change the database and return null.] */
            json = null;
        }

        return json;
    }

    /**
     * Update the `reported` properties information in the database, and return a string with a json that contains a
     * collection of added properties, or properties with new value.
     *
     * @param propertyMap - Map of `reported` property to change the database.
     * @return Json with added or changed properties
     * @throws IllegalArgumentException This exception is thrown if the properties in the map do not fits the requirements.
     */
    public String updateReportedProperty(Map<String, Object> propertyMap) throws IllegalArgumentException
    {
        String json;
        if(propertyMap != null)
        {
            /* Codes_SRS_TWIN_21_025: [The updateReportedProperty shall add all provided properties to the Reported property.] */
            /* Codes_SRS_TWIN_21_060: [The updateReportedProperty shall only change properties in the map, keep the others as is.] */
            /* Codes_SRS_TWIN_21_062: [All `key` and `value` in property shall be case sensitive.] */
            /* Codes_SRS_TWIN_21_079: [If any `key` is null, the updateReportedProperty shall throw IllegalArgumentException.] */
            /* Codes_SRS_TWIN_21_080: [If any `key` is empty, the updateReportedProperty shall throw IllegalArgumentException.] */
            /* Codes_SRS_TWIN_21_081: [If any `key` is more than 128 characters long, the updateReportedProperty shall throw IllegalArgumentException.] */
            /* Codes_SRS_TWIN_21_082: [If any `key` has an illegal character, the updateReportedProperty shall throw IllegalArgumentException.] */
            /* Codes_SRS_TWIN_21_083: [If any `key` already exists, the updateReportedProperty shall replace the existed value by the new one.] */
            /* Codes_SRS_TWIN_21_084: [If any `value` is null, the updateReportedProperty shall store it but do not report on Json.] */
            JsonElement updatedElements = properties.updateReported(propertyMap);

            if (updatedElements == null)
            {
                /* Codes_SRS_TWIN_21_027: [If the provided `property` map is null, the updateReportedProperty shall not change the database and return null.] */
                /* Codes_SRS_TWIN_21_028: [If no Reported property changed its value, the updateReportedProperty shall return null.] */
                json = null;
            }
            else
            {
                /* Codes_SRS_TWIN_21_026: [The updateReportedProperty shall return a string with json representing the Reported properties with changes.] */
                json = updatedElements.toString();
            }
        }
        else
        {
            /* Codes_SRS_TWIN_21_027: [If the provided `property` map is null, the updateReportedProperty shall not change the database and return null.] */
            json = null;
        }

        return json;
    }
    
    /**
     * Update the properties information in the database, using the information parsed from the provided json.
     * It will fire a callback if any property was added, excluded, or had its value updated.
     *
     * @param json - Json with property to change the database.
     * @throws IllegalArgumentException This exception is thrown if the Json is not well formed.
     */
    public void updateTwin(String json) throws IllegalArgumentException
    {
        /* Codes_SRS_TWIN_21_071: [If the provided json is empty, the updateTwin shall not change the database and not call the OnDesiredCallback or the OnReportedCallback.] */
        /* Codes_SRS_TWIN_21_072: [If the provided json is null, the updateTwin shall not change the database and not call the OnDesiredCallback or the OnReportedCallback.] */
        if((json != null) && (!json.isEmpty()))
        {
            Map<String, Object> jsonTree;
            try
            {
                /* Codes_SRS_TWIN_21_097: [If the provided json have any duplicated `properties`, the updateTwin shall throw IllegalArgumentException.] */
                /* Codes_SRS_TWIN_21_098: [If the provided json is properties only and contains duplicated `desired` or `reported`, the updateTwin shall throws IllegalArgumentException.] */
                /* Codes_SRS_TWIN_21_094: [If the provided json have any duplicated `key`, the updateTwin shall use the content of the last one in the String.] */
                Gson gson = new GsonBuilder().disableInnerClassSerialization().create();
                jsonTree = (Map<String, Object>) gson.fromJson(json, HashMap.class);
            }
            catch (Exception e)
            {
                /* Codes_SRS_TWIN_21_043: [If the provided json is not valid, the updateTwin shall throws IllegalArgumentException.] */
                throw new IllegalArgumentException("Malformed Json: " + e);
            }
            boolean propertiesLevel = false;
            for (Map.Entry<String, Object> entry : jsonTree.entrySet())
            {
                if (entry.getKey().equals(PROPERTIES_TAG))
                {
                    /* Codes_SRS_TWIN_21_039: [The updateTwin shall fill the fields the properties in the Twin class with the keys and values provided in the json string.] */
                    /* Codes_SRS_TWIN_21_040: [The updateTwin shall not change fields that is not reported in the json string.] */
                    /* Codes_SRS_TWIN_21_041: [The updateTwin shall create a list with all properties that was updated (new key or value) by the new json.] */
                    /* Codes_SRS_TWIN_21_042: [If a valid key has a null value, the updateTwin shall delete this property.] */
                    /* Codes_SRS_TWIN_21_044: [If OnDesiredCallback was provided, the updateTwin shall create a new map with a copy of all pars key values updated by the json in the Desired property, and OnDesiredCallback passing this map as parameter.] */
                    /* Codes_SRS_TWIN_21_045: [If OnReportedCallback was provided, the updateTwin shall create a new map with a copy of all pars key values updated by the json in the Reported property, and OnReportedCallback passing this map as parameter.] */
                    /* Codes_SRS_TWIN_21_046: [If OnDesiredCallback was not provided, the updateTwin shall not do anything with the list of updated desired properties.] */
                    /* Codes_SRS_TWIN_21_047: [If OnReportedCallback was not provided, the updateTwin shall not do anything with the list of updated reported properties.] */
                    /* Codes_SRS_TWIN_21_069: [If there is no change in the Desired property, the updateTwin shall not change the reported database and not call the OnReportedCallback.] */
                    /* Codes_SRS_TWIN_21_070: [If there is no change in the Reported property, the updateTwin shall not change the reported database and not call the OnReportedCallback.] */
                    properties.update((Map<String, Object>) entry.getValue(), onDesiredCallback, onReportedCallback);
                    propertiesLevel = true;
                }
                else if ((entry.getKey().equals(DESIRED_TAG)) || (entry.getKey().equals(REPORTED_TAG)))
                {
                    /* Codes_SRS_TWIN_21_089: [If the provided json contains `desired` or `reported` in its first level, the updateTwin shall parser the json as properties only.] */
                    if (propertiesLevel == false)
                    {
                        /* Codes_SRS_TWIN_21_090: [If the provided json is properties only and contains other tag different than `desired` or `reported`, the updateTwin shall throws IllegalArgumentException.] */
                        properties.update(jsonTree, onDesiredCallback, onReportedCallback);
                    }
                    else
                    {
                        /* Codes_SRS_TWIN_21_091: [If the provided json is NOT properties only and contains `desired` or `reported` in its first level, the updateTwin shall throws IllegalArgumentException.] */
                        throw new IllegalArgumentException("Invalid Entry");
                    }
                    break;
                }
                else
                {
                    throw new IllegalArgumentException("Invalid Entry");
                }
            }
        }
    }

    /**
     * Update the `desired` properties information in the database, using the information parsed from the provided json.
     * It will fire a callback if any property was added, excluded, or had its value updated.
     *
     * @param json - Json with `desired` property to change the database.
     * @throws IllegalArgumentException This exception is thrown if the Json is not well formed.
     */
    public void updateDesiredProperty(String json) throws IllegalArgumentException
    {
        /* Codes_SRS_TWIN_21_065: [If the provided json is empty, the updateDesiredProperty shall not change the database and not call the OnDesiredCallback.] */
        /* Codes_SRS_TWIN_21_066: [If the provided json is null, the updateDesiredProperty shall not change the database and not call the OnDesiredCallback.] */
        if((json != null) && (!json.isEmpty()))
        {
            /* Codes_SRS_TWIN_21_029: [The updateDesiredProperty shall update the Desired property using the information provided in the json.] */
            /* Codes_SRS_TWIN_21_030: [The updateDesiredProperty shall generate a map with all pairs key value that had its content changed.] */
            /* Codes_SRS_TWIN_21_031: [The updateDesiredProperty shall send the map with all changed pairs to the upper layer calling onDesiredCallback (TwinPropertiesChangeCallback).] */
            /* Codes_SRS_TWIN_21_032: [If the OnDesiredCallback is set as null, the updateDesiredProperty shall discard the map with the changed pairs.] */
            /* Codes_SRS_TWIN_21_033: [If there is no change in the Desired property, the updateDesiredProperty shall not change the database and not call the OnDesiredCallback.] */
            /* Codes_SRS_TWIN_21_092: [If the provided json is not valid, the updateDesiredProperty shall throws IllegalArgumentException.] */
            try
            {
                properties.updateDesired(json, onDesiredCallback);
            }
            catch (com.google.gson.JsonSyntaxException e)
            {
                /* Codes_SRS_TWIN_21_096: [If the provided json have any duplicated `key`, the updateDesiredProperty shall throws IllegalArgumentException.] */
                throw new IllegalArgumentException("Malformed json: " + e);
            }
        }
    }

    /**
     * Update the `reported` properties information in the database, using the information parsed from the provided json.
     * It will fire a callback if any property was added, excluded, or had its value updated.
     *
     * @param json - Json with `reported` property to change the database.
     * @throws IllegalArgumentException This exception is thrown if the Json is not well formed.
     */
    public void updateReportedProperty(String json) throws IllegalArgumentException
    {
        /* Codes_SRS_TWIN_21_067: [If the provided json is empty, the updateReportedProperty shall not change the database and not call the OnReportedCallback.] */
        /* Codes_SRS_TWIN_21_068: [If the provided json is null, the updateReportedProperty shall not change the database and not call the OnReportedCallback.] */
        if((json != null) && (!json.isEmpty()))
        {
            /* Codes_SRS_TWIN_21_034: [The updateReportedProperty shall update the Reported property using the information provided in the json.] */
            /* Codes_SRS_TWIN_21_035: [The updateReportedProperty shall generate a map with all pairs key value that had its content changed.] */
            /* Codes_SRS_TWIN_21_036: [The updateReportedProperty shall send the map with all changed pairs to the upper layer calling onReportedCallback (TwinPropertiesChangeCallback).] */
            /* Codes_SRS_TWIN_21_037: [If the OnReportedCallback is set as null, the updateReportedProperty shall discard the map with the changed pairs.] */
            /* Codes_SRS_TWIN_21_038: [If there is no change in the Reported property, the updateReportedProperty shall not change the database and not call the OnReportedCallback.] */
            /* Codes_SRS_TWIN_21_093: [If the provided json is not valid, the updateReportedProperty shall throws IllegalArgumentException.] */
            try
            {
                properties.updateReported(json, onReportedCallback);
            }
            catch (com.google.gson.JsonSyntaxException e)
            {
                /* Codes_SRS_TWIN_21_095: [If the provided json have any duplicated `key`, the updateReportedProperty shall throws IllegalArgumentException.] */
                throw new IllegalArgumentException("Malformed json: " + e);
            }
        }
    }

    /**
     * Return the `desired` property version.
     *
     * @return Integer that contains the `desired` property version (it can be null).
     */
    public Integer getDesiredPropertyVersion()
    {
        /* Codes_SRS_TWIN_21_048: [The getDesiredPropertyVersion shall return the desired property version.] */
        return properties.getDesiredVersion();
    }

    /**
     * Return the `reported` property version.
     *
     * @return Integer that contains the `reported` property version (it can be null).
     */
    public Integer getReportedPropertyVersion()
    {
        /* Codes_SRS_TWIN_21_049: [The getReportedPropertyVersion shall return the reported property version.] */
        return properties.getReportedVersion();
    }

    /**
     * Return a map with all `desired` properties in the database.
     *
     * @return A map with all `desired` properties in the database (it can be null).
     */
    public Map<String, Object> getDesiredPropertyMap()
    {
        /* Codes_SRS_TWIN_21_050: [The getDesiredPropertyMap shall return a map with all desired property key value pairs.] */
        return properties.getDesiredPropertyMap();
    }

    /**
     * Return a map with all `reported` properties in the database.
     *
     * @return A map with all `reported` properties in the database (it can be null).
     */
    public Map<String, Object> getReportedPropertyMap()
    {
        /* Codes_SRS_TWIN_21_051: [The getReportedPropertyMap shall return a map with all reported property key value pairs.] */
        return properties.getReportedPropertyMap();
    }
}
