// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.twin;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Representation of a single Twin.
 *
 * <p> The Twin can contain one {@link TwinCollection} of <b>Tags</b>, and one
 *     {@link TwinCollection} of <b>properties.desired</b>.
 *
 * <p> Each entity in the collections can contain a associated {@link TwinMetadata}.
 *
 * <p> These metadata are provided by the Service and contains information about the last
 *     updated date time, and version.
 *
 * <p> For instance, the following is a valid Twin, represented as
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
@SuppressWarnings("unused") // A number of private members are unused but may be filled in or used by serialization
public class Twin
{
    // the twin desired properties
    private static final String PROPERTIES_TAG = "properties";
    @Expose(serialize = false)
    @SerializedName(PROPERTIES_TAG)
    private TwinProperties properties;

    /**
     * CONSTRUCTOR
     *
     * <p> This constructor creates an instance of the Twin with the provided {@link TwinCollection}
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
     * @param desiredProperty the {@link TwinCollection} with the desired properties. It can be {@code null}.
     * @param reportedProperty the {@link TwinCollection} with the reported properties. It can be {@code null}.
     */
    public Twin(TwinCollection desiredProperty, TwinCollection reportedProperty)
    {
        if (desiredProperty != null || reportedProperty != null)
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
        Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                .serializeNulls()
                .create();

        JsonElement json = gson.toJsonTree(this).getAsJsonObject();

        // Since null values are lost when building the json tree, need to manually re-add properties as reported
        // properties may have contained a property with a null value. Those must be preserved so users can delete
        // properties.
        if (json != null && this.properties != null)
        {
            json.getAsJsonObject().add("properties", this.properties.toJsonElement());
        }

        return json;
    }

    /**
     * Getter for the desired property.
     *
     * @return The {@code TwinCollection} with the desired property content. It can be {@code null}.
     */
    public TwinCollection getDesiredProperties()
    {
        if (this.properties == null)
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
    public TwinCollection getReportedProperties()
    {
        if (this.properties == null)
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
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();

        JsonObject jsonObject = gson.toJsonTree(this).getAsJsonObject();

        if (this.properties != null)
        {
            jsonObject.add(PROPERTIES_TAG, this.properties.toJsonElementWithMetadata());
        }

        return jsonObject.toString();
    }

    /**
     * Factory
     *
     * <p> Create a new instance of the Twin parsing the provided string as a JSON with the full Twin information.
     *
     * @param json the {@code String} with the JSON received from the service. It cannot be {@code null} or empty.
     * @return The new instance of the {@code Twin}.
     */
    public static Twin createFromTwinJson(String json)
    {
        if (Tools.isNullOrEmpty(json))
        {
            throw new IllegalArgumentException("JSON with result is null or empty");
        }

        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                .disableHtmlEscaping()
                .create();
        Twin result = gson.fromJson(json, Twin.class);

        /*
         * During the deserialization process, the GSON will convert both tags and
         * properties to a raw Map, which will includes the $version and $metadata
         * as part of the collection. So, we need to reorganize this map using the
         * TwinCollection format. This constructor will do that.
         */
        if (result.properties != null)
        {
            result.properties = new TwinProperties(result.properties.getDesired(), result.properties.getReported());
        }

        return result;
    }

    /**
     * Factory
     *
     * <p> Create a new instance of the Twin parsing the provided string as a JSON with only desired properties information.
     *
     * @param json the {@code String} with the JSON received from the service. It cannot be {@code null} or empty.
     * @return The new instance of the {@code Twin}.
     */
    public static Twin createFromDesiredPropertyJson(String json)
    {
        if (Tools.isNullOrEmpty(json))
        {
            throw new IllegalArgumentException("JSON with result is null or empty");
        }

        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                .disableHtmlEscaping()
                .create();

        TwinCollection result = gson.fromJson(json, TwinCollection.class);

        return new Twin(result, null);
    }

    /**
     * Factory
     *
     * <p> Create a new instance of the Twin parsing the provided string as a JSON with only reported properties information.
     *
     * @param json the {@code String} with the JSON received from the service. It cannot be {@code null} or empty.
     * @return The new instance of the {@code Twin}.
     */
    public static Twin createFromReportedPropertyJson(String json)
    {
        if (Tools.isNullOrEmpty(json))
        {
            throw new IllegalArgumentException("JSON with result is null or empty");
        }

        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                .disableHtmlEscaping()
                .create();

        TwinCollection result = gson.fromJson(json, TwinCollection.class);

        return new Twin(null, result);
    }

    /**
     * Factory
     *
     * <p> Create a new instance of the Twin parsing the provided string as a JSON with only desired properties information.
     *
     * @param json the {@code String} with the JSON received from the service. It cannot be {@code null} or empty.
     * @return The new instance of the {@code Twin}.
     */
    public static Twin createFromPropertiesJson(String json)
    {
        if (Tools.isNullOrEmpty(json))
        {
            throw new IllegalArgumentException("JSON with result is null or empty");
        }

        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                .disableHtmlEscaping()
                .create();
        
        TwinProperties result = gson.fromJson(json, TwinProperties.class);

        return new Twin(result.getDesired(), result.getReported());
    }

    /**
     * Empty constructor
     *
     * <p>
     *     Used only by the tools that will deserialize this class.
     * </p>
     */
    @SuppressWarnings("unused")
    Twin()
    {
    }
}
