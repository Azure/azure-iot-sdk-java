// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.microsoft.azure.sdk.iot.provisioning.service.configs.ReprovisionPolicy;
import mockit.Deencapsulation;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Unit tests for reprovisionPolicy
 * 100% methods, 100% lines covered
 */
public class ReprovisionPolicyTest 
{
    private static final boolean expectedUpdateHubAssignment = true;
    private static final boolean expectedMigrateDeviceData = true;

    //Tests_SRS_REPROVISION_POLICY_34_001: [This function shall return the saved updateHubAssignment.]
    @Test
    public void getUpdateHubAssignmentGets()
    {
        //arrange
        ReprovisionPolicy reprovisionPolicy = new ReprovisionPolicy();
        Deencapsulation.setField(reprovisionPolicy, "updateHubAssignment", expectedUpdateHubAssignment);

        //act
        boolean actualUpdateHubAssignment = reprovisionPolicy.getUpdateHubAssignment();

        //assert
        assertEquals(expectedUpdateHubAssignment, actualUpdateHubAssignment);
    }

    //Tests_SRS_REPROVISION_POLICY_34_002: [This function shall save the provided updateHubAssignment.]
    @Test
    public void setUupdateHubAssignmentSets()
    {
        //arrange
        ReprovisionPolicy reprovisionPolicy = new ReprovisionPolicy();

        //act
        reprovisionPolicy.setUpdateHubAssignment(expectedUpdateHubAssignment);

        //assert
        assertEquals(expectedUpdateHubAssignment, (boolean) Deencapsulation.getField(reprovisionPolicy, "updateHubAssignment"));
    }

    //Tests_SRS_REPROVISION_POLICY_34_003: [This function shall return the saved migrateDeviceData.]
    @Test
    public void getMigrateDeviceDataGets()
    {
        //arrange
        ReprovisionPolicy reprovisionPolicy = new ReprovisionPolicy();
        Deencapsulation.setField(reprovisionPolicy, "migrateDeviceData", expectedMigrateDeviceData);

        //act
        boolean actualMigrateDeviceData = reprovisionPolicy.getMigrateDeviceData();

        //assert
        assertEquals(expectedMigrateDeviceData, actualMigrateDeviceData);
    }

    //Tests_SRS_REPROVISION_POLICY_34_004: [This function shall save the provided migrateDeviceData.]
    @Test
    public void setMigrateDeviceDataSets()
    {
        //arrange
        ReprovisionPolicy reprovisionPolicy = new ReprovisionPolicy();

        //act
        reprovisionPolicy.setMigrateDeviceData(expectedMigrateDeviceData);

        //assert
        assertEquals(expectedMigrateDeviceData, (boolean) Deencapsulation.getField(reprovisionPolicy, "migrateDeviceData"));
    }
}
