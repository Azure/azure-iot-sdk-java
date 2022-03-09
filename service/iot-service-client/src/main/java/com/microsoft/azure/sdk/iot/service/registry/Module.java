/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.registry;

import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.registry.serializers.RegistryIdentityParser;
import lombok.Getter;

public class Module extends RegistryIdentity
{
    /**
     * Module name
     * A case-sensitive string (up to 128 char long)
     * of ASCII 7-bit alphanumeric chars
     * + {'-', ':', '.', '+', '%', '_', '#', '*', '?', '!', '(', ')', ',', '=', '@', ';', '$', '''}.
     */
    @Getter
    private final String id;

    /**
     * Specifies the module's managed by owner
     */
    @Getter
    private final String managedBy;

    /**
     * Creates a device using the given id. The device will use Symmetric Key for authentication.
     *
     * @param deviceId String containing the device name.
     */
    public Module(String deviceId, String moduleId)
    {
        this(deviceId, moduleId, AuthenticationType.SAS);
    }

    public Module(String deviceId, String moduleId, AuthenticationType authenticationType)
    {
        super(deviceId, authenticationType);

        if (moduleId == null || moduleId.isEmpty())
        {
            throw new IllegalArgumentException("moduleId cannot be null or empty");
        }

        this.id = moduleId;
        this.managedBy = "";
    }

    Module(RegistryIdentityParser parser) throws IllegalArgumentException
    {
        super(parser);

        if (parser.getModuleId() == null)
        {
            throw new IllegalArgumentException("deviceParser must have a moduleId assigned");
        }

        this.id = parser.getModuleId();
        this.managedBy = parser.getManagedBy();
    }

    /**
     * Converts this into a RegistryIdentityParser object. To serialize a Device object, it must first be converted to a RegistryIdentityParser object.
     * @return the RegistryIdentityParser object that can be serialized.
     */
    RegistryIdentityParser toRegistryIdentityParser()
    {
        RegistryIdentityParser registryIdentityParser = super.toRegistryIdentityParser();
        registryIdentityParser.setModuleId(this.id);
        registryIdentityParser.setManagedBy(this.managedBy);

        return registryIdentityParser;
    }
}
