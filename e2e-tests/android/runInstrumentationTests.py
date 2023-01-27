import subprocess
import os
import time
import sys

env=dict(os.environ, PATH="path")
cmd = "adb -s "+sys.argv[1]+" shell am instrument -w -r -e debug false -e package 'com.microsoft.azure.sdk.iot.androidthings' com.microsoft.azure.sdk.iot.androidthings.test/android.support.test.runner.AndroidJUnitRunner"
print(cmd)
p = subprocess.Popen(cmd,  stdout=subprocess.PIPE, stderr=subprocess.PIPE, encoding='utf8')
(out, err) = p.communicate()
print(out)
print(err)
os.popen("taskkill /IM cmd.exe /FI \"WINDOWTITLE eq C:\windows\system32\cmd.exe - emulator*\"")
os.popen("setx ANDROID_DEVICE_NAME \"\"").read()
if "Failures:" in out or len(err)>0:
    sys.exit(-1)
    print("exit-1")
else:
    sys.exit(0)
    print("exit-0")