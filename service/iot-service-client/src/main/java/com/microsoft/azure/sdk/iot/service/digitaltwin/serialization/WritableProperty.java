// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.digitaltwin.serialization;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/*
An optional, helper class for deserializing a digital twin. A writable property is one that the service may request a change for from the device.
*/
public final class WritableProperty
{
    /**
     * The desired value of a property.
     */
    @JsonProperty("desiredValue")
    public Object DesiredValue;

    /**
     * The version of the property with the specified desired value.
     */
    @JsonProperty("desiredVersion")
    public int DesiredVersion;

    /**
     * The version of the reported property value
     */
    @JsonProperty("ackVersion")
    public int AckVersion;

    /**
     * The response code of the property update request, usually an HTTP Status Code (e.g. 200).
     */
    @JsonProperty("ackCode")
    public int AckCode;

    /**
     * The message response of the property update request.
     */
    @JsonProperty("ackDescription")
    public String AckDescription;

    /**
     * The time when this property was last updated.
     */
    @JsonProperty("ackDescription")
    public OffsetDateTime LastUpdateTime;

    public Object getDesiredValue() {
        return this.DesiredValue;
    }

    public int getDesiredVersion() {
        return this.DesiredVersion;
    }

    public int getAckVersion() {
        return this.AckVersion;
    }

    public int getAckCode() {
        return this.AckCode;
    }

    public String getAckDescription() {
        return this.AckDescription;
    }

    public OffsetDateTime getLastUpdateTime() {
        return this.LastUpdateTime;
    }

    public void setDesiredValue(Object DesiredValue) {
        this.DesiredValue = DesiredValue;
    }

    public void setDesiredVersion(int DesiredVersion) {
        this.DesiredVersion = DesiredVersion;
    }

    public void setAckVersion(int AckVersion) {
        this.AckVersion = AckVersion;
    }

    public void setAckCode(int AckCode) {
        this.AckCode = AckCode;
    }

    public void setAckDescription(String AckDescription) {
        this.AckDescription = AckDescription;
    }

    public void setLastUpdateTime(OffsetDateTime LastUpdateTime) {
        this.LastUpdateTime = LastUpdateTime;
    }
}
