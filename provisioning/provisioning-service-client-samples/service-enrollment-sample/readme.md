# Provisioning Service Client Sample For Individual Enrollment

## Overview

This is a quick tutorial with the steps to create, get, query, and delete an individualEnrollment in the Microsoft Azure IoT Hub 
Device Provisioning Service using the [ProvisioningServiceClient](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.sdk.iot.provisioning.service.provisioningserviceclient?view=azure-java-stable)
on the java SDK.

All the artifacts that you need to execute this sample are ready to be built and executed on this sample.

## References

[Provisioning service client - documentation](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.sdk.iot.provisioning.service.provisioningserviceclient?view=azure-java-stable)
[Provisioning service client - source code](https://github.com/Azure/azure-iot-sdk-java/tree/main/provisioning/provisioning-service-client)

## How to run the samples on Linux or Windows

Note that the samples for Windows and Linux use Maven.

1. Clone the java SDK repo (https://github.com/Azure/azure-iot-sdk-java.git).
2. Compile the SDK. This step is only necessary if you don't want to use a precompiled Maven package.
    1. Change to the root **azure-iot-sdk-java** directory.
    2. Run `mvn install`. It will download all needed packages, compile and test the SDK.
    3. At the end of this step, you will have the `com.microsoft.azure.sdk.iot.provisioning.service` on your machine.
3. Navigate to the sample root `azure-iot-sdk-java/provisioning/provisioning-samples/service-enrollment-sample`.
4. Edit the `/src/main/java/samples/com/microsoft/azure/sdk/iot/ServiceEnrollmentSample.java` 
   to add your provisioning service information:
    1. Replace the `[Provisioning Connection String]` by the Provisioning Connection String that you copied from the 
        portal.
        ```java
        /*
         * Details of the Provisioning.
         */
        private static final String PROVISIONING_CONNECTION_STRING = "[Provisioning Connection String]";
        ```
        
    2. For **TPM** attestation:
        1. From the device that you have, you must copy the registrationId and the endorsementKey. If you don't have 
            a physical device with TPM, you can use the [tpm-simulator](https://github.com/Azure/azure-iot-sdk-java/tree/main/provisioning/provisioning-tools/tpm-simulator).
        2. Fill the `REGISTRATION_ID` and the `TPM_ENDORSEMENT_KEY` with the device information.
            ```java
            private static final String REGISTRATION_ID = "[RegistrationId]";
            private static final String TPM_ENDORSEMENT_KEY = "[TPM Endorsement Key]";
            ```
        3. Optionally, provide other parameters like `IOTHUB_HOST_NAME`, `DEVICE_ID`, and `PROVISIONING_STATUS`. The 
            `IOTHUB_HOST_NAME` must fit one of the IoT Hubs that you linked to your provisioning service.
            ```java
            // Optional parameters
            private static final String IOTHUB_HOST_NAME = "[Host name].azure-devices.net";
            private static final String DEVICE_ID = "myJavaDevice";
            private static final ProvisioningStatus PROVISIONING_STATUS = ProvisioningStatus.ENABLED;
            ```
           **Note:** If you will not provide these parameters, you must **remove** the lines #45 to #48, which add it to 
           the individualEnrollment configuration, from your sample.
            ```java
            // The following parameters are optional. Remove it if you don't need.
            individualEnrollment.setDeviceId(DEVICE_ID);
            individualEnrollment.setIotHubHostName(IOTHUB_HOST_NAME);
            individualEnrollment.setProvisioningStatus(PROVISIONING_STATUS);
            ```

    3. For **X509** attestation:
        1. From the device that you have, you must copy the registrationId and the client certificate. If you don't have 
            a physical device with X509, you can use the [provisioning X509 cert generator](https://github.com/Azure/azure-iot-sdk-java/tree/main/provisioning/provisioning-tools/provisioning-x509-cert-generator).
            Answer `Y` to provide your common name, the Client Cert commonName is your registrationId.
        2. Fill the `REGISTRATION_ID` with the device commonName, and replace the `TPM_ENDORSEMENT_KEY` by the
            `PUBLIC_KEY_CERTIFICATE_STRING` that contains your client certificate. Be careful to do **not** change your
            certificate, _adding_ or _removing_ characters like spaces, tabs or new lines (`\n`).
            ```java
            private static final String REGISTRATION_ID = "[RegistrationId]";
            private static final String PUBLIC_KEY_CERTIFICATE_STRING =
                    "-----BEGIN CERTIFICATE-----\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "-----END CERTIFICATE-----\n";
            ```
        3. Replace the Attestation mechanism in the sample, line #41, to work with X509 client certificate instead of TPM.

            **Replace:**
            ```java
            Attestation attestation = new TpmAttestation(device.getValue());
            ```

            **By:**
            ```java
            Attestation attestation = X509Attestation.createFromClientCertificates(device.getValue());
            ```
            
5. In a command line, navigate to the directory `azure-iot-sdk-java/provisioning/provisioning-samples/service-enrollment-sample` 
    where the `pom.xml` file for this test lives, and build your sample:
    ```
    {sample root}/>mvn install -DskipTests
    ```
6. Navigate to the folder containing the executable JAR file for the sample and run the sample as follows:

    The executable JAR file for create a IndividualEnrollment can be found at:
    ```
    {sample root}/target/service-enrollment-sample-{version}-with-deps.jar
    ```

    Navigate to the `target` directory that containing the jar. Run the sample using the following command:
    ```
    java -jar ./service-enrollment-sample-{version}-with-deps.jar
    ```

## How to create a new provisioning App, step by step

If you prefer to create and populate your own java app, this section will guide you step by step. 

1. If you don't want to use the precompiled Service SDK, follow the steps [1 and 2](#how-to-run-the-samples-on-linux-or-windows) above to produce it 
    locally on your machine.
2. On your development machine, create a empty folder called `provisioning-getstarted`. That will be the base 
    directory for all provisioning samples that you want to create.
3. Navigate to the `provisioning-getstarted` folder, create a Maven project called `service-enrollment-sample`, using 
    the following command at your command prompt. Note this is a single, long command:
```
mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=service-enrollment-sample -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
```
4. At your command prompt, navigate to the `service-enrollment-sample` folder.
5. Using a text editor, open the `pom.xml` file in the `service-enrollment-sample` folder and add the following 
    dependency to the **dependencies** node. This dependency enables you to use the **provisioning-service-client** 
    package in your app to communicate with your Device Provisioning Service:
    ```java
    <dependencies>
        <dependency>
            <groupId>com.microsoft.azure.sdk.iot.provisioning</groupId>
            <artifactId>provisioning-service-client</artifactId>
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
    2. Navigate to the directory `service-enrollment-sample\target` containing the `.jar` file. 
    3. Run the sample using the following command:
    ```
    java -jar ./service-enrollment-sample-1.0-SNAPSHOT-with-deps.jar
    ```
    4. As a result, it should print `Hello world!` in the screen.
9. Now, you are ready to write the provisioning code to create the IndividualEnrollment. Using a text editor, open the 
    `service-enrollment-sample\src\main\java\com\mycompany\app\App.java` file.
10. Add the following import statements at the beginning of the file, after the `package com.mycompany.app;`:
    ```java
    import com.microsoft.azure.sdk.iot.provisioning.service.ProvisioningServiceClient;
    import com.microsoft.azure.sdk.iot.provisioning.service.Query;
    import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
    import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;
    
    import java.util.UUID;
    ```
11. Copy the connection string for your Device Provisioning Service from the portal, you will need it for the next step.
    1. Access the [Azure portal](http://portal.azure.com/).
    2. Navigate to your Device Provisioning Service.
    3. Click on `Shared access policies`.
    4. Select the desired `POLICY`. It should be the one that you have `EnrollmentWrite` permission.
    5. Copy the `Primary key connection string`.
12. Add the following class-level variable to the **App** class. Replace the `{Provisioning Connection String}` with 
    your provisioning connection string:
    ```java
    /*
     * Details of the Provisioning.
     */
    private static final String PROVISIONING_CONNECTION_STRING = "{Provisioning Connection String}";
    ```
13. For **TPM** attestation:
    1. From the device that you have, you must copy the registrationId and the endorsementKey. If you don't have 
        a physical device with TPM, you can use the [tpm-simulator](https://github.com/Azure/azure-iot-sdk-java/tree/main/provisioning/provisioning-tools/tpm-simulator).
    2. Add the follow class-level variable to the **App**, and fill the `REGISTRATION_ID` with the registrationId and 
        the `TPM_ENDORSEMENT_KEY` with the endorsementKey that you copied from the hardware or emulator.
        ```java
        private static final String REGISTRATION_ID = "[RegistrationId]";
        private static final String TPM_ENDORSEMENT_KEY = "[TPM Endorsement Key]";
        ```
14. For **X509** attestation:
    1. From the device that you have, you must copy the registrationId and the client certificate. If you don't have 
        a physical device with X509, you can use the [provisioning X509 cert generator](https://github.com/Azure/azure-iot-sdk-java/tree/main/provisioning/provisioning-tools/provisioning-x509-cert-generator).
        Answer `Y` to provide your common name, the Client Cert commonName is your registrationId.
    2. Add the follow class-level variable to the **App**, and fill the `REGISTRATION_ID` with the registrationId and 
        the `PUBLIC_KEY_CERTIFICATE_STRING` with the client certificate that you copied from the hardware or emulator.
        ```java
        private static final String REGISTRATION_ID = "[RegistrationId]";
        private static final String PUBLIC_KEY_CERTIFICATE_STRING =
                "-----BEGIN CERTIFICATE-----\n" +
                "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                "-----END CERTIFICATE-----\n";
        ```
15. Optionally, you can provide some extra parameters to your individualEnrollment, 
    ```java
    // Optional parameters
    private static final String IOTHUB_HOST_NAME = "[Host name].azure-devices.net";
    private static final String DEVICE_ID = "myJavaDevice";
    private static final ProvisioningStatus PROVISIONING_STATUS = ProvisioningStatus.ENABLED;
    ```
16. Update your `main` method signature to include the following `throws` clause:
    ```java
    public static void main(String[] args) throws ProvisioningServiceClientException
    ```
17. Create a new instance of the [ProvisioningServiceClient](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.sdk.iot.provisioning.service.provisioningserviceclient?view=azure-java-stable)
    replacing the `System.out.println( "Hello World!" );` by:
    ```java
     System.out.println("Beginning my sample for the Provisioning Service Client!");

     // ********************************** Create a Provisioning Service Client ************************************
     ProvisioningServiceClient provisioningServiceClient =
             ProvisioningServiceClient.createFromConnectionString(PROVISIONING_CONNECTION_STRING);
    ```
18. After that, you can create a new IndividualEnrollment. 
    1. Every individualEnrollment needs a unique name, called **RegistrationId**, and an **[Attestation](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.sdk.iot.provisioning.service.configs._Attestation)**
        that can be [TpmAttestation](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.sdk.iot.provisioning.service.configs._Tpm_Attestation)
        or [X509Attestation](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.sdk.iot.provisioning.service.configs._X509_Attestation).
        For this sample, we will use TPM.
    2. Once you defined the RegistrationId and the Attestation mechanism, you can create the [Enrollment](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.sdk.iot.provisioning.service.configs._Enrollment)
        configuration. Other *optional* parameters can be added, for instance, the **DeviceID**, the 
        **IotHubHostName**, and the **ProvisioningStatus**, if you don't want the optional parameters, just remove
        it from the following code.
        ```java
        // ******************************** Create a new individual enrollment config **********************************
        System.out.println("\nCreate a new individualEnrollment...");
        Attestation attestation = new TpmAttestation(TPM_ENDORSEMENT_KEY);
        IndividualEnrollment individualEnrollment =
                new IndividualEnrollment(
                        REGISTRATION_ID,
                        attestation);

        // The following parameters are optional. Remove it if you don't need.
        individualEnrollment.setDeviceId(DEVICE_ID);
        individualEnrollment.setIotHubHostName(IOTHUB_HOST_NAME);
        individualEnrollment.setProvisioningStatus(PROVISIONING_STATUS);
        ```
    3. For **X509**, replace the Attestation mechanism in the preview code, to work with X509 client certificate 
        instead of TPM.  
        **Replace:**
        ```java
        Attestation attestation = new TpmAttestation(TPM_ENDORSEMENT_KEY);
        ```
        **By:**
        ```java
        Attestation attestation = X509Attestation.createFromClientCertificates(PUBLIC_KEY_CERTIFICATE_STRING);
        ```
    4. Now, call the [createOrUpdateIndividualEnrollment](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.sdk.iot.provisioning.service.provisioningserviceclient.createorupdateindividualenrollment?view=azure-java-stable#com_microsoft_azure_sdk_iot_provisioning_service_ProvisioningServiceClient_createOrUpdateIndividualEnrollment_IndividualEnrollment_)
        on the ProvisioningServiceClient to create a new IndividualEnrollment.
        ```java
        // ************************************ Create the individualEnrollment *************************************
        System.out.println("\nAdd new individualEnrollment...");
        IndividualEnrollment individualEnrollmentResult =  provisioningServiceClient.createOrUpdateIndividualEnrollment(individualEnrollment);
        System.out.println("\nIndividualEnrollment created with success...");
        System.out.println(individualEnrollmentResult.toString());
        ```
19. Save and close the `app.java` file.
20. Build and run the **App** as you did in the item *8*.
    1. The **App** should print something like:
        ```
        Beginning my sample for the Provisioning Service Client!
    
        Create new individualEnrollment config...
    
        Add new individualEnrollment...
        
        IndividualEnrollment created with success...
        {
          "registrationId": "myRegistrationId",
          "deviceId": "myJavaDevice",
          "attestation": {
            "type": "tpm",
            "tpm": {
              "endorsementKey": "myEndorsementKey"
            }
          },
          "iotHubHostName": "contoso-iothub.azure-devices.net",
          "provisioningStatus": "enabled",
          "etag": "\"xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx\""
        }
        ```
    2. Check in the Azure Portal if your IndividualEnrollment was created with success.
    3. Delete the IndividualEnrollment using the Portal, or create a new certificate or endorsement key for the next test.
21. Check the created individualEnrollment information in your **App**. You can consult using 2 ProvisioningServiceClient APIs, 
    `get` and `query`.
    1. Use the [getIndividualEnrollment](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.sdk.iot.provisioning.service.provisioningserviceclient?view=azure-java-stable.getindividualenrollment#com_microsoft_azure_sdk_iot_provisioning_service__Provisioning_Service_Client_getIndividualEnrollment_String_)
        to **get** an specific individualEnrollment using the registrationId. Add the following code and check the result.
        ```java
        // ************************************* Get info of individualEnrollment *************************************
        System.out.println("\nGet the individualEnrollment information...");
        IndividualEnrollment getResult = provisioningServiceClient.getIndividualEnrollment(registrationId);
        System.out.println(getResult.toString());
        ```
    2. Use the [createIndividualEnrollmentQuery](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.sdk.iot.provisioning.service.provisioningserviceclient?view=azure-java-stable.createindividualenrollmentquery#com_microsoft_azure_sdk_iot_provisioning_service__Provisioning_Service_Client_createIndividualEnrollmentQuery_QuerySpecification_)
        to create a **query** for the individual enrollments in the provisioning service. The [QuerySpecificationBuilder](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.sdk.iot.provisioning.service.configs._Query_Specification_Builder)
        will help you to create a correct [QuerySpecification](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.sdk.iot.provisioning.service.configs._Query_Specification).
        For this sample, we will query all **`"*"`** individualEnrollments is the provisioning service.
        ```java
        // ************************************ Query info of individualEnrollment ************************************
        System.out.println("\nCreate a query for individualEnrollments...");
        QuerySpecification querySpecification = 
               new QuerySpecificationBuilder("*", QuerySpecificationBuilder.FromType.ENROLLMENTS)
                       .createSqlQuery();
        Query query = provisioningServiceClient.createIndividualEnrollmentQuery(querySpecification);

        while(query.hasNext())
        {
            System.out.println();
            System.out.println("Query the next individualEnrollments...");
            QueryResult queryResult = query.next();
            System.out.println(queryResult.toString());
        }    
        ```
22. Delete the individualEnrollment from the provisioning service. You can delete the individualEnrollment adding the following code that 
    invokes the API [deleteIndividualEnrollment](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.sdk.iot.provisioning.service.provisioningserviceclient?view=azure-java-stable.deleteindividualenrollment#com_microsoft_azure_sdk_iot_provisioning_service__Provisioning_Service_Client_deleteIndividualEnrollment_String_):
    ```java
    // *********************************** Delete info of individualEnrollment ************************************
    System.out.println("\nDelete the individualEnrollment...");
    provisioningServiceClient.deleteIndividualEnrollment(registrationId);
    ```
23. Save and close the `app.java` file.
24. Build and run the **App** as you did in the item *8*. Check the results on your console. Note that you will **not** 
    see any new individualEnrollment in the Azure Portal, because we are deleting it in the item *19*.
