/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.jobs;

import com.microsoft.azure.sdk.iot.service.auth.AuthenticationMechanism;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.registry.DeviceStatus;
import com.microsoft.azure.sdk.iot.service.registry.serializers.AuthenticationParser;
import com.microsoft.azure.sdk.iot.service.registry.serializers.AuthenticationTypeParser;
import com.microsoft.azure.sdk.iot.service.registry.serializers.SymmetricKeyParser;
import com.microsoft.azure.sdk.iot.service.registry.serializers.X509ThumbprintParser;
import com.microsoft.azure.sdk.iot.service.twin.TwinCollection;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.UUID;

public class ExportImportDevice
{
    @NonNull
    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    private String eTag;

    @Getter
    @Setter
    private ImportMode importMode;

    @Getter
    @Setter
    private DeviceStatus status;

    @Getter
    @Setter
    private String statusReason;

    private AuthenticationMechanism authentication;

    @Getter
    @Setter
    private TwinCollection tags = null;

    @Getter
    @Setter
    private TwinCollection reportedProperties = null;

    @Getter
    @Setter
    private TwinCollection desiredProperties = null;

    /**
     * Default constructor for an ExportImportDevice object. Randomly generates a device ID and uses a randomly generated shared access signature for authentication
     */
    public ExportImportDevice()
    {
        this.authentication = new AuthenticationMechanism(AuthenticationType.SAS);
        this.id = "exportImportDevice_" + UUID.randomUUID();
    }

    /**
     * Constructor for an ExportImportDevice object.
     * @param deviceId the id of the new device
     * @param authenticationType the type of authentication to be used. For shared access signature and self signed x.509, all keys shall be generated automatically.
     * @throws IllegalArgumentException if the provided deviceId or authenticationType is null or empty
     */
    public ExportImportDevice(String deviceId, AuthenticationType authenticationType) throws IllegalArgumentException
    {
        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("DeviceId cannot be null.");
        }

        if (authenticationType == null)
        {
            throw new IllegalArgumentException("AuthenticationType cannot be null");
        }

        this.authentication = new AuthenticationMechanism(authenticationType);
        this.id = deviceId;
    }

    /**
     * Getter for device authentication mechanism.
     * @return The device authentication mechanism.
     */
    public AuthenticationMechanism getAuthentication()
    {
        return authentication;
    }

    /**
     * Setter for device authentication mechanism.
     * @param authentication The device authentication mechanism.
     * @throws IllegalArgumentException if the provided authentication is null
     */
    public void setAuthentication(AuthenticationMechanism authentication) throws IllegalArgumentException
    {
        if (authentication == null)
        {
            throw new IllegalArgumentException("The provided authentication object may not be null");
        }

        this.authentication = authentication;
    }

    /**
     * Converts this into a ExportImportDeviceParser object. To serialize a ExportImportDevice object, it must first be converted to a ExportImportDeviceParser object.
     * @return the ExportImportDeviceParser object that can be serialized.
     */
    @SuppressWarnings("unused") //Unknown if this is actually used outside of a test.
    ExportImportDeviceParser toExportImportDeviceParser()
    {
        ExportImportDeviceParser parser = new ExportImportDeviceParser();
        parser.setETag(this.eTag);
        parser.setId(this.id);
        parser.setStatusReason(this.statusReason);

        if (this.importMode != null)
        {
            parser.setImportMode(this.importMode.toString());
        }

        if (this.status != null)
        {
            parser.setStatus(this.status.toString());
        }

        if (this.authentication != null)
        {
            parser.setAuthentication(new AuthenticationParser());
            if (this.getAuthentication().getAuthenticationType() != null)
            {
                parser.getAuthentication().setType(AuthenticationTypeParser.valueOf(this.authentication.getAuthenticationType().toString()));

                //noinspection StatementWithEmptyBody
                if (this.getAuthentication().getAuthenticationType() == AuthenticationType.CERTIFICATE_AUTHORITY)
                {
                    //do nothing
                }
                else if (this.getAuthentication().getAuthenticationType() == AuthenticationType.SELF_SIGNED)
                {
                    if (this.authentication.getPrimaryThumbprint() != null && this.authentication.getSecondaryThumbprint() != null)
                    {
                        parser.getAuthentication().setThumbprint(new X509ThumbprintParser(
                                this.authentication.getPrimaryThumbprint(),
                                this.authentication.getSecondaryThumbprint()));
                    }
                    else
                    {
                        throw new IllegalStateException("ExportImportDevice cannot have self signed authentication without a complete thumbprint.");
                    }
                }
                else if (this.getAuthentication().getAuthenticationType() == AuthenticationType.SAS)
                {
                    if (this.authentication.getSymmetricKey() != null
                            && this.authentication.getSymmetricKey().getPrimaryKey() != null
                            && this.authentication.getSymmetricKey().getSecondaryKey() != null)
                    {
                        parser.getAuthentication().setSymmetricKey(new SymmetricKeyParser(
                                this.authentication.getSymmetricKey().getPrimaryKey(),
                                this.authentication.getSymmetricKey().getSecondaryKey()));
                    }
                    else
                    {
                        throw new IllegalStateException("ExportImportDevice cannot have SAS authentication without a complete symmetric key.");
                    }
                }
            }
        }
        
        parser.setTags(this.tags);

        return parser;
    }
}