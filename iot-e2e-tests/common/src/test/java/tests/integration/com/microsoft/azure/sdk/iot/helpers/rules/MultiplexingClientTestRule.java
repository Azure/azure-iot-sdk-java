package tests.integration.com.microsoft.azure.sdk.iot.helpers.rules;

import com.microsoft.azure.sdk.iot.device.exceptions.MultiplexingClientDeviceRegistrationAuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;

@Slf4j
public class MultiplexingClientTestRule implements TestRule
{
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
        public void evaluate() throws Throwable {
            try
            {
                base.evaluate();
            }
            catch (MultiplexingClientDeviceRegistrationAuthenticationException e)
            {
                // This particular exception contains a map of deviceId -> exception that further elaborates why a given test failed and threw this exception
                log.error("Multiplexing client test threw a MultiplexingClientDeviceRegistrationAuthenticationException");
                for (String deviceId : e.getRegistrationExceptions().keySet())
                {
                    // rather than just logging the stacktrace of e, log the error messages of each of e's children
                    // exceptions since they have more context on why authentication failed for each device
                    log.error("Error for device {} was {}", deviceId, e.getRegistrationExceptions().get(deviceId).getMessage());
                }

                // rethrow the exception as is after printing its details
                throw e;
            }
        }
    }
}
