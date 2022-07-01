// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.twin;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
@SuppressWarnings("unused") // A number of private members are unused but may be filled in or used by serialization
public class TwinState
{
    /**
     * Device name
     * A case-sensitive string (up to 128 char long)
     * of ASCII 7-bit alphanumeric chars
     * + {'-', ':', '.', '+', '%', '_', '#', '*', '?', '!', '(', ')', ',', '=', '@', ';', '$', '''}.
     */
    private static final String DEVICE_ID_TAG = "deviceId";
    @Expose
    @SerializedName(DEVICE_ID_TAG)
    @Getter
    @Setter
    private String deviceId = null;

    /**
     * Module name
     * A case-sensitive string (up to 128 char long)
     * of ASCII 7-bit alphanumeric chars
     * + {'-', ':', '.', '+', '%', '_', '#', '*', '?', '!', '(', ')', ',', '=', '@', ';', '$', '''}.
     */
    private static final String MODULE_ID_TAG = "moduleId";
    @Expose
    @SerializedName(MODULE_ID_TAG)
    @Getter
    @Setter
    private String moduleId = null;

    /**
     * Device generation Id
     */
    private static final String GENERATION_ID_TAG = "generationId";
    @Expose
    @SerializedName(GENERATION_ID_TAG)
    @Getter
    @Setter
    private String generationId = null;

    /**
     * A string representing a weak ETAG version
     * of this JSON description. This is a hash.
     */
    private static final String ETAG_TAG = "etag";
    @Expose
    @SerializedName(ETAG_TAG)
    @Getter
    @Setter
    private String eTag = null;

    /**
     * An Integer representing a Twin version.
     */
    private static final String VERSION_TAG = "version";
    @Expose
    @SerializedName(VERSION_TAG)
    @Getter
    @Setter
    private Integer version = null;

    /**
     * "Enabled", "Disabled".
     * If "enabled", this device is authorized to connect.
     * If "disabled" this device cannot receive or send messages, and statusReason must be set.
     */
    private static final String STATUS_TAG = "status";
    @Expose
    @SerializedName(STATUS_TAG)
    @Getter
    @Setter
    private TwinStatus status = null;

    /**
     * A 128 char long string storing the reason of suspension.
     * (all UTF-8 chars allowed).
     */
    private static final String STATUS_REASON_TAG = "statusReason";
    @Expose
    @SerializedName(STATUS_REASON_TAG)
    @Getter
    @Setter
    private String statusReason = null;

    /**
     * Datetime of last time the state was updated.
     */
    private static final String STATUS_UPDATED_TIME_TAG = "statusUpdatedTime";
    @Expose
    @SerializedName(STATUS_UPDATED_TIME_TAG)
    @Getter
    @Setter
    private String statusUpdatedTime = null;

    /**
     * Status of the device:
     * {"connected" | "disconnected"}
     */
    private static final String CONNECTION_STATE_TAG = "connectionState";
    @Expose
    @SerializedName(CONNECTION_STATE_TAG)
    protected TwinConnectionState connectionState = null;

    /**
     * Datetime of last time the connection state was updated.
     */
    private static final String CONNECTION_STATE_UPDATED_TIME_TAG = "connectionStateUpdatedTime";
    @Expose
    @SerializedName(CONNECTION_STATE_UPDATED_TIME_TAG)
    @Getter
    @Setter
    private String connectionStateUpdatedTime = null;

    /**
     * Cloud to device message count.
     */
    private static final String CLOUD_TO_DEVICE_MESSAGE_COUNT = "cloudToDeviceMessageCount";
    @Expose
    @SerializedName(CLOUD_TO_DEVICE_MESSAGE_COUNT)
    @Getter
    @Setter
    private Integer cloudToDeviceMessageCount = null;

    /**
     * Datetime of last time the device authenticated, received, or sent a message.
     */
    private static final String LAST_ACTIVITY_TIME_TAG = "lastActivityTime";
    @Expose
    @SerializedName(LAST_ACTIVITY_TIME_TAG)
    @Getter
    @Setter
    private String lastActivityTime = null;

    /**
     * Datetime of last time the device authenticated, received, or sent a message.
     */
    private static final String CAPABILITIES_TAG = "capabilities";
    @Expose
    @SerializedName(CAPABILITIES_TAG)
    @Getter
    @Setter
    private DeviceCapabilities capabilities = null;

    /**
     * The Digital Twin model id of the device and module
     * The value will be null for a non-pnp device.
     * The value will be null for a pnp device until the device connects and registers with the model id.
     */
    private static final String MODEL_ID = "modelId";
    @Expose
    @SerializedName(MODEL_ID)
    @Getter
    @Setter
    private String modelId = null;

    // the twin tags
    private static final String TAGS_TAG = "tags";
    @Expose
    @SerializedName(TAGS_TAG)
    private TwinCollection tags;

    // the twin desired properties
    private static final String PROPERTIES_TAG = "properties";
    @Expose
    @SerializedName(PROPERTIES_TAG)
    private TwinProperties properties;

    // the twin configurations
    private static final String CONFIGURATION_TAG = "configurations";
    @Expose
    @SerializedName(CONFIGURATION_TAG)
    @Getter
    @Setter
    private Map<String, ConfigurationInfo> configurations;

    private static final String DEVICE_SCOPE = "deviceScope";
    @SerializedName(DEVICE_SCOPE)
    @Getter
    @Setter
    private String deviceScope;

    private static final String PARENT_SCOPES = "parentScopes";
    @SerializedName(PARENT_SCOPES)
    @Getter
    private final List<String> parentScopes = new ArrayList<>();

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
        if (tags != null)
        {
            this.tags = TwinCollection.createFromRawCollection(tags);
        }
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
        Gson gson = TwinGsonBuilder.getGson();

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
     * Getter for the tags.
     *
     * @return The {@code TwinCollection} with the tags content. It can be {@code null}.
     */
    public TwinCollection getTags()
    {
        return this.tags;
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
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().disableHtmlEscaping().create();
        JsonObject jsonObject = gson.toJsonTree(this).getAsJsonObject();

        if (this.tags != null)
        {
            jsonObject.add(TAGS_TAG, this.tags.toJsonElementWithMetadata());
        }

        if (this.properties != null)
        {
            jsonObject.add(PROPERTIES_TAG, this.properties.toJsonElementWithMetadata());
        }

        return jsonObject.toString();
    }

    /**
     * <p> Create a new instance of the TwinState parsing the provided string as a JSON with the full Twin information.
     *
     * @param json the {@code String} with the JSON received from the service. It cannot be {@code null} or empty.
     */
    public TwinState(String json)
    {
        if (Tools.isNullOrEmpty(json))
        {
            throw new IllegalArgumentException("JSON with result is null or empty");
        }

        Gson gson = TwinGsonBuilder.getGson();

        TwinState result = gson.fromJson(json, TwinState.class);

        /*
         * During the deserialization process, the GSON will convert both tags and
         * properties to a raw Map, which will includes the $version and $metadata
         * as part of the collection. So, we need to reorganize this map using the
         * TwinCollection format. This constructor will do that.
         */
        this.tags = new TwinCollection(result.getTags());
        if (result.properties != null)
        {
            this.properties = new TwinProperties(result.properties.getDesired(), result.properties.getReported());
        }

        this.configurations = result.configurations;
        this.deviceScope = result.deviceScope;
        this.parentScopes.addAll(result.parentScopes);
        this.connectionState = result.connectionState;
        this.setDeviceId(result.getDeviceId());
        this.setModuleId(result.getModuleId());
        this.setETag(result.getETag());
        this.setConnectionStateUpdatedTime(result.getConnectionStateUpdatedTime());
        this.setCapabilities(result.getCapabilities());
        this.setModelId(result.getModelId());
        this.setGenerationId(result.getGenerationId());
        this.setLastActivityTime(result.getLastActivityTime());
        this.setStatus(result.getStatus());
        this.setStatusReason(result.getStatusReason());
        this.setStatusUpdatedTime(result.getStatusUpdatedTime());
        this.setVersion(result.getVersion());
        this.setCloudToDeviceMessageCount(result.cloudToDeviceMessageCount);
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
        if (Tools.isNullOrEmpty(json))
        {
            throw new IllegalArgumentException("JSON with result is null or empty");
        }

        Gson gson = TwinGsonBuilder.getGson();
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
        if (Tools.isNullOrEmpty(json))
        {
            throw new IllegalArgumentException("JSON with result is null or empty");
        }

        Gson gson = TwinGsonBuilder.getGson();
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
        if (Tools.isNullOrEmpty(json))
        {
            throw new IllegalArgumentException("JSON with result is null or empty");
        }

        Gson gson = TwinGsonBuilder.getGson();
        TwinProperties result = gson.fromJson(json, TwinProperties.class);

        return new TwinState(null, result.getDesired(), result.getReported());
    }

    /**
     * Get the connection state
     * @return the connection state
     */
    public String getConnectionState()
    {
        return this.connectionState.toString();
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
    }
}
