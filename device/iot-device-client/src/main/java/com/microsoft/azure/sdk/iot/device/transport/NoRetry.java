/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.device.transport;

import javafx.util.Duration;

/**
  * Represents a retry policy that performs no retries.
  */
public class NoRetry implements RetryPolicy
{
    /**
     * Determines whether the operation should be retried and the interval until the next retry.
     *
     * @param currentRetryCount the number of retries for the given operation
     * @param lastException the last exception encountered
     * @return the retry decision.
     */
    public RetryDecision ShouldRetry(int currentRetryCount, Exception lastException)
    {
        // Codes_SRS_NORETRY_28_001: [The function shall return the false and 0 as the RetryDecision despite on inputs.]
        return new RetryDecision(false, Duration.UNKNOWN);
    }
}
