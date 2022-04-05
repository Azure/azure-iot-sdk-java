// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.twin;

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
    @Expose
    @SerializedName(DESIRED_PROPERTIES_TAG)
    private TwinCollection desired;

    // the twin reported properties
    private static final String REPORTED_PROPERTIES_TAG = "reported";
    @Expose
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
        if ((desired == null) && (reported == null))
        {
            throw new IllegalArgumentException("Desired property cannot be null.");
        }

        if (desired != null)
        {
            this.desired = TwinCollection.createFromRawCollection(desired);
            if (desired.getVersion() != null)
            {
                this.desired.setVersion(desired.getVersion());
            }
        }
        if (reported != null)
        {
            this.reported = TwinCollection.createFromRawCollection(reported);
            if (reported.getVersion() != null)
            {
                this.reported.setVersion(reported.getVersion());
            }
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
        JsonObject twinJson = new JsonObject();

        if (this.desired != null)
        {
            twinJson.add(DESIRED_PROPERTIES_TAG, this.desired.toJsonObject());
        }

        if (this.reported != null)
        {
            twinJson.add(REPORTED_PROPERTIES_TAG, this.reported.toJsonObject());
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
    JsonElement toJsonElementWithMetadata()
    {
        JsonObject twinJson = new JsonObject();

        if (this.desired != null)
        {
            twinJson.add(DESIRED_PROPERTIES_TAG, this.desired.toJsonElementWithMetadata());
        }

        if (this.reported != null)
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
        return this.desired;
    }

    /**
     * Getter for the reported property.
     *
     * @return The {@code TwinCollection} with the reported property content. It can be {@code null}.
     */
    public TwinCollection getReported()
    {
        return this.reported;
    }

    /**
     * Creates a pretty print JSON with the content of this class and subclasses.
     *
     * @return The {@code String} with the pretty print JSON.
     */
    @Override
    public String toString()
    {
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
    TwinProperties()
    {
    }
}
