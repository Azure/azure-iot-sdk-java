@REM Copyright (c) Microsoft. All rights reserved.
@REM Licensed under the MIT license. See LICENSE file in the project root for full license information.

setlocal

set build-root=%~dp0..
rem // resolve to fully qualified path
for %%i in ("%build-root%") do set build-root=%%~fi

REM -- Websocket Transport Layer --
cd %build-root%\websocket-transport-layer
call mvn install
if errorlevel 1 goto :eof
cd %build-root%

REM -- Java Service Client --
cd %build-root%\service
call mvn install -DskipITs=false
if errorlevel 1 goto :eof
cd %build-root%

REM -- Java Device Client --
cd %build-root%\device
call mvn install -DskipITs=false
if errorlevel 1 goto :eof
cd %build-root%
