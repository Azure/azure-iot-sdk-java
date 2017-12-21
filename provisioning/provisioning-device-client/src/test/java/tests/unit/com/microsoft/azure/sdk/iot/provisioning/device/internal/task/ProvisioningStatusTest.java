/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.device.internal.task;

import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.ProvisioningStatus;
import mockit.Deencapsulation;
import org.junit.Test;

import static com.microsoft.azure.sdk.iot.provisioning.device.internal.task.ProvisioningStatus.*;
import static org.junit.Assert.assertEquals;

/* Unit tests for Provisioning Status
 * Coverage 100% line, 100% method
 */
public class ProvisioningStatusTest
{
    //SRS_ProvisioningStatus_25_001: [ Constructor to create an enum ]
    //SRS_ProvisioningStatus_25_002: [ This method shall return the enum corresponding to the type. ]
    @Test
    public void fromStringReturnsUnassigned() throws Exception
    {
        ProvisioningStatus status = Deencapsulation.invoke(ProvisioningStatus.class, "fromString", "Unassigned");
        assertEquals(UNASSIGNED, status);
    }

    @Test
    public void fromStringReturnsAssigned() throws Exception
    {
        ProvisioningStatus status = Deencapsulation.invoke(ProvisioningStatus.class, "fromString", "Assigned");
        assertEquals(ASSIGNED, status);
    }

    @Test
    public void fromStringReturnsAssigning() throws Exception
    {
        ProvisioningStatus status = Deencapsulation.invoke(ProvisioningStatus.class, "fromString", "assigning");
        assertEquals(ASSIGNING, status);
    }

    @Test
    public void fromStringReturnsFailed() throws Exception
    {
        ProvisioningStatus status = Deencapsulation.invoke(ProvisioningStatus.class, "fromString", "failed");
        assertEquals(FAILED, status);
    }

    @Test
    public void fromStringReturnsDisabled() throws Exception
    {
        ProvisioningStatus status = Deencapsulation.invoke(ProvisioningStatus.class, "fromString", "disabled");
        assertEquals(DISABLED, status);
    }

    //SRS_ProvisioningStatus_25_003: [ If none of the enum's match the type it shall return null. ]
    @Test
    public void fromStringReturnsUnknown() throws Exception
    {
        ProvisioningStatus status = Deencapsulation.invoke(ProvisioningStatus.class, "fromString", "unknown");
        assertEquals(null, status);
    }
}
