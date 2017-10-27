/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package tests.unit.com.microsoft.azure.sdk.iot.dps.device.internal.provisioningtask;

import com.microsoft.azure.sdk.iot.provisioning.security.SecurityClient;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientConfig;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientRegistrationCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientRegistrationInfo;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientStatusCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ProvisioningDeviceClientContract;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.OperationRegistrationStatusParser;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.ResponseParser;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.provisioningtask.*;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.*;

import static com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientStatus.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/*
    Unit test for ProvisioningTask
    Coverage : 100% method, 100% line
 */
@RunWith(JMockit.class)
public class ProvisioningTaskTest
{
    private static final String TEST_OPERATION_ID = "testOperationId";
    private static final int MAX_TIME_TO_WAIT_FOR_REGISTRATION = 1000000;
    private static final int MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE = 10000;
    private static final String TEST_HUB = "TestHub";
    private static final String TEST_DEVICE_ID = "testDeviceId";

    @Mocked
    SecurityClient mockedSecurityClient;
    @Mocked
    ProvisioningDeviceClientContract mockedProvisioningDeviceClientContract;
    @Mocked
    ProvisioningDeviceClientConfig mockedProvisioningDeviceClientConfig;
    @Mocked
    ProvisioningDeviceClientStatusCallback mockedProvisioningDeviceClientStatusCallback;
    @Mocked
    ProvisioningDeviceClientRegistrationCallback mockedProvisioningDeviceClientRegistrationCallback;
    @Mocked
    Authorization mockedAuthorization;
    @Mocked
    ExecutorService mockedExecutorService;
    @Mocked
    Executors mockedExecutors;
    @Mocked
    RegisterTask mockedRegisterTask;
    @Mocked
    StatusTask mockedStatusTask;
    @Mocked
    FutureTask mockedFutureTask;
    @Mocked
    ResponseParser mockedResponseParser;
    @Mocked
    OperationRegistrationStatusParser mockedOperationRegistrationStatusParser;
    @Mocked
    Future<?> mockedFuture;
    @Mocked
    RequestData mockedRequestData;

    //SRS_provisioningtask_25_001: [ Constructor shall save provisioningDeviceClientConfig , securityClient, provisioningDeviceClientContract, provisioningDeviceClientStatusCallback, dpsStatusCallbackContext.]
    //SRS_ProvisioningTask_25_015: [ Constructor shall start the executor with a fixed thread pool of size 2.]
    //SRS_provisioningtask_25_003: [ Constructor shall trigger status callback if provided with status DPS_DEVICE_STATUS_UNAUTHENTICATED.]
    @Test
    public void constructorSucceeds() throws ProvisioningDeviceClientException
    {
        //arrange
        //act
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedSecurityClient, mockedProvisioningDeviceClientContract,
                                                                     mockedProvisioningDeviceClientStatusCallback, null);
        //assert
        new Verifications()
        {
            {
                Executors.newFixedThreadPool(2);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_UNAUTHENTICATED, null, null);
                times = 1;
            }
        };
        assertEquals(mockedProvisioningDeviceClientConfig, Deencapsulation.getField(testProvisioningTask, "provisioningDeviceClientConfig"));
        assertEquals(mockedSecurityClient, Deencapsulation.getField(testProvisioningTask, "securityClient"));
        assertEquals(mockedProvisioningDeviceClientContract, Deencapsulation.getField(testProvisioningTask, "provisioningDeviceClientContract"));
        assertEquals(mockedProvisioningDeviceClientStatusCallback, Deencapsulation.getField(testProvisioningTask, "provisioningDeviceClientStatusCallback"));
        assertNotNull(Deencapsulation.getField(testProvisioningTask, "authorization"));
    }

    //SRS_provisioningtask_25_002: [ Constructor throw ProvisioningDeviceClientException if provisioningDeviceClientConfig , securityClient or provisioningDeviceClientContract is null.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void constructorThrowsOnNullConfig() throws ProvisioningDeviceClientException
    {
        //arrange
        //act
        ProvisioningTask testProvisioningTask = new ProvisioningTask(null, mockedSecurityClient, mockedProvisioningDeviceClientContract,
                                                                     mockedProvisioningDeviceClientStatusCallback, null);
        //assert
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void constructorThrowsOnNullSecurityClient() throws ProvisioningDeviceClientException
    {
        //arrange
        //act
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, null, mockedProvisioningDeviceClientContract,
                                                                     mockedProvisioningDeviceClientStatusCallback, null);
        //assert
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void constructorThrowsOnNullContract() throws ProvisioningDeviceClientException
    {
        //arrange
        //act
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedSecurityClient, null,
                                                                     mockedProvisioningDeviceClientStatusCallback, null);
        //assert
    }

    //SRS_provisioningtask_25_005: [ This method shall save the registration callback. ]
    @Test
    public void setRegistrationCallbackSucceeds() throws ProvisioningDeviceClientException
    {
        //arrange
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedSecurityClient, mockedProvisioningDeviceClientContract,
                                                                     mockedProvisioningDeviceClientStatusCallback, null);
        //act
        testProvisioningTask.setRegistrationCallback(mockedProvisioningDeviceClientRegistrationCallback, null);
        //assert
        assertEquals(mockedProvisioningDeviceClientRegistrationCallback, Deencapsulation.getField(testProvisioningTask, "provisioningDeviceClientRegistrationCallback"));

    }

    //SRS_provisioningtask_25_004: [ This method shall throw ProvisioningDeviceClientException if the provisioningDeviceClientRegistrationCallback is null. ]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void setRegistrationCallbackThrowsOnNullCB() throws ProvisioningDeviceClientException
    {
        //arrange
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedSecurityClient, mockedProvisioningDeviceClientContract,
                                                                     mockedProvisioningDeviceClientStatusCallback, null);
        //act
        testProvisioningTask.setRegistrationCallback(null, null);

    }

    //SRS_provisioningtask_25_007: [ This method shall invoke Register task and status task to execute the state machine of the service as per below rules.]
    /*
        Service State Machine Rules

        SRS_provisioningtask_25_008: [ This method shall invoke register task and wait for it to complete.]
        SRS_provisioningtask_25_009: [ This method shall invoke status callback with status DPS_DEVICE_STATUS_AUTHENTICATED if register task completes successfully.]
        SRS_provisioningtask_25_010: [ This method shall invoke status task to get the current state of the device registration and wait until a terminal state is reached.]
        SRS_provisioningtask_25_011: [ Upon reaching one of the terminal state i.e ASSIGNED, this method shall invoke registration callback with the information retrieved from service for IotHub Uri and DeviceId. Also if status callback is defined then it shall be invoked with status DPS_DEVICE_STATUS_ASSIGNED.]
        SRS_provisioningtask_25_012: [ Upon reaching one of the terminal states i.e FAILED or DISABLED, this method shall invoke registration callback with error message received from service. Also if status callback is defined then it shall be invoked with status DPS_DEVICE_STATUS_ERROR.]
        SRS_provisioningtask_25_013: [ Upon reaching intermediate state i.e UNASSIGNED or ASSIGNING, this method shall continue to query for status until a terminal state is reached. Also if status callback is defined then it shall be invoked with status DPS_DEVICE_STATUS_ASSIGNING.]
    */

    @Test
    public void invokeRegisterReturnsUnassignedToAssignedSucceeds() throws Exception, InterruptedException, ExecutionException, TimeoutException, ProvisioningDeviceClientException
    {
        // arrange
        // Register expectations
        new NonStrictExpectations()
        {
            {
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_REGISTRATION, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
            }
        };

        // Moving from status unassigned to assigned
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "unassigned";
                mockedResponseParser.getStatus();
                result = "unassigned";
            }
        };

       // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedResponseParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
            }
        };

        // State machine expectations
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "assigned";
                mockedResponseParser.getStatus();
                result = "assigned";
                mockedResponseParser.getStatus();
                result = "assigned";
                mockedResponseParser.getStatus();
                result = "assigned";
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedResponseParser.getRegistrationStatus();
                result = mockedOperationRegistrationStatusParser;
                mockedOperationRegistrationStatusParser.getAssignedHub();
                result = TEST_HUB;
                mockedOperationRegistrationStatusParser.getDeviceId();
                result = TEST_DEVICE_ID;
            }
        };
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedSecurityClient, mockedProvisioningDeviceClientContract,
                                                                     mockedProvisioningDeviceClientStatusCallback, null);
        testProvisioningTask.setRegistrationCallback(mockedProvisioningDeviceClientRegistrationCallback, null);

        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_UNAUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_AUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNING, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNED, null, null);
                times = 1;
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationInfo)any, null);
                times = 1;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }

    @Test
    public void invokeRegisterReturnsAssigningToStatusAssignedSucceeds() throws Exception
    {
        // arrange
        // Register expectations
        new NonStrictExpectations()
        {
            {
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_REGISTRATION, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
            }
        };

        // Moving from status register assigning to status assigned
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedResponseParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
            }
        };

        // State machine expectations
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "assigned";
                mockedResponseParser.getStatus();
                result = "assigned";
                mockedResponseParser.getStatus();
                result = "assigned";
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedResponseParser.getRegistrationStatus();
                result = mockedOperationRegistrationStatusParser;
                mockedOperationRegistrationStatusParser.getAssignedHub();
                result = TEST_HUB;
                mockedOperationRegistrationStatusParser.getDeviceId();
                result = TEST_DEVICE_ID;
            }
        };
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedSecurityClient, mockedProvisioningDeviceClientContract,
                                                                     mockedProvisioningDeviceClientStatusCallback, null);
        testProvisioningTask.setRegistrationCallback(mockedProvisioningDeviceClientRegistrationCallback, null);

        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_UNAUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_AUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNING, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNED, null, null);
                times = 1;
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationInfo)any, null);
                times = 1;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }

    @Test
    public void invokeRegisterReturnsAssignedSucceeds() throws Exception
    {
        // arrange
        // Register expectations
        new NonStrictExpectations()
        {
            {
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_REGISTRATION, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
                mockedResponseParser.getOperationId();
                result = TEST_OPERATION_ID;
            }
        };

        // Moving from status register assigning to status assigned
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "assigned";
                mockedResponseParser.getStatus();
                result = "assigned";
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedResponseParser.getRegistrationStatus();
                result = mockedOperationRegistrationStatusParser;
                mockedOperationRegistrationStatusParser.getAssignedHub();
                result = TEST_HUB;
                mockedOperationRegistrationStatusParser.getDeviceId();
                result = TEST_DEVICE_ID;
            }
        };
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedSecurityClient, mockedProvisioningDeviceClientContract,
                                                                     mockedProvisioningDeviceClientStatusCallback, null);
        testProvisioningTask.setRegistrationCallback(mockedProvisioningDeviceClientRegistrationCallback, null);
        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_UNAUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_AUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNING, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNED, null, null);
                times = 1;
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationInfo)any, null);
                times = 1;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }

    @Test
    public void invokeRegisterReturnsFailedSucceeds() throws Exception
    {
        // arrange
        // Register expectations
        new NonStrictExpectations()
        {
            {
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_REGISTRATION, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
                mockedResponseParser.getOperationId();
                result = TEST_OPERATION_ID;
            }
        };

        // Moving from status register assigning to status assigned
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "failed";
                mockedResponseParser.getStatus();
                result = "failed";
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedResponseParser.getRegistrationStatus();
                result = mockedOperationRegistrationStatusParser;
                mockedOperationRegistrationStatusParser.getErrorMessage();
                result = "Test Error Message";
            }
        };
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedSecurityClient, mockedProvisioningDeviceClientContract,
                                                                     mockedProvisioningDeviceClientStatusCallback, null);
        testProvisioningTask.setRegistrationCallback(mockedProvisioningDeviceClientRegistrationCallback, null);
        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_UNAUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_AUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNING, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNED, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ERROR, (Exception) any, null);
                times = 1;
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationInfo)any, null);
                times = 1;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }

    @Test
    public void invokeRegisterReturnsDisabledSucceeds() throws Exception
    {
        // arrange
        // Register expectations
        new NonStrictExpectations()
        {
            {
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_REGISTRATION, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
                mockedResponseParser.getOperationId();
                result = TEST_OPERATION_ID;
            }
        };

        // Moving from status register assigning to status assigned
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "disabled";
                mockedResponseParser.getStatus();
                result = "disabled";
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedResponseParser.getRegistrationStatus();
                result = mockedOperationRegistrationStatusParser;
                mockedOperationRegistrationStatusParser.getErrorMessage();
                result = "Test Error Message";
            }
        };
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedSecurityClient, mockedProvisioningDeviceClientContract,
                                                                     mockedProvisioningDeviceClientStatusCallback, null);
        testProvisioningTask.setRegistrationCallback(mockedProvisioningDeviceClientRegistrationCallback, null);
        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_UNAUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_AUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNING, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNED, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ERROR, (Exception) any, null);
                times = 1;
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationInfo)any, null);
                times = 1;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }

    @Test
    public void invokeStatusAssigningToAssignedSucceeds() throws Exception
    {
        // arrange
        // Register expectations
        new NonStrictExpectations()
        {
            {
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_REGISTRATION, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
            }
        };

        // Moving from status register assigning to status assigning
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedResponseParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
            }
        };

        // Moving from status assigning to status assigned
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
            }
        };

        // State machine expectations
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "assigned";
                mockedResponseParser.getStatus();
                result = "assigned";
                mockedResponseParser.getStatus();
                result = "assigned";
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedResponseParser.getRegistrationStatus();
                result = mockedOperationRegistrationStatusParser;
                mockedOperationRegistrationStatusParser.getAssignedHub();
                result = TEST_HUB;
                mockedOperationRegistrationStatusParser.getDeviceId();
                result = TEST_DEVICE_ID;
            }
        };
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedSecurityClient, mockedProvisioningDeviceClientContract,
                                                                     mockedProvisioningDeviceClientStatusCallback, null);
        testProvisioningTask.setRegistrationCallback(mockedProvisioningDeviceClientRegistrationCallback, null);
        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_UNAUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_AUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNING, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNED, null, null);
                times = 1;
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationInfo)any, null);
                times = 1;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }

    @Test
    public void invokeStatusAssigningToFailedSucceeds() throws Exception
    {
        // arrange
        // Register expectations
        new NonStrictExpectations()
        {
            {
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_REGISTRATION, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
            }
        };

        // Moving from status register assigning to status assigning
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedResponseParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
            }
        };

        // Moving from status assigning to status failed
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
            }
        };

        // State machine expectations
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "failed";
                mockedResponseParser.getStatus();
                result = "failed";
                mockedResponseParser.getStatus();
                result = "failed";
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedResponseParser.getRegistrationStatus();
                result = mockedOperationRegistrationStatusParser;
                mockedOperationRegistrationStatusParser.getErrorMessage();
                result = "Some error Message";
            }
        };
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedSecurityClient, mockedProvisioningDeviceClientContract,
                                                                     mockedProvisioningDeviceClientStatusCallback, null);
        testProvisioningTask.setRegistrationCallback(mockedProvisioningDeviceClientRegistrationCallback, null);
        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_UNAUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_AUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNING, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ERROR, (Exception) any, null);
                times = 1;
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationInfo)any, null);
                times = 1;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }

    @Test
    public void invokeStatusAssigningToDisabledSucceeds() throws Exception
    {
        // arrange
        // Register expectations
        new NonStrictExpectations()
        {
            {
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_REGISTRATION, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
            }
        };

        // Moving from status register assigning to status assigning
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedResponseParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
            }
        };

        // Moving from status assigning to status failed
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
            }
        };

        // State machine expectations
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "disabled";
                mockedResponseParser.getStatus();
                result = "disabled";
                mockedResponseParser.getStatus();
                result = "disabled";
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedResponseParser.getRegistrationStatus();
                result = mockedOperationRegistrationStatusParser;
                mockedOperationRegistrationStatusParser.getErrorMessage();
                result = "Some error Message";
            }
        };
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedSecurityClient, mockedProvisioningDeviceClientContract,
                                                                     mockedProvisioningDeviceClientStatusCallback, null);
        testProvisioningTask.setRegistrationCallback(mockedProvisioningDeviceClientRegistrationCallback, null);
        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_UNAUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_AUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNING, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ERROR, (Exception) any, null);
                times = 1;
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationInfo)any, null);
                times = 1;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }

    @Test
    public void invokeStatusReturnsUnAssignedToDisabledSucceeds() throws Exception
    {
        // arrange
        // Register expectations
        new NonStrictExpectations()
        {
            {
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_REGISTRATION, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
            }
        };

        // Moving from status register unassigned to status unassigned
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "unassigned";
                mockedResponseParser.getStatus();
                result = "unassigned";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedResponseParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
            }
        };

        // Moving from status unassigned to status disabled
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "unassigned";
                mockedResponseParser.getStatus();
                result = "unassigned";
                mockedResponseParser.getStatus();
                result = "unassigned";
                mockedResponseParser.getStatus();
                result = "unassigned";
            }
        };

        // State machine expectations
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "disabled";
                mockedResponseParser.getStatus();
                result = "disabled";
                mockedResponseParser.getStatus();
                result = "disabled";
                mockedResponseParser.getStatus();
                result = "disabled";
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedResponseParser.getRegistrationStatus();
                result = mockedOperationRegistrationStatusParser;
                mockedOperationRegistrationStatusParser.getAssignedHub();
                result = TEST_HUB;
                mockedOperationRegistrationStatusParser.getDeviceId();
                result = TEST_DEVICE_ID;
            }
        };
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedSecurityClient, mockedProvisioningDeviceClientContract,
                                                                     mockedProvisioningDeviceClientStatusCallback, null);
        testProvisioningTask.setRegistrationCallback(mockedProvisioningDeviceClientRegistrationCallback, null);
        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_UNAUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_AUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNING, null, null);
                times = 2;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNED, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ERROR, (Exception) any, null);
                times = 1;
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationInfo)any, null);
                times = 1;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }

    @Test
    public void invokeStatusReturnsUnAssignedToAssigningSucceeds() throws Exception
    {
        // arrange
        // Register expectations
        new NonStrictExpectations()
        {
            {
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_REGISTRATION, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
            }
        };

        // Moving from status register unassigned to status unassigned
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "unassigned";
                mockedResponseParser.getStatus();
                result = "unassigned";
                mockedResponseParser.getStatus();
                result = "unassigned";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedResponseParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
            }
        };

        // Moving from status unassigned to status assigning
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "unassigned";
                mockedResponseParser.getStatus();
                result = "unassigned";
            }
        };

        // Moving from status assigning to status assigned
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigned";
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedResponseParser.getRegistrationStatus();
                result = mockedOperationRegistrationStatusParser;
                mockedOperationRegistrationStatusParser.getAssignedHub();
                result = TEST_HUB;
                mockedOperationRegistrationStatusParser.getDeviceId();
                result = TEST_DEVICE_ID;
            }
        };

        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedSecurityClient, mockedProvisioningDeviceClientContract,
                                                                     mockedProvisioningDeviceClientStatusCallback, null);
        testProvisioningTask.setRegistrationCallback(mockedProvisioningDeviceClientRegistrationCallback, null);
        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_UNAUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_AUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNING, null, null);
                times = 2;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ERROR, (Exception) any, null);
                times = 0;
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationInfo)any, null);
                times = 1;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }

    @Test
    public void invokeStatusUnAssignedToAssignedSucceeds() throws Exception
    {
        // arrange
        // Register expectations
        new NonStrictExpectations()
        {
            {
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_REGISTRATION, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
            }
        };

        // Moving from status register unassigned to status unassigned
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "unassigned";
                mockedResponseParser.getStatus();
                result = "unassigned";
                mockedResponseParser.getStatus();
                result = "unassigned";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedResponseParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
            }
        };

        // Moving from status unassigned to status assigning
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "unassigned";
                mockedResponseParser.getStatus();
                result = "unassigned";
            }
        };

        // Moving from status assigning to status assigned
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigned";
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedResponseParser.getRegistrationStatus();
                result = mockedOperationRegistrationStatusParser;
                mockedOperationRegistrationStatusParser.getAssignedHub();
                result = TEST_HUB;
                mockedOperationRegistrationStatusParser.getDeviceId();
                result = TEST_DEVICE_ID;
            }
        };

        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedSecurityClient, mockedProvisioningDeviceClientContract,
                                                                     mockedProvisioningDeviceClientStatusCallback, null);
        testProvisioningTask.setRegistrationCallback(mockedProvisioningDeviceClientRegistrationCallback, null);

        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_UNAUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_AUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNING, null, null);
                times = 2;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ERROR, (Exception) any, null);
                times = 0;
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationInfo)any, null);
                times = 1;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }

    @Test
    public void statusCallBackIsNotMandatory() throws Exception
    {
        // arrange
        // Register expectations
        new NonStrictExpectations()
        {
            {
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_REGISTRATION, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
            }
        };

        // Moving from status register unassigned to status unassigned
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "unassigned";
                mockedResponseParser.getStatus();
                result = "unassigned";
                mockedResponseParser.getStatus();
                result = "unassigned";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedResponseParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
            }
        };

        // Moving from status unassigned to status assigning
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "unassigned";
                mockedResponseParser.getStatus();
                result = "unassigned";
            }
        };

        // Moving from status assigning to status assigned
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigned";
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedResponseParser.getRegistrationStatus();
                result = mockedOperationRegistrationStatusParser;
                mockedOperationRegistrationStatusParser.getAssignedHub();
                result = TEST_HUB;
                mockedOperationRegistrationStatusParser.getDeviceId();
                result = TEST_DEVICE_ID;
            }
        };

        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedSecurityClient, mockedProvisioningDeviceClientContract,
                                                                     null, null);
        testProvisioningTask.setRegistrationCallback(mockedProvisioningDeviceClientRegistrationCallback, null);
        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_UNAUTHENTICATED, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_AUTHENTICATED, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNING, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNED, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ERROR, (Exception) any, null);
                times = 0;
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationInfo)any, null);
                times = 1;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }

    @Test
    public void registrationCallbackThrowsIfNoneProvided() throws Exception
    {
        // arrange
        // Register expectations
        new NonStrictExpectations()
        {
            {
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_REGISTRATION, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
            }
        };

        // Moving from status register unassigned to status unassigned
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "unassigned";
                mockedResponseParser.getStatus();
                result = "unassigned";
                mockedResponseParser.getStatus();
                result = "unassigned";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedResponseParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
            }
        };

        // Moving from status unassigned to status assigning
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "unassigned";
                mockedResponseParser.getStatus();
                result = "unassigned";
            }
        };

        // Moving from status assigning to status assigned
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigned";
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedResponseParser.getRegistrationStatus();
                result = mockedOperationRegistrationStatusParser;
                mockedOperationRegistrationStatusParser.getAssignedHub();
                result = TEST_HUB;
                mockedOperationRegistrationStatusParser.getDeviceId();
                result = TEST_DEVICE_ID;
            }
        };

        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedSecurityClient, mockedProvisioningDeviceClientContract,
                                                                     mockedProvisioningDeviceClientStatusCallback, null);
        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_UNAUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_AUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNING, null, null);
                times = 2;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ERROR, (Exception) any, null);
                times = 1;
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationInfo)any, null);
                times = 0;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }

    @Test
    public void invokeStatusTriggersStatusCBOnNullParser() throws Exception
    {
        // arrange
        // Register expectations
        new NonStrictExpectations()
        {
            {
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_REGISTRATION, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
            }
        };

        // Moving from status register unassigned to status unassigned
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "unassigned";
                mockedResponseParser.getStatus();
                result = "unassigned";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedResponseParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = null;
            }
        };

        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedSecurityClient, mockedProvisioningDeviceClientContract,
                                                                     mockedProvisioningDeviceClientStatusCallback, null);
        testProvisioningTask.setRegistrationCallback(mockedProvisioningDeviceClientRegistrationCallback, null);
        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_UNAUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_AUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNING, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNED, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ERROR, (Exception) any, null);
                times = 1;
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationInfo)any, null);
                times = 0;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }

    @Test
    public void invokeStatusTriggersStatusCBOnNullStatus() throws Exception
    {
        // arrange
        // Register expectations
        new NonStrictExpectations()
        {
            {
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_REGISTRATION, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
            }
        };

        // Moving from status register unassigned to status unassigned
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = null;
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedResponseParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
            }
        };

        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedSecurityClient, mockedProvisioningDeviceClientContract,
                                                                     mockedProvisioningDeviceClientStatusCallback, null);
        testProvisioningTask.setRegistrationCallback(mockedProvisioningDeviceClientRegistrationCallback, null);
        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_UNAUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_AUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNING, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNED, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ERROR, (Exception) any, null);
                times = 1;
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationInfo)any, null);
                times = 0;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }

    @Test
    public void invokeStatusTriggersStatusCBOnInaccurateStatus() throws Exception
    {
        // arrange
        // Register expectations
        new NonStrictExpectations()
        {
            {
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_REGISTRATION, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
            }
        };

        // Moving from status register unassigned to status unassigned
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "assigning";
                mockedResponseParser.getStatus();
                result = "blah";
                mockedResponseParser.getStatus();
                result = "blah";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedResponseParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
            }
        };

        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedSecurityClient, mockedProvisioningDeviceClientContract,
                                                                     mockedProvisioningDeviceClientStatusCallback, null);
        testProvisioningTask.setRegistrationCallback(mockedProvisioningDeviceClientRegistrationCallback, null);
        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_UNAUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_AUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNING, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNED, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ERROR, (Exception) any, null);
                times = 1;
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationInfo)any, null);
                times = 0;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }

    @Test
    public void invokeRegisterTriggersStatusCBOnInaccurateStatus() throws Exception
    {
        // arrange
        // Register expectations
        new NonStrictExpectations()
        {
            {
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_REGISTRATION, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
            }
        };

        // Moving from status register unassigned to status unassigned
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = "blah";
            }
        };

        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedSecurityClient, mockedProvisioningDeviceClientContract,
                                                                     mockedProvisioningDeviceClientStatusCallback, null);
        testProvisioningTask.setRegistrationCallback(mockedProvisioningDeviceClientRegistrationCallback, null);
        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_UNAUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_AUTHENTICATED, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNING, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNED, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ERROR, (Exception) any, null);
                times = 1;
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationInfo)any, null);
                times = 0;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }

    @Test
    public void invokeRegisterTriggersStatusCBOnNullResponse() throws Exception
    {
        // arrange
        // Register expectations
        new NonStrictExpectations()
        {
            {
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_REGISTRATION, TimeUnit.MILLISECONDS);
                result = null;
            }
        };

        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedSecurityClient, mockedProvisioningDeviceClientContract,
                                                                     mockedProvisioningDeviceClientStatusCallback, null);
        testProvisioningTask.setRegistrationCallback(mockedProvisioningDeviceClientRegistrationCallback, null);
        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_UNAUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_AUTHENTICATED, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNING, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNED, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ERROR, (Exception) any, null);
                times = 1;
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationInfo)any, null);
                times = 0;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }

    @Test
    public void invokeRegisterTriggersStatusCBOnNullStatus() throws Exception
    {
        // arrange
        // Register expectations
        new NonStrictExpectations()
        {
            {
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_REGISTRATION, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
            }
        };

        // Moving from status register unassigned to status unassigned
        new StrictExpectations()
        {
            {
                mockedResponseParser.getStatus();
                result = null;
            }
        };

        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedSecurityClient, mockedProvisioningDeviceClientContract,
                                                                     mockedProvisioningDeviceClientStatusCallback, null);
        testProvisioningTask.setRegistrationCallback(mockedProvisioningDeviceClientRegistrationCallback, null);
        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_UNAUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_AUTHENTICATED, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNING, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNED, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ERROR, (Exception) any, null);
                times = 1;
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationInfo)any, null);
                times = 0;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }

    @Test
    public void invokeRegisterTriggersStatusCBOnNullOperationId() throws Exception
    {
        // arrange
        // Register expectations
        new NonStrictExpectations()
        {
            {
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_REGISTRATION, TimeUnit.MILLISECONDS);
                result = mockedResponseParser;
                mockedResponseParser.getStatus();
                result = "assigned";
                mockedResponseParser.getOperationId();
                result = null;
            }
        };

        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedSecurityClient, mockedProvisioningDeviceClientContract,
                                                                     mockedProvisioningDeviceClientStatusCallback, null);
        testProvisioningTask.setRegistrationCallback(mockedProvisioningDeviceClientRegistrationCallback, null);
        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_UNAUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_AUTHENTICATED, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNING, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNED, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ERROR, (Exception) any, null);
                times = 1;
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationInfo)any, null);
                times = 0;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }

    //SRS_provisioningtask_25_006: [ This method shall invoke the status callback, if any of the task fail or throw any exception. ]
    @Test
    public void timeoutExceptionTriggersStatusCallback() throws Exception
    {
        // arrange
        // Register expectations
        new NonStrictExpectations()
        {
            {
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_REGISTRATION, TimeUnit.MILLISECONDS);
                result = new TimeoutException();
            }
        };

        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedSecurityClient, mockedProvisioningDeviceClientContract,
                                                                     mockedProvisioningDeviceClientStatusCallback, null);
        testProvisioningTask.setRegistrationCallback(mockedProvisioningDeviceClientRegistrationCallback, null);
        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_UNAUTHENTICATED, null, null);
                times = 1;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_AUTHENTICATED, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNING, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ASSIGNED, null, null);
                times = 0;
                mockedProvisioningDeviceClientStatusCallback.run(DPS_DEVICE_STATUS_ERROR, (Exception) any, null);
                times = 1;
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationInfo)any, null);
                times = 0;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }

    //SRS_provisioningtask_25_014: [ This method shall shutdown the executors if they have not already shutdown. ]
    @Test
    public void closeShutsDownExecutorNow() throws Exception
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockedExecutorService.isShutdown();
                result = false;
            }
        };

        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedSecurityClient, mockedProvisioningDeviceClientContract,
                                                                     mockedProvisioningDeviceClientStatusCallback, null);
        testProvisioningTask.setRegistrationCallback(mockedProvisioningDeviceClientRegistrationCallback, null);
        //act
        testProvisioningTask.close();

        new Verifications()
        {
            {
                mockedProvisioningDeviceClientContract.close();
                times = 1;
                mockedExecutorService.shutdownNow();
                times = 1;
            }
        };
    }

    @Test
    public void closeShutsDownExecutor() throws Exception
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockedExecutorService.isShutdown();
                result = true;
            }
        };

        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedSecurityClient, mockedProvisioningDeviceClientContract,
                                                                     mockedProvisioningDeviceClientStatusCallback, null);
        testProvisioningTask.setRegistrationCallback(mockedProvisioningDeviceClientRegistrationCallback, null);
        //act
        testProvisioningTask.close();

        new Verifications()
        {
            {
                mockedProvisioningDeviceClientContract.close();
                times = 1;
                mockedExecutorService.shutdownNow();
                times = 0;
            }
        };
    }
}
