// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.sample;

import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClientImpl;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;
import java.util.Scanner;

@Slf4j
public class GetModelSample
{
    private static final String CONNECTION_STRING = System.getenv("IOTHUB_CONNECTION_STRING");
    private static final String MODEL_ID = System.getenv("MODEL_ID");

    private static final String usage = "In order to run this sample, you must set environment variables for \n" +
            "IOTHUB_CONNECTION_STRING - Your IoT Hub's connection string\n" +
            "MODEL_ID - your digital twin id to invoke the command onto";

    public static void main(String[] args) {
        verifyInputs();
        
        DigitalTwinServiceClient digitalTwinServiceClient = DigitalTwinServiceClientImpl.buildFromConnectionString().connectionString(CONNECTION_STRING).build();

        log.info("Getting the model with model id " + MODEL_ID);

        String modelDefinition = digitalTwinServiceClient.getModel(MODEL_ID);

        log.info("Got the model definition, the returned string was:");
        log.info(modelDefinition);

        log.info("Enter any key to finish");
        new Scanner(System.in).nextLine();
    }

    private static void verifyInputs() {
        if (isNullOrEmpty(CONNECTION_STRING) || isNullOrEmpty(MODEL_ID)) {
            log.warn(usage);
            System.exit(0);
        }
    }

    private static boolean isNullOrEmpty(String s)
    {
        return s == null || s.length() == 0;
    }


}
