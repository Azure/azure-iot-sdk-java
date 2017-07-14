/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.deps.serializer.TwinParser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DeviceTwinDevice
{
    /**
     *Codes_SRS_DEVICETWINDEVICE_25_001: [** The DeviceTwinDevice class has the following properties: deviceId, a container for tags, desired and reported properties, and a twin object. **]**
     */
    private String deviceId;
    private String eTag;
    private Map<String, Object> tag = null;
    private Map<String, Object> reportedProperties = null;
    private Map<String, Object> desiredProperties = null;
    private TwinParser twinParser = null;

    /**
     * Constructor to create instance for a device
     *
     * @param deviceId Device ID for this device
     * @throws IllegalArgumentException This exception is thrown if the device id is null or empty
     */
    public DeviceTwinDevice(String deviceId) throws IllegalArgumentException
    {
        if (deviceId == null || deviceId.length() == 0)
        {
            /*
            **Codes_SRS_DEVICETWINDEVICE_25_002: [** The constructor shall throw IllegalArgumentException if the input string is empty or null.**]**
             */
            throw new IllegalArgumentException("Device ID cannot be null or empty");
        }
        /*
        **Codes_SRS_DEVICETWINDEVICE_25_003: [** The constructor shall create a new instance of twin object for this device and store the device id.**]**
         */
        this.deviceId = deviceId;
        this.eTag = null;
        this.twinParser = new TwinParser();
        this.twinParser.enableTags();
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
     * Setter for ETag
     *
     * @param eTag is the value of the etag
     * @throws IllegalArgumentException if the provided etag is null or empty
     */
    public void setETag(String eTag) throws IllegalArgumentException
    {
        if (eTag == null || eTag.length() == 0)
        {
            /*
            **Codes_SRS_DEVICETWINDEVICE_21_029: [** The setETag shall throw IllegalArgumentException if the input string is empty or null.**]**
             */
            throw new IllegalArgumentException("ETag cannot be null or empty");
        }

        /*
        **Codes_SRS_DEVICETWINDEVICE_21_030: [** The setETag shall store the eTag.**]**
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
        **Codes_SRS_DEVICETWINDEVICE_21_031: [** The getETag shall return the stored eTag.**]**
         */
        return this.eTag;
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

    public void clearTags()
    {
        this.tag = null;
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
     * Clear desired properties set so far
     */

    public void clearDesiredProperties()
    {
        this.desiredProperties = null;
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
     * Setter for the reported properties
     *
     * @param reportedProperties A map of validated key and value pairs for reported properties
     */
    protected void setReportedProperties(Map<String, Object> reportedProperties)
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
    protected void setDesiredProperties(Map<String, Object> desiredProperties)
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
    protected void setTags(Map<String, Object> tag)
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
    protected Map<String, Object> getTagsMap()
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
    protected Map<String, Object> getDesiredMap()
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
    protected Map<String, Object> getReportedMap()
    {
        /*
        **Codes_SRS_DEVICETWINDEVICE_25_026: [** This method shall return the reportedProperties map**]**
         */
        return this.reportedProperties;
    }

    /**
     * Getter for the twin serializer object for this device
     *
     * @return  An object for twin serializer for this device
     */
    protected TwinParser getTwinParser()
    {
        /*
        **Codes_SRS_DEVICETWINDEVICE_25_028: [** This method shall return the twinParser for this device**]**
         */
        return twinParser;
    }

    /**
     * String representation for this device containing device id, tags, desired and reported properties
     * @return  String representation for this device
     */
    public String toString()
    {
        /*
        **Codes_SRS_DEVICETWINDEVICE_25_015: [** This method shall append device id, etag, tags, desired and reported properties to string (if present) and return **]**
         */
        StringBuilder thisDevice = new StringBuilder();

        thisDevice.append("Device ID: " + this.getDeviceId() + "\n");
        if(this.getETag() != null)
        {
            thisDevice.append("ETag: " + this.getETag() + "\n");
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

    private Set<Pair> mapToSet(Map<String, Object> map)
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

    private Map<String, Object> setToMap(Set<Pair> set)
    {
        Map<String, Object> map = new HashMap<>();

        if (set != null)
        {
            for (Pair p : set)
            {
                map.put(p.getKey(), p.getValue());
            }
        }

        return map;
    }
}
