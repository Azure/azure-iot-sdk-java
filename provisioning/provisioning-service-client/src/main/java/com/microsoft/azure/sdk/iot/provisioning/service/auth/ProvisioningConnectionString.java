// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.auth;

/**
 * Expose all connections string properties and methods
 * for user, device and connection string serialization.
 */
public class ProvisioningConnectionString extends ProvisioningConnectionStringBuilder
{
    protected static final String VALUE_PAIR_DELIMITER = ";";
    protected static final String VALUE_PAIR_SEPARATOR = "=";
    protected static final String HOST_NAME_SEPARATOR = ".";

    protected static final String HOST_NAME_PROPERTY_NAME = "HostName";
    protected static final String SHARED_ACCESS_KEY_NAME_PROPERTY_NAME = "SharedAccessKeyName";
    protected static final String SHARED_ACCESS_KEY_PROPERTY_NAME = "SharedAccessKey";
    protected static final String SHARED_ACCESS_SIGNATURE_PROPERTY_NAME = "SharedAccessSignature";

    private static final String USER_SEPARATOR = "@";
    private static final String USER_SAS = "SAS.";
    private static final String USER_ROOT = "root.";

    // Included in the device connection string
    protected String hostName;
    protected String deviceProvisioningServiceName;
    protected AuthenticationMethod authenticationMethod;
    protected String sharedAccessKeyName;
    protected String sharedAccessKey;
    protected String sharedAccessSignature;

    /**
     * Serialize user string
     *
     * @return The user string in the following format: "SharedAccessKeyName@SAS.root.deviceProvisioningServiceName"
     */
    public String getUserString()
    {
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_21_001: [The getUserString shall serialize the object properties to a string using the following format: SharedAccessKeyName@SAS.root.deviceProvisioningServiceName] */
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.sharedAccessKeyName);
        stringBuilder.append(USER_SEPARATOR);
        stringBuilder.append(USER_SAS);
        stringBuilder.append(USER_ROOT);
        stringBuilder.append(this.deviceProvisioningServiceName);
        return stringBuilder.toString();
    }

    /**
     * Serialize connection string
     *
     * @return Provisioning connection string
     */
    @Override
    public String toString()
    {
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_21_002: [The toString shall serialize the object to a string using the following format: HostName=HOSTNAME.b.c.d;SharedAccessKeyName=ACCESSKEYNAME;SharedAccessKey=1234567890abcdefghijklmnopqrstvwxyz=;SharedAccessSignature=] */
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(HOST_NAME_PROPERTY_NAME);
        stringBuilder.append(VALUE_PAIR_SEPARATOR);
        stringBuilder.append(this.hostName);
        stringBuilder.append(VALUE_PAIR_DELIMITER);

        stringBuilder.append(SHARED_ACCESS_KEY_NAME_PROPERTY_NAME);
        stringBuilder.append(VALUE_PAIR_SEPARATOR);
        stringBuilder.append(this.sharedAccessKeyName);
        stringBuilder.append(VALUE_PAIR_DELIMITER);

        stringBuilder.append(SHARED_ACCESS_KEY_PROPERTY_NAME);
        stringBuilder.append(VALUE_PAIR_SEPARATOR);
        stringBuilder.append(this.sharedAccessKey);
        stringBuilder.append(VALUE_PAIR_DELIMITER);

        stringBuilder.append(SHARED_ACCESS_SIGNATURE_PROPERTY_NAME);
        stringBuilder.append(VALUE_PAIR_SEPARATOR);
        stringBuilder.append(this.sharedAccessSignature);

        return stringBuilder.toString();
    }

    /**
     * Getter for deviceProvisioningServiceName
     *
     * @return The Device Provisioning Service name string
     */
    public String getDeviceProvisioningServiceName()
    {
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_21_003: [The getDeviceProvisioningServiceName shall return the stored deviceProvisioningServiceName.] */
        return this.deviceProvisioningServiceName;
    }

    /**
     * Getter for authenticationMethod
     *
     * @return The authenticationMethod object
     */
    public AuthenticationMethod getAuthenticationMethod()
    {
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_21_004: [The getAuthenticationMethod shall return the stored authenticationMethod.] */
        return this.authenticationMethod;
    }

    /**
     * Getter for sharedAccessKeyName
     *
     * @return The sharedAccessKeyName string
     */
    public String getSharedAccessKeyName()
    {
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_21_005: [The getSharedAccessKeyName shall return the stored sharedAccessKeyName.] */
        return this.sharedAccessKeyName;
    }

    /**
     * Getter for sharedAccessKey
     *
     * @return The sharedAccessKey string
     */
    public String getSharedAccessKey()
    {
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_21_006: [The getSharedAccessKey shall return the stored sharedAccessKey.] */
        return this.sharedAccessKey;
    }

    /**
     * Getter for sharedAccessSignature
     *
     * @return The sharedAccessSignature string
     */
    public String getSharedAccessSignature()
    {
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_21_007: [The getSharedAccessSignature shall return the stored sharedAccessSignature.] */
        return this.sharedAccessSignature;
    }

    /**
     * Getter for hostName
     *
     * @return The hostName string
     */
    public String getHostName()
    {
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_21_008: [The getHostName shall return the stored hostName.] */
        return this.hostName;
    }

    /**
     * Setter for sharedAccessKeyName
     *
     * @param sharedAccessKeyName The value of the signature to set
     */
    void setSharedAccessKeyName(String sharedAccessKeyName)
    {
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_21_009: [The setSharedAccessKeyName shall update the sharedAccessKeyName by the provided one.] */
        this.sharedAccessKeyName = sharedAccessKeyName;
    }

    /**
     * Setter for sharedAccessKey
     *
     * @param sharedAccessKey The value of the signature to set
     */
    void setSharedAccessKey(String sharedAccessKey)
    {
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_21_010: [The setSharedAccessKey shall update the sharedAccessKey by the provided one.] */
        this.sharedAccessKey = sharedAccessKey;
    }

    /**
     * Setter for sharedAccessSignature
     *
     * @param sharedAccessSignature The value of the signature to set
     */
    void setSharedAccessSignature(String sharedAccessSignature)
    {
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_21_011: [The setSharedAccessSignature shall update the sharedAccessSignature by the provided one.] */
        this.sharedAccessSignature = sharedAccessSignature;
    }

    /**
     * Package private constructor
     */
    @SuppressWarnings("unused")
    ProvisioningConnectionString()
    {
    }
}
