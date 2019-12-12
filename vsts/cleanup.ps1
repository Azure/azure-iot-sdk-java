if ($IOTHUB_CONNECTION_STRING.length -eq 0)
{
        $exception = "Iot hub connection string not set"
		throw $exception
}

if ($IOT_DPS_CONNECTION_STRING.length -eq 0)
{
        $exception = "DPS connection string not set"
		throw $exception
}


#Install the Java SDK
mvn install -DskipTests=true -T 2C

#move to sample folder where you can run the sample that deletes all devices tied to a hub
cd service/iot-service-samples/device-deletion-sample/target

#Run sample code to delete all devices from these iot hubs
echo "Cleaning up iot hub and dps"
java -jar deviceDeletionSample.jar $IOTHUB_CONNECTION_STRING $IOT_DPS_CONNECTION_STRING