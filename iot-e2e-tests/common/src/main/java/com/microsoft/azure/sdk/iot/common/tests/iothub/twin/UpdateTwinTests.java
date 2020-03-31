/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.tests.iothub.twin;

import com.microsoft.azure.sdk.iot.common.helpers.ConditionalIgnoreRule;
import com.microsoft.azure.sdk.iot.common.helpers.StandardTierOnlyRule;
import com.microsoft.azure.sdk.iot.common.helpers.Tools;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.devicetwin.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubPreconditionFailedException;

import org.junit.Test;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * Test class containing all tests to be run on JVM and android pertaining to Queries. Class needs to be extended
 * in order to run these tests as that extended class handles setting connection strings and certificate generation
 */
public class UpdateTwinTests
{
    protected static String iotHubConnectionString = "";
    private static String deviceIdPrefix = "java-service-client-e2e-test";
    private static final int TEMPERATURE_RANGE = 100;
    private static final int HUMIDITY_RANGE = 100;

    @Test(expected = IotHubPreconditionFailedException.class)
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void UpdateTwin_WithMismatchingEtag_ExceptionThrown() throws IOException, InterruptedException, IotHubException, GeneralSecurityException, URISyntaxException, ModuleClientException {
        // setup a new device in IoT Hub
        String deviceId = deviceIdPrefix.concat("-" + UUID.randomUUID().toString());
        RegistryManager registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
        Device deviceAdded = Device.createFromId(deviceId, null, null);
        Tools.addDeviceWithRetry(registryManager, deviceAdded);

        // create the twin client and device
        DeviceTwin twinClient = DeviceTwin.createFromConnectionString(iotHubConnectionString);
        DeviceTwinDevice deviceTwin = new DeviceTwinDevice(deviceId);

        // change the properties of the device without specifying an etag
        changeDesiredProperties(twinClient, deviceTwin, null);

        // change the properties of the device with speciying an etag that mismatches the cloud twin
        changeDesiredProperties(twinClient, deviceTwin, "XXXXXXXXXXXX");

        registryManager.removeDevice(deviceId);
    }

    private static void changeDesiredProperties(DeviceTwin twinClient, DeviceTwinDevice device, String etag) throws IOException, IotHubException {
        Set<Pair> desiredProperties = new HashSet<Pair>();
        desiredProperties.add(new Pair("temp", new Random().nextInt(TEMPERATURE_RANGE)));
        desiredProperties.add(new Pair("hum", new Random().nextInt(HUMIDITY_RANGE)));
        device.setDesiredProperties(desiredProperties);

        // if an etag is provided, use it
        if (etag != null) {
            device.setETag(etag);
        }

        twinClient.updateTwin(device);
    }
}
