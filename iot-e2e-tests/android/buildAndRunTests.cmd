REM -- Build Android project --
ECHO building android project
set skipAndroidTests=%1
ECHO %skipAndroidTests% 
if %skipAndroidTests%==false (call gradlew :app:assembleDebug :app:assembleDebugAndroidTest
REM -- Select Android Device --
python AndroidDeviceSelect.py
REM -- set device variable --
set /p udid=<device_udid.txt
ECHO We're working with "%udid%"
REM -- installing device and test apk--
ECHO installing app apk on device
call adb -s %udid% install -r -g  app\build\outputs\apk\app-debug.apk
ECHO installing test apk on device
call adb -s %udid% install -r -g  app\build\outputs\apk\app-debug-androidTest.apk
REM -- Starting Android Tests --
ECHO starting android tests
call adb -s %udid% shell am instrument -w -r  -e debug false -e IOTHUB_CONNECTION_STRING '%IOTHUB_CONNECTION_STRING%'  -e package com.microsoft.azure.sdk.iot.android com.iothub.azure.microsoft.com.androidsample.test/android.support.test.runner.AndroidJUnitRunner)
