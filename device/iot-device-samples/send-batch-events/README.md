# Send Events In Batch Sample 

Sample application that uses the Azure IoT Java SDK to send telemetry messages in batch to the
Azure IoT Hub cloud service or to an Azure IoT Edge device. The sample demonstrates how to connect
and send messages using a protocol of your choices as a parameter. However, currently only HTTPS messages will be sent in a batch request and MQTT and AMQP protocols will queue the messages individually.

## Build the sample

```
$> cd {sample root}
$> mvn install -DskipTests
```

## Run the sample

Listed below is the command to launch the sample along with a description of its arguments

```
$> cd {sample root}/send-batch-events
$> java -jar target/send-batch-events-{version}-with-deps.jar "{connection string}" {number of messages to send} {protocol}  {path to trusted root ca}
```

### Arguments Description

* Connection String:
  * IoT Hub connection string format:

    ```
    HostName=your-hub.azure-devices.net;DeviceId=yourDevice;SharedAccessKey=XXXYYYZZZ=;
    ```

  * IoT Edge connection string:

    ```
    HostName=your-hub.azure-devices.net;DeviceId=yourDevice;SharedAccessKey=XXXYYYZZZ=;GatewayHostName=mygateway.contoso.com
    ```

* Number of messages - Expressed in decimal
* Protocol - Choices are "mqtt", "https", "amqps", "amqps_ws", "mqtt_ws"
* Path to trusted CA certificate: This is optional for IoT Hub since it's certificate is signed by public root CA. For the Edge Hub, if the CA is not a public root, a path tp the root CA certificate in PEM format is absolutely required. This is required even if the root certificate is installed in the trusted certificate store of the OS.

Sample command invocation:

```
$> java -jar target/send-batch-events-1.14.0-with-deps.jar "{connection string}" 20 mqtt /home/user/ca_cert.pem
```

## Verify output

If everything was correctly provided via the CLI arguments, the following should be observed on stdout

```
...
Sending the following event messages in batch:
{"deviceId":"MyJavaDevice","messageId":0,"temperature":22.794061892854135,"humidity":37.62552699796588}
{"deviceId":"MyJavaDevice","messageId":1,"temperature":21.53579682739172,"humidity":37.074460372012084}
{"deviceId":"MyJavaDevice","messageId":2,"temperature":28.469394562293672,"humidity":48.85065152753948}
{"deviceId":"MyJavaDevice","messageId":3,"temperature":27.59171413398098,"humidity":33.7627855634459}
{"deviceId":"MyJavaDevice","messageId":4,"temperature":27.645685621530887,"humidity":49.181172831276676}
IoT Hub responded to message d630cd17-4b84-49f1-878a-901f53ac4038 with status OK_EMPTY
...
```
