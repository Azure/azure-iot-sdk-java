# Microsoft Azure IoT SDKs for Java

This SDK is tested nightly on Windows 10, Ubuntu 1604, and on an Android emulator. For additional details
for each tested platform, see the respective sections below. 

While this SDK contains several different packages, each package supports the same platforms and JDKs.

## Supported JDK versions

Currently, this SDK supports JDK versions 8 and 11. This SDK _may_ work with other versions, but there are no guarantees 
that they will _always_ work for those versions nor are there guarantees that we will fix bugs that are only present on
those versions.


## Windows 10

Note that, while we only directly test on Windows 10, we do support other Windows versions as well.

Nightly test platform details:
Apache Maven 3.8.1
Java version: 1.8.0_292, vendor: AdoptOpenJDK

Default locale: en_US, platform encoding: Cp1252

OS name: "windows server 2016", version: "10.0", arch: "amd64", family: "windows"

## Ubuntu 2204

Note that, while we only directly test on Ubuntu 22.04, we do generally support other popular Linux distributions. 

Nightly test platform details:
Apache Maven 3.8.1

Java 8 version: 1.8.0_332, vendor: Eclipse Temurin
Java 11 version: 11.0.15_10, vendor: Eclipse Temurin

Default locale: en_US, platform encoding: UTF-8

OS name: "linux", version: "4.15.0-1113-azure", arch: "amd64", family: "unix"

## Android

This SDK supports Android API versions 24, 25, 26, 27, and 28.

The gradle version used in nightly tests is 3.6.3 and the API version used in nightly tests is 28.

The build.gradle file that outlines more specifics can be found [here](./iot-e2e-tests/android/app/build.gradle).

### Older Android API versions

Older API versions have known issues that we don't plan to fix. For additional details on that, see [this issue](https://github.com/Azure/azure-iot-sdk-java/issues/747).


## Miscellaneous support notes

- This library has known compatibility issues with JDK 7 and below, and those JDK versions are not supported.
- This library does not officially support being run on MacOS.
- This library does not officially support minification via Proguard.
- This library does not officially support being used in Kotlin applications.
