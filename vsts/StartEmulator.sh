#!/usr/bin/env bash

echo 'Listing available android sdks for installation'
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --list | grep system-images

SDK="${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager"
AVD="${ANDROID_HOME}/cmdline-tools/latest/bin/avdmanager"
EMU="${ANDROID_HOME}/emulator/emulator"
ADB="${ANDROID_HOME}/platform-tools/adb"

NAME_EMU="android_emulator"
IMG_EMU='system-images;android-28;default;x86'

# Install AVD files
echo "y" | $SDK --install "${IMG_EMU}"

# Create emulator
echo "no" | $AVD create avd -n ${NAME_EMU} -k "${IMG_EMU}" --force

echo ""
echo "List AVDs:"
$EMU -list-avds

# Start emulator in background and with no UI (-no-window), as we're only running database tests.
nohup $EMU -avd ${NAME_EMU} -no-window -no-snapshot -no-audio -no-boot-anim > /dev/null 2>&1 &

$ADB wait-for-device
$ADB shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done; input keyevent 82'
$ADB devices