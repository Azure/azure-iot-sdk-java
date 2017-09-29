// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

public class TransportUtils
{
    public static final String JAVA_DEVICE_CLIENT_IDENTIFIER = "com.microsoft.azure.sdk.iot.iot-device-client/";
    public static final String CLIENT_VERSION = "1.5.35";

    private static final byte[] SLEEP_INTERVALS = {1, 2, 4, 8, 16, 32, 60};
    /** Generates a reconnection time with an exponential backoff
     * and a maximum value of 60 seconds.
     *
     * @param currentAttempt the number of attempts
     * @return the sleep interval in milliseconds until the next attempt.
     */
    public static int generateSleepInterval(int currentAttempt)
    {
        if (currentAttempt > 7)
        {
            return SLEEP_INTERVALS[6] * 1000;
        }
        else if (currentAttempt > 0)
        {
            return SLEEP_INTERVALS[currentAttempt - 1] * 1000;
        }
        else
        {
            return 0;
        }
    }
}
