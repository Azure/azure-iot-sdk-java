#!/usr/bin/env bash

echo 'Listing available android sdks for installation'
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --list | grep system-images

emulatorImage='system-images;android-28;google_apis;x86_64'
avdName='Pixel_9.0'

echo ''
echo "Installing emulator image ${emulatorImage}"
echo "y" | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --install $emulatorImage

echo ''
echo "Creating android emulator with name ${avdName}"
echo "no" | $ANDROID_HOME/cmdline-tools/latest/bin/avdmanager create avd -n $avdName -k $emulatorImage --force

echo ''
echo 'Listing active android emulators'
$ANDROID_HOME/emulator/emulator -list-avds

echo ''
echo "Starting emulator in background thread"
nohup $ANDROID_HOME/emulator/emulator -avd $avdName -gpu auto -no-snapshot > /dev/null 2>&1 &

echo ''
echo 'Waiting for emulator to boot up...'
nohup $ANDROID_HOME/emulator/emulator -avd android_emulator -no-snapshot -no-audio -no-boot-anim -accel auto -gpu auto -qemu -lcd-density 420 > /dev/null 2>&1 &
    $ANDROID_HOME/platform-tools/adb wait-for-device shell 'while [[ -z $(getprop sys.boot_completed | tr -d '\r') ]]; do sleep 1; done'
    $ANDROID_HOME/platform-tools/adb devices echo "Emulator started"

$ANDROID_HOME/platform-tools/adb devices

