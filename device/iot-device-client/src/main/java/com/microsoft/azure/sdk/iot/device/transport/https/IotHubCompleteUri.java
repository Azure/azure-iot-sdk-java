// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.https;

/**
 * A URI for a device to instruct an IoT Hub to mark a message as being
 * completed.
 */
final class IotHubCompleteUri
{
    /**
     * The path to be appended to an IoT Hub URI. The %s will be replaced by the
     * message etag.
     */
    private static final String COMPLETE_PATH_FORMAT =
            "/messages/devicebound/%s";

    /** The underlying IoT Hub URI. */
    private final IotHubUri uri;

    /**
     * Constructor. Returns a URI for a device to instruct an IoT Hub to mark a
     * message as being completed. The URI does not include a protocol.
     *
     * @param iotHubHostname the IoT Hub name.
     * @param deviceId the device ID.
     * @param eTag the message e-tag.
     * @param moduleId the module id, or null if not communicating as a module
     */
    public IotHubCompleteUri(String iotHubHostname, String deviceId, String eTag, String moduleId)
    {
        String completePath = String.format(COMPLETE_PATH_FORMAT, eTag);
        this.uri = new IotHubUri(iotHubHostname, deviceId, completePath, moduleId);
    }

    /**
     * Returns the string representation of the IoT Hub complete message URI.
     *
     * @return the string representation of the IoT Hub complete message URI.
     */
    @Override
    public String toString()
    {
        return this.uri.toString();
    }

    /**
     * Returns the string representation of the IoT Hub hostname.
     *
     * @return the string representation of the IoT Hub hostname.
     */
    public String getHostname()
    {
        return this.uri.getHostname();
    }

    /**
     * Returns the string representation of the IoT Hub path.
     *
     * @return the string representation of the IoT Hub path.
     */
    public String getPath()
    {
        return this.uri.getPath();
    }

    protected IotHubCompleteUri()
    {
        this.uri = null;
    }
}
