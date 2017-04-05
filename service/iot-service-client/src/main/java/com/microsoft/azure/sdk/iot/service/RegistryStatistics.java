/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

public class RegistryStatistics
{
    private long totalDeviceCount;
    private long enabledDeviceCount;
    private long disabledDeviceCount;

    private RegistryStatistics()
    {
        this.totalDeviceCount = 0;
        this.enabledDeviceCount = 0;
        this.disabledDeviceCount = 0;
    }

    public long getDisabledDeviceCount()
    {
        return disabledDeviceCount;
    }

    public long getEnabledDeviceCount()
    {
        return enabledDeviceCount;
    }

    public long getTotalDeviceCount()
    {
        return totalDeviceCount;
    }
}
