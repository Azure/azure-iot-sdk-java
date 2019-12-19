# Azure IoT Digital Twins Device SDK Android Sample

**PREVIEW - WILL LIKELY HAVE BREAKING CHANGES**

This project contains a sample implementation of a simulated environmental sensor. It shows how to:

  * Implement the environmental sensor interface
  * Create an interfaceInstance for this interface
  * Use the digital twin device client to register this interfaceInstance and interact with the Digital Twins services.
  * How to respond to command invocations
  * How to respond to property updates

## How to run the sample

### Setup environment
* Install [Android Studio](https://developer.android.com/studio) 
* Install Android SDK API V24: 
  * 'Configure' ->  'SDK Manager', select 'Android 7.0' from list and 'apply'. 
  * It is temporary since latest PAHO MQTT library has a break change requires JDK 8+. Once this issue is addressed, Digital Twin device client would support JDK 7 which is Android API 19+. 
* Install virtual device
  * 'Configure' -> 'AVD manager' -> 'create virtual device...' -> select 'Pixel 2'(or any device you prefer) from list and click 'next' -> select 'Nougat'(which is Android 7.0 installed on above) 
* Import Android project
  * 'Import project(Gradle, Eclipse ADT, etc)' -> select 'digital-twin\device-samples\Android\build.gradle' and click 'next' to 'Finish'
* Install 'Lombok' plugin and enable annotation process: 
  * 'File' -> 'Settings' -> 'Plugins' -> 'Marketplace' -> select 'Lombok' from list and click 'Install'
  * 'File' -> 'Settings' -> 'Other Settings' -> 'Lombok plugin' -> Check 'Enable Lombok plugin for this project'
### Sample Arguments

In order to run this sample, you must add your Digital Twin device connection string under 'digital-twin\device-samples\Android\gradle.properties':
- DIGITAL_TWIN_CONNECTION_STRING=[YOUR DIGITAL TWIN DEVICE CONNECTION STRING]

### Other Prerequisites
In order to run this sample, you will need an IoT Hub. You will also need at least one device registered in this hub so that the sample can register as that device
* [Setup Your IoT Hub][lnk-setup-iot-hub]

### Run the sample
Select 'app' on top middle of tool bar -> select the virtual device you created -> click 'Run' or 'Debug' icon. An Android simulator should be launched and the sample application with a draft UI should show up shortly.
     

The sample will register to use the Environmental Sensor interface, report some properties on the interface, send some telemetry on the
interface, and then will sit idle and wait for updates from the cloud such as command invocations and writable property updates

Note that the DigitalTwinClient depends on the DeviceClient class from the com.microsoft.azure.sdk.iot.iot-device-client library to communicate with the hub. The sample shows how to compose these two together.

[lnk-setup-iot-hub]: https://aka.ms/howtocreateazureiothub
