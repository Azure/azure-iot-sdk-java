/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
*/



package tests.unit.com.microsoft.azure.sdk.iot.provisioning.device.internal.task;

import com.microsoft.azure.sdk.iot.provisioning.device.*;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.ProvisioningDeviceClientConfig;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.RegistrationOperationStatusParser;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.TpmRegistrationResultParser;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ProvisioningDeviceClientContract;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.DeviceRegistrationResultParser;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.*;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderTpm;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.*;

import static com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientStatus.*;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/*
  Unit test for ProvisioningTask
  Coverage : 100% method, 99% line
*/

@SuppressWarnings("ThrowableNotThrown")
@RunWith(JMockit.class)
public class ProvisioningTaskTest
{
    private static final String TEST_OPERATION_ID = "testOperationId";
    private static final int MAX_TIME_TO_WAIT_FOR_REGISTRATION = 1000000;
    private static final int MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE = 10000;
    private static final String TEST_HUB = "TestHub";
    private static final String TEST_DEVICE_ID = "testDeviceId";

    @Mocked
    SecurityProvider mockedSecurityProvider;
    @Mocked
    SecurityProviderTpm mockedSecurityProviderTpm;
    @Mocked
    TpmRegistrationResultParser mockedTpm;
    @Mocked
    ProvisioningDeviceClientContract mockedProvisioningDeviceClientContract;
    @Mocked
    ProvisioningDeviceClientConfig mockedProvisioningDeviceClientConfig;
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
    RegistrationOperationStatusParser mockedRegistrationOperationStatusParser;
    @Mocked
    DeviceRegistrationResultParser mockedDeviceRegistrationResultParser;
    @Mocked
    Future<?> mockedFuture;
    @Mocked
    RequestData mockedRequestData;
    @Mocked
    RegistrationResult mockedRegistrationData;
    @Mocked
    Thread mockedThread;
    @Mocked
    ProvisioningDeviceClientException mockProvisioningDeviceClientException;
    @Mocked
    SecurityProviderException mockSecurityProviderException;

    private void constructorExpectations()
    {
        new NonStrictExpectations()
        {
            {
                mockedProvisioningDeviceClientConfig.getRegistrationCallback();
                result = mockedProvisioningDeviceClientRegistrationCallback;
                mockedProvisioningDeviceClientConfig.getRegistrationCallbackContext();
                result = null;
            }
        };
    }

    //SRS_provisioningtask_25_001: [ Constructor shall save provisioningDeviceClientConfig , securityProvider, provisioningDeviceClientContract, provisioningDeviceClientStatusCallback, dpsStatusCallbackContext.]
    //SRS_ProvisioningTask_25_015: [ Constructor shall start the executor with a fixed thread pool of size 2.]
    //SRS_provisioningtask_25_003: [ Constructor shall trigger status callback if provided with status PROVISIONING_DEVICE_STATUS_UNAUTHENTICATED.]
    @Test
    public void constructorSucceeds() throws ProvisioningDeviceClientException
    {
        //arrange
        constructorExpectations();
        //act
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedProvisioningDeviceClientContract);
        //assert
        new Verifications()
        {
            {
                Executors.newFixedThreadPool(2);
                times = 1;
            }
        };
        assertEquals(mockedProvisioningDeviceClientConfig, Deencapsulation.getField(testProvisioningTask, "provisioningDeviceClientConfig"));
        assertEquals(mockedSecurityProvider, Deencapsulation.getField(testProvisioningTask, "securityProvider"));
        assertEquals(mockedProvisioningDeviceClientContract, Deencapsulation.getField(testProvisioningTask, "provisioningDeviceClientContract"));
        assertNotNull(Deencapsulation.getField(testProvisioningTask, "authorization"));
    }

    @Test
    public void invokeRegisterReturnsAssignedSucceeds() throws Exception
    {
        // arrange
        constructorExpectations();
        // Register expectations
        registerExpectations();

        // Moving from status register assigning to status assigned
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getRegistrationState();
                result = mockedDeviceRegistrationResultParser;
                mockedDeviceRegistrationResultParser.getAssignedHub();
                result = TEST_HUB;
                mockedDeviceRegistrationResultParser.getDeviceId();
                result = TEST_DEVICE_ID;
                Deencapsulation.newInstance(RegistrationResult.class, new Class[] {String.class, String.class, String.class, ProvisioningDeviceClientStatus.class},
                        any, any, any, PROVISIONING_DEVICE_STATUS_ASSIGNED);
                result = mockedRegistrationData;
            }
        };
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig,
                mockedProvisioningDeviceClientContract);
        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationResult)any, null, null);
                maxTimes = 1;
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
        constructorExpectations();
        // Register expectations
        registerExpectations();

        // Moving from status register assigning to status assigned
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "failed";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "failed";
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getRegistrationState();
                result = mockedDeviceRegistrationResultParser;
                mockedDeviceRegistrationResultParser.getErrorMessage();
                result = "Test Error Message";
                Deencapsulation.newInstance(RegistrationResult.class, new Class[] {String.class, String.class, String.class, ProvisioningDeviceClientStatus.class},
                        any, any, any, PROVISIONING_DEVICE_STATUS_FAILED);
                result = mockedRegistrationData;
            }
        };
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig,
                mockedProvisioningDeviceClientContract);
        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationResult)any, (Exception) any, null);
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
        constructorExpectations();
        // Register expectations
        registerExpectations();

        // Moving from status register assigning to status assigned
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "disabled";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "disabled";
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getRegistrationState();
                result = mockedDeviceRegistrationResultParser;
                mockedDeviceRegistrationResultParser.getErrorMessage();
                result = "Test Error Message";
                Deencapsulation.newInstance(RegistrationResult.class, new Class[] {String.class, String.class, String.class, ProvisioningDeviceClientStatus.class},
                        any, any, any, PROVISIONING_DEVICE_STATUS_DISABLED);
                result = mockedRegistrationData;
            }
        };
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig,
                mockedProvisioningDeviceClientContract);
        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationResult)any, (Exception) any,
                        null);
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
        constructorExpectations();
        // Register expectations
        registerExpectations();

        // Moving from status register assigning to status assigning
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedRegistrationOperationStatusParser;
            }
        };

        // Moving from status assigning to status assigned
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
            }
        };

        // State machine expectations
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getRegistrationState();
                result = mockedDeviceRegistrationResultParser;
                mockedDeviceRegistrationResultParser.getAssignedHub();
                result = TEST_HUB;
                mockedDeviceRegistrationResultParser.getDeviceId();
                result = TEST_DEVICE_ID;
                Deencapsulation.newInstance(RegistrationResult.class, new Class[] {String.class, String.class, String.class, ProvisioningDeviceClientStatus.class},
                        TEST_HUB, TEST_DEVICE_ID, any, PROVISIONING_DEVICE_STATUS_ASSIGNED);
                result = mockedRegistrationData;
            }
        };
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig,
                mockedProvisioningDeviceClientContract);

        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationResult)any, null, null);
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
        constructorExpectations();
        // Register expectations
        registerExpectations();

        // Moving from status register assigning to status assigning
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedRegistrationOperationStatusParser;
            }
        };

        // Moving from status assigning to status failed
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
            }
        };

        // State machine expectations
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "failed";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "failed";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "failed";
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getRegistrationState();
                result = mockedDeviceRegistrationResultParser;
                mockedDeviceRegistrationResultParser.getErrorMessage();
                result = "Some error Message";
                Deencapsulation.newInstance(RegistrationResult.class, new Class[] {String.class, String.class, String.class, ProvisioningDeviceClientStatus.class},
                        any, any, any, PROVISIONING_DEVICE_STATUS_FAILED);
                result = mockedRegistrationData;
            }
        };
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig,
                mockedProvisioningDeviceClientContract);

        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationResult)any,(Exception) any, null);
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
        constructorExpectations();
        // Register expectations
        registerExpectations();

        // Moving from status register assigning to status assigning
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedRegistrationOperationStatusParser;
            }
        };

        // Moving from status assigning to status failed
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
            }
        };

        // State machine expectations
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "disabled";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "disabled";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "disabled";
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getRegistrationState();
                result = mockedDeviceRegistrationResultParser;
                mockedDeviceRegistrationResultParser.getErrorMessage();
                result = "Some error Message";
                Deencapsulation.newInstance(RegistrationResult.class, new Class[] {String.class, String.class, String.class, ProvisioningDeviceClientStatus.class},
                        any, any, any, PROVISIONING_DEVICE_STATUS_DISABLED);
                result = mockedRegistrationData;
            }
        };
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig,
                mockedProvisioningDeviceClientContract);

        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationResult)any, (Exception) any,null);
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
        constructorExpectations();
        // Register expectations
        registerExpectations();

        // Moving from status register unassigned to status unassigned
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "unassigned";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "unassigned";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedRegistrationOperationStatusParser;
            }
        };

        // Moving from status unassigned to status disabled
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "unassigned";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "unassigned";
            }
        };

        // State machine expectations
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "disabled";
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getRegistrationState();
                result = mockedDeviceRegistrationResultParser;
                mockedDeviceRegistrationResultParser.getErrorMessage();
                result = "Test Error Message";
                Deencapsulation.newInstance(RegistrationResult.class, new Class[] {String.class, String.class, String.class, ProvisioningDeviceClientStatus.class},
                        null, null, any, PROVISIONING_DEVICE_STATUS_DISABLED);
                result = mockedRegistrationData;
            }
        };
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig,
                mockedProvisioningDeviceClientContract);

        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationResult)any,  (Exception) any,null);
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
        constructorExpectations();
        // Register expectations
        registerExpectations();

        // Moving from status register unassigned to status unassigned
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "unassigned";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "unassigned";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "unassigned";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedRegistrationOperationStatusParser;
            }
        };

        // Moving from status unassigned to status assigning
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "unassigned";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "unassigned";
            }
        };

        // Moving from status assigning to status assigned
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getRegistrationState();
                result = mockedDeviceRegistrationResultParser;
                mockedDeviceRegistrationResultParser.getAssignedHub();
                result = TEST_HUB;
                mockedDeviceRegistrationResultParser.getDeviceId();
                result = TEST_DEVICE_ID;
                Deencapsulation.newInstance(RegistrationResult.class, new Class[] {String.class, String.class, String.class, ProvisioningDeviceClientStatus.class},
                        TEST_HUB, TEST_DEVICE_ID, any, PROVISIONING_DEVICE_STATUS_ASSIGNED);
                result = mockedRegistrationData;
            }
        };

        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig,
                mockedProvisioningDeviceClientContract);

        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationResult)any, (Exception) any,null);
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
        constructorExpectations();
        // Register expectations
        registerExpectations();

        // Moving from status register unassigned to status unassigned
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "unassigned";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "unassigned";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "unassigned";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedRegistrationOperationStatusParser;
            }
        };

        // Moving from status unassigned to status assigning
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "unassigned";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "unassigned";
            }
        };

        // Moving from status assigning to status assigned
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getRegistrationState();
                result = mockedDeviceRegistrationResultParser;
                mockedDeviceRegistrationResultParser.getAssignedHub();
                result = TEST_HUB;
                mockedDeviceRegistrationResultParser.getDeviceId();
                result = TEST_DEVICE_ID;
                Deencapsulation.newInstance(RegistrationResult.class, new Class[] {String.class, String.class, String.class, ProvisioningDeviceClientStatus.class},
                        TEST_HUB, TEST_DEVICE_ID, any, PROVISIONING_DEVICE_STATUS_ASSIGNED);
                result = mockedRegistrationData;
            }
        };

        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig,
                mockedProvisioningDeviceClientContract);

        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationResult)any, (Exception) any, null);
                times = 1;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void registrationCallbackThrowsIfNoneProvided() throws Exception
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                mockedProvisioningDeviceClientConfig.getRegistrationCallback();
                result = null;
                mockedProvisioningDeviceClientConfig.getRegistrationCallbackContext();
                result = null;
            }
        };
        //act
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig,
                mockedProvisioningDeviceClientContract);
    }

    @Test
    public void nextStatusNullThrowsException() throws Exception
    {
        // arrange
        constructorExpectations();
        // Register expectations
        registerExpectations();

        // Moving from status register assigning to status assigned
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "failed";
                mockedRegistrationOperationStatusParser.getStatus();
                result = null;
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getRegistrationState();
                result = mockedDeviceRegistrationResultParser;
                mockedDeviceRegistrationResultParser.getErrorMessage();
                result = "Test Error Message";
                Deencapsulation.newInstance(RegistrationResult.class, new Class[] {String.class, String.class, String.class, ProvisioningDeviceClientStatus.class},
                        any, any, any, PROVISIONING_DEVICE_STATUS_ERROR);
                result = mockedRegistrationData;
            }
        };
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig,
                mockedProvisioningDeviceClientContract);
        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationResult)any, (Exception) any, null);
                times = 1;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }


    @Test
    public void invokeStatusTriggersRegistrationCBOnNullParser() throws Exception
    {
        // arrange
        constructorExpectations();
        // Register expectations
        registerExpectations();

        // Moving from status register unassigned to status unassigned
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "unassigned";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "unassigned";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = null;
                Deencapsulation.newInstance(RegistrationResult.class, new Class[] {String.class, String.class, String.class, ProvisioningDeviceClientStatus.class},
                        null, null, any, PROVISIONING_DEVICE_STATUS_ERROR);
                result = mockedRegistrationData;
            }
        };

        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig,
                mockedProvisioningDeviceClientContract);

        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationResult)any, (Exception) any, null);
                times = 1;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }

    @Test
    public void invokeStatusTriggersRegistrationCBOnNullStatus() throws Exception
    {
        // arrange
        constructorExpectations();
        // Register expectations
        registerExpectations();

        // Moving from status register unassigned to status unassigned
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = null;
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedRegistrationOperationStatusParser;
                Deencapsulation.newInstance(RegistrationResult.class, new Class[] {String.class, String.class, String.class, ProvisioningDeviceClientStatus.class},
                        null, null, any, PROVISIONING_DEVICE_STATUS_ERROR);
                result = mockedRegistrationData;
            }
        };

        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedProvisioningDeviceClientContract);
        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationResult)any, (Exception) any, null);
                times = 1;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }

    @Test
    public void invokeStatusTriggersRegistrationCBOnInaccurateStatus() throws Exception
    {
        // arrange
        constructorExpectations();
        // Register expectations
        registerExpectations();

        // Moving from status register unassigned to status unassigned
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "blah";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "blah";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedRegistrationOperationStatusParser;
                Deencapsulation.newInstance(RegistrationResult.class, new Class[] {String.class, String.class, String.class, ProvisioningDeviceClientStatus.class},
                        null, null, any, PROVISIONING_DEVICE_STATUS_ERROR);
                result = mockedRegistrationData;
            }
        };

        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedProvisioningDeviceClientContract);
        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationResult)any, (Exception) any, null);
                times = 1;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }

    @Test
    public void invokeRegisterTriggersRegistrationCBOnInaccurateStatus() throws Exception
    {
        // arrange
        constructorExpectations();
        // Register expectations
        new NonStrictExpectations()
        {
            {
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_REGISTRATION, TimeUnit.MILLISECONDS);
                result = mockedRegistrationOperationStatusParser;
                Deencapsulation.newInstance(RegistrationResult.class, new Class[] {String.class, String.class, String.class, ProvisioningDeviceClientStatus.class},
                                            null, null, null, PROVISIONING_DEVICE_STATUS_ERROR);
                result = mockedRegistrationData;
            }
        };

        // Moving from status register unassigned to status unassigned
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "blah";
            }
        };

        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedProvisioningDeviceClientContract);
        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationResult)any, (Exception) any, null);
                times = 1;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }


    @Test
    public void invokeRegisterTriggersRegistrationCBOnNullResponse() throws Exception
    {
        // arrange
        constructorExpectations();

        // Register expectations
        new NonStrictExpectations()
        {
            {
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_REGISTRATION, TimeUnit.MILLISECONDS);
                result = null;
                Deencapsulation.newInstance(RegistrationResult.class, new Class[] {String.class, String.class, String.class, ProvisioningDeviceClientStatus.class},
                        null, null, any, PROVISIONING_DEVICE_STATUS_ERROR);
                result = mockedRegistrationData;
            }
        };

        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedProvisioningDeviceClientContract);

        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationResult)any, (Exception) any, null);
                times = 1;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }

    @Test
    public void invokeRegisterTriggersCBOnNullStatus() throws Exception
    {
        // arrange
        constructorExpectations();
        // Register expectations
        new NonStrictExpectations()
        {
            {
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_REGISTRATION, TimeUnit.MILLISECONDS);
                result = mockedRegistrationOperationStatusParser;
                Deencapsulation.newInstance(RegistrationResult.class, new Class[] {String.class, String.class, String.class, ProvisioningDeviceClientStatus.class},
                                            null, null, any, PROVISIONING_DEVICE_STATUS_ERROR);
                result = mockedRegistrationData;
            }
        };

        // Moving from status register unassigned to status unassigned
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = null;
            }
        };

        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedProvisioningDeviceClientContract);
        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationResult)any, (Exception) any, null);
                times = 1;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }

    @Test
    public void invokeRegisterTriggersCBOnNullOperationId() throws Exception
    {
        // arrange
        constructorExpectations();
        // Register expectations
        new NonStrictExpectations()
        {
            {
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_REGISTRATION, TimeUnit.MILLISECONDS);
                result = mockedRegistrationOperationStatusParser;
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
                mockedRegistrationOperationStatusParser.getOperationId();
                result = null;
                Deencapsulation.newInstance(RegistrationResult.class, new Class[] {String.class, String.class, String.class, ProvisioningDeviceClientStatus.class},
                        null, null, any, PROVISIONING_DEVICE_STATUS_ERROR);
                result = mockedRegistrationData;
            }
        };

        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedProvisioningDeviceClientContract);
        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationResult)any, (Exception) any, null);
                times = 1;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }

    //SRS_provisioningtask_25_006: [ This method shall invoke the status callback, if any of the task fail or throw any exception. ]
    @Test
    public void timeoutExceptionTriggersCallback() throws Exception
    {
        // arrange
        constructorExpectations();
        // Register expectations
        registerExpectations();

        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedProvisioningDeviceClientContract);
        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationResult)any, (Exception) any, null);
                times = 1;
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
        constructorExpectations();
        new NonStrictExpectations()
        {
            {
                mockedExecutorService.isShutdown();
                result = false;
            }
        };

        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedProvisioningDeviceClientContract);
        //act
        Deencapsulation.invoke(testProvisioningTask, "close");

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
        constructorExpectations();
        new NonStrictExpectations()
        {
            {
                mockedExecutorService.isShutdown();
                result = true;
            }
        };

        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedProvisioningDeviceClientContract);

        //act
        Deencapsulation.invoke(testProvisioningTask, "close");

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

    //SRS_provisioningtask_25_002: [ Constructor throw ProvisioningDeviceClientException if provisioningDeviceClientConfig , securityProvider or provisioningDeviceClientContract is null.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void constructorThrowsOnNullConfig() throws ProvisioningDeviceClientException
    {
        //arrange
        //act
        ProvisioningTask testProvisioningTask = new ProvisioningTask(null, mockedProvisioningDeviceClientContract);
        //assert
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void constructorThrowsOnNullSecurityProvider() throws ProvisioningDeviceClientException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockedProvisioningDeviceClientConfig.getSecurityProvider();
                result = null;
            }
        };

        //act
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedProvisioningDeviceClientContract);
        //assert
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void constructorThrowsOnNullContract() throws ProvisioningDeviceClientException
    {
        //arrange
        //act
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, null);
        //assert
    }

    //SRS_provisioningtask_25_007: [ This method shall invoke Register task and status task to execute the state machine of the service as per below rules.]
    /*    Service State Machine Rules

    SRS_provisioningtask_25_008: [ This method shall invoke register task and wait for it to complete.]
    SRS_provisioningtask_25_009: [ This method shall invoke status callback with status PROVISIONING_DEVICE_STATUS_AUTHENTICATED if register task completes successfully.]
    SRS_provisioningtask_25_010: [ This method shall invoke status task to get the current state of the device registration and wait until a terminal state is reached.]
    SRS_provisioningtask_25_011: [ Upon reaching one of the terminal state i.e ASSIGNED, this method shall invoke registration callback with the information retrieved from service for IotHub Uri and DeviceId. Also if status callback is defined then it shall be invoked with status PROVISIONING_DEVICE_STATUS_ASSIGNED.]
    SRS_provisioningtask_25_012: [ Upon reaching one of the terminal states i.e FAILED or DISABLED, this method shall invoke registration callback with error message received from service. Also if status callback is defined then it shall be invoked with status PROVISIONING_DEVICE_STATUS_ERROR.]
    SRS_provisioningtask_25_013: [ Upon reaching intermediate state i.e UNASSIGNED or ASSIGNING, this method shall continue to query for status until a terminal state is reached. Also if status callback is defined then it shall be invoked with status PROVISIONING_DEVICE_STATUS_ASSIGNING.]
    */
    @Test
    public void invokeRegisterReturnsUnassignedToAssignedSucceeds() throws Exception, InterruptedException, ExecutionException, TimeoutException, ProvisioningDeviceClientException
    {
        // arrange
        constructorExpectations();
        // Register expectations
        registerExpectations();

        // Moving from status unassigned to assigned
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "unassigned";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "unassigned";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedRegistrationOperationStatusParser;
            }
        };

        // State machine expectations
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getRegistrationState();
                result = mockedDeviceRegistrationResultParser;
                mockedDeviceRegistrationResultParser.getAssignedHub();
                result = TEST_HUB;
                mockedDeviceRegistrationResultParser.getDeviceId();
                result = TEST_DEVICE_ID;
                Deencapsulation.newInstance(RegistrationResult.class, new Class[] {String.class, String.class, String.class, ProvisioningDeviceClientStatus.class},
                        TEST_HUB, TEST_DEVICE_ID, any, PROVISIONING_DEVICE_STATUS_ASSIGNED);
                result = mockedRegistrationData;
            }
        };
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedProvisioningDeviceClientContract);

        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationResult)any, (Exception) any, null);
                times = 1;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
            }
        };
    }

    //SRS_ProvisioningTask_34_016: [ Upon reaching the terminal state ASSIGNED, if the saved security client is an instance of SecurityClientTpm, the security client shall decrypt and store the authentication key from the statusResponseParser.]
    @Test
    public void invokeRegisterReturnsAssigningToStatusAssignedSucceedsWithTPMSecurityProvider() throws Exception
    {
        // arrange
        constructorExpectations();

        // Register expectations
        registerExpectations();

        // Moving from status register assigning to status assigned
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedRegistrationOperationStatusParser;
            }
        };

        // State machine expectations
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getRegistrationState();
                result = mockedDeviceRegistrationResultParser;
                mockedDeviceRegistrationResultParser.getAssignedHub();
                result = TEST_HUB;
                mockedDeviceRegistrationResultParser.getDeviceId();
                result = TEST_DEVICE_ID;
                Deencapsulation.newInstance(RegistrationResult.class, new Class[] {String.class, String.class, String.class, ProvisioningDeviceClientStatus.class},
                        TEST_HUB, TEST_DEVICE_ID, any, PROVISIONING_DEVICE_STATUS_ASSIGNED);
                result = mockedRegistrationData;
                mockedDeviceRegistrationResultParser.getTpm();
                result = mockedTpm;
                mockedTpm.getAuthenticationKey();
                result = encodeBase64String("some auth key".getBytes());
                mockedProvisioningDeviceClientConfig.getSecurityProvider();
                result = mockedSecurityProviderTpm;
            }
        };
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedProvisioningDeviceClientContract);

        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationResult)any, null, null);
                times = 1;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
                mockedTpm.getAuthenticationKey();
                times = 3; //called twice for verifying it is not null or empty, once for actual use
                mockedSecurityProviderTpm.activateIdentityKey((byte[]) any);
                times = 1;
            }
        };
    }

    //SRS_ProvisioningTask_34_016: [ Upon reaching the terminal state ASSIGNED, if the saved security client is an instance of SecurityClientTpm, the security client shall decrypt and store the authentication key from the statusResponseParser.]
    @Test
    public void invokeRegisterReturnsAssigningToStatusAssignedSucceedsWithNonTpmSecurityProvider() throws Exception
    {
        // arrange
        constructorExpectations();

        // Register expectations
        registerExpectations();

        // Moving from status register assigning to status assigned
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedRegistrationOperationStatusParser;
            }
        };

        // State machine expectations
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getRegistrationState();
                result = mockedDeviceRegistrationResultParser;
                mockedDeviceRegistrationResultParser.getAssignedHub();
                result = TEST_HUB;
                mockedDeviceRegistrationResultParser.getDeviceId();
                result = TEST_DEVICE_ID;
                Deencapsulation.newInstance(RegistrationResult.class, new Class[] {String.class, String.class, String.class, ProvisioningDeviceClientStatus.class},
                        TEST_HUB, TEST_DEVICE_ID, any, PROVISIONING_DEVICE_STATUS_ASSIGNED);
                result = mockedRegistrationData;
                mockedDeviceRegistrationResultParser.getTpm();
                result = mockedTpm;
                mockedTpm.getAuthenticationKey();
                result = "some auth key";
                mockedProvisioningDeviceClientConfig.getSecurityProvider();
                result = mockedSecurityProvider;
            }
        };
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedProvisioningDeviceClientContract);

        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientRegistrationCallback.run((ProvisioningDeviceClientRegistrationResult)any, null, null);
                times = 1;
                mockedProvisioningDeviceClientContract.open((RequestData) any);
                times = 1;
                mockedProvisioningDeviceClientContract.close();
                times = 1;
                mockedTpm.getAuthenticationKey();
                times = 0;
            }
        };
    }

    //Tests_SRS_ProvisioningTask_34_017: [Upon reaching the terminal state ASSIGNED, if the saved security client is an instance of SecurityClientTpm and if the registration status json does not contain an authentication key, this function shall throw a ProvisioningDeviceClientException.]
    @Test
    public void invokeRegisterReturnsAssigningToStatusAssignedThrowsForMissingTPMJson() throws Exception
    {
        // arrange
        constructorExpectations();

        // Register expectations
        registerExpectations();

        // Moving from status register assigning to status assigned
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedRegistrationOperationStatusParser;
            }
        };

        // State machine expectations
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
            }
        };

        registrationStatusJsonExpectations(TEST_HUB, TEST_DEVICE_ID, null, null);
        new NonStrictExpectations()
        {
            {
                mockedProvisioningDeviceClientConfig.getSecurityProvider();
                result = mockedSecurityProviderTpm;
                new ProvisioningDeviceClientException(anyString);
                result = mockProvisioningDeviceClientException;
            }
        };
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedProvisioningDeviceClientContract);

        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientRegistrationCallback.run((RegistrationResult) any, mockProvisioningDeviceClientException, any);
                times = 1;
            }
        };
    }

    //Tests_SRS_ProvisioningTask_34_017: [Upon reaching the terminal state ASSIGNED, if the saved security client is an instance of SecurityClientTpm and if the registration status json does not contain an authentication key, this function shall throw a ProvisioningDeviceClientException.]
    @Test
    public void invokeRegisterReturnsAssigningToStatusAssignedThrowsForMissingTPMAuthenticationKeyJson() throws Exception
    {
        // arrange
        constructorExpectations();

        // Register expectations
        registerExpectations();

        // Moving from status register assigning to status assigned
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedRegistrationOperationStatusParser;
            }
        };

        // State machine expectations
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
            }
        };

        registrationStatusJsonExpectations(TEST_HUB, TEST_DEVICE_ID, mockedTpm, null);
        new NonStrictExpectations()
        {
            {
                mockedProvisioningDeviceClientConfig.getSecurityProvider();
                result = mockedSecurityProviderTpm;
                new ProvisioningDeviceClientException(anyString);
                result = mockProvisioningDeviceClientException;
            }
        };
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedProvisioningDeviceClientContract);

        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientRegistrationCallback.run((RegistrationResult) any, mockProvisioningDeviceClientException, any);
                times = 1;
            }
        };
    }

    //Tests_SRS_ProvisioningTask_34_017: [Upon reaching the terminal state ASSIGNED, if the saved security client is an instance of SecurityClientTpm and if the registration status json does not contain an authentication key, this function shall throw a ProvisioningDeviceClientException.]
    @Test
    public void invokeRegisterReturnsAssigningToStatusAssignedThrowsForEmptyTPMAuthenticationKeyJson() throws Exception
    {
        // arrange
        constructorExpectations();

        // Register expectations
        registerExpectations();

        // Moving from status register assigning to status assigned
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedRegistrationOperationStatusParser;
            }
        };

        // State machine expectations
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
            }
        };

        registrationStatusJsonExpectations(TEST_HUB, TEST_DEVICE_ID, mockedTpm, "");
        new NonStrictExpectations()
        {
            {
                mockedProvisioningDeviceClientConfig.getSecurityProvider();
                result = mockedSecurityProviderTpm;
                new ProvisioningDeviceClientException(anyString);
                result = mockProvisioningDeviceClientException;
            }
        };
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedProvisioningDeviceClientContract);

        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientRegistrationCallback.run((RegistrationResult) any, mockProvisioningDeviceClientException, any);
                times = 1;
            }
        };
    }

    //Tests_SRS_ProvisioningTask_34_018: [Upon reaching the terminal state ASSIGNED, if the registration status json is missing an assigned hub or device id, this function shall throw a ProvisioningDeviceClientException.]
    @Test
    public void invokeRegisterReturnsAssigningToStatusAssignedThrowsForMissingDeviceIdJson() throws Exception
    {
        // arrange
        constructorExpectations();
        // Register expectations
        registerExpectations();

        // Moving from status register assigning to status assigned
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedRegistrationOperationStatusParser;
            }
        };

        // State machine expectations
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
            }
        };

        registrationStatusJsonExpectations(TEST_HUB, null, null, null);
        new NonStrictExpectations()
        {
            {
                new ProvisioningDeviceClientException(anyString);
                result = mockProvisioningDeviceClientException;
            }
        };
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedProvisioningDeviceClientContract);

        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientRegistrationCallback.run((RegistrationResult) any, mockProvisioningDeviceClientException, any);
                times = 1;
            }
        };
    }

    //Tests_SRS_ProvisioningTask_34_018: [Upon reaching the terminal state ASSIGNED, if the registration status json is missing an assigned hub or device id, this function shall throw a ProvisioningDeviceClientException.]
    @Test
    public void invokeRegisterReturnsAssigningToStatusAssignedThrowsForEmptyDeviceIdJson() throws Exception
    {
        // arrange
        constructorExpectations();

        // Register expectations
        registerExpectations();

        // Moving from status register assigning to status assigned
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedRegistrationOperationStatusParser;
            }
        };

        // State machine expectations
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
            }
        };

        registrationStatusJsonExpectations(TEST_HUB, "", null, null);
        new NonStrictExpectations()
        {
            {
                new ProvisioningDeviceClientException(anyString);
                result = mockProvisioningDeviceClientException;
            }
        };
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedProvisioningDeviceClientContract);

        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientRegistrationCallback.run((RegistrationResult) any, mockProvisioningDeviceClientException, any);
                times = 1;
            }
        };
    }

    //Tests_SRS_ProvisioningTask_34_018: [Upon reaching the terminal state ASSIGNED, if the registration status json is missing an assigned hub or device id, this function shall throw a ProvisioningDeviceClientException.]
    @Test
    public void invokeRegisterReturnsAssigningToStatusAssignedThrowsForMissingHostnameJson() throws Exception
    {
        // arrange
        constructorExpectations();

        // Register expectations
        registerExpectations();

        // Moving from status register assigning to status assigned
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedRegistrationOperationStatusParser;
            }
        };

        // State machine expectations
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
            }
        };

        registrationStatusJsonExpectations(null, TEST_DEVICE_ID, null, null);
        new NonStrictExpectations()
        {
            {
                new ProvisioningDeviceClientException(anyString);
                result = mockProvisioningDeviceClientException;
            }
        };
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedProvisioningDeviceClientContract);

        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientRegistrationCallback.run((RegistrationResult) any, mockProvisioningDeviceClientException, any);
                times = 1;
            }
        };
    }

    //Tests_SRS_ProvisioningTask_34_018: [Upon reaching the terminal state ASSIGNED, if the registration status json is missing an assigned hub or device id, this function shall throw a ProvisioningDeviceClientException.]
    @Test
    public void invokeRegisterReturnsAssigningToStatusAssignedThrowsForEmptyHostnameJson() throws Exception
    {
        // arrange
        constructorExpectations();
        // Register expectations
        registerExpectations();

        // Moving from status register assigning to status assigned
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedRegistrationOperationStatusParser;
            }
        };

        // State machine expectations
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
            }
        };

        registrationStatusJsonExpectations("", TEST_DEVICE_ID, null, null);
        new NonStrictExpectations()
        {
            {
                new ProvisioningDeviceClientException(anyString);
                result = mockProvisioningDeviceClientException;
            }
        };
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedProvisioningDeviceClientContract);

        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientRegistrationCallback.run((RegistrationResult) any, mockProvisioningDeviceClientException, any);
                times = 1;
            }
        };
    }

    //SRS_ProvisioningTask_25_006: [ This method shall invoke the status callback, if any of the task fail or throw any exception. ]
    @Test
    public void securityClientExceptionThrownHandledByStatusCallback() throws Exception
    {
        // arrange
        constructorExpectations();

        // Register expectations
        registerExpectations();

        // Moving from status register assigning to status assigned
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigning";
            }
        };

        // Invoke Status expectations
        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getOperationId();
                result = TEST_OPERATION_ID;
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);
                result = mockedRegistrationOperationStatusParser;
            }
        };

        // State machine expectations
        new StrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
                mockedRegistrationOperationStatusParser.getStatus();
                result = "assigned";
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getRegistrationState();
                result = mockedDeviceRegistrationResultParser;
                mockedDeviceRegistrationResultParser.getAssignedHub();
                result = TEST_HUB;
                mockedDeviceRegistrationResultParser.getDeviceId();
                result = TEST_DEVICE_ID;
                Deencapsulation.newInstance(RegistrationResult.class, new Class[] {String.class, String.class, String.class, ProvisioningDeviceClientStatus.class},
                        TEST_HUB, TEST_DEVICE_ID, any, PROVISIONING_DEVICE_STATUS_ASSIGNED);
                result = mockedRegistrationData;
                mockedDeviceRegistrationResultParser.getTpm();
                result = mockedTpm;
                mockedTpm.getAuthenticationKey();
                result = mockSecurityProviderException;
                mockedProvisioningDeviceClientConfig.getSecurityProvider();
                result = mockedSecurityProviderTpm;
            }
        };
        ProvisioningTask testProvisioningTask = new ProvisioningTask(mockedProvisioningDeviceClientConfig, mockedProvisioningDeviceClientContract);

        //act
        testProvisioningTask.call();

        //assert
        new Verifications()
        {
            {
                mockedProvisioningDeviceClientRegistrationCallback.run((RegistrationResult) any, mockSecurityProviderException, any);
                times = 1;
            }
        };
    }

    private void registerExpectations() throws InterruptedException, ExecutionException, TimeoutException
    {
        new NonStrictExpectations()
        {
            {
                mockedFutureTask.get(MAX_TIME_TO_WAIT_FOR_REGISTRATION, TimeUnit.MILLISECONDS);
                result = mockedRegistrationOperationStatusParser;
                mockedRegistrationOperationStatusParser.getOperationId();
                result = TEST_OPERATION_ID;
            }
        };
    }

    private void registrationStatusJsonExpectations(String hostname, String deviceId, TpmRegistrationResultParser mockedTpmRegistrationResultParser, String authenticationKey)
    {
        new NonStrictExpectations()
        {
            {
                mockedRegistrationOperationStatusParser.getRegistrationState();
                result = mockedDeviceRegistrationResultParser;
                mockedDeviceRegistrationResultParser.getAssignedHub();
                result = hostname;
                mockedDeviceRegistrationResultParser.getDeviceId();
                result = deviceId;

                //TPM flow specific json
                mockedDeviceRegistrationResultParser.getTpm();
                result = mockedTpmRegistrationResultParser;

            }
        };

        if (mockedTpmRegistrationResultParser != null)
        {
            new NonStrictExpectations()
            {
                {
                    mockedTpmRegistrationResultParser.getAuthenticationKey();
                    result = authenticationKey;
                }
            };
        }
    }
}
