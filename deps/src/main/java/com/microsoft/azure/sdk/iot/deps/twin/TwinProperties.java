// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.twin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Representation of a single Twin Properties for the {@link TwinState}.
 *
 * <p> The Properties on the TwinState shall contains one {@link TwinCollection} of <b>desired</b> property.
 *
 * <p> The desired property is a collection that can contain a associated {@link TwinMetadata}.
 *
 * <p> These metadata are provided by the Service and contains information about the last
 *     updated date time, and version.
 *
 * <p> For instance, the following is a valid desired property, represented as
 *     {@code properties.desired} in the rest API.
 * <pre>
 *     {@code
 *      {
 *          "desired": {
 *              "MaxSpeed":{
 *                  "Value":500,
 *                  "NewValue":300
 *              },
 *              "$metadata":{
 *                  "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *                  "$lastUpdatedVersion":4,
 *                  "MaxSpeed":{
 *                      "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *                      "$lastUpdatedVersion":4,
 *                      "Value":{
 *                          "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *                          "$lastUpdatedVersion":4
 *                      },
 *                      "NewValue":{
 *                          "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *                          "$lastUpdatedVersion":4
 *                      }
 *                  }
 *              },
 *              "$version":4
 *          },
 *          "reported": {
 *              "MaxSpeed":{
 *                  "Value":500,
 *                  "NewValue":300
 *              },
 *              "$metadata":{
 *                  "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *                  "$lastUpdatedVersion":5,
 *                  "MaxSpeed":{
 *                      "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *                      "$lastUpdatedVersion":4,
 *                      "Value":{
 *                          "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *                          "$lastUpdatedVersion":5
 *                      },
 *                      "NewValue":{
 *                          "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *                          "$lastUpdatedVersion":4
 *                      }
 *                  }
 *              },
 *              "$version":6
 *          }
 *      }
 *     }
 * </pre>
 *
 * @see <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-device-twins">Understand and use device twins in IoT Hub</a>
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iothub/devicetwinapi">Device Twin Api</a>
 */
public class TwinProperties
{
    // the twin desired properties
    private static final String DESIRED_PROPERTIES_TAG = "desired";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(DESIRED_PROPERTIES_TAG)
    private TwinCollection desired;

    // the twin reported properties
    private static final String REPORTED_PROPERTIES_TAG = "reported";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(REPORTED_PROPERTIES_TAG)
    private TwinCollection reported;

    /**
     * CONSTRUCTOR
     *
     * <p> This constructor creates an instance of the TwinProperties with the provided {@link TwinCollection}
     *     desired property.
     *
     * <p> When serialized, this class will looks like the following example:
     * <pre>
     * {@code
     *  "desired":{
     *      "MaxSpeed":{
     *          "Value":500,
     *          "NewValue":300
     *      },
     *      "$version":4
     *  },
     *  "reported":{
     *      "MaxSpeed":{
     *          "Value":500,
     *          "NewValue":300
     *      },
     *      "$version":4
     *  }
     * }
     * </pre>
     *
     * @param desired the {@link TwinCollection} with the desired property. It cannot be {@code null}.
     * @param reported the {@link TwinCollection} with the reported property. It cannot be {@code null}.
     * @exception IllegalArgumentException if both desired and reported properties are {@code null}.
     */
    TwinProperties(TwinCollection desired, TwinCollection reported)
    {
        /* SRS_TWIN_PROPERTIES_21_001: [The constructor shall throw IllegalArgumentException if the provided desired and reported properties is null.] */
        if((desired == null) && (reported == null))
        {
            throw new IllegalArgumentException("Desired property cannot be null.");
        }

        /* SRS_TWIN_PROPERTIES_21_002: [The constructor shall store the provided desired and reported properties converting from the row collection.] */
        if(desired != null)
        {
            this.desired = TwinCollection.createFromRawCollection(desired);
        }
        if(reported != null)
        {
            this.reported = TwinCollection.createFromRawCollection(reported);
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
    protected JsonElement toJsonElement()
    {
        /* SRS_TWIN_PROPERTIES_21_003: [The toJsonElement shall return a JsonElement with the information in this class in a JSON format.] */
        JsonObject twinJson = new JsonObject();

        /* SRS_TWIN_PROPERTIES_21_004: [If the desired property is null, the toJsonElement shall not include the `desired` in the final JSON.] */
        if(this.desired != null)
        {
            twinJson.add(DESIRED_PROPERTIES_TAG, this.desired.toJsonElement());
        }

        /* SRS_TWIN_PROPERTIES_21_005: [If the reported property is null, the toJsonElement shall not include the `reported` in the final JSON.] */
        if(this.reported != null)
        {
            twinJson.add(REPORTED_PROPERTIES_TAG, this.reported.toJsonElement());
        }

        return twinJson;
    }

    /**
     * Serializer
     *
     * <p>
     *     Creates a {@code JsonElement}, which the content represents
     *     the information in this class and its subclasses in a JSON format.
     *
     *     If the desired property contains metadata, this method will include
     *     it in the final JSON.
     *
     *     This is useful if the caller will integrate this JSON with JSON from
     *     other classes to generate a consolidated JSON.
     * </p>

     * @return The {@code JsonElement} with the content of this class.
     */
    protected JsonElement toJsonElementWithMetadata()
    {
        /* SRS_TWIN_PROPERTIES_21_006: [The toJsonElementWithMetadata shall return a JsonElement with the information in this class, including metadata, in a JSON format.] */
        JsonObject twinJson = new JsonObject();

        /* SRS_TWIN_PROPERTIES_21_007: [If the desired property is null, the toJsonElementWithMetadata shall not include the `desired` in the final JSON.] */
        if(this.desired != null)
        {
            twinJson.add(DESIRED_PROPERTIES_TAG, this.desired.toJsonElementWithMetadata());
        }

        /* SRS_TWIN_PROPERTIES_21_008: [If the reported property is null, the toJsonElementWithMetadata shall not include the `reported` in the final JSON.] */
        if(this.reported != null)
        {
            twinJson.add(REPORTED_PROPERTIES_TAG, this.reported.toJsonElementWithMetadata());
        }

        return twinJson;
    }

    /**
     * Getter for the desired property.
     *
     * @return The {@code TwinCollection} with the desired property content. It can be {@code null}.
     */
    public TwinCollection getDesired()
    {
        /* SRS_TWIN_PROPERTIES_21_009: [The getDesired shall return a TwinCollection with the stored desired property.] */
        if(this.desired == null)
        {
            return null;
        }
        return new TwinCollection(this.desired);
    }

    /**
     * Getter for the reported property.
     *
     * @return The {@code TwinCollection} with the reported property content. It can be {@code null}.
     */
    public TwinCollection getReported()
    {
        /* SRS_TWIN_PROPERTIES_21_010: [The getReported shall return a TwinCollection with the stored reported property.] */
        if(this.reported == null)
        {
            return null;
        }
        return new TwinCollection(this.reported);
    }

    /**
     * Creates a pretty print JSON with the content of this class and subclasses.
     *
     * @return The {@code String} with the pretty print JSON.
     */
    @Override
    public String toString()
    {
        /* SRS_TWIN_PROPERTIES_21_011: [The toString shall return a String with the information in this class in a pretty print JSON.] */
        return toJsonElementWithMetadata().toString();
    }

    /**
     * Empty constructor
     *
     * <p>
     *     Used only by the tools that will deserialize this class.
     * </p>
     */
    @SuppressWarnings("unused")
    protected TwinProperties()
    {
        /* SRS_TWIN_PROPERTIES_21_012: [The TwinProperties shall provide an empty constructor to make GSON happy.] */
    }
}
