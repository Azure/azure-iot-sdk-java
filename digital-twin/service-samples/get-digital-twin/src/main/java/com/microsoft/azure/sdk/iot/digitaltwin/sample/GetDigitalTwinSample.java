// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClientImpl;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Scanner;

@Slf4j
public class GetDigitalTwinSample
{
    private static final String IOTHUB_CONNECTION_STRING = System.getenv("IOTHUB_CONNECTION_STRING");
    private static final String DEVICE_ID = System.getenv("DEVICE_ID");

    private static final String usage = "In order to run this sample, you must set environment variables for \n" +
            "IOTHUB_CONNECTION_STRING - Your IoT Hub's connection string\n" +
            "DEVICE_ID - The ID of the device to get the digital twin of\n";

    public static void main(String[] args) throws IOException {
        verifyInputs();
        DigitalTwinServiceClient digitalTwinServiceClient = DigitalTwinServiceClientImpl.buildFromConnectionString().connectionString(IOTHUB_CONNECTION_STRING).build();

        log.info("Getting the status of digital twin " + DEVICE_ID);

        String digitalTwin = digitalTwinServiceClient.getDigitalTwin(DEVICE_ID);

        log.info("Got the status of the digital twin successfully, the returned string was:");
        log.info(toPrettyFormat(digitalTwin));

        log.info("Enter any key to finish");
        new Scanner(System.in).nextLine();
    }

    private static void verifyInputs() {
        if (isNullOrEmpty(IOTHUB_CONNECTION_STRING) || isNullOrEmpty(DEVICE_ID)) {
            log.warn(usage);
            System.exit(0);
        }
    }

    private static boolean isNullOrEmpty(String s)
    {
        return s == null || s.length() == 0;
    }

    public static String toPrettyFormat(String jsonString) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Object json = mapper.readValue(jsonString, Object.class);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
    }
}
