/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

/**
 * Enum for device connection state
 */
public enum DeviceConnectionState
{
    Disconnected("Disconnected"),
    Connected("Connected"),

    // For some reason, preview hubs send this value with a lower case "disconnected" vs "Disconnected" and "connected" vs "Connected"
    disconnected("disconnected"),
    connected("connected");

    private final String connectionState;

    DeviceConnectionState(String state)
    {
        this.connectionState = state;
    }

    public String getValue()
    {
        return connectionState;
    }

    public static DeviceConnectionState fromString(String connectionState)
    {
        for (DeviceConnectionState possibleConnectionState : DeviceConnectionState.values())
        {
            if (possibleConnectionState.connectionState.equalsIgnoreCase(connectionState))
            {
                return possibleConnectionState;
            }
        }
        return null;
    }
}

