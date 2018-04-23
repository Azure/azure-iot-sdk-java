// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.twin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.deps.util.Tools;

/**
 * Representation of a single Twin.
 *
 * <p> The TwinState can contain one {@link TwinCollection} of <b>Tags</b>, and one
 *     {@link TwinCollection} of <b>properties.desired</b>.
 *
 * <p> Each entity in the collections can contain a associated {@link TwinMetadata}.
 *
 * <p> These metadata are provided by the Service and contains information about the last
 *     updated date time, and version.
 *
 * <p> For instance, the following is a valid TwinState, represented as
 *     {@code initialTwin} in the rest API.
 * <pre>
 *     {@code
 *      {
 *          "initialTwin": {
 *              "tags":{
 *                  "SpeedUnity":"MPH",
 *                  "$metadata":{
 *                      "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *                      "$lastUpdatedVersion":4,
 *                      "SpeedUnity":{
 *                          "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *                          "$lastUpdatedVersion":4
 *                      }
 *                  },
 *                  "$version":4
 *              },
 *              "properties":{
 *                  "desired": {
 *                      "MaxSpeed":{
 *                          "Value":500,
 *                          "NewValue":300
 *                      },
 *                      "$metadata":{
 *                          "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *                          "$lastUpdatedVersion":4,
 *                          "MaxSpeed":{
 *                              "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *                              "$lastUpdatedVersion":4,
 *                              "Value":{
 *                                  "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *                                  "$lastUpdatedVersion":4
 *                              },
 *                              "NewValue":{
 *                                  "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *                                  "$lastUpdatedVersion":4
 *                              }
 *                          }
 *                      },
 *                      "$version":4
 *                  },
 *                  "reported": {
 *                      "MaxSpeed":{
 *                          "Value":500,
 *                          "NewValue":300
 *                      },
 *                      "$metadata":{
 *                          "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *                          "$lastUpdatedVersion":5,
 *                          "MaxSpeed":{
 *                              "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *                              "$lastUpdatedVersion":4,
 *                              "Value":{
 *                                  "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *                                  "$lastUpdatedVersion":5
 *                              },
 *                              "NewValue":{
 *                                  "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *                                  "$lastUpdatedVersion":4
 *                              }
 *                          }
 *                      },
 *                      "$version":6
 *                  }
 *              }
 *          }
 *      }
 *     }
 * </pre>
 *
 * @see <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-device-twins">Understand and use device twins in IoT Hub</a>
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iothub/devicetwinapi">Device Twin Api</a>
 */
public class TwinState extends RegisterManager
{
    // the twin tags
    private static final String TAGS_TAG = "tags";
    @Expose(serialize = false, deserialize = true)
    @SerializedName(TAGS_TAG)
    private TwinCollection tags;

    // the twin desired properties
    private static final String PROPERTIES_TAG = "properties";
    @Expose(serialize = false, deserialize = true)
    @SerializedName(PROPERTIES_TAG)
    private TwinProperties properties;


    /**
     * CONSTRUCTOR
     *
     * <p> This constructor creates an instance of the TwinState with the provided {@link TwinCollection}
     *     tags and desired properties.
     *
     * <p> When serialized, this class will looks like the following example:
     * <pre>
     *     {@code
     *          "initialTwin": {
     *              "tags":{
     *                  "SpeedUnity":"MPH",
     *                  "$version":4
     *              }
     *              "properties":{
     *                  "desired":{
     *                      "MaxSpeed":{
     *                          "Value":500,
     *                          "NewValue":300
     *                      },
     *                      "$version":4
     *                  }
     *              }
     *          }
     *      }
     *     }
     * </pre>
     *
     * @param tags the {@link TwinCollection} with the initial tags state. It can be {@code null}.
     * @param desiredProperty the {@link TwinCollection} with the desired properties. It can be {@code null}.
     * @param reportedProperty the {@link TwinCollection} with the reported properties. It can be {@code null}.
     */
    public TwinState(TwinCollection tags, TwinCollection desiredProperty, TwinCollection reportedProperty)
    {
        /* SRS_TWIN_STATE_21_001: [The constructor shall store the provided tags, desiredProperty, and reportedProperty.] */
        if(tags != null)
        {
            this.tags = TwinCollection.createFromRawCollection(tags);
        }
        if((desiredProperty != null) || (reportedProperty != null))
        {
            this.properties = new TwinProperties(desiredProperty, reportedProperty);
        }
    }

    /**
     * Serializer
     *
     * <p>
     *     Creates a {@code JsonElement}, which the content represents
     *     the information in this class and its subclasses in a JSON format.
     *
     *     This is useful if the caller will integrate this JSON with JSON from
     *     other classes to generate a consolidated JSON.
     * </p>

     * @return The {@code JsonElement} with the content of this class.
     */
    public JsonElement toJsonElement()
    {
        /* SRS_TWIN_STATE_21_002: [The toJsonElement shall return a JsonElement with the information in this class in a JSON format.] */
        /* SRS_TWIN_STATE_21_003: [If the tags is null, the toJsonElement shall not include the `tags` in the final JSON.] */
        /* SRS_TWIN_STATE_21_004: [If the property is null, the toJsonElement shall not include the `properties` in the final JSON.] */
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        JsonElement json = gson.toJsonTree(this).getAsJsonObject();

        //since null values are lost when building the json tree, need to manually re-add properties as reported properties
        // may have contained a property with a null value. Those must be preserved so users can delete properties
        if (json != null && this.properties != null)
        {
            //Codes_SRS_TWIN_STATE_34_024: [The json element shall include all null desired and reported properties.]
            json.getAsJsonObject().add("properties", this.properties.toJsonElement());
        }

        return json;
    }

    /**
     * Getter for the tags.
     *
     * @return The {@code TwinCollection} with the tags content. It can be {@code null}.
     */
    public TwinCollection getTags()
    {
        /* SRS_TWIN_STATE_21_005: [The getTags shall return a TwinCollection with the stored tags.] */
        return new TwinCollection(this.tags);
    }

    /**
     * Getter for the desired property.
     *
     * @return The {@code TwinCollection} with the desired property content. It can be {@code null}.
     */
    public TwinCollection getDesiredProperty()
    {
        /* SRS_TWIN_STATE_21_006: [The getDesiredProperty shall return a TwinCollection with the stored desired property.] */
        if(this.properties == null)
        {
            return null;
        }
        return this.properties.getDesired();
    }

    /**
     * Getter for the reported property.
     *
     * @return The {@code TwinCollection} with the reported property content. It can be {@code null}.
     */
    public TwinCollection getReportedProperty()
    {
        /* SRS_TWIN_STATE_21_007: [The getReportedProperty shall return a TwinCollection with the stored reported property.] */
        if(this.properties == null)
        {
            return null;
        }
        return this.properties.getReported();
    }

    /**
     * Creates a pretty print JSON with the content of this class and subclasses.
     *
     * @return The {@code String} with the pretty print JSON.
     */
    @Override
    public String toString()
    {
        /* SRS_TWIN_STATE_21_008: [The toString shall return a String with the information in this class in a pretty print JSON.] */
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().disableHtmlEscaping().create();
        JsonObject jsonObject = gson.toJsonTree(this).getAsJsonObject();

        /* SRS_TWIN_STATE_21_009: [If the tags is null, the JSON shall not include the `tags`.] */
        if(this.tags != null)
        {
            jsonObject.add(TAGS_TAG, this.tags.toJsonElementWithMetadata());
        }

        /* SRS_TWIN_STATE_21_010: [If the properties is null, the JSON shall not include the `properties`.] */
        if(this.properties != null)
        {
            jsonObject.add(PROPERTIES_TAG, this.properties.toJsonElementWithMetadata());
        }

        return jsonObject.toString();
    }

    /**
     * Factory
     *
     * <p> Create a new instance of the TwinState parsing the provided string as a JSON with the full Twin information.
     *
     * @param json the {@code String} with the JSON received from the service. It cannot be {@code null} or empty.
     * @return The new instance of the {@code TwinState}.
     */
    public static TwinState createFromTwinJson(String json)
    {
        /* SRS_TWIN_STATE_21_011: [The factory shall throw IllegalArgumentException if the JSON is null or empty.] */
        if(Tools.isNullOrEmpty(json))
        {
            throw new IllegalArgumentException("JSON with result is null or empty");
        }

        /* SRS_TWIN_STATE_21_012: [The factory shall throw JsonSyntaxException if the JSON is invalid.] */
        /* SRS_TWIN_STATE_21_013: [The factory shall deserialize the provided JSON for the twin class and subclasses.] */
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        TwinState result = gson.fromJson(json, TwinState.class);

        /*
         * During the deserialization process, the GSON will convert both tags and
         * properties to a raw Map, which will includes the $version and $metadata
         * as part of the collection. So, we need to reorganize this map using the
         * TwinCollection format. This constructor will do that.
         */
        result.tags = new TwinCollection(result.getTags());
        if(result.properties != null)
        {
            result.properties = new TwinProperties(result.properties.getDesired(), result.properties.getReported());
        }

        return result;
    }

    /**
     * Factory
     *
     * <p> Create a new instance of the TwinState parsing the provided string as a JSON with only desired properties information.
     *
     * @param json the {@code String} with the JSON received from the service. It cannot be {@code null} or empty.
     * @return The new instance of the {@code TwinState}.
     */
    public static TwinState createFromDesiredPropertyJson(String json)
    {
        /* SRS_TWIN_STATE_21_014: [The factory shall throw IllegalArgumentException if the JSON is null or empty.] */
        if(Tools.isNullOrEmpty(json))
        {
            throw new IllegalArgumentException("JSON with result is null or empty");
        }

        /* SRS_TWIN_STATE_21_015: [The factory shall throw JsonSyntaxException if the JSON is invalid.] */
        /* SRS_TWIN_STATE_21_016: [The factory shall deserialize the provided JSON for the Twin class and subclasses.] */
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        TwinCollection result = gson.fromJson(json, TwinCollection.class);

        return new TwinState(null, result, null);
    }

    /**
     * Factory
     *
     * <p> Create a new instance of the TwinState parsing the provided string as a JSON with only reported properties information.
     *
     * @param json the {@code String} with the JSON received from the service. It cannot be {@code null} or empty.
     * @return The new instance of the {@code TwinState}.
     */
    public static TwinState createFromReportedPropertyJson(String json)
    {
        /* SRS_TWIN_STATE_21_017: [The factory shall throw IllegalArgumentException if the JSON is null or empty.] */
        if(Tools.isNullOrEmpty(json))
        {
            throw new IllegalArgumentException("JSON with result is null or empty");
        }

        /* SRS_TWIN_STATE_21_018: [The factory shall throw JsonSyntaxException if the JSON is invalid.] */
        /* SRS_TWIN_STATE_21_019: [The factory shall deserialize the provided JSON for the Twin class and subclasses.] */
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        TwinCollection result = gson.fromJson(json, TwinCollection.class);

        return new TwinState(null, null, result);
    }

    /**
     * Factory
     *
     * <p> Create a new instance of the TwinState parsing the provided string as a JSON with only desired properties information.
     *
     * @param json the {@code String} with the JSON received from the service. It cannot be {@code null} or empty.
     * @return The new instance of the {@code TwinState}.
     */
    public static TwinState createFromPropertiesJson(String json)
    {
        /* SRS_TWIN_STATE_21_020: [The factory shall throw IllegalArgumentException if the JSON is null or empty.] */
        if(Tools.isNullOrEmpty(json))
        {
            throw new IllegalArgumentException("JSON with result is null or empty");
        }

        /* SRS_TWIN_STATE_21_021: [The factory shall throw JsonSyntaxException if the JSON is invalid.] */
        /* SRS_TWIN_STATE_21_022: [The factory shall deserialize the provided JSON for the Twin class and subclasses.] */
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        TwinProperties result = gson.fromJson(json, TwinProperties.class);

        return new TwinState(null, result.getDesired(), result.getReported());
    }

    /**
     * Empty constructor
     *
     * <p>
     *     Used only by the tools that will deserialize this class.
     * </p>
     */
    @SuppressWarnings("unused")
    protected TwinState()
    {
        /* SRS_TWIN_STATE_21_023: [The TwinState shall provide an empty constructor to make GSON happy.] */
    }
}
