// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.models.DigitalTwinInterfaces;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.models.InterfaceModel;

import java.util.HashMap;
import java.util.Map;

public class DigitalTwin
{
    /**
     * Interface(s) data on the digital twin.
     */
    @JsonProperty(value = "interfaces")
    private Map<String, InterfaceModel> interfaceInstances;

    /**
     * Version of digital twin.
     */
    @JsonProperty(value = "version")
    private Long version;

    public DigitalTwin(DigitalTwinInterfaces digitalTwinInterfaces) {
        this.interfaceInstances = new HashMap<String, InterfaceModel>();
        for (String digitalTwinInterfaceComponentName : digitalTwinInterfaces.interfaces().keySet()) {
            this.interfaceInstances.put(digitalTwinInterfaceComponentName, digitalTwinInterfaces.interfaces().get(digitalTwinInterfaceComponentName));
        }
        this.version = digitalTwinInterfaces.version();
    }

    /**
     * Get version of digital twin.
     *
     * @return the version value
     */
    public Long version() {
        return this.version;
    }

    /**
     * Set version of digital twin.
     *
     * @param version the version value to set
     * @return the DigitalTwins object itself.
     */
    public DigitalTwin withVersion(Long version) {
        this.version = version;
        return this;
    }

    /**
     * Get interface(s) data on the digital twin.
     *
     * @return the interfaces value
     */
    public Map<String, InterfaceModel> interfaceInstances() {
        return this.interfaceInstances;
    }

    /**
     * Set interface(s) data on the digital twin.
     *
     * @param interfaceInstances the interfaces value to set
     * @return the DigitalTwinInterfaces object itself.
     */
    public DigitalTwin withInterfaceInstances(Map<String, InterfaceModel> interfaceInstances) {
        this.interfaceInstances = interfaceInstances;
        return this;
    }

}
