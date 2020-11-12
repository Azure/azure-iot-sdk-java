#!/usr/bin/env bash

# -e flag to force this command to run on what should be the only emulator active on this machine
echo 'Installing APKs on emulator'
adb -e install -r iot-e2e-tests/android/app/build/outputs/apk/debug/app-debug.apk
adb -e install -r iot-e2e-tests/android/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk

#List instrumentation classes, for logging purposes only
echo 'Listing available instrumentations:'
adb -e shell pm list instrumentation
echo ''

annotationString="com.microsoft.azure.sdk.iot.android.helper.${TEST_GROUP_ID}"
echo 'Running android tests with annotation'
echo $annotationString

echo 'Enabling verbose logging of packages under test'
adb logcat com.microsoft.azure.sdk.iot.*:V
adb logcat tests.integration.com.microsoft.azure.sdk.*:V

#Return code from adb shell isn't returned as one would expect. Need to capture output logs and analyze them to determine if this test run was a success or not
TestLogs=$(adb -e shell am instrument -w -e annotation $annotationString com.iothub.azure.microsoft.com.androide2e.test/android.support.test.runner.AndroidJUnitRunner)

echo 'Result of running tests'
echo "$TestLogs"

#This string is always present in the output logs from android testing if a test failed. Hacky, but there is no return code
# to check instead.
FailureMessage='FAILURES!!!'

if grep -q "$FailureMessage" <<< "$TestLogs"; then
    echo 'Test failures detected, exiting...'
    exit -1
fi

InstrumentationFailureMessage='INSTRUMENTATION_STATUS_CODE: -1'

if grep -q "$InstrumentationFailureMessage" <<< "$TestLogs"; then
    echo 'Instrumentation failures detected, exiting...'
    exit -1
fi

AnotherInstrumentationFailureMessage='INSTRUMENTATION_FAILED'
if grep -q "$AnotherInstrumentationFailureMessage" <<< "$TestLogs"; then
    echo 'Instrumentation failures detected, exiting...'
    exit -1
fi

# adb also supports running all tests that do not have a given annotation
# adb -e shell am instrument -w -e notAnnotation com.microsoft.azure.sdk.iot.android.helper.TestGroup39 com.iothub.azure.microsoft.com.androide2e.test/android.support.test.runner.AndroidJUnitRunner