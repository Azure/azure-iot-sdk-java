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
call mvn clean install -DskipTests=true -T 2C
if errorlevel 1 goto :eof
cd %build-root%\digital-twin\e2e-tests\android
call gradle wrapper
call gradlew :clean :apk:clean :apk:assembleDebug
call gradlew :apk:assembleDebugAndroidTest -PAppCenterAppSecret=%APPCENTER_DIGITAL_TWIN_APP_SECRET% -PIOTHUB_CONNECTION_STRING=%IOTHUB_CONNECTION_STRING% -PEVENT_HUB_CONNECTION_STRING=%EVENT_HUB_CONNECTION_STRING%
