// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Representation of a single Twin metadata for the Device Provisioning Service.
 *
 * <p> The metadata is a set of pairs lastUpdated/lastUpdatedVersion for each
 *     property and sub-property in the Twin. It is optionally provided by
 *     the provisioning service and the clients can only ready it.
 *     
 * <p> This class store the Date and Version for each entity in the {@link TwinCollection}.
 *
 * <p> For instance, the following is a valid TwinCollection with its metadata.
 * <pre>
 * {@code
 *  "$metadata":{
 *      "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *      "$lastUpdatedVersion":4,
 *      "MaxSpeed":{
 *          "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *          "$lastUpdatedVersion":3,
 *          "Value":{
 *              "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *              "$lastUpdatedVersion":5
 *          },
 *          "NewValue":{
 *              "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *              "$lastUpdatedVersion":5
 *          }
 *      }
 *  }
 * }
 * </pre>
 *
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
 * @see <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-device-twins">Understand and use device twins in IoT Hub</a>
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iothub/devicetwinapi">Device Twin Api</a>
 */
public class TwinMetadata implements Serializable
{
    // the entity last updated date and time in the TwinCollection
    static final String LAST_UPDATE_TAG = "$lastUpdated";

    @Getter
    private Date lastUpdated;

    // the entity last updated version in the TwinCollection
    static final String LAST_UPDATE_VERSION_TAG = "$lastUpdatedVersion";

    @Getter
    private final Integer lastUpdatedVersion;

    /**
     * CONSTRUCTOR
     *
     * <p> This private constructor will receive and store the metadata parameters.
     *
     * @param lastUpdated the {@code String} with the date and time UTC of the last update on the entity. It can be {@code null}, empty or invalid.
     * @param lastUpdatedVersion the {@code Integer} with the version of the last update on the entity. It can be {@code null}.
     * @throws IllegalArgumentException If no valid parameter was provide and the class will be empty, or if the DateTime is invalid.
     */
    TwinMetadata(String lastUpdated, Integer lastUpdatedVersion)
    {
        if (!(lastUpdated == null || lastUpdated.isEmpty()))
        {
            /* SRS_TWIN_METADATA_21_001: [The constructor shall parse the provided `lastUpdated` String to the Date and store it as the TwinMetadata lastUpdated.] */
            /* SRS_TWIN_METADATA_21_002: [The constructor shall throw IllegalArgumentException if it cannot convert the provided `lastUpdated` String to Date.] */
            this.lastUpdated = ParserUtility.getDateTimeUtc(lastUpdated);
        }

        /* SRS_TWIN_METADATA_21_003: [The constructor shall store the provided lastUpdatedVersion as is.] */
        this.lastUpdatedVersion = lastUpdatedVersion;

        if ((this.lastUpdatedVersion == null) && (this.lastUpdated == null))
        {
            throw new IllegalArgumentException("no valid data to create a TwinMetadata.");
        }
    }

    /**
     * Metadata extractor
     *
     * <p> This internal method will try to find $lastUpdated and $lastUpdatedVersion at the first
     *     level of the provided Map (Object), and create a new instance of the TwinMetadata with
     *     this information.
     *
     * <p> Once the provide Object can or cannot be a Map, and, if it is a Map, it can or cannot
     *     contains a valid metadata, this method contains the label <b>try</b>, which means that
     *     it can return a valid TwinMetadata or {@code null}.
     *
     * <p> For instance, for the following Map, this method will create a TwinMetadata with
     *     {@code lastUpdated = 2015-09-21T02:07:44.238Z} and {@code lastUpdatedVersion = 3}
     * <pre>
     * {@code
     * "$lastUpdated":"2015-09-21T02:07:44.238Z",
     * "$lastUpdatedVersion":3,
     * "Value":{
     *     "$lastUpdated":"2016-09-21T02:07:44.238Z",
     *     "$lastUpdatedVersion":5
     * },
     * "NewValue":{
     *     "$lastUpdated":"2017-09-21T02:07:44.238Z",
     *     "$lastUpdatedVersion":5
     * }
     * </pre>
     *
     * @param metadata the {@code Object} that may contains the metadata.
     * @return A valid TwinMetadata instance it the provided metadata {@code Object} is a Map with
     *         data and version metadata, or {@code null} for the other cases.
     * @throws IllegalArgumentException If no valid parameter was provide and the class will be empty, or if the DateTime is invalid.
     */
    // Unchecked casts of Maps to Map<String, Object> are safe as long as service is returning valid twin json payloads. Since all json keys are Strings, all maps must be Map<String, Object>
    @SuppressWarnings("unchecked")
    static TwinMetadata tryExtractFromMap(Object metadata)
    {
        /* SRS_TWIN_METADATA_21_004: [The tryExtractFromMap shall return null if the provided metadata is not a Map.] */
        if (!(metadata instanceof Map))
        {
            return null;
        }

        /* SRS_TWIN_METADATA_21_005: [If the provide metadata contains date or version, the tryExtractFromMap shall return a new instance of TwinMetadata with this information.] */
        /* SRS_TWIN_METADATA_21_006: [The tryExtractFromMap shall throw IllegalArgumentException if it cannot convert the provided `lastUpdated` String to Date or the version in a Number.] */
        String lastUpdated = null;
        Integer lastUpdatedVersion = null;
        for (Map.Entry<? extends String, Object> entry: ((Map<? extends String, Object>)metadata).entrySet())
        {
            String key = entry.getKey();
            if (key.equals(LAST_UPDATE_TAG))
            {
                lastUpdated = (String)entry.getValue();
            }
            else if (key.equals(LAST_UPDATE_VERSION_TAG))
            {
                if (!(entry.getValue() instanceof Number))
                {
                    throw new IllegalArgumentException("Version in the metadata shall be a number");
                }
                lastUpdatedVersion = ((Number)entry.getValue()).intValue();
            }
        }
        if ((lastUpdatedVersion != null) || !(lastUpdated == null || lastUpdated.isEmpty()))
        {
            return new TwinMetadata(lastUpdated, lastUpdatedVersion);
        }
        return null;
    }

    /**
     * Serializer
     *
     * <p>
     *     Creates a {@code JsonElement}, which the content represents
     *     the information in this class in a JSON format.
     *
     *     This is useful if the caller will integrate this JSON with JSON from
     *     other classes to generate a consolidated JSON.
     * </p>

     * @return The {@code JsonElement} with the content of this class.
     */
    JsonElement toJsonElement()
    {
        /* SRS_TWIN_METADATA_21_009: [The toJsonElement shall return a JsonElement with the information in this class in a JSON format.] */
        JsonObject jsonObject = new JsonObject();
        if (this.lastUpdated != null)
        {
            jsonObject.addProperty(LAST_UPDATE_TAG, ParserUtility.dateTimeUtcToString(this.lastUpdated));
        }
        if (this.lastUpdatedVersion != null)
        {
            jsonObject.addProperty(LAST_UPDATE_VERSION_TAG, this.lastUpdatedVersion);
        }
        return jsonObject;
    }
}
