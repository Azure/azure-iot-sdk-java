// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.internal.LinkedTreeMap;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * INNER TWINPARSER CLASS
 *
 * TwinProperty is a representation of the device twin property collection.
 * It can represent `Desired` property as well `Reported` property.
 *
 */
public class TwinProperty
{

    private static final String VERSION_TAG = "$version";
    private static final String METADATA_TAG = "$metadata";
    private static final String LAST_UPDATE_TAG = "$lastUpdated";
    private static final String LAST_UPDATE_VERSION_TAG = "$lastUpdatedVersion";

    private Object lock = new Object();

    private class Property
    {
        private Object value;
        private TwinMetadata metadata;
        private Property(Object val, Integer propertyVersion)
        {
            this.value = val;
            this.metadata = new TwinMetadata(propertyVersion);
        }
    }

    private ConcurrentMap<String, Property> property = new ConcurrentHashMap<>();;
    private Integer version;
    private Boolean reportMetadata;

    protected TwinProperty()
    {
        this.reportMetadata = false;
        this.version = null;
    }

    protected void enableMetadata()
    {
        /* Codes_SRS_TWINPARSER_21_020: [The enableMetadata shall enable report metadata in Json for the Desired and for the Reported Properties.] */
        this.reportMetadata = true;
    }

    protected Boolean addProperty(String key, Object value, Integer propertyVersion) throws IllegalArgumentException
    {
        /* Codes_SRS_TWINPARSER_21_059: [The updateDesiredProperty shall only change properties in the map, keep the others as is.] */
        /* Codes_SRS_TWINPARSER_21_061: [All `key` and `value` in property shall be case sensitive.] */
        /* Codes_SRS_TWINPARSER_21_060: [The updateReportedProperty shall only change properties in the map, keep the others as is.] */
        /* Codes_SRS_TWINPARSER_21_062: [All `key` and `value` in property shall be case sensitive.] */
        Boolean change = false;

        if(key == null)
        {
            /* Codes_SRS_TWINPARSER_21_073: [If any `key` is null, the updateDesiredProperty shall throw IllegalArgumentException.] */
            /* Codes_SRS_TWINPARSER_21_079: [If any `key` is null, the updateReportedProperty shall throw IllegalArgumentException.] */
            throw new IllegalArgumentException("Property key shall not be null");
        }
        if(key.isEmpty())
        {
            /* Codes_SRS_TWINPARSER_21_074: [If any `key` is empty, the updateDesiredProperty shall throw IllegalArgumentException.] */
            /* Codes_SRS_TWINPARSER_21_080: [If any `key` is empty, the updateReportedProperty shall throw IllegalArgumentException.] */
            throw new IllegalArgumentException("Property key shall not be empty");
        }
        if(key.length()>128)
        {
            /* Codes_SRS_TWINPARSER_21_075: [If any `key` is more than 128 characters long, the updateDesiredProperty shall throw IllegalArgumentException.] */
            /* Codes_SRS_TWINPARSER_21_081: [If any `key` is more than 128 characters long, the updateReportedProperty shall throw IllegalArgumentException.] */
            throw new IllegalArgumentException("Property key is too big for json");
        }
        if(key.contains(".") || key.contains(" ") || key.contains("$") )
        {
            /* Codes_SRS_TWINPARSER_21_076: [If any `key` has an illegal character, the updateDesiredProperty shall throw IllegalArgumentException.] */
            /* Codes_SRS_TWINPARSER_21_082: [If any `key` has an illegal character, the updateReportedProperty shall throw IllegalArgumentException.] */
            throw new IllegalArgumentException("Property key contains illegal character");
        }

        if((!property.containsKey(key)) || (!property.get(key).value.equals(value)) || reportMetadata)
        {
            change = true;
        }

        /* Codes_SRS_TWINPARSER_21_077: [If any `key` already exists, the updateDesiredProperty shall replace the existed value by the new one.] */
        /* Codes_SRS_TWINPARSER_21_083: [If any `key` already exists, the updateReportedProperty shall replace the existed value by the new one.] */
        property.put(key, new Property(value, propertyVersion));

        return change;
    }

    protected JsonElement update(Map<String, Object> property)
    {
        JsonElement updatedJsonElement;
        TwinProperty updated = new TwinProperty();

        synchronized (lock)
        {
            if (property != null)
            {
                for (Map.Entry<String, Object> entry : property.entrySet())
                {
                    if (addProperty(entry.getKey(), entry.getValue(), null))
                    {
                        if (entry.getValue() != null)
                        {
                            /* Codes_SRS_TWINPARSER_21_078: [If any `value` is null, the updateDesiredProperty shall store it but do not report on Json.] */
                            /* Codes_SRS_TWINPARSER_21_084: [If any `value` is null, the updateReportedProperty shall store it but do not report on Json.] */
                            updated.addProperty(entry.getKey(), entry.getValue(), null);
                        }
                    }
                }

                if (updated.size() > 0)
                {
                    updatedJsonElement = updated.toJsonElement();
                }
                else
                {
                    updatedJsonElement = null;
                }
            }
            else
            {
                updatedJsonElement = null;
            }
        }

        return updatedJsonElement;
    }

    protected Integer getVersion()
    {
        /* Codes_SRS_TWINPARSER_21_048: [The getDesiredPropertyVersion shall return the desired property version.] */
        /* Codes_SRS_TWINPARSER_21_049: [The getReportedPropertyVersion shall return the reported property version.] */
        return this.version;
    }

    protected TwinMetadata getMetadata(String key)
    {
        TwinMetadata twinMetadata = null;

        synchronized (lock)
        {
            if (property.containsKey(key))
            {
                twinMetadata = property.get(key).metadata;
            }
            else
            {
                twinMetadata = null;
            }
        }

        return twinMetadata;
    }

    protected Map<String, Object> getPropertyMap()
    {
        /* Codes_SRS_TWINPARSER_21_050: [The getDesiredPropertyMap shall return a map with all desired property key value pairs.] */
        /* Codes_SRS_TWINPARSER_21_051: [The getReportedPropertyMap shall return a map with all reported property key value pairs.] */
        Map<String, Object> propertyMap = null;

        synchronized (lock)
        {
            if (property.isEmpty())
            {
                propertyMap = null;
            }
            else
            {
                propertyMap = new HashMap<>();
                for (Map.Entry<String, Property> e : property.entrySet())
                {
                    if (e.getValue().value == null)
                    {
                        propertyMap.put(e.getKey(), null);
                    }
                    else
                    {
                        propertyMap.put(e.getKey(), e.getValue().value.toString());
                    }
                }
            }
        }
        return propertyMap;
    }

    protected int size()
    {
        return this.property.size();
    }

    protected Object get(String key)
    {
        Object result = null;

        synchronized (lock)
        {
            if (property.containsKey(key))
            {
                result = property.get(key).value;
            }
            else
            {
                result = null;
            }
        }
        return result;
    }

    protected String toJson()
    {
        return toJsonElement().toString();
    }

    protected JsonElement toJsonElement()
    {
        /* Codes_SRS_TWINPARSER_21_017: [The toJsonElement shall return a JsonElement with information in the TwinParser using json format.] */
        Gson gson = new GsonBuilder().create();
        Map<String, Object> diffMap = new HashMap<>();
        Map<String, TwinMetadata> metadata = new HashMap<>();

        synchronized (lock)
        {
            for (Map.Entry<String, Property> entry : property.entrySet())
            {
                /* Codes_SRS_TWINPARSER_21_018: [The toJsonElement shall not include null fields.] */
                if(entry.getValue().value != null)
                {
                    diffMap.put(entry.getKey(), entry.getValue().value);
                    metadata.put(entry.getKey(), entry.getValue().metadata);
                }
            }
        }

        if(reportMetadata)
        {
            diffMap.put(METADATA_TAG, metadata);
        }

        if(version != null)
        {
            diffMap.put(VERSION_TAG, version);
        }

        return gson.toJsonTree(diffMap);
    }

    protected void update(LinkedTreeMap<String, Object> jsonTree,
                       TwinChangedCallback onCallback) throws IllegalArgumentException
    {
        Map<String, Object> diffField = new HashMap<>();
        Map<String, Object> diffMetadata = new HashMap<>();

        try
        {
            /* Codes_SRS_TWINPARSER_21_039: [The updateTwin shall fill the fields the properties in the TwinParser class with the keys and values provided in the json string.] */
            /* Codes_SRS_TWINPARSER_21_029: [The updateDesiredProperty shall update the Desired property using the information provided in the json.] */
            /* Codes_SRS_TWINPARSER_21_034: [The updateReportedProperty shall update the Reported property using the information provided in the json.] */
            updateVersion(jsonTree);
            /* Codes_SRS_TWINPARSER_21_041: [The updateTwin shall create a list with all properties that was updated (new key or value) by the new json.] */
            /* Codes_SRS_TWINPARSER_21_030: [The updateDesiredProperty shall generate a map with all pairs key value that had its content changed.] */
            /* Codes_SRS_TWINPARSER_21_035: [The updateReportedProperty shall generate a map with all pairs key value that had its content changed.] */
            diffField = updateFields(jsonTree);
            diffMetadata = updateMetadata(jsonTree);
        }
        catch (Exception e)
        {
            /* Codes_SRS_TWINPARSER_21_092: [If the provided json is not valid, the updateDesiredProperty shall throws IllegalArgumentException.] */
            throw new IllegalArgumentException("Malformed Json:" + e);
        }

        if(reportMetadata)
        {
            for(Map.Entry<String, Object> entry : diffMetadata.entrySet())
            {
                Property val = property.get(entry.getKey());
                if (val == null)
                {
                    diffField.put(entry.getKey(), null);
                }
                else
                {
                    diffField.put(entry.getKey(), val.value.toString());
                }
            }
        }

        /* Codes_SRS_TWINPARSER_21_046: [If OnDesiredCallback was not provided, the updateTwin shall not do anything with the list of updated desired properties.] */
        /* Codes_SRS_TWINPARSER_21_047: [If OnReportedCallback was not provided, the updateTwin shall not do anything with the list of updated reported properties.] */
        /* Codes_SRS_TWINPARSER_21_069: [If there is no change in the Desired property, the updateTwin shall not change the reported collection and not call the OnReportedCallback.] */
        /* Codes_SRS_TWINPARSER_21_070: [If there is no change in the Reported property, the updateTwin shall not change the reported collection and not call the OnReportedCallback.] */
        /* Codes_SRS_TWINPARSER_21_032: [If the OnDesiredCallback is set as null, the updateDesiredProperty shall discard the map with the changed pairs.] */
        /* Codes_SRS_TWINPARSER_21_033: [If there is no change in the Desired property, the updateDesiredProperty shall not change the collection and not call the OnDesiredCallback.] */
        /* Codes_SRS_TWINPARSER_21_037: [If the OnReportedCallback is set as null, the updateReportedProperty shall discard the map with the changed pairs.] */
        /* Codes_SRS_TWINPARSER_21_038: [If there is no change in the Reported property, the updateReportedProperty shall not change the collection and not call the OnReportedCallback.] */
        /* Codes_SRS_TWINPARSER_21_093: [If the provided json is not valid, the updateReportedProperty shall throws IllegalArgumentException.] */
        if((diffField.size() != 0) &&(onCallback != null))
        {
            /* Codes_SRS_TWINPARSER_21_044: [If OnDesiredCallback was provided, the updateTwin shall create a new map with a copy of all pars key values updated by the json in the Desired property, and OnDesiredCallback passing this map as parameter.] */
            /* Codes_SRS_TWINPARSER_21_045: [If OnReportedCallback was provided, the updateTwin shall create a new map with a copy of all pars key values updated by the json in the Reported property, and OnReportedCallback passing this map as parameter.] */
            /* Codes_SRS_TWINPARSER_21_031: [The updateDesiredProperty shall send the map with all changed pairs to the upper layer calling onDesiredCallback (TwinChangedCallback).] */
            /* Codes_SRS_TWINPARSER_21_036: [The updateReportedProperty shall send the map with all changed pairs to the upper layer calling onReportedCallback (TwinChangedCallback).] */
            onCallback.execute(diffField);
        }
    }

    protected void update(String json, TwinChangedCallback onCallback) throws IllegalArgumentException
    {
        LinkedTreeMap<String, Object> newValues;
        try
        {
            /* Codes_SRS_TWINPARSER_21_095: [If the provided json have any duplicated `key`, the updateReportedProperty shall throws IllegalArgumentException.] */
            /* Codes_SRS_TWINPARSER_21_096: [If the provided json have any duplicated `key`, the updateDesiredProperty shall throws IllegalArgumentException.] */
            Gson gson = new GsonBuilder().create();
            newValues = (LinkedTreeMap<String, Object>) gson.fromJson(json, LinkedTreeMap.class);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Malformed Json:" + e);
        }
        update(newValues, onCallback);
    }

    private void updateVersion(LinkedTreeMap<String, Object> jsonTree)
    {
        for (Map.Entry<String, Object> entry : jsonTree.entrySet())
        {
            if (entry.getKey().equals(VERSION_TAG))
            {
                version = new Integer( (int) ((double) entry.getValue()));
                break;
            }
        }
    }

    private Map<String, Object>  updateMetadata(LinkedTreeMap<String, Object> jsonTree)
    {
        Map<String, Object> diff = new HashMap<>();
        for (Map.Entry<String, Object> entry : jsonTree.entrySet())
        {
            if(entry.getKey().equals(METADATA_TAG))
            {
                LinkedTreeMap<String, Object> metadataTree = (LinkedTreeMap<String, Object>)entry.getValue();
                for (LinkedTreeMap.Entry<String, Object> item : metadataTree.entrySet())
                {
                    synchronized (lock)
                    {
                        if (property.containsKey(item.getKey()))
                        {
                            LinkedTreeMap<String, Object> itemTree = (LinkedTreeMap<String, Object>) item.getValue();
                            String lastUpdated = null;
                            Integer lastUpdatedVersion = null;
                            for (LinkedTreeMap.Entry<String, Object> metadataItem : itemTree.entrySet())
                            {
                                if (metadataItem.getKey().equals(LAST_UPDATE_TAG))
                                {
                                    lastUpdated = metadataItem.getValue().toString();
                                }
                                else if (metadataItem.getKey().equals(LAST_UPDATE_VERSION_TAG))
                                {
                                    lastUpdatedVersion = (int) ((double) metadataItem.getValue());
                                }
                            }
                            if (property.get(item.getKey()).metadata.update(lastUpdated, lastUpdatedVersion))
                            {
                                diff.put(item.getKey(), item.getValue().toString());
                            }
                        }
                    }
                }
                break;
            }
        }
        return diff;
    }

    private Map<String, Object> updateFields(LinkedTreeMap<String, Object> jsonTree) throws IllegalArgumentException
    {
        Map<String, Object> diff = new HashMap<>();

        for (Map.Entry<String, Object> entry : jsonTree.entrySet())
        {
            if(entry.getKey().isEmpty())
            {
                throw new IllegalArgumentException("Invalid Key on Json");
            }
            if(!entry.getKey().contains("$"))
            {
                synchronized (lock)
                {
                    /* Codes_SRS_TWINPARSER_21_040: [The updateTwin shall not change fields that is not reported in the json string.] */
                    if (property.containsKey(entry.getKey()))
                    {
                        if (entry.getValue() == null)
                        {
                            /* Codes_SRS_TWINPARSER_21_042: [If a valid key has a null value, the updateTwin shall delete this property.] */
                            property.remove(entry.getKey());
                            diff.put(entry.getKey(), null);
                        }
                        else if (!property.get(entry.getKey()).value.equals(entry.getValue()))
                        {
                            property.put(entry.getKey(), new Property(entry.getValue(), null));
                            diff.put(entry.getKey(), entry.getValue().toString());
                        }
                    }
                    else if (entry.getValue() != null)
                    {
                        property.put(entry.getKey(), new Property(entry.getValue(), null));
                        diff.put(entry.getKey(), entry.getValue().toString());
                    }
                }
            }
        }

        return diff;
    }

}
