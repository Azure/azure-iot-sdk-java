@REM Copyright (c) Microsoft. All rights reserved.
@REM Licensed under the MIT license. See LICENSE file in the project root for full license information.

@REM -- Select Android Device --
python AndroidDeviceSelect.py
@REM -- set device variable --
call renew_env.cmd 
ECHO We're working with "%ANDROID_DEVICE_NAME%"
@REM -- installing device and test apk--
ECHO installing apk on device
call adb -s %ANDROID_DEVICE_NAME% install -r -t "things\build\outputs\apk\debug\things-debug.apk" 
ECHO installing test apk on device
call adb -s %ANDROID_DEVICE_NAME% install -r -t "things\build\outputs\apk\androidTest\debug\things-debug-androidTest.apk" 
@REM -- Starting Android Tests --
ECHO starting android tests
python runInstrumentationTests.py %ANDROID_DEVICE_NAME%
