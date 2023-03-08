@REM Copyright (c) Microsoft. All rights reserved.
@REM Licensed under the MIT license. See LICENSE file in the project root for full license information.

setlocal

set build-root=%~dp0..
@REM // resolve to fully qualified path
for %%i in ("%build-root%") do set build-root=%%~fi

@REM -- E2E Test Build --
cd %build-root%
mvn -Dmaven.javadoc.skip=true --projects :iot-e2e-common --also-make clean install -DskipTests --batch-mode -q