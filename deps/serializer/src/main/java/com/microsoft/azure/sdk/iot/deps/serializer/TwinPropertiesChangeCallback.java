// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import java.util.Map;

/**
 * INNER TWIN CLASS
 *
 * An interface for an IoT Hub Device Twin callback.
 *
 * Developers are expected to create an implementation of this interface,
 * and the transport will call {@link TwinPropertiesChangeCallback}
 * upon receiving a property changes from an IoT Hub Device Twin.
 */
public interface TwinPropertiesChangeCallback
{
    /**
     * Executes the callback.
     *
     * @param propertyMap is a collection of properties that had its values changed.
     */
    void execute(Map<String , Object> propertyMap);
}
