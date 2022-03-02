package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.exceptions.MultiplexingClientDeviceRegistrationAuthenticationException;
import com.microsoft.azure.sdk.iot.device.exceptions.MultiplexingClientException;
import com.microsoft.azure.sdk.iot.device.exceptions.MultiplexingDeviceUnauthorizedException;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.RetryPolicy;
import com.microsoft.azure.sdk.iot.device.transport.amqps.IoTHubConnectionType;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.microsoft.azure.sdk.iot.device.DeviceClientConfig.DEFAULT_KEEP_ALIVE_INTERVAL_IN_SECONDS;

/**
 * A client for creating multiplexed connections to IoT hub. A multiplexed connection allows for multiple device clients
 * to communicate to the service through a single AMQPS connection.
 * <p>
 * A given AMQPS connection requires a TLS connection, so multiplexing may be worthwhile if you want to limit the number
 * of TLS connections needed to connect multiple device clients to IoT hub.
 * <p>
 * A given multiplexing client also has a fixed amount of worker threads regardless of how many device clients are
 * being multiplexed. Comparatively, every non-multiplexed device client instance has its own set of worker
 * threads. Multiplexing may be worthwhile if you want fewer worker threads.
 * <p>
 * Only AMQPS and AMQPS_WS support multiplexing, and only symmetric key authenticated devices can be multiplexed.
 * <p>
 * {@link ModuleClient} instances cannot be multiplexed.
 */
@Slf4j
public class MultiplexingClient
{
    public static final long DEFAULT_SEND_PERIOD_MILLIS = 10L;
    public static final long DEFAULT_RECEIVE_PERIOD_MILLIS = 10L;
    public static final int DEFAULT_MAX_MESSAGES_TO_SEND_PER_THREAD = 10;
    static final long DEFAULT_REGISTRATION_TIMEOUT_MILLISECONDS = 60 * 1000; // 1 minute
    static final long DEFAULT_UNREGISTRATION_TIMEOUT_MILLISECONDS = 60 * 1000; // 1 minute
    private static final String OPEN_ERROR_MESSAGE = "Failed to open the multiplexing connection";

    // keys are deviceIds. Helps to optimize look ups later on which device Ids are already registered.
    private final Map<String, DeviceClient> multiplexedDeviceClients;
    private final DeviceIO deviceIO;
    private final String hostName;
    private final IotHubClientProtocol protocol;

    // This lock is used to keep open/close/register/unregister operations atomic to prevent race conditions
    private final Object operationLock = new Object();

    // Optional settings from MultiplexingClientOptions
    private final SSLContext sslContext;
    private final ProxySettings proxySettings;

    /**
     * The maximum number of devices that can be multiplexed together on a single multiplexed AMQPS connection
     */
    public static final int MAX_MULTIPLEX_DEVICE_COUNT_AMQPS = 1000;

    /**
     * The maximum number of devices that can be multiplexed together on a single multiplexed AMQPS_WS connection
     */
    public static final int MAX_MULTIPLEX_DEVICE_COUNT_AMQPS_WS = 500;

    /**
     * Instantiate a new MultiplexingClient that will establish a multiplexed connection through a proxy.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param protocol The transport protocol that this client will build the multiplexed connection on. Must be either
     *                 {@link IotHubClientProtocol#AMQPS} or {@link IotHubClientProtocol#AMQPS_WS}.
     */
    public MultiplexingClient(String hostName, IotHubClientProtocol protocol)
    {
        this(hostName, protocol, null);
    }

    /**
     * Instantiate a new MultiplexingClient that will establish a multiplexed connection through a proxy.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param protocol The transport protocol that this client will build the multiplexed connection on. Must be
     * {@link IotHubClientProtocol#AMQPS_WS} since using {@link IotHubClientProtocol#AMQPS} does not support proxies.
     * @param options The optional parameters to configure this client to use.
     */
    public MultiplexingClient(String hostName, IotHubClientProtocol protocol, MultiplexingClientOptions options)
    {
        Objects.requireNonNull(hostName);
        Objects.requireNonNull(protocol);

        switch (protocol)
        {
            case AMQPS:
            case AMQPS_WS:
                break;
            default:
                throw new IllegalArgumentException("Multiplexing is only supported for AMQPS and AMQPS_WS");
        }

        // Deliberately using HashMap instead of ConcurrentHashMap here. HashMap is faster for several operations such
        // .size() and we can control the adding/removing of elements to it with synchronization within this client.
        this.multiplexedDeviceClients = new HashMap<>();
        this.hostName = hostName;
        this.protocol = protocol;
        this.proxySettings = options != null ? options.getProxySettings() : null;
        long sendPeriod = options != null ? options.getSendPeriod() : DEFAULT_SEND_PERIOD_MILLIS;
        long receivePeriod = options != null ? options.getReceivePeriod() : DEFAULT_RECEIVE_PERIOD_MILLIS;
        int sendMessagesPerThread = options != null ? options.getMaxMessagesSentPerSendThread() : DEFAULT_MAX_MESSAGES_TO_SEND_PER_THREAD;
        int keepAliveInterval = options != null ? options.getKeepAliveInterval() : DEFAULT_KEEP_ALIVE_INTERVAL_IN_SECONDS;

        if (sendPeriod < 0)
        {
            throw new IllegalArgumentException("Send period cannot be negative");
        }
        else if (sendPeriod == 0) //default builder value for this option, signals that user didn't set a value
        {
            sendPeriod = DEFAULT_SEND_PERIOD_MILLIS;
        }

        if (receivePeriod < 0)
        {
            throw new IllegalArgumentException("Receive period cannot be negative");
        }
        else if (receivePeriod == 0) //default builder value for this option, signals that user didn't set a value
        {
            receivePeriod = DEFAULT_RECEIVE_PERIOD_MILLIS;
        }

        if (sendMessagesPerThread == 0) //default builder value for this option, signals that user didn't set a value
        {
            sendMessagesPerThread = DEFAULT_MAX_MESSAGES_TO_SEND_PER_THREAD;
        }

        this.sslContext = options != null ? options.getSslContext() : null;
        this.deviceIO = new DeviceIO(hostName, protocol, sslContext, proxySettings, sendPeriod, receivePeriod, keepAliveInterval);
        this.deviceIO.setMaxNumberOfMessagesSentPerSendThread(sendMessagesPerThread);
    }

    /**
     * Opens this multiplexing client. This may be done before or after registering any number of device clients.
     * <p>
     * This call behaves synchronously, so if it returns without throwing, then all registered device clients were
     * successfully opened.
     * <p>
     * If this client is already open, then this method will do nothing.
     * <p>
     * @throws MultiplexingClientException If any IO or authentication errors occur while opening the multiplexed connection.
     * @throws MultiplexingClientDeviceRegistrationAuthenticationException If one or many of the registered devices failed to authenticate.
     * Any devices not found in the map of registration exceptions provided by
     * {@link MultiplexingClientDeviceRegistrationAuthenticationException#getRegistrationExceptions()} have registered successfully.
     * Even when this is thrown, the AMQPS/AMQPS_WS connection is still open, and other clients may be registered to it.
     */
    public void open() throws MultiplexingClientException
    {
        this.open(false);
    }

    /**
     * Opens this multiplexing client. This may be done before or after registering any number of device clients.
     * <p>
     * This call behaves synchronously, so if it returns without throwing, then all registered device clients were
     * successfully opened.
     * <p>
     * If this client is already open, then this method will do nothing.
     * <p>
     * @param withRetry if true, this open call will apply the current retry policy to allow for the open call to be
     * retried if it fails.
     *
     * @throws MultiplexingClientException If any IO or authentication errors occur while opening the multiplexed connection.
     * @throws MultiplexingClientDeviceRegistrationAuthenticationException If one or many of the registered devices failed to authenticate.
     * Any devices not found in the map of registration exceptions provided by
     * {@link MultiplexingClientDeviceRegistrationAuthenticationException#getRegistrationExceptions()} have registered successfully.
     * Even when this is thrown, the AMQPS/AMQPS_WS connection is still open, and other clients may be registered to it.
     */
    public void open(boolean withRetry) throws MultiplexingClientException
    {
        synchronized (this.operationLock)
        {
            log.info("Opening multiplexing client");
            try
            {
                this.deviceIO.openWithoutWrappingException(withRetry);
            }
            catch (TransportException e)
            {
                // AMQP layer may throw a MultiplexingDeviceUnauthorizedException in some cases. In these
                // cases, we want to copy the registration exceptions map from this TransportException into this thrown exception
                // so that users don't need to look at the cause of the thrown exception to get this important information.
                if (e instanceof MultiplexingDeviceUnauthorizedException)
                {
                    MultiplexingClientDeviceRegistrationAuthenticationException newException =
                            new MultiplexingClientDeviceRegistrationAuthenticationException(OPEN_ERROR_MESSAGE, e);

                    // Bring the exceptions map from the cause to the root level exception, so that users don't have to use
                    // fields from inner exceptions.
                    newException.setRegistrationExceptionsMap(((MultiplexingDeviceUnauthorizedException) e).getRegistrationExceptions());

                    throw newException;
                }

                throw new MultiplexingClientException(OPEN_ERROR_MESSAGE, e);
            }
            log.info("Successfully opened multiplexing client");
        }
    }

    /**
     * Close this multiplexing client. This will close all active device sessions as well as the AMQP connection.
     * <p>
     * If this client is already closed, then this method will do nothing.
     * <p>
     * Once closed, this client can be re-opened. It will preserve all previously registered device clients.
     * <p>
     * @throws MultiplexingClientException This exception is not thrown by this method anymore.
     */
    public void close() throws MultiplexingClientException
    {
        synchronized (this.operationLock)
        {
            log.info("Closing multiplexing client");

            for (DeviceClient deviceClient : this.multiplexedDeviceClients.values())
            {
                deviceClient.closeFileUpload();
            }

            this.deviceIO.close();

            // Note that this method does not close each of the registered device client instances. This is intentional
            // as the calls to deviceClient.close() do nothing besides close the deviceIO layer, which is already closed
            // by the above code.

            log.info("Successfully closed multiplexing client");
        }
    }

    /**
     * Register a device client to this multiplexing client. This method may be called before or after opening the
     * multiplexed connection.
     * <p>
     * Users should use {@link #registerDeviceClients(Iterable)} for registering multiple devices as it has some
     * performance improvements over repeatedly calling this method for individual device registrations. This method blocks on each registration, whereas
     * {@link #registerDeviceClients(Iterable)} blocks on all of the registrations after starting them all asynchronously.
     * <p>
     * A device client can be unregistered using {@link #unregisterDeviceClient(DeviceClient)}, {@link #unregisterDeviceClient(DeviceClient, long)},
     * {@link #unregisterDeviceClients(Iterable)}, or {@link #unregisterDeviceClients(Iterable, long)}. A device client will not be unregistered
     * automatically if it encounters a non-retryable exception, so users are responsible for unregistering a device client
     * when they no longer want it in this multiplexing client.
     * <p>
     * Up to {@link #MAX_MULTIPLEX_DEVICE_COUNT_AMQPS} devices can be registered on a multiplexed AMQPS connection,
     * and up to {@link #MAX_MULTIPLEX_DEVICE_COUNT_AMQPS_WS} devices can be registered on a multiplexed AMQPS_WS connection.
     * <p>
     * If the multiplexing client is already open, then this device client will automatically
     * be opened, too. If the multiplexing client is not open yet, then this device client will not be opened until
     * {@link MultiplexingClient#open()} is called.
     * <p>
     * If the multiplexed connection is already open, then this call will add this device client to the
     * multiplexed connection, and then will block until the registration has been completed.
     * <p>
     * Any proxy settings set to the provided device clients will be overwritten by the proxy settings of this multiplexing client.
     * <p>
     * The registered device client must use the same transport protocol (AMQPS or AMQPS_WS) that this multiplexing client uses.
     * <p>
     * Each registered device client may have its own retry policy and its own SAS token expiry time, separate from every other registered device client.
     * <p>
     * The registered device client must use symmetric key based authentication.
     * <p>
     * The registered device client must belong to the same IoT hub as all previously registered device clients.
     * <p>
     * If the provided device client is already registered to this multiplexing client, then then this method will do nothing.
     * <p>
     * Any subscriptions (twin, methods, cloud to device messages) set on this device client from when it was 
     * previously registered to any multiplexing client will need to be set again as subscriptions and their callbacks are not preserved.
     * <p>
     * @throws InterruptedException If the thread gets interrupted while waiting for the registration to succeed. This
     * will never be thrown if the multiplexing client is not open yet.
     * @throws MultiplexingClientDeviceRegistrationAuthenticationException If the device failed to register. Details for
     * this failure can be found nested within the map given by
     * {@link MultiplexingClientDeviceRegistrationAuthenticationException#getRegistrationExceptions()}. If this exception is
     * thrown, the device was not registered, and therefore it does not need to be unregistered.
     * @throws com.microsoft.azure.sdk.iot.device.exceptions.MultiplexingClientDeviceRegistrationTimeoutException If this operation takes longer than the default timeout allows.
     * @throws MultiplexingClientException If any other Exception is thrown, it will be nested into this exception.
     * @param deviceClient The device client to associate with this multiplexing client.
     */
    public void registerDeviceClient(DeviceClient deviceClient) throws InterruptedException, MultiplexingClientException {
        this.registerDeviceClient(deviceClient, DEFAULT_REGISTRATION_TIMEOUT_MILLISECONDS);
    }

    /**
     * Register a device client to this multiplexing client. This method may be called before or after opening the
     * multiplexed connection.
     * <p>
     * Users should use {@link #registerDeviceClients(Iterable)} for registering multiple devices as it has some
     * performance improvements over repeatedly calling this method for individual device registrations. This method blocks on each registration, whereas
     * {@link #registerDeviceClients(Iterable)} blocks on all of the registrations after starting them all asynchronously.
     * <p>
     * A device client can be unregistered using {@link #unregisterDeviceClient(DeviceClient)}, {@link #unregisterDeviceClient(DeviceClient, long)},
     * {@link #unregisterDeviceClients(Iterable)}, or {@link #unregisterDeviceClients(Iterable, long)}. A device client will not be unregistered
     * automatically if it encounters a non-retryable exception, so users are responsible for unregistering a device client
     * when they no longer want it in this multiplexing client.
     * <p>
     * Up to {@link #MAX_MULTIPLEX_DEVICE_COUNT_AMQPS} devices can be registered on a multiplexed AMQPS connection,
     * and up to {@link #MAX_MULTIPLEX_DEVICE_COUNT_AMQPS_WS} devices can be registered on a multiplexed AMQPS_WS connection.
     * <p>
     * If the multiplexing client is already open, then this device client will automatically
     * be opened, too. If the multiplexing client is not open yet, then this device client will not be opened until
     * {@link MultiplexingClient#open()} is called.
     * <p>
     * If the multiplexed connection is already open, then this call will add this device client to the
     * multiplexed connection, and then will block until the registration has been completed.
     * <p>
     * Any proxy settings set to the provided device clients will be overwritten by the proxy settings of this multiplexing client.
     * <p>
     * The registered device client must use the same transport protocol (AMQPS or AMQPS_WS) that this multiplexing client uses.
     * <p>
     * Each registered device client may have its own retry policy and its own SAS token expiry time, separate from every other registered device client.
     * <p>
     * The registered device client must use symmetric key based authentication.
     * <p>
     * The registered device client must belong to the same IoT hub as all previously registered device clients.
     * <p>
     * If the provided device client is already registered to this multiplexing client, then then this method will do nothing.
     * <p>
     * Any subscriptions (twin, methods, cloud to device messages) set on this device client from when it was 
     * previously registered to any multiplexing client will need to be set again as subscriptions and their callbacks are not preserved.
     * <p>
     * @throws InterruptedException If the thread gets interrupted while waiting for the registration to succeed. This
     * will never be thrown if the multiplexing client is not open yet.
     * @throws MultiplexingClientDeviceRegistrationAuthenticationException If the device failed to register. Details for
     * this failure can be found nested within the map given by
     * {@link MultiplexingClientDeviceRegistrationAuthenticationException#getRegistrationExceptions()}. If this exception is
     * thrown, the device was not registered, and therefore it does not need to be unregistered.
     * @throws com.microsoft.azure.sdk.iot.device.exceptions.MultiplexingClientDeviceRegistrationTimeoutException If this operation takes longer than the provided timeout allows.
     * @throws MultiplexingClientException If any other Exception is thrown, it will be nested into this exception.
     * @param deviceClient The device client to associate with this multiplexing client.
     * @param timeoutMilliseconds How long (in milliseconds) to let this operation wait for all registrations to complete. If this threshold is passed, a {@link com.microsoft.azure.sdk.iot.device.exceptions.MultiplexingClientDeviceRegistrationTimeoutException} is thrown.
     */
    public void registerDeviceClient(DeviceClient deviceClient, long timeoutMilliseconds) throws InterruptedException, MultiplexingClientException {
        Objects.requireNonNull(deviceClient);
        List<DeviceClient> clientList = new ArrayList<>();
        clientList.add(deviceClient);
        registerDeviceClients(clientList, timeoutMilliseconds);
    }

    /**
     * Register multiple device clients to this multiplexing client. This method may be called before or after opening the multiplexed
     * connection.
     * <p>
     * Up to {@link #MAX_MULTIPLEX_DEVICE_COUNT_AMQPS} devices can be registered on a multiplexed AMQPS connection,
     * and up to {@link #MAX_MULTIPLEX_DEVICE_COUNT_AMQPS_WS} devices can be registered on a multiplexed AMQPS_WS connection.
     * <p>
     * A device client can be unregistered using {@link #unregisterDeviceClient(DeviceClient)}, {@link #unregisterDeviceClient(DeviceClient, long)},
     * {@link #unregisterDeviceClients(Iterable)}, or {@link #unregisterDeviceClients(Iterable, long)}. A device client will not be unregistered
     * automatically if it encounters a non-retryable exception, so users are responsible for unregistering a device client
     * when they no longer want it in this multiplexing client.
     * <p>
     * If the multiplexing client is already open, then these device clients will automatically
     * be opened, too. If the multiplexing client is not open yet, then these device clients will not be opened until
     * {@link MultiplexingClient#open()} is called.
     * <p>
     * If the multiplexed connection is already open, then this call will asynchronously add each device client to the
     * multiplexed connection, and then will block until all registrations have been completed.
     * <p>
     * Any proxy settings set to the provided device clients will be overwritten by the proxy settings of this multiplexing client.
     * <p>
     * The registered device clients must use the same transport protocol (AMQPS or AMQPS_WS) that this multiplexing client uses.
     * <p>
     * Each registered device client may have its own retry policy and its own SAS token expiry time, separate from every other registered device client.
     * <p>
     * The registered device clients must use symmetric key based authentication.
     * <p>
     * The registered device clients must belong to the same IoT hub as all previously registered device clients.
     * <p>
     * If any of these device clients are already registered to this multiplexing client, then then this method will
     * not do anything to that particular device client. All other provided device clients will still be registered though.
     * <p>
     * Any subscriptions (twin, methods, cloud to device messages) set on these device clients from when it was 
     * previously registered to any multiplexing client will need to be set again as subscriptions and their callbacks are not preserved.
     * <p>
     * @throws InterruptedException If the thread gets interrupted while waiting for the registration to succeed. This
     * will never be thrown if the multiplexing client is not open yet.
     * @throws MultiplexingClientDeviceRegistrationAuthenticationException If one or more devices failed to register.
     * Details for each failure can be found in the map provided by
     * {@link MultiplexingClientDeviceRegistrationAuthenticationException#getRegistrationExceptions()}. Any devices not
     * found in the map of registration exceptions provided by this exception have registered successfully. Any devices
     * that are found in the map of registration exceptions provided by this exception were not registered, and therefore
     * do not need to be unregistered.
     * @throws com.microsoft.azure.sdk.iot.device.exceptions.MultiplexingClientDeviceRegistrationTimeoutException If this operation takes longer than the default timeout allows.
     * @throws MultiplexingClientException If any other Exception is thrown, it will be nested into this exception.
     * @param deviceClients The device clients to associate with this multiplexing client.
     */
    public void registerDeviceClients(Iterable<DeviceClient> deviceClients) throws InterruptedException, MultiplexingClientException
    {
        this.registerDeviceClients(deviceClients, DEFAULT_REGISTRATION_TIMEOUT_MILLISECONDS);
    }

    /**
     * Register multiple device clients to this multiplexing client. This method may be called before or after opening the multiplexed
     * connection.
     * <p>
     * Up to {@link #MAX_MULTIPLEX_DEVICE_COUNT_AMQPS} devices can be registered on a multiplexed AMQPS connection,
     * and up to {@link #MAX_MULTIPLEX_DEVICE_COUNT_AMQPS_WS} devices can be registered on a multiplexed AMQPS_WS connection.
     * <p>
     * A device client can be unregistered using {@link #unregisterDeviceClient(DeviceClient)}, {@link #unregisterDeviceClient(DeviceClient, long)},
     * {@link #unregisterDeviceClients(Iterable)}, or {@link #unregisterDeviceClients(Iterable, long)}. A device client will not be unregistered
     * automatically if it encounters a non-retryable exception, so users are responsible for unregistering a device client
     * when they no longer want it in this multiplexing client.
     * <p>
     * If the multiplexing client is already open, then these device clients will automatically
     * be opened, too. If the multiplexing client is not open yet, then these device clients will not be opened until
     * {@link MultiplexingClient#open()} is called.
     * <p>
     * If the multiplexed connection is already open, then this call will asynchronously add each device client to the
     * multiplexed connection, and then will block until all registrations have been completed.
     * <p>
     * Any proxy settings set to the provided device clients will be overwritten by the proxy settings of this multiplexing client.
     * <p>
     * The registered device clients must use the same transport protocol (AMQPS or AMQPS_WS) that this multiplexing client uses.
     * <p>
     * Each registered device client may have its own retry policy and its own SAS token expiry time, separate from every other registered device client.
     * <p>
     * The registered device clients must use symmetric key based authentication.
     * <p>
     * The registered device clients must belong to the same IoT hub as all previously registered device clients.
     * <p>
     * If any of these device clients are already registered to this multiplexing client, then then this method will
     * not do anything to that particular device client. All other provided device clients will still be registered though.
     * <p>
     * Any subscriptions (twin, methods, cloud to device messages) set on these device clients from when it was 
     * previously registered to any multiplexing client will need to be set again as subscriptions and their callbacks are not preserved.
     * <p>
     * @throws InterruptedException If the thread gets interrupted while waiting for the registration to succeed. This
     * will never be thrown if the multiplexing client is not open yet.
     * @throws MultiplexingClientDeviceRegistrationAuthenticationException If one or more devices failed to register.
     * Details for each failure can be found in the map provided by
     * {@link MultiplexingClientDeviceRegistrationAuthenticationException#getRegistrationExceptions()}. Any devices not
     * found in the map of registration exceptions provided by this exception have registered successfully. Any devices
     * that are found in the map of registration exceptions provided by this exception were not registered, and therefore
     * do not need to be unregistered.
     * @throws com.microsoft.azure.sdk.iot.device.exceptions.MultiplexingClientDeviceRegistrationTimeoutException If this operation takes longer than the provided timeout allows.
     * @throws MultiplexingClientException If any other Exception is thrown, it will be nested into this exception.
     * @param deviceClients The device clients to associate with this multiplexing client.
     * @param timeoutMilliseconds How long (in milliseconds) to let this operation wait for all registrations to complete. If this threshold is passed, a {@link com.microsoft.azure.sdk.iot.device.exceptions.MultiplexingClientDeviceRegistrationTimeoutException} is thrown.
     */
    public void registerDeviceClients(Iterable<DeviceClient> deviceClients, long timeoutMilliseconds) throws InterruptedException, MultiplexingClientException
    {
        Objects.requireNonNull(deviceClients);

        if (timeoutMilliseconds <= 0)
        {
            throw new IllegalArgumentException("Cannot set a device registration timeout of less than or equal to 0 milliseconds");
        }

        synchronized (this.operationLock)
        {
            List<DeviceClientConfig> deviceClientConfigsToRegister = new ArrayList<>();

            Map<String, DeviceClient> devicesToRegisterMap = new HashMap<>();
            for (DeviceClient deviceClientToRegister : deviceClients)
            {
                devicesToRegisterMap.put(deviceClientToRegister.getConfig().getDeviceId(), deviceClientToRegister);
                DeviceClientConfig configToAdd = deviceClientToRegister.getConfig();

                // Overwrite the sslContext of the new client to match the multiplexing client
                // settings if it was set. If it wasn't set, defer to the default client to set this ssl context
                if (this.sslContext != null)
                {
                    configToAdd.getAuthenticationProvider().setSSLContext(this.sslContext);
                }

                // Overwrite the proxy settings of the new client to match the multiplexing client settings
                configToAdd.setProxy(this.proxySettings);

                if (configToAdd.getAuthenticationType() != DeviceClientConfig.AuthType.SAS_TOKEN)
                {
                    throw new UnsupportedOperationException("Can only register to multiplex a device client that uses SAS token based authentication");
                }

                if (configToAdd.getProtocol() != this.protocol)
                {
                    throw new UnsupportedOperationException("A device client cannot be registered to a multiplexing client that specifies a different transport protocol.");
                }

                if (this.protocol == IotHubClientProtocol.AMQPS && this.multiplexedDeviceClients.size() > MAX_MULTIPLEX_DEVICE_COUNT_AMQPS)
                {
                    throw new UnsupportedOperationException(String.format("Multiplexed connections over AMQPS only support up to %d devices", MAX_MULTIPLEX_DEVICE_COUNT_AMQPS));
                }

                // Typically client side validation is duplicate work, but IoT hub doesn't give a good error message when closing the
                // AMQPS_WS connection so this is the only way that users will know about this limit
                if (this.protocol == IotHubClientProtocol.AMQPS_WS && this.multiplexedDeviceClients.size() > MAX_MULTIPLEX_DEVICE_COUNT_AMQPS_WS)
                {
                    throw new UnsupportedOperationException(String.format("Multiplexed connections over AMQPS_WS only support up to %d devices", MAX_MULTIPLEX_DEVICE_COUNT_AMQPS_WS));
                }

                if (!this.hostName.equalsIgnoreCase(configToAdd.getIotHubHostname()))
                {
                    throw new UnsupportedOperationException("A device client cannot be registered to a multiplexing client that specifies a different host name.");
                }

                if (deviceClientToRegister.getDeviceIO() != null && deviceClientToRegister.getDeviceIO().isOpen() && !deviceClientToRegister.isMultiplexed)
                {
                    throw new UnsupportedOperationException("Cannot register a device client to a multiplexed connection when the device client was already opened.");
                }

                deviceClientToRegister.setAsMultiplexed();
                deviceClientToRegister.setDeviceIO(this.deviceIO);
                deviceClientToRegister.setConnectionType(IoTHubConnectionType.USE_MULTIPLEXING_CLIENT);

                // Set notifies us if the device client is already in the set
                boolean deviceAlreadyRegistered = this.multiplexedDeviceClients.containsKey(deviceClientToRegister.getConfig().getDeviceId());
                if (deviceAlreadyRegistered)
                {
                    log.debug("Device {} wasn't registered to the multiplexed connection because it is already registered.", configToAdd.getDeviceId());
                }
                else
                {
                    deviceClientConfigsToRegister.add(configToAdd);
                }
            }

            // if the device IO hasn't been created yet, then this client will be registered once it is created.
            for (DeviceClientConfig configBeingRegistered : deviceClientConfigsToRegister)
            {
                log.info("Registering device {} to multiplexing client", configBeingRegistered.getDeviceId());
            }

            try
            {
                this.deviceIO.registerMultiplexedDeviceClient(deviceClientConfigsToRegister, timeoutMilliseconds);

                // Only update the local state map once the register call has succeeded
                this.multiplexedDeviceClients.putAll(devicesToRegisterMap);
            }
            catch (MultiplexingClientDeviceRegistrationAuthenticationException e)
            {
                // If registration failed, 1 or more clients should not be considered registered in this layer's state.
                // Save the exception so it can be rethrown once the local state has been updated to match the actual state
                // of the multiplexed connection.
                for (DeviceClient clientsThatAttemptedToRegister : deviceClients)
                {
                    // Only update the local state map once the register call has succeeded
                    String deviceIdThatAttemptedToRegister = clientsThatAttemptedToRegister.getConfig().getDeviceId();

                    if (!e.getRegistrationExceptions().containsKey(deviceIdThatAttemptedToRegister))
                    {
                        this.multiplexedDeviceClients.put(deviceIdThatAttemptedToRegister, clientsThatAttemptedToRegister);
                    }
                }

                throw e;
            }
        }
    }

    /**
     * Unregister a device client from this multiplexing client. This method may be called before or after opening the
     * multiplexed connection.
     * <p>
     * Users should use {@link #unregisterDeviceClients(Iterable)} for unregistering multiple devices as it has some
     * performance improvements over repeatedly calling this method for individual device unregistrations. This method blocks on each unregistration, whereas
     * {@link #registerDeviceClients(Iterable)} blocks on all of the unregistrations after starting them all asynchronously.
     * <p>
     * If the multiplexed connection is already open, then this call will close the AMQP device session associated with
     * this device, but it will not close any other registered device sessions or the multiplexing client itself.
     * <p>
     * If the multiplexed connection is already open, and this call would unregister the last device client,
     * the multiplexed connection will remain open. The multiplexed connection can only be closed by calling
     * {@link #close()}
     * <p>
     * Once a device client is unregistered, it may be re-registered to this or any other multiplexing client. It cannot
     * be used in non-multiplexing scenarios or used by the deprecated {@link TransportClient}.
     * <p>
     * Any subscriptions set on this device client for twin/methods/cloud to device messages will need to be set again
     * after this device is re-registered.
     * <p>
     * @param deviceClient The device client to unregister from this multiplexing client.
     * @throws InterruptedException If the thread gets interrupted while waiting for the unregistration to succeed.
     * @throws com.microsoft.azure.sdk.iot.device.exceptions.MultiplexingClientDeviceRegistrationTimeoutException If the unregistration takes longer than the default timeout allows.
     * @throws MultiplexingClientException If any other Exception is thrown, it will be nested into this exception.
     */
    @SuppressWarnings("deprecation")
    public void unregisterDeviceClient(DeviceClient deviceClient) throws InterruptedException, MultiplexingClientException
    {
        this.unregisterDeviceClient(deviceClient, DEFAULT_UNREGISTRATION_TIMEOUT_MILLISECONDS);
    }

    /**
     * Unregister a device client from this multiplexing client. This method may be called before or after opening the
     * multiplexed connection.
     * <p>
     * Users should use {@link #unregisterDeviceClients(Iterable)} for unregistering multiple devices as it has some
     * performance improvements over repeatedly calling this method for individual device unregistrations. This method blocks on each unregistration, whereas
     * {@link #registerDeviceClients(Iterable)} blocks on all of the unregistrations after starting them all asynchronously.
     * <p>
     * If the multiplexed connection is already open, then this call will close the AMQP device session associated with
     * this device, but it will not close any other registered device sessions or the multiplexing client itself.
     * <p>
     * If the multiplexed connection is already open, and this call would unregister the last device client,
     * the multiplexed connection will remain open. The multiplexed connection can only be closed by calling
     * {@link #close()}
     * <p>
     * Once a device client is unregistered, it may be re-registered to this or any other multiplexing client. It cannot
     * be used in non-multiplexing scenarios or used by the deprecated {@link TransportClient}.
     * <p>
     * Any subscriptions set on this device client for twin/methods/cloud to device messages will need to be set again
     * after this device is re-registered.
     * <p>
     * @param deviceClient The device client to unregister from this multiplexing client.
     * @param timeoutMilliseconds How long (in milliseconds) to let this operation wait for all unregistrations to complete. If this threshold is passed, a {@link com.microsoft.azure.sdk.iot.device.exceptions.MultiplexingClientDeviceRegistrationTimeoutException} is thrown.
     * @throws InterruptedException If the thread gets interrupted while waiting for the unregistration to succeed.
     * @throws com.microsoft.azure.sdk.iot.device.exceptions.MultiplexingClientDeviceRegistrationTimeoutException If the unregistration takes longer than the provided timeout allows.
     * @throws MultiplexingClientException If any other Exception is thrown, it will be nested into this exception.
     */
    @SuppressWarnings("deprecation")
    public void unregisterDeviceClient(DeviceClient deviceClient, long timeoutMilliseconds) throws InterruptedException, MultiplexingClientException
    {
        Objects.requireNonNull(deviceClient);
        List<DeviceClient> clientList = new ArrayList<>();
        clientList.add(deviceClient);
        unregisterDeviceClients(clientList, timeoutMilliseconds);
    }

    /**
     * Unregister multiple device clients from this multiplexing client. This method may be called before or after opening the
     * multiplexed connection.
     * <p>
     * If the multiplexed connection is already open, then this call will close the AMQP device session associated with
     * this device, but it will not close any other registered device sessions or the multiplexing client itself.
     * <p>
     * If the multiplexed connection is already open, and this call would unregister the last device clients,
     * the multiplexed connection will remain open. The multiplexed connection can only be closed by calling
     * {@link #close()}
     * <p>
     * Once a device client is unregistered, it may be re-registered to this or any other multiplexing client. It cannot
     * be used in non-multiplexing scenarios or used by the deprecated {@link TransportClient}.
     * <p>
     * Any subscriptions set on these device clients for twin/methods/cloud to device messages will need to be set again
     * after these devices are re-registered.
     * <p>
     * @param deviceClients The device clients to unregister from this multiplexing client.
     * @throws InterruptedException If the thread gets interrupted while waiting for the unregistration to succeed.
     * @throws com.microsoft.azure.sdk.iot.device.exceptions.MultiplexingClientDeviceRegistrationTimeoutException If the unregistration takes longer than the default timeout allows.
     * @throws MultiplexingClientException If any other Exception is thrown, it will be nested into this exception.
     */
    @SuppressWarnings("deprecation")
    public void unregisterDeviceClients(Iterable<DeviceClient> deviceClients) throws InterruptedException, MultiplexingClientException
    {
        this.unregisterDeviceClients(deviceClients, DEFAULT_UNREGISTRATION_TIMEOUT_MILLISECONDS);
    }

    /**
     * Unregister multiple device clients from this multiplexing client. This method may be called before or after opening the
     * multiplexed connection.
     * <p>
     * If the multiplexed connection is already open, then this call will close the AMQP device session associated with
     * this device, but it will not close any other registered device sessions or the multiplexing client itself.
     * <p>
     * If the multiplexed connection is already open, and this call would unregister the last device clients,
     * the multiplexed connection will remain open. The multiplexed connection can only be closed by calling
     * {@link #close()}
     * <p>
     * Once a device client is unregistered, it may be re-registered to this or any other multiplexing client. It cannot
     * be used in non-multiplexing scenarios or used by the deprecated {@link TransportClient}.
     * <p>
     * Any subscriptions set on these device clients for twin/methods/cloud to device messages will need to be set again
     * after these devices are re-registered.
     * <p>
     * @param deviceClients The device clients to unregister from this multiplexing client.
     * @param timeoutMilliseconds How long (in milliseconds) to let this operation wait for all unregistrations to complete. If this threshold is passed, a {@link com.microsoft.azure.sdk.iot.device.exceptions.MultiplexingClientDeviceRegistrationTimeoutException} is thrown.
     * @throws InterruptedException If the thread gets interrupted while waiting for the unregistration to succeed.
     * @throws com.microsoft.azure.sdk.iot.device.exceptions.MultiplexingClientDeviceRegistrationTimeoutException If the unregistration takes longer than the provided timeout allows.
     * @throws MultiplexingClientException If any other Exception is thrown, it will be nested into this exception.
     */
    @SuppressWarnings("deprecation")
    public void unregisterDeviceClients(Iterable<DeviceClient> deviceClients, long timeoutMilliseconds) throws InterruptedException, MultiplexingClientException
    {
        Objects.requireNonNull(deviceClients);

        if (timeoutMilliseconds <= 0)
        {
            throw new IllegalArgumentException("Cannot set a device unregistration timeout of less than 0 milliseconds");
        }

        synchronized (this.operationLock)
        {
            List<DeviceClientConfig> deviceClientConfigsToRegister = new ArrayList<>();
            for (DeviceClient deviceClientToUnregister : deviceClients)
            {
                DeviceClientConfig configToUnregister = deviceClientToUnregister.getConfig();
                deviceClientConfigsToRegister.add(configToUnregister);
                log.info("Unregistering device {} from multiplexing client", deviceClientToUnregister.getConfig().getDeviceId());
                this.multiplexedDeviceClients.remove(deviceClientToUnregister.getConfig().getDeviceId());
                deviceClientToUnregister.setDeviceIO(null);

                // Clear the device client's subscriptions after it has been unregistered from the multiplexing client
                // We may make this optional at some point in the future if users want to be able to preserve subscriptions.
                deviceClientToUnregister.markTwinAsUnsubscribed();
                deviceClientToUnregister.markMethodsAsUnsubscribed();
                deviceClientToUnregister.setMessageCallback(null, null);
            }

            this.deviceIO.unregisterMultiplexedDeviceClient(deviceClientConfigsToRegister, timeoutMilliseconds);
        }
    }

    /**
     * Registers a callback to be executed when the connection status of the multiplexed connection as a whole changes.
     * The callback will be fired with a status and a reason why the multiplexed connection's status changed. When the
     * callback is fired, the provided context will be provided alongside the status and reason.
     *
     * <p>Note that this callback will not be fired for device specific connection status changes. In order to be notified
     * when a particular device's connection status changes, you will need to register a connection status change callback
     * on that device client instance using {@link DeviceClient#registerConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback, Object)}.
     *
     * <p>Note that the thread used to deliver this callback should not be used to call open()/closeNow() on the client
     * that this callback belongs to. All open()/closeNow() operations should be done on a separate thread</p>
     *
     * @param callback The callback to be fired when the connection status of the multiplexed connection changes.
     *                 Can be null to unset this listener as long as the provided callbackContext is also null.
     * @param callbackContext a context to be passed to the callback. Can be {@code null}.
     */
    public void registerConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback callback, Object callbackContext)
    {
        this.deviceIO.registerMultiplexingConnectionStateCallback(callback, callbackContext);
    }

    /**
     * Returns if a device client for the provided device Id is already registered to this multiplexing client.
     * @param deviceId The Id of the device client to look for.
     * @return True if a device client is already registered with this Id. False otherwise.
     */
    public boolean isDeviceRegistered(String deviceId)
    {
        synchronized (this.operationLock)
        {
            return this.multiplexedDeviceClients.containsKey(deviceId);
        }
    }

    /**
     * Get the number of currently registered devices on this multiplexing client.
     * @return The number of currently registered devices on this multiplexing client.
     */
    public int getRegisteredDeviceCount()
    {
        synchronized (this.operationLock)
        {
            // O(1) operation since HashMap saves this value as an integer rather than iterating over each element.
            // So there is no need to be more clever about this.

            // Note that ConcurrentHashMap's version of this method has O(n) time complexity, so we avoid using that type here.
            // Instead we just make every method synchronous to protect against race conditions.
            return this.multiplexedDeviceClients.size();
        }
    }

    /**
     * Sets the given retry policy for the multiplexing client level connection management.
     * <a href="https://github.com/Azure/azure-iot-sdk-java/blob/main/device/iot-device-client/devdoc/requirement_docs/com/microsoft/azure/iothub/retryPolicy.md">
     *     See more details about the default retry policy and about using custom retry policies here</a>
     * @param retryPolicy The policy that the multiplexing client will use when reconnecting.
     */
    public void setRetryPolicy(RetryPolicy retryPolicy)
    {
        this.deviceIO.setMultiplexingRetryPolicy(retryPolicy);
    }
}
