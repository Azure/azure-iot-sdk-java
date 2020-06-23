@REM Copyright (c) Microsoft. All rights reserved.
@REM Licensed under the MIT license. See LICENSE file in the project root for full license information.

set build-root=%~dp0..

SET isPullRequestBuild=true
if "%TARGET_BRANCH%" == "$(System.PullRequest.TargetBranch)" (SET isPullRequestBuild=false)

cd %build-root%\iot-e2e-tests\android
call gradle wrapper
call gradlew :clean :app:clean :app:assembleDebug
call gradlew :app:assembleDebugAndroidTest -PIotHubConnectionString=%IOTHUB_CONNECTION_STRING% -PIotHubInvalidCertConnectionString=%IOTHUB_CONN_STRING_INVALIDCERT% -PDeviceProvisioningServiceConnectionString=%IOT_DPS_CONNECTION_STRING% -PDeviceProvisioningServiceIdScope=%DEVICE_PROVISIONING_SERVICE_ID_SCOPE% -PInvalidDeviceProvisioningServiceGlobalEndpoint=%INVALID_DEVICE_PROVISIONING_SERVICE_GLOBAL_ENDPOINT% -PInvalidDeviceProvisioningServiceConnectionString=%INVALID_DEVICE_PROVISIONING_SERVICE_CONNECTION_STRING% -PFarAwayIotHubConnectionString=%FAR_AWAY_IOTHUB_CONNECTION_STRING% -PCustomAllocationWebhookUrl=%CUSTOM_ALLOCATION_POLICY_WEBHOOK% -PIsBasicTierHub=%IS_BASIC_TIER_HUB% -PIsPullRequest=%isPullRequestBuild%
