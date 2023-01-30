/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.device.transport;

import lombok.extern.slf4j.Slf4j;

/**
 * Represents the retry details
 */
@Slf4j
public class RetryDecision
{
    private final boolean shouldRetry;
    private final long duration;

    /**
     * Constructor.
     *
     * @param shouldRetry the max number of retries allowed in the policies.
     * @param duration the min interval between each retry in milliseconds.
     */
    public RetryDecision(boolean shouldRetry, long duration)
    {
        this.duration = duration;
        this.shouldRetry = shouldRetry;

        // When a retry policy specifies the descision we should log what the new timing was, this will help with backoff debugging
        if (!shouldRetry){
            log.debug("NOTE: A new instance of RetryDecision has been created with retry disabled, the client will not perform any retries.");
        } else {
            log.debug("NOTE: A new instance of RetryDecision has been created with retry enabled, the client will retry after {} milliseconds", duration);
        }
    }

    /**
     * Getter for the getRetryDecision
     * @return true if the operation should be retried; otherwise false
     */
    public boolean shouldRetry()
    {
        return this.shouldRetry;
    }

    /**
     * Getter for the duration
     * @return the Duration which represents the interval to wait until the next retry..
     */
    public long getDuration()
    {
        return this.duration;
    }
}