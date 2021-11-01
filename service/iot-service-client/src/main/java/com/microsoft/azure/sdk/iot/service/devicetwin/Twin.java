/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.devicetwin;

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

    private TwinCollection tags;
    private TwinCollection reportedProperties;
    private TwinCollection desiredProperties;

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

        if (deviceId == null || deviceId.isEmpty())
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

        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("deviceId cannot be null or empty.");
        }

        if (moduleId == null || moduleId.isEmpty())
        {
            throw new IllegalArgumentException("moduleId cannot be null or empty.");
        }
        this.deviceId = deviceId;
        this.moduleId = moduleId;
    }

    /**
     * Setter for twin tags.
     *
     * @param tags A set of tag key/value pairs.
     * @throws IllegalArgumentException This exception is thrown if the set tags is {@code null}.
     */
    public void setTags(Set<Pair> tags) throws IllegalArgumentException
    {
        if (tags == null)
        {
            throw new IllegalArgumentException("tags cannot be null");
        }
        this.tags = this.setToMap(tags);
    }

    /**
     * Getter for the twin tags.
     *
     * @return A set of tag key/value pairs.
     */
    public Set<Pair> getTags()
    {
        return this.mapToSet(this.tags);
    }

    /**
     * Clears the tags set so far.
     */
    public void clearTags()
    {
        this.tags = null;
    }

    /**
     * Getter for the tag version.
     *
     * @return The {@code Integer} with the tags collection version.
     * @throws IllegalArgumentException If the tags is {@code null}.
     */
    public Integer getTagsVersion()
    {
        if (this.tags == null)
        {
            throw new IllegalArgumentException("tag is null");
        }
        return this.tags.getVersion();
    }

    /**
     * Getter to get the desired properties setter.
     *
     * @return A set of desired property pairs.
     */
    public Set<Pair> getDesiredProperties()
    {
        return this.mapToSet(this.desiredProperties);
    }

    /**
     * Setter for the desired properties.
     *
     * @param desiredProperties A set of key/value pairs for desired properties.
     * @throws IllegalArgumentException This exception is thrown if the set is {@code null}.
     */
    public void setDesiredProperties(Set<Pair> desiredProperties) throws IllegalArgumentException
    {
        if (desiredProperties == null)
        {
            throw new IllegalArgumentException("desiredProperties cannot be null");
        }
        this.desiredProperties = this.setToMap(desiredProperties);
    }

    /**
     * Clears the desired properties set so far.
     */
    public void clearDesiredProperties()
    {
        this.desiredProperties = null;
    }

    /**
     * Getter for the desired properties version.
     *
     * @return The {@code Integer} with the desired properties collection version.
     * @throws IllegalArgumentException If the desired properties is {@code null}.
     */
    public Integer getDesiredPropertiesVersion()
    {
        if (this.desiredProperties == null)
        {
            throw new IllegalArgumentException("desiredProperties is null.");
        }
        return this.desiredProperties.getVersion();
    }

    /**
     * Clear tags and desired properties set so far.
     */
    public void clearTwin()
    {
        this.clearTags();
        this.clearDesiredProperties();
    }

    /**
     * Getter to get reported properties setter.
     *
     * @return A set of reported property pairs.
     */
    public Set<Pair> getReportedProperties()
    {
        return this.mapToSet(this.reportedProperties);
    }

    /**
     * Getter for the reported properties version.
     *
     * @return The {@code Integer} with the desired properties collection version.
     * @throws IllegalArgumentException if the reported properties is {@code null}.
     */
    public Integer getReportedPropertiesVersion()
    {
        if (this.reportedProperties == null)
        {
            throw new IllegalArgumentException("reportedProperties is null");
        }
        return this.reportedProperties.getVersion();
    }

    /**
     * Setter for the reported properties.
     *
     * @param reportedProperties A map of validated key/value pairs for reported properties.
     */
    void setReportedProperties(TwinCollection reportedProperties)
    {
        this.reportedProperties = reportedProperties;
    }

    /**
     * Setter for the desired properties.
     *
     * @param desiredProperties A map of validated key/value pairs for desired properties.
     */
    void setDesiredProperties(TwinCollection desiredProperties)
    {
        this.desiredProperties = desiredProperties;
    }

    /**
     * Setter for tags.
     *
     * @param tags A map of validated key/value pairs for tags.
     */
    void setTags(TwinCollection tags)
    {
        this.tags = tags;
    }

    /**
     * Getter for tags.
     *
     * @return  A map of validated key/value pairs for tags.
     */
    TwinCollection getTagsMap()
    {
        return this.tags;
    }

    /**
     * Getter for desired properties.
     *
     * @return  A map of validated key/value pairs for desired properties.
     */
    protected TwinCollection getDesiredMap()
    {
        return this.desiredProperties;
    }

    /**
     * Getter for reported properties.
     *
     * @return  A map of validated key/value pairs for reported properties.
     */
    protected TwinCollection getReportedMap()
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
    public String tagsToString()
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
    public String desiredPropertiesToString()
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
    public String reportedPropertiesToString()
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

    private Set<Pair> mapToSet(TwinCollection map)
    {
        Set<Pair> setPair = new HashSet<>();

        if (map != null)
        {
            for (Map.Entry<String, Object> setEntry : map.entrySet())
            {
                setPair.add(new Pair(setEntry.getKey(), setEntry.getValue()));
            }
        }

        return setPair;
    }

    private TwinCollection setToMap(Set<Pair> set)
    {
        TwinCollection map = new TwinCollection();

        if (set != null)
        {
            for (Pair p : set)
            {
                if (map.containsKey(p.getKey()))
                {
                    throw new IllegalArgumentException(
                            "Set must not contain multiple pairs with the same keys. Duplicate key: " + p.getKey());
                }

                map.put(p.getKey(), p.getValue());
            }
        }

        return map;
    }
}
