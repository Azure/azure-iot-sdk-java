// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;
import org.apache.qpid.proton.amqp.Symbol;

import java.util.HashMap;
import java.util.Map;

public class AmqpsDeviceOperations
{
    protected String VERSION_IDENTIFIER_KEY = "com.microsoft:client-version";

    protected String SENDER_LINK_ENDPOINT_PATH = "";
    protected String RECEIVER_LINK_ENDPOINT_PATH = "";

    protected String SENDER_LINK_TAG = "";
    protected String RECEIVER_LINK_TAG = "";

    protected String senderLinkAddress;
    protected String receiverLinkAddress;

    Map<Symbol, Object> amqpProperties = new HashMap<>();

    public AmqpsDeviceOperations()
    {
        amqpProperties.put(Symbol.getSymbol(VERSION_IDENTIFIER_KEY), TransportUtils.JAVA_DEVICE_CLIENT_IDENTIFIER + TransportUtils.CLIENT_VERSION);
    }

    public Map<Symbol, Object> getAmqpProperties()
    {
        return amqpProperties;
    }

    public String getSenderLinkTag()
    {
        return SENDER_LINK_TAG;
    }

    public String getReceiverLinkTag()
    {
        return RECEIVER_LINK_TAG;
    }

    public String getSenderLinkAddress()
    {
        return senderLinkAddress;
    }

    public String getReceiverLinkAddress()
    {
        return receiverLinkAddress;
    }
}
