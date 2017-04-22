@REM Copyright (c) Microsoft. All rights reserved.
@REM Licensed under the MIT license. See LICENSE file in the project root for full license information.

setlocal

set build-root=%~dp0..
rem // resolve to fully qualified path
for %%i in ("%build-root%") do set build-root=%%~fi

REM -- Java Dependencies --
cd %build-root%\deps
call mvn install
if errorlevel 1 goto :eof
cd %build-root%

REM -- Java Service Client --
cd %build-root%\service\iot-service-client
call mvn install
if errorlevel 1 goto :eof
cd %build-root%

REM -- Java Service Samples --
cd %build-root%\service\iot-service-samples
call mvn install
if errorlevel 1 goto :eof
cd %build-root%

REM -- Java Device Client --
cd %build-root%\device\iot-device-client
call mvn install
if errorlevel 1 goto :eof
cd %build-root%

REM -- Java Device Samples --
cd %build-root%\device\iot-device-samples
call mvn install
if errorlevel 1 goto :eof
cd %build-root%

REM -- Java SDK --
cd %build-root%
call mvn install -DskipITs=false
if errorlevel 1 goto :eof
cd %build-root%
