/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import lombok.extern.slf4j.Slf4j;

/**
  * Represents a retry policy that performs no retries.
  */
@Slf4j
public class NoRetry implements RetryPolicy
{
    public NoRetry() {
        log.info("NOTE: A no-retry policy has been craeted, the client will not perform any retries on disconnect.");
    }
    /**
     * Always says to not retry.
     *
     * @param currentRetryCount the number of retries for the given operation
     * @param lastException the last exception encountered
     * @return the retry decision.
     */
    public RetryDecision getRetryDecision(int currentRetryCount, TransportException lastException)
    {
        // Codes_SRS_NORETRY_28_001: [The function shall return the false and 0 as the RetryDecision despite on inputs.]
        return new RetryDecision(false, 0);
    }
}
