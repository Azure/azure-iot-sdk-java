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
#
# -dns-server is set explicitly because the emulator otherwise resolves through its own DNS proxy, which forwards to
# whatever resolvers the host agent happens to have. On the hosted agents that has proven unreliable: lookups of the
# test hub and provisioning service hostnames intermittently come back as
#   java.net.UnknownHostException: Unable to resolve host "...": No address associated with hostname
# for some tests while other tests in the same run resolve the same hostname successfully. Pointing the guest at fixed
# public resolvers takes the host's resolver configuration out of the picture.
nohup $EMU -avd ${NAME_EMU} -no-window -no-snapshot -no-audio -no-boot-anim -dns-server 8.8.8.8,8.8.4.4 > /dev/null 2>&1 &

$ADB wait-for-device
$ADB shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done; input keyevent 82'
$ADB devices

# Logged so that a DNS failure later in the run can be told apart from an emulator that never had working name
# resolution to begin with. Does not fail the task; the tests report their own failures.
#
# ping's exit status is not used to judge DNS, because it also fails when ICMP is simply not allowed out, which is
# common and harmless here. Only the resolution step is inspected: ping prints the resolved address when the lookup
# succeeded, and reports an unknown host or bad address when it did not.
echo "Checking name resolution from inside the emulator:"
resolutionOutput=$($ADB shell ping -c 1 -W 5 azure-devices-provisioning.net 2>&1)
echo "${resolutionOutput}"

case "${resolutionOutput}" in
    *"unknown host"*|*"bad address"*|*"Name or service not known"*|*"Temporary failure in name resolution"*)
        echo "WARNING: emulator could not resolve azure-devices-provisioning.net. Name resolution is broken in the guest."
        ;;
    *)
        echo "Name resolution succeeded. Any ping failure above is ICMP reachability, not DNS."
        ;;
esac