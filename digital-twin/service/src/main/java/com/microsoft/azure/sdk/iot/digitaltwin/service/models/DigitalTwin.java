// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service.models;

import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.models.DigitalTwinInterfaces;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.models.InterfaceModel;
import lombok.Getter;

import java.util.Map;

@Getter
public class DigitalTwin {
    /**
     * Interface(s) data on the digital twin.
     */
    private final Map<String, InterfaceModel> interfaceInstances;

    /**
     * Version of digital twin.
     */
    private final long version;

    public DigitalTwin(DigitalTwinInterfaces digitalTwinInterfaces) {
        this.interfaceInstances = digitalTwinInterfaces.interfaces();
        this.version = digitalTwinInterfaces.version();
    }

}
