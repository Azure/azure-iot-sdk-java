# Prepare your development environment

This document describes how to prepare your development environment to use the *Microsoft Azure IoT device SDK for Java*.

* [Java JDK SE](#installjava)
* [Maven 3](#installmaven)
* [Azure IoT device SDK for Java](#installiot)
	* [Build from source](#installiotsource)
	* [Include using Maven](#installiotmaven)
* [Build for Android device](#installiotandroid)
* [Application Samples](#samplecode)

<a name="installjava"></a>
## Install Java JDK SE
To use the SDK and run the samples you will need **Java SE 8**.

### Windows
For downloads and installation instructions go [here](http://www.oracle.com/technetwork/java/javase/downloads/index.html).

#### Set up environment variables
* Please make sure that the `PATH` environment variable includes the full path to the `jdk1.8.x\bin` directory. (Example: C:\\Program Files\\Java\\jdk1.8.0_60\\bin)
* Please make sure that the `JAVA_HOME` environment variable includes the full path to the `jdk1.8.x` directory. (Example: JAVA_HOME=C:\\Program Files\\Java\\jdk1.8.0_60)

You can change your environment variables by going to windows->view advanced system settings->environment variables

You can test whether your `PATH` variable is set correctly by restarting your console and running `java -version`.

### Linux
**Note:** If you are running a version of Ubuntu below 14.10, you must run the command shown below to add the repository that contains the **openjdk-8-jdk** package to Ubuntu's list of software sources before you attempt to use the **apt-get** command to install openjdk-8-jdk:

```
sudo add-apt-repository ppa:openjdk-r/ppa
```
On Linux, the Java OpenJDK 8 can be installed as follows:
```
sudo apt-get update
sudo apt-get install openjdk-8-jdk
```

#### Set up environment variables
* Please make sure that the `PATH` environment variable includes the full path to the bin folder containing java.
```
which java
echo $PATH
```
Ensure that the bin directory shown by the ```which java``` command matches one of the directories shown in your $PATH variable.
If it does not:
```
export PATH=/path/to/java/bin:$PATH
```

* Please make sure that the `JAVA_HOME` environment variable includes the full path to the jdk.
```
update-alternatives --config java
```
Take note of the jdk location. ```update-alternatives``` will show something similar to ***/usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java***. The jdk directory would then be ***/usr/lib/jvm/java-8-openjdk-amd64/***.

```
export JAVA_HOME=/path/to/jdk
```

<a name="installmaven"></a>
## Install Maven
Using **_Maven 3_** is the recommended way to install **Azure IoT SDKs for Java**.

### on Windows
For downloads and installation instructions go here: https://maven.apache.org/download.cgi

**Set up environment variables**: make sure that the `PATH` environment variable includes the full path to the `apache-maven-3.x.x\bin` directory. (Example: F:\\Setups\\apache-maven-3.3.3\\bin). The `apache-maven-3.x.x` directory is where Maven 3 is installed.
You can verify that the environment variables necessary to run **_Maven 3_** have been set correctly by restarting your console and running `mvn --version`.

### on Linux
In a shell, type the following commands:
```
sudo apt-get update
sudo apt-get install maven
```
**Set up environment variables**: ensure the `PATH` environment variable contains the full path to the bin folder containing **_Maven 3_**.
```
which mvn
echo $PATH
```
Ensure that the bin directory shown by the ```which mvn``` command matches one of the directories shown in your $PATH variable.
If it does not:
```
export PATH=/path/to/mvn/bin:$PATH
```
You can verify that the environment variables necessary to run **_Maven 3_** have been set correctly by running
```
mvn --version
```
<a name="installiot"></a>
## Install Azure IoT device and service SDKs for Java

There are two ways to get the .jar libraries for the Azure IoT device and service SDKs:
* Include the project as a dependency in your project if your project is a Maven project.
* Download the source code and build on your machine

<a name="installiotmaven"></a>
### Get Azure IoT SDKs for Java from Maven (as a dependency)
_This is the recommended method of including the Azure IoT SDKs in your project, however this method will only work if your project is a Maven project and if you have gone through the setup described above_

#### for the Device SDK
* Navigate to http://search.maven.org, search for **com.microsoft.azure.sdk.iot** and take note of the latest version number (or the version number of whichever version of the sdk you desire to use).
* In your main pom.xml file, add the Azure IoT Device SDK as a dependency using your desired version as follows:
```xml
	<dependency>
		<groupId>com.microsoft.azure.sdk.iot</groupId>
		<artifactId>iot-device-client</artifactId>
		<version>1.14.1</version>
		<!--This is the current version number as of the writing of this document. Yours may be different.-->
	</dependency>
```
#### for the Service SDK
* Navigate to http://search.maven.org, search for **com.microsoft.azure.sdk.iot** and take note of the latest version number of the service client (or the version number of whichever version of the sdk you desire to use).
* In your main pom.xml file, add the Azure IoT Service SDK as a dependency using your desired version as follows:
```xml
	<dependency>
		<groupId>com.microsoft.azure.sdk.iot</groupId>
		<artifactId>iot-service-client</artifactId>
		<version>1.15.1</version>
		<!--This is the current version number as of the writing of this document. Yours may be different.-->
	</dependency>
```
<a name="installiotsource"></a>
### Build Azure IoT device and service SDKs for Java from the source code
* Get a copy of the **Azure IoT SDK for Java** from GitHub (current repo). You should fetch a copy of the source from the **master** branch of the GitHub repository: <https://github.com/Azure/azure-iot-sdk-java>
```
	git clone https://github.com/Azure/azure-iot-sdk-java.git
```
* When you have obtained a copy of the source, you can build the SDKs for Java.

### for the device SDK
Open a command prompt and use the following commands:
```
	cd azure-iot-sdk-java/device
	mvn install
```
The compiled JAR file with all dependencies bundled in can then be found at:
```
{IoT SDK for Java root}/device/iot-device-client/target/iot-device-client-{version}.jar
```
When you're ready to use the Java device SDK in your own project, include this JAR file in your project, as well as any JAR files that the device sdk depends on.

### for the service SDK
Open a command prompt and use the following commands:
```
	cd azure-iot-sdk-java/service
	mvn install
```
The compiled JAR file can then be found at:
```
{IoT SDK for Java root}/service/iot-service-client/target/iot-service-client-{version}.jar
```
When you're ready to use the Java service SDK in your own project, include this JAR file in your project, as well as any JAR files that the service sdk depends on

<a name="installiotandroid"></a> 
## Building for Android Device
- Download and install [Android Studio][android-studio]
- Load and build **sample** located at java\device\samples\android-sample
- Sample has dependence on remote library `iot-device-client`. It is currently set to use the latest version of the library. If you want to choose a different version, please update `device\samples\android-sample\app\build.gradle` file to point to the version you want to use. For list of available versions search [Maven Repository][maven-repository]

<a name="samplecode"></a>
## Sample applications

This repository contains various [simple sample applications][device-samples] that illustrate how to use the Microsoft Azure IoT device SDK for Java.

[device-samples]: ../device/iot-device-samples/
[android-studio]: https://developer.android.com/studio/index.html
[certify-iot-device-android]:https://github.com/Azure/azure-iot-sdks/blob/master/doc/iotcertification/iot_certification_android_java/iot_certification_android_java.md
[maven-repository]:http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure.sdk.iot%22


