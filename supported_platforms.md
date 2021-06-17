# Microsoft Azure IoT SDKs for Java

This SDK is tested nightly on Windows 10, Ubuntu 1604, and on an Android emulator. For additional details
for each tested platform, see the respective sections below. 

While this SDK contains several different packages, each package supports the same platforms and JDKs.

## Supported JDK versions

Currently, this SDK supports JDK versions 8 and 11. This SDK may work with other versions, but there are no guarantees 
that they will always work for those versions nor are there guarantees that we will fix bugs that are only present on
those versions.


## Windows 10

Nightly test platform details:
Apache Maven 3.8.1 (05c21c65bdfed0f71a2f2ada8b84da59348c4c5d)
Java version: 1.8.0_292, vendor: AdoptOpenJDK, runtime: C:\hostedtoolcache\windows\Java_Adopt_jdk\8.0.292-10\x64\jre
Default locale: en_US, platform encoding: Cp1252
OS name: "windows server 2016", version: "10.0", arch: "amd64", family: "windows"

Note that, while we only directly test on Windows 10, we do support other Windows versions as well.

## Ubuntu 1604

Nightly test platform details:
Apache Maven 3.8.1 (05c21c65bdfed0f71a2f2ada8b84da59348c4c5d)
Java version: 1.8.0_292, vendor: AdoptOpenJDK, runtime: /usr/lib/jvm/adoptopenjdk-8-hotspot-amd64/jre
Default locale: en_US, platform encoding: UTF-8
OS name: "linux", version: "4.15.0-1113-azure", arch: "amd64", family: "unix"

Note that, while we only directly test on Ubuntu 1604, we do generally support other popular linux distributions. 

## Android

This SDK supports Android API versions 24, 25, 26, 27, and 28.

The android emulator image used for nightly tests is "system-images;android-28;google_apis;x86_64"

The gradle version used in nightly tests is 3.6.3

The build.gradle file that outlines more specifics can be found [here](./iot-e2e-tests/android/app/build.gradle)

### Older Android API versions

Older API versions have known issues that we don't plan to fix. For additional details on that, see [this issue](https://github.com/Azure/azure-iot-sdk-java/issues/747)


## Miscellaneous support notes

- This library has known compatibility issues with JDK 7 and below, and those JDK versions are not supported.
- This library does not officially support being run on MacOS
- This library does not officially support minification via [Proguard](http://android-doc.github.io/tools/help/proguard.html).
- This library does not officially support being used in [Kotlin](https://kotlinlang.org) applications.