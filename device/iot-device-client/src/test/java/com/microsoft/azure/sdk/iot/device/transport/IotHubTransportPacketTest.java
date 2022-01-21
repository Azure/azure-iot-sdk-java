// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import mockit.Deencapsulation;
import mockit.Mocked;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for IotHubTransportPacket.
 * Methods: 100%
 * Lines: 100%
 */
public class IotHubTransportPacketTest
{
    @Mocked
    Message mockMsg;
    @Mocked
    IotHubEventCallback mockCallback;

    // Tests_SRS_IOTHUBTRANSPORTPACKET_11_001: [The constructor shall save the message, callback, status, startTimeMillis, and callback context.]
    // Tests_SRS_IOTHUBTRANSPORTPACKET_11_002: [The function shall return the message given in the constructor.]
    @Test
    public void getMessageReturnsMessage()
    {
        //arrange
        final Map<String, Object> context = new HashMap<>();

        //act
        IotHubTransportPacket packet = new IotHubTransportPacket(mockMsg, mockCallback, context, IotHubStatusCode.OK_EMPTY, 10, null);
        Message testMsg = packet.getMessage();

        //assert
        final Message expectedMsg = mockMsg;
        assertThat(testMsg, is(expectedMsg));
    }

    // Tests_SRS_IOTHUBTRANSPORTPACKET_11_001: [The constructor shall save the message, callback, status, startTimeMillis, and callback context.]
    // Tests_SRS_IOTHUBTRANSPORTPACKET_11_003: [The function shall return the event callback given in the constructor.]
    @Test
    public void getCallbackReturnsCallback()
    {
        //arrange
        final Map<String, Object> context = new HashMap<>();
        IotHubStatusCode expectedStatus = IotHubStatusCode.MESSAGE_CANCELLED_ONCLOSE;

        //act
        IotHubTransportPacket packet = new IotHubTransportPacket(mockMsg, mockCallback, context, expectedStatus, 10, null);
        IotHubEventCallback testCallback = packet.getCallback();

        //assert
        final IotHubEventCallback expectedCallback = mockCallback;
        assertThat(testCallback, is(expectedCallback));
        assertEquals(expectedStatus, packet.getStatus());
    }

    // Tests_SRS_IOTHUBTRANSPORTPACKET_11_001: [The constructor shall save the message, callback, status, startTimeMillis, and callback context.]
    // Tests_SRS_IOTHUBTRANSPORTPACKET_34_005: [This function shall return the saved status.]
    @Test
    public void getStatusReturnsStatus()
    {
        //arrange
        final Map<String, Object> context = new HashMap<>();
        IotHubStatusCode expectedStatus = IotHubStatusCode.MESSAGE_CANCELLED_ONCLOSE;

        //act
        IotHubTransportPacket packet = new IotHubTransportPacket(mockMsg, mockCallback, context, expectedStatus, 10, null);
        IotHubEventCallback testCallback = packet.getCallback();

        //assert
        final IotHubEventCallback expectedCallback = mockCallback;
        assertThat(testCallback, is(expectedCallback));
        assertEquals(expectedStatus, packet.getStatus());
    }


    // Tests_SRS_IOTHUBTRANSPORTPACKET_11_001: [The constructor shall save the message, callback, status, startTimeMillis, and callback context.]
    // Tests_SRS_IOTHUBTRANSPORTPACKET_11_004: [The function shall return the callback context given in the constructor.] 
    @Test
    public void getContextReturnsContext()
    {
        //arrange
        final Map<String, Object> context = new HashMap<>();
        final String key = "test-key";
        final String value = "test-value";
        context.put(key, value);

        //act
        IotHubTransportPacket packet = new IotHubTransportPacket(mockMsg, mockCallback, context, IotHubStatusCode.OK_EMPTY, 10, null);
        Map<String, Object> testContext = (Map<String, Object>) packet.getContext();

        //assert
        Set<Map.Entry<String, Object>> testEntrySet = testContext.entrySet();
        final Set<Map.Entry<String, Object>> expectedEntrySet = context.entrySet();
        assertThat(testEntrySet, everyItem(isIn(expectedEntrySet)));
    }

    // Tests_SRS_IOTHUBTRANSPORTPACKET_34_006: [This function shall save the provided status.]
    @Test
    public void setStatusSavesStatus()
    {
        //arrange
        IotHubStatusCode expectedStatus = IotHubStatusCode.ERROR;
        IotHubTransportPacket packet = new IotHubTransportPacket(mockMsg, mockCallback, new Object(), IotHubStatusCode.MESSAGE_CANCELLED_ONCLOSE, 10, null);

        //act
        packet.setStatus(expectedStatus);

        //assert
        assertEquals(expectedStatus, packet.getStatus());
    }

    // Tests_SRS_IOTHUBTRANSPORTPACKET_11_001: [The constructor shall save the message, callback, status, startTimeMillis, and callback context.]
    // Tests_SRS_IOTHUBTRANSPORTPACKET_34_007: [This function shall return the saved startTimeMillis.]
    @Test
    public void getStartTimeMillisReturnsStartTime()
    {
        //arrange
        long expectedStartTime = 1230;

        //act
        IotHubTransportPacket packet = new IotHubTransportPacket(mockMsg, mockCallback, new Object(), IotHubStatusCode.OK_EMPTY, expectedStartTime, null);
        long actualStartTime = packet.getStartTimeMillis();

        //assert
        assertEquals(expectedStartTime, actualStartTime);
    }

    // Tests_SRS_IOTHUBTRANSPORTPACKET_34_008: [This function shall return the saved current retry attempt.]
    @Test
    public void getRetryAttemptReturns()
    {
        //arrange
        final int expectedRetryAttempt = 123;
        IotHubTransportPacket packet = new IotHubTransportPacket(mockMsg, mockCallback, new Object(), IotHubStatusCode.OK_EMPTY, 10, null);
        Deencapsulation.setField(packet, "currentRetryAttempt", expectedRetryAttempt);

        //act
        long actualRetryAttempt = packet.getCurrentRetryAttempt();

        //assert
        assertEquals(expectedRetryAttempt, actualRetryAttempt);
    }

    // Tests_SRS_IOTHUBTRANSPORTPACKET_34_009: [This function shall increment the saved retry attempt count by 1.]
    @Test
    public void incrementRetryAttemptIncrementsByOne()
    {
        //arrange
        final long expectedRetryAttempt = 1;
        IotHubTransportPacket packet = new IotHubTransportPacket(mockMsg, mockCallback, new Object(), IotHubStatusCode.OK, 10, null);
        Deencapsulation.setField(packet, "currentRetryAttempt", 0);

        //act
        packet.incrementRetryAttempt();

        //assert
        int actualRetryAttempt = Deencapsulation.getField(packet, "currentRetryAttempt");
        assertEquals(expectedRetryAttempt, actualRetryAttempt);
    }

    // Tests_SRS_IOTHUBTRANSPORTPACKET_34_011: [If message is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullMessage()
    {
        //act
        new IotHubTransportPacket(null, mockCallback, new Object(), IotHubStatusCode.OK_EMPTY, 1, null);
    }

    // Tests_SRS_IOTHUBTRANSPORTPACKET_34_010: [If startTimeMillis is 0 or negative, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForZeroMillisecondsStartTime()
    {
        //act
        new IotHubTransportPacket(mockMsg, mockCallback, new Object(), IotHubStatusCode.OK_EMPTY, 0, null);
    }

    // Tests_SRS_IOTHUBTRANSPORTPACKET_34_010: [If startTimeMillis is 0 or negative, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNegativeMillisecondsStartTime()
    {
        //act
        new IotHubTransportPacket(mockMsg, mockCallback, new Object(), IotHubStatusCode.OK_EMPTY, -1, null);
    }
}
