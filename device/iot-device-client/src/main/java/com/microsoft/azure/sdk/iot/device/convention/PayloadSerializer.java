package com.microsoft.azure.sdk.iot.device.convention;

import lombok.Getter;

/**
 * Provides the serialization for a specified convention.
 * <p>
 *     The serializer is responsible for converting all of your objects into the correct format for the {@link PayloadConvention} that uses it.
 * </p>
 * <p>
 *     By default we have implemented the {@link GsonPayloadSerializer} class that uses {@link com.google.gson.Gson} to handle the serialization for the {@link DefaultPayloadConvention} class.
 * </p>
 */
public abstract class PayloadSerializer
{
    /**
     * Used to specify what type of content to expect.
     * <p>
     *  This can be free-form but should adhere to standard MIME types. For example, "application/json" is what we implement by default.
     * </p>
     * @return A string representing the content type to use when sending a payload.
     */
    @Getter
    String contentType;

    /**
     * Serialize the specified object to a string.
     * @param objectToSerialize Object to serialize.
     * @return A serialized string of the object.
     */
    public abstract String serializeToString(Object objectToSerialize);

    /**
     * Convert the serialized string to an object.
     * @param stringToDeserialize String to deserialize.
     * @param <T> The type you want to return.
     * @param typeOfT The class to attempt the conversion with. Used to safely convert numbers and complex objects.
     * @return A fully deserialized type.
     */
    public abstract <T> T deserializeToType(String stringToDeserialize, Class<T> typeOfT);

    /**
     * Converts the object using the serializer.
     *
     * <p>
     *     This class is used by the PayloadCollection-based classes to attempt to convert from the native serializer type to the desired type.
     *     When you implement this you need to be aware of what type your serializer will use for anonymous types.
     * </p>
     * @param objectToConvert The object to convert.
     * @param typeOfT The class to attempt the conversion with. Used to safely convert numbers and complex objects.
     * @param <T> The type to convert to.
     * @return A converted object.
     */
    public abstract <T> T convertFromObject(Object objectToConvert, Class<T> typeOfT);

    /**
     * Gets a nested property from the serialized data.
     * <p>
     *     This is used internally by our PayloadCollection-based classes to attempt to get a property of the underlying object.
     *     An example of this would be a property under the component.
     * </p>
     * @param nestedObject The object that might contain the nested property.
     * @param propertyName The name of the property to be retrieved.
     * @param typeOfT The class to attempt the conversion with. Used to safely convert numbers and complex objects.
     * @param <T> The type to convert the retrieved property to.
     * @return A converted object.
     */
    public abstract <T> T getNestedObjectValue(Object nestedObject, String propertyName, Class<T> typeOfT);

        /**
     * Creates the correct {@link WritablePropertyResponse} to be used with this serializer.
     * @param value The value of the property.
     * @param statusCode The status code of the write operation.
     * @param version The version the property is responding to.
     * @return The writable property response to be used with this serializer.
     */
    public abstract WritablePropertyResponse createWritablePropertyResponse(Object value, int statusCode, long version);

    /**
     * Creates the correct {@link WritablePropertyResponse} to be used with this serializer.
     * @param value The value of the property.
     * @param statusCode The status code of the write operation.
     * @param version The version the property is responding to.
     * @param description An optional description of the writable property response.
     * @return The writable property response to be used with this serializer.
     */
    public abstract WritablePropertyResponse createWritablePropertyResponse(Object value, int statusCode, long version, String description);
}