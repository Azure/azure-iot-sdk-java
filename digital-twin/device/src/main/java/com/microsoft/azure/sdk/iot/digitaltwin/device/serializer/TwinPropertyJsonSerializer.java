// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.device.serializer;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinPropertyResponse;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinReportProperty;
import lombok.NonNull;

import java.util.List;

import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.isNotEmpty;

public final class TwinPropertyJsonSerializer {
    public static final String DIGITAL_TWIN_INTERFACE_INSTANCE_NAME_PREFIX = "$iotin:";
    private static final JsonParser JSON_PARSER = new JsonParser();
    static final String ATTRIBUTE_VALUE = "value";
    static final String ATTRIBUTE_STATUS_CODE = "sc";
    static final String ATTRIBUTE_STATUS_VERSION = "sv";
    static final String ATTRIBUTE_STATUS_DESCRIPTION = "sd";

    private TwinPropertyJsonSerializer() {
    }

    public static Property serializeReportProperty(
            @NonNull final String digitalTwinInterfaceInstanceName,
            @NonNull final List<DigitalTwinReportProperty> reportProperties
    ) {
        JsonObject propertiesNode = new JsonObject();
        for (DigitalTwinReportProperty reportProperty : reportProperties) {
            JsonObject propertyNode = new JsonObject();
            if (isNotEmpty(reportProperty.getPropertyValue())) {
                propertyNode.add(ATTRIBUTE_VALUE, JSON_PARSER.parse(reportProperty.getPropertyValue()));
            } else {
                propertyNode.add(ATTRIBUTE_VALUE, JsonNull.INSTANCE);
            }
            DigitalTwinPropertyResponse propertyResponse = reportProperty.getPropertyResponse();
            if (propertyResponse != null) {
                propertyNode.addProperty(ATTRIBUTE_STATUS_CODE, propertyResponse.getStatusCode());
                propertyNode.addProperty(ATTRIBUTE_STATUS_VERSION, propertyResponse.getStatusVersion());
                String statusDescription = propertyResponse.getStatusDescription();
                if (isNotEmpty(statusDescription)) {
                    propertyNode.addProperty(ATTRIBUTE_STATUS_DESCRIPTION, statusDescription);
                }
            }
            propertiesNode.add(reportProperty.getPropertyName(), propertyNode);
        }
        return new Property(DIGITAL_TWIN_INTERFACE_INSTANCE_NAME_PREFIX + digitalTwinInterfaceInstanceName, propertiesNode);
    }

}
