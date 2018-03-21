/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.device.transport;

import javafx.util.Duration;

import java.util.Random;

/**
 * Represents a retry policy that performs exponential backoff with jitter retries.
 * The function to calculate the next interval is the following
 */
public class ExponentialBackoff implements RetryPolicy
{
    private int retryCount;
    private Duration minBackoff;
    private Duration maxBackoff;
    private Duration deltaBackoff;
    private boolean firstFastRetry;

    private Random random = new Random();

    /**
     * Constructor.
     *
     * @param retryCount the max number of retries allowed in the policies.
     * @param minBackoff the min interval between each retry.
     * @param maxBackoff the max interval between each retry.
     * @param deltaBackoff the max delta allowed between retries.
     * @param firstFastRetry indicates whether the first retry should be immediate.
     */
    public ExponentialBackoff(int retryCount, Duration minBackoff, Duration maxBackoff, Duration deltaBackoff, boolean firstFastRetry)
    {
        // Codes_SRS_EXPONENTIALBACKOFF_28_001: [If the retryCount is less than or equal to 0, the function shall throw an IllegalArgumentException.]
        if (retryCount <= 0)
        {
            throw new IllegalArgumentException("retryCount cannot be less than or equal to 0.");
        }

        // Codes_SRS_EXPONENTIALBACKOFF_28_002: [Constructor should save retryCount, minBackoff, maxBackoff, deltaBackoff and firstFastRetry]
        this.retryCount = retryCount;
        this.minBackoff = minBackoff;
        this.maxBackoff = maxBackoff;
        this.deltaBackoff = deltaBackoff;
        this.firstFastRetry = firstFastRetry;
    }

    /**
     * Determines whether the operation should be retried and the interval until the next retry.
     *
     * @param currentRetryCount the number of retries for the given operation
     * @param lastException the last exception encountered
     * @return the retry decision.
     */
    public RetryDecision ShouldRetry(int currentRetryCount, Exception lastException)
    {
        // Codes_SRS_EXPONENTIALBACKOFF_28_003: [The function shall indicate immediate retry on first retry if firstFastRetry is true]
        if (currentRetryCount == 0 && this.firstFastRetry) {
            return new RetryDecision(true, Duration.ZERO);
        }

        // Codes_SRS_EXPONENTIALBACKOFF_28_004: [The function shall return non-zero wait time on first retry if firstFastRetry is false]
        // Codes_SRS_EXPONENTIALBACKOFF_28_005: [The function shall return waitTime according to
        // F(x) = min(Cmin+ (2^(x-1)-1) * rand(C * (1 – Jd), C*(1-Ju)), Cmax) where  x is the xth retry.]
        if (currentRetryCount < this.retryCount)
        {
            int deltaBackoffLowbound = (int)(this.deltaBackoff.toMillis() * 0.8);
            int deltaBackoffUpperbound = (int)(this.deltaBackoff.toMillis() * 1.2);
            int randomDeltaBackOff = random.nextInt(deltaBackoffUpperbound - deltaBackoffLowbound);
            int exponentialBackOffWithJitter = (int)((Math.pow(2.0, (double)currentRetryCount) - 1.0) * (randomDeltaBackOff + deltaBackoffLowbound));
            int finalWaitTimeUntilNextRetry = (int)Math.min(this.minBackoff.toMillis()+ (double)exponentialBackOffWithJitter, this.maxBackoff.toMillis());
            return new RetryDecision(true, new Duration((double)finalWaitTimeUntilNextRetry));
        }

        return new RetryDecision(false, Duration.UNKNOWN);
    }
}
