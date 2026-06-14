# This script assumes it is being run from the root of the repository
cd iot-e2e-tests\android

# This script pulls down this version of gradle because the default version installed on ADO
# can change over time. This allows us more control over when we want to upgrade gradle versions
Invoke-WebRequest -Uri "https://services.gradle.org/distributions/gradle-9.2.1-bin.zip" -OutFile ./gradle.zip
Expand-Archive ./gradle.zip

Write-Host "Starting the Gradle Wrapper"
./gradle/gradle-9.2.1/bin/gradle wrapper --stacktrace

Write-Host "Assembling the source APK"
./gradlew :app:assembleDebug

# Unlike in the Linux/Windows e2e tests, these secrets are loaded into the Android BuildConfig.java
# file that is generated during this assembly. This is done because the android tests run on emulators
# and the emulators have no access to the environment variables on the OS that runs the emulator,
# so this is the only way to pass along these secrets.
Write-Host "Assembling the test APK with the provided secrets"
./gradlew :app:assembleDebugAndroidTest `
    `-PIOTHUB_CONNECTION_STRING=$env:IOTHUB_CONNECTION_STRING `
    `-PIOT_DPS_CONNECTION_STRING=$env:IOT_DPS_CONNECTION_STRING `
    `-PIOT_DPS_ID_SCOPE=$env:IOT_DPS_ID_SCOPE `
    `-PRECYCLE_TEST_IDENTITIES=true
