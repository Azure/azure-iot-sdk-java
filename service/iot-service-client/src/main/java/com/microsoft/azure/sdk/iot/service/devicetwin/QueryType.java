/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.devicetwin;

public enum QueryType
{
    TWIN("twin"),
    DEVICE_JOB("deviceJob"),
    JOB_RESPONSE("jobResponse"),
    RAW("raw"),
    UNKNOWN("unknown");

    private final String type;

    QueryType(String type)
    {
        this.type = type;
    }

    public String getValue()
    {
        return type;
    }

    public static QueryType fromString(String type)
    {
        for (QueryType queryType : QueryType.values())
        {
            if (queryType.type.equalsIgnoreCase(type))
            {
                return queryType;
            }
        }
        return null;
    }
}
