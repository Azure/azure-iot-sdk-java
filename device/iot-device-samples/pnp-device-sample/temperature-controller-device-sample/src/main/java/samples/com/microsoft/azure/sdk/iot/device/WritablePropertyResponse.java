// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot.device;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WritablePropertyResponse
{
    /* Empty constructor. */
    public WritablePropertyResponse()
    { }

    /**
     * Convenience constructor for specifying the properties.
     *
     * @param propertyValue The unserialized property value.     *
     * @param ackCode The acknowledgement code, usually an HTTP Status Code e.g. 200, 400.
     * @param ackVersion The acknowledgement version, as supplied in the property update request.
     */
    public WritablePropertyResponse(Object propertyValue, int ackCode, long ackVersion)
    {
        PropertyValue = propertyValue;
        AckCode = ackCode;
        AckVersion = ackVersion;
    }

    /**
     * Convenience constructor for specifying the properties.
     *
     * @param propertyValue The unserialized property value.     *
     * @param ackCode The acknowledgement code, usually an HTTP Status Code e.g. 200, 400.
     * @param ackVersion The acknowledgement version, as supplied in the property update request.
     * @param ackDescription The acknowledgement description, an optional, human-readable message about the result of the property update.
     */
    public WritablePropertyResponse(Object propertyValue, int ackCode, long ackVersion, String ackDescription)
    {
        PropertyValue = propertyValue;
        AckCode = ackCode;
        AckVersion = ackVersion;
        AckDescription = ackDescription;
    }

    /* The unserialized property value. */
    @JsonProperty(value = "value")
    public Object PropertyValue;

    /* The acknowledgement code, usually an HTTP Status Code e.g. 200, 400. */
    @JsonProperty(value = "ac")
    public int AckCode;

    /* The acknowledgement version, as supplied in the property update request. */
    @JsonProperty(value = "av")
    public long AckVersion;

    /* The acknowledgement description, an optional, human-readable message about the result of the property update. */
    @JsonProperty(value = "ad")
    public String AckDescription;
}
