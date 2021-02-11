# Provisioning Service Client Sample For EnrollmentGroup

## Overview

This is a quick tutorial with the steps to create, get, query, and delete an EnrollmentGroup in the Microsoft Azure IoT 
Hub Device Provisioning Service using the [Provisioning Service Client](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.sdk.iot.provisioning.service.provisioningserviceclient?view=azure-java-stable)
on the java SDK.

All the artifacts that you need to execute this sample are ready to be built and executed on this sample.

## References

[Provisioning service client documentation](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.sdk.iot.provisioning.service.provisioningserviceclient?view=azure-java-stable)
[Provisioning service client source code](https://github.com/Azure/azure-iot-sdk-java/tree/master/provisioning/provisioning-service-client)

## How to run the samples on Linux or Windows

Note that the samples for Windows and Linux use Maven.

1. Clone the java SDK repo (https://github.com/Azure/azure-iot-sdk-java.git).
2. Compile the SDK. This step is only necessary if you don't want to use a precompiled Maven package.
    1. Change to the root **azure-iot-sdk-java** directory.
    2. Run `mvn install`. It will download all needed packages, compile and test the SDK.
    3. At the end of this step, you will have the `com.microsoft.azure.sdk.iot.provisioning.service` on your machine.
3. Navigate to the sample root `azure-iot-sdk-java/provisioning/provisioning-samples/service-enrollment-group-sample`.
4. Edit the `/src/main/java/samples/com/microsoft/azure/sdk/iot/ServiceEnrollmentGroupSample.java` 
   to add your provisioning service information, you must edit:
    1. Replace the `[Provisioning Connection String]` by the Provisioning Connection String that you copied from the 
        portal.
        ```java
        /*
         * Details of the Provisioning.
         */
        private static final String PROVISIONING_CONNECTION_STRING = "[Provisioning Connection String]";
        ```
    2. You must copy the root certificate for the group of devices. If you don't have it, you can use the 
        [provisioning X509 cert generator](https://github.com/Azure/azure-iot-sdk-java/tree/master/provisioning/provisioning-tools/provisioning-x509-cert-generator).
        Fill the `PUBLIC_KEY_CERTIFICATE_STRING` with the root certificate. Be careful to do **not** change your
        certificate, _adding_ or _removing_ characters like spaces, tabs or new lines (`\n`).
        ```java
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
    3. Optionally, fill the `IOTHUB_HOST_NAME` with the Iot Hub that you linked to your provisioning service.
        ```java
        private static final String IOTHUB_HOST_NAME = "[Host name].azure-devices.net";
        ```
       **Note:** If you will not provide these parameters, you must **remove** the lines #62 and #63, which add it to 
       the enrollmentGroup configuration, from your sample.
        ```java
        enrollmentGroup.setIotHubHostName(IOTHUB_HOST_NAME);                // Optional parameter.
        enrollmentGroup.setProvisioningStatus(ProvisioningStatus.ENABLED);  // Optional parameter.
        ```
5. In a command line, navigate to the directory `azure-iot-sdk-java/provisioning/provisioning-samples/service-enrollment-group-sample` 
    where the `pom.xml` file for this test lives, and build your sample:
    ```
    {sample root}/>mvn install -DskipTests
    ```
6. Navigate to the folder containing the executable JAR file for the sample and run the sample as follows:

    The executable JAR file for create an enrollmentGroup can be found at:
    ```
    {sample root}/target/service-enrollment-group-sample-{version}-with-deps.jar
    ```

    Navigate to the `target` directory that containing the jar. Run the sample using the following command:
    ```
    java -jar ./service-enrollment-group-sample-{version}-with-deps.jar
    ```

## How to create a new provisioning App, step by step

If you prefer to create and populate your own java app, this section will guide you step by step. 

1. If you don't want to use the precompiled Service SDK, follow the steps [1 and 2](#how-to-run-the-samples-on-linux-or-windows) above to produce it 
    locally on your machine.
2. On your development machine, create a empty folder called `provisioning-getstarted`. That will be the base 
    directory for all provisioning samples that you want to create.
3. Navigate to the `provisioning-getstarted` folder, create a Maven project called `service-enrollment-group-sample`, 
    using the following command at your command prompt. Note this is a single, long command:
```
mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=service-enrollment-group-sample -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
```
4. At your command prompt, navigate to the `service-enrollment-group-sample` folder.
5. Using a text editor, open the `pom.xml` file in the `service-enrollment-group-sample` folder and add the following 
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
    2. Navigate to the directory `service-enrollment-group-sample\target` containing the `.jar` file. 
    3. Run the sample using the following command:
    ```
    java -jar ./service-enrollment-group-sample-1.0-SNAPSHOT-with-deps.jar
    ```
    4. As a result, it should print `Hello world!` in the screen.
9. Now, you are ready to write the provisioning code to create the enrollmentGroup. Using a text editor, open the 
    `service-enrollment-group-sample\src\main\java\com\mycompany\app\App.java` file.
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
12. Add the following class-level variables to the **App** class. Replace the `{Provisioning Connection String}` with 
    your provisioning connection string:
    ```java
    /*
     * Details of the Provisioning.
     */
    private static final String PROVISIONING_CONNECTION_STRING = "{Provisioning Connection String}";
    ```
13. You must copy the root certificate for the group of devices. If you don't have it, you can use the 
    [provisioning X509 cert generator](https://github.com/Azure/azure-iot-sdk-java/tree/master/provisioning/provisioning-tools/provisioning-x509-cert-generator).
    Add the follow class-level variable to the **App**, and fill the `PUBLIC_KEY_CERTIFICATE_STRING` with the 
    root certificate.
    ```java
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
14. Update your `main` method signature to include the following `throws` clause:
    ```java
    public static void main(String[] args) throws ProvisioningServiceClientException
    ```
15. Create a new instance of the [ProvisioningServiceClient](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.sdk.iot.provisioning.service.provisioningserviceclient?view=azure-java-stable)
     replacing the `System.out.println( "Hello World!" );` by:
    ```java
     System.out.println("Beginning my sample for the Provisioning Service Client!");

     // ********************************** Create a Provisioning Service Client ************************************
     ProvisioningServiceClient provisioningServiceClient =
             ProvisioningServiceClient.createFromConnectionString(PROVISIONING_CONNECTION_STRING);
    ```
16. After that, you can create a new enrollmentGroup. 
    1. Every enrollmentGroup needs a unique name, called **EnrollmentGroupId**, and an **[Attestation](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.sdk.iot.provisioning.service.configs._Attestation)**
        that must be a root certificate on [X509Attestation](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.sdk.iot.provisioning.service.configs._X509_Attestation).
    2. Once you defined the EnrollmentGroupId and the Attestation mechanism, you can create the [EnrollmentGroup](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.sdk.iot.provisioning.service.configs._EnrollmentGroup)
        configuration. Other *optional* parameters can be added, for instance, the  **IotHubHostName**, and the 
        **ProvisioningStatus**.
        ```java
        // *************************************** Create a new enrollmentGroup ****************************************
        System.out.println("\nCreate a new enrollmentGroup...");

        String enrollmentGroupId = "enrollmentgroupid-" + UUID.randomUUID();
        Attestation attestation = X509Attestation.createFromRootCertificates(PUBLIC_KEY_CERTIFICATE_STRING);

        EnrollmentGroup enrollmentGroup =
                new EnrollmentGroup(
                        enrollmentGroupId,
                        attestation);
        enrollmentGroup.setIotHubHostName("[Host name].azure-devices.net");    // Optional, remove if you don't need. Must fit the linked IoT Hub.
        enrollmentGroup.setProvisioningStatus(ProvisioningStatus.ENABLED);     // Optional, remove if you don't need.
        ```
    3. Now, call the [createOrUpdateEnrollmentGroup](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.sdk.iot.provisioning.service.provisioningserviceclient.createorupdateenrollmentgroup?view=azure-java-stable#com_microsoft_azure_sdk_iot_provisioning_service_ProvisioningServiceClient_createOrUpdateEnrollmentGroup_EnrollmentGroup_)
        on the ProvisioningServiceClient to create a new enrollmentGroup.
        ```java
        // *************************************** Create a new enrollment group ***************************************
        System.out.println("\nAdd new enrollmentGroup...");
        EnrollmentGroup enrollmentGroupResult =  provisioningServiceClient.createOrUpdateEnrollmentGroup(enrollmentGroup);
        System.out.println("\nEnrollmentGroup created with success...");
        System.out.println(enrollmentGroupResult);
        ```
17. Save and close the `app.java` file.
18. Build and run the **App** as you did in the item *8*.
    1. The **App** should print something like:
        ```
        Beginning my sample for the Provisioning Service Client!
    
        Create new enrollmentGroup config...
    
        Add new enrollmentGroup...
        
        EnrollmentGroup created with success...
        {
            "enrollmentGroupId":"enrollmentgroupid-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
            "attestation":{
                "type":"x509",
                "signingCertificates":{
                    "primary":{
                        "info": {
                            "subjectName": "CN=ROOT_xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx, OU=Azure IoT, O=MSFT, C=US",
                            "sha1Thumbprint": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
                            "sha256Thumbprint": "validEnrollmentGroupId",
                            "issuerName": "CN=ROOT_xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx, OU=Azure IoT, O=MSFT, C=US",
                            "notBeforeUtc": "2017-11-14T12:34:18Z",
                            "notAfterUtc": "2017-11-20T12:34:18Z",
                            "serialNumber": "xxxxxxxxxxxxxxxxxx",
                            "version": 3
                        }
                    }
                }
            },
            "iotHubHostName":"ContosoIoTHub.azure-devices.net",
            "provisioningStatus":"enabled",
            "etag": "\"xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx\""
        }
        ```
    2. Check in the Azure Portal if your enrollmentGroup was created with success.
    3. Delete the EnrollmentGroup using the Portal, or create a new certificate for the next test.
19. Check the created enrollmentGroup information in your **App**. You can consult using 2 ProvisioningServiceClient APIs, 
    `get` and `query`.
    1. Use the [getEnrollmentGroup](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.sdk.iot.provisioning.service.provisioningserviceclient?view=azure-java-stable.getenrollmentgroup#com_microsoft_azure_sdk_iot_provisioning_service__Provisioning_Service_Client_getEnrollmentGroup_String_)
        to **get** an specific enrollmentGroup using the EnrollmentGroupId. Add the following code.
        ```java
        // **************************************** Get info of enrollmentGroup ****************************************
        System.out.println("\nGet the enrollmentGroup information...");
        EnrollmentGroup getResult = provisioningServiceClient.getEnrollmentGroup(enrollmentGroupId);
        System.out.println(getResult);
        ```
    2. Use the [createEnrollmentGroupQuery](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.sdk.iot.provisioning.service.provisioningserviceclient?view=azure-java-stable.createenrollmentgroupquery#com_microsoft_azure_sdk_iot_provisioning_service__Provisioning_Service_Client_createEnrollmentGroupQuery_QuerySpecification_)
       to create a **query** for the enrollmentGroups in the provisioning service. The [QuerySpecificationBuilder](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.sdk.iot.provisioning.service.configs._Query_Specification_Builder)
       will help you to create a correct [QuerySpecification](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.sdk.iot.provisioning.service.configs._Query_Specification).
       For this sample, we will query all **`"*"`** enrollmentGroups is the provisioning service.
        ```java
        // *************************************** Query info of enrollmentGroup ***************************************
        System.out.println("\nCreate a query for the enrollmentGroups...");
        QuerySpecification querySpecification =
                new QuerySpecificationBuilder("*", QuerySpecificationBuilder.FromType.ENROLLMENT_GROUPS)
                        .createSqlQuery();
        Query query = provisioningServiceClient.createEnrollmentGroupQuery(querySpecification);

        while(query.hasNext())
        {
            System.out.println("\nQuery the next enrollmentGroups...");
            QueryResult queryResult = query.next();
            System.out.println(queryResult);
        }
        ```
20. Delete the enrollmentGroup from the provisioning service. You can delete the enrollmentGroup adding the following 
    code that invokes the API [deleteEnrollmentGroup](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.sdk.iot.provisioning.service.provisioningserviceclient?view=azure-java-stable.deleteenrollmentgroup#com_microsoft_azure_sdk_iot_provisioning_service__Provisioning_Service_Client_deleteEnrollmentGroup_String_):
    ```java
    // ************************************** Delete info of enrollmentGroup ***************************************
    System.out.println("\nDelete the enrollmentGroup...");
    provisioningServiceClient.deleteEnrollmentGroup(enrollmentGroupId);
    ```
21. Save and close the `app.java` file.
22. Build and run the **App** as you did in the item *8*. Check the results on your console. Note that you will **not** 
    see any new enrollmentGroup in the Azure Portal, because we are deleting it in the item *19*.
