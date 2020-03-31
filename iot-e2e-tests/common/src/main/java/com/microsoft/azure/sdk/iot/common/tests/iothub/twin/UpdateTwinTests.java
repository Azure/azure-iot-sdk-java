/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.tests.iothub.twin;

import com.microsoft.azure.sdk.iot.common.helpers.ConditionalIgnoreRule;
import com.microsoft.azure.sdk.iot.common.helpers.StandardTierOnlyRule;
import com.microsoft.azure.sdk.iot.common.helpers.Tools;
import com.microsoft.azure.sdk.iot.common.tests.iothub.serviceclient.ServiceClientTests;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.devicetwin.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubPreconditionFailedException;
import org.junit.Test;
import org.junit.runners.Parameterized;

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

    public UpdateTwinTests(IotHubServiceClientProtocol protocol) {
        this.testInstance = new ServiceClientITRunner(protocol);
    }

    private class ServiceClientITRunner {
        private IotHubServiceClientProtocol protocol;
        private String deviceId;

        public ServiceClientITRunner(IotHubServiceClientProtocol protocol) {
            this.protocol = protocol;
            this.deviceId = deviceIdPrefix.concat("-" + UUID.randomUUID().toString());

        }
    }

    private ServiceClientITRunner testInstance;

    @Parameterized.Parameters(name = "{0}")
    public static Collection inputsCommon() throws IOException {
        List inputs = Arrays.asList(
                new Object[][]
                        {
                                {IotHubServiceClientProtocol.AMQPS},
                                {IotHubServiceClientProtocol.AMQPS_WS}
                        }
        );

        return inputs;
    }

    @Test(expected = IotHubPreconditionFailedException.class)
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void UpdateTwin_WithMismatchingEtag_ExceptionThrown() throws IOException, InterruptedException, IotHubException, GeneralSecurityException, URISyntaxException, ModuleClientException {
        // setup a new device in IoT Hub
        RegistryManager registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
        Device deviceAdded = Device.createFromId(testInstance.deviceId, null, null);
        Tools.addDeviceWithRetry(registryManager, deviceAdded);

        // create the twin client and device
        DeviceTwin twinClient = DeviceTwin.createFromConnectionString(iotHubConnectionString);
        DeviceTwinDevice deviceTwin = new DeviceTwinDevice(testInstance.deviceId);

        // change the properties of the device without specifying an etag
        changeDesiredProperties(twinClient, deviceTwin, null);

        // change the properties of the device with speciying an etag that mismatches the cloud twin
        changeDesiredProperties(twinClient, deviceTwin, "XXXXXXXXXXXX");

        registryManager.removeDevice(testInstance.deviceId);
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
