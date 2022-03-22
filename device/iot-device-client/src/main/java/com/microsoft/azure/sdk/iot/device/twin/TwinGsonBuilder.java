package com.microsoft.azure.sdk.iot.device.twin;

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
                    // gson treats all numbers as doubles unless we set this option. As a result, if a user passed in a reported property value as an int, we would serialize/deserialize it as a double anyways.
                    // See this discussion for more details https://stackoverflow.com/questions/45734769/why-does-gson-parse-an-integer-as-a-double
                    .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)

                    // The intendend way to delete a reported property from a twin is to update the property to have a
                    // null value, so we need to allow null valued properties to be serialized.
                    .serializeNulls()

                    .excludeFieldsWithoutExposeAnnotation()
                    .disableHtmlEscaping()
                    .create();
        }

        return gson;
    }
}
