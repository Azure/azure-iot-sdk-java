import os
import time
import re


def startAvd():
    isEmulatorImagePresent = False
    emulatorList = os.popen("emulator -list-avds").read()
    formatted_string = emulatorList.strip().split('\n')
    for x in formatted_string:
        if "test" in x:
            isEmulatorImagePresent = True
            break
    if isEmulatorImagePresent:
        print("Starting emulator")
        os.popen("start cmd.exe /k emulator @test")
        os.popen("adb wait-for-device")
        waitForDeviceToComeOnline()
        writeToFile((getDeviceList()[0]).split()[0])
    else:
        print("Creating emulator")
        os.popen("echo no | avdmanager create avd --force -n test -k \"system-images;android-25;google_apis_playstore;x86\"").read()
        time.sleep(10)
        startAvd()

def waitForDeviceToComeOnline():
    deviceBootCOmpleted = os.popen("adb shell getprop sys.boot_completed").read()
    while deviceBootCOmpleted.rstrip() != "1":
        print("sleeping 2secs for device to come online")
        time.sleep(2)
        deviceBootCOmpleted = os.popen("adb shell getprop sys.boot_completed").read()

def writeToFile(deviceName):
    os.popen("setx ANDROID_DEVICE_NAME "+deviceName).read()

def getDeviceList():
    res = os.popen("adb devices").read()
    formatted_string = res.strip().split('\n')
    deviceList = []
    for x in formatted_string:
        if "\tdevice" in x:
            deviceList.append(x)
    print (deviceList)
    return deviceList

def killAvd():
    hasRealDevice = False
    deviceList = getDeviceList()
    print("Getting connected Devices")
    for device in deviceList:
        if not device.startswith('emulator'):
            hasRealDevice = True
            print("found real device "+device)
            device = re.sub('\tdevice$', '', device)
            writeToFile(device)
            break
    if not hasRealDevice:
        for emulator in deviceList:
            os.popen("adb -s " + emulator.split()[0] + " emu kill")
            time.sleep(30)
        startAvd()

print("selecting android device")
killAvd()