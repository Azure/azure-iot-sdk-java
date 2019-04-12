/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.device;

import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClient;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientRegistrationCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.ProvisioningDeviceClientConfig;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ProvisioningDeviceClientContract;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.ProvisioningTask;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;

/*
    Unit tests for ProvisioningDeviceClient
    Coverage : 100% methods, 100% lines
 */

@RunWith(JMockit.class)
public class ProvisioningDeviceClientTest
{
    private static final String END_POINT = "testEndPoint";
    private static final String SCOPE_ID = "testScopeId";
    private static final ProvisioningDeviceClientTransportProtocol TEST_PROTOCOL = ProvisioningDeviceClientTransportProtocol.HTTPS;
    private static final String CUSTOM_PAYLOAD = "{ \"dpsCustomPayload\": \"dataValue\" }";

    @Mocked
    SecurityProvider mockedSecurityProvider;

    @Mocked
    ProvisioningDeviceClientRegistrationCallback mockedRegistrationCB;

    @Mocked
    ExecutorService mockedExecutorService;

    @Mocked
    Executors mockedExecutors;

    @Mocked
    ProvisioningDeviceClientConfig mockedProvisioningDeviceClientConfig;

    @Mocked
    ProvisioningDeviceClientContract mockedProvisioningDeviceClientContract;

    @Mocked
    ProvisioningTask mockedProvisioningTask;

    //SRS_ProvisioningDeviceClient_25_005: [ The constructor shall create provisioningDeviceClientConfig and set all the provided values to it.. ]
    //SRS_ProvisioningDeviceClient_25_006: [ The constructor shall create provisioningDeviceClientContract with the given config. ]
    //SRS_ProvisioningDeviceClient_25_007: [ The constructor shall create an executor service with fixed thread pool of size 1. ]
    @Test
    public void constructorSucceeds() throws ProvisioningDeviceClientException
    {
        //arrange
        //act
        ProvisioningDeviceClient testProvisioningDeviceClient = ProvisioningDeviceClient.create(END_POINT, SCOPE_ID, TEST_PROTOCOL, mockedSecurityProvider);

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientConfig.setProvisioningServiceGlobalEndpoint(END_POINT);
                times = 1;
                mockedProvisioningDeviceClientConfig.setIdScope(SCOPE_ID);
                times = 1;
                mockedProvisioningDeviceClientConfig.setSecurityProvider(mockedSecurityProvider);
                times = 1;
                mockedProvisioningDeviceClientConfig.setProtocol(TEST_PROTOCOL);
                times = 1;
                ProvisioningDeviceClientContract.createProvisioningContract((ProvisioningDeviceClientConfig) any);
                times = 1;
                Executors.newFixedThreadPool(1);
                times = 1;
            }
        };
    }

    //SRS_ProvisioningDeviceClient_25_001: [ The constructor shall throw IllegalArgumentException if globalEndpoint is null or empty. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullEndPoint() throws ProvisioningDeviceClientException
    {
        //act
        ProvisioningDeviceClient testProvisioningDeviceClient = ProvisioningDeviceClient.create(null, SCOPE_ID, TEST_PROTOCOL, mockedSecurityProvider);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyEndPoint() throws ProvisioningDeviceClientException
    {
        //act
        ProvisioningDeviceClient testProvisioningDeviceClient = ProvisioningDeviceClient.create("", SCOPE_ID, TEST_PROTOCOL, mockedSecurityProvider);
    }

    //SRS_ProvisioningDeviceClient_25_002: [ The constructor shall throw IllegalArgumentException if SCOPE_ID is null or empty. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullScopeId() throws ProvisioningDeviceClientException
    {
        //act
        ProvisioningDeviceClient testProvisioningDeviceClient = ProvisioningDeviceClient.create(END_POINT, null, TEST_PROTOCOL, mockedSecurityProvider);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyScopeId() throws ProvisioningDeviceClientException
    {
        //act
        ProvisioningDeviceClient testProvisioningDeviceClient = ProvisioningDeviceClient.create(END_POINT, "", TEST_PROTOCOL, mockedSecurityProvider);
    }

    //SRS_ProvisioningDeviceClient_25_003: [ The constructor shall throw IllegalArgumentException if protocol is null. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullProtocol() throws ProvisioningDeviceClientException
    {
        //act
        ProvisioningDeviceClient testProvisioningDeviceClient = ProvisioningDeviceClient.create(END_POINT, SCOPE_ID, null, mockedSecurityProvider);
    }

    //SRS_ProvisioningDeviceClient_25_004: [ The constructor shall throw IllegalArgumentException if securityClient is null. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullSecurityClient() throws ProvisioningDeviceClientException
    {
        //act
        ProvisioningDeviceClient testProvisioningDeviceClient = ProvisioningDeviceClient.create(END_POINT, SCOPE_ID, TEST_PROTOCOL, null);
    }

    //SRS_ProvisioningDeviceClient_25_009: [ This method shall set the config with the callback. ]
    //SRS_ProvisioningDeviceClient_25_010: [ This method shall start the executor with the ProvisioningTask. ]
    @Test
    public void registerSucceeds() throws ProvisioningDeviceClientException
    {
        //arrange
        ProvisioningDeviceClient testProvisioningDeviceClient = ProvisioningDeviceClient.create(END_POINT, SCOPE_ID, TEST_PROTOCOL, mockedSecurityProvider);
        //act
        testProvisioningDeviceClient.registerDevice(mockedRegistrationCB, null);

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientConfig.setRegistrationCallback((ProvisioningDeviceClientRegistrationCallback) any, any);
                times = 1;
                mockedExecutorService.submit((ProvisioningTask) any);
                times = 1;
            }
        };
    }

    //SRS_ProvisioningDeviceClient_25_008: [ This method shall throw IllegalArgumentException if provisioningDeviceClientRegistrationCallback is null. ]
    @Test (expected = IllegalArgumentException.class)
    public void registerThrowsOnNullCB() throws ProvisioningDeviceClientException
    {
        //arrange
        ProvisioningDeviceClient testProvisioningDeviceClient = ProvisioningDeviceClient.create(END_POINT, SCOPE_ID, TEST_PROTOCOL, mockedSecurityProvider);
        //act
        testProvisioningDeviceClient.registerDevice(null, null);
    }

    //SRS_ProvisioningDeviceClient_25_011: [ This method shall check if executor is terminated and if not shall shutdown the executor. ]
    @Test
    public void closeNowSucceeds() throws ProvisioningDeviceClientException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockedExecutorService.isTerminated();
                result = false;
            }
        };

        ProvisioningDeviceClient testProvisioningDeviceClient = ProvisioningDeviceClient.create(END_POINT, SCOPE_ID, TEST_PROTOCOL, mockedSecurityProvider);
        //act
        testProvisioningDeviceClient.closeNow();
        //assert
        new Verifications()
        {
            {
                mockedExecutorService.shutdownNow();
                times = 1;
            }
        };
    }

    @Test
    public void closeNowTerminatedSucceeds() throws ProvisioningDeviceClientException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockedExecutorService.isTerminated();
                result = true;
            }
        };

        ProvisioningDeviceClient testProvisioningDeviceClient = ProvisioningDeviceClient.create(END_POINT, SCOPE_ID, TEST_PROTOCOL, mockedSecurityProvider);
        //act
        testProvisioningDeviceClient.closeNow();
        //assert
        new Verifications()
        {
            {
                mockedExecutorService.shutdownNow();
                times = 0;
            }
        };
    }

    @Test
    public void setProvisioningPayloadSucceeds() throws ProvisioningDeviceClientException
    {
        //arrange
        ProvisioningDeviceClient testProvisioningDeviceClient = ProvisioningDeviceClient.create(END_POINT, SCOPE_ID, TEST_PROTOCOL, mockedSecurityProvider);
        //act
        testProvisioningDeviceClient.setProvisioningPayload(CUSTOM_PAYLOAD);
        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientConfig.setCustomPayload(CUSTOM_PAYLOAD);
                times = 1;
            }
        };
    }
}
