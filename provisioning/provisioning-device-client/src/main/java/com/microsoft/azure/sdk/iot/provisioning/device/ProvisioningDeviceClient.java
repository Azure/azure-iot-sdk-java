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
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProvisioningDeviceClient
{
    private static final int MAX_THREADS_TO_RUN = 1;

    private ProvisioningDeviceClientConfig provisioningDeviceClientConfig;
    private ProvisioningDeviceClientContract provisioningDeviceClientContract;
    private ExecutorService executor;

    /**
     * Creates an instance of ProvisioningDeviceClient
     * @param globalEndpoint global endpoint for the service to connect to. Cannot be {@code null}.
     * @param idScope IdScope for the instance of the service hosted by you. Cannot be {@code null}.
     * @param protocol Protocol to communicate with the service onto. Cannot be {@code null}.
     * @param securityProvider Security Provider for X509 or TPM flow. Cannot be {@code null}.
     * @return An instance of ProvisioningDeviceClient
     * @throws ProvisioningDeviceClientException if any of the underlying API calls fail to process.
     */
    public static ProvisioningDeviceClient create(String globalEndpoint, String idScope, ProvisioningDeviceClientTransportProtocol protocol, SecurityProvider securityProvider) throws ProvisioningDeviceClientException
    {
        return new ProvisioningDeviceClient(globalEndpoint, idScope, protocol, securityProvider);
    }

    private ProvisioningDeviceClient(String globalEndpoint, String idScope, ProvisioningDeviceClientTransportProtocol protocol, SecurityProvider securityProvider) throws ProvisioningDeviceClientException
    {
        if (globalEndpoint == null || globalEndpoint.isEmpty())
        {
            //SRS_ProvisioningDeviceClient_25_001: [ The constructor shall throw IllegalArgumentException if globalEndpoint is null or empty. ]
            throw new IllegalArgumentException("global endpoint cannot be null or empty");
        }

        if (idScope == null || idScope.isEmpty())
        {
            //SRS_ProvisioningDeviceClient_25_002: [ The constructor shall throw IllegalArgumentException if idScope is null or empty. ]
            throw new IllegalArgumentException("scope id cannot be null or empty");
        }

        if (protocol == null)
        {
            //SRS_ProvisioningDeviceClient_25_003: [ The constructor shall throw IllegalArgumentException if protocol is null. ]
            throw new IllegalArgumentException("protocol cannot be null");
        }

        if (securityProvider == null)
        {
            //SRS_ProvisioningDeviceClient_25_004: [ The constructor shall throw IllegalArgumentException if securityProvider is null. ]
            throw new IllegalArgumentException("Security provider cannot be null");
        }

        //SRS_ProvisioningDeviceClient_25_005: [ The constructor shall create provisioningDeviceClientConfig and set all the provided values to it.. ]
        this.provisioningDeviceClientConfig = new ProvisioningDeviceClientConfig();

        this.provisioningDeviceClientConfig.setProvisioningServiceGlobalEndpoint(globalEndpoint);
        this.provisioningDeviceClientConfig.setIdScope(idScope);
        this.provisioningDeviceClientConfig.setProtocol(protocol);
        this.provisioningDeviceClientConfig.setSecurityProvider(securityProvider);

        //SRS_ProvisioningDeviceClient_25_006: [ The constructor shall create provisioningDeviceClientContract with the given config. ]
        this.provisioningDeviceClientContract = ProvisioningDeviceClientContract.createProvisioningContract(this.provisioningDeviceClientConfig);
        //SRS_ProvisioningDeviceClient_25_007: [ The constructor shall create an executor service with fixed thread pool of size 1. ]
        this.executor = Executors.newFixedThreadPool(MAX_THREADS_TO_RUN);
    }

    /**
     * Register's a device with the service and provides you with iothub uri and the registered device.
     * @param provisioningDeviceClientRegistrationCallback Callback where you can retrieve the status of registration like iothub uri and the registered device or
     *                                                     any exception that was caused during registration process. Cannot be {@code null}.
     * @param context Context for the callback. Can be {@code null}.
     * @throws ProvisioningDeviceClientException if any of the underlying API calls fail to process.
     */
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

    /**
     * Set the Custom Provisioning payload to send to DPS during the registration process
     * @param jsonPayload The json payload that will be transferred to DPS
     */
    public void setProvisioningPayload(String jsonPayload)
    {
        this.provisioningDeviceClientConfig.setCustomPayload(jsonPayload);
    }

    /**
     * Closes all the executors opened by the client if they have not already closed.
     */
    public void closeNow()
    {
        //SRS_ProvisioningDeviceClient_25_011: [ This method shall check if executor is terminated and if not shall shutdown the executor. ]
        if (executor != null && !executor.isTerminated())
        {
            executor.shutdownNow();
        }
    }
}
