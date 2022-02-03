## Azure IoT SDK Logging

The Azure IoT SDK uses the [SLF4j](http://www.slf4j.org/faq.html) logging facade. In order to capture the SDK's logs, 
you will need to provide a consumer for this logging facade. As an example, the sample projects in this repo use
the library org.slf4j.slf4j-log4j12
      
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
            <version>1.7.29</version>
        </dependency>

Alongside the above dependency, you need to add log4j.properties files into your code's resources folder that
specify what package's logs will be captured, and at what log level. An example log4j.properties
file can be seen in a device client sample [here](device/iot-device-samples/send-event/src/main/resources/log4j.properties)

Note that the log4j.properties file requires you to explicitly choose which packages to collect logs from. Packages to collect
logs from must be specified in the properties file. Specified packages will include the logs
of any subpackages, but do not include the logs of their dependencies. As an example, adding the package 
```com.microsoft.azure.sdk.iot.service``` will collect logs from classes like ```com.microsoft.azure.sdk.iot.service.messaging.ServiceClient```, 
and will collect logs for classes like ```com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpSendHandler``` but will not
collect logs from a class of its dependency, such as ```com.microsoft.azure.sdk.iot.provisioning.device.transport.amqp.ErrorLoggingBaseHandler```

Adding these packages to your log4j.properties file should be sufficient to capture all logs that this SDK generates:
```
log4j.logger.com.microsoft.azure.sdk.iot.device = DEBUG 
log4j.logger.com.microsoft.azure.sdk.iot.service = DEBUG 
log4j.logger.com.microsoft.azure.sdk.iot.deps = DEBUG 
log4j.logger.com.microsoft.azure.sdk.iot.provisioning.device = DEBUG 
log4j.logger.com.microsoft.azure.sdk.iot.provisioning.service = DEBUG 
log4j.logger.com.microsoft.azure.sdk.iot.provisioning.security = DEBUG 
```

Or for simplicity:
```
log4j.logger.com.microsoft.azure.sdk.iot = DEBUG 
```

This SDK does print logs at TRACE, DEBUG, INFO, WARN, and ERROR levels, and your log4j.properties file
allows you to choose these levels for each package. For example, to get trace level logs in the deps package,
but info level logs in the device and service packages, your log4j.properties file would include

```
log4j.logger.com.microsoft.azure.sdk.iot.device = INFO 
log4j.logger.com.microsoft.azure.sdk.iot.service = INFO 
log4j.logger.com.microsoft.azure.sdk.iot.deps = TRACE 
```

Another notable logging consumer that works with SLF4j is [logback](http://logback.qos.ch/). For more information on 
using logback with SLF4j, see [this tutorial](https://mkyong.com/logging/slf4j-logback-tutorial/)

You can also make your existing logging framework compatible with SLF4j as documented [here](http://www.slf4j.org/faq.html#slf4j_compatible)



