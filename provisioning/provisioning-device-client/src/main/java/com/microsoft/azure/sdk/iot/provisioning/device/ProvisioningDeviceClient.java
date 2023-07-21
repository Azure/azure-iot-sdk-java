/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device;

import com.microsoft.azure.sdk.iot.provisioning.device.internal.ProvisioningDeviceClientConfig;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.SDKUtils;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceHubException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.ProvisioningTask;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ProvisioningDeviceClientContract;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class ProvisioningDeviceClient
{
    private static final int MAX_THREADS_TO_RUN = 1;
    private static final int DEFAULT_TIMEOUT_SECONDS = 60;

    private final ProvisioningDeviceClientConfig provisioningDeviceClientConfig;
    private final ProvisioningDeviceClientContract provisioningDeviceClientContract;
    private final ExecutorService executor;

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
            throw new IllegalArgumentException("global endpoint cannot be null or empty");
        }

        if (idScope == null || idScope.isEmpty())
        {
            throw new IllegalArgumentException("scope id cannot be null or empty");
        }

        if (protocol == null)
        {
            throw new IllegalArgumentException("protocol cannot be null");
        }

        if (securityProvider == null)
        {
            throw new IllegalArgumentException("Security provider cannot be null");
        }

        this.provisioningDeviceClientConfig = new ProvisioningDeviceClientConfig();

        this.provisioningDeviceClientConfig.setProvisioningServiceGlobalEndpoint(globalEndpoint);
        this.provisioningDeviceClientConfig.setIdScope(idScope);
        this.provisioningDeviceClientConfig.setProtocol(protocol);
        this.provisioningDeviceClientConfig.setSecurityProvider(securityProvider);

        this.provisioningDeviceClientContract = ProvisioningDeviceClientContract.createProvisioningContract(this.provisioningDeviceClientConfig);
        this.executor = Executors.newFixedThreadPool(MAX_THREADS_TO_RUN);

        log.debug("Initialized a ProvisioningDeviceClient instance using SDK version {}", SDKUtils.PROVISIONING_DEVICE_CLIENT_VERSION);
    }

    /**
     * Asynchronously registers a device with the service and provides you with iothub uri and the registered device.
     * @param provisioningDeviceClientRegistrationCallback Callback where you can retrieve the status of registration like iothub uri and the registered device or
     *                                                     any exception that was caused during registration process. Cannot be {@code null}.
     * @param context Context for the callback. Can be {@code null}.
     * @throws ProvisioningDeviceClientException if any of the underlying API calls fail to process.
     */
    public void registerDevice(ProvisioningDeviceClientRegistrationCallback provisioningDeviceClientRegistrationCallback, Object context) throws ProvisioningDeviceClientException
    {
        if (provisioningDeviceClientRegistrationCallback == null)
        {
            throw new IllegalArgumentException("registration callback cannot be null");
        }

        this.provisioningDeviceClientConfig.setRegistrationCallback(provisioningDeviceClientRegistrationCallback, context);

        log.debug("Starting provisioning thread...");
        Callable<Object> provisioningTask = new ProvisioningTask(this.provisioningDeviceClientConfig, this.provisioningDeviceClientContract);
        executor.submit(provisioningTask);
    }

    /**
     * Asynchronously registers a device with the service and provides you with iothub uri and the registered device.
     * @param provisioningDeviceClientRegistrationCallback Callback where you can retrieve the status of registration like iothub uri and the registered device or
     *                                                     any exception that was caused during registration process. Cannot be {@code null}.
     * @param context Context for the callback. Can be {@code null}.
     * @param additionalData Additional data for device registration.
     * @throws ProvisioningDeviceClientException if any of the underlying API calls fail to process.
     */
    public void registerDevice(ProvisioningDeviceClientRegistrationCallback provisioningDeviceClientRegistrationCallback, Object context, AdditionalData additionalData) throws ProvisioningDeviceClientException
    {
        if (provisioningDeviceClientRegistrationCallback == null)
        {
            throw new IllegalArgumentException("registration callback cannot be null");
        }

        if (additionalData != null)
        {
            this.provisioningDeviceClientConfig.setPayload(additionalData.getProvisioningPayload());
        }

        this.provisioningDeviceClientConfig.setRegistrationCallback(provisioningDeviceClientRegistrationCallback, context);

        log.debug("Starting provisioning thread...");
        ProvisioningTask provisioningTask = new ProvisioningTask(this.provisioningDeviceClientConfig, this.provisioningDeviceClientContract);
        executor.submit(provisioningTask);
    }

    /**
     * Synchronously registers a device with the service and provides you with iothub uri and the registered device.
     * @return the details of the finished provisioning.
     * @throws ProvisioningDeviceClientException If any exception is thrown during provisioning.
     * @throws InterruptedException If this operation is interrupted while waiting for the provisioning to finish.
     */
    public ProvisioningDeviceClientRegistrationResult registerDeviceSync() throws ProvisioningDeviceClientException, InterruptedException
    {
        return registerDeviceSync(DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * Synchronously registers a device with the service and provides you with iothub uri and the registered device.
     * @param additionalData Additional data for device registration.
     * @return the details of the finished provisioning.
     * @throws ProvisioningDeviceClientException If any exception is thrown during provisioning.
     * @throws InterruptedException If this operation is interrupted while waiting for the provisioning to finish.
     */
    public ProvisioningDeviceClientRegistrationResult registerDeviceSync(AdditionalData additionalData) throws ProvisioningDeviceClientException, InterruptedException
    {
        return registerDeviceSync(additionalData, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * Synchronously registers a device with the service and provides you with iothub uri and the registered device.
     * @param timeout the amount of time, in seconds, to wait for provisioning to finish. If this operation exceeds the
     * provided timeout, it will throw {@link ProvisioningDeviceClientException}.
     * @return the details of the finished provisioning.
     * @throws ProvisioningDeviceClientException If any exception is thrown during provisioning.
     * @throws InterruptedException If this operation is interrupted while waiting for the provisioning to finish.
     */
    public ProvisioningDeviceClientRegistrationResult registerDeviceSync(int timeout) throws ProvisioningDeviceClientException, InterruptedException
    {
        return registerDeviceSync(null, timeout);
    }

    /**
     * Synchronously registers a device with the service and provides you with iothub uri and the registered device.
     * @param additionalData Additional data for device registration.
     * @param timeout the amount of time, in seconds, to wait for provisioning to finish. If this operation exceeds the
     * provided timeout, it will throw {@link ProvisioningDeviceClientException}.
     * @return the details of the finished provisioning.
     * @throws ProvisioningDeviceClientException If any exception is thrown during provisioning.
     * @throws InterruptedException If this operation is interrupted while waiting for the provisioning to finish.
     */
    public ProvisioningDeviceClientRegistrationResult registerDeviceSync(AdditionalData additionalData, int timeout) throws ProvisioningDeviceClientException, InterruptedException
    {
        final CountDownLatch registrationLatch = new CountDownLatch(1);
        AtomicReference<ProvisioningDeviceClientRegistrationResult> registrationResultReference = new AtomicReference<>();
        AtomicReference<Exception> registrationExceptionReference = new AtomicReference<>();
        registerDevice(
            (provisioningDeviceClientRegistrationResult, e, context) ->
            {
                registrationResultReference.set(provisioningDeviceClientRegistrationResult);
                registrationExceptionReference.set(e);
                registrationLatch.countDown();
            },
            null,
            additionalData);

        // Wait until registration finishes or for a max amount of time
        boolean timedOut = !registrationLatch.await(timeout, TimeUnit.SECONDS);
        if (timedOut)
        {
            throw new ProvisioningDeviceClientException("Timed out waiting for registration to complete");
        }

        ProvisioningDeviceClientRegistrationResult registrationResult = registrationResultReference.get();
        Exception registrationException = registrationExceptionReference.get();

        if (registrationException != null)
        {
            if (registrationException instanceof ProvisioningDeviceHubException)
            {
                throw (ProvisioningDeviceHubException) registrationException;
            }
            else
            {
                // This shouldn't happen because the registration exception should always be of type ProvisioningDeviceHubException
                throw new ProvisioningDeviceClientException("Failed to register", registrationException);
            }
        }

        return registrationResult;
    }

    /**
     * Closes all the executors opened by the client if they have not already closed.
     */
    public void close()
    {
        if (executor != null && !executor.isTerminated())
        {
            executor.shutdownNow();
        }
    }
}
