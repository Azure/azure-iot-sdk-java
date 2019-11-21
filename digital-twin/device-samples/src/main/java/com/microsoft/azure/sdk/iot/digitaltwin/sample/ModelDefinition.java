/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.digitaltwin.sample;

import com.microsoft.azure.sdk.iot.digitaltwin.device.AbstractDigitalTwinInterfaceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinCommandRequest;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinCommandResponse;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ModelDefinition extends AbstractDigitalTwinInterfaceClient {
    private static final String modelDefinitionInterfaceId = "urn:azureiot:ModelDiscovery:ModelDefinition:1";

    private final String environmentalSensorModelDefinition;

    private static final String getModelDefinitionCommandName = "getModelDefinition";

    @Builder
    private ModelDefinition(@NonNull String digitalTwinInterfaceInstanceName) throws IOException, URISyntaxException {
        super(digitalTwinInterfaceInstanceName, modelDefinitionInterfaceId);

        //Model definition is located in a json file within the resources folder of this sample
        ClassLoader classLoader = ModelDefinition.class.getClassLoader();
        try (InputStream is = classLoader.getResourceAsStream("EnvironmentalSensor.interface.json")) {
            environmentalSensorModelDefinition = IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }

    @Override
    protected DigitalTwinCommandResponse onCommandReceived(DigitalTwinCommandRequest digitalTwinCommandRequest) {
        try {
            // There is only one command that ModelDefinition defines, and it is getModelDefinition. That command must specify the
            // model Id in the payload, and the device must return the model definition in the command response payload
            if (getModelDefinitionCommandName.equals(digitalTwinCommandRequest.getCommandName()))
            {
                String commandPayload = digitalTwinCommandRequest.getPayload().replace("\"", "");
                if (commandPayload.equals(EnvironmentalSensor.ENVIRONMENTAL_SENSOR_INTERFACE_ID))
                {
                    return DigitalTwinCommandResponse.builder()
                        .status(STATUS_CODE_COMPLETED)
                        .payload(this.environmentalSensorModelDefinition)
                        .build();
                }
            }

            return DigitalTwinCommandResponse.builder()
                    .status(STATUS_CODE_NOT_IMPLEMENTED)
                    .build();
        } catch (Exception e) {
            log.warn("OnCommandReceived failed.", e);
            return DigitalTwinCommandResponse.builder()
                    .status(500)
                    .build();
        }
    }
}
