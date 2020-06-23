package com.microsoft.azure.sdk.iot.common.helpers.rules;

import com.microsoft.azure.sdk.iot.common.helpers.IntegrationTest;
import com.microsoft.azure.sdk.iot.common.helpers.annotations.ContinuousIntegrationTest;
import com.microsoft.azure.sdk.iot.common.helpers.annotations.DeviceProvisioningServiceTest;
import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class DeviceProvisioningServiceTestRule implements TestRule
{
    @Override
    public Statement apply(Statement base, Description description) {
        return new IgnorableStatement(base, description);
    }

    private class IgnorableStatement extends Statement {

        private final Statement base;
        private final Description description;

        public IgnorableStatement(Statement base, Description description) {
            this.base = base;
            this.description = description;
        }

        @Override
        public void evaluate() throws Throwable {
            DeviceProvisioningServiceTest annotation = description.getAnnotation(DeviceProvisioningServiceTest.class);
            DeviceProvisioningServiceTest classAnnotation = description.getTestClass().getAnnotation(DeviceProvisioningServiceTest.class);
            if (annotation != null || classAnnotation != null)
            {
                Assume.assumeTrue("Test is ignored", IntegrationTest.runProvisioningTests);
            }

            base.evaluate();
        }
    }
}
