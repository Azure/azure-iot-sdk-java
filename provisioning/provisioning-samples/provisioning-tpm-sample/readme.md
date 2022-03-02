# Provisioning Device Client Sample to Register a device using TPM Emulator

## Overview

This is a quick tutorial with the steps to register a device in the Microsoft Azure IoT Hub Device Provisioning Service using the [ProvisioningDeviceClient][povisioning-device-client] on the java SDK.

All the artifacts that you need to execute this sample are ready to be built and executed on this sample.

## References

[Provisioning Device Client - Documentation](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.sdk.iot.provisioning.device._Provisioning_Device_Client)  
[Provisioning Device Client - Source Code][povisioning-device-client]

## TPM Simulator

TPM simulator is located in [here][tpm-simulator]. To start the TPM simulator double click on the [exe][tpm-simulator] and let it run until the end of registration. Registration ID and Endorsement key are valid only for an instance of a run. Visit step 15 below to retrieve endorsement key and registration id.

## How to run the samples on Linux or Windows

Note that the samples for Windows and Linux use Maven.

1. Clone the java SDK repo (https://github.com/Azure/azure-iot-sdk-java.git).
2. Compile the SDK. This step is only necessary if you don't want to use a precompiled Maven package.
    1. Change to the root **azure-iot-sdk-java** directory.
    2. Run `mvn install -DskipTests=true`. It will download all needed packages, compile the SDK.
    3. At the end you will have the `com.microsoft.azure.sdk.iot.provisioning.device` in your local maven repository.
3. Navigate to the sample root `azure-iot-sdk-java/provisioning/provisioning-samples/provisioning-tpm-sample`.
4. Edit the `/src/main/java/samples/com/microsoft/azure/sdk/iot/ProvisioningTpmSample.java` 
   to add your provisioning service information, you must edit:
    ```java
    /*
     * Details of the Provisioning.
     */
    private static final String idScope = "[Your ID scope here]";
    private static final String globalEndpoint = "[Your Provisioning Service Global Endpoint here]";
    private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.HTTPS;
    ```
    Please obtain details of IdScope and globalEndpoint from the [portal][azure-portal] where you have created your service. Follow instructions in step <11> to retreive this information from portal.
    You can choose one of available protocols [HTTPS, AMQP, MQTT, AMQP_WS, MQTT_WS] for registration. 

5. Start the TPM simulator as described in section [TPM Simulator](#TPM-simulator)

6. In a command line, build your sample:
    ```
    {sample root}/>mvn clean install
    ```
7. Navigate to the folder containing the executable JAR file for the sample and run the sample as follows:

    The executable JAR file for registering a device using X509 emulator can be found at:
    ```
    {sample root}/target/provisioning-tpm-sample-{version}-with-deps.jar
    ```

    Navigate to the `target` directory that contains the jar. Run the sample using the following command:
    ```
    java -jar ./provisioning-tpm-sample-{version}-with-deps.jar
    ```
8. Follow the instructions on the prompt for successful registration. 
    ```
    Starting...
    Beginning setup.
    Endorsement Key : 
    <Endorsement key value>
    Registration Id : 
    <Registration Id Value>
    Please visit Azure Portal [azure-portal] and create a TPM Individual Enrollment with the information above i.e EndorsementKey and RegistrationId 
    Press enter when you are ready to run registration after enrolling with the service

    Waiting for Provisioning Service to register
    Waiting for Provisioning Service to register
    Waiting for Provisioning Service to register
    Waiting for Provisioning Service to register
    IotHUb Uri : <hostName>.azure-devices.net
    Device ID : <deviceId>
    ```

## How to create a new provisioning App, step by step

If you prefer to create and populate your own java app, this section will guide you step by step. 

1. If you don't want to use the precompiled Provisioning Device SDK, follow the steps 1 and 2 above to produce it locally on your 
    machine.
2. On your development machine, create a empty folder called `provisioning-getstarted`. That will be the base 
    directories for all provisioning samples that you want to create.
3. Navigate to the `provisioning-getstarted` folder, create a Maven project called `provisioning-tpm-sample`, using 
    the following command at your command prompt.
```
mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=provisioning-tpm-sample -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
```
4. At your command prompt, navigate to the `provisioning-tpm-sample` folder.
5. Using a text editor, open the `pom.xml` file in the `provisioning-tpm-sample` folder and add the following 
    dependency to the **dependencies** node. This dependency enables you to use the **provisioning-device-client** and **tpm-client-emulator**
    package in your app to communicate with your Device Provisioning Service:
    ```java
    <dependencies>
        <dependency>
            <groupId>com.microsoft.azure.sdk.iot.provisioning</groupId>
            <artifactId>provisioning-device-client</artifactId>
            <version>0.0.1</version>
        </dependency>
        <dependency>
            <groupId>com.microsoft.azure.sdk.iot.provisioning.security</groupId>
            <artifactId>tpm-client-emulator</artifactId>
            <version>0.0.1</version>
        </dependency>
    </dependencies>
     ``` 
6. Add the following **build** node after the **dependencies** node. This configuration (1) instructs Maven to use 
    java 1.8 to build the app, (2) create a manifest to point the main entrance of your sample to the App class
    in the App.java, and (3) create a full contained jar, including dependencies:
    ```java
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>2.6</version>
                <configuration>
                    <archive>
                        <manifest>
                            <addClasspath>true</addClasspath>
                            <mainClass>com.mycompany.app.App</mainClass>
                        </manifest>
                    </archive>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.3</version>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                </configuration>
            </plugin>
            <plugin>
                <artifactId>maven-shade-plugin</artifactId>
                <version>2.4</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <filters>
                                <filter>
                                    <artifact>*:*</artifact>
                                    <excludes>
                                        <exclude>META-INF/*.SF</exclude>
                                        <exclude>META-INF/*.RSA</exclude>
                                    </excludes>
                                </filter>
                            </filters>
                            <shadedArtifactAttached>true</shadedArtifactAttached>
                            <shadedClassifierName>with-deps</shadedClassifierName>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
     ``` 
7. Save and close the `pom.xml` file.
8. At this point you can build and run your new **App** to make sure that everything is working as expected.
    1. In a command line, navigate to the sample folder (where the `pom.xml` file lives) and build it:
    ```
    {sample root}/>mvn install
    ```
    2. Navigate to the directory `provisioning-tpm-sample\target` containing the `.jar` file. 
    3. Run the sample using the following command:
    ```
    java -jar ./provisioning-tpm-sample-{version}-SNAPSHOT-with-deps.jar
    ```
    4. As a result, it should print `Hello world!` in the screen.
9. Now, you are ready to write the provisioning code to register a device with service. Using a text editor, open the 
    `provisioning-tpm-sample\src\main\java\com\mycompany\app\App.java` file.
10. Add the following import statements at the beginning of the file, after the `package com.mycompany.app;`:
    ```java
    import com.microsoft.azure.sdk.iot.deps.util.Base64;
    import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientStatus;
    import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceConnectionException;
    import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderTpm;
    import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
    import com.microsoft.azure.sdk.iot.provisioning.security.hsm.SecurityProviderTPMEmulator;
    import com.microsoft.azure.sdk.iot.provisioning.device.*;
    import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
    import java.util.Scanner;
    ```
11. Copy the Global Endpoint and idScope for your Device Provisioning Service from the portal, you will need it for the next step.
    1. Access the [Azure portal][azure-portal].
    2. Navigate to your Device Provisioning Service.
    3. Click on `Overview`.
    4. Copy the value of `Global device endpoint`.
    5. Copy the value of `ID Scope`.
12. Add the following class-level variables to the **App** class. Replace the `[Your ID scope here]` with 
    your Id Scope, the `[Your Provisioning Service Global Endpoint here]` by your Global device endpoint, and the `ProvisioningDeviceClientTransportProtocol.HTTPS` or protocol of your choice.
    ```java
    /*
     * Details of the Provisioning.
     */
    private static final String idScope = "[Your ID scope here]";
    private static final String globalEndpoint = "[Your Provisioning Service Global Endpoint here]";
    private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.HTTPS;
    ```
13. Create a `ProvisioningStatus` inner class which will hold information of the state of registration.
    ```java
    static class ProvisioningStatus
    {
        ProvisioningDeviceClientRegistrationResult provisioningDeviceClientRegistrationInfoClient = new ProvisioningDeviceClientRegistrationResult();
        Exception exception;
    }
    ```
    `provisioningDeviceClientRegistrationInfoClient` will be used to hold the information of the registration with IotHub. Also if there are any problems 
    with registration then exception can be captured in variable `exception`.

14. Add the registration callback, where you will receive your registration information upon completion.
    ```java
        static class ProvisioningDeviceClientRegistrationCallbackImpl implements ProvisioningDeviceClientRegistrationCallback
    {
        @Override
        public void run(ProvisioningDeviceClientRegistrationResult provisioningDeviceClientRegistrationResult, Exception exception, Object context)
        {
            if (context instanceof ProvisioningStatus)
            {
                ProvisioningStatus status = (ProvisioningStatus) context;
                status.provisioningDeviceClientRegistrationInfoClient = provisioningDeviceClientRegistrationResult;
                status.exception = exception;
            }
            else
            {
                System.out.println("Received unknown context");
            }
        }
    }
    ```
    Note : We will send the status object as a callback context in later step as mentioned in <15.b>. During the callback, we will save the registration info or any exceptions if sent by the provisioning device client to the status object.

15. If you do not wish to retrieve registration id and endorsement key to enroll the device on the portal you can skip this step.
Now start the TPM simulator as mentioned in section TPM Simulator above.
Start adding the following to your main.
    ```java
        SecurityProviderTpm securityClientTPMEmulator = null;
        Scanner scanner = new Scanner(System.in);
        try
        {
            securityClientTPMEmulator = new SecurityProviderTPMEmulator();
            System.out.println("Endorsement Key : \n" + new String(encodeBase64(securityClientTPMEmulator.getEndorsementKey())));
            System.out.println("Registration Id : \n" + securityClientTPMEmulator.getRegistrationId());
            System.out.println("Please visit Azure Portal [azure-portal] and create a TPM Individual Enrollment with the information above i.e EndorsementKey and RegistrationId \n" +
                                       "Press enter when you are ready to run registration after enrolling with the service");
            scanner.nextLine();
        }
        catch (SecurityClientException e)
        {
            e.printStackTrace();
        }
    ```
    The above step shall create a new instance of the TPM emulator and provide you with Registration ID and Endorsement Key. After this step you should enroll your device with the above information using [ProvisioningServiceClient][povisioning-single-enrollement-sample] or visiting the [portal][azure-portal]. On the portal 
    1. Navigate to your provisioning service
    2. Go to tab `ManageEnrollmets`
    3. Navigate to `IndividualEnrollments`.
    4. Click add and provide the information from above to create a enrollemnt.

16. Create a new instance of the [ProvisioningDeviceClient][povisioning-device-client]
     replacing the `System.out.println( "Hello World!" );` by:
    ```java
      System.out.println("Starting...");
        System.out.println("Beginning setup.");
        ProvisioningDeviceClient provisioningDeviceClient = null;
        try
        {
            ProvisioningStatus provisioningStatus = new ProvisioningStatus();

            provisioningDeviceClient = ProvisioningDeviceClient.create(globalEndpoint, idScope, PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL, securityClientTPMEmulator );

            provisioningDeviceClient.registerDevice(new ProvisioningDeviceClientRegistrationCallbackImpl(), provisioningStatus);

            while (provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() != ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED)
            {
                if (provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ERROR ||
                        provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_DISABLED ||
                        provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_FAILED )

                {
                    provisioningStatus.exception.printStackTrace();
                    System.out.println("Registration error, bailing out");
                    break;
                }
                System.out.println("Waiting for Provisioning Service to register");
                Thread.sleep(MAX_TIME_TO_WAIT_FOR_REGISTRATION);
            }

            if (provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED)
            {
                System.out.println("IotHUb Uri : " + provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri());
                System.out.println("Device ID : " + provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId());
                // connect to iothub
            }
        }
        catch (ProvisioningDeviceClientException | InterruptedException e)
        {
            System.out.println("Provisioning Device Client threw an exception" + e.getMessage());
            if (provisioningDeviceClient != null)
            {
                provisioningDeviceClient.closeNow();
            }
        }
        provisioningDeviceClient.closeNow();

    ```
    This piece of code does the following :
    a. Creates an instance of provisioning device client providing globalEndpoint, idScope, PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL as updated in step 12 and use the instance of security provider craeted in step 15 i.e.  `securityClientTPMEmulator`. Please follow instructions from [TPM Simulator] on step <15> if you want to retrieve registration id and endorsement key from the simulator.
    b. Calls to register the device by providing a new instance of registration callback created in step 14. Note we are also providing a new status instance as callback context.
    c. Wait for the registration to complete with the service by waiting for the status to change to PROVISIONING_DEVICE_STATUS_ASSIGNED. 
    d. If the registration fails with PROVISIONING_DEVICE_STATUS_ERROR or PROVISIONING_DEVICE_STATUS_DISABLED or PROVISIONING_DEVICE_STATUS_FAILED, an exception will be thrown. This exception can be retrieved from RegistrationCallback.
    e. If registration is successful, the provisioning service will return an IotHub Uri and Device ID, which you can use to connect to IoT Hub. 
    f. Upon completing with registration, you can now close the client.

16. Save and close the `app.java` file.
17. Build and run the **App** as you did in the item *8*.
    1. The **App** should print something like:
    ```
    Starting...
    Beginning setup.
    Endorsement Key : 
    <Endorsement key value>
    Registration Id : 
    <Registration Id Value>
    Please visit Azure Portal [azure-portal] and create a TPM Individual Enrollment with the information above i.e EndorsementKey and RegistrationId 
    Press enter when you are ready to run registration after enrolling with the service

    Waiting for Provisioning Service to register
    Waiting for Provisioning Service to register
    Waiting for Provisioning Service to register
    Waiting for Provisioning Service to register
    IotHUb Uri : <hostName>.azure-devices.net
    Device ID : <deviceId>
    ```

18. Save and close the `app.java` file.
19. Build and run the **App** as you did in the item *8*. Check the results on your console.
20. Upon successful completion of the registration you can now close the TPM simulator if you dont want to proceed to How to connect to your provisioned device, step by step. If not keep the simulator running until end.


## How to connect to your provisioned device, step by step

At this point in the tutorial, you have successfully managed to provision a device to an IoT Hub. Now, you will learn how to connect to that device and have it start sending telemetry.
This part of the tutorial builds on top of the code you wrote in the previous section.

1. Add a dependency from your sample to the Java SDK's Device Client package.
	1. In your current project, find the pom.xml file that you edited earlier. Open that file in your favorite text editor.
	2. In that pom.xml, you will find a section that lists the dependencies of your project. You will need to copy the dependency below and insert it into that section.

    ```
    <dependencies>
		...
        <dependency>
            <groupId>com.microsoft.azure.sdk.iot</groupId>
            <artifactId>iot-device-client</artifactId>
            <version>1.5.37</version>
        </dependency>
		...
	</dependencies>
     ``` 
	 
	 Your pom.xml's dependencies should look like this afterwards:
	 
	 ```
	 <dependencies>
	    <dependency>
            <groupId>com.microsoft.azure.sdk.iot</groupId>
            <artifactId>iot-device-client</artifactId>
            <version>1.5.37</version>
        </dependency>
        <dependency>
            <groupId>com.microsoft.azure.sdk.iot.provisioning</groupId>
            <artifactId>provisioning-device-client</artifactId>
            <version>0.0.1</version>
        </dependency>
        <dependency>
            <groupId>com.microsoft.azure.sdk.iot.provisioning.security</groupId>
            <artifactId>tpm-client-emulator</artifactId>
            <version>0.0.1</version>
        </dependency>
    </dependencies>
	 ```
	 
2. Add logic to connect to your provisioned device and have it send a single message to its hub.
	1. Open up your `app.java` file. Near the top, after the `package com.mycompany.app;` statement, add one more import statement
	
	```java
		import com.microsoft.azure.sdk.iot.device.*;
	```
	
	2. From the previous section, you added some code to your `app.java` file that contained the line:
	
	```java
	// connect to iothub
	```
	
	Replace that comment with the following code:
	
    ```java
    String iotHubUri = provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri();
    String deviceId = provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId();
    DeviceClient deviceClient = null;
    try
    {
        deviceClient = DeviceClient.createFromSecurityProvider(iotHubUri, deviceId, securityClientTPMEmulator, IotHubClientProtocol.MQTT);
        deviceClient.open();
        Message messageToSendFromDeviceToHub =  new Message("Whatever message you would like to send");
        IotHubEventCallback callback = new IotHubEventCallback()
        {
            @Override
            public void execute(IotHubStatusCode responseStatus, Object callbackContext)
            {
                System.out.println("Message received! Response status: " + responseStatus);
            }
        };
                        
        System.out.println("Sending message from provisioned device...");
        deviceClient.sendEventAsync(messageToSendFromDeviceToHub, callback, null);
    }
    catch (IOException e)
    {
        System.out.println("Device client threw an exception: " + e.getMessage());
        if (deviceClient != null)
        {
            deviceClient.closeNow();
        }
    }	
    ```
	
	Note that, when creating the device client instance, you can change which protocol the device client will use for communication. You have the following options:
	
    ```java
    IotHubClientProtocol.MQTT
    IotHubClientProtocol.HTTPS
    IotHubClientProtocol.AMQPS
    IotHubClientProtocol.MQTT_WS
    IotHubClientProtocol.AMQPS_WS
    ```
		 
4. Save and close the `app.java` file.
5. Make sure you start the simulator still running. Then, build and run the **App** as you did in the item *8* in the previous section. Check the results on your console.
6. The console should yield an output like:
```
	Starting...
	Beginning setup.
	Waiting for Provisioning Service to register
	Waiting for Provisioning Service to register
	Waiting for Provisioning Service to register
	Waiting for Provisioning Service to register
	IotHUb Uri : <hostName>.azure-devices.net
	Device ID : <deviceId>
	Sending message from provisioned device...
	Message received! Response status: OK_EMPTY	
```

7. Now you can close the simulator when you are done running your sample.


[azure-portal]: https://www.portal.azure.com
[povisioning-device-client]:https://github.com/Azure/azure-iot-sdk-java/tree/main/provisioning/provisioning-device-client
[povisioning-service-client]:https://github.com/Azure/azure-iot-sdk-java/tree/main/provisioning/provisioning-service-client
[povisioning-single-enrollment-sample]:https://github.com/Azure/azure-iot-sdk-java/tree/main/provisioning/provisioning-samples/service-enrollment-sample
[povisioning-group-enrollment-sample]:https://github.com/Azure/azure-iot-sdk-java/tree/main/provisioning/provisioning-samples/service-enrollment-group-sample
[tpm-simulator]:../../provisioning-tools/tpm-simulator/Simulator.exe