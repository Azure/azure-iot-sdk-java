
cd iot-e2e-tests\android

wget https://downloads.gradle-dn.com/distributions/gradle-6.8.3-bin.zip
unzip -d . gradle-6.8.3-bin.zip
ls
./gradle-6.8.3/bin/gradle wrapper
./gradlew :app:assembleDebug
./gradlew :app:assembleDebugAndroidTest `-PIOTHUB_CONNECTION_STRING=$env:IOTHUB_CONNECTION_STRING `-PIOT_DPS_CONNECTION_STRING=$env:IOT_DPS_CONNECTION_STRING `-PIOT_DPS_ID_SCOPE=$env:DEVICE_PROVISIONING_SERVICE_ID_SCOPE `-PDPS_GLOBALDEVICEENDPOINT_INVALIDCERT=$env:INVALID_DEVICE_PROVISIONING_SERVICE_GLOBAL_ENDPOINT `-PPROVISIONING_CONNECTION_STRING_INVALIDCERT=$env:INVALID_DEVICE_PROVISIONING_SERVICE_CONNECTION_STRING `-PDPS_GLOBALDEVICEENDPOINT=$env:DPS_GLOBALDEVICEENDPOINT `-PIS_BASIC_TIER_HUB=$env:IS_BASIC_TIER_HUB `-PIS_PULL_REQUEST=$env:isPullRequestBuild `-PRECYCLE_TEST_IDENTITIES=true
