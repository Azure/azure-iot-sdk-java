/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device;

import com.microsoft.azure.sdk.iot.provisioning.security.SecurityClient;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.provisioningtask.ProvisioningTask;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ProvisioningDeviceClientContract;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProvisioningDeviceClient
{
    private static final int MAX_THREADS_TO_RUN = 2;

    private ProvisioningDeviceClientConfig provisioningDeviceClientConfig;
    private ProvisioningDeviceClientStatusCallback provisioningDeviceClientStatusCallback;
    private Object dpsStatusCallbackContext;

    private SecurityClient securityClient;
    private ProvisioningDeviceClientContract provisioningDeviceClientContract;
    private ProvisioningTask provisioningTask;

    private ExecutorService executor;

    public ProvisioningDeviceClient(ProvisioningDeviceClientConfig provisioningDeviceClientConfig, ProvisioningDeviceClientStatusCallback provisioningDeviceClientStatusCallBack, Object dpsCallbackContext) throws ProvisioningDeviceClientException
    {
        if(provisioningDeviceClientConfig == null || provisioningDeviceClientStatusCallBack == null)
        {
            throw new IllegalArgumentException("provisioningDeviceClientConfig or provisioningDeviceClientStatusCallback cannot be null");
        }

        //save parameters
        this.provisioningDeviceClientConfig = provisioningDeviceClientConfig;
        this.provisioningDeviceClientStatusCallback = provisioningDeviceClientStatusCallBack;
        this.dpsStatusCallbackContext = dpsCallbackContext;

        //create relevant security client
        this.securityClient = this.provisioningDeviceClientConfig.getSecurityClient();

        //transport create
        this.provisioningDeviceClientContract = ProvisioningDeviceClientContract.createProvisioningContract(provisioningDeviceClientConfig);

        this.provisioningTask = new ProvisioningTask(this.provisioningDeviceClientConfig, this.securityClient, this.provisioningDeviceClientContract, provisioningDeviceClientStatusCallback, dpsStatusCallbackContext);

        this.executor = Executors.newFixedThreadPool(MAX_THREADS_TO_RUN);
    }

    public void registerDevice(ProvisioningDeviceClientRegistrationCallback provisioningDeviceClientRegistrationCallback, Object dpsRegistrationCallbackContext) throws ProvisioningDeviceClientException
    {
        if (provisioningDeviceClientRegistrationCallback == null)
        {
            throw new ProvisioningDeviceClientException("registration callback cannot be null");
        }

        this.provisioningTask.setRegistrationCallback(provisioningDeviceClientRegistrationCallback, dpsRegistrationCallbackContext);
        // how do you plan to handle exception here as there is no get
        executor.submit(this.provisioningTask);
    }

    public void close()
    {
        if (provisioningTask != null)
        {
            provisioningTask.close();
        }

        if (executor != null && !executor.isTerminated())
        {
            executor.shutdownNow();
        }
    }
}
