// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import org.apache.qpid.proton.amqp.Symbol;

import java.io.IOException;

public class AmqpsDeviceMethods extends AmqpsDeviceOperations
{
    public final static String APPLICATION_PROPERTY_KEY_IOTHUB_METHOD_NAME = "IoThub-methodname";
    public final static String APPLICATION_PROPERTY_KEY_IOTHUB_STATUS = "IoThub-status";

    private final String CORRELATION_ID_KEY = "com.microsoft:channel-correlation-id";

    private final String API_VERSION_KEY = "com.microsoft:api-version";
    private final String API_VERSION_VALUE = "2016-11-14";

    public AmqpsDeviceMethods(String deviceId) throws IOException
    {
        SENDER_LINK_TAG = "sender_link_devicemethods";
        RECEIVER_LINK_TAG = "receiver_link_devicemethods";

        SENDER_LINK_ENDPOINT_PATH = "/devices/%s/methods/devicebound";
        RECEIVER_LINK_ENDPOINT_PATH = "/devices/%s/methods/devicebound";

        senderLinkAddress = String.format(SENDER_LINK_ENDPOINT_PATH, deviceId);
        receiverLinkAddress = String.format(RECEIVER_LINK_ENDPOINT_PATH, deviceId);

        amqpProperties.put(Symbol.getSymbol(CORRELATION_ID_KEY), Symbol.getSymbol(deviceId));
        amqpProperties.put(Symbol.getSymbol(API_VERSION_KEY), API_VERSION_VALUE);
    }
}
