// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

import org.junit.Test;
import static org.junit.Assert.*;

public class RetryDecisionTest
{
    // Tests_SRS_RETRYDECISION_28_001: [The constructor shall save the duration and getRetryDecision]
    // Tests_SRS_RETRYDECISION_28_002: [The function shall return the value of getRetryDecision]
    // Tests_SRS_RETRYDECISION_28_003: [The function shall return the value of duration]
    @Test
    public void constructorSavesParameters()
    {
        //act
        final RetryDecision retryDecisionTest = new RetryDecision(true, 10);

        // assert
        assertTrue(retryDecisionTest.shouldRetry());
        assertEquals(10, retryDecisionTest.getDuration());
    }
}
