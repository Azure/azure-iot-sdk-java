package com.microsoft.azure.sdk.iot.deps.convention;

import lombok.Getter;
import lombok.Setter;

/// <summary>
/// The payload convention class.
/// </summary>
/// <remarks>The payload convention is used to define a specific serializer as well as a specific content encoding.
/// For example, IoT has a <see href="https://docs.microsoft.com/en-us/azure/iot-pnp/concepts-convention">convention</see> that is designed
/// to make it easier to get started with products that use specific conventions by default.</remarks>
public abstract class PayloadConvention
{
    /// <summary>
    /// Gets the serializer used for the payload.
    /// </summary>
    /// <value>A serializer that will be used to convert the payload object to a string.</value>
    @Getter
    @Setter
    public PayloadSerializer PayloadSerializer;

    /// <summary>
    /// Gets the encoder used for the payload to be serialized.
    /// </summary>
    /// <value>An encoder that will be used to convert the serialized string to a byte array.</value>
    @Getter
    public PayloadEncoder PayloadEncoder;

    /// <summary>
    /// Returns the byte array for the convention-based message.
    /// </summary>
    /// <remarks>This base class will use the <see cref="PayloadSerializer"/> and <see cref="PayloadEncoder"/> to create this byte array.</remarks>
    /// <param name="objectToSendWithConvention">The convention-based message that is to be sent.</param>
    /// <returns>The correctly encoded object for this convention.</returns>
    public byte[] GetObjectBytes(Object objectToSendWithConvention)
    {
        String serializedString = PayloadSerializer.serializeToString(objectToSendWithConvention);
        return PayloadEncoder.EncodeStringToByteArray(serializedString);
    }
}