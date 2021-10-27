package tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.rules;

import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;

public class LonghaulTestRule implements TestRule
{
    @Override
    public Statement apply(Statement base, Description description)
    {
        return new IgnorableStatement(base, description);
    }

    private static class IgnorableStatement extends Statement
    {
        private final Statement base;

        public IgnorableStatement(Statement base, Description description)
        {
            this.base = base;
        }

        @Override
        public void evaluate() throws Throwable
        {
            boolean runLonghaulTests = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue("RUN_LONGHAUL_TESTS", "false"));
            Assume.assumeTrue("Test is ignored", runLonghaulTests);
            base.evaluate();
        }
    }
}
