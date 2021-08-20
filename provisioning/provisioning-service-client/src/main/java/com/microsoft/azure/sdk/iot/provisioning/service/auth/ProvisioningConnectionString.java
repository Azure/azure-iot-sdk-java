// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.auth;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Expose all connections string properties and methods
 * for user, device and connection string serialization.
 */
public class ProvisioningConnectionString extends ProvisioningConnectionStringBuilder
{
    static final String VALUE_PAIR_DELIMITER = ";";
    static final String VALUE_PAIR_SEPARATOR = "=";
    static final String HOST_NAME_SEPARATOR = ".";

    static final String HOST_NAME_PROPERTY_NAME = "HostName";
    static final String SHARED_ACCESS_KEY_NAME_PROPERTY_NAME = "SharedAccessKeyName";
    static final String SHARED_ACCESS_KEY_PROPERTY_NAME = "SharedAccessKey";
    static final String SHARED_ACCESS_SIGNATURE_PROPERTY_NAME = "SharedAccessSignature";

    private static final String USER_SEPARATOR = "@";
    private static final String USER_SAS = "SAS.";
    private static final String USER_ROOT = "root.";

    // Included in the device connection string
    @Getter
    @Setter(AccessLevel.MODULE)
    String hostName;

    @Getter
    @Setter(AccessLevel.MODULE)
    String deviceProvisioningServiceName;

    @Getter
    @Setter(AccessLevel.MODULE)
    AuthenticationMethod authenticationMethod;

    @Getter
    @Setter(AccessLevel.MODULE)
    String sharedAccessKeyName;

    @Getter
    @Setter(AccessLevel.MODULE)
    String sharedAccessKey;

    @Getter
    @Setter(AccessLevel.MODULE)
    String sharedAccessSignature;

    /**
     * Serialize user string
     *
     * @return The user string in the following format: "SharedAccessKeyName@SAS.root.deviceProvisioningServiceName"
     */
    public String getUserString()
    {
        /* Codes_SRS_PROVISIONINGCONNECTIONSTRING_21_001: [The getUserString shall serialize the object properties to a string using the following format: SharedAccessKeyName@SAS.root.deviceProvisioningServiceName] */
        return this.sharedAccessKeyName +
                USER_SEPARATOR +
                USER_SAS +
                USER_ROOT +
                this.deviceProvisioningServiceName;
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

        return HOST_NAME_PROPERTY_NAME +
                VALUE_PAIR_SEPARATOR +
                this.hostName +
                VALUE_PAIR_DELIMITER +
                SHARED_ACCESS_KEY_NAME_PROPERTY_NAME +
                VALUE_PAIR_SEPARATOR +
                this.sharedAccessKeyName +
                VALUE_PAIR_DELIMITER +
                SHARED_ACCESS_KEY_PROPERTY_NAME +
                VALUE_PAIR_SEPARATOR +
                this.sharedAccessKey +
                VALUE_PAIR_DELIMITER +
                SHARED_ACCESS_SIGNATURE_PROPERTY_NAME +
                VALUE_PAIR_SEPARATOR +
                this.sharedAccessSignature;
    }

    /**
     * Package private constructor
     */
    @SuppressWarnings("unused")
    ProvisioningConnectionString()
    {
    }
}
