// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.https;

/** A URI for a device to poll for messages from an IoT Hub. */
final class IotHubMessageUri
{
    /** The path to be appended to an IoT Hub URI. */
    private static final String MESSAGE_PATH = "/messages/devicebound";

    /** The underlying IoT Hub URI. */
    private final IotHubUri uri;

    /**
     * Constructor. Returns a URI for a device to poll for messages from an
     * IoT Hub. The URI does not include a protocol.
     *
     * @param iotHubHostname the IoT Hub name.
     * @param deviceId the device ID.
     * @param moduleId the module ID. May be null
     */
    public IotHubMessageUri(String iotHubHostname, String deviceId, String moduleId)
    {
        this.uri = new IotHubUri(iotHubHostname, deviceId, MESSAGE_PATH, moduleId);
    }

    /**
     * Returns the string representation of the IoT Hub message URI.
     *
     * @return the string representation of the IoT Hub message URI.
     */
    @Override
    public String toString()
    {
        return (this.uri != null ? this.uri.toString() : null);
    }

    /**
     * Returns the string representation of the IoT Hub hostname.
     *
     * @return the string representation of the IoT Hub hostname.
     */
    public String getHostname()
    {
        return (this.uri != null ? this.uri.getHostname() : null);
    }

    /**
     * Returns the string representation of the IoT Hub path.
     *
     * @return the string representation of the IoT Hub path.
     */
    public String getPath()
    {
        return (this.uri != null ? this.uri.getPath() : null);
    }

    @SuppressWarnings("unused")
    protected IotHubMessageUri()
    {
        this.uri = null;
    }
}
