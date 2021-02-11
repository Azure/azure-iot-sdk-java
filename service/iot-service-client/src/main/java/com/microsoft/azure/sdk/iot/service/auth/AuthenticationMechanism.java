/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.auth;

import com.microsoft.azure.sdk.iot.service.Tools;

/**
 * Authentication mechanism, used to store the device symmetric key.
 */
public class AuthenticationMechanism
{
    private SymmetricKey symmetricKey;
    private X509Thumbprint thumbprint;
    private AuthenticationType type;

    private static final String ILLEGAL_SYMMETRIC_KEY_STRING = "The provided symmetric key cannot be null";
    private static final String ILLEGAL_AUTHENTICATION_TYPE = "The provided authentication type cannot be null";

    /**
     * Constructor that saves a symmetric key used for SAS authentication
     * @param symmetricKey the key to use for authentication
     * @throws IllegalArgumentException if the provided symmetricKey is null
     */
    public AuthenticationMechanism(SymmetricKey symmetricKey) throws IllegalArgumentException
    {
        //Codes_SRS_AUTHENTICATION_MECHANISM_34_012: [This constructor shall throw an IllegalArgumentException if the provided symmetricKey is null.]
        if (symmetricKey == null)
        {
            throw new IllegalArgumentException(ILLEGAL_SYMMETRIC_KEY_STRING);
        }

        //Codes_SRS_AUTHENTICATION_MECHANISM_34_003: [This constructor shall save the provided symmetricKey to the returned instance.]
        this.symmetricKey = symmetricKey;
        this.type = AuthenticationType.SAS;
    }

    /**
     * Constructor that saves a thumbprint used for self signed authentication
     * @param primaryThumbprint the primary thumbprint to use for authentication
     * @param secondaryThumbprint the secondary thumbprint to use for authentication
     */
    public AuthenticationMechanism(String primaryThumbprint, String secondaryThumbprint)
    {
        //Codes_SRS_AUTHENTICATION_MECHANISM_34_004: [This constructor shall save the provided thumbprint to the returned instance.]
        this.thumbprint = new X509Thumbprint(primaryThumbprint, secondaryThumbprint);
        this.type = AuthenticationType.SELF_SIGNED;
    }

    /**
     * Constructor that is used for certificate authority authentication. Necessary keys will be generated automatically, and can be overwritten later as well.
     * @param authenticationType the type of authentication for this to use.
     */
    public AuthenticationMechanism(AuthenticationType authenticationType)
    {

        this.type = authenticationType;

        //noinspection StatementWithEmptyBody
        if (this.type == AuthenticationType.CERTIFICATE_AUTHORITY)
        {
            //Codes_SRS_AUTHENTICATION_MECHANISM_34_022: [If the provided authentication type is certificate authority signed, no thumbprint or symmetric key will be generated.]
            //do nothing
        }
        else if (this.type == AuthenticationType.SELF_SIGNED)
        {
            //Codes_SRS_AUTHENTICATION_MECHANISM_34_023: [If the provided authentication type is self signed, a thumbprint will be generated, but no symmetric key will be generated.]
            this.thumbprint = new X509Thumbprint();
        }
        else if (this.type == AuthenticationType.SAS)
        {
            //Codes_SRS_AUTHENTICATION_MECHANISM_34_024: [If the provided authentication type is SAS, a symmetric key will be generated, but no thumbprint will be generated.]
            this.symmetricKey = new SymmetricKey();
        }
    }

    /**
     * Getter for symmetric key.
     * @return The symmetric key.
     */
    public SymmetricKey getSymmetricKey()
    {
        //Codes_SRS_AUTHENTICATION_MECHANISM_34_005: [This function shall return this object's symmetric key.]
        return this.symmetricKey;
    }

    /**
     * Returns the primary thumbprint
     * @return the primary thumbprint. It may be {@code null}
     */
    public String getPrimaryThumbprint()
    {
        if (this.thumbprint == null)
        {
            return null;
        }

        //Codes_SRS_AUTHENTICATION_MECHANISM_34_020: [This function shall return the primary thumbprint of this object.]
        return this.thumbprint.getPrimaryThumbprint();
    }

    /**
     * Returns the secondary thumbprint
     * @return the secondary thumbprint. It may be {@code null}
     */
    public String getSecondaryThumbprint()
    {
        if (this.thumbprint == null)
        {
            return null;
        }

        //Codes_SRS_AUTHENTICATION_MECHANISM_34_021: [This function shall return the secondary thumbprint of this object.]
        return this.thumbprint.getSecondaryThumbprint();
    }

    /**
     * Setter for symmetric key.
     * @param symmetricKey the symmetric key to set
     * @throws IllegalArgumentException if the provided symmetricKey is null
     */
    public void setSymmetricKey(SymmetricKey symmetricKey) throws IllegalArgumentException
    {
        //Codes_SRS_AUTHENTICATION_MECHANISM_34_013: [If the provided symmetricKey is null, this function shall throw an IllegalArgumentException.]
        if (symmetricKey == null)
        {
            throw new IllegalArgumentException(ILLEGAL_SYMMETRIC_KEY_STRING);
        }

        //Codes_SRS_AUTHENTICATION_MECHANISM_34_007: [This function shall set this object's symmetric key to the provided value.]
        this.symmetricKey = symmetricKey;

        //Codes_SRS_AUTHENTICATION_MECHANISM_34_019: [This function shall set this object's authentication type to SAS.]
        this.type = AuthenticationType.SAS;
    }

    /**
     * Setter for the primary thumbprint
     *
     * @param primaryThumbprint the value to set
     */
    public void setPrimaryThumbprint(String primaryThumbprint)
    {
        if (this.thumbprint == null)
        {
            this.thumbprint = new X509Thumbprint();
        }

        //Codes_SRS_AUTHENTICATION_MECHANISM_34_015: [This function shall set this object's primary thumbprint to the provided value.]
        this.thumbprint.setPrimaryThumbprint(primaryThumbprint);

        //Codes_SRS_AUTHENTICATION_MECHANISM_34_017: [This function shall set this object's authentication type to SelfSigned.]
        this.type = AuthenticationType.SELF_SIGNED;
    }

    /**
     * Setter for the secondary thumbprint
     * @param secondaryThumbprint the value to set
     */
    public void setSecondaryThumbprint(String secondaryThumbprint)
    {
        if (this.thumbprint == null)
        {
            this.thumbprint = new X509Thumbprint();
        }

        //Codes_SRS_AUTHENTICATION_MECHANISM_34_016: [This function shall set this object's secondary thumbprint to the provided value.]
        this.thumbprint.setSecondaryThumbprint(secondaryThumbprint);

        //Codes_SRS_AUTHENTICATION_MECHANISM_34_018: [This function shall set this object's authentication type to SelfSigned.]
        this.type = AuthenticationType.SELF_SIGNED;
    }

    /**
     * Getter for authentication type.
     * @return The authentication type.
     */
    public AuthenticationType getAuthenticationType()
    {
        //Codes_SRS_AUTHENTICATION_MECHANISM_34_009: [This function shall return the AuthenticationType of this object.]
        return this.type;
    }

    /**
     * Setter for the authentication type of this object
     * @param type the type of authentication to set
     * @throws IllegalArgumentException if the provided type is null
     */
    public void setAuthenticationType(AuthenticationType type) throws IllegalArgumentException
    {
        //Codes_SRS_AUTHENTICATION_MECHANISM_34_014: [If the provided type is null, this function shall throw an IllegalArgumentException.]
        if (type == null)
        {
            throw new IllegalArgumentException(ILLEGAL_AUTHENTICATION_TYPE);
        }

        //Codes_SRS_AUTHENTICATION_MECHANISM_34_011: [This function shall set this object's authentication type to the provided value.]
        this.type = type;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other instanceof AuthenticationMechanism)
        {
            AuthenticationMechanism otherAuthentication = (AuthenticationMechanism) other;
            if (this.type != otherAuthentication.type)
            {
                return false;
            }

            if (this.type == AuthenticationType.CERTIFICATE_AUTHORITY)
            {
                //ignore the thumbprint and symmetric key properties
                return true;
            }
            else if (this.type == AuthenticationType.SAS)
            {
                return Tools.areEqual(this.symmetricKey, otherAuthentication.symmetricKey);
            }
            else if (this.type == AuthenticationType.SELF_SIGNED)
            {
                return Tools.areEqual(this.thumbprint, otherAuthentication.thumbprint);
            }
        }

        return false;
    }
}
