// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.transport.ExponentialBackoffWithJitter;
import com.microsoft.azure.sdk.iot.device.transport.RetryDecision;
import com.microsoft.azure.sdk.iot.device.transport.RetryPolicy;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.junit.Test;

import java.security.SecureRandom;

import static org.junit.Assert.*;

public class ExponentialBackoffWithJitterTest
{
    @Mocked
    SecureRandom mockedRandom;

    // Tests_SRS_EXPONENTIALBACKOFF_28_001: [If the retryCount is less than or equal to 0, the function shall throw an IllegalArgumentException.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsWithZeroRetryCount()
    {
        //act
        final RetryPolicy exp = new ExponentialBackoffWithJitter(
                0, 100, 10 * 1000, 100, true);
    }

    // Tests_SRS_EXPONENTIALBACKOFF_28_002: [Constructor should save retryCount, minBackoff, maxBackoff, deltaBackoff and firstFastRetry]
    @Test
    public void constructorSavesParameterToLocal()
    {
        //act
        final RetryPolicy exp = new ExponentialBackoffWithJitter(
                10, 2 * 1000, 2 * 1000, 2 * 1000, false);

        // assert
        assertEquals(10, Deencapsulation.getField(exp, "retryCount"));
        assertEquals(2 * 1000L, Deencapsulation.getField(exp, "minBackoff"));
        assertEquals(2 * 1000L, Deencapsulation.getField(exp, "maxBackoff"));
        assertEquals(2 * 1000L, Deencapsulation.getField(exp, "deltaBackoff"));
        assertFalse((boolean)Deencapsulation.getField(exp, "firstFastRetry"));
    }

    // Tests_SRS_EXPONENTIALBACKOFF_28_006: [Constructor should have default values retryCount, minBackoff, maxBackoff, deltaBackoff and firstFastRetry]
    @Test
    public void constructorHaveDefaultValues()
    {
        //act
        final RetryPolicy exp = new ExponentialBackoffWithJitter();

        // assert
        assertEquals(Integer.MAX_VALUE, Deencapsulation.getField(exp, "retryCount"));
        assertEquals(100L, Deencapsulation.getField(exp, "minBackoff"));
        assertEquals(10 * 1000L, Deencapsulation.getField(exp, "maxBackoff"));
        assertEquals(100L, Deencapsulation.getField(exp, "deltaBackoff"));
        assertTrue((boolean)Deencapsulation.getField(exp, "firstFastRetry"));
    }

    // Tests_SRS_EXPONENTIALBACKOFF_28_003: [The function shall indicate immediate retry on first retry if firstFastRetry is true]
    @Test
    public void shouldRetryResultWithFirstFastRetry()
    {
        // arrange
        final RetryPolicy exp = new ExponentialBackoffWithJitter(
            10, 100, 10 * 1000, 100, true);
        RetryDecision expected = new RetryDecision(true, 0);

        // act
        RetryDecision actual = exp.getRetryDecision(0, null);

        //assert
        assertTrue(actual.shouldRetry());
        assertEquals(0, actual.getDuration());
    }

    // Tests_SRS_EXPONENTIALBACKOFF_28_004: [The function shall return non-zero wait time on first retry if firstFastRetry is false]
    @Test
    public void shouldRetryResultWithNoFirstFastRetry()
    {
        // arrange
        final RetryPolicy exp = new ExponentialBackoffWithJitter(
                10, 100, 10 * 1000, 100, false);

        // act
        RetryDecision actual = exp.getRetryDecision(0, null);

        //assert
        assertTrue(actual.shouldRetry());
        assertTrue(actual.getDuration() > 0);
    }

    // Tests_SRS_EXPONENTIALBACKOFF_28_005: [The function shall return waitTime according to
    //      F(x) = min(Cmin+ (2^(x-1)-1) * rand(C * (1 – Jd), C*(1-Ju)), Cmax) where  x is the xth retry.]
    @Test
    public void shouldRetryResultWithOnlyCurrentRetryCount()
    {
        // arrange
        final RetryPolicy exp = new ExponentialBackoffWithJitter(
                10, 0, 0, 0, true);
        Deencapsulation.setField(exp, "random", mockedRandom);

        new NonStrictExpectations()
        {
            {
                mockedRandom.nextInt(anyInt);
                result = 0;
            }
        };

        // act
        RetryDecision actual = exp.getRetryDecision(1, null);

        //assert
        assertTrue(actual.shouldRetry());
        assertEquals(0, actual.getDuration());
    }

    // Tests_SRS_EXPONENTIALBACKOFF_28_005: [The function shall return waitTime according to
    //      F(x) = min(Cmin+ (2^(x-1)-1) * rand(C * (1 – Jd), C*(1-Ju)), Cmax) where  x is the xth retry.]
    @Test
    public void shouldRetryResultWithMinMaxBackOffReturnsMinBackOff()
    {
        // arrange
        final RetryPolicy exp = new ExponentialBackoffWithJitter(
                10, 10, 100, 0, true);
        Deencapsulation.setField(exp, "random", mockedRandom);

        new NonStrictExpectations()
        {
            {
                mockedRandom.nextInt(anyInt);
                result = 0;
            }
        };

        // act
        RetryDecision actual = exp.getRetryDecision(1, null);

        //assert
        assertTrue(actual.shouldRetry());
        assertEquals(10, actual.getDuration());
    }

    // Tests_SRS_EXPONENTIALBACKOFF_28_005: [The function shall return waitTime according to
    //      F(x) = min(Cmin+ (2^(x-1)-1) * rand(C * (1 – Jd), C*(1-Ju)), Cmax) where  x is the xth retry.]
    @Test
    public void shouldRetryResultWithLargeCurrentRetryCountReturnsMaxBackOff()
    {
        // arrange
        final RetryPolicy exp = new ExponentialBackoffWithJitter(
                10000, 10, 100, 10, true);
        Deencapsulation.setField(exp, "random", mockedRandom);

        new NonStrictExpectations()
        {
            {
                mockedRandom.nextInt(anyInt);
                result = 0;
            }
        };

        // act
        RetryDecision actual = exp.getRetryDecision(999, null);

        //assert
        assertTrue(actual.shouldRetry());
        assertEquals(100, actual.getDuration());
    }

    // Tests_SRS_EXPONENTIALBACKOFF_28_005: [The function shall return waitTime according to
    //      F(x) = min(Cmin+ (2^(x-1)-1) * rand(C * (1 – Jd), C*(1-Ju)), Cmax) where  x is the xth retry.]
    @Test
    public void shouldRetryResultRetry1stTime()
    {
        // arrange
        final RetryPolicy exp = new ExponentialBackoffWithJitter(
                10000, 10, 100, 10, true);
        final double deltaBackoffLowBound = 10 * 0.8;
        final int count = 2;
        Deencapsulation.setField(exp, "random", mockedRandom);

        new NonStrictExpectations()
        {
            {
                mockedRandom.nextInt(anyInt);
                result = 0;
            }
        };

        // act
        RetryDecision actual = exp.getRetryDecision(count, null);

        //assert
        int expected = (int)((Math.pow(2.0, (double)count) - 1.0) * deltaBackoffLowBound) + 10;
        assertTrue(actual.shouldRetry());
        assertEquals(expected, actual.getDuration());
    }
}


