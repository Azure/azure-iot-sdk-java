/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.helpers;


import lombok.extern.slf4j.Slf4j;
import org.junit.Rule;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.rules.Timeout;
import org.junit.runner.Description;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.rules.*;

/**
 * Common rules and flags for all integration tests
 */
@Slf4j
public abstract class IntegrationTest
{
    @Rule
    public TestRule watcher = new TestWatcher()
    {
        protected void starting(Description description)
        {
            log.info("Starting test: {}", description.getMethodName());
        }

        protected void finished(Description description)
        {
            log.info("Finished test: {}", description.getMethodName());
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
    public DigitalTwinTestRule digitalTwinTestRule = new DigitalTwinTestRule();

    @Rule
    public ContinuousIntegrationTestRule continuousIntegrationTestRule = new ContinuousIntegrationTestRule();

    @Rule
    public FlakeyTestRule flakeyTestRule = new FlakeyTestRule();

    @Rule
    public MultiplexingClientTestRule multiplexingClientTestRule = new MultiplexingClientTestRule();

    @Rule
    public ErrInjTestRule errInjTestRule = new ErrInjTestRule();

    // The order of these two rules matters since the throttle resistant test rule will rerun tests "for free" if they
    // encounter a throttling exception, but the RerurnFailedTestRule will rerun a failed test only up to X times if it encounters
    // any exception. With the below order, a test can fail any number of times due to throttling without it counting
    // towards the X allowed retries defined in the RerunFailedTestRule. The overall test timeout rule will prevent
    // a test from infinitely retrying on throttling errors.
    @Rule
    public RuleChain testRerunRuleChain = RuleChain.outerRule(new RerunFailedTestRule())
        .around(new ThrottleResistantTestRule());

    int E2E_TEST_TIMEOUT_MILLISECONDS = 5 * 60 * 1000;

    // Each test must finish in under 5 minutes. Only the token renewal test should last longer,
    // but that test overrides this value to fit its needs as a very long test.
    @Rule
    public Timeout timeout = new Timeout(E2E_TEST_TIMEOUT_MILLISECONDS);

    public static boolean isBasicTierHub;
    public static boolean isPullRequest;

    //By default, run all tests. Even if env vars aren't set
    public static boolean runIotHubTests = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue("RUN_IOTHUB_TESTS", "true"));
    public static boolean runErrInjTests = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue("RUN_ERRINJ_TESTS", "true"));
    public static boolean runProvisioningTests = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue("RUN_PROVISIONING_TESTS", "true"));
    public static boolean runDigitalTwinTests = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue("RUN_DIGITAL_TESTS", "true"));

    // Determines if the tear down for a given test should delete the device identity, or recycle it so that another test can use it
    public static boolean recycleIdentities = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue("RECYCLE_TEST_IDENTITIES", "false"));

    // Infinite read timeout for all http operations
    public static int HTTP_READ_TIMEOUT = 0;

    // Amqp specific timeout values for waiting on authentication/device sessions to open
    public static int AMQP_AUTHENTICATION_SESSION_TIMEOUT_SECONDS = 4 * 60;
    public static int AMQP_DEVICE_SESSION_TIMEOUT_SECONDS = 4 * 60;

    public static X509CertificateGenerator x509CertificateGenerator = new X509CertificateGenerator();
}
