// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers;

import io.reactivex.rxjava3.core.Flowable;
import lombok.NonNull;
import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.join;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class Tools {

    private static final String INTERFACE_ID_DELIMITER = ":";

    public static String retrieveEnvironmentVariableValue(String environmentVariableName) {
        String environmentVariableValue = System.getenv().get(environmentVariableName);
        if (isBlank(environmentVariableValue)) {
            environmentVariableValue = System.getProperty(environmentVariableName);
            if (isBlank(environmentVariableValue)) {
                throw new IllegalArgumentException("Environment variable is not set: " + environmentVariableName);
            }
        }

        return environmentVariableValue;
    }

    // Interface ID is in the format: [urn:namespace:name:version]
    public static String retrieveInterfaceNameFromInterfaceId(String interfaceId) {
        String[] interfaceIdParts = interfaceId.split(INTERFACE_ID_DELIMITER);
        return interfaceIdParts[interfaceIdParts.length - 2];
    }

    public static List<String> generateRandomStringList(int listSize) {
        return Flowable.fromSupplier(UUID::randomUUID).map(UUID::toString).repeat(listSize).toList().blockingGet();
    }

    public static List<Integer> generateRandomIntegerList(int listSize) {
        return Flowable.fromSupplier(RandomUtils::nextInt).repeat(listSize).toList().blockingGet();
    }

    private static String createSinglePropertyPatch(@NonNull String propertyName, @NonNull String propertyValue) {
        return "\"" + propertyName + "\": {"
                +"      \"desired\": {"
                +"          \"value\": \"" + propertyValue + "\""
                +"      }"
                +"  }";
    }

    public static String createPropertyPatch(Map<String, String> propertiesMap) {
        List<String> propertyPatches = new ArrayList<>();
        for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
            propertyPatches.add(createSinglePropertyPatch(entry.getKey(), entry.getValue()));
        }

        return "{ \"properties\": {" + join(",", propertyPatches) + "} }";
    }
}
