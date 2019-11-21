# Building the Digital Twin Device SDK

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
