#!/bin/bash
# Copyright (c) Microsoft. All rights reserved.
# Licensed under the MIT license. See LICENSE file in the project root for full license information.

build_root=$(cd "$(dirname "$0")/.." && pwd)

# -- Java Dependencies --
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
cd $build_root/deps
mvn install
[ $? -eq 0 ] || exit $?

# -- Java Service Client --
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
cd $build_root/service/iot-service-client
mvn install
[ $? -eq 0 ] || exit $?

# -- Java Service Samples --
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
cd $build_root/service/iot-service-samples
mvn install
[ $? -eq 0 ] || exit $?

# -- Java Device Client --
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
cd $build_root/device/iot-device-client
mvn install
[ $? -eq 0 ] || exit $?

# -- Java Device Samples --
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
cd $build_root/device/iot-device-samples
mvn install
[ $? -eq 0 ] || exit $?

# -- Java SDK --
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
cd $build_root
mvn install -DskipITs=false
[ $? -eq 0 ] || exit $?
