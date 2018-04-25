@REM Copyright (c) Microsoft. All rights reserved.
@REM Licensed under the MIT license. See LICENSE file in the project root for full license information.

setlocal

set build-root=%~dp0..
rem // resolve to fully qualified path
for %%i in ("%build-root%") do set build-root=%%~fi

REM --Java SDK Run E2E  --
cd %build-root%
call mvn install -DskipITs=false -T 1C -Dfailsafe.rerunFailingTestsCount=2
if errorlevel 1 goto :eof
cd %build-root%

REM -- Java Dependencies --
cd %build-root%\deps
call mvn -q javadoc:javadoc
if errorlevel 1 goto :eof
echo.
echo [info] ---------------------------------------------------------------------
echo [info] javadoc for deps succeeded
echo [info] ---------------------------------------------------------------------
echo.
cd %build-root%

REM -- Java Service Client --
cd %build-root%\service\iot-service-client
call mvn -q javadoc:javadoc
if errorlevel 1 goto :eof
echo.
echo [info] ---------------------------------------------------------------------
echo [info] javadoc for iot-service-client succeeded
echo [info] ---------------------------------------------------------------------
echo.
cd %build-root%

REM -- Java Device Client --
cd %build-root%\device\iot-device-client
call mvn -q javadoc:javadoc
if errorlevel 1 goto :eof
echo.
echo [info] ---------------------------------------------------------------------
echo [info] javadoc for iot-device-client succeeded
echo [info] ---------------------------------------------------------------------
echo.
cd %build-root%

REM -- Java Provisioning --
cd %build-root%\provisioning
call mvn -q javadoc:javadoc
if errorlevel 1 goto :eof
echo.
echo [info] ---------------------------------------------------------------------
echo [info] javadoc for provisioning succeeded
echo [info] ---------------------------------------------------------------------
echo.
cd %build-root%

