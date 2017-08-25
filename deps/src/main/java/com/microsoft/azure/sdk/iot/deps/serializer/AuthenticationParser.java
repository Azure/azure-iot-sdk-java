// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AuthenticationParser
{
    private static final String SYMMETRIC_KEY_NAME = "symmetricKey";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(SYMMETRIC_KEY_NAME)
    private SymmetricKeyParser symmetricKey;

    private static final String X509_THUMBPRINT_NAME = "x509Thumbprint";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(X509_THUMBPRINT_NAME)
    private X509ThumbprintParser thumbprint;

    private static final String AUTHENTICATION_TYPE_NAME = "type";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(AUTHENTICATION_TYPE_NAME)
    private AuthenticationTypeParser type;

    public AuthenticationParser()
    {
        //Codes_SRS_AUTHENTICATION_PARSER_34_001: [This Constructor shall create a new instance of an authenticationParser object and return it.]
        //do nothing
    }

    /**
     * Getter for SymmetricKey
     *
     * @return The value of SymmetricKey
     */
    public SymmetricKeyParser getSymmetricKey()
    {
        //Codes_SRS_AUTHENTICATION_PARSER_34_006: [This method shall return the value of this object's symmetricKey.]
        return symmetricKey;
    }

    /**
     * Setter for SymmetricKey
     *
     * @param symmetricKey the value to set SymmetricKey to
     */
    public void setSymmetricKey(SymmetricKeyParser symmetricKey)
    {
        //Codes_SRS_AUTHENTICATION_PARSER_34_007: [This method shall set the value of symmetricKey equal to the provided value.]
        this.symmetricKey = symmetricKey;
    }

    /**
     * Getter for Thumbprint
     *
     * @return The value of Thumbprint
     */
    public X509ThumbprintParser getThumbprint()
    {
        //Codes_SRS_AUTHENTICATION_PARSER_34_004: [This method shall return the value of this object's thumbprint.]
        return thumbprint;
    }

    /**
     * Setter for Thumbprint
     *
     * @param thumbprint the value to set Thumbprint to
     */
    public void setThumbprint(X509ThumbprintParser thumbprint)
    {
        //Codes_SRS_AUTHENTICATION_PARSER_34_005: [This method shall set the value of this object's thumbprint equal to the provided value.]
        this.thumbprint = thumbprint;
    }

    /**
     * Getter for Type
     *
     * @return The value of Type
     */
    public AuthenticationTypeParser getType()
    {
        //Codes_SRS_AUTHENTICATION_PARSER_34_002: [This method shall return the value of this object's authenticationTypeParser.]
        return type;
    }

    /**
     * Setter for Type
     * @param type the value to set Type to
     * @throws IllegalArgumentException if the provided type is null
     */
    public void setType(AuthenticationTypeParser type) throws IllegalArgumentException
    {
        if (type == null)
        {
            //Codes_SRS_AUTHENTICATION_PARSER_34_003: [If the provided type is null, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("Type may not be set to null");
        }

        //Codes_SRS_AUTHENTICATION_PARSER_34_008: [This method shall set the value of this object's authentication type equal to the provided value.]
        this.type = type;
    }
}
