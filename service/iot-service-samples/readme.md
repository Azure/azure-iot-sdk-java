# Samples for the Azure IoT Service SDK for Java

This folder contains simple samples showing how to use the various features of the Microsoft Azure IoT Hub service from a back-end application running Java code.

## List of samples

* [Device manager sample](device-manager-sample): Shows how to work with the device ID registry of IoT Hub. 
* [Service client sample](service-client-sample): Shows how to send Cloud to Device messages through IoT Hub. 

## How to run the samples on Linux or Windows

Note that the below samples use Maven.

<a name="prerequisites"></a>
### Prerequisites
In order to run the device samples on Linux or Windows, you will first need the following prerequisites:
* [Setup your IoT hub][lnk-setup-iot-hub]
* After creating the IoT Hub, retreive the iothubowner connection string (mentioned in instructions to create the hub).

### Setup environment
[Prepare your platform following the instructions here to install Java and Maven][devbox-setup]

### Get and run the samples
You need to first clone the repository or download the sample project folder on your machine.

#### Build and run the Device Manager Sample application:
1. Preparing the Device Manager Sample application:
   1. Navigate to the main sample file for Device Manager
   It can be found at:
   {IoT Java SDK root}\service\iot-service-samples\device-manager-sample\src\main\java\samples\com\microsoft\azure\sdk\iot\service\sdk\SampleUtils.java
   2. Locate the following code in the file:
	```java
	public static final String iotHubConnectionString = "[sample iot hub connection string goes here]";
	public static final String storageConnectionString = "[sample storage connection string goes here]";
	public static final String deviceId = "[Device name goes here]";
	public static final String exportFileLocation = "[Insert local folder here - something like C:\\foldername\\]";
	```
   3. Replace "[sample iot hub connection string goes here]" with the connection information for iothubowner user (see [Prerequisites](#prerequisites))].
   4. Replace "[Device name goes here]" with the name of the device you want to create, read. update or delete.
      Note: _The **storageConnectionString** and **exportFileLocation** values are only used by the import and export samples._
   5. Locate the main function in the DeviceManagerSample.java file: 
      ```java
	   public static void main(String[] args) throws IOException, URISyntaxException, Exception
	  ``` 
      Notice there are function calls implemented for each CRUD operation and they called from the main function in order.
   6. Pick the operations you want to run, and comment out the others. Notice that the add operation requires you to provide some keys to associate with the device.
    
2. Building the Device Manager Sample application:

   To build the Device Manager Sample application using Maven, at a command prompt navigate to the **service\iot-service-samples\device-manager-sample** folder. Then execute the following command and check for build errors:
   ```
   mvn clean package
   ```

3. Running the Device Manager Sample application:

   To run the Device Manager Sample application using Maven, execute the following command.
   ```
   mvn exec:java -Dexec.mainClass="samples.com.microsoft.azure.sdk.iot.DeviceManagerSample"
   ```

   You can verify the result of your operation by using [Device Explorer or iothub-explorer tool][lnk-manage-iot-hub].

#### Build and run the Service Client Sample application:

1. Preparing the Service Client Sample application:
   1. Navigate to the main sample file for Service Client.
	  It can be found at: 
	  {IoT SDK root}\service\iot-service-samples\service-client-sample\src\main\java\samples\com\microsoft\azure\sdk\iot\service\sdk\ServiceClientSample.java
   2. Locate the following code in the file:
   ```java
   private static final String connectionString = "[Connection string goes here]";
   private static final String deviceId = "[Device name goes here]";
   ```
   3. Replace "[Connection string goes here]" with the connection information for iothubowner user (see [Prerequisites](#prerequisites))].
   4. Replace "[Device name goes here]" with the name of the device you are using.
   5. Locate the main function:
   ```java
   public static void main(String[] args) throws IOException, URISyntaxException, Exception
   ```
   6. Update the value of the local variable "commandMessage" to contain the message you want to send to the device.
    
2. Building the Service Client Sample application:

    To build the Service Client Sample application using Maven, at a command prompt navigate to the **\service\iot-service-samples\service-client-sample** folder. Then execute the following command and check for build errors:
    
    ```
    mvn clean package
    ```

3. Running the Service Client Sample application:

	To run the Service Client Sample application using Maven, execute the following command.
    
    ```
    mvn exec:java -Dexec.mainClass="samples.com.microsoft.azure.sdk.iot.ServiceClientSample"
    ```

	You can verify the result of your operation by using [Device Explorer or iothub-explorer tool][lnk-manage-iot-hub].

[devbox-setup]: ../../doc/java-devbox-setup.md
[lnk-setup-iot-hub]: https://aka.ms/howtocreateazureiothub
[lnk-manage-iot-hub]: https://aka.ms/manageiothub
[android-studio]: https://developer.android.com/studio/index.html
