// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.transport.RetryDecision;
import javafx.util.Duration;
import org.junit.Test;
import static org.junit.Assert.*;

public class RetryDecisionTest
{
    // Tests_SRS_RETRYDECISION_28_001: [The constructor shall save the duration and shouldRetry]
    // Tests_SRS_RETRYDECISION_28_002: [The function shall return the value of shouldRetry]
    // Tests_SRS_RETRYDECISION_28_003: [The function shall return the value of duration]
    @Test
    public void constructorSavesParameters()
    {
        //act
        final RetryDecision retryDecisionTest = new RetryDecision(true, Duration.millis(10));

        // assert
        assertTrue(retryDecisionTest.getShouldRetry());
        assertEquals(Duration.millis(10), retryDecisionTest.getDuration());
    }
}
