/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;

import java.security.SecureRandom;

/**
 * Represents a retry policy that performs exponential backoff with jitter retries.
 */
public class ExponentialBackoffWithJitter implements RetryPolicy
{
    // Codes_SRS_EXPONENTIALBACKOFF_28_006: [Constructor should have default values retryCount, minBackoff, maxBackoff, deltaBackoff and firstFastRetry]
    private int retryCount = Integer.MAX_VALUE;
    private long minBackoff = 100;
    private long maxBackoff = 10*1000; //10 seconds
    private long deltaBackoff = 100;
    private boolean firstFastRetry = true;

    private SecureRandom random = new SecureRandom();

    /**
     * Constructor with default backoff values and firstFastRetry
     */
    public ExponentialBackoffWithJitter()
    {

    }

    /**
     * Constructor.
     *
     * @param retryCount the max number of retries allowed in the policies.
     * @param minBackoff the min interval between each retry.
     * @param maxBackoff the max interval between each retry.
     * @param deltaBackoff the max delta allowed between retries.
     * @param firstFastRetry indicates whether the first retry should be immediate.
     */
    public ExponentialBackoffWithJitter(int retryCount, long minBackoff, long maxBackoff, long deltaBackoff, boolean firstFastRetry)
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
     * @return the retry decision.
     */
    public RetryDecision getRetryDecision(int currentRetryCount, TransportException lastException)
    {
        // Codes_SRS_EXPONENTIALBACKOFF_28_003: [The function shall indicate immediate retry on first retry if firstFastRetry is true]
        if (currentRetryCount == 0 && this.firstFastRetry) {
            return new RetryDecision(true, 0);
        }

        // Codes_SRS_EXPONENTIALBACKOFF_28_004: [The function shall return non-zero wait time on first retry if firstFastRetry is false]
        // Codes_SRS_EXPONENTIALBACKOFF_28_005: [The function shall return waitTime according to
        // F(x) = min(Cmin+ (2^(x-1)-1) * rand(C * (1 – Jd), C*(1-Ju)), Cmax) where  x is the xth retry.]
        if (currentRetryCount < this.retryCount)
        {
            int deltaBackoffLowbound = (int)(this.deltaBackoff * 0.8);
            int deltaBackoffUpperbound = (int)(this.deltaBackoff * 1.2);
            long randomDeltaBackOff = random.nextInt(deltaBackoffUpperbound - deltaBackoffLowbound);
            long exponentialBackOffWithJitter = (int)((Math.pow(2.0, (double)currentRetryCount) - 1.0) * (randomDeltaBackOff + deltaBackoffLowbound));
            long finalWaitTimeUntilNextRetry = (int)Math.min(this.minBackoff + (double)exponentialBackOffWithJitter, this.maxBackoff);
            return new RetryDecision(true, finalWaitTimeUntilNextRetry);
        }

        return new RetryDecision(false, 0);
    }
}
