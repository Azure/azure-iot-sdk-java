/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.registry;

import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.registry.serializers.DeviceCapabilitiesParser;
import com.microsoft.azure.sdk.iot.service.registry.serializers.RegistryIdentityParser;
import com.microsoft.azure.sdk.iot.service.twin.DeviceCapabilities;
import com.microsoft.azure.sdk.iot.service.ParserUtility;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * The Device class extends the RegistryIdentity class
 * implementing constructors and serialization functionality.
 */
public class Device extends RegistryIdentity
{
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
     * Creates a device using the given id. The device will use Symmetric Key for authentication.
     *
     * @param deviceId String containing the device name.
     */
    public Device(String deviceId)
    {
        this(deviceId, AuthenticationType.SAS);
    }

    /**
     * Creates a device using the given id. The device will use Symmetric Key for authentication.
     *
     * @param deviceId String containing the device name.
     * @param authenticationType the type of authentication that this device will use.
     */
    public Device(String deviceId, AuthenticationType authenticationType)
    {
        super(deviceId, authenticationType);
        this.status = DeviceStatus.Enabled;
        this.statusUpdatedTime = UTC_TIME_DEFAULT;
    }

    /**
     * Converts this into a {@link RegistryIdentityParser} object. To serialize a Device object, it must first be converted to a
     * {@link RegistryIdentityParser} object.
     *
     * @return The {@link RegistryIdentityParser} object that can be serialized.
     */
    RegistryIdentityParser toRegistryIdentityParser()
    {
        RegistryIdentityParser registryIdentityParser = super.toRegistryIdentityParser();
        registryIdentityParser.setStatus(this.status.toString());
        registryIdentityParser.setStatusReason(this.statusReason);
        registryIdentityParser.setStatusUpdatedTime(ParserUtility.getDateTimeUtc(this.statusUpdatedTime));

        if (this.capabilities != null)
        {
            registryIdentityParser.setCapabilities(new DeviceCapabilitiesParser());
            registryIdentityParser.getCapabilities().setIotEdge(this.capabilities.isIotEdge());
        }

        registryIdentityParser.setScope(this.scope);
        registryIdentityParser.setParentScopes(this.parentScopes);

        return registryIdentityParser;
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
    Device(RegistryIdentityParser parser) throws IllegalArgumentException
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
}
