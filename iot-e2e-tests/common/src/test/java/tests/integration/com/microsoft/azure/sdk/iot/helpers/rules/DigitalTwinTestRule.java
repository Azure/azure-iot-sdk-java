// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.integration.com.microsoft.azure.sdk.iot.helpers.rules;

import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.DigitalTwinTest;

public class DigitalTwinTestRule implements TestRule {
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
            DigitalTwinTest methodAnnotation = description.getAnnotation(DigitalTwinTest.class);
            DigitalTwinTest classAnnotation = description.getTestClass().getAnnotation(DigitalTwinTest.class);
            if (methodAnnotation != null || classAnnotation != null) {
                Assume.assumeTrue("Test is ignored", IntegrationTest.runDigitalTwinTests);
            }

            base.evaluate();
        }
    }
}
