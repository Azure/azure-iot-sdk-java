// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import org.apache.qpid.proton.amqp.Symbol;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AmqpsDeviceTwin extends AmqpsDeviceOperations
{
    private final String CORRELATION_ID_KEY = "com.microsoft:channel-correlation-id";

    private final String API_VERSION_KEY = "com.microsoft:api-version";
    private final String API_VERSION_VALUE = "2016-11-14";

    public final static String MESSAGE_ANNOTATION_FIELD_KEY_OPERATION = "operation";
    public final static String MESSAGE_ANNOTATION_FIELD_KEY_RESOURCE = "resource";
    public final static String MESSAGE_ANNOTATION_FIELD_VALUE_OPERATION_GET = "GET";
    public final static String MESSAGE_ANNOTATION_FIELD_VALUE_OPERATION_PATCH = "PATCH";
    public final static String MESSAGE_ANNOTATION_FIELD_VALUE_OPERATION_PUT = "PUT";

    public final static String MESSAGE_ANNOTATION_FIELD_VALUE_RESOURCE_REPORTED = "/properties/reported";
    public final static String MESSAGE_ANNOTATION_FIELD_VALUE_RESOURCE_DESIRED = "/notifications/twin/properties/desired";

    public AmqpsDeviceTwin(String deviceId) throws IOException
    {
        SENDER_LINK_TAG = "sender_link_devicetwin";
        RECEIVER_LINK_TAG = "receiver_link_devicetwin";

        SENDER_LINK_ENDPOINT_PATH = "/devices/%s/twin";
        RECEIVER_LINK_ENDPOINT_PATH = "/devices/%s/twin";

        senderLinkAddress = String.format(SENDER_LINK_ENDPOINT_PATH, deviceId);
        receiverLinkAddress = String.format(RECEIVER_LINK_ENDPOINT_PATH, deviceId);

        amqpProperties.put(Symbol.getSymbol(CORRELATION_ID_KEY), Symbol.getSymbol("twin:"+deviceId));
        amqpProperties.put(Symbol.getSymbol(API_VERSION_KEY), API_VERSION_VALUE);
    }
}
