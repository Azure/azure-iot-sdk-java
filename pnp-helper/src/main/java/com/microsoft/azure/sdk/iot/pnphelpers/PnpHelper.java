// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.pnphelpers;

import com.google.gson.Gson;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.service.devicetwin.Pair;
import lombok.NonNull;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;

public class PnpHelper {

    private static final String TELEMETRY_COMPONENT_NAME = "$.sub";
    private static final String ENCODING_UTF_8 = StandardCharsets.UTF_8.name();
    private static final String CONTENT_APPLICATION_JSON = "application/json";
    private static final String PROPERTY_COMPONENT_IDENTIFIER_KEY = "__t";
    private static final String PROPERTY_COMPONENT_IDENTIFIER_VALUE = "c";

    private static final Gson gson = new Gson();

    /**
     * Create a plug and play compatible telemetry message.
     * @param telemetryName The name of the telemetry, as defined in the DTDL interface. Must be 64 characters or less. For more details refer <see href="https://github.com/Azure/opendigitaltwins-dtdl/blob/master/DTDL/v2/dtdlv2.md#telemetry"
     * @param telemetryValue The telemetry payload, in the format defined in the DTDL interface.
     * @return A plug and play compatible telemetry message, which can be sent to IoT Hub.
     */
    public static Message createIotHubMessageUtf8(@NonNull String telemetryName, @NonNull Object telemetryValue) {
        return createIotHubMessageUtf8(telemetryName, telemetryValue, null);
    }

    /**
     * Create a plug and play compatible telemetry message.
     * @param telemetryName The name of the telemetry, as defined in the DTDL interface. Must be 64 characters or less. For more details refer <see href="https://github.com/Azure/opendigitaltwins-dtdl/blob/master/DTDL/v2/dtdlv2.md#telemetry"
     * @param telemetryValue The telemetry payload, in the format defined in the DTDL interface.
     * @param componentName (optional) The name of the component in which the telemetry is defined. Can be null for telemetry defined under the root interface.
     * @return A plug and play compatible telemetry message, which can be sent to IoT Hub.
     */
    public static Message createIotHubMessageUtf8(@NonNull String telemetryName, @NonNull Object telemetryValue, String componentName) {
        Map<String, Object> payload = singletonMap(telemetryName, telemetryValue);

        Message message = new Message(gson.toJson(payload));
        message.setContentEncoding(ENCODING_UTF_8);
        message.setContentTypeFinal(CONTENT_APPLICATION_JSON);

        if (componentName != null) {
            message.setProperty(TELEMETRY_COMPONENT_NAME, componentName);
        }

        return message;
    }

    /**
     * Create a key-value property patch for both read-only and read-write properties.
     * @param propertyName The property name, as defined in the DTDL interface.
     * @param propertyValue The property value, in the format defined in the DTDL interface.
     * @return The property path for read-only and read-write property updates.
     *
     * The property patch is created in the below format:
     * {
     *     "samplePropertyName": 20
     * }
     */
    public static Set<Property> createPropertyPatch(@NonNull String propertyName, @NonNull Object propertyValue) {
        return singleton(new Property(propertyName, propertyValue));
    }



    /**
     * Create a key-value property patch for both read-only and read-write properties.
     * @param propertyName The property name, as defined in the DTDL interface.
     * @param propertyValue The property value, in the format defined in the DTDL interface.
     * @param componentName (optional) The name of the component in which the property is defined. Can be null for property defined under the root interface.
     * @return The property path for read-only and read-write property updates.
     *
     * The property patch is created in the below format:
     * {
     *     "sampleComponentName": {
     *         "__t": "c",
     *         "samplePropertyName"": 20
     *     }
     * }
     */
    public static Set<Property> createPropertyPatch(@NonNull final String propertyName, @NonNull final Object propertyValue, String componentName) {
        Map<String, Object> componentProperty = new HashMap<String, Object>() {{
            put(PROPERTY_COMPONENT_IDENTIFIER_KEY, PROPERTY_COMPONENT_IDENTIFIER_VALUE);
            put(propertyName, propertyValue);
        }};

        return singleton(new Property(componentName, componentProperty));
    }

    /**
     * Create a key-embedded value property patch for read-write properties.
     * Embedded value property updates are sent from a device in response to a service-initiated read-write property update.
     * @param propertyName The property name, as defined in the DTDL interface.
     * @param propertyValue The property value, in the format defined in the DTDL interface.
     * @param ackCode The acknowledgment code from the device, for the embedded value property update.
     * @param ackVersion The version no. of the service-initiated read-write property update.
     * @param ackDescription (optional) The description from the device, accompanying the embedded value property update.
     * @return The property patch for embedded value property updates for read-write properties.
     *
     * The property patch is created in the below format:
     * {
     *     "samplePropertyName": {
     *         "value": 20,
     *         "ac": 200,
     *         "av": 5,
     *         "ad": "The update was successful."
     *     }
     * }
     */
    public static Set<Property> createPropertyEmbeddedValuePatch(
            @NonNull String propertyName,
            @NonNull final Object propertyValue,
            @NonNull final Integer ackCode,
            @NonNull final Long ackVersion,
            String ackDescription) {

        Map<String, Object> embeddedProperty = new HashMap<String, Object>() {{
            put("value", propertyValue);
            put("ac", ackCode);
            put("av", ackVersion);
        }};

        if (ackDescription != null && !ackDescription.isEmpty()) {
            embeddedProperty.put("ad", ackDescription);
        }

        return singleton(new Property(propertyName, embeddedProperty));
    }

    /**
     * Create a key-embedded value property patch for read-write properties.
     * Embedded value property updates are sent from a device in response to a service-initiated read-write property update.
     * @param propertyName The property name, as defined in the DTDL interface.
     * @param propertyValue The property value, in the format defined in the DTDL interface.
     * @param componentName (optional) The name of the component in which the property is defined. Can be null for property defined under the root interface.
     * @param ackCode The acknowledgment code from the device, for the embedded value property update.
     * @param ackVersion The version no. of the service-initiated read-write property update.
     * @param ackDescription (optional) The description from the device, accompanying the embedded value property update.
     * @return The property patch for embedded value property updates for read-write properties.
     *
     * The property patch is created in the below format:
     * {
     *     "sampleComponentName": {
     *         "__t": "c",
     *         "samplePropertyName": {
     *             "value": 20,
     *             "ac": 200,
     *             "av": 5,
     *             "ad": "The update was successful."
     *         }
     *     }
     * }
     */
    public static Set<Property> createPropertyEmbeddedValuePatch(
            @NonNull final String propertyName,
            @NonNull final Object propertyValue,
            String componentName,
            @NonNull final Integer ackCode,
            @NonNull final Long ackVersion,
            String ackDescription) {

        final Map<String, Object> embeddedProperty = new HashMap<String, Object>() {{
            put("value", propertyValue);
            put("ac", ackCode);
            put("av", ackVersion);
        }};

        if (ackDescription != null && !ackDescription.isEmpty()) {
            embeddedProperty.put("ad", ackDescription);
        }

        Map<String, Object> componentProperty = new HashMap<String, Object>() {{
            put(PROPERTY_COMPONENT_IDENTIFIER_KEY, PROPERTY_COMPONENT_IDENTIFIER_VALUE);
            put(propertyName, embeddedProperty);
        }};

        return singleton(new Property(componentName, componentProperty));
    }

<<<<<<< HEAD
    /**
     * Helper to retrieve the command request value from a plug and play compatible command invocation request received.
     * @param jsonPayload The command payload in json format.
     * @return The plug and play command request value.
     *
     * The command request to be parsed is in the below format:
     * {
     *     "commandRequest": {
     *         "value": 20
     *     }
     * }
     */
    public static JsonElement getPnpCommandRequestValue(@NonNull String jsonPayload) {
        JsonObject jsonObject = gsonBuilder.create().fromJson(jsonPayload, JsonObject.class);
        return jsonObject.get("commandRequest").getAsJsonObject().get("value");
    }

    /**
     * Create a key-value property patch for twin update. This patch is to update a property on a component of a device.
     * @param propertyName The property name, as defined in the DTDL interface.
     * @param propertyValue The property value, in the format defined in the DTDL interface.
     * @param componentName The name of the component on which to update the property.
     * @return The property patch for twin update.
     *
     * The property patch should be in the below format:
     *   "componentName": {
     *      "__t": "c",
     *      "propertyName": {
     *        "value": "hello"
     *      }
     */
    public static Set<Pair> CreateComponentPropertyPatch(@NonNull String propertyName, @NonNull double propertyValue, @NonNull String componentName)
    {
        JsonObject patchJson = new JsonObject();
        patchJson.addProperty(PROPERTY_COMPONENT_IDENTIFIER_KEY, PROPERTY_COMPONENT_IDENTIFIER_VALUE);
        patchJson.addProperty(propertyName, propertyValue);
        return singleton(new Pair(componentName, patchJson));
    }

    /**
     * Helper to construct the command to call on a component of a device..
     * @param componentName The name of the component on which to invoke the command.
     * @param commandName The name of the command to invoke.
     * @return The command name to invoke.
     *
     * The command to invoke for components should be in the format:
     * "componentName*commandName"
     */
    public static String CreateComponentCommandName(String componentName, String commandName)
    {
        return componentName + "*" + commandName;
    }
=======
>>>>>>> origin/master
}
