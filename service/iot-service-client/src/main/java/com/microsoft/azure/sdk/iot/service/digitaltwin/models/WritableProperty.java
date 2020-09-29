package com.microsoft.azure.sdk.iot.service.digitaltwin.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

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
    @Getter
    @Setter
    public Object DesiredValue;

    /**
     * The version of the property with the specified desired value.
     */
    @JsonProperty("desiredVersion")
    @Getter
    @Setter
    public int DesiredVersion;

    /**
     * The version of the reported property value
     */
    @JsonProperty("ackVersion")
    @Getter
    @Setter
    public int AckVersion;

    /**
     * The response code of the property update request, usually an HTTP Status Code (e.g. 200).
     */
    @JsonProperty("ackCode")
    @Getter
    @Setter
    public int AckCode;

    /**
     * The message response of the property update request.
     */
    @JsonProperty("ackDescription")
    @Getter
    @Setter
    public String AckDescription;

    /**
     * The time when this property was last updated.
     */
    @JsonProperty("ackDescription")
    @Getter
    @Setter
    public OffsetDateTime LastUpdateTime;

}
