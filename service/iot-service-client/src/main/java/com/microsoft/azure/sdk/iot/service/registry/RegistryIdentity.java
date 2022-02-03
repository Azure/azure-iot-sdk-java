/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.registry;

import com.microsoft.azure.sdk.iot.service.auth.AuthenticationMechanism;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.SymmetricKey;
import com.microsoft.azure.sdk.iot.service.ParserUtility;
import com.microsoft.azure.sdk.iot.service.registry.serializers.AuthenticationParser;
import com.microsoft.azure.sdk.iot.service.registry.serializers.AuthenticationTypeParser;
import com.microsoft.azure.sdk.iot.service.registry.serializers.RegistryIdentityParser;
import com.microsoft.azure.sdk.iot.service.registry.serializers.SymmetricKeyParser;
import com.microsoft.azure.sdk.iot.service.registry.serializers.X509ThumbprintParser;
import lombok.Getter;

/**
 * The RegistryIdentity class
 * implementing constructors and serialization functionality.
 */
public class RegistryIdentity
{
    final String UTC_TIME_DEFAULT = "0001-01-01T00:00:00";
    private final static String OFFSET_TIME_DEFAULT = "0001-01-01T00:00:00-00:00";

    /**
     * Device name
     * A case-sensitive string (up to 128 char long)
     * of ASCII 7-bit alphanumeric chars
     * + {'-', ':', '.', '+', '%', '_', '#', '*', '?', '!', '(', ')', ',', '=', '@', ';', '$', '''}.
     */
    @Getter
    private String deviceId;

    @Getter
    private String generationId;

    /**
     * Status of the device:
     * {"connected" | "disconnected"}
     */
    @Getter
    private DeviceConnectionState connectionState;

    /**
     * Datetime of last time the connection state was updated.
     */
    @Getter
    private String connectionStateUpdatedTime;

    /**
     * Datetime of last time the device authenticated, received, or sent a message.
     */
    @Getter
    private String lastActivityTime;

    /**
     * Number of messages received by the device
     */
    @Getter
    private long cloudToDeviceMessageCount;

    /**
     * Create an RegistryIdentity instance using the given device name
     *
     * @param deviceId Name of the device (used as device id)
     * @throws IllegalArgumentException if deviceId is null
     */
    RegistryIdentity(String deviceId, AuthenticationType authenticationType)
    {
        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("Device Id cannot be null or empty.");
        }

        if (authenticationType == null)
        {
            throw new IllegalArgumentException("authenticationType cannot be null or empty");
        }

        this.setPropertiesToDefaultValues();
        this.deviceId = deviceId;
        this.authentication = new AuthenticationMechanism(authenticationType);
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
     * @throws IllegalArgumentException if primaryThumbprint or secondaryThumbprint is null or empty
     */
    public final void setThumbprint(String primaryThumbprint, String secondaryThumbprint)
    {
        if (primaryThumbprint == null || primaryThumbprint.isEmpty() || secondaryThumbprint == null || secondaryThumbprint.isEmpty())
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
    @Getter
    private String eTag;

    /*
     * Specifies whether this device uses a key for authentication, an X509 certificate, or something else
     */
    private AuthenticationMechanism authentication;

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
     * Converts this into a RegistryIdentityParser object. To serialize a Device object, it must first be converted to a RegistryIdentityParser object.
     * @return the RegistryIdentityParser object that can be serialized.
     */
    RegistryIdentityParser toRegistryIdentityParser()
    {
        RegistryIdentityParser registryIdentityParser = new RegistryIdentityParser();
        registryIdentityParser.setCloudToDeviceMessageCount(this.cloudToDeviceMessageCount);
        registryIdentityParser.setConnectionState(this.connectionState.toString());
        registryIdentityParser.setConnectionStateUpdatedTime(ParserUtility.getDateTimeUtc(this.connectionStateUpdatedTime));
        registryIdentityParser.setDeviceId(this.deviceId);
        registryIdentityParser.setETag(this.eTag);
        registryIdentityParser.setLastActivityTime(ParserUtility.getDateTimeUtc(this.lastActivityTime));
        registryIdentityParser.setGenerationId(this.generationId);

        registryIdentityParser.setAuthenticationParser(new AuthenticationParser());
        registryIdentityParser.getAuthenticationParser().setType(AuthenticationTypeParser.valueOf(this.authentication.getAuthenticationType().toString()));

        //noinspection StatementWithEmptyBody
        if (this.authentication.getAuthenticationType() == AuthenticationType.CERTIFICATE_AUTHORITY)
        {
            // do nothing
        }
        else if (this.authentication.getAuthenticationType() == AuthenticationType.SELF_SIGNED)
        {
            registryIdentityParser.getAuthenticationParser().setThumbprint(new X509ThumbprintParser(this.getPrimaryThumbprint(), this.getSecondaryThumbprint()));
        }
        else if (this.authentication.getAuthenticationType() == AuthenticationType.SAS)
        {
            registryIdentityParser.getAuthenticationParser().setSymmetricKey(new SymmetricKeyParser(this.getPrimaryKey(), this.getSecondaryKey()));
        }

        return registryIdentityParser;
    }

    /**
     * Retrieves information from the provided parser and saves it to this. All information on this will be overwritten.
     * @param parser the parser to read from
     * @throws IllegalArgumentException if the provided parser is missing the authentication field, or the deviceId field. It also shall
     * be thrown if the authentication object in the parser uses SAS authentication and is missing one of the symmetric key fields,
     * or if it uses SelfSigned authentication and is missing one of the thumbprint fields.
     */
    RegistryIdentity(RegistryIdentityParser parser) throws IllegalArgumentException
    {
        if (parser.getAuthenticationParser() == null || parser.getAuthenticationParser().getType() == null)
        {
            throw new IllegalArgumentException("deviceParser must have an authentication type assigned");
        }

        if (parser.getDeviceId() == null)
        {
            throw new IllegalArgumentException("deviceParser must have a deviceId assigned");
        }

        AuthenticationType authenticationType = AuthenticationType.valueOf(parser.getAuthenticationParser().getType().toString());

        this.deviceId = parser.getDeviceId();
        this.authentication = new AuthenticationMechanism(authenticationType);

        this.cloudToDeviceMessageCount = parser.getCloudToDeviceMessageCount();
        this.deviceId = parser.getDeviceId();
        this.eTag = parser.getETag();
        this.generationId = parser.getGenerationId();

        if (parser.getConnectionStateUpdatedTime() != null)
        {
            this.connectionStateUpdatedTime = ParserUtility.getUTCDateStringFromDate(parser.getConnectionStateUpdatedTime());
        }

        if (parser.getLastActivityTime() != null)
        {
            this.lastActivityTime = ParserUtility.getUTCDateStringFromDate(parser.getLastActivityTime());
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
                    && parser.getAuthenticationParser().getThumbprint().getPrimaryThumbprint() != null
                    && parser.getAuthenticationParser().getThumbprint().getSecondaryThumbprint() != null)
            {
                this.setThumbprint(parser.getAuthenticationParser().getThumbprint().getPrimaryThumbprint(), parser.getAuthenticationParser().getThumbprint().getSecondaryThumbprint());
            }
            else
            {
                throw new IllegalArgumentException("AuthenticationParser object in the provided RegistryIdentityParser object is missing one or more thumbprint values");
            }
        }
        else if (authenticationType == AuthenticationType.SAS)
        {
            if (parser.getAuthenticationParser().getSymmetricKey() != null
                    && parser.getAuthenticationParser().getSymmetricKey().getPrimaryKey() != null
                    && parser.getAuthenticationParser().getSymmetricKey().getSecondaryKey() != null)
            {
                this.getSymmetricKey().setPrimaryKey(parser.getAuthenticationParser().getSymmetricKey().getPrimaryKey());
                this.getSymmetricKey().setSecondaryKey(parser.getAuthenticationParser().getSymmetricKey().getSecondaryKey());
            }
            else
            {
                throw new IllegalArgumentException("AuthenticationParser object in the provided RegistryIdentityParser object is missing one or more symmetric keys");
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
    }
}
