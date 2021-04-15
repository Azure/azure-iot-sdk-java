/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import com.microsoft.azure.sdk.iot.deps.serializer.AuthenticationParser;
import com.microsoft.azure.sdk.iot.deps.serializer.AuthenticationTypeParser;
import com.microsoft.azure.sdk.iot.deps.serializer.ExportImportDeviceParser;
import com.microsoft.azure.sdk.iot.deps.serializer.SymmetricKeyParser;
import com.microsoft.azure.sdk.iot.deps.serializer.X509ThumbprintParser;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.ModuleClient;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.Module;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.RegistryManagerOptions;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.auth.SymmetricKey;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubExceptionManager;
import com.microsoft.azure.sdk.iot.service.transport.TransportUtils;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

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

    private static Queue<TestDeviceIdentity> testSasDeviceQueue = new ConcurrentLinkedQueue<>();
    private static Queue<TestDeviceIdentity> testX509DeviceQueue = new ConcurrentLinkedQueue<>();
    private final static Object testSasDeviceQueueLock = new Object();
    private final static Object testX509DeviceQueueLock = new Object();
    private static Queue<TestDeviceIdentity> testSasDeviceWithTwinQueue = new ConcurrentLinkedQueue<>();
    private static Queue<TestDeviceIdentity> testX509DeviceWithTwinQueue = new ConcurrentLinkedQueue<>();

    private static Queue<TestModuleIdentity> testSasModuleQueue = new ConcurrentLinkedQueue<>();
    private static Queue<TestModuleIdentity> testX509ModuleQueue = new ConcurrentLinkedQueue<>();
    private final static Object testSasModuleQueueLock = new Object();
    private final static Object testX509ModuleQueueLock = new Object();
    private static Queue<TestModuleIdentity> testSasModuleWithTwinQueue = new ConcurrentLinkedQueue<>();
    private static Queue<TestModuleIdentity> testX509ModuleWithTwinQueue = new ConcurrentLinkedQueue<>();

    // number of devices to add in bulk when proactively adding devices to the queue of available test devices
    private static final int PROACTIVE_TEST_DEVICE_REGISRATION_COUNT = 100;

    /**
     * Return a device identity and client that can be used for a test. If a recycled identity is available to use, this method will prioritize using
     * that instead of creating a new identity. When creating a new identity, this method will create a batch of identities. One from the batch will
     * be returned, and the rest will be cached and available to the next caller of this method. This method will also prioritize
     * returning cached identities with twin changes over cached identities with no twin changes, but needCleanTwin can be used
     * to never return a cached identity with twin changes.
     *
     * For instance, a test method that just wants to send telemetry from a device identity should set needCleanTwin to false
     * so that it can use any available device identity. Conversely, a test method that involves setting reported or desired properties
     * should set needCleanTwin to true to avoid previous twin state interfering with the upcoming test.
     *
     * @param iotHubConnectionString The connection string for the IoT Hub where the identity will be registered to.
     * @param protocol The device side protocol for the client to use.
     * @param authenticationType The device side authentication type for the client to use.
     * @param needCleanTwin True if the returned device identity needs to not have any pre-existing desired or reported properties, false otherwise.
     * @return A {@link TestDeviceIdentity} that was either recycled from a previous test when possible, or was just created for this test.
     * @throws URISyntaxException If the connection string cannot be parsed.
     * @throws IOException If the registry addition of a device fails.
     * @throws IotHubException If the registry addition of a device fails.
     * @throws GeneralSecurityException If creating the x509 certificates for an x509 device fails.
     */
    public static TestDeviceIdentity getTestDevice(String iotHubConnectionString, IotHubClientProtocol protocol, AuthenticationType authenticationType, boolean needCleanTwin) throws URISyntaxException, IOException, IotHubException, GeneralSecurityException
    {
        if (authenticationType == AuthenticationType.SAS)
        {
            synchronized (testSasDeviceQueueLock)
            {
                RegistryManager registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);

                TestDeviceIdentity testDeviceIdentity;
                if (!needCleanTwin && testSasDeviceWithTwinQueue.size() > 0)
                {
                    log.debug("Acquiring test device from testSasDeviceWithTwinQueue");
                    testDeviceIdentity = testSasDeviceWithTwinQueue.remove();
                }
                else if (testSasDeviceQueue.size() > 0)
                {
                    log.debug("Acquiring test device from testSasDeviceQueue");
                    testDeviceIdentity = testSasDeviceQueue.remove();
                }
                else
                {
                    // No cached devices to return, so create a new set of devices to cache, and return one of the newly created devices
                    log.debug("Proactively adding another {} devices to the SAS test device queue", PROACTIVE_TEST_DEVICE_REGISRATION_COUNT);
                    List<Device> devicesToAdd = new ArrayList<>();
                    for (int i = 0; i < PROACTIVE_TEST_DEVICE_REGISRATION_COUNT; i++)
                    {
                        Device deviceToAdd = Device.createDevice("test-device-" + UUID.randomUUID().toString(), AuthenticationType.SAS);
                        deviceToAdd.setSymmetricKey(new SymmetricKey());
                        devicesToAdd.add(deviceToAdd);
                    }

                    addDevices(devicesToAdd, iotHubConnectionString);

                    for (int i = 0; i < PROACTIVE_TEST_DEVICE_REGISRATION_COUNT; i++)
                    {
                        testSasDeviceQueue.add(new TestDeviceIdentity(null, devicesToAdd.get(i)));
                    }

                    testDeviceIdentity = testSasDeviceQueue.remove();
                }

                if (needCleanTwin)
                {
                    // a bit proactive to say its twin is updated, but a test that requires a clean twin likely will update the twin.
                    // This means each test isn't responsible for setting this value for themselves which would be a hassle
                    testDeviceIdentity.twinUpdated = true;
                }

                testDeviceIdentity.setDeviceClient(new DeviceClient(registryManager.getDeviceConnectionString(testDeviceIdentity.getDevice()), protocol));
                return testDeviceIdentity;
            }
        }
        else
        {
            synchronized (testX509DeviceQueueLock)
            {
                RegistryManager registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
                TestDeviceIdentity testDeviceIdentity;
                if (!needCleanTwin && testX509DeviceWithTwinQueue.size() > 0)
                {
                    log.debug("Acquiring test device from testX509DeviceWithTwinQueue");
                    testDeviceIdentity = testX509DeviceWithTwinQueue.remove();
                }
                else if (testX509DeviceQueue.size() > 0)
                {
                    log.debug("Acquiring test device from testX509DeviceQueue");
                    testDeviceIdentity = testX509DeviceQueue.remove();
                }
                else
                {
                    // No cached devices to return, so create a new set of devices to cache, and return one of the newly created devices
                    log.debug("Proactively adding another {} devices to the X509 test device queue", PROACTIVE_TEST_DEVICE_REGISRATION_COUNT);
                    List<Device> devicesToAdd = new ArrayList<>();
                    for (int i = 0; i < PROACTIVE_TEST_DEVICE_REGISRATION_COUNT; i++)
                    {
                        Device deviceToAdd = Device.createDevice("test-device-" + UUID.randomUUID().toString(), AuthenticationType.SELF_SIGNED);
                        String x509Thumbprint = IntegrationTest.x509CertificateGenerator.getX509Thumbprint();
                        deviceToAdd.setThumbprintFinal(x509Thumbprint, x509Thumbprint);
                        devicesToAdd.add(deviceToAdd);
                    }

                    addDevices(devicesToAdd, iotHubConnectionString);
                    for (int i = 0; i < PROACTIVE_TEST_DEVICE_REGISRATION_COUNT; i++)
                    {
                        testX509DeviceQueue.add(new TestDeviceIdentity(null, devicesToAdd.get(i)));
                    }

                    testDeviceIdentity = testX509DeviceQueue.remove();
                }

                if (needCleanTwin)
                {
                    // a bit proactive to say its twin is updated, but a test that requires a clean twin likely will update the twin.
                    // This means each test isn't responsible for setting this value for themselves which would be a hassle
                    testDeviceIdentity.twinUpdated = true;
                }

                SSLContext sslContext = SSLContextBuilder.buildSSLContext(IntegrationTest.x509CertificateGenerator.getPublicCertificate(), IntegrationTest.x509CertificateGenerator.getPrivateKey());
                DeviceClient client = new DeviceClient(registryManager.getDeviceConnectionString(testDeviceIdentity.getDevice()), protocol, sslContext);
                testDeviceIdentity.setDeviceClient(client);
                return testDeviceIdentity;
            }
        }
    }

    /**
     * Return a module identity and client that can be used for a test. If a recycled identity is available to use, this method will prioritize using
     * that instead of creating a new identity. When creating a new identity, this method will not create a batch of identities
     * since there is no bulk add service API for just modules. This method will also prioritize
     * returning cached identities with twin changes over cached identities with no twin changes, but needCleanTwin can be used
     * to never return a cached identity with twin changes.
     *
     * For instance, a test method that just wants to send telemetry from a module identity should set needCleanTwin to false
     * so that it can use any available module. Conversely, a test method that involves setting reported or desired properties
     * should set needCleanTwin to true to avoid previous twin state interfering with the upcoming test.
     *
     * @param iotHubConnectionString The connection string for the IoT Hub where the identity will be registered to.
     * @param protocol The device side protocol for the client to use.
     * @param authenticationType The device side authentication type for the client to use.
     * @param needCleanTwin True if the returned module identity needs to not have any pre-existing desired or reported properties, false otherwise.
     * @return A {@link TestDeviceIdentity} that was either recycled from a previous test when possible, or was just created for this test.
     * @throws URISyntaxException If the connection string cannot be parsed.
     * @throws IOException If the registry addition of a module fails.
     * @throws IotHubException If the registry addition of a module fails.
     * @throws GeneralSecurityException If creating the x509 certificates for an x509 module fails.
     */
    public static TestModuleIdentity getTestModule(String iotHubConnectionString, IotHubClientProtocol protocol, AuthenticationType authenticationType, boolean needCleanTwin) throws URISyntaxException, IOException, IotHubException, InterruptedException, ModuleClientException, GeneralSecurityException
    {
        RegistryManager registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);

        if (authenticationType == AuthenticationType.SAS)
        {
            synchronized (testSasModuleQueueLock)
            {
                TestModuleIdentity testModuleIdentity;
                if (!needCleanTwin && testSasModuleWithTwinQueue.size() > 0)
                {
                    log.debug("Acquiring test module from testSasModuleWithTwinQueue");
                    testModuleIdentity = testSasModuleWithTwinQueue.remove();
                }
                else if (testSasModuleQueue.size() > 0)
                {
                    log.debug("Acquiring test module from testSasModuleQueue");
                    testModuleIdentity = testSasModuleQueue.remove();
                }
                else
                {
                    log.debug("Acquiring test module by creating new one");
                    TestDeviceIdentity testDeviceIdentity = getTestDevice(iotHubConnectionString, protocol, authenticationType, needCleanTwin);
                    Module module = Module.createModule(testDeviceIdentity.device.getDeviceId(), "test-module-" + UUID.randomUUID(), authenticationType);
                    module = addModuleWithRetry(registryManager, module);
                    testModuleIdentity = new TestModuleIdentity(null, testDeviceIdentity.device, module);
                }

                ModuleClient moduleClient = new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, testModuleIdentity.device, testModuleIdentity.module), protocol);
                testModuleIdentity.setModuleClient(moduleClient);
                return testModuleIdentity;
            }
        }
        else
        {
            synchronized (testX509ModuleQueueLock)
            {
                TestModuleIdentity testModuleIdentity;
                if (!needCleanTwin && testX509ModuleWithTwinQueue.size() > 0)
                {
                    log.debug("Acquiring test module from testX509ModuleWithTwinQueue");
                    testModuleIdentity = testX509ModuleWithTwinQueue.remove();
                }
                else if (testX509ModuleQueue.size() > 0)
                {
                    log.debug("Acquiring test module from testX509ModuleQueue");
                    testModuleIdentity = testX509ModuleQueue.remove();
                }
                else
                {
                    log.debug("Acquiring test module by creating new one");
                    TestDeviceIdentity testDeviceIdentity = getTestDevice(iotHubConnectionString, protocol, authenticationType, needCleanTwin);
                    Module module = Module.createModule(testDeviceIdentity.device.getDeviceId(), "test-module-" + UUID.randomUUID(), authenticationType);
                    String x509Thumbprint = IntegrationTest.x509CertificateGenerator.getX509Thumbprint();
                    module.setThumbprintFinal(x509Thumbprint, x509Thumbprint);
                    module = addModuleWithRetry(registryManager, module);
                    testModuleIdentity = new TestModuleIdentity(null, testDeviceIdentity.device, module);
                }

                SSLContext sslContext = SSLContextBuilder.buildSSLContext(IntegrationTest.x509CertificateGenerator.getPublicCertificate(), IntegrationTest.x509CertificateGenerator.getPrivateKey());
                ModuleClient moduleClient = new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, testModuleIdentity.device, testModuleIdentity.module), protocol, sslContext);
                testModuleIdentity.setModuleClient(moduleClient);
                return testModuleIdentity;
            }
        }
    }

    /**
     * If the environment variable RECYCLE_TEST_IDENTITIES is set to true, then the identity will be recycled. Otherwise it
     * will be removed from the IoT Hub's registry.
     * @param testIdentity The test identity to dispose/recycle.
     */
    public static void disposeTestIdentity(TestIdentity testIdentity, String iotHubConnectionString)
    {
        if (!IntegrationTest.recycleIdentities)
        {
            try
            {
                if (testIdentity != null && testIdentity.getDeviceId() != null && !testIdentity.getDeviceId().isEmpty())
                {
                    log.debug("Removing device {} from IoT Hub registry...", testIdentity.getDeviceId());
                    getRegistyManager(iotHubConnectionString).removeDevice(testIdentity.getDeviceId());
                    log.debug("Successfully removed device {} from IoT Hub registry.", testIdentity.getDeviceId());
                }
            }
            catch (IOException | IotHubException e)
            {
                log.error("Failed to clean up device identity {}", testIdentity.getDeviceId(), e);
            }
        }
        else
        {
            if (testIdentity instanceof TestDeviceIdentity)
            {
                if (testIdentity.getDevice().getAuthenticationType() == AuthenticationType.SAS)
                {
                    if (testIdentity.twinUpdated)
                    {
                        log.debug("Requeueing device {} into testSasDeviceWithTwinQueue", testIdentity.getDeviceId());
                        testSasDeviceWithTwinQueue.add((TestDeviceIdentity) testIdentity);
                    }
                    else
                    {
                        log.debug("Requeueing device {} into testSasDeviceQueue", testIdentity.getDeviceId());
                        testSasDeviceQueue.add((TestDeviceIdentity) testIdentity);
                    }
                }
                else
                {
                    if (testIdentity.twinUpdated)
                    {
                        log.debug("Requeueing device {} into testX509DeviceWithTwinQueue", testIdentity.getDeviceId());
                        testX509DeviceWithTwinQueue.add((TestDeviceIdentity) testIdentity);
                    }
                    else
                    {
                        log.debug("Requeueing device {} into testX509DeviceQueue", testIdentity.getDeviceId());
                        testX509DeviceQueue.add((TestDeviceIdentity) testIdentity);
                    }
                }
            }
            else if (testIdentity instanceof TestModuleIdentity)
            {
                if (((TestModuleIdentity) testIdentity).getModule().getAuthenticationType() == AuthenticationType.SAS)
                {
                    if (testIdentity.twinUpdated)
                    {
                        log.debug("Requeueing module {} into testSasModuleWithTwinQueue", ((TestModuleIdentity) testIdentity).getModuleId());
                        testSasModuleWithTwinQueue.add((TestModuleIdentity) testIdentity);
                    }
                    else
                    {
                        log.debug("Requeueing module {} into testSasModuleQueue", ((TestModuleIdentity) testIdentity).getModuleId());
                        testSasModuleQueue.add((TestModuleIdentity) testIdentity);
                    }
                }
                else
                {
                    if (testIdentity.twinUpdated)
                    {
                        log.debug("Requeueing module {} into testX509ModuleWithTwinQueue", ((TestModuleIdentity) testIdentity).getModuleId());
                        testX509ModuleWithTwinQueue.add((TestModuleIdentity) testIdentity);
                    }
                    else
                    {
                        log.debug("Requeueing module {} into testX509ModuleQueue", ((TestModuleIdentity) testIdentity).getModuleId());
                        testX509ModuleQueue.add((TestModuleIdentity) testIdentity);
                    }
                }
            }
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

    // This call mimics what should be a registry manager API for adding devices in bulk. Can be removed once we add support in our
    // registry manager for this
    private static final String IMPORT_MODE_CREATE = "create";
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
            if (device.getAuthenticationType() == AuthenticationType.SAS)
            {
                authenticationParser.setType(AuthenticationTypeParser.SAS);
                authenticationParser.setSymmetricKey(new SymmetricKeyParser(device.getSymmetricKey().getPrimaryKey(), device.getSymmetricKey().getSecondaryKey()));
            }
            else
            {
                authenticationParser.setType(AuthenticationTypeParser.SELF_SIGNED);
                authenticationParser.setThumbprint(new X509ThumbprintParser(device.getPrimaryThumbprint(), device.getSecondaryThumbprint()));
            }

            exportImportDevice.setAuthentication(authenticationParser);
            exportImportDevice.setImportMode(IMPORT_MODE_CREATE);
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