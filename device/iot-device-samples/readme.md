# Samples for the Azure IoT device SDK for Java

This folder contains simple samples showing how to use the various features of the Microsoft Azure IoT Hub service from a device running Java code.

## List of samples

* [Simple send sample](send-event): Shows how to connect and send messages to IoT Hub, passing the protocol of your choices as a parameter.
* [Simple send/receive sample](send-receive-sample): Shows how to connect then send and receive messages to and from IoT Hub, passing the protocol of your choices as a parameter.
* [Simple send serialized messages sample](send-serialized-event): Shows how to connect and send serialized messages to IoT Hub, passing the protocol of your choices as a parameter.
* [Simple sample handling messages received](handle-messages): : Shows how to connect to IoT Hub and manage messages received from IoT Hub, passing the protocol of your choices as a parameter.
* [Android sample](android-sample): Shows how to connect to IoT Hub send and receive messages to and from IoT Hub.

## How to run the samples on Linux or Windows

Note that the samples for Windows and Linux use Maven.

### Prerequisites
In order to run the device samples on Linux or Windows, you will first need the following prerequisites:
* [Setup your IoT hub][lnk-setup-iot-hub]
* [Provision your device and get its credentials][lnk-manage-iot-hub]

### Setup environment
Prepare your platform following the instructions [here][devbox-setup] to install Java and Maven.

### Get and run the samples
1. Clone the repository or download the sample (the one you want to try) project's folder on your device.
1. In a command line, navigate to the sample folder and build:
	```
	{sample root}/>mvn install -DskipTests
	```
1. Navigate to the folder containing the executable JAR file for the sample and run the sample as follows:

	For example, the executable JAR file for sending and receving event, **send-receive-sample** can be found at:

	```
	{sample root}/target/send-event-{version}-with-deps.jar
	```

	Navigate to the directory containing the jar. Run the sample using the following command:

	```
	java -jar ./send-receive-sample-{version}-with-deps.jar "{connection string}" "{number of requests to send}" "{https or amqps or mqtt or amqps_ws }"
	```

	Note that the double quotes around each argument are required, but the braces '{' and '}' should be removed.

### More details on command line arguments
Samples use the following command line arguments:
* [Device connection string] - `HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key>`
* [Number of requests to send]: For example, **5** 
* [`https | amqps | mqtt | amqps_ws`]: For example, amqps_ws (AMQP over WebSocket)
* [Path to certificate to enable 1-way authentication]: For example, `certs\ms.der` **optional argument**

Path to certificate is an **optional** argument and would be needed in case you want to point it to the local copy of the Server side certificate. Please note that this option is used by client for validating Root CA sent by Azure IoT Hub Server as part of TLS handshake. It is for 1-way TLS authentication and is **not** for specifying client side certificate (2-way TLS authentication).

## How to run the Android sample
1. Download and install [Android Studio][android-studio]
1. Load and build the sample located in device\samples\android-sample in Android Studio (you will need to clone or copy the repository)
1. Sample has dependency on the remote library `iot-device-client`. It is currently set to use the latest version of the library. If you want to choose a different version, please update `device\iot-device-samples\android-sample\app\build.gradle` file to point to the version you want to use. For list of available versions search [Maven Repository][maven-repository]

## Logging 
The java device client SDK uses the Apache [SLF4J][SLF4J-logging] logging facade. To enable logging in
your application, you will need to include a logging implementation dependency in your application to consume
this SDK's logging facade. An example of this can be found in the device client's "send receive sample" folder

In that folder, you will find the pom file declares a dependency on slf4j-simple which is a simple, non-configurable
example of one such consumer of the logging facade. When using this logger in particular, you will get all logs above
and including INFO level printed to your console. There are other possible consumers of SLF4J, and it is well documented
on their [website][SLF4J-logging]

[devbox-setup]: ../../doc/java-devbox-setup.md
[lnk-setup-iot-hub]: https://aka.ms/howtocreateazureiothub
[lnk-manage-iot-hub]: https://aka.ms/manageiothub
[android-studio]: https://developer.android.com/studio/index.html
[SLF4J-logging]: https://www.slf4j.org/
[log4j-properties]: https://logging.apache.org/log4j/1.2/manual.html