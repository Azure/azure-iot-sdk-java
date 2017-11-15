// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

/**
 * Abstract class with the parser for the provisioning configurations.
 *
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
 */
public abstract class Serializable implements java.io.Serializable
{
    /**
     * Serializer
     *
     * <p>
     *     Creates a {@code String}, which the content represents the
     *     information in the child class and its subclasses in a JSON format.
     * </p>
     *
     * @return The {@code String} with the JSON.
     */
    public String toJson()
    {
        /* SRS_SERIALIZABLE_21_001: [The toJson shall return a String with the information in the child class in a JSON format.] */
        return toJsonElement().toString();
    }

    /**
     * Creates a pretty print JSON with the content of the child class and subclasses.
     *
     * @return The {@code String} with the pretty print JSON.
     */
    @Override
    public String toString()
    {
        /* SRS_SERIALIZABLE_21_002: [The toString shall return a String with the information in the child class in a pretty print JSON.] */
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        return gson.toJson(toJsonElement());
    }

    protected abstract JsonElement toJsonElement();
}
