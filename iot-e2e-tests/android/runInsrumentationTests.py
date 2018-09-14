import subprocess
import os
import time
import subprocess
import sys

env=dict(os.environ, PATH="path")
cmd = "adb -s "+sys.argv[1]+" shell am instrument -w -r  -e debug false -e IOTHUB_CONNECTION_STRING '"+env["IOTHUB_CONNECTION_STRING"]+"' -e IOTHUB_E2E_X509_CERT_BASE64 "+env["IOTHUB_E2E_X509_CERT_BASE64"]+" -e IOTHUB_E2E_X509_PK_BASE64 "+env["IOTHUB_E2E_X509_PRIVATE_KEY_BASE64"]+" -e IOTHUB_E2E_X509_THUMBPRINT "+env["IOTHUB_E2E_X509_THUMBPRINT"]+"  -e package com.microsoft.azure.sdk.iot.android com.iothub.azure.microsoft.com.androidsample.test/android.support.test.runner.AndroidJUnitRunner"
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