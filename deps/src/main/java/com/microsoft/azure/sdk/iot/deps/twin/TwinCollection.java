// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.twin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.deps.util.Tools;

import java.util.HashMap;
import java.util.Map;

/**
 * Representation of a single Twin collection.
 *
 * <p> The TwinCollection is an extension of a {@code HashMap} of {@code String} and
 *     {@code Object} that contain individual and general versioning mechanism.
 *
 * <p> By the Twin definition, the {@code Object} can contain types of {@code Boolean},
 *     {@code Number}, {@code String}, {@code Object}, or a sub-TwinCollection, but
 *     it cannot be types defined by the user or arrays.
 *
 * <p> A TwinCollection can contain up to 5 levels of sub TwinCollections. Once the
 *     TwinCollection is a extension of the {@code HashMap}, both TwinCollection as well
 *     as its sub-TwinCollections can be casted to Map of String and Object.
 *
 * <p> The collection will be represented in the rest API as a JSON in the body. It can
 *     or cannot contain the metadata (identified by the <b>$</b> character at the
 *     beginning of the key.
 *
 * <p> Because of the Twin metadata, the character <b>$</b> is not allowed in the entry key.
 *
 * <p> For instance, the following JSON is a valid TwinCollection with its metadata.
 * <pre>
 * {@code
 * {
 *     "Color":"White",
 *     "MaxSpeed":{
 *         "Value":500,
 *         "NewValue":300
 *     },
 *     "$metadata":{
 *         "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *         "$lastUpdatedVersion":4,
 *         "Color":{
 *             "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *             "$lastUpdatedVersion":4,
 *         },
 *         "MaxSpeed":{
 *             "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *             "$lastUpdatedVersion":4,
 *             "Value":{
 *                 "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *                 "$lastUpdatedVersion":4
 *             },
 *             "NewValue":{
 *                 "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *                 "$lastUpdatedVersion":4
 *             }
 *         }
 *     },
 *     "$version":4
 * }
 * }
 * </pre>
 *
 * <p> This class exposes the Twin collection with or without metadata as a Map here
 *     user can gat both the value and the metadata. For instance, in the above TwinCollection,
 *     {@link #get(Object)} for <b>Color</b> will return <b>White</b> and the {@link #getTwinMetadata(String)}
 *     for <b>Color</b> will return the Object TwinMetadata that contain {@link TwinMetadata#getLastUpdated()}
 *     that will returns the {@code Date} <b>2017-09-21T02:07:44.238Z</b> and {@link TwinMetadata#getLastUpdatedVersion()}
 *     that will returns the {@code Integer} <b>4</b>.
 *
 * <p> For the nested TwinCollection, you can do the same, for instance, the following code will return the
 *     value and metadata of the <b>NewValue</b> nested in <b>MaxSpeed</b>:
 * <pre>
 * {@code
 *      // Get the value of the MaxSpeed, which is a inner TwinCollection.
 *      TwinCollection innerMaxSpeed = (TwinCollection) twinCollection.get("MaxSpeed");
 *
 *      // From the inner TwinCollection, get the value of the NewValue.
 *      Long maxSpeedNewValue = innerMaxSpeed.get("NewValue");
 *
 *      // As in the root TwinCollection, the inner TwinCollection contain its own metadata.
 *      // So, get the metadata information for the inner NewValue.
 *      TwinMetadata maxSpeedNewValueMetadata = innerMaxSpeed.getTwinMetadata("NewValue");
 *      Date newValueLastUpdated = maxSpeedNewValueMetadata.getLastUpdated(); //Shall contain `2017-09-21T02:07:44.238Z`
 *      Integer newValueLastUpdatedVersion = maxSpeedNewValueMetadata.getLastUpdatedVersion(); //Shall contain `4`
 * }
 * </pre>
 *
 * @see <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-device-twins">Understand and use device twins in IoT Hub</a>
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iothub/devicetwinapi">Device Twin Api</a>
 */
public class TwinCollection extends HashMap<String, Object>
{
    // By definition, Twin maps cannot contain more than 5 levels.
    private static final int MAX_TWIN_LEVEL = 6;

    // the Twin collection version
    private static final String VERSION_TAG = "$version";
    private Integer version;

    // the Twin collection metadata
    private static final String METADATA_TAG = "$metadata";
    private TwinMetadata twinMetadata;
    private Map<String, TwinMetadata> metadataMap = new HashMap<>();

    /**
     * Constructor
     *
     * <p> Creates an empty collection. Fill it with {@link #put(String, Object)}
     *     or {@link #putAll(Map)}.
     */
    public TwinCollection()
    {
        /* SRS_TWIN_COLLECTION_21_001: [The constructor shall create a new instance of the super class.] */
        super();
    }

    /**
     * Constructor
     *
     * <p> Creates a new Twin collection coping the provided Map.
     *
     * @param map the Map of {@code ? extends String} and {@code Object} with the Twin collection
     */
    public TwinCollection(Map<? extends String, Object> map)
    {
        /* SRS_TWIN_COLLECTION_21_002: [If the Map is null or empty, the constructor shall create a new empty instance.] */
        if((map != null) && !map.isEmpty())
        {
            /* SRS_TWIN_COLLECTION_21_003: [The constructor shall create a new instance of the super class and add the provided Map by calling putAll.] */
            this.putAll(map);
        }
    }

    /**
     * Constructor
     *
     * <p> Creates a new Twin collection coping the provided collection.
     *
     * @param collection the Collection of {@code ? extends String} and {@code Object} with the Twin collection
     */
    public TwinCollection(TwinCollection collection)
    {
        /* SRS_TWIN_COLLECTION_21_025: [If the Collection is null or empty, the constructor shall create a new empty instance.] */
        if((collection != null) && !collection.isEmpty())
        {
            /* SRS_TWIN_COLLECTION_21_026: [The constructor shall create a new instance of the super class and add the provided Map.] */
            /* SRS_TWIN_COLLECTION_21_027: [The constructor shall copy the version and metadata from the provided TwinCollection.] */
            this.version = collection.getVersion();
            this.twinMetadata = collection.getTwinMetadata();
            for (Map.Entry<String, Object> entry: collection.entrySet())
            {
                if(entry.getValue() instanceof TwinCollection)
                {
                    super.put((String)entry.getKey(), new TwinCollection((TwinCollection)entry.getValue()));
                }
                else
                {
                    super.put((String)entry.getKey(), entry.getValue());
                }
                this.metadataMap.put((String)entry.getKey(), collection.getTwinMetadata((String)entry.getKey()));
            }
        }
    }

    /**
     * Add all information in the provided Map to the TwinCollection.
     *
     * <p> Override {@code HashMap.putAll(Map)}.
     *
     * <p> This function will add all entries in the Map to the TwinCollection. If the provided
     *     key already exists, it will replace the value by the new one. This function will not
     *     delete or change the content of the other keys in the Map.
     *
     * <p> As defined by the Twin, the value of a entry can be an inner Map. TwinCollection will
     *     accept up to 5 levels of inner Maps.
     *
     * @param map A {@code Map} of entries to add to the TwinCollection.
     */
    @Override
    public void putAll(Map<? extends String, ?> map)
    {
        /* SRS_TWIN_COLLECTION_21_004: [The putAll shall throw IllegalArgumentException if the provided Map is null, empty or invalid.] */
        if((map == null) || map.isEmpty())
        {
            throw new IllegalArgumentException("map to add cannot be null or empty.");
        }

        /* SRS_TWIN_COLLECTION_21_005: [The putAll shall copy all entries in the provided Map to the TwinCollection.] */
        for(Map.Entry<? extends String, ?> entry: map.entrySet())
        {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Add a single new entry in the TwinCollection.
     *
     * <p> Override {@code HashMap.put(String, Object)}.
     *
     * <p> This function will add a single pair key value to the TwinCollection. By the
     *     Twin definition, the {@code Object} can contain types of {@code Boolean},
     *     {@code Number}, {@code String}, {@code Object}, or up to 5 levels of
     *     sub-TwinCollection, but it cannot be types defined by the user or arrays.
     *
     * @param key the {@code String} that represent the key of the new entry. It cannot be {#code null} or empty.
     * @param value the {@code Object} that represents the value of the new entry. It cannot be user defined type or array.
     * @return The {@code Object} that correspond to the last value of this key. It will be {@code null} if there is no previous value.
     */
    @Override
    public Object put(String key, Object value)
    {
        if (key == null || key.isEmpty())
        {
            /* SRS_TWIN_COLLECTION_21_010: [The put shall throw IllegalArgumentException if the provided key is null, empty, or invalid, or if the value is invalid.] */
            throw new IllegalArgumentException("Key cannot be null or empty");
        }

        /* SRS_TWIN_COLLECTION_21_006: [The put shall return the previous value of the key.] */
        Object last = get(key);
        /* SRS_TWIN_COLLECTION_21_007: [The put shall add the new pair key value to the TwinCollection.] */
        if(value instanceof Map)
        {
            /* SRS_TWIN_COLLECTION_21_008: [If the value contain a Map, the put shall convert this map in inner TwinCollection.] */
            super.put(key, new TwinCollection((Map<? extends String, Object>)value));
        }
        else
        {
            super.put(key, value);
        }

        /* SRS_TWIN_COLLECTION_21_009: [The put shall throw IllegalArgumentException if the final collection contain more that 5 levels.] */
        /* Codes_SRS_TWIN_COLLECTION_34_028: [The put shall not validate the map if the provided key is a metadata tag, or a version tag.] */
        if (!key.equals(VERSION_TAG) && !key.equals(METADATA_TAG))
        {
            ParserUtility.validateMap(this, MAX_TWIN_LEVEL, true);
        }

        return last;
    }

    /**
     * Internal Constructor from raw map.
     *
     * <p> This internal constructor is used to the deserialization process.
     *
     * <p> During the deserialization process, the GSON will convert both tags and
     *     properties to a raw Map, which will includes the $version and $metadata
     *     as part of the collection. So, we need to reorganize this map using the
     *     TwinCollection format. This constructor will do that.
     *
     * <p> For instance, the following JSON is a valid TwinCollection with its metadata.
     * <pre>
     * {@code
     * {
     *     "Color":"White",
     *     "MaxSpeed":{
     *         "Value":500,
     *         "NewValue":300
     *     },
     *     "$metadata":{
     *         "$lastUpdated":"2017-09-21T02:07:44.238Z",
     *         "$lastUpdatedVersion":4,
     *         "Color":{
     *             "$lastUpdated":"2017-09-21T02:07:44.238Z",
     *             "$lastUpdatedVersion":4,
     *         },
     *         "MaxSpeed":{
     *             "$lastUpdated":"2017-09-21T02:07:44.238Z",
     *             "$lastUpdatedVersion":4,
     *             "Value":{
     *                 "$lastUpdated":"2017-09-21T02:07:44.238Z",
     *                 "$lastUpdatedVersion":4
     *             },
     *             "NewValue":{
     *                 "$lastUpdated":"2017-09-21T02:07:44.238Z",
     *                 "$lastUpdatedVersion":4
     *             }
     *         }
     *     },
     *     "$version":4
     * }
     * }
     * </pre>
     *
     * @param rawCollection the {@code Map<? extends String, Object>} with contain all TwinCollection information, without
     *                      any differentiation between each entity is the Twin information and each entity is part
     *                      of the Twin metadata.
     * @return The instance of the {@link TwinCollection}.
     * @throws IllegalArgumentException If the provided rowCollection contain an invalid parameter.
     */
    protected static TwinCollection createFromRawCollection(Map<? extends String, Object> rawCollection)
    {
        TwinCollection twinCollection = new TwinCollection();
        Map<? extends String, Object> metadata = null;

        /* SRS_TWIN_COLLECTION_21_011: [The constructor shall convert the provided rawCollection in a valid TwinCollection.] */
        for (Map.Entry<? extends String, Object> entry: rawCollection.entrySet())
        {
            /* SRS_TWIN_COLLECTION_21_012: [If the entity contain the key `$version`, the constructor shall set the version with the value of this entity.] */
            if(entry.getKey().equals(VERSION_TAG))
            {
                /* SRS_TWIN_COLLECTION_21_013: [The constructor shall throw IllegalArgumentException if the entity contain the key `$version` and its value is not a integer.] */
                if(!(entry.getValue() instanceof Number))
                {
                    throw new IllegalArgumentException("version is not a number");
                }
                twinCollection.version = ((Number) entry.getValue()).intValue();
            }
            /* SRS_TWIN_COLLECTION_21_014: [If the entity contain the key `$metadata`, the constructor shall create a TwinMetadata with the value of this entity.] */
            else if(entry.getKey().equals(METADATA_TAG))
            {
                metadata = (Map<? extends String, Object>)entry.getValue();
            }
            else
            {
                /* SRS_TWIN_COLLECTION_21_015: [The constructor shall throw IllegalArgumentException if the Twin collection contain more than 5 levels.] */
                twinCollection.put(entry.getKey(), entry.getValue());
            }
        }

        if(metadata != null)
        {
            TwinCollection.addMetadata(twinCollection, metadata);
        }

        return twinCollection;
    }


    private static void addMetadata(TwinCollection twinCollection, Map<? extends String, Object> metadata)
    {
        String lastUpdated = null;
        Integer lastUpdatedVersion = null;
        for(Map.Entry<? extends String, Object> entry: metadata.entrySet())
        {
            String key = entry.getKey();
            if(key.equals(TwinMetadata.LAST_UPDATE_TAG))
            {
                lastUpdated = (String)entry.getValue();
            }
            else if((key.equals(TwinMetadata.LAST_UPDATE_VERSION_TAG))&& (entry.getValue() instanceof Number))
            {
                lastUpdatedVersion = ((Number)entry.getValue()).intValue();
            }
            else
            {
                /* SRS_TWIN_COLLECTION_21_024: [The constructor shall throw IllegalArgumentException if the metadata is inconsistent with the TwinCollection.] */
                Object valueInCollection = twinCollection.get(key);
                if(valueInCollection == null)
                {
                    throw new IllegalArgumentException("Twin metadata is inconsistent");
                }
                TwinMetadata twinMetadata = TwinMetadata.tryExtractFromMap(entry.getValue());
                if(twinMetadata != null)
                {
                    twinCollection.metadataMap.put(key, twinMetadata);
                }
                if(valueInCollection instanceof TwinCollection)
                {
                    TwinCollection.addMetadata((TwinCollection)valueInCollection, (Map<? extends String, Object>)entry.getValue());
                }
            }
        }
        if((lastUpdatedVersion != null) || !Tools.isNullOrEmpty(lastUpdated))
        {
            twinCollection.twinMetadata = new TwinMetadata(lastUpdated, lastUpdatedVersion);
        }
    }

    /**
     * Serializer
     *
     * <p> Creates a {@code JsonElement}, which the content represents
     *     the information in this class and its subclasses in a JSON format.
     *
     * <p> This is useful if the caller will integrate this JSON with JSON from
     *     other classes to generate a consolidated JSON.
     *
     * @return The {@code JsonElement} with the content of this class.
     */
    public JsonElement toJsonElement()
    {
        /* SRS_TWIN_COLLECTION_21_016: [The toJsonElement shall return a JsonElement with the information in this class in a JSON format.] */
        /* SRS_TWIN_COLLECTION_21_017: [The toJsonElement shall not include any metadata in the returned JsonElement.] */
        return ParserUtility.mapToJsonElement(this);
    }

    /**
     * Serializer with metadata.
     *
     * <p> Return a JsonElement with the full content of this class,
     *     including the metadata.
     *
     * @return The {@code JsonElement} with the full content of this class.
     */
    protected JsonElement toJsonElementWithMetadata()
    {
        /* SRS_TWIN_COLLECTION_21_018: [The toJsonElementWithMetadata shall return a JsonElement with the information in this class in a JSON format.] */
        JsonObject jsonObject = ParserUtility.mapToJsonElement(this).getAsJsonObject();

        /* SRS_TWIN_COLLECTION_21_019: [If version is not null, the toJsonElementWithMetadata shall include the $version in the returned jsonElement.] */
        if(this.version != null)
        {
            jsonObject.addProperty(VERSION_TAG, this.version);
        }

        /* SRS_TWIN_COLLECTION_21_020: [If twinMetadata is not null, the toJsonElementWithMetadata shall include the $metadata in the returned jsonElement.] */
        JsonObject jsonMetadata = new JsonObject();
        this.fillJsonMetadata(jsonMetadata);
        if(!jsonMetadata.entrySet().isEmpty())
        {
            jsonObject.add(METADATA_TAG, jsonMetadata);
        }

        return jsonObject;
    }

    private void fillJsonMetadata(JsonObject jsonMetadata)
    {
        if(this.twinMetadata != null)
        {
            jsonMetadata.addProperty(TwinMetadata.LAST_UPDATE_TAG, ParserUtility.dateTimeUtcToString(this.twinMetadata.getLastUpdated()));
            jsonMetadata.addProperty(TwinMetadata.LAST_UPDATE_VERSION_TAG, this.twinMetadata.getLastUpdatedVersion());
        }

        for(Map.Entry<String, TwinMetadata> entry: this.metadataMap.entrySet())
        {
            if(entry.getValue() != null)
            {
                JsonObject subMapJson = entry.getValue().toJsonElement().getAsJsonObject();
                Object value = get(entry.getKey());
                if (value instanceof TwinCollection)
                {
                    ((TwinCollection) value).fillJsonMetadata(subMapJson);
                }
                jsonMetadata.add(entry.getKey(), subMapJson);
            }
        }
    }

    /**
     * Getter for the version.
     *
     * @return The {@code Integer} with the version content. It can be {@code null}.
     */
    public Integer getVersion()
    {
        /* SRS_TWIN_COLLECTION_21_021: [The getVersion shall return a Integer with the stored version.] */
        return this.version;
    }

    /**
     * Getter for the TwinCollection metadata
     *
     * @return the {@link TwinMetadata} of the Whole TwinCollection. It can be {@code null}.
     */
    public TwinMetadata getTwinMetadata()
    {
        /* SRS_TWIN_COLLECTION_21_022: [The getTwinMetadata shall return the metadata of the whole TwinCollection.] */
        if(this.twinMetadata == null)
        {
            return null;
        }
        return new TwinMetadata(this.twinMetadata);
    }

    /**
     * Getter for the entry metadata in the TwinCollection.
     *
     * @param key the {@code String} with the name of the entry to retrieve the metadata.
     * @return the {@link TwinMetadata} ot the specific entry in the TwinCollection. It can be {@code null}.
     */
    public TwinMetadata getTwinMetadata(String key)
    {
        /* SRS_TWIN_COLLECTION_21_023: [The getTwinMetadata shall return the metadata of the entry that correspond to the provided key.] */
        if(this.metadataMap.get(key) == null)
        {
            return null;
        }
        return new TwinMetadata(this.metadataMap.get(key));
    }

    /**
     * Creates a pretty print JSON with the content of this class and subclasses.
     *
     * @return The {@code String} with the pretty print JSON.
     */
    @Override
    public String toString()
    {
        /* SRS_TWIN_COLLECTION_21_024: [The toString shall return a String with the information in this class in a pretty print JSON.] */
        return toJsonElementWithMetadata().toString();
    }
}
