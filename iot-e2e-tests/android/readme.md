# Microsoft Azure IoT Hub Java Android tests

Microsoft Azure IoT Hub Java Android tests run instrumented tests to check iot sdk

## Prerequisites 

 * download and install android studio and sdk tools [link](https://developer.android.com/studio/index.html).
 * download avd images for android-25.
 * set path for emulator, adb and avdmanager in Environment variables 

## How to run the tests

- mvn install -Dskipaandroid=false 

## How to see android logs

- adb logcat


## Add local.properties
add sdk location to the file in below format
sdk.dir=C\:\\AndroidSdk