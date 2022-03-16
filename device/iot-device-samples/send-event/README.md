# Send Event Sample

Sample application that uses the Azure IoT Java SDK to send telemetry messages to the
Azure IoT Hub cloud service or to an Azure IoT Edge device. The sample demonstrates how to connect
and send messages using a protocol of your choices as a parameter.

## Build the sample

```
$> cd {sample root}
$> mvn install -DskipTests
```

## Run the sample

Listed below is the command to launch the sample along with a description of its arguments

```
$> cd {sample root}/send-event
$> java -jar target/send-event-{version}-with-deps.jar [-c "{connection string}"] [-h] [-p {protocol}] [-pc {path to trusted root ca}] [-r {number of requests to send}]
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
* Path to trusted CA certificate: This is optional for IoT Hub since it's certificate is signed by public root CA. For the Edge Hub, if the CA is not a public root, a path to the root CA certificate in PEM format is absolutely required. This is required even if the root certificate is installed in the trusted certificate store of the OS.

Sample command invocation:

```
$> java -jar target/send-event-1.14.0-with-deps.jar -c "{connection string}" -r 20 -p mqtt -pc /home/user/ca_cert.pem
```

## Verify output

If everything was correctly provided via the CLI arguments, the following should be observed on stdout

```
...
IoT Hub responded to message d630cd17-4b84-49f1-878a-901f53ac4038 with status OK_EMPTY
IoT Hub responded to message e45e5998-5ea1-49bd-978c-5a149e2012e8 with status OK_EMPTY
IoT Hub responded to message f12eb242-b314-4ded-98a6-9f2229a9c2b7 with status OK_EMPTY
IoT Hub responded to message 033cc1fd-b3fe-43a8-b05a-5e1e188d2b8c with status OK_EMPTY
IoT Hub responded to message 1695f37f-455a-4386-a46b-57fe4e4fae44 with status OK_EMPTY
...
```
