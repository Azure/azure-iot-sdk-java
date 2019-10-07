// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.sample;

import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.microsoft.azure.sdk.iot.digitaltwin.device.AbstractDigitalTwinInterfaceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinReportProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.isNotEmpty;

@Slf4j
public class DeviceInformation extends AbstractDigitalTwinInterfaceClient {
    private static final String DEVICE_INFORMATION_INTERFACE_ID = "urn:azureiot:DeviceManagement:DeviceInformation:1";
    private static final String DEVICE_INFORMATION_INTERFACE_INSTANCE = "deviceInformation";
    private static final String PROPERTY_MANUFACTURER = "manufacturer";
    private static final String PROPERTY_MODEL = "model";
    private static final String PROPERTY_SOFTWARE_VERSION = "swVersion";
    private static final String PROPERTY_OPERATING_SYSTEM = "osName";
    private static final String PROPERTY_PROCESSOR_ARCHITECTURE = "processorArchitecture";
    private static final String PROPERTY_PROCESSOR_MANUFACTURER = "processorManufacturer";
    private static final String PROPERTY_TOTAL_STORAGE = "totalStorage";
    private static final String PROPERTY_TOTAL_MEMORY = "totalMemory";
    private final Map<String, ValueNode> properties;

    @Builder
    private DeviceInformation(String manufacturer,
            String model,
            String softwareVersion,
            String osName,
            String processorArchitecture,
            String processorManufacturer,
            Double totalStorage,
            Double totalMemory) {
        super(DEVICE_INFORMATION_INTERFACE_INSTANCE, DEVICE_INFORMATION_INTERFACE_ID);
        this.properties = new HashMap<>();
        if (isNotEmpty(manufacturer)) {
            properties.put(PROPERTY_MANUFACTURER, TextNode.valueOf(manufacturer));
        }
        if (isNotEmpty(model)) {
            properties.put(PROPERTY_MODEL, TextNode.valueOf(model));
        }
        if (isNotEmpty(softwareVersion)) {
            properties.put(PROPERTY_SOFTWARE_VERSION, TextNode.valueOf(softwareVersion));
        }
        if (isNotEmpty(osName)) {
            properties.put(PROPERTY_OPERATING_SYSTEM, TextNode.valueOf(osName));
        }
        if (isNotEmpty(processorArchitecture)) {
            properties.put(PROPERTY_PROCESSOR_ARCHITECTURE, TextNode.valueOf(processorArchitecture));
        }
        if (isNotEmpty(processorManufacturer)) {
            properties.put(PROPERTY_PROCESSOR_MANUFACTURER, TextNode.valueOf(processorManufacturer));
        }
        if (totalStorage != null) {
            properties.put(PROPERTY_TOTAL_STORAGE, DoubleNode.valueOf(totalStorage));
        }
        if (totalMemory != null) {
            properties.put(PROPERTY_TOTAL_MEMORY, DoubleNode.valueOf(totalMemory));
        }
    }

    @Override
    protected void onRegistered() {
        super.onRegistered();
        if (!properties.isEmpty()) {
            log.debug("Reporting device information...");
            List<DigitalTwinReportProperty> reportProperties = new ArrayList<>();
            for (Entry<String, ValueNode> entry : properties.entrySet()) {
                DigitalTwinReportProperty property = DigitalTwinReportProperty.builder()
                        .propertyName(entry.getKey())
                        .propertyValue(entry.getValue().toString())
                        .build();
                reportProperties.add(property);
            }
            Disposable reportPropertiesProcess = reportPropertiesAsync(reportProperties)
                    .subscribe(new Consumer<DigitalTwinClientResult>() {
                        @Override
                        public void accept(DigitalTwinClientResult result) {
                            log.debug("Report device information was {}", result);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) {
                            log.debug("Report device information failed.", throwable);
                        }
                    });
            log.debug("Once application quit, should dispose {}.", reportPropertiesProcess);
        }
    }
}
