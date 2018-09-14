@REM Copyright (c) Microsoft. All rights reserved.
@REM Licensed under the MIT license. See LICENSE file in the project root for full license information.

@REM -- Build Android project --
ECHO building android project
call gradle wrapper
call gradlew :clean :app:clean :app:assembleDebug :app:assembleDebugAndroidTest
set skipAndroidTests=%1
ECHO %skipAndroidTests% 
if %skipAndroidTests%==false (
@REM -- Select Android Device --
python AndroidDeviceSelect.py
@REM -- set device variable --
call renew_env.cmd 
ECHO We're working with "%ANDROID_DEVICE_NAME%"
@REM -- installing device and test apk--
ECHO installing app apk on device
call adb -s %ANDROID_DEVICE_NAME% install -r -g  app\build\outputs\apk\debug\app-debug.apk
ECHO installing test apk on device
call adb -s %ANDROID_DEVICE_NAME% install -r -g  app\build\outputs\apk\androidTest\debug\app-debug-androidTest.apk
@REM -- Starting Android Tests --
ECHO starting android tests
python runInsrumentationTests.py %ANDROID_DEVICE_NAME%
)
