// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.transport;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.isIn;

import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubResponseCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.ResponseMessage;
import com.microsoft.azure.sdk.iot.device.transport.IotHubCallbackPacket;

import com.microsoft.azure.sdk.iot.device.transport.IotHubOutboundPacket;
import mockit.Mocked;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Unit tests for IotHubCallbackPacket. */
public class IotHubCallbackPacketTest
{
    @Mocked
    IotHubEventCallback mockEventCallback;
    @Mocked
    IotHubResponseCallback mockResponseCallback;
    @Mocked
    IotHubOutboundPacket mockIotHubOutboundPacket;
    @Mocked
    ResponseMessage mockMessage;

    // Tests_SRS_IOTHUBCALLBACKPACKET_11_001: [The constructor shall save the status, eventCallback, and callback context.]
    // Tests_SRS_IOTHUBCALLBACKPACKET_11_002: [The function shall return the status given in the constructor.]
    @Test
    public void getStatusReturnsStatus()
    {
        final IotHubStatusCode status = IotHubStatusCode.BAD_FORMAT;
        final Map<String, Object> context = new HashMap<>();

        IotHubCallbackPacket packet =
                new IotHubCallbackPacket(status, mockEventCallback, context);
        IotHubStatusCode testStatus = packet.getStatus();

        final IotHubStatusCode expectedStatus = status;
        assertThat(testStatus, is(expectedStatus));
    }

    // Tests_SRS_IOTHUBCALLBACKPACKET_11_001: [The constructor shall save the status, eventCallback, and callback context.]
    // Tests_SRS_IOTHUBCALLBACKPACKET_11_003: [The function shall return the eventCallback given in the constructor.]
    @Test
    public void getEventCallbackReturnsCallback()
    {
        final IotHubStatusCode status =
                IotHubStatusCode.HUB_OR_DEVICE_ID_NOT_FOUND;
        final Map<String, Object> context = new HashMap<>();

        IotHubCallbackPacket packet =
                new IotHubCallbackPacket(status, mockEventCallback, context);
        IotHubEventCallback testCallback = packet.getCallback();

        assertThat(testCallback, is(mockEventCallback));
    }

    // Tests_SRS_IOTHUBCALLBACKPACKET_11_001: [The constructor shall save the status, eventCallback, and callback context.]
    // Tests_SRS_IOTHUBCALLBACKPACKET_11_004: [The function shall return the callback context given in the constructor.]
    @Test
    public void getContextReturnsContext()
    {
        final IotHubStatusCode status = IotHubStatusCode.OK;
        final Map<String, Object> context = new HashMap<>();
        final String key = "test-key";
        final String value = "test-value";
        context.put(key, value);

        IotHubCallbackPacket packet =
                new IotHubCallbackPacket(status, mockEventCallback, context);
        Map<String, Object> testContext =
                (Map<String, Object>) packet.getContext();
        Set<Map.Entry<String, Object>> testEntrySet = testContext.entrySet();

        final Set<Map.Entry<String, Object>> expectedEntrySet =
                context.entrySet();
        assertThat(testEntrySet, everyItem(isIn(expectedEntrySet)));
    }

    // Tests_SRS_IOTHUBCALLBACKPACKET_11_001: [The constructor shall save the status, eventCallback, and callback context.]
    // Tests_SRS_IOTHUBCALLBACKPACKET_21_007: [The constructor shall set message and responseCallback as null.]
    @Test
    public void getResponseCallbackReturnsNull()
    {
        final IotHubStatusCode status =
                IotHubStatusCode.HUB_OR_DEVICE_ID_NOT_FOUND;
        final Map<String, Object> context = new HashMap<>();

        IotHubCallbackPacket packet =
                new IotHubCallbackPacket(status, mockEventCallback, context);
        IotHubResponseCallback testCallback = packet.getResponseCallback();
        ResponseMessage testMessage = packet.getResponseMessage();

        assertNull(testCallback);
        assertNull(testMessage);
    }

    // Tests_SRS_IOTHUBCALLBACKPACKET_21_006: [The constructor shall save the responseMessage, responseCallback, and callback context.]
    // Tests_SRS_IOTHUBCALLBACKPACKET_21_005: [The getResponseCallback shall return the responseCallback given in the constructor.]
    @Test
    public void getResponseCallbackReturnsCallback()
    {
        // arrange
        final Map<String, Object> context = new HashMap<>();

        // act
        IotHubCallbackPacket packet =
                new IotHubCallbackPacket(mockMessage, mockResponseCallback, context);

        // assert
        IotHubResponseCallback testCallback = packet.getResponseCallback();
        assertThat(testCallback, is(mockResponseCallback));

        ResponseMessage testMessage = packet.getResponseMessage();
        assertThat(testMessage, is(mockMessage));

        Map<String, Object> testContext =
                (Map<String, Object>) packet.getContext();
        Set<Map.Entry<String, Object>> testEntrySet = testContext.entrySet();
        final Set<Map.Entry<String, Object>> expectedEntrySet =
                context.entrySet();
        assertThat(testEntrySet, everyItem(isIn(expectedEntrySet)));
    }

    // Tests_SRS_IOTHUBCALLBACKPACKET_21_009: [The constructor shall set status and eventCallback as null.]
    @Test
    public void getResponseCallbackSetEventCallbackAndStatusAsNull()
    {
        // arrange
        final Map<String, Object> context = new HashMap<>();

        // act
        IotHubCallbackPacket packet =
                new IotHubCallbackPacket(mockMessage, mockResponseCallback, context);

        // assert
        assertNull(packet.getCallback());
        assertNull(packet.getStatus());
    }

}
