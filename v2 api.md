<details>
  <summary>DeviceClient, ModuleClient, and Options</summary>
  
```java
public final class DeviceClient extends InternalClient
{
    public DeviceClient(String connString, IotHubClientProtocol protocol) throws URISyntaxException;

    public DeviceClient(String connString, IotHubClientProtocol protocol, ClientOptions clientOptions) throws URISyntaxException;

    public DeviceClient(String hostName, String deviceId, SasTokenProvider sasTokenProvider, IotHubClientProtocol protocol);

    public DeviceClient(String hostName, String deviceId, SasTokenProvider sasTokenProvider, IotHubClientProtocol protocol, ClientOptions clientOptions);

    public DeviceClient(String uri, String deviceId, SecurityProvider securityProvider, IotHubClientProtocol protocol) throws URISyntaxException, IOException;

    public DeviceClient(String uri, String deviceId, SecurityProvider securityProvider, IotHubClientProtocol protocol, ClientOptions clientOptions) throws URISyntaxException, IOException;

    public void open(boolean withRetry) throws IOException;

    public void close();

    public FileUploadSasUriResponse getFileUploadSasUri(FileUploadSasUriRequest request) throws IOException;

    public void completeFileUpload(FileUploadCompletionNotification notification) throws IOException;
    
    public void sendEventAsync(Message message, IotHubEventCallback callback, Object callbackContext);

    public void sendEventBatchAsync(List<Message> messages, IotHubEventCallback callback, Object callbackContext);

    public DeviceClient setMessageCallback(MessageCallback callback, Object context);

    public void getTwinAsync() throws IOException;

    public <Type1, Type2> void startTwinAsync(
        IotHubEventCallback twinStatusCallback, 
        Object twinStatusCallbackContext,
        PropertyCallback<Type1, Type2> genericPropertyCallback, 
        Object genericPropertyCallbackContext)
            throws IOException;

    public void startTwinAsync(
        IotHubEventCallback twinStatusCallback, 
        Object twinStatusCallbackContext,
        TwinPropertyCallback genericPropertyCallback, 
        Object genericPropertyCallbackContext)
            throws IOException;

    public void startTwinAsync(
        IotHubEventCallback twinStatusCallback, 
        Object twinStatusCallbackContext,
        TwinPropertiesCallback genericPropertiesCallback, 
        Object genericPropertyCallbackContext)
            throws IOException;

    public void subscribeToDesiredPropertiesAsync(Map<Property, Pair<PropertyCallback<String, Object>, Object>> onDesiredPropertyChange) throws IOException;

    public void subscribeToTwinDesiredPropertiesAsync(Map<Property, Pair<TwinPropertyCallback, Object>> onDesiredPropertyChange) throws IOException;

    public void sendReportedPropertiesAsync(Set<Property> reportedProperties) throws IOException;

    public void sendReportedPropertiesAsync(Set<Property> reportedProperties, int version) throws IOException;

    public void sendReportedPropertiesAsync(ReportedPropertiesParameters reportedPropertiesParameters) throws IOException;

    public void sendReportedPropertiesAsync(
        Set<Property> reportedProperties, 
        Integer version, 
        CorrelatingMessageCallback correlatingMessageCallback, 
        Object correlatingMessageCallbackContext, 
        IotHubEventCallback reportedPropertiesCallback, 
        Object reportedPropertiesCallbackContext) 
            throws IOException;

    public void subscribeToMethodsAsync(
        DeviceMethodCallback methodCallback, 
        Object methodCallbackContext,
        IotHubEventCallback methodStatusCallback, 
        Object methodStatusCallbackContext)
            throws IOException;
    
    public void setConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback callback, Object callbackContext);

    public void setRetryPolicy(RetryPolicy retryPolicy);

    public void setOperationTimeout(long timeout);

    public ProductInfo getProductInfo();

    public DeviceClientConfig getConfig();
       
    public boolean isMultiplexed();
}

public final class ModuleClient extends InternalClient
{
    public ModuleClient(String connectionString, IotHubClientProtocol protocol) throws URISyntaxException;

    public ModuleClient(String connectionString, IotHubClientProtocol protocol, ClientOptions clientOptions) throws URISyntaxException;

    public ModuleClient(String hostName, String deviceId, String moduleId, SasTokenProvider sasTokenProvider, IotHubClientProtocol protocol);

    public ModuleClient(String hostName, String deviceId, String moduleId, SasTokenProvider sasTokenProvider, IotHubClientProtocol protocol, ClientOptions clientOptions);

    public static ModuleClient createFromEnvironment(UnixDomainSocketChannel unixDomainSocketChannel) throws ModuleClientException;

    public static ModuleClient createFromEnvironment(UnixDomainSocketChannel unixDomainSocketChannel, IotHubClientProtocol protocol) throws ModuleClientException;

    public static ModuleClient createFromEnvironment(UnixDomainSocketChannel unixDomainSocketChannel, IotHubClientProtocol protocol, ClientOptions clientOptions) throws ModuleClientException;

    public void open(boolean withRetry) throws IOException;

    public void close();
    
    public void sendEventAsync(Message message, IotHubEventCallback callback, Object callbackContext, String outputName);

    public MethodResult invokeMethod(String deviceId, MethodRequest methodRequest) throws ModuleClientException;
    
    public MethodResult invokeMethod(String deviceId, String moduleId, MethodRequest methodRequest) throws ModuleClientException;

    public void getTwinAsync() throws IOException;

    public <Type1, Type2> void startTwinAsync(
        IotHubEventCallback twinStatusCallback, 
        Object twinStatusCallbackContext,
        PropertyCallback<Type1, Type2> genericPropertyCallback, 
        Object genericPropertyCallbackContext)
            throws IOException;

    public void startTwinAsync(
        IotHubEventCallback twinStatusCallback, 
        Object twinStatusCallbackContext,
        TwinPropertyCallback genericPropertyCallback, 
        Object genericPropertyCallbackContext)
            throws IOException;

    public void startTwinAsync(
        IotHubEventCallback twinStatusCallback, 
        Object twinStatusCallbackContext,
        TwinPropertiesCallback genericPropertiesCallback, 
        Object genericPropertyCallbackContext)
            throws IOException;

    public ModuleClient setMessageCallback(MessageCallback callback, Object context);

    public ModuleClient setMessageCallback(String inputName, MessageCallback callback, Object context);
    
    public void subscribeToDesiredPropertiesAsync(Map<Property, Pair<PropertyCallback<String, Object>, Object>> onDesiredPropertyChange) throws IOException;

    public void subscribeToTwinDesiredPropertiesAsync(Map<Property, Pair<TwinPropertyCallback, Object>> onDesiredPropertyChange) throws IOException;

    public void sendReportedPropertiesAsync(Set<Property> reportedProperties) throws IOException;

    public void sendReportedPropertiesAsync(Set<Property> reportedProperties, int version) throws IOException;

    public void sendReportedPropertiesAsync(ReportedPropertiesParameters reportedPropertiesParameters) throws IOException;

    public void sendReportedPropertiesAsync(
        Set<Property> reportedProperties, 
        Integer version, 
        CorrelatingMessageCallback correlatingMessageCallback, 
        Object correlatingMessageCallbackContext, 
        IotHubEventCallback reportedPropertiesCallback, 
        Object reportedPropertiesCallbackContext) 
            throws IOException;

    public void subscribeToMethodsAsync(
        DeviceMethodCallback methodCallback, 
        Object methodCallbackContext,
        IotHubEventCallback methodStatusCallback, 
        Object methodStatusCallbackContext)
            throws IOException;
    
    public void setConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback callback, Object callbackContext);
        
    public void setRetryPolicy(RetryPolicy retryPolicy);

    public void setOperationTimeout(long timeout);

    public ProductInfo getProductInfo();

    public DeviceClientConfig getConfig();
}

@Builder
public final class ClientOptions
{
    @Getter
    private final String modelId;

    @Getter
    private final SSLContext sslContext;

    @Getter
    private final ProxySettings proxySettings;

    @Getter
    @Builder.Default
    private final int keepAliveInterval = DEFAULT_KEEP_ALIVE_INTERVAL_IN_SECONDS;

    @Getter
    @Builder.Default
    private final int httpsReadTimeout = DEFAULT_HTTPS_READ_TIMEOUT_MILLISECONDS;

    @Getter
    @Builder.Default
    private final int httpsConnectTimeout = DEFAULT_HTTPS_CONNECT_TIMEOUT_MILLISECONDS;

    @Getter
    @Builder.Default
    private final long sasTokenExpiryTime = DEFAULT_SAS_TOKEN_EXPIRY_TIME_SECONDS;

    @Getter
    @Builder.Default
    private final int amqpAuthenticationSessionTimeout = DeviceClientConfig.DEFAULT_AMQP_OPEN_AUTHENTICATION_SESSION_TIMEOUT_IN_SECONDS;

    @Getter
    @Builder.Default
    private final int amqpDeviceSessionTimeout = DeviceClientConfig.DEFAULT_AMQP_OPEN_DEVICE_SESSIONS_TIMEOUT_IN_SECONDS;

    @Getter
    @Builder.Default
    private final int messagesSentPerSendInterval = DEFAULT_MAX_MESSAGES_TO_SEND_PER_THREAD;

    @Getter
    @Builder.Default
    private final int sendInterval = SEND_PERIOD_MILLIS;

    @Getter
    @Builder.Default
    private final int receiveInterval = RECEIVE_PERIOD_MILLIS;
}
```

</details>

<details>
  <summary>MultiplexingClient and Options</summary>

```java
public class MultiplexingClient
{
    public MultiplexingClient(String hostName, IotHubClientProtocol protocol);

    public MultiplexingClient(String hostName, IotHubClientProtocol protocol, MultiplexingClientOptions options);

    public void open() throws MultiplexingClientException;

    public void open(boolean withRetry) throws MultiplexingClientException;

    public void close() throws MultiplexingClientException;

    public void registerDeviceClient(DeviceClient deviceClient) throws InterruptedException, MultiplexingClientException ;
    	
    public void registerDeviceClient(DeviceClient deviceClient, long timeoutMilliseconds) throws InterruptedException, MultiplexingClientException;

    public void registerDeviceClients(Iterable<DeviceClient> deviceClients) throws InterruptedException, MultiplexingClientException;

    public void registerDeviceClients(Iterable<DeviceClient> deviceClients, long timeoutMilliseconds) throws InterruptedException, MultiplexingClientException;

    public void unregisterDeviceClient(DeviceClient deviceClient) throws InterruptedException, MultiplexingClientException;

    public void unregisterDeviceClient(DeviceClient deviceClient, long timeoutMilliseconds) throws InterruptedException, MultiplexingClientException;

    public void unregisterDeviceClients(Iterable<DeviceClient> deviceClients) throws InterruptedException, MultiplexingClientException;

    public void unregisterDeviceClients(Iterable<DeviceClient> deviceClients, long timeoutMilliseconds) throws InterruptedException, MultiplexingClientException;

    public void setConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback callback, Object callbackContext);

    public boolean isDeviceRegistered(String deviceId);

    public int getRegisteredDeviceCount();

    public void setRetryPolicy(RetryPolicy retryPolicy);
}

@Builder
public class MultiplexingClientOptions
{
    @Getter
    private final ProxySettings proxySettings;

    @Getter
    private final SSLContext sslContext;

    @Getter
    @Builder.Default
    private final long sendInterval = DEFAULT_SEND_PERIOD_MILLIS;

    @Getter
    @Builder.Default
    private final long receiveInterval = DEFAULT_RECEIVE_PERIOD_MILLIS;

    @Getter
    @Builder.Default
    private final int maxMessagesSentPerSendInterval = DEFAULT_MAX_MESSAGES_TO_SEND_PER_THREAD;

    @Getter
    @Builder.Default
    public final int keepAliveInterval = DEFAULT_KEEP_ALIVE_INTERVAL_IN_SECONDS;
}
```

</details>

<details>
  <summary>ServiceClient and Options</summary>

```java
public class ServiceClient
{
    public ServiceClient(
        String connectionString, 
        IotHubServiceClientProtocol iotHubServiceClientProtocol);

    public ServiceClient(
            String connectionString,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ServiceClientOptions options);

    public ServiceClient(
            String hostName,
            TokenCredential credential,
            IotHubServiceClientProtocol iotHubServiceClientProtocol);

    public ServiceClient(
            String hostName,
            TokenCredential credential,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ServiceClientOptions options);

    public ServiceClient(
            String hostName,
            AzureSasCredential azureSasCredential,
            IotHubServiceClientProtocol iotHubServiceClientProtocol);

    public ServiceClient(
            String hostName,
            AzureSasCredential azureSasCredential,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ServiceClientOptions options);

    public void open() throws IOException;

    public void close() throws IOException;

    public void send(String deviceId, Message message) throws IOException, IotHubException;

    public void send(String deviceId, String moduleId, Message message) throws IOException, IotHubException;

    public FeedbackReceiver getFeedbackReceiver(FeedbackMessageReceivedCallback feedbackMessageReceivedCallback);

    public FileUploadNotificationReceiver getFileUploadNotificationReceiver(FileUploadNotificationReceivedCallback fileUploadNotificationReceivedCallback);
}

@Builder
public class ServiceClientOptions
{
    @Getter
    private final ProxyOptions proxyOptions;

    @Getter
    private final SSLContext sslContext;
}

public interface FeedbackMessageReceivedCallback
{
    public IotHubMessageResult onFeedbackMessageReceived(FeedbackBatch feedbackBatch);
}

public class FeedbackReceiver
{
    public void open() throws IOException;

    public void close();
}

public interface FileUploadNotificationReceivedCallback
{
    public IotHubMessageResult onFileUploadNotificationReceived(FileUploadNotification notification);
}

public class FileUploadNotificationReceiver
{
    public void open() throws IOException;

    public void close();
}

```

</details>

<details>
  <summary>RegistryManager and Options</summary>

```java

```

</details>

<details>
  <summary>TwinClient and Options</summary>

```java

```

</details>


<details>
  <summary>MethodsClient and Options</summary>

```java

```

</details>

<details>
  <summary>JobsClient and Options</summary>

```java

```

</details>