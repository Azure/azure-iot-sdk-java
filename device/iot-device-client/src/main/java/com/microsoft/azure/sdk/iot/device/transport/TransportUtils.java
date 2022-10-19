// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
public class TransportUtils
{
    public static final String IOTHUB_API_VERSION = "2020-09-30";

    private static final String JAVA_DEVICE_CLIENT_IDENTIFIER = "com.microsoft.azure.sdk.iot.iot-device-client";
    public static final String CLIENT_VERSION = getPackageVersion();

    private static final String JAVA_RUNTIME = System.getProperty("java.version");
    private static final String OPERATING_SYSTEM = System.getProperty("java.runtime.name").toLowerCase().contains("android") ? "Android" : System.getProperty("os.name");
    private static final String PROCESSOR_ARCHITECTURE = System.getProperty("os.arch");

    public final static String USER_AGENT_STRING = JAVA_DEVICE_CLIENT_IDENTIFIER + "/" + CLIENT_VERSION + " (" + JAVA_RUNTIME + "; " + OPERATING_SYSTEM +"; " + PROCESSOR_ARCHITECTURE + ")";

    // Gets the version of this SDK package from the iothub-device-client.properties file
    private static String getPackageVersion()
    {
        Map<String, String> properties = getProperties("iothub-device-client.properties");
        return properties.getOrDefault("version", "UnknownVersion");
    }

    private static Map<String, String> getProperties(String propertiesFileName)
    {
        try (InputStream inputStream = TransportUtils.class.getClassLoader().getResourceAsStream(propertiesFileName))
        {
            if (inputStream != null)
            {
                Properties properties = new Properties();
                properties.load(inputStream);
                return Collections.unmodifiableMap(properties.entrySet().stream()
                    .collect(Collectors.toMap(entry -> (String) entry.getKey(),
                        entry -> (String) entry.getValue())));
            }
        }
        catch (IOException ex)
        {
            log.warn("Failed to get properties from " + propertiesFileName, ex);
        }

        return Collections.emptyMap();
    }
}
