/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.auth;

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
     */
    public AuthenticationMechanism(SymmetricKey symmetricKey)
    {
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
            //do nothing
        }
        else if (this.type == AuthenticationType.SELF_SIGNED)
        {
            this.thumbprint = new X509Thumbprint();
        }
        else if (this.type == AuthenticationType.SAS)
        {
            this.symmetricKey = new SymmetricKey();
        }
    }

    /**
     * Getter for symmetric key.
     * @return The symmetric key.
     */
    public SymmetricKey getSymmetricKey()
    {
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

        return this.thumbprint.getSecondaryThumbprint();
    }

    /**
     * Setter for symmetric key.
     * @param symmetricKey the symmetric key to set
     */
    public void setSymmetricKey(SymmetricKey symmetricKey)
    {
        this.symmetricKey = symmetricKey;
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

        this.thumbprint.setPrimaryThumbprint(primaryThumbprint);
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

        this.thumbprint.setSecondaryThumbprint(secondaryThumbprint);
        this.type = AuthenticationType.SELF_SIGNED;
    }

    /**
     * Getter for authentication type.
     * @return The authentication type.
     */
    public AuthenticationType getAuthenticationType()
    {
        return this.type;
    }

    /**
     * Setter for the authentication type of this object
     * @param type the type of authentication to set
     */
    public void setAuthenticationType(AuthenticationType type)
    {
        this.type = type;
    }
}
