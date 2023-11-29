## Azure IoT SDK Logging

The Azure IoT SDK uses the [SLF4j](http://www.slf4j.org/faq.html) logging facade. In order to capture the SDK's logs, 
you will need to provide a consumer for this logging facade. As an example, the sample projects in this repo use
the library org.apache.logging.log4j.log4j-slf4j-impl

```xml
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-api</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-core</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-slf4j-impl</artifactId>
</dependency>
```

Alongside the above dependencies, you need to add log4j2.properties files into your code's resources folder that
specify what package's logs will be captured, and at what log level. An example log4j2.properties
file can be seen in a device client sample [here](iothub/device/iot-device-samples/send-event/src/main/resources/log4j2.properties)

Note that the log4j2.properties file requires you to explicitly choose which packages to collect logs from. Packages to collect
logs from must be specified in the properties file. Specified packages will include the logs
of any subpackages, but do not include the logs of their dependencies. As an example, adding the package 
```com.microsoft.azure.sdk.iot.service``` will collect logs from classes like ```com.microsoft.azure.sdk.iot.service.messaging.MessagingClient```, 
and will collect logs for classes like ```com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpSendHandler``` but will not
collect logs from any class outside of this namespace.

Adding these packages to your log4j2.properties file should be sufficient to capture all logs that this SDK generates:
```
logger.sdk.device.name = com.microsoft.azure.sdk.iot.device
logger.sdk.device.level = INFO

logger.sdk.device.name = com.microsoft.azure.sdk.iot.service
logger.sdk.service.level = INFO

logger.sdk.provisioningDevice.name = com.microsoft.azure.sdk.iot.provisioning.device
logger.sdk.provisioningDevice.level = INFO

logger.sdk.provisioningService.name = com.microsoft.azure.sdk.iot.provisioning.service
logger.sdk.provisioningService.level = INFO

logger.sdk.provisioningService.name = com.microsoft.azure.sdk.iot.provisioning.security
logger.sdk.provisioningSecurity.level = INFO 
```

Or for simplicity:
```
logger.sdk.device.name = com.microsoft.azure.sdk.iot
logger.sdk.device.level = INFO
```

This SDK does print logs at TRACE, DEBUG, INFO, WARN, and ERROR levels, and your log4j.properties file
allows you to choose these levels for each package. For example, to get trace level logs in the service client package,
but info level logs in the device client package, your log4j.properties file would include

```
logger.sdk.device.name = com.microsoft.azure.sdk.iot.device
logger.sdk.device.level = INFO

logger.sdk.device.name = com.microsoft.azure.sdk.iot.service
logger.sdk.service.level = TRACE
```

Another notable logging consumer that works with SLF4j is [logback](http://logback.qos.ch/). For more information on 
using logback with SLF4j, see [this tutorial](https://mkyong.com/logging/slf4j-logback-tutorial/)

You can also make your existing logging framework compatible with SLF4j as documented [here](http://www.slf4j.org/faq.html#slf4j_compatible)



