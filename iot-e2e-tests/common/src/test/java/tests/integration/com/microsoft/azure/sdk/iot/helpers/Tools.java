/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import com.microsoft.azure.sdk.iot.deps.serializer.AuthenticationParser;
import com.microsoft.azure.sdk.iot.deps.serializer.ExportImportDeviceParser;
import com.microsoft.azure.sdk.iot.deps.serializer.SymmetricKeyParser;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.Module;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubExceptionManager;
import com.microsoft.azure.sdk.iot.service.transport.TransportUtils;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
            log.debug("Likely running the JVM tests, ignoring ClassNotFoundException\n");
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

    public static Device addDeviceWithRetry(RegistryManager registryManager, Device device) throws IotHubException, IOException, InterruptedException
    {
        long startTime = System.currentTimeMillis();
        Device ret = null;
        while (System.currentTimeMillis() - startTime < RETRY_TIMEOUT_ON_NETWORK_FAILURE_MILLISECONDS)
        {
            try
            {
                log.debug("Attempting to add device {} to registry", device.getDeviceId());
                ret = registryManager.addDevice(device);
                log.debug("Successfully added device {} to registry", device.getDeviceId());
                break;
            }
            catch (UnknownHostException | SocketException | SocketTimeoutException e)
            {
                log.warn("Failed to add device " + device.getDeviceId());
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

    public static void addDevicesWithRetry(List<Device> devices, String connectionString) throws IotHubException, IOException, InterruptedException
    {
        // IoT hub only allows for bulk adding of devices at up to 100 per request, so take the provided devices iterable
        // and break it into 100 devices or smaller chunks
        List<Device> subIterable = new ArrayList<>();
        List<Device> devicesClone = new ArrayList<>(); //create a clone of the source list so elements can be removed from it instead
        devicesClone.addAll(devices);
        while (devicesClone.size() > 0)
        {
            Device device = devicesClone.remove(0);
            subIterable.add(device);

            // wait until sub list has the Iot Hub limit of 100 devices to add, or until there will be no more devices to add
            if (subIterable.size() > 99 || devicesClone.size() <= 0)
            {
                long startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < RETRY_TIMEOUT_ON_NETWORK_FAILURE_MILLISECONDS)
                {
                    try
                    {
                        addDevices(subIterable, connectionString);
                        break;
                    }
                    catch (UnknownHostException | SocketException | SocketTimeoutException e)
                    {
                        log.warn("Failed to add devices");
                        e.printStackTrace();
                        Thread.sleep(WAIT_FOR_RETRY);
                        if (System.currentTimeMillis() - startTime >= RETRY_TIMEOUT_ON_NETWORK_FAILURE_MILLISECONDS)
                        {
                            throw e;
                        }
                    }

                }

                // clear the sub list so it can be filled back up again with the next devices to add
                subIterable.clear();
            }
        }
    }

    // This call mimics what should be a registry manager API for adding devices in bulk. Can be removed once we add support in our
    // registry manager for this
    private static final String IMPORT_MODE = "create";
    public static void addDevices(Iterable<Device> devices, String connectionString) throws IOException, IotHubException {
        if (devices == null)
        {
            throw new IllegalArgumentException("devices cannot be null");
        }

        IotHubConnectionString iotHubConnectionString = IotHubConnectionString.createConnectionString(connectionString);
        URL url = getBulkDeviceAddUrl(iotHubConnectionString);

        List<ExportImportDeviceParser> parsers = new ArrayList<>();
        for (Device device : devices)
        {
            ExportImportDeviceParser exportImportDevice = new ExportImportDeviceParser();
            exportImportDevice.setId(device.getDeviceId());
            AuthenticationParser authenticationParser = new AuthenticationParser();
            authenticationParser.setSymmetricKey(new SymmetricKeyParser(device.getSymmetricKey().getPrimaryKey(), device.getSymmetricKey().getSecondaryKey()));
            exportImportDevice.setAuthentication(authenticationParser);
            exportImportDevice.setImportMode(IMPORT_MODE);
            parsers.add(exportImportDevice);
        }

        ExportImportDevicesParser body = new ExportImportDevicesParser();
        body.setExportImportDevices(parsers);

        String sasTokenString = new IotHubServiceSasToken(iotHubConnectionString).toString();

        HttpRequest request = new HttpRequest(url, HttpMethod.POST, body.toJson().getBytes());
        request.setReadTimeoutMillis(IntegrationTest.HTTP_READ_TIMEOUT);
        request.setHeaderField("authorization", sasTokenString);
        request.setHeaderField("Accept", "application/json");
        request.setHeaderField("Content-Type", "application/json");
        request.setHeaderField("charset", "utf-8");

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);
    }

    public static URL getBulkDeviceAddUrl(IotHubConnectionString iotHubConnectionString) throws MalformedURLException
    {
        String stringBuilder = "https://" +
                iotHubConnectionString.getHostName() +
                "/devices/?api-version=" +
                TransportUtils.IOTHUB_API_VERSION;
        return new URL(stringBuilder);
    }

    public static Module addModuleWithRetry(RegistryManager registryManager, Module module) throws IotHubException, IOException, InterruptedException
    {
        long startTime = System.currentTimeMillis();
        Module ret = null;
        while (System.currentTimeMillis() - startTime < RETRY_TIMEOUT_ON_NETWORK_FAILURE_MILLISECONDS)
        {
            try
            {
                log.debug("Attempting to add module {} to registry", module.getId());
                ret = registryManager.addModule(module);
                log.debug("Successfully added module {} to registry", module.getId());
                break;
            }
            catch (UnknownHostException | SocketException | SocketTimeoutException e)
            {
                log.warn("Failed to add module " + module.getId());
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