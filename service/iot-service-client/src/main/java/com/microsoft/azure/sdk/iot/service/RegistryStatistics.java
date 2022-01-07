/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.service.serializers.RegistryStatisticsParser;
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
        //Codes_SRS_SERVICE_SDK_JAVA_REGISTRY_STATISTICS_34_003: [If the provided RegistryStatisticsParser object is null, an IllegalArgumentException shall be thrown.]
        if (registryStatisticsParser == null)
        {
            throw new IllegalArgumentException("The registryStatisticsParser may not be null");
        }

        //Codes_SRS_SERVICE_SDK_JAVA_REGISTRY_STATISTICS_34_001: [This method shall convert the provided parser into a RegistryStatistics object and return it.]
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
        //Codes_SRS_SERVICE_SDK_JAVA_REGISTRY_STATISTICS_34_002: [This method shall convert this into a RegistryStatisticsParser object and return it.]
        RegistryStatisticsParser parser = new RegistryStatisticsParser();
        parser.setTotalDeviceCount(this.totalDeviceCount);
        parser.setEnabledDeviceCount(this.enabledDeviceCount);
        parser.setDisabledDeviceCount(this.disabledDeviceCount);

        return parser;
    }
}
