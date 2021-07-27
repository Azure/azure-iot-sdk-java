package com.microsoft.azure.sdk.iot.deps.convention;

import com.google.gson.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private Gson gsonSerailizer;

    public GsonPayloadSerializer()
    {
        gsonSerailizer = new GsonBuilder().create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String serializeToString(Object objectToSerialize)
    {
        return gsonSerailizer.toJson(objectToSerialize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T deserializeToType(String stringToDeserialize, Class<T> typeOfT)
    {
        return gsonSerailizer.fromJson(stringToDeserialize, typeOfT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T convertFromObject(Object objectToConvert, Class<T> typeOfT)
    {
        if (typeOfT.isAssignableFrom(JsonElement.class))
        {
            return gsonSerailizer.fromJson((JsonElement) objectToConvert, typeOfT);
        }
        else if (typeOfT.equals(WritablePropertyResponse.class))
        {
            return (T)deserializeToType(gsonSerailizer.toJson(objectToConvert), GsonWritablePropertyResponse.class);
        }
        return deserializeToType(gsonSerailizer.toJson(objectToConvert), typeOfT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T getNestedObjectValue(Object nestedObject, String propertyName, Class<T> typeOfT)
    {

        JsonObject jsonObject = gsonSerailizer.toJsonTree(nestedObject).getAsJsonObject();
        return convertFromObject(jsonObject.get(propertyName), typeOfT);
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
