/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.deps.serializer.*;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationMechanism;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.SymmetricKey;

/**
 * The BaseDevice class
 * implementing constructors and serialization functionality.
 */
public class BaseDevice
{
    protected final String UTC_TIME_DEFAULT = "0001-01-01T00:00:00";
    protected final String OFFSET_TIME_DEFAULT = "0001-01-01T00:00:00-00:00";

    /**
     * Create an BaseDevice instance using the given device name
     *
     * @param deviceId Name of the device (used as device id)
     * @param symmetricKey - Device key. If parameter is null, then the key will be auto generated.
     * @throws IllegalArgumentException if deviceId is null
     */
    protected BaseDevice(String deviceId, SymmetricKey symmetricKey)
            throws IllegalArgumentException
    {
        // Codes_SRS_SERVICE_SDK_JAVA_BASEDEVICE_12_004: [The constructor shall throw IllegalArgumentException
        // if the input string is empty or null]
        if (Tools.isNullOrEmpty(deviceId))
        {
            throw new IllegalArgumentException("Device Id cannot be null or empty.");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_BASEDEVICE_15_007: [The constructor shall store
        // the input device status and symmetric key into a member variable]
        // Codes_SRS_SERVICE_SDK_JAVA_BASEDEVICE_12_005: [If the input symmetric key is empty, the constructor shall create
        // a new SymmetricKey instance using AES encryption and store it into a member variable]
        if (symmetricKey == null)
        {
            this.authentication = new AuthenticationMechanism(AuthenticationType.SAS);
        }
        else
        {
            this.authentication = new AuthenticationMechanism(symmetricKey);
        }

        // Codes_SRS_SERVICE_SDK_JAVA_BASEDEVICE_12_006: [The constructor shall initialize all properties to default values]
        this.setPropertiesToDefaultValues();
        this.deviceId = deviceId;
    }

    /**
     * Create an BaseDevice instance using the given device name with the given authenticationType
     *
     * @param deviceId Name of the device (used as device id)
     * @param authenticationType - The type of authentication used by this device.
     */
    protected BaseDevice(String deviceId, AuthenticationType authenticationType)
    {
        if (Tools.isNullOrEmpty(deviceId))
        {
            //Codes_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_009: [The function shall throw IllegalArgumentException if the provided deviceId or authenticationType is empty or null.]
            throw new IllegalArgumentException("The provided device Id must not be null or empty");
        }

        if (authenticationType == null)
        {
            //Codes_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_009: [The function shall throw IllegalArgumentException if the provided deviceId or authenticationType is empty or null.]
            throw new IllegalArgumentException("The provided authentication type must not be null");
        }

        //Codes_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_011: [If the provided authenticationType is certificate authority, no symmetric key shall be generated and no thumbprint shall be generated]
        //Codes_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_012: [If the provided authenticationType is SAS, a symmetric key shall be generated but no thumbprint shall be generated]
        //Codes_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_013: [If the provided authenticationType is self signed, a thumbprint shall be generated but no symmetric key shall be generated]
        this.authentication = new AuthenticationMechanism(authenticationType);
        this.setPropertiesToDefaultValues();
        this.deviceId = deviceId;
    }

    // Codes_SRS_SERVICE_SDK_JAVA_BASEDEVICE_12_001: [The Device class shall have the following properties: deviceId, Etag,
    // SymmetricKey, ConnectionState, ConnectionStateUpdatedTime, LastActivityTime, symmetricKey, thumbprint, authentication]
    /**
     * Device name
     * A case-sensitive string (up to 128 char long)
     * of ASCII 7-bit alphanumeric chars
     * + {'-', ':', '.', '+', '%', '_', '#', '*', '?', '!', '(', ')', ',', '=', '@', ';', '$', '''}.
     */
    protected String deviceId;

    /**
     * Getter for device name
     *
     * @return The deviceId string
     */
    public String getDeviceId()
    {
        return deviceId;
    }

    /**
     * Device generation Id
     */
    protected String generationId;

    /**
     * Getter for GenerationId
     * @return The generationId string
     */
    public String getGenerationId()
    {
        return generationId;
    }

    /**
     * Getter for SymmetricKey object
     * @return The symmetricKey object
     */
    public SymmetricKey getSymmetricKey()
    {
        if (this.authentication == null)
        {
            return null;
        }

        return this.authentication.getSymmetricKey();
    }

    /**
     * Setter for SymmetricKey object
     *
     * @param symmetricKey symmetricKey to be set
     * @throws IllegalArgumentException if the provided symmetric key is null
     */
    public void setSymmetricKey(SymmetricKey symmetricKey) throws  IllegalArgumentException
    {
        if (symmetricKey == null)
        {
            throw new IllegalArgumentException("Symmetric key cannot be null");
        }

        if (this.authentication == null)
        {
            this.authentication = new AuthenticationMechanism(symmetricKey);
        }
        else
        {
            this.authentication.setSymmetricKey(symmetricKey);
        }
    }

    /**
     * Getter for PrimaryKey part of the SymmetricKey
     *
     * @return The primaryKey string
     */
    public String getPrimaryKey()
    {
        if (getSymmetricKey() == null)
        {
            return null;
        }

        return getSymmetricKey().getPrimaryKey();
    }

    /**
     * Getter for SecondaryKey part of the SymmetricKey
     *
     * @return The secondaryKey string
     */
    public String getSecondaryKey()
    {
        if (getSymmetricKey() == null)
        {
            return null;
        }

        return getSymmetricKey().getSecondaryKey();
    }

    /**
     * Setter for X509 thumbprint
     * @param primaryThumbprint the primary thumbprint to set
     * @param secondaryThumbprint the secondary thumbprint to set
     *
     * @deprecated as of service-client version 1.15.1, please use {@link #setThumbprintFinal(String, String)}
     *
     * @throws IllegalArgumentException if primaryThumbprint or secondaryThumbprint is null or empty
     */
    @Deprecated
    public void setThumbprint(String primaryThumbprint, String secondaryThumbprint)
    {
        if (Tools.isNullOrEmpty(primaryThumbprint) || Tools.isNullOrEmpty(secondaryThumbprint))
        {
            throw new IllegalArgumentException("Thumbprint may not be null or empty");
        }

        if (this.authentication == null)
        {
            this.authentication = new AuthenticationMechanism(AuthenticationType.SELF_SIGNED);
        }

        this.authentication.setPrimaryThumbprint(primaryThumbprint);
        this.authentication.setSecondaryThumbprint(secondaryThumbprint);
    }

    /**
     * Setter for X509 thumbprint
     * @param primaryThumbprint the primary thumbprint to set
     * @param secondaryThumbprint the secondary thumbprint to set
     * @throws IllegalArgumentException if primaryThumbprint or secondaryThumbprint is null or empty
     */
    public final void setThumbprintFinal(String primaryThumbprint, String secondaryThumbprint)
    {
        if (Tools.isNullOrEmpty(primaryThumbprint) || Tools.isNullOrEmpty(secondaryThumbprint))
        {
            throw new IllegalArgumentException("Thumbprint may not be null or empty");
        }

        if (this.authentication == null)
        {
            this.authentication = new AuthenticationMechanism(AuthenticationType.SELF_SIGNED);
        }

        this.authentication.setPrimaryThumbprint(primaryThumbprint);
        this.authentication.setSecondaryThumbprint(secondaryThumbprint);
    }

    /**
     * Getter for primary thumbprint part of the whole thumbprint
     *
     * @return The primary thumbprint string
     */
    public String getPrimaryThumbprint()
    {
        if (this.authentication == null)
        {
            return null;
        }

        return authentication.getPrimaryThumbprint();
    }

    /**
     * Getter for secondary thumbprint part of the whole thumbprint
     *
     * @return The secondary thumbprint string
     */
    public String getSecondaryThumbprint()
    {
        if (this.authentication == null)
        {
            return null;
        }

        return authentication.getSecondaryThumbprint();
    }

    /**
     * A string representing a weak ETAG version
     * of this JSON description. This is a hash.
     */
    protected String eTag;

    /**
     * Getter for eTag
     *
     * @return The eTag string
     */
    public String geteTag()
    {
        return eTag;
    }

     /**
     * Status of the device:
     * {"connected" | "disconnected"}
     */
    protected DeviceConnectionState connectionState;

    /**
     * Getter for connection state
     *
     * @return The connectionState string
     */
    public DeviceConnectionState getConnectionState()
    {
        return connectionState;
    }

    /**
     * Datetime of last time the connection state was updated.
     */
    protected String connectionStateUpdatedTime;

    /**
     * Getter for connection state updated time
     *
     * @return The string containing the time when the connectionState parameter was updated
     */
    public String getConnectionStateUpdatedTime()
    {
        return connectionStateUpdatedTime;
    }

    /**
     * Datetime of last time the device authenticated, received, or sent a message.
     */
    protected String lastActivityTime;

    /**
     * Getter for last activity time
     *
     * @return The string containing the time when the lastActivity parameter was updated
     */
    public String getLastActivityTime()
    {
        return lastActivityTime;
    }

    /**
     * Number of messages received by the device
     */
    protected long cloudToDeviceMessageCount;

    /**
     * Getter for cloud to device message count
     *
     * @return The string containing the time when the cloudToDeviceMessageCount parameter was updated
     */
    public long getCloudToDeviceMessageCount()
    {
        return cloudToDeviceMessageCount;
    }

    /**
     * Setter for force update boolean
     * @deprecated  This method currently only validates forceUpdate parameter
     *
     * @param forceUpdate - Boolean controlling if the update should be forced or not
     * @throws IllegalArgumentException if the provided argument is null
     */
    @Deprecated
    public void setForceUpdate(Boolean forceUpdate) throws IllegalArgumentException
    {
        if (forceUpdate == null)
        {
            throw new IllegalArgumentException();
        }
    }

    /*
     * Specifies whether this device uses a key for authentication, an X509 certificate, or something else
     */
    AuthenticationMechanism authentication;

    /**
     * Getter for the authentication type of this device
     * @return the authentication type
     */
    public AuthenticationType getAuthenticationType()
    {
        if (this.authentication == null)
        {
            return null;
        }

        return this.authentication.getAuthenticationType();
    }

    /**
     * Converts this into a DeviceParser object. To serialize a Device object, it must first be converted to a DeviceParser object.
     * @return the DeviceParser object that can be serialized.
     */
    DeviceParser toDeviceParser()
    {
        //Codes_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_018: [This method shall return a new instance of a DeviceParser object that is populated using the properties of this.]
        DeviceParser deviceParser = new DeviceParser();
        deviceParser.setCloudToDeviceMessageCount(this.cloudToDeviceMessageCount);
        deviceParser.setConnectionState(this.connectionState.toString());
        deviceParser.setConnectionStateUpdatedTime(ParserUtility.getDateTimeUtc(this.connectionStateUpdatedTime));
        deviceParser.setDeviceId(this.deviceId);
        deviceParser.seteTag(this.eTag);
        deviceParser.setLastActivityTime(ParserUtility.getDateTimeUtc(this.lastActivityTime));
        deviceParser.setGenerationId(this.generationId);

        deviceParser.setAuthenticationParser(new AuthenticationParser());
        deviceParser.getAuthenticationParser().setType(AuthenticationTypeParser.valueOf(this.authentication.getAuthenticationType().toString()));

        //noinspection StatementWithEmptyBody
        if (this.authentication.getAuthenticationType() == AuthenticationType.CERTIFICATE_AUTHORITY)
        {
            // do nothing
        }
        else if (this.authentication.getAuthenticationType() == AuthenticationType.SELF_SIGNED)
        {
            if (Tools.isNullOrEmpty(this.authentication.getPrimaryThumbprint()) || Tools.isNullOrEmpty(this.authentication.getSecondaryThumbprint()))
            {
                //Codes_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_020: [If this device uses self signed authentication, but does not have a primary and secondary thumbprint saved, an IllegalStateException shall be thrown.]
                throw new IllegalStateException("Device object using self signed authentication needs to have both primary and secondary thumbprints");
            }

            deviceParser.getAuthenticationParser().setThumbprint(new X509ThumbprintParser(this.getPrimaryThumbprint(), this.getSecondaryThumbprint()));
        }
        else if (this.authentication.getAuthenticationType() == AuthenticationType.SAS)
        {
            if (this.authentication.getSymmetricKey() == null
                    || Tools.isNullOrEmpty(this.authentication.getSymmetricKey().getPrimaryKey())
                    || Tools.isNullOrEmpty(this.authentication.getSymmetricKey().getSecondaryKey()))
            {
                //Codes_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_019: [If this device uses sas authentication, but does not have a primary and secondary symmetric key saved, an IllegalStateException shall be thrown.]
                throw new IllegalStateException("Device object using SAS authentication needs to have both primary and secondary keys");
            }

            deviceParser.getAuthenticationParser().setSymmetricKey(new SymmetricKeyParser(this.getPrimaryKey(), this.getSecondaryKey()));
        }

        return  deviceParser;
    }

    /**
     * Retrieves information from the provided parser and saves it to this. All information on this will be overwritten.
     * @param parser the parser to read from
     * @throws IllegalArgumentException if the provided parser is missing the authentication field, or the deviceId field. It also shall
     * be thrown if the authentication object in the parser uses SAS authentication and is missing one of the symmetric key fields,
     * or if it uses SelfSigned authentication and is missing one of the thumbprint fields.
     */
    BaseDevice(DeviceParser parser) throws IllegalArgumentException
    {
        if (parser.getAuthenticationParser() == null || parser.getAuthenticationParser().getType() == null)
        {
            //Codes_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_015: [If the provided parser is missing a value for its authentication or its device Id, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("deviceParser must have an authentication type assigned");
        }

        if (parser.getDeviceId() == null)
        {
            //Codes_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_015: [If the provided parser is missing a value for its authentication or its device Id, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("deviceParser must have a deviceId assigned");
        }

        //Codes_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_014: [This constructor shall create a new Device object using the values within the provided parser.]
        AuthenticationType authenticationType = AuthenticationType.valueOf(parser.getAuthenticationParser().getType().toString());

        this.deviceId = parser.getDeviceId();
        this.authentication = new AuthenticationMechanism(authenticationType);

        this.cloudToDeviceMessageCount = parser.getCloudToDeviceMessageCount();
        this.deviceId = parser.getDeviceId();
        this.eTag = parser.geteTag();
        this.generationId = parser.getGenerationId();

        if (parser.getConnectionStateUpdatedTime() != null)
        {
            this.connectionStateUpdatedTime = ParserUtility.getDateStringFromDate(parser.getConnectionStateUpdatedTime());
        }

        if (parser.getLastActivityTime() != null)
        {
            this.lastActivityTime = ParserUtility.getDateStringFromDate(parser.getLastActivityTime());
        }

        if (parser.getConnectionState() != null)
        {
            this.connectionState = DeviceConnectionState.valueOf(parser.getConnectionState());
        }

        this.authentication = new AuthenticationMechanism(authenticationType);

        //noinspection StatementWithEmptyBody
        if (authenticationType == AuthenticationType.CERTIFICATE_AUTHORITY)
        {
            //do nothing
        }
        else if (authenticationType == AuthenticationType.SELF_SIGNED)
        {
            if (parser.getAuthenticationParser().getThumbprint() != null
                    && parser.getAuthenticationParser().getThumbprint().getPrimaryThumbprintFinal() != null
                    && parser.getAuthenticationParser().getThumbprint().getSecondaryThumbprintFinal() != null)
            {
                this.setThumbprintFinal(parser.getAuthenticationParser().getThumbprint().getPrimaryThumbprintFinal(), parser.getAuthenticationParser().getThumbprint().getSecondaryThumbprintFinal());
            }
            else
            {
                //Codes_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_017: [If the provided parser uses SELF_SIGNED authentication and is missing one or both thumbprint, an IllegalArgumentException shall be thrown.]
                throw new IllegalArgumentException("AuthenticationParser object in the provided DeviceParser object is missing one or more thumbprint values");
            }
        }
        else if (authenticationType == AuthenticationType.SAS)
        {
            if (parser.getAuthenticationParser().getSymmetricKey() != null
                    && parser.getAuthenticationParser().getSymmetricKey().getPrimaryKeyFinal() != null
                    && parser.getAuthenticationParser().getSymmetricKey().getSecondaryKeyFinal() != null)
            {
                this.getSymmetricKey().setPrimaryKeyFinal(parser.getAuthenticationParser().getSymmetricKey().getPrimaryKeyFinal());
                this.getSymmetricKey().setSecondaryKeyFinal(parser.getAuthenticationParser().getSymmetricKey().getSecondaryKeyFinal());
            }
            else
            {
                //Codes_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_016: [If the provided parser uses SAS authentication and is missing one or both symmetric keys, an IllegalArgumentException shall be thrown.]
                throw new IllegalArgumentException("AuthenticationParser object in the provided DeviceParser object is missing one or more symmetric keys");
            }
        }
    }

    /*
     * Set default properties for a base device
     */
    private void setPropertiesToDefaultValues()
    {
        this.generationId = "";
        this.eTag = "";
        this.connectionState = DeviceConnectionState.Disconnected;
        this.connectionStateUpdatedTime = UTC_TIME_DEFAULT;
        this.lastActivityTime = OFFSET_TIME_DEFAULT;
        this.cloudToDeviceMessageCount = 0;
        this.setForceUpdate(false);
    }
}
