/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
public class SDKUtils
{
    private static final String SERVICE_API_VERSION = "2019-03-31";
    public static final String PROVISIONING_DEVICE_CLIENT_IDENTIFIER = "com.microsoft.azure.sdk.iot.dps.dps-device-client/";
    public static final String PROVISIONING_DEVICE_CLIENT_VERSION = getPackageVersion();

    private static final String JAVA_RUNTIME = System.getProperty("java.version");
    private static final String OPERATING_SYSTEM = System.getProperty("java.runtime.name").toLowerCase().contains("android") ? "Android" : System.getProperty("os.name");
    private static final String PROCESSOR_ARCHITECTURE = System.getProperty("os.arch");

    public static String getServiceApiVersion()
    {
        return SERVICE_API_VERSION;
    }

    public static String getUserAgentString()
    {
        return PROVISIONING_DEVICE_CLIENT_IDENTIFIER + PROVISIONING_DEVICE_CLIENT_VERSION + " (" + JAVA_RUNTIME + "; " + OPERATING_SYSTEM +"; " + PROCESSOR_ARCHITECTURE + ")";
    }

    // Gets the version of this SDK package from the provisioning-device-client.properties file
    private static String getPackageVersion()
    {
        Map<String, String> properties = getProperties("provisioning-device-client.properties");
        return properties.getOrDefault("version", "UnknownVersion");
    }

    private static Map<String, String> getProperties(String propertiesFileName)
    {
        try (InputStream inputStream = SDKUtils.class.getClassLoader().getResourceAsStream(propertiesFileName))
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
