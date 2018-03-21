// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.device.transport.RetryDecision;
import com.microsoft.azure.sdk.iot.device.transport.NoRetry;
import java.util.Arrays;
import java.util.Collection;
import javafx.util.Duration;
import org.junit.Before;
import org.junit.runners.Parameterized;
import org.junit.runner.RunWith;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class NoRetryTest
{
    private int currentRetryCount;
    private Exception lastException;
    private NoRetry retryNoRetry;

    public NoRetryTest(int currentRetryCount, Exception lastException)
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
                {1, new IotHubException()},
        });
    }

    // Tests_SRS_NORETRY_28_001: [The function shall return the false and 0 as the RetryDecision despite on inputs.]
    @Test
    public void VerifyShouldRetryResult()
    {
        // arrange
        RetryDecision expected = new RetryDecision(false, Duration.UNKNOWN);

        // act
        RetryDecision actual = retryNoRetry.ShouldRetry(this.currentRetryCount, this.lastException);

        // assert
        assertEquals(expected.getShouldRetry(), actual.getShouldRetry());
        assertEquals(expected.getDuration(), actual.getDuration());
    }
}
