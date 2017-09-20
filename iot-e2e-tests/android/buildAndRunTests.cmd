call gradlew :app:assembleDebug :app:assembleDebugAndroidTest
python AndroidDeviceSelect.py
set /p device=<devcie.txt
ECHO We're working with "%device%"
call adb -s %device% install -r -g  app\build\outputs\apk\app-debug.apk
call adb -s %device% install -r -g  app\build\outputs\apk\app-debug-androidTest.apk
call adb -s %device% shell am instrument -w -r  -e debug false -e iotHubConnectionString '%IOTHUB_CONNECTION_STRING%'  -e package com.microsoft.azure.sdk.iot.android com.iothub.azure.microsoft.com.androidsample.test/android.support.test.runner.AndroidJUnitRunner