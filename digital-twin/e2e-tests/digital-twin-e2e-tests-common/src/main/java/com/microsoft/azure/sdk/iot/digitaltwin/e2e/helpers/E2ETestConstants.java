// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers;

public class E2ETestConstants {
    public static final int MAX_THREADS_MULTITHREADED_TEST = 5;
    public static final int MAX_WAIT_TIME_FOR_ASYNC_CALL_IN_SECONDS = 5;
    public static final String DCM_ID = "urn:contoso:azureiot:sdk:testinterface:cm:2";
    public static final String COMPONENT_KEY = "interfaces";
    public static final String VERSION_KEY = "version";
    public static final String PROPERTY_KEY = "properties";
    public static final String REPORTED_KEY = "reported";
    public static final String DESIRED_KEY = "desired";
    public static final String MODEL_DISCOVERY_MODEL_NAME = "modelInformation";
    public static final String DEFAULT_IMPLEMENTED_MODEL_INFORMATION_COMPONENT_NAME = "urn_azureiot_ModelDiscovery_DigitalTwin";

    public static final String IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME = "IOTHUB_CONNECTION_STRING";
    public static final String EVENT_HUB_CONNECTION_STRING_VAR_NAME = "EVENT_HUB_CONNECTION_STRING";
}
