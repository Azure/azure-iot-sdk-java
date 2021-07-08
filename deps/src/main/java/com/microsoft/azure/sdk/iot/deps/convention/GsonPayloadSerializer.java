package com.microsoft.azure.sdk.iot.deps.convention;

import lombok.Getter;

/// <summary>
/// A <see cref="JsonConvert"/> <see cref="PayloadSerializer"/> implementation.
/// </summary>
public class GsonPayloadSerializer extends PayloadSerializer
{
    /// <summary>
    /// The content type string.
    /// </summary>
    private static final String ApplicationJson = "application/json";

    /// <summary>
    /// The default instance of this class.
    /// </summary>
    @Getter
    public static final GsonPayloadSerializer Instance = new GsonPayloadSerializer();

    @Getter
    public String ContentType = ApplicationJson;

    /// <inheritdoc/>
    @Override
    public String serializeToString(Object objectToSerialize)
    {
        return "";
    }

    /// <inheritdoc/>
    @Override
    public <T> T deserializeToType(String stringToDeserialize)
    {
        return (T) (new Object());
    }

    /// <inheritdoc/>
    @Override
    public <T> T convertFromObject(Object objectToConvert)
    {
        return (T) (new Object());
    }

    /// <inheritdoc/>
    @Override
    public <T> T getNestedObjectValue(Object nestedObject, String propertyName)
    {
        return (T) (new Object());
    }

    /// <inheritdoc/>
    public IWritablePropertyResponse createWritablePropertyResponse(Object value, int statusCode, long version, String description)
    {
        return new GsonWritablePropertyResponse(value, statusCode, version, description);
    }

    /// <inheritdoc/>
    public IWritablePropertyResponse createWritablePropertyResponse(Object value, int statusCode, long version)
    {
        return createWritablePropertyResponse(value, statusCode, version, null);
    }
}
