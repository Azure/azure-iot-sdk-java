package com.microsoft.azure.sdk.iot.service.twin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;

/**
 * Helper class for building Gson objects to be used when serializing/deserializing twin objects.
 */
class TwinGsonBuilder
{
    static Gson gson;

    /**
     * Get the singleton gson instance.
     * @return the singleton gson instance.
     */
    static Gson getGson()
    {
        if (gson == null)
        {
            gson = new GsonBuilder()
                    .excludeFieldsWithoutExposeAnnotation()
                    .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                    .disableHtmlEscaping()
                    .serializeNulls()
                    .create();
        }

        return gson;
    }
}
