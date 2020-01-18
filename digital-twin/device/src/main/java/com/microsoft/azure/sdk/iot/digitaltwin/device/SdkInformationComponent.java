// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.device;

import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinReportProperty;
import io.reactivex.rxjava3.functions.Consumer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static java.util.Arrays.asList;
import static lombok.AccessLevel.PACKAGE;

/**
 * SdkInformationComponent is an example of Digital Twin component. It reports SDK information to Digital Twin Service.
 * It is a singleton.
 */
@Slf4j
public class SdkInformationComponent extends AbstractDigitalTwinComponent {
    static final String SDK_INFORMATION_COMPONENT_NAME = "urn_azureiot_Client_SDKInformation";
    private static final String SDK_INFORMATION_INTERFACE_ID = "urn:azureiot:Client:SDKInformation:1";
    private static final String DIGITAL_TWIN_SDK_INFORMATION_PROPERTY_LANGUAGE = "language";
    private static final String DIGITAL_TWIN_SDK_INFORMATION_PROPERTY_VERSION = "version";
    private static final String DIGITAL_TWIN_SDK_INFORMATION_PROPERTY_VENDOR = "vendor";
    private static final String DIGITAL_TWIN_SDK_INFORMATION_LANGUAGE = "Java";
    private static final String DIGITAL_TWIN_SDK_INFORMATION_VERSION = "1.0.0";
    private static final String DIGITAL_TWIN_SDK_INFORMATION_VENDOR = "Microsoft";
    private static final SdkInformationComponent INSTANCE = new SdkInformationComponent();
    @Getter(PACKAGE)
    private final List<DigitalTwinReportProperty> sdkInformationProperties;

    private SdkInformationComponent() {
        super(SDK_INFORMATION_COMPONENT_NAME, SDK_INFORMATION_INTERFACE_ID);
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
        sdkInformationProperties = asList(propertyLanguage, propertyVersion, propertyVendor);
    }

    /**
     * Retrieve SdkInformationComponent instance
     * @return SdkInformationComponent instance
     */
    public static SdkInformationComponent getInstance() {
        return INSTANCE;
    }

    @Override
    public void ready() {
        super.ready();
        reportPropertiesAsync(sdkInformationProperties).subscribe(
                new Consumer<DigitalTwinClientResult>() {
                    @Override
                    public void accept(DigitalTwinClientResult result) {
                        log.debug("ReportSdkInformation was {}", result);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        log.error("ReportSdkInformation failed", throwable);
                    }
                });
    }
}
