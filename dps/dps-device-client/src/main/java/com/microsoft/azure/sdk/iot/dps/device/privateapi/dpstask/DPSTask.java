/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.dps.device.privateapi.dpstask;

import com.microsoft.azure.sdk.iot.dps.device.privateapi.dpsparser.DPSResponseParser;
import com.microsoft.azure.sdk.iot.dps.device.DPSConfig;
import com.microsoft.azure.sdk.iot.dps.device.DPSRegistrationCallback;
import com.microsoft.azure.sdk.iot.dps.device.privateapi.DPSRegistrationInfo;
import com.microsoft.azure.sdk.iot.dps.security.DPSSecurityClient;
import com.microsoft.azure.sdk.iot.dps.device.DpsStatusCallback;
import com.microsoft.azure.sdk.iot.dps.device.privateapi.DPSAuthorization;
import com.microsoft.azure.sdk.iot.dps.device.DPSDeviceStatus;
import com.microsoft.azure.sdk.iot.dps.device.privateapi.DPSTransport;
import com.microsoft.azure.sdk.iot.dps.device.privateapi.exceptions.DPSAuthenticationException;
import com.microsoft.azure.sdk.iot.dps.device.privateapi.exceptions.DPSClientException;

import java.util.concurrent.*;

import static com.microsoft.azure.sdk.iot.dps.device.DPSDeviceStatus.DPS_DEVICE_STATUS_ASSIGNED;
import static com.microsoft.azure.sdk.iot.dps.device.DPSDeviceStatus.DPS_DEVICE_STATUS_ASSIGNING;
import static com.microsoft.azure.sdk.iot.dps.device.DPSDeviceStatus.DPS_DEVICE_STATUS_ERROR;

public class DPSTask implements Callable
{
    private static final int MAX_THREADS_TO_RUN = 2;
    private static final int MAX_TIME_TO_WAIT_FOR_REGISTRATION = 1000000;
    private static final int MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE = 10000;

    private DPSSecurityClient dpsSecurityClient = null;
    private DPSTransport dpsTransport = null;
    private DPSConfig dpsConfig = null;

    private DpsStatusCallback dpsStatusCallback = null;
    private Object dpsStatusCallbackContext = null;

    private DPSRegistrationCallback dpsRegistrationCallback = null;
    private Object dpsRegistrationCallbackContext = null;

    private DPSAuthorization dpsAuthorization = null;
    private DPSDeviceStatus dpsStatus = null;

    private ExecutorService executor;

    // use callback to inform status to user
    public DPSTask(DPSConfig dpsConfig, DPSSecurityClient dpsSecurityClient, DPSTransport dpsTransport, DpsStatusCallback dpsStatusCallback, Object dpsStatusCallbackContext) throws DPSClientException
    {
        if (dpsSecurityClient == null)
        {
            throw new DPSClientException("security client cannot be null");
        }

        if (dpsTransport == null)
        {
            throw new DPSClientException("DPS Transport cannot be null");
        }

        if (dpsStatusCallback == null)
        {
            throw new DPSClientException("DPS Error callback cannot be null");
        }

        this.dpsConfig = dpsConfig;
        this.dpsSecurityClient = dpsSecurityClient;
        this.dpsTransport = dpsTransport;
        this.dpsStatusCallback = dpsStatusCallback;
        this.dpsStatusCallbackContext = dpsStatusCallbackContext;
        this.dpsAuthorization = new DPSAuthorization();
        this.executor = Executors.newFixedThreadPool(MAX_THREADS_TO_RUN);

        this.dpsStatus = DPSDeviceStatus.DPS_DEVICE_STATUS_UNAUTHENTICATED;
        invokeStatusCallback(this.dpsStatus, null);
    }

    public void setRegistrationCallback(DPSRegistrationCallback dpsRegistrationCallback, Object dpsRegistrationCallbackContext)
    {
        this.dpsRegistrationCallback = dpsRegistrationCallback;
        this.dpsRegistrationCallbackContext = dpsRegistrationCallbackContext;
    }

    public void invokeStatusCallback(DPSDeviceStatus status, String reason)
    {
        if (this.dpsStatusCallback != null)
        {
            this.dpsStatusCallback.run(status, reason, this.dpsStatusCallbackContext);
        }
    }

    public void invokeRegistrationCallback(DPSRegistrationInfo registrationInfo)
    {
        if (this.dpsRegistrationCallback != null)
        {
            this.dpsRegistrationCallback.run(registrationInfo, this.dpsRegistrationCallbackContext);
        }
    }

    // this thread will continue to run until DPS status is assigned and registered or exit on error
    // DPS State machine
    @Override
    public Object call() throws Exception
    {
        try
        {
            DPSRegisterTask dpsRegisterTask = new DPSRegisterTask(this.dpsConfig, dpsSecurityClient, dpsTransport, dpsAuthorization);
            FutureTask<DPSResponseParser> futureRegisterTask = new FutureTask<DPSResponseParser>(dpsRegisterTask);
            executor.submit(futureRegisterTask);
            this.dpsStatus = DPSDeviceStatus.DPS_DEVICE_STATUS_READY_TO_AUTHENTICATE;
            invokeStatusCallback(this.dpsStatus, null);

            DPSResponseParser dpsAuthenticationResponseParser = null;
            dpsAuthenticationResponseParser = futureRegisterTask.get(MAX_TIME_TO_WAIT_FOR_REGISTRATION, TimeUnit.MILLISECONDS);

            if (dpsAuthenticationResponseParser == null)
            {
                this.dpsStatus = DPSDeviceStatus.DPS_DEVICE_STATUS_ERROR;
                invokeStatusCallback(this.dpsStatus, null);
                return null;
            }
            DPSStatus status = DPSStatus.fromString(dpsAuthenticationResponseParser.getStatus());
            if (status == null)
            {
                this.dpsStatus = DPSDeviceStatus.DPS_DEVICE_STATUS_ERROR;
                invokeStatusCallback(this.dpsStatus, null);
                return null;
            }
            switch (status)
            {
                case ASSIGNING:

                    if (dpsAuthenticationResponseParser.getOperationId() != null)
                    {
                        this.dpsStatus = DPSDeviceStatus.DPS_DEVICE_STATUS_AUTHENTICATED;
                        invokeStatusCallback(this.dpsStatus, null);
                    }
                    else
                    {
                        invokeStatusCallback(this.dpsStatus, null);
                        throw new DPSAuthenticationException("operation id could not be retrieved, authentication failure");
                    }

                    // look for what else is needed for this step - perhaps just op id for x509
                    DPSResponseParser dpsStatusResponseParser = null;
                    do
                    {
                        DPSStatusTask dpsStatusTask = new DPSStatusTask(dpsSecurityClient, dpsTransport, dpsAuthenticationResponseParser.getOperationId(), this.dpsAuthorization);
                        FutureTask<DPSResponseParser> futureStatusTask = new FutureTask<DPSResponseParser>(dpsStatusTask);
                        executor.submit(futureStatusTask);
                        dpsStatusResponseParser = futureStatusTask.get();
                        if (DPSStatus.fromString(dpsStatusResponseParser.getStatus()) != DPSStatus.ASSIGNED)
                        {
                            this.dpsStatus = DPS_DEVICE_STATUS_ASSIGNING;
                            invokeStatusCallback(this.dpsStatus, null);
                        }
                        Thread.sleep(MAX_TIME_TO_WAIT_FOR_STATUS_UPDATE);
                    }
                    while (DPSStatus.fromString(dpsStatusResponseParser.getStatus()) != DPSStatus.ASSIGNED);

                    this.dpsStatus = DPS_DEVICE_STATUS_ASSIGNED;
                    invokeStatusCallback(this.dpsStatus, null);
                    DPSRegistrationInfo registrationInfo = new DPSRegistrationInfo(dpsStatusResponseParser.getRegistrationStatus().getAssignedHub(), dpsStatusResponseParser.getRegistrationStatus().getDeviceId(), DPS_DEVICE_STATUS_ASSIGNED);
                    invokeRegistrationCallback(registrationInfo);
                    break;
                case ASSIGNED:
                    this.dpsStatus = DPS_DEVICE_STATUS_ASSIGNED;
                    invokeStatusCallback(this.dpsStatus, null);
                    break;
                case FAILED:
                case UNASSIGNED:
                case BLACKLISTED:
                    this.dpsStatus = DPS_DEVICE_STATUS_ERROR;
                    invokeStatusCallback(this.dpsStatus, null);
                    break;
            }
        }
        catch (ExecutionException | TimeoutException | DPSClientException e)
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
