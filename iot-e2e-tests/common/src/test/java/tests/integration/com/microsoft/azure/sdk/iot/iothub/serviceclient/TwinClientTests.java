// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.iothub.serviceclient;

import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.twin.Pair;
import com.microsoft.azure.sdk.iot.service.twin.Twin;
import com.microsoft.azure.sdk.iot.service.twin.TwinClient;
import com.microsoft.azure.sdk.iot.service.twin.TwinClientOptions;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestConstants;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestDeviceIdentity;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static junit.framework.TestCase.*;

@Slf4j
@IotHubTest
public class TwinClientTests extends IntegrationTest
{
    protected static String iotHubConnectionString = "";
    private static final TwinClientOptions twinClientOptions = TwinClientOptions.builder().httpReadTimeoutSeconds(HTTP_READ_TIMEOUT).build();

    @BeforeClass
    public static void setUp() throws IOException
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);

        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));
    }

    @Test
    public void testTwinTags() throws IOException, GeneralSecurityException, IotHubException, URISyntaxException
    {
        TestDeviceIdentity testDeviceIdentity = Tools.getTestDevice(iotHubConnectionString, IotHubClientProtocol.AMQPS, AuthenticationType.SAS, true);
        TwinClient twinClient = new TwinClient(iotHubConnectionString, twinClientOptions);
        Twin twin = twinClient.get(testDeviceIdentity.getDeviceId());

        String expectedTagKey = UUID.randomUUID().toString();
        String expectedTagValue = UUID.randomUUID().toString();

        Set<Pair> tags = new HashSet<>();
        tags.add(new Pair(expectedTagKey, expectedTagValue));
        twin.setTags(tags);

        twinClient.patch(twin);
        twin = twinClient.get(testDeviceIdentity.getDeviceId());

        assertNotNull(twin);
        assertNotNull(twin.getTags());
        assertEquals(1, twin.getTags().size());

        for (Pair pair : twin.getTags())
        {
            String actualTagKey = pair.getKey();
            String actualTagValue = (String) pair.getValue();

            assertEquals(expectedTagKey, actualTagKey);
            assertEquals(expectedTagValue, actualTagValue);
        }
    }
}
