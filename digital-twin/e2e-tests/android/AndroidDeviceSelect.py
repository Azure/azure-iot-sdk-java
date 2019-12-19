import os
import time
import re

def setTarget(deviceName):
    #os.popen("echo " + deviceName + ">device_udid.txt").read()
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
            setTarget(device)
            break
    if not hasRealDevice:
        print("no real android device found")

print("selecting android device")
killAvd()