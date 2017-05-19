// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.DeviceTwin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

abstract public class Device implements PropertyCallBack<String, Object>
{
    private HashSet<Property> reportedProp = new HashSet<>();
    private HashMap<Property, Pair<PropertyCallBack<String, Object>, Object>> desiredProp = new HashMap<>();

    public HashSet<Property> getReportedProp()
    {
        /*
        **Codes_SRS_DEVICE_25_001: [**This method shall return a HashSet of properties that user has set by calling hasReportedProp.**]**
         */
        return reportedProp;
    }

    public void setReportedProp(Property reportedProp)
    {
        if (reportedProp == null)
        {
            /*
            **Codes_SRS_DEVICE_25_004: [**If the parameter reportedProp is null then this method shall throw IllegalArgumentException**]**
             */
            throw new IllegalArgumentException("Reported property cannot be null");
        }
        /*
        **Codes_SRS_DEVICE_25_002: [**The function shall add the new property to the map.**]**
        **Codes_SRS_DEVICE_25_003: [**If the already existing property is altered and added then the this method shall replace the old one.**]**
         */
        this.reportedProp.add(reportedProp);
    }

    public HashMap<Property, Pair<PropertyCallBack<String, Object>, Object>> getDesiredProp()
    {
        /*
        **Codes_SRS_DEVICE_25_005: [**The function shall return the HashMap containing the property and its callback and context pair set by the user so far.**]**
         */
        return this.desiredProp;
    }

    public void setDesiredPropertyCallback(Property desiredProp, PropertyCallBack<String, Object> desiredPropCallBack, Object desiredPropCallBackContext)
    {
        if (desiredProp  == null)
        {
            /*
            **Codes_SRS_DEVICE_25_007: [**If the parameter desiredProp is null then this method shall throw IllegalArgumentException**]**
             */
            throw new IllegalArgumentException("desired property cannot be null");
        }
        /*
        **Codes_SRS_DEVICE_25_006: [**The function shall add the property and its callback and context pair to the user map of desired properties.**]**
        **Codes_SRS_DEVICE_25_008: [**This method shall add the parameters to the map even if callback and object pair are null**]**
         */
        this.desiredProp.put(desiredProp, new Pair<>(desiredPropCallBack, desiredPropCallBackContext));
    }

    public void clean()
    {
        /*
        **Codes_SRS_DEVICE_25_009: [**The method shall remove all the reported and desired properties set by the user so far and mark existing collections as null to be garbage collected.**]**
         */
        if (reportedProp != null)
        {
            for (Iterator repProperty = reportedProp.iterator(); repProperty.hasNext();)
            {
                repProperty.next();
                repProperty.remove();
            }
        }
        reportedProp = null;

        if (desiredProp != null)
        {
            for (Iterator desiredProperty = desiredProp.entrySet().iterator(); desiredProperty.hasNext();)
            {
                desiredProperty.next();
                desiredProperty.remove();
            }
        }
        desiredProp = null;
    }

}
