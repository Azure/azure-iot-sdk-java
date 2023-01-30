// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.runners.Parameterized;
import org.junit.runner.RunWith;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class NoRetryTest
{
    private final int currentRetryCount;
    private final TransportException lastException;
    private NoRetry retryNoRetry;

    public NoRetryTest(int currentRetryCount, TransportException lastException)
    {
        super();
        this.currentRetryCount = currentRetryCount;
        this.lastException = lastException;
    }

    @Before
    public void initialize()
    {
        retryNoRetry = new NoRetry();
    }

    @Parameterized.Parameters
    public static Collection inputs()
    {
        return Arrays.asList(new Object[][] {
                {0, null},
                {1, null},
                {-1, null},
                {1, new TransportException()},
        });
    }

    // Tests_SRS_NORETRY_28_001: [The function shall return the false and 0 as the RetryDecision despite on inputs.]
    @Test
    public void VerifyShouldRetryResult()
    {
        // arrange
        RetryDecision expected = new RetryDecision(false, 0);

        // act
        RetryDecision actual = retryNoRetry.getRetryDecision(this.currentRetryCount, this.lastException);

        // assert
        assertEquals(expected.shouldRetry(), actual.shouldRetry());
        assertEquals(expected.getDuration(), actual.getDuration());
    }
}
