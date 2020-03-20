/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.deps.serializer.*;
import com.microsoft.azure.sdk.iot.deps.twin.DeviceCapabilities;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.SymmetricKey;

/**
 * The Device class extends the BaseDevice class
 * implementing constructors and serialization functionality.
 */
public class Device extends BaseDevice
{
    /**
     * Static create function
     * Creates device object using the given name.
     * If input device status and symmetric key are null then they will be auto generated.
     *
     * @param deviceId - String containing the device name
     * @param status - Device status. If parameter is null, then the status will be set to Enabled.
     * @param symmetricKey - Device key. If parameter is null, then the key will be auto generated.
     * @return Device object
     * @throws IllegalArgumentException This exception is thrown if {@code deviceId} is {@code null} or empty.
     */
    public static Device createFromId(String deviceId, DeviceStatus status, SymmetricKey symmetricKey)
            throws IllegalArgumentException
    {
        // Codes_SRS_SERVICE_SDK_JAVA_DEVICE_12_002: [The function shall throw IllegalArgumentException if the input string is empty or null]
        if (Tools.isNullOrEmpty(deviceId))
        {
            throw new IllegalArgumentException(deviceId);
        }

        // Codes_SRS_SERVICE_SDK_JAVA_DEVICE_12_003: [The function shall create a new instance
        // of Device using the given deviceId and return it]
        return new Device(deviceId, status, symmetricKey);
    }

    /**
     * Static create function
     * Creates device object using the given name that will use a Certificate Authority signed certificate for authentication.
     * If input device status is null then it will be auto generated.
     *
     * @param deviceId - String containing the device name
     * @param authenticationType - The type of authentication used by this device.
     * @return Device object
     * @throws IllegalArgumentException This exception is thrown if {@code deviceId} is {@code null} or empty.
     */
    public static Device createDevice(String deviceId, AuthenticationType authenticationType)
    {
        // Codes_SRS_SERVICE_SDK_JAVA_DEVICE_34_009: [The function shall throw IllegalArgumentException if the provided deviceId or authenticationType is empty or null.]
        if (Tools.isNullOrEmpty(deviceId))
        {
            throw new IllegalArgumentException("The provided device Id must not be null or empty");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_DEVICE_34_009: [The function shall throw IllegalArgumentException if the provided deviceId or authenticationType is empty or null.]
        if (authenticationType == null)
        {
            throw new IllegalArgumentException("The provided authentication type must not be null");
        }

        return new Device(deviceId, authenticationType);
    }

    /**
     * Create an Device instance using the given device name
     *
     * @param deviceId Name of the device (used as device id)
     * @param status - Device status. If parameter is null, then the status will be set to Enabled.
     * @param symmetricKey - Device key. If parameter is null, then the key will be auto generated.
     * @throws IllegalArgumentException This exception is thrown if the encryption method is not supported by the keyGenerator
     */
    protected Device(String deviceId, DeviceStatus status, SymmetricKey symmetricKey)
        throws IllegalArgumentException
    {
        // Codes_SRS_SERVICE_SDK_JAVA_DEVICE_28_001: [The constructor shall set the deviceId, status and symmetricKey.]
        super(deviceId, symmetricKey);

        // Codes_SRS_SERVICE_SDK_JAVA_DEVICE_12_006: [The constructor shall initialize all properties to default values]
        this.setPropertiesToDefaultValues();
        this.status = status != null ? status : DeviceStatus.Enabled;
    }

    /**
     * Create an Device instance using the given device name that uses a Certificate Authority signed certificate
     *
     * @param deviceId Name of the device (used as device id)
     * @param authenticationType - The type of authentication used by this device.
     */
    private Device(String deviceId, AuthenticationType authenticationType)
    {
        // Codes_SRS_SERVICE_SDK_JAVA_DEVICE_28_002: [The constructor shall set the deviceId and symmetricKey.]
        super(deviceId, authenticationType);

        // Codes_SRS_SERVICE_SDK_JAVA_DEVICE_28_003: [The constructor shall initialize all properties to default values]
        this.setPropertiesToDefaultValues();
    }

    // Codes_SRS_SERVICE_SDK_JAVA_DEVICE_12_001: [The Device class shall have the following properties: deviceId, Etag,
    // SymmetricKey, ConnectionState, ConnectionStateUpdatedTime, LastActivityTime, symmetricKey, thumbprint, authentication]
    /**
     * "Enabled", "Disabled".
     * If "Enabled", this device is authorized to connect.
     * If "Disabled" this device cannot receive or send messages, and statusReason must be set.
     */
    protected DeviceStatus status;

    /**
     * Getter for DeviceStatus object
     * @return The deviceStatus object
     */
    public DeviceStatus getStatus()
    {
        return status;
    }

    /**
     * Setter for DeviceStatus object
     *
     * @param status status to be set
     */
    public void setStatus(DeviceStatus status)
    {
        this.status = status;
    }

    /**
     * A 128 char long string storing the reason of suspension.
     * (all UTF-8 chars allowed).
     */
    protected String statusReason;

    private String scope;

    /**
     * Getter for status reason
     *
     * @return The statusReason string
     */
    public String getStatusReason()
    {
        return statusReason;
    }

    /**
     * Datetime of last time the state was updated.
     */
    protected String statusUpdatedTime;

    /**
     * Getter for status updated time string
     *
     * @return The string containing the time when the statusUpdated parameter was updated
     */
    public String getStatusUpdatedTime()
    {
        return statusUpdatedTime;
    }

    protected DeviceCapabilities capabilities;

    /**
     * Getter for capabilities
     *
     * @return The DeviceCapabilities containing capabilites that are enabled on the device
     */
    public DeviceCapabilities getCapabilities()
    {
        return capabilities;
    }

    /**
     * Setter for DeviceCapabilities object
     *
     * @param capabilities capabilities to be set
     */
    public void setCapabilities(DeviceCapabilities capabilities)
    {
        this.capabilities = capabilities;
    }

    /**
     * Get the security scope for this device
     * @return the security scope for this device
     */
    public String getScope()
    {
        return this.scope;
    }

    /**
     * Set the security scope for this device
     * @param scope the security scope to set
     */
    public void setScope(String scope)
    {
        this.scope = scope;
    }

    /**
     * Converts this into a DeviceParser object. To serialize a Device object, it must first be converted to a DeviceParser object.
     * @return the DeviceParser object that can be serialized.
     */
    DeviceParser toDeviceParser()
    {
        //Codes_SRS_SERVICE_SDK_JAVA_DEVICE_34_018: [This method shall return a new instance of a DeviceParser object that is populated using the properties of this.]
        DeviceParser deviceParser = super.toDeviceParser();
        deviceParser.setStatus(this.status.toString());
        deviceParser.setStatusReason(this.statusReason);
        deviceParser.setStatusUpdatedTime(ParserUtility.getDateTimeUtc(this.statusUpdatedTime));

        if (this.capabilities != null)
        {
            deviceParser.setCapabilities(new DeviceCapabilitiesParser());
            deviceParser.getCapabilities().setIotEdge(this.capabilities.isIotEdge());
        }

        deviceParser.setScope(this.scope);

        return deviceParser;
    }

    /**
     * Retrieves information from the provided parser and saves it to this. All information on this will be overwritten.
     * @param parser the parser to read from
     * @throws IllegalArgumentException if the provided parser is missing the authentication field, or the deviceId field. It also shall
     * be thrown if the authentication object in the parser uses SAS authentication and is missing one of the symmetric key fields,
     * or if it uses SelfSigned authentication and is missing one of the thumbprint fields.
     */
    Device(DeviceParser parser) throws IllegalArgumentException
    {
        //Codes_SRS_SERVICE_SDK_JAVA_DEVICE_34_014: [This constructor shall create a new Device object using the values within the provided parser.]
        super(parser);

        this.statusReason = parser.getStatusReason();

        if (parser.getCapabilities() != null)
        {
            this.capabilities = new DeviceCapabilities();
            capabilities.setIotEdge(parser.getCapabilities().getIotEdge());
        }

        if (parser.getStatusUpdatedTime() != null)
        {
            this.statusUpdatedTime = ParserUtility.getDateStringFromDate(parser.getStatusUpdatedTime());
        }

        if (parser.getStatus() != null)
        {
            this.status = DeviceStatus.fromString(parser.getStatus());
        }

        this.scope = parser.getScope();
    }

    /*
     * Set default properties for a device
     */
    private void setPropertiesToDefaultValues()
    {
        this.status = DeviceStatus.Enabled;
        this.statusReason = "";
        this.statusUpdatedTime = UTC_TIME_DEFAULT;
        this.scope = "";
    }
}
