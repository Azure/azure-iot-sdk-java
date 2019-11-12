# Building the Digital Twin Device SDK

## Initial development environment setup
To get setup to build applications that enable the Digital Twin for JAVA:


* Clone and perform initial on the GitHub repo containing the [IoTHub Device Client](https://github.com/Azure/azure-iot-sdk-java) preview SDK.
* Clone and perform initial on the GitHub repo containing the Digital Twin SDK.


```
 git clone -b version-change --recursive https://github.com/Azure/azure-iot-sdk-java.git
 cd azure-iot-sdk-java
 mvn install
 
 cd ..
 git clone https://github.com/Azure/azure-iot-sdk-java.git
```

Because Digital Twin feature works only with a preview API version, build and install SDK client to maven local is required.

## Initial project
* Open Intellij, File -> New Project -> Project from Existing Sources, select [pom.xml](../pom.xml) and select OK.
* Digital Twin project uses lombok. To setup lombok, follow steps below:
 
  * Install Intellij lombok plugin from files -> settings -> plugins
  * Enable annotation processing from files -> settings -> build, execution, deployment -> compiler -> annotation processors -> check "enable annotation processing"
  
## Build API doc
To get API doc, perform following command on the GitHub repo containing the Digital Twin SDK
```
cd azure-iot-sdk-java/digital-twin
mvn javadoc:javadoc
```

API doc are generated under /device/target/site/apidocs for device and /service/target/site/apidocs for service.