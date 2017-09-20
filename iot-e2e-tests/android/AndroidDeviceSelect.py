import subprocess
import os

def getAndroidDeviceUDID():
    res = os.popen("adb devices").read()
    formatted_string =  res.strip().split('\n')
    deviceList = []
    for x in formatted_string:
        if "\tdevice" in x:
            deviceList.append(x)
    device_count = deviceList.__len__()
    if device_count==0:
        sp = subprocess.Popen("emulator @test", stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        out, err = sp.communicate()
        if out:
            print "standard output of subprocess:"
            print out
            return getAndroidDeviceUDID()
        if err:
            print "standard error of subprocess:"
            print err
            return createAvd()
    elif device_count==1:
        return deviceList[0].split( )[0]
    elif device_count>1:
        print 'multiple device are available, please select one'
        for i in range(0, device_count):
            print str(i+1) + ', '+ deviceList[i]
        nb = input('Choose a option ')
        return deviceList[nb].split( )[0]

def createAvd():
    res = os.popen("echo no | avdmanager create avd -n test -k \"system-images;android-25;google_apis;x86\"").read()
    emulatorList = os.popen("emulator -list-avds").read()
    formatted_string = emulatorList.strip().split('\n')
    for x in formatted_string:
        if "test" in x:
            getAndroidDeviceUDID()
        else:
            None
os.popen("echo "+getAndroidDeviceUDID()+">devcie.txt").read()
