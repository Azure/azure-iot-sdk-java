/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.common.helpers;

import com.microsoft.azure.sdk.iot.service.BaseDevice;
import com.microsoft.azure.sdk.iot.service.Module;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import java.io.IOException;
import java.util.Collection;

public class Tools
{
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