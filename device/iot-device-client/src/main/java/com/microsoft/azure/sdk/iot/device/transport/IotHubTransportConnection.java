/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;

import java.util.Queue;

public interface IotHubTransportConnection
{
    void open(Queue<DeviceClientConfig> deviceClientConfigs) throws TransportException;
    void setListener(IotHubListener listener) throws TransportException;
    void close() throws TransportException;
    IotHubStatusCode sendMessage(Message message) throws TransportException;
    boolean sendMessageResult(Message message, IotHubMessageResult result) throws TransportException;
}
