// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.io.Serializable;

/**
 * Representation of a single Device Provisioning Service X509 Primary and Secondary CA reference.
 *
 * <p> this class creates a representation of an X509 CA references. It can receive primary and secondary
 *     CA references.
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
    // the primary X509 CA reference
    private static final String PRIMARY_TAG = "primary";
    @Expose
    @SerializedName(PRIMARY_TAG)
    @Getter
    private String primary;

    // the secondary X509 CA reference
    private static final String SECONDARY_TAG = "secondary";
    @Expose
    @SerializedName(SECONDARY_TAG)
    @Getter
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
     */
    X509CAReferences(String primary, String secondary)
    {
        this.primary = primary;
        this.secondary = secondary;
    }

    /**
     * Constructor [COPY]
     *
     * <p> Creates a new instance of the {@code X509CAReferences} copping the content of the provided one.
     *
     * @param x509CAReferences the original {@code X509CAReferences} to copy.
     */
    public X509CAReferences(X509CAReferences x509CAReferences)
    {
        this.primary = x509CAReferences.primary;
        this.secondary = x509CAReferences.secondary;
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
    }
}
