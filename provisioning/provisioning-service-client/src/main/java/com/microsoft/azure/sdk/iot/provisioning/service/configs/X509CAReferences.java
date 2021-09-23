// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.provisioning.service.Tools;

import java.io.Serializable;

/**
 * Representation of a single Device Provisioning Service X509 Primary and Secondary CA reference.
 *
 * <p> this class creates a representation of an X509 CA references. It can receive primary and secondary
 *     CA references, but only the primary is mandatory.
 *
 * <p> Users must provide the CA reference as a {@code String}. This class will encapsulate both in a
 *     single {@link X509Attestation}.
 *
 * <p> The following JSON is an example of the result of this class.
 * <pre>
 * {@code
 *  {
 *      "primary": "ValidCAReference-1",
 *      "secondary": "validCAReference-2"
 *  }
 * }
 * </pre>
 *
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
 */
public class X509CAReferences implements Serializable
{
    // the primary X509 CA reference [mandatory]
    private static final String PRIMARY_TAG = "primary";
    @Expose
    @SerializedName(PRIMARY_TAG)
    private String primary;

    // the secondary X509 CA reference
    private static final String SECONDARY_TAG = "secondary";
    @Expose
    @SerializedName(SECONDARY_TAG)
    private String secondary;

    /**
     * CONSTRUCTOR
     *
     * <p> Creates a new instance of the X509 CA references using the provided CA references.
     *
     * <P> The CA reference is a {@code String} with the name that you gave for your certificate.
     *
     * @param primary the {@code String} with the primary CA reference.
     * @param secondary the {@code String} with the secondary CA reference.
     * @throws IllegalArgumentException if the primary CA reference is {@code null} or empty.
     */
    X509CAReferences(String primary, String secondary)
    {
        /* SRS_X509_CAREFERENCE_21_002: [The constructor shall store the primary and secondary CA references.] */
        this.primary = primary;
        this.secondary = secondary;
    }

    /**
     * Constructor [COPY]
     *
     * <p> Creates a new instance of the {@code X509CAReferences} copping the content of the provided one.
     *
     * @param x509CAReferences the original {@code X509CAReferences} to copy.
     * @throws IllegalArgumentException if the provided X509CAReferences is null or if its primary CA reference is null.
     */
    public X509CAReferences(X509CAReferences x509CAReferences)
    {
        /* SRS_X509_CAREFERENCE_21_004: [The constructor shall create a copy of the primary and secondary CA references and store it.] */
        this.primary = x509CAReferences.primary;
        this.secondary = x509CAReferences.secondary;
    }

    /**
     * Getter for the primary.
     *
     * @deprecated as of provisioning-service-client version 1.3.3, please use {@link #getPrimaryFinal()}
     *
     * @return the {@code String} with the stored primary. It cannot be {@code null}.
     */
    @Deprecated
    public String getPrimary()
    {
        /* SRS_X509_CAREFERENCE_21_005: [The getPrimary shall return the stored primary.] */
        return this.primary;
    }

    /**
     * Getter for the primary.
     *
     * @return the {@code String} with the stored primary. It cannot be {@code null}.
     */
    public final String getPrimaryFinal()
    {
        /* SRS_X509_CAREFERENCE_21_005: [The getPrimary shall return the stored primary.] */
        return this.primary;
    }

    /**
     * Getter for the secondary.
     * @deprecated as of provisioning-service-client version 1.3.3, please use {@link #getSecondaryFinal()}
     *
     * @return the {@code String} with the stored secondary. It can be {@code null}.
     */
    @Deprecated
    public String getSecondary()
    {
        /* SRS_X509_CAREFERENCE_21_006: [The getSecondary shall return the stored secondary.] */
        return this.secondary;
    }

    /**
     * Getter for the secondary.
     *
     * @return the {@code String} with the stored secondary. It can be {@code null}.
     */
    public final String getSecondaryFinal()
    {
        /* SRS_X509_CAREFERENCE_21_006: [The getSecondary shall return the stored secondary.] */
        return this.secondary;
    }

    /**
     * Empty constructor
     *
     * <p>
     *     Used only by the tools that will deserialize this class.
     * </p>
     */
    @SuppressWarnings("unused")
    X509CAReferences()
    {
        /* SRS_X509_CAREFERENCE_21_007: [The X509CAReferences shall provide an empty constructor to make GSON happy.] */
    }
}
