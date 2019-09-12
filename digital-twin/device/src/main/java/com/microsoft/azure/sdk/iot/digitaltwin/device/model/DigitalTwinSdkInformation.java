package com.microsoft.azure.sdk.iot.digitaltwin.device.model;

import java.util.List;

import static java.util.Arrays.asList;

public final class DigitalTwinSdkInformation {
    private static final String DIGITAL_TWIN_SDK_INFORMATION_PROPERTY_LANGUAGE = "language";
    private static final String DIGITAL_TWIN_SDK_INFORMATION_PROPERTY_VERSION = "version";
    private static final String DIGITAL_TWIN_SDK_INFORMATION_PROPERTY_VENDOR = "vendor";
    private static final String DIGITAL_TWIN_SDK_INFORMATION_LANGUAGE = "Java";
    private static final String DIGITAL_TWIN_SDK_INFORMATION_VERSION = "1.0.0";
    private static final String DIGITAL_TWIN_SDK_INFORMATION_VENDOR = "Microsoft";
    public static final List<DigitalTwinReportProperty> DIGITAL_TWIN_SDK_INFORMATION_PROPERTIES = createProperties();

    private DigitalTwinSdkInformation() {

    }

    private static List<DigitalTwinReportProperty> createProperties() {
        DigitalTwinReportProperty propertyLanguage = DigitalTwinReportProperty.builder()
                                                                              .propertyName(DIGITAL_TWIN_SDK_INFORMATION_PROPERTY_LANGUAGE)
                                                                              .propertyValue(DIGITAL_TWIN_SDK_INFORMATION_LANGUAGE)
                                                                              .build();
        DigitalTwinReportProperty propertyVersion = DigitalTwinReportProperty.builder()
                                                                             .propertyName(DIGITAL_TWIN_SDK_INFORMATION_PROPERTY_VERSION)
                                                                             .propertyValue(DIGITAL_TWIN_SDK_INFORMATION_VERSION)
                                                                             .build();
        DigitalTwinReportProperty propertyVendor = DigitalTwinReportProperty.builder()
                                                                            .propertyName(DIGITAL_TWIN_SDK_INFORMATION_PROPERTY_VENDOR)
                                                                            .propertyValue(DIGITAL_TWIN_SDK_INFORMATION_VENDOR)
                                                                            .build();
        return asList(propertyLanguage, propertyVersion, propertyVendor);
    }
}
