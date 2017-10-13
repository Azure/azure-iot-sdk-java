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
import com.microsoft.azure.sdk.iot.provisioning.device.internal.ProvisioningDeviceClientRegistrationInfo;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceHubException;
import com.microsoft.azure.sdk.iot.dps.security.DPSSecurityClient;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.ProvisioningDeviceClientAuthorization;
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

    private DPSSecurityClient dpsSecurityClient = null;
    private ProvisioningDeviceClientContract provisioningDeviceClientContract = null;
    private ProvisioningDeviceClientConfig provisioningDeviceClientConfig = null;

    private ProvisioningDeviceClientStatusCallback provisioningDeviceClientStatusCallback = null;
    private Object dpsStatusCallbackContext = null;

    private ProvisioningDeviceClientRegistrationCallback provisioningDeviceClientRegistrationCallback = null;
    private Object dpsRegistrationCallbackContext = null;

    private ProvisioningDeviceClientAuthorization provisioningDeviceClientAuthorization = null;
    private ProvisioningDeviceClientStatus dpsStatus = null;

    private ExecutorService executor;

    // use callback to inform status to user
    public ProvisioningTask(ProvisioningDeviceClientConfig provisioningDeviceClientConfig, DPSSecurityClient dpsSecurityClient, ProvisioningDeviceClientContract provisioningDeviceClientContract, ProvisioningDeviceClientStatusCallback provisioningDeviceClientStatusCallback, Object dpsStatusCallbackContext) throws ProvisioningDeviceClientException
    {
        if (dpsSecurityClient == null)
        {
            throw new ProvisioningDeviceClientException("security client cannot be null");
        }

        if (provisioningDeviceClientContract == null)
        {
            throw new ProvisioningDeviceClientException("DPS Transport cannot be null");
        }

        if (provisioningDeviceClientStatusCallback == null)
        {
            throw new ProvisioningDeviceClientException("DPS Error callback cannot be null");
        }

        this.provisioningDeviceClientConfig = provisioningDeviceClientConfig;
        this.dpsSecurityClient = dpsSecurityClient;
        this.provisioningDeviceClientContract = provisioningDeviceClientContract;
        this.provisioningDeviceClientStatusCallback = provisioningDeviceClientStatusCallback;
        this.dpsStatusCallbackContext = dpsStatusCallbackContext;
        this.provisioningDeviceClientAuthorization = new ProvisioningDeviceClientAuthorization();
        this.executor = Executors.newFixedThreadPool(MAX_THREADS_TO_RUN);

        this.dpsStatus = ProvisioningDeviceClientStatus.DPS_DEVICE_STATUS_UNAUTHENTICATED;
        invokeStatusCallback(this.dpsStatus, null);
    }

    public void setRegistrationCallback(ProvisioningDeviceClientRegistrationCallback provisioningDeviceClientRegistrationCallback, Object dpsRegistrationCallbackContext)
    {
        this.provisioningDeviceClientRegistrationCallback = provisioningDeviceClientRegistrationCallback;
        this.dpsRegistrationCallbackContext = dpsRegistrationCallbackContext;
    }

    public void invokeStatusCallback(ProvisioningDeviceClientStatus status, String reason)
    {
        if (this.provisioningDeviceClientStatusCallback != null)
        {
            this.provisioningDeviceClientStatusCallback.run(status, reason, this.dpsStatusCallbackContext);
        }
    }

    public void invokeRegistrationCallback(ProvisioningDeviceClientRegistrationInfo registrationInfo)
    {
        if (this.provisioningDeviceClientRegistrationCallback != null)
        {
            this.provisioningDeviceClientRegistrationCallback.run(registrationInfo, this.dpsRegistrationCallbackContext);
        }
    }

    // this thread will continue to run until DPS status is assigned and registered or exit on error
    // DPS State machine
    @Override
    public Object call() throws Exception
    {
        try
        {
            RegisterTask registerTask = new RegisterTask(this.provisioningDeviceClientConfig, dpsSecurityClient, provisioningDeviceClientContract, provisioningDeviceClientAuthorization);
            FutureTask<ResponseParser> futureRegisterTask = new FutureTask<ResponseParser>(registerTask);
            executor.submit(futureRegisterTask);
            this.dpsStatus = ProvisioningDeviceClientStatus.DPS_DEVICE_STATUS_READY_TO_AUTHENTICATE;
            invokeStatusCallback(this.dpsStatus, null);

            ResponseParser dpsAuthenticationResponseParser = null;
            dpsAuthenticationResponseParser = futureRegisterTask.get(MAX_TIME_TO_WAIT_FOR_REGISTRATION, TimeUnit.MILLISECONDS);

            if (dpsAuthenticationResponseParser == null)
            {
                this.dpsStatus = ProvisioningDeviceClientStatus.DPS_DEVICE_STATUS_ERROR;
                invokeStatusCallback(this.dpsStatus, null);
                return null;
            }
            ProvisioningStatus status = ProvisioningStatus.fromString(dpsAuthenticationResponseParser.getStatus());
            if (status == null)
            {
                this.dpsStatus = ProvisioningDeviceClientStatus.DPS_DEVICE_STATUS_ERROR;
                invokeStatusCallback(this.dpsStatus, null);
                return null;
            }

            if (dpsAuthenticationResponseParser.getOperationId() != null)
            {
                this.dpsStatus = ProvisioningDeviceClientStatus.DPS_DEVICE_STATUS_AUTHENTICATED;
                invokeStatusCallback(this.dpsStatus, null);
            }
            else
            {
                invokeStatusCallback(this.dpsStatus, null);
                throw new ProvisioningDeviceClientAuthenticationException("operation id could not be retrieved, authentication failure");
            }

            ResponseParser dpsStatusResponseParser = null;
            StatusTask statusTask = new StatusTask(dpsSecurityClient, provisioningDeviceClientContract, dpsAuthenticationResponseParser.getOperationId(), this.provisioningDeviceClientAuthorization);
            FutureTask<ResponseParser> futureStatusTask = new FutureTask<ResponseParser>(statusTask);
            executor.submit(futureStatusTask);
            dpsStatusResponseParser = futureStatusTask.get();
            if (ProvisioningStatus.fromString(dpsStatusResponseParser.getStatus()) != ProvisioningStatus.ASSIGNED)
            {
                this.dpsStatus = DPS_DEVICE_STATUS_ASSIGNING;
                invokeStatusCallback(this.dpsStatus, null);
            }
            ProvisioningStatus nextStatus = ProvisioningStatus.fromString(dpsStatusResponseParser.getStatus());
            boolean isContinue = false;
            do
            {
                switch (nextStatus)
                {
                    case ASSIGNING:
                        // look for what else is needed for this step - perhaps just op id for x509
                        Thread.sleep(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE);
                        statusTask = new StatusTask(dpsSecurityClient, provisioningDeviceClientContract, dpsAuthenticationResponseParser.getOperationId(), this.provisioningDeviceClientAuthorization);
                        futureStatusTask = new FutureTask<ResponseParser>(statusTask);
                        executor.submit(futureStatusTask);
                        dpsStatusResponseParser = futureStatusTask.get();
                        if (ProvisioningStatus.fromString(dpsStatusResponseParser.getStatus()) != ProvisioningStatus.ASSIGNED)
                        {
                            this.dpsStatus = DPS_DEVICE_STATUS_ASSIGNING;
                            invokeStatusCallback(this.dpsStatus, null);
                        }
                        Thread.sleep(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE);
                        nextStatus = ProvisioningStatus.fromString(dpsStatusResponseParser.getStatus());
                        isContinue = true;
                        break;
                    case ASSIGNED:
                        this.dpsStatus = DPS_DEVICE_STATUS_ASSIGNED;
                        invokeStatusCallback(this.dpsStatus, null);
                        ProvisioningDeviceClientRegistrationInfo registrationInfo = new ProvisioningDeviceClientRegistrationInfo(dpsStatusResponseParser.getRegistrationStatus().getAssignedHub(), dpsStatusResponseParser.getRegistrationStatus().getDeviceId(), DPS_DEVICE_STATUS_ASSIGNED);
                        invokeRegistrationCallback(registrationInfo);
                        nextStatus = ProvisioningStatus.fromString(dpsStatusResponseParser.getStatus());
                        isContinue = false;
                        break;
                    case FAILED:
                    case UNASSIGNED:
                    case BLACKLISTED:
                        this.dpsStatus = DPS_DEVICE_STATUS_ERROR;
                        ProvisioningDeviceHubException dpsHubException = new ProvisioningDeviceHubException(dpsStatusResponseParser.getRegistrationStatus().getErrorMessage());
                        invokeStatusCallback(this.dpsStatus, dpsHubException.getMessage());
                        nextStatus = ProvisioningStatus.fromString(dpsStatusResponseParser.getStatus());
                        isContinue = false;
                        break;
                }
            }
            while (isContinue);
        }
        catch (ExecutionException | TimeoutException | ProvisioningDeviceClientException e)
        {
            this.dpsStatus = DPS_DEVICE_STATUS_ERROR;
            if (e.getMessage() != null)
            {
                invokeStatusCallback(this.dpsStatus, e.getMessage());
            }
            else
            {
                invokeStatusCallback(this.dpsStatus, e.toString());
            }
        }

        return null;
    }

    public void close()
    {
        if (executor != null && !executor.isShutdown())
        {
            executor.shutdownNow();
        }
    }
}
