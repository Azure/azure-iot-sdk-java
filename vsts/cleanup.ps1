﻿#Install the Java SDK
mvn install -DskipTests=true -T 2C

#move to sample folder where you can run the sample that deletes all devices tied to a hub
cd service/iot-service-samples/device-deletion-sample/target

#Run sample code to delete all devices from these iot hubs
echo "Cleaning up iot hub and dps"
java -jar deviceDeletionSample.jar $IOTHUB_CONNECTION_STRING $IOT_DPS_CONNECTION_STRING