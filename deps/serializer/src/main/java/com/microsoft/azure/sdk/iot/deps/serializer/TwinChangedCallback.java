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
 * and the transport will call {@link TwinChangedCallback#execute(Map)}
 * upon receiving a property or tags changes from an IoT Hub Device Twin.
 */
public interface TwinChangedCallback
{
    /**
     * Executes the callback.
     *
     * @param changes is a sub-collection of properties or tags that had its values changed.
     */
    void execute(Map<String , Object> changes);
}
