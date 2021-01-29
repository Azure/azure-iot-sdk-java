<details>
<summary>ServiceClient</summary>

```
/**
 * Create ServiceClient from the specified connection string
 * @param iotHubServiceClientProtocol  protocol to use
 * @param connectionString The connection string for the IotHub
 * @return The created ServiceClient object
 * @throws IOException This exception is thrown if the object creation failed
 */
public static ServiceClient createFromConnectionString(String connectionString, IotHubServiceClientProtocol iotHubServiceClientProtocol) throws IOException

/**
 * Create ServiceClient from the specified connection string
 * @param iotHubServiceClientProtocol  protocol to use
 * @param connectionString The connection string for the IotHub
 * @param options The connection options to use when connecting to the service.
 * @return The created ServiceClient object
 * @throws IOException This exception is thrown if the object creation failed
 */
public static ServiceClient createFromConnectionString(String connectionString, IotHubServiceClientProtocol iotHubServiceClientProtocol, ServiceClientOptions options) throws IOException

/**
 * Create a {@link ServiceClient} instance with a custom {@link TokenCredential} to allow for finer grain control
 * of authentication tokens used in the underlying connection.
 *
 * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
 * @param authenticationTokenProvider The custom {@link TokenCredential} that will provide authentication tokens to
 *                                    this library when they are needed.
 * @param authorizationType The type of authentication tokens that the provided {@link TokenCredential}
 *                          implementation will always give.
 * @param iotHubServiceClientProtocol The protocol to open the connection with.
 * @return The created {@link ServiceClient} instance.
 */
public static ServiceClient createFromTokenCredential(String hostName, TokenCredential authenticationTokenProvider, TokenCredentialType authorizationType, IotHubServiceClientProtocol iotHubServiceClientProtocol)

/**
 * Create a {@link ServiceClient} instance with a custom {@link TokenCredential} to allow for finer grain control
 * of authentication tokens used in the underlying connection.
 *
 * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
 * @param authenticationTokenProvider The custom {@link TokenCredential} that will provide authentication tokens to
 *                                    this library when they are needed.
 * @param authorizationType The type of authentication tokens that the provided {@link TokenCredential}
 *                          implementation will always give.
 * @param iotHubServiceClientProtocol The protocol to open the connection with.
 * @param options The connection options to use when connecting to the service.
 * @return The created {@link ServiceClient} instance.
 */
public static ServiceClient createFromTokenCredential(String hostName, TokenCredential authenticationTokenProvider, TokenCredentialType authorizationType, IotHubServiceClientProtocol iotHubServiceClientProtocol, ServiceClientOptions options)
```
</details>

<details>
<summary>RegistryManager</summary>

```
/**
 * Static constructor to create instance from connection string
 *
 * @param connectionString The iot hub connection string
 * @return The instance of RegistryManager
 * @throws IOException This exception is never thrown.
 */
public static RegistryManager createFromConnectionString(String connectionString) throws IOException

/**
 * Static constructor to create instance from connection string
 *
 * @param connectionString The iot hub connection string
 * @param options The connection options to use when connecting to the service.
 * @return The instance of RegistryManager
 * @throws IOException This exception is never thrown.
 */
public static RegistryManager createFromConnectionString(String connectionString, RegistryManagerOptions options) throws IOException

/**
 * Create a new RegistryManager instance.
 *
 * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
 * @param authenticationTokenProvider The custom {@link TokenCredential} that will provide authentication tokens to
 *                                    this library when they are needed.
 * @param tokenCredentialType The type of authentication tokens that the provided {@link TokenCredential}
 *                          implementation will always give.
 * @return The instance of RegistryManager
 */
public static RegistryManager createFromTokenCredential(String hostName, TokenCredential authenticationTokenProvider, TokenCredentialType tokenCredentialType)

/**
 * Create a new RegistryManager instance.
 *
 * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
 * @param authenticationTokenProvider The custom {@link TokenCredential} that will provide authentication tokens to
 *                                    this library when they are needed.
 * @param tokenCredentialType The type of authentication tokens that the provided {@link TokenCredential}
 *                          implementation will always give.
 * @param options The connection options to use when connecting to the service.
 * @return The instance of RegistryManager
 */
public static RegistryManager createFromTokenCredential(String hostName, TokenCredential authenticationTokenProvider, TokenCredentialType tokenCredentialType, RegistryManagerOptions options)
```
</details>

<details>
<summary>DeviceMethod</summary>

```
/**
 * Create a DeviceMethod instance from the information in the connection string.
 *
 * @param connectionString is the IoTHub connection string.
 * @return an instance of the DeviceMethod.
 * @throws IOException This exception is never thrown.
 */
public static DeviceMethod createFromConnectionString(String connectionString) throws IOException

/**
 * Create a DeviceMethod instance from the information in the connection string.
 *
 * @param connectionString is the IoTHub connection string.
 * @param options the configurable options for each operation on this client. May not be null.
 * @return an instance of the DeviceMethod.
 * @throws IOException This exception is never thrown.
 */
public static DeviceMethod createFromConnectionString(String connectionString, DeviceMethodClientOptions options) throws IOException

/**
 * Create a new DeviceMethod instance.
 *
 * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
 * @param authenticationTokenProvider The custom {@link TokenCredential} that will provide authentication tokens to
 *                                    this library when they are needed.
 * @param tokenCredentialType The type of authentication tokens that the provided {@link TokenCredential}
 *                          implementation will always give.
 * @return the new DeviceMethod instance.
 */
public static DeviceMethod createFromTokenCredential(String hostName, TokenCredential authenticationTokenProvider, TokenCredentialType tokenCredentialType)

/**
 * Create a new DeviceMethod instance.
 *
 * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
 * @param authenticationTokenProvider The custom {@link TokenCredential} that will provide authentication tokens to
 *                                    this library when they are needed.
 * @param tokenCredentialType The type of authentication tokens that the provided {@link TokenCredential}
 *                          implementation will always give.
 * @param options The connection options to use when connecting to the service.
 * @return the new DeviceMethod instance.
 */
public static DeviceMethod createFromTokenCredential(String hostName, TokenCredential authenticationTokenProvider, TokenCredentialType tokenCredentialType, DeviceMethodClientOptions options)
```
</details>

<details>
<summary>DeviceTwin</summary>

```
/**
 * Static constructor to create instance from connection string.
 *
 * @param connectionString The iot hub connection string.
 * @return The instance of DeviceTwin.
 * @throws IOException This exception is never thrown.
 */
public static DeviceTwin createFromConnectionString(String connectionString) throws IOException

/**
 * Static constructor to create instance from connection string.
 *
 * @param connectionString The iot hub connection string.
 * @param options the configurable options for each operation on this client. May not be null.
 * @return The instance of DeviceTwin.
 * @throws IOException This exception is never thrown.
 */
public static DeviceTwin createFromConnectionString(String connectionString, DeviceTwinClientOptions options) throws IOException

/**
 * Create a new DeviceTwin instance.
 *
 * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
 * @param authenticationTokenProvider The custom {@link TokenCredential} that will provide authentication tokens to
 *                                    this library when they are needed.
 * @param tokenCredentialType The type of authentication tokens that the provided {@link TokenCredential}
 *                          implementation will always give.
 * @return the new DeviceTwin instance.
 */
public static DeviceTwin createFromTokenCredential(String hostName, TokenCredential authenticationTokenProvider, TokenCredentialType tokenCredentialType)

/**
 * Create a new DeviceTwin instance.
 *
 * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
 * @param authenticationTokenProvider The custom {@link TokenCredential} that will provide authentication tokens to
 *                                    this library when they are needed.
 * @param tokenCredentialType The type of authentication tokens that the provided {@link TokenCredential}
 *                          implementation will always give.
 * @param options The connection options to use when connecting to the service.
 * @return the new DeviceTwin instance.
 */
public static DeviceTwin createFromTokenCredential(String hostName, TokenCredential authenticationTokenProvider, TokenCredentialType tokenCredentialType, DeviceTwinClientOptions options)
```
</details>

<details>
<summary>JobClient</summary>

```
/**
 * Static constructor to create instance from connection string
 *
 * @param connectionString The iot hub connection string
 * @return The instance of JobClient
 * @throws IOException This exception is never thrown.
 * @throws IllegalArgumentException if the provided connectionString is {@code null} or empty
 */
public static JobClient createFromConnectionString(String connectionString) throws IOException, IllegalArgumentException

/**
 * Create a new JobClient instance.
 *
 * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
 * @param authenticationTokenProvider The custom {@link TokenCredential} that will provide authentication tokens to
 *                                    this library when they are needed.
 * @param tokenCredentialType The type of authentication tokens that the provided {@link TokenCredential}
 *                          implementation will always give.
 * @return The new JobClient instance.
 */
public static JobClient createFromTokenCredential(String hostName, TokenCredential authenticationTokenProvider, TokenCredentialType tokenCredentialType)

/**
 * Create a new JobClient instance.
 *
 * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
 * @param authenticationTokenProvider The custom {@link TokenCredential} that will provide authentication tokens to
 *                                    this library when they are needed.
 * @param tokenCredentialType The type of authentication tokens that the provided {@link TokenCredential}
 *                          implementation will always give.
 * @param options The connection options to use when connecting to the service.
 * @return The new JobClient instance.
 */
public static JobClient createFromTokenCredential(String hostName, TokenCredential authenticationTokenProvider, TokenCredentialType tokenCredentialType, JobClientOptions options)
```
</details>

<details>
<summary>DigitalTwinClient</summary>

```
/**
 * Creates an implementation instance of {@link DigitalTwins} that is used to invoke the Digital Twin features
 * @param connectionString The IoT Hub connection string
 * @return The instantiated DigitalTwinClient.
 */
public static DigitalTwinClient createFromConnectionString(String connectionString)

/**
 * Creates an implementation instance of {@link DigitalTwins} that is used to invoke the Digital Twin features
 *
 * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
 * @param authenticationTokenProvider The custom {@link TokenCredential} that will provide authentication tokens to
 *                                    this library when they are needed.
 * @param tokenCredentialType The type of authentication tokens that the provided {@link TokenCredential}
 *                          implementation will always give.
 * @return The instantiated DigitalTwinClient.
 */
public static DigitalTwinClient createFromTokenCredential(String hostName, TokenCredential authenticationTokenProvider, TokenCredentialType tokenCredentialType)
```
</details>

<details>
<summary>DigitalTwinAsyncClient</summary>

```
/**
 * Creates an implementation instance of {@link DigitalTwins} that is used to invoke the Digital Twin features
 *
 * @param connectionString The IoTHub connection string
 * @return The instantiated DigitalTwinAsyncClient.
 */
public static DigitalTwinAsyncClient createFromConnectionString(String connectionString)

/**
 * Creates an implementation instance of {@link DigitalTwins} that is used to invoke the Digital Twin features
 *
 * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
 * @param authenticationTokenProvider The custom {@link TokenCredential} that will provide authentication tokens to
 *                                    this library when they are needed.
 * @param tokenCredentialType The type of authentication tokens that the provided {@link TokenCredential}
 *                          implementation will always give.
 * @return The instantiated DigitalTwinAsyncClient.
 */
public static DigitalTwinAsyncClient createFromTokenCredential(String hostName, TokenCredential authenticationTokenProvider, TokenCredentialType tokenCredentialType)
```
</details>

<details>
<summary>Query</summary>

```
/**
 * Sends request for the query to the IotHub.
 *
 * @param iotHubConnectionString Hub Connection String.
 * @param url URL to Query on.
 * @param method HTTP Method for the requesting a query.
 * @param httpConnectTimeout the http connect timeout to use for this request.
 * @param httpReadTimeout the http read timeout to use for this request.
 * @param proxy the proxy to use, or null if no proxy should be used.
 * @return QueryResponse object which holds the response Iterator.
 * @throws IOException If any of the input parameters are not valid.
 * @throws IotHubException If HTTP response other then status ok is received.
 */
public QueryResponse sendQueryRequest(IotHubConnectionString iotHubConnectionString,
                                      URL url,
                                      HttpMethod method,
                                      int httpConnectTimeout,
                                      int httpReadTimeout,
                                      Proxy proxy) throws IOException, IotHubException

/**
 * Sends request for the query to the IotHub.
 *
 * @param authenticationTokenProvider The custom {@link TokenCredential} that will provide authentication tokens to
 *                                    this library when they are needed.
 * @param tokenCredentialType The type of authentication tokens that the provided {@link TokenCredential}
 *                          implementation will always give.
 * @param url URL to Query on.
 * @param method HTTP Method for the requesting a query.
 * @param httpConnectTimeout the http connect timeout to use for this request.
 * @param httpReadTimeout the http read timeout to use for this request.
 * @param proxy the proxy to use, or null if no proxy should be used.
 * @return QueryResponse object which holds the response Iterator.
 * @throws IOException If any of the input parameters are not valid.
 * @throws IotHubException If HTTP response other then status ok is received.
 */
public QueryResponse sendQueryRequest(TokenCredential authenticationTokenProvider,
                                      TokenCredentialType tokenCredentialType,
                                      URL url,
                                      HttpMethod method,
                                      int httpConnectTimeout,
                                      int httpReadTimeout,
                                      Proxy proxy) throws IOException, IotHubException
```
</details>

<details>
<summary>TokenCredential</summary>

```
public interface TokenCredential {
    Mono<AccessToken> getToken(TokenRequestContext var1);
}
```
</details>

<details>
<summary>AccessToken</summary>

```
public class AccessToken {
    public AccessToken(String token, OffsetDateTime expiresAt)

    public String getToken()
    public OffsetDateTime getExpiresAt()
    public boolean isExpired()
}
```
</details>

<details>
<summary>IotHubConnectionStringCredential</summary>

```
public class IotHubConnectionStringCredential implements TokenCredential
{
    /**
     * Construct a new {@link IotHubConnectionStringCredential}.
     * @param iotHubConnectionString The connection string for your IoT Hub.
     */
    public IotHubConnectionStringCredential(String iotHubConnectionString)

    /**
     * Construct a new {@link IotHubConnectionStringCredential}.
     * @param iotHubConnectionString The connection string for your IoT Hub.
     * @param tokenLifespanSeconds The number of seconds that the generated SAS tokens should be valid for. If less than
     *                             or equal to 0, the default time to live will be used.
     */
    public IotHubConnectionStringCredential(String iotHubConnectionString, long tokenLifespanSeconds)

    /**
     * Get a valid SAS token. The returned token may be a cached SAS token from previous calls if that token is still
     * valid. This function will proactively renew the token ahead of its expiry time in order to avoid clock skew issues.
     * @param tokenRequestContext the context that the token will be used for
     * @return a non-expired SAS token built from the connection string that was provided in the constructor.
     */
    @Override
    public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext)
}
```
</details>