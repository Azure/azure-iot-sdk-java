package com.microsoft.azure.sdk.iot.deps.convention;

import lombok.Getter;

/**
 * A {@link com.google.gson.Gson} {@link PayloadSerializer} implementation.
 */
public class GsonPayloadSerializer extends PayloadSerializer
{
    /**
     * The content type string.
     */
    private static final String ApplicationJson = "application/json";

    /**
     * The default instance of this class.
     */
    @Getter
    static final GsonPayloadSerializer Instance = new GsonPayloadSerializer();

    @Getter
    String ContentType = ApplicationJson;

    /**
     * {@inheritDoc}
     */
    @Override
    public String serializeToString(Object objectToSerialize)
    {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T deserializeToType(String stringToDeserialize)
    {
        return (T) (new Object());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T convertFromObject(Object objectToConvert)
    {
        return (T) (new Object());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T getNestedObjectValue(Object nestedObject, String propertyName)
    {
        return (T) (new Object());
    }

    /**
     * {@inheritDoc}
     */
    public WritablePropertyResponse createWritablePropertyResponse(Object value, int statusCode, long version, String description)
    {
        return new GsonWritablePropertyResponse(value, statusCode, version, description);
    }

    /**
     * {@inheritDoc}
     */
    public WritablePropertyResponse createWritablePropertyResponse(Object value, int statusCode, long version)
    {
        return createWritablePropertyResponse(value, statusCode, version, null);
    }
}
