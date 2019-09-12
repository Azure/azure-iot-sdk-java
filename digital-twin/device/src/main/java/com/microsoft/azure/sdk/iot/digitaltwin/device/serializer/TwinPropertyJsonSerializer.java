package com.microsoft.azure.sdk.iot.digitaltwin.device.serializer;

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
    private static final String ATTRIBUTE_VALUE = "value";
    private static final String ATTRIBUTE_STATUS_CODE = "sc";
    private static final String ATTRIBUTE_STATUS_VERSION = "sv";
    private static final String ATTRIBUTE_STATUS_DESCRIPTION = "sd";
    private static final JsonParser JSON_PARSER = new JsonParser();

    private TwinPropertyJsonSerializer() {
    }

    public static Property serializeReportProperty(
            @NonNull final String digitalTwinInterfaceInstanceName,
            @NonNull final List<DigitalTwinReportProperty> reportProperties
    ) {
        JsonObject propertiesNode = new JsonObject();
        for (DigitalTwinReportProperty reportProperty : reportProperties) {
            JsonObject propertyNode = new JsonObject();
            propertyNode.add(ATTRIBUTE_VALUE, JSON_PARSER.parse(reportProperty.getPropertyValue()));
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
