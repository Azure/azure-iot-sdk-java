/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.twin;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * Represent the twin on IoT hub. Implementing constructors and serialization functionality.
 * <p>The object is a representation of a module twin if and only if the moduleId is set.</p>
 */
public class Twin
{
    @Getter
    private String deviceId;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private String moduleId;

    @Getter
    @Setter
    private String eTag;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private Integer version;

    private final TwinCollection tags = new TwinCollection();
    private final TwinCollection reportedProperties = new TwinCollection();
    private final TwinCollection desiredProperties = new TwinCollection();

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private Map<String, ConfigurationInfo> configurations;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private DeviceCapabilities capabilities;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private String connectionState;

    /**
     * The DTDL model Id of the device.
     * The value will be {@code null} for a non-PnP device.
     * The value will be {@code null} for a PnP device until the device connects and registers with a model Id.
     */
    @Getter
    @Setter
    private String modelId;

    /**
     * The scope of the device. Auto-generated and immutable for edge devices and modifiable in leaf devices to create child/parent relationship.
     * <p>For more information, see <a href="https://docs.microsoft.com/azure/iot-edge/iot-edge-as-gateway?view=iotedge-2020-11#parent-and-child-relationships">this document</a>.</p>
     */
    @Getter
    private String deviceScope;

    /**
     * The scopes of the upper level edge devices if applicable. Only available for edge devices.
     * <p>For more information, see <a href="https://docs.microsoft.com/azure/iot-edge/iot-edge-as-gateway?view=iotedge-2020-11#parent-and-child-relationships">this document</a>.</p>
     */
    @Getter
    private List<String> parentScopes = new ArrayList<>();

    public static Twin fromJson(String json)
    {
        TwinState twinState = new TwinState(json);

        Twin twin = new Twin(twinState.getDeviceId());
        twin.setVersion(twinState.getVersion());
        twin.setETag(twinState.getETag());

        twin.getTags().setVersion(twinState.getTags().getVersion());
        if (twinState.getTags().size() > 0)
        {
            twin.getTags().putAll(twinState.getTags());
        }

        twin.getDesiredProperties().setVersion(twinState.getDesiredProperties().getVersion());
        if (twinState.getDesiredProperties().size() > 0)
        {
            twin.getDesiredProperties().putAll(twinState.getDesiredProperties());
        }

        twin.getReportedProperties().setVersion(twinState.getReportedProperties().getVersion());
        if (twinState.getReportedProperties().size() > 0)
        {
            twin.getReportedProperties().putAll(twinState.getReportedProperties());
        }

        twin.setCapabilities(twinState.getCapabilities());
        twin.setConnectionState(twinState.getConnectionState());
        twin.setConfigurations(twinState.getConfigurations());
        twin.setModelId(twinState.getModelId());
        twin.setDeviceScope(twinState.getDeviceScope());
        twin.setParentScopes(twinState.getParentScopes());

        if (twinState.getModuleId() != null && !twinState.getModuleId().isEmpty())
        {
            twin.setModuleId(twinState.getModuleId());
        }

        return twin;
    }

    /**
     * Constructor to create an instance for a device.
     */
    public Twin()
    {
    }

    /**
     * Constructor to create instance for a device.
     *
     * @param deviceId Id for this device.
     * @throws IllegalArgumentException This exception is thrown if the device Id is {@code null} or empty
     */
    public Twin(String deviceId) throws IllegalArgumentException
    {
        this();

        if (Tools.isNullOrEmpty(deviceId))
        {
            throw new IllegalArgumentException("deviceId cannot be null or empty.");
        }

        this.deviceId = deviceId;
    }

    /**
     * Constructor to create an instance for a module.
     *
     * @param deviceId Id for this device.
     * @param moduleId Id for this device's module.
     * @throws IllegalArgumentException This exception is thrown if the device id is {@code null} or empty.
     */
    public Twin(String deviceId, String moduleId) throws IllegalArgumentException
    {
        this();

        if (Tools.isNullOrEmpty(deviceId))
        {
            throw new IllegalArgumentException("deviceId cannot be null or empty.");
        }

        if (Tools.isNullOrEmpty(moduleId))
        {
            throw new IllegalArgumentException("moduleId cannot be null or empty.");
        }

        this.deviceId = deviceId;
        this.moduleId = moduleId;
    }

    /**
     * Getter for the twin tags.
     *
     * @return A set of tag key/value pairs.
     */
    public TwinCollection getTags()
    {
        return this.tags;
    }

    /**
     * Getter to get the desired properties setter.
     *
     * @return A set of desired property pairs.
     */
    public TwinCollection getDesiredProperties()
    {
        return this.desiredProperties;
    }

    /**
     * Getter to get reported properties setter.
     *
     * @return A set of reported property pairs.
     */
    public TwinCollection getReportedProperties()
    {
        return this.reportedProperties;
    }

    /**
     * Sets the device scope.
     *
     * @param deviceScope The device scope to set.
     */
    void setDeviceScope(String deviceScope) { this.deviceScope = deviceScope; }

    /**
     * Sets the parent scopes.
     *
     * @param parentScopes The parent scopes.
     */
    void setParentScopes(List<String> parentScopes) { this.parentScopes = parentScopes; }

    /**
     * String representation for this device containing device Id, tags, desired and reported properties.
     *
     * @return String representation for this device.
     */
    public String toString()
    {
        StringBuilder thisDevice = new StringBuilder();

        thisDevice.append("Device Id: ").append(this.deviceId).append("\n");
        if (this.moduleId != null && !this.moduleId.isEmpty())
        {
            thisDevice.append("Module Id: ").append(this.moduleId).append("\n");
        }
        if (this.eTag != null)
        {
            thisDevice.append("ETag: ").append(this.eTag).append("\n");
        }
        if (this.version != null)
        {
            thisDevice.append("Version: ").append(this.version).append("\n");
        }

        thisDevice.append("Model Id: ").append(this.modelId).append("\n");
        if (this.deviceScope != null)
        {
            thisDevice.append("Device scope: ").append(this.deviceScope).append("\n");
        }
        if (this.parentScopes != null && !this.parentScopes.isEmpty())
        {
            thisDevice.append("Parent scopes: ")
                    .append(String.join(",", this.parentScopes))
                    .append("\n");
        }

        thisDevice.append(tagsToString());
        thisDevice.append(reportedPropertiesToString());
        thisDevice.append(desiredPropertiesToString());

        return thisDevice.toString();
    }

    /**
     * String representation for this device containing tags.
     *
     * @return String representation for this device tags.
     */
    private String tagsToString()
    {
        StringBuilder thisDeviceTags = new StringBuilder();
        if (tags != null)
        {
            thisDeviceTags.append("Tags: ").append(this.tags.toString()).append("\n");
        }
        return thisDeviceTags.toString();
    }

    /**
     * String representation for this device containing desired properties.
     *
     * @return  String representation for this device desired properties.
     */
    private String desiredPropertiesToString()
    {
        StringBuilder thisDeviceRepProp = new StringBuilder();
        if (this.desiredProperties != null)
        {
            thisDeviceRepProp.append("Desired properties: ").append(this.desiredProperties.toString()).append("\n");
        }
        return thisDeviceRepProp.toString();
    }

    /**
     * String representation for this device containing reported properties.'
     *
     * @return  String representation for this device reported properties.
     */
    private String reportedPropertiesToString()
    {
        StringBuilder thisDeviceDesProp = new StringBuilder();
        if (this.reportedProperties != null)
        {
            thisDeviceDesProp.append("Reported properties: ")
                    .append(this.reportedProperties.toString())
                    .append("\n");
        }
        return thisDeviceDesProp.toString();
    }
}
