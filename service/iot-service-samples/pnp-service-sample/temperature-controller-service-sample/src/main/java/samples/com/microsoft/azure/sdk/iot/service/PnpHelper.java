// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot.service;

import com.google.gson.JsonObject;
import java.util.Set;

import com.microsoft.azure.sdk.iot.service.devicetwin.Pair;
import org.checkerframework.checker.nullness.qual.NonNull;

import static java.util.Collections.singleton;

/*
 A helper class for formatting command requests and properties as per plug and play convention.
*/
public class PnpHelper {

    private static final String PROPERTY_COMPONENT_IDENTIFIER_KEY = "__t";
    private static final String PROPERTY_COMPONENT_IDENTIFIER_VALUE = "c";

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

}
