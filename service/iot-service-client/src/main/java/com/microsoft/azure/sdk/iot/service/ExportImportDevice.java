/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.deps.serializer.*;
import com.microsoft.azure.sdk.iot.deps.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationMechanism;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;

import java.util.UUID;

public class ExportImportDevice
{
    // CODES_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_15_001: [The ExportImportDevice class shall have the following properties: id,
    // Etag, importMode, status, statusReason, authentication]

    private String id;
    private String eTag;
    private ImportMode importMode;
    private DeviceStatus status;
    private String statusReason;
    private AuthenticationMechanism authentication;
    private TwinCollection tags = null;
    private TwinCollection reportedProperties = null;
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
     * Setter for device id.
     * @param id The device id.
     * @throws IllegalArgumentException if the provided id is null
     */
    public void setId(String id) throws IllegalArgumentException
    {
        //Codes_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_055: [If the provided id is null, an IllegalArgumentException shall be thrown.]
        if (id == null)
        {
            throw new IllegalArgumentException("The provided id may not be null");
        }

        this.id = id;
    }

    /**
     * Getter for device id.
     * @return The device id.
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * Getter for device eTag.
     * @return The device eTag.
     */
    public String geteTag()
    {
        return eTag;
    }

    /**
     * Setter for device eTag.
     * @param eTag The device eTag.
     */
    public void seteTag(String eTag)
    {
        this.eTag = eTag;
    }

    /**
     * Getter for device import mode.
     * @return The device import mode.
     */
    public ImportMode getImportMode()
    {
        return importMode;
    }

    /**
     * Setter for device import mode.
     * @param importMode The device import mode.
     */
    public void setImportMode(ImportMode importMode)
    {
        this.importMode = importMode;
    }

    /**
     * Getter for device status.
     * @return The device status.
     */
    public DeviceStatus getStatus()
    {
        return status;
    }

    /**
     * Setter for device status.
     * @param status The device status.
     */
    public void setStatus(DeviceStatus status)
    {
        this.status = status;
    }

    /**
     * Getter for device status reason.
     * @return The device status reason.
     */
    public String getStatusReason()
    {
        return statusReason;
    }

    /**
     * Setter for device status reason.
     * @param statusReason The device status reason.
     */
    public void setStatusReason(String statusReason)
    {
        this.statusReason = statusReason;
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

	/**
	 * @return the tags
	 */
	public TwinCollection getTags() {
		return tags;
	}

	/**
	 * @param tags the tags to set
	 */
	public void setTags(TwinCollection tags) {
		this.tags = tags;
	}

	/**
	 * @return the reportedProperties
	 */
	public TwinCollection getReportedProperties() {
		return reportedProperties;
	}

	/**
	 * @param reportedProperties the reportedProperties to set
	 */
	public void setReportedProperties(TwinCollection reportedProperties) {
		this.reportedProperties = reportedProperties;
	}

	/**
	 * @return the desiredProperties
	 */
	public TwinCollection getDesiredProperties() {
		return desiredProperties;
	}

	/**
	 * @param desiredProperties the desiredProperties to set
	 */
	public void setDesiredProperties(TwinCollection desiredProperties) {
		this.desiredProperties = desiredProperties;
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
            else if (!Tools.areEqual(this.getImportMode(), otherExportImportDevice.getImportMode()))
            {
                return false;
            }

            return true;
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
     * Retrieves information from the provided parser and returns it in a new ExportImportDevice instance. All information on this shall be overwritten.
     * @param parser the parser to read from
     * @throws IllegalArgumentException if the provided parser is missing the authentication or id fields. It also shall
     * be thrown if the authentication object in the parser uses SAS authentication and is missing one of the symmetric key fields, or if it uses SelfSigned authentication
     * and is missing one of the thumbprint fields.
     */
    ExportImportDevice(ExportImportDeviceParser parser) throws IllegalArgumentException
    {
        if (parser.getId() == null)
        {
            //Codes_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_053: [If the provided parser does not have values for the properties deviceId or authentication, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("The id property of the parser object may not be null");
        }

        if (parser.getAuthentication() == null)
        {
            //Codes_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_053: [If the provided parser does not have values for the properties deviceId or authentication, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("The authentication property of the parser object may not be null");
        }

        //Codes_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_052: [This constructor shall use the properties of the provided parser object to set the new ExportImportDevice's properties.]
        this.eTag = parser.getETag();
        this.id = parser.getId();
        this.statusReason = parser.getStatusReason();

        if (parser.getImportMode() != null)
        {
            this.importMode = ImportMode.valueOf(parser.getImportMode());
        }

        if (parser.getStatus() != null)
        {
            this.status = DeviceStatus.fromString(parser.getStatus());
        }

        this.authentication = new AuthenticationMechanism(AuthenticationType.valueOf(parser.getAuthentication().getType().toString()));
        if (this.authentication.getAuthenticationType() == AuthenticationType.CERTIFICATE_AUTHORITY)
        {
            //do nothing
        }
        else if (this.authentication.getAuthenticationType() == AuthenticationType.SELF_SIGNED)
        {
            if (parser.getAuthentication().getThumbprint() == null
                    || Tools.isNullOrEmpty(parser.getAuthentication().getThumbprint().getPrimaryThumbprint())
                    || Tools.isNullOrEmpty(parser.getAuthentication().getThumbprint().getSecondaryThumbprint()))
            {
                //Codes_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_059: [If the provided parser uses self signed authentication and is missing one or both thumbprints, two new thumbprints will be generated.]
                this.authentication = new AuthenticationMechanism(this.authentication.getAuthenticationType());
            }
            else
            {
                String primaryThumbprint = parser.getAuthentication().getThumbprint().getPrimaryThumbprint();
                String secondaryThumbprint = parser.getAuthentication().getThumbprint().getSecondaryThumbprint();
                this.authentication = new AuthenticationMechanism(primaryThumbprint, secondaryThumbprint);
            }
        }
        else if (this.authentication.getAuthenticationType() == AuthenticationType.SAS)
        {
            if (parser.getAuthentication().getSymmetricKey() == null
                    || Tools.isNullOrEmpty(parser.getAuthentication().getSymmetricKey().getPrimaryKey())
                    || Tools.isNullOrEmpty(parser.getAuthentication().getSymmetricKey().getSecondaryKey()))
            {
                //Codes_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_058: [If the provided parser uses SAS authentication and is missing one or both symmetric keys, two new keys will be generated.]
                this.authentication = new AuthenticationMechanism(AuthenticationType.SAS);
            }
            else
            {
                String primaryKey = parser.getAuthentication().getSymmetricKey().getPrimaryKey();
                String secondaryKey = parser.getAuthentication().getSymmetricKey().getSecondaryKey();
                this.authentication.getSymmetricKey().setPrimaryKey(primaryKey);
                this.authentication.getSymmetricKey().setSecondaryKey(secondaryKey);
            }
        }
    }

    /**
     * Converts this into a ExportImportDeviceParser object. To serialize a ExportImportDevice object, it must first be converted to a ExportImportDeviceParser object.
     * @return the ExportImportDeviceParser object that can be serialized.
     */
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