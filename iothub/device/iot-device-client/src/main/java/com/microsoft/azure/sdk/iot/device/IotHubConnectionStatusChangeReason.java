/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.device;

public enum IotHubConnectionStatusChangeReason
{
    /**
     *  The SAS token associated with the client has expired, and cannot be renewed.
     *  The supplied credentials need to be fixed before a connection can be established.
     *  <p>This is returned with a connection status of {@link com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus#DISCONNECTED}.</p>
     */
    EXPIRED_SAS_TOKEN,

    /**
     * Incorrect credentials were supplied to the client instance.
     * The supplied credentials need to be fixed before a connection can be established.
     * <p>This is returned with a connection status of {@link com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus#DISCONNECTED}.</p>
     */
    BAD_CREDENTIAL,

    /**
     * The client was disconnected due to a transient exception, but the retry policy expired before a connection could be re-established.
     * If you want to perform more operations on the device client, you should {@link DeviceClient#close()} and then {@link DeviceClient#open(boolean)} the client.
     * <p>This is returned with a connection status of {@link com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus#DISCONNECTED}.</p>
     */
    RETRY_EXPIRED,

    /**
     * The client was disconnected due to loss of network, the client will attempt for recovery.
     * <p>This is returned with a connection status of {@link com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus#DISCONNECTED_RETRYING}.</p>
     */
    NO_NETWORK,

    /**
     * This can be returned with either a connection status of {@link com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus#DISCONNECTED} or {@link com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus#DISCONNECTED_RETRYING}.
     * <p>When returned with a connection status of {@link com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus#DISCONNECTED_RETRYING}, this signifies that the client is trying to recover from a disconnect due to a transient exception.
     * Do NOT close or open the client instance. Once the client successfully reports {@link com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus#CONNECTED}, operations will be resumed.</p>
     * <p>When returned with a connection status of {@link com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus#DISCONNECTED}, this signifies that client is disconnected due to a non-retryable exception.
     * You should inspect the throwable supplied in the {@link IotHubConnectionStatusChangeCallback} to determine what action needs to be taken.
     * If you want to perform more operations on the device client, you should {@link DeviceClient#close()} and then {@link DeviceClient#open(boolean)} the client.</p>
     */
    COMMUNICATION_ERROR,

    /**
     * The client is connected, and ready to be used.
     * <p>This is returned with a connection status of {@link com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus#CONNECTED}.</p>
     */
    CONNECTION_OK,

    /**
     * The client has been closed gracefully.
     *  <p>This is returned with a connection status of {@link com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus#DISCONNECTED}.</p>
     */
    CLIENT_CLOSE
}
