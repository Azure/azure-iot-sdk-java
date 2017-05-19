// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.


package tests.unit.com.microsoft.azure.sdk.iot.device.DeviceTwin;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceTwinMessage;
import mockit.Deencapsulation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DeviceTwinMessageTest
{

    /*
    **Tests_SRS_DEVICETWINMESSAGE_25_001: [**The constructor shall save the message body by calling super with the body as parameter.**]**
     */
    @Test
    public void constructorSetsRequiredPrivateMembers()
    {
        //arrange
        final byte[] actualData = {};

        //act
        DeviceTwinMessage msg = new DeviceTwinMessage(actualData);

        //assert
        String actualVersion = Deencapsulation.getField(msg, "version");
        String actualRequestId = Deencapsulation.getField(msg, "requestId");
        String actualStatus = Deencapsulation.getField(msg, "status");
        DeviceOperations operationType = Deencapsulation.getField(msg, "operationType");

        assertNull(actualVersion);
        assertNull(actualRequestId);
        assertNull(actualStatus);
        assertEquals(operationType, DeviceOperations.DEVICE_OPERATION_UNKNOWN);
    }

    /*
    **Tests_SRS_DEVICETWINMESSAGE_25_002: [**If the message body is null, the constructor shall throw an IllegalArgumentException thrown by base constructor.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsIfDataIsNULL()
    {
        DeviceTwinMessage msg = new DeviceTwinMessage(null);

    }

    @Test
    public void gettersGetDefaultsIfNotSet()
    {
        //arrange
        final byte[] actualData = {};
        DeviceTwinMessage msg = new DeviceTwinMessage(actualData);

        //act
        String testVersion = msg.getVersion();
        String testRequestId = msg.getRequestId();
        String testStatus = msg.getStatus();
        DeviceOperations testOpType = msg.getDeviceOperationType();

        //assert
        String actualVersion = Deencapsulation.getField(msg, "version");
        String actualRequestId = Deencapsulation.getField(msg, "requestId");
        String actualStatus = Deencapsulation.getField(msg, "status");
        DeviceOperations operationType = Deencapsulation.getField(msg, "operationType");

        assertEquals(actualRequestId, testRequestId);
        assertEquals(actualStatus, testStatus);
        assertEquals(actualVersion, testVersion);
    }

    /*
    **Tests_SRS_DEVICETWINMESSAGE_25_003: [**The function shall set the version.**]**
     */
    @Test
    public void setVersionSetsCorrectVersion()
    {
        //arrange
        final byte[] actualData = {};
        DeviceTwinMessage msg = new DeviceTwinMessage(actualData);

        //act
       msg.setVersion("12");

        //assert
        assertEquals(msg.getVersion(), "12");
    }

    /*
    **Tests_SRS_DEVICETWINMESSAGE_25_004: [**The function shall return the value of the version either set by the setter or the default (null) if unset so far.**]**
     */
    @Test
    public void getVersionRetrievesVersion()
    {
        //arrange
        final byte[] actualData = {};
        DeviceTwinMessage msg = new DeviceTwinMessage(actualData);
        msg.setVersion("12");

        //act
        String version = msg.getVersion();

        //assert
        assertEquals(version, "12");

    }

    /*
    **Tests_SRS_DEVICETWINMESSAGE_25_005: [**The function shall save the request id.**]**
     */
    @Test
    public void setRequestIdSets()
    {
        //arrange
        final byte[] actualData = {};
        DeviceTwinMessage msg = new DeviceTwinMessage(actualData);

        //act
        msg.setRequestId("12");

        //assert
        assertEquals(msg.getRequestId(), "12");

    }

    /*
    **Tests_SRS_DEVICETWINMESSAGE_25_006: [**The function shall return the value of the request id either set by the setter or the default (null) if unset so far.**]**
     */
    @Test
    public void getRequestIdGets()
    {
        //arrange
        final byte[] actualData = {};
        DeviceTwinMessage msg = new DeviceTwinMessage(actualData);
        msg.setRequestId("12");

        //act
        String requestId = msg.getRequestId();

        //assert
        assertEquals(requestId, "12");

    }

    /*
    **Tests_SRS_DEVICETWINMESSAGE_25_007: [**The function shall save the status.**]**
     */
    @Test
    public void setStatusIdSets()
    {
        //arrange
        final byte[] actualData = {};
        DeviceTwinMessage msg = new DeviceTwinMessage(actualData);

        //act
        msg.setStatus("12");

        //assert
        assertEquals(msg.getStatus(), "12");
    }

    /*
    **Tests_SRS_DEVICETWINMESSAGE_25_008: [**The function shall return the value of the status either set by the setter or the default (null) if unset so far.**]**
     */
    @Test
    public void getStatusGets()
    {
        //arrange
        final byte[] actualData = {};
        DeviceTwinMessage msg = new DeviceTwinMessage(actualData);
        msg.setStatus("12");

        //act
        String status = msg.getStatus();

        //assert
        assertEquals(status, "12");
    }

    /*
    **Tests_SRS_DEVICETWINMESSAGE_25_009: [**The function shall save the device twin operation type.**]**
     */
    @Test
    public void setTwinOpSets()
    {
        //arrange
        final byte[] actualData = {};
        DeviceTwinMessage msg = new DeviceTwinMessage(actualData);

        //act
        msg.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_RESPONSE);

        //assert
        assertEquals(msg.getDeviceOperationType(), DeviceOperations.DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_RESPONSE);

    }

    /*
    **Tests_SRS_DEVICETWINMESSAGE_25_010: [**The function shall return the operation type either set by the setter or the default (DEVICE_OPERATION_UNKNOWN) if unset so far.**]**
     */
    @Test
    public void getTwinOpGets()
    {
        //arrange
        final byte[] actualData = {};
        DeviceTwinMessage msg = new DeviceTwinMessage(actualData);
        msg.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_RESPONSE);

        //act
        DeviceOperations op = msg.getDeviceOperationType();

        //assert
        assertEquals(op, DeviceOperations.DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_RESPONSE);

    }
}
