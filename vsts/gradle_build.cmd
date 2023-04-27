@REM Copyright (c) Microsoft. All rights reserved.
@REM Licensed under the MIT license. See LICENSE file in the project root for full license information.

set build-root=%~dp0..

SET isPullRequestBuild=true
if "%TARGET_BRANCH%" == "$(System.PullRequest.TargetBranch)" (SET isPullRequestBuild=false)

cd %build-root%\iot-e2e-tests\android
call gradle wrapper --gradle-version 6.8.3 --warning-mode all
call gradlew :clean :app:clean :app:assembleDebug

if %ERRORLEVEL% NEQ 0 (
  echo "Gradle build failed with error code %ERRORLEVEL%"
  exit 1
)

call gradlew :app:assembleDebugAndroidTest -PIOTHUB_CONNECTION_STRING=%IOTHUB_CONNECTION_STRING% -PIOT_DPS_CONNECTION_STRING=%IOT_DPS_CONNECTION_STRING% -PIOT_DPS_ID_SCOPE=%DEVICE_PROVISIONING_SERVICE_ID_SCOPE% -PDPS_GLOBALDEVICEENDPOINT_INVALIDCERT=%INVALID_DEVICE_PROVISIONING_SERVICE_GLOBAL_ENDPOINT% -PPROVISIONING_CONNECTION_STRING_INVALIDCERT=%INVALID_DEVICE_PROVISIONING_SERVICE_CONNECTION_STRING% -PDPS_GLOBALDEVICEENDPOINT=%DPS_GLOBALDEVICEENDPOINT% -PIS_BASIC_TIER_HUB=%IS_BASIC_TIER_HUB% -PIS_PULL_REQUEST=%isPullRequestBuild% -PRECYCLE_TEST_IDENTITIES=true

if %ERRORLEVEL% NEQ 0 (
  echo "Gradle build failed with error code %ERRORLEVEL%"
  exit 1
)
