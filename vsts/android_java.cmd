@REM Copyright (c) Microsoft. All rights reserved.
@REM Licensed under the MIT license. See LICENSE file in the project root for full license information.

setlocal

set build-root=%~dp0..
@REM // resolve to fully qualified path
for %%i in ("%build-root%") do set build-root=%%~fi

@REM -- Delete m2 folder--

call RD /S /Q "c:/users/%USERNAME%/.m2/repository/com/microsoft/azure/sdk/iot"

@REM -- Android Test Build --
cd %build-root%
call mvn clean install -Dmaven.javadoc.skip=true -DskipTests -T 2C --projects com.microsoft.azure.sdk.iot:iot-e2e-common,com.microsoft.azure.sdk.iot:iot-deps,com.microsoft.azure.sdk.iot:iot-device-client,com.microsoft.azure.sdk.iot:iot-service-client,com.microsoft.azure.sdk.iot.provisioning:provisioning-device-client,com.microsoft.azure.sdk.iot.provisioning:provisioning-service-client,com.microsoft.azure.sdk.iot.provisioning.security:security-provider,com.microsoft.azure.sdk.iot.provisioning.security:tpm-provider,com.microsoft.azure.sdk.iot.provisioning.security:x509-provider,com.microsoft.azure.sdk.iot.provisioning.security:tpm-provider-emulator,com.microsoft.azure.sdk.iot.provisioning.security:dice-provider-emulator
if errorlevel 1 goto :eof
cd %build-root%\iot-e2e-tests\android
call gradle wrapper
call gradlew :clean :app:clean :app:assembleDebug
call gradlew :app:assembleDebugAndroidTest -PIotHubConnectionString=%IOTHUB_CONNECTION_STRING% -PIotHubInvalidCertConnectionString=%IOTHUB_CONN_STRING_INVALIDCERT% -PDeviceProvisioningServiceConnectionString=%IOT_DPS_CONNECTION_STRING% -PDeviceProvisioningServiceIdScope=%DEVICE_PROVISIONING_SERVICE_ID_SCOPE% -PInvalidDeviceProvisioningServiceGlobalEndpoint=%INVALID_DEVICE_PROVISIONING_SERVICE_GLOBAL_ENDPOINT% -PInvalidDeviceProvisioningServiceConnectionString=%INVALID_DEVICE_PROVISIONING_SERVICE_CONNECTION_STRING% -PFarAwayIotHubConnectionString=%FAR_AWAY_IOTHUB_CONNECTION_STRING% -PCustomAllocationWebhookUrl=%CUSTOM_ALLOCATION_POLICY_WEBHOOK% -PIsBasicTierHub=%IS_BASIC_TIER_HUB%
