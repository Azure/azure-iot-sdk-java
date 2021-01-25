package tests.integration.com.microsoft.azure.sdk.iot.helpers.rules;

import com.microsoft.azure.sdk.iot.service.exceptions.IotHubTooManyRequestsException;
import lombok.extern.slf4j.Slf4j;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * On lower tier IoT Hubs, this test project often hits throttling limits. Rather than add retry logic to every call
 * to the hub, this rule will catch these throttling errors and re-run the test until it passes or fails for non-throttling
 * reasons. It has a configurable delay between each re-run to give IoT Hub time to free up some throughput
 */
@Slf4j
public class ThrottleResistantTestRule implements TestRule
{
    final static String CONNECTION_REFUSED = "connection refused";
    final static int THROTTLING_RETRY_DELAY_MILLISECONDS = 10 * 1000;

    @Override
    public Statement apply(Statement base, Description description) {
        return new ThrottleResistantStatement(base, description);
    }

    private static class ThrottleResistantStatement extends Statement {

        private final Statement base;
        private final Description description;

        public ThrottleResistantStatement(Statement base, Description description) {
            this.base = base;
            this.description = description;
        }

        @Override
        public void evaluate() throws Throwable {
            boolean ranWithoutThottling = false;
            while (!ranWithoutThottling)
            {
                try
                {
                    base.evaluate();
                    ranWithoutThottling = true;
                }
                catch (IotHubTooManyRequestsException e)
                {
                    log.warn("Thottling detected in test {}, waiting for {} milliseconds and then re-running the test", description.getMethodName(), THROTTLING_RETRY_DELAY_MILLISECONDS, e);
                    Thread.sleep(THROTTLING_RETRY_DELAY_MILLISECONDS);
                }
                catch (Exception e)
                {
                    if (e.getMessage() != null && e.getMessage().toLowerCase().contains(CONNECTION_REFUSED))
                    {
                        log.warn("Thottling detected in test {}, waiting for {} milliseconds and then re-running the test", description.getMethodName(), THROTTLING_RETRY_DELAY_MILLISECONDS, e);
                        Thread.sleep(THROTTLING_RETRY_DELAY_MILLISECONDS);
                    }
                    else
                    {
                        // If throw exception wasn't about throttling, then re-throw it
                        throw e;
                    }
                }
            }
        }
    }
}