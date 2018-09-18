/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.deps.twin.ConfigurationInfo;
import com.microsoft.azure.sdk.iot.deps.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.deps.twin.DeviceCapabilities;
import com.microsoft.azure.sdk.iot.deps.util.Tools;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The DeviceTwinDevice class represent the twin on iotHub.
 * implementing constructors and serialization functionality.
 * If object is a representation of the module twin if and only if the moduleId is set.
 */
public class DeviceTwinDevice
{
    /**
     *Codes_SRS_DEVICETWINDEVICE_25_001: [** The DeviceTwinDevice class has the following properties: deviceId, a container for tags, desired and reported properties, and a twin object. **]**
     */
    private String deviceId;
    private String moduleId;
    private String eTag;
    private Integer version;
    private TwinCollection tag = null;
    private TwinCollection reportedProperties = null;
    private TwinCollection desiredProperties = null;
    private Map<String, ConfigurationInfo> configurations = null;
    private DeviceCapabilities capabilities = null;

    /**
     * Constructor to create instance for a device
     */
    public DeviceTwinDevice()
    {
        /*
        **Codes_SRS_DEVICETWINDEVICE_25_003: [** The constructor shall create a new instance of twin object for this device and store the device id.**]**
         */
        this.deviceId = null;
        this.moduleId = null;
        this.eTag = null;
        this.version = null;
    }

    /**
     * Constructor to create instance for a device.
     *
     * @param deviceId Device ID for this device
     * @throws IllegalArgumentException This exception is thrown if the device id is null or empty
     */
    public DeviceTwinDevice(String deviceId) throws IllegalArgumentException
    {
        this();

        if (Tools.isNullOrEmpty(deviceId))
        {
            /*
            **Codes_SRS_DEVICETWINDEVICE_25_002: [** The constructor shall throw IllegalArgumentException if the input string is empty or null.**]**
             */
            throw new IllegalArgumentException("Device ID cannot be null or empty");
        }
        this.deviceId = deviceId;
    }

    /**
     * Constructor to create instance for a module.
     *
     * @param deviceId Device ID for the device which this module belongs to
     * @param moduleId Module ID for this module
     * @throws IllegalArgumentException This exception is thrown if the device id is null or empty
     */
    public DeviceTwinDevice(String deviceId, String moduleId) throws IllegalArgumentException
    {
        this();

        if (Tools.isNullOrEmpty(deviceId))
        {
            /*
             **Codes_SRS_DEVICETWINDEVICE_28_005: [** The constructor shall throw IllegalArgumentException if the deviceId is empty or null.**]**
             */
            throw new IllegalArgumentException("Device ID cannot be null or empty");
        }

        if (Tools.isNullOrEmpty(moduleId))
        {
            /*
             **Codes_SRS_DEVICETWINDEVICE_28_006: [** The constructor shall throw IllegalArgumentException if the moduleId is empty or null.**]**
             */
            throw new IllegalArgumentException("Module ID cannot be null or empty");
        }
        this.deviceId = deviceId;
        this.moduleId = moduleId;
    }

    /**
     * Getter to get device ID
     * @return device id for this device
     */
    public String getDeviceId()
    {
        /*
        **Codes_SRS_DEVICETWINDEVICE_25_004: [** This method shall return the device id **]**
         */
        return this.deviceId;
    }

    /**
     * Getter to get module ID
     * @return device id for this device
     */
    public String getModuleId()
    {
        /*
         **Codes_SRS_DEVICETWINDEVICE_28_001: [** This method shall return the module id **]**
         */
        return this.moduleId;
    }

    /**
     * Setter for ETag
     *
     * @param eTag is the value of the etag
     * @throws IllegalArgumentException if the provided etag is null or empty
     */
    public void setETag(String eTag) throws IllegalArgumentException
    {
        if (Tools.isNullOrEmpty(eTag))
        {
            /*
            **Codes_SRS_DEVICETWINDEVICE_21_029: [** The seteTag shall throw IllegalArgumentException if the input string is empty or null.**]**
             */
            throw new IllegalArgumentException("ETag cannot be null or empty");
        }

        /*
        **Codes_SRS_DEVICETWINDEVICE_21_030: [** The seteTag shall store the eTag.**]**
         */
        this.eTag = eTag;
    }

    /**
     * Getter for the eTag
     *
     * @return the stored eTag. It will be {@code null} if not set.
     */
    public String getETag()
    {
        /*
        **Codes_SRS_DEVICETWINDEVICE_21_031: [** The geteTag shall return the stored eTag.**]**
         */
        return this.eTag;
    }

    /**
     * Setter for Twin version
     *
     * @param version is the value of the version
     */
    void setVersion(Integer version)
    {
        /*
        **Codes_SRS_DEVICETWINDEVICE_21_032: [** The setVersion shall store the Twin version.**]**
         */
        this.version = version;
    }

    /**
     * Getter for the Twin version
     *
     * @return the stored version. It can be {@code null}.
     */
    public Integer getVersion()
    {
        /*
        **Codes_SRS_DEVICETWINDEVICE_21_033: [** The getVersion shall return the stored Twin version.**]**
         */
        return this.version;
    }

    /**
     * Setter for the tags
     *
     * @param tags A set of tag key-value pairs
     * @throws IllegalArgumentException This exception is thrown if the set tags is null
     */
    public void setTags(Set<Pair> tags) throws IllegalArgumentException
    {
        if (tags == null)
        {
            /*
            **Codes_SRS_DEVICETWINDEVICE_25_008: [** If the tags Set is null then this method shall throw IllegalArgumentException.**]**
             */
            throw new IllegalArgumentException("tags cannot be null");
        }
        /*
        **Codes_SRS_DEVICETWINDEVICE_25_007: [** This method shall convert the set of pairs of tags to a map and save it. **]**
         */
        this.tag = this.setToMap(tags);
    }


    /**
     * Getter to get Tags Set
     * @return A set of tag key value pairs.
     */
    public Set<Pair> getTags()
    {
        /*
        **Codes_SRS_DEVICETWINDEVICE_25_009: [** This method shall convert the tags map to a set of pairs and return with it. **]**
        **Codes_SRS_DEVICETWINDEVICE_25_010: [** If the tags map is null then this method shall return empty set of pairs.**]**
         */
        return this.mapToSet(this.tag);
    }

    /**
     * Clear tags set so far
     */
    public void clearTags()
    {
        this.tag = null;
    }

    /**
     * Getter for the tag version.
     *
     * @return The {@code Integer} with the Tags Collection version.
     * @throws IllegalArgumentException if the tags is {@code null}.
     */
    public Integer getTagsVersion()
    {
        if(this.tag == null)
        {
            /*
             **Codes_SRS_DEVICETWINDEVICE_21_034: [** If the tags map is null then this method shall throw IllegalArgumentException.**]**
             */
            throw new IllegalArgumentException("Tags is null");
        }
        /*
         **Codes_SRS_DEVICETWINDEVICE_21_035: [** The method shall return the version in the tag TwinCollection.**]**
         */
        return this.tag.getVersion();
    }

    /**
     * Getter to get Desired Properties set
     * @return A set of desired property pairs.
     */
    public Set<Pair> getDesiredProperties()
    {
        /*
        **Codes_SRS_DEVICETWINDEVICE_25_013: [** This method shall convert the desiredProperties map to a set of pairs and return with it. **]**
        **Codes_SRS_DEVICETWINDEVICE_25_014: [** If the desiredProperties map is null then this method shall return empty set of pairs.**]**
         */
        return this.mapToSet(this.desiredProperties);
    }

    /**
     * Setter for the desired properties
     *
     * @param desiredProperties A set of key-value pairs for desired properties
     * @throws IllegalArgumentException This exception is thrown if the set is null
     */
    public void setDesiredProperties(Set<Pair> desiredProperties) throws IllegalArgumentException
    {
        if (desiredProperties == null)
        {
            /*
            **Codes_SRS_DEVICETWINDEVICE_25_012: [** If the desiredProperties Set is null then this method shall throw IllegalArgumentException.**]**
             */
            throw new IllegalArgumentException("desiredProperties cannot be null");
        }
        /*
        **Codes_SRS_DEVICETWINDEVICE_25_011: [** This method shall convert the set of pairs of desiredProperties to a map and save it. **]**
         */
        this.desiredProperties = this.setToMap(desiredProperties);
    }

    /**
     * Sets the module id of this object
     * @param moduleId the module id of this object. Allowed to be null or empty
     */
    void setModuleId(String moduleId)
    {
        // Codes_SRS_DEVICETWINDEVICE_34_040: [This method shall save the provided moduleId.]
        this.moduleId = moduleId;
    }

    /**
     * Clear desired properties set so far
     */
    public void clearDesiredProperties()
    {
        this.desiredProperties = null;
    }

    /**
     * Getter for the desired properties version.
     *
     * @return The {@code Integer} with the Desired properties Collection version.
     * @throws IllegalArgumentException if the desired properties is {@code null}.
     */
    public Integer getDesiredPropertiesVersion()
    {
        if(this.desiredProperties == null)
        {
            /*
             **Codes_SRS_DEVICETWINDEVICE_21_036: [** If the desired properties is null then this method shall throw IllegalArgumentException.**]**
             */
            throw new IllegalArgumentException("Desired properties is null");
        }
        /*
         **Codes_SRS_DEVICETWINDEVICE_21_037: [** The method shall return the version in the desired properties TwinCollection.**]**
         */
        return this.desiredProperties.getVersion();
    }

    /**
     * Clear tags and desired properties set so far
     */
    public void clearTwin()
    {
        this.clearTags();
        this.clearDesiredProperties();
    }

    /**
     * Getter to get Reported Properties Set
     * @return A set of reported property pairs.
     */
    public Set<Pair> getReportedProperties()
    {
        /*
        **Codes_SRS_DEVICETWINDEVICE_25_005: [** This method shall convert the reported properties map to a set of pairs and return with it. **]**
         */
        /*
        **Codes_SRS_DEVICETWINDEVICE_25_006: [** If the reported properties map is null then this method shall return empty set of pairs.**]**
         */
        return this.mapToSet(this.reportedProperties);
    }

    /**
     * Getter for the reported properties version.
     *
     * @return The {@code Integer} with the Desired properties Collection version.
     * @throws IllegalArgumentException if the reported properties is {@code null}.
     */
    public Integer getReportedPropertiesVersion()
    {
        if(this.reportedProperties == null)
        {
            /*
             **Codes_SRS_DEVICETWINDEVICE_21_038: [** If the reported properties is null then this method shall throw IllegalArgumentException.**]**
             */
            throw new IllegalArgumentException("Reported properties is null");
        }
        /*
         **Codes_SRS_DEVICETWINDEVICE_21_039: [** The method shall return the version in the reported properties TwinCollection.**]**
         */
        return this.reportedProperties.getVersion();
    }

    /**
     * Setter for the reported properties
     *
     * @param reportedProperties A map of validated key and value pairs for reported properties
     */
    protected void setReportedProperties(TwinCollection reportedProperties)
    {
        /*
        **Codes_SRS_DEVICETWINDEVICE_25_022: [** This method shall save the reportedProperties map**]**
         */
        this.reportedProperties = reportedProperties;
    }

    /**
     * Setter for the desired properties
     *
     * @param desiredProperties A map of validated key and value pairs for desired properties
     */
    protected void setDesiredProperties(TwinCollection desiredProperties)
    {
        /*
        **Codes_SRS_DEVICETWINDEVICE_25_023: [** This method shall save the desiredProperties map**]**
         */
        this.desiredProperties = desiredProperties;
    }

    /**
     * Setter for the tags
     *
     * @param tag A map of validated key and value pairs for tag
     */
    protected void setTags(TwinCollection tag)
    {
        /*
        **Codes_SRS_DEVICETWINDEVICE_25_024: [** This method shall save the tags map**]**
         */
        this.tag = tag;
    }

    /**
     * Getter for the tags
     *
     * @return  A map of validated key and value pairs for tag
     */
    protected TwinCollection getTagsMap()
    {
        /*
        **Codes_SRS_DEVICETWINDEVICE_25_025: [** This method shall return the tags map**]**
         */
        return this.tag;
    }

    /**
     * Getter for the desired properties
     *
     * @return  A map of validated key and value pairs for desired properties
     */
    protected TwinCollection getDesiredMap()
    {
        /*
        **Codes_SRS_DEVICETWINDEVICE_25_027: [** This method shall return the desiredProperties map**]**
         */
        return this.desiredProperties;
    }

    /**
     * Getter for the reported properties
     *
     * @return  A map of validated key and value pairs for reported properties
     */
    protected TwinCollection getReportedMap()
    {
        /*
        **Codes_SRS_DEVICETWINDEVICE_25_026: [** This method shall return the reportedProperties map**]**
         */
        return this.reportedProperties;
    }

    /**
     * Setter for the configuration properties
     *
     * @param configurations is the configuration properties.
     */
    protected void setConfigurations(Map<String, ConfigurationInfo> configurations)
    {
        /*
         **Codes_SRS_DEVICETWINDEVICE_28_002: [** The getConfigurations shall return the stored configuration properties.**]**
         */
        this.configurations = configurations;
    }

    /**
     * Getter for the configuration properties
     *
     * @return the configuration properties. It can be {@code null}.
     */
    public Map<String, ConfigurationInfo> getConfigurations()
    {
        /*
         **Codes_SRS_DEVICETWINDEVICE_28_002: [** The getConfigurations shall return the stored configuration properties.**]**
         */
        return this.configurations;
    }

    /**
     * Setter for capabilities
     *
     * @param capabilities is the value of the capabilities
     */
    protected void setCapabilities(DeviceCapabilities capabilities)
    {
        /*
         **Codes_SRS_DEVICETWINDEVICE_28_003: [** The setCapabilities shall store the device capabilities.**]**
         */
        this.capabilities = capabilities;
    }

    /**
     * Getter for capabilities
     *
     * @return the value of the capabilities. It can be {@code null}.
     */
    public DeviceCapabilities getCapabilities()
    {
        /*
         **Codes_SRS_DEVICETWINDEVICE_28_004: [** The getCapabilities shall return the stored capabilities.**]**
         */
        return this.capabilities;
    }

    /**
     * String representation for this device containing device id, tags, desired and reported properties
     * @return  String representation for this device
     */
    public String toString()
    {
        /*
        **Codes_SRS_DEVICETWINDEVICE_25_015: [** This method shall append device id, module id, etag, version, tags, desired and reported properties to string (if present) and return **]**
         */
        StringBuilder thisDevice = new StringBuilder();

        thisDevice.append("Device ID: " + this.getDeviceId() + "\n");
        if (this.moduleId != null && !this.moduleId.isEmpty())
        {
            thisDevice.append("Module ID: " + this.getModuleId() + "\n");
        }
        if(this.getETag() != null)
        {
            thisDevice.append("ETag: " + this.getETag() + "\n");
        }
        if(this.getVersion() != null)
        {
            thisDevice.append("Version: " + this.getVersion() + "\n");
        }
        thisDevice.append(tagsToString());
        thisDevice.append(reportedPropertiesToString());
        thisDevice.append(desiredPropertiesToString());

        return thisDevice.toString();
    }

    /**
     * String representation for this device containing tags
     * @return  String representation for this device tags
     */
    public String tagsToString()
    {
        /*
        **Codes_SRS_DEVICETWINDEVICE_25_016: [** This method shall convert the tags map to string (if present) and return **]**
         */
        StringBuilder thisDeviceTags = new StringBuilder();
        if (tag != null)
        {
            thisDeviceTags.append("Tags:" + this.tag.toString() + "\n");
        }
        /*
        **Codes_SRS_DEVICETWINDEVICE_25_017: [** This method shall return an empty string if tags map is empty or null and return **]**
         */
        return thisDeviceTags.toString();

    }

    /**
     * String representation for this device containing desired properties
     * @return  String representation for this device desired properties
     */
    public String desiredPropertiesToString()
    {
        StringBuilder thisDeviceRepProp = new StringBuilder();
        if (this.desiredProperties != null)
        {
            /*
            **Codes_SRS_DEVICETWINDEVICE_25_018: [** This method shall convert the desiredProperties map to string (if present) and return **]**
             */
            thisDeviceRepProp.append("Desired Properties: " + this.desiredProperties.toString() + "\n");
        }
        /*
        **Codes_SRS_DEVICETWINDEVICE_25_019: [** This method shall return an empty string if desiredProperties map is empty or null and return **]**
         */
        return thisDeviceRepProp.toString();
    }

    /**
     * String representation for this device containing reported properties
     * @return  String representation for this device reported properties
     */
    public String reportedPropertiesToString()
    {
        StringBuilder thisDeviceDesProp = new StringBuilder();
        if (this.reportedProperties != null)
        {
            /*
            **Codes_SRS_DEVICETWINDEVICE_25_020: [** This method shall convert the reportedProperties map to string (if present) and return **]**
             */
            thisDeviceDesProp.append("Reported Properties" + this.reportedProperties.toString() + "\n");
        }
        /*
        **Codes_SRS_DEVICETWINDEVICE_25_021: [** This method shall return an empty string if reportedProperties map is empty or null and return **]**
         */
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
                    throw new IllegalArgumentException("Set must not contain multiple pairs with the same keys. Duplicate key: " + p.getKey());
                }

                map.put(p.getKey(), p.getValue());
            }
        }

        return map;
    }
}
