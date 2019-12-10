// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClientImpl;
import com.microsoft.azure.sdk.iot.digitaltwin.service.models.DigitalTwinCommandResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Scanner;

@Slf4j
public class InvokeDigitalTwinCommandSample
{
    private static final String IOTHUB_CONNECTION_STRING = System.getenv("IOTHUB_CONNECTION_STRING");
    private static final String DEVICE_ID = System.getenv("DEVICE_ID");
    private static final String INTERFACE_INSTANCE_NAME = System.getenv("INTERFACE_INSTANCE_NAME");
    private static final String COMMAND_NAME = System.getenv("COMMAND_NAME");
    private static String PAYLOAD = System.getenv("PAYLOAD"); //optional

    private static final String usage = "In order to run this sample, you must set environment variables for \n" +
            "IOTHUB_CONNECTION_STRING - Your IoT Hub's connection string\n" +
            "DEVICE_ID - The ID of the device to invoke the command onto\n" +
            "INTERFACE_INSTANCE_NAME - The interface the command belongs to\n" +
            "COMMAND_NAME - The name of the command to invoke on your digital twin\n" +
            "PAYLOAD - (optional) The json payload to include in the command";

    public static void main(String[] args) throws IOException {
        verifyInputs();

        DigitalTwinServiceClient digitalTwinServiceClient = DigitalTwinServiceClientImpl.buildFromConnectionString().connectionString(IOTHUB_CONNECTION_STRING).build();

        log.info("Invoking " + COMMAND_NAME + " on device " + DEVICE_ID + " with interface instance name " + INTERFACE_INSTANCE_NAME);

        DigitalTwinCommandResponse digitalTwinCommandResponse = digitalTwinServiceClient.invokeCommand(DEVICE_ID, INTERFACE_INSTANCE_NAME, COMMAND_NAME, PAYLOAD);

        log.info("Command invoked on the device successfully, the returned status was " + digitalTwinCommandResponse.getStatus() + " and the request id was " + digitalTwinCommandResponse.getRequestId());

        if (digitalTwinCommandResponse.getPayload() == null)
        {
            log.info("The returned PAYLOAD was null");
        }
        else
        {
            log.info("The returned PAYLOAD was ");
            log.info(toPrettyFormat(digitalTwinCommandResponse.getPayload()));
        }

        log.info("Enter any key to finish\n");
        new Scanner(System.in).nextLine();
    }

    private static void verifyInputs() {
        if (isNullOrEmpty(IOTHUB_CONNECTION_STRING) || isNullOrEmpty(DEVICE_ID) || isNullOrEmpty(INTERFACE_INSTANCE_NAME) || isNullOrEmpty(COMMAND_NAME)) {
            log.warn(usage);
            System.exit(0);
        }

        if (PAYLOAD == null || PAYLOAD.isEmpty())
        {
            PAYLOAD = null;
        }
    }

    private static boolean isNullOrEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static String toPrettyFormat(String jsonString) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Object json = mapper.readValue(jsonString, Object.class);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
    }
}
