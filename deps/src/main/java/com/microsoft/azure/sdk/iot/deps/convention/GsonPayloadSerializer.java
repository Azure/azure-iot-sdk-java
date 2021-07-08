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
    public String SerializeToString(Object objectToSerialize)
    {
        return "";
    }

    /// <inheritdoc/>
    @Override
    public <T> T DeserializeToType(String stringToDeserialize)
    {
        return (T) (new Object());
    }

    /// <inheritdoc/>
    @Override
    public <T> T ConvertFromObject(Object objectToConvert)
    {
        return (T) (new Object());
    }

    /// <inheritdoc/>
    @Override
    public <T> boolean TryGetNestedObjectValue(Object nestedObject, String propertyName, T outValue)
    {
        return true;
    }

    /// <inheritdoc/>
    public IWritablePropertyResponse CreateWritablePropertyResponse(Object value, int statusCode, long version, String description)
    {
        return new GsonWritablePropertyResponse(value, statusCode, version, description);
    }

    /// <inheritdoc/>
    public IWritablePropertyResponse CreateWritablePropertyResponse(Object value, int statusCode, long version)
    {
        return CreateWritablePropertyResponse(value, statusCode, version, null);
    }
}
