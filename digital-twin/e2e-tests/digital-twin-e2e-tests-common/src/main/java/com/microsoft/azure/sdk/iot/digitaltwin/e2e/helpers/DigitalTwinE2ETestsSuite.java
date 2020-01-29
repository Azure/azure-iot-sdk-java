package com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers;

import com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests.DigitalTwinCommandE2ETests;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests.DigitalTwinPropertiesE2ETests;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests.DigitalTwinRegisterInterfacesE2ETests;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests.DigitalTwinServiceClientE2ETests;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests.DigitalTwinTelemetryE2ETests;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests.DigitalTwinTelemetryParameterizedE2ETests;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import static org.junit.Assume.assumeTrue;

@RunWith(Suite.class)
@Suite.SuiteClasses( {DigitalTwinServiceClientE2ETests.class,
        DigitalTwinRegisterInterfacesE2ETests.class,
        DigitalTwinTelemetryE2ETests.class,
        DigitalTwinTelemetryParameterizedE2ETests.class,
        DigitalTwinPropertiesE2ETests.class,
        DigitalTwinCommandE2ETests.class})
public class DigitalTwinE2ETestsSuite {
    public static boolean runDigitalTwinTests = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue("RUN_DIGITAL_TWIN_TESTS", "true"));

    @Before
    public void checkIfTestShouldBeRun() {
        assumeTrue(runDigitalTwinTests);
    }
}
