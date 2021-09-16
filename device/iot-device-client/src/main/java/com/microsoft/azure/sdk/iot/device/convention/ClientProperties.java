// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.convention;

import com.microsoft.azure.sdk.iot.deps.convention.ClientPropertyCollection;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Container for the properties that are writable and reported from the client
 */
@AllArgsConstructor
public class ClientProperties
{
    @Getter
    @Setter
    private ClientPropertyCollection writableProperties;

    @Getter
    @Setter
    private ClientPropertyCollection reportedFromClient;

    /**
     * Gets the entries of the ClientPropertyCollection as a set of Properties to be sent to the IoT hub service.
     *
     * @param clientPropertyCollection The client properties collection to convert
     * @return A set of properties
     */
     public static Set<Property> getCollectionAsSetOfProperty(ClientPropertyCollection clientPropertyCollection)
    {
        HashSet<Property> toReturn = new HashSet<>();
        for (Map.Entry<String, Object> entry : clientPropertyCollection.entrySet())
        {
            toReturn.add(new Property(entry.getKey(), entry.getValue()));
        }
        return toReturn;
    }
}
