/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClient;
import com.microsoft.azure.sdk.iot.service.registry.RegistryStatistics;
import com.microsoft.azure.sdk.iot.service.registry.serializers.AuthenticationParser;
import com.microsoft.azure.sdk.iot.service.registry.serializers.AuthenticationTypeParser;
import com.microsoft.azure.sdk.iot.service.registry.serializers.ExportImportDeviceParser;
import com.microsoft.azure.sdk.iot.service.registry.serializers.SymmetricKeyParser;
import com.microsoft.azure.sdk.iot.service.registry.serializers.X509ThumbprintParser;
import com.microsoft.azure.sdk.iot.device.ClientOptions;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.ModuleClient;
import com.microsoft.azure.sdk.iot.service.registry.Device;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.registry.Module;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClientOptions;
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
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest.*;

@Slf4j
public class Tools
{
    private static final long RETRY_TIMEOUT_ON_NETWORK_FAILURE_MILLISECONDS = 60 * 1000;
    private static final long WAIT_FOR_RETRY = 2000;
    private static boolean IS_ANDROID = false;

    private static final String ANDROID_BUILD_CONFIG_CLASS = "com.iothub.azure.microsoft.com.androide2e.test.BuildConfig";
    private static final Map<String, String> ANDROID_ENV_VAR = retrieveAndroidEnvVariables();

    public static String retrieveEnvironmentVariableValue(String environmentVariableName)
    {
        String environmentVariableValue;

        if (ANDROID_ENV_VAR.containsKey(environmentVariableName))
        {
            IS_ANDROID = true;
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
            IS_ANDROID = true;
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
                    IS_ANDROID = true;
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
            IS_ANDROID = false;
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

    private static RegistryClient registryClient;

    public static RegistryClient getRegistyManager(String iotHubConnectionString) throws IOException
    {
        if (registryClient == null)
        {
            RegistryClientOptions options = RegistryClientOptions.builder().httpReadTimeoutSeconds(HTTP_READ_TIMEOUT).build();
            registryClient = new RegistryClient(iotHubConnectionString, options);
        }

        return registryClient;
    }

    private static Queue<TestDeviceIdentity> testSasDeviceQueue = new ConcurrentLinkedQueue<>();  // queue contains SAS device identities that are ready to be re-used, and that have no twin modifications yet
    private static Queue<TestDeviceIdentity> testX509DeviceQueue = new ConcurrentLinkedQueue<>(); // queue contains x509 device identities that are ready to be re-used, and that have no twin modifications yet
    private static Queue<TestDeviceIdentity> testSasDeviceWithTwinQueue = new ConcurrentLinkedQueue<>(); // queue contains SAS device identities that are ready to be re-used, and that have some twin modifications to them already
    private static Queue<TestDeviceIdentity> testX509DeviceWithTwinQueue = new ConcurrentLinkedQueue<>(); // queue contains x509 device identities that are ready to be re-used, and that have some twin modifications to them already
    private final static Object testSasDeviceQueueLock = new Object();
    private final static Object testX509DeviceQueueLock = new Object();

    private static Queue<TestModuleIdentity> testSasModuleQueue = new ConcurrentLinkedQueue<>(); // queue contains SAS module identities that are ready to be re-used, and that have no twin modifications yet
    private static Queue<TestModuleIdentity> testX509ModuleQueue = new ConcurrentLinkedQueue<>(); // queue contains x509 module identities that are ready to be re-used, and that have no twin modifications yet
    private static Queue<TestModuleIdentity> testSasModuleWithTwinQueue = new ConcurrentLinkedQueue<>(); // queue contains SAS module identities that are ready to be re-used, and that have some twin modifications to them already
    private static Queue<TestModuleIdentity> testX509ModuleWithTwinQueue = new ConcurrentLinkedQueue<>();  // queue contains x509 module identities that are ready to be re-used, and that have some twin modifications to them already
    private final static Object testSasModuleQueueLock = new Object();
    private final static Object testX509ModuleQueueLock = new Object();

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
     * @param optionsBuilder The options that this client will use.
     * @return A {@link TestDeviceIdentity} that was either recycled from a previous test when possible, or was just created for this test.
     * @throws URISyntaxException If the connection string cannot be parsed.
     * @throws IOException If the registry addition of a device fails.
     * @throws IotHubException If the registry addition of a device fails.
     * @throws GeneralSecurityException If creating the x509 certificates for an x509 device fails.
     */
    public static TestDeviceIdentity getTestDevice(String iotHubConnectionString, IotHubClientProtocol protocol, AuthenticationType authenticationType, boolean needCleanTwin, ClientOptions.ClientOptionsBuilder optionsBuilder) throws URISyntaxException, IOException, IotHubException, GeneralSecurityException
    {
        TestDeviceIdentity testDeviceIdentity;
        if (authenticationType == AuthenticationType.SAS)
        {
            testDeviceIdentity = getSasTestDevice(iotHubConnectionString, protocol, needCleanTwin, optionsBuilder);
        }
        else
        {
            testDeviceIdentity = getX509TestDevice(iotHubConnectionString, protocol, needCleanTwin, optionsBuilder);
        }

        if (needCleanTwin)
        {
            // a bit proactive to say its twin is updated, but a test that requires a clean twin likely will update the twin.
            // This means each test isn't responsible for setting this value for themselves which would be a hassle
            testDeviceIdentity.twinUpdated = true;
        }

        return testDeviceIdentity;
    }

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
        return getTestDevice(iotHubConnectionString, protocol, authenticationType, needCleanTwin, ClientOptions.builder());
    }

    private static TestDeviceIdentity getSasTestDevice(String iotHubConnectionString, IotHubClientProtocol protocol, boolean needCleanTwin, ClientOptions.ClientOptionsBuilder optionsBuilder) throws URISyntaxException, IOException, IotHubException, GeneralSecurityException
    {
        // Don't want multiple methods calling this simultaneously and each thinking that they need to create
        // 100 devices. Forcing them to enter this block one at a time means that the first caller creates the 100 devices,
        // and the subsequent callers just uses one of those devices.
        synchronized (testSasDeviceQueueLock)
        {
            TestDeviceIdentity testDeviceIdentity;
            if (!needCleanTwin && testSasDeviceWithTwinQueue.size() > 0)
            {
                log.debug("Acquiring test device from testSasDeviceWithTwinQueue");
                testDeviceIdentity = testSasDeviceWithTwinQueue.remove();
            }
            else
            {
                if (testSasDeviceQueue.size() < 1)
                {
                    // No cached devices to return, so create a new set of devices to cache, and return one of the newly created devices
                    log.debug("Proactively adding another {} devices to the SAS test device queue", PROACTIVE_TEST_DEVICE_REGISRATION_COUNT);
                    List<Device> devicesToAdd = new ArrayList<>();
                    for (int i = 0; i < PROACTIVE_TEST_DEVICE_REGISRATION_COUNT; i++)
                    {
                        Device deviceToAdd = new Device("test-device-" + UUID.randomUUID().toString(), AuthenticationType.SAS);
                        deviceToAdd.setSymmetricKey(new SymmetricKey());
                        devicesToAdd.add(deviceToAdd);
                    }

                    addDevices(devicesToAdd, iotHubConnectionString);

                    for (int i = 0; i < PROACTIVE_TEST_DEVICE_REGISRATION_COUNT; i++)
                    {
                        testSasDeviceQueue.add(new TestDeviceIdentity(null, devicesToAdd.get(i)));
                    }
                }
                log.debug("Acquiring test device from testSasDeviceQueue");
                testDeviceIdentity = testSasDeviceQueue.remove();
            }

            optionsBuilder
                .amqpAuthenticationSessionTimeout(AMQP_AUTHENTICATION_SESSION_TIMEOUT_SECONDS)
                .amqpDeviceSessionTimeout(AMQP_DEVICE_SESSION_TIMEOUT_SECONDS);

            if (protocol == IotHubClientProtocol.HTTPS)
            {
                // By default, the SDK sends an HTTP request checking for c2d messages once every 10 milliseconds. This is
                // more aggressive than it needs to be for test purposes and likely causes unnecessary work from the service
                // in handling these requests. This option increases this period to lessen that burden without sacrificing
                // the test's speed by more than 1 second.
                //
                // This option isn't applicable to MQTT/MQTT_WS/AMQPS/AMQPS_WS since they don't need to poll the service
                // in order to receive c2d messages.
                optionsBuilder.receiveInterval(HTTP_RECEIVE_PERIOD);
            }

            ClientOptions clientOptions = optionsBuilder.build();

            testDeviceIdentity.setDeviceClient(new DeviceClient(getDeviceConnectionString(iotHubConnectionString, testDeviceIdentity.getDevice()), protocol, clientOptions));
            return testDeviceIdentity;
        }
    }

    private static TestDeviceIdentity getX509TestDevice(String iotHubConnectionString, IotHubClientProtocol protocol, boolean needCleanTwin, ClientOptions.ClientOptionsBuilder optionsBuilder) throws URISyntaxException, IOException, IotHubException, GeneralSecurityException
    {
        // Don't want multiple methods calling this simultaneously and each thinking that they need to create
        // 100 devices. Forcing them to enter this block one at a time means that the first caller creates the 100 devices,
        // and the subsequent callers just uses one of those devices.
        synchronized (testX509DeviceQueueLock)
        {
            TestDeviceIdentity testDeviceIdentity;
            if (!needCleanTwin && testX509DeviceWithTwinQueue.size() > 0)
            {
                log.debug("Acquiring test device from testX509DeviceWithTwinQueue");
                testDeviceIdentity = testX509DeviceWithTwinQueue.remove();
            }
            else
            {
                if (testX509DeviceQueue.size() < 1)
                {
                    // No cached devices to return, so create a new set of devices to cache, and return one of the newly created devices
                    log.debug("Proactively adding another {} devices to the X509 test device queue", PROACTIVE_TEST_DEVICE_REGISRATION_COUNT);
                    List<Device> devicesToAdd = new ArrayList<>();
                    for (int i = 0; i < PROACTIVE_TEST_DEVICE_REGISRATION_COUNT; i++)
                    {
                        Device deviceToAdd = new Device("test-device-" + UUID.randomUUID().toString(), AuthenticationType.SELF_SIGNED);
                        String x509Thumbprint = IntegrationTest.x509CertificateGenerator.getX509Thumbprint();
                        deviceToAdd.setThumbprint(x509Thumbprint, x509Thumbprint);
                        devicesToAdd.add(deviceToAdd);
                    }

                    addDevices(devicesToAdd, iotHubConnectionString);
                    for (int i = 0; i < PROACTIVE_TEST_DEVICE_REGISRATION_COUNT; i++)
                    {
                        testX509DeviceQueue.add(new TestDeviceIdentity(null, devicesToAdd.get(i)));
                    }
                }

                log.debug("Acquiring test device from testX509DeviceQueue");
                testDeviceIdentity = testX509DeviceQueue.remove();
            }

            SSLContext sslContext = SSLContextBuilder.buildSSLContext(IntegrationTest.x509CertificateGenerator.getX509Certificate(), IntegrationTest.x509CertificateGenerator.getPrivateKey());
            ClientOptions clientOptions = optionsBuilder
                .sslContext(sslContext)
                .amqpAuthenticationSessionTimeout(AMQP_AUTHENTICATION_SESSION_TIMEOUT_SECONDS)
                .amqpDeviceSessionTimeout(AMQP_DEVICE_SESSION_TIMEOUT_SECONDS)
                .build();
            DeviceClient client = new DeviceClient(getDeviceConnectionString(iotHubConnectionString, testDeviceIdentity.getDevice()), protocol, clientOptions);
            testDeviceIdentity.setDeviceClient(client);
            return testDeviceIdentity;
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
     * @param optionsBuilder The options that the client will use.
     * @return A {@link TestDeviceIdentity} that was either recycled from a previous test when possible, or was just created for this test.
     * @throws URISyntaxException If the connection string cannot be parsed.
     * @throws IOException If the registry addition of a module fails.
     * @throws IotHubException If the registry addition of a module fails.
     * @throws GeneralSecurityException If creating the x509 certificates for an x509 module fails.
     */
    public static TestModuleIdentity getTestModule(String iotHubConnectionString, IotHubClientProtocol protocol, AuthenticationType authenticationType, boolean needCleanTwin, ClientOptions.ClientOptionsBuilder optionsBuilder) throws URISyntaxException, IOException, IotHubException, InterruptedException, GeneralSecurityException
    {
        TestModuleIdentity testModuleIdentity;
        if (authenticationType == AuthenticationType.SAS)
        {
            testModuleIdentity = getSasTestModule(iotHubConnectionString, protocol, needCleanTwin, optionsBuilder);
        }
        else
        {
            testModuleIdentity = getX509TestModule(iotHubConnectionString, protocol, needCleanTwin, optionsBuilder);
        }

        if (needCleanTwin)
        {
            // a bit proactive to say its twin is updated, but a test that requires a clean twin likely will update the twin.
            // This means each test isn't responsible for setting this value for themselves which would be a hassle
            testModuleIdentity.twinUpdated = true;
        }

        return testModuleIdentity;
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
    public static TestModuleIdentity getTestModule(String iotHubConnectionString, IotHubClientProtocol protocol, AuthenticationType authenticationType, boolean needCleanTwin) throws URISyntaxException, IOException, IotHubException, InterruptedException, GeneralSecurityException
    {
        return getTestModule(iotHubConnectionString, protocol, authenticationType, needCleanTwin, ClientOptions.builder());
    }

    private static TestModuleIdentity getSasTestModule(String iotHubConnectionString, IotHubClientProtocol protocol, boolean needCleanTwin, ClientOptions.ClientOptionsBuilder optionsBuilder) throws URISyntaxException, IOException, IotHubException, InterruptedException, GeneralSecurityException
    {
        // Want to make sure that no thread checks the size of a queue and then has the size change before it can
        // remove an identity from the queue.
        synchronized (testSasModuleQueueLock)
        {
            TestModuleIdentity testModuleIdentity;
            if (!needCleanTwin && testSasModuleWithTwinQueue.size() > 0)
            {
                log.debug("Acquiring test module from testSasModuleWithTwinQueue");
                testModuleIdentity = testSasModuleWithTwinQueue.remove();
            }
            else
            {
                if (testSasModuleQueue.size() < 1)
                {
                    // No cached modules to return, so create a new set of modules to cache, and return one of the newly created modules
                    log.debug("Proactively adding another {} modules to the SAS test module queue", PROACTIVE_TEST_DEVICE_REGISRATION_COUNT);
                    List<Device> devices = new ArrayList<>();
                    List<Module> modulesToAdd = new ArrayList<>();
                    for (int i = 0; i < PROACTIVE_TEST_DEVICE_REGISRATION_COUNT; i++)
                    {
                        TestDeviceIdentity testDeviceIdentity = getTestDevice(iotHubConnectionString, protocol, AuthenticationType.SAS, needCleanTwin);
                        devices.add(testDeviceIdentity.device);
                        modulesToAdd.add(new Module(testDeviceIdentity.device.getDeviceId(), "test-module-" + UUID.randomUUID(), AuthenticationType.SAS));
                    }

                    addModules(modulesToAdd, iotHubConnectionString);

                    for (int i = 0; i < PROACTIVE_TEST_DEVICE_REGISRATION_COUNT; i++)
                    {
                        testSasModuleQueue.add(new TestModuleIdentity(null, devices.get(i), modulesToAdd.get(i)));
                    }
                }

                log.debug("Acquiring test module from testSasModuleQueue");
                testModuleIdentity = testSasModuleQueue.remove();
            }

            ClientOptions clientOptions = optionsBuilder
                .amqpAuthenticationSessionTimeout(AMQP_AUTHENTICATION_SESSION_TIMEOUT_SECONDS)
                .amqpDeviceSessionTimeout(AMQP_DEVICE_SESSION_TIMEOUT_SECONDS)
                .build();
            ModuleClient moduleClient = new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, testModuleIdentity.device, testModuleIdentity.module), protocol, clientOptions);
            testModuleIdentity.setModuleClient(moduleClient);
            return testModuleIdentity;
        }
    }

    private static TestModuleIdentity getX509TestModule(String iotHubConnectionString, IotHubClientProtocol protocol, boolean needCleanTwin, ClientOptions.ClientOptionsBuilder optionsBuilder) throws URISyntaxException, IOException, IotHubException, InterruptedException, GeneralSecurityException
    {
        // Want to make sure that no thread checks the size of a queue and then has the size change before it can
        // remove an identity from the queue.
        synchronized (testX509ModuleQueueLock)
        {
            TestModuleIdentity testModuleIdentity;
            if (!needCleanTwin && testX509ModuleWithTwinQueue.size() > 0)
            {
                log.debug("Acquiring test module from testX509ModuleWithTwinQueue");
                testModuleIdentity = testX509ModuleWithTwinQueue.remove();
            }
            else
            {
                if (testX509ModuleQueue.size() < 1)
                {
                    // No cached modules to return, so create a new set of modules to cache, and return one of the newly created modules
                    log.debug("Proactively adding another {} modules to the SAS test module queue", PROACTIVE_TEST_DEVICE_REGISRATION_COUNT);
                    List<Device> devices = new ArrayList<>();
                    List<Module> modulesToAdd = new ArrayList<>();
                    for (int i = 0; i < PROACTIVE_TEST_DEVICE_REGISRATION_COUNT; i++)
                    {
                        TestDeviceIdentity testDeviceIdentity = getTestDevice(iotHubConnectionString, protocol, AuthenticationType.SELF_SIGNED, needCleanTwin);
                        devices.add(testDeviceIdentity.device);
                        Module module = new Module(testDeviceIdentity.device.getDeviceId(), "test-module-" + UUID.randomUUID(), AuthenticationType.SELF_SIGNED);
                        String x509Thumbprint = IntegrationTest.x509CertificateGenerator.getX509Thumbprint();
                        module.setThumbprint(x509Thumbprint, x509Thumbprint);
                        modulesToAdd.add(module);
                    }

                    addModules(modulesToAdd, iotHubConnectionString);

                    for (int i = 0; i < PROACTIVE_TEST_DEVICE_REGISRATION_COUNT; i++)
                    {
                        testX509ModuleQueue.add(new TestModuleIdentity(null, devices.get(i), modulesToAdd.get(i)));
                    }
                }

                log.debug("Acquiring test module from testX509ModuleQueue");
                testModuleIdentity = testX509ModuleQueue.remove();
            }

            SSLContext sslContext = SSLContextBuilder.buildSSLContext(IntegrationTest.x509CertificateGenerator.getX509Certificate(), IntegrationTest.x509CertificateGenerator.getPrivateKey());
            ClientOptions clientOptions = optionsBuilder
                .sslContext(sslContext)
                .amqpAuthenticationSessionTimeout(AMQP_AUTHENTICATION_SESSION_TIMEOUT_SECONDS)
                .amqpDeviceSessionTimeout(AMQP_DEVICE_SESSION_TIMEOUT_SECONDS)
                .build();

            ModuleClient moduleClient = new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, testModuleIdentity.device, testModuleIdentity.module), protocol, clientOptions);
            testModuleIdentity.setModuleClient(moduleClient);
            return testModuleIdentity;
        }
    }

    /**
     * If the environment variable RECYCLE_TEST_IDENTITIES is set to true, then the identities will be recycled. Otherwise they
     * will be removed from the IoT Hub's registry.
     * @param testIdentities The test identities to dispose/recycle.
     */
    public static void disposeTestIdentities(Iterable<? extends TestIdentity> testIdentities, String iotHubConnectionString)
    {
        if (IntegrationTest.recycleIdentities)
        {
            // When recycling identities, there is no need to do anything in bulk. Just call the overload of this function
            // that takes a single identity.
            for (TestIdentity testIdentity : testIdentities)
            {
                disposeTestIdentity(testIdentity, iotHubConnectionString);
            }
        }
        else
        {
            // when not recycling identities, it is faster to delete them in bulk rather than one at a time.
            List<Device> devicesToDelete = new ArrayList<>();
            for (TestIdentity testIdentity : testIdentities)
            {
                devicesToDelete.add(testIdentity.getDevice());
            }

            // No need to dispose of modules in particular.
            // Deleting a device that has a module will delete both the device and module.
            disposeTestDevices(devicesToDelete, iotHubConnectionString);
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

    private static void disposeTestDevices(Iterable<Device> devices, String iotHubConnectionString)
    {
        List<Device> subList = new ArrayList<>();
        for (Device device : devices)
        {
            // Bulk device removal is limited to, at most, 100 devices per request. Create a batch of 100 devices,
            // delete those 100 devices, and then create another batch.
            subList.add(device);

            if (subList.size() > 99)
            {
                try
                {
                    removeDevices(subList, iotHubConnectionString);
                }
                catch (IOException | IotHubException e)
                {
                    log.error("Failed to delete a batch of devices from the registry", e);
                }

                subList.clear();
            }
        }

        if (subList.size() > 0)
        {
            try
            {
                removeDevices(subList, iotHubConnectionString);
            }
            catch (IOException | IotHubException e)
            {
                log.error("Failed to delete a batch of devices from the registry", e);
            }
        }
    }

    public static Device addDeviceWithRetry(RegistryClient registryClient, Device device) throws IotHubException, IOException, InterruptedException
    {
        long startTime = System.currentTimeMillis();
        Device ret = null;
        while (System.currentTimeMillis() - startTime < RETRY_TIMEOUT_ON_NETWORK_FAILURE_MILLISECONDS)
        {
            try
            {
                log.debug("Attempting to add device {} to registry", device.getDeviceId());
                ret = registryClient.addDevice(device);
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
    private static void addDevices(Iterable<Device> devices, String connectionString) throws IOException, IotHubException {
        if (devices == null)
        {
            throw new IllegalArgumentException("devices cannot be null");
        }

        IotHubConnectionString iotHubConnectionString = IotHubConnectionString.createIotHubConnectionString(connectionString);
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

        bulkRegistryOperation(body.toJson(), url, connectionString);
    }

    private static void addModules(Iterable<Module> modules, String connectionString) throws IOException, IotHubException {
        if (modules == null)
        {
            throw new IllegalArgumentException("modules cannot be null");
        }

        IotHubConnectionString iotHubConnectionString = IotHubConnectionString.createIotHubConnectionString(connectionString);
        URL url = getBulkDeviceAddUrl(iotHubConnectionString);

        List<ExportImportDeviceParser> parsers = new ArrayList<>();
        for (Module module : modules)
        {
            ExportImportDeviceParser exportImportDevice = new ExportImportDeviceParser();
            exportImportDevice.setId(module.getDeviceId());
            exportImportDevice.setModuleId(module.getId());
            AuthenticationParser authenticationParser = new AuthenticationParser();
            if (module.getAuthenticationType() == AuthenticationType.SAS)
            {
                authenticationParser.setType(AuthenticationTypeParser.SAS);
                authenticationParser.setSymmetricKey(new SymmetricKeyParser(module.getSymmetricKey().getPrimaryKey(), module.getSymmetricKey().getSecondaryKey()));
            }
            else
            {
                authenticationParser.setType(AuthenticationTypeParser.SELF_SIGNED);
                authenticationParser.setThumbprint(new X509ThumbprintParser(module.getPrimaryThumbprint(), module.getSecondaryThumbprint()));
            }

            exportImportDevice.setAuthentication(authenticationParser);
            exportImportDevice.setImportMode(IMPORT_MODE_CREATE);
            parsers.add(exportImportDevice);
        }

        ExportImportDevicesParser body = new ExportImportDevicesParser();
        body.setExportImportDevices(parsers);

        bulkRegistryOperation(body.toJson(), url, connectionString);
    }

    // This call mimics what should be a registry manager API for removing devices in bulk. Can be removed once we add support in our
    // registry manager for this
    private static final String IMPORT_MODE_DELETE = "delete";
    private static void removeDevices(Iterable<Device> devices, String connectionString) throws IOException, IotHubException {
        if (devices == null)
        {
            throw new IllegalArgumentException("devices cannot be null");
        }

        IotHubConnectionString iotHubConnectionString = IotHubConnectionString.createIotHubConnectionString(connectionString);
        URL url = getBulkDeviceAddUrl(iotHubConnectionString);

        List<ExportImportDeviceParser> parsers = new ArrayList<>();
        for (Device device : devices)
        {
            ExportImportDeviceParser exportImportDevice = new ExportImportDeviceParser();
            exportImportDevice.setId(device.getDeviceId());
            exportImportDevice.setImportMode(IMPORT_MODE_DELETE);
            parsers.add(exportImportDevice);
        }

        ExportImportDevicesParser body = new ExportImportDevicesParser();
        body.setExportImportDevices(parsers);

        bulkRegistryOperation(body.toJson(), url, connectionString);
    }

    private static void bulkRegistryOperation(String jsonPayload, URL url, String connectionString) throws IOException, IotHubException {
        if (jsonPayload == null)
        {
            throw new IllegalArgumentException("devices cannot be null");
        }

        IotHubConnectionString iotHubConnectionString = IotHubConnectionString.createIotHubConnectionString(connectionString);

        String sasTokenString = new IotHubServiceSasToken(iotHubConnectionString).toString();

        HttpRequest request = new HttpRequest(url, HttpMethod.POST, jsonPayload.getBytes(StandardCharsets.UTF_8), sasTokenString);
        request.setReadTimeoutSeconds(IntegrationTest.HTTP_READ_TIMEOUT);

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

    public static Module addModuleWithRetry(RegistryClient registryClient, Module module) throws IotHubException, IOException, InterruptedException
    {
        long startTime = System.currentTimeMillis();
        Module ret = null;
        while (System.currentTimeMillis() - startTime < RETRY_TIMEOUT_ON_NETWORK_FAILURE_MILLISECONDS)
        {
            try
            {
                log.debug("Attempting to add module {} to registry", module.getId());
                ret = registryClient.addModule(module);
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

    public static void getStatisticsWithRetry(RegistryClient registryClient) throws IotHubException, IOException, InterruptedException
    {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < RETRY_TIMEOUT_ON_NETWORK_FAILURE_MILLISECONDS)
        {
            try
            {
                RegistryStatistics statistics = registryClient.getStatistics();
                assertNotNull(statistics);
                assertTrue(statistics.getTotalDeviceCount() >= 0);
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

    public static Device getDeviceWithRetry(RegistryClient registryClient, String id) throws IotHubException, IOException, InterruptedException
    {
        long startTime = System.currentTimeMillis();
        Device ret = null;
        while (System.currentTimeMillis() - startTime < RETRY_TIMEOUT_ON_NETWORK_FAILURE_MILLISECONDS)
        {
            try
            {
                ret = registryClient.getDevice(id);
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

    public static Module getModuleWithRetry(RegistryClient registryClient, String deviceid, String moduleid) throws IotHubException, IOException, InterruptedException
    {
        long startTime = System.currentTimeMillis();
        Module ret = null;
        while (System.currentTimeMillis() - startTime < RETRY_TIMEOUT_ON_NETWORK_FAILURE_MILLISECONDS)
        {
            try
            {
                ret = registryClient.getModule(deviceid, moduleid);
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

    public static boolean isLinux()
    {
        return !isAndroid() && System.getProperty("os.name").toLowerCase().contains("linux");
    }

    public static boolean isAndroid()
    {
        return IS_ANDROID;
    }

    public static String getHostName(String iotHubConnectionString)
    {
        return IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString).getHostName();
    }

    public static String getDeviceConnectionString(String iothubConnectionString, Device device)
    {
        if (device == null)
        {
            throw new IllegalArgumentException("device cannot be null");
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("HostName=%s;", getHostName(iothubConnectionString)));
        stringBuilder.append(String.format("DeviceId=%s;", device.getDeviceId()));
        if (device.getPrimaryKey() == null)
        {
            //self signed or CA signed
            stringBuilder.append("x509=true");
        }
        else
        {
            stringBuilder.append(String.format("SharedAccessKey=%s", device.getPrimaryKey()));
        }
        return stringBuilder.toString();
    }
}