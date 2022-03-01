// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.iothub.twin;

import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.twin.GetTwinCorrelatingMessageCallback;
import com.microsoft.azure.sdk.iot.device.twin.Twin;
import com.microsoft.azure.sdk.iot.device.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.device.twin.ReportedPropertiesUpdateCorrelatingMessageCallback;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.twin.Pair;
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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

@IotHubTest
@StandardTierHubOnlyTest
@RunWith(Parameterized.class)
public class TwinTests extends TwinCommon
{
    public TwinTests(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws IOException, InterruptedException, IotHubException, URISyntaxException, GeneralSecurityException, ModuleClientException
    {
        super(protocol, authenticationType, clientType);
    }

    @Before
    public void setup() throws GeneralSecurityException, ModuleClientException, IOException, InterruptedException, URISyntaxException, IotHubException
    {
        this.testInstance.setup();
    }

    @Test
    public void testBasicTwinFlow() throws InterruptedException, IOException, IotHubException, TimeoutException
    {
        super.testBasicTwinFlow();
    }

    @Test
    public void receiveMultipleDesiredPropertiesAtOnce() throws IOException, InterruptedException, IotHubException, TimeoutException
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
        Set<Pair> desiredProperties = new HashSet<>();
        desiredProperties.add(new Pair(desiredPropertyKey1, desiredPropertyValue1));
        desiredProperties.add(new Pair(desiredPropertyKey2, desiredPropertyValue2));
        testInstance.serviceTwin.setDesiredProperties(desiredProperties);
        testInstance.twinServiceClient.patch(testInstance.serviceTwin);

        desiredPropertyUpdatedLatch.await();
        com.microsoft.azure.sdk.iot.device.twin.Twin desiredPropertyUpdate = desiredPropertyUpdateAtomicReference.get();

        // the desired property update received by the device must match the key/value pair sent by the service client
        assertTrue(isPropertyInTwinCollection(desiredPropertyUpdate.getDesiredProperties(), desiredPropertyKey1, desiredPropertyValue1));
        assertTrue(isPropertyInTwinCollection(desiredPropertyUpdate.getDesiredProperties(), desiredPropertyKey2, desiredPropertyValue2));
    }

    @Test
    public void receiveMultipleDesiredPropertiesSequentially() throws IOException, InterruptedException, IotHubException, TimeoutException
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
        Set<Pair> desiredProperties = new HashSet<>();
        desiredProperties.add(new Pair(desiredPropertyKey1, desiredPropertyValue1));
        testInstance.serviceTwin.setDesiredProperties(desiredProperties);
        testInstance.twinServiceClient.patch(testInstance.serviceTwin);

        desiredProperty1UpdatedLatch.await();
        com.microsoft.azure.sdk.iot.device.twin.Twin desiredPropertyUpdate = desiredPropertyUpdateAtomicReference.get();

        // the desired property update received by the device must match the key/value pair sent by the service client
        assertTrue(isPropertyInTwinCollection(desiredPropertyUpdate.getDesiredProperties(), desiredPropertyKey1, desiredPropertyValue1));

        desiredProperties.clear();
        desiredProperties.add(new Pair(desiredPropertyKey2, desiredPropertyValue2));
        testInstance.serviceTwin.setDesiredProperties(desiredProperties);
        testInstance.twinServiceClient.patch(testInstance.serviceTwin);

        desiredProperty2UpdatedLatch.await();
        desiredPropertyUpdate = desiredPropertyUpdateAtomicReference.get();

        assertTrue(isPropertyInTwinCollection(desiredPropertyUpdate.getDesiredProperties(), desiredPropertyKey2, desiredPropertyValue2));
    }

    @Test
    public void sendMultipleReportedPropertiesAtOnce() throws IOException, TimeoutException, InterruptedException, IotHubException
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

        com.microsoft.azure.sdk.iot.device.twin.TwinCollection twinCollection = new com.microsoft.azure.sdk.iot.device.twin.TwinCollection();
        twinCollection.put(reportedPropertyKey1, reportedPropertyValue1);
        twinCollection.put(reportedPropertyKey2, reportedPropertyValue2);
        IotHubStatusCode statusCode = testInstance.testIdentity.getClient().updateReportedProperties(twinCollection);

        assertEquals(IotHubStatusCode.OK, statusCode);

        com.microsoft.azure.sdk.iot.service.twin.Twin serviceClientTwin = testInstance.getServiceClientTwin();
        assertTrue(isPropertyInSet(serviceClientTwin.getReportedProperties(), reportedPropertyKey1, reportedPropertyValue1));
        assertTrue(isPropertyInSet(serviceClientTwin.getReportedProperties(), reportedPropertyKey2, reportedPropertyValue2));
    }

    @Test
    public void sendMultipleReportedPropertiesSequentially() throws TimeoutException, InterruptedException, IOException, IotHubException
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
        com.microsoft.azure.sdk.iot.device.twin.TwinCollection twinCollection = new com.microsoft.azure.sdk.iot.device.twin.TwinCollection();
        twinCollection.put(reportedPropertyKey1, reportedPropertyValue1);
        IotHubStatusCode statusCode = testInstance.testIdentity.getClient().updateReportedProperties(twinCollection);
        assertEquals(IotHubStatusCode.OK, statusCode);

        // send a different reported property
        twinCollection.clear();
        twinCollection.put(reportedPropertyKey2, reportedPropertyValue2);
        statusCode = testInstance.testIdentity.getClient().updateReportedProperties(twinCollection);
        assertEquals(IotHubStatusCode.OK, statusCode);

        com.microsoft.azure.sdk.iot.service.twin.Twin serviceClientTwin = testInstance.getServiceClientTwin();
        assertTrue(isPropertyInSet(serviceClientTwin.getReportedProperties(), reportedPropertyKey1, reportedPropertyValue1));
        assertTrue(isPropertyInSet(serviceClientTwin.getReportedProperties(), reportedPropertyKey2, reportedPropertyValue2));
    }

    // Both updateReportedPropertiesAsync and getTwinAsync have overloads that expose a verbose state callback detailing
    // when a message is queued, sent, ack'd, etc. This test makes sure that those callbacks are all executed as expected and in order.
    @ContinuousIntegrationTest
    @Test
    public void testCorrelatingMessageCallbackOverloads() throws TimeoutException, InterruptedException, IOException, IotHubException
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
                public void onRequestAcknowledged(Message message, Object callbackContext, TransportException e)
                {
                    if (message != null && callbackContext.equals(expectedGetTwinContext) && e == null)
                    {
                        getTwinOnRequestAcknowledgedLatch.countDown();
                    }
                }

                @Override
                public void onResponseReceived(Twin twin, Message message, Object callbackContext, IotHubStatusCode statusCode, TransportException e)
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

        getTwinOnRequestQueuedLatch.await();
        getTwinOnRequestSentLatch.await();
        getTwinOnRequestAcknowledgedLatch.await();
        getTwinOnResponseReceivedLatch.await();
        getTwinOnResponseAcknowledgedLatch.await();

        Twin twin = twinAtomicReference.get();

        // a twin should have no the desired property yet
        assertNotNull(twin);
        assertNotNull(twin.getReportedProperties());
        assertNotNull(twin.getDesiredProperties());
        assertFalse(twin.getDesiredProperties().containsKey(desiredPropertyKey));

        // send a desired property update and wait for it to be received by the device/module
        Set<Pair> desiredProperties = new HashSet<>();
        desiredProperties.add(new Pair(desiredPropertyKey, desiredPropertyValue));
        testInstance.serviceTwin.setDesiredProperties(desiredProperties);
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
        TwinCollection reportedProperties = new TwinCollection();
        reportedProperties.put(reportedPropertyKey, reportedPropertyValue);

        // send the reported properties and wait for the service to have acknowledged them
        final Object expectedUpdateReportedPropertiesContext = new Object();
        CountDownLatch updateReportedPropertiesOnRequestQueuedLatch = new CountDownLatch(1);
        CountDownLatch updateReportedPropertiesOnRequestSentLatch = new CountDownLatch(1);
        CountDownLatch updateReportedPropertiesOnRequestAcknowledgedLatch = new CountDownLatch(1);
        CountDownLatch updateReportedPropertiesOnResponseReceivedLatch = new CountDownLatch(1);
        CountDownLatch updateReportedPropertiesOnResponseAcknowledgedLatch = new CountDownLatch(1);

        AtomicReference<IotHubStatusCode> iotHubStatusCodeAtomicReference = new AtomicReference<>();
        testInstance.testIdentity.getClient().updateReportedPropertiesAsync(
            reportedProperties,
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
                public void onRequestAcknowledged(Message message, Object callbackContext, TransportException e)
                {
                    if (message != null && callbackContext.equals(expectedUpdateReportedPropertiesContext) && e == null)
                    {
                        updateReportedPropertiesOnRequestAcknowledgedLatch.countDown();
                    }
                }

                @Override
                public void onResponseReceived(Message message, Object callbackContext, IotHubStatusCode statusCode, TransportException e)
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

        updateReportedPropertiesOnRequestQueuedLatch.await();
        updateReportedPropertiesOnRequestSentLatch.await();
        updateReportedPropertiesOnRequestAcknowledgedLatch.await();
        updateReportedPropertiesOnResponseReceivedLatch.await();
        updateReportedPropertiesOnResponseAcknowledgedLatch.await();

        IotHubStatusCode statusCode = iotHubStatusCodeAtomicReference.get();

        // the reported properties request should have been ack'd with OK from the service
        assertEquals(IotHubStatusCode.OK, statusCode);

        // get the twin from the service client to check if the reported property is now present
        testInstance.serviceTwin = testInstance.getServiceClientTwin();

        Set<Pair> reportedPropertiesSet = testInstance.serviceTwin.getReportedProperties();
        assertTrue(reportedPropertiesSet.size() > 0);
        assertTrue("Did not find expected reported property key and/or value after the device reported it", isPropertyInSet(reportedPropertiesSet, reportedPropertyKey, reportedPropertyValue));
    }
}
