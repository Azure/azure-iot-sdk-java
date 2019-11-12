// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.sample;

import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClientImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

@Slf4j
public class GetDigitalTwinSample
{
    private static final String CONNECTION_STRING = System.getenv("IOTHUB_CONNECTION_STRING");
    private static final String DIGITAL_TWIN_ID = System.getenv("DIGITAL_TWIN_ID");

    private static final String usage = "In order to run this sample, you must set environment variables for \n" +
            "IOTHUB_CONNECTION_STRING - Your IoT Hub's connection string\n" +
            "DIGITAL_TWIN_ID - your digital twin id to invoke the command onto\n";

    public static void main(String[] args) {
        verifyInputs();
        DigitalTwinServiceClient digitalTwinServiceClient = DigitalTwinServiceClientImpl.buildFromConnectionString().connectionString(CONNECTION_STRING).build();

        log.info("Getting the status of digital twin " + DIGITAL_TWIN_ID);

        String digitalTwin = digitalTwinServiceClient.getDigitalTwin(DIGITAL_TWIN_ID);

        log.info("Got the status of the digital twin successfully, the returned string was:");
        log.info(digitalTwin);

        log.info("Enter any key to finish");
        new Scanner(System.in).nextLine();
    }

    private static void verifyInputs() {
        if (isNullOrEmpty(CONNECTION_STRING) || isNullOrEmpty(DIGITAL_TWIN_ID)) {
            log.warn(usage);
            System.exit(0);
        }
    }

    private static boolean isNullOrEmpty(String s)
    {
        return s == null || s.length() == 0;
    }
}
