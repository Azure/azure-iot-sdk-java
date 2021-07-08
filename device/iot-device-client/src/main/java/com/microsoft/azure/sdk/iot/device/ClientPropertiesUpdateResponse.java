package com.microsoft.azure.sdk.iot.device;

import lombok.Getter;
import lombok.Setter;

/**
 * The response of the {@link InternalClient#updateClientPropertiesAsync(ClientPropertyCollection)} operation.
 */
public class ClientPropertiesUpdateResponse
{
    /**
     * The request Id that is associated with the {@link InternalClient#updateClientPropertiesAsync(ClientPropertyCollection)} operation.
     * <p>
     * This request Id is relevant only for operations over MQTT, and can be used for tracking the operation on service side logs.
     * Note that you would need to contact the support team to track operations on the service side.
     * </p>
     */
    public String RequestId;

    /**
     * The updated version after the property patch has been applied.
     */
    public long Version;
}
