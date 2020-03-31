/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.tests.iothub.twin;

import com.microsoft.azure.sdk.iot.common.helpers.ClientType;
import com.microsoft.azure.sdk.iot.common.helpers.ConditionalIgnoreRule;
import com.microsoft.azure.sdk.iot.common.helpers.StandardTierOnlyRule;
import com.microsoft.azure.sdk.iot.common.setup.iothub.DeviceTwinCommon;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.devicetwin.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubPreconditionFailedException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * Test class containing all tests to be run on JVM and android pertaining to Queries. Class needs to be extended
 * in order to run these tests as that extended class handles setting connection strings and certificate generation
 */
public class UpdateTwinTests extends DeviceTwinCommon
{
    public UpdateTwinTests(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint)
    {
        super(protocol, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint);
    }

    @Before
    public void setUpNewDeviceAndModule() throws IOException, IotHubException, URISyntaxException, InterruptedException, ModuleClientException, GeneralSecurityException
    {
        super.setUpNewDeviceAndModule();
    }

    @Test (expected = IotHubPreconditionFailedException.class)
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void UpdateTwin_WithMismatchingEtag_ExceptionThrown() throws IOException, InterruptedException, IotHubException, GeneralSecurityException, URISyntaxException, ModuleClientException
    {
        final int numOfDevices = 1;

        addMultipleDevices(numOfDevices);

        // set desired properties without specifying an etag
        String queryProperty = PROPERTY_KEY_QUERY + UUID.randomUUID().toString();
        String queryPropertyValue = PROPERTY_VALUE_QUERY + UUID.randomUUID().toString();
        setDesiredProperties(queryProperty, queryPropertyValue, numOfDevices, null);

        Thread.sleep(DESIRED_PROPERTIES_PROPAGATION_TIME_MILLIS);

        // set desired properties with specifying an etag that mismatches the cloud twin
        queryProperty = PROPERTY_KEY_QUERY + UUID.randomUUID().toString();
        queryPropertyValue = PROPERTY_VALUE_QUERY + UUID.randomUUID().toString();
        setDesiredProperties(queryProperty, queryPropertyValue, numOfDevices, "XXXXXXXXXXXX");

        removeMultipleDevices(MAX_DEVICES);
    }

    public void setDesiredProperties(String queryProperty, String queryPropertyValue, int numberOfDevices, String etag) throws IOException, IotHubException
    {
        for (int i = 0; i < numberOfDevices; i++)
        {
            Set<Pair> desiredProperties = new HashSet<>();
            desiredProperties.add(new Pair(queryProperty, queryPropertyValue));
            devicesUnderTest[i].sCDeviceForTwin.setDesiredProperties(desiredProperties);

            // if an etag is provided, use it
            if (etag != null) {
                devicesUnderTest[i].sCDeviceForTwin.setETag(etag);
            }

            sCDeviceTwin.updateTwin(devicesUnderTest[i].sCDeviceForTwin);
            devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }
    }
}
