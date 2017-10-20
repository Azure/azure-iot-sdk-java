# Microsoft Azure IoT Hub Java Android tests

Microsoft Azure IoT Hub Java Android tests run instrumented tests to check iot sdk

## prerequisites 

 * download and install android studio and sdk tools [link](https://developer.android.com/studio/index.html).
 * download avd images for android-25.
 * set path for emulator, adb and avdmanager [link](https://stackoverflow.com/questions/20564514/adb-is-not-recognized-as-an-internal-or-external-command-operable-program-or)


## How to run the tests

- mvn install

## How to see android logs

- adb logcat


## Add local.properties
add sdk location to the file in below format
sdk.dir=C\:\\AndroidSdk