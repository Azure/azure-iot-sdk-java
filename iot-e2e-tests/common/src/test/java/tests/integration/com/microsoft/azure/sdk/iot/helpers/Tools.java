/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.Module;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Tools
{
    private static final long RETRY_TIMEOUT_ON_NETWORK_FAILURE_MILLISECONDS = 60 * 1000;
    private static final long WAIT_FOR_RETRY = 2000;

    private static final String ANDROID_BUILD_CONFIG_CLASS = "com.iothub.azure.microsoft.com.androide2e.test.BuildConfig";
    private static final Map<String, String> ANDROID_ENV_VAR = retrieveAndroidEnvVariables();

    public static String retrieveEnvironmentVariableValue(String environmentVariableName)
    {
        String environmentVariableValue;

        if (ANDROID_ENV_VAR.containsKey(environmentVariableName))
        {
            environmentVariableValue = ANDROID_ENV_VAR.get(environmentVariableName);
        }
        else
        {
            environmentVariableValue = System.getenv().get(environmentVariableName);
            if ((environmentVariableValue == null) || environmentVariableValue.isEmpty())
            {
                environmentVariableValue = System.getProperty(environmentVariableName);
            }
        }

        return environmentVariableValue;
    }

    public static String retrieveEnvironmentVariableValue(String environmentVariableName, String defaultValue)
    {
        String environmentVariableValue;
        if (ANDROID_ENV_VAR.containsKey(environmentVariableName))
        {
            environmentVariableValue = ANDROID_ENV_VAR.get(environmentVariableName);
        }
        else
        {
            environmentVariableValue = System.getenv().get(environmentVariableName);

            if ((environmentVariableValue == null) || environmentVariableValue.isEmpty())
            {
                environmentVariableValue = System.getProperty(environmentVariableName);
            }
        }

        if (environmentVariableValue == null || environmentVariableValue.isEmpty())
        {
            return defaultValue;
        }

        return environmentVariableValue;
    }

    private static Map<String, String> retrieveAndroidEnvVariables()
    {
        Map<String, String> envVariables = new HashMap<>();
        try
        {
            Class buildConfig = Class.forName(ANDROID_BUILD_CONFIG_CLASS);
            Arrays.stream(buildConfig.getFields()).forEach(field -> {
                try
                {
                    envVariables.put(field.getName(), field.get(null).toString());
                }
                catch (IllegalAccessException e)
                {
                    log.error("Cannot access the following field: {}", field.getName(), e);
                }
            });
        }
        catch (ClassNotFoundException e)
        {
            log.debug("Likely running the JVM tests, ignoring ClassNotFoundException");
        }

        return envVariables;
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

    public synchronized static Device addDeviceWithRetry(RegistryManager registryManager, Device device) throws IotHubException, IOException, InterruptedException
    {
        long startTime = System.currentTimeMillis();
        Device ret = null;
        while (System.currentTimeMillis() - startTime < RETRY_TIMEOUT_ON_NETWORK_FAILURE_MILLISECONDS)
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
                if (System.currentTimeMillis() - startTime >= RETRY_TIMEOUT_ON_NETWORK_FAILURE_MILLISECONDS)
                {
                    throw e;
                }
            }

        }

        return ret;
    }

    public synchronized static Module addModuleWithRetry(RegistryManager registryManager, Module module) throws IotHubException, IOException, InterruptedException
    {
        long startTime = System.currentTimeMillis();
        Module ret = null;
        while (System.currentTimeMillis() - startTime < RETRY_TIMEOUT_ON_NETWORK_FAILURE_MILLISECONDS)
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
                if (System.currentTimeMillis() - startTime >= RETRY_TIMEOUT_ON_NETWORK_FAILURE_MILLISECONDS)
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
        while (System.currentTimeMillis() - startTime < RETRY_TIMEOUT_ON_NETWORK_FAILURE_MILLISECONDS)
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
                if (System.currentTimeMillis() - startTime >= RETRY_TIMEOUT_ON_NETWORK_FAILURE_MILLISECONDS)
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
        while (System.currentTimeMillis() - startTime < RETRY_TIMEOUT_ON_NETWORK_FAILURE_MILLISECONDS)
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
                if (System.currentTimeMillis() - startTime >= RETRY_TIMEOUT_ON_NETWORK_FAILURE_MILLISECONDS)
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
        while (System.currentTimeMillis() - startTime < RETRY_TIMEOUT_ON_NETWORK_FAILURE_MILLISECONDS)
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
                if (System.currentTimeMillis() - startTime >= RETRY_TIMEOUT_ON_NETWORK_FAILURE_MILLISECONDS)
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