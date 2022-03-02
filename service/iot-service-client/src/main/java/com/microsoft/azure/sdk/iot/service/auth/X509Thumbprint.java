// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.auth;

import com.microsoft.azure.sdk.iot.service.Tools;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.security.SecureRandom;

public class X509Thumbprint
{
    private String primaryThumbprint;
    private String secondaryThumbprint;

    //Thumbprint format used by devices created manually
    private static final String THUMBPRINT_REGEX = "^([A-Fa-f0-9]{2}){20}$";

    //Thumbprint format used by devices that are provisioned by DPS
    private static final String THUMBPRINT_REGEX_DPS = "^([A-Fa-f0-9]{2}){32}$";

    //Thumbprints are made up of 40 hex characters
    private static final int THUMBPRINT_DIGIT_MAX = 16;
    private static final int THUMBPRINT_LENGTH = 40;

    /**
     * Constructor for an X509 Thumbprint that randomly generates the primary and secondary thumbprints
     */
    X509Thumbprint()
    {
        //Codes_SRS_X509THUMBPRINT_34_011: [This constructor shall generate a random primary and secondary thumbprint.]
        this.primaryThumbprint = generateValidThumbprint();
        this.secondaryThumbprint = generateValidThumbprint();
    }

    /**
     * Constructor for an X509 Thumbprint with the provided primary and secondary thumbprints
     * @param primaryThumbprint the primary thumbprint
     * @param secondaryThumbprint the secondary thumbprint
     * @throws IllegalArgumentException if the provided thumbprint is an invalid format
     */
    X509Thumbprint(String primaryThumbprint, String secondaryThumbprint)
    {
        //Codes_SRS_X509THUMBPRINT_34_010: [This constructor shall throw an IllegalArgumentException if the provided thumbprints are null, empty, or not a valid format.]
        validateThumbprint(primaryThumbprint);
        validateThumbprint(secondaryThumbprint);

        //Codes_SRS_X509THUMBPRINT_34_006: [This constructor shall create an X509Thumbprint with the provided primary thumbprint and the provided secondary thumbprint.]
        this.primaryThumbprint = primaryThumbprint;
        this.secondaryThumbprint = secondaryThumbprint;
    }

    /**
     * Getter for the primary thumbprint
     * @return the primary thumbprint
     */
    String getPrimaryThumbprint()
    {
        //Codes_SRS_X509THUMBPRINT_34_001: [The function shall return the primary thumbprint value of this.]
        return this.primaryThumbprint;
    }

    /**
     * Getter for the secondary thumbprint
     * @return the secondary thumbprint
     */
    String getSecondaryThumbprint()
    {
        //Codes_SRS_X509THUMBPRINT_34_002: [The function shall return the secondary thumbprint value of this.]
        return this.secondaryThumbprint;
    }

    /**
     * Setter for primary thumbprint
     * @param primaryThumbprint the thumbprint value to set
     * @throws IllegalArgumentException if the provided thumbprint is an invalid format
     */
    void setPrimaryThumbprint(String primaryThumbprint) throws IllegalArgumentException
    {
        //Codes_SRS_X509THUMBPRINT_34_007: [If the provided thumbprint string is null, empty, or not the proper format, an IllegalArgumentException shall be thrown.]
        validateThumbprint(primaryThumbprint);

        //Codes_SRS_X509THUMBPRINT_34_003: [The function shall set the primary thumbprint to the given value.]
        this.primaryThumbprint = primaryThumbprint;
    }

    /**
     * Setter for secondary thumbprint
     * @param secondaryThumbprint the thumbprint value to set
     * @throws IllegalArgumentException if the provided thumbprint is an invalid format
     */
    void setSecondaryThumbprint(String secondaryThumbprint) throws IllegalArgumentException
    {
        //Codes_SRS_X509THUMBPRINT_34_008: [If the provided thumbprint string is not the proper format, an IllegalArgumentException shall be thrown.]
        validateThumbprint(secondaryThumbprint);

        //Codes_SRS_X509THUMBPRINT_34_004: [The function shall set the secondary thumbprint to the given value.]
        this.secondaryThumbprint = secondaryThumbprint;
    }


    @SuppressFBWarnings("HE_EQUALS_USE_HASHCODE") // Can't integrate hashcode into this function without breaking changes
    @Override
    public boolean equals(Object other)
    {
        if (other instanceof X509Thumbprint)
        {
            X509Thumbprint otherThumbprint = (X509Thumbprint) other;

            return (Tools.areEqual(this.getPrimaryThumbprint(), otherThumbprint.getPrimaryThumbprint())
                && Tools.areEqual(this.getSecondaryThumbprint(), otherThumbprint.getSecondaryThumbprint()));
        }

        return false;
    }

    /**
     * Validate the thumbprint
     * @param thumbprint The thumbprint to validate
     * @throws IllegalArgumentException if the provided thumbprint is the incorrect format
     */
    private void validateThumbprint(String thumbprint) throws IllegalArgumentException
    {
        if (thumbprint == null)
        {
            throw new IllegalArgumentException("Invalid format for primary/secondary thumbprint: thumbprint may not be null");
        }

        if (thumbprint.isEmpty())
        {
            throw new IllegalArgumentException("Invalid format for primary/secondary thumbprint: thumbprint may not be empty");
        }

        if (!thumbprint.matches(THUMBPRINT_REGEX) && !thumbprint.matches(THUMBPRINT_REGEX_DPS))
        {
            throw new IllegalArgumentException("Invalid format for primary/secondary thumbprint");
        }
    }

    /**
     * Creates a valid, random thumbprint
     * @return the generated thumbprint
     */
    private String generateValidThumbprint()
    {
        StringBuilder thumbprint = new StringBuilder();
        SecureRandom rand = new SecureRandom(); //SecureRandom chooses its own seed, better than providing timestamp

        for (int i = 0; i < THUMBPRINT_LENGTH; i++)
        {
            thumbprint.append(Integer.toHexString(rand.nextInt(THUMBPRINT_DIGIT_MAX)));
        }

        return thumbprint.toString();
    }
}