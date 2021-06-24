// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot.device;

import com.google.gson.Gson;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.Message;
import lombok.NonNull;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;

/*
 A helper class for formatting command requests and properties as per plug and play convention.
*/
public class PnpConvention {

    private static final String TELEMETRY_COMPONENT_NAME = "$.sub";
    private static final String ENCODING_UTF_8 = StandardCharsets.UTF_8.name();
    private static final String CONTENT_APPLICATION_JSON = "application/json";
    private static final String PROPERTY_COMPONENT_IDENTIFIER_KEY = "__t";
    private static final String PROPERTY_COMPONENT_IDENTIFIER_VALUE = "c";

    private static final Gson gson = new Gson();

    /**
     * Create a plug and play compatible telemetry message.
     * @param telemetryName The name of the telemetry, as defined in the DTDL interface. Must be 64 characters or less. For more details see <a href="https://github.com/Azure/opendigitaltwins-dtdl/blob/master/DTDL/v2/dtdlv2.md#telemetry">this documentation</a>
     * @param telemetryValue The telemetry payload, in the format defined in the DTDL interface.
     * @return A plug and play compatible telemetry message, which can be sent to IoT Hub.
     */
    public static Message createIotHubMessageUtf8(@NonNull String telemetryName, @NonNull Object telemetryValue) {
        Map<String, Object> payload = singletonMap(telemetryName, telemetryValue);
        return createIotHubMessageUtf8(payload, null);
    }

    /**
     * Create a plug and play compatible telemetry message.
     * @param telemetryName The name of the telemetry, as defined in the DTDL interface. Must be 64 characters or less. For more details see <a href="https://github.com/Azure/opendigitaltwins-dtdl/blob/master/DTDL/v2/dtdlv2.md#telemetry">this documentation</a>
     * @param telemetryValue The telemetry payload, in the format defined in the DTDL interface.
     * @param componentName (optional) The name of the component in which the telemetry is defined. Can be null for telemetry defined under the root interface.
     * @return A plug and play compatible telemetry message, which can be sent to IoT Hub.
     */
    public static Message createIotHubMessageUtf8(@NonNull String telemetryName, @NonNull Object telemetryValue, String componentName) {
        Map<String, Object> payload = singletonMap(telemetryName, telemetryValue);
        return createIotHubMessageUtf8(payload, componentName);
    }

    /**
     * Create a plug and play compatible telemetry message.
     * @param telemetryPairs The unserialized name and value telemetry pairs, as defined in the DTDL interface. Names must be 64 characters or less. For more details see
     *                       <a href="https://github.com/Azure/opendigitaltwins-dtdl/blob/master/DTDL/v2/dtdlv2.md#telemetry">this documentation</a>
     * @param componentName (optional) The name of the component in which the telemetry is defined. Can be null for telemetry defined under the root interface.
     * @return A plug and play compatible telemetry message, which can be sent to IoT Hub.
     */
    public static Message createIotHubMessageUtf8(@NonNull Map<String, Object> telemetryPairs, String componentName) {
        Message message = new Message(gson.toJson(telemetryPairs));
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
     * @param componentName The name of the component in which the property is defined. Can be null for property defined under the root interface.
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
    public static Set<Property> createComponentPropertyPatch(@NonNull final String propertyName, @NonNull final Object propertyValue, @NonNull String componentName) {

        return createComponentPropertyPatch(componentName, new HashMap<String, Object>()
        {{
            put(propertyName, propertyValue);
        }});
    }

    /**
     * Create a key-value property patch for both read-only and read-write properties.
     * @param propertyKeyValuePairs The property name and an unserialized value, as defined in the DTDL interface.
     * @param componentName The name of the component in which the property is defined. Can be null for property defined under the root interface.
     * @return The property patch for read-only and read-write property updates
     *
     * The property patch is created in the below format:
     * {
     *     "sampleComponentName": {
     *         "__t": "c",
     *         "samplePropertyName"": 20
     *     }
     * }
     */
    public static Set<Property> createComponentPropertyPatch(@NonNull String componentName, @NonNull HashMap<String, Object> propertyKeyValuePairs) {
        Map<String, Object> componentProperty = new HashMap<String, Object>() {{
            put(PROPERTY_COMPONENT_IDENTIFIER_KEY, PROPERTY_COMPONENT_IDENTIFIER_VALUE);
            putAll(propertyKeyValuePairs);
        }};

        return singleton(new Property(componentName, componentProperty));
    }

    /**
     * Creates a response to a write request on a device property.
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
    public static Set<Property> createWritablePropertyResponse(
            @NonNull String propertyName,
            @NonNull final Object propertyValue,
            @NonNull final Integer ackCode,
            @NonNull final Long ackVersion,
            String ackDescription) {

        return createWritablePropertyResponse(new HashMap<String, Object>()
        {{
            put(propertyName, propertyValue);
        }}, ackCode, ackVersion, ackDescription);
    }

    /**
     * Creates a response to a write request on a device property.
     * @param propertyPairs The name and unserialized value of the property to report.
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
    public static Set<Property> createWritablePropertyResponse(
            @NonNull HashMap<String, Object> propertyPairs,
            @NonNull final Integer ackCode,
            @NonNull final Long ackVersion,
            String ackDescription) {

        Set<Property> response = new HashSet<>();

        propertyPairs.forEach((k,v) ->
            {
                if(k == null || k.isEmpty())
                {
                    throw new IllegalArgumentException("One of the propertyPairs keys was null, empty, or white space.");
                }

                if (ackDescription != null && !ackDescription.isEmpty()) {
                    response.add(new Property(k, new WritablePropertyResponse(v, ackCode, ackVersion, ackDescription)));
                }
                else
                {
                    response.add(new Property(k, new WritablePropertyResponse(v, ackCode, ackVersion)));
                }
            });

        return response;
    }

    /**
     * Creates a response to a write request on a device property.
     * @param propertyName The property name, as defined in the DTDL interface.
     * @param propertyValue The property value, in the format defined in the DTDL interface.
     * @param componentName The name of the component in which the property is defined. Can be null for property defined under the root interface.
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
    public static Set<Property> createComponentWritablePropertyResponse(
            @NonNull final String propertyName,
            @NonNull final Object propertyValue,
            @NonNull String componentName,
            @NonNull final Integer ackCode,
            @NonNull final Long ackVersion,
            String ackDescription) {

        return createComponentWritablePropertyResponse(new HashMap<String, Object>()
        {{
            put(propertyName, propertyValue);
        }}, componentName, ackCode, ackVersion, ackDescription);
    }

    /**
     * Creates a response to a write request on a device property.
     * @param propertyPairs The name and unserialized value of the property to report.
     * @param componentName The name of the component in which the property is defined. Can be null for property defined under the root interface.
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
    public static Set<Property> createComponentWritablePropertyResponse(
            @NonNull HashMap<String, Object> propertyPairs,
            @NonNull String componentName,
            @NonNull final Integer ackCode,
            @NonNull final Long ackVersion,
            String ackDescription) {

        HashMap<String, Object> propertyMap = new HashMap<>();

        propertyPairs.forEach((k,v) ->
        {
            if(k == null || k.isEmpty())
            {
                throw new IllegalArgumentException("One of the propertyPairs keys was null, empty, or white space.");
            }

            if (ackDescription != null && !ackDescription.isEmpty()) {
                propertyMap.put(k, new WritablePropertyResponse(v, ackCode, ackVersion, ackDescription));
            }
            else
            {
                propertyMap.put(k, new WritablePropertyResponse(v, ackCode, ackVersion));
            }
        });

        Map<String, Object> componentProperty = new HashMap<String, Object>() {{
            put(PROPERTY_COMPONENT_IDENTIFIER_KEY, PROPERTY_COMPONENT_IDENTIFIER_VALUE);
            putAll(propertyMap);
        }};

        return singleton(new Property(componentName, componentProperty));
    }
}
