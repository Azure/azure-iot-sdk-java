/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */
package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;

/**
 * An interface for the retry policy.
 */
public interface RetryPolicy
{
    /**
     * Determines whether the operation should be retried and the interval until the next retry.
     *
     * @param currentRetryCount the number of retries for the given operation
     * @param lastException the latest exception explaining why the retry is happening. This exception is guaranteed to
     *                      be retryable. In the event of a terminal exception occurring, this API will not be called.
     *                      Looking at this exception allows you to prevent retry on certain retryable exceptions, but
     *                      does not allow you to retry exceptions that are terminal.
     * @return the retry decision.
     */
    RetryDecision getRetryDecision(int currentRetryCount, TransportException lastException);
}