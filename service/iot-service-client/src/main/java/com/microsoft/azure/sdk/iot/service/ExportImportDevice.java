/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.deps.serializer.*;
import com.microsoft.azure.sdk.iot.deps.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationMechanism;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.UUID;

public class ExportImportDevice
{
    // CODES_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_15_001: [The ExportImportDevice class shall have the following properties: id,
    // Etag, importMode, status, statusReason, authentication]

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
        //Codes_SRS_SERVICE_SDK_JAVA_DEVICE_34_050: [This constructor shall automatically set the authentication type of this object to be SAS, and shall generate a deviceId and symmetric key.]
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
        //Codes_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_057: [If either the provided deviceId or authenticationType is null or empty, an IllegalArgumentException shall be thrown.]
        if (Tools.isNullOrEmpty(deviceId))
        {
            throw new IllegalArgumentException("DeviceId cannot be null.");
        }

        if (authenticationType == null)
        {
            throw new IllegalArgumentException("AuthenticationType cannot be null");
        }

        //Codes_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_051: [This constructor shall save the provided deviceId and authenticationType to itself.]
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
        //Codes_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_056: [If the provided authentication is null, an IllegalArgumentException shall be thrown.]
        if (authentication == null)
        {
            throw new IllegalArgumentException("The provided authentication object may not be null");
        }

        this.authentication = authentication;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if (other instanceof ExportImportDevice)
        {
            ExportImportDevice otherExportImportDevice = (ExportImportDevice) other;

            if (!Tools.areEqual(this.getAuthentication(), otherExportImportDevice.getAuthentication()))
            {
                return false;
            }
            else if (!Tools.areEqual(this.getStatus(), otherExportImportDevice.getStatus()))
            {
                return false;
            }
            else
            {
                return Tools.areEqual(this.getImportMode(), otherExportImportDevice.getImportMode());
            }

        }

        return false;
    }

    @Override
    public int hashCode()
    {
        int result = id.hashCode();
        result = 31 * result + authentication.hashCode();
        return result;
    }

    /**
     * Converts this into a ExportImportDeviceParser object. To serialize a ExportImportDevice object, it must first be converted to a ExportImportDeviceParser object.
     * @return the ExportImportDeviceParser object that can be serialized.
     */
    @SuppressWarnings("unused") //Unknown if this is actually used outside of a test.
    ExportImportDeviceParser toExportImportDeviceParser()
    {
        //Codes_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_054: [This method shall convert this into an ExportImportDeviceParser object and return it.]
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
                        //Codes_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_061: [If this device uses self signed authentication, but does not have a primary and secondary thumbprint saved, an IllegalStateException shall be thrown.]
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
                        //Codes_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_060: [If this device uses sas authentication, but does not have a primary and secondary symmetric key saved, an IllegalStateException shall be thrown.]
                        throw new IllegalStateException("ExportImportDevice cannot have SAS authentication without a complete symmetric key.");
                    }
                }
            }
        }
        
        parser.setTags(this.tags);

        return parser;
    }
}