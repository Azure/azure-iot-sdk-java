package com.microsoft.azure.sdk.iot.deps.convention;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * The payload convention class.
 *
 * <p>
 *     The payload convention is used to define a specific serializer as well as a specific content encoding.
 *     For example, IoT has a <a href="https://docs.microsoft.com/en-us/azure/iot-pnp/concepts-convention">convention</a> that is designed
 *     to make it easier to get started with products that use specific conventions by default.
 * </p>
 */
public abstract class PayloadConvention
{
    /**
     * Gets the serializer used for the payload.
     */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    PayloadSerializer PayloadSerializer;

    /**
     * Gets the encoder used for the payload to be serialized.
     */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    PayloadEncoder PayloadEncoder;

    /**
     * Returns the byte array for the convention-based message.
     * <p>
     *     This base class will use the {@link PayloadSerializer} and {@link PayloadEncoder} to create this byte array.
     * </p>
     * @param objectToSendWithConvention The convention-based message that is to be sent.
     * @return The correctly encoded object for this convention.
     */
    public byte[] GetObjectBytes(Object objectToSendWithConvention)
    {
        String serializedString = PayloadSerializer.serializeToString(objectToSendWithConvention);
        return PayloadEncoder.encodeStringToByteArray(serializedString);
    }
}