// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.iothub.serviceclient;

import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubPreconditionFailedException;
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
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.setup.TwinCommon;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static junit.framework.TestCase.*;
import static org.junit.Assert.fail;

@Slf4j
@IotHubTest
@StandardTierHubOnlyTest
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

        twin.getTags().put(expectedTagKey, expectedTagValue);

        twinClient.patch(twin);
        twin = twinClient.get(testDeviceIdentity.getDeviceId());

        assertNotNull(twin);
        assertNotNull(twin.getTags());
        assertEquals(1, twin.getTags().size());

        for (String actualTagKey : twin.getTags().keySet())
        {
            String actualTagValue = (String) twin.getTags().get(actualTagKey);

            assertEquals(expectedTagKey, actualTagKey);
            assertEquals(expectedTagValue, actualTagValue);
        }
    }

    @Test
    public void testPatchTwin() throws IOException, GeneralSecurityException, IotHubException, URISyntaxException
    {
        TestDeviceIdentity testDeviceIdentity = Tools.getTestDevice(iotHubConnectionString, IotHubClientProtocol.AMQPS, AuthenticationType.SAS, true);
        TwinClient twinClient = new TwinClient(iotHubConnectionString, twinClientOptions);
        Twin twin = twinClient.get(testDeviceIdentity.getDeviceId());

        String expectedDesiredPropertyKey = UUID.randomUUID().toString();
        String expectedDesiredPropertyValue = UUID.randomUUID().toString();

        twin.getDesiredProperties().put(expectedDesiredPropertyKey, expectedDesiredPropertyValue);

        // act
        twinClient.patch(twin);

        // assert
        twin = twinClient.get(testDeviceIdentity.getDeviceId());
        assertTrue(TwinCommon.isPropertyInTwinCollection(twin.getDesiredProperties(), expectedDesiredPropertyKey, expectedDesiredPropertyValue));
    }

    @Test
    public void testReplaceTwin() throws IOException, GeneralSecurityException, IotHubException, URISyntaxException
    {
        TestDeviceIdentity testDeviceIdentity = Tools.getTestDevice(iotHubConnectionString, IotHubClientProtocol.AMQPS, AuthenticationType.SAS, true);
        TwinClient twinClient = new TwinClient(iotHubConnectionString, twinClientOptions);
        Twin twin = twinClient.get(testDeviceIdentity.getDeviceId());

        String desiredPropertyToBeReplacedKey = UUID.randomUUID().toString();
        String desiredPropertyToBeReplacedValue = UUID.randomUUID().toString();

        twin.getDesiredProperties().put(desiredPropertyToBeReplacedKey, desiredPropertyToBeReplacedValue);

        // add some properties to the twin that will be removed when the twinClient.replace call executes
        twinClient.patch(twin);

        twin = twinClient.get(testDeviceIdentity.getDeviceId());
        assertTrue(TwinCommon.isPropertyInTwinCollection(twin.getDesiredProperties(), desiredPropertyToBeReplacedKey, desiredPropertyToBeReplacedValue));

        String expectedDesiredPropertyKey = UUID.randomUUID().toString();
        String expectedDesiredPropertyValue = UUID.randomUUID().toString();

        twin.getDesiredProperties().clear();
        twin.getDesiredProperties().put(expectedDesiredPropertyKey, expectedDesiredPropertyValue);

        // act
        twin = twinClient.replace(twin);

        // assert
        assertFalse("old twin property was not deleted when twin client replaced it", TwinCommon.isPropertyInTwinCollection(twin.getDesiredProperties(), desiredPropertyToBeReplacedKey, desiredPropertyToBeReplacedValue));
        assertTrue("new twin property was not saved when twin client added it using twinClient.replace", TwinCommon.isPropertyInTwinCollection(twin.getDesiredProperties(), expectedDesiredPropertyKey, expectedDesiredPropertyValue));
    }

    @Test
    public void updateDesiredPropertiesThrowsForOutOfDateEtag() throws IOException, InterruptedException, IotHubException, TimeoutException, GeneralSecurityException, URISyntaxException
    {
        TestDeviceIdentity testDeviceIdentity = Tools.getTestDevice(iotHubConnectionString, IotHubClientProtocol.AMQPS, AuthenticationType.SAS, true);
        TwinClient twinClient = new TwinClient(iotHubConnectionString, twinClientOptions);
        Twin twin = twinClient.get(testDeviceIdentity.getDeviceId());

        String initialEtag = twin.getETag();

        final String desiredPropertyKey1 = UUID.randomUUID().toString();
        final String desiredPropertyValue1 = UUID.randomUUID().toString();

        final String desiredPropertyKey2 = UUID.randomUUID().toString();
        final String desiredPropertyValue2 = UUID.randomUUID().toString();

        // send a desired property update and wait for it to be received by the device/module
        twin.getDesiredProperties().put(desiredPropertyKey1, desiredPropertyValue1);
        twin = twinClient.patch(twin);

        // intentionally set an out of date etag to test the precondition failed logic
        twin.setETag(initialEtag);

        try
        {
            //TODO not taking if-match header?
            twin.getDesiredProperties().put(desiredPropertyKey2, desiredPropertyValue2);
            twinClient.patch(twin);
            fail("Expected a IotHubPreconditionFailedException to be thrown since the etag of the twin is out of date");
        }
        catch (IotHubPreconditionFailedException e)
        {
            // expected throw, ignore the exception
        }
    }
}
