package com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.Timeout;
import org.junit.Ignore;

import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.retrieveEnvironmentVariableValue;
import static org.junit.Assume.assumeTrue;

@Ignore
public class DigitalTwinE2ETests {
    private static boolean runDigitalTwinTests = Boolean.parseBoolean(retrieveEnvironmentVariableValue("RUN_DIGITAL_TWIN_TESTS", "true"));
    private static boolean isBasicTier = Boolean.parseBoolean(retrieveEnvironmentVariableValue("IS_BASIC_TIER_HUB"));

    @Rule
    public Timeout globalTimeout = Timeout.seconds(60); // 1 minute max per method tested

    @BeforeClass
    public static void checkIfTestShouldBeRun() {

        // Will run the tests if the flag to run Digital Twin E2E tests is set to true, and the hub being tested against is not a basic tier hub
        assumeTrue(runDigitalTwinTests && !isBasicTier);
    }
}
