/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.deps.serializer.*;
import com.microsoft.azure.sdk.iot.deps.twin.DeviceCapabilities;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.SymmetricKey;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * The Device class extends the BaseDevice class
 * implementing constructors and serialization functionality.
 */
public class Device extends BaseDevice
{
    /**
     * Static create function.
     * Creates device object using the given name.
     * If input device status and symmetric key are null then they will be auto generated.
     *
     * @param deviceId String containing the device name.
     * @param status Device status. If parameter is null, then the status will be set to {@code "Enabled"}.
     * @param symmetricKey Device key. If parameter is null, then the key will be auto generated.
     * @return Device object
     * @throws IllegalArgumentException This exception is thrown if {@code deviceId} is {@code null} or empty.
     */
    public static Device createFromId(String deviceId, DeviceStatus status, SymmetricKey symmetricKey)
            throws IllegalArgumentException
    {
        if (Tools.isNullOrEmpty(deviceId))
        {
            throw new IllegalArgumentException(deviceId);
        }

        return new Device(deviceId, status, symmetricKey);
    }

    /**
     * Static create function.
     * Creates device object using the given name that will use a Certificate Authority signed certificate for
     * authentication.
     * If input device status is {@code null} then it will be auto-generated.
     *
     * @param deviceId String containing the device name.
     * @param authenticationType The type of authentication used by this device.
     * @return Device object.
     * @throws IllegalArgumentException This exception is thrown if {@code deviceId} is {@code null} or empty.
     */
    public static Device createDevice(String deviceId, AuthenticationType authenticationType)
    {
        if (Tools.isNullOrEmpty(deviceId))
        {
            throw new IllegalArgumentException("The provided device Id must not be null or empty");
        }

        if (authenticationType == null)
        {
            throw new IllegalArgumentException("The provided authentication type must not be null");
        }

        return new Device(deviceId, authenticationType);
    }

    /**
     * Create an Device instance using the given device name.
     *
     * @param deviceId Name of the device (used as device id)
     * @param status Device status. If parameter is null, then the status will be set to {@code "Enabled"}.
     * @param symmetricKey Device key. If parameter is {@code null}, then the key will be auto-generated.
     * @throws IllegalArgumentException This exception is thrown if the encryption method is not supported by the key
     * generator.
     */
    private Device(String deviceId, DeviceStatus status, SymmetricKey symmetricKey)
        throws IllegalArgumentException
    {
        super(deviceId, symmetricKey);

        this.setPropertiesToDefaultValues();
        this.status = status != null ? status : DeviceStatus.Enabled;
    }

    /**
     * Create an Device instance using the given device name that uses a certificate authority signed certificate.
     *
     * @param deviceId Name of the device (used as device id).
     * @param authenticationType The type of authentication used by this device.
     */
    private Device(String deviceId, AuthenticationType authenticationType)
    {
        super(deviceId, authenticationType);

        this.setPropertiesToDefaultValues();
    }

    /**
     * If {@code "Enabled"}, this device is authorized to connect.
     * If {@code "Disabled"} this device cannot receive or send messages, and {@link #statusReason} must be set.
     */
    @Getter
    @Setter
    private DeviceStatus status;

    /**
     * A 128 char long string storing the reason of suspension (all UTF-8 chars allowed).
     */
    @Getter
    private String statusReason;

    /**
     * The date time when the status of this device was last updated.
     */
    @Getter
    private String statusUpdatedTime;

    /**
     * The optional capabilities that this device has.
     */
    @Getter
    @Setter
    private DeviceCapabilities capabilities;

    /**
     * <p>For edge devices, this is auto-generated and immutable.</p>
     * <p>For leaf devices, set this to create child/parent relationship. The value to set a parent edge device can be
     * retrieved from calling the parent edge device's {@link #getScope()} method.</p>
     * <p>For more information, see <a href="https://docs.microsoft.com/azure/iot-edge/iot-edge-as-gateway?view=iotedge-2020-11#parent-and-child-relationships">this document</a>.</p>
     */
    @Setter
    @Getter
    private String scope;

    /**
     * The scopes of the upper level edge devices, if applicable.
     * <p>For edge devices, the value to set a parent edge device can be retrieved from the calling parent edge device's
     * {@link #getScope()} method.</p>
     * <p>For leaf devices, this could be set to the same value as {@link #getScope()} or left for the service to copy
     * over.</p>
     * <p>For now, this list can only have 1 element in the collection.</p>
     * <p>For more information, see <a href="https://docs.microsoft.com/azure/iot-edge/iot-edge-as-gateway?view=iotedge-2020-11#parent-and-child-relationships">this document</a>.</p>
     */
    @Getter
    private List<String> parentScopes = new ArrayList<>();

    /**
     * Converts this into a {@link DeviceParser} object. To serialize a Device object, it must first be converted to a
     * {@link DeviceParser} object.
     *
     * @return The {@link DeviceParser} object that can be serialized.
     */
    DeviceParser toDeviceParser()
    {
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
        deviceParser.setParentScopes(this.parentScopes);

        return deviceParser;
    }

    /**
     * Retrieves information from the provided parser and saves it to this. All information on this will be overwritten.
     *
     * @param parser The parser to read from.
     * @throws IllegalArgumentException If the provided parser is missing the authentication field, or the deviceId
     * field. It also shall be thrown if the authentication object in the parser uses SAS authentication and is missing
     * one of the symmetric key fields, or if it uses SelfSigned authentication and is missing one of the thumbprint
     * fields.
     */
    Device(DeviceParser parser) throws IllegalArgumentException
    {
        super(parser);

        this.statusReason = parser.getStatusReason();

        if (parser.getCapabilities() != null)
        {
            this.capabilities = new DeviceCapabilities();
            capabilities.setIotEdge(parser.getCapabilities().getIotEdge());
        }

        if (parser.getStatusUpdatedTime() != null)
        {
            this.statusUpdatedTime = ParserUtility.getUTCDateStringFromDate(parser.getStatusUpdatedTime());
        }

        if (parser.getStatus() != null)
        {
            this.status = DeviceStatus.fromString(parser.getStatus());
        }

        this.scope = parser.getScope();

        this.parentScopes = parser.getParentScopes();
    }

    /*
     * Set default properties for a device.
     */
    private void setPropertiesToDefaultValues()
    {
        this.status = DeviceStatus.Enabled;
        this.statusReason = "";
        this.statusUpdatedTime = UTC_TIME_DEFAULT;
        this.scope = "";
        this.parentScopes = new ArrayList<>();
    }
}
