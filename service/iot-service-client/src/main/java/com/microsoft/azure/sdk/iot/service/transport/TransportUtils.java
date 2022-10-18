// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.transport;

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
    /** Version identifier key */
    public static final String versionIdentifierKey = "com.microsoft:client-version";
    public static final String javaServiceClientIdentifier = "com.microsoft.azure.sdk.iot.iot-service-client/";
    public static final String serviceVersion = getPackageVersion();

    private static final String JAVA_RUNTIME = System.getProperty("java.version");
    private static final String OPERATING_SYSTEM = System.getProperty("java.runtime.name").toLowerCase().contains("android") ? "Android" : System.getProperty("os.name");
    private static final String PROCESSOR_ARCHITECTURE = System.getProperty("os.arch");

    public static final String USER_AGENT_STRING = javaServiceClientIdentifier + serviceVersion + " (" + JAVA_RUNTIME + "; " + OPERATING_SYSTEM +"; " + PROCESSOR_ARCHITECTURE + ")";
    public static final String IOTHUB_API_VERSION = "2021-04-12";

    // Gets the version of this SDK package from the package.properties file
    private static String getPackageVersion()
    {
        Map<String, String> properties = getProperties("package.properties");
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