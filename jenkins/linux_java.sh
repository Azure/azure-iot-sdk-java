#!/bin/bash
# Copyright (c) Microsoft. All rights reserved.
# Licensed under the MIT license. See LICENSE file in the project root for full license information.

build_root=$(cd "$(dirname "$0")/.." && pwd)

# -- Java SDK Run E2E --
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
cd $build_root
mvn install -DskipITs=false -T 1C -Dfailsafe.rerunFailingTestsCount=2
[ $? -eq 0 ] || exit $?

# -- Java Dependencies --
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
cd $build_root/deps
mvn -q javadoc:javadoc
[ $? -eq 0 ] || exit $?
echo
echo [info] ---------------------------------------------------------------------
echo [info] javadoc for deps succeeded
echo [info] ---------------------------------------------------------------------
echo

# -- Java Service Client --
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
cd $build_root/service/iot-service-client
mvn -q javadoc:javadoc
[ $? -eq 0 ] || exit $?
echo
echo [info] ---------------------------------------------------------------------
echo [info] javadoc for iot-service-client succeeded
echo [info] ---------------------------------------------------------------------
echo

# -- Java Device Client --
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
cd $build_root/device/iot-device-client
mvn -q javadoc:javadoc
[ $? -eq 0 ] || exit $?
echo
echo [info] ---------------------------------------------------------------------
echo [info] javadoc for iot-device-client succeeded
echo [info] ---------------------------------------------------------------------
echo

# -- Java Provisioning --
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
cd $build_root/provisioning
mvn -q javadoc:javadoc
[ $? -eq 0 ] || exit $?
echo
echo [info] ---------------------------------------------------------------------
echo [info] javadoc for provisioning succeeded
echo [info] ---------------------------------------------------------------------
echo


