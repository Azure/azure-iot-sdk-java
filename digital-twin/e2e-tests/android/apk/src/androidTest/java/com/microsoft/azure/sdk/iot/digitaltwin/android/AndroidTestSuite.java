/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.digitaltwin.android;

import com.microsoft.azure.sdk.iot.digitaltwin.android.helper.TestGroup40;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests.DigitalTwinCommandE2ETests;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests.DigitalTwinPropertiesE2ETests;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests.DigitalTwinRegisterInterfacesE2ETests;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests.DigitalTwinServiceClientE2ETests;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests.DigitalTwinTelemetryE2ETests;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests.DigitalTwinTelemetryParameterizedE2ETests;

import org.junit.Rule;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@TestGroup40
@RunWith(Suite.class)
@Suite.SuiteClasses( {DigitalTwinServiceClientE2ETests.class,
        DigitalTwinRegisterInterfacesE2ETests.class,
        DigitalTwinTelemetryE2ETests.class,
        DigitalTwinTelemetryParameterizedE2ETests.class,
        DigitalTwinPropertiesE2ETests.class,
        DigitalTwinCommandE2ETests.class})
public class AndroidTestSuite {
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5 * 60); // 5 minutes max per method tested
}
