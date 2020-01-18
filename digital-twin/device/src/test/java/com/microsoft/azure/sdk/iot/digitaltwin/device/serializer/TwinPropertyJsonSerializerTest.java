// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.device.serializer;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinPropertyResponse;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinReportProperty;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.TwinPropertyJsonSerializer.ATTRIBUTE_STATUS_CODE;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.TwinPropertyJsonSerializer.ATTRIBUTE_STATUS_DESCRIPTION;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.TwinPropertyJsonSerializer.ATTRIBUTE_STATUS_VERSION;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.TwinPropertyJsonSerializer.ATTRIBUTE_VALUE;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.TwinPropertyJsonSerializer.DIGITAL_TWIN_COMPONENT_NAME_PREFIX;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.TwinPropertyJsonSerializer.serializeReportProperty;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TwinPropertyJsonSerializerTest {
    private static final String DIGITAL_TWIN_COMPONENT_NAME = "DIGITAL_TWIN_COMPONENT_NAME";
    private static final String PROPERTY_NAME_1 = "PROPERTY_NAME_1";
    private static final String PROPERTY_NAME_2 = "PROPERTY_NAME_2";
    private static final String PROPERTY_VALUE_1 = "true";
    private static final String PROPERTY_VALUE_2 = "{\"number\":123,\"str\":\"abc\",\"boolean\":true}";
    private static final int STATUS_CODE = 1234;
    private static final int STATUS_VERSION = 5678;
    private static final String STATUS_DESCRIPTION = "STATUS_DESCRIPTION";
    private static final DigitalTwinPropertyResponse PROPERTY_RESPONSE = DigitalTwinPropertyResponse.builder()
                                                                                                    .statusCode(STATUS_CODE)
                                                                                                    .statusVersion(STATUS_VERSION)
                                                                                                    .statusDescription(STATUS_DESCRIPTION)
                                                                                                    .build();
    @Mock
    private DigitalTwinReportProperty property1;
    @Mock
    private DigitalTwinReportProperty property2;

    @Before
    public void setUp() {
        when(property1.getPropertyName()).thenReturn(PROPERTY_NAME_1);
        when(property1.getPropertyValue()).thenReturn(PROPERTY_VALUE_1);
        when(property2.getPropertyName()).thenReturn(PROPERTY_NAME_2);
        when(property2.getPropertyValue()).thenReturn(PROPERTY_VALUE_2);
        when(property2.getPropertyResponse()).thenReturn(PROPERTY_RESPONSE);
    }

    @Test
    public void serializeReportPropertyTest() {
        List<DigitalTwinReportProperty> reportProperties = asList(property1, property2);
        Property property = serializeReportProperty(DIGITAL_TWIN_COMPONENT_NAME, reportProperties);
        assertThat(property.getKey()).isEqualTo(DIGITAL_TWIN_COMPONENT_NAME_PREFIX + DIGITAL_TWIN_COMPONENT_NAME);
        assertThat(property.getValue()).isInstanceOf(JsonObject.class);
        JsonObject value = (JsonObject) property.getValue();
        assertThat(value.entrySet()).hasSameSizeAs(reportProperties);
        JsonObject property1 = value.getAsJsonObject(PROPERTY_NAME_1);
        assertThat(property1).isNotNull();
        assertThat(property1.entrySet()).hasSize(1);
        assertThat(property1.get(ATTRIBUTE_VALUE).toString()).isEqualTo(PROPERTY_VALUE_1);
        JsonObject property2 = value.getAsJsonObject(PROPERTY_NAME_2);
        assertThat(property2).isNotNull();
        assertThat(property2.entrySet()).hasSize(4);
        assertThat(property2.get(ATTRIBUTE_VALUE).toString()).isEqualTo(PROPERTY_VALUE_2);
        assertThat(property2.get(ATTRIBUTE_STATUS_CODE).getAsInt()).isEqualTo(PROPERTY_RESPONSE.getStatusCode());
        assertThat(property2.get(ATTRIBUTE_STATUS_VERSION).getAsInt()).isEqualTo(PROPERTY_RESPONSE.getStatusVersion());
        assertThat(property2.get(ATTRIBUTE_STATUS_DESCRIPTION).getAsString()).isEqualTo(PROPERTY_RESPONSE.getStatusDescription());
    }

    @Test
    public void serializeReportPropertyWithNullValueTest() {
        DigitalTwinReportProperty propertyWithNullValue = mock(DigitalTwinReportProperty.class);
        when(propertyWithNullValue.getPropertyName()).thenReturn(PROPERTY_NAME_1);
        when(propertyWithNullValue.getPropertyValue()).thenReturn(null);
        List<DigitalTwinReportProperty> reportProperties = singletonList(propertyWithNullValue);
        Property property = serializeReportProperty(DIGITAL_TWIN_COMPONENT_NAME, reportProperties);
        assertThat(property.getKey()).isEqualTo(DIGITAL_TWIN_COMPONENT_NAME_PREFIX + DIGITAL_TWIN_COMPONENT_NAME);
        assertThat(property.getValue()).isInstanceOf(JsonObject.class);
        JsonObject value = (JsonObject) property.getValue();
        assertThat(value.entrySet()).hasSameSizeAs(reportProperties);
        JsonObject property1 = value.getAsJsonObject(PROPERTY_NAME_1);
        assertThat(property1).isNotNull();
        assertThat(property1.entrySet()).hasSize(1);
        assertThat(property1.get(ATTRIBUTE_VALUE)).isEqualTo(JsonNull.INSTANCE);
    }
}
