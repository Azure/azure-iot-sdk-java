/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class Rerun implements TestRule
{
    private int count;

    public Rerun(int count)
    {
        this.count = count;
    }

    public Statement apply(Statement base, Description description)
    {
        return statement(base, description);
    }

    private Statement statement(final Statement base, final Description description)
    {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Throwable lastThrowable = null;

                // implement retry logic here
                for (int i = 0; i < count; i++) {
                    try {
                        base.evaluate();
                        return;
                    } catch (Throwable t) {
                        lastThrowable = t;
                        System.err.println(description.getDisplayName() + ": Failed run " + (i+1) + " with throwable " + Tools.getStackTraceFromThrowable(t));
                    }
                }
                System.err.println(description.getDisplayName() + ": Test failed after " + count + " failures");
                throw lastThrowable;
            }
        };
    }
}
