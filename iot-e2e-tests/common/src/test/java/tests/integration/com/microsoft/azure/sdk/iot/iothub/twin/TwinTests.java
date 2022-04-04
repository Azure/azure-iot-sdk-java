// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.iothub.twin;

import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.device.twin.GetTwinCorrelatingMessageCallback;
import com.microsoft.azure.sdk.iot.device.twin.ReportedPropertiesUpdateCorrelatingMessageCallback;
import com.microsoft.azure.sdk.iot.device.twin.ReportedPropertiesUpdateResponse;
import com.microsoft.azure.sdk.iot.device.twin.Twin;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.ClientType;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.setup.TwinCommon;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

@IotHubTest
@StandardTierHubOnlyTest
@RunWith(Parameterized.class)
public class TwinTests extends TwinCommon
{
    public TwinTests(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws IOException, InterruptedException, IotHubException, URISyntaxException, GeneralSecurityException
    {
        super(protocol, authenticationType, clientType);
    }

    @Before
    public void setup() throws GeneralSecurityException, IOException, InterruptedException, URISyntaxException, IotHubException, IotHubClientException
    {
        this.testInstance.setup();
    }

    @Test
    public void testBasicTwinFlow() throws InterruptedException, IOException, IotHubException, TimeoutException, IotHubClientException
    {
        super.testBasicTwinFlow(true);
    }

    @Test
    public void receiveMultipleDesiredPropertiesAtOnce() throws IOException, InterruptedException, IotHubException, TimeoutException, IotHubClientException
    {
        final String desiredPropertyKey1 = UUID.randomUUID().toString();
        final String desiredPropertyValue1 = UUID.randomUUID().toString();

        final String desiredPropertyKey2 = UUID.randomUUID().toString();
        final String desiredPropertyValue2 = UUID.randomUUID().toString();

        // subscribe to desired properties
        final CountDownLatch desiredPropertyUpdatedLatch = new CountDownLatch(1);
        AtomicReference<Twin> desiredPropertyUpdateAtomicReference = new AtomicReference<>();
        testInstance.testIdentity.getClient().subscribeToDesiredProperties(
            (twin, context) ->
            {
                desiredPropertyUpdateAtomicReference.set(twin);
                desiredPropertyUpdatedLatch.countDown();
            },
            null);

        // send a desired property update and wait for it to be received by the device/module
        testInstance.serviceTwin.getDesiredProperties().put(desiredPropertyKey1, desiredPropertyValue1);
        testInstance.serviceTwin.getDesiredProperties().put(desiredPropertyKey2, desiredPropertyValue2);
        testInstance.twinServiceClient.patch(testInstance.serviceTwin);

        desiredPropertyUpdatedLatch.await();
        com.microsoft.azure.sdk.iot.device.twin.Twin desiredPropertyUpdate = desiredPropertyUpdateAtomicReference.get();

        // the desired property update received by the device must match the key/value pair sent by the service client
        assertTrue(isPropertyInTwinCollection(desiredPropertyUpdate.getDesiredProperties(), desiredPropertyKey1, desiredPropertyValue1));
        assertTrue(isPropertyInTwinCollection(desiredPropertyUpdate.getDesiredProperties(), desiredPropertyKey2, desiredPropertyValue2));
    }

    @Test
    public void receiveMultipleDesiredPropertiesSequentially() throws IOException, InterruptedException, IotHubException, TimeoutException, IotHubClientException
    {
        final String desiredPropertyKey1 = UUID.randomUUID().toString();
        final String desiredPropertyValue1 = UUID.randomUUID().toString();

        final String desiredPropertyKey2 = UUID.randomUUID().toString();
        final String desiredPropertyValue2 = UUID.randomUUID().toString();

        // subscribe to desired properties
        final CountDownLatch desiredProperty1UpdatedLatch = new CountDownLatch(1);
        final CountDownLatch desiredProperty2UpdatedLatch = new CountDownLatch(1);
        AtomicReference<Twin> desiredPropertyUpdateAtomicReference = new AtomicReference<>();
        testInstance.testIdentity.getClient().subscribeToDesiredProperties(
            (twin, context) ->
            {
                desiredPropertyUpdateAtomicReference.set(twin);

                if (twin.getDesiredProperties().containsKey(desiredPropertyKey1))
                {
                    desiredProperty1UpdatedLatch.countDown();
                }

                if (twin.getDesiredProperties().containsKey(desiredPropertyKey2))
                {
                    desiredProperty2UpdatedLatch.countDown();
                }
            },
            null);

        // send a desired property update and wait for it to be received by the device/module
        testInstance.serviceTwin.getDesiredProperties().put(desiredPropertyKey1, desiredPropertyValue1);
        testInstance.serviceTwin = testInstance.twinServiceClient.patch(testInstance.serviceTwin);

        desiredProperty1UpdatedLatch.await();
        com.microsoft.azure.sdk.iot.device.twin.Twin desiredPropertyUpdate = desiredPropertyUpdateAtomicReference.get();

        // the desired property update received by the device must match the key/value pair sent by the service client
        assertTrue(isPropertyInTwinCollection(desiredPropertyUpdate.getDesiredProperties(), desiredPropertyKey1, desiredPropertyValue1));

        testInstance.serviceTwin.getDesiredProperties().clear();
        testInstance.serviceTwin.getDesiredProperties().put(desiredPropertyKey2, desiredPropertyValue2);
        testInstance.serviceTwin = testInstance.twinServiceClient.patch(testInstance.serviceTwin);

        desiredProperty2UpdatedLatch.await();
        desiredPropertyUpdate = desiredPropertyUpdateAtomicReference.get();

        assertTrue(isPropertyInTwinCollection(desiredPropertyUpdate.getDesiredProperties(), desiredPropertyKey2, desiredPropertyValue2));
    }

    @Test
    public void sendMultipleReportedPropertiesAtOnce() throws IOException, TimeoutException, InterruptedException, IotHubException, IotHubClientException
    {
        final String reportedPropertyKey1 = UUID.randomUUID().toString();
        final String reportedPropertyValue1 = UUID.randomUUID().toString();

        final String reportedPropertyKey2 = UUID.randomUUID().toString();
        final String reportedPropertyValue2 = UUID.randomUUID().toString();

        testInstance.testIdentity.getClient().subscribeToDesiredProperties(
            (twin, context) ->
            {
                // don't care about desired properties for this test. Just need to open twin links
            },
            null);

        Twin twin = testInstance.testIdentity.getClient().getTwin();
        twin.getReportedProperties().put(reportedPropertyKey1, reportedPropertyValue1);
        twin.getReportedProperties().put(reportedPropertyKey2, reportedPropertyValue2);
        ReportedPropertiesUpdateResponse response = testInstance.testIdentity.getClient().updateReportedProperties(twin.getReportedProperties());

        assertTrue(response.getVersion() > 0);

        com.microsoft.azure.sdk.iot.service.twin.Twin serviceClientTwin = testInstance.getServiceClientTwin();
        assertTrue(isPropertyInTwinCollection(serviceClientTwin.getReportedProperties(), reportedPropertyKey1, reportedPropertyValue1));
        assertTrue(isPropertyInTwinCollection(serviceClientTwin.getReportedProperties(), reportedPropertyKey2, reportedPropertyValue2));
    }

    @Test
    public void sendMultipleReportedPropertiesSequentially() throws TimeoutException, InterruptedException, IOException, IotHubException, IotHubClientException
    {
        final String reportedPropertyKey1 = UUID.randomUUID().toString();
        final String reportedPropertyValue1 = UUID.randomUUID().toString();

        final String reportedPropertyKey2 = UUID.randomUUID().toString();
        final String reportedPropertyValue2 = UUID.randomUUID().toString();

        testInstance.testIdentity.getClient().subscribeToDesiredProperties(
            (twin, context) ->
            {
                // don't care about desired properties for this test. Just need to open twin links
            },
            null);

        // send one reported property
        Twin twin = testInstance.testIdentity.getClient().getTwin();
        twin.getReportedProperties().put(reportedPropertyKey1, reportedPropertyValue1);
        ReportedPropertiesUpdateResponse response = testInstance.testIdentity.getClient().updateReportedProperties(twin.getReportedProperties());
        twin.getReportedProperties().setVersion(response.getVersion());

        // send a different reported property
        twin.getReportedProperties().clear();
        twin.getReportedProperties().put(reportedPropertyKey2, reportedPropertyValue2);
        response = testInstance.testIdentity.getClient().updateReportedProperties(twin.getReportedProperties());
        twin.getReportedProperties().setVersion(response.getVersion());

        com.microsoft.azure.sdk.iot.service.twin.Twin serviceClientTwin = testInstance.getServiceClientTwin();
        assertTrue(isPropertyInTwinCollection(serviceClientTwin.getReportedProperties(), reportedPropertyKey1, reportedPropertyValue1));
        assertTrue(isPropertyInTwinCollection(serviceClientTwin.getReportedProperties(), reportedPropertyKey2, reportedPropertyValue2));
    }

    @Test
    public void canDeleteReportedProperties() throws IOException, TimeoutException, InterruptedException, IotHubException, IotHubClientException
    {
        final String reportedPropertyKey = UUID.randomUUID().toString();
        final String reportedPropertyValue = UUID.randomUUID().toString();

        testInstance.testIdentity.getClient().subscribeToDesiredProperties(
                (twin, context) ->
                {
                    // don't care about desired properties for this test. Just need to open twin links
                },
                null);

        Twin twin = testInstance.testIdentity.getClient().getTwin();
        twin.getReportedProperties().put(reportedPropertyKey, reportedPropertyValue);
        ReportedPropertiesUpdateResponse response = testInstance.testIdentity.getClient().updateReportedProperties(twin.getReportedProperties());

        assertTrue(response.getVersion() > 0);

        twin.getReportedProperties().setVersion(response.getVersion());
        twin.getReportedProperties().put(reportedPropertyKey, null);
        response = testInstance.testIdentity.getClient().updateReportedProperties(twin.getReportedProperties());

        assertTrue(response.getVersion() > 0);

        com.microsoft.azure.sdk.iot.service.twin.Twin serviceClientTwin = testInstance.getServiceClientTwin();
        assertFalse(isPropertyInTwinCollection(serviceClientTwin.getReportedProperties(), reportedPropertyKey, reportedPropertyValue));
    }


    // Both updateReportedPropertiesAsync and getTwinAsync have overloads that expose a verbose state callback detailing
    // when a message is queued, sent, ack'd, etc. This test makes sure that those callbacks are all executed as expected and in order.
    @ContinuousIntegrationTest
    @Test
    public void testCorrelatingMessageCallbackOverloads() throws TimeoutException, InterruptedException, IOException, IotHubException, IotHubClientException
    {
        final String desiredPropertyKey = UUID.randomUUID().toString();
        final String desiredPropertyValue = UUID.randomUUID().toString();

        // subscribe to desired properties
        final CountDownLatch desiredPropertyUpdatedLatch = new CountDownLatch(1);
        AtomicReference<com.microsoft.azure.sdk.iot.device.twin.Twin> desiredPropertyUpdateAtomicReference = new AtomicReference<>();
        testInstance.testIdentity.getClient().subscribeToDesiredProperties(
            (twin, context) ->
            {
                desiredPropertyUpdateAtomicReference.set(twin);
                desiredPropertyUpdatedLatch.countDown();
            },
            null);

        // after subscribing to desired properties, onMethodInvoked getTwin to get the initial state
        AtomicReference<com.microsoft.azure.sdk.iot.device.twin.Twin> twinAtomicReference = new AtomicReference<>();

        final Object expectedGetTwinContext = new Object();
        final CountDownLatch getTwinOnRequestQueuedLatch = new CountDownLatch(1);
        final CountDownLatch getTwinOnRequestSentLatch = new CountDownLatch(1);
        final CountDownLatch getTwinOnRequestAcknowledgedLatch = new CountDownLatch(1);
        final CountDownLatch getTwinOnResponseReceivedLatch = new CountDownLatch(1);
        final CountDownLatch getTwinOnResponseAcknowledgedLatch = new CountDownLatch(1);

        testInstance.testIdentity.getClient().getTwinAsync(
            new GetTwinCorrelatingMessageCallback()
            {
                @Override
                public void onRequestQueued(Message message, Object callbackContext)
                {
                    if (message != null && callbackContext.equals(expectedGetTwinContext))
                    {
                        getTwinOnRequestQueuedLatch.countDown();
                    }
                }

                @Override
                public void onRequestSent(Message message, Object callbackContext)
                {
                    if (message != null && callbackContext.equals(expectedGetTwinContext))
                    {
                        getTwinOnRequestSentLatch.countDown();
                    }
                }

                @Override
                public void onRequestAcknowledged(Message message, Object callbackContext, IotHubClientException e)
                {
                    if (message != null && callbackContext.equals(expectedGetTwinContext) && e == null)
                    {
                        getTwinOnRequestAcknowledgedLatch.countDown();
                    }
                }

                @Override
                public void onResponseReceived(Twin twin, Message message, Object callbackContext, IotHubStatusCode statusCode, IotHubClientException e)
                {
                    if (message != null && callbackContext.equals(expectedGetTwinContext) && e == null && statusCode == IotHubStatusCode.OK)
                    {
                        getTwinOnResponseReceivedLatch.countDown();
                        twinAtomicReference.set(twin);
                    }
                }

                @Override
                public void onResponseAcknowledged(Message message, Object callbackContext)
                {
                    if (message != null && callbackContext.equals(expectedGetTwinContext))
                    {
                        getTwinOnResponseAcknowledgedLatch.countDown();
                    }
                }
            },
            expectedGetTwinContext);

        assertTrue("Timed out waiting for a callback", getTwinOnRequestQueuedLatch.await(TWIN_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS));
        assertTrue("Timed out waiting for a callback", getTwinOnRequestSentLatch.await(TWIN_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS));
        assertTrue("Timed out waiting for a callback", getTwinOnRequestAcknowledgedLatch.await(TWIN_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS));
        assertTrue("Timed out waiting for a callback", getTwinOnResponseReceivedLatch.await(TWIN_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS));
        assertTrue("Timed out waiting for a callback", getTwinOnResponseAcknowledgedLatch.await(TWIN_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS));

        Twin twin = twinAtomicReference.get();

        // a twin should have no the desired property yet
        assertNotNull(twin);
        assertNotNull(twin.getReportedProperties());
        assertNotNull(twin.getDesiredProperties());
        assertFalse(twin.getDesiredProperties().containsKey(desiredPropertyKey));

        // send a desired property update and wait for it to be received by the device/module
        testInstance.serviceTwin.getDesiredProperties().put(desiredPropertyKey, desiredPropertyValue);
        testInstance.twinServiceClient.patch(testInstance.serviceTwin);

        desiredPropertyUpdatedLatch.await();
        com.microsoft.azure.sdk.iot.device.twin.Twin desiredPropertyUpdate = desiredPropertyUpdateAtomicReference.get();

        // the desired property update received by the device must match the key/value pair sent by the service client
        assertTrue(desiredPropertyUpdate.getDesiredProperties().containsKey(desiredPropertyKey));
        String value = (String) desiredPropertyUpdate.getDesiredProperties().get(desiredPropertyKey);
        assertEquals(desiredPropertyValue, value);

        // create some reported properties
        final String reportedPropertyKey = UUID.randomUUID().toString();
        final String reportedPropertyValue = UUID.randomUUID().toString();
        twin.getReportedProperties().put(reportedPropertyKey, reportedPropertyValue);

        // send the reported properties and wait for the service to have acknowledged them
        final Object expectedUpdateReportedPropertiesContext = new Object();
        CountDownLatch updateReportedPropertiesOnRequestQueuedLatch = new CountDownLatch(1);
        CountDownLatch updateReportedPropertiesOnRequestSentLatch = new CountDownLatch(1);
        CountDownLatch updateReportedPropertiesOnRequestAcknowledgedLatch = new CountDownLatch(1);
        CountDownLatch updateReportedPropertiesOnResponseReceivedLatch = new CountDownLatch(1);
        CountDownLatch updateReportedPropertiesOnResponseAcknowledgedLatch = new CountDownLatch(1);

        AtomicReference<IotHubStatusCode> iotHubStatusCodeAtomicReference = new AtomicReference<>();
        testInstance.testIdentity.getClient().updateReportedPropertiesAsync(
                twin.getReportedProperties(),
            new ReportedPropertiesUpdateCorrelatingMessageCallback()
            {
                @Override
                public void onRequestQueued(Message message, Object callbackContext)
                {
                    if (message != null && callbackContext.equals(expectedUpdateReportedPropertiesContext))
                    {
                        updateReportedPropertiesOnRequestQueuedLatch.countDown();
                    }
                }

                @Override
                public void onRequestSent(Message message, Object callbackContext)
                {
                    if (message != null && callbackContext.equals(expectedUpdateReportedPropertiesContext))
                    {
                        updateReportedPropertiesOnRequestSentLatch.countDown();
                    }
                }

                @Override
                public void onRequestAcknowledged(Message message, Object callbackContext, IotHubClientException e)
                {
                    if (message != null && callbackContext.equals(expectedUpdateReportedPropertiesContext) && e == null)
                    {
                        updateReportedPropertiesOnRequestAcknowledgedLatch.countDown();
                    }
                }

                @Override
                public void onResponseReceived(Message message, Object callbackContext, IotHubStatusCode statusCode, ReportedPropertiesUpdateResponse response, IotHubClientException e)
                {
                    if (message != null && callbackContext.equals(expectedUpdateReportedPropertiesContext) && e == null && statusCode == IotHubStatusCode.OK)
                    {
                        updateReportedPropertiesOnResponseReceivedLatch.countDown();
                        iotHubStatusCodeAtomicReference.set(statusCode);
                    }
                }

                @Override
                public void onResponseAcknowledged(Message message, Object callbackContext)
                {
                    if (message != null && callbackContext.equals(expectedUpdateReportedPropertiesContext))
                    {
                        updateReportedPropertiesOnResponseAcknowledgedLatch.countDown();
                    }
                }
            },
            expectedUpdateReportedPropertiesContext);

        assertTrue("Timed out waiting for a callback", updateReportedPropertiesOnRequestQueuedLatch.await(TWIN_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS));
        assertTrue("Timed out waiting for a callback", updateReportedPropertiesOnRequestSentLatch.await(TWIN_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS));
        assertTrue("Timed out waiting for a callback", updateReportedPropertiesOnRequestAcknowledgedLatch.await(TWIN_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS));
        assertTrue("Timed out waiting for a callback", updateReportedPropertiesOnResponseReceivedLatch.await(TWIN_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS));
        assertTrue("Timed out waiting for a callback", updateReportedPropertiesOnResponseAcknowledgedLatch.await(TWIN_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS));

        IotHubStatusCode statusCode = iotHubStatusCodeAtomicReference.get();

        // the reported properties request should have been ack'd with OK from the service
        assertEquals(IotHubStatusCode.OK, statusCode);

        // get the twin from the service client to check if the reported property is now present
        testInstance.serviceTwin = testInstance.getServiceClientTwin();

        com.microsoft.azure.sdk.iot.service.twin.TwinCollection reportedProperties = testInstance.serviceTwin.getReportedProperties();
        assertTrue(reportedProperties.size() > 0);
        assertTrue("Did not find expected reported property key and/or value after the device reported it", isPropertyInTwinCollection(reportedProperties, reportedPropertyKey, reportedPropertyValue));
    }
}
