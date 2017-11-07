/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device;

import com.microsoft.azure.sdk.iot.provisioning.device.internal.ProvisioningDeviceClientConfig;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.ProvisioningTask;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ProvisioningDeviceClientContract;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProvisioningDeviceClient
{
    private static final int MAX_THREADS_TO_RUN = 1;

    private ProvisioningDeviceClientConfig provisioningDeviceClientConfig;
    private ProvisioningDeviceClientContract provisioningDeviceClientContract;
    private ExecutorService executor;

    public static ProvisioningDeviceClient create(String globalEndpoint, String scopeId, ProvisioningDeviceClientTransportProtocol protocol, SecurityClient securityClient) throws ProvisioningDeviceClientException
    {
        return new ProvisioningDeviceClient(globalEndpoint, scopeId, protocol, securityClient);
    }

    private ProvisioningDeviceClient(String globalEndpoint, String scopeId, ProvisioningDeviceClientTransportProtocol protocol, SecurityClient securityClient) throws ProvisioningDeviceClientException
    {
        if (globalEndpoint == null || globalEndpoint.isEmpty())
        {
            //SRS_ProvisioningDeviceClient_25_001: [ The constructor shall throw IllegalArgumentException if globalEndpoint is null or empty. ]
            throw new IllegalArgumentException("global endpoint cannot be null or empty");
        }

        if (scopeId == null || scopeId.isEmpty())
        {
            //SRS_ProvisioningDeviceClient_25_002: [ The constructor shall throw IllegalArgumentException if scopeId is null or empty. ]
            throw new IllegalArgumentException("scope id cannot be null or empty");
        }

        if (protocol == null)
        {
            //SRS_ProvisioningDeviceClient_25_003: [ The constructor shall throw IllegalArgumentException if protocol is null. ]
            throw new IllegalArgumentException("protocol cannot be null");
        }

        if (securityClient == null)
        {
            //SRS_ProvisioningDeviceClient_25_004: [ The constructor shall throw IllegalArgumentException if securityClient is null. ]
            throw new IllegalArgumentException("Security client cannot be null");
        }

        //SRS_ProvisioningDeviceClient_25_005: [ The constructor shall create provisioningDeviceClientConfig and set all the provided values to it.. ]
        this.provisioningDeviceClientConfig = new ProvisioningDeviceClientConfig();

        this.provisioningDeviceClientConfig.setProvisioningServiceGlobalEndpoint(globalEndpoint);
        this.provisioningDeviceClientConfig.setScopeId(scopeId);
        this.provisioningDeviceClientConfig.setProtocol(protocol);
        this.provisioningDeviceClientConfig.setSecurityClient(securityClient);

        //SRS_ProvisioningDeviceClient_25_006: [ The constructor shall create provisioningDeviceClientContract with the given config. ]
        this.provisioningDeviceClientContract = ProvisioningDeviceClientContract.createProvisioningContract(this.provisioningDeviceClientConfig);
        //SRS_ProvisioningDeviceClient_25_007: [ The constructor shall create an executor service with fixed thread pool of size 1. ]
        this.executor = Executors.newFixedThreadPool(MAX_THREADS_TO_RUN);
    }

    public void registerDevice(ProvisioningDeviceClientRegistrationCallback provisioningDeviceClientRegistrationCallback, Object context) throws ProvisioningDeviceClientException
    {
        if (provisioningDeviceClientRegistrationCallback == null)
        {
            //SRS_ProvisioningDeviceClient_25_008: [ This method shall throw IllegalArgumentException if provisioningDeviceClientRegistrationCallback is null. ]
            throw new IllegalArgumentException("registration callback cannot be null");
        }

        //SRS_ProvisioningDeviceClient_25_009: [ This method shall set the config with the callback. ]
        this.provisioningDeviceClientConfig.setRegistrationCallback(provisioningDeviceClientRegistrationCallback, context);

        //SRS_ProvisioningDeviceClient_25_010: [ This method shall start the executor with the ProvisioningTask. ]
        ProvisioningTask provisioningTask = new ProvisioningTask(this.provisioningDeviceClientConfig, this.provisioningDeviceClientContract);
        executor.submit(provisioningTask);
    }

    public void closeNow()
    {
        //SRS_ProvisioningDeviceClient_25_011: [ This method shall check if executor is terminated and if not shall shutdown the executor. ]
        if (executor != null && !executor.isTerminated())
        {
            executor.shutdownNow();
        }
    }
}
