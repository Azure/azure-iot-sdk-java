import subprocess
import os
import time


def startAvd():
    isEmulatorImagePresent = False
    emulatorList = os.popen("emulator -list-avds").read()
    formatted_string = emulatorList.strip().split('\n')
    for x in formatted_string:
        if "test" in x:
            isEmulatorImagePresent = True
            break
    if isEmulatorImagePresent:
        os.popen("start cmd.exe /k emulator @test")
        time.sleep(30)
        writeToFile((getDeviceList()[0]).split()[0])
    else:
        os.popen("echo no | avdmanager create avd -n test -k \"system-images;android-25;google_apis;x86\"").read()
        time.sleep(10)
        startAvd()


def writeToFile(deviceName):
    os.popen("echo " + deviceName + ">device_udid.txt").read()

def getDeviceList():
    res = os.popen("adb devices").read()
    formatted_string = res.strip().split('\n')
    deviceList = []
    for x in formatted_string:
        if "\tdevice" in x:
            deviceList.append(x)
    return deviceList

def killAvd():
    hasRealDevice = False
    deviceList = getDeviceList()
    for device in deviceList:
        if not device.startswith('emulator'):
            hasRealDevice = True
            writeToFile(device)
            break
    if not hasRealDevice:
        for device in deviceList:
            os.popen("adb -s " + device.split()[0] + " emu kill")
    startAvd()


killAvd()




