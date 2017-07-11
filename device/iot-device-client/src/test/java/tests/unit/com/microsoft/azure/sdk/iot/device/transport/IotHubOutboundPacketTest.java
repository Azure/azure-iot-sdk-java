// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubResponseCallback;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.transport.IotHubOutboundPacket;
import mockit.Mocked;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/** Unit tests for IotHubOutboundPacket. */
public class IotHubOutboundPacketTest
{
    @Mocked
    Message mockMsg;
    @Mocked
    IotHubEventCallback mockCallback;
    @Mocked
    IotHubResponseCallback mockResponseCallback;

    // Tests_SRS_IOTHUBOUTBOUNDPACKET_11_001: [The constructor shall save the message, callback, and callback context.]
    // Tests_SRS_IOTHUBOUTBOUNDPACKET_11_002: [The function shall return the message given in the constructor.]
    @Test
    public void getMessageReturnsMessage()
    {
        final Map<String, Object> context = new HashMap<>();

        IotHubOutboundPacket packet =
                new IotHubOutboundPacket(mockMsg, mockCallback, context);
        Message testMsg = packet.getMessage();

        final Message expectedMsg = mockMsg;
        assertThat(testMsg, is(expectedMsg));
    }

    // Tests_SRS_IOTHUBOUTBOUNDPACKET_11_001: [The constructor shall save the message, callback, and callback context.]
    // Tests_SRS_IOTHUBOUTBOUNDPACKET_11_003: [The function shall return the event callback given in the constructor.]
    // Tests_SRS_IOTHUBOUTBOUNDPACKET_21_007: [The constructor shall set the response callback as null.]
    @Test
    public void getCallbackReturnsCallback()
    {
        final Map<String, Object> context = new HashMap<>();

        IotHubOutboundPacket packet =
                new IotHubOutboundPacket(mockMsg, mockCallback, context);
        IotHubEventCallback testCallback = packet.getCallback();
        IotHubResponseCallback testResponseCallback = packet.getResponseCallback();

        final IotHubEventCallback expectedCallback = mockCallback;
        assertThat(testCallback, is(expectedCallback));
        assertNull(testResponseCallback);
    }

    // Tests_SRS_IOTHUBOUTBOUNDPACKET_21_005: [The constructor shall save the message, callback, and callback context.]
    // Tests_SRS_IOTHUBOUTBOUNDPACKET_11_006: [The function shall return the response callback given in the constructor.]
    // Tests_SRS_IOTHUBOUTBOUNDPACKET_21_008: [The constructor shall set the event callback as null.]
    @Test
    public void getCallbackWithMessageReturnsCallback()
    {
        final Map<String, Object> context = new HashMap<>();

        IotHubOutboundPacket packet =
                new IotHubOutboundPacket(mockMsg, mockResponseCallback, context);
        IotHubEventCallback testCallback = packet.getCallback();
        IotHubResponseCallback testResponseCallback = packet.getResponseCallback();

        final IotHubResponseCallback expectedCallback = mockResponseCallback;
        assertThat(testResponseCallback, is(expectedCallback));
        assertNull(testCallback);
    }

    // Tests_SRS_IOTHUBOUTBOUNDPACKET_11_001: [The constructor shall save the message, callback, and callback context.]
    // Tests_SRS_IOTHUBOUTBOUNDPACKET_11_004: [The function shall return the callback context given in the constructor.] 
    @Test
    public void getContextReturnsContext()
    {
        final Map<String, Object> context = new HashMap<>();
        final String key = "test-key";
        final String value = "test-value";
        context.put(key, value);

        IotHubOutboundPacket packet =
                new IotHubOutboundPacket(mockMsg, mockCallback, context);
        Map<String, Object> testContext =
                (Map<String, Object>) packet.getContext();
        Set<Map.Entry<String, Object>> testEntrySet = testContext.entrySet();

        final Set<Map.Entry<String, Object>> expectedEntrySet =
                context.entrySet();
        assertThat(testEntrySet, everyItem(isIn(expectedEntrySet)));
    }
}
