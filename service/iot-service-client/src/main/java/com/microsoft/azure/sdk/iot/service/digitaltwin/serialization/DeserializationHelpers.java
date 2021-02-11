// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.digitaltwin.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DeserializationHelpers {

    /**
     * Converts the payload object into a generic type.
     * There are two different paths we will have to take based on the type T
     * In case of a String, we need to write the value of the payload as a String
     * In case of any other type that the user decides to deserialize the payload, we will use mapper.convertValue to perform the conversion.
     * Updates a digital twin.
     * @param mapper Object Mapper
     * @param payload payload object to cast.
     * @param clazz The class to deserialize the object into.
     * @param <T> The generic type to deserialize the object into.
     * @return T The generic type response.
     * @throws JsonProcessingException Json parsing exception.
     */
    @SuppressWarnings("unchecked")
    public static <T> T castObject(ObjectMapper mapper, Object payload, Class<T> clazz) throws JsonProcessingException {
        if (clazz.isAssignableFrom(String.class)){
            return (T)mapper.writeValueAsString(payload);
        }
        else {
            return mapper.convertValue(payload, clazz);
        }
    }
}
