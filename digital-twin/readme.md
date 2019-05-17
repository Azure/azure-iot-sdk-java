# Azure IoT Digital Twin Client SDKs API design docs

The Azure IoT Digital Twin SDKs provide functionalites for devices and modules to connect to Azure IoT Digital Twin.

## Project setup
Digital Twin project is using lombok. To setup lombok, follow steps below:

1. Install Intellij lombok plugin from files -> settings -> plugins
2. Enable annotation processing from  files -> settings -> build, execution, deployment -> compiler -> annotation processors -> check "enable annotation processing"

## DigitalTwinDeviceClient

DigitalTwinDeviceClient stands for a DCM instance. It allocates resources for different components sharing with the safe transport layer, meanwhile exposes interface to register components, attach model definition handler, trigger Twin sync up and send device information. 

### Interfaces
    
Interfaces for a DigitalTwinDeviceClient is defined as following:

#### C

```c
```

#### C#

```csharp
```

#### Java

```java
void registerInterfaces(@NonNull String deviceCapabilityModelId, @Nullable List<DigitalTwinInterfaceClient> digitalTwinInterfaceClients) throws DigitalTwinException;
```

#### Node

```typescript
```

#### Python

```python
```

### Sample code

Following is the example to create a DCM client and connect to Azure IoT Hub:

#### C

```c
```

#### C#

```csharp
```

#### Java

```java
DcmClient dcmClient = DcmClient.builder()
                               .transport(transport)
                               .deviceId(DEVICE_ID)
                               // Nullable
                               .dcmId(DCM_ID)
                               // Nullable
                               .moduleId(MODULE_CLIENT)
                               .build();
dcmClient.connect();

// optional: register components
Map<String, String> components = new HashMap<>();
components.put(ENVIRONMENTAL_SENSOR_COMPONENT, ENVIRONMENTAL_SENSOR_INTERFACE);
dcmClient.registerComponents(components);

// optional: register model discovery handler
ModelDiscoveryHandler modelDiscoveryHandler = new MyModelDiscoveryHandler();
dcmClient.registerModelDiscoveryHandler(modelDiscoveryHandler);

// optional: send device information
DeviceInformation deviceInformation = DeviceInformation.builder().build();
dcmClient.sendDeviceInformation(deviceInformation);

// TODO: create IoTHub client used by regular SDK client 
IoTHubClient sdkClient = dcmClient.attachSdkClient();

// create Digital Twin client that used by specified component
DigitalTwinClient digitalTwinClient = dcmClient.attachDigitalTwinClient(COMPONENT_NAME, COMPONENT_INTERFACE);
// Application developer implements user friendly interface using DigitalTwinClient
ComponentInterface componentInterface = new ComponentInterface(digitalTwinClient);
// trigger twin sync up for all components and regular SDK client
dcmClient.getTwin();

// detach will disconnect specified client from transport layer
dcmClient.detachClient(sdkClient);
dcmClient.detachClient(digitalTwinClient);

// disconnect will disconnect transport layer from cloud
dcmClient.disconnect();
```

#### Node

```typescript
```

#### Python

```python
```

## IoTHubClient

IoTHubClient provides functionalities for regular SDK client. 

### Interfaces
    
Interfaces for a IoTHubClient is defined as following:

#### C

```c
```

#### C#

```csharp
```

#### Java

```java
    // Telemetry D2C
    void sendTelemetry(@NonNull TelemetryMessage telemetryMessage) throws IoTException;
    void sendTelemetry(@NonNull TelemetryMessage telemetryMessage, @NonNull RetryPolicy<Void> operationRetryPolicy) throws IoTException;
    // C2D message listener
    void registerTelemetryHandler(@NonNull TelemetryHandler telemetryHandler) throws IoTException;
    void registerTelemetryHandler(@NonNull TelemetryHandler telemetryHandler, @NonNull RetryPolicy<Void> operationRetryPolicy) throws IoTException;
    void unregisterTelemetryHandler() throws IoTException;
    void unregisterTelemetryHandler(@NonNull RetryPolicy<Void> operationRetryPolicy) throws IoTException;
    // Twin reported property
    void sendReportedState(@NonNull TwinReportRequest twinReportedProperty) throws IoTException;
    void sendReportedState(@NonNull TwinReportRequest twinReportedProperty, @NonNull RetryPolicy<Void> operationRetryPolicy) throws IoTException;
    // Twin desired property listener
    void registerTwinHandler(@NonNull TwinHandler twinHandler) throws IoTException;
    void registerTwinHandler(@NonNull TwinHandler twinHandler, @NonNull RetryPolicy<Void> operationRetryPolicy) throws IoTException;
    void unregisterTwinHandler() throws IoTException;
    void unregisterTwinHandler(@NonNull RetryPolicy<Void> operationRetryPolicy) throws IoTException;
    // Method
    void registerCommandHandler(@NonNull String commandName, @NonNull CommandHandler commandHandler) throws IoTException;
    void registerCommandHandler(@NonNull String commandName, @NonNull CommandHandler commandHandler, @NonNull RetryPolicy<Void> operationRetryPolicy) throws IoTException;
    void unregisterCommandHandler(@NonNull String commandName) throws IoTException;
    void unregisterCommandHandler(@NonNull String commandName, @NonNull RetryPolicy<Void> operationRetryPolicy) throws IoTException;
```

#### Node

```typescript
```

#### Python

```python
```

## DigitalTwinClient

DigitalTwinClient provides functionalities for DigitalTwin.

### Interfaces
    
DigitalTwinClient extends all interfaces from IoTHubClient and extra as following:

#### C

```c
```

#### C#

```csharp
```

#### Java

```java
// Async command response
void sendAsyncCommandUpdate(@NonNull AsyncCommandResponse asyncCommandResponse) throws IoTException;
void sendAsyncCommandUpdate(@NonNull AsyncCommandResponse asyncCommandMesage, @NonNull RetryPolicy<Void> operationRetryPolicy) throws IoTException;
```

#### Node

```typescript
```

#### Python

```python
```