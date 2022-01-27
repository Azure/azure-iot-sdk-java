<details>
  <summary>DeviceClient, ModuleClient, and Options</summary>
  
```java
public final class DeviceClient extends InternalClient implements Closeable
{
    @Deprecated
    public static final String HOSTNAME_ATTRIBUTE = "HostName=";

    @Deprecated
    public static final String DEVICE_ID_ATTRIBUTE = "DeviceId=";

    @Deprecated
    public static final String SHARED_ACCESS_KEY_ATTRIBUTE = "SharedAccessKey=";

    @Deprecated
    public static final String SHARED_ACCESS_TOKEN_ATTRIBUTE = "SharedAccessSignature=";

    @Deprecated
    public static final Charset CONNECTION_STRING_CHARSET = StandardCharsets.UTF_8;

    @Deprecated
    public static long SEND_PERIOD_MILLIS = 10L;

    @Deprecated
    public static long RECEIVE_PERIOD_MILLIS_AMQPS = 10L;
    
    @Deprecated
    public static long RECEIVE_PERIOD_MILLIS_MQTT = 10L;
	
    @Deprecated
    public static long RECEIVE_PERIOD_MILLIS_HTTPS = 25*60*1000; /*25 minutes*/

    @Deprecated
    public DeviceClient(String connString, TransportClient transportClient) throws URISyntaxException;

    public DeviceClient(String connString, IotHubClientProtocol protocol) throws URISyntaxException;

    public DeviceClient(String connString, IotHubClientProtocol protocol, ClientOptions clientOptions) throws URISyntaxException;

    public DeviceClient(String hostName, String deviceId, SasTokenProvider sasTokenProvider, IotHubClientProtocol protocol);

    public DeviceClient(String hostName, String deviceId, SasTokenProvider sasTokenProvider, IotHubClientProtocol protocol, ClientOptions clientOptions);

    @Deprecated
    public DeviceClient(String connString, IotHubClientProtocol protocol, String publicKeyCertificate, boolean isCertificatePath, String privateKey, boolean isPrivateKeyPath) throws URISyntaxException;

    @Deprecated
    public DeviceClient(String connString, IotHubClientProtocol protocol, SSLContext sslContext) throws URISyntaxException;

    public static DeviceClient createFromSecurityProvider(String uri, String deviceId, SecurityProvider securityProvider, IotHubClientProtocol protocol) throws URISyntaxException, IOException;

    public static DeviceClient createFromSecurityProvider(String uri, String deviceId, SecurityProvider securityProvider, IotHubClientProtocol protocol, ClientOptions clientOptions) throws URISyntaxException, IOException;

    public void open() throws IOException;

    public void open(boolean withRetry) throws IOException;

    @Deprecated
    public void close() throws IOException;

    public void closeNow() throws IOException;

    @Deprecated
    public void uploadToBlobAsync(
		String destinationBlobName, 
		InputStream inputStream, 
		long streamLength,
        IotHubEventCallback callback, 
		Object callbackContext) 
			throws IOException;

    public FileUploadSasUriResponse getFileUploadSasUri(FileUploadSasUriRequest request) throws IOException, URISyntaxException;

    @Deprecated
    public void completeFileUploadAsync(FileUploadCompletionNotification notification) throws IOException;

    public void completeFileUpload(FileUploadCompletionNotification notification) throws IOException;

    public void sendEventAsync(Message message, IotHubEventCallback callback, Object callbackContext);

    public void sendEventBatchAsync(List<Message> messages, IotHubEventCallback callback, Object callbackContext);

    public DeviceClient setMessageCallback(MessageCallback callback, Object context);

    public void getDeviceTwin() throws IOException;

    public <Type1, Type2> void startDeviceTwin(
        IotHubEventCallback deviceTwinStatusCallback, 
        Object deviceTwinStatusCallbackContext,
        PropertyCallBack<Type1, Type2> genericPropertyCallBack, 
        Object genericPropertyCallBackContext)
            throws IOException;

    public void startDeviceTwin(
        IotHubEventCallback deviceTwinStatusCallback, 
        Object deviceTwinStatusCallbackContext,
        TwinPropertyCallBack genericPropertyCallBack, 
        Object genericPropertyCallBackContext)
            throws IOException;

    public void startDeviceTwin(
        IotHubEventCallback deviceTwinStatusCallback, 
        Object deviceTwinStatusCallbackContext,
        TwinPropertiesCallback genericPropertiesCallBack, 
        Object genericPropertyCallBackContext)
            throws IOException;

    @Deprecated
    public void registerConnectionStateCallback(IotHubConnectionStateCallback callback, Object callbackContext);

    public void subscribeToDesiredProperties(Map<Property, Pair<PropertyCallBack<String, Object>, Object>> onDesiredPropertyChange) throws IOException;

    public void subscribeToTwinDesiredProperties(Map<Property, Pair<TwinPropertyCallBack, Object>> onDesiredPropertyChange) throws IOException;

    public void sendReportedProperties(Set<Property> reportedProperties) throws IOException;

    public void sendReportedProperties(Set<Property> reportedProperties, int version) throws IOException;

    public void sendReportedProperties(ReportedPropertiesParameters reportedPropertiesParameters) throws IOException;

    public void sendReportedProperties(
        Set<Property> reportedProperties, 
        Integer version, 
        CorrelatingMessageCallback correlatingMessageCallback, 
        Object correlatingMessageCallbackContext, 
        IotHubEventCallback reportedPropertiesCallback, 
        Object reportedPropertiesCallbackContext) 
			throws IOException;

    public void subscribeToDeviceMethod(
        DeviceMethodCallback methodCallback, 
        Object deviceMethodCallbackContext,
        IotHubEventCallback deviceMethodStatusCallback, 
        Object deviceMethodStatusCallbackContext)
            throws IOException;

    public void registerConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback callback, Object callbackContext);

    public void setRetryPolicy(RetryPolicy retryPolicy);

    public void setOperationTimeout(long timeout);

    public ProductInfo getProductInfo();

    public DeviceClientConfig getConfig();

    public void setOption(String optionName, Object value);
	
    public void setProxySettings(ProxySettings proxySettings);

    public boolean isMultiplexed();
}

public final class ModuleClient extends InternalClient
{
    public ModuleClient(String connectionString, IotHubClientProtocol protocol) throws ModuleClientException, URISyntaxException;
    
    public ModuleClient(String connectionString, IotHubClientProtocol protocol, ClientOptions clientOptions) throws ModuleClientException, URISyntaxException;
    
    @Deprecated
    public ModuleClient(String connectionString, IotHubClientProtocol protocol, String publicKeyCertificate, boolean isCertificatePath, String privateKey, boolean isPrivateKeyPath) throws ModuleClientException, URISyntaxException;
    
    @Deprecated
    public ModuleClient(String connectionString, IotHubClientProtocol protocol, SSLContext sslContext) throws ModuleClientException, URISyntaxException;
    
    public ModuleClient(String hostName, String deviceId, String moduleId, SasTokenProvider sasTokenProvider, IotHubClientProtocol protocol);
    
    public ModuleClient(String hostName, String deviceId, String moduleId, SasTokenProvider sasTokenProvider, IotHubClientProtocol protocol, ClientOptions clientOptions);
    
    public static ModuleClient createFromEnvironment() throws ModuleClientException;
    
    public static ModuleClient createFromEnvironment(IotHubClientProtocol protocol) throws ModuleClientException;
    
    public static ModuleClient createFromEnvironment(IotHubClientProtocol protocol, ClientOptions clientOptions) throws ModuleClientException;
    
    public void open() throws IOException;
    
    public void open(boolean withRetry) throws IOException;

    public void close();
    
    public void sendEventAsync(Message message, IotHubEventCallback callback, Object callbackContext, String outputName);

    public MethodResult invokeMethod(String deviceId, MethodRequest methodRequest) throws ModuleClientException;
    
    public MethodResult invokeMethod(String deviceId, String moduleId, MethodRequest methodRequest) throws ModuleClientException;
    
    public void getTwin() throws IOException;
    
    public <Type1, Type2> void startTwin(
        IotHubEventCallback deviceTwinStatusCallback, 
        Object deviceTwinStatusCallbackContext,
        PropertyCallBack<Type1, Type2> genericPropertyCallBack, 
        Object genericPropertyCallBackContext)
            throws IOException;
	
    public void startTwin(
        IotHubEventCallback deviceTwinStatusCallback, 
        Object deviceTwinStatusCallbackContext,
        TwinPropertyCallBack genericPropertyCallBack, 
        Object genericPropertyCallBackContext)
		    throws IOException;
    
    public void startTwin(
        IotHubEventCallback deviceTwinStatusCallback, 
        Object deviceTwinStatusCallbackContext,
        TwinPropertiesCallback genericPropertiesCallBack, 
        Object genericPropertyCallBackContext)
            throws IOException;
    
    public void subscribeToMethod(
        DeviceMethodCallback methodCallback, 
        Object methodCallbackContext,
        IotHubEventCallback methodStatusCallback, 
        Object methodStatusCallbackContext)
            throws IOException;
    
    public ModuleClient setMessageCallback(MessageCallback callback, Object context);
	
    public ModuleClient setMessageCallback(String inputName, MessageCallback callback, Object context);
	
    public void subscribeToDeviceMethod(
		DeviceMethodCallback methodCallback, 
		Object deviceMethodCallbackContext,
        IotHubEventCallback deviceMethodStatusCallback, 
		Object deviceMethodStatusCallbackContext)
            throws IOException;
	
	public void sendEventAsync(Message message, IotHubEventCallback callback, Object callbackContext);

    public void sendEventBatchAsync(List<Message> messages, IotHubEventCallback callback, Object callbackContext);

    public void subscribeToDesiredProperties(Map<Property, Pair<PropertyCallBack<String, Object>, Object>> onDesiredPropertyChange) throws IOException;

    public void subscribeToTwinDesiredProperties(Map<Property, Pair<TwinPropertyCallBack, Object>> onDesiredPropertyChange) throws IOException;

    public void sendReportedProperties(Set<Property> reportedProperties) throws IOException;

    public void sendReportedProperties(Set<Property> reportedProperties, int version) throws IOException;

    public void sendReportedProperties(ReportedPropertiesParameters reportedPropertiesParameters) throws IOException;

    public void sendReportedProperties(
		Set<Property> reportedProperties, 
		Integer version, 
		CorrelatingMessageCallback correlatingMessageCallback, 
		Object correlatingMessageCallbackContext, 
		IotHubEventCallback reportedPropertiesCallback, 
		Object reportedPropertiesCallbackContext) 
			throws IOException;

    public void registerConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback callback, Object callbackContext);

    public void setRetryPolicy(RetryPolicy retryPolicy);

    public void setOperationTimeout(long timeout);

    public ProductInfo getProductInfo();

    public DeviceClientConfig getConfig();

    public void setOption(String optionName, Object value);
	
    public void setProxySettings(ProxySettings proxySettings);

    public boolean isMultiplexed();
}

@Builder
public final class ClientOptions
{
    @Getter
    private final String modelId;

    @Getter
    private final SSLContext sslContext;

    @Getter
    @Builder.Default
    private final int keepAliveInterval = DEFAULT_KEEP_ALIVE_INTERVAL_IN_SECONDS;
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

    public void registerConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback callback, Object callbackContext);

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
    private final long sendPeriod = DEFAULT_SEND_PERIOD_MILLIS;

    @Getter
    @Builder.Default
    private final long receivePeriod = DEFAULT_RECEIVE_PERIOD_MILLIS;

    @Getter
    @Builder.Default
    private final int maxMessagesSentPerSendThread = DEFAULT_MAX_MESSAGES_TO_SEND_PER_THREAD;

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
	@Deprecated
    public static ServiceClient createFromConnectionString(
            String connectionString,
            IotHubServiceClientProtocol iotHubServiceClientProtocol)
				throws IOException;

    @Deprecated
    public static ServiceClient createFromConnectionString(
            String connectionString,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ServiceClientOptions options)
				throws IOException;
			
	public ServiceClient(String connectionString, IotHubServiceClientProtocol iotHubServiceClientProtocol);
	
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
	
	@Deprecated
    public CompletableFuture<Void> openAsync();
	
	@Deprecated
    public CompletableFuture<Void> closeAsync();
	
	@Deprecated
    public CompletableFuture<Void> sendAsync(String deviceId, Message message);
	
	@Deprecated 
	public FeedbackReceiver getFeedbackReceiver(String deviceId);
	
	public FeedbackReceiver getFeedbackReceiver();
	
	public FileUploadNotificationReceiver getFileUploadNotificationReceiver();
}

@Builder
public class ServiceClientOptions
{
    @Getter
    private final ProxyOptions proxyOptions;

    @Getter
    private final SSLContext sslContext;
}

public class FeedbackReceiver
{
	@Deprecated
    public FeedbackReceiver(
		String hostName, 
		String userName, 
		String sasToken, 
		IotHubServiceClientProtocol iotHubServiceClientProtocol, 
		String deviceId);
	    
	public FeedbackReceiver(
		String hostName, 
		String userName, 
		String sasToken, 
		IotHubServiceClientProtocol iotHubServiceClientProtocol);
    
	public FeedbackReceiver(
		String hostName, 
		String userName, 
		String sasToken, 
		IotHubServiceClientProtocol iotHubServiceClientProtocol, 
		ProxyOptions proxyOptions);
    
	public FeedbackReceiver(
            String hostName,
            String userName,
            String sasToken,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ProxyOptions proxyOptions,
            SSLContext sslContext);
			
    public FeedbackReceiver(
            String hostName,
            String userName,
            String sasToken,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ProxyOptions proxyOptions,
            SSLContext sslContext);
			
    public void open() throws IOException;

    public void close() throws IOException;

    public FeedbackBatch receive() throws IOException;

    public FeedbackBatch receive(long timeoutMs) throws IOException;
	
	public CompletableFuture<Void> openAsync();
	
	public CompletableFuture<Void> closeAsync();
	
	public CompletableFuture<FeedbackBatch> receiveAsync();
	
	public CompletableFuture<FeedbackBatch> receiveAsync(long timeoutMs);
}

public class FileUploadNotificationReceiver
{
    public void open();

    public void close();

    public FileUploadNotification receive() throws IOException;

    public FileUploadNotification receive(long timeoutMs) throws IOException;
	
	public CompletableFuture<Void> openAsync();
	
	public CompletableFuture<Void> closeAsync();
	
	public CompletableFuture<FileUploadNotification> receiveAsync();
	
	public CompletableFuture<FileUploadNotification> receiveAsync(long timeoutMs);
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