/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.sdk.iot.deps.ws.impl;

import com.microsoft.azure.sdk.iot.deps.ws.WebSocketHeader;
import com.microsoft.azure.sdk.iot.deps.ws.impl.WebSocketSniffer;
import org.apache.qpid.proton.engine.impl.TransportWrapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class WebSocketSnifferTest
{
    @Test
    public void testMakeDetermination_wrapper1()
    {
        TransportWrapper mockTransportWrapper1 = mock(TransportWrapper.class);
        TransportWrapper mockTransportWrapper2 = mock(TransportWrapper.class);

        WebSocketSniffer webSocketSniffer = new WebSocketSniffer(mockTransportWrapper1, mockTransportWrapper2);

        assertEquals("Incorrect header size", WebSocketHeader.MIN_HEADER_LENGTH_MASKED, webSocketSniffer.bufferSize());

        byte[] bytes = new byte[WebSocketHeader.MIN_HEADER_LENGTH_MASKED];
        bytes[0] = WebSocketHeader.FINAL_OPCODE_BINARY;

        webSocketSniffer.makeDetermination(bytes);
        assertEquals("Incorrect wrapper selected", mockTransportWrapper1, webSocketSniffer.getSelectedTransportWrapper());
    }

    @Test
    public void testMakeDetermination_wrapper2()
    {
        TransportWrapper mockTransportWrapper1 = mock(TransportWrapper.class);
        TransportWrapper mockTransportWrapper2 = mock(TransportWrapper.class);

        WebSocketSniffer webSocketSniffer = new WebSocketSniffer(mockTransportWrapper1, mockTransportWrapper2);

        assertEquals("Incorrect header size", WebSocketHeader.MIN_HEADER_LENGTH_MASKED, webSocketSniffer.bufferSize());

        byte[] bytes = new byte[WebSocketHeader.MIN_HEADER_LENGTH_MASKED];
        bytes[0] = (byte) 0x81;

        webSocketSniffer.makeDetermination(bytes);
        assertEquals("Incorrect wrapper selected", mockTransportWrapper2, webSocketSniffer.getSelectedTransportWrapper());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMakeDetermination_insufficient_bytes()
    {
        TransportWrapper mockTransportWrapper1 = mock(TransportWrapper.class);
        TransportWrapper mockTransportWrapper2 = mock(TransportWrapper.class);

        WebSocketSniffer webSocketSniffer = new WebSocketSniffer(mockTransportWrapper1, mockTransportWrapper2);

        assertEquals("Incorrect header size", WebSocketHeader.MIN_HEADER_LENGTH_MASKED, webSocketSniffer.bufferSize());

        byte[] bytes = new byte[WebSocketHeader.MIN_HEADER_LENGTH_MASKED - 1];
        bytes[0] = WebSocketHeader.FINAL_OPCODE_BINARY;

        webSocketSniffer.makeDetermination(bytes);
    }
}
