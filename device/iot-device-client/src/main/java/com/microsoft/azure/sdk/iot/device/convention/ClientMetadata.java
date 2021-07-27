// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.convention;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.deps.util.Tools;
import lombok.Getter;

import java.util.Date;
import java.util.Map;

/**
 * Representation of a single metadata item for the {@link ClientPropertyCollection}.
 *
 * <p> The metadata is a set of pairs lastUpdated/lastUpdatedVersion for each
 * property and sub-property in the client. It is optionally provided by
 * the service and the clients can only ready it.
 *
 * <p> This class store the Date and Version for each entity in the {@link ClientPropertyCollection}.
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
 *          "$lastUpdatedBy": "newconfig",
 *          "$lastUpdatedByDigest": "637570574076206429",
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
 * @see <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-device-twins">Understand and use device twins in IoT Hub</a>
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iothub/devicetwinapi">Device Twin Api</a>
 */
// Unchecked casts of Maps to Map<String, Object> are safe as long as service is returning valid twin json payloads. Since all json keys are Strings, all maps must be Map<String, Object>
@SuppressWarnings("unchecked")
public class ClientMetadata {

    // the entity last updated date and time in the TwinCollection
    public static final String LAST_UPDATE_TAG = "$lastUpdated";

    /**
     * The last updated date.
     */
    @Getter
    private Date lastUpdated;

    // the entity last updated version in the TwinCollection
    public static final String LAST_UPDATE_VERSION_TAG = "$lastUpdatedVersion";

    /**
     * The last updated version.
     */
    @Getter
    private final Integer lastUpdatedVersion;

    // the entity last updated by in the TwinCollection. Value is a configuration applied on the Twin.
    public static final String LAST_UPDATED_BY = "$lastUpdatedBy";

    /**
     * Last updated by.
     */
    @Getter
    private String lastUpdatedBy;

    // the entity last updated by digest in the TwinCollection which represents service internal version for the applied configuration.
    public static final String LAST_UPDATED_BY_DIGEST = "$lastUpdatedByDigest";

    /**
     * Last updated by digest.
     */
    @Getter
    private String lastUpdatedByDigest;

    /**
     * CONSTRUCTOR
     *
     * <p> This private constructor will receive and store the metadata parameters.
     *
     * @param lastUpdated        the {@code String} with the date and time UTC of the last update on the entity. It can be {@code null}, empty or invalid.
     * @param lastUpdatedVersion the {@code Integer} with the version of the last update on the entity. It can be {@code null}.
     * @throws IllegalArgumentException If no valid parameter was provide and the class will be empty, or if the DateTime is invalid.
     */
    ClientMetadata(String lastUpdated, Integer lastUpdatedVersion, String lastUpdatedBy, String lastUpdatedByDigest) {
        if (!Tools.isNullOrEmpty(lastUpdated)) {
            /* SRS_TWIN_METADATA_21_001: [The constructor shall parse the provided `lastUpdated` String to the Date and store it as the TwinMetadata lastUpdated.] */
            /* SRS_TWIN_METADATA_21_002: [The constructor shall throw IllegalArgumentException if it cannot convert the provided `lastUpdated` String to Date.] */
            this.lastUpdated = ParserUtility.getDateTimeUtc(lastUpdated);
        }

        if (lastUpdatedBy != null) {
            this.lastUpdatedBy = lastUpdatedBy;
        }

        if (lastUpdatedByDigest != null) {
            this.lastUpdatedByDigest = lastUpdatedByDigest;
        }

        /* SRS_TWIN_METADATA_21_003: [The constructor shall store the provided lastUpdatedVersion as is.] */
        this.lastUpdatedVersion = lastUpdatedVersion;

        /* SRS_TWIN_METADATA_21_012: [The constructor shall throw IllegalArgumentException if both lastUpdated and lastUpdatedVersion are null.] */
        if ((this.lastUpdatedVersion == null) && (this.lastUpdated == null)) {
            throw new NullPointerException("no valid data to create a ClientMetadata.");
        }
    }

    /**
     * CONSTRUCTOR (copy)
     *
     * <P> This private constructor will create a new instance of the ClientMetadata coping the information from the provided one.
     *
     * @param metadata the original {@code ClientMetadata} to copy.
     */
    ClientMetadata(ClientMetadata metadata) {
        /* SRS_TWIN_METADATA_21_010: [The constructor shall throw IllegalArgumentException if the provided metadata is null.] */
        if (metadata == null) {
            throw new NullPointerException("metadata to copy cannot be null");
        }

        /* SRS_TWIN_METADATA_21_011: [The constructor shall copy the content of the provided metadata.] */
        this.lastUpdated = metadata.getLastUpdated();
        this.lastUpdatedVersion = metadata.getLastUpdatedVersion();
        this.lastUpdatedBy = metadata.getLastUpdatedBy();
        this.lastUpdatedByDigest = metadata.getLastUpdatedByDigest();
    }

    /**
     * Metadata extractor
     *
     * <p> This internal method will try to find $lastUpdated and $lastUpdatedVersion at the first
     * level of the provided Map (Object), and create a new instance of the ClientMetadata with
     * this information.
     *
     * <p> Once the provide Object can or cannot be a Map, and, if it is a Map, it can or cannot
     * contains a valid metadata, this method contains the label <b>try</b>, which means that
     * it can return a valid ClientMetadata or {@code null}.
     *
     * <p> For instance, for the follow Map, this method will create a ClientMetadata with
     * {@code lastUpdated = 2015-09-21T02:07:44.238Z} and {@code lastUpdatedVersion = 3}
     *
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
     * }
     * </pre>
     *
     * @param metadata the {@code Object} that may contains the metadata.
     * @return A valid ClientMetadata instance it the provided metadata {@code Object} is a Map with
     * data and version metadata, or {@code null} for the other cases.
     * @throws IllegalArgumentException If no valid parameter was provide and the class will be empty, or if the DateTime is invalid.
     */
    protected static ClientMetadata tryExtractFromMap(Object metadata) {
        /* SRS_TWIN_METADATA_21_004: [The tryExtractFromMap shall return null if the provided metadata is not a Map.] */
        if (!(metadata instanceof Map)) {
            return null;
        }

        String lastUpdated = null;
        Integer lastUpdatedVersion = null;
        String lastUpdatedBy = null;
        String lastUpdatedByDigest = null;
        for (Map.Entry<? extends String, Object> entry : ((Map<? extends String, Object>) metadata).entrySet()) {
            String key = entry.getKey();
            if (key.equals(LAST_UPDATE_TAG)) {
                lastUpdated = (String) entry.getValue();
            } else if (key.equals(LAST_UPDATE_VERSION_TAG)) {
                if (!(entry.getValue() instanceof Number)) {
                    throw new IllegalArgumentException("Version in the metadata shall be a number");
                }
                lastUpdatedVersion = ((Number) entry.getValue()).intValue();
            } else if (key.equals(ClientMetadata.LAST_UPDATED_BY)) {
                lastUpdatedBy = entry.getValue().toString();
            } else if (key.equals(ClientMetadata.LAST_UPDATED_BY_DIGEST)) {
                lastUpdatedByDigest = entry.getValue().toString();
            }
        }

        if ((lastUpdatedVersion != null) || !Tools.isNullOrEmpty(lastUpdated)) {
            return new ClientMetadata(lastUpdated, lastUpdatedVersion, lastUpdatedBy, lastUpdatedByDigest);
        }
        return null;
    }

    /**
     * Serializer
     *
     * <p>
     * Creates a {@code JsonElement}, which the content represents
     * the information in this class in a JSON format.
     * <p>
     * This is useful if the caller will integrate this JSON with JSON from
     * other classes to generate a consolidated JSON.
     * </p>
     *
     * @return The {@code JsonElement} with the content of this class.
     */
    protected JsonElement toJsonElement() {
        /* SRS_TWIN_METADATA_21_009: [The toJsonElement shall return a JsonElement with the information in this class in a JSON format.] */
        JsonObject jsonObject = new JsonObject();
        if (this.lastUpdated != null) {
            jsonObject.addProperty(LAST_UPDATE_TAG, ParserUtility.dateTimeUtcToString(this.lastUpdated));
        }
        if (this.lastUpdatedVersion != null) {
            jsonObject.addProperty(LAST_UPDATE_VERSION_TAG, this.lastUpdatedVersion);
        }
        if (this.lastUpdatedBy != null) {
            jsonObject.addProperty(LAST_UPDATED_BY, this.lastUpdatedBy);
        }
        if (this.lastUpdatedByDigest != null) {
            jsonObject.addProperty(LAST_UPDATED_BY_DIGEST, this.lastUpdatedByDigest);
        }
        return jsonObject;
    }

    /**
     * Creates a pretty print JSON with the content of this class and subclasses.
     *
     * @return The {@code String} with the pretty print JSON.
     */
    @Override
    public String toString() {
        /* SRS_TWIN_METADATA_21_010: [The toString shall return a String with the information in this class in a pretty print JSON.] */
        return toJsonElement().toString();
    }
}
