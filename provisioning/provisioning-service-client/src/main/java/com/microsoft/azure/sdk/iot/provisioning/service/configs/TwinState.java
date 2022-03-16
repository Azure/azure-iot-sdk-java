// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.io.Serializable;

/**
 * Representation of a single Twin initial state for the Device Provisioning Service.
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
 *              }
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
 *                  }
 *              }
 *          }
 *      }
 *     }
 * </pre>
 *
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollmentgroup">Device Enrollment Group</a>
 * @see <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-device-twins">Understand and use device twins in IoT Hub</a>
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iothub/devicetwinapi">Device Twin Api</a>
 */
public class TwinState implements Serializable
{
    // the twin tags
    private static final String TAGS_TAG = "tags";
    @Expose
    @SerializedName(TAGS_TAG)
    @Getter
    private TwinCollection tags;

    // the twin desired properties
    private static final String DESIRED_PROPERTIES_TAG = "properties";
    @Expose
    @SerializedName(DESIRED_PROPERTIES_TAG)
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
     * @param desiredProperty the {@link TwinCollection} with the initial desired properties. It can be {@code null}.
     */
    public TwinState(TwinCollection tags, TwinCollection desiredProperty)
    {
        /* SRS_TWIN_STATE_21_001: [The constructor shall store the provided tags and desiredProperty.] */
        if (tags != null)
        {
            this.tags = TwinCollection.createFromRawCollection(tags);
        }
        if (desiredProperty != null)
        {
            this.properties = new TwinProperties(desiredProperty);
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
    JsonElement toJsonElement()
    {
        /* SRS_TWIN_STATE_21_002: [The toJsonElement shall return a JsonElement with the information in this class in a JSON format.] */
        JsonObject twinJson = new JsonObject();

        /* SRS_TWIN_STATE_21_003: [If the tags is null, the toJsonElement shall not include the `tags` in the final JSON.] */
        if (this.tags != null)
        {
            twinJson.add(TAGS_TAG, this.tags.toJsonElement());
        }

        /* SRS_TWIN_STATE_21_004: [If the property is null, the toJsonElement shall not include the `properties` in the final JSON.] */
        if (this.properties != null)
        {
            twinJson.add(DESIRED_PROPERTIES_TAG, this.properties.toJsonElement());
        }

        return twinJson;
    }

    /**
     * Getter for the desired property.
     *
     * @return The {@code TwinCollection} with the desired property content. It can be {@code null}.
     */
    public TwinCollection getDesiredProperty()
    {
        /* SRS_TWIN_STATE_21_006: [The getDesiredProperty shall return a TwinCollection with the stored desired property.] */
        if (this.properties == null)
        {
            return null;
        }
        return this.properties.getDesired();
    }

    /**
     * Creates a pretty print JSON with the content of this class and subclasses.
     *
     * @return The {@code String} with the pretty print JSON.
     */
    @Override
    public String toString()
    {
        /* SRS_TWIN_STATE_21_007: [The toString shall return a String with the information in this class in a pretty print JSON.] */
        JsonObject twinJson = new JsonObject();

        /* SRS_TWIN_STATE_21_008: [If the tags is null, the JSON shall not include the `tags`.] */
        if (this.tags != null)
        {
            twinJson.add(TAGS_TAG, this.tags.toJsonElementWithMetadata());
        }

        /* SRS_TWIN_STATE_21_009: [If the properties is null, the JSON shall not include the `properties`.] */
        if (this.properties != null)
        {
            twinJson.add(DESIRED_PROPERTIES_TAG, this.properties.toJsonElementWithMetadata());
        }

        return twinJson.toString();
    }

    /**
     * Empty constructor
     *
     * <p>
     *     Used only by the tools that will deserialize this class.
     * </p>
     */
    @SuppressWarnings("unused")
    TwinState()
    {
        /* SRS_TWIN_STATE_21_010: [The TwinState shall provide an empty constructor to make GSON happy.] */
    }
}
