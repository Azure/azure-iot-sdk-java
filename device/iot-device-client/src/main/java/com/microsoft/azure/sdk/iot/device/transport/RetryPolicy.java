/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */
package com.microsoft.azure.sdk.iot.device.transport;

/**
 * An interface for the retry policy.
 */
public interface RetryPolicy
{
    /**
     * Determines whether the operation should be retried and the interval until the next retry.
     *
     * @param currentRetryCount the number of retries for the given operation
     * @param lastException the last exception encountered
     * @return the retry decision.
     */
    RetryDecision ShouldRetry(int currentRetryCount, Exception lastException);
}