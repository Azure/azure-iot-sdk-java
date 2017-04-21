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
cd %build-root%\service
call mvn install
if errorlevel 1 goto :eof
cd %build-root%

REM -- Java Device Client --
cd %build-root%\device
call mvn install
if errorlevel 1 goto :eof
cd %build-root%

REM -- Java Device Client - integration tests --
cd %build-root%\iot-device-tests
call mvn install -DskipITs=false
if errorlevel 1 goto :eof
cd %build-root%

REM -- Java Service Client - integration tests --
cd %build-root%\iot-service-tests
call mvn install -DskipITs=false
if errorlevel 1 goto :eof
cd %build-root%
