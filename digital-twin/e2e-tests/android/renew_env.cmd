@echo off
echo.
echo Refreshing ANDROID_DEVICE_NAME from registry

:: Get User ANDROID_DEVICE_NAME
for /f "tokens=3*" %%A in ('reg query "HKCU\Environment" /v ANDROID_DEVICE_NAME') do set userANDROID_DEVICE_NAME=%%A%%B

:: Set Refreshed ANDROID_DEVICE_NAME
set ANDROID_DEVICE_NAME=%userANDROID_DEVICE_NAME%

echo Refreshed ANDROID_DEVICE_NAME
echo %ANDROID_DEVICE_NAME%