/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.dps.device;

import com.microsoft.azure.sdk.iot.dps.device.privateapi.dpstask.DPSTask;
import com.microsoft.azure.sdk.iot.dps.device.privateapi.DPSTransport;
import com.microsoft.azure.sdk.iot.dps.device.privateapi.exceptions.DPSClientException;
import com.microsoft.azure.sdk.iot.dps.security.hsm.DPSSecurityClientDiceEmulator;
import com.microsoft.azure.sdk.iot.dps.security.hsm.DPSSecurityClientTPMEmulator;
import com.microsoft.azure.sdk.iot.dps.security.DPSHsmType;
import com.microsoft.azure.sdk.iot.dps.security.DPSSecurityClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DpsDeviceClient
{
    private static final int MAX_THREADS_TO_RUN = 2;

    private DPSConfig dpsConfig;
    private DpsStatusCallback dpsStatusCallback;
    private Object dpsStatusCallbackContext;

    private DPSSecurityClient dpsSecurityClient;
    private DPSTransport dpsTransport;
    private DPSTask dpsTask;

    private ExecutorService executor;

    public DpsDeviceClient(DPSConfig dpsConfig, DpsStatusCallback dpsStatusCallBack, Object dpsCallbackContext) throws DPSClientException
    {
        if(dpsConfig == null || dpsStatusCallBack == null)
        {
            throw new IllegalArgumentException("dpsConfig or dpsStatusCallback cannot be null");
        }

        //save parameters
        this.dpsConfig = dpsConfig;
        this.dpsStatusCallback = dpsStatusCallBack;
        this.dpsStatusCallbackContext = dpsCallbackContext;

        //create relevant security client
        this.dpsSecurityClient = this.createRelevantSecurityObject();

        //transport create
        this.dpsTransport = DPSTransport.createDPSTransport(dpsConfig);

        this.dpsTask = new DPSTask(this.dpsConfig, this.dpsSecurityClient, this.dpsTransport, dpsStatusCallback, dpsStatusCallbackContext);

        this.executor = Executors.newFixedThreadPool(MAX_THREADS_TO_RUN);
    }

    public void registerDevice(DPSRegistrationCallback dpsRegistrationCallback, Object dpsRegistrationCallbackContext) throws DPSClientException
    {
        if (dpsRegistrationCallback == null)
        {
            throw new DPSClientException("registration callback cannot be null");
        }

        this.dpsTask.setRegistrationCallback(dpsRegistrationCallback, dpsRegistrationCallbackContext);
        // how do you plan to handle exception here as there is no get
        executor.submit(this.dpsTask);
    }

    public void close()
    {
        if (dpsTask != null)
        {
            dpsTask.close();
        }

        if (executor != null && !executor.isTerminated())
        {
            executor.shutdownNow();
        }
    }

    private DPSSecurityClient createRelevantSecurityObject()
    {
        DPSSecurityClient dpsSecurityClient = null;

        if (this.dpsConfig.getDPSHsmType() == DPSHsmType.TPM_EMULATOR)
        {
            dpsSecurityClient = new DPSSecurityClientTPMEmulator();
        }
        else if (this.dpsConfig.getDPSHsmType() == DPSHsmType.DICE_EMULATOR)
        {
            dpsSecurityClient = new DPSSecurityClientDiceEmulator();
        }
        else if (this.dpsConfig.getDPSHsmType() == DPSHsmType.THIRD_PARTY)
        {
            dpsSecurityClient = dpsConfig.getThirdPartySecurityType();
        }

        return dpsSecurityClient;
    }
}
