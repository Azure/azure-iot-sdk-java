/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.registry;

import com.microsoft.azure.sdk.iot.service.registry.serializers.RegistryStatisticsParser;
import lombok.Getter;

public class RegistryStatistics
{
    @Getter
    private final long totalDeviceCount;

    @Getter
    private final long enabledDeviceCount;

    @Getter
    private final long disabledDeviceCount;

    /**
     * Consructs a RegistryStatisics object based off of a RegistryStatisticsParser object
     * @param registryStatisticsParser the object to base the constructed object on
     */
    RegistryStatistics(RegistryStatisticsParser registryStatisticsParser)
    {
        if (registryStatisticsParser == null)
        {
            throw new IllegalArgumentException("The registryStatisticsParser may not be null");
        }

        this.totalDeviceCount = registryStatisticsParser.getTotalDeviceCount();
        this.enabledDeviceCount = registryStatisticsParser.getEnabledDeviceCount();
        this.disabledDeviceCount = registryStatisticsParser.getDisabledDeviceCount();
    }

    /**
     * Converts this into a RegistryStatisticsParser object that can be used for serialization and deserialization
     * @return the created RegistryStatisticsParser object
     */
    @SuppressWarnings("unused") // A number of private members are unused but may be filled in or used by serialization
    RegistryStatisticsParser toRegistryStatisticsParser()
    {
        RegistryStatisticsParser parser = new RegistryStatisticsParser();
        parser.setTotalDeviceCount(this.totalDeviceCount);
        parser.setEnabledDeviceCount(this.enabledDeviceCount);
        parser.setDisabledDeviceCount(this.disabledDeviceCount);

        return parser;
    }
}
