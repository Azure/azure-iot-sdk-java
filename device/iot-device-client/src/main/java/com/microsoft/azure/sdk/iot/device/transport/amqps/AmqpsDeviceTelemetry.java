// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

public class AmqpsDeviceTelemetry extends AmqpsDeviceOperations
{
    public AmqpsDeviceTelemetry(String deviceId)
    {
        SENDER_LINK_ENDPOINT_PATH = "/devices/%s/messages/events";
        RECEIVER_LINK_ENDPOINT_PATH = "/devices/%s/messages/devicebound";

        SENDER_LINK_TAG = "sender_link_telemetry";
        RECEIVER_LINK_TAG = "receiver_link_telemetry";

        senderLinkAddress = String.format(SENDER_LINK_ENDPOINT_PATH, deviceId);
        receiverLinkAddress = String.format(RECEIVER_LINK_ENDPOINT_PATH, deviceId);
    }
}
