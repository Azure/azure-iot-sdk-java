/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.ModuleClient;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.Module;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.RegistryManagerOptions;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;
import static tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest.HTTP_READ_TIMEOUT;

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

    private static RegistryManager registryManager;

    public static RegistryManager getRegistyManager(String iotHubConnectionString) throws IOException
    {
        if (registryManager == null)
        {
            RegistryManagerOptions options = RegistryManagerOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build();
            registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString, options);
        }

        return registryManager;
    }

    public static TestDeviceIdentity getTestDevice(String iotHubConnectionString, IotHubClientProtocol protocol, AuthenticationType authenticationType) throws URISyntaxException, IOException, IotHubException, GeneralSecurityException, InterruptedException
    {
        RegistryManager registryManager = getRegistyManager(iotHubConnectionString);
        if (authenticationType == SAS)
        {
            String deviceId = "java-file-upload-e2e-test-".concat(UUID.randomUUID().toString());
            Device device = com.microsoft.azure.sdk.iot.service.Device.createFromId(deviceId, null, null);
            device = Tools.addDeviceWithRetry(registryManager, device);

            DeviceClient deviceClient = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), protocol);
            return new TestDeviceIdentity(deviceClient, device);
        }
        else if (authenticationType == SELF_SIGNED)
        {
            String deviceId = "java-file-upload-e2e-test-x509-".concat(UUID.randomUUID().toString());
            Device device = com.microsoft.azure.sdk.iot.service.Device.createDevice(deviceId, SELF_SIGNED);
            String x509Thumbprint = IntegrationTest.x509CertificateGenerator.getX509Thumbprint();
            device.setThumbprintFinal(x509Thumbprint, x509Thumbprint);
            device = Tools.addDeviceWithRetry(registryManager, device);

            SSLContext sslContext =
                SSLContextBuilder.buildSSLContext(
                    IntegrationTest.x509CertificateGenerator.getPublicCertificate(),
                    IntegrationTest.x509CertificateGenerator.getPrivateKey());

            DeviceClient deviceClient = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), protocol, sslContext);

            return new TestDeviceIdentity(deviceClient, device);
        }
        else
        {
            throw new IllegalArgumentException("Test code has not been written for this authentication type yet");
        }
    }

    public static TestModuleIdentity getTestModule(String iotHubConnectionString, IotHubClientProtocol protocol, AuthenticationType authenticationType) throws URISyntaxException, IOException, IotHubException, InterruptedException, ModuleClientException, GeneralSecurityException
    {
        if (authenticationType == SAS)
        {
            //sas device to house the sas module under test
            String deviceId = "java-method-e2e-test-device".concat("-" + UUID.randomUUID().toString());
            Device device = Device.createFromId(deviceId, null, null);
            device = Tools.addDeviceWithRetry(getRegistyManager(iotHubConnectionString), device);

            //sas module client under test
            String moduleId = "java-method-e2e-test-module".concat("-" + UUID.randomUUID().toString());
            Module module = Module.createFromId(deviceId, moduleId, null);
            module = Tools.addModuleWithRetry(getRegistyManager(iotHubConnectionString), module);
            ModuleClient moduleClient = new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, device, module), protocol);

            return new TestModuleIdentity(moduleClient, device, module);
        }
        else if (authenticationType == SELF_SIGNED)
        {
            String x509Thumbprint = IntegrationTest.x509CertificateGenerator.getX509Thumbprint();
            SSLContext sslContext =
                SSLContextBuilder.buildSSLContext(
                    IntegrationTest.x509CertificateGenerator.getPublicCertificate(),
                    IntegrationTest.x509CertificateGenerator.getPrivateKey());

            //x509 device to house the x509 module under test
            String deviceId = "java-method-e2e-test-device-x509".concat("-" + UUID.randomUUID().toString());
            Device device = Device.createDevice(deviceId, SELF_SIGNED);
            device.setThumbprintFinal(x509Thumbprint, x509Thumbprint);
            device = Tools.addDeviceWithRetry(getRegistyManager(iotHubConnectionString), device);

            //x509 module client under test
            String moduleId = "java-method-e2e-test-module-x509".concat("-" + UUID.randomUUID().toString());
            Module module = Module.createModule(deviceId, moduleId, SELF_SIGNED);
            module.setThumbprintFinal(x509Thumbprint, x509Thumbprint);
            module = Tools.addModuleWithRetry(getRegistyManager(iotHubConnectionString), module);
            ModuleClient moduleClient = new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, device, module), protocol, sslContext);

            return new TestModuleIdentity(moduleClient, device, module);
        }
        else
        {
            throw new IllegalArgumentException("Test code has not been written for this path yet");
        }
    }

    public static void disposeTestIdentity(TestIdentity testIdentity, String iotHubConnectionString)
    {
        try
        {
            if (testIdentity != null && !com.microsoft.azure.sdk.iot.service.Tools.isNullOrEmpty(testIdentity.getDeviceId()))
            {
                getRegistyManager(iotHubConnectionString).removeDevice(testIdentity.getDeviceId());
            }
        }
        catch (IOException | IotHubException e)
        {
            log.error("Failed to clean up device identity {}", testIdentity.getDeviceId(), e);
        }
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