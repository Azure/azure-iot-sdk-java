/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.common.helpers;

import com.microsoft.azure.sdk.iot.service.BaseDevice;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.Module;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collection;

public class Tools
{
    private static final long RETRY_TIMEOUT_ON_NETWORK_FAILURE = 60 * 1000;
    private static final long WAIT_FOR_RETRY = 2000;

    public static String retrieveEnvironmentVariableValue(String environmentVariableName)
    {
        String environmentVariableValue = System.getenv().get(environmentVariableName);
        if ((environmentVariableValue == null) || environmentVariableValue.isEmpty())
        {
            environmentVariableValue = System.getProperty(environmentVariableName);
            if (environmentVariableValue == null || environmentVariableValue.isEmpty())
            {
                throw new IllegalArgumentException("Environment variable is not set: " + environmentVariableName);
            }
        }

        return environmentVariableValue;
    }

    /**
     * Checks if the provided exception contains a certain type of exception in its cause chain
     * @param possibleExceptionCause the type of exception to be searched for
     * @param exceptionToSearch the exception to search the stacktrace of
     * @return if any variant of the possibleExceptionCause is found at any depth of the exception cause chain
     */
    public static boolean isCause(Class<? extends Throwable> possibleExceptionCause, Throwable exceptionToSearch)
    {
        return possibleExceptionCause.isInstance(exceptionToSearch) || (exceptionToSearch != null && isCause(possibleExceptionCause, exceptionToSearch.getCause()));
    }

    /**
     * Uses the provided registry manager to delete all the devices and modules specified in the arguments
     * @param registryManager the registry manager to use. Will not be closed after this call
     * @param identitiesToDispose the list of modules and or devices to be removed from the iot hub using the provided
     *                            registry manager
     * @throws IOException if deleting the identity fails
     * @throws IotHubException if deleting the identity fails
     */
    public static void removeDevicesAndModules(RegistryManager registryManager, Collection<BaseDevice> identitiesToDispose)
    {
        if (identitiesToDispose != null && !identitiesToDispose.isEmpty())
        {
            for (BaseDevice identityToDispose : identitiesToDispose)
            {
                try
                {
                    if (identityToDispose instanceof Module)
                    {
                        registryManager.removeModule(identityToDispose.getDeviceId(), ((Module) identityToDispose).getId());
                    }
                }
                catch (IOException | IotHubException e)
                {
                    System.out.println("Failed to remove module " + ((Module) identityToDispose).getId() + " from device " + identityToDispose.getDeviceId());
                    e.printStackTrace();
                }

                try
                {
                    registryManager.removeDevice(identityToDispose.getDeviceId());
                }
                catch (IOException | IotHubException e)
                {
                    System.out.println("Failed to remove device " + identityToDispose.getDeviceId());
                    e.printStackTrace();
                }
            }
        }
    }

    public static Device addDeviceWithRetry(RegistryManager registryManager, Device device) throws IotHubException, IOException, InterruptedException
    {
        long startTime = System.currentTimeMillis();
        Device ret = null;
        while (System.currentTimeMillis() - startTime < RETRY_TIMEOUT_ON_NETWORK_FAILURE)
        {
            try
            {
                ret = registryManager.addDevice(device);
                break;
            }
            catch (UnknownHostException | SocketException e)
            {
                System.out.println("Failed to add device " + device.getDeviceId());
                e.printStackTrace();
                Thread.sleep(WAIT_FOR_RETRY);
                if (System.currentTimeMillis() - startTime >= RETRY_TIMEOUT_ON_NETWORK_FAILURE)
                {
                    throw e;
                }
            }

        }

        return ret;
    }

    public static Module addModuleWithRetry(RegistryManager registryManager, Module module) throws IotHubException, IOException, InterruptedException
    {
        long startTime = System.currentTimeMillis();
        Module ret = null;
        while (System.currentTimeMillis() - startTime < RETRY_TIMEOUT_ON_NETWORK_FAILURE)
        {
            try
            {
                ret = registryManager.addModule(module);
                break;
            }
            catch (UnknownHostException | SocketException e)
            {
                System.out.println("Failed to add module " + module.getId());
                e.printStackTrace();
                Thread.sleep(WAIT_FOR_RETRY);
                if (System.currentTimeMillis() - startTime >= RETRY_TIMEOUT_ON_NETWORK_FAILURE)
                {
                    throw e;
                }
            }
        }
        return ret;
    }

    public static void getStatisticsWithRetry(RegistryManager registryManager) throws IotHubException, IOException, InterruptedException
    {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < RETRY_TIMEOUT_ON_NETWORK_FAILURE)
        {
            try
            {
                registryManager.getStatistics();
                break;
            }
            catch (UnknownHostException | SocketException e)
            {
                System.out.println("Failed to get statistics ");
                e.printStackTrace();
                Thread.sleep(WAIT_FOR_RETRY);
                if (System.currentTimeMillis() - startTime >= RETRY_TIMEOUT_ON_NETWORK_FAILURE)
                {
                    throw e;
                }
            }
        }


    }

    public static Device getDeviceWithRetry(RegistryManager registryManager, String id) throws IotHubException, IOException, InterruptedException
    {
        long startTime = System.currentTimeMillis();
        Device ret = null;
        while (System.currentTimeMillis() - startTime < RETRY_TIMEOUT_ON_NETWORK_FAILURE)
        {
            try
            {
                ret = registryManager.getDevice(id);
                break;
            }
            catch (UnknownHostException | SocketException e)
            {
                System.out.println("Failed to get device ");
                e.printStackTrace();
                Thread.sleep(WAIT_FOR_RETRY);
                if (System.currentTimeMillis() - startTime >= RETRY_TIMEOUT_ON_NETWORK_FAILURE)
                {
                    throw e;
                }
            }
        }
        return ret;
    }

    public static Module getModuleWithRetry(RegistryManager registryManager, String deviceid, String moduleid) throws IotHubException, IOException, InterruptedException
    {
        long startTime = System.currentTimeMillis();
        Module ret = null;
        while (System.currentTimeMillis() - startTime < RETRY_TIMEOUT_ON_NETWORK_FAILURE)
        {
            try
            {
                ret = registryManager.getModule(deviceid, moduleid);
                break;
            }
            catch (UnknownHostException | SocketException e)
            {
                System.out.println("Failed to get module ");
                e.printStackTrace();
                Thread.sleep(WAIT_FOR_RETRY);
                if (System.currentTimeMillis() - startTime >= RETRY_TIMEOUT_ON_NETWORK_FAILURE)
                {
                    throw e;
                }
            }
        }
        return ret;
    }

    public static String getStackTraceFromThrowable(Throwable throwable)
    {
        return ExceptionUtils.getStackTrace(throwable);
    }
}