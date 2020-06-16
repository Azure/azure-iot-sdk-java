/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.helpers;

import com.microsoft.azure.sdk.iot.common.helpers.rules.*;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * Common rules and flags for all integration tests
 */
public abstract class IntegrationTest
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

    // Need to define all the rules here so that every integration test validates that it should run.
    @Rule
    public BasicTierHubOnlyTestRule basicTierHubOnlyTestRule = new BasicTierHubOnlyTestRule();

    @Rule
    public StandardTierHubOnlyTestRule standardTierHubOnlyTestRule = new StandardTierHubOnlyTestRule();

    @Rule
    public IotHubTestRule iotHubTestRule = new IotHubTestRule();

    @Rule
    public DeviceProvisioningServiceTestRule deviceProvisioningServiceTestRule = new DeviceProvisioningServiceTestRule();

    @Rule
    public ContinuousIntegrationTestRule continuousIntegrationTestRule = new ContinuousIntegrationTestRule();

    @Rule
    public FlakeyTestRule flakeyTestRule = new FlakeyTestRule();

    public static boolean isBasicTierHub;
    public static boolean isPullRequest;

    //By default, run all tests. Even if env vars aren't set
    public static boolean runIotHubTests = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue("RUN_IOTHUB_TESTS", "true"));
    public static boolean runProvisioningTests = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue("RUN_PROVISIONING_TESTS", "true"));

}
