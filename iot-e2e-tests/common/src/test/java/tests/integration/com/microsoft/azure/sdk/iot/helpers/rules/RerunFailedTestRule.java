package tests.integration.com.microsoft.azure.sdk.iot.helpers.rules;

import lombok.extern.slf4j.Slf4j;
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
            for (int i = 0; i < NUMBER_OF_RETRIES; i++)
            {
                try
                {
                    base.evaluate();
                }
                catch (Throwable e)
                {
                    if (i == NUMBER_OF_RETRIES - 1)
                    {
                        log.info("Test failed on final rerun. Not rerunning this test anymore");
                        throw e;
                    }
                    else
                    {
                        log.info("Test failed on run {} with error {}. Rerunning the test.", i, e.getMessage());
                    }
                }
            }
        }
    }
}
