package tests.integration.com.microsoft.azure.sdk.iot.helpers.rules;

import lombok.extern.slf4j.Slf4j;
import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Test rule that allows for us to configure how many times to rerun a particular test until it passes
 */
@Slf4j
public class RerunFailedTestRule implements TestRule
{
    private static final int NUMBER_OF_RETRIES = 3; // how many times a single test will be retried if it failed the first time

    @Override
    public Statement apply(Statement base, Description description) {
        return new MultiplexingClientTestStatement(base, description);
    }

    private static class MultiplexingClientTestStatement extends Statement {

        private final Statement base;

        public MultiplexingClientTestStatement(Statement base, Description description) {
            this.base = base;
        }

        @Override
        public void evaluate() throws Throwable
        {
            int attempt = 0;
            while (true)
            {
                try
                {
                    attempt++;
                    base.evaluate();
                    return; // if the test passes, no need to rerun the test
                }
                catch (AssumptionViolatedException e)
                {
                    // This exception is thrown when an assumption isn't satisfied (for instance,
                    // "assumeTrue(protocol == HTTPS)" when protocol is AMQPS). In cases like these,
                    // there is no need to rerun as the test should be skipped. JUnit understands that
                    // a thrown AssumptionViolatedException means that the test was successfully skipped.
                    throw e;
                }
                catch (Throwable e)
                {
                    if (attempt >= NUMBER_OF_RETRIES)
                    {
                        log.info("Test failed on final rerun. Not rerunning this test anymore");
                        throw e;
                    }
                    else
                    {
                        log.info("Test failed on run {} with error {}. Rerunning the test.", attempt, e.getMessage());
                    }
                }
            }
        }
    }
}
