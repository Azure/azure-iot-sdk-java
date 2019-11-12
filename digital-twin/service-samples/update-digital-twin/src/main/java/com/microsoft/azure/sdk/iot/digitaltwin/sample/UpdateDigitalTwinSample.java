// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.sample;

import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClientImpl;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Scanner;

@Slf4j
public class UpdateDigitalTwinSample
{
    private static final String CONNECTION_STRING = System.getenv("IOTHUB_CONNECTION_STRING");
    private static final String DIGITAL_TWIN_ID = System.getenv("DIGITAL_TWIN_ID");
    private static final String INTERFACE_INSTANCE_NAME = System.getenv("INTERFACE_INSTANCE_NAME");
    private static final String PROPERTY_NAME = System.getenv("PROPERTY_NAME");
    private static final String PROPERTY_VALUE = System.getenv("PROPERTY_VALUE");

    private static final String usage = "In order to run this sample, you must set environment variables for \n" +
            "IOTHUB_CONNECTION_STRING - Your IoT Hub's connection string\n" +
            "DIGITAL_TWIN_ID - your digital twin id to invoke the command onto\n" +
            "INTERFACE_INSTANCE_NAME - the interface the command belongs to\n" +
            "PROPERTY_NAME - the name of the property to update on your digital twin\n" +
            "PROPERTY_VALUE - the value of the property to set";

    public static void main(String[] args) throws IOException
    {
        verifyInputs();
        DigitalTwinServiceClient digitalTwinServiceClient = DigitalTwinServiceClientImpl.buildFromConnectionString().connectionString(CONNECTION_STRING).build();

        log.info("Getting the status of digital twin " + DIGITAL_TWIN_ID);

        String patch = buildUpdatePatchSinglePropertyOnSingleInterface(PROPERTY_NAME, PROPERTY_VALUE);

        String digitalTwin = digitalTwinServiceClient.updateDigitalTwinProperties(DIGITAL_TWIN_ID, INTERFACE_INSTANCE_NAME, patch);

        log.info("Got the status of the digital twin successfully, the returned string was:");
        log.info(digitalTwin);

        log.info("Enter any key to finish");
        new Scanner(System.in).nextLine();
    }

    private static String buildUpdatePatchSinglePropertyOnSingleInterface(String propertyName, String propertyValue)
    {
        String patch =
                "{" +
                "  \"properties\": {" +
                "    \"" + propertyName + "\": {" +
                "      \"desired\": {" +
                "        \"value\": \"" + propertyValue + "\"" +
                "      }" +
                "    }" +
                "  }" +
                "}";

        return patch;
    }

    private static String buildUpdatePatchMultiplePropertiesOnSameInterface(String propertyName, String propertyValue, String property2Name, String property2Value)
    {
        String patch =
                "{" +
                "  \"properties\": {" +
                "    \"" + propertyName + "\": {" +
                "      \"desired\": {" +
                "        \"value\": \"" + propertyValue + "\"" +
                "      }" +
                "    }," +
                "    \"" + property2Name + "\": {" +
                "      \"desired\": {" +
                "        \"value\": \"" + property2Value + "\"" +
                "      }" +
                "    }" +
                "  }" +
                "}";

        return patch;
    }

    private static void verifyInputs() {
        if (isNullOrEmpty(CONNECTION_STRING) || isNullOrEmpty(DIGITAL_TWIN_ID) || isNullOrEmpty(INTERFACE_INSTANCE_NAME) || isNullOrEmpty(PROPERTY_NAME) || isNullOrEmpty(PROPERTY_VALUE)) {
            log.warn(usage);
            System.exit(0);
        }
    }

    private static boolean isNullOrEmpty(String s)
    {
        return s == null || s.length() == 0;
    }
}
