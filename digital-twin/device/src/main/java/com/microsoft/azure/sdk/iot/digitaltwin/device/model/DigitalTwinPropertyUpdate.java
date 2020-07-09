// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.device.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * Structure filled in by the Digital Twin SDK on invoking an interface's property callback routine with information about the request.
 */
@Builder
@Getter
public class DigitalTwinPropertyUpdate {
    /**
     * Name of the property being update
     */
    @NonNull
    private final String propertyName;
    /**
     * Value that the device application had previously reported for this property.
     * This value may be NULL if the application never reported a property.
     * It will also be NULL when an update arrives to the given property <b>after</b> the initial callback.
     */
    private final String propertyReported;
    /**
     * Version (from the service) of this property.
     * This version should be specified when updating this property.
     */
    private final Integer desiredVersion;
    /**
     * Number of bytes in propertyDesired.
     */
    private final String propertyDesired;
}
