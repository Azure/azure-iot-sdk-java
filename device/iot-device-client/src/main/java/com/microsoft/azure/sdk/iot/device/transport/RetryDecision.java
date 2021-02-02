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
        // Codes_SRS_RETRYDECISION_28_001: [The constructor shall save the duration and getRetryDecision]
        this.duration = duration;
        this.shouldRetry = shouldRetry;

        // When a retry policy specifies the descision we should log what the new timing was, this will help with backoff debugging
        if (!shouldRetry){
            log.info("NOTE: A new instance of {} has been created with retry disabled, the client will not perform any retries.", RetryDecision.class.getName());
        } else {
            log.info("NOTE: A new instance of {} has been created with retry enabled, the client will perform retries every {} milliseconds.", RetryDecision.class.getName(), duration);
        }
    }

    /**
     * Getter for the getRetryDecision
     * @return true if the operation should be retried; otherwise false
     */
    public boolean shouldRetry()
    {
        // Codes_SRS_RETRYDECISION_28_002: [The function shall return the value of getRetryDecision]
        return this.shouldRetry;
    }

    /**
     * Getter for the duration
     * @return the Duration which represents the interval to wait until the next retry..
     */
    public long getDuration()
    {
        // Codes_SRS_RETRYDECISION_28_003: [The function shall return the value of duration]
        return this.duration;
    }
}