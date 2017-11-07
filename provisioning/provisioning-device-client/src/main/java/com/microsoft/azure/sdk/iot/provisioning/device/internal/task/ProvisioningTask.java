/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.task;

import com.microsoft.azure.sdk.iot.provisioning.device.*;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.ProvisioningDeviceClientConfig;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientStatus;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceConnectionException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.ResponseParser;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceHubException;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityClient;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ProvisioningDeviceClientContract;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientAuthenticationException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityClientX509;

import java.util.concurrent.*;

import static com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientStatus.*;

public class ProvisioningTask implements Callable
{
    private static final int MAX_THREADS_TO_RUN = 2;
    private static final int MAX_TIME_TO_WAIT_FOR_REGISTRATION = 1000000;
    private static final int MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE = 10000;

    private SecurityClient securityClient = null;
    private ProvisioningDeviceClientContract provisioningDeviceClientContract = null;
    private ProvisioningDeviceClientConfig provisioningDeviceClientConfig = null;

    private ProvisioningDeviceClientRegistrationCallback provisioningDeviceClientRegistrationCallback = null;
    private Object dpsRegistrationCallbackContext = null;

    private Authorization authorization = null;
    private ProvisioningDeviceClientStatus dpsStatus = null;

    private ExecutorService executor;

    /**
     * Constructor for creating a provisioning task
     * @param provisioningDeviceClientConfig Config that contains details pertaining to Service
     * @param provisioningDeviceClientContract Contract with the service over the specified protocol
     * @throws ProvisioningDeviceClientException If any of the input parameters are invalid then this exception is thrown
     */
    public ProvisioningTask(ProvisioningDeviceClientConfig provisioningDeviceClientConfig,
                            ProvisioningDeviceClientContract provisioningDeviceClientContract) throws ProvisioningDeviceClientException
    {
        if (provisioningDeviceClientContract == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("DPS Transport cannot be null"));
        }

        if (provisioningDeviceClientConfig == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("Config cannot be null"));
        }

        //SRS_ProvisioningTask_25_001: [ Constructor shall save provisioningDeviceClientConfig, provisioningDeviceClientContract.]
        this.provisioningDeviceClientConfig = provisioningDeviceClientConfig;
        this.securityClient = provisioningDeviceClientConfig.getSecurityClient();

        if (securityClient == null)
        {
            //SRS_ProvisioningTask_25_002: [ Constructor shall shall throw ProvisioningDeviceClientException if the securityClient is null.]
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("Security client cannot be null"));
        }

        this.provisioningDeviceClientContract = provisioningDeviceClientContract;
        this.provisioningDeviceClientRegistrationCallback = provisioningDeviceClientConfig.getRegistrationCallback();
        this.dpsRegistrationCallbackContext = provisioningDeviceClientConfig.getRegistrationCallbackContext();

        //SRS_ProvisioningTask_25_004: [ This method shall throw ProvisioningDeviceClientException if the provisioningDeviceClientRegistrationCallback is null. ]
        if (provisioningDeviceClientRegistrationCallback == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("Registration callback cannot be null"));
        }

        this.authorization = new Authorization();
        //SRS_ProvisioningTask_25_015: [ Constructor shall start the executor with a fixed thread pool of size 2.]
        this.executor = Executors.newFixedThreadPool(MAX_THREADS_TO_RUN);
    }

    private void invokeRegistrationCallback(RegistrationResult registrationInfo, Exception e) throws ProvisioningDeviceClientException
    {
        if (this.provisioningDeviceClientRegistrationCallback != null)
        {
            this.provisioningDeviceClientRegistrationCallback.run(registrationInfo, e, this.dpsRegistrationCallbackContext);
        }
        else
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("Registration callback cannot be null"));
        }
    }

    private ResponseParser invokeRegister() throws InterruptedException, ExecutionException, TimeoutException,
                                                   ProvisioningDeviceClientException
    {
        RegisterTask registerTask = new RegisterTask(this.provisioningDeviceClientConfig, securityClient,
                                                     provisioningDeviceClientContract, authorization);
        FutureTask<ResponseParser> futureRegisterTask = new FutureTask<ResponseParser>(registerTask);
        executor.submit(futureRegisterTask);
        ResponseParser registrationResponseParser =  futureRegisterTask.get(MAX_TIME_TO_WAIT_FOR_REGISTRATION,
                                                                            TimeUnit.MILLISECONDS);

        if (registrationResponseParser == null)
        {
            this.dpsStatus = PROVISIONING_DEVICE_STATUS_ERROR;
            throw new ProvisioningDeviceClientAuthenticationException("Registration response could not be retrieved, " +
                                                                              "authentication failure");
        }

        ProvisioningStatus status = ProvisioningStatus.fromString(registrationResponseParser.getStatus());
        if (status == null)
        {
            this.dpsStatus = PROVISIONING_DEVICE_STATUS_ERROR;
            throw new ProvisioningDeviceClientAuthenticationException("Received null status for registration, " +
                                                                              "authentication failure");
        }

        if (registrationResponseParser.getOperationId() == null)
        {
            throw new ProvisioningDeviceClientAuthenticationException("operation id could not be retrieved, " +
                                                                              "authentication failure");
        }

        return registrationResponseParser;
    }

    private ResponseParser invokeStatus(String operationId) throws TimeoutException, InterruptedException, ExecutionException,
                                                                   ProvisioningDeviceClientException
    {
        // To-Do : Add appropriate wait time retrieved from Service
        Thread.sleep(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE);
        StatusTask statusTask = new StatusTask(securityClient, provisioningDeviceClientContract, operationId,
                                               this.authorization);
        FutureTask<ResponseParser> futureStatusTask = new FutureTask<ResponseParser>(statusTask);
        executor.submit(futureStatusTask);
        ResponseParser statusResponseParser =  futureStatusTask.get(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE, TimeUnit.MILLISECONDS);

        if (statusResponseParser == null)
        {
            this.dpsStatus = PROVISIONING_DEVICE_STATUS_ERROR;
            throw new ProvisioningDeviceClientAuthenticationException("Status response could not be retrieved, " +
                                                                              "authentication failure");
        }

         if (statusResponseParser.getStatus() == null)
        {
            this.dpsStatus = PROVISIONING_DEVICE_STATUS_ERROR;
            throw new ProvisioningDeviceClientAuthenticationException("Status could not be retrieved, " +
                                                                              "authentication failure");
        }

        if (ProvisioningStatus.fromString(statusResponseParser.getStatus()) == null)
        {
            this.dpsStatus = PROVISIONING_DEVICE_STATUS_ERROR;
            throw new ProvisioningDeviceClientAuthenticationException("Status could not be retrieved, " +
                                                                              "authentication failure");
        }
        return statusResponseParser;
    }

    private void executeStateMachineForStatus(ResponseParser registrationResponseParser)
            throws TimeoutException, InterruptedException, ExecutionException, ProvisioningDeviceClientException
    {
        boolean isContinue = false;
        ResponseParser statusResponseParser = registrationResponseParser;
        ProvisioningStatus nextStatus = ProvisioningStatus.fromString(registrationResponseParser.getStatus());
        // continue invoking for status until a terminal state is reached
        do
        {
            if (nextStatus == null)
            {
                throw new ProvisioningDeviceClientException("Did not receive a valid status");
            }

            switch (nextStatus)
            {
                case UNASSIGNED:
                    //intended fall through
                case ASSIGNING:
                    statusResponseParser = this.invokeStatus(registrationResponseParser.getOperationId());
                    nextStatus = ProvisioningStatus.fromString(statusResponseParser.getStatus());
                    isContinue = true;
                    break;
                case ASSIGNED:
                    this.dpsStatus = PROVISIONING_DEVICE_STATUS_ASSIGNED;
                    RegistrationResult registrationInfo = new RegistrationResult(
                            statusResponseParser.getRegistrationStatus().getAssignedHub(),
                            statusResponseParser.getRegistrationStatus().getDeviceId(), PROVISIONING_DEVICE_STATUS_ASSIGNED);
                    this.invokeRegistrationCallback(registrationInfo, null);
                    isContinue = false;
                    break;
                case FAILED:
                    this.dpsStatus = PROVISIONING_DEVICE_STATUS_FAILED;
                    ProvisioningDeviceHubException dpsHubException = new ProvisioningDeviceHubException(
                            statusResponseParser.getRegistrationStatus().getErrorMessage());
                    registrationInfo = new RegistrationResult(null, null, PROVISIONING_DEVICE_STATUS_FAILED);
                    this.invokeRegistrationCallback(registrationInfo, dpsHubException);
                    isContinue = false;
                    break;
                case DISABLED:
                    this.dpsStatus = PROVISIONING_DEVICE_STATUS_DISABLED;
                    dpsHubException = new ProvisioningDeviceHubException(
                            statusResponseParser.getRegistrationStatus().getErrorMessage());
                    registrationInfo = new RegistrationResult(null, null, PROVISIONING_DEVICE_STATUS_DISABLED);
                    this.invokeRegistrationCallback(registrationInfo, dpsHubException);
                    isContinue = false;
                    break;
            }
        }
        while (isContinue);
    }

    // this thread will continue to run until DPS status is assigned and registered or exit on error
    // DPS State machine

    /**
     * This method executes the State machine with the device goes through during registration.
     * @return Returns {@code null}
     * @throws Exception This exception is thrown if any of the exception during execution is not handled.
     */
    @Override
    public Object call() throws Exception
    {
        try
        {
            //SRS_ProvisioningTask_25_015: [ This method shall invoke open call on the contract.]
            provisioningDeviceClientContract.open(new RequestData(securityClient.getRegistrationId(), securityClient.getSSLContext(), securityClient instanceof SecurityClientX509));
            //SRS_ProvisioningTask_25_007: [ This method shall invoke Register task and status task to execute the state machine of the service as per below rules.]
            /*
            Service State Machine Rules

            SRS_ProvisioningTask_25_008: [ This method shall invoke register task and wait for it to complete.]
            SRS_ProvisioningTask_25_009: [ This method shall invoke status callback with status PROVISIONING_DEVICE_STATUS_AUTHENTICATED if register task completes successfully.]
            SRS_ProvisioningTask_25_010: [ This method shall invoke status task to get the current state of the device registration and wait until a terminal state is reached.]
            SRS_ProvisioningTask_25_011: [ Upon reaching one of the terminal state i.e ASSIGNED, this method shall invoke registration callback with the information retrieved from service for IotHub Uri and DeviceId. Also if status callback is defined then it shall be invoked with status PROVISIONING_DEVICE_STATUS_ASSIGNED.]
            SRS_ProvisioningTask_25_012: [ Upon reaching one of the terminal states i.e FAILED or DISABLED, this method shall invoke registration callback with error message received from service. Also if status callback is defined then it shall be invoked with status PROVISIONING_DEVICE_STATUS_ERROR.]
            SRS_ProvisioningTask_25_013: [ Upon reaching intermediate state i.e UNASSIGNED or ASSIGNING, this method shall continue to query for status until a terminal state is reached. Also if status callback is defined then it shall be invoked with status PROVISIONING_DEVICE_STATUS_ASSIGNING.]
            State diagram :

            One of the following states can be reached from register or status task - (A) Unassigned (B) Assigning (C) Assigned (D) Fail (E) Disable

                Return-State	A	            B	        C	        D	        E
                Register-State	B, C, D, E	    C, D, E	    terminal	terminal	terminal
                Status-State	B, C, D, E	    C, D, E	    terminal	terminal	terminal
             */
            ResponseParser registrationResponseParser = this.invokeRegister();

            this.executeStateMachineForStatus(registrationResponseParser);
            this.close();
        }
        catch (ExecutionException | TimeoutException | ProvisioningDeviceClientException e)
        {
            //SRS_ProvisioningTask_25_006: [ This method shall invoke the status callback, if any of the task fail or throw any exception. ]
            this.dpsStatus = PROVISIONING_DEVICE_STATUS_ERROR;
            invokeRegistrationCallback(new RegistrationResult(null, null, PROVISIONING_DEVICE_STATUS_ERROR), e);
            //SRS_ProvisioningTask_25_015: [ This method shall invoke close call on the contract and close the threads started.]
            this.close();
        }
        return null;
    }

    /**
     * This method shall shutdown the existing threads if not already done so.
     */
    private void close() throws ProvisioningDeviceConnectionException
    {
        provisioningDeviceClientContract.close();
        //SRS_ProvisioningTask_25_014: [ This method shall shutdown the executors if they have not already shutdown. ]
        if (executor != null && !executor.isShutdown())
        {
            executor.shutdownNow();
        }
    }
}
