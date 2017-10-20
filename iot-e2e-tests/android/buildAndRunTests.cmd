REM -- Build Android project --
ECHO building android project
set skipAndroidTests=%1
ECHO %skipAndroidTests% 
if %skipAndroidTests%==false (call gradlew :app:assembleDebug :app:assembleDebugAndroidTest
REM -- Select Android Device --
python AndroidDeviceSelect.py
REM -- set device variable --
call renew_env.cmd 
ECHO We're working with "%ANDROID_DEVICE_NAME%"
REM -- installing device and test apk--
ECHO installing app apk on device
call adb -s %ANDROID_DEVICE_NAME% install -r -g  app\build\outputs\apk\app-debug.apk
ECHO installing test apk on device
call adb -s %ANDROID_DEVICE_NAME% install -r -g  app\build\outputs\apk\app-debug-androidTest.apk
REM -- Starting Android Tests --
ECHO starting android tests
python runInsrumentationTests.py %ANDROID_DEVICE_NAME%
)
