@REM Copyright (c) Microsoft. All rights reserved.
@REM Licensed under the MIT license. See LICENSE file in the project root for full license information.

@REM -- Build Android project --
ECHO building android project
call gradle wrapper
call gradlew :clean :app:clean :app:assembleDebug 
call gradlew :app:assembleDebugAndroidTest -PIotHubConnectionString=%IOTHUB_CONNECTION_STRING% -PIotHubPublicCertBase64=%IOTHUB_E2E_X509_CERT_BASE64% -PIotHubPrivateKeyBase64=%IOTHUB_E2E_X509_PRIVATE_KEY_BASE64% -PIotHubThumbprint=%IOTHUB_E2E_X509_THUMBPRINT% -PIotHubInvalidCertConnectionString=%IOTHUB_DEVICE_CONN_STRING_INVALIDCERT% -PAppCenterAppSecret=%APPCENTER_APP_SECRET%
