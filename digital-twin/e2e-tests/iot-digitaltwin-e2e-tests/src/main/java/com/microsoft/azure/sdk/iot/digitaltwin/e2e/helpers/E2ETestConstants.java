// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers;

public class E2ETestConstants {
    public static final int MAX_THREADS_MULTITHREADED_TEST = 5;
    public static final int MAX_WAIT_TIME_FOR_ASYNC_CALL_IN_SECONDS = 5;
    public static final String DCM_ID = "urn:contoso:azureiot:sdk:testinterface:cm:2";

    public static final String IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME = "IOTHUB_CONNECTION_STRING";
    public static final String EVENT_HUB_CONNECTION_STRING_VAR_NAME = "EVENT_HUB_CONNECTION_STRING";
}
