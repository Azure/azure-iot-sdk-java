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
    //TODO (Tim) change this back to TransportException once HTTP and AMQP are done
    void open(Queue<DeviceClientConfig> deviceClientConfigs) throws Exception;
    void setListener(IotHubListener listener) throws Exception;
    void close() throws Exception;
    IotHubStatusCode sendMessage(Message message) throws Exception;
    boolean sendMessageResult(Message message, IotHubMessageResult result) throws Exception;
}
