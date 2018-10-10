@REM Copyright (c) Microsoft. All rights reserved.
@REM Licensed under the MIT license. See LICENSE file in the project root for full license information.

setlocal

set build-root=%~dp0..
@REM // resolve to fully qualified path
for %%i in ("%build-root%") do set build-root=%%~fi

@REM -- Delete m2 folder--

call RD /S /Q "c:/users/%USERNAME%/.m2"

@REM -- Android Test Build --
cd %build-root%
call mvn install -DskipAndroidTests=false -DskipITs=true
if errorlevel 1 goto :eof
cd %build-root%
