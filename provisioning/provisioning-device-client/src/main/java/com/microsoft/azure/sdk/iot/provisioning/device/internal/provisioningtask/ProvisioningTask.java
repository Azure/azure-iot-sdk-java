/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.provisioningtask;

import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientConfig;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientStatusCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.ResponseParser;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientRegistrationCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceHubException;
import com.microsoft.azure.sdk.iot.dps.security.SecurityClient;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientStatus;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ProvisioningDeviceClientContract;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientAuthenticationException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;

import java.util.concurrent.*;

import static com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientStatus.DPS_DEVICE_STATUS_ASSIGNED;
import static com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientStatus.DPS_DEVICE_STATUS_ASSIGNING;
import static com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientStatus.DPS_DEVICE_STATUS_ERROR;

public class ProvisioningTask implements Callable
{
    private static final int MAX_THREADS_TO_RUN = 2;
    private static final int MAX_TIME_TO_WAIT_FOR_REGISTRATION = 1000000;
    private static final int MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE = 10000;

    private SecurityClient securityClient = null;
    private ProvisioningDeviceClientContract provisioningDeviceClientContract = null;
    private ProvisioningDeviceClientConfig provisioningDeviceClientConfig = null;

    private ProvisioningDeviceClientStatusCallback provisioningDeviceClientStatusCallback = null;
    private Object dpsStatusCallbackContext = null;

    private ProvisioningDeviceClientRegistrationCallback provisioningDeviceClientRegistrationCallback = null;
    private Object dpsRegistrationCallbackContext = null;

    private Authorization authorization = null;
    private ProvisioningDeviceClientStatus dpsStatus = null;

    private ExecutorService executor;

    /**
     * Constructor for creating a provisioning task
     * @param provisioningDeviceClientConfig Config that contains details pertaining to Service
     * @param securityClient Security client that holds information about device authentication
     * @param provisioningDeviceClientContract Contract with the service over the specified protocol
     * @param provisioningDeviceClientStatusCallback Callback which provides status of the device during registration. Can be {@code null}
     * @param dpsStatusCallbackContext Context for the callback
     * @throws ProvisioningDeviceClientException If any of the input parameters are invalid then this exception is thrown
     */
    public ProvisioningTask(ProvisioningDeviceClientConfig provisioningDeviceClientConfig,
                            SecurityClient securityClient,
                            ProvisioningDeviceClientContract provisioningDeviceClientContract,
                            ProvisioningDeviceClientStatusCallback provisioningDeviceClientStatusCallback,
                            Object dpsStatusCallbackContext) throws ProvisioningDeviceClientException
    {
        //SRS_provisioningtask_25_002: [ Constructor throw ProvisioningDeviceClientException if provisioningDeviceClientConfig , securityClient or provisioningDeviceClientContract is null.]
        if (securityClient == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("security client cannot be null"));
        }

        if (provisioningDeviceClientContract == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("DPS Transport cannot be null"));
        }

        if (provisioningDeviceClientConfig == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("Config cannot be null"));
        }

        //SRS_provisioningtask_25_001: [ Constructor shall save provisioningDeviceClientConfig , securityClient, provisioningDeviceClientContract, provisioningDeviceClientStatusCallback, dpsStatusCallbackContext.]
        this.provisioningDeviceClientConfig = provisioningDeviceClientConfig;
        this.securityClient = securityClient;
        this.provisioningDeviceClientContract = provisioningDeviceClientContract;
        this.provisioningDeviceClientStatusCallback = provisioningDeviceClientStatusCallback;
        this.dpsStatusCallbackContext = dpsStatusCallbackContext;
        this.authorization = new Authorization();
        //SRS_ProvisioningTask_25_015: [ Constructor shall start the executor with a fixed thread pool of size 2.]
        this.executor = Executors.newFixedThreadPool(MAX_THREADS_TO_RUN);

        //SRS_provisioningtask_25_003: [ Constructor shall trigger status callback if provided with status DPS_DEVICE_STATUS_UNAUTHENTICATED.]
        this.dpsStatus = ProvisioningDeviceClientStatus.DPS_DEVICE_STATUS_UNAUTHENTICATED;
        invokeStatusCallback(this.dpsStatus, null);
    }

    /**
     * Setter for the registration call back
     * @param provisioningDeviceClientRegistrationCallback Callback to provide details of the registration. Cannot be {@code null}
     * @param dpsRegistrationCallbackContext Context for the callback
     * @throws ProvisioningDeviceClientException If any of the input parameters are invalid
     */
    public void setRegistrationCallback(ProvisioningDeviceClientRegistrationCallback provisioningDeviceClientRegistrationCallback,
                                        Object dpsRegistrationCallbackContext) throws ProvisioningDeviceClientException
    {
        //SRS_provisioningtask_25_004: [ This method shall throw ProvisioningDeviceClientException if the provisioningDeviceClientRegistrationCallback is null. ]
        if (provisioningDeviceClientRegistrationCallback == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("Registration callback cannot be null"));
        }
        //SRS_provisioningtask_25_005: [ This method shall save the registration callback. ]
        this.provisioningDeviceClientRegistrationCallback = provisioningDeviceClientRegistrationCallback;
        this.dpsRegistrationCallbackContext = dpsRegistrationCallbackContext;
    }

    private void invokeStatusCallback(ProvisioningDeviceClientStatus status, Exception e)
    {
        if (this.provisioningDeviceClientStatusCallback != null)
        {
            this.provisioningDeviceClientStatusCallback.run(status, e, this.dpsStatusCallbackContext);
        }
    }

    private void invokeRegistrationCallback(RegistrationInfo registrationInfo) throws ProvisioningDeviceClientException
    {
        if (this.provisioningDeviceClientRegistrationCallback != null)
        {
            this.provisioningDeviceClientRegistrationCallback.run(registrationInfo, this.dpsRegistrationCallbackContext);
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
            this.dpsStatus = DPS_DEVICE_STATUS_ERROR;
            throw new ProvisioningDeviceClientAuthenticationException("Registration response could not be retrieved, " +
                                                                              "authentication failure");
        }

        ProvisioningStatus status = ProvisioningStatus.fromString(registrationResponseParser.getStatus());
        if (status == null)
        {
            this.dpsStatus = DPS_DEVICE_STATUS_ERROR;
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
            this.dpsStatus = DPS_DEVICE_STATUS_ERROR;
            throw new ProvisioningDeviceClientAuthenticationException("Status response could not be retrieved, " +
                                                                              "authentication failure");
        }

         if (statusResponseParser.getStatus() == null)
        {
            this.dpsStatus = DPS_DEVICE_STATUS_ERROR;
            throw new ProvisioningDeviceClientAuthenticationException("Status could not be retrieved, " +
                                                                              "authentication failure");
        }

        if (ProvisioningStatus.fromString(statusResponseParser.getStatus()) == null)
        {
            this.dpsStatus = DPS_DEVICE_STATUS_ERROR;
            throw new ProvisioningDeviceClientAuthenticationException("Status could not be retrieved, " +
                                                                              "authentication failure");
        }
        return statusResponseParser;
    }

    private void executeStateMachineForStatus(ProvisioningStatus nextStatus, ResponseParser registrationResponseParser)
            throws TimeoutException, InterruptedException, ExecutionException, ProvisioningDeviceClientException
    {
        boolean isContinue = false;
        ResponseParser statusResponseParser = registrationResponseParser;
        // continue invoking for status until a terminal state is reached
        do
        {
            switch (nextStatus)
            {
                case UNASSIGNED:
                    this.dpsStatus = DPS_DEVICE_STATUS_ASSIGNING;
                    invokeStatusCallback(this.dpsStatus, null);
                    //intended fall through
                case ASSIGNING:
                    statusResponseParser = this.invokeStatus(registrationResponseParser.getOperationId());
                    if (ProvisioningStatus.fromString(statusResponseParser.getStatus()) == ProvisioningStatus.ASSIGNING)
                    {
                        this.dpsStatus = DPS_DEVICE_STATUS_ASSIGNING;
                        invokeStatusCallback(this.dpsStatus, null);
                    }
                    nextStatus = ProvisioningStatus.fromString(statusResponseParser.getStatus());
                    isContinue = true;
                    break;
                case ASSIGNED:
                    this.dpsStatus = DPS_DEVICE_STATUS_ASSIGNED;
                    this.invokeStatusCallback(this.dpsStatus, null);
                    RegistrationInfo registrationInfo = new RegistrationInfo(
                            statusResponseParser.getRegistrationStatus().getAssignedHub(),
                            statusResponseParser.getRegistrationStatus().getDeviceId(), DPS_DEVICE_STATUS_ASSIGNED);
                    this.invokeRegistrationCallback(registrationInfo);
                    isContinue = false;
                    break;
                case FAILED:
                    //intended fall through
                case DISABLED:
                    this.dpsStatus = DPS_DEVICE_STATUS_ERROR;
                    ProvisioningDeviceHubException dpsHubException = new ProvisioningDeviceHubException(
                            statusResponseParser.getRegistrationStatus().getErrorMessage());
                    this.invokeStatusCallback(this.dpsStatus, dpsHubException);
                    registrationInfo = new RegistrationInfo(null, null, DPS_DEVICE_STATUS_ERROR);
                    this.invokeRegistrationCallback(registrationInfo);
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
            //SRS_provisioningtask_25_007: [ This method shall invoke Register task and status task to execute the state machine of the service as per below rules.]
            /*
            Service State Machine Rules

            SRS_provisioningtask_25_008: [ This method shall invoke register task and wait for it to complete.]
            SRS_provisioningtask_25_009: [ This method shall invoke status callback with status DPS_DEVICE_STATUS_AUTHENTICATED if register task completes successfully.]
            SRS_provisioningtask_25_010: [ This method shall invoke status task to get the current state of the device registration and wait until a terminal state is reached.]
            SRS_provisioningtask_25_011: [ Upon reaching one of the terminal state i.e ASSIGNED, this method shall invoke registration callback with the information retrieved from service for IotHub Uri and DeviceId. Also if status callback is defined then it shall be invoked with status DPS_DEVICE_STATUS_ASSIGNED.]
            SRS_provisioningtask_25_012: [ Upon reaching one of the terminal states i.e FAILED or DISABLED, this method shall invoke registration callback with error message received from service. Also if status callback is defined then it shall be invoked with status DPS_DEVICE_STATUS_ERROR.]
            SRS_provisioningtask_25_013: [ Upon reaching intermediate state i.e UNASSIGNED or ASSIGNING, this method shall continue to query for status until a terminal state is reached. Also if status callback is defined then it shall be invoked with status DPS_DEVICE_STATUS_ASSIGNING.]
            State diagram :

            One of the following states can be reached from register or status task - (A) Unassigned (B) Assigning (C) Assigned (D) Fail (E) Disable

                Return-State	A	            B	        C	        D	        E
                Register-State	B, C, D, E	    C, D, E	    terminal	terminal	terminal
                Status-State	B, C, D, E	    C, D, E	    terminal	terminal	terminal
             */
            ResponseParser registrationResponseParser = this.invokeRegister();
            this.dpsStatus = ProvisioningDeviceClientStatus.DPS_DEVICE_STATUS_AUTHENTICATED;
            invokeStatusCallback(this.dpsStatus, null);

            this.executeStateMachineForStatus(ProvisioningStatus.fromString(registrationResponseParser.getStatus()), registrationResponseParser);
        }
        catch (ExecutionException | TimeoutException | ProvisioningDeviceClientException e)
        {
            //SRS_provisioningtask_25_006: [ This method shall invoke the status callback, if any of the task fail or throw any exception. ]
            this.dpsStatus = DPS_DEVICE_STATUS_ERROR;
            invokeStatusCallback(this.dpsStatus, e);

        }
        return null;
    }

    /**
     * This method shall shutdown the existing threads if not already done so.
     */
    public void close()
    {
        //SRS_provisioningtask_25_014: [ This method shall shutdown the executors if they have not already shutdown. ]
        if (executor != null && !executor.isShutdown())
        {
            executor.shutdownNow();
        }
    }
}
