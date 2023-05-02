@REM Copyright (c) Microsoft. All rights reserved.
@REM Licensed under the MIT license. See LICENSE file in the project root for full license information.

@REM set build-root=%~dp0..

@REM SET isPullRequestBuild=true
@REM if "%TARGET_BRANCH%" == "$(System.PullRequest.TargetBranch)" (SET isPullRequestBuild=false)

cd iot-e2e-tests\android
./gradlew wrapper --gradle-version 5.5
@REM gradlew :clean :app:clean :app:assembleDebug

@REM if %ERRORLEVEL% NEQ 0 (
@REM   echo "Gradle build failed with error code %ERRORLEVEL%"
@REM   exit 1
@REM )

@REM gradlew :app:assembleDebugAndroidTest -PIOTHUB_CONNECTION_STRING=%IOTHUB_CONNECTION_STRING% -PIOT_DPS_CONNECTION_STRING=%IOT_DPS_CONNECTION_STRING% -PIOT_DPS_ID_SCOPE=%DEVICE_PROVISIONING_SERVICE_ID_SCOPE% -PDPS_GLOBALDEVICEENDPOINT_INVALIDCERT=%INVALID_DEVICE_PROVISIONING_SERVICE_GLOBAL_ENDPOINT% -PPROVISIONING_CONNECTION_STRING_INVALIDCERT=%INVALID_DEVICE_PROVISIONING_SERVICE_CONNECTION_STRING% -PDPS_GLOBALDEVICEENDPOINT=%DPS_GLOBALDEVICEENDPOINT% -PIS_BASIC_TIER_HUB=%IS_BASIC_TIER_HUB% -PIS_PULL_REQUEST=%isPullRequestBuild% -PRECYCLE_TEST_IDENTITIES=true

@REM if %ERRORLEVEL% NEQ 0 (
@REM   echo "Gradle build failed with error code %ERRORLEVEL%"
@REM   exit 1
@REM )
