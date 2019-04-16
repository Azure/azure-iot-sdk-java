/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.helpers;

import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.rules.Timeout;
import org.junit.runner.Description;

public class IntegrationTest
{
    @Rule
    public TestRule watcher = new TestWatcher()
    {
        protected void starting(Description description)
        {
            System.out.println("Starting test: " + description.getMethodName());
        }

        protected void finished(Description description)
        {
            System.out.println("Finished test: " + description.getMethodName());
        }
    };

    @Rule
    public final ConditionalIgnoreRule mConditionalIgnore = new ConditionalIgnoreRule();

    public static final int E2E_TEST_TIMEOUT_MS = 15 * 60 * 1000;

    //This timeout applies to all individual tests in classes that inherit from this class
    @Rule
    public Timeout timeout = new Timeout(E2E_TEST_TIMEOUT_MS);

    public static boolean isBasicTierHub;
}
