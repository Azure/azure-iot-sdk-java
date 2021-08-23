// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.twin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

abstract public class Device implements PropertyCallBack<String, Object>
{
    private final HashSet<Property> reportedProp = new HashSet<>();
    private final HashMap<Property, Pair<PropertyCallBack<String, Object>, Object>> desiredProp = new HashMap<>();

    public HashSet<Property> getReportedProp()
    {
        return reportedProp;
    }

    /**
     * Save the provided property. If there is a saved property with the same key as the key in the provided reportedProp, the old value will
     * be overwritten by the new value
     * @param reportedProp the key and value to save as a reported property
     */
    public void setReportedProp(Property reportedProp)
    {
        if (reportedProp == null)
        {
            throw new IllegalArgumentException("Reported property cannot be null");
        }

        Property duplicateProperty = null;
        for (Property property : this.reportedProp)
        {
            if (property.getKey().equalsIgnoreCase(reportedProp.getKey()))
            {
                //to avoid duplicate keys, the old value will be overridden
                duplicateProperty = property;
            }
        }

        if (duplicateProperty != null)
        {
            this.reportedProp.remove(duplicateProperty);
        }

        this.reportedProp.add(reportedProp);
    }

    public HashMap<Property, Pair<PropertyCallBack<String, Object>, Object>> getDesiredProp()
    {
        return this.desiredProp;
    }

    public void setDesiredPropertyCallback(Property desiredProp, PropertyCallBack<String, Object> desiredPropCallBack, Object desiredPropCallBackContext)
    {
        if (desiredProp  == null)
        {
            throw new IllegalArgumentException("desired property cannot be null");
        }

        this.desiredProp.put(desiredProp, new Pair<>(desiredPropCallBack, desiredPropCallBackContext));
    }

    public void clean()
    {
        for (Iterator<Property> repProperty = reportedProp.iterator(); repProperty.hasNext();)
        {
            repProperty.next();
            repProperty.remove();
        }

        for (Iterator<Map.Entry<Property, Pair<PropertyCallBack<String, Object>, Object>>> desiredProperty = desiredProp.entrySet().iterator(); desiredProperty.hasNext();)
        {
            desiredProperty.next();
            desiredProperty.remove();
        }
    }
}
