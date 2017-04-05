/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.RegistryStatistics;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import mockit.Mocked;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class RegistryStatisticsTests
{

    @Test
    public void constructorSucceeds(@Mocked RegistryManager mockedRegistryManager) throws IOException, IotHubException
    {
        //arrange

        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        // act
        RegistryStatistics statistics = registryManager.getStatistics();
    }

    @Test
    public void gettersSucceeds(@Mocked RegistryManager mockedRegistryManager) throws IOException, IotHubException
    {
        //arrange

        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";

        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        RegistryStatistics statistics = registryManager.getStatistics();

        //act
        final long disabledDeviceCount = statistics.getDisabledDeviceCount();
        final long enabledDeviceCount = statistics.getEnabledDeviceCount();
        final long totalDeviceCount = statistics.getTotalDeviceCount();

        //assert

        assertEquals(disabledDeviceCount, 0);
        assertEquals(enabledDeviceCount, 0);
        assertEquals(totalDeviceCount, 0);

    }

}
