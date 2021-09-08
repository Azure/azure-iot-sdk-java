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
    PayloadSerializer payloadSerializer;

    /**
     * Gets the encoder used for the payload to be serialized.
     */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    PayloadEncoder payloadEncoder;

    /**
     * Returns the byte array for the convention-based message.
     * <p>
     *     This base class will use the {@link PayloadSerializer} and {@link PayloadEncoder} to create this byte array.
     * </p>
     * @param objectToSendWithConvention The convention-based message that is to be sent.
     * @return The correctly encoded byte array for this convention.
     */
    public byte[] getObjectBytes(Object objectToSendWithConvention)
    {
        String serializedString = getPayloadSerializer().serializeToString(objectToSendWithConvention);
        return getPayloadEncoder().encodeStringToByteArray(serializedString);
    }

    /**
     * Uses the encoding and the serializer to return an object.
     * <p>
     *     This base class will use the {@link PayloadSerializer} and {@link PayloadEncoder} to create this byte array.
     * </p>
     * @param bytesToConvertToObject The convention-based message that is to be sent.
     * @param objectType The class of the object you want to deserialize to.
     * @param <T> The type to return
     * @return The correctly decoded object for this convention.
     */
    public <T> T getObjectFromBytes(byte[] bytesToConvertToObject, Class<T> objectType)
    {
        String serializedString = getPayloadEncoder().decodeByteArrayToString(bytesToConvertToObject);
        return getPayloadSerializer().deserializeToType(serializedString, objectType);
    }

    /**
     * Creates the correct {@link WritablePropertyResponse} to be used with this serializer.
     * @param value The value of the property.
     * @param statusCode The status code of the write operation.
     * @param version The version the property is responding to.
     * @param description An optional description of the writable property response.
     * @return The writable property response to be used with this serializer.
     */
    public WritablePropertyResponse createWritablePropertyResponse(Object value, int statusCode, long version, String description)
    {
        return getPayloadSerializer().createWritablePropertyResponse(value, statusCode, version, description);
    }

    /**
     * Creates the correct {@link WritablePropertyResponse} to be used with this serializer.
     * @param value The value of the property.
     * @param statusCode The status code of the write operation.
     * @param version The version the property is responding to.
     * @return The writable property response to be used with this serializer.
     */
    public WritablePropertyResponse createWritablePropertyResponse(Object value, int statusCode, long version)
    {
        return getPayloadSerializer().createWritablePropertyResponse(value, statusCode, version, null);
    }

}