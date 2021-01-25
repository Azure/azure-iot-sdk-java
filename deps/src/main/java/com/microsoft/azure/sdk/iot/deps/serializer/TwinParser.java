// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * TwinParser Representation including the twin collection and Json serializer and deserializer.
 * @deprecated As of release 0.4.0, replaced by {@link com.microsoft.azure.sdk.iot.deps.twin.TwinState}
 */
// Unchecked casts of Maps to Map<String, Object> are safe as long as service is returning valid twin json payloads. Since all json keys are Strings, all maps must be Map<String, Object>
@SuppressWarnings("unchecked")
@Deprecated
public class TwinParser
{

    private TwinChangedCallback onDesiredCallback = null;
    private TwinChangedCallback onReportedCallback = null;
    private static TwinChangedCallback onTagsCallback = null;

    private static final String TAGS_TAG = "tags";
    private static final String PROPERTIES_TAG = "properties";
    private static final String DESIRED_TAG = "desired";
    private static final String REPORTED_TAG = "reported";

    private static final int MAX_MAP_LEVEL = 5;

    protected TwinTags tags = null;
    protected TwinProperties properties = new TwinProperties();
    protected RegisterManagerParser manager = new RegisterManagerParser();

    /**
     * CONSTRUCTOR
     * Create a TwinParser instance with default values.
     *      set OnDesiredCallback as null
     *      set OnReportedCallback as null
     *      set Tags as null
     *
     */
    public TwinParser()
    {
    }

    /**
     * CONSTRUCTOR
     * Create a TwinParser instance with default values.
     *      set OnReportedCallback as null
     *      set Tags as null
     *
     * @param onDesiredCallback - Callback function to report changes on the `Desired` collection.
     */
    public TwinParser(TwinChangedCallback onDesiredCallback)
    {
        this();
        setDesiredCallback(onDesiredCallback);
    }

    /**
     * CONSTRUCTOR
     * Create a TwinParser instance with default values.
     *      set Tags as null
     *
     * @param onDesiredCallback - Callback function to report changes on the `Desired` collection.
     * @param onReportedCallback - Callback function to report changes on the `Reported` collection.
     */
    public TwinParser(TwinChangedCallback onDesiredCallback, TwinChangedCallback onReportedCallback)
    {
        this();

        setDesiredCallback(onDesiredCallback);
        setReportedCallback(onReportedCallback);
    }

    /**
     * Set the callback function to report changes on the `Desired` collection when `TwinParser`
     * receives a new json.
     *
     * @param onDesiredCallback - Callback function to report changes on the `Desired` collection.
     */
    public void setDesiredCallback(TwinChangedCallback onDesiredCallback)
    {
        this.onDesiredCallback = onDesiredCallback;
    }

    /**
     * Set the callback function to report changes on the `Reported` collection when `TwinParser`
     * receives a new json.
     *
     * @param onReportedCallback - Callback function to report changes on the `Reported` collection.
     */
    public void setReportedCallback(TwinChangedCallback onReportedCallback)
    {
        this.onReportedCallback = onReportedCallback;
    }

    /**
     * Set the callback function to report changes on the `tags` collection when `TwinParser`
     * receives a new json.
     *
     * @param onTagsCallback - Callback function to report changes on the `Reported` collection.
     */
    public void setTagsCallback(TwinChangedCallback onTagsCallback)
    {
        TwinParser.onTagsCallback = onTagsCallback;
    }

    /**
     * Create a String with a json content that represents all the information in the TwinParser class and innerClasses.
     *
     * @return String with the json content.
     */
    public String toJson()
    {
        return toJsonElement().toString();
    }

    /**
     * Create a JsonElement that represents all the information in the TwinParser class and innerClasses.
     *
     * @return JsonElement with the Twin information.
     */
    public JsonElement toJsonElement()
    {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        JsonObject twinJson = gson.toJsonTree(manager).getAsJsonObject();

        if (tags != null)
        {
            twinJson.add(TAGS_TAG, tags.toJsonElement());
        }

        twinJson.add(PROPERTIES_TAG, this.properties.toJsonElement());

        return twinJson;
    }

    /**
     * Enable tags in the Twin collection and in the Json.
     *
     */
    public void enableTags()
    {
        if (this.tags == null)
        {
            this.tags = new TwinTags();
        }
    }

    /**
     * Enable metadata report in the Json.
     *
     */
    public void enableMetadata()
    {
        properties.enableDesiredMetadata();
        properties.enableReportedMetadata();
    }

    /**
     * Update properties and tags information in the collection, and return a string with a json that contains a
     * sub-collection of added properties, properties with new value, added tags, and tags with new values.
     *
     * @param desiredPropertyMap - Map of `desired` property to change the collection.
     * @param reportedPropertyMap - Map of `reported` property to change the collection.
     * @param tagsMap - Map of `tags` to change the collection.
     * @return Json with added or changed properties and tags
     * @throws IllegalArgumentException This exception is thrown if the properties or tags in the maps do not fits the requirements.
     * @throws IOException This exception is thrown if tags the is not enabled.
     */
    public String updateTwin(Map<String, Object> desiredPropertyMap,
                             Map<String, Object> reportedPropertyMap,
                             Map<String, Object> tagsMap)
            throws IllegalArgumentException, IOException
    {
        JsonObject jsonProperty = new JsonObject();
        JsonObject jsonTwin;

        validateMap(desiredPropertyMap);
        validateMap(reportedPropertyMap);
        validateMap(tagsMap);

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        jsonTwin = gson.toJsonTree(manager).getAsJsonObject();

        if ((tags == null) && (tagsMap != null))
        {
            throw new IOException("Update not enabled Tags");
        }

        if ((desiredPropertyMap == null) && (reportedPropertyMap == null) && (tagsMap == null))
        {
            throw new IllegalArgumentException("Null maps");
        }

        JsonElement jsonDesiredProperty = innerUpdateDesiredProperty(desiredPropertyMap);
        if (jsonDesiredProperty != null)
        {
            jsonProperty.add(DESIRED_TAG, jsonDesiredProperty);
        }
        else
        {
            jsonProperty.add(DESIRED_TAG, new JsonObject());
        }

        JsonElement jsonReportedProperty = innerUpdateReportedProperty(reportedPropertyMap);
        if (jsonReportedProperty != null)
        {
            jsonProperty.add(REPORTED_TAG, jsonReportedProperty);
        }
        else
        {
            jsonProperty.add(REPORTED_TAG,  new JsonObject());
        }

        JsonElement jsonTags = null;
        if (tags != null)
        {
            if (tagsMap == null)
            {
                jsonTwin.add(TAGS_TAG, new JsonObject());
            }
            else
            {
                jsonTags = innerUpdateTags(tagsMap);
                if (jsonTags != null)
                {
                    jsonTwin.add(TAGS_TAG, jsonTags);
                }
                else
                {
                    jsonTwin.add(TAGS_TAG, new JsonObject());
                }
            }
        }

        if ((jsonDesiredProperty!=null) || (jsonReportedProperty!=null) || (jsonTags!=null))
        {
            jsonTwin.add(PROPERTIES_TAG, jsonProperty);
        }
        else
        {
            return null;
        }

        return jsonTwin.toString();
    }


    /**
     * Update the `desired` properties information in the collection, and return a string with a json that contains a
     * sub-collection of added properties, or properties with new value.
     *
     * @param propertyMap - Map of `desired` property to change the collection.
     * @return Json with added or changed properties
     * @throws IllegalArgumentException This exception is thrown if the properties in the map do not fits the requirements.
     */
    public String updateDesiredProperty(Map<String, Object> propertyMap) throws IllegalArgumentException
    {
        if (propertyMap == null)
        {
            throw new IllegalArgumentException("Null desired property map.");
        }

        JsonElement jsonElement = innerUpdateDesiredProperty(propertyMap);
        if (jsonElement == null)
        {
            return null;
        }
        return jsonElement.toString();
    }

    private JsonElement innerUpdateDesiredProperty(Map<String, Object> propertyMap) throws IllegalArgumentException
    {
        JsonElement updatedElements;

        if (propertyMap != null)
        {
            validateMap(propertyMap);
            updatedElements = properties.updateDesired(propertyMap);
        }
        else
        {
            updatedElements = null;
        }

        return updatedElements;
    }

    /**
     * Update the `reported` properties information in the collection, and return a string with a json that contains a
     * sub-collection of added properties, or properties with new value.
     *
     * @param propertyMap - Map of `reported` property to change the collection.
     * @return Json with added or changed properties
     * @throws IllegalArgumentException This exception is thrown if the properties in the map do not fits the requirements.
     */
    public String updateReportedProperty(Map<String, Object> propertyMap) throws IllegalArgumentException
    {
        if (propertyMap == null)
        {
            throw new IllegalArgumentException("Null reported property map.");
        }

        JsonElement jsonElement = innerUpdateReportedProperty(propertyMap);
        if (jsonElement == null)
        {
            return null;
        }
        return jsonElement.toString();
    }

    private JsonElement innerUpdateReportedProperty(Map<String, Object> propertyMap) throws IllegalArgumentException
    {
        JsonElement updatedElements;

        if (propertyMap != null)
        {
            validateMap(propertyMap);
            updatedElements = properties.updateReported(propertyMap);
        }
        else
        {
            updatedElements = null;
        }

        return updatedElements;
    }
    
    /**
     * Update the `tags` information in the collection, and return a string with a json that contains a
     * sub-collection of added tags, or tags with new value.
     *
     * @param tagsMap - Map of `tags` to change the collection.
     * @return Json with added or changed tags
     * @throws IllegalArgumentException This exception is thrown if the tags in the map do not fits the requirements.
     * @throws IOException This exception is thrown if tags the is not enabled.
     */
    public String updateTags(Map<String, Object> tagsMap) throws IllegalArgumentException, IOException
    {
        JsonElement jsonElement = innerUpdateTags(tagsMap);
        if (jsonElement == null)
        {
            return null;
        }
        return jsonElement.toString();
    }

    private JsonElement innerUpdateTags(Map<String, Object> tagsMap) throws IllegalArgumentException, IOException
    {
        JsonElement updatedElements;

        if (tags == null)
        {
            throw new IOException("Update not enabled Tags");
        }

        if (tagsMap != null)
        {
            validateMap(tagsMap);
            updatedElements = tags.update(tagsMap);
        }
        else
        {
            throw new IllegalArgumentException("Null tags map");
        }

        return updatedElements;
    }

    /**
     * Reset the `desired` properties information in the collection, deleting all old properties and add all the new provided ones.
     * Return a string with a json that contains a sub-collection of added properties.
     *
     * @param propertyMap - Map of `desired` property to change the collection.
     * @return Json with added properties
     * @throws IllegalArgumentException This exception is thrown if the properties in the map do not fits the requirements.
     */
    public String resetDesiredProperty(Map<String, Object> propertyMap) throws IllegalArgumentException
    {
        String json;

        if (propertyMap != null)
        {
            validateMap(propertyMap);

            JsonElement updatedElements = properties.resetDesired(propertyMap);

            if (updatedElements == null)
            {
                json = "{}";
            }
            else
            {
                json = updatedElements.toString();
            }
        }
        else
        {
            throw new IllegalArgumentException("Null property map");
        }

        return json;
    }

    /**
     * Reset the `reported` properties information in the collection, deleting all old properties and add all the new provided ones.
     * Return a string with a json that contains a sub-collection of added properties.
     *
     * @param propertyMap - Map of `reported` property to change the collection.
     * @return Json with added properties
     * @throws IllegalArgumentException This exception is thrown if the properties in the map do not fits the requirements.
     */
    public String resetReportedProperty(Map<String, Object> propertyMap) throws IllegalArgumentException
    {
        String json;

        if (propertyMap != null)
        {
            validateMap(propertyMap);
            JsonElement updatedElements = properties.resetReported(propertyMap);

            if (updatedElements == null)
            {
                json = "{}";
            }
            else
            {
                json = updatedElements.toString();
            }
        }
        else
        {
            throw new IllegalArgumentException("Null property map");
        }

        return json;
    }

    /**
     * Reset the `tags` information in the collection, deleting all old tags and add all the new provided ones.
     * Return a string with a json that contains a sub-collection of added tags.
     *
     * @param tagsMap - Map of `tags` to change the collection.
     * @return Json with added tags
     * @throws IllegalArgumentException This exception is thrown if the tags in the map do not fits the requirements.
     * @throws IOException This exception is thrown if tags the is not enabled.
     */
    public String resetTags(Map<String, Object> tagsMap) throws IllegalArgumentException, IOException
    {
        String json;

        if (tags == null)
        {
             throw new IOException("Update not enabled Tags");
        }

        if (tagsMap != null)
        {
            validateMap(tagsMap);

            tags = new TwinTags();
            JsonElement updatedElements = this.tags.update(tagsMap);

            if (updatedElements == null)
            {
                json = "{}";
            }
            else
            {
                json = updatedElements.toString();
            }
        }
        else
        {
            throw new IllegalArgumentException("Null tags map");
        }

        return json;
    }

    /**
     * Update the properties information in the collection, using the information parsed from the provided json.
     * It will fire a callback if any property was added, excluded, or had its value updated.
     *
     * @param json - Json with property to change the collection.
     * @throws IllegalArgumentException This exception is thrown if the Json is not well formed.
     */
    public void updateTwin(String json) throws IllegalArgumentException
    {
        if (json == null)
        {
            throw new IllegalArgumentException("Null json");
        }

        validateJson(json);

        if (!json.isEmpty())
        {
            Gson gson = new GsonBuilder().disableInnerClassSerialization().disableHtmlEscaping().create();
            Map<String, Object> jsonTree;
            try
            {
                jsonTree = (Map<String, Object>) gson.fromJson(json, HashMap.class);
                manager = gson.fromJson(json, RegisterManagerParser.class);
            }
            catch (JsonSyntaxException e)
            {
                throw new IllegalArgumentException("Malformed Json: " + e);
            }

            boolean propertiesLevel = false;
            for (Map.Entry<String, Object> entry : jsonTree.entrySet())
            {
                if (entry.getKey().equals(PROPERTIES_TAG))
                {
                    properties.update((Map<String, Object>) entry.getValue(), onDesiredCallback, onReportedCallback);
                    propertiesLevel = true;
                }
                else if ((entry.getKey().equals(DESIRED_TAG)) || (entry.getKey().equals(REPORTED_TAG)))
                {
                    if (!propertiesLevel)
                    {
                        properties.update(jsonTree, onDesiredCallback, onReportedCallback);
                    }
                    else
                    {
                        throw new IllegalArgumentException("Invalid Entry");
                    }
                    break;
                }
                else if (entry.getKey().equals(TAGS_TAG))
                {
                    if (tags != null)
                    {
                        tags.update((Map<String, Object>) entry.getValue(), onTagsCallback);
                    }
                    propertiesLevel = true;
                }
            }
        }
    }

    /**
     * Update the `desired` properties information in the collection, using the information parsed from the provided json.
     * It will fire a callback if any property was added, excluded, or had its value updated.
     *
     * @param json - Json with `desired` property to change the collection.
     * @throws IllegalArgumentException This exception is thrown if the Json is not well formed.
     */
    public void updateDesiredProperty(String json) throws IllegalArgumentException
    {
        if (json == null)
        {
            throw new IllegalArgumentException("Null json");
        }

        if (!json.isEmpty())
        {
            try
            {
                properties.updateDesired(json, onDesiredCallback);
            }
            catch (JsonSyntaxException e)
            {
                throw new IllegalArgumentException("Malformed json: " + e);
            }
        }
    }

    /**
     * Update the `reported` properties information in the collection, using the information parsed from the provided json.
     * It will fire a callback if any property was added, excluded, or had its value updated.
     *
     * @param json - Json with `reported` property to change the collection.
     * @throws IllegalArgumentException This exception is thrown if the Json is not well formed.
     */
    public void updateReportedProperty(String json) throws IllegalArgumentException
    {
        if (json == null)
        {
            throw new IllegalArgumentException("Null json");
        }

        if (!json.isEmpty())
        {
            try
            {
                properties.updateReported(json, onReportedCallback);
            }
            catch (JsonSyntaxException e)
            {
                throw new IllegalArgumentException("Malformed json: " + e);
            }
        }
    }

    /**
     * Update the device manager information in the collection, and return a string with a json that contains a
     * the new device manager description, including new and old values.
     *
     * @param deviceId - Device name
     * @param status - Device status("enabled", "disabled")
     * @param statusReason - A 128 char long string storing the reason of suspension (for status="disabled").
     * @return Json with the manager description. Null if nothing change.
     * @throws IllegalArgumentException This exception is thrown if there are any inconsistent in the provided description.
     */
    public String updateDeviceManager(String deviceId, TwinStatus status, String statusReason) throws IllegalArgumentException
    {
        boolean change = false;

        manager.validateDeviceManager(deviceId, status, statusReason);

        if (manager.setStatus(status, statusReason))
        {
            change = true;
        }

        if (manager.setDeviceId(deviceId))
        {
            change = true;
        }

        if (!change)
        {
            return null;
        }

        return toJson();
    }

    /**
     * Return the `desired` property version.
     *
     * @return Integer that contains the `desired` property version (it can be null).
     */
    public Integer getDesiredPropertyVersion()
    {
        return properties.getDesiredVersion();
    }

    /**
     * Return the `reported` property version.
     *
     * @return Integer that contains the `reported` property version (it can be null).
     */
    public Integer getReportedPropertyVersion()
    {
        return properties.getReportedVersion();
    }

    /**
     * Return a map with all `desired` properties in the collection.
     *
     * @return A map with all `desired` properties in the collection (it can be null).
     */
    public Map<String, Object> getDesiredPropertyMap()
    {
        return properties.getDesiredPropertyMap();
    }

    /**
     * Return a map with all `reported` properties in the collection.
     *
     * @return A map with all `reported` properties in the collection (it can be null).
     */
    public Map<String, Object> getReportedPropertyMap()
    {
        return properties.getReportedPropertyMap();
    }

    /**
     * Return a map with all `tags` in the collection.
     *
     * @return A map with all `tags` in the collection (it can be null).
     * @throws IOException This exception is thrown if tags the is not enabled.
     */
    public Map<String, Object> getTagsMap() throws IOException
    {
        if (this.tags == null)
        {
             throw new IOException("Update not enabled Tags");
        }

         return this.tags.getMap();
    }

    /**
     * Getter for device name
     *
     * @return Device name
     * A case-sensitive string (up to 128 char long)
     * of ASCII 7-bit alphanumeric chars
     * + {'-', ':', '.', '+', '%', '_', '#', '*', '?', '!', '(', ')', ',', '=', '@', ';', '$', '''}.
     */
    public String getDeviceId()
    {
        return this.manager.deviceId;
    }

    /**
     * Setter for device name
     *
     * @param deviceId device id
     * A case-sensitive string (up to 128 char long)
     * of ASCII 7-bit alphanumeric chars
     * + {'-', ':', '.', '+', '%', '_', '#', '*', '?', '!', '(', ')', ',', '=', '@', ';', '$', '''}
     * @throws IllegalArgumentException if the provided device Id is not valid.
     */
    public void setDeviceId(String deviceId) throws IllegalArgumentException
    {
        ParserUtility.validateId(deviceId);
        this.manager.setDeviceId(deviceId);
    }

    /**
     * Getter for device generation name
     *
     * @return Device generation Id
     */
    public String getGenerationId()
    {
        return this.manager.generationId;
    }

    /**
     * Getter for ETag
     *
     * @return A string representing a weak ETAG version
     * of this JSON description. This is a hash.
     */
    public String getETag()
    {
        return this.manager.eTag;
    }

    /**
     * Getter for Twin version
     *
     * @return A Integer representing a twin tags and properties version in the JSON. It can be {@code null}
     */
    public Integer getVersion()
    {
        return this.manager.version;
    }

    /**
     * Setter for ETag.
     *
     * @param eTag is a string representing a weak ETAG version
     * of this JSON description. This is a hash.
     * @throws IllegalArgumentException if the provided eTag Id is not valid.
     */
    public void setETag(String eTag) throws IllegalArgumentException
    {
        ParserUtility.validateStringUTF8(eTag);
        this.manager.eTag = eTag;
    }

    /**
     * Getter for device status
     *
     * @return "enabled", "disabled".
     * If "enabled", this device is authorized to connect.
     * If "disabled" this device cannot receive or send messages, and statusReason must be set.
     */
    public TwinStatus getStatus()
    {
        return this.manager.status;
    }

    /**
     * Getter for status reason
     *
     * @return A 128 char long string storing the reason of suspension.
     * (all UTF-8 chars allowed).
     */
    public String getStatusReason()
    {
        return this.manager.statusReason;
    }

    /**
     * Getter for status updated date and time
     *
     * @return Datetime of last time the state was updated.
     */
    public String getStatusUpdatedTime()
    {
        return this.manager.statusUpdatedTime;
    }

    /**
     * Getter for connection state
     *
     * @return The connectionState string
     */
    public TwinConnectionState getConnectionState()
    {
        return this.manager.connectionState;
    }

    /**
     * Getter for connection state updated date and time
     *
     * @return The string containing the time when the connectionState parameter was updated
     */
    public String getConnectionStateUpdatedTime()
    {
        return this.manager.connectionStateUpdatedTime;
    }

    /**
     * Getter for last activity time
     *
     * @return The string containing the time when the lastActivity parameter was updated
     */
    public String getLastActivityTime()
    {
        return this.manager.lastActivityTime;
    }

    private void validateJson(String json) throws IllegalArgumentException
    {
        Map<String, Object> map;
        try
        {
            Gson gson = new GsonBuilder().disableInnerClassSerialization().disableHtmlEscaping().create();
            map = (Map<String, Object>) gson.fromJson(json, HashMap.class);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Malformed Json: " + e);
        }

        if (map != null)
        {
            boolean propertiesLevel = false;
            boolean containsTagsOrProperties = false;
            for (Map.Entry<String, Object> entry : map.entrySet())
            {
                if (entry.getKey().equals(PROPERTIES_TAG))
                {
                    properties.validate((Map<String, Object>) entry.getValue());
                    propertiesLevel = true;
                    containsTagsOrProperties = true;
                }
                else if ((entry.getKey().equals(DESIRED_TAG)) || (entry.getKey().equals(REPORTED_TAG)))
                {
                    if (!propertiesLevel)
                    {
                        properties.validate(map);
                        containsTagsOrProperties = true;
                    }
                    else
                    {
                        throw new IllegalArgumentException("Invalid Entry");
                    }
                    break;
                }
                else if (entry.getKey().equals(TAGS_TAG))
                {
                    if (tags != null)
                    {
                        tags.validate((Map<String, Object>) entry.getValue());
                    }
                    propertiesLevel = true;
                    containsTagsOrProperties = true;
                }
            }
            if (!containsTagsOrProperties)
            {
                throw new IllegalArgumentException("Json do not contains twin information");
            }
        }
    }

    private void validateMap(Map<String, Object> map) throws IllegalArgumentException
    {
        if (map != null)
        {
            validateMap(map, 0, MAX_MAP_LEVEL);
        }
    }

    private void validateMap(Map<String, Object> map, int level, int maxLevel) throws IllegalArgumentException
    {
        level ++;
        
        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            Object value = entry.getValue();

            if ((value != null) && ((value.getClass().isArray()) || (value.getClass().isLocalClass())))
            {
                throw new IllegalArgumentException("Malformed Json: illegal value type");
            }

            if (value instanceof Map)
            {
                if (level <= maxLevel)
                {
                    validateMap((Map<String, Object>) value, level, maxLevel);
                }
                else
                {
                    throw new IllegalArgumentException("Malformed Json: exceed " + MAX_MAP_LEVEL + " levels");
                }
            }
        }
    }
}
