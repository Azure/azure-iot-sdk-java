import subprocess
import os
import time


def getAndroidDeviceUDID():
    res = os.popen("adb devices").read()
    formatted_string = res.strip().split('\n')
    deviceList = []
    for x in formatted_string:
        if "\tdevice" in x:
            deviceList.append(x)
    device_count = deviceList.__len__()
    if device_count == 0:
        startAvd()
    else:
        writeToFile(deviceList[0].split()[0])


def startAvd():
    isEmulatorPresent = False
    emulatorList = os.popen("emulator -list-avds").read()
    formatted_string = emulatorList.strip().split('\n')
    for x in formatted_string:
        if "test1" in x:
            isEmulatorPresent = True
            break

    if isEmulatorPresent:
        os.popen("start cmd.exe /k emulator @test")
        time.sleep(30)
        getAndroidDeviceUDID()
    else:
        res = os.popen("echo no | avdmanager create avd -n test -k \"system-images;android-25;google_apis;x86\"").read()
        startAvd()


def writeToFile(deviceName):
    os.popen("echo " + deviceName + ">device_udid.txt").read()
	
getAndroidDeviceUDID()
