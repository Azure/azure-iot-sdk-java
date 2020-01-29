package com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.retrieveEnvironmentVariableValue;
import static org.junit.Assume.assumeTrue;

@RunWith(Suite.class)
@Suite.SuiteClasses( {DigitalTwinServiceClientE2ETests.class,
        DigitalTwinRegisterComponentsE2ETests.class,
        DigitalTwinTelemetryE2ETests.class,
        DigitalTwinTelemetryParameterizedE2ETests.class,
        DigitalTwinPropertiesE2ETests.class,
        DigitalTwinCommandE2ETests.class})
public class DigitalTwinE2ETestsSuite {
    private static boolean runDigitalTwinTests = Boolean.parseBoolean(retrieveEnvironmentVariableValue("RUN_DIGITAL_TWIN_TESTS", "true"));
    private static boolean isBasicTier = Boolean.parseBoolean(retrieveEnvironmentVariableValue("RUN_DIGITAL_TWIN_TESTS"));

    @BeforeClass
    public static void checkIfTestShouldBeRun() {

        // Will run the tests if the flag to run Digital Twin E2E tests is set to true, and the hub being tested against is not a basic tier hub
        assumeTrue(runDigitalTwinTests && !isBasicTier);
    }
}
