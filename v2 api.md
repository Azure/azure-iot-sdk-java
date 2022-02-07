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
    @Setter
    @Getter
    private ConnectionLostCallback connectionLostCallback;
    
    public void open() throws IOException;

    public void close();
}

public interface FileUploadNotificationReceivedCallback
{
    public IotHubMessageResult onFileUploadNotificationReceived(FileUploadNotification notification);
}

public class FileUploadNotificationReceiver
{
    @Setter
    @Getter
    private ConnectionLostCallback connectionLostCallback;
    
    public void open() throws IOException;

    public void close();
}

```

</details>

<details>
  <summary>RegistryClient and Options</summary>

```java
public final class RegistryClient
{
    public RegistryClient(String connectionString);

    public RegistryClient(String connectionString, RegistryClientOptions options);

    public RegistryClient(String hostName, TokenCredential credential);

    public RegistryClient(String hostName, TokenCredential credential, RegistryClientOptions options);

    public RegistryClient(String hostName, AzureSasCredential azureSasCredential);

    public RegistryClient(String hostName, AzureSasCredential azureSasCredential, RegistryClientOptions options);

    public Device addDevice(Device device) throws IOException, IotHubException;

    public Device getDevice(String deviceId) throws IOException, IotHubException;

    public String getDeviceConnectionString(Device device);

    public Device updateDevice(Device device) throws IOException, IotHubException;

    public void removeDevice(String deviceId) throws IOException, IotHubException;

    public void removeDevice(Device device) throws IOException, IotHubException;

    public RegistryStatistics getStatistics() throws IOException, IotHubException;

    public Module addModule(Module module) throws IOException, IotHubException;

    public Module getModule(String deviceId, String moduleId) throws IOException, IotHubException;

    public List<Module> getModulesOnDevice(String deviceId) throws IOException, IotHubException;

    public Module updateModule(Module module) throws IOException, IotHubException;

    public void removeModule(String deviceId, String moduleId) throws IOException, IotHubException;

    public void removeModule(Module module) throws IOException, IotHubException;
}

@Builder
public final class RegistryClientOptions
{
    @Getter
    private final ProxyOptions proxyOptions;

    @Getter
    @Builder.Default
    private final int httpReadTimeout = DEFAULT_HTTP_READ_TIMEOUT_MS;

    @Getter
    @Builder.Default
    private final int httpConnectTimeout = DEFAULT_HTTP_CONNECT_TIMEOUT_MS;
}
```

</details>

<details>
  <summary>TwinClient and Options</summary>

```java

public final class TwinClient
{
    public TwinClient(String connectionString);

    public TwinClient(String connectionString, TwinClientOptions options);

    public TwinClient(String hostName, TokenCredential credential);

    public TwinClient(String hostName, TokenCredential credential, TwinClientOptions options);

    public TwinClient(String hostName, AzureSasCredential azureSasCredential);

    public TwinClient(String hostName, AzureSasCredential azureSasCredential, TwinClientOptions options);

    public Twin get(String deviceId) throws IotHubException, IOException;

    public Twin get(String deviceId, String moduleId) throws IotHubException, IOException;

    public void patch(Twin twin) throws IotHubException, IOException;

    public Twin replace(Twin twin) throws IotHubException, IOException;
}

@Builder
public final class TwinClientOptions
{
    @Getter
    private final ProxyOptions proxyOptions;

    @Getter
    @Builder.Default
    private final int httpReadTimeout = DEFAULT_HTTP_READ_TIMEOUT_MS;

    @Getter
    @Builder.Default
    private final int httpConnectTimeout = DEFAULT_HTTP_CONNECT_TIMEOUT_MS;
}


```

</details>


<details>
  <summary>MethodsClient and Options</summary>

```java

public final class DirectMethodsClient
{
    public DirectMethodsClient(String connectionString);

    public DirectMethodsClient(String connectionString, DirectMethodsClientOptions options);

    public DirectMethodsClient(String hostName, TokenCredential credential);

    public DirectMethodsClient(String hostName, TokenCredential credential, DirectMethodsClientOptions options);

    public DirectMethodsClient(String hostName, AzureSasCredential azureSasCredential);

    public DirectMethodsClient(String hostName, AzureSasCredential azureSasCredential, DirectMethodsClientOptions options);

    public MethodResult invoke(String deviceId, String methodName) throws IotHubException, IOException;

    public MethodResult invoke(String deviceId, String methodName, DirectMethodRequestOptions options) throws IotHubException, IOException;

    public MethodResult invoke(String deviceId, String moduleId, String methodName) throws IotHubException, IOException;

    public MethodResult invoke(String deviceId, String moduleId, String methodName, DirectMethodRequestOptions options) throws IotHubException, IOException;
}

@Builder
public final class DirectMethodsClientOptions
{
    @Getter
    private final ProxyOptions proxyOptions;

    @Getter
    @Builder.Default
    private final int httpReadTimeout = DEFAULT_HTTP_READ_TIMEOUT_MS;

    @Getter
    @Builder.Default
    private final int httpConnectTimeout = DEFAULT_HTTP_CONNECT_TIMEOUT_MS;
}

@Builder
public final class DirectMethodRequestOptions
{
    @Getter
    private final Object payload;

    @Getter
    @Builder.Default
    private final int methodResponseTimeout = 200;

    @Getter
    @Builder.Default
    private final int methodConnectTimeout = 200;
}

```

</details>

<details>
  <summary>JobsClient and Options</summary>

```java

public final class RegistryJobsClient
{
    public RegistryJobsClient(String connectionString);

    public RegistryJobsClient(String connectionString, RegistryJobsClientOptions clientOptions);

    public RegistryJobsClient(String hostName, TokenCredential credential);

    public RegistryJobsClient(String hostName, TokenCredential credential, RegistryJobsClientOptions clientOptions);

    public RegistryJobsClient(String hostName, AzureSasCredential azureSasCredential);

    public RegistryJobsClient(String hostName, AzureSasCredential azureSasCredential, RegistryJobsClientOptions clientOptions);

    public RegistryJob exportDevices(String exportBlobContainerUri, boolean excludeKeys) throws IOException, IotHubException;

    public RegistryJob exportDevices(RegistryJob exportDevicesParameters) throws IOException, IotHubException;

    public RegistryJob importDevices(String importBlobContainerUri, String outputBlobContainerUri) throws IOException, IotHubException;

    public RegistryJob importDevices(RegistryJob importDevicesParameters) throws IOException, IotHubException;

    public RegistryJob get(String jobId) throws IOException, IotHubException;
}

@Builder
public final class RegistryJobsClientOptions
{
    @Getter
    private final ProxyOptions proxyOptions;

    @Getter
    @Builder.Default
    private final int httpReadTimeout = DEFAULT_HTTP_READ_TIMEOUT_MS;

    @Getter
    @Builder.Default
    private final int httpConnectTimeout = DEFAULT_HTTP_CONNECT_TIMEOUT_MS;
}

public final class ScheduledJobsClient
{
    public ScheduledJobsClient(String connectionString);

    public ScheduledJobsClient(String connectionString, ScheduledJobsClientOptions clientOptions);

    public ScheduledJobsClient(String hostName, TokenCredential credential);

    public ScheduledJobsClient(String hostName, TokenCredential credential, ScheduledJobsClientOptions clientOptions);

    public ScheduledJobsClient(String hostName, AzureSasCredential azureSasCredential);

    public ScheduledJobsClient(String hostName, AzureSasCredential azureSasCredential, ScheduledJobsClientOptions clientOptions);

    public ScheduledJob scheduleUpdateTwin(String jobId, String queryCondition, Twin updateTwin, Date startTimeUtc, long maxExecutionTimeInSeconds)
            throws IOException, IotHubException;

    public ScheduledJob scheduleDirectMethod(String jobId, String queryCondition, String methodName, Date startTimeUtc)
            throws IOException, IotHubException;

    public ScheduledJob scheduleDirectMethod(String jobId, String queryCondition, String methodName, Date startTimeUtc, DirectMethodsJobOptions options)
            throws IOException, IotHubException;

    public ScheduledJob get(String jobId) throws IOException, IotHubException;

    public ScheduledJob cancel(String jobId) throws IOException, IotHubException;
}

@Builder
public final class ScheduledJobsClientOptions
{
    @Getter
    private final ProxyOptions proxyOptions;

    @Getter
    @Builder.Default
    private final int httpReadTimeout = DEFAULT_HTTP_READ_TIMEOUT_MS;

    @Getter
    @Builder.Default
    private final int httpConnectTimeout = DEFAULT_HTTP_CONNECT_TIMEOUT_MS;
}

```

</details>

<details>
  <summary>QueryClient and Options</summary>

```java

@Slf4j
public final class QueryClient
{
    public QueryClient(String connectionString);

    public QueryClient(String connectionString, QueryClientOptions options);

    public QueryClient(String hostName, TokenCredential credential);

    public QueryClient(String hostName, TokenCredential credential, QueryClientOptions options);

    public QueryClient(String hostName, AzureSasCredential azureSasCredential);

    public QueryClient(String hostName, AzureSasCredential azureSasCredential, QueryClientOptions options);

    public TwinQueryResponse queryTwins(String query) throws IOException, IotHubException;

    public TwinQueryResponse queryTwins(String query, QueryPageOptions options) throws IOException, IotHubException;

    public JobQueryResponse queryJobs(String query) throws IOException, IotHubException;

    public JobQueryResponse queryJobs(String query, QueryPageOptions options) throws IOException, IotHubException;

    public JobQueryResponse queryJobs(JobType jobType, JobStatus jobStatus) throws IOException, IotHubException;

    public JobQueryResponse queryJobs(JobType jobType, JobStatus jobStatus, QueryPageOptions options) throws IOException, IotHubException;

    public RawQueryResponse queryRaw(String query) throws IOException, IotHubException;

    public RawQueryResponse queryRaw(String query, QueryPageOptions options) throws IOException, IotHubException;
}

public class TwinQueryResponse
{
    @Getter
    String continuationToken = "";

    public boolean hasNext();

    public Twin next() throws IotHubException, IOException;

    public Twin next(QueryPageOptions pageOptions) throws IotHubException, IOException;
}

public class TwinQueryExample
{
    public void printTwins(String connectionString)
    {
        QueryClient queryClient = new QueryClient(connectionString);
        String twinQueryString = "SELECT * FROM devices";
        QueryPageOptions queryPageOptions = QueryPageOptions.builder().pageSize(20).build();
        TwinQueryResponse twinQueryResponse = queryClient.queryTwins(twinQueryString, queryPageOptions);
        
        while (twinQueryResponse.hasNext())
        {
            Twin queriedTwin = twinQueryResponse.next();
            System.out.println(queriedTwin);
        }
    }
}

@Builder
public final class QueryClientOptions
{
    @Getter
    private final ProxyOptions proxyOptions;

    @Getter
    @Builder.Default
    private final int httpReadTimeout = DEFAULT_HTTP_READ_TIMEOUT_MS;

    @Getter
    @Builder.Default
    private final int httpConnectTimeout = DEFAULT_HTTP_CONNECT_TIMEOUT_MS;
}

@Builder
public final class QueryPageOptions
{
    @Getter
    @Builder.Default
    private int pageSize = 50;

    @Getter
    private String continuationToken;
}

```

</details>

<details>
  <summary>ConfigurationsClient and Options</summary>

```java

public class ConfigurationsClient
{
    public ConfigurationsClient(String connectionString);

    public ConfigurationsClient(String connectionString, ConfigurationsClientOptions options);

    public ConfigurationsClient(String hostName, TokenCredential credential);

    public ConfigurationsClient(String hostName, TokenCredential credential, ConfigurationsClientOptions options);

    public ConfigurationsClient(String hostName, AzureSasCredential azureSasCredential);

    public ConfigurationsClient(String hostName, AzureSasCredential azureSasCredential, ConfigurationsClientOptions options);

    public Configuration create(Configuration configuration) throws IOException, IotHubException;

    public Configuration get(String configurationId) throws IOException, IotHubException;

    public List<Configuration> get(int maxCount) throws IOException, IotHubException;

    public Configuration replace(Configuration configuration) throws IOException, IotHubException;

    public void delete(String configurationId) throws IOException, IotHubException;

    public void delete(Configuration configuration) throws IOException, IotHubException;

    public void applyConfigurationContentOnDevice(String deviceId, ConfigurationContent content) throws IOException, IotHubException;
}

@Builder
public class ConfigurationsClientOptions
{
    @Getter
    private final ProxyOptions proxyOptions;

    @Getter
    @Builder.Default
    private final int httpReadTimeout = DEFAULT_HTTP_READ_TIMEOUT_MS;

    @Getter
    @Builder.Default
    private final int httpConnectTimeout = DEFAULT_HTTP_CONNECT_TIMEOUT_MS;
}

```

</details>